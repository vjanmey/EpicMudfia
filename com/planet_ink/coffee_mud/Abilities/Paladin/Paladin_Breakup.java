package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Paladin_Breakup extends StdAbility
{
	@Override public String ID() { return "Paladin_Breakup"; }
	private final static String localizedName = CMLib.lang()._("Breakup Fight");
	@Override public String name() { return localizedName; }
	private static final String[] triggerStrings =_i(new String[] {"BREAKUP"});
	@Override public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override protected int canAffectCode(){return 0;}
	@Override protected int canTargetCode(){return Ability.CAN_MOBS;}
	@Override public int usageType(){return USAGE_MOVEMENT;}
	@Override public int classificationCode() {   return Ability.ACODE_SKILL|Ability.DOMAIN_LEGAL; }

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat())
		{
			mob.tell(_("You must end combat before trying to break up someone elses fight."));
			return false;
		}
		if((!auto)&&(!(CMLib.flags().isGood(mob))))
		{
			mob.tell(_("You don't feel worthy of a such a good act."));
			return false;
		}
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(!target.isInCombat())
		{
			mob.tell(_("@x1 is not fighting anyone!",target.name(mob)));
			return false;
		}

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,auto?_("<T-NAME> exude(s) a peaceful aura."):_("<S-NAME> break(s) up the fight between <T-NAME> and @x1.",target.getVictim().name()));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.makePeace();
				final MOB victim=target.getVictim();
				if((victim!=null)
				   &&(victim.getVictim()==target))
					victim.makePeace();
			}
		}
		else
			beneficialVisualFizzle(mob,target,_("<S-NAME> attempt(s) to break up <T-NAME>'s fight, but fail(s)."));


		// return whether it worked
		return success;
	}
}
