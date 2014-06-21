package com.planet_ink.coffee_mud.Abilities.Common;
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
public class Shearing extends CommonSkill
{
	@Override public String ID() { return "Shearing"; }
	private final static String localizedName = CMLib.lang()._("Shearing");
	@Override public String name() { return localizedName; }
	private static final String[] triggerStrings =_i(new String[] {"SHEAR","SHEARING"});
	@Override public int classificationCode() {   return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_ANIMALAFFINITY; }
	@Override public String[] triggerStrings(){return triggerStrings;}

	private MOB sheep=null;
	protected boolean failed=false;
	public Shearing()
	{
		super();
		displayText=_("You are shearing something...");
		verb=_("shearing");
	}

	protected int getDuration(MOB mob, int weight)
	{
		int duration=((weight/(10+getXLEVELLevel(mob))));
		duration = super.getDuration(duration, mob, 1, 10);
		if(duration>40) duration=40;
		return duration;
	}
	@Override protected int baseYield() { return 1; }

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((sheep!=null)
		&&(affected instanceof MOB)
		&&(((MOB)affected).location()!=null)
		&&((!((MOB)affected).location().isInhabitant(sheep))))
			unInvoke();
		return super.tick(ticking,tickID);
	}

	public Vector getMyWool(MOB M)
	{
		final Vector wool=new Vector();
		if((M!=null)
		&&(M.charStats().getMyRace()!=null)
		&&(M.charStats().getMyRace().myResources()!=null)
		&&(M.charStats().getMyRace().myResources().size()>0))
		{
			final List<RawMaterial> V=M.charStats().getMyRace().myResources();
			for(int v=0;v<V.size();v++)
				if((V.get(v) != null)
				&&(V.get(v).material()==RawMaterial.RESOURCE_WOOL))
					wool.addElement(V.get(v));
		}
		return wool;
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				final MOB mob=(MOB)affected;
				if((sheep!=null)&&(!aborted))
				{
					if((failed)||(!mob.location().isInhabitant(sheep)))
						commonTell(mob,_("You messed up your shearing completely."));
					else
					{
						mob.location().show(mob,null,sheep,getActivityMessageType(),_("<S-NAME> manage(s) to shear <O-NAME>."));
						spreadImmunity(sheep);
						final int yield=abilityCode()<=0?1:abilityCode();
						for(int i=0;i<yield;i++)
						{
							final Vector V=getMyWool(sheep);
							for(int v=0;v<V.size();v++)
							{
								RawMaterial I=(RawMaterial)V.elementAt(v);
								I=(RawMaterial)I.copyOf();
								mob.location().addItem(I,ItemPossessor.Expire.Monster_EQ);
							}
						}
					}
				}
			}
		}
		super.unInvoke();
	}


	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		MOB target=null;
		final Room R=mob.location();
		if(R==null) return false;
		sheep=null;
		if((mob.isMonster()
		&&(!CMLib.flags().isAnimalIntelligence(mob)))
		&&(commands.size()==0))
		{
			for(int i=0;i<R.numInhabitants();i++)
			{
				final MOB M=R.fetchInhabitant(i);
				if((M!=mob)&&(CMLib.flags().canBeSeenBy(M,mob))&&(getMyWool(M).size()>0))
				{
					target=M;
					break;
				}
			}
		}
		else
		if(commands.size()==0)
			mob.tell(_("Shear what?"));
		else
			target=super.getTarget(mob,commands,givenTarget);

		if(target==null) return false;
		if((getMyWool(target).size()<=0)
		||(!target.okMessage(target,CMClass.getMsg(target,target,this,CMMsg.MSG_OK_ACTION,null))))
		{
			commonTell(mob,target,null,_("You can't shear <T-NAME>, there's no wool left on <T-HIM-HER>."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		failed=!proficiencyCheck(mob,0,auto);
		final CMMsg msg=CMClass.getMsg(mob,target,this,getActivityMessageType(),getActivityMessageType(),getActivityMessageType(),_("<S-NAME> start(s) shearing <T-NAME>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			sheep=target;
			verb=_("shearing @x1",target.name());
			playSound="scissor.wav";
			final int duration=getDuration(mob,target.phyStats().weight());
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
