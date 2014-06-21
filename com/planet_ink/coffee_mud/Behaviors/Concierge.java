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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
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
public class Concierge extends StdBehavior
{
	@Override public String ID(){return "Concierge";}

	public DVector rates=new DVector(2);
	public DVector destinations=new DVector(3);
	public DVector thingsToSay=new DVector(2);
	double basePrice=0.0;

	@Override
	public String accountForYourself()
	{
		return "direction giving and selling";
	}

	@Override
	public void setParms(String newParm)
	{
		super.setParms(newParm);
		rates.clear();
		if((CMath.isInteger(newParm))
		||(CMath.isDouble(newParm)))
		{
			basePrice=CMath.s_double(newParm);
			return;
		}
		final List<String> V=CMParms.parseSemicolons(newParm,true);
		String s=null;
		int x=0;
		double price=0;
		Room R=null;
		Area A=null;
		for(int v=0;v<V.size();v++)
		{
			s=V.get(v);
			x=s.indexOf('=');
			if(x>0)
			{
				price=CMath.s_double(s.substring(x+1));
				s=s.substring(0,x);
			}
			A=null;
			R=CMLib.map().getRoom(s);
			if(R==null) A=CMLib.map().findArea(s);
			if(A!=null)
				rates.addElement(A,Double.valueOf(price));
			else
			if((R!=null)&&(!rates.contains(R)))
				rates.addElement(R,Double.valueOf(price));
			else
				rates.addElement(s,Double.valueOf(price));
		}
		basePrice=price;
	}

	public double getPrice(Environmental E)
	{
		if(E==null) return basePrice;
		if(rates.size()==0) return basePrice;
		final int rateIndex=rates.indexOf(E);
		if(rateIndex<0) return basePrice;
		return ((Double)rates.elementAt(rateIndex,2)).doubleValue();
	}

	@SuppressWarnings("unchecked")
	public Environmental findDestination(MOB observer, MOB mob, String where)
	{
		DVector stringsToDo=null;
		if(rates.size()==0) return CMLib.map().findArea(where);
		for(int r=rates.size()-1;r>=0;r--)
			if(rates.elementAt(r,1) instanceof String)
			{
				final String place=(String)rates.elementAt(r,1);
				if((observer!=null)&&(observer.location()!=null))
				{
					if(stringsToDo==null) stringsToDo=new DVector(2);
					stringsToDo.addElement(place,rates.elementAt(r,2));
				}
				rates.removeElementAt(r);
			}
		if((stringsToDo!=null)&&(observer!=null))
		{
			TrackingLibrary.TrackingFlags flags;
			flags = new TrackingLibrary.TrackingFlags()
					.plus(TrackingLibrary.TrackingFlag.AREAONLY);
			final List<Room> roomsInRange=
				CMLib.tracking().getRadiantRooms(observer.location(),flags,50);
			Room R=null;
			String place=null;
			for(int r=0;r<stringsToDo.size();r++)
			{
				place=(String)stringsToDo.elementAt(r,1);
				R=(Room)CMLib.english().fetchEnvironmental(roomsInRange,place,false);
				if(R!=null) rates.addElement(R,stringsToDo.elementAt(r,2));
			}
			stringsToDo.clear();
			stringsToDo=null;
		}
		Environmental E=CMLib.english().fetchEnvironmental(rates.getDimensionVector(1),where,true);
		if(E==null)E=CMLib.english().fetchEnvironmental(rates.getDimensionVector(1),where,false);
		return E;
	}

