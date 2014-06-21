package com.planet_ink.coffee_mud.Locales;
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
@SuppressWarnings("unchecked")
public class WaterSurface extends StdRoom implements Drink
{
	@Override public String ID(){return "WaterSurface";}
	public WaterSurface()
	{
		super();
		name="the water";
		basePhyStats.setWeight(2);
		recoverPhyStats();
		climask=Places.CLIMASK_WET;
	}
	@Override public int domainType(){return Room.DOMAIN_OUTDOORS_WATERSURFACE;}
	@Override public long decayTime(){return 0;}
	@Override public void setDecayTime(long time){}

	protected String UnderWaterLocaleID(){return "UnderWaterGrid";}
	protected int UnderWaterDomainType(){return Room.DOMAIN_OUTDOORS_UNDERWATER;}
	protected boolean IsUnderWaterFatClass(Room thatSea){return (thatSea instanceof UnderWaterGrid)||(thatSea instanceof UnderWaterThinGrid);}

	@Override
	public void giveASky(int depth)
	{
		if(skyedYet) return;
		if(depth>1000) return;
		super.giveASky(depth+1);
		skyedYet=true;

		if((roomID().length()==0)
		&&(getGridParent()!=null)
		&&(getGridParent().roomID().length()==0))
			return;

		if((rawDoors()[Directions.DOWN]==null)
		&&(domainType()!=UnderWaterDomainType())
		&&(domainType()!=Room.DOMAIN_OUTDOORS_AIR)
		&&(CMProps.getIntVar(CMProps.Int.SKYSIZE)!=0))
		{
			Exit dnE=null;
			final Exit upE=CMClass.getExit("StdOpenDoorway");
			if(CMProps.getIntVar(CMProps.Int.SKYSIZE)>0)
				dnE=upE;
			else
				dnE=CMClass.getExit("UnseenWalkway");
			final GridLocale sea=(GridLocale)CMClass.getLocale(UnderWaterLocaleID());
			sea.setRoomID("");
			sea.setArea(getArea());
			rawDoors()[Directions.DOWN]=sea;
			setRawExit(Directions.DOWN,dnE);
			sea.rawDoors()[Directions.UP]=this;
			sea.setRawExit(Directions.UP,upE);
			for(int d=0;d<4;d++)
			{
				final Room thatRoom=rawDoors()[d];
				Room thatSea=null;
				if((thatRoom!=null)&&(getRawExit(d)!=null))
				{
					thatRoom.giveASky(depth+1);
					thatSea=thatRoom.rawDoors()[Directions.DOWN];
				}
				if((thatSea!=null)
				&&(thatSea.roomID().length()==0)
				&&(IsUnderWaterFatClass(thatSea)))
				{
					sea.rawDoors()[d]=thatSea;
					sea.setRawExit(d,getRawExit(d));
					thatSea.rawDoors()[Directions.getOpDirectionCode(d)]=sea;
					if(thatRoom!=null)
					{
						Exit xo=thatRoom.getRawExit(Directions.getOpDirectionCode(d));
						if((xo==null)||(xo.hasADoor())) xo=upE;
						thatSea.setRawExit(Directions.getOpDirectionCode(d),xo);
					}
					((GridLocale)thatSea).clearGrid(null);
				}
			}
			sea.clearGrid(null);
		}
	}

	@Override
	public void clearSky()
	{
		if(!skyedYet) return;
		super.clearSky();
		final Room room=rawDoors()[Directions.DOWN];
		if(room==null) return;
		if((room.roomID().length()==0)
		&&(IsUnderWaterFatClass(room)))
		{
			((GridLocale)room).clearGrid(null);
			rawDoors()[Directions.DOWN]=null;
			setRawExit(Directions.DOWN,null);
			CMLib.map().emptyRoom(room,null);
			room.destroy();
			skyedYet=false;
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		switch(WaterSurface.isOkWaterSurfaceAffect(this,msg))
		{
		case -1: return false;
		case 1: return true;
		}
		return super.okMessage(myHost,msg);
	}

	public static int isOkWaterSurfaceAffect(Room room, CMMsg msg)
	{
		if(CMLib.flags().isSleeping(room))
			return 0;

		if(((msg.targetMinor()==CMMsg.TYP_LEAVE)
			||(msg.targetMinor()==CMMsg.TYP_ENTER)
			||(msg.targetMinor()==CMMsg.TYP_FLEE))
		&&(msg.amITarget(room))
		&&(msg.sourceMinor()!=CMMsg.TYP_RECALL)
		&&((msg.targetMinor()==CMMsg.TYP_ENTER)
		   ||(!(msg.tool() instanceof Ability))
		   ||(!CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING)))
		&&(!CMLib.flags().isFalling(msg.source()))
		&&(!CMLib.flags().isInFlight(msg.source()))
		&&(!CMLib.flags().isWaterWorthy(msg.source())))
		{
			final MOB mob=msg.source();
			boolean hasBoat=false;
			for(int i=0;i<mob.numItems();i++)
			{
				final Item I=mob.getItem(i);
				if((I!=null)&&(I instanceof Rideable)&&(((Rideable)I).rideBasis()==Rideable.RIDEABLE_WATER))
				{	hasBoat=true; break;}
			}
			if((!CMLib.flags().isWaterWorthy(mob))
			&&(!hasBoat)
			&&(!CMLib.flags().isInFlight(mob)))
			{
				mob.tell(CMLib.lang()._("You need to swim or ride a boat that way."));
				return -1;
			}
			else
			if(CMLib.flags().isSwimming(mob))
				if(mob.phyStats().weight()>Math.round(CMath.mul(mob.maxCarry(),0.50)))
				{
					mob.tell(CMLib.lang()._("You are too encumbered to swim."));
					return -1;
				}
		}
		else
		if(((msg.sourceMinor()==CMMsg.TYP_SIT)||(msg.sourceMinor()==CMMsg.TYP_SLEEP))
		&&(!(msg.target() instanceof Exit))
		&&((msg.source().riding()==null)||(!CMLib.flags().isSwimming(msg.source().riding()))))
		{
			msg.source().tell(CMLib.lang()._("You cannot rest here."));
			return -1;
		}
		else
		if(msg.amITarget(room)
		&&(msg.targetMinor()==CMMsg.TYP_DRINK)
		&&(room instanceof Drink))
		{
			if(((Drink)room).liquidType()==RawMaterial.RESOURCE_SALTWATER)
			{
				msg.source().tell(CMLib.lang()._("You don't want to be drinking saltwater."));
				return -1;
			}
			return 1;
		}
		return 0;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		UnderWater.sinkAffects(this,msg);
	}
	@Override public int thirstQuenched(){return 1000;}
	@Override public int liquidHeld(){return Integer.MAX_VALUE-1000;}
	@Override public int liquidRemaining(){return Integer.MAX_VALUE-1000;}
	@Override public int liquidType(){return RawMaterial.RESOURCE_FRESHWATER;}
	@Override public void setLiquidType(int newLiquidType){}
	@Override public void setThirstQuenched(int amount){}
	@Override public void setLiquidHeld(int amount){}
	@Override public void setLiquidRemaining(int amount){}
	@Override public boolean disappearsAfterDrinking(){return false;}
	@Override public boolean containsDrink(){return true;}
	@Override public int amountTakenToFillMe(Drink theSource){return 0;}
	@Override public List<Integer> resourceChoices(){return UnderWater.roomResources;}
}
