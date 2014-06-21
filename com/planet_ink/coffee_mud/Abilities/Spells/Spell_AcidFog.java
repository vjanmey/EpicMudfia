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
public class Spell_AcidFog extends Spell
{
	@Override public String ID() { return "Spell_AcidFog"; }
	private final static String localizedName = CMLib.lang()._("Acid Fog");
	@Override public String name() { return localizedName; }
	private final static String localizedStaticDisplay = CMLib.lang()._("(Acid Fog)");
	@Override public String displayText() { return localizedStaticDisplay; }
	@Override public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override protected int canTargetCode(){return CAN_MOBS;}
	@Override public int minRange(){return 2;}
	@Override public int maxRange(){return adjustedMaxInvokerRange(5);}
	Room castingLocation=null;
	@Override public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}
	@Override public long flags(){return Ability.FLAG_EARTHBASED;}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)
		&&(affected!=null)
		&&(affected instanceof MOB))
		{
			final MOB M=(MOB)affected;
			if(M.location()!=castingLocation)
				unInvoke();
			else
			if((!M.amDead())&&(M.location()!=null))
			{
				final int damage=M.phyStats().level()+super.getXLEVELLevel(invoker())+(2*super.getX1Level(invoker()));
				CMLib.combat().postDamage(invoker,M,this,CMLib.dice().roll(1,damage,0),CMMsg.TYP_ACID,-1,"<T-NAME> sizzle(s) in the acid fog!");
				if((!M.isInCombat())&&(M!=invoker)&&(M.location()!=null)&&(M.location().isInhabitant(invoker))&&(CMLib.flags().canBeSeenBy(invoker,M)))
					CMLib.combat().postAttack(M,invoker,M.fetchWieldedItem());
			}
		}
		return super.tick(ticking,tickID);
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
		{
			if((!mob.amDead())&&(mob.location()!=null))
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,_("<S-NAME> manage(s) to escape the acid fog!"));
		}
	}


	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Set<MOB> h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell(_("There doesn't appear to be anyone here worth melting."));
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,verbalCastCode(mob,null,auto),auto?_("A horrendous cloud of acid appears!"):_("^S<S-NAME> incant(s) and wave(s) <S-HIS-HER> arms around.^?")))
				for (final Object element : h)
				{
					final MOB target=(MOB)element;

					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
					final CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_ACID|(auto?CMMsg.MASK_ALWAYS:0),null);
					if((mob.location().okMessage(mob,msg))
					   &&(mob.location().okMessage(mob,msg2))
					   &&(target.fetchEffect(this.ID())==null))
					{
						mob.location().send(mob,msg);
						mob.location().send(mob,msg2);
						if((msg.value()<=0)&&(msg2.value()<=0)&&(target.location()==mob.location()))
						{
							castingLocation=mob.location();
							success=maliciousAffect(mob,target,asLevel,((mob.phyStats().level()+super.getXLEVELLevel(mob)+(2*super.getX1Level(mob)))*10),-1);
							target.location().show(target,null,CMMsg.MSG_OK_ACTION,_("<S-NAME> become(s) enveloped in the acid fog!"));
						}
					}
				}
		}
		else
			return maliciousFizzle(mob,null,_("<S-NAME> incant(s), but the spell fizzles."));


		// return whether it worked
		return success;
	}
}