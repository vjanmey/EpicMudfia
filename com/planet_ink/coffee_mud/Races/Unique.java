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
public class Unique extends StdRace
{
	@Override public String ID(){	return "Unique"; }
	@Override public String name(){ return "Unique"; }
	@Override public int shortestMale(){return 64;}
	@Override public int shortestFemale(){return 60;}
	@Override public int heightVariance(){return 12;}
	@Override public int lightestWeight(){return 100;}
	@Override public int weightVariance(){return 100;}
	@Override public long forbiddenWornBits(){return 0;}
	@Override public String racialCategory(){return "Unique";}
	@Override public boolean fertile(){return false;}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
	@Override public int[] bodyMask(){return parts;}

	private final int[] agingChart={0,0,0,0,0,YEARS_AGE_LIVES_FOREVER,YEARS_AGE_LIVES_FOREVER,YEARS_AGE_LIVES_FOREVER,YEARS_AGE_LIVES_FOREVER};
	@Override public int[] getAgingChart(){return agingChart;}

	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	@Override public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name(viewer) + "^r is pulsating in an unstable rage!^N";
		else
		if(pct<.20)
			return "^r" + mob.name(viewer) + "^r is massively and amazingly angry.^N";
		else
		if(pct<.30)
			return "^r" + mob.name(viewer) + "^r is very angry.^N";
		else
		if(pct<.40)
			return "^y" + mob.name(viewer) + "^y is somewhat angry.^N";
		else
		if(pct<.50)
			return "^y" + mob.name(viewer) + "^y is very irritated.^N";
		else
		if(pct<.60)
			return "^p" + mob.name(viewer) + "^p is starting to show irritation.^N";
		else
		if(pct<.70)
			return "^p" + mob.name(viewer) + "^p is definitely serious and concerned.^N";
		else
		if(pct<.80)
			return "^g" + mob.name(viewer) + "^g is growing serious and concerned.^N";
		else
		if(pct<.90)
			return "^g" + mob.name(viewer) + "^g is definitely unamused and is starting to notice.^N";
		else
		if(pct<.99)
			return "^g" + mob.name(viewer) + "^g is no longer amused, though still unconcerned.^N";
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
				resources.addElement(makeResource
				("a "+name().toLowerCase()+" internal organ",RawMaterial.RESOURCE_MEAT));
			}
		}
		return resources;
	}
}
