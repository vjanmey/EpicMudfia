package com.planet_ink.coffee_mud.Abilities.Spells;
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
public class Spell_MassHaste extends Spell
{
	@Override public String ID() { return "Spell_MassHaste"; }
	private final static String localizedName = CMLib.lang()._("Mass Haste");
	@Override public String name() { return localizedName; }
	@Override public String displayText(){return "";}
	@Override public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	@Override protected int canAffectCode(){return 0;}
	@Override public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Set<MOB> h=properTargets(mob,givenTarget,false);
		if(h==null)
		{
			mob.tell(_("There doesn't appear to be anyone here worth speeding up."));
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
			if(mob.location().show(mob,null,this,verbalCastCode(mob,null,auto),auto?"":_("^S<S-NAME> wave(s) <S-HIS-HER> arms and speak(s) quickly.^?")))
				for (final Object element : h)
				{
					final MOB target=(MOB)element;

					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
					if((mob.location().okMessage(mob,msg))
					   &&(target.fetchEffect("Spell_Haste")==null)
					   &&(target.fetchEffect("Spell_MassHaste")==null))
					{
						mob.location().send(mob,msg);
						mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,_("<S-NAME> speed(s) up!"));
						final Spell_Haste haste=new Spell_Haste();
						haste.setProficiency(proficiency());
						haste.beneficialAffect(mob,target,asLevel,0);
					}
				}
		}
		else
			return beneficialWordsFizzle(mob,null,_("<S-NAME> wave(s) <S-HIS-HER> arms and speak(s) quickly, but the spell fizzles."));


		// return whether it worked
		return success;
	}
}
