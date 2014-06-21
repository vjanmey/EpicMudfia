package com.planet_ink.coffee_mud.Commands;
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
public class Ignore extends StdCommand
{
	public Ignore(){}

	private final String[] access=_i(new String[]{"IGNORE"});
	@Override public String[] getAccessWords(){return access;}
	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		final PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;
		final Set<String> h=pstats.getIgnored();
		if((commands.size()<2)||(((String)commands.elementAt(1)).equalsIgnoreCase("list")))
		{
			if(h.size()==0)
				mob.tell(_("You have no names on your ignore list.  Use IGNORE ADD to add more."));
			else
			{
				final StringBuffer str=new StringBuffer(_("You are ignoring: "));
				for (final Object element : h)
					str.append(((String)element)+" ");
				mob.tell(str.toString());
			}
		}
		else
		if(((String)commands.elementAt(1)).equalsIgnoreCase("ADD"))
		{
			String name=CMParms.combine(commands,2);
			if(name.length()==0)
			{
				mob.tell(_("Add whom?"));
				return false;
			}
			name=CMStrings.capitalizeAndLower(name);
			if((!CMLib.players().playerExists(name))&&(name.indexOf('@')<0))
			{
				mob.tell(_("No player by that name was found."));
				return false;
			}
			if(h.contains(name))
			{
				mob.tell(_("That name is already on your list."));
				return false;
			}
			h.add(name);
			mob.tell(_("The Player '@x1' has been added to your ignore list.",name));
		}
		else
		if(((String)commands.elementAt(1)).equalsIgnoreCase("REMOVE"))
		{
			final String name=CMParms.combine(commands,2);
			if(name.length()==0)
			{
				mob.tell(_("Remove whom?"));
				return false;
			}
			if(!h.contains(name))
			{
				mob.tell(_("That name '@x1' does not appear on your list.  Watch your casing!",name));
				return false;
			}
			h.remove(name);
			mob.tell(_("The Player '@x1' has been removed from your ignore list.",name));
		}
		else
		{
			mob.tell(_("Parameter '@x1' is not recognized.  Try LIST, ADD, or REMOVE.",((String)commands.elementAt(1))));
			return false;
		}
		return false;
	}

	@Override public boolean canBeOrdered(){return true;}


}