	@Override
	public boolean okMessage(Environmental affecting, CMMsg msg)
	{
		if(!super.okMessage(affecting,msg))
			return false;
		final MOB source=msg.source();
		if(!canFreelyBehaveNormal(affecting))
			return true;
		final MOB observer=(MOB)affecting;
		if((source!=observer)
		&&(msg.amITarget(observer))
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(!CMSecurity.isAllowed(source,source.location(),CMSecurity.SecFlag.CMDROOMS))
		&&(!CMSecurity.isAllowed(source,source.location(),CMSecurity.SecFlag.CMDMOBS))
		&&(msg.tool()!=null))
		{
			final int destIndex=destinations.indexOf(source);
			if(destIndex<0)
			{
				CMLib.commands().postSay(observer,source,_("What's this for?  Please tell me where you'd like to go first."),true,false);
				return false;
			}
			else
			if(!(msg.tool() instanceof Coins))
			{
				CMLib.commands().postSay(observer,source,_("I'm sorry, I can only accept money."),true,false);
				return false;
			}
			else
			if(!((Coins)msg.tool()).getCurrency().equalsIgnoreCase(CMLib.beanCounter().getCurrency(observer)))
			{
				CMLib.commands().postSay(observer,source,_("I'm sorry, I don't accept that kind of currency."),true,false);
				return false;
			}
			final Environmental destination=(Environmental)destinations.elementAt(destIndex,2);
			final Double paid=(Double)destinations.elementAt(destinations.indexOf(source),3);
			final double owed=getPrice(destination)-paid.doubleValue();
			if(owed<=0.0)
			{
				CMLib.commands().postSay(observer,source,_("Hey, you've already paid me!"),true,false);
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((ticking instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB)
		&&(thingsToSay.size()>0)
		&&(canFreelyBehaveNormal(ticking)))
		{
			final MOB observer=(MOB)ticking;
			synchronized(thingsToSay)
			{
				while(thingsToSay.size()>0)
				{
					final MOB source=(MOB)thingsToSay.elementAt(0,1);
					final String msg=(String)thingsToSay.elementAt(0,2);
					thingsToSay.removeElementAt(0);
					CMLib.commands().postSay(observer,source,msg,true,false);
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	public String getDestination(MOB from, Environmental to)
	{
		final Command C=CMClass.getCommand("TrailTo");
		if(C==null) return "Umm.. I'm stupid.";
		String name=to.Name();
		if(to instanceof Room) name=CMLib.map().getExtendedRoomID((Room)to);
		final TrackingLibrary.TrackingFlags flags = new TrackingLibrary.TrackingFlags().plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS);
		final Vector<Room> set=new Vector<Room>();
		final int radius=100;
		CMLib.tracking().getRadiantRooms(from.location(),set,flags,null,radius,null);
		return CMLib.tracking().getTrailToDescription(from.location(),set,name,false,false,radius,null,1);
	}

	@Override
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		if(!canFreelyBehaveNormal(affecting)) return;

		final MOB source=msg.source();
		final MOB observer=(MOB)affecting;
		if((source!=observer)
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(msg.amITarget(observer))
		&&(msg.tool() instanceof Coins))
		{
			final int destIndex=destinations.indexOf(source);
			if((CMLib.flags().canBeSeenBy(source,observer))
			&&(CMLib.flags().canBeSeenBy(observer,source))
			&&(destIndex>=0))
			{
				final Environmental destination=(Environmental)destinations.elementAt(destIndex,2);
				final Double paid=(Double)destinations.elementAt(destIndex,3);
				double owed=getPrice(destination)-paid.doubleValue();
				owed-=((Coins)msg.tool()).getTotalValue();
				if(owed>0.0)
				{
					destinations.setElementAt(destIndex,3,Double.valueOf(owed));
					CMLib.commands().postSay(observer,source,_("Ok, you still owe @x1.",CMLib.beanCounter().nameCurrencyLong(observer,owed)),true,false);
					return;
				}
				else
				if(owed<0.0)
				{
					final double change=-owed;
					final Coins C=CMLib.beanCounter().makeBestCurrency(observer,change);
					if((change>0.0)&&(C!=null))
					{
						// this message will actually end up triggering the hand-over.
						final CMMsg newMsg=CMClass.getMsg(observer,source,C,CMMsg.MSG_SPEAK,_("^T<S-NAME> say(s) 'Heres your change.' to <T-NAMESELF>.^?"));
						C.setOwner(observer);
						final long num=C.getNumberOfCoins();
						final String curr=C.getCurrency();
						final double denom=C.getDenomination();
						C.destroy();
						C.setNumberOfCoins(num);
						C.setCurrency(curr);
						C.setDenomination(denom);
						msg.addTrailerMsg(newMsg);
					}
					else
						CMLib.commands().postSay(observer,source,_("Gee, thanks. :)"),true,false);
				}
				((Coins)msg.tool()).destroy();
				thingsToSay.addElement(msg.source(),"Thank you. The way to "+destination.name()+" from here is: "+this.getDestination(observer,destination));
				destinations.removeElement(msg.source());
			}
			else
			if(!CMLib.flags().canBeSeenBy(source,observer))
				CMLib.commands().postSay(observer,null,_("Wha?  Where did this come from?  Cool!"),true,false);
		}
		else
		if((msg.source()==observer)
		&&(msg.targetMinor()==CMMsg.TYP_SPEAK)
		&&(msg.target() instanceof MOB)
		&&(msg.tool() instanceof Coins)
		&&(((Coins)msg.tool()).amDestroyed())
		&&(!msg.source().isMine(msg.tool()))
		&&(!((MOB)msg.target()).isMine(msg.tool())))
			CMLib.beanCounter().giveSomeoneMoney(msg.source(),(MOB)msg.target(),((Coins)msg.tool()).getTotalValue());
		else
		if((msg.source()!=observer)
		&&(msg.targetMinor()==CMMsg.TYP_SPEAK)
		&&(!msg.source().isMonster())
		&&((msg.target()==observer)||(observer.location().numPCInhabitants()==1))
		&&(msg.sourceMessage()!=null))
		{
			final String say=CMStrings.getSayFromMessage(msg.sourceMessage());
			if((say!=null)&&(say.length()>0))
			{
				final Environmental E=findDestination(observer,msg.source(),say);
				if(E==null)
					synchronized(thingsToSay)
					{
						thingsToSay.addElement(msg.source(),"I'm sorry, I don't know where '"+say+"' is.");
						return;
					}
				final int index=destinations.indexOf(msg.source());
				final Double paid=(index>=0)?(Double)destinations.elementAt(index,3):Double.valueOf(0.0);
				destinations.removeElement(msg.source());
				final double rate=getPrice(E);
				if(rate<=0.0)
					thingsToSay.addElement(msg.source(),"Yes, the way to "+E.name()+" from here is: "+this.getDestination(observer,E));
				else
				{
					destinations.addElement(msg.source(),E,paid);
					thingsToSay.addElement(msg.source(),"Yep, I can help you find "+E.name()+", but you'll need to give me "+CMLib.beanCounter().nameCurrencyLong(observer,rate)+" first.");
				}
			}
		}
		else
		if((msg.source()!=observer)
		&&(msg.target()==observer.location())
		&&(msg.targetMinor()==CMMsg.TYP_LEAVE)
		&&(destinations.contains(msg.source())))
			destinations.removeElement(msg.source());
	}
}
