package com.planet_ink.coffee_mud.Abilities.Spells;
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
public class Spell_Command extends Spell
{
	@Override public String ID() { return "Spell_Command"; }
	private final static String localizedName = CMLib.lang()._("Command");
	@Override public String name() { return localizedName; }
	@Override public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	@Override public int classificationCode(){	return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Vector V=new Vector();
		if(commands.size()>0)
		{
			V.addElement(commands.elementAt(0));
			commands.removeElementAt(0);
		}

		final MOB target=getTarget(mob,V,givenTarget);
		if(target==null) return false;

		if(commands.size()==0)
		{
			if(mob.isMonster())
				commands.addElement("FLEE");
			else
			{
				mob.tell(_("Command @x1 to do what?",((String)V.elementAt(0))));
				return false;
			}
		}

		if((!target.mayIFight(mob))||(!target.isMonster()))
		{
			mob.tell(_("You can't command @x1.",target.name(mob)));
			return false;
		}

		if(((String)commands.elementAt(0)).toUpperCase().startsWith("FOL"))
		{
			mob.tell(_("You can't command someone to follow."));
			return false;
		}

		CMObject O=CMLib.english().findCommand(target,(Vector)commands.clone());
		if(O instanceof Command)
		{
			if((!((Command)O).canBeOrdered())||(!((Command)O).securityCheck(mob))||(((Command)O).ID().equals("Sleep")))
			{
				mob.tell(_("You can't command someone to doing that."));
				return false;
			}
		}
		else
		{
			if(O instanceof Ability)
				O=CMLib.english().getToEvoke(target,(Vector)commands.clone());
			if(O instanceof Ability)
			{
				if(CMath.bset(((Ability)O).flags(),Ability.FLAG_NOORDERING))
				{
					mob.tell(_("You can't command @x1 to do that.",target.name(mob)));
					return false;
				}
			}
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":_("^S<S-NAME> command(s) <T-NAMESELF> to '@x1'.^?",CMParms.combine(commands,0)));
			final CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
			final CMMsg omsg=CMClass.getMsg(mob,target,null,CMMsg.MSG_ORDER,null);
			if((mob.location().okMessage(mob,msg))
			&&((mob.location().okMessage(mob,msg2)))
			&&(mob.location().okMessage(mob, omsg)))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().send(mob,msg2);
					mob.location().send(mob,omsg);
					if((msg2.value()<=0)&&(omsg.sourceMinor()==CMMsg.TYP_ORDER))
					{
						invoker=mob;
						target.makePeace();
						target.enqueCommand(commands,Command.METAFLAG_FORCED|Command.METAFLAG_ORDER,0);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,_("<S-NAME> attempt(s) to command <T-NAMESELF>, but it definitely didn't work."));


		// return whether it worked
		return success;
	}
}
