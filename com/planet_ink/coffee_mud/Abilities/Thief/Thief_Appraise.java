package com.planet_ink.coffee_mud.Abilities.Thief;
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
public class Thief_Appraise extends ThiefSkill
{
	@Override public String ID() { return "Thief_Appraise"; }
	private final static String localizedName = CMLib.lang()._("Appraise");
	@Override public String name() { return localizedName; }
	@Override protected int canAffectCode(){return 0;}
	@Override protected int canTargetCode(){return Ability.CAN_ITEMS;}
	@Override public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings =_i(new String[] {"APPRAISE"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override protected boolean disregardsArmorCheck(MOB mob){return true;}
	public int code=0;
	@Override public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STREETSMARTS;}

	@Override public int abilityCode(){return code;}
	@Override public void setAbilityCode(int newCode){code=newCode;}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell(_("What would you like to appraise?"));
			return false;
		}
		final Item target=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,(String)commands.elementAt(0));
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(_("You don't see '@x1' here.",((String)commands.elementAt(0))));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+abilityCode()+(2*super.getXLEVELLevel(mob)));
		if(levelDiff<0) levelDiff=0;
		levelDiff*=5;
		final boolean success=proficiencyCheck(mob,-levelDiff,auto);

		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,_("<S-NAME> appraise(s) <T-NAMESELF>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			double realValue=0.0;
			if(target instanceof Coins)
				realValue = ((Coins)target).getTotalValue();
			else
				realValue=target.value();
			int materialCode=target.material();
			int weight=target.basePhyStats().weight();
			int height=target.basePhyStats().height();
			int allWeight=target.phyStats().weight();
			if(!success)
			{
				final double deviance=CMath.div(CMLib.dice().roll(1,100,0)+50,100);
				realValue=CMath.mul(realValue,deviance);
				materialCode=CMLib.dice().roll(1,RawMaterial.CODES.TOTAL(),-1);
				weight=(int)Math.round(CMath.mul(weight,deviance));
				height=(int)Math.round(CMath.mul(height,deviance));
				allWeight=(int)Math.round(CMath.mul(allWeight,deviance));
			}
			final StringBuffer str=new StringBuffer("");
			str.append(_("@x1 is made of @x2",target.name(mob),RawMaterial.CODES.NAME(materialCode)));
			str.append(_(" is worth about @x1.",CMLib.beanCounter().nameCurrencyShort(mob,realValue)));
			if(target instanceof Armor)
				str.append(_("\n\r@x1 is a size @x2.",target.name(mob),""+height));
			if(weight!=allWeight)
				str.append(_("\n\rIt weighs @x1 pounds empty and @x2 pounds right now.",""+weight,""+allWeight));
			else
				str.append(_("\n\rIt weighs @x1 pounds.",""+weight));
			mob.tell(str.toString());
		}
		return success;
	}

}
