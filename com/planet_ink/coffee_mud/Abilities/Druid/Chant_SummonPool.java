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

@SuppressWarnings("rawtypes")
public class Chant_SummonPool extends Chant
{
	@Override public String ID() { return "Chant_SummonPool"; }
	private final static String localizedName = CMLib.lang()._("Summon Pool");
	@Override public String name() { return localizedName; }
	@Override protected int canAffectCode(){return Ability.CAN_ITEMS;}
	@Override protected int canTargetCode(){return 0;}
	@Override public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_WEATHER_MASTERY;}
	@Override public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected Room SpringLocation=null;
	protected Item littleSpring=null;

	@Override
	public void unInvoke()
	{
		if(SpringLocation==null)
			return;
		if(littleSpring==null)
			return;
		if(canBeUninvoked())
			SpringLocation.showHappens(CMMsg.MSG_OK_VISUAL,_("The little pool dries up."));
		super.unInvoke();
		if(canBeUninvoked())
		{
			final Item spring=littleSpring; // protects against uninvoke loops!
			littleSpring=null;
			spring.destroy();
			SpringLocation.recoverRoomStats();
			SpringLocation=null;
		}
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
		{
			mob.tell(_("This magic only works in caves."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":_("^S<S-NAME> chant(s) for a pool.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final String itemID = "Spring";

				final Item newItem=CMClass.getItem(itemID);
				if(newItem==null)
				{
					mob.tell(_("There's no such thing as a '@x1'.\n\r",itemID));
					return false;
				}
				newItem.setName(_("a magical pool"));
				newItem.setDisplayText(_("a little magical pool flows here."));
				newItem.setDescription(_("The pool is coming magically from the ground.  The water looks pure and clean."));

				mob.location().addItem(newItem);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,_("Suddenly, @x1 starts flowing here.",newItem.name()));
				SpringLocation=mob.location();
				littleSpring=newItem;
				beneficialAffect(mob,newItem,asLevel,0);
				mob.location().recoverPhyStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,_("<S-NAME> chant(s) for a pool, but nothing happens."));

		// return whether it worked
		return success;
	}
}
