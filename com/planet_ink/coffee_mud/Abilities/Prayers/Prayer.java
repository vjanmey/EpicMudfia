package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

@SuppressWarnings("rawtypes")
public class Prayer extends StdAbility
{
	@Override public String ID() { return "Prayer"; }
	private final static String localizedName = CMLib.lang()._("a Prayer");
	@Override public String name() { return localizedName; }
	@Override public String displayText(){ return "";}
	@Override protected int canAffectCode(){return 0;}
	@Override protected int canTargetCode(){return CAN_MOBS;}
	@Override public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings =_i(new String[] {"PRAY","PR"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public int classificationCode(){return Ability.ACODE_PRAYER;}


	protected String prayWord(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return "pray(s) to "+mob.getMyDeity().name();
		return "pray(s)";
	}

	protected String prayForWord(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return "pray(s) for "+mob.getMyDeity().name();
		return "pray(s)";
	}

	protected String inTheNameOf(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return " in the name of "+mob.getMyDeity().name();
		return "";
	}
	protected String againstTheGods(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return " against "+mob.getMyDeity().name();
		return " against the gods";
	}
	protected String hisHerDiety(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return mob.getMyDeity().name();
		return "<S-HIS-HER> god";
	}
	protected String ofDiety(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return " of "+mob.getMyDeity().name();
		return "";
	}
	protected String prayingWord(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return "praying to "+mob.getMyDeity().name();
		return "praying";
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical target, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,target,auto,asLevel))
			return false;
		if((!auto)
		&&(!mob.isMonster())
		&&(!disregardsArmorCheck(mob))
		&&(mob.isMine(this))
		&&(!appropriateToMyFactions(mob)))
		{
			int hq=500;
			if(CMath.bset(flags(),Ability.FLAG_HOLY))
			{
				if(!CMath.bset(flags(),Ability.FLAG_UNHOLY))
					hq=1000;
			}
			else
			if(CMath.bset(flags(),Ability.FLAG_UNHOLY))
				hq=0;

			int basis=0;
			if(hq==0)
				basis=CMLib.factions().getAlignPurity(mob.fetchFaction(CMLib.factions().AlignID()),Faction.Align.EVIL);
			else
			if(hq==1000)
				basis=CMLib.factions().getAlignPurity(mob.fetchFaction(CMLib.factions().AlignID()),Faction.Align.GOOD);
			else
			{
				basis=CMLib.factions().getAlignPurity(mob.fetchFaction(CMLib.factions().AlignID()),Faction.Align.NEUTRAL);
				basis-=10;
			}

			if(CMLib.dice().rollPercentage()>basis)
				return true;

			if(hq==0)
				mob.tell(_("The evil nature of @x1 disrupts your prayer.",name()));
			else
			if(hq==1000)
				mob.tell(_("The goodness of @x1 disrupts your prayer.",name()));
			else
			if(CMLib.flags().isGood(mob))
				mob.tell(_("The anti-good nature of @x1 disrupts your thought.",name()));
			else
			if(CMLib.flags().isEvil(mob))
				mob.tell(_("The anti-evil nature of @x1 disrupts your thought.",name()));
			return false;
		}
		return true;
	}

}
