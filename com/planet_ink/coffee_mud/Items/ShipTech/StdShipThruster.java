package com.planet_ink.coffee_mud.Items.ShipTech;
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
import com.planet_ink.coffee_mud.Items.interfaces.ShipComponent.ShipEngine;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
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
public class StdShipThruster extends StdCompFuelConsumer implements ShipComponent.ShipEngine
{
	@Override public String ID(){	return "StdShipThruster";}

	protected float 	installedFactor	= 1.0F;
	protected int		maxThrust		= 1000;
	protected int		thrust			= 0;
	protected long		specificImpulse	= SpaceObject.VELOCITY_SUBLIGHT;
	protected double	fuelEfficiency	= 0.33;

	public StdShipThruster()
	{
		super();
		setName("a thruster engine");
		basePhyStats.setWeight(5000);
		setDisplayText("a thruster engine sits here.");
		setDescription("");
		baseGoldValue=500000;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
		setCapacity(basePhyStats.weight()+10000);
	}
	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdShipThruster)) return false;
		return super.sameAs(E);
	}
	@Override public double getFuelEfficiency() { return fuelEfficiency; }
	@Override public void setFuelEfficiency(double amt) { fuelEfficiency=amt; }
	@Override public float getInstalledFactor() { return installedFactor; }
	@Override public void setInstalledFactor(float pct) { if((pct>=0.0)&&(pct<=2.0)) installedFactor=pct; }
	@Override public int getMaxThrust(){return maxThrust;}
	@Override public void setMaxThrust(int max){maxThrust=max;}
	@Override public int getThrust(){return thrust;}
	@Override public void setThrust(int current){thrust=current;}
	@Override public long getSpecificImpulse() { return specificImpulse; }
	@Override
	public void setSpecificImpulse(long amt)
	{
		if(amt > 0)
			specificImpulse = amt;
	}

	@Override public TechType getTechType() { return TechType.SHIP_ENGINE; }
	@Override protected boolean willConsumeFuelIdle() { return getThrust()>0; }

	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		executeThrusterMsg(this, myHost, circuitKey, msg);
	}


	public static boolean reportError(ShipEngine me, Software controlI, MOB mob, String literalMessage, String controlMessage)
	{
		if((mob!=null) && (mob.location()==CMLib.map().roomLocation(me)) && (literalMessage!=null))
			mob.tell(literalMessage);
		if(controlMessage!=null)
		{
			if(controlI!=null)
				controlI.addScreenMessage(controlMessage);
			else
			if((mob!=null)&&(me!=null))
				mob.tell(CMLib.lang()._("A panel on @x1 reports '@x2'.",me.name(mob),controlMessage));
		}
		return false;
	}

	public static boolean executeThrust(ShipEngine me, String circuitKey, MOB mob, Software controlI, ShipEngine.ThrustPort portDir, final int amount)
	{
		final SpaceObject obj=CMLib.map().getSpaceObject(me, true);
		final Manufacturer manufacturer=me.getFinalManufacturer();
		if(!(obj instanceof SpaceShip))
			return reportError(me, controlI, mob, me.name(mob)+" rumbles and fires, but nothing happens.","Failure: "+me.name(mob)+": exhaust ports.");
		final SpaceShip ship=(SpaceShip)obj;
		if((portDir==null)||(amount<0))
			return reportError(me, controlI, mob, me.name(mob)+" rumbles loudly, but accomplishes nothing.","Failure: "+me.name(mob)+": exhaust control.");
		int thrust=Math.round(me.getInstalledFactor() * amount);
		if(thrust > me.getMaxThrust())
			thrust=me.getMaxThrust();
		thrust=(int)Math.round(manufacturer.getReliabilityPct() * thrust);

		if(portDir==ThrustPort.AFT) // when thrusting aft, the thrust is continual, so save it
			me.setThrust(thrust);
		final int fuelToConsume=(int)Math.round(CMath.ceiling(thrust*me.getFuelEfficiency()*manufacturer.getEfficiencyPct()));
		final long accelleration=thrust/ship.getMass();
		if(me.consumeFuel(fuelToConsume))
		{
			final String code=Technical.TechCommand.ACCELLLERATION.makeCommand(portDir,Integer.valueOf((int)accelleration),Long.valueOf(me.getSpecificImpulse()));
			final CMMsg msg=CMClass.getMsg(mob, ship, me, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
			if(ship.okMessage(mob, msg))
			{
				ship.executeMsg(mob, msg);
				return true;
			}
		}
		else
		{
			final String code=Technical.TechCommand.COMPONENTFAILURE.makeCommand(TechType.SHIP_ENGINE, "Failure:_"+me.name().replace(' ','_')+":_insufficient_fuel.");
			for(final Iterator<Electronics.Computer> c=CMLib.tech().getComputers(circuitKey);c.hasNext();)
			{
				final Electronics.Computer C=c.next();
				if((controlI==null)||(C!=controlI.owner()))
				{
					final CMMsg msg2=CMClass.getMsg(mob, C, me, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
					if(C.okMessage(mob, msg2))
						C.executeMsg(mob, msg2);
				}
			}
			return reportError(me, controlI, mob, me.name(mob)+" rumbles loudly, then sputters down.","Failure: "+me.name(mob)+": insufficient fuel.");
		}
		return false;
	}

	public static boolean executeCommand(ShipEngine me, String circuitKey, CMMsg msg)
	{
		final Software controlI=(msg.tool() instanceof Software)?((Software)msg.tool()):null;
		final MOB mob=msg.source();
		final String[] parts=msg.targetMessage().split(" ");
		final TechCommand command=TechCommand.findCommand(parts);
		if(command==null)
			return reportError(me, controlI, mob, me.name(mob)+" does not respond.","Failure: "+me.name(mob)+": control failure.");
		final Object[] parms=command.confirmAndTranslate(parts);
		if(parms==null)
			return reportError(me, controlI, mob, me.name(mob)+" did not respond.","Failure: "+me.name(mob)+": control syntax failure.");
		if(command == TechCommand.THRUST)
			return executeThrust(me, circuitKey, mob, controlI, (ShipEngine.ThrustPort)parms[0],((Integer)parms[1]).intValue());
		return reportError(me, controlI, mob, me.name(mob)+" refused to respond.","Failure: "+me.name(mob)+": control command failure.");
	}

	public static void executeThrusterMsg(ShipEngine me, Environmental myHost, String circuitKey, CMMsg msg)
	{

		if(msg.amITarget(me))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
				if(executeCommand(me, circuitKey, msg))
					me.activate(true);
				break;
			case CMMsg.TYP_DEACTIVATE:
				me.setThrust(0);
				me.activate(false);
				//TODO:what does the ship need to know?
				break;
			case CMMsg.TYP_POWERCURRENT:
			{
				final Manufacturer manufacturer=me.getFinalManufacturer();
				final int fuelToConsume=(int)Math.round(CMath.ceiling(me.getThrust()*me.getFuelEfficiency()*manufacturer.getEfficiencyPct()));
				if(me.consumeFuel(fuelToConsume))
				{
					final SpaceObject obj=CMLib.map().getSpaceObject(me, true);
					if(obj instanceof SpaceShip)
					{
						final SpaceShip ship=(SpaceShip)obj;
						final long accelleration=me.getThrust()/ship.getMass();
						final String code=Technical.TechCommand.ACCELLLERATION.makeCommand(ThrustPort.AFT,Integer.valueOf((int)accelleration),Long.valueOf(me.getSpecificImpulse()));
						final CMMsg msg2=CMClass.getMsg(msg.source(), ship, me, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
						if(ship.okMessage(msg.source(), msg2))
							ship.executeMsg(msg.source(), msg2);
					}
				}
				else
				{
					CMMsg msg2=CMClass.getMsg(msg.source(), me, me, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE|CMMsg.MASK_CNTRLMSG, "", CMMsg.NO_EFFECT,null);
					if(me.owner() instanceof Room)
					{
						if(((Room)me.owner()).okMessage(msg.source(), msg2))
							((Room)me.owner()).send(msg.source(), msg2);
					}
					else
					if(me.okMessage(msg.source(), msg2))
						me.executeMsg(msg.source(), msg2);
					final String code=Technical.TechCommand.COMPONENTFAILURE.makeCommand(TechType.SHIP_ENGINE, "Failure: "+me.name()+": insufficient_fuel.");
					for(final Iterator<Electronics.Computer> c=CMLib.tech().getComputers(circuitKey);c.hasNext();)
					{
						final Electronics.Computer C=c.next();
						msg2=CMClass.getMsg(msg.source(), C, me, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
						if(C.okMessage(msg.source(), msg2))
							C.executeMsg(msg.source(), msg2);
					}
				}
				break;
			}
			}
		}
	}
}
