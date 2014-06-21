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
import com.planet_ink.coffee_mud.Items.BasicTech.StdElecItem;
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
public class StdElecCompItem extends StdElecItem implements ShipComponent
{
	@Override public String ID(){	return "StdElecCompItem";}

	protected float installedFactor = 1.0f;
	private volatile String circuitKey=null;


	public StdElecCompItem()
	{
		super();
		setName("an electric component");
		setDisplayText("an electric component sits here.");
		setDescription("");
		baseGoldValue=50000;
		basePhyStats.setWeight(500);
		setUsesRemaining(100);
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
	}

	@Override public float getInstalledFactor() { return installedFactor; }
	@Override public void setInstalledFactor(float pct) { installedFactor=pct; }

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdElecCompItem)) return false;
		return super.sameAs(E);
	}

	@Override
	public void destroy()
	{
		if((!destroyed)&&(circuitKey!=null))
		{
			CMLib.tech().unregisterElectronics(this,circuitKey);
			circuitKey=null;
		}
		super.destroy();
	}

	@Override
	public void setOwner(ItemPossessor newOwner)
	{
		final ItemPossessor prevOwner=super.owner;
		super.setOwner(newOwner);
		if(prevOwner != newOwner)
		{
			if(newOwner instanceof Room)
				circuitKey=CMLib.tech().registerElectrics(this,circuitKey);
			else
			{
				CMLib.tech().unregisterElectronics(this,circuitKey);
				circuitKey=null;
			}
		}
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
				if(!isAllWiringConnected(this))
				{
					if(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG))
						msg.source().tell(_("The panel containing @x1 is not activated or connected.",name()));
					return false;
				}
				break;
			case CMMsg.TYP_DEACTIVATE:
				break;
			case CMMsg.TYP_LOOK:
				break;
			case CMMsg.TYP_POWERCURRENT:
				if((!(this instanceof Electronics.FuelConsumer))
				&&(!(this instanceof Electronics.PowerGenerator))
				&& activated()
				&& (powerNeeds()>0)
				&& (msg.value()>0))
				{
					double amtToTake=Math.min((double)powerNeeds(), (double)msg.value());
					msg.setValue(msg.value()-(int)Math.round(amtToTake));
					amtToTake *= getFinalManufacturer().getEfficiencyPct();
					if(subjectToWearAndTear() && (usesRemaining()<=200))
						amtToTake *= CMath.div(usesRemaining(), 100.0);
					setPowerRemaining(Math.min(powerCapacity(), Math.round(amtToTake) + powerRemaining()));
				}
				break;
			}
		}
		return super.okMessage(host, msg);
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DROP:
				setInstalledFactor((float)CMath.div(msg.value(),100.0));
				break;
			case CMMsg.TYP_ACTIVATE:
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, CMMsg.MSG_OK_VISUAL, _("<S-NAME> activate(s) <T-NAME>."));
				this.activate(true);
				break;
			case CMMsg.TYP_DEACTIVATE:
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, CMMsg.MSG_OK_VISUAL, _("<S-NAME> deactivate(s) <T-NAME>."));
				this.activate(false);
				break;
			case CMMsg.TYP_LOOK:
				super.executeMsg(host, msg);
				if(CMLib.flags().canBeSeenBy(this, msg.source()))
					msg.source().tell(_("@x1 is currently @x2",name(),(activated()?"connected.\n\r":"deactivated/disconnected.\n\r")));
				return;
			case CMMsg.TYP_REPAIR:
				if(CMLib.dice().rollPercentage()<msg.value())
				{
					setUsesRemaining(usesRemaining()<100?100:usesRemaining());
					msg.source().tell(_("@x1 is now repaired.\n\r",name()));
				}
				else
				{
					final int repairRequired=100-usesRemaining();
					if(repairRequired>0)
					{
						int repairApplied=(int)Math.round(CMath.mul(repairRequired, CMath.div(msg.value(), 100)));
						if(repairApplied < 0)
							repairApplied=1;
						setUsesRemaining(usesRemaining()+repairApplied);
						msg.source().tell(_("@x1 is now @x2% repaired.\n\r",name(),""+usesRemaining()));
					}
				}
				break;
			case CMMsg.TYP_ENHANCE:
				if((CMLib.dice().rollPercentage()<msg.value())&&(CMLib.dice().rollPercentage()<50))
				{
					float addAmt=0.01f;
					if(getInstalledFactor() < 1.0)
					{
						addAmt=(float)(CMath.div(100.0, msg.value()) * 0.1);
						if(addAmt < 0.1f)
							addAmt=0.1f;
					}
					setInstalledFactor(this.getInstalledFactor()+addAmt);
					msg.source().tell(msg.source(),this,null,_("<T-NAME> is now enhanced.\n\r"));
				}
				else
				{
					msg.source().tell(msg.source(),this,null,_("Your attempt to enhance <T-NAME> has failed.\n\r"));
				}
				break;
			}
		}
		super.executeMsg(host, msg);
	}

	@Override
	public boolean subjectToWearAndTear()
	{
		return true;
	}
}
