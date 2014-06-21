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
public class Toadstool extends StdRace
{
	@Override public String ID(){	return "Toadstool"; }
	@Override public String name(){ return "Toadstool"; }
	@Override public int shortestMale(){return 1;}
	@Override public int shortestFemale(){return 1;}
	@Override public int heightVariance(){return 1;}
	@Override public int lightestWeight(){return 1;}
	@Override public int weightVariance(){return 1;}
	@Override public long forbiddenWornBits(){return Integer.MAX_VALUE;}
	@Override public String racialCategory(){return "Vegetation";}
	@Override public int availabilityCode(){return 0;}
	@Override public int[] getBreathables() { return breatheAnythingArray; }

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,0 ,0 ,0 ,0 ,0 ,0 ,1 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 };
	@Override public int[] bodyMask(){return parts;}

	private final int[] agingChart={0,0,0,0,0,YEARS_AGE_LIVES_FOREVER,YEARS_AGE_LIVES_FOREVER,YEARS_AGE_LIVES_FOREVER,YEARS_AGE_LIVES_FOREVER};
	@Override public int[] getAgingChart(){return agingChart;}

	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setWeight(1);
		affectableStats.setHeight(1);
		affectableStats.setAttackAdjustment(0);
		affectableStats.setArmor(0);
		affectableStats.setSensesMask(affectableStats.sensesMask()
			|PhyStats.CAN_NOT_MOVE|PhyStats.CAN_NOT_SPEAK|PhyStats.CAN_NOT_TASTE);
		affectableStats.setDamage(0);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_GOLEM);
	}
	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,1);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,1);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
	}
	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(_("the toadstool shuffle"));
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_MUSHROOMS);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponType(Weapon.TYPE_NATURAL);
		}
		return naturalWeapon;
	}

	@Override
	public String makeMobName(char gender, int age)
	{
		return super.makeMobName('N', Race.AGE_MATURE);
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name(viewer) + "^r is almost squashed!^N";
		else
		if(pct<.25)
			return "^y" + mob.name(viewer) + "^y is severely gashed and bruised.^N";
		else
		if(pct<.40)
			return "^p" + mob.name(viewer) + "^p has lots of gashes and bruises.^N";
		else
		if(pct<.55)
			return "^p" + mob.name(viewer) + "^p has some serious bruises.^N";
		else
		if(pct<.70)
			return "^g" + mob.name(viewer) + "^g has some bruises.^N";
		else
		if(pct<.85)
			return "^g" + mob.name(viewer) + "^g has a few small bruises.^N";
		else
		if(pct<.95)
			return "^g" + mob.name(viewer) + "^g is barely bruised.^N";
		else
			return "^c" + mob.name(viewer) + "^c is in perfect condition^N";
	}
	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<5;i++)
					resources.addElement(makeResource
					("some "+name().toLowerCase()+" flesh",RawMaterial.RESOURCE_MUSHROOMS));
			}
		}
		return resources;
	}
}
