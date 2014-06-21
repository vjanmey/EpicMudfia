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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class Apothecary extends Cooking
{
	@Override public String ID() { return "Apothecary"; }
	private final static String localizedName = CMLib.lang()._("Apothecary");
	@Override public String name() { return localizedName; }
	private static final String[] triggerStrings =_i(new String[] {"APOTHECARY","MIX"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public String supportedResourceString(){return "MISC";}
	@Override public String cookWordShort(){return "mix";}
	@Override public String cookWord(){return "mixing";}
	@Override public boolean honorHerbs(){return false;}
	@Override protected ExpertiseLibrary.SkillCostDefinition getRawTrainingCost() { return CMProps.getSkillTrainCostFormula(ID()); }

	@Override public String parametersFile(){ return "poisons.txt";}
	@Override protected List<List<String>> loadRecipes(){return super.loadRecipes(parametersFile());}

	public Apothecary()
	{
		super();

		defaultFoodSound = "hotspring.wav";
		defaultDrinkSound = "hotspring.wav";
	}

	@Override public boolean supportsDeconstruction() { return false; }

	@Override
	public boolean mayICraft(final Item I)
	{
		if(I==null) return false;
		if(!super.mayBeCrafted(I))
			return false;
		if(I instanceof Perfume)
		{
			return true;
		}
		else
		if(I instanceof Drink)
		{
			final Drink D=(Drink)I;
			if(D.liquidType()!=RawMaterial.RESOURCE_POISON)
				return false;
			if(CMLib.flags().flaggedAffects(D, Ability.FLAG_INTOXICATING).size()>0)
				return false;
			if(CMLib.flags().domainAffects(D, Ability.ACODE_POISON).size()>0)
				return true;
			return true;
		}
		else
		if(I instanceof MagicDust)
		{
			final MagicDust M=(MagicDust)I;
			final List<Ability> spells=M.getSpells();
			if((spells == null)||(spells.size()==0))
				return false;
			return true;
		}
		else
			return false;
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((!super.invoke(mob,commands,givenTarget,auto,asLevel))||(buildingI==null))
			return false;
		final Ability A2=buildingI.fetchEffect(0);
		if((A2!=null)
		&&(buildingI instanceof Drink))
		{
			((Drink)buildingI).setLiquidType(RawMaterial.RESOURCE_POISON);
			buildingI.setMaterial(RawMaterial.RESOURCE_POISON);
		}
		return true;
	}
}
