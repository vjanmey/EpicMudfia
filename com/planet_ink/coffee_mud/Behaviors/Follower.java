package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Follower extends ActiveTicker
{
	@Override public String ID(){return "Follower";}
	@Override protected int canImproveCode(){return Behavior.CAN_ITEMS|Behavior.CAN_MOBS;}
	protected boolean realFollow=false;
	protected boolean noFollowers=false;
	protected boolean inventory=false;
	protected int lastNumPeople=-1;
	protected Room lastRoom=null;
	protected MOB lastOwner=null;

	int direction=-1;

	public Follower()
	{
		super();
		minTicks=0;
		maxTicks=0;
		direction=-1;
	}

	@Override
	public void setParms(String newParms)
	{
		minTicks=0;
		maxTicks=0;
		chance=100;
		super.setParms(newParms);
		final Vector<String> V=CMParms.parse(newParms.toUpperCase());
		realFollow=V.contains("GROUP");
		noFollowers=V.contains("NOFOLLOWERS");
		inventory=V.contains("INVENTORY")||V.contains("INV");
	}

	@Override
	public String accountForYourself()
	{
		return "natural friendly following";
	}

	@Override
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);

		final MOB mob=msg.source();
		if(mob.amDead()) return;
		if(mob.location()==null) return;

		if(affecting instanceof MOB)
		{
			if((!canFreelyBehaveNormal(affecting))||(realFollow))
				return;

			if((direction<0)
			&&(msg.amITarget(((MOB)affecting).location()))
			&&(CMLib.flags().canBeSeenBy(mob,(MOB)affecting))
			&&(msg.othersMessage()!=null)
			&&((msg.targetMinor()==CMMsg.TYP_LEAVE)
			 ||(msg.targetMinor()==CMMsg.TYP_FLEE))
			&&(CMLib.masking().maskCheck(getParms(),mob,false))
			&&(CMLib.dice().rollPercentage()<chance))
			{
				String directionWent=msg.othersMessage();
				final int x=directionWent.lastIndexOf(' ');
				if(x>=0)
				{
					directionWent=directionWent.substring(x+1);
					direction=Directions.getDirectionCode(directionWent);
				}
				else
					direction=-1;
			}
		}
	}

	public MOB pickRandomMOBHere(Environmental ticking, Room room)
	{
		if(room==null) return null;
		if((room.numInhabitants()!=lastNumPeople)
		||(room!=lastRoom))
		{
			lastNumPeople=room.numInhabitants();
			lastRoom=room;
			for(int i=0;i<room.numInhabitants();i++)
			{
				final MOB M=room.fetchInhabitant(i);
				if((M!=null)
				&&(M!=ticking)
				&&(!CMSecurity.isAllowed(M,room,CMSecurity.SecFlag.CMDMOBS))
				&&(!CMSecurity.isAllowed(M,room,CMSecurity.SecFlag.CMDROOMS))
				&&(CMLib.masking().maskCheck(getParms(),M,false)))
					return M;
			}
		}
		return null;
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(!super.okMessage(host,msg))
			return false;
		if((host instanceof Item)
		&&(msg.tool()==host)
		&&(msg.sourceMinor()==CMMsg.TYP_SELL))
		{
			msg.source().tell(_("You can not sell @x1.",host.name()));
			return false;
		}
		return true;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if((ticking instanceof Item)
		&&((lastOwner==null)
		   ||((!inventory)&&(!CMLib.flags().isInTheGame(lastOwner,false)))))
		{
			final Item I=(Item)ticking;
			if((I.owner()!=null)
			&&(I.owner() instanceof MOB)
			&&(CMLib.masking().maskCheck(getParms(),I.owner(),false))
			&&(!CMSecurity.isAllowed((MOB)I.owner(),((MOB)I.owner()).location(),CMSecurity.SecFlag.CMDMOBS))
			&&(!CMSecurity.isAllowed((MOB)I.owner(),((MOB)I.owner()).location(),CMSecurity.SecFlag.CMDROOMS)))
				lastOwner=(MOB)I.owner();
			else
			if(!inventory)
			{
				final MOB M=pickRandomMOBHere(I,CMLib.map().roomLocation(I));
				if(M!=null) lastOwner=M;
			}
		}

		if(!canAct(ticking,tickID))
			return true;

		if(ticking instanceof MOB)
		{
			if(tickID!=Tickable.TICKID_MOB)
				return true;
			if(!canFreelyBehaveNormal(ticking))
				return true;
			final MOB mob=(MOB)ticking;
			final Room room=mob.location();
			if((noFollowers)&&(mob.numFollowers()>0))
				return true;
			if(realFollow)
			{
				if(mob.amFollowing()==null)
				{
					final MOB M=pickRandomMOBHere(mob,room);
					if(M!=null)
						CMLib.commands().postFollow(mob,M,false);
				}
			}
			else
			if(direction>=0)
			{
				final Room otherRoom=room.getRoomInDir(direction);

				if(otherRoom!=null)
				{
					if(!otherRoom.getArea().Name().equals(room.getArea().Name()))
						direction=-1;
				}
				else
					direction=-1;

				if(direction<0)
					return true;

				boolean move=true;
				for(int m=0;m<room.numInhabitants();m++)
				{
					final MOB inhab=room.fetchInhabitant(m);
					if((inhab!=null)
					&&(CMSecurity.isAllowed(inhab,room,CMSecurity.SecFlag.CMDMOBS)
					   ||CMSecurity.isAllowed(inhab,room,CMSecurity.SecFlag.CMDROOMS)))
						move=false;
				}
				if(move)
					CMLib.tracking().walk(mob,direction,false,false);
				direction=-1;
			}
		}
		else
		if((ticking instanceof Item)
		&&(lastOwner!=null)
		&&(lastOwner.location()!=null))
		{
			final Item I=(Item)ticking;
			if(I.container()!=null) I.setContainer(null);

			final Room R=CMLib.map().roomLocation(I);
			if(R==null)	return true;

			if(R!=lastOwner.location())
				lastOwner.location().moveItemTo(I,ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
			if((inventory)&&(R.isInhabitant(lastOwner)))
			{
				CMLib.commands().postGet(lastOwner,null,I,true);
				if(!lastOwner.isMine(I))
				{
					lastOwner.moveItemTo(I);
					if(lastOwner.location()!=null)
						lastOwner.location().recoverRoomStats();
				}
			}

		}
		return true;
	}
}
