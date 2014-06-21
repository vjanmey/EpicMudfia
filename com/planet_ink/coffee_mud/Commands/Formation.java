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
public class Formation extends StdCommand
{
	public Formation(){}

	private final String[] access=_i(new String[]{"FORMATION"});
	@Override public String[] getAccessWords(){return access;}

	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		final MOB leader=CMLib.combat().getFollowedLeader(mob);
		final List<MOB>[] done=CMLib.combat().getFormation(mob);
		if(commands.size()==0)
		{
			final StringBuffer str=new StringBuffer("");
			for(int i=0;i<done.length;i++)
				if(done[i]!=null)
				{
					if(i==0)
						str.append(_("^xfront  - ^.^?"));
					else
						str.append(_("^xrow +@x1 - ^.^?",""+i));
					for(int i2=0;i2<done[i].size();i2++)
						str.append(((i2>0)?", ":"")+done[i].get(i2).name());
					str.append("\n\r");
				}
			mob.session().colorOnlyPrintln(str.toString());
		}
		else
		if(commands.size()==1)
			mob.tell(_("Put whom in what row?"));
		else
		if(mob.numFollowers()==0)
			mob.tell(_("Noone is following you!"));
		else
		{
			String row=(String)commands.lastElement();
			if("FRONT".startsWith(row.toUpperCase()))
				row="0";
			commands.removeElementAt(commands.size()-1);
			final String name=CMParms.combine(commands,0);
			MOB who=null;
			if(CMLib.english().containsString(mob.name(),name)
			   ||CMLib.english().containsString(mob.Name(),name))
			{
				mob.tell(_("You can not move your own position.  You are always the leader of your party."));
				return false;
			}
			for(int f=0;f<mob.numFollowers();f++)
			{
				final MOB M=mob.fetchFollower(f);
				if(M==null) continue;
				if(CMLib.english().containsString(M.name(),name)
				   ||CMLib.english().containsString(M.Name(),name))
				{who=M; break;}
			}
			if(who==null)
			{
				mob.tell(_("There is noone following you called @x1.",name));
				return false;
			}
			if((!CMath.isNumber(row))||(CMath.s_int(row)<0))
				mob.tell(_("'@x1' is not a valid row in which to put @x2.  Try number greater than 0.",row,who.name()));
			else
			{
				int leaderRow=-1;
				for(int f=0;f<done.length;f++)
					if((done[f]!=null)&&(done[f].contains(leader)))
					{
						leaderRow=f;
						break;
					}
				if(leaderRow<0)
					mob.tell(_("You do not exist."));
				else
				if(CMath.s_int(row)<leaderRow)
					mob.tell(_("You can not place @x1 behind your own position, which is @x2.",who.name(),""+leaderRow));
				else
				{
					mob.addFollower(who,CMath.s_int(row)-leaderRow);
					mob.tell(_("You have positioned @x1 to row @x2",who.name(),""+CMath.s_int(row)));
				}
			}
		}
		return false;
	}

	@Override public boolean canBeOrdered(){return true;}

}
