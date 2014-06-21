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
public class Spell_MindLight extends Spell
{
	@Override public String ID() { return "Spell_MindLight"; }
	private final static String localizedName = CMLib.lang()._("Mind Light");
	@Override public String name() { return localizedName; }
	private final static String localizedStaticDisplay = CMLib.lang()._("(Mind Light spell)");
	@Override public String displayText() { return localizedStaticDisplay; }
	@Override protected int canAffectCode(){return CAN_ROOMS;}
	@Override protected int canTargetCode(){return CAN_ROOMS;}
	@Override public int classificationCode(){	return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;}
	@Override public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		final Room room=(Room)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			room.showHappens(CMMsg.MSG_OK_VISUAL, _("The mind light starts to fade."));
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(!(affected instanceof Room)) return true;
		final Room R=(Room)affected;
		if((invoker()!=null)&&(canBeUninvoked()))
		{
			if(!R.isInhabitant(invoker()))
			   unInvoke();
			return false;
		}
		for(int m=0;m<R.numInhabitants();m++)
		{
			final MOB M=R.fetchInhabitant(m);
			if(M!=null)
			{
				if(invoker()!=null)
					M.curState().adjMana((invoker().charStats().getStat(CharStats.STAT_INTELLIGENCE)+invoker().charStats().getStat(CharStats.STAT_WISDOM))/2,M.maxState());
				else
					M.curState().adjMana((M.charStats().getStat(CharStats.STAT_INTELLIGENCE)+M.charStats().getStat(CharStats.STAT_WISDOM))/2,M.maxState());
			}
		}
		return true;
	}


	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Physical target = mob.location();

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(mob,null,null,_("The Mind Light is already here!"));
			return false;
		}


		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob,target,auto), _((auto?"T":"^S<S-NAME> incant(s) and gesture(s) and t")+"he mind light envelopes everyone.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob.location(),asLevel,0);
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,_("<S-NAME> incant(s) lightly, but the spell fizzles."));

		// return whether it worked
		return success;
	}
}
