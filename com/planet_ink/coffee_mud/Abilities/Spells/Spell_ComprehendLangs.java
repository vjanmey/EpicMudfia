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
public class Spell_ComprehendLangs extends Spell
{
	@Override public String ID() { return "Spell_ComprehendLangs"; }
	private final static String localizedName = CMLib.lang()._("Comprehend Languages");
	@Override public String name() { return localizedName; }
	private final static String localizedStaticDisplay = CMLib.lang()._("(Comprehend Languages)");
	@Override public String displayText() { return localizedStaticDisplay; }
	@Override public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override public int classificationCode(){	return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;}


	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			mob.tell(_("You no longer feel so comprehensive."));
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof MOB)
		&&(!msg.amISource((MOB)affected))
		&&(msg.tool() instanceof Ability))
		{
			if((msg.tool().ID().equals("Fighter_SmokeSignals"))
			&&(msg.sourceMinor()==CMMsg.NO_EFFECT)
			&&(msg.targetMinor()==CMMsg.NO_EFFECT)
			&&(msg.othersMessage()!=null))
				msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_OK_VISUAL,_("The smoke signals seem to say '@x1'.",msg.othersMessage()),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
			else
			if(((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
			   ||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL)))
			&&(msg.sourceMessage()!=null)
			&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)
			&&(((MOB)affected).fetchEffect(msg.tool().ID())==null))
			{
				final String str=CMStrings.getSayFromMessage(msg.sourceMessage());
				if(str!=null)
				{
					if(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL))
						msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,msg.othersCode(),_("@x1 (translated from @x2)",CMStrings.substituteSayInMessage(msg.othersMessage(),str),msg.tool().name())));
					else
					if(msg.amITarget(affected)&&(msg.targetMessage()!=null))
						msg.addTrailerMsg(CMClass.getMsg(msg.source(),affected,null,CMMsg.NO_EFFECT,msg.targetCode(),CMMsg.NO_EFFECT,_("@x1 (translated from @x2)",CMStrings.substituteSayInMessage(msg.targetMessage(),str),msg.tool().name())));
					else
					if((msg.othersMessage()!=null)&&(msg.othersMessage().indexOf('\'')>0))
					{
						String otherMes=msg.othersMessage();
						if(msg.target()!=null)
							otherMes=CMLib.coffeeFilter().fullOutFilter(((MOB)affected).session(),(MOB)affected,msg.source(),msg.target(),msg.tool(),otherMes,false);
						msg.addTrailerMsg(CMClass.getMsg(msg.source(),affected,null,CMMsg.NO_EFFECT,msg.othersCode(),CMMsg.NO_EFFECT,_("@x1 (translated from @x2)",CMStrings.substituteSayInMessage(otherMes,str),msg.tool().name())));
					}
				}
			}
		}
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(((MOB)target).isInCombat())
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,_("<S-NAME> already <S-HAS-HAVE> comprehension."));
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
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?_("<T-NAME> feel(s) more comprehending."):_("^S<S-NAME> invoke(s) the power of comprehension!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,_("<S-NAME> attempt(s) to invoke a spell, but fail(s) miserably."));

		// return whether it worked
		return success;
	}
}
