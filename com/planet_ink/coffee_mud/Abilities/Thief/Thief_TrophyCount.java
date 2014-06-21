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

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

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
public class Thief_TrophyCount extends ThiefSkill
{
	@Override public String ID() { return "Thief_TrophyCount"; }
	private final static String localizedName = CMLib.lang()._("Trophy Count");
	@Override public String name() { return localizedName; }
	@Override public String displayText(){ return "";}
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override protected int canTargetCode(){return 0;}
	@Override public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	@Override public boolean isAutoInvoked(){return true;}
	@Override public boolean canBeUninvoked(){return false;}
	private static final String[] triggerStrings =_i(new String[] {"TROPHYCOUNT"});
	@Override protected boolean disregardsArmorCheck(MOB mob){return true;}
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_COMBATLORE;}
	Hashtable theList=new Hashtable();

	@Override
	public String text()
	{
		final StringBuffer str=new StringBuffer("<MOBS>");
		for(final Enumeration e=theList.elements();e.hasMoreElements();)
		{
			final String[] one=(String[])e.nextElement();
			str.append("<MOB>");
			str.append(CMLib.xml().convertXMLtoTag("RACE",one[0]));
			str.append(CMLib.xml().convertXMLtoTag("KILLS",one[1]));
			str.append("</MOB>");
		}
		str.append("</MOBS>");
		return str.toString();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_DEATH)
		&&(msg.tool()!=null)
		&&(msg.tool()==affected))
		{
			final Race R=msg.source().charStats().getMyRace();
			if(!R.ID().equalsIgnoreCase("StdRace"))
			{
				String[] set=(String[])theList.get(R.name());
				if(set==null)
				{
					set=new String[4];
					set[0]=R.name();
					set[1]="0";
					theList.put(R.name(),set);
				}
				set[1]=Integer.toString(CMath.s_int(set[1])+1);
				if((affected!=null)&&(affected instanceof MOB))
				{
					final Ability A=((MOB)affected).fetchAbility(ID());
					if(A!=null)	A.setMiscText(text());
				}
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public void setMiscText(String str)
	{
		theList.clear();
		if((str.trim().length()>0)&&(str.trim().startsWith("<MOBS>")))
		{
			final List<XMLLibrary.XMLpiece> buf=CMLib.xml().parseAllXML(str);
			final List<XMLLibrary.XMLpiece> V=CMLib.xml().getContentsFromPieces(buf,"MOBS");
			if(V!=null)
			for(int i=0;i<V.size();i++)
			{
				final XMLLibrary.XMLpiece ablk=V.get(i);
				if(ablk.tag.equalsIgnoreCase("MOB"))
				{
					final String[] one=new String[4];
					one[0]=CMLib.xml().getValFromPieces(ablk.contents,"RACE");
					one[1]=CMLib.xml().getValFromPieces(ablk.contents,"KILLS");
					theList.put(one[0],one);
				}
			}
		}
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(proficiencyCheck(mob,0,auto))
		{
			final StringBuffer str=new StringBuffer("");
			str.append(_("@x1Kills\n\r",CMStrings.padRight(_("Name"),20)));
			for(final Enumeration e=theList.elements();e.hasMoreElements();)
			{
				final String[] one=(String[])e.nextElement();
				final int kills=CMath.s_int(one[1]);
				str.append(CMStrings.padRight(one[0],20)+kills+"\n\r");
			}
			if(mob.session()!=null)
				mob.session().rawPrintln(str.toString());
			return true;
		}
		mob.tell(_("You failed to recall your count."));
		return false;
	}
}
