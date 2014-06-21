package com.planet_ink.coffee_mud.Abilities.Misc;
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
public class Paralysis extends StdAbility implements HealthCondition
{
	@Override public String ID() { return "Paralysis"; }
	private final static String localizedName = CMLib.lang()._("Paralysis");
	@Override public String name() { return localizedName; }
	private final static String localizedStaticDisplay = CMLib.lang()._("(Paralyzed)");
	@Override public String displayText() { return localizedStaticDisplay; }
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override protected int canTargetCode(){return CAN_MOBS;}
	@Override public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	@Override public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings =_i(new String[] {"PARALYZE"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public int classificationCode(){return Ability.ACODE_SKILL;}
	@Override public long flags(){return Ability.FLAG_UNHOLY|Ability.FLAG_PARALYZING;}

	@Override
	public String getHealthConditionDesc()
	{
		return "Paralysis";
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;

		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_MOVE);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell(_("The paralysis eases out of your muscles."));
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(!CMLib.flags().canMove((MOB)target))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_PARALYZE|(auto?CMMsg.MASK_ALWAYS:0),auto?"":_("^S<S-NAME> paralyze(s) <T-NAMESELF>.^?"));
			if(target.location().okMessage(target,msg))
			{
				target.location().send(target,msg);
				if(msg.value()<=0)
				{
					int levelDiff=(adjustedLevel(mob, asLevel)-target.phyStats().level());
					if(levelDiff<0) levelDiff=0;
					success=maliciousAffect(mob,target,asLevel,10 + (levelDiff/10),-1);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,_("<S-NAME> can't move!"));
				}
			}
		}
		else
			return maliciousFizzle(mob,target,_("<S-NAME> attempt(s) to paralyze <T-NAMESELF>, but fail(s)!"));


		// return whether it worked
		return success;
	}
}