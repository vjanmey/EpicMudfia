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
public class Spell_GravitySlam extends Spell
{
	@Override public String ID() { return "Spell_GravitySlam"; }
	private final static String localizedName = CMLib.lang()._("Gravity Slam");
	@Override public String name() { return localizedName; }
	@Override public int maxRange(){return adjustedMaxInvokerRange(5);}
	@Override public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	@Override public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;}
	@Override public long flags(){return Ability.FLAG_MOVING;}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),_(auto?"":"^S<S-NAME> incant(s) and point(s) at <T-NAMESELF>!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;

				int damage = 0;
				int maxDie =  (int)Math.round((adjustedLevel(mob,asLevel)+(2.0*super.getX1Level(mob)))/2.0);
				if(!CMLib.flags().isInFlight(target))
					maxDie=maxDie/2;
				final Room R=mob.location();
				if((R.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
				||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER))
					maxDie=maxDie/6;
				if((R.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
				||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
					maxDie=maxDie/4;

				damage += CMLib.dice().roll(maxDie,20,6+maxDie);
				if(msg.value()>0)
					damage = (int)Math.round(CMath.div(damage,2.0));
				if(!CMLib.flags().isInFlight(target))
					mob.location().show(target,null,CMMsg.MSG_OK_ACTION,_("<S-NAME> <S-IS-ARE> hurled up into the air and **SLAMMED** back down!"));
				else
					mob.location().show(target,null,CMMsg.MSG_OK_ACTION,_("<S-NAME> <S-IS-ARE> hurled even higher into the air and **SLAMMED** back down!"));

				if(target.location()==mob.location())
					CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,Weapon.TYPE_BASHING,"The fall <DAMAGE> <T-NAME>!");
			}
		}
		else
			return maliciousFizzle(mob,target,_("<S-NAME> incant(s) and point(s) at <T-NAMESELF>, but flub(s) the spell."));


		// return whether it worked
		return success;
	}
}
