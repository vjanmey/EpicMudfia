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
public class JewelMaking extends EnhancedCraftingSkill implements ItemCraftor, MendingSkill
{
	@Override public String ID() { return "JewelMaking"; }
	private final static String localizedName = CMLib.lang()._("Jewel Making");
	@Override public String name() { return localizedName; }
	private static final String[] triggerStrings =_i(new String[] {"JEWEL","JEWELMAKING"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public String supportedResourceString(){return "GLASS|PRECIOUS|SAND";}
	@Override
	public String parametersFormat(){ return
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t"
		+"ITEM_CLASS_ID\tSTATUE||CODED_WEAR_LOCATION\tN_A\tBASE_ARMOR_AMOUNT\tOPTIONAL_RESOURCE_OR_MATERIAL\tCODED_SPELL_LIST";}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int RCP_WOOD=3;
	protected static final int RCP_VALUE=4;
	protected static final int RCP_CLASSTYPE=5;
	protected static final int RCP_MISCTYPE=6;
	//private static final int RCP_CAPACITY=7;
	protected static final int RCP_ARMORDMG=8;
	protected static final int RCP_EXTRAREQ=9;
	protected static final int RCP_SPELL=10;

	protected Vector beingDone=null;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if(fireRequired)
			{
				if((buildingI==null)
				||(getRequiredFire(mob,0)==null))
				{
					messedUp=true;
					unInvoke();
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override public String parametersFile(){ return "jewelmaking.txt";}
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
					if((beingDone!=null)&&(beingDone.size()>=2))
					{
						if(messedUp)
							commonEmote(mob,"<S-NAME> mess(es) up "+verb+".");
						else
						{
							final Item I=(Item)beingDone.elementAt(1);
							buildingI.setBaseValue(buildingI.baseGoldValue()+(I.baseGoldValue()*2));
							buildingI.setDescription(buildingI.description()+" "+(String)beingDone.elementAt(0));
						}
						beingDone=null;
					}
					else
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
						if(activity == CraftingActivity.REFITTING)
							commonEmote(mob,"<S-NAME> mess(es) up refitting "+buildingI.name()+".");
						else
							commonEmote(mob,"<S-NAME> mess(es) up "+verb+".");
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
						if(activity == CraftingActivity.REFITTING)
						{
							buildingI.basePhyStats().setHeight(0);
							buildingI.recoverPhyStats();
						}
						else
							dropAWinner(mob,buildingI);
					}
				}
				buildingI=null;
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
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I))
			return false;
		if((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PRECIOUS)
		{
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
			else
			if(I instanceof Armor)
			{
				if(I.fitsOn(Wearable.WORN_EARS)
				||I.fitsOn(Wearable.WORN_EYES)
				||I.fitsOn(Wearable.WORN_HEAD)
				||I.fitsOn(Wearable.WORN_NECK)
				||I.fitsOn(Wearable.WORN_FEET)
				||I.fitsOn(Wearable.WORN_LEFT_FINGER)
				||I.fitsOn(Wearable.WORN_RIGHT_FINGER)
				||I.fitsOn(Wearable.WORN_LEFT_WRIST)
				||I.fitsOn(Wearable.WORN_RIGHT_WRIST))
					return true;
				return (isANativeItem(I.Name()));
			}
			if(I.rawProperLocationBitmap()==Wearable.WORN_HELD)
				return true;
			return true;
		}
		else
		if((I instanceof Armor)
		&&(((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
			||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL)))
		{
			final Armor A=(Armor)I;
			if((CMath.bset(A.getLayerAttributes(), Armor.LAYERMASK_SEETHROUGH))
			&&(A.basePhyStats().armor()<3))
				return true;
			if((A.basePhyStats().armor()<2)
			&&(I.fitsOn(Wearable.WORN_EARS)
				||I.fitsOn(Wearable.WORN_EYES)
				||I.fitsOn(Wearable.WORN_HEAD)
				||I.fitsOn(Wearable.WORN_NECK)
				||I.fitsOn(Wearable.WORN_FEET)
				||I.fitsOn(Wearable.WORN_LEFT_FINGER)
				||I.fitsOn(Wearable.WORN_RIGHT_FINGER)
				||I.fitsOn(Wearable.WORN_LEFT_WRIST)
				||I.fitsOn(Wearable.WORN_RIGHT_WRIST)))
					return true;
			return (isANativeItem(I.Name()));
		}
		return (isANativeItem(I.Name()));
	}

	@Override public boolean supportsMending(Physical I){ return canMend(null,I,true);}
	@Override
	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		if(!super.canMend(mob,E,quiet)) return false;
		if((!(E instanceof Item))
		||(!mayICraft((Item)E)))
		{
			if(!quiet)
				commonTell(mob,_("That's not an jewelworked item."));
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

		fireRequired=true;

		final CraftParms parsedVars=super.parseAutoGenerate(auto,givenTarget,commands);
		givenTarget=parsedVars.givenTarget;

		final PairVector<Integer,Integer> enhancedTypes=enhancedTypes(mob,commands);
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,parsedVars.autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,_("Make what? Enter \"jewel list\" for a list.  You may also enter jewel encrust <gem name> <item name>, jewel mount <gem name> <item name>, jewel refit <item name>, jewel learn <item>, jewel scan, jewel mend <item name>, or jewel stop to cancel."));
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
		fireRequired=true;
		bundling=false;
		int duration=4;
		String misctype="";
		if(str.equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			int toggler=1;
			final int toggleTop=2;
			final StringBuffer buf=new StringBuffer("");
			final int[] cols={
					ListingLibrary.ColFixer.fixColWidth(27,mob.session()),
					ListingLibrary.ColFixer.fixColWidth(3,mob.session()),
					ListingLibrary.ColFixer.fixColWidth(5,mob.session())
				};
			for(int r=0;r<toggleTop;r++)
				buf.append((r>0?" ":"")+CMStrings.padRight(_("Item"),cols[0])+" "+CMStrings.padRight(_("Lvl"),cols[1])+" "+CMStrings.padRight(_("Metal"),cols[2]));
			buf.append("\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				final List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					final String item=replacePercent(V.get(RCP_FINALNAME),"");
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					final String wood=getComponentDescription(mob,V,RCP_WOOD);
					if(wood.length()>5)
					{
						if(toggler>1) buf.append("\n\r");
						toggler=toggleTop;
					}
					if(((level<=xlevel(mob))||allFlag)
					&&((mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
					{
						buf.append(CMStrings.padRight(item,cols[0])+" "+CMStrings.padRight(""+level,cols[1])+" "+CMStrings.padRightPreserve(""+wood,cols[2])+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop) toggler=1;
					}
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
		if((str.equalsIgnoreCase("encrust"))||(str.equalsIgnoreCase("mount")))
		{
			final String word=str.toLowerCase();
			if(commands.size()<3)
			{
				commonTell(mob,_("@x1 what jewel onto what item?",CMStrings.capitalizeAndLower(word)));
				return false;
			}
			final Item fire=getRequiredFire(mob,parsedVars.autoGenerate);
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			aborted=false;
			messedUp=false;
			if(fire==null) return false;
			final String jewel=(String)commands.elementAt(1);
			final String rest=CMParms.combine(commands,2);
			final Environmental jewelE=mob.location().fetchFromMOBRoomFavorsItems(mob,null,jewel,Wearable.FILTER_UNWORNONLY);
			final Environmental thangE=mob.location().fetchFromMOBRoomFavorsItems(mob,null,rest,Wearable.FILTER_UNWORNONLY);
			if((jewelE==null)||(!CMLib.flags().canBeSeenBy(jewelE,mob)))
			{
				commonTell(mob,_("You don't see any '@x1' here.",jewel));
				return false;
			}
			if((thangE==null)||(!CMLib.flags().canBeSeenBy(thangE,mob)))
			{
				commonTell(mob,_("You don't see any '@x1' here.",rest));
				return false;
			}
			if((!(jewelE instanceof RawMaterial))||(!(jewelE instanceof Item))
			   ||(((((Item)jewelE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PRECIOUS)
				  &&((((Item)jewelE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_GLASS)))
			{
				commonTell(mob,_("A @x1 is not suitable to @x2 on anything.",jewelE.name(),word));
				return false;
			}
			final Item jewelI=(Item)CMLib.materials().unbundle((Item)jewelE,1,null);
			if(jewelI==null)
			{
				commonTell(mob,_("@x1 is not pure enough to be @x2ed with.  You will need to use a gathered one.",jewelE.name(),word));
				return false;
			}
			if((!(thangE instanceof Item))
			   ||(!thangE.isGeneric())
			   ||(((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_CLOTH)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_METAL)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_MITHRIL)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_SYNTHETIC)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_ROCK)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LEATHER)))
			{
				commonTell(mob,_("A @x1 is not suitable to be @x2ed on.",thangE.name(),word));
				return false;
			}
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			buildingI=(Item)thangE;
			beingDone=new Vector();
			String materialName=RawMaterial.CODES.NAME(jewelI.material()).toLowerCase();
			if(word.equals("encrust"))
			{
				beingDone.addElement(CMStrings.capitalizeAndLower(buildingI.name())+" is encrusted with bits of "+materialName+".");
				startStr=_("<S-NAME> start(s) encrusting @x1 with @x2.",buildingI.name(),materialName);
				displayText=_("You are encrusting @x1 with @x2",buildingI.name(),materialName);
				verb=_("encrusting @x1 with bits of @x2",buildingI.name(),materialName);
			}
			else
			{
				materialName=CMLib.english().startWithAorAn(materialName).toLowerCase();
				beingDone.addElement(CMStrings.capitalizeAndLower(buildingI.name())+" has "+materialName+" mounted on it.");
				startStr=_("<S-NAME> start(s) mounting @x1 onto @x2.",materialName,buildingI.name());
				displayText=_("You are mounting @x1 onto @x2",materialName,buildingI.name());
				verb=_("mounting @x1 onto @x2",materialName,buildingI.name());
			}
			beingDone.addElement(jewelI);
			messedUp=!proficiencyCheck(mob,0,auto);
			duration=10;
			final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),startStr);
			if(mob.location().okMessage(mob,msg))
			{
				jewelI.destroy();
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,duration);
				return true;
			}
			return false;
		}
		if(str.equalsIgnoreCase("scan"))
			return publicScan(mob,commands);
		else
		if(str.equalsIgnoreCase("mend"))
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			messedUp=false;
			final Item fire=getRequiredFire(mob,parsedVars.autoGenerate);
			if(fire==null) return false;
			final Vector newCommands=CMParms.parse(CMParms.combine(commands,1));
			buildingI=getTarget(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
			if(!canMend(mob, buildingI,false)) return false;
			activity = CraftingActivity.MENDING;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			startStr=_("<S-NAME> start(s) mending @x1.",buildingI.name());
			displayText=_("You are mending @x1",buildingI.name());
			verb=_("mending @x1",buildingI.name());
		}
		else
		if(str.equalsIgnoreCase("refit"))
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			messedUp=false;
			final Item fire=getRequiredFire(mob,parsedVars.autoGenerate);
			if(fire==null) return false;
			final Vector newCommands=CMParms.parse(CMParms.combine(commands,1));
			buildingI=getTarget(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
			if(buildingI==null) return false;
			if(!mayICraft(mob,buildingI))
				return false;
			if(buildingI.phyStats().height()==0)
			{
				commonTell(mob,_("@x1 is already the right size.",buildingI.name(mob)));
				return false;
			}
			activity = CraftingActivity.REFITTING;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			startStr=_("<S-NAME> start(s) refitting @x1.",buildingI.name());
			displayText=_("You are refitting @x1",buildingI.name());
			verb=_("refitting @x1",buildingI.name());
		}
		else
		{
			beingDone=null;
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			aborted=false;
			messedUp=false;
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
				commonTell(mob,_("You don't know how to make a '@x1'.  Try \"jewel list\" for a list.",recipeName));
				return false;
			}
			misctype=foundRecipe.get(RCP_MISCTYPE);
			bundling=misctype.equalsIgnoreCase("BUNDLE");
			if(!bundling)
			{
				final Item fire=getRequiredFire(mob,parsedVars.autoGenerate);
				if(fire==null) return false;
			}
			else
				fireRequired=false;

			final String woodRequiredStr = foundRecipe.get(RCP_WOOD);
			final List<Object> componentsFoundList=getAbilityComponents(mob, woodRequiredStr, "make "+CMLib.english().startWithAorAn(recipeName),parsedVars.autoGenerate);
			if(componentsFoundList==null) return false;
			int woodRequired=CMath.s_int(woodRequiredStr);
			woodRequired=adjustWoodRequired(woodRequired,mob);

			if(amount>woodRequired) woodRequired=amount;
			final String otherRequired=foundRecipe.get(RCP_EXTRAREQ);
			final int[] pm={RawMaterial.MATERIAL_MITHRIL,RawMaterial.MATERIAL_METAL};
			final int[][] data=fetchFoundResourceData(mob,
												woodRequired,"metal",pm,
												otherRequired.length()>0?1:0,otherRequired,null,
												false,
												parsedVars.autoGenerate,
												enhancedTypes);
			if(data==null) return false;
			fixDataForComponents(data,componentsFoundList);
			woodRequired=data[0][FOUND_AMT];

			final Session session=mob.session();
			if((misctype.equalsIgnoreCase("statue"))
			&&(session!=null)
			&&((statue==null)||(statue.trim().length()==0)))
			{
				final Ability me=this;
				final Physical target=givenTarget;
				session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
				{
					@Override public void showPrompt() {session.promptPrint(_("What is this item a representation of?\n\r: "));}
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
				CMLib.materials().destroyResourcesValue(mob.location(),woodRequired,data[0][FOUND_CODE],data[1][FOUND_CODE],null)
				+CMLib.ableMapper().destroyAbilityComponents(componentsFoundList);
			buildingI=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
			if(buildingI==null)
			{
				commonTell(mob,_("There's no such thing as a @x1!!!",foundRecipe.get(RCP_CLASSTYPE)));
				return false;
			}
			duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),4);
			String itemName=null;
			if((otherRequired.length()>0)&&(otherRequired.equalsIgnoreCase("PRECIOUS")))
				itemName=replacePercent(foundRecipe.get(RCP_FINALNAME),RawMaterial.CODES.NAME((data[1][FOUND_CODE]))).toLowerCase();
			else
				itemName=replacePercent(foundRecipe.get(RCP_FINALNAME),RawMaterial.CODES.NAME(data[0][FOUND_CODE])).toLowerCase();
			if(bundling)
				itemName="a "+woodRequired+"# "+itemName;
			else
				itemName=CMLib.english().startWithAorAn(itemName);
			buildingI.setName(itemName);
			startStr=_("<S-NAME> start(s) making @x1.",buildingI.name());
			displayText=_("You are making @x1",buildingI.name());
			verb=_("making @x1",buildingI.name());
			playSound="tinktinktink.wav";
			buildingI.setDisplayText(_("@x1 lies here",itemName));
			if((data[1][FOUND_CODE]>0)
			&&(((data[0][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
			   ||((data[0][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL))
			&&(((data[1][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PRECIOUS)))
				buildingI.setDescription(_("@x1 made of @x2.",itemName,RawMaterial.CODES.NAME(data[0][FOUND_CODE]).toLowerCase()));
			else
				buildingI.setDescription(itemName+". ");
			buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired,bundling));
			buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE))+(woodRequired*(RawMaterial.CODES.VALUE(data[0][FOUND_CODE]))));
			buildingI.setSecretIdentity(getBrand(mob));
			if(data[1][FOUND_CODE]==0)
				buildingI.setMaterial(data[0][FOUND_CODE]);
			else
			{
				buildingI.setMaterial(data[1][FOUND_CODE]);
				buildingI.setBaseValue(buildingI.baseGoldValue()+RawMaterial.CODES.VALUE(data[1][FOUND_CODE]));
			}
			buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL)));
			//int capacity=CMath.s_int((String)foundRecipe.get(RCP_CAPACITY));
			final int armordmg=CMath.s_int(foundRecipe.get(RCP_ARMORDMG));
			final String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
			addSpells(buildingI,spell);
			if((buildingI instanceof Armor)&&(!(buildingI instanceof FalseLimb)))
			{
				((Armor)buildingI).basePhyStats().setArmor(0);
				if(armordmg!=0)
					((Armor)buildingI).basePhyStats().setArmor(armordmg);
				setWearLocation(buildingI,misctype,0);
			}
			if((misctype.equalsIgnoreCase("statue"))
			&&(statue!=null)
			&&(statue.trim().length()>0))
			{
				buildingI.setName(_("@x1 of @x2",itemName,statue.trim()));
				buildingI.setDisplayText(_("@x1 of @x2 is here",itemName,statue.trim()));
				buildingI.setDescription(_("@x1 of @x2. ",itemName,statue.trim()));
			}
			if(bundling) buildingI.setBaseValue(lostValue);
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

		final CMMsg msg=CMClass.getMsg(mob,buildingI,getActivityMessageType(),startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			buildingI=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
			enhanceItem(mob,buildingI,enhancedTypes);
			return true;
		}
		else
		if(bundling)
		{
			messedUp=false;
			aborted=false;
			unInvoke();
		}
		return false;
	}
}
