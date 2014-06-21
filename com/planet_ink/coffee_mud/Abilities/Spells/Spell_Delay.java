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
@SuppressWarnings({"unchecked","rawtypes"})
public class Spell_Delay extends Spell
{
	@Override public String ID() { return "Spell_Delay"; }
	private final static String localizedName = CMLib.lang()._("Delay");
	@Override public String name() { return localizedName; }
	private final static String localizedStaticDisplay = CMLib.lang()._("(Delay spell)");
	@Override public String displayText() { return localizedStaticDisplay; }
	@Override protected int canAffectCode(){return CAN_ROOMS;}
	@Override protected int canTargetCode(){return CAN_ROOMS;}
	private Ability shooter=null;
	protected Vector parameters=null;
	@Override public int classificationCode(){	return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}
	protected int overridemana(){return Ability.COST_ALL;}
	@Override public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		if((shooter==null)||(parameters==null))
			return;
		if(canBeUninvoked())
		{
			shooter = (Ability)shooter.copyOf();
			final MOB newCaster=CMClass.getMOB("StdMOB");
			newCaster.setName(_("the thin air"));
			newCaster.setDescription(" ");
			newCaster.setDisplayText(" ");
			newCaster.basePhyStats().setLevel(invoker.phyStats().level()+(2*getXLEVELLevel(invoker)));
			newCaster.recoverPhyStats();
			newCaster.recoverCharStats();
			newCaster.setLocation((Room)affected);
			newCaster.addAbility(shooter);
			try
			{
				shooter.setProficiency(100);
				shooter.invoke(newCaster,parameters,null,false,invoker.phyStats().level()+(2*getXLEVELLevel(invoker)));
			}
			catch(final Exception e){Log.errOut("DELAY/"+CMParms.combine(parameters,0),e);}
			newCaster.delAbility(shooter);
			newCaster.setLocation(null);
			newCaster.destroy();
		}
		super.unInvoke();
		if(canBeUninvoked())
		{
			shooter=null;
			parameters=null;
		}
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell(_("You must specify what arcane spell to delay, and any necessary parameters."));
			return false;
		}
		commands.insertElementAt("CAST",0);
		shooter=CMLib.english().getToEvoke(mob,commands);
		parameters=commands;
		if((shooter==null)||((shooter.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_SPELL))
		{
			parameters=null;
			shooter=null;
			mob.tell(_("You don't know any arcane spell by that name."));
			return false;
		}

		if(shooter.enchantQuality()==Ability.QUALITY_MALICIOUS)
		for(int m=0;m<mob.location().numInhabitants();m++)
		{
			final MOB M=mob.location().fetchInhabitant(m);
			if((M!=null)&&(M!=mob)&&(!M.mayIFight(mob)))
			{
				mob.tell(_("You cannot delay that spell here -- there are other players present!"));
				return false;
			}
		}
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Physical target = mob.location();
		if((target.fetchEffect(this.ID())!=null)||(givenTarget!=null))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":_("A delay has already been cast here!"));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			return false;
		}

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob,target,auto), auto?"":_("^S<S-NAME> point(s) and shout(s) 'NOW!'.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.tell(_("You hear a clock start ticking down in your head...20...19..."));
				beneficialAffect(mob,mob.location(),asLevel,5);
				shooter=null;
				parameters=null;
			}
		}
		else
			return beneficialWordsFizzle(mob,null,_("<S-NAME> point(s) and shout(s) 'NOW', but then look(s) frustrated."));

		// return whether it worked
		return success;
	}
}
