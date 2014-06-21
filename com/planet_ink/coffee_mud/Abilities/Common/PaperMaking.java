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
public class PaperMaking extends CraftingSkill implements ItemCraftor
{
	@Override public String ID() { return "PaperMaking"; }
	private final static String localizedName = CMLib.lang()._("Paper Making");
	@Override public String name() { return localizedName; }
	private static final String[] triggerStrings =_i(new String[] {"PAPERMAKE","PAPERMAKING"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public String supportedResourceString(){return "WOODEN|HEMP|SILK|CLOTH";}
	@Override
	public String parametersFormat(){ return
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t"
		+"ITEM_CLASS_ID\tRESOURCE_OR_MATERIAL\tSTATUE||\tN_A\tCODED_SPELL_LIST";}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int RCP_WOOD=3;
	protected static final int RCP_VALUE=4;
	protected static final int RCP_CLASSTYPE=5;
	protected static final int RCP_WOODTYPE=6;
	protected static final int RCP_MISCTYPE=7;
	//protected static final int RCP_MISCTEXT=8;
	protected static final int RCP_SPELL=9;

	@Override public boolean supportsDeconstruction() { return false; }

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

	@Override public String parametersFile(){ return "papermaking.txt";}
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
						commonTell(mob,_("<S-NAME> mess(es) up making @x1.",buildingI.name(mob)));
					else
						dropAWinner(mob,buildingI);
				}
				buildingI=null;
			}
		}
		super.unInvoke();
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
		final Session session=mob.session();

		final CraftParms parsedVars=super.parseAutoGenerate(auto,givenTarget,commands);
		givenTarget=parsedVars.givenTarget;

		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,parsedVars.autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,_("Papermake what? Enter \"papermake list\" for a list, or \"papermake stop\" to cancel."));
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
					ListingLibrary.ColFixer.fixColWidth(22,mob.session()),
					ListingLibrary.ColFixer.fixColWidth(3,mob.session())
				};
			final StringBuffer buf=new StringBuffer(_("@x1 @x2 Material required\n\r",CMStrings.padRight(_("Item"),cols[0]),CMStrings.padRight(_("Lvl"),cols[1])));
			for(int r=0;r<recipes.size();r++)
			{
				final List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					final String item=replacePercent(V.get(RCP_FINALNAME),"");
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					String material=V.get(RCP_WOODTYPE);
					final String wood=getComponentDescription(mob,V,RCP_WOOD);
					if(wood.length()>5) material="";
					if(((level<=xlevel(mob))||allFlag)
					&&((mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
						buf.append(CMStrings.padRight(item,cols[0])+" "+CMStrings.padRight(""+level,cols[1])+" "+wood+" "+material.toLowerCase()+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		activity = CraftingActivity.CRAFTING;
		buildingI=null;
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
		String materialDesc="";
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
					materialDesc=foundRecipe.get(RCP_WOODTYPE);
					if(materialDesc.equalsIgnoreCase("WOOD"))
						materialDesc="WOODEN";
					break;
				}
			}
		}

		if(materialDesc.length()==0)
			materialDesc="WOODEN";

		if(foundRecipe==null)
		{
			commonTell(mob,_("You don't know how to make a '@x1'.  Try \"make list\" for a list.",recipeName));
			return false;
		}

		final String woodRequiredStr = foundRecipe.get(RCP_WOOD);
		final List<Object> componentsFoundList=getAbilityComponents(mob, woodRequiredStr, "make "+CMLib.english().startWithAorAn(recipeName),parsedVars.autoGenerate);
		if(componentsFoundList==null) return false;
		int woodRequired=CMath.s_int(woodRequiredStr);
		woodRequired=adjustWoodRequired(woodRequired,mob);

		final int[][] data=fetchFoundResourceData(mob,
											woodRequired,materialDesc,null,
											0,null,null,
											false,
											parsedVars.autoGenerate,
											null);
		if(data==null) return false;
		woodRequired=data[0][FOUND_AMT];

		final String misctype=(foundRecipe.size()>RCP_MISCTYPE)?foundRecipe.get(RCP_MISCTYPE).trim():"";
		if((misctype.equalsIgnoreCase("statue"))
		&&(session!=null)
		&&((statue==null)||(statue.trim().length()==0)))
		{
			final Ability me=this;
			final Physical target=givenTarget;
			session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
			{
				@Override public void showPrompt() {session.promptPrint(_("What is this of?\n\r: "));}
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

		if(parsedVars.autoGenerate<=0)
		{
			CMLib.materials().destroyResourcesValue(mob.location(),woodRequired,data[0][FOUND_CODE],0,null);
			CMLib.ableMapper().destroyAbilityComponents(componentsFoundList);
		}
		buildingI=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
		if(buildingI==null)
		{
			commonTell(mob,_("There's no such thing as a @x1!!!",foundRecipe.get(RCP_CLASSTYPE)));
			return false;
		}
		duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),4);
		String itemName=foundRecipe.get(RCP_FINALNAME).toLowerCase();
		itemName=CMLib.english().startWithAorAn(itemName);
		buildingI.setName(itemName);
		startStr=_("<S-NAME> start(s) making @x1.",buildingI.name());
		displayText=_("You are making @x1",buildingI.name());
		verb=_("making @x1",buildingI.name());
		playSound="crumple.wav";
		buildingI.setDisplayText(_("@x1 lies here",itemName));
		buildingI.setDescription(itemName+". ");
		buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired,bundling));
		buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE))+(woodRequired*(RawMaterial.CODES.VALUE(data[0][FOUND_CODE]))));
		buildingI.setMaterial(data[0][FOUND_CODE]);
		final String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
		addSpells(buildingI,spell);
		buildingI.setSecretIdentity(getBrand(mob));
		if(((data[0][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN)
		||(data[0][FOUND_CODE]==RawMaterial.RESOURCE_RICE))
			buildingI.setMaterial(RawMaterial.RESOURCE_PAPER);
		if(buildingI instanceof Recipe)
			((Recipe)buildingI).setTotalRecipePages(CMath.s_int(woodRequiredStr));
		buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL)));
		buildingI.recoverPhyStats();
		buildingI.text();
		buildingI.recoverPhyStats();

		if((misctype.equalsIgnoreCase("statue"))
		&&(statue!=null)
		&&(statue.trim().length()>0))
		{
			if(buildingI.Name().indexOf('%')>0)
			{
				buildingI.setName(CMStrings.replaceAll(buildingI.Name(), "%", statue.trim()));
				buildingI.setDisplayText(CMStrings.replaceAll(buildingI.displayText(), "%", statue.trim()));
				buildingI.setDescription(CMStrings.replaceAll(buildingI.description(), "%", statue.trim()));
			}
			else
			{
				buildingI.setName(_("@x1 of @x2",itemName,statue.trim()));
				buildingI.setDisplayText(_("@x1 of @x2 is here",itemName,statue.trim()));
				buildingI.setDescription(_("@x1 of @x2. ",itemName,statue.trim()));
			}
			verb=_("making @x1",buildingI.name());
		}

		messedUp=!proficiencyCheck(mob,0,auto);

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
		return true;
	}
}
