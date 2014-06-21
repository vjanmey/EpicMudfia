package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
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
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.CMLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.interfaces.WebMacro;

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
public class Generate extends StdCommand
{
	public Generate(){}
	private static final SHashtable<String,CMClass.CMObjectType> OBJECT_TYPES=new SHashtable<String,CMClass.CMObjectType>(new Object[][]{
			{"STRING",CMClass.CMObjectType.LIBRARY},
			{"AREA",CMClass.CMObjectType.AREA},
			{"MOB",CMClass.CMObjectType.MOB},
			{"ROOM",CMClass.CMObjectType.LOCALE},
			{"ITEM",CMClass.CMObjectType.ITEM},
	});

	private final String[] access=_i(new String[]{"GENERATE"});
	@Override public String[] getAccessWords(){return access;}

	public void createNewPlace(MOB mob, Room oldR, Room R, int direction)
	{
		if(R.roomID().length()==0)
		{
			R.setArea(oldR.getArea());
			R.setRoomID(oldR.getArea().getNewRoomID(oldR, direction));
		}
		Exit E=R.getExitInDir(Directions.getOpDirectionCode(direction));
		if(E==null) E = CMClass.getExit("Open");
		oldR.setRawExit(direction, E);
		oldR.rawDoors()[direction]=R;
		final int opDir=Directions.getOpDirectionCode(direction);
		if(R.getRoomInDir(opDir)!=null)
			mob.tell(_("An error has caused the following exit to be one-way."));
		else
		{
			R.setRawExit(opDir, E);
			R.rawDoors()[opDir]=oldR;
		}
		CMLib.database().DBUpdateExits(oldR);
		final String dirName=((R instanceof SpaceShip)||(R.getArea() instanceof SpaceShip))?
				Directions.getShipDirectionName(direction):Directions.getDirectionName(direction);
		oldR.showHappens(CMMsg.MSG_OK_VISUAL,_("A new place materializes to the @x1",dirName));
	}

	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<3)
		{
			mob.tell(_("Generate what? Try GENERATE [TYPE] [ID] (FROM [DATA_FILE_PATH]) ([VAR=VALUE]..) [DIRECTION]"));
			return false;
		}
		final String finalLog = mob.Name()+" called generate command with parms: " + CMParms.combine(commands, 1);
		CMFile file = null;
		if((commands.size()>3)&&((String)commands.elementAt(3)).equalsIgnoreCase("FROM"))
		{
			file = new CMFile(Resources.buildResourcePath((String)commands.elementAt(4)),mob);
			commands.removeElementAt(3);
			commands.removeElementAt(3);
		}
		else
			file = new CMFile(Resources.buildResourcePath("randareas/example.xml"),mob);
		if(!file.canRead())
		{
			mob.tell(_("Random data file '@x1' not found.  Aborting.",file.getCanonicalPath()));
			return false;
		}
		final StringBuffer xml = file.textUnformatted();
		final List<XMLLibrary.XMLpiece> xmlRoot = CMLib.xml().parseAllXML(xml);
		final Hashtable definedIDs = new Hashtable();
		CMLib.percolator().buildDefinedIDSet(xmlRoot,definedIDs);
		final String typeName = (String)commands.elementAt(1);
		String objectType = typeName.toUpperCase().trim();
		CMClass.CMObjectType codeI=OBJECT_TYPES.get(objectType);
		if(codeI==null)
		{
			for(final Enumeration e=OBJECT_TYPES.keys();e.hasMoreElements();)
			{
				final String key =(String)e.nextElement();
				if(key.startsWith(typeName.toUpperCase().trim()))
				{
					objectType = key;
					codeI=OBJECT_TYPES.get(key);
				}
			}
			if(codeI==null)
			{
				mob.tell(_("'@x1' is an unknown object type.  Try: @x2",typeName,CMParms.toStringList(OBJECT_TYPES.keys())));
				return false;
			}
		}
		int direction=-1;
		if((codeI==CMClass.CMObjectType.AREA)||(codeI==CMClass.CMObjectType.LOCALE))
		{
			final String possDir=(String)commands.lastElement();
			direction = Directions.getGoodDirectionCode(possDir);
			if(direction<0)
			{
				mob.tell(_("When creating an area or room, the LAST parameter to this command must be a direction to link to this room by."));
				return false;
			}
			if(mob.location().getRoomInDir(direction)!=null)
			{
				final String dirName=((mob.location() instanceof SpaceShip)||(mob.location().getArea() instanceof SpaceShip))?
						Directions.getShipDirectionName(direction):Directions.getDirectionName(direction);
				mob.tell(_("A room already exists in direction @x1. Action aborted.",dirName));
				return false;
			}
		}
		final String idName = ((String)commands.elementAt(2)).toUpperCase().trim();
		if((!(definedIDs.get(idName) instanceof XMLLibrary.XMLpiece))
		||(!((XMLLibrary.XMLpiece)definedIDs.get(idName)).tag.equalsIgnoreCase(objectType)))
		{
			mob.tell(_("The @x1 id '@x2' has not been defined in the data file.",objectType,idName));
			final StringBuffer foundIDs=new StringBuffer("");
			for(final Enumeration tkeye=OBJECT_TYPES.keys();tkeye.hasMoreElements();)
			{
				final String tKey=(String)tkeye.nextElement();
				foundIDs.append("^H"+tKey+"^N: \n\r");
				final Vector xmlTagsV=new Vector();
				for(final Enumeration keys=definedIDs.keys();keys.hasMoreElements();)
				{
					final String key=(String)keys.nextElement();
					if((definedIDs.get(key) instanceof XMLLibrary.XMLpiece)
					&&(((XMLLibrary.XMLpiece)definedIDs.get(key)).tag.equalsIgnoreCase(tKey)))
						xmlTagsV.addElement(key.toLowerCase());
				}
				foundIDs.append(CMParms.toStringList(xmlTagsV)+"\n\r");
			}
			mob.tell(_("Found ids include: \n\r@x1",foundIDs.toString()));
			return false;
		}

		final XMLLibrary.XMLpiece piece=(XMLLibrary.XMLpiece)definedIDs.get(idName);
		definedIDs.putAll(CMParms.parseEQParms(commands,3,commands.size()));
		try
		{
			CMLib.percolator().checkRequirements(piece, definedIDs);
		}
		catch(final CMException cme)
		{
			mob.tell(_("Required ids for @x1 were missing: @x2",idName,cme.getMessage()));
			return false;
		}
		final Vector V = new Vector();
		try
		{
			switch(codeI)
			{
			case LIBRARY:
			{
				CMLib.percolator().preDefineReward(null, null, null, piece, definedIDs);
				CMLib.percolator().defineReward(null, null, null, piece, piece.value,definedIDs);
				final String s=CMLib.percolator().findString("STRING", piece, definedIDs);
				if(s!=null)
					V.addElement(s);
				break;
			}
			case AREA:
				CMLib.percolator().preDefineReward(null, null, null, piece, definedIDs);
				CMLib.percolator().defineReward(null, null, null, piece, piece.value,definedIDs);
				final Area A=CMLib.percolator().findArea(piece, definedIDs, direction);
				if(A!=null)
					V.addElement(A);
				break;
			case MOB:
				CMLib.percolator().preDefineReward(null, null, null, piece, definedIDs);
				CMLib.percolator().defineReward(null, null, null, piece, piece.value,definedIDs);
				V.addAll(CMLib.percolator().findMobs(piece, definedIDs));
				break;
			case LOCALE:
			{
				final Exit[] exits=new Exit[Directions.NUM_DIRECTIONS()];
				CMLib.percolator().preDefineReward(null, null, null, piece, definedIDs);
				CMLib.percolator().defineReward(null, null, null, piece, piece.value,definedIDs);
				final Room R=CMLib.percolator().buildRoom(piece, definedIDs, exits, direction);
				if(R!=null)
					V.addElement(R);
				break;
			}
			case ITEM:
				CMLib.percolator().preDefineReward(null, null, null, piece, definedIDs);
				CMLib.percolator().defineReward(null, null, null, piece, piece.value,definedIDs);
				V.addAll(CMLib.percolator().findItems(piece, definedIDs));
				break;
			default:
				break;
			}
		}
		catch(final CMException cex)
		{
			mob.tell(_("Unable to generate: @x1",cex.getMessage()));
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
				Log.debugOut("Generate",cex);
			return false;
		}
		if(V.size()==0)
			mob.tell(_("Nothing generated."));
		else
		for(int v=0;v<V.size();v++)
			if(V.elementAt(v) instanceof MOB)
			{
				((MOB)V.elementAt(v)).bringToLife(mob.location(),true);
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,_("@x1 appears.",((MOB)V.elementAt(v)).name()));
				Log.sysOut("Generate",mob.Name()+" generated mob "+((MOB)V.elementAt(v)).name());
			}
			else
			if(V.elementAt(v) instanceof Item)
			{
				mob.location().addItem((Item)V.elementAt(v));
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,_("@x1 appears.",((Item)V.elementAt(v)).name()));
				Log.sysOut("Generate",mob.Name()+" generated item "+((Item)V.elementAt(v)).name());
			}
			else
			if(V.elementAt(v) instanceof String)
				mob.tell((String)V.elementAt(v));
			else
			if(V.elementAt(v) instanceof Room)
			{
				final Room R=(Room)V.elementAt(v);
				createNewPlace(mob,mob.location(),R,direction);
				CMLib.database().DBCreateRoom(R);
				CMLib.database().DBUpdateExits(R);
				CMLib.database().DBUpdateItems(R);
				CMLib.database().DBUpdateMOBs(R);
				Log.sysOut("Generate",mob.Name()+" generated room "+R.roomID());
			}
			else
			if(V.elementAt(v) instanceof Area)
			{
				final Area A=(Area)V.elementAt(v);
				CMLib.map().addArea(A);
				CMLib.database().DBCreateArea(A);
				Room R=A.getRoom(A.Name()+"#0");
				if(R==null) R=A.getFilledProperMap().nextElement();
				createNewPlace(mob,mob.location(),R,direction);
				mob.tell(_("Saving remaining rooms for area '@x1'...",A.name()));
				for(final Enumeration e=A.getFilledProperMap();e.hasMoreElements();)
				{
					R=(Room)e.nextElement();
					CMLib.database().DBCreateRoom(R);
					CMLib.database().DBUpdateExits(R);
					CMLib.database().DBUpdateItems(R);
					CMLib.database().DBUpdateMOBs(R);
				}
				mob.tell(_("Done saving remaining rooms for area '@x1'",A.name()));
				Log.sysOut("Generate",mob.Name()+" generated area "+A.name());
			}
		Log.sysOut("Generate",finalLog);
		return true;
	}

	@Override public boolean canBeOrdered(){return false;}

	@Override public boolean securityCheck(MOB mob){return CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.CMDAREAS);}
}
