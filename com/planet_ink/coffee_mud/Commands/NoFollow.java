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
public class NoFollow extends Follow
{
	public NoFollow(){}

	private final String[] access=_i(new String[]{"NOFOLLOW","NOFOL"});
	@Override public String[] getAccessWords(){return access;}

	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if((commands.size()>1)&&(commands.elementAt(0) instanceof String))
		{
			if(((String)commands.elementAt(0)).equalsIgnoreCase("UNFOLLOW"))
			{
				unfollow(mob,((commands.size()>1)&&(commands.elementAt(1) instanceof String)&&(((String)commands.elementAt(1)).equalsIgnoreCase("QUIETLY"))));
				return false;
			}
			MOB M=mob.fetchFollower(CMParms.combine(commands,1));
			if((M==null)&&(mob.location()!=null))
			{
				M=mob.location().fetchInhabitant(CMParms.combine(commands,1));
				if(M!=null)
					mob.tell(_("@x1 is not following you!",M.name(mob)));
				else
					mob.tell(_("There is noone here called '@x1' following you!",CMParms.combine(commands,1)));
				return false;
			}
			if((mob.location()!=null)&&(M!=null)&&(M.amFollowing()==mob))
			{
				nofollow(M,true,false);
				return true;
			}
			mob.tell(_("There is noone called '@x1' following you!",CMParms.combine(commands,1)));
			return false;
		}
		if(!CMath.bset(mob.getBitmap(),MOB.ATT_NOFOLLOW))
		{
			mob.setBitmap(CMath.setb(mob.getBitmap(),MOB.ATT_NOFOLLOW));
			//unfollow(mob,false);
			mob.tell(_("You are no longer accepting new followers."));
		}
		else
		{
			mob.setBitmap(CMath.unsetb(mob.getBitmap(),MOB.ATT_NOFOLLOW));
			mob.tell(_("You are now accepting new followers."));
		}
		return false;
	}

	@Override public boolean canBeOrdered(){return true;}


}
