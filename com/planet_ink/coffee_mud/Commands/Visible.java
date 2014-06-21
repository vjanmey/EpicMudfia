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
@SuppressWarnings({"unchecked","rawtypes"})
public class Visible extends StdCommand
{
	public Visible(){}

	private final String[] access=_i(new String[]{"VISIBLE","VIS"});
	@Override public String[] getAccessWords(){return access;}

	public static java.util.List<Ability> returnOffensiveAffects(Physical fromMe)
	{
		final MOB newMOB=CMClass.getFactoryMOB();
		final Vector offenders=new Vector();
		for(int a=0;a<fromMe.numEffects();a++) // personal
		{
			final Ability A=fromMe.fetchEffect(a);
			if((A!=null)&&(A.canBeUninvoked()))
			{
				try
				{
					newMOB.recoverPhyStats();
					A.affectPhyStats(newMOB,newMOB.phyStats());
					if(CMLib.flags().isInvisible(newMOB)||CMLib.flags().isHidden(newMOB))
					  offenders.addElement(A);
				}
				catch(final Exception e)
				{}
			}
		}
		newMOB.destroy();
		return offenders;
	}

	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		final String str=_("Prop_WizInvis");
		final Ability A=mob.fetchEffect(str);
		boolean didSomething=false;
		if(A!=null)
		{
			final Command C=CMClass.getCommand("WizInv");
			if((C!=null)&&(C.securityCheck(mob)))
			{
				didSomething=true;
				C.execute(mob,new XVector("WIZINV","OFF"),metaFlags);
			}
		}
		final java.util.List V=returnOffensiveAffects(mob);
		if(V.size()==0)
		{
			if(!didSomething)
			mob.tell(_("You are not invisible or hidden!"));
		}
		else
		for(int v=0;v<V.size();v++)
			((Ability)V.get(v)).unInvoke();
		return false;
	}
	@Override public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getActionCost(ID());}
	@Override public double combatActionsCost(MOB mob, List<String> cmds){return 0.25;}
	@Override public boolean canBeOrdered(){return true;}

}
