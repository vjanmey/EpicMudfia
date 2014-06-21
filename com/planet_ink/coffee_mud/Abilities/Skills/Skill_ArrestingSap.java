package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Thief.Thief_Sap;
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
public class Skill_ArrestingSap extends StdSkill implements HealthCondition
{
	@Override public String ID() { return "Skill_ArrestingSap"; }
	private final static String localizedName = CMLib.lang()._("Arresting Sap");
	@Override public String name() { return localizedName; }
	@Override public String displayText() { return _("(Knocked out: "+tickDown+")"); }
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override protected int canTargetCode(){return CAN_MOBS;}
	@Override public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings =_i(new String[] {"ASAP"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	protected int enhancement=0;
	@Override public int abilityCode(){return enhancement;}
	@Override public void setAbilityCode(int newCode){enhancement=newCode;}
	@Override public int usageType(){return USAGE_MOVEMENT;}
	protected boolean utterSafety=false;
	@Override public int classificationCode() {   return Ability.ACODE_SKILL|Ability.DOMAIN_LEGAL; }

	@Override
	public String getHealthConditionDesc()
	{
		return "Unconscious";
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amISource(mob))&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS)))
		{
			if((msg.sourceMajor(CMMsg.MASK_EYES))
			||(msg.sourceMajor(CMMsg.MASK_HANDS))
			||(msg.sourceMajor(CMMsg.MASK_MOUTH))
			||(msg.sourceMajor(CMMsg.MASK_MOVE)))
			{
				if(msg.sourceMessage()!=null)
					mob.tell(_("You are way too drowsy."));
				return false;
			}
		}
		if(utterSafety)
		{
			if((msg.source()==affected)&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
				return false;
			if((CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS)
			&&(msg.target()==affected)
			&&(affected instanceof MOB)))
			{
				if((!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))&&(affected!=msg.source()))
					msg.source().tell((MOB)affected,null,null,_("<S-NAME> is already out!"));
				makeMyPeace((MOB)affected);
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SLEEPING);
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(!(target instanceof MOB)) return Ability.QUALITY_INDIFFERENT;
			final MOB targetM=(MOB)target;
			if(mob.baseWeight()<(targetM.baseWeight()-450))
				return Ability.QUALITY_INDIFFERENT;
			if(Skill_Arrest.getWarrantsOf(targetM, CMLib.law().getLegalObject(mob.location().getArea())).size()==0)
				return Ability.QUALITY_INDIFFERENT;
			return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
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
			if((mob.location()!=null)&&(!mob.amDead()))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,_("<S-NAME> regain(s) consciousness."));
				CMLib.commands().postStand(mob,true);
				if((utterSafety)&&(mob.isMonster()))
					CMLib.tracking().wanderAway(mob,false,true);
			}
			else
				mob.tell(_("You regain consciousness."));
		}
	}

	public void makeMyPeace(MOB target)
	{
		target.makePeace();
		final Room R=target.location();
		if(R!=null)
			for(int i=0;i<R.numInhabitants();i++)
			{
				final MOB M=R.fetchInhabitant(i);
				if((M!=null)&&(M.getVictim()==target))
					M.setVictim(null);
			}
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		boolean safety=false;
		int ticks=3;
		if(auto)
		{
			if(commands!=null)
			for(int c=commands.size()-1;c>=0;c--)
			{
				if(CMath.isInteger((String)commands.elementAt(c)))
				{
					ticks=CMath.s_int((String)commands.elementAt(c));
					commands.removeElementAt(c);
				}
				else
				if(((String)commands.elementAt(c)).equalsIgnoreCase("SAFELY"))
				{
					safety=true;
					commands.removeElementAt(c);
				}
			}
		}

		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(!auto)
		{
			if(mob.baseWeight()<(target.baseWeight()-450))
			{
				mob.tell(_("@x1 is way to big to knock out!",target.name(mob)));
				return false;
			}
			if(Skill_Arrest.getWarrantsOf(target, CMLib.law().getLegalObject(mob.location().getArea())).size()==0)
			{
				mob.tell(_("@x1 has no warrants out here.",target.name(mob)));
				return false;
			}
		}
		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*super.getXLEVELLevel(mob)));
		if(levelDiff>0)
			levelDiff=levelDiff*3;
		else
			levelDiff=0;
		levelDiff-=(abilityCode()*mob.charStats().getStat(CharStats.STAT_STRENGTH));

		// now see if it worked
		boolean success=proficiencyCheck(mob,(-levelDiff)+(-((target.charStats().getStat(CharStats.STAT_STRENGTH)-mob.charStats().getStat(CharStats.STAT_STRENGTH)))),auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:CMMsg.MASK_MALICIOUS),(mob==target)?_("<T-NAME> hit(s) the floor!"):_("^F^<FIGHT^><S-NAME> rear(s) back and sap(s) <T-NAMESELF>, knocking <T-HIM-HER> out!^</FIGHT^>^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(target.riding()!=null)
				msg.addTrailerMsg(CMClass.getMsg(target,target.riding(),CMMsg.TYP_DISMOUNT,null));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target.riding()!=null)
					target.setRiding(null);
				success=maliciousAffect(mob,target,asLevel,ticks,-1);
				if(mob.getVictim()==target) mob.setVictim(null);
				final Skill_ArrestingSap A=(Skill_ArrestingSap)target.fetchEffect(ID());
				if(A!=null) A.utterSafety=safety;
				if(safety) makeMyPeace(target);
			}
		}
		else
			return maliciousFizzle(mob,target,_("<S-NAME> rear(s) back and attempt(s) to knock <T-NAMESELF> out, but fail(s)."));

		// return whether it worked
		return success;
	}
}
