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
public class StdCommand implements Command
{
	protected final String ID;
	private final String[] access=null;
	public StdCommand()
	{
		final String id=this.getClass().getName();
		final int x=id.lastIndexOf('.');
		if(x>=0) 
			ID=id.substring(x+1);
		else
			ID=id;
	}
	@Override public String ID() { return ID; }
	@Override public String name() { return ID();}

	@Override public String[] getAccessWords(){return access;}
	@Override public void initializeClass(){}
	
	public String _(final String str, final String ... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}
	
	public static String[] _i(final String[] str)
	{
		for(int i=0;i<str.length;i++)
			str[i]=CMLib.lang().commandWordTranslation(str[i]);
		return str;
	}

	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		// accepts the mob executing, and a Vector of Strings as a parm.
		// the return value is arbitrary, though false is conventional.
		return false;
	}
	@Override
	public boolean preExecute(MOB mob, Vector commands, int metaFlags, int secondsElapsed, double actionsRemaining)
		throws java.io.IOException
	{
		return true;
	}

	@Override
	public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException
	{
		// fake it!
		final Vector commands = new Vector();
		commands.add(getAccessWords()[0]);
		for(final Object o : args)
			commands.add(o.toString());
		return Boolean.valueOf(execute(mob,commands,metaFlags));
	}

	public boolean checkArguments(Class[][] fmt, Object... args)
	{
		for (final Class[] element : fmt)
		{
			final Class[] ff=element;
			if(ff.length==args.length)
			{
				boolean check=true;
				for(int i=0;i<ff.length;i++)
				{
					if((args[i]!=null)
					&&(ff[i]!=null)
					&&(!ff[i].isAssignableFrom(args[i].getClass())))
					{
						check=false;
						break;
					}
				}
				if(check)
					return true;
			}
		}
		final StringBuilder str=new StringBuilder("");
		str.append(_("Illegal arguments. Sent: "));
		for(final Object o : args)
			if(o==null)
				str.append(_("null "));
			else
				str.append(o.getClass().getSimpleName()).append(" ");
		str.append(_(". Correct: "));
		for (final Class[] element : fmt)
			for(final Class c : element)
				str.append(c.getSimpleName()).append(" ");
		Log.errOut(ID(),str.toString());
		return false;
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getActionCost(ID(), 0.0);
	}
	@Override
	public double combatActionsCost(MOB mob, List<String> cmds)
	{
		return CMProps.getCombatActionCost(ID(), 0.0);
	}
	@Override
	public double checkedActionsCost(final MOB mob, final List<String> cmds)
	{
		if(mob!=null)
			return mob.isInCombat() ? combatActionsCost(mob,cmds) : actionsCost(mob,cmds);
		return actionsCost(mob,cmds);
	}
	@Override public boolean canBeOrdered(){return true;}
	@Override public boolean securityCheck(MOB mob){return true;}
	@Override public CMObject newInstance(){return this;}
	@Override
	public CMObject copyOf()
	{
		try
		{
			final Object O=this.clone();
			return (CMObject)O;
		}
		catch(final CloneNotSupportedException e)
		{
			return this;
		}
	}

	protected final static Filterer<Environmental> noCoinFilter=new Filterer<Environmental>()
	{
		@Override
		public boolean passesFilter(Environmental obj)
		{
			return !(obj instanceof Coins);
		}
	};

	@Override public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
