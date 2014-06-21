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
public class Wake extends StdCommand
{
	public Wake(){}

	private final String[] access=_i(new String[]{"WAKE"});
	@Override public String[] getAccessWords(){return access;}
	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands!=null)
			commands.removeElementAt(0);
		if((commands==null)||(commands.size()==0))
		{
			if(!CMLib.flags().isSleeping(mob))
				mob.tell(_("You aren't sleeping!?"));
			else
			{
				final CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_STAND,_("<S-NAME> awake(s) and stand(s) up."));
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
			}
		}
		else
		{
			final String whom=CMParms.combine(commands,0);
			final MOB M=mob.location().fetchInhabitant(whom);
			if((M==null)||(!CMLib.flags().canBeSeenBy(M,mob)))
			{
				mob.tell(_("You don't see '@x1' here.",whom));
				return false;
			}
			if(!CMLib.flags().isSleeping(M))
			{
				mob.tell(_("@x1 is awake!",M.name(mob)));
				return false;
			}
			final CMMsg msg=CMClass.getMsg(mob,M,null,CMMsg.MSG_NOISYMOVEMENT,_("<S-NAME> attempt(s) to wake <T-NAME> up."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				execute(M,null,metaFlags|Command.METAFLAG_ORDER);
			}
		}
		return false;
	}
	@Override public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCombatActionCost(ID());}
	@Override public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getActionCost(ID());}
	@Override public boolean canBeOrdered(){return true;}


}