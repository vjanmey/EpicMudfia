package com.planet_ink.coffee_mud.Items.Basic;
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
@SuppressWarnings({"unchecked","rawtypes"})
public class StdRideable extends StdContainer implements Rideable
{
	@Override public String ID(){	return "StdRideable";}
	protected int rideBasis=Rideable.RIDEABLE_WATER;
	protected int riderCapacity=4;
	protected List<Rider> riders=new SVector<Rider>();
	public StdRideable()
	{
		super();
		setName("a boat");
		setDisplayText("a boat is docked here.");
		setDescription("Looks like a boat");
		basePhyStats().setWeight(2000);
		recoverPhyStats();
		capacity=3000;
		material=RawMaterial.RESOURCE_OAK;
	}

	@Override
	public void destroy()
	{
		while(riders.size()>0)
		{
			final Rider mob=fetchRider(0);
			if(mob!=null)
			{
				mob.setRiding(null);
				delRider(mob);
			}
		}
		super.destroy();
	}

	public boolean savable()
	{
		Rider R=null;
		for(int r=0;r<numRiders();r++)
		{
			R=fetchRider(r);
			if(!R.isSavable())
				return false;
		}
		return super.isSavable();
	}

	@Override
	public boolean isMobileRideBasis()
	{
		switch(rideBasis())
		{
			case RIDEABLE_SIT:
			case RIDEABLE_TABLE:
			case RIDEABLE_ENTERIN:
			case RIDEABLE_SLEEP:
			case RIDEABLE_LADDER:
				return false;
		}
		return true;
	}
	// common item/mob stuff
	@Override public int rideBasis(){return rideBasis;}
	@Override public void setRideBasis(int basis){rideBasis=basis;}
	@Override public int riderCapacity(){ return riderCapacity;}
	@Override public void setRiderCapacity(int newCapacity){riderCapacity=newCapacity;}
	@Override public int numRiders(){return riders.size();}
	@Override
	public Rider fetchRider(int which)
	{
		try	{ return riders.get(which);	}
		catch(final java.lang.ArrayIndexOutOfBoundsException e){}
		return null;
	}
	@Override
	public void addRider(Rider mob)
	{
		if((mob!=null)&&(!riders.contains(mob)))
			riders.add(mob);
	}
	@Override public Iterator<Rider> riders(){return riders.iterator();}
	@Override
	public void delRider(Rider mob)
	{
		if(mob!=null)
			while(riders.remove(mob))
				{}
	}

	@Override
	protected void cloneFix(Item E)
	{
		super.cloneFix(E);
		riders=new SVector();
	}
	@Override
	public Set<MOB> getRideBuddies(Set<MOB> list)
	{
		if(list==null) return list;
		for(int r=0;r<numRiders();r++)
		{
			final Rider R=fetchRider(r);
			if((R instanceof MOB)
			&&(!list.contains(R)))
				list.add((MOB)R);
		}
		return list;
	}

