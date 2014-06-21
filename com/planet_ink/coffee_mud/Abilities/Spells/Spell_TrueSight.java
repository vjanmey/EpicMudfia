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
public class Spell_TrueSight extends Spell
{
	@Override public String ID() { return "Spell_TrueSight"; }
	private final static String localizedName = CMLib.lang()._("True Sight");
	@Override public String name() { return localizedName; }
	private final static String localizedStaticDisplay = CMLib.lang()._("(True Sight)");
	@Override public String displayText() { return localizedStaticDisplay; }
	@Override public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell(_("You no longer have true sight."));
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		if(!(affected instanceof MOB))
		   return true;
		if((msg.amISource((MOB)affected))
		&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
		&&(msg.target()!=null)
		&&(!msg.target().name().equals(msg.target().Name())))
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,this,CMMsg.MSG_OK_VISUAL,_("@x1 is truly @x2.",msg.target().name(),msg.target().Name()),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
		return true;
	}


	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_OVERLOOKING,affected.phyStats().level()+(2*getXLEVELLevel(invoker()))+200+affectableStats.getStat(CharStats.STAT_SAVE_OVERLOOKING));
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_HIDDEN);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_INVISIBLE);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_SNEAKERS);
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat()&&(!CMLib.flags().canBeSeenBy(mob.getVictim(),mob)))
				return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,_("<S-NAME> already <S-HAS-HAVE>  true sight."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?_("<T-NAME> gain(s) true sight!"):_("^S<S-NAME> incant(s) softly, and gain(s) true sight!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,_("<S-NAME> incant(s) and open(s) <S-HIS-HER> eyes, but the spell fizzles."));

		return success;
	}
}
