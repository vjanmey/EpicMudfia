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
public class Chant_BlueMoon extends Chant
{
	@Override public String ID() { return "Chant_BlueMoon"; }
	private final static String localizedName = CMLib.lang()._("Blue Moon");
	@Override public String name() { return localizedName; }
	private final static String localizedStaticDisplay = CMLib.lang()._("(Blue Moon)");
	@Override public String displayText() { return localizedStaticDisplay; }
	@Override public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	@Override public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	@Override protected int canAffectCode(){return CAN_ROOMS;}
	@Override protected int canTargetCode(){return 0;}
	@Override public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_MOONALTERING;}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof Room)))
			return;
		final Room R=(Room)affected;
		if(canBeUninvoked())
			R.showHappens(CMMsg.MSG_OK_VISUAL,_("The blue moon sets."));

		super.unInvoke();

	}
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(affected==null) return false;
		if(affected instanceof Room)
		{
			final Room R=(Room)affected;
			if(!R.getArea().getClimateObj().canSeeTheMoon(R,this))
				unInvoke();
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		   &&(msg.target()!=null)
		   &&(msg.target() instanceof MOB))
		{
			final MOB mob=(MOB)msg.target();
			int recovery=(int)Math.round(CMath.div((msg.value()),2.0));
			if(CMLib.flags().isGood(mob)) recovery=recovery*-1;
			msg.setValue(msg.value()+recovery);
		}
		return true;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(!CMLib.flags().isGood(mob))
				return Ability.QUALITY_INDIFFERENT;
			if((mob.isInCombat())&&CMLib.flags().isGood(mob.getVictim()))
				return Ability.QUALITY_INDIFFERENT;
			final Room R=mob.location();
			if(R!=null)
			{
				if(!R.getArea().getClimateObj().canSeeTheMoon(R,null))
					return Ability.QUALITY_INDIFFERENT;
				if(R.fetchEffect(ID())!=null)
					return Ability.QUALITY_INDIFFERENT;
				for(final Enumeration<Ability> a=R.effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=null)
					&&((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_MOONALTERING))
						return Ability.QUALITY_INDIFFERENT;
				}

			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room target=mob.location();
		if(target==null) return false;
		if(!target.getArea().getClimateObj().canSeeTheMoon(target,null))
		{
			mob.tell(_("You must be able to see the moon for this magic to work."));
			return false;
		}
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(_("This place is already under the blue moon."));
			return false;
		}
		for(final Enumeration<Ability> a=target.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_MOONALTERING))
			{
				mob.tell(_("The moon is already under @x1, and can not be changed until this magic is gone.",A.name()));
				return false;
			}
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
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":_("^S<S-NAME> chant(s) to the sky.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,_("The Blue Moon Rises!"));
					beneficialAffect(mob,target,asLevel,0);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,_("<S-NAME> chant(s) to the sky, but the magic fades."));
		// return whether it worked
		return success;
	}
}
