package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary.CataData;
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
public class Catalog extends StdCommand
{
	public Catalog(){}

	private final String[] access=_i(new String[]{"CATALOG"});
	@Override public String[] getAccessWords(){return access;}

	protected TreeMap<String,String> currentCats=new TreeMap<String,String>();

	public boolean catalog(Room R, MOB mob, Physical P)
		throws java.io.IOException
	{
		final Physical origP=P;
		final Physical cataP=CMLib.catalog().getCatalogObj(P);
		if((!(P instanceof DBIdentifiable))
		||(!((DBIdentifiable)P).canSaveDatabaseID()))
		{
			mob.tell(_("The object '@x1' can not be cataloged.",P.Name()));
			return false;
		}
		String newCat=currentCats.get(mob.Name());
		if(newCat==null) newCat="";
		String msg="<S-NAME> catalog(s) <T-NAMESELF> into category '"+newCat+"'.";
		final CataData data=CMLib.catalog().getCatalogData(cataP);
		final String oldCat=(data!=null)?data.category():"";
		final String catagory=(data!=null)?" in category '"+oldCat+"'":"";
		/*
		if(CMLib.flags().isCataloged(P))
		{
			mob.tell(_("The object '@x1' is already cataloged@x2.",P.Name(),catagory));
			return false;
		}
		*/
		if(cataP!=null)
		{
			CMLib.catalog().changeCatalogUsage(P,true);
			final StringBuffer diffs=CMLib.catalog().checkCatalogIntegrity(P);
			if((diffs==null)||(diffs.length()==0))
			{
				if((data!=null)&&(!data.category().equals(newCat)))
				{
					if((mob.session()==null)
					||(!mob.session().confirm(_("The object '@x1' already exists in the catalog@x2 , exactly as it is.  Would you like to change it to category '@x3'(y/N)?",cataP.Name(),catagory,newCat),_("N"))))
					{
						return false;
					}
					else
					{
						CMLib.catalog().updateCatalogCatagory(cataP,newCat);
						return true;
					}
				}
				else
				{
					mob.tell(_("The object '@x1' already exists in the catalog@x2 , exactly as it is.",cataP.Name(),catagory));
					return true;
				}
			}
			if((data!=null)&&(!data.category().equals(newCat)))
				diffs.insert(0,"New category: '"+newCat+"', ");
			if((mob.session()==null)
			||(!mob.session().confirm(_("Cataloging that object will change the existing cataloged '@x1'@x2 by altering the following properties: @x3.  Please confirm (y/N)?",P.Name(),catagory,diffs.toString()),_("Y"))))
			{
				CMLib.catalog().changeCatalogUsage(origP,false);
				return false;
			}
			msg="<S-NAME> modif(ys) the cataloged version of <T-NAMESELF>.";
			if((data!=null)&&(!data.category().equals(newCat)))
				CMLib.catalog().updateCatalogCatagory(cataP,newCat);
			CMLib.catalog().updateCatalog(P);
		}
		else
		{
			CMLib.catalog().addCatalog(newCat,P);
		}
		R.show(mob,P,CMMsg.MSG_OK_VISUAL,msg);
		return true;
	}


