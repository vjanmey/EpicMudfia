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
public class Spell_FakeFood extends Spell
{
	@Override public String ID() { return "Spell_FakeFood"; }
	private final static String localizedName = CMLib.lang()._("Fake Food");
	@Override public String name() { return localizedName; }
	@Override protected int canTargetCode(){return 0;}
	@Override public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;}
	@Override public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":_("^S<S-NAME> invoke(s) a spell dramatically.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Food F=(Food)CMClass.getItem("GenFood");
				switch(CMLib.dice().roll(1,5,0))
				{
				case 1: F.setName(_("a shiny apple"));
						F.setDisplayText(_("A shiny red apple sits here."));
						F.setDescription(_("It looks tasty and crisp!"));
						break;
				case 2: F.setName(_("a nice peach"));
						F.setDisplayText(_("A nice peach sits here."));
						F.setDescription(_("It looks tasty!"));
						break;
				case 3: F.setName(_("a big pot pie"));
						F.setDisplayText(_("A big pot pie has been left here."));
						F.setDescription(_("It sure looks good!"));
						break;
				case 4: F.setName(_("a juicy steak"));
						F.setDisplayText(_("A juicy steak has been left here."));
						F.setDescription(_("It sure looks good!"));
						break;
				case 5: F.setName(_("a bit of food"));
						F.setDisplayText(_("A bit of food has been left here."));
						F.setDescription(_("It sure looks good!"));
						break;
				}
				F.setNourishment(0);
				F.setBaseValue(0);
				for(int f=0;f<5;f++)
				{
					final Food F2=(Food)F.copyOf();
					F2.recoverPhyStats();
					mob.location().addItem(F2,ItemPossessor.Expire.Resource);
					mob.location().show(mob,null,F2,CMMsg.MSG_OK_VISUAL,_("<O-NAME> appears!"));
				}
			}
		}
		else
			beneficialVisualFizzle(mob,null,_("<S-NAME> dramatically attempt(s) to invoke a spell, but fizzle(s) the spell."));


		// return whether it worked
		return success;
	}
}
