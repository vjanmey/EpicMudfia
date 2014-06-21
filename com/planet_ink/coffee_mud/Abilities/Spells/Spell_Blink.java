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
public class Spell_Blink extends Spell
{
	@Override public String ID() { return "Spell_Blink"; }
	private final static String localizedName = CMLib.lang()._("Blink");
	@Override public String name() { return localizedName; }
	private final static String localizedStaticDisplay = CMLib.lang()._("(Blink spell)");
	@Override public String displayText() { return localizedStaticDisplay; }
	@Override public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override public int classificationCode(){	return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,_("<S-NAME> stop(s) blinking."));
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)&&(affected!=null)&&(affected instanceof MOB))
		{
			final MOB mob=(MOB)affected;
			final int roll=CMLib.dice().roll(1,8,0);
			if(mob.isInCombat())
			{
				int move=0;
				switch(roll)
				{
				case 1: move=-2; break;
				case 2: move=-1; break;
				case 7: move=1; break;
				case 8: move=2; break;
				default: move=0;
				}
				if(move==0)
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,_("<S-NAME> vanish(es) and reappear(s) again."));
				else
				{
					int rangeTo=mob.rangeToTarget();
					rangeTo+=move;
					if((move==0)||(rangeTo<0)||(rangeTo>mob.location().maxRange()))
						move=0;
					else
					{
						mob.setAtRange(rangeTo);
						if(mob.getVictim().getVictim()==mob)
							mob.getVictim().setAtRange(rangeTo);
					}
					switch(move)
					{
					case 0:
						mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,_("<S-NAME> vanish(es) and reappear(s) again."));
						break;
					case 1:
						mob.location().show(mob,null,mob.getVictim(),CMMsg.MSG_OK_VISUAL,_("<S-NAME> vanish(es) and reappear(s) a bit further from <O-NAMESELF>."));
						break;
					case 2:
						mob.location().show(mob,null,mob.getVictim(),CMMsg.MSG_OK_VISUAL,_("<S-NAME> vanish(es) and reappear(s) much further from <O-NAMESELF>."));
						break;
					case -1:
						mob.location().show(mob,null,mob.getVictim(),CMMsg.MSG_OK_VISUAL,_("<S-NAME> vanish(es) and reappear(s) a bit closer to <O-NAMESELF>."));
						break;
					case -2:
						mob.location().show(mob,null,mob.getVictim(),CMMsg.MSG_OK_VISUAL,_("<S-NAME> vanish(es) and reappear(s) much closer to <O-NAMESELF>."));
						break;
					}
				}
				if(mob.getVictim()==null) mob.setVictim(null); // correct range
			}
			else
			if((roll>2)&&(roll<7))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,_("<S-NAME> vanish(es) and reappear(s) a few feet away."));
			else
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,_("<S-NAME> vanish(es) and reappear(s) again."));
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?_("<T-NAME> begin(s) to blink!"):_("^S<S-NAME> cast(s) a spell at <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target.location()==mob.location())
					if((mob.phyStats().level()+(2*getXLEVELLevel(mob)))>5)
						success=beneficialAffect(mob,target,asLevel,(mob.phyStats().level()+(2*getXLEVELLevel(mob)))-4);
					else
						success=beneficialAffect(mob,target,asLevel,mob.phyStats().level()+(2*getXLEVELLevel(mob)));
			}
		}
		else
			return beneficialWordsFizzle(mob,target,_("<S-NAME> cast(s) a spell to <T-NAMESELF>, but the magic fizzles."));

		// return whether it worked
		return success;
	}
}
