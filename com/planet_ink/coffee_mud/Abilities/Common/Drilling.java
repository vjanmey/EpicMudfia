package com.planet_ink.coffee_mud.Abilities.Common;
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
public class Drilling extends GatheringSkill
{
	@Override public String ID() { return "Drilling"; }
	private final static String localizedName = CMLib.lang()._("Drilling");
	@Override public String name() { return localizedName; }
	private static final String[] triggerStrings =_i(new String[] {"DRILL","DRILLING"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public int classificationCode(){return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_GATHERINGSKILL;}
	@Override protected boolean allowedWhileMounted(){return false;}
	@Override public String supportedResourceString(){return "LIQUID";}

	protected Item found=null;
	private Drink container=null;
	protected String foundShortName="";
	public Drilling()
	{
		super();
		displayText=_("You are drilling...");
		verb=_("drilling");
	}

	protected int getDuration(MOB mob, int level)
	{
		return getDuration(35,mob,level,10);
	}
	@Override protected int baseYield() { return 1; }

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if(tickUp==6)
			{
				if(found!=null)
				{
					commonTell(mob,_("You have found some @x1!",foundShortName));
					displayText=_("You are drilling out some @x1",foundShortName);
					verb=_("drilling out some @x1",foundShortName);
					playSound="drill.wav";
				}
				else
				{
					final StringBuffer str=new StringBuffer(_("You can't seem to find anything worth drilling around here.\n\r"));
					final int d=lookingFor(RawMaterial.MATERIAL_LIQUID,mob.location());
					if(d<0)
						str.append(_("You might try elsewhere."));
					else
						str.append(_("You might try @x1.",Directions.getInDirectionName(d)));
					commonTell(mob,str.toString());
					unInvoke();
				}

			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				final MOB mob=(MOB)affected;
				if((found!=null)&&(!aborted))
				{
					int amount=((found.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_CLOTH)?
							   (CMLib.dice().roll(1,10,0)*(abilityCode())):
							   (CMLib.dice().roll(1,3,0)*(abilityCode()));
					String s="s";
					if(amount==1) s="";
					if(amount>(container.liquidHeld()-container.liquidRemaining()))
						amount=(container.liquidHeld()-container.liquidRemaining());
					if(amount>((Container)container).capacity())
						amount=((Container)container).capacity();
					mob.location().show(mob,null,getActivityMessageType(),_("<S-NAME> manage(s) to drill out @x1 pound@x2 of @x3.",""+amount,s,foundShortName));
					for(int i=0;i<amount;i++)
					{
						final Item newFound=(Item)found.copyOf();
						final Room R=mob.location();
						if(R==null) break;
						R.addItem(newFound,ItemPossessor.Expire.Player_Drop);
						if((container!=null)
						&&(container instanceof Container))
						{
							if(mob.isMine(container))
							{
								CMLib.commands().postGet(mob,null,newFound,true);
								if(mob.isMine(newFound))
									newFound.setContainer((Container)container);
							}
							else
							if(R.isContent((Item)container))
								newFound.setContainer((Container)container);
						}
					}
				}
			}
		}
		super.unInvoke();
	}


	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		bundling=false;
		if((!auto)
		&&(commands.size()>0)
		&&(((String)commands.firstElement()).equalsIgnoreCase("bundle")))
		{
			bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
				return super.bundle(mob,commands);
			return false;
		}

		final Item I=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(I==null) return false;
		if((!(I instanceof Container))
		||(((Container)I).capacity()<=((Container)I).phyStats().weight()))
		{
			commonTell(mob,_("@x1 doesn't look like it can hold anything.",I.name(mob)));
			return false;
		}
		final int resourceType=mob.location().myResource();
		if((!(I instanceof Drink))||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID))
		{
			commonTell(mob,_("@x1 doesn't look like it can hold a liquid.",I.name(mob)));
			return false;
		}
		final List<Item> V=((Container)I).getContents();
		if(((Drink)I).containsDrink())
		{
			for(int v=0;v<V.size();v++)
			{
				final Item I2=V.get(v);
				if((I2.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
				{
					if(I2.material()!=resourceType)
					{
						commonTell(mob,_("@x1 needs to have the @x2 removed first.",I.name(mob),I2.name(mob)));
						return false;
					}
				}
			}
			if(((Drink)I).liquidRemaining()>0)
			{
				commonTell(mob,_("You need to empty @x1 first.",I.name(mob)));
				return false;
			}
		}

		verb=_("drilling");
		found=null;
		playSound=null;
		if(!confirmPossibleMaterialLocation(RawMaterial.MATERIAL_LIQUID,mob.location()))
		{
			commonTell(mob,_("You don't think this is a good place to drill."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if((proficiencyCheck(mob,0,auto))
		   &&(((resourceType&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)))
		{
			found=(Item)CMLib.materials().makeResource(resourceType,Integer.toString(mob.location().domainType()),false,null);
			foundShortName="nothing";
			if(found!=null)
				foundShortName=RawMaterial.CODES.NAME(found.material()).toLowerCase();
		}
		final int duration=getDuration(mob,1);
		final CMMsg msg=CMClass.getMsg(mob,found,this,getActivityMessageType(),_("<S-NAME> start(s) drilling."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			container=(Drink)I;
			found=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