	public Physical findCatalog(int whatKind, String ID, boolean exactOnly)
	{
		final Object[] data=new Object[]{null,null};
		if((data[0]==null)&&((whatKind==0)||(whatKind==1)))
		{ data[0]=CMLib.catalog().getCatalogMob(ID); if(data[0]!=null) data[1]=Integer.valueOf(1);}
		if((data[0]==null)&&((whatKind==0)||(whatKind==2)))
		{ data[0]=CMLib.catalog().getCatalogItem(ID); if(data[0]!=null) data[1]=Integer.valueOf(2);}
		if(exactOnly) return (Physical)data[0];
		if((data[0]==null)&&((whatKind==0)||(whatKind==1)))
		{
			final String[] names=CMLib.catalog().getCatalogMobNames().clone();
			for (final String name : names)
				if(CMLib.english().containsString(name, ID))
				{	data[0]=CMLib.catalog().getCatalogMob(name); data[1]=Integer.valueOf(1); break;}
			if(data[0] == null)
			{
				for(int s=0;s<names.length;s++)
					names[s] = CMStrings.removeColors(names[s]);
				for (final String name : names)
					if(ID.equalsIgnoreCase(name))
					{	data[0]=CMLib.catalog().getCatalogMob(name); data[1]=Integer.valueOf(1); break;}
				if(data[0] == null)
					for (final String name : names)
						if(CMLib.english().containsString(name, ID))
						{	data[0]=CMLib.catalog().getCatalogMob(name); data[1]=Integer.valueOf(1); break;}
			}
		}
		if((data[0]==null)&&((whatKind==0)||(whatKind==2)))
		{
			final String[] names=CMLib.catalog().getCatalogItemNames().clone();
			for (final String name : names)
				if(CMLib.english().containsString(name, ID))
				{	data[0]=CMLib.catalog().getCatalogItem(name); data[1]=Integer.valueOf(2); break;}
			if(data[0] == null)
			{
				for(int s=0;s<names.length;s++)
					names[s] = CMStrings.removeColors(names[s]);
				for (final String name : names)
					if(ID.equalsIgnoreCase(name))
					{	data[0]=CMLib.catalog().getCatalogItem(name); data[1]=Integer.valueOf(2); break;}
				if(data[0] == null)
					for (final String name : names)
						if(CMLib.english().containsString(name, ID))
						{	data[0]=CMLib.catalog().getCatalogItem(name); data[1]=Integer.valueOf(2); break;}
			}
		}
		return (Physical)data[0];
	}

	public Enumeration getRoomSet(MOB mob, String which)
	{
		Enumeration rooms=null;
		if(which.equalsIgnoreCase("ROOM"))
			rooms=new XVector(CMLib.map().getExtendedRoomID(mob.location())).elements();
		else
		if(which.equalsIgnoreCase("AREA"))
			rooms=mob.location().getArea().getProperRoomnumbers().getRoomIDs();
		else
		if(which.equalsIgnoreCase("WORLD"))
			rooms=CMLib.map().roomIDs();
		return rooms;
	}

	public int getObjectType(Vector commands)
	{
		int whatKind=0;
		if((commands.size()>0)&&("MOBS".startsWith(((String)commands.firstElement()).toUpperCase().trim())))
		{ commands.removeElementAt(0); whatKind=1; }
		if((commands.size()>0)&&("ITEMS".startsWith(((String)commands.firstElement()).toUpperCase().trim())))
		{ commands.removeElementAt(0); whatKind=2;}
		return whatKind;
	}

	public boolean checkUserRoomSetEntry(Vector commands)
	{
		if(commands.size()==0) return false;
		if((((String)commands.firstElement()).equalsIgnoreCase("ROOM"))
		||(((String)commands.firstElement()).equalsIgnoreCase("AREA"))
		||(((String)commands.firstElement()).equalsIgnoreCase("WORLD")))
			return true;
		return false;

	}

