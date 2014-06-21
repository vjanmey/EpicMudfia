package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

@SuppressWarnings("rawtypes")
public class Sinking extends StdAbility
{
	@Override public String ID() { return "Sinking"; }
	private final static String localizedName = CMLib.lang()._("Sinking");
	@Override public String name() { return localizedName; }
	@Override public String displayText(){ return "";}
	@Override protected int canAffectCode(){return CAN_ITEMS|Ability.CAN_MOBS;}
	@Override protected int canTargetCode(){return 0;}
	protected boolean isTreading=false;
	public Room room=null;
	protected int sinkTickDown=1;

	protected boolean reversed(){return proficiency()==100;}

	protected boolean isWaterSurface(Room R)
	{
		if(R==null) return false;
		if((R.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
		||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
			return true;
		return false;
	}
	protected boolean isUnderWater(Room R)
	{
		if(R==null) return false;
		if((R.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
		||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER))
			return true;
		return false;
	}

	protected boolean canSinkFrom(Room fromHere, int direction)
	{
		if((fromHere==null)||(direction<0)||(direction>=Directions.NUM_DIRECTIONS()))
			return false;

		final Room toHere=fromHere.getRoomInDir(direction);
		if((toHere==null)
		||(fromHere.getExitInDir(direction)==null)
		||(!fromHere.getExitInDir(direction).isOpen()))
			return false;
		if((!isWaterSurface(toHere))&&(!isUnderWater(toHere)))
			return false;
		return true;
	}

	protected boolean stopSinking(MOB mob)
	{
		unInvoke();
		mob.delEffect(this);
		return false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((affected!=null)&&(affected instanceof MOB)&&(msg.amISource((MOB)affected)))
		{
			if((msg.sourceMinor()==CMMsg.TYP_ENTER)
			&&(msg.target() instanceof Room)
			&&((((Room)msg.target()).domainType()==Room.DOMAIN_INDOORS_AIR)
			   ||(((Room)msg.target()).domainType()==Room.DOMAIN_OUTDOORS_AIR))
			&&(!CMLib.flags().isFlying(msg.source())))
			{
				msg.source().tell(_("You can't seem to get there from here."));
				return false;
			}
		}
		return true;
	}
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		final MOB mob=msg.source();
		if((affected!=null)&&(affected instanceof MOB)&&(msg.amISource((MOB)affected)))
		{
			if(msg.sourceMinor()==CMMsg.TYP_RECALL)
				stopSinking(mob);
			else
			if((msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING)))
				stopSinking(mob);
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(tickID!=Tickable.TICKID_MOB)
			return true;

		if(affected==null)
			return false;

		if((--sinkTickDown)>0)
			return true;
		sinkTickDown=1;

		int direction=Directions.DOWN;
		String addStr=_("down");
		if(reversed())
		{
			direction=Directions.UP;
			addStr=_("upwards");
		}
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if(mob==null) return false;
			Room R=mob.location();
			if(R==null) return false;

			if(!isWaterSurface(R)
			||(CMLib.flags().isWaterWorthy(mob))
			||(CMLib.flags().isInFlight(mob))
			||(mob.phyStats().weight()<1)
			||(!canSinkFrom(R,direction)))
				return stopSinking(mob);

			final Ability A=mob.fetchAbility("Skill_Swim");
			if(((direction==Directions.DOWN)&&(A!=null))
			&&(A.proficiencyCheck(mob,25,(A.proficiency()>=75))
			&&(mob.curState().getMovement()>0)))
			{
				if((R.show(mob,null,CMMsg.MSG_NOISYMOVEMENT,_("<S-NAME> tread(s) water.")))
				&&(!mob.isMonster()))
				{
					isTreading=true;
					CMLib.combat().expendEnergy(mob,true);
					mob.recoverPhyStats();
					return true;
				}
			}
			isTreading=false;
			mob.recoverPhyStats();
			mob.tell(_("\n\r\n\rYOU ARE SINKING @x1!!\n\r\n\r",addStr.toUpperCase()));
			CMLib.tracking().walk(mob,direction,false,false);
			R=mob.location();
			if((R!=null)&&(!canSinkFrom(R,direction)))
			{
				return stopSinking(mob);
			}
			return true;
		}
		else
		if(affected instanceof Item)
		{
			final Item item=(Item)affected;
			if((room==null)
			&&(item.owner()!=null)
			&&(item.owner() instanceof Room))
				room=(Room)item.owner();

			if((room==null)
			||((room!=null)&&(!room.isContent(item)))
			||(!CMLib.flags().isGettable(item)))
			{
				unInvoke();
				return false;
			}

			final Item ultContainerI=item.ultimateContainer(null);

			if(CMLib.flags().isInFlight(ultContainerI)
			||(CMLib.flags().isWaterWorthy(ultContainerI))
			||(item.container()!=null)
			||(item.phyStats().weight()<1))
			{
				unInvoke();
				return false;
			}
			if(room.numItems()>100)
			{
				sinkTickDown=CMLib.dice().roll(1,room.numItems()/50,0);
				if((--sinkTickDown)>0)
					return true;
			}
			final Room nextRoom=room.getRoomInDir(direction);
			if((nextRoom!=null)&&(canSinkFrom(room,direction)))
			{
				room.show(invoker,null,item,CMMsg.MSG_OK_ACTION,_("<O-NAME> sinks @x1.",addStr));
				nextRoom.moveItemTo(item,ItemPossessor.Expire.Player_Drop);
				room=nextRoom;
				nextRoom.show(invoker,null,item,CMMsg.MSG_OK_ACTION,_("<O-NAME> sinks in from @x1.",(reversed()?_("below"):_("above"))));
				return true;
			}
			if(reversed())
				return true;
			unInvoke();
			return false;
		}
		return false;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((!CMLib.flags().isWaterWorthy(affected))
		&&(!CMLib.flags().isInFlight(affected))
		&&(!isTreading)
		&&(affected.phyStats().weight()>=1))
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_FALLING);
	}

	@Override
	public void setAffectedOne(Physical P)
	{
		if(P instanceof Room)
			room=(Room)P;
		else
			super.setAffectedOne(P);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!auto) return false;
		final Physical target=givenTarget;
		if(target==null) return false;
		if((target instanceof Item)&&(room==null)) return false;
		if(target.fetchEffect("Sinking")==null)
		{
			final Sinking F=new Sinking();
			F.setProficiency(proficiency());
			F.invoker=null;
			if(target instanceof MOB)
				F.invoker=(MOB)target;
			else
				F.invoker=CMClass.getMOB("StdMOB");
			target.addEffect(F);
			F.setSavable(false);
			F.makeLongLasting();
			if(!(target instanceof MOB))
				CMLib.threads().startTickDown(F,Tickable.TICKID_MOB,1);
			target.recoverPhyStats();
		}
		return true;
	}
}
