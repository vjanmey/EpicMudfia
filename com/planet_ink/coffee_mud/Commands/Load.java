package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
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

import java.io.*;
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
public class Load extends StdCommand
{
	public Load(){}

	private final String[] access=_i(new String[]{"LOAD"});
	@Override public String[] getAccessWords(){return access;}

	public final String[] combine(final String[] set1, final CMClass.CMObjectType[] set2)
	{
		final String[] fset=new String[set1.length+set2.length];
		for(int x=0;x<set1.length;x++)
			fset[x]=set1[x];
		for(int x=0;x<set2.length;x++)
			fset[set1.length+x]=set2[x].toString();
		return fset;
	}

	public final String ARCHON_LIST[]=combine(new String[]{"RESOURCE","FACTION"},CMClass.CMObjectType.values());

	public final Ammunition getNextAmmunition(String type, List<Ammunition> ammos)
	{
		for(final Ammunition ammo : ammos)
			if((!ammo.amDestroyed())&&(ammo.usesRemaining() > 0)&&(ammo.ammunitionType().equalsIgnoreCase(type)))
				return ammo;
		return null;
	}

	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(mob==null) return true;
		boolean tryArchon=CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LOADUNLOAD);
		if(commands.size()<3)
		{
			if(tryArchon)
				mob.tell(_("LOAD what? Try @x1 [CLASSNAME]",CMParms.toStringList(ARCHON_LIST)));
			else
				mob.tell(_("Load what where?"));
			return false;
		}
		String what=(String)commands.elementAt(1);
		String name=CMParms.combine(commands,2);
		if(tryArchon)
		{
			final Item I=mob.fetchWieldedItem();
			if((I instanceof AmmunitionWeapon)&&((AmmunitionWeapon)I).requiresAmmunition())
				tryArchon=false;
			for(final String aList : ARCHON_LIST)
				if(what.equalsIgnoreCase(aList))
					tryArchon=true;
		}
		if(!tryArchon)
		{
			commands.removeElementAt(0);
			final XVector ammoV=new XVector(what);
			final List<Item> baseAmmoItems=CMLib.english().fetchItemList(mob,mob,null,ammoV,Wearable.FILTER_UNWORNONLY,false);
			final List<Ammunition> ammos=new XVector<Ammunition>();
			for (Item I : baseAmmoItems)
			{
				if(I instanceof Ammunition)
				{
					ammos.add((Ammunition)I);
				}
			}
			if(baseAmmoItems.size()==0)
				mob.tell(_("You don't seem to have any ammunition like that."));
			else
			if((ammos.size()==0)&&(!what.equalsIgnoreCase("all")))
				mob.tell(_("You can't seem to use that as ammunition."));
			else
			{
				commands.removeElementAt(0);
				final List<Item> baseItems=CMLib.english().fetchItemList(mob,mob,null,commands,Wearable.FILTER_ANY,false);
				final List<AmmunitionWeapon> items=new XVector<AmmunitionWeapon>();
				for (Item I : baseItems)
				{
					if((I instanceof AmmunitionWeapon)&&((AmmunitionWeapon)I).requiresAmmunition())
						items.add((AmmunitionWeapon)I);
				}
				boolean doneOne=false;
				if(baseItems.size()==0)
					mob.tell(_("You don't seem to have that."));
				else
				if(items.size()==0)
					mob.tell(_("You can't seem to load that."));
				else
				for(final AmmunitionWeapon W : items)
				{
					Ammunition ammunition = getNextAmmunition(W.ammunitionType(),ammos);
					if(ammunition==null)
					{
						mob.tell(_("You are all out of @x1.",W.ammunitionType()));
					}
					else
					while((ammunition != null)
					&&((W.ammunitionRemaining() < W.ammunitionCapacity())||(!doneOne)))
					{
						final CMMsg newMsg=CMClass.getMsg(mob,W,ammunition,CMMsg.MSG_RELOAD,_("<S-NAME> reload(s) <T-NAME> with <O-NAME>."));
						if(mob.location().okMessage(mob,newMsg))
						{
							doneOne=true;
							mob.location().send(mob,newMsg);
							ammunition = getNextAmmunition(W.ammunitionType(),ammos);
						}
						else
							break;
					}
				}
			}
		}
		else
		{
			if((what.equalsIgnoreCase("FACTION"))
			&&(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDFACTIONS)))
			{
				final Faction F=CMLib.factions().getFaction(name);
				if(F==null)
					mob.tell(_("Faction file '@x1' was not found.",name));
				else
					mob.tell(_("Faction '@x1' from file '@x2' was loaded.",F.name(),name));
				return false;
			}
			else
			if(what.equalsIgnoreCase("RESOURCE"))
			{
				final CMFile F=new CMFile(name,mob,CMFile.FLAG_LOGERRORS);
				if((!F.exists())||(!F.canRead()))
					mob.tell(_("File '@x1' could not be accessed.",name));
				else
				{
					final StringBuffer buf=Resources.getFileResource(name,true); // enforces its own security
					if((buf==null)||(buf.length()==0))
						mob.tell(_("Resource '@x1' was not found.",name));
					else
						mob.tell(_("Resource '@x1' was loaded.",name));
				}
			}
			else
			if(CMSecurity.isASysOp(mob))
			{
				try
				{
					if(name.toUpperCase().endsWith(".JAVA"))
					{
						while(name.startsWith("/")) name=name.substring(1);
						Class<?> C=null;
						Object CO=null;
						try
						{
							C=Class.forName("com.sun.tools.javac.Main", true, CMClass.instance());
							if(C!=null) CO=C.newInstance();
						}catch(final Exception e)
						{
							Log.errOut("Load",e.getMessage());
						}
						final ByteArrayOutputStream bout=new ByteArrayOutputStream();
						final PrintWriter pout=new PrintWriter(new OutputStreamWriter(bout));
						if(CO==null)
						{
							mob.tell(_("Unable to instantiate compiler.  You might try including your Java JDK's lib/tools.jar in your classpath next time you boot the mud."));
							return false;
						}
						final String[] args=new String[]{name};
						if(C!=null)
						{
							final java.lang.reflect.Method M=C.getMethod("compile",new Class[]{args.getClass(),PrintWriter.class});
							final Object returnVal=M.invoke(CO,new Object[]{args,pout});
							if((returnVal instanceof Integer)&&(((Integer)returnVal).intValue()!=0))
							{
								mob.tell(_("Compile failed:"));
								if(mob.session()!=null)
									mob.session().rawOut(bout.toString());
								return false;
							}
						}
						name=name.substring(0,name.length()-5)+".class";
					}

					String unloadClassName=name;
					if(unloadClassName.toUpperCase().endsWith(".CLASS"))
						unloadClassName=unloadClassName.substring(0,unloadClassName.length()-6);
					unloadClassName=unloadClassName.replace('\\','.');
					unloadClassName=unloadClassName.replace('/','.');

					if(what.equalsIgnoreCase("CLASS"))
					{
						final Object O=CMClass.getObjectOrPrototype(unloadClassName);
						if(O!=null)
						{
							final CMClass.CMObjectType x=CMClass.getObjectType(O);
							if(x!=null) what=x.toString();
						}
					}
					final CMObjectType whatType=CMClass.findObjectType(what);
					if(whatType==null)
						mob.tell(_("Don't know how to load a '@x1'.  Try one of the following: @x2",what,CMParms.toStringList(ARCHON_LIST)));
					else
					{
						final Object O=CMClass.getObjectOrPrototype(unloadClassName);
						if((O instanceof CMObject)
						&&(name.toUpperCase().endsWith(".CLASS"))
						&&(CMClass.delClass(whatType,(CMObject)O)))
							mob.tell(_("@x1 was unloaded.",unloadClassName));
						if(CMClass.loadClass(whatType,name,false))
						{
							mob.tell(_("@x1 @x2 was successfully loaded.",CMStrings.capitalizeAndLower(what),name));
							return true;
						}
					}
				}
				catch(final java.lang.Error err)
				{
					mob.tell(err.getMessage());
				}
				catch(final Exception t)
				{
					Log.errOut("Load",t.getClass().getName()+": "+t.getMessage());
				}
				mob.tell(_("@x1 @x2 was not loaded.",CMStrings.capitalizeAndLower(what),name));
			}
		}
		return false;
	}

	@Override public boolean canBeOrdered(){return true;}
	@Override public boolean securityCheck(MOB mob){return super.securityCheck(mob);}
	@Override public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCombatActionCost(ID());}
	@Override public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getActionCost(ID());}
}
