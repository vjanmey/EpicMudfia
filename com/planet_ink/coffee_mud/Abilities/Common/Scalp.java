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

@SuppressWarnings({"unchecked","rawtypes"})
public class Scalp extends CommonSkill
{
	@Override public String ID() { return "Scalp"; }
	private final static String localizedName = CMLib.lang()._("Scalping");
	@Override public String name() { return localizedName; }
	private static final String[] triggerStrings =_i(new String[] {"SCALP","SCALPING"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	public static Vector lastSoManyScalps=new Vector();
	@Override public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_ANATOMY;}

	private DeadBody body=null;
	protected boolean failed=false;
	public Scalp()
	{
		super();
		displayText=_("You are scalping something...");
		verb=_("scalping");
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((body!=null)
		&&(affected instanceof MOB)
		&&(((MOB)affected).location()!=null)
		&&((!((MOB)affected).location().isContent(body)))
		&&((!((MOB)affected).isMine(body))))
			unInvoke();
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
				if((body!=null)&&(!aborted))
				{
					if((failed)||(!mob.location().isContent(body)))
						commonTell(mob,_("You messed up your scalping completely."));
					else
					{
						mob.location().show(mob,null,body,getActivityMessageType(),_("<S-NAME> manage(s) to scalp <O-NAME>."));
						lastSoManyScalps.addElement(body);
						if(lastSoManyScalps.size()>100)
							lastSoManyScalps.removeElementAt(0);
						final Item scalp=CMClass.getItem("GenItem");
						String race="";
						if((body.charStats()!=null)&&(body.charStats().getMyRace()!=null))
							race=" "+body.charStats().getMyRace().name();
						if(body.name().startsWith("the body"))
							scalp.setName(_("the@x1 scalp@x2",race,body.name().substring(8)));
						else
							scalp.setName(_("a@x1 scalp",race));
						if(body.displayText().startsWith("the body"))
							scalp.setDisplayText(_("the@x1 scalp@x2",race,body.displayText().substring(8)));
						else
							scalp.setDisplayText(_("a@x1 scalp sits here",race));
						scalp.setBaseValue(1);
						scalp.setDescription(_("This is the bloody top of that poor creatures head."));
						scalp.setMaterial(RawMaterial.RESOURCE_MEAT);
						scalp.setSecretIdentity("This scalp was cut by "+mob.name()+".");
						mob.location().addItem(scalp,ItemPossessor.Expire.Monster_EQ);
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
		body=null;
		Item I=null;
		if((mob.isMonster()
		&&(!CMLib.flags().isAnimalIntelligence(mob)))
		&&(commands.size()==0))
		{
			for(int i=0;i<mob.location().numItems();i++)
			{
				final Item I2=mob.location().getItem(i);
				if((I2!=null)
				&&(I2 instanceof DeadBody)
				&&(CMLib.flags().canBeSeenBy(I2,mob))
				&&(I2.container()==null))
				{
					I=I2;
					break;
				}
			}
		}
		else
			I=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);

		if(I==null) return false;
		if((!(I instanceof DeadBody))
		   ||(((DeadBody)I).charStats()==null)
		   ||(((DeadBody)I).charStats().getMyRace()==null)
		   ||(((DeadBody)I).charStats().getMyRace().bodyMask()[Race.BODY_HEAD]==0))
		{
			commonTell(mob,_("You can't scalp @x1.",I.name(mob)));
			return false;
		}
		if(lastSoManyScalps.contains(I))
		{
			commonTell(mob,_("@x1 has already been scalped.",I.name(mob)));
			return false;

		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		failed=!proficiencyCheck(mob,0,auto);
		final CMMsg msg=CMClass.getMsg(mob,I,this,getActivityMessageType(),getActivityMessageType(),getActivityMessageType(),_("<S-NAME> start(s) scalping <T-NAME>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			I=(Item)msg.target();
			body=(DeadBody)I;
			verb=_("scalping @x1",I.name());
			playSound="ripping.wav";
			int duration=(I.phyStats().weight()/(10+getXLEVELLevel(mob)));
			if(duration<3) duration=3;
			if(duration>40) duration=40;
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
