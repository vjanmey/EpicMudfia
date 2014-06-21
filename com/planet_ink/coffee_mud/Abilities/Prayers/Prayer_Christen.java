package com.planet_ink.coffee_mud.Abilities.Prayers;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class Prayer_Christen extends Prayer
{
	@Override public String ID() { return "Prayer_Christen"; }
	private final static String localizedName = CMLib.lang()._("Christen");
	@Override public String name() { return localizedName; }
	@Override protected int canTargetCode(){return 0;}
	@Override public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;}
	@Override public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	@Override public long flags(){return Ability.FLAG_HOLY;}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isMonster())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat())
		{
			mob.tell(_("Not while you're fighting!"));
			return false;
		}
		if(commands.size()<2)
		{
			mob.tell(_("Christen whom what?"));
			return false;
		}
		String name=((String)commands.lastElement()).trim();
		commands.removeElementAt(commands.size()-1);
		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null) return false;

		if((!(target instanceof CagedAnimal))||(target.phyStats().ability()<=0)||(!target.isGeneric()))
		{
			mob.tell(_("You may only christen a child."));
			return false;
		}
		if(name.length()==0)
		{
			mob.tell(_("Christen @x1 what?",target.name(mob)));
			return false;
		}
		if(name.indexOf(' ')>=0)
		{
			mob.tell(_("The name may not have a space in it."));
			return false;
		}

		name=CMStrings.capitalizeAndLower(name);

		if(CMLib.players().playerExists(name))
		{
			mob.tell(_("That name is already taken.  Please choose another."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?_("<T-NAME> becomes @x1.",name):_("^S<S-NAME> christen(s) <T-NAMESELF> '@x1'.^?",name));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				String oldName=target.Name();
				target.setName(name);
				target.setDisplayText(_("@x1 is here.",name));
				String txt=((CagedAnimal)target).cageText();
				int startNameX=txt.indexOf("<NAME>");
				int endNameX=(startNameX>=0)?txt.indexOf("</NAME>",startNameX):-1;
				if(endNameX>startNameX)
					oldName=txt.substring(startNameX+6,endNameX);
				txt=CMStrings.replaceFirst(txt,"<NAME>"+oldName+"</NAME>","<NAME>"+name+"</NAME>");
				txt=CMStrings.replaceFirst(txt,"<DISP>"+oldName,"<DISP>"+name);
				((CagedAnimal)target).setCageText(txt);
				final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CHRISTENINGS);
				for(int i=0;i<channels.size();i++)
					CMLib.commands().postChannel(channels.get(i),mob.clans(),target.name()+" was just christened.",true);
				CMLib.leveler().postExperience(mob,null,null,5,false);
			}
		}
		else
			beneficialWordsFizzle(mob,target,_("<S-NAME> @x1 over <T-NAME>, but lose(s) <S-HIS-HER> concentration.",prayWord(mob)));


		// return whether it worked
		return success;
	}
}
