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
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
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
public class ScrimShaw extends EnhancedCraftingSkill implements ItemCraftor, MendingSkill
{
	@Override public String ID() { return "ScrimShaw"; }
	private final static String localizedName = CMLib.lang()._("Scrimshawing");
	@Override public String name() { return localizedName; }
	private static final String[] triggerStrings =_i(new String[] {"SCRIM","SCRIMSHAWING"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public String supportedResourceString(){return "BONE";}
	@Override
	public String parametersFormat(){ return
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t"
		+"ITEM_CLASS_ID\tSTATUE||LID_LOCK||WEAPON_CLASS||RIDE_BASIS\t"
		+"CONTAINER_CAPACITY||BASE_DAMAGE||LIGHT_DURATION";}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int RCP_WOOD=3;
	protected static final int RCP_VALUE=4;
	protected static final int RCP_CLASSTYPE=5;
	protected static final int RCP_MISCTYPE=6;
	protected static final int RCP_CAPACITY=7;
	protected static final int RCP_SPELL=8;

	protected Item key=null;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if(buildingI==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	@Override public String parametersFile(){ return "scrimshaw.txt";}
	@Override protected List<List<String>> loadRecipes(){return super.loadRecipes(parametersFile());}

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
						if(activity == CraftingActivity.MENDING)
							messedUpCrafting(mob);
						else
						if(activity == CraftingActivity.LEARNING)
						{
							commonEmote(mob,"<S-NAME> fail(s) to learn how to make "+buildingI.name()+".");
							buildingI.destroy();
						}
						else
							commonTell(mob,_("<S-NAME> mess(es) up scrimshawing @x1.",buildingI.name(mob)));
					}
					else
					{
						if(activity == CraftingActivity.MENDING)
							buildingI.setUsesRemaining(100);
						else
						if(activity==CraftingActivity.LEARNING)
						{
							deconstructRecipeInto( buildingI, recipeHolder );
							buildingI.destroy();
						}
						else
						{
							dropAWinner(mob,buildingI);
							if(key!=null)
							{
								dropAWinner(mob,key);
								if(buildingI instanceof Container)
									key.setContainer((Container)buildingI);
							}
						}
					}
				}
				buildingI=null;
				key=null;
				activity = CraftingActivity.CRAFTING;
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean mayICraft(final Item I)
	{
		if(I==null) return false;
		if(!super.mayBeCrafted(I))
			return false;
		if(I.material()!=RawMaterial.RESOURCE_BONE)
			return false;
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I))
			return false;
		if(I instanceof Rideable)
		{
			final Rideable R=(Rideable)I;
			final int rideType=R.rideBasis();
			switch(rideType)
			{
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
		if(I instanceof Ammunition)
			return true;
		if(I instanceof Armor)
			return (I.rawProperLocationBitmap()&Wearable.WORN_FEET)==0;
		if(I instanceof Weapon)
			return !((I instanceof AmmunitionWeapon) && ((AmmunitionWeapon)I).requiresAmmunition());
		if(I instanceof Container)
			return true;
		if((I instanceof Drink)&&(!(I instanceof Potion)))
			return false;
		if(I instanceof FalseLimb)
			return true;
		if((I instanceof Light)&&((I.rawProperLocationBitmap()&Wearable.WORN_MOUTH)>0))
			return true;
		if(I.rawProperLocationBitmap()==Wearable.WORN_HELD)
			return true;
		return (isANativeItem(I.Name()));
	}

	@Override public boolean supportsMending(Physical item){ return canMend(null,item,true);}
	@Override
	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		if(!super.canMend(mob,E,quiet)) return false;
		if((!(E instanceof Item))||(!mayICraft((Item)E)))
		{
			if(!quiet)
				commonTell(mob,_("That's not a scrimshawable item."));
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
	public boolean invoke(final MOB mob, Vector commands, Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Vector originalCommands=(Vector)commands.clone();
		if(super.checkStop(mob, commands))
			return true;

		final CraftParms parsedVars=super.parseAutoGenerate(auto,givenTarget,commands);
		givenTarget=parsedVars.givenTarget;

		final PairVector<Integer,Integer> enhancedTypes=enhancedTypes(mob,commands);
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,parsedVars.autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,_("Scrim what? Enter \"scrim list\" for a list, \"scrim scan\", \"scrim learn <item>\" to gain recipes, \"scrim mend <item>\", or \"scrim stop\" to cancel."));
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
					ListingLibrary.ColFixer.fixColWidth(23,mob.session()),
					ListingLibrary.ColFixer.fixColWidth(3,mob.session())
				};
			final StringBuffer buf=new StringBuffer(_("@x1 @x2 Bone required\n\r",CMStrings.padRight(_("Item"),cols[0]),CMStrings.padRight(_("Lvl"),cols[1])));
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
			enhanceList(mob);
			return true;
		}
		else
		if((commands.firstElement() instanceof String)&&(((String)commands.firstElement())).equalsIgnoreCase("learn"))
		{
			return doLearnRecipe(mob, commands, givenTarget, auto, asLevel);
		}
		else
		if(str.equalsIgnoreCase("scan"))
			return publicScan(mob,commands);
		else
		if(str.equalsIgnoreCase("mend"))
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			key=null;
			messedUp=false;
			final Vector newCommands=CMParms.parse(CMParms.combine(commands,1));
			buildingI=getTarget(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
			if(!canMend(mob,buildingI,false)) return false;
			activity = CraftingActivity.MENDING;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			startStr=_("<S-NAME> start(s) mending @x1.",buildingI.name());
			displayText=_("You are mending @x1",buildingI.name());
			verb=_("mending @x1",buildingI.name());
		}
		else
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			key=null;
			messedUp=false;
			aborted=false;
			String statue=null;
			if((commands.size()>1)&&((String)commands.lastElement()).startsWith("STATUE="))
			{
				statue=(((String)commands.lastElement()).substring(7)).trim();
				if(statue.length()==0)
					statue=null;
				else
					commands.removeElementAt(commands.size()-1);
			}
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
				commonTell(mob,_("You don't know how to scrim a '@x1'.  Try \"scrim list\" for a list.",recipeName));
				return false;
			}

			final String woodRequiredStr = foundRecipe.get(RCP_WOOD);
			final List<Object> componentsFoundList=getAbilityComponents(mob, woodRequiredStr, "make "+CMLib.english().startWithAorAn(recipeName),parsedVars.autoGenerate);
			if(componentsFoundList==null) return false;
			int woodRequired=CMath.s_int(woodRequiredStr);
			woodRequired=adjustWoodRequired(woodRequired,mob);

			final String misctype=foundRecipe.get(RCP_MISCTYPE);
			final int[] pm={RawMaterial.RESOURCE_BONE};
			bundling=misctype.equalsIgnoreCase("BUNDLE");
			final int[][] data=fetchFoundResourceData(mob,
												woodRequired,"bone",pm,
												0,null,null,
												bundling,
												parsedVars.autoGenerate,
												enhancedTypes);
			if(data==null) return false;
			fixDataForComponents(data,componentsFoundList);
			woodRequired=data[0][FOUND_AMT];
			if(amount>woodRequired) woodRequired=amount;

			final Session session=mob.session();
			if((misctype.equalsIgnoreCase("statue"))
			&&(session!=null)
			&&((statue==null)||(statue.trim().length()==0)))
			{
				final Ability me=this;
				final Physical target=givenTarget;
				session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
				{
					@Override public void showPrompt() {session.promptPrint(_("What is this a statue of?\n\r: "));}
					@Override public void timedOut() {}
					@Override public void callBack()
					{
						final String of=this.input;
						if((of.trim().length()==0)||(of.indexOf('<')>=0))
							return;
						final Vector newCommands=(Vector)originalCommands.clone();
						newCommands.add("STATUE="+of);
						me.invoke(mob, newCommands, target, auto, asLevel);
					}
				});
				return false;
			}

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
			startStr=_("<S-NAME> start(s) scrimshawing @x1.",buildingI.name());
			displayText=_("You are scrimshawing @x1",buildingI.name());
			verb=_("scrimshawing @x1",buildingI.name());
			buildingI.setDisplayText(_("@x1 lies here",itemName));
			buildingI.setDescription(itemName+". ");
			buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired,bundling));
			buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE))+(woodRequired*(RawMaterial.CODES.VALUE(data[0][FOUND_CODE]))));
			buildingI.setMaterial(data[0][FOUND_CODE]);
			buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL)));
			buildingI.setSecretIdentity(getBrand(mob));
			final int capacity=CMath.s_int(foundRecipe.get(RCP_CAPACITY));
			final String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
			if(bundling) buildingI.setBaseValue(lostValue);
			addSpells(buildingI,spell);
			key=null;

			if((misctype.equalsIgnoreCase("statue"))
			&&(statue!=null)
			&&(statue.trim().length()>0))
			{
				buildingI.setName(_("@x1 of @x2",itemName,statue.trim()));
				buildingI.setDisplayText(_("@x1 of @x2 is here",itemName,statue.trim()));
				buildingI.setDescription(_("@x1 of @x2. ",itemName,statue.trim()));
			}
			else
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
					key=CMClass.getItem("GenKey");
					((DoorKey)key).setKey(((Container)buildingI).keyName());
					key.setName(_("a key"));
					key.setDisplayText(_("a small key sits here"));
					key.setDescription(_("looks like a key to @x1",buildingI.name()));
					key.recoverPhyStats();
					key.text();
				}
			}
			if(buildingI instanceof Weapon)
			{
				((Weapon)buildingI).basePhyStats().setAttackAdjustment(abilityCode()-1);
				((Weapon)buildingI).setWeaponClassification(Weapon.CLASS_FLAILED);
				setWeaponTypeClass((Weapon)buildingI,misctype,Weapon.TYPE_BASHING,Weapon.TYPE_PIERCING);
				buildingI.basePhyStats().setDamage(capacity);
				((Weapon)buildingI).setRawProperLocationBitmap(Wearable.WORN_WIELD|Wearable.WORN_HELD);
				((Weapon)buildingI).setRawLogicalAnd(false);
			}
			if(buildingI instanceof Rideable)
			{
				setRideBasis((Rideable)buildingI,misctype);
			}
			if(buildingI instanceof Light)
			{
				((Light)buildingI).setDuration(capacity);
				if((buildingI instanceof Container)&&(((Container)buildingI).containTypes()!=Container.CONTAIN_SMOKEABLES))
					((Container)buildingI).setCapacity(0);
			}
			buildingI.recoverPhyStats();
			buildingI.text();
			buildingI.recoverPhyStats();
		}


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
			enhanceItem(mob,buildingI,enhancedTypes);
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
