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
public class Fish extends StdRace
{
	@Override public String ID(){	return "Fish"; }
	@Override public String name(){ return "Fish"; }
	@Override public int shortestMale(){return 2;}
	@Override public int shortestFemale(){return 2;}
	@Override public int heightVariance(){return 3;}
	@Override public int lightestWeight(){return 5;}
	@Override public int weightVariance(){return 15;}
	@Override public long forbiddenWornBits(){return ~(Wearable.WORN_EYES);}
	@Override public String racialCategory(){return "Amphibian";}
	private final String[]racialAbilityNames={"Skill_Swim"};
	private final int[]racialAbilityLevels={1};
	private final int[]racialAbilityProficiencies={100};
	private final boolean[]racialAbilityQuals={false};
	@Override protected String[] racialAbilityNames(){return racialAbilityNames;}
	@Override protected int[] racialAbilityLevels(){return racialAbilityLevels;}
	@Override protected int[] racialAbilityProficiencies(){return racialAbilityProficiencies;}
	@Override protected boolean[] racialAbilityQuals(){return racialAbilityQuals;}
	@Override public int[] getBreathables() { return breatheWaterArray; }

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,0 ,1 ,0 ,0 ,0 ,1 ,0 ,0 ,0 ,2 ,1 ,0 ,1 ,0 };
	@Override public int[] bodyMask(){return parts;}

	private final int[] agingChart={0,1,1,2,2,3,3,4,4};
	@Override public int[] getAgingChart(){return agingChart;}

	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	@Override public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,3);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,7);
	}
	@Override
	public String arriveStr()
	{
		return "swims in";
	}
	@Override
	public String leaveStr()
	{
		return "swims";
	}
	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(_("nasty stingers"));
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponType(Weapon.TYPE_PIERCING);
		}
		return naturalWeapon;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		final MOB mob=(MOB)affected;
		final Room R=mob.location();
		if((R!=null)
		&&((R.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
			||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
			||(R.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
			||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
			||((RawMaterial.CODES.GET(R.getAtmosphere())&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)))
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SWIMMING);
	}

	@Override
	public boolean okMessage(Environmental affected, CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.amISource((MOB)affected))
		&&(msg.source().isMonster())
		&&(msg.target() instanceof Room)
		&&(msg.tool() instanceof Exit)
		&&(Arrays.binarySearch(getBreathables(), ((Room)msg.target()).getAtmosphere())<0))
		{
			((MOB)affected).tell(_("That way looks too dry."));
			return false;
		}
		return true;
	}

	@Override
	public String makeMobName(char gender, int age)
	{
		switch(age)
		{
			case Race.AGE_INFANT:
			case Race.AGE_TODDLER:
			case Race.AGE_CHILD:
				return name().toLowerCase()+" fry";
			default:
				return super.makeMobName('N', age);
		}
	}


	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name(viewer) + "^r is facing a cold death!^N";
		else
		if(pct<.20)
			return "^r" + mob.name(viewer) + "^r is covered in blood.^N";
		else
		if(pct<.30)
			return "^r" + mob.name(viewer) + "^r is bleeding badly from lots of wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.name(viewer) + "^y has numerous bloody wounds and gashes.^N";
		else
		if(pct<.50)
			return "^y" + mob.name(viewer) + "^y has some bloody wounds and gashes.^N";
		else
		if(pct<.60)
			return "^p" + mob.name(viewer) + "^p has a few bloody wounds.^N";
		else
		if(pct<.70)
			return "^p" + mob.name(viewer) + "^p is cut and bruised heavily.^N";
		else
		if(pct<.80)
			return "^g" + mob.name(viewer) + "^g has some minor cuts and bruises.^N";
		else
		if(pct<.90)
			return "^g" + mob.name(viewer) + "^g has a few bruises and scratched scales.^N";
		else
		if(pct<.99)
			return "^g" + mob.name(viewer) + "^g has a few small bruises.^N";
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
				("some "+name().toLowerCase(),RawMaterial.RESOURCE_FISH));
				resources.addElement(makeResource
				("a "+name().toLowerCase()+" scales",RawMaterial.RESOURCE_SCALES));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",RawMaterial.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