	@Override
	public boolean execute(final MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		Room R=mob.location();
		if(R==null) return false;
		if((commands!=null)&&(commands.size()>1))
		{
			commands.removeElementAt(0);
			final String[] types={"object","mobs","items"};

			if(checkUserRoomSetEntry(commands))
			{
				final String which=((String)commands.firstElement()).toLowerCase();
				final Enumeration rooms=getRoomSet(mob,which);
				commands.removeElementAt(0);
				final int whatKind=getObjectType(commands);
				final String type=types[whatKind];

				if((mob.session()!=null)
				&&(mob.session().confirm(_("You are about to auto-catalog (room-reset and save) all @x1 @x2.\n\rThis command, if used improperly, may alter @x3 in this @x4.\n\rAre you absolutely sure (y/N)?",which,type,type,which),_("N")))
				&&((which.equalsIgnoreCase("ROOM"))||mob.session().confirm(_("I'm serious now.  You can't abort this, and it WILL modify stuff.\n\rHave you tested this command on small areas and know what you're doing?.\n\rAre you absolutely POSITIVELY sure (y/N)?"),_("N"))))
				{
					Physical P=null;
					String roomID=null;
					for(;rooms.hasMoreElements();)
					{
						roomID=(String)rooms.nextElement();
						R=CMLib.coffeeMaker().makeNewRoomContent(CMLib.map().getRoom(roomID),false);
						final Vector<CatalogLibrary.RoomContent> contents=CMLib.catalog().roomContent(R);
						boolean dirty=false;
						for(final CatalogLibrary.RoomContent content : contents)
						{
							P=content.P();
							if(P instanceof Coins) continue;
							if((P instanceof MOB)&&(whatKind!=2))
							{
								if(catalog(R,mob,P))
								{
									content.flagDirty();
									dirty=true;
								}
							}
							else
							if((P instanceof Item)&&(whatKind!=1))
							{
								if(catalog(R,mob,P))
								{
									content.flagDirty();
									dirty=true;
								}
							}
						}
						if(dirty)
						{
							CMLib.catalog().updateRoomContent(roomID, contents);
							CMLib.map().resetRoom(CMLib.map().getRoom(roomID), true);
						}
						R.destroy();
					}
				}
			}
			else
			if(((String)commands.firstElement()).equalsIgnoreCase("CATAGORY")||((String)commands.firstElement()).equalsIgnoreCase("CATEGORY"))
			{
				commands.removeElementAt(0);
				String ID=CMParms.combine(commands,0);
				if(ID.equalsIgnoreCase("none"))
				{
					commands.add("");
					ID="";
				}
				String oldCat=currentCats.get(mob.Name());
				if(oldCat == null) oldCat="";
				mob.tell(_("Your current category is '@x1'.",oldCat));
				if(commands.size()>0)
				{
					ID=ID.toUpperCase().trim();
					if(ID.length()>0)
					{
						if((!CMParms.contains(CMLib.catalog().getItemCatalogCatagories(),ID))
						&&(!CMParms.contains(CMLib.catalog().getMobCatalogCatagories(),ID)))
						{
							final Session session=mob.session();
							final String newCat=ID;
							if(newCat.equalsIgnoreCase("GLOBAL")||newCat.equalsIgnoreCase("NONE")||newCat.equalsIgnoreCase("UNCATEGORIZED"))
								mob.tell(_("That is not a valid new catagory to create."));
							else
							if(session!=null)
								session.prompt(new InputCallback(InputCallback.Type.CONFIRM,"N")
								{
									@Override public void showPrompt() { session.promptPrint(_("Create new category '@x1' (y/N)?",newCat));}
									@Override public void timedOut() {}
									@Override public void callBack()
									{
										if(this.confirmed)
										{
											currentCats.put(mob.Name(), newCat);
											mob.tell(_("Your category is now '@x1' for new mob/item catalog additions.",newCat));
											mob.tell(_("To change back to the global category, enter CATALOG CATAGORY NONE"));
										}
									}
								});
						}
						else
						{
							currentCats.put(mob.Name(), ID);
							mob.tell(_("Your category is now '@x1' for new mob/item catalog additions.",ID));
							mob.tell(_("To change back to the global category, enter CATALOG CATAGORY NONE"));
						}
					}
					else
					{
						currentCats.remove(mob.Name());
						mob.tell(_("Your category is now '' (the global category)."));
					}
				}
			}
			else
			if(((String)commands.firstElement()).equalsIgnoreCase("LIST"))
			{
				commands.removeElementAt(0);
				final int whatKind=getObjectType(commands);
				final String ID=CMParms.combine(commands,0);
				final StringBuffer list=new StringBuffer("");
				int col=0;
				MOB M=null;
				Item I=null;
				CatalogLibrary.CataData data;
				if((whatKind==0)||(whatKind==1))
				{
					String cat=currentCats.get(mob.Name());
					if(cat==null) cat="";
					list.append("^HMobs ("+(cat)+")\n\r^N");
					list.append(CMStrings.padRight(_("Name"),34)+" ");
					list.append(CMStrings.padRight("#",3));
					list.append(CMStrings.padRight(_("Name"),34)+" ");
					list.append(CMStrings.padRight("#",3));
					list.append("\n\r"+CMStrings.repeat("-",78)+"\n\r");
					final String[] names=CMLib.catalog().getCatalogMobNames(cat);
					for (final String name : names)
					{
						M=CMLib.catalog().getCatalogMob(name);
						if(M!=null)
						{
							data=CMLib.catalog().getCatalogMobData(name);
							if((ID==null)||(ID.length()==0)||(CMLib.english().containsString(M.Name(),ID)))
							{
								list.append(CMStrings.padRight(M.Name(),34)).append(" ");
								list.append(CMStrings.padRight(Integer.toString(data.numReferences()),3));
								if(col==0)
								{
									col++;
								}
								else
								{
									col++;
									list.append("\n\r");
								}
							}
						}
					}
					list.append("\n\r\n\r");
				}
				if((whatKind==0)||(whatKind==2))
				{
					String cat=currentCats.get(mob.Name());
					if(cat==null) cat="";
					list.append("^HItems ("+(cat)+")\n\r^N");
					list.append(CMStrings.padRight(_("Name"),34)+" ");
					list.append(CMStrings.padRight("#",3)+" ");
					list.append(CMStrings.padRight(_("Rate"),6)+" ");
					list.append(CMStrings.padRight(_("Mask"),31)+" ");
					list.append("\n\r"+CMStrings.repeat("-",78)+"\n\r");
					final String[] names=CMLib.catalog().getCatalogItemNames(cat);
					for (final String name : names)
					{
						I=CMLib.catalog().getCatalogItem(name);
						if(I!=null)
						{
							data=CMLib.catalog().getCatalogItemData(name);
							if((ID==null)||(ID.length()==0)||(CMLib.english().containsString(I.Name(),ID)))
							{
								list.append(CMStrings.padRight(I.Name(),34)+" ");
								list.append(CMStrings.padRight(Integer.toString(data.numReferences()),3)+" ");
								if(data.getRate()<=0.0)
								{
									list.append("N/A   ");
									list.append(CMStrings.padRight(" ",31));
								}
								else
								{
									list.append(CMStrings.padRight(CMath.toPct(data.getRate()),6)+" ");
									list.append(data.getWhenLive()?"L ":"D ");
									list.append(CMStrings.padRight(data.getMaskStr(),29));
								}
								list.append("\n\r");
							}
						}
					}
					list.append("\n\r");
				}
				if(mob.session()!=null)
					mob.session().wraplessPrintln(list.toString());
			}
			else
			if(((String)commands.firstElement()).equalsIgnoreCase("DELETE"))
			{
				commands.removeElementAt(0);
				final int whatKind=getObjectType(commands);
				final String ID=CMParms.combine(commands,0);
				Physical[] del=null;
				if(ID.equalsIgnoreCase("everydamnmob"))
					del=CMLib.catalog().getCatalogMobs();
				else
				if(ID.equalsIgnoreCase("everydamnitem"))
					del=CMLib.catalog().getCatalogItems();
				else
				if(ID.equalsIgnoreCase("everydamnthing"))
				{
					final java.util.List<Physical> V=new Vector<Physical>();
					V.addAll(Arrays.asList(CMLib.catalog().getCatalogItems()));
					V.addAll(Arrays.asList(CMLib.catalog().getCatalogMobs()));
					del=V.toArray(new Physical[0]);
				}
				else
				{
					final Physical P=findCatalog(whatKind,ID,false);
					if(P==null)
					{
						mob.tell(_("'@x1' not found in catalog! Try CATALOG LIST",ID));
						return false;
					}
					del=new Physical[]{P};
				}
				for (final Physical P : del)
				{
					final CatalogLibrary.CataData data=CMLib.catalog().getCatalogData(P);
					if(P instanceof MOB)
					{
						String prefix="";
						if((data!=null)&&(data.numReferences()>0))
							prefix="Catalog MOB '"+((MOB)P).Name()+"' is currently listed as being in use '"+data.numReferences()+" times.  ";
						if((mob.session()!=null)
						&&((del.length>10)||(mob.session().confirm(_("@x1This will permanently delete mob '@x2' from the catalog.  Are you sure (y/N)?",prefix,((MOB)P).Name()),_("N")))))
						{
							CMLib.catalog().delCatalog(P);
							mob.tell(_("MOB '@x1 has been permanently removed from the catalog.",((MOB)P).Name()));
						}
					}
					else
					if(P instanceof Item)
					{
						String prefix="";
						if((data!=null)&&(data.numReferences()>0))
							prefix="Catalog Item '"+((Item)P).Name()+"' is currently listed as being in use '"+data.numReferences()+" times.  ";
						if((mob.session()!=null)
						&&((del.length>10)||(mob.session().confirm(_("@x1This will permanently delete item '@x2' from the catalog.  Are you sure (y/N)?",prefix,((Item)P).Name()),_("N")))))
						{
							CMLib.catalog().delCatalog(P);
							mob.tell(_("Item '@x1 has been permanently removed from the catalog.",P.Name()));
						}
					}
				}
			}
			else
			if(((String)commands.firstElement()).equalsIgnoreCase("EDIT"))
			{
				commands.removeElementAt(0);
				final int whatKind=getObjectType(commands);
				final String ID=CMParms.combine(commands,0);
				final Physical P=findCatalog(whatKind,ID,false);
				if(P==null)
				{
					mob.tell(_("'@x1' not found in catalog! Try CATALOG LIST",ID));
					return false;
				}
				final CatalogLibrary.CataData data=CMLib.catalog().getCatalogData(P);
				if(P instanceof MOB)
				{
					mob.tell(_("There is no extra mob data to edit. See help on CATALOG."));
				}
				else
				if(P instanceof Item)
				{
					if(mob.session()!=null)
					{
						Double newPct=null;
						while(newPct == null)
						{
							final String newRate=mob.session().prompt(_("Enter a new Drop Rate or 0% to disable (@x1): ",CMath.toPct(data.getRate())), CMath.toPct(data.getRate()));
							if(newRate.trim().length()==0)
								return false;
							else
							if(CMath.isPct(newRate))
								newPct=Double.valueOf(CMath.s_pct(newRate));
							else
								mob.tell(_("'@x1' is not a valid percentage value.  Try something like 10%",newRate));
						}
						data.setRate(newPct.doubleValue());
						if(data.getRate()<=0.0)
						{
							data.setMaskStr("");
							data.setRate(0.0);
							CMLib.database().DBUpdateItem("CATALOG_ITEMS",(Item)P);
							Log.sysOut("Catalog",mob.Name()+" modified catalog item "+P.Name());
							mob.tell(_("No drop item."));
							return false;
						}
						final String choice=mob.session().choose(_("Is this for L)ive mobs or D)ead ones (@x1): ",(data.getWhenLive()?"L":"D")),_("LD"), (data.getWhenLive()?"L":"D"));
						data.setWhenLive(choice.equalsIgnoreCase("L"));
						String newMask="?";
						while(newMask.equalsIgnoreCase("?"))
						{
							newMask=mob.session().prompt(_("Enter new MOB selection mask, or NULL (@x1)\n\r: ",data.getMaskStr()),data.getMaskStr());
							if(newMask.equalsIgnoreCase("?"))
								mob.tell(CMLib.masking().maskHelp("\n","disallow"));
						}
						if((newMask.length()==0)||(newMask.equalsIgnoreCase("null")))
						{
							data.setMaskStr("");
							data.setRate(0.0);
							mob.tell(_("Mask removed."));
						}
						else
						{
							data.setMaskStr(newMask);
						}
						CMLib.database().DBUpdateItem("CATALOG_ITEMS",(Item)P);
						Log.sysOut("Catalog",mob.Name()+" modified catalog item "+P.Name());
						mob.tell(_("Item '@x1 has been updated.",P.Name()));
					}
				}
			}
			else
			if((((String)commands.firstElement()).equalsIgnoreCase("SCAN"))
			||(((String)commands.firstElement()).equalsIgnoreCase("DBSCAN")))
			{
				final boolean db=((String)commands.firstElement()).toUpperCase().startsWith("DB");
				commands.removeElementAt(0);
				if(checkUserRoomSetEntry(commands))
				{
					final String which=((String)commands.firstElement()).toLowerCase();
					final Enumeration rooms=getRoomSet(mob,which);
					commands.removeElementAt(0);

					final int whatKind=getObjectType(commands);
					Physical P=null;
					String roomID=null;
					for(;rooms.hasMoreElements();)
					{
						roomID=(String)rooms.nextElement();
						R=CMLib.map().getRoom(roomID);
						if(db) R=CMLib.coffeeMaker().makeNewRoomContent(R,false);
						final Vector<CatalogLibrary.RoomContent> contents=CMLib.catalog().roomContent(R);
						for(final CatalogLibrary.RoomContent content : contents)
						{
							P=content.P();
							if(P instanceof Coins) continue;
							if((P instanceof MOB)&&(whatKind!=2)&&(CMLib.flags().isCataloged(P)))
								if(CMLib.catalog().getCatalogObj(P)!=null)
									mob.tell(_("Check: MOB @x1 in @x2 is cataloged.",P.Name(),roomID));
								else
									mob.tell(_("Error: MOB @x1 in @x2 is falsely cataloged.",P.Name(),roomID));

							if((P instanceof Item)&&(whatKind!=1)&&(CMLib.flags().isCataloged(P)))
								if(CMLib.catalog().getCatalogObj(P)!=null)
									mob.tell(_("Check: Item @x1 in @x2 is cataloged.",P.Name(),roomID));
								else
									mob.tell(_("Error: Item @x1 in @x2 is falsely cataloged.",P.Name(),roomID));
						}
						if(db) R.destroy();
					}
				}
				else
					mob.tell(_("Scan what?"));
			}
			else
			if((((String)commands.firstElement()).equalsIgnoreCase("OVERLOOK"))
			||(((String)commands.firstElement()).equalsIgnoreCase("DBOVERLOOK")))
			{
				final boolean db=((String)commands.firstElement()).toUpperCase().startsWith("DB");
				commands.removeElementAt(0);
				if(checkUserRoomSetEntry(commands))
				{
					final String which=((String)commands.firstElement()).toLowerCase();
					final Enumeration rooms=getRoomSet(mob,which);
					commands.removeElementAt(0);

					final int whatKind=getObjectType(commands);
					Environmental E=null;
					String roomID=null;
					for(;rooms.hasMoreElements();)
					{
						roomID=(String)rooms.nextElement();
						R=CMLib.map().getRoom(roomID);
						if(db) R=CMLib.coffeeMaker().makeNewRoomContent(R,false);
						final Vector<CatalogLibrary.RoomContent> contents=CMLib.catalog().roomContent(R);
						for(final CatalogLibrary.RoomContent content : contents)
						{
							E=content.P();
							if(E instanceof Coins) continue;
							if((E instanceof MOB)&&(whatKind!=2)&&(!CMLib.flags().isCataloged(E)))
								if(CMLib.catalog().getCatalogMob(E.Name())!=null)
									mob.tell(_("MOB @x1 in @x2 should be tied to the catalog.",E.Name(),roomID));
								else
									mob.tell(_("MOB @x1 in @x2 is not cataloged.",E.Name(),roomID));

							if((E instanceof Item)&&(whatKind!=1)&&(!CMLib.flags().isCataloged(E)))
								if(CMLib.catalog().getCatalogItem(E.Name())!=null)
									mob.tell(_("Item @x1 in @x2 should be tied to the catalog.",E.Name(),roomID));
								else
									mob.tell(_("Item @x1 in @x2 is not cataloged.",E.Name(),roomID));
						}
						if(db) R.destroy();
					}
				}
				else
					mob.tell(_("Scan what?"));
			}
			else
			if(((String)commands.firstElement()).equalsIgnoreCase("CLEAN"))
			{
				commands.removeElementAt(0);
				if(checkUserRoomSetEntry(commands))
				{
					final String which=((String)commands.firstElement()).toLowerCase();
					final Enumeration rooms=getRoomSet(mob,which);
					commands.removeElementAt(0);
					final int whatKind=getObjectType(commands);
					final String type=types[whatKind];
					if((mob.session()!=null)
					&&(mob.session().confirm(_("You are about to auto-clean (and auto-save) all @x1 @x2.\n\rThis command, if used improperly, may alter @x3 in this @x4.\n\rAre you absolutely sure (y/N)?",which,type,type,which),_("N"))))
					{
						Physical P=null;
						String roomID=null;
						for(;rooms.hasMoreElements();)
						{
							roomID=(String)rooms.nextElement();
							R=CMLib.coffeeMaker().makeNewRoomContent(CMLib.map().getRoom(roomID),false);
							final Vector<CatalogLibrary.RoomContent> contents=CMLib.catalog().roomContent(R);
							boolean dirty=false;
							for(final CatalogLibrary.RoomContent content : contents)
							{
								P=content.P();
								if(P instanceof Coins) continue;
								if((P instanceof MOB)&&(whatKind!=2))
								{
									if((CMLib.flags().isCataloged(P))
									&&(CMLib.catalog().getCatalogObj(P)==null))
									{
										CMLib.catalog().changeCatalogUsage(P,false);
										content.flagDirty();
										dirty=true;
										mob.tell(_("MOB @x1 in @x2 was cleaned.",P.Name(),roomID));
									}
								}
								else
								if((P instanceof Item)&&(whatKind!=1))
								{
									if((CMLib.flags().isCataloged(P))
									&&(CMLib.catalog().getCatalogObj(P)==null))
									{
										CMLib.catalog().changeCatalogUsage(P,false);
										content.flagDirty();
										dirty=true;
										mob.tell(_("Item @x1 in @x2 was cleaned.",P.Name(),roomID));
									}
								}
							}
							if(dirty)
							{
								CMLib.catalog().updateRoomContent(roomID, contents);
								CMLib.map().resetRoom(CMLib.map().getRoom(roomID), true);
							}
							R.destroy();
						}
					}
				}
				else
					mob.tell(_("Clean what?"));
			}
			else
			{
				Physical P=null;
				final String ID=CMParms.combine(commands,0);
				if(ID.equalsIgnoreCase("SELF")||ID.equalsIgnoreCase("ME"))
					P=mob;
				if(P==null)
					P=R.fetchFromRoomFavorMOBs(null,ID);
				if(P!=null)
				{
					if(!catalog(R,mob,P))
						return false;
					if((P instanceof DBIdentifiable)
					&&(((DBIdentifiable)P).canSaveDatabaseID())
					&&(((DBIdentifiable)P).databaseID().length()>0))
					{
						final Room startRoom=CMLib.map().getStartRoom(P);
						if(startRoom !=null)
						{
							if(P instanceof MOB)
								CMLib.database().DBUpdateMOB(startRoom.roomID(),(MOB)P);
							else
								CMLib.database().DBUpdateItem(startRoom.roomID(),(Item)P);
						}
					}
				}
				else
					mob.tell(_("You don't see '@x1' here!",ID));
			}
		}
		else
			mob.tell(_("Catalog huh? Try CATALOG LIST (MOBS/ITEMS) (MASK), CATALOG [mob/item name], CATALOG DELETE [mob/item name], CATALOG EDIT [item name]."));
		return false;
	}

	@Override public boolean canBeOrdered(){return false;}
	@Override public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CATALOG);}
}
