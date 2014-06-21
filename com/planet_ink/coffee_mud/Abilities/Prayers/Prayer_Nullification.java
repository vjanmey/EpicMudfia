package com.planet_ink.coffee_mud.Abilities.Prayers;
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
public class Prayer_Nullification extends Prayer
{
	@Override public String ID() { return "Prayer_Nullification"; }
	private final static String localizedName = CMLib.lang()._("Nullification");
	@Override public String name() { return localizedName; }
	@Override public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_NEUTRALIZATION;}
	@Override public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	@Override public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	@Override protected int canAffectCode(){return 0;}
	@Override protected int canTargetCode(){return 0;}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				for(final Enumeration<Ability> a=target.effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=null)&&(A.canBeUninvoked())&&(!A.isAutoInvoked())
					&&(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
					   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
					   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
					   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)))
					{
						if((A.invoker()!=null)&&((A.invoker().phyStats().level()<=(mob.phyStats().level()+(2*super.getXLEVELLevel(mob))))))
							if((mob==target)&&(A.invoker()!=mob)&&(A.abstractQuality()==Ability.QUALITY_MALICIOUS))
								return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
							else
							if((mob.getVictim()==target)&&(A.invoker()!=mob)&&(A.abstractQuality()!=Ability.QUALITY_MALICIOUS))
								return super.castingQuality(mob, target,Ability.QUALITY_MALICIOUS);
					}
				}
			}
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			final MOB target=mob.location().fetchInhabitant(i);
			if((target!=null)&&(success))
			{
				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?_("<T-NAME> become(s) nullified."):_("^S<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>.^?"));
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					Ability revokeThis=null;
					boolean foundSomethingAtLeast=false;
					for(int a=0;a<target.numEffects();a++) // personal affects
					{
						final Ability A=target.fetchEffect(a);
						if((A!=null)&&(A.canBeUninvoked())&&(!A.isAutoInvoked())
						&&(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
						   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
						   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
						   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)))
						{
							foundSomethingAtLeast=true;
							if((A.invoker()!=null)&&((A.invoker().phyStats().level()<=(mob.phyStats().level()+(2*super.getXLEVELLevel(mob))))))
								revokeThis=A;
						}
					}

					if(revokeThis==null)
					{
						if(foundSomethingAtLeast)
							mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,_("The magic on <T-NAME> appears too powerful to be nullified."));
						else
						if(auto)
							mob.tell(mob,target,null,_("Nothing seems to be happening to <T-NAME>."));
					}
					else
						revokeThis.unInvoke();
				}
			}
			else
				beneficialWordsFizzle(mob,target,auto?"":_("<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>, but @x1 does not heed.",hisHerDiety(mob)));
		}

		// return whether it worked
		return success;
	}
}
