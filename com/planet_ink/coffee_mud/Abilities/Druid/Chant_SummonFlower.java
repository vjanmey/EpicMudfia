package com.planet_ink.coffee_mud.Abilities.Druid;
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

public class Chant_SummonFlower extends Chant_SummonPlants
{
	@Override public String ID() { return "Chant_SummonFlower"; }
	private final static String localizedName = CMLib.lang()._("Summon Flower");
	@Override public String name() { return localizedName; }
	@Override public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTGROWTH;}
	@Override public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	@Override protected int canAffectCode(){return CAN_ITEMS;}
	@Override protected int canTargetCode(){return 0;}
	protected boolean processing=false;

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.amITarget(littlePlants))
		&&(!processing)
		&&((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_PUSH)||(msg.targetMinor()==CMMsg.TYP_PULL)))
		{
			processing=true;
			final Ability A=littlePlants.fetchEffect(ID());
			if(A!=null)
			{
				CMLib.threads().deleteTick(A,-1);
				littlePlants.delEffect(A);
				littlePlants.setSecretIdentity("");
			}
			if(littlePlants.fetchBehavior("Decay")==null)
			{
				final Behavior B=CMClass.getBehavior("Decay");
				B.setParms("min="+CMProps.getIntVar(CMProps.Int.TICKSPERMUDMONTH)+" max="+CMProps.getIntVar(CMProps.Int.TICKSPERMUDMONTH)+" chance=100");
				littlePlants.addBehavior(B);
				B.executeMsg(myHost,msg);
			}
			processing=false;
		}
	}
	public Item buildFlower(MOB mob, Room room)
	{
		final Item newItem=CMClass.getItem("GenItem");
		newItem.setMaterial(RawMaterial.RESOURCE_GREENS);
		switch(CMLib.dice().roll(1,5,0))
		{
		case 1:
			newItem.setName(_("a red rose"));
			newItem.setDisplayText(_("a red rose is growing here."));
			newItem.setDescription("");
			break;
		case 2:
			newItem.setName(_("a nice daisy"));
			newItem.setDisplayText(_("a nice daisy is growing here."));
			newItem.setDescription("");
			break;
		case 3:
			newItem.setName(_("a white carnation"));
			newItem.setDisplayText(_("a beautiful white carnation is growing here"));
			newItem.setDescription("");
			break;
		case 4:
			newItem.setName(_("a happy sunflower"));
			newItem.setDisplayText(_("a happy sunflower is growing here."));
			newItem.setDescription(_("Happy flowers have little yellow blooms."));
			break;
		case 5:
			newItem.setName(_("a lovely gladiola"));
			newItem.setDisplayText(_("a lovely gladiola is growing here."));
			newItem.setDescription("");
			break;
		}
		newItem.setSecretIdentity(mob.Name());
		newItem.setMiscText(newItem.text());
		room.addItem(newItem);
		final Chant_SummonFlower newChant=new Chant_SummonFlower();
		newItem.basePhyStats().setLevel(10+newChant.getX1Level(mob));
		newItem.setExpirationDate(0);
		room.showHappens(CMMsg.MSG_OK_ACTION,CMLib.lang()._("Suddenly, @x1 sprout(s) up here.",newItem.name()));
		newChant.PlantsLocation=room;
		newChant.littlePlants=newItem;
		if(CMLib.law().doesOwnThisProperty(mob,room))
		{
			newChant.setInvoker(mob);
			newChant.setMiscText(mob.Name());
			newItem.addNonUninvokableEffect(newChant);
		}
		else
			newChant.beneficialAffect(mob,newItem,0,(newChant.adjustedLevel(mob,0)*240)+450);
		room.recoverPhyStats();
		return newItem;
	}

	@Override
	protected Item buildMyPlant(MOB mob, Room room)
	{
		return buildFlower(mob,room);
	}
}
