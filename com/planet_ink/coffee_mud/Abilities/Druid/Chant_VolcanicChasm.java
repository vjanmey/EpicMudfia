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
public class Chant_VolcanicChasm extends Chant
{
	@Override public String ID() { return "Chant_VolcanicChasm"; }
	private final static String localizedName = CMLib.lang()._("Volcanic Chasm");
	@Override public String name() { return localizedName; }
	@Override public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_DEEPMAGIC;}
	@Override public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	@Override protected int canAffectCode(){return Ability.CAN_ROOMS;}
	@Override protected int canTargetCode(){return Ability.CAN_ROOMS;}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof Room))
		{
			final Room R=(Room)affected;
			for(int i=0;i<R.numInhabitants();i++)
			{
				final MOB M=R.fetchInhabitant(i);
				if((M!=null)&&(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_FIRE)))
				{
					CMLib.combat().postDamage(invoker(),M,this,CMLib.dice().roll(1,M.phyStats().level()+(2*super.getXLEVELLevel(invoker())),1),CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_MELTING,"The extreme heat <DAMAGES> <T-NAME>!");
					if((!M.isInCombat())
					&&(M!=invoker)
					&&(!M.amDead())
					&&(!M.amDestroyed())
					&&(invoker!=null)
					&&(R.isInhabitant(invoker))
					&&(CMLib.flags().canBeSeenBy(invoker,M)))
						CMLib.combat().postAttack(M,invoker,M.fetchWieldedItem());
				}
			}
			for(int i=0;i<R.numItems();i++)
			{
				final Item I=R.getItem(i);
				if((I!=null)&&(!CMLib.flags().isOnFire(I)))
				{
					final Ability A=CMClass.getAbility("Burning");
					if(A!=null)	A.invoke(invoker(),I,true,0);
				}
			}
		}
		return super.tick(ticking, tickID);
	}

	protected boolean checked=false;
	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((!checked)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(affected instanceof Room))
		{
			checked=true;
			if(!CMLib.threads().isTicking(this,-1))
				CMLib.threads().startTickDown(this,Tickable.TICKID_SPELL_AFFECT,1);
		}
		super.executeMsg(host,msg);
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			final Room R=mob.location();
			if(R!=null)
			{
				if(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room target=mob.location();
		if(target==null) return false;
		if((!auto)
		&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE))
		{
			mob.tell(_("This chant only works in caves."));
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
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":_("^S<S-NAME> chant(s) to the walls.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					for(int i=0;i<target.numInhabitants();i++)
					{
						final MOB M=target.fetchInhabitant(i);
						if((M!=null)&&(mob!=M))
							mob.location().show(mob,M,CMMsg.MASK_MALICIOUS|CMMsg.TYP_OK_VISUAL,null);
					}
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,_("Flames and sulfurous steam leap from cracks opening around you!"));
					maliciousAffect(mob,target,asLevel,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,_("<S-NAME> chant(s) the walls, but the magic fades."));
		// return whether it worked
		return success;
	}
}
