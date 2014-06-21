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
public class Chant_MeteorStrike extends Chant
{
	@Override public String ID() { return "Chant_MeteorStrike"; }
	private final static String localizedName = CMLib.lang()._("Meteor Strike");
	@Override public String name() { return localizedName; }
	@Override public int maxRange(){return adjustedMaxInvokerRange(5);}
	@Override public int minRange(){return 1;}
	@Override public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_ROCKCONTROL;}
	@Override public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			final Set<MOB> h=properTargets(mob,target,false);
			if(h==null)
				return Ability.QUALITY_INDIFFERENT;

			final Room R=mob.location();
			if(R!=null)
			{
				if((R.domainType()&Room.INDOORS)>0)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Set<MOB> h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell(_("There doesn't appear to be anyone here worth striking at."));
			return false;
		}
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell(_("You must be outdoors to strike with meteors."));
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{

			if(mob.location().show(mob,null,this,verbalCastCode(mob,null,auto),_(auto?"A devastating meteor shower erupts!":"^S<S-NAME> chant(s) for a devastating meteor shower!^?")+CMLib.protocol().msp("meteor.wav",40)))
				for (final Object element : h)
				{
					final MOB target=(MOB)element;

					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						invoker=mob;

						int damage = 0;
						final int maxDie =  (adjustedLevel( mob, asLevel )+(2*super.getX1Level(mob))) / 2;
						damage = CMLib.dice().roll(maxDie,6,30);
						if(msg.value()>0)
							damage = (int)Math.round(CMath.div(damage,2.0));
						if(target.location()==mob.location())
							CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BASHING,"The meteors <DAMAGE> <T-NAME>!");
					}
				}
		}
		else
			return maliciousFizzle(mob,null,_("<S-NAME> chant(s) to the sky, but nothing happens."));


		// return whether it worked
		return success;
	}
}
