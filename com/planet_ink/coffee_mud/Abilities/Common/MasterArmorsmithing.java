package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
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
   Copyright 2004 Tim Kassebaum

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
public class MasterArmorsmithing extends Armorsmithing implements ItemCraftor
{
	@Override public String ID() { return "MasterArmorsmithing"; }
	private final static String localizedName = CMLib.lang()._("Master Armorsmithing");
	@Override public String name() { return localizedName; }
	private static final String[] triggerStrings =_i(new String[] {"MARMORSMITH","MASTERARMORSMITHING"});
	@Override public String[] triggerStrings(){return triggerStrings;}

	@Override public String parametersFile(){ return "masterarmorsmith.txt";}
	@Override protected List<List<String>> loadRecipes(){return super.loadRecipes(parametersFile());}

	@Override
	protected boolean masterCraftCheck(final Item I)
	{
		if(I.basePhyStats().level()<30)
		{
			Ability A;
			for(int i=0;i<I.numEffects();i++)
			{
				A=I.fetchEffect( i );
				if(A instanceof TriggeredAffect)
				{
					final long flags=A.flags();
					final int triggers=((TriggeredAffect) A).triggerMask();
					if( CMath.bset( flags, Ability.FLAG_ADJUSTER )
					&&  CMath.bset(triggers,TriggeredAffect.TRIGGER_WEAR_WIELD))
						return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;

		final CraftParms parsedVars=super.parseAutoGenerate(auto,givenTarget,commands);

		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,parsedVars.autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,_("Make what? Enter \"marmorsmith list\" for a list,\"marmorsmith scan\", \"marmorsmith learn <item>\", \"marmorsmith mend <item>\", or \"marmorsmith stop\" to cancel."));
			return false;
		}
		if(parsedVars.autoGenerate>0)
			commands.insertElementAt(Integer.valueOf(parsedVars.autoGenerate),0);
		return super.invoke(mob,commands,givenTarget,auto,asLevel);
	}
}
