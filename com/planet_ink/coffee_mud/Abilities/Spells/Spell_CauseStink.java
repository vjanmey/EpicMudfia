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
public class Spell_CauseStink extends Spell
{
	@Override public String ID() { return "Spell_CauseStink"; }
	private final static String localizedName = CMLib.lang()._("Cause Stink");
	@Override public String name() { return localizedName; }
	private final static String localizedStaticDisplay = CMLib.lang()._("(Cause Stink)");
	@Override public String displayText() { return localizedStaticDisplay; }
	@Override public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	@Override protected int canTargetCode(){return CAN_MOBS;}
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_TRANSMUTATION;}
	public int cycle=1;

	@Override
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		super.affectCharStats(affectedMob, affectableStats);
		final int amount=affectableStats.getStat(CharStats.STAT_CHARISMA)/2;
		affectableStats.setStat(CharStats.STAT_CHARISMA, amount);
		affectableStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ, affectableStats.getStat(CharStats.STAT_MAX_CHARISMA_ADJ)-amount);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(tickID==Tickable.TICKID_MOB)
		{
			if(CMLib.dice().rollPercentage()>20) return true;
			if(!(affected instanceof MOB)) return false;
			final MOB mob=(MOB)affected;
			final Room room=mob.location();
			if(room==null) return false;

			String str=null;
			switch(cycle++)
			{
			case 1: str=_("<S-NAME> emanates an unpleasant odor"); break;
			case 2: str=_("<S-NAME> smells like <S-HE-SHE> hasn't bathed in a month!"); break;
			case 3: str=_("<S-NAME> smells bad!"); break;
			case 4: str=_("<S-NAME> <S-IS-ARE> giving off a horrid odor!"); break;
			case 5: str=_("<S-NAME> really stinks!"); break;
			case 6: str=_("Whew! <S-NAME> REALLY stinks!"); break;
			case 7: str=_("<S-NAME> has an odor resembling that of a skunk!"); break;
			case 8: str=_("<S-NAME> smells like a dead skunk!"); break;
			case 9: str=_("<S-NAME> stinks horribly!"); break;
			case 10: str=_("<S-NAME> seems very very stinky!"); cycle=1; break;
			default: cycle=0; break;
			}
			if(str!=null)
			{
				final CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MASK_ALWAYS|CMMsg.MASK_SOUND|CMMsg.MASK_EYES|CMMsg.TYP_GENERAL,str);
				if(room.okMessage(mob,msg))
				for(int m=0;m<room.numInhabitants();m++)
				{
					final MOB M2=room.fetchInhabitant(m);
					if((!M2.isMonster())&&(M2!=mob)&&(CMLib.flags().canSmell(M2)))
						M2.executeMsg(M2,msg);
				}
			}
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				final Room R=room.getRoomInDir(d);
				if((R!=null)&&(R.numPCInhabitants()>0))
					for(int i=0;i<R.numInhabitants();i++)
					{
						final MOB M=R.fetchInhabitant(i);
						if((M!=null)&&(!M.isMonster())&&(CMLib.flags().canSmell(M)))
							M.tell(_("There is a very bad smell coming from @x1.",Directions.getFromDirectionName(Directions.getOpDirectionCode(d))));
					}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.amITarget(affected))
		&&(msg.targetMinor()==CMMsg.TYP_SNIFF)
		&&(CMLib.flags().canSmell(msg.source())))
			msg.source().tell(msg.source(),affected,null,_("<T-NAME> smell(s) absolutely HORRIBLE!!!"));
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":_("^S<S-NAME> point(s) and utter(s) a stinky spell at <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
					success=maliciousAffect(mob,target,asLevel,0,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,_("<S-NAME> point(s) and utter(s) a spell at <T-NAMESELF>, but it fizzles."));

		// return whether it worked
		return success;
	}
}
