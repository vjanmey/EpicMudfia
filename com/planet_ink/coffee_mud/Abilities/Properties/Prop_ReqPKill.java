package com.planet_ink.coffee_mud.Abilities.Properties;
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
public class Prop_ReqPKill extends Property implements TriggeredAffect
{
	@Override public String ID() { return "Prop_ReqPKill"; }
	@Override public String name(){ return "Playerkill ONLY Zone";}
	@Override protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;}

	@Override public long flags(){return Ability.FLAG_ZAPPER;}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_ENTER;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&(msg.target()!=null)
		&&(((msg.target() instanceof Room)&&(msg.targetMinor()==CMMsg.TYP_ENTER))
		   ||((msg.target() instanceof Rideable)&&(msg.targetMinor()==CMMsg.TYP_SIT)))
		&&(!CMLib.flags().isFalling(msg.source()))
		&&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area)))
		{
			if((!msg.source().isMonster())
			   &&(!CMath.bset(msg.source().getBitmap(),MOB.ATT_PLAYERKILL)))
			{
				msg.source().tell(_("You must have your playerkill flag set to enter here."));
				return false;
			}
		}
		if((!msg.source().isMonster())
 		&&(!CMath.bset(msg.source().getBitmap(),MOB.ATT_PLAYERKILL)))
		{
			final Room R=CMLib.map().roomLocation(msg.source());
			if((R!=null)&&((R==affected)||(R.getArea()==affected)||((affected instanceof Area)&&(((Area)affected).inMyMetroArea(R.getArea())))))
			{
				msg.source().tell(_("Your PLAYERKILL flag is now ON!"));
				msg.source().setBitmap(CMath.setb(msg.source().getBitmap(),MOB.ATT_PLAYERKILL));
			}
		}
		return super.okMessage(myHost,msg);
	}
}
