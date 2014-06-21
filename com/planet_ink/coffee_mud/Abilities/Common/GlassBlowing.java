package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
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
public class GlassBlowing extends CraftingSkill implements ItemCraftor
{
	@Override public String ID() { return "GlassBlowing"; }
	private final static String localizedName = CMLib.lang()._("Glass Blowing");
	@Override public String name() { return localizedName; }
	private static final String[] triggerStrings =_i(new String[] {"GLASSBLOW","GLASSBLOWING"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public String supportedResourceString(){return "_GLASS|SAND";}
	@Override
	public String parametersFormat(){ return
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t"
	   +"ITEM_CLASS_ID\tLID_LOCK\tCONTAINER_CAPACITY||LIQUID_CAPACITY\tCODED_SPELL_LIST";}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int RCP_WOOD=3;
	protected static final int RCP_VALUE=4;
	protected static final int RCP_CLASSTYPE=5;
	protected static final int RCP_MISCTYPE=6;
	protected static final int RCP_CAPACITY=7;
	protected static final int RCP_SPELL=8;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if((buildingI==null)
			||(getRequiredFire(mob,0)==null))
			{
				messedUp=true;
				unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override public String parametersFile(){ return "glassblowing.txt";}
	@Override protected List<List<String>> loadRecipes(){return super.loadRecipes(parametersFile());}

	@Override
	protected boolean doLearnRecipe(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		fireRequired=false;
		return super.doLearnRecipe( mob, commands, givenTarget, auto, asLevel );
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				final MOB mob=(MOB)affected;
				if((buildingI!=null)&&(!aborted))
				{
					if(messedUp)
					{
						if(activity == CraftingActivity.LEARNING)
							commonEmote(mob,"<S-NAME> fail(s) to learn how to make "+buildingI.name()+".");
						else
							commonTell(mob,_("@x1 explodes!",CMStrings.capitalizeAndLower(buildingI.name(mob))));
						buildingI.destroy();
					}
					else
					if(activity==CraftingActivity.LEARNING)
					{
						deconstructRecipeInto( buildingI, recipeHolder );
						buildingI.destroy();
					}
					else
						dropAWinner(mob,buildingI);
				}
				buildingI=null;
			}
		}
		super.unInvoke();
	}

	@Override public boolean supportsDeconstruction() { return true; }

	@Override
	public boolean mayICraft(final Item I)
	{
		if(I==null) return false;
		if(!super.mayBeCrafted(I))
			return false;
		if(I.material()!=RawMaterial.RESOURCE_GLASS)
			return false;
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I))
			return false;
		if(I instanceof Rideable)
		{
			final Rideable R=(Rideable)I;
			final int rideType=R.rideBasis();
			switch(rideType)
			{
			case Rideable.RIDEABLE_LADDER:
			case Rideable.RIDEABLE_SLEEP:
			case Rideable.RIDEABLE_SIT:
			case Rideable.RIDEABLE_TABLE:
				return true;
			default:
				return false;
			}
		}
		if(I instanceof Shield)
			return true;
		if(I instanceof Weapon)
		{
			final Weapon W=(Weapon)I;
			if((W.weaponClassification()!=Weapon.CLASS_BLUNT)
			||((W instanceof AmmunitionWeapon) && ((AmmunitionWeapon)W).requiresAmmunition()))
				return false;
			return true;
		}
		if(I instanceof Light)
			return true;
		if(I instanceof Armor)
			return false;
		if((I instanceof Drink)&&(!(I instanceof Potion)))
			return true;
		if(I instanceof Potion)
			return false;
		if(I instanceof Container)
			return true;
		if(I instanceof FalseLimb)
			return true;
		if(I instanceof Wand)
			return true;
		if(I.rawProperLocationBitmap()==Wearable.WORN_HELD)
			return true;
		return (isANativeItem(I.Name()));
	}

	public boolean supportsMending(Physical I){ return canMend(null,I,true);}
	@Override
	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		if(!super.canMend(mob,E,quiet)) return false;
		if((!(E instanceof Item))
		||(!mayICraft((Item)E)))
		{
			if(!quiet)
				commonTell(mob,_("That's not a glassblown item."));
			return false;
		}
		return true;
	}

	@Override
	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return super.getComponentDescription( mob, recipe, RCP_WOOD );
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		fireRequired=true;

		final CraftParms parsedVars=super.parseAutoGenerate(auto,givenTarget,commands);
		givenTarget=parsedVars.givenTarget;

		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,parsedVars.autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,_("Make what? Enter \"glassblow list\" for a list, \"glassblow learn <item>\" to gain recipes, or \"glassblow stop\" to cancel."));
			return false;
		}
		if((!auto)
		&&(commands.size()>0)
		&&(((String)commands.firstElement()).equalsIgnoreCase("bundle")))
		{
			bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
				return super.bundle(mob,commands);
			return false;
		}
		final List<List<String>> recipes=addRecipes(mob,loadRecipes());
		final String str=(String)commands.elementAt(0);
		String startStr=null;
		bundling=false;
		int duration=4;
		if(str.equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			final int[] cols={
					ListingLibrary.ColFixer.fixColWidth(29,mob.session()),
					ListingLibrary.ColFixer.fixColWidth(3,mob.session())
				};
			final StringBuffer buf=new StringBuffer(_("@x1 @x2 Sand required\n\r",CMStrings.padRight(_("Item"),cols[0]),CMStrings.padRight(_("Lvl"),cols[1])));
			for(int r=0;r<recipes.size();r++)
			{
				final List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					final String item=replacePercent(V.get(RCP_FINALNAME),"");
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					final String wood=getComponentDescription(mob,V,RCP_WOOD);
					if(((level<=xlevel(mob))||allFlag)
					&&((mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
						buf.append(CMStrings.padRight(item,cols[0])+" "+CMStrings.padRight(""+level,cols[1])+" "+wood+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		else
		if((commands.firstElement() instanceof String)&&(((String)commands.firstElement())).equalsIgnoreCase("learn"))
		{
			return doLearnRecipe(mob, commands, givenTarget, auto, asLevel);
		}
		final Item fire=getRequiredFire(mob,parsedVars.autoGenerate);
		if(fire==null) return false;
		activity = CraftingActivity.CRAFTING;
		buildingI=null;
		messedUp=false;
		int amount=-1;
		if((commands.size()>1)&&(CMath.isNumber((String)commands.lastElement())))
		{
			amount=CMath.s_int((String)commands.lastElement());
			commands.removeElementAt(commands.size()-1);
		}
		final String recipeName=CMParms.combine(commands,0);
		List<String> foundRecipe=null;
		final List<List<String>> matches=matchingRecipeNames(recipes,recipeName,true);
		for(int r=0;r<matches.size();r++)
		{
			final List<String> V=matches.get(r);
			if(V.size()>0)
			{
				final int level=CMath.s_int(V.get(RCP_LEVEL));
				if((parsedVars.autoGenerate>0)||(level<=xlevel(mob)))
				{
					foundRecipe=V;
					break;
				}
			}
		}
		if(foundRecipe==null)
		{
			commonTell(mob,_("You don't know how to make a '@x1'.  Try \"glassblow list\" for a list.",recipeName));
			return false;
		}

		final String woodRequiredStr = foundRecipe.get(RCP_WOOD);
		final List<Object> componentsFoundList=getAbilityComponents(mob, woodRequiredStr, "make "+CMLib.english().startWithAorAn(recipeName),parsedVars.autoGenerate);
		if(componentsFoundList==null) return false;
		int woodRequired=CMath.s_int(woodRequiredStr);
		woodRequired=adjustWoodRequired(woodRequired,mob);

		if(amount>woodRequired) woodRequired=amount;
		final String misctype=foundRecipe.get(RCP_MISCTYPE);
		bundling=misctype.equalsIgnoreCase("BUNDLE");
		final int[] pm={RawMaterial.RESOURCE_SAND,RawMaterial.RESOURCE_CRYSTAL,RawMaterial.RESOURCE_GLASS};
		final int[][] data=fetchFoundResourceData(mob,
											woodRequired,"sand",pm,
											0,null,null,
											bundling,
											parsedVars.autoGenerate,
											null);
		if(data==null) return false;
		woodRequired=data[0][FOUND_AMT];
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final int lostValue=parsedVars.autoGenerate>0?0:
			CMLib.materials().destroyResourcesValue(mob.location(),woodRequired,data[0][FOUND_CODE],0,null)
			+CMLib.ableMapper().destroyAbilityComponents(componentsFoundList);
		buildingI=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
		if(buildingI==null)
		{
			commonTell(mob,_("There's no such thing as a @x1!!!",foundRecipe.get(RCP_CLASSTYPE)));
			return false;
		}
		duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),4);
		String itemName=replacePercent(foundRecipe.get(RCP_FINALNAME),RawMaterial.CODES.NAME(data[0][FOUND_CODE])).toLowerCase();
		if(bundling)
			itemName="a "+woodRequired+"# "+itemName;
		else
			itemName=CMLib.english().startWithAorAn(itemName);
		buildingI.setName(itemName);
		startStr=_("<S-NAME> start(s) blowing @x1.",buildingI.name());
		displayText=_("You are blowing @x1",buildingI.name());
		verb=_("blowing @x1",buildingI.name());
		playSound="fire.wav";
		buildingI.setDisplayText(_("@x1 lies here",itemName));
		buildingI.setDescription(itemName+". ");
		buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired,bundling));
		buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE)));

		if(data[0][FOUND_CODE]==RawMaterial.RESOURCE_SAND)
			buildingI.setMaterial(RawMaterial.RESOURCE_GLASS);
		else
			buildingI.setMaterial(data[0][FOUND_CODE]);

		buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL)));
		buildingI.setSecretIdentity(getBrand(mob));
		final int capacity=CMath.s_int(foundRecipe.get(RCP_CAPACITY));
		final String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
		addSpells(buildingI,spell);
		if(buildingI instanceof Container)
		{
			if(capacity>0)
				((Container)buildingI).setCapacity(capacity+woodRequired);
			if(misctype.equalsIgnoreCase("LID"))
				((Container)buildingI).setLidsNLocks(true,false,false,false);
			else
			if(misctype.equalsIgnoreCase("LOCK"))
			{
				((Container)buildingI).setLidsNLocks(true,false,true,false);
				((Container)buildingI).setKeyName(Double.toString(Math.random()));
			}
			((Container)buildingI).setContainTypes(Container.CONTAIN_ANYTHING);
		}
		if(buildingI instanceof Drink)
		{
			if(CMLib.flags().isGettable(buildingI))
			{
				((Drink)buildingI).setLiquidRemaining(0);
				((Drink)buildingI).setLiquidHeld(capacity*50);
				((Drink)buildingI).setThirstQuenched(250);
				if((capacity*50)<250)
					((Drink)buildingI).setThirstQuenched(capacity*50);
			}
		}
		if(bundling) buildingI.setBaseValue(lostValue);
		buildingI.recoverPhyStats();
		buildingI.text();
		buildingI.recoverPhyStats();


		messedUp=!proficiencyCheck(mob,0,auto);

		if(bundling)
		{
			messedUp=false;
			duration=1;
			verb=_("bundling @x1",RawMaterial.CODES.NAME(buildingI.material()).toLowerCase());
			startStr=_("<S-NAME> start(s) @x1.",verb);
			displayText=_("You are @x1",verb);
		}

		if(parsedVars.autoGenerate>0)
		{
			commands.addElement(buildingI);
			return true;
		}

		final CMMsg msg=CMClass.getMsg(mob,buildingI,this,getActivityMessageType(),startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			buildingI=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		else
		if(bundling)
		{
			messedUp=false;
			aborted=false;
			unInvoke();
		}
		return true;
	}
}
