package com.planet_ink.coffee_mud.Races;
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

import java.util.List;
import java.util.Vector;
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
public class Cobra extends Snake
{
	@Override public String ID(){	return "Cobra"; }
	@Override public String name(){ return "Cobra"; }
	@Override public int shortestMale(){return 6;}
	@Override public int shortestFemale(){return 6;}
	@Override public int heightVariance(){return 3;}
	@Override public int lightestWeight(){return 15;}
	@Override public int weightVariance(){return 20;}
	@Override public String racialCategory(){return "Serpent";}
	private final String[]racialAbilityNames={"Poison_Heartstopper"};
	private final int[]racialAbilityLevels={5};
	private final int[]racialAbilityProficiencies={30};
	private final boolean[]racialAbilityQuals={false};
	@Override public String[] racialAbilityNames(){return racialAbilityNames;}
	@Override public int[] racialAbilityLevels(){return racialAbilityLevels;}
	@Override public int[] racialAbilityProficiencies(){return racialAbilityProficiencies;}
	@Override public boolean[] racialAbilityQuals(){return racialAbilityQuals;}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,0 ,0 ,0 ,1 ,0 ,0 ,0 ,0 ,1 ,0 ,1 ,0 };
	@Override public int[] bodyMask(){return parts;}

	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_DEXTERITY,18);
	}
	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("a pair of "+name().toLowerCase()+" fangs",RawMaterial.RESOURCE_BONE));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" scales",RawMaterial.RESOURCE_SCALES));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" meat",RawMaterial.RESOURCE_MEAT));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",RawMaterial.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