	@Override
	public boolean mobileRideBasis()
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WAGON:
		case Rideable.RIDEABLE_WATER:
			return true;
		}
		return false;
	}
	@Override
	public String stateString(Rider R)
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WAGON:
		case Rideable.RIDEABLE_WATER:
			return "riding in";
		case Rideable.RIDEABLE_ENTERIN:
			return "in";
		case Rideable.RIDEABLE_SIT:
			return "on";
		case Rideable.RIDEABLE_TABLE:
			return "at";
		case Rideable.RIDEABLE_LADDER:
			return "climbing on";
		case Rideable.RIDEABLE_SLEEP:
			return "on";
		}
		return "riding in";
	}
	@Override
	public String putString(Rider R)
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WAGON:
		case Rideable.RIDEABLE_WATER:
		case Rideable.RIDEABLE_SLEEP:
		case Rideable.RIDEABLE_ENTERIN:
			return "in";
		case Rideable.RIDEABLE_SIT:
		case Rideable.RIDEABLE_TABLE:
		case Rideable.RIDEABLE_LADDER:
			return "on";
		}
		return "in";
	}

	@Override
	public String mountString(int commandType, Rider R)
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WAGON:
		case Rideable.RIDEABLE_WATER:
			return "board(s)";
		case Rideable.RIDEABLE_SIT:
			return "sit(s) on";
		case Rideable.RIDEABLE_TABLE:
			return "sit(s) at";
		case Rideable.RIDEABLE_ENTERIN:
			return "get(s) into";
		case Rideable.RIDEABLE_LADDER:
			return "climb(s) onto";
		case Rideable.RIDEABLE_SLEEP:
			if(commandType==CMMsg.TYP_SIT)
				return "sit(s) down on";
			return "lie(s) down on";
		}
		return "board(s)";
	}
	@Override
	public String dismountString(Rider R)
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WATER:
			return "disembark(s) from";
		case Rideable.RIDEABLE_TABLE:
			return "get(s) up from";
		case Rideable.RIDEABLE_SIT:
		case Rideable.RIDEABLE_SLEEP:
		case Rideable.RIDEABLE_WAGON:
		case Rideable.RIDEABLE_LADDER:
			return "get(s) off of";
		case Rideable.RIDEABLE_ENTERIN:
			return "get(s) out of";
		}
		return "disembark(s) from";
	}
	@Override
	public String stateStringSubject(Rider R)
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WATER:
		case Rideable.RIDEABLE_WAGON:
			return "being ridden by";
		case Rideable.RIDEABLE_TABLE:
			return "occupied by";
		case Rideable.RIDEABLE_SIT:	return "";
		case Rideable.RIDEABLE_SLEEP: return "";
		case Rideable.RIDEABLE_ENTERIN: return "occupied by";
		case Rideable.RIDEABLE_LADDER: return "occupied by";
		}
		return "";
	}

	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		if(rideBasis==Rideable.RIDEABLE_AIR)
			phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_FLYING);
		else
		if(rideBasis==Rideable.RIDEABLE_WATER)
			phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_SWIMMING);
	}
	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if(!CMLib.flags().hasSeenContents(this))
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_NOT_SEEN);
			if((mob.isInCombat())&&(mob.rangeToTarget()==0)&&(amRiding(mob)))
			{
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-mob.basePhyStats().attackAdjustment());
				affectableStats.setDamage(affectableStats.damage()-mob.basePhyStats().damage());
			}
			if((rideBasis()==Rideable.RIDEABLE_LADDER)
			&&(amRiding(mob)))
			{
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_CLIMBING);
				affectableStats.setSpeed(affectableStats.speed()/2);
			}
		}
	}

	@Override
	public String displayText(MOB mob)
	{
 		if((numRiders()>0)
 		&&(stateStringSubject(this).length()>0)
 		&&(displayText!=null)
 		&&(displayText.length()>0)
 		&&CMLib.flags().hasSeenContents(this))
		{
			final StringBuffer sendBack=new StringBuffer(name(mob));
			sendBack.append(" "+stateStringSubject(this)+" ");
			for(int r=0;r<numRiders();r++)
			{
				final Rider rider=fetchRider(r);
				if(rider!=null)
				{
					if(r>0)
					{
						sendBack.append(", ");
						if(r==numRiders()-1)
							sendBack.append("and ");
					}
					sendBack.append(rider.name(mob));
				}

			}
			return sendBack.toString();
		}
 		return super.displayText(mob);
	}
	@Override
	public boolean amRiding(Rider mob)
	{
		return riders.contains(mob);
	}
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_ADVANCE:
			if((rideBasis()==Rideable.RIDEABLE_LADDER)
			&&(amRiding(msg.source())))
			{
				msg.source().tell(_("You cannot advance while @x1 @x2!",stateString(msg.source()),name(msg.source())));
				return false;
			}
			break;
		case CMMsg.TYP_RETREAT:
			if((rideBasis()==Rideable.RIDEABLE_LADDER)
			&&(amRiding(msg.source())))
			{
				msg.source().tell(_("You cannot retreat while @x1 @x2!",stateString(msg.source()),name(msg.source())));
				return false;
			}
			break;
		case CMMsg.TYP_DISMOUNT:
			if(msg.amITarget(this))
			{
				if((msg.tool()!=null)
				   &&(msg.tool() instanceof Rider))
				{
					if(!amRiding((Rider)msg.tool()))
					{
						msg.source().tell(_("@x1 is not @x2 @x3!",msg.tool().name(),stateString((Rider)msg.tool()),name(msg.source())));
						if(((Rider)msg.tool()).riding()==this)
							((Rider)msg.tool()).setRiding(null);
						return false;
					}
				}
				else
				if(!amRiding(msg.source()))
				{
					msg.source().tell(_("You are not @x1 @x2!",stateString(msg.source()),name(msg.source())));
					if(msg.source().riding()==this)
						msg.source().setRiding(null);
					return false;
				}
				// protects from standard item rejection
				return true;
			}
			break;
		case CMMsg.TYP_SIT:
			if(amRiding(msg.source()))
			{
				msg.source().tell(_("You are @x1 @x2!",stateString(msg.source()),name(msg.source())));
				msg.source().setRiding(this);
				return false;
			}
			else
			if((riding()!=msg.source())
			&&((rideBasis()==Rideable.RIDEABLE_SIT)
			||(rideBasis()==Rideable.RIDEABLE_ENTERIN)
			||(rideBasis()==Rideable.RIDEABLE_TABLE)
			||(rideBasis()==Rideable.RIDEABLE_SLEEP)))
			{
				if(msg.amITarget(this)
				&&(numRiders()>=riderCapacity())
				&&(!amRiding(msg.source())))
				{
					// for items
					msg.source().tell(_("@x1 is full.",name(msg.source())));
					// for mobs
					// msg.source().tell(_("No more can fit on @x1.",name(msg.source())));
					return false;
				}
				return true;
			}
			else
			if(msg.amITarget(this))
			{
				msg.source().tell(_("You cannot sit on @x1.",name(msg.source())));
				return false;
			}
			break;
		case CMMsg.TYP_SLEEP:
			if((amRiding(msg.source()))
			&&(((!msg.amITarget(this))&&(msg.target()!=null))
			   ||((rideBasis()!=Rideable.RIDEABLE_SLEEP)&&(rideBasis()!=Rideable.RIDEABLE_ENTERIN))))
			{
				msg.source().tell(_("You are @x1 @x2!",stateString(msg.source()),name(msg.source())));
				msg.source().setRiding(this);
				return false;
			}
			else
			if((riding()!=msg.source())
			&&((rideBasis()==Rideable.RIDEABLE_SLEEP)
			||(rideBasis()==Rideable.RIDEABLE_ENTERIN)))
			{
				if(msg.amITarget(this)
				&&(numRiders()>=riderCapacity())
				&&(!amRiding(msg.source())))
				{
					// for items
					msg.source().tell(_("@x1 is full.",name(msg.source())));
					// for mobs
					// msg.source().tell(_("No more can fit on @x1.",name(msg.source())));
					return false;
				}
				return true;
			}
			else
			if(msg.amITarget(this))
			{
				msg.source().tell(_("You cannot lie down on @x1.",name(msg.source())));
				return false;
			}
			break;
		case CMMsg.TYP_MOUNT:
		{
			if(amRiding(msg.source()))
			{
				msg.source().tell(null,msg.source(),null,_("<T-NAME> <T-IS-ARE> @x1 @x2!",stateString(msg.source()),name(msg.source())));
				msg.source().setRiding(this);
				return false;
			}
			if((riding()==msg.target())&&(msg.tool() instanceof Item))
			{
				msg.source().tell(null,msg.source(),null,_("<T-NAME> <T-IS-ARE> already @x1 @x2!",stateString(msg.source()),name(msg.source())));
				return false;
			}
			if(msg.amITarget(this))
			{
				final Rider whoWantsToRide=(msg.tool() instanceof Rider)?(Rider)msg.tool():msg.source();
				if(amRiding(whoWantsToRide))
				{
					msg.source().tell(null,whoWantsToRide,null,_("<T-NAME> <T-IS-ARE> @x1 @x2!",stateString(msg.source()),name(msg.source())));
					whoWantsToRide.setRiding(this);
					return false;
				}
				if((msg.tool() instanceof MOB)
				&&(!CMLib.flags().isBoundOrHeld((MOB)msg.tool())))
				{
					msg.source().tell(_("@x1 won't let you do that.",((MOB)msg.tool()).name(msg.source())));
					return false;
				}
				else
				if(riding()==whoWantsToRide)
				{
					if(msg.tool() instanceof Physical)
						msg.source().tell(_("@x1 can not be mounted to @x2!",((Physical)msg.tool()).name(msg.source()),name(msg.source())));
					else
						msg.source().tell(_("@x1 can not be mounted to @x2!",msg.tool().name(),name(msg.source())));
					return false;
				}
				else
				if(msg.tool() instanceof Rideable)
				{
					msg.source().tell(_("@x1 is not allowed on @x2.",((Rideable)msg.tool()).name(msg.source()),name(msg.source())));
					return false;
				}
				if(msg.tool()==null)
					switch(rideBasis())
					{
					case Rideable.RIDEABLE_ENTERIN:
					case Rideable.RIDEABLE_SIT:
					case Rideable.RIDEABLE_SLEEP:
						msg.source().tell(_("@x1 can not be mounted in this way.",name(msg.source())));
						return false;
					default:
						break;
					}
				if((numRiders()>=riderCapacity())
				&&(!amRiding(whoWantsToRide)))
				{
					// for items
					msg.source().tell(_("@x1 is full.",name(msg.source())));
					// for mobs
					// msg.source().tell(_("No more can fit on @x1.",name(msg.source())));
					return false;
				}
				// protects from standard item rejection
				return true;
			}
			break;
		}
		case CMMsg.TYP_ENTER:
			if(amRiding(msg.source())
			&&(msg.target()!=null)
			&&(msg.target() instanceof Room))
			{
				final Room sourceRoom=msg.source().location();
				final Room targetRoom=(Room)msg.target();
				if((sourceRoom!=null)&&(!msg.amITarget(sourceRoom)))
				{
					boolean ok=((targetRoom.domainType()&Room.INDOORS)==0)
								||(targetRoom.maxRange()>4);
					switch(rideBasis)
					{
					case Rideable.RIDEABLE_LAND:
					case Rideable.RIDEABLE_WAGON:
						if((targetRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)
						  ||(targetRoom.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
						  ||(targetRoom.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
						  ||(targetRoom.domainType()==Room.DOMAIN_INDOORS_AIR)
						  ||(targetRoom.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
						  ||(targetRoom.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE))
							ok=false;
							if((rideBasis==Rideable.RIDEABLE_WAGON)
							&&((riding()==null)
							   ||(!(riding() instanceof MOB))
							   ||(((MOB)riding()).basePhyStats().weight()<(basePhyStats().weight()/5))))
							{
								msg.source().tell(_("@x1 doesn't seem to be moving.",name(msg.source())));
								return false;
							}
						break;
					case Rideable.RIDEABLE_AIR:
						break;
					case Rideable.RIDEABLE_LADDER:
						ok=true;
						break;
					case Rideable.RIDEABLE_WATER:
						if((sourceRoom.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
						&&(targetRoom.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
						&&(sourceRoom.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)
						&&(targetRoom.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE))
							ok=false;
						else
							ok=true;
						if((targetRoom.domainType()==Room.DOMAIN_INDOORS_AIR)
						||(targetRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)
						||(targetRoom.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
						||(targetRoom.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER))
							ok=false;
						break;
					}
					if(!ok)
					{
						msg.source().tell(_("You cannot ride @x1 that way.",name(msg.source())));
						return false;
					}
					if(CMLib.flags().isSitting(msg.source()))
					{
						msg.source().tell(_("You cannot crawl while @x1 @x2.",stateString(msg.source()),name(msg.source())));
						return false;
					}
				}
			}
			break;
		case CMMsg.TYP_GIVE:
			if(msg.target() instanceof MOB)
			{
				final MOB tmob=(MOB)msg.target();
				if((amRiding(tmob))&&(!amRiding(msg.source())))
				{
					if(rideBasis()==Rideable.RIDEABLE_ENTERIN)
						msg.source().tell(msg.source(),tmob,null,_("<T-NAME> must exit first."));
					else
						msg.source().tell(msg.source(),tmob,null,_("<T-NAME> must disembark first."));
					return false;
				}
			}
			break;
		case CMMsg.TYP_BUY:
		case CMMsg.TYP_BID:
		case CMMsg.TYP_SELL:
			if((amRiding(msg.source()))
			&&(rideBasis()!=Rideable.RIDEABLE_TABLE)
			&&(rideBasis()!=Rideable.RIDEABLE_SIT))
			{
				msg.source().tell(_("You can not do that while @x1 @x2.",stateString(msg.source()),name(msg.source())));
				return false;
			}
			return super.okMessage(myHost,msg);
		}
		if((msg.sourceMajor(CMMsg.MASK_HANDS))
		&&(amRiding(msg.source()))
		&&((msg.sourceMessage()!=null)||(msg.othersMessage()!=null))
		&&(msg.target()!=this)
		&&(msg.tool()!=this)
		&&((!CMLib.utensils().reachableItem(msg.source(),msg.target()))
			|| (!CMLib.utensils().reachableItem(msg.source(),msg.tool()))
			|| ((msg.sourceMinor()==CMMsg.TYP_GIVE)&&(msg.target() instanceof MOB)&&(msg.target()!=this)&&(!amRiding((MOB)msg.target()))))
		&&(!((msg.sourceMinor()==CMMsg.TYP_GIVE)&&(msg.target() instanceof MOB)&&(amRiding((MOB)msg.target()))&&(CMLib.flags().isStanding(msg.source())))))
		{
			// some of the above applies to genrideable items only
			msg.source().tell(_("You can not do that while @x1 @x2.",stateString(msg.source()),name(msg.source())));
			return false;
		}
		return super.okMessage(myHost,msg);
	}
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_LOOK:
		case CMMsg.TYP_EXAMINE:
			if((msg.target()==this)
			&&(numRiders()>0)
			&&(CMLib.flags().canBeSeenBy(this,msg.source())))
				msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_OK_VISUAL,displayText(),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
			break;
		case CMMsg.TYP_DISMOUNT:
			if((msg.tool()!=null)
			   &&(msg.tool() instanceof Rider))
			{
				((Rider)msg.tool()).setRiding(null);
				if(msg.source().location()!=null)
					msg.source().location().recoverRoomStats();
			}
			else
			if(amRiding(msg.source()))
			{
				msg.source().setRiding(null);
				if(msg.source().location()!=null)
					msg.source().location().recoverRoomStats();
			}
			break;
		case CMMsg.TYP_ENTER:
		case CMMsg.TYP_LEAVE:
		case CMMsg.TYP_FLEE:
			if((rideBasis()==Rideable.RIDEABLE_LADDER)
			&&(amRiding(msg.source())))
			{
				msg.source().setRiding(null);
				if(msg.source().location()!=null)
					msg.source().location().recoverRoomStats();
			}
			break;
		case CMMsg.TYP_MOUNT:
		case CMMsg.TYP_SIT:
		case CMMsg.TYP_SLEEP:
			if(msg.amITarget(this))
			{
				if((msg.tool()!=null)
				   &&(msg.tool() instanceof Rider))
				{
					final Rider R = (Rider)msg.tool();
					R.setRiding(this);
					if(msg.tool() instanceof MOB)
					switch(rideBasis())
					{
					case Rideable.RIDEABLE_SIT:
					case Rideable.RIDEABLE_ENTERIN:
						R.basePhyStats().setDisposition(R.basePhyStats().disposition()|PhyStats.IS_SITTING);
						break;
					case Rideable.RIDEABLE_SLEEP:
						R.basePhyStats().setDisposition(R.basePhyStats().disposition()|PhyStats.IS_SLEEPING);
						break;
					default:
						break;
					}
					if(msg.source().location()!=null)
						msg.source().location().recoverRoomStats();
				}
				else
				if(!amRiding(msg.source()))
				{
					msg.source().setRiding(this);
					if(msg.source().location()!=null)
						msg.source().location().recoverRoomStats();
				}
			}
			break;
		}
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_STAND:
		case CMMsg.TYP_QUIT:
		case CMMsg.TYP_PANIC:
		case CMMsg.TYP_DEATH:
			if(amRiding(msg.source()))
			{
			   msg.source().setRiding(null);
				if(msg.source().location()!=null)
					msg.source().location().recoverRoomStats();
			}
			break;
		}
	}
}
