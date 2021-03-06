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
public class Shambler extends StdRace
{
	@Override public String ID(){	return "Shambler"; }
	@Override public String name(){ return "Shambler"; }
	@Override public int shortestMale(){return 34;}
	@Override public int shortestFemale(){return 30;}
	@Override public int heightVariance(){return 12;}
	@Override public int lightestWeight(){return 140;}
	@Override public int weightVariance(){return 30;}
	@Override public long forbiddenWornBits(){return ~(Wearable.WORN_HELD);}
	@Override public String racialCategory(){return "Vegetation";}
	@Override public boolean uncharmable(){return true;}
	@Override public int[] getBreathables() { return breatheAnythingArray; }

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,1 ,0 ,1 ,0 ,2 ,2 ,1 ,2 ,2 ,0 ,0 ,1 ,0 ,1 ,0 };
	@Override public int[] bodyMask(){return parts;}

	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	@Override public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_GOLEM);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(affected.phyStats().level()));
		affectableStats.setDamage(affectableStats.damage()+(affected.phyStats().level()/4));
	}
	@Override
	public void affectCharState(MOB affectedMOB, CharState affectableState)
	{
		affectableState.setHunger((Integer.MAX_VALUE/2)+10);
		affectedMOB.curState().setHunger(affectableState.getHunger());
	}
	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.STAT_GENDER,'N');
		affectableStats.setStat(CharStats.STAT_SAVE_POISON,affectableStats.getStat(CharStats.STAT_SAVE_POISON)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_MIND,affectableStats.getStat(CharStats.STAT_SAVE_MIND)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_GAS,affectableStats.getStat(CharStats.STAT_SAVE_GAS)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_PARALYSIS,affectableStats.getStat(CharStats.STAT_SAVE_PARALYSIS)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_UNDEAD,affectableStats.getStat(CharStats.STAT_SAVE_UNDEAD)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_DISEASE,affectableStats.getStat(CharStats.STAT_SAVE_DISEASE)+100);
	}
	@Override
	public String arriveStr()
	{
		return "shambles in";
	}
	@Override
	public String leaveStr()
	{
		return "shambles";
	}
	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(_("a horrible limb"));
			naturalWeapon.setRanges(0,1);
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_OAK);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
		}
		return naturalWeapon;
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name(viewer) + "^r is near destruction!^N";
		else
		if(pct<.20)
			return "^r" + mob.name(viewer) + "^r is massively shredded and damaged.^N";
		else
		if(pct<.30)
			return "^r" + mob.name(viewer) + "^r is extremely shredded and damaged.^N";
		else
		if(pct<.40)
			return "^y" + mob.name(viewer) + "^y is very shredded and damaged.^N";
		else
		if(pct<.50)
			return "^y" + mob.name(viewer) + "^y is shredded and damaged.^N";
		else
		if(pct<.60)
			return "^p" + mob.name(viewer) + "^p is shredded and slightly damaged.^N";
		else
		if(pct<.70)
			return "^p" + mob.name(viewer) + "^p has lost numerous strands.^N";
		else
		if(pct<.80)
			return "^g" + mob.name(viewer) + "^g has lost some strands.^N";
		else
		if(pct<.90)
			return "^g" + mob.name(viewer) + "^g has lost a few strands.^N";
		else
		if(pct<.99)
			return "^g" + mob.name(viewer) + "^g is no longer in perfect condition.^N";
		else
			return "^c" + mob.name(viewer) + "^c is in perfect condition.^N";
	}
	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<3;i++)
				resources.addElement(makeResource
					("a pile of vegetation",RawMaterial.RESOURCE_VINE));
			}
		}
		return resources;
	}
}
