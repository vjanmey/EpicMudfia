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
public class Cow extends StdRace
{
	@Override public String ID(){	return "Cow"; }
	@Override public String name(){ return "Cow"; }
	@Override public int shortestMale(){return 48;}
	@Override public int shortestFemale(){return 48;}
	@Override public int heightVariance(){return 6;}
	@Override public int lightestWeight(){return 250;}
	@Override public int weightVariance(){return 100;}
	@Override public long forbiddenWornBits(){return ~(Wearable.WORN_FEET|Wearable.WORN_NECK|Wearable.WORN_EARS|Wearable.WORN_EYES);}
	@Override public String racialCategory(){return "Bovine";}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,0 ,1 ,4 ,4 ,1 ,0 ,1 ,1 ,0 ,0 };
	@Override public int[] bodyMask(){return parts;}

	private final int[] agingChart={0,1,2,4,7,15,20,21,22};
	@Override public int[] getAgingChart(){return agingChart;}

	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	@Override public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	@Override
	public boolean canBreedWith(Race R)
	{
		if((!super.sameAs(R))&&(R!=null))
			return R.ID().equals("Bull")||R.ID().equals("Buffalo");
		return true;
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,15);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,5);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
	}

	@Override
	public Race mixRace(Race race, String newRaceID, String newRaceName)
	{
		if(ID().equals("Cow"))
		{
			if((race!=null)&&(race.ID().equalsIgnoreCase("Bull")))
				return CMClass.getRace("Cow");
			if((race!=null)&&(race.ID().equalsIgnoreCase("Buffalo")))
				return CMClass.getRace("Buffalo");
		}
		return super.mixRace(race, newRaceID, newRaceName);
	}

	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(_("a pair of stubby horns"));
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponType(Weapon.TYPE_PIERCING);
		}
		return naturalWeapon;
	}

	@Override
	public String makeMobName(char gender, int age)
	{
		switch(age)
		{
			case Race.AGE_INFANT:
			case Race.AGE_TODDLER:
			case Race.AGE_CHILD:
				return "calf";
			case Race.AGE_YOUNGADULT:
				switch(gender)
				{
				case 'M': case 'm': return "young bull";
				case 'F': case 'f': return "young cow";
				default: return name().toLowerCase();
				}
			case Race.AGE_MATURE:
			case Race.AGE_MIDDLEAGED:
			default:
				switch(gender)
				{
				case 'M': case 'm': return "bull";
				case 'F': case 'f': return "cow";
				default: return name().toLowerCase();
				}
			case Race.AGE_OLD:
			case Race.AGE_VENERABLE:
			case Race.AGE_ANCIENT:
				switch(gender)
				{
				case 'M': case 'm': return "old bull";
				case 'F': case 'f': return "old cow";
				default: return "old "+name().toLowerCase();
				}
		}
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name(viewer) + "^r is hovering on deaths door!^N";
		else
		if(pct<.20)
			return "^r" + mob.name(viewer) + "^r is covered in blood and matted hair.^N";
		else
		if(pct<.30)
			return "^r" + mob.name(viewer) + "^r is bleeding badly from lots of wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.name(viewer) + "^y has large patches of bloody matted fur.^N";
		else
		if(pct<.50)
			return "^y" + mob.name(viewer) + "^y has some bloody matted fur.^N";
		else
		if(pct<.60)
			return "^p" + mob.name(viewer) + "^p has a lot of cuts and gashes.^N";
		else
		if(pct<.70)
			return "^p" + mob.name(viewer) + "^p has a few cut patches.^N";
		else
		if(pct<.80)
			return "^g" + mob.name(viewer) + "^g has a cut patch of fur.^N";
		else
		if(pct<.90)
			return "^g" + mob.name(viewer) + "^g has some disheveled fur.^N";
		else
		if(pct<.99)
			return "^g" + mob.name(viewer) + "^g has some misplaced hairs.^N";
		else
			return "^c" + mob.name(viewer) + "^c is in perfect health.^N";
	}
	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("a pair of "+name().toLowerCase()+" horns",RawMaterial.RESOURCE_BONE));
				for(int i=0;i<10;i++)
					resources.addElement(makeResource
					("a strip of "+name().toLowerCase()+" leather",RawMaterial.RESOURCE_LEATHER));
				for(int i=0;i<20;i++)
					resources.addElement(makeResource
					("some "+name().toLowerCase()+" milk",RawMaterial.RESOURCE_MILK));
				for(int i=0;i<7;i++)
					resources.addElement(makeResource
					("a pound of "+name().toLowerCase()+" meat",RawMaterial.RESOURCE_BEEF));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
