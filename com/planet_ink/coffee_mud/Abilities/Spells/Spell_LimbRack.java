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
public class Spell_LimbRack extends Spell
{
	@Override public String ID() { return "Spell_LimbRack"; }
	private final static String localizedName = CMLib.lang()._("Limb Rack");
	@Override public String name() { return localizedName; }
	private final static String localizedStaticDisplay = CMLib.lang()._("(Being pulled apart)");
	@Override public String displayText() { return localizedStaticDisplay; }
	@Override public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}
	public List<String> limbsToRemove=new Vector<String>();

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(invoker==null) return false;
		final MOB mob=(MOB)affected;
		if((mob.location()!=null)
		&&(mob.charStats().getMyRace().bodyMask()[Race.BODY_ARM]>=0)
		&&(mob.charStats().getMyRace().bodyMask()[Race.BODY_LEG]>=0))
		{
			final String str=(text().equalsIgnoreCase("ARMSONLY"))?
				_("<T-NAME> <T-IS-ARE> having <T-HIS-HER> arms pulled from <T-HIS-HER> body!"):_("<T-NAME> <T-IS-ARE> having <T-HIS-HER> arms and legs pulled from <T-HIS-HER> body!");
			CMLib.combat().postDamage(invoker,mob,this,mob.maxState().getHitPoints()/(10-(getXLEVELLevel(invoker)/2)),CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,Weapon.TYPE_BURSTING,str);
		}

		return true;
	}

	@Override
	public void unInvoke()
	{
		if((affected instanceof MOB)
		&&(((MOB)affected).amDead()))
		{
			final MOB mob=(MOB)affected;
			if((mob.location()!=null)
			&&(mob.charStats().getMyRace().bodyMask()[Race.BODY_ARM]>0)
			&&(mob.charStats().getMyRace().bodyMask()[Race.BODY_LEG]>0))
			{
				if(text().equalsIgnoreCase("ARMSONLY"))
					mob.location().show(mob,null,null,CMMsg.MSG_OK_VISUAL,_("<S-NAME> has <S-HIS-HER> arms TORN OFF!"));
				else
					mob.location().show(mob,null,null,CMMsg.MSG_OK_VISUAL,_("<S-NAME> has <S-HIS-HER> arms and legs TORN OFF!"));
				Amputator A=(Amputator)mob.fetchEffect("Amputation");
				if(A==null) A=(Amputator)CMClass.getAbility("Amputation");
				boolean success=true;
				for(int i=0;i<limbsToRemove.size();i++)
					success=success && (A.amputate(mob,A,limbsToRemove.get(i))!=null);
				if(success)
				{
					if(mob.fetchEffect(A.ID())==null)
						mob.addNonUninvokableEffect(A);
				}
			}
			CMLib.utensils().confirmWearability(mob);
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		Amputator A=(Amputator)target.fetchEffect("Amputation");
		if(A==null)	A=(Amputator)CMClass.getAbility("Amputation");
		final List<String> remainingLimbList=A.remainingLimbNameSet(target);
		for(int i=remainingLimbList.size()-1;i>=0;i--)
		{
			final String gone=remainingLimbList.get(i);
			if((!gone.toUpperCase().endsWith(" ARM"))
			&&(!gone.toUpperCase().endsWith(" LEG")))
				remainingLimbList.remove(i);
		}
		if((remainingLimbList.size()==0)
		||((target.charStats().getMyRace().bodyMask()[Race.BODY_ARM]<=0)
		&&(target.charStats().getMyRace().bodyMask()[Race.BODY_LEG]<=0)))
		{
			if(!auto)
				mob.tell(_("There is nothing left on @x1 to rack off!",target.name(mob)));
			return false;
		}

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
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),_(auto?"!":"^S<S-NAME> invoke(s) a stretching spell upon <T-NAMESELF>"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					super.maliciousAffect(mob,target,asLevel,12,-1);
					final Ability A2=target.fetchEffect(ID());
					if(A2!=null)
					{
						((Spell_LimbRack)A2).limbsToRemove=new Vector<String>();
						((Spell_LimbRack)A2).limbsToRemove.addAll(remainingLimbList);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,_("<S-NAME> incant(s) stretchingly at <T-NAMESELF>, but flub(s) the spell."));


		// return whether it worked
		return success;
	}
}
