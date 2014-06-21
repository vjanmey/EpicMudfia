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

@SuppressWarnings({"unchecked","rawtypes"})
public class Scrapping extends CommonSkill
{
	@Override public String ID() { return "Scrapping"; }
	private final static String localizedName = CMLib.lang()._("Scrapping");
	@Override public String name() { return localizedName; }
	private static final String[] triggerStrings =_i(new String[] {"SCRAP","SCRAPPING"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override protected ExpertiseLibrary.SkillCostDefinition getRawTrainingCost() { return CMProps.getSkillTrainCostFormula(ID()); }
	@Override public int classificationCode() {   return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_NATURELORE; }

	protected Item found=null;
	boolean fireRequired=false;
	protected int amount=0;
	protected String oldItemName="";
	protected String foundShortName="";
	protected boolean messedUp=false;
	public Scrapping()
	{
		super();
		displayText=_("You are scrapping...");
		verb=_("scrapping");
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if((found==null)||(fireRequired&&(getRequiredFire(mob,0)==null)))
			{
				messedUp=true;
				unInvoke();
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
					if(messedUp)
						commonTell(mob,_("You've messed up scrapping @x1!",oldItemName));
					else
					{
						amount=amount*abilityCode();
						String s="s";
						if(amount==1) s="";
						mob.location().show(mob,null,getActivityMessageType(),_("<S-NAME> manage(s) to scrap @x1 pound@x2 of @x3.",""+amount,s,foundShortName));
						for(int i=0;i<amount;i++)
						{
							final Item newFound=(Item)found.copyOf();
							mob.location().addItem(newFound,ItemPossessor.Expire.Player_Drop);
							CMLib.commands().postGet(mob,null,newFound,true);
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
		verb=_("scrapping");
		final String str=CMParms.combine(commands,0);
		final Item I=mob.location().findItem(null,str);
		if((I==null)||(!CMLib.flags().canBeSeenBy(I,mob)))
		{
			commonTell(mob,_("You don't see anything called '@x1' here.",str));
			return false;
		}
		boolean okMaterial=true;
		oldItemName=I.Name();
		switch(I.material()&RawMaterial.MATERIAL_MASK)
		{
		case RawMaterial.MATERIAL_FLESH:
		case RawMaterial.MATERIAL_LIQUID:
		case RawMaterial.MATERIAL_PAPER:
		case RawMaterial.MATERIAL_ENERGY:
		case RawMaterial.MATERIAL_GAS:
		case RawMaterial.MATERIAL_VEGETATION:
			{ okMaterial=false; break;}
		}
		if(!okMaterial)
		{
			commonTell(mob,_("You don't know how to scrap @x1.",I.name(mob)));
			return false;
		}

		if(I instanceof RawMaterial)
		{
			commonTell(mob,_("@x1 already looks like scrap.",I.name(mob)));
			return false;
		}

		if(CMLib.flags().enchanted(I))
		{
			commonTell(mob,_("@x1 is enchanted, and can't be scrapped.",I.name(mob)));
			return false;
		}

		final Vector V=new Vector();
		int totalWeight=0;
		for(int i=0;i<mob.location().numItems();i++)
		{
			final Item I2=mob.location().getItem(i);
			if((I2!=null)&&(I2.sameAs(I)))
			{
				totalWeight+=I2.phyStats().weight();
				V.addElement(I2);
			}
		}

		final LandTitle t=CMLib.law().getLandTitle(mob.location());
		if((t!=null)&&(!CMLib.law().doesHavePriviledgesHere(mob,mob.location())))
		{
			mob.tell(_("You are not allowed to scrap anything here."));
			return false;
		}

		for(int i=0;i<mob.location().numItems();i++)
		{
			final Item I2=mob.location().getItem(i);
			if((I2.container()!=null)&&(V.contains(I2.container())))
			{
				commonTell(mob,_("You need to remove the contents of @x1 first.",I2.name(mob)));
				return false;
			}
		}
		amount=totalWeight/5;
		if(amount<1)
		{
			commonTell(mob,_("You don't have enough here to get anything from."));
			return false;
		}
		fireRequired=false;
		if(((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_GLASS)
		||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
		||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_SYNTHETIC)
		||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL))
		{
			final Item fire=getRequiredFire(mob,0);
			fireRequired=true;
			if(fire==null) return false;
		}

		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		int duration=getDuration(45,mob,1,10);
		messedUp=!proficiencyCheck(mob,0,auto);
		found=CMLib.materials().makeItemResource(I.material());
		foundShortName="nothing";
		playSound="ripping.wav";
		if(found!=null)
			foundShortName=RawMaterial.CODES.NAME(found.material()).toLowerCase();
		final CMMsg msg=CMClass.getMsg(mob,I,this,getActivityMessageType(),_("<S-NAME> start(s) scrapping @x1.",I.name()));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			for(int v=0;v<V.size();v++)
			{
				if(((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PRECIOUS)
				||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
				||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL))
					duration+=((Item)V.elementAt(v)).phyStats().weight();
				else
					duration+=((Item)V.elementAt(v)).phyStats().weight()/2;
				((Item)V.elementAt(v)).destroy();
			}
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
