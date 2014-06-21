package com.planet_ink.coffee_mud.Abilities.Thief;
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
public class Thief_Palm extends ThiefSkill
{
	@Override public String ID() { return "Thief_Palm"; }
	private final static String localizedName = CMLib.lang()._("Palm");
	@Override public String name() { return localizedName; }
	@Override protected int canAffectCode(){return 0;}
	@Override protected int canTargetCode(){return Ability.CAN_ITEMS;}
	@Override public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings =_i(new String[] {"PALM"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	@Override public double castingTime(final MOB mob, final List<String> cmds){return CMProps.getActionSkillCost(ID(),0.0);}
	@Override public double combatCastingTime(final MOB mob, final List<String> cmds){return CMProps.getCombatActionSkillCost(ID(),0.0);}
	@Override public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALING;}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final boolean success=proficiencyCheck(mob,0,auto);
		if(!success)
			beneficialVisualFizzle(mob,null,_("<S-NAME> attempt(s) to palm something and fail(s)."));
		else
		{
			if((commands.size()>0)&&(!((String)commands.lastElement()).equalsIgnoreCase("UNOBTRUSIVELY")))
			   commands.addElement("UNOBTRUSIVELY");
			try
			{
				final Command C=CMClass.getCommand("Get");
				commands.insertElementAt("GET",0);
				if(C!=null) C.execute(mob,commands,0);
			}
			catch(final Exception e)
			{}
		}
		return success;
	}
}
