package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.SecFlag;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.*;
import com.planet_ink.miniweb.interfaces.HTTPRequest;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
public class ListCmd extends StdCommand
{
	public ListCmd(){}

	private final String[] access=_i(new String[]{"LIST"});
	@Override public String[] getAccessWords(){return access;}

	public static class WorldFilter implements Filterer<Area>
	{
		private final TimeClock to;
		public WorldFilter(Room R)
		{
			if((R!=null)&&(R.getArea()!=null))
				to=R.getArea().getTimeObj();
			else
				to=CMLib.time().globalClock();
		}
		@Override
		public boolean passesFilter(Area obj)
		{
			return (obj.getTimeObj()==to);
		}
	}

	public StringBuilder listAllQualifies(Session viewerS, Vector cmds)
	{
		final StringBuilder str=new StringBuilder("");
		final Map<String,Map<String,AbilityMapper.AbilityMapping>> map=CMLib.ableMapper().getAllQualifiesMap(null);
		str.append("<<EACH CLASS>>\n\r");
		Map<String,AbilityMapper.AbilityMapping> subMap=map.get("EACH");
		str.append(CMStrings.padRight(_("Skill ID"), ListingLibrary.ColFixer.fixColWidth(20.0,viewerS)));
		str.append(CMStrings.padRight(_("Lvl"), ListingLibrary.ColFixer.fixColWidth(4.0,viewerS)));
		str.append(CMStrings.padRight(_("Gain"), ListingLibrary.ColFixer.fixColWidth(5.0,viewerS)));
		str.append(CMStrings.padRight(_("Prof"), ListingLibrary.ColFixer.fixColWidth(5.0,viewerS)));
		str.append(CMStrings.padRight(_("Mask"), ListingLibrary.ColFixer.fixColWidth(40.0,viewerS)));
		str.append("\n\r");
		for(final AbilityMapper.AbilityMapping mapped : subMap.values())
		{
			str.append(CMStrings.padRight(mapped.abilityID, ListingLibrary.ColFixer.fixColWidth(20.0,viewerS)));
			str.append(CMStrings.padRight(""+mapped.qualLevel, ListingLibrary.ColFixer.fixColWidth(4.0,viewerS)));
			str.append(CMStrings.padRight(mapped.autoGain?_("yes"):_("no"), ListingLibrary.ColFixer.fixColWidth(5.0,viewerS)));
			str.append(CMStrings.padRight(""+mapped.defaultProficiency, ListingLibrary.ColFixer.fixColWidth(5.0,viewerS)));
			str.append(CMStrings.padRight(mapped.extraMask, ListingLibrary.ColFixer.fixColWidth(40.0,viewerS)));
			str.append("\n\r");
		}
		str.append("\n\r");
		str.append("<<ALL CLASSES>>\n\r");
		subMap=map.get("ALL");
		str.append(CMStrings.padRight(_("Skill ID"), ListingLibrary.ColFixer.fixColWidth(20.0,viewerS)));
		str.append(CMStrings.padRight(_("Lvl"), ListingLibrary.ColFixer.fixColWidth(4.0,viewerS)));
		str.append(CMStrings.padRight(_("Gain"), ListingLibrary.ColFixer.fixColWidth(5.0,viewerS)));
		str.append(CMStrings.padRight(_("Prof"), ListingLibrary.ColFixer.fixColWidth(5.0,viewerS)));
		str.append(CMStrings.padRight(_("Mask"), ListingLibrary.ColFixer.fixColWidth(40.0,viewerS)));
		str.append("\n\r");
		for(final AbilityMapper.AbilityMapping mapped : subMap.values())
		{
			str.append(CMStrings.padRight(mapped.abilityID, ListingLibrary.ColFixer.fixColWidth(20.0,viewerS)));
			str.append(CMStrings.padRight(""+mapped.qualLevel, ListingLibrary.ColFixer.fixColWidth(4.0,viewerS)));
			str.append(CMStrings.padRight(mapped.autoGain?_("yes"):_("no"), ListingLibrary.ColFixer.fixColWidth(5.0,viewerS)));
			str.append(CMStrings.padRight(""+mapped.defaultProficiency, ListingLibrary.ColFixer.fixColWidth(5.0,viewerS)));
			str.append(CMStrings.padRight(mapped.extraMask, ListingLibrary.ColFixer.fixColWidth(40.0,viewerS)));
			str.append("\n\r");
		}
		return str;
	}

	public StringBuilder roomDetails(Session viewerS, Vector these, Room likeRoom)
	{return roomDetails(viewerS,these.elements(),likeRoom);}

	public StringBuilder roomDetails(Session viewerS, Enumeration these, Room likeRoom)
	{
		final StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements()) return lines;
		if(likeRoom==null) return lines;
		Room thisThang=null;
		String thisOne=null;
		final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(31.0,viewerS);
		final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(43.0,viewerS);
		for(final Enumeration r=these;r.hasMoreElements();)
		{
			thisThang=(Room)r.nextElement();
			thisOne=thisThang.roomID();
			if((thisOne.length()>0)&&(thisThang.getArea().Name().equals(likeRoom.getArea().Name())))
				lines.append(CMStrings.padRightPreserve("^<LSTROOMID^>"+thisOne+"^</LSTROOMID^>",COL_LEN1)+": "+CMStrings.limit(thisThang.displayText(),COL_LEN2)+"\n\r");
		}
		lines.append("\n\r");
		return lines;
	}

	public StringBuilder roomExpires(Session viewerS, Enumeration these, Room likeRoom)
	{
		final StringBuilder lines=new StringBuilder("The time is: "+CMLib.time().date2String(System.currentTimeMillis())+"\n\r\n\r");
		if(!these.hasMoreElements()) return lines;
		if(likeRoom==null) return lines;
		Room thisThang=null;
		String thisOne=null;
		for(final Enumeration r=these;r.hasMoreElements();)
		{
			thisThang=(Room)r.nextElement();
			thisOne=thisThang.roomID();
			if((thisOne.length()>0)&&(thisThang.getArea().Name().equals(likeRoom.getArea().Name())))
			{
				String expires=null;
				if(thisThang.expirationDate()==0)
					expires="*";
				else
					expires=CMLib.time().date2String(thisThang.expirationDate());
				lines.append(CMStrings.padRightPreserve("^<LSTROOMID^>"+thisOne+"^</LSTROOMID^>",30)+": "+expires+"\n\r");
			}
		}
		lines.append("\n\r");
		return lines;
	}
	public StringBuilder roomPropertyDetails(Session viewerS, Area A, String rest)
	{
		if(rest.trim().length()==0)
			return roomPropertyDetails(viewerS, A.getMetroMap(), null);
		else
		if(rest.trim().equalsIgnoreCase("area"))
			return roomPropertyDetails(viewerS, A.getMetroMap(), null);
		else
		if(rest.trim().equalsIgnoreCase("world"))
			return roomPropertyDetails(viewerS, CMLib.map().rooms(), null);
		else
		if(rest.trim().toLowerCase().startsWith("area "))
			return roomPropertyDetails(viewerS, A.getMetroMap(), rest.trim().substring(5).trim());
		else
		if(rest.trim().toLowerCase().startsWith("world "))
			return roomPropertyDetails(viewerS, CMLib.map().rooms(), rest.trim().substring(6).trim());
		else
			return new StringBuilder("Illegal parameters... try LIST REALESTATE AREA/WORLD (USERNAME/CLANNAME)");
	}

	public StringBuilder roomPropertyDetails(Session viewerS, Enumeration these, String owner)
	{
		final StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements()) return lines;
		LandTitle t=null;
		Room thisThang=null;
		String thisOne=null;
		for(final Enumeration r=these;r.hasMoreElements();)
		{
			thisThang=(Room)r.nextElement();
			t=CMLib.law().getLandTitle(thisThang);
			if(t!=null)
			{
				thisOne=thisThang.roomID();
				if((thisOne.length()>0)&&((owner==null)||(t.getOwnerName().equalsIgnoreCase(owner))))
					lines.append(CMStrings.padRightPreserve("^<LSTROOMID^>"+thisOne+"^</LSTROOMID^>",30)+": "+CMStrings.limit(thisThang.displayText(),23)+CMStrings.limit(" ("+t.getOwnerName()+", $"+t.getPrice()+")",20)+"\n\r");
			}
		}
		lines.append("\n\r");
		return lines;
	}

	public String cataMark(Environmental E)
	{
		if(E==null) return "";
		if(CMLib.catalog().isCatalogObj(E))
			return "^g";
		return "";
	}

	public boolean canShowTo(MOB showTo, MOB show)
	{
		if((show!=null)
		&&(show.session()!=null)
		&&(showTo!=null)
		&&(((show.phyStats().disposition()&PhyStats.IS_CLOAKED)==0)
			||((CMSecurity.isAllowedAnywhere(showTo,CMSecurity.SecFlag.CLOAK)||CMSecurity.isAllowedAnywhere(showTo,CMSecurity.SecFlag.WIZINV))
				&&(showTo.phyStats().level()>=show.phyStats().level()))))
			return true;
		return false;
	}

	public StringBuffer getStuff(MOB mob, Vector commands, int start, Enumeration<Room> r)
	{
		boolean mobOnly=false;
		boolean itemOnly=false;
		boolean roomOnly=false;
		boolean exitOnly=false;
		boolean zapperMask=false;
		boolean zapperMask2=false;
		MaskingLibrary.CompiledZapperMask compiledZapperMask=null;
		String who="";
		if(commands.size()>start)
			who=commands.get(start).toString().toUpperCase();

		final StringBuffer lines=new StringBuffer("");
		String rest=CMParms.combine(commands,start);
		if((who.equals("ROOM"))
		||(who.equals("ROOMS")))
		{
			roomOnly=true;
			commands.remove(start);
			rest=CMParms.combine(commands,start);
		}
		else
		if(who.equalsIgnoreCase("RESOURCE")||who.equalsIgnoreCase("RESOURCES")||who.equalsIgnoreCase("ENVRESOURCES")||(who.equalsIgnoreCase("TYPE")||who.equalsIgnoreCase("TYPES")))
		{
			return new StringBuffer(roomResources(mob.session(), r, mob.location()).toString());
		}
		else
		if((who.equals("EXIT "))
		||(who.equals("EXITS")))
		{
			exitOnly=true;
			commands.remove(start);
			rest=CMParms.combine(commands,start);
		}
		else
		if((who.equals("ITEM"))
		||(who.equals("ITEMS")))
		{
			itemOnly=true;
			commands.remove(start);
			rest=CMParms.combine(commands,start);
		}
		else
		if((who.equals("MOB"))
		||(who.equals("MOBS")))
		{
			mobOnly=true;
			commands.remove(start);
			rest=CMParms.combine(commands,start);
		}
		else
		if(who.equals("MOBMASK")||who.equals("MOBMASK="))
		{
			mobOnly=true;
			zapperMask=true;
			commands.remove(start);
			lines.append("^xMask used:^?^.^N "+CMLib.masking().maskDesc(who)+"\n\r");
			compiledZapperMask=CMLib.masking().maskCompile(CMParms.combine(commands,start));
			rest="";
		}
		else
		if(who.equals("ITEMMASK")||who.equals("ITEMMASK="))
		{
			itemOnly=true;
			zapperMask=true;
			commands.remove(start);
			lines.append("^xMask used:^?^.^N "+CMLib.masking().maskDesc(who)+"\n\r");
			compiledZapperMask=CMLib.masking().maskCompile(CMParms.combine(commands,start));
			rest="";
		}
		else
		if(who.equals("MOBMASK2")||who.equals("MOBMASK2="))
		{
			mobOnly=true;
			zapperMask2=true;
			commands.remove(start);
			rest=CMParms.combine(commands,start);
			lines.append("^xMask used:^?^.^N "+CMLib.masking().maskDesc(rest)+"\n\r");
		}
		else
		if(who.equals("ITEMMASK2")||who.equals("ITEMMASK2="))
		{
			itemOnly=true;
			zapperMask2=true;
			commands.remove(start);
			rest=CMParms.combine(commands,start);
			lines.append("^xMask used:^?^.^N "+CMLib.masking().maskDesc(rest)+"\n\r");
		}
		Room R = null;

		try
		{
			for(;r.hasMoreElements();)
			{
				R=r.nextElement();
				if((R!=null)&&(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.WHERE))&&(CMLib.flags().canAccess(mob,R.getArea())))
				{
					if((!mobOnly)&&(!itemOnly)&&(!exitOnly))
						if((rest.length()==0)
						||CMLib.english().containsString(R.displayText(),rest)
						||CMLib.english().containsString(R.description(),rest))
						{
							lines.append("^!"+CMStrings.padRight("*",17)+"^?| ");
							lines.append(R.displayText(mob));
							lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
							lines.append("\n\r");
						}
					if(exitOnly)
					{
						for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
						{
							final Exit E=R.getRawExit(d);
							if((E!=null)
							&&((rest.length()==0)
								||((E.Name().length()>0)&&(CMLib.english().containsString(E.Name(),rest)))
								||((E.doorName().length()>0)&& CMLib.english().containsString(E.doorName(),rest))
								||(CMLib.english().containsString(E.viewableText(mob,R).toString(),rest))))
							{
								lines.append("^!"+CMStrings.padRight(Directions.getDirectionName(d),17)+"^N| ");
								lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
								lines.append("\n\r");
							}
						}
					}
					if((!mobOnly)&&(!roomOnly)&&(!exitOnly))
						for(int i=0;i<R.numItems();i++)
						{
							final Item I=R.getItem(i);
							if((zapperMask)&&(itemOnly))
							{
								if(CMLib.masking().maskCheck(compiledZapperMask,I,true))
								{
									lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),17)+"^N| ");
									lines.append(R.displayText(mob));
									lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
									lines.append("\n\r");
								}
							}
							else
							if((zapperMask2)&&(itemOnly))
							{
								if(CMLib.masking().maskCheck(rest,I,true))
								{
									lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),17)+"^N| ");
									lines.append(R.displayText(mob));
									lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
									lines.append("\n\r");
								}
							}
							else
							if((rest.length()==0)
							||(CMLib.english().containsString(I.name(),rest))
							||(CMLib.english().containsString(I.displayText(),rest))
							||(CMLib.english().containsString(I.description(),rest)))
							{
								lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),17)+"^N| ");
								lines.append(R.displayText(mob));
								lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
								lines.append("\n\r");
							}
						}
					for(int m=0;m<R.numInhabitants();m++)
					{
						final MOB M=R.fetchInhabitant(m);
						if((M!=null)&&((M.isMonster())||(canShowTo(mob,M))))
						{
							if((!itemOnly)&&(!roomOnly)&&(!exitOnly))
								if((zapperMask)&&(mobOnly))
								{
									if(CMLib.masking().maskCheck(compiledZapperMask,M,true))
									{
										lines.append("^!"+CMStrings.padRight(cataMark(M)+M.name(mob),17)+"^N| ");
										lines.append(R.displayText(mob));
										lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
										lines.append("\n\r");
									}
								}
								else
								if((zapperMask2)&&(mobOnly))
								{
									if(CMLib.masking().maskCheck(rest,M,true))
									{
										lines.append("^!"+CMStrings.padRight(cataMark(M)+M.name(mob),17)+"^N| ");
										lines.append(R.displayText(mob));
										lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
										lines.append("\n\r");
									}
								}
								else
								if((rest.length()==0)
								||(CMLib.english().containsString(M.name(),rest))
								||(CMLib.english().containsString(M.displayText(),rest))
								||(CMLib.english().containsString(M.description(),rest)))
								{
									lines.append("^!"+CMStrings.padRight(cataMark(M)+M.name(mob),17)+"^N| ");
									lines.append(R.displayText(mob));
									lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
									lines.append("\n\r");
								}
							if((!mobOnly)&&(!roomOnly)&&(!exitOnly))
							{
								for(int i=0;i<M.numItems();i++)
								{
									final Item I=M.getItem(i);
									if((zapperMask)&&(itemOnly))
									{
										if(CMLib.masking().maskCheck(compiledZapperMask,I,true))
										{
											lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),17)+"^N| ");
											lines.append("INV: "+cataMark(M)+M.name(mob)+"^N");
											lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
											lines.append("\n\r");
										}
									}
									else
									if((zapperMask2)&&(itemOnly))
									{
										if(CMLib.masking().maskCheck(rest,I,true))
										{
											lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),17)+"^N| ");
											lines.append("INV: "+cataMark(M)+M.name(mob)+"^N");
											lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
											lines.append("\n\r");
										}
									}
									else
									if((rest.length()==0)
									||(CMLib.english().containsString(I.name(),rest))
									||(CMLib.english().containsString(I.displayText(),rest))
									||(CMLib.english().containsString(I.description(),rest)))
									{
										lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),17)+"^N| ");
										lines.append("INV: "+cataMark(M)+M.name(mob)+"^N");
										lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
										lines.append("\n\r");
									}
								}
								final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
								if(SK!=null)
								for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
								{
									final Environmental E=i.next();
									if((zapperMask)&&(E instanceof Item)&&(itemOnly))
									{
										if(CMLib.masking().maskCheck(compiledZapperMask,E,true))
										{
											lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),17)+"^N| ");
											lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
											lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
											lines.append("\n\r");
										}
									}
									else
									if((zapperMask)&&(E instanceof MOB)&&(mobOnly))
									{
										if(CMLib.masking().maskCheck(compiledZapperMask,E,true))
										{
											lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),17)+"^N| ");
											lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
											lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
											lines.append("\n\r");
										}
									}
									else
									if((zapperMask2)&&(E instanceof Item)&&(itemOnly))
									{
										if(CMLib.masking().maskCheck(rest,E,true))
										{
											lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),17)+"^N| ");
											lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
											lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
											lines.append("\n\r");
										}
									}
									else
									if((zapperMask2)&&(E instanceof MOB)&&(mobOnly))
									{
										if(CMLib.masking().maskCheck(rest,E,true))
										{
											lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),17)+"^N| ");
											lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
											lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
											lines.append("\n\r");
										}
									}
									else
									if((rest.length()==0)
									||(CMLib.english().containsString(E.name(),rest))
									||(CMLib.english().containsString(E.displayText(),rest))
									||(CMLib.english().containsString(E.description(),rest)))
									{
										lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),17)+"^N| ");
										lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
										lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
										lines.append("\n\r");
									}
								}
							}
						}
					}
				}
			}
		}catch(final NoSuchElementException nse){}
		return lines;
	}

	public StringBuilder roomTypes(MOB mob, Enumeration<Room> these, Room likeRoom, Vector commands)
	{
		final StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements()) return lines;
		if(likeRoom==null) return lines;
		if(commands.size()==1)
		{
			Room thisThang=null;
			String thisOne=null;
			for(final Enumeration r=these;r.hasMoreElements();)
			{
				thisThang=(Room)r.nextElement();
				thisOne=thisThang.roomID();
				if((thisOne.length()>0)&&(thisThang.getArea().Name().equals(likeRoom.getArea().Name())))
					lines.append(CMStrings.padRightPreserve(thisOne,30)+": "+thisThang.ID()+"\n\r");
			}
			lines.append("\n\r");
		}
		else
		{
			lines.append(getStuff(mob, commands, 1, these));
		}
		return lines;
	}

	public StringBuilder roomResources(Session viewerS, Vector these, Room likeRoom)
	{
		return roomResources(viewerS, these.elements(),likeRoom);
	}
	
	public StringBuilder roomResources(Session viewerS, Enumeration these, Room likeRoom)
	{
		final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(30.0,viewerS);
		final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(15.0,viewerS);
		final StringBuilder lines=new StringBuilder(CMStrings.padRight(_("Room ID#"),COL_LEN1)+"| "
										   +CMStrings.padRight(_("Room Type"),COL_LEN2)+"| "
										   +"Resource\n\r");
		if(!these.hasMoreElements()) return lines;
		if(likeRoom==null) return lines;
		Room thisThang=null;
		String thisOne=null;
		for(final Enumeration r=these;r.hasMoreElements();)
		{
			thisThang=(Room)r.nextElement();
			thisOne=thisThang.roomID();
			if((thisOne.length()>0)&&(thisThang.getArea().Name().equals(likeRoom.getArea().Name())))
			{
				lines.append(CMStrings.padRight(thisOne,COL_LEN1)+": ");
				lines.append(CMStrings.padRight(thisThang.ID(),COL_LEN2)+": ");
				String thisRsc="-";
				if(thisThang.myResource()>=0)
					thisRsc=RawMaterial.CODES.NAME(thisThang.myResource());
				lines.append(thisRsc+"\n\r");
			}
		}
		lines.append("\n\r");
		return lines;
	}

	public StringBuilder areaConquests(Session viewerS, Enumeration these)
	{
		final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(26.0,viewerS);
		final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(40.0,viewerS);
		final StringBuilder lines=new StringBuilder(CMStrings.padRight(_("Area"),COL_LEN1)+"| "
										   +CMStrings.padRight(_("Clan"),COL_LEN2)+"| "
										   +"Controlled\n\r");
		if(!these.hasMoreElements()) return lines;
		Area thisThang=null;
		String thisOne=null;
		for(final Enumeration r=these;r.hasMoreElements();)
		{
			thisThang=(Area)r.nextElement();
			thisOne=thisThang.name();
			if(thisOne.length()>0)
			{
				lines.append(CMStrings.padRight(thisOne,COL_LEN1)+": ");
				String controller="The Archons";
				String fully="";
				final LegalBehavior law=CMLib.law().getLegalBehavior(thisThang);
				if(law!=null)
				{
					controller=law.rulingOrganization();
					fully=""+((controller.length()>0)&&law.isFullyControlled());
				}
				lines.append(CMStrings.padRight(controller,COL_LEN2)+": ");
				lines.append(fully+"\n\r");
			}
		}
		lines.append("\n\r");
		return lines;
	}

	protected void dumpThreadGroup(Session viewerS, StringBuilder lines,ThreadGroup tGroup, boolean ignoreZeroTickThreads, boolean extend)
	{
		final int ac = tGroup.activeCount();
		final int agc = tGroup.activeGroupCount();
		final Thread tArray[] = new Thread [ac+1];
		final ThreadGroup tgArray[] = new ThreadGroup [agc+1];

		tGroup.enumerate(tArray,false);
		tGroup.enumerate(tgArray,false);

		lines.append(" ^HTGRP^?  ^H" + tGroup.getName() + "^?\n\r");
		for (int i = 0; i<ac; ++i)
		{
			if (tArray[i] != null)
			{
				if((ignoreZeroTickThreads)&&(!tArray[i].isAlive()))
					continue;
				lines.append(tArray[i].isAlive()? "  ok   " : " BAD!  ");
				lines.append(CMStrings.padRight(tArray[i].getName(),20)+": ");
				final String summary;
				if(tArray[i] instanceof MudHost)
					summary=CMClass.classID(tArray[i])+": "+((MudHost)tArray[i]).getStatus();
				else
				{
					final Runnable R=CMLib.threads().findRunnableByThread(tArray[i]);
					if(R instanceof TickableGroup)
						summary=((TickableGroup)R).getName()+": "+((TickableGroup)R).getStatus();
					else
					if(R instanceof Session)
					{
						final Session S=(Session)R;
						final MOB mob=S.mob();
						final String mobName=(mob==null)?"null":mob.Name();
						summary="session "+mobName+": "+S.getStatus().toString()+": "+CMParms.combineWithQuotes(S.getPreviousCMD(),0);
					}
					else
					if(R instanceof CMRunnable)
						summary=CMClass.classID(R)+": active for "+((CMRunnable)R).activeTimeMillis()+"ms";
					else
					if(CMClass.classID(R).length()>0)
						summary=CMClass.classID(R);
					else
					if(extend)
						summary=tArray[i].toString();
					else
						summary="";
				}
				lines.append(summary+"\n\r");
			}
		}

		if (agc > 0)
		{
			lines.append("{\n\r");
			for (int i = 0; i<agc; ++i)
			{
				if (tgArray[i] != null)
					dumpThreadGroup(viewerS,lines,tgArray[i],ignoreZeroTickThreads,extend);
			}
			lines.append("}\n\r");
		}
	}


	public StringBuilder listThreads(Session viewerS, MOB mob, boolean ignoreZeroTickThreads, boolean extend)
	{
		final StringBuilder lines=new StringBuilder("^xStatus|Name                 ^.^?\n\r");
		try
		{
			ThreadGroup topTG = Thread.currentThread().getThreadGroup();
			while (topTG != null && topTG.getParent() != null)
				topTG = topTG.getParent();
			if (topTG != null)
				dumpThreadGroup(viewerS,lines,topTG,ignoreZeroTickThreads, extend);

		}
		catch (final Exception e)
		{
			lines.append ("\n\rBastards! Exception while listing threads: " + e.getMessage() + "\n\r");
		}
		return lines;

	}

	public void addScripts(DVector DV, Room R, ShopKeeper SK, MOB M, Item I, PhysicalAgent E)
	{
		if(E==null) return;
		for(final Enumeration<Behavior> e=E.behaviors();e.hasMoreElements();)
		{
			final Behavior B=e.nextElement();
			if(B instanceof ScriptingEngine)
			{
				final java.util.List<String> files=B.externalFiles();
				if(files != null)
					for(int f=0;f<files.size();f++)
						DV.addElement(files.get(f),E,R,M,I,B);
				final String nonFiles=((ScriptingEngine)B).getVar("*","COFFEEMUD_SYSTEM_INTERNAL_NONFILENAME_SCRIPT");
				if(nonFiles.trim().length()>0)
					DV.addElement("*Custom*"+nonFiles.trim(),E,R,M,I,B);
			}
		}
		for(final Enumeration<ScriptingEngine> e=E.scripts();e.hasMoreElements();)
		{
			final ScriptingEngine SE=e.nextElement();
			final java.util.List<String> files=SE.externalFiles();
			if(files != null)
				for(int f=0;f<files.size();f++)
					DV.addElement(files.get(f),E,R,M,I,SE);
			final String nonFiles=SE.getVar("*","COFFEEMUD_SYSTEM_INTERNAL_NONFILENAME_SCRIPT");
			if(nonFiles.trim().length()>0)
				DV.addElement("*Custom*"+nonFiles.trim(),E,R,M,I,SE);
		}
	}

	public void addShopScripts(DVector DV, Room R, MOB M, Item I, Environmental E)
	{
		if(E==null) return;
		final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(E);
		if(SK!=null)
		{
			for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
			{
				final Environmental E2=i.next();
				if(E2 instanceof PhysicalAgent)
					addScripts(DV,R,SK,M,I,(PhysicalAgent)E2);
			}
		}
	}

	public StringBuilder listScripts(Session viewerS, MOB mob, Vector cmds)
	{
		if(cmds.size()==0) return new StringBuilder("");
		cmds.removeElementAt(0);
		if(cmds.size()==0)
			return new StringBuilder("List what script details? Try LIST SCRIPTS (COUNT/DETAILS/CUSTOM)");
		final String rest=CMParms.combine(cmds,0);
		final DVector scriptTree=new DVector(6);
		Area A=null;
		Room R=null;
		WorldMap.LocatedPair LP=null;
		PhysicalAgent AE=null;
		for(final Enumeration e=CMLib.map().areas();e.hasMoreElements();)
		{
			A=(Area)e.nextElement(); if(A==null) continue;
			for(final Enumeration<WorldMap.LocatedPair> ae=CMLib.map().scriptHosts(A);ae.hasMoreElements();)
			{
				LP=ae.nextElement(); if(LP==null) continue;
				AE=LP.obj(); if(AE==null) continue;
				R=LP.room(); if(R==null) R=CMLib.map().getStartRoom(AE);

				if((AE instanceof Area)||(AE instanceof Exit))
				{
					if(R==null) R=A.getRandomProperRoom();
					addScripts(scriptTree,R,null,null,null,AE);
					addShopScripts(scriptTree,R,null,null,AE);
				}
				else
				if(AE instanceof Room)
				{
					addScripts(scriptTree,R,null,null,null,AE);
					addShopScripts(scriptTree,R,null,null,AE);
				}
				else
				if(AE instanceof MOB)
				{
					addScripts(scriptTree,R,null,(MOB)AE,null,AE);
					addShopScripts(scriptTree,R,(MOB)AE,null,AE);
				}
				else
				if(AE instanceof Item)
				{
					final ItemPossessor IP=((Item)AE).owner();
					if(IP instanceof MOB)
					{
						addScripts(scriptTree,R,null,(MOB)IP,(Item)AE,AE);
						addShopScripts(scriptTree,R,(MOB)IP,(Item)AE,AE);
					}
					else
					{
						addScripts(scriptTree,R,null,null,(Item)AE,AE);
						addShopScripts(scriptTree,R,null,(Item)AE,AE);
					}
				}
			}
		}

		StringBuilder lines=new StringBuilder("");
		if(rest.equalsIgnoreCase("COUNT"))
		{
			final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(50.0,viewerS);
			final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(5.0,viewerS);
			lines=new StringBuilder("^x")
			.append(CMStrings.padRight(_("Script File"),COL_LEN1))
			.append(CMStrings.padRight(_("Usage"),COL_LEN2))
			.append("^.^N\n\r");
			scriptTree.sortBy(1);
			if(scriptTree.size()>0)
			{
				String lastOne=(String)scriptTree.elementAt(0,1);
				if(lastOne.startsWith("*Custom*")) lastOne="*Custom*";
				int counter=1;
				for(int d=1;d<scriptTree.size();d++)
				{
					String scriptFilename=(String)scriptTree.elementAt(d,1);
					if(scriptFilename.startsWith("*Custom*")) scriptFilename="*Custom*";
					if(lastOne.equalsIgnoreCase(scriptFilename))
						counter++;
					else
					{
						lines.append(CMStrings.padRight(lastOne,COL_LEN1));
						lines.append(CMStrings.padRight(""+counter,COL_LEN2));
						lines.append("^.^N\n\r");
						counter=1;
						lastOne=scriptFilename;
					}
				}
				lines.append(CMStrings.padRight(lastOne,COL_LEN1));
				lines.append(CMStrings.padRight(""+counter,COL_LEN2));
				lines.append("\n\r");
			}
		}
		else
		if(rest.equalsIgnoreCase("DETAILS"))
		{
			final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(30.0,viewerS);
			final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(20.0,viewerS);
			final int COL_LEN3=ListingLibrary.ColFixer.fixColWidth(25.0,viewerS);
			lines=new StringBuilder("^x")
			.append(CMStrings.padRight(_("Script File"),COL_LEN1))
			.append(CMStrings.padRight(_("Host"),COL_LEN2))
			.append(CMStrings.padRight(_("Location"),COL_LEN3))
			.append("^.^N\n\r");
			scriptTree.sortBy(1);
			if(scriptTree.size()>0)
			{
				for(int d=0;d<scriptTree.size();d++)
				{
					final String scriptFilename=(String)scriptTree.elementAt(d,1);
					final Environmental host=(Environmental)scriptTree.elementAt(d,2);
					final Room room=(Room)scriptTree.elementAt(d,3);
					lines.append(CMStrings.padRight(scriptFilename,COL_LEN1));
					lines.append(CMStrings.padRight(host.Name(),COL_LEN2));
					lines.append(CMStrings.padRight(CMLib.map().getExtendedRoomID(room),COL_LEN3));
					lines.append("^.^N\n\r");
				}
			}
		}
		else
		if(rest.equalsIgnoreCase("CUSTOM"))
		{
			lines=new StringBuilder("^xCustom Scripts")
									.append("^.^N\n\r");
			scriptTree.sortBy(1);
			if(scriptTree.size()>0)
			{
				for(int d=0;d<scriptTree.size();d++)
				{
					final String scriptFilename=(String)scriptTree.elementAt(d,1);
					if(scriptFilename.startsWith("*Custom*"))
					{
						final Environmental host=(Environmental)scriptTree.elementAt(d,2);
						final Room room=(Room)scriptTree.elementAt(d,3);
						lines.append("^xHost: ^.^N").append(host.Name())
							 .append(", ^xLocation: ^.^N").append(CMLib.map().getExtendedRoomID(room));
						lines.append("^.^N\n\r");
						lines.append(scriptFilename.substring(8));
						lines.append("^.^N\n\r");
					}
				}
			}
		}
		else
			return new StringBuilder("Invalid parameter for LIST SCRIPTS.  Enter LIST SCRIPTS alone for help.");
		return lines;
	}

	public StringBuilder listLinkages(Session viewerS, MOB mob, String rest)
	{
		Faction useFaction=null;
		for(final Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();)
		{
			final Faction F=e.nextElement();
			if(F.showInSpecialReported()) useFaction=F;
		}
		final StringBuilder buf=new StringBuilder("Links: \n\r");
		final List<List<Area>> areaLinkGroups=new Vector<List<Area>>();
		Enumeration<Area> a;
		if(rest.equalsIgnoreCase("world"))
			a=CMLib.map().areas();
		else
			a=new XVector<Area>(mob.location().getArea()).elements();
		for(;a.hasMoreElements();)
		{
			final Area A=a.nextElement();
			buf.append(A.name()+"\t"+A.numberOfProperIDedRooms()+" rooms\t");
			if(!A.getProperMap().hasMoreElements())
			{
				buf.append("\n\r");
				continue;
			}
			final List<List<Room>> linkedGroups=new Vector();
			int numMobs=0;
			int totalAlignment=0;
			int totalLevels=0;
			for(final Enumeration r=A.getCompleteMap();r.hasMoreElements();)
			{
				final Room R=(Room)r.nextElement();
				if(R.roomID().length()>0)
				{
					List<Room> myVec=null;
					List<Room> clearVec=null;
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						final Room R2=R.rawDoors()[d];
						if(R2!=null)
						{
							for(int g=0;g<linkedGroups.size();g++)
							{
								final List<Room> G=linkedGroups.get(g);
								if(G.size()==0)
									clearVec=G;
								else
								if(G.contains(R2))
								{
									if(myVec==null)
									{
										myVec=G;
										myVec.add(R);
									}
									else
									if(myVec!=G)
									{
										for(int g2=0;g2<myVec.size();g2++)
											G.add(myVec.get(g2));
										myVec.clear();
										clearVec=myVec;
										myVec=G;
									}
								}
							}
						}
					}
					if(myVec==null)
					{
						if(clearVec!=null)
							clearVec.add(R);
						else
						{
							clearVec=new Vector();
							clearVec.add(R);
							linkedGroups.add(clearVec);
						}
					}
				}
				for(int g=linkedGroups.size()-1;g>=0;g--)
				{
					if((linkedGroups.get(g)).size()==0)
						linkedGroups.remove(g);
				}

				for(int m=0;m<R.numInhabitants();m++)
				{
					final MOB M=R.fetchInhabitant(m);
					if((M!=null)
					&&(M.isMonster())
					&&(M.getStartRoom()!=null)
					&&(M.getStartRoom().getArea()==R.getArea()))
					{
						numMobs++;
						if((useFaction!=null)
						&&(CMLib.factions().getFaction(useFaction.factionID())!=null)
						&&(M.fetchFaction(useFaction.factionID())!=Integer.MAX_VALUE))
							totalAlignment+=M.fetchFaction(useFaction.factionID());
						totalLevels+=M.phyStats().level();
					}
				}

			}
			final StringBuilder ext=new StringBuilder("links ");
			List<Area> myVec=null;
			List<Area> clearVec=null;
			for(final Enumeration r=A.getCompleteMap();r.hasMoreElements();)
			{
				final Room R=(Room)r.nextElement();
				if(R.roomID().length()>0)
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Room R2=R.rawDoors()[d];
					if((R2!=null)&&(R2.getArea()!=R.getArea()))
					{
						ext.append(Directions.getDirectionName(d)+" to "+R2.getArea().name()+" ("+R.roomID()+"/"+R2.roomID()+") ");
						for(int g=0;g<areaLinkGroups.size();g++)
						{
							final List<Area> G=areaLinkGroups.get(g);
							if(G.size()==0)
								clearVec=G;
							else
							if(G.contains(R2.getArea()))
							{
								if(myVec==null)
								{
									myVec=G;
									myVec.add(R.getArea());
								}
								else
								if(myVec!=G)
								{
									for(int g2=0;g2<myVec.size();g2++)
										G.add(myVec.get(g2));
									myVec.clear();
									clearVec=myVec;
									myVec=G;
								}
							}
						}
					}
				}
			}
			if(myVec==null)
			{
				if(clearVec!=null)
					clearVec.add(A);
				else
				{
					clearVec=new Vector();
					clearVec.add(A);
					areaLinkGroups.add(clearVec);
				}
			}
			if(numMobs>0)
				buf.append(_("@x1 mobs\t@x2 avg levels\t",""+numMobs,""+(totalLevels/numMobs)));
			if((numMobs>0)&&(useFaction!=null)&&(CMLib.factions().getFaction(useFaction.factionID())!=null))
				buf.append((totalAlignment/numMobs)+" avg "+useFaction.name());
			if(linkedGroups.size()>0)
			{
				buf.append("\tgroups: "+linkedGroups.size()+" sizes: ");
				for(final List<Room> grp : linkedGroups)
					buf.append(grp.size()+" ");
			}
			buf.append("\t"+ext.toString()+"\n\r");
		}
		buf.append(_("There were @x1 area groups:",""+areaLinkGroups.size()));
		for(int g=areaLinkGroups.size()-1;g>=0;g--)
		{
			if(areaLinkGroups.get(g).size()==0)
				areaLinkGroups.remove(g);
		}
		final StringBuilder unlinkedGroups=new StringBuilder("");
		for(final List<Area> V : areaLinkGroups)
		{
			buf.append(V.size()+" ");
			if(V.size()<4)
			{
				for(int v=0;v<V.size();v++)
					unlinkedGroups.append(V.get(0).name()+"\t");
				unlinkedGroups.append("|\t");
			}

		}
		buf.append("\n\r");
		buf.append(_("Small Group Areas:\t@x1",unlinkedGroups.toString()));
		Log.sysOut("Lister",buf.toString());
		return buf;
	}


	public StringBuilder journalList(Session viewerS, String partialjournal)
	{
		final StringBuilder buf=new StringBuilder("");
		String journal=null;
		for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			final JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if((CMJ.NAME()+"S").startsWith(partialjournal.toUpperCase().trim()))
				journal=CMJ.NAME().trim();
		}
		if(journal==null) return buf;
		final List<JournalsLibrary.JournalEntry> V=CMLib.database().DBReadJournalMsgs("SYSTEM_"+journal+"S");
		final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(3.0,viewerS);
		final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(10.0,viewerS);
		if(V!=null)
		{
			buf.append("\n\r^x"+CMStrings.padRight("#",COL_LEN1+2)+CMStrings.padRight(_("From"),COL_LEN2)+" Entry^.^N\n\r");
			buf.append("---------------------------------------------\n\r");
			for(int j=0;j<V.size();j++)
			{
				final JournalsLibrary.JournalEntry entry=V.get(j);
				final String from=entry.from;
				final String message=entry.msg;
				buf.append(CMStrings.padRight((j+1)+"",COL_LEN1)+") "+CMStrings.padRight(from,COL_LEN2)+" "+message+"\n\r");
			}
		}
		return buf;
	}

	public StringBuilder listReports(Session viewerS, MOB mob)
	{
		mob.tell(_("\n\r^xCoffeeMud System Report:^.^N"));
		try
		{
			System.gc();
			Thread.sleep(1500);
		}catch(final Exception e){}
		final StringBuilder buf=new StringBuilder("");
		final long totalTime=System.currentTimeMillis()-CMSecurity.getStartTime();
		buf.append(_("The system has been running for ^H@x1^?.\n\r",""+CMLib.english().returnTime(totalTime,0)));
		final long free=Runtime.getRuntime().freeMemory()/1024;
		final long total=Runtime.getRuntime().totalMemory()/1024;
		buf.append(_("The system is utilizing ^H@x1^?kb out of ^H@x2^?kb.\n\r",""+(total-free),""+total));
		buf.append(_("\n\r^xTickables report:^.^N\n\r"));
		final String totalTickers=CMLib.threads().systemReport("totalTickers");
		final String tickGroupSize=CMLib.threads().systemReport("TICKGROUPSIZE");
		final long totalMillis=CMath.s_long(CMLib.threads().systemReport("totalMillis"));
		final long totalTicks=CMath.s_long(CMLib.threads().systemReport("totalTicks"));
		buf.append(_("There are ^H@x1^? ticking objects in ^H@x2^? groups.\n\r",totalTickers,tickGroupSize));
		buf.append(_("The ticking objects have consumed: ^H@x1^?.\n\r",CMLib.english().returnTime(totalMillis,totalTicks)));
		/*
		String topGroupNumber=CMLib.threads().systemReport("topGroupNumber");
		long topGroupMillis=CMath.s_long(CMLib.threads().systemReport("topGroupMillis"));
		long topGroupTicks=CMath.s_long(CMLib.threads().systemReport("topGroupTicks"));
		long topObjectMillis=CMath.s_long(CMLib.threads().systemReport("topObjectMillis"));
		long topObjectTicks=CMath.s_long(CMLib.threads().systemReport("topObjectTicks"));
		buf.append(_("The most active group, #^H@x1^?, has consumed: ^H@x2^?.\n\r",topGroupNumber,CMLib.english().returnTime(topGroupMillis,topGroupTicks)));
		String topObjectClient=CMLib.threads().systemReport("topObjectClient");
		String topObjectGroup=CMLib.threads().systemReport("topObjectGroup");
		if(topObjectClient.length()>0)
		{
			buf.append(_("The most active object has been '^H@x1^?', from group #^H@x2^?.\n\r",topObjectClient,topObjectGroup));
			buf.append(_("That object has consumed: ^H@x1^?.\n\r",CMLib.english().returnTime(topObjectMillis,topObjectTicks)));
		}
		*/
		buf.append("\n\r");
		buf.append(_("^xServices report:^.^N\n\r"));
		buf.append(_("There are ^H@x1^? active out of ^H@x2^? live worker threads.\n\r",CMLib.threads().systemReport("numactivethreads"),CMLib.threads().systemReport("numthreads")));
		int threadNum=0;
		String threadName=CMLib.threads().systemReport("Thread"+threadNum+"name");
		while(threadName.trim().length()>0)
		{
			final long saveThreadMilliTotal=CMath.s_long(CMLib.threads().systemReport("Thread"+threadNum+"MilliTotal"));
			final long saveThreadTickTotal=CMath.s_long(CMLib.threads().systemReport("Thread"+threadNum+"TickTotal"));
			buf.append("Service '"+threadName+"' has consumed: ^H"+CMLib.english().returnTime(saveThreadMilliTotal,saveThreadTickTotal)+" ("+CMLib.threads().systemReport("Thread"+threadNum+"Status")+")^?.");
			buf.append("\n\r");
			threadNum++;
			threadName=CMLib.threads().systemReport("Thread"+threadNum+"name");
		}
		buf.append("\n\r");
		buf.append(_("^xSession report:^.^N\n\r"));
		final long totalMOBMillis=CMath.s_long(CMLib.threads().systemReport("totalMOBMillis"));
		final long totalMOBTicks=CMath.s_long(CMLib.threads().systemReport("totalMOBTicks"));
		buf.append(_("There are ^H@x1^? ticking players logged on.\n\r",""+CMLib.sessions().getCountLocalOnline()));
		buf.append(_("The ticking players have consumed: ^H@x1^?.\n\r",""+CMLib.english().returnTime(totalMOBMillis,totalMOBTicks)));
		/*
		long topMOBMillis=CMath.s_long(CMLib.threads().systemReport("topMOBMillis"));
		long topMOBTicks=CMath.s_long(CMLib.threads().systemReport("topMOBTicks"));
		String topMOBClient=CMLib.threads().systemReport("topMOBClient");
		if(topMOBClient.length()>0)
		{
			buf.append(_("The most active mob has been '^H@x1^?'\n\r",topMOBClient));
			buf.append(_("That mob has consumed: ^H@x1^?.\n\r",CMLib.english().returnTime(topMOBMillis,topMOBTicks)));
		}
		*/
		return buf;
	}

	public void listUsers(Session viewerS, MOB mob, Vector commands)
	{
		if(commands.size()==0) return;
		commands.removeElementAt(0);
		int sortBy=-1;
		if(commands.size()>0)
		{
			final String rest=CMParms.combine(commands,0).toUpperCase();
			sortBy = CMLib.players().getCharThinSortCode(rest,true);
			if(sortBy<0)
			{
				mob.tell(_("Unrecognized sort criteria: @x1",rest));
				return;
			}
		}
		final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(8.0,viewerS);
		final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(10.0,viewerS);
		final int COL_LEN3=ListingLibrary.ColFixer.fixColWidth(4.0,viewerS);
		final int COL_LEN4=ListingLibrary.ColFixer.fixColWidth(5.0,viewerS);
		final int COL_LEN5=ListingLibrary.ColFixer.fixColWidth(23.0,viewerS);
		final int COL_LEN6=ListingLibrary.ColFixer.fixColWidth(18.0,viewerS);
		final int COL_LEN7=ListingLibrary.ColFixer.fixColWidth(15.0,viewerS);
		final StringBuilder head=new StringBuilder("");
		head.append("[");
		head.append(CMStrings.padRight(_("Race"),COL_LEN1)+" ");
		head.append(CMStrings.padRight(_("Class"),COL_LEN2)+" ");
		head.append(CMStrings.padRight(_("Lvl"),COL_LEN3)+" ");
		head.append(CMStrings.padRight(_("Hours"),COL_LEN4)+" ");
		switch(sortBy)
		{
		case 6: head.append(CMStrings.padRight(_("E-Mail"),COL_LEN5)+" "); break;
		case 7: head.append(CMStrings.padRight(_("IP Address"),COL_LEN5)+" "); break;
		default: head.append(CMStrings.padRight(_("Last"),COL_LEN6)+" "); break;
		}

		head.append("] Character name\n\r");
		java.util.List<PlayerLibrary.ThinPlayer> allUsers=CMLib.database().getExtendedUserList();
		final java.util.List<PlayerLibrary.ThinPlayer> oldSet=allUsers;
		final int showBy=sortBy;
		final PlayerLibrary lib=CMLib.players();
		while((oldSet.size()>0)&&(sortBy>=0)&&(sortBy<=7))
		{
			if(oldSet==allUsers) allUsers=new Vector();
			if((sortBy<3)||(sortBy>4))
			{
				PlayerLibrary.ThinPlayer selected=oldSet.get(0);
				for(int u=1;u<oldSet.size();u++)
				{
					final PlayerLibrary.ThinPlayer U=oldSet.get(u);
					if(lib.getThinSortValue(selected,sortBy).compareTo(lib.getThinSortValue(U,sortBy))>0)
					   selected=U;
				}
				if(selected!=null)
				{
					oldSet.remove(selected);
					allUsers.add(selected);
				}
			}
			else
			{
				PlayerLibrary.ThinPlayer selected=oldSet.get(0);
				for(int u=1;u<oldSet.size();u++)
				{
					final PlayerLibrary.ThinPlayer U=oldSet.get(u);
					if(CMath.s_long(lib.getThinSortValue(selected,sortBy))>CMath.s_long(lib.getThinSortValue(U,sortBy)))
					   selected=U;
				}
				if(selected!=null)
				{
					oldSet.remove(selected);
					allUsers.add(selected);
				}
			}
		}

		for(int u=0;u<allUsers.size();u++)
		{
			final PlayerLibrary.ThinPlayer U=allUsers.get(u);

			head.append("[");
			head.append(CMStrings.padRight(U.race,COL_LEN1)+" ");
			head.append(CMStrings.padRight(U.charClass,COL_LEN2)+" ");
			head.append(CMStrings.padRight(""+U.level,COL_LEN3)+" ");
			final long age=Math.round(CMath.div(CMath.s_long(""+U.age),60.0));
			head.append(CMStrings.padRight(""+age,COL_LEN4)+" ");
			switch(showBy)
			{
			case 6: head.append(CMStrings.padRight(U.email,COL_LEN5)+" "); break;
			case 7: head.append(CMStrings.padRight(U.ip,COL_LEN5)+" "); break;
			default: head.append(CMStrings.padRight(CMLib.time().date2String(U.last),COL_LEN6)+" "); break;
			}
			head.append("] "+CMStrings.padRight("^<LSTUSER^>"+U.name+"^</LSTUSER^>",COL_LEN7));
			head.append("\n\r");
		}
		mob.tell(head.toString());
	}

	public void listAccounts(Session viewerS, MOB mob, Vector commands)
	{
		if(commands.size()==0) return;
		commands.removeElementAt(0);
		int sortBy=-1;
		if(commands.size()>0)
		{
			final String rest=CMParms.combine(commands,0).toUpperCase();
			sortBy = CMLib.players().getCharThinSortCode(rest,true);
			if(sortBy<0)
			{
				mob.tell(_("Unrecognized sort criteria: @x1",rest));
				return;
			}
		}
		final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(10.0,viewerS);
		final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(18.0,viewerS);
		final int COL_LEN3=ListingLibrary.ColFixer.fixColWidth(23.0,viewerS);
		final StringBuilder head=new StringBuilder("");
		head.append("^X");
		head.append("[");
		head.append(CMStrings.padRight(_("Account"),COL_LEN1)+" ");
		head.append(CMStrings.padRight(_("Last"),COL_LEN2)+" ");
		switch(sortBy)
		{
			default : head.append(CMStrings.padRight(_("E-Mail"),COL_LEN3)+" "); break;
			case 7: head.append(CMStrings.padRight(_("IP Address"),COL_LEN3)+" "); break;
		}

		head.append("] Characters^.^N\n\r");
		List<PlayerAccount> allAccounts=CMLib.database().DBListAccounts(null);
		final List<PlayerAccount> oldSet=allAccounts;
		final Hashtable<String, PlayerLibrary.ThinPlayer> thinAcctHash=new Hashtable<String, PlayerLibrary.ThinPlayer>();
		for(final PlayerAccount acct : allAccounts)
		{
			final PlayerLibrary.ThinPlayer selectedU=new PlayerLibrary.ThinPlayer();
			selectedU.email=acct.getEmail();
			selectedU.ip=acct.getLastIP();
			selectedU.last=acct.getLastDateTime();
			selectedU.name=acct.getAccountName();
			thinAcctHash.put(acct.getAccountName(), selectedU);
		}
		final int showBy=sortBy;
		final PlayerLibrary lib=CMLib.players();
		while((oldSet.size()>0)&&(sortBy>=0)&&(sortBy<=7))
		{
			if(oldSet==allAccounts)
				allAccounts=new Vector<PlayerAccount>();
			if((sortBy<3)||(sortBy>4))
			{
				PlayerAccount selected = oldSet.get(0);
				if(selected != null)
				{
					PlayerLibrary.ThinPlayer selectedU=thinAcctHash.get(selected.getAccountName());
					for(int u=1;u<oldSet.size();u++)
					{
						final PlayerAccount acct = oldSet.get(u);
						final PlayerLibrary.ThinPlayer U=thinAcctHash.get(acct.getAccountName());
						if(lib.getThinSortValue(selectedU,sortBy).compareTo(lib.getThinSortValue(U,sortBy))>0)
						{
							selected=acct;
							selectedU=U;
						}
					}
					oldSet.remove(selected);
					allAccounts.add(selected);
				}
			}
			else
			{
				PlayerAccount selected = oldSet.get(0);
				if(selected!=null)
				{
					PlayerLibrary.ThinPlayer selectedU=thinAcctHash.get(selected.getAccountName());
					for(int u=1;u<oldSet.size();u++)
					{
						final PlayerAccount acct = oldSet.get(u);
						final PlayerLibrary.ThinPlayer U=thinAcctHash.get(acct.getAccountName());
						if(CMath.s_long(lib.getThinSortValue(selectedU,sortBy))>CMath.s_long(lib.getThinSortValue(U,sortBy)))
						{
							selected=acct;
							selectedU=U;
						}
					}
					oldSet.remove(selected);
					allAccounts.add(selected);
				}
			}
		}

		for(int u=0;u<allAccounts.size();u++)
		{
			final PlayerAccount U=allAccounts.get(u);
			final StringBuilder line=new StringBuilder("");
			line.append("[");
			line.append(CMStrings.padRight(U.getAccountName(),COL_LEN1)+" ");
			line.append(CMStrings.padRight(CMLib.time().date2String(U.getLastDateTime()),COL_LEN2)+" ");
			String players = CMParms.toStringList(U.getPlayers());
			final Vector<String> pListsV = new Vector<String>();
			while(players.length()>0)
			{
				int x=players.length();
				if(players.length()>20)
				{
					x=players.lastIndexOf(',',20);
					if(x<0) x=24;
				}
				pListsV.addElement(players.substring(0,x));
				players=players.substring(x).trim();
				if(players.startsWith(",")) players=players.substring(1).trim();
			}
			switch(showBy)
			{
			default: line.append(CMStrings.padRight(U.getEmail(),COL_LEN3)+" "); break;
			case 7: line.append(CMStrings.padRight(U.getLastIP(),COL_LEN3)+" "); break;
			}
			line.append("] ");
			final int len = line.length();
			head.append(line.toString());
			boolean notYet = true;
			for(final String s : pListsV)
			{
				if(notYet)
					notYet=false;
				else
					head.append(CMStrings.repeat(" ", len));
				head.append(s);
				head.append("\n\r");
			}
			if(pListsV.size()==0)
				head.append("\n\r");
		}
		mob.tell(head.toString());
	}

	public StringBuilder listRaces(Session viewerS, Enumeration these, String rest)
	{
		final List<String> parms=CMParms.parse(rest.toUpperCase());
		final boolean shortList=parms.contains("SHORT");
		if(shortList) parms.remove("SHORT");
		final String restRest=CMParms.combine(parms).trim();
		final StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements()) return lines;
		int column=0;
		final int COL_LEN=ListingLibrary.ColFixer.fixColWidth(25.0,viewerS);
		if(shortList)
		{
			final Vector raceNames=new Vector();
			for(final Enumeration e=these;e.hasMoreElements();)
			{
				final Race R=(Race)e.nextElement();
				if((restRest.length()==0)
				||(CMLib.english().containsString(R.ID(), restRest))
				||(CMLib.english().containsString(R.name(), restRest))
				||(CMLib.english().containsString(R.racialCategory(), restRest)))
					raceNames.addElement(R.ID());
			}
			lines.append(CMParms.toStringList(raceNames));
		}
		else
		for(final Enumeration e=these;e.hasMoreElements();)
		{
			final Race R=(Race)e.nextElement();
			if((restRest.length()==0)
			||(CMLib.english().containsString(R.ID(), restRest))
			||(CMLib.english().containsString(R.name(), restRest))
			||(CMLib.english().containsString(R.racialCategory(), restRest)))
			{
				if(++column>3)
				{
					lines.append("\n\r");
					column=1;
				}
				lines.append(CMStrings.padRight(R.ID()
											+(R.isGeneric()?"*":"")
											+" ("+R.racialCategory()+")",COL_LEN));
			}
		}
		lines.append("\n\r");
		return lines;
	}
	public StringBuilder listCharClasses(Session viewerS, Enumeration these, boolean shortList)
	{
		final StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements()) return lines;
		int column=0;
		final int COL_LEN=ListingLibrary.ColFixer.fixColWidth(25.0,viewerS);
		if(shortList)
		{
			final Vector classNames=new Vector();
			for(final Enumeration e=these;e.hasMoreElements();)
				classNames.addElement(((CharClass)e.nextElement()).ID());
			lines.append(CMParms.toStringList(classNames));
		}
		else
		for(final Enumeration e=these;e.hasMoreElements();)
		{
			final CharClass thisThang=(CharClass)e.nextElement();
			if(++column>2)
			{
				lines.append("\n\r");
				column=1;
			}
			lines.append(CMStrings.padRight(thisThang.ID()
										+(thisThang.isGeneric()?"*":"")
										+" ("+thisThang.baseClass()+")",COL_LEN));
		}
		lines.append("\n\r");
		return lines;
	}
	public StringBuilder listRaceCats(Session viewerS, Enumeration these, boolean shortList)
	{
		final StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements()) return lines;
		int column=0;
		final Vector raceCats=new Vector();
		Race R=null;
		final int COL_LEN=ListingLibrary.ColFixer.fixColWidth(25.0,viewerS);
		for(final Enumeration e=these;e.hasMoreElements();)
		{
			R=(Race)e.nextElement();
			if(!raceCats.contains(R.racialCategory()))
				raceCats.addElement(R.racialCategory());
		}
		final Object[] sortedB=(new TreeSet(raceCats)).toArray();
		if(shortList)
		{
			final String[] sortedC=new String[sortedB.length];
			for(int i=0;i<sortedB.length;i++)
				sortedC[i]=(String)sortedB[i];
			lines.append(CMParms.toStringList(sortedC));
		} else
			for (final Object element : sortedB)
			{
				final String raceCat=(String)element;
				if(++column>3)
				{
					lines.append("\n\r");
					column=1;
				}
				lines.append(CMStrings.padRight(raceCat,COL_LEN));
			}
		lines.append("\n\r");
		return lines;
	}

	public StringBuilder listQuests(Session viewerS)
	{
		final StringBuilder buf=new StringBuilder("");
		if(CMLib.quests().numQuests()==0)
			buf.append(_("No quests loaded."));
		else
		{
			buf.append("\n\r^xQuest Report:^.^N\n\r");
			final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(5.0,viewerS);
			final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(30.0,viewerS);
			buf.append("\n\r^x"+CMStrings.padRight("#",COL_LEN1)+CMStrings.padRight(_("Name"),COL_LEN2)+" Status^.^N\n\r");
			for(int i=0;i<CMLib.quests().numQuests();i++)
			{
				final Quest Q=CMLib.quests().fetchQuest(i);
				if(Q!=null)
				{
					buf.append(CMStrings.padRight(""+(i+1),COL_LEN1)+CMStrings.padRight("^<LSTQUEST^>"+Q.name()+"^</LSTQUEST^>",COL_LEN2)+" ");
					if(Q.running())
					{
						String minsLeft="("+Q.minsRemaining()+" mins left)";
						if(Q.duration()==0)
							minsLeft="(Eternal)";

						if(Q.isCopy())
							buf.append(_("copy running @x1",minsLeft));
						else
							buf.append("running "+minsLeft);
					}
					else
					if(Q.suspended())
						buf.append("disabled");
					else
					if(Q.waiting())
					{
						long min=Q.waitRemaining();
						if(min>0)
						{
							min=min*CMProps.getTickMillis();
							if(min>60000)
								buf.append(_("waiting (@x1 minutes left)",""+(min/60000)));
							else
								buf.append(_("waiting (@x1 seconds left)",""+(min/1000)));
						}
						else
							buf.append(_("waiting (@x1 minutes left)",""+min));
					}
					else
						buf.append("loaded");
					buf.append("^N\n\r");
				}
			}
		}
		return buf;
	}

	public StringBuilder listJournals(Session viewerS)
	{
		final StringBuilder buf=new StringBuilder("");
		final List<String> journals=CMLib.database().DBReadJournals();

		if(journals.size()==0)
			buf.append(_("No journals exits."));
		else
		{
			final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(5.0,viewerS);
			final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(30.0,viewerS);
			buf.append("\n\r^xJournals List:^.^N\n\r");
			buf.append("\n\r^x"+CMStrings.padRight("#",COL_LEN1)+CMStrings.padRight(_("Name"),COL_LEN2)+" Messages^.^N\n\r");
			for(int i=0;i<journals.size();i++)
			{
				final String journal=journals.get(i);
				final int messages=CMLib.database().DBCountJournal(journal,null,null);
				buf.append(CMStrings.padRight(""+(i+1),COL_LEN1)+CMStrings.padRight(journal,COL_LEN2)+" "+messages);
				buf.append("^N\n\r");
			}
		}
		return buf;
	}

	public StringBuilder listTicks(Session viewerS, String whichGroupStr)
	{
		final StringBuilder msg=new StringBuilder("\n\r");
		boolean activeOnly=false;
		String mask=null;
		Set<Pair<Integer,Integer>> whichTicks=null;
		Set<Integer> whichGroups=null;
		final int x=whichGroupStr.lastIndexOf(' ');
		String finalCol="tickercodeword";
		String finalColName="Status";
		if(x>0)
		{
			String lastWord=whichGroupStr.substring(x+1).trim().toLowerCase();
			final String[] validCols={"tickername","tickerid","tickerstatus","tickerstatusstr","tickercodeword","tickertickdown","tickerretickdown","tickermillitotal","tickermilliavg","tickerlaststartmillis","tickerlaststopmillis","tickerlaststartdate","tickerlaststopdate","tickerlastduration","tickersuspended"};
			final int y=CMParms.indexOf(validCols,lastWord);
			if(y>=0)
				finalCol=lastWord;
			else
			for(final String w : validCols)
				if(w.endsWith(lastWord))
				{
					lastWord=w;
					finalCol=lastWord;
				}
			if(!finalCol.equals(lastWord))
				return new StringBuilder("Invalid column: '"+lastWord+"'.  Valid cols are: "+CMParms.toStringList(validCols));
			else
			{
				whichGroupStr=whichGroupStr.substring(0,x).trim();
				finalColName=finalCol;
				if(finalColName.startsWith("ticker"))
					finalColName=finalColName.substring(6);
				if(finalColName.startsWith("milli"))
					finalColName="ms"+finalColName.substring(5);
				finalColName=CMStrings.limit(CMStrings.capitalizeAndLower(finalColName),5);
			}
		}

		if("ACTIVE".startsWith(whichGroupStr.toUpperCase())&&(whichGroupStr.length()>0))
			activeOnly=true;
		else
		if("PROBLEMS".startsWith(whichGroupStr.toUpperCase())&&(whichGroupStr.length()>0))
		{
			whichTicks=new HashSet<Pair<Integer,Integer>>();
			final String problemSets=CMLib.threads().systemReport("tickerProblems");
			final List<String> sets=CMParms.parseSemicolons(problemSets, true);
			for(final String set : sets)
			{
				final List<String> pair=CMParms.parseCommas(set, true);
				if(pair.size()==2)
					whichTicks.add(new Pair<Integer,Integer>(Integer.valueOf(CMath.s_int(pair.get(0))), Integer.valueOf(CMath.s_int(pair.get(1)))));
			}
		}
		else
		if(CMath.isInteger(whichGroupStr)&&(whichGroupStr.length()>0))
		{
			whichGroups=new HashSet<Integer>();
			whichGroups.add(Integer.valueOf(CMath.s_int(whichGroupStr)));
		}
		else
		if(whichGroupStr.length()>0)
		{
			mask=whichGroupStr.toUpperCase().trim();
		}
		final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(4.0,viewerS);
		final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(20.0,viewerS);
		final int COL_LEN3=ListingLibrary.ColFixer.fixColWidth(3.0,viewerS);
		final int COL_LEN4=ListingLibrary.ColFixer.fixColWidth(8.0,viewerS);
		if(!activeOnly)
			msg.append(CMStrings.padRight(_("Grp"),COL_LEN1)+CMStrings.padRight(_("Client"),COL_LEN2)+" "+CMStrings.padRight(_("ID"),COL_LEN3)+CMStrings.padRight(finalColName,COL_LEN4));
		msg.append(CMStrings.padRight(_("Grp"),COL_LEN1)+CMStrings.padRight(_("Client"),COL_LEN2)+" "+CMStrings.padRight(_("ID"),COL_LEN3)+CMStrings.padRight(finalColName,COL_LEN4)+"\n\r");
		int col=0;
		final int numGroups=CMath.s_int(CMLib.threads().tickInfo("tickGroupSize"));
		if((mask!=null)&&(mask.length()==0)) mask=null;
		String chunk=null;
		for(int group=0;group<numGroups;group++)
		{
			if((whichGroups==null)||(whichGroups.contains(Integer.valueOf(group))))
			{
				final int tickersSize=CMath.s_int(CMLib.threads().tickInfo("tickersSize"+group));
				for(int tick=0;tick<tickersSize;tick++)
				{
					if((whichTicks==null)||(whichTicks.contains(new Pair<Integer,Integer>(Integer.valueOf(group), Integer.valueOf(tick)))))
					{
						final long tickerlaststartdate=CMath.s_long(CMLib.threads().tickInfo("tickerlaststartmillis"+group+"-"+tick));
						final long tickerlaststopdate=CMath.s_long(CMLib.threads().tickInfo("tickerlaststopmillis"+group+"-"+tick));
						final boolean isActive=(tickerlaststopdate<tickerlaststartdate);
						if((!activeOnly)||(isActive))
						{
							final String name=CMLib.threads().tickInfo("tickerName"+group+"-"+tick);
							if((mask==null)||(name.toUpperCase().indexOf(mask)>=0))
							{
								final String id=CMLib.threads().tickInfo("tickerID"+group+"-"+tick);
								final String status=CMLib.threads().tickInfo(finalCol+group+"-"+tick);
								final boolean suspended=CMath.s_bool(CMLib.threads().tickInfo("tickerSuspended"+group+"-"+tick));
								if(((col++)>=2)||(activeOnly))
								{
									msg.append("\n\r");
									col=1;
								}
								chunk=CMStrings.padRight(""+group,COL_LEN1)
								   +CMStrings.padRight(name,COL_LEN2)
								   +" "+CMStrings.padRight(id+"",COL_LEN3)
								   +CMStrings.padRight((activeOnly?(status+(suspended?"*":"")):status+(suspended?"*":"")),COL_LEN4);
								msg.append(chunk);
							}
						}
					}
				}
			}
		}
		return msg;
	}

	public StringBuilder listSubOps(Session viewerS)
	{
		final StringBuilder msg=new StringBuilder("");
		final int COL_LEN=ListingLibrary.ColFixer.fixColWidth(25.0,viewerS);
		for(final Enumeration a=CMLib.map().areas();a.hasMoreElements();)
		{
			final Area A=(Area)a.nextElement();
			msg.append(CMStrings.padRight(A.Name(),COL_LEN)+": ");
			if(A.getSubOpList().length()==0)
				msg.append(_("No Area staff defined.\n\r"));
			else
				msg.append(A.getSubOpList()+"\n\r");
		}
		return msg;
	}

	protected String reallyFindOneWays(Session viewerS, Vector commands)
	{
		final StringBuilder str=new StringBuilder("");
		try
		{
			for(final Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				final Room R=(Room)r.nextElement();
				if(R.roomID().length()>0)
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						final Room R2=R.rawDoors()[d];
						if((R2!=null)&&(R2.rawDoors()[Directions.getOpDirectionCode(d)]!=R))
							str.append(_("@x1: @x2 to @x3\n\r",CMStrings.padRight(R.roomID(),30),Directions.getDirectionName(d),R2.roomID()));
					}
			}
		}catch(final NoSuchElementException e){}
		if(str.length()==0) str.append(_("None!"));
		if(CMParms.combine(commands,1).equalsIgnoreCase("log"))
			Log.rawSysOut(str.toString());
		return str.toString();
	}


	protected String unlinkedExits(Session viewerS, Vector commands)
	{
		final StringBuilder str=new StringBuilder("");
		try
		{
			for(final Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				final Room R=(Room)r.nextElement();
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Room R2=R.rawDoors()[d];
					final Exit E2=R.getRawExit(d);
					if((R2==null)&&(E2!=null))
						str.append(_("@x1: @x2 to @x3 (@x4)\n\r",CMStrings.padRight(R.roomID(),30),Directions.getDirectionName(d),E2.temporaryDoorLink(),E2.displayText()));
				}
			}
		}catch(final NoSuchElementException e){}
		if(str.length()==0) str.append(_("None!"));
		if(CMParms.combine(commands,1).equalsIgnoreCase("log"))
			Log.rawSysOut(str.toString());
		return str.toString();
	}

	public String listResources(MOB mob, String parm)
	{
		final Iterator<String> keyIter=Resources.findResourceKeys(parm);
		if(!keyIter.hasNext())
			return "";
		final String key = keyIter.next();
		if(!keyIter.hasNext())
		{
			final StringBuilder str=new StringBuilder("^x"+key+"^?\n\r");
			final Object o=Resources.getResource(key);
			if(o instanceof List) str.append(CMParms.toStringList((List)o));
			else
			if(o instanceof Map) str.append(CMParms.toStringList((Map)o));
			else
			if(o instanceof Set) str.append(CMParms.toStringList((Set)o));
			else
			if(o instanceof String[]) str.append(CMParms.toStringList((String[])o));
			else
			if(o instanceof boolean[]) str.append(CMParms.toStringList((boolean[])o));
			if(o instanceof byte[]) str.append(CMParms.toStringList((byte[])o));
			else
			if(o instanceof char[]) str.append(CMParms.toStringList((char[])o));
			else
			if(o instanceof double[]) str.append(CMParms.toStringList((double[])o));
			else
			if(o instanceof int[]) str.append(CMParms.toStringList((int[])o));
			else
			if(o instanceof long[]) str.append(CMParms.toStringList((long[])o));
			else
			if(o!=null)
				str.append(o.toString());
			return str.toString();
		}
		final Enumeration<String> keys=new IteratorEnumeration<String>(Resources.findResourceKeys(parm));
		return CMLib.lister().reallyList2Cols(mob,keys).toString();
	}

	public String listHelpFileRequests(MOB mob, String rest)
	{
		final String fileName=Log.instance().getLogFilename(Log.Type.help);
		if(fileName==null)
			return "This feature requires that help request log entries be directed to a file.";
		final CMFile f=new CMFile(fileName,mob,CMFile.FLAG_LOGERRORS);
		if((!f.exists())||(!f.canRead()))
			return "File '"+f.getName()+"' does not exist.";
		final List<String> V=Resources.getFileLineVector(f.text());
		final Hashtable entries = new Hashtable();
		for(int v=0;v<V.size();v++)
		{
			final String s=V.get(v);
			if(s.indexOf(" help  Help")>0)
			{
				final int x=s.indexOf("wanted help on",19);
				final String helpEntry=s.substring(x+14).trim().toLowerCase();
				int[] sightings=(int[])entries.get(helpEntry);
				if(sightings==null)
				{
					sightings=new int[1];
					if(CMLib.help().getHelpText(helpEntry,mob,false)!=null)
						sightings[0]=-1;
					entries.put(helpEntry,sightings);
				}
				if(sightings[0]>=0)
					sightings[0]++;
				else
					sightings[0]--;
			}
		}
		final Hashtable readyEntries = new Hashtable(entries.size());
		for(final Enumeration e=entries.keys();e.hasMoreElements();)
		{
			final Object key=e.nextElement();
			final int[] val=(int[])entries.get(key);
			readyEntries.put(key,Integer.valueOf(val[0]));
		}
		final DVector sightingsDV=DVector.toDVector(readyEntries);
		sightingsDV.sortBy(2);
		final StringBuilder str=new StringBuilder("^HHelp entries, sorted by popularity: ^N\n\r");
		for(int d=0;d<sightingsDV.size();d++)
			str.append("^w"+CMStrings.padRight(sightingsDV.elementAt(d,2).toString(),4))
			   .append(" ")
			   .append(sightingsDV.elementAt(d,1).toString())
			   .append("\n\r");
		return str.toString()+"^N";
	}

	public String listRecipes(MOB mob, String rest)
	{
		final StringBuilder str = new StringBuilder("");
		if(rest.trim().length()==0)
		{
			str.append(_("Common Skills with editable recipes: "));
			for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
			{
				final Ability A=e.nextElement();
				if(A instanceof ItemCraftor)
				{
					final ItemCraftor iA = (ItemCraftor)A;
					if((iA.parametersFormat()==null)
					||(iA.parametersFormat().length()==0)
					||(iA.parametersFile()==null)
					||(iA.parametersFile().length()==0))
						continue;
					str.append(A.ID()).append(", ");
				}
			}
			if(str.toString().endsWith(", "))
				str.delete(str.length()-2,str.length());
		}
		else
		{
			final Ability A=CMClass.findAbility(rest,Ability.ACODE_COMMON_SKILL,-1,false);
			if(A==null)
				str.append(_("Ability '@x1' does not exist -- try list recipes",rest));
			else
			if(!(A instanceof ItemCraftor))
				str.append(_("Ability '@x1' is not a proper ability -- try list recipes",A.ID()));
			else
			{
				final ItemCraftor iA = (ItemCraftor)A;
				if((iA.parametersFormat()==null)
				||(iA.parametersFormat().length()==0)
				||(iA.parametersFile()==null)
				||(iA.parametersFile().length()==0))
					str.append(_("Ability '@x1' is not editable -- try list recipes",A.ID()));
				else
					str.append(CMLib.ableParms().getRecipeList(iA));
			}
		}
		return str.toString();
	}
	public String listMaterials()
	{
		return CMParms.toStringList(RawMaterial.Material.values());
	}
	
	private enum SpaceFilterCode {SPACE, BODIES, MOONS, STARS, SHIPS}
	
	public String getSpaceObjectType(final SpaceObject obj)
	{
		final String type;
		if((obj instanceof Physical) && (!(obj instanceof SpaceShip)) && CMLib.flags().isLightSource(((Physical)obj)))
			type="Star";
		else
		if(obj instanceof SpaceShip)
			type="Ship";
		else
		if((obj instanceof Area) && (!(obj instanceof SpaceShip)) && (obj.radius() > (SpaceObject.Distance.MoonRadius.dm+10000L)))
			type="Planet";
		else
		if((obj instanceof Area) && (!(obj instanceof SpaceShip)) && (obj.radius() <= (SpaceObject.Distance.MoonRadius.dm+10000L)))
			type="Moon";
		else
			type="Obj.";
		return type;
	}
	
	private String shortenNumber(long number, int len)
	{
		String s=""+number;
		char c='d';
		while(s.length()>len)
		{
			number=Math.round(number/1000);
			switch(c)
			{
			case 'd': c='k'; break;
			case 'k': c='m'; break;
			case 'm': c='b'; break;
			case 'b': c='t'; break;
			case 't': c='q'; break;
			}
			s=""+number+Character.toString(c);
		}
		return s;
	}
	
	public String listSpace(MOB mob, Vector commands)
	{
		final Session viewerS=mob.session();
		if(viewerS==null)
			return "";
		StringBuilder str=new StringBuilder("");
		String listWhat=commands.get(0).toString().toUpperCase().trim();
		Filterer<SpaceObject> filter=null;
		for(final SpaceFilterCode code : SpaceFilterCode.values())
			if(code.toString().toUpperCase().startsWith(listWhat))
			{
				filter=new Filterer<SpaceObject>(){
					@Override public boolean passesFilter(SpaceObject obj)
					{
						switch(code)
						{
						case SPACE: return true;
						case BODIES: return (obj instanceof Area) && (!(obj instanceof SpaceShip)) && (obj.radius() > (SpaceObject.Distance.MoonRadius.dm+10000L));
						case SHIPS: return (obj instanceof SpaceShip);
						case STARS: return (obj instanceof Physical) && (!(obj instanceof SpaceShip)) && CMLib.flags().isOnFire((Physical)obj);
						case MOONS: return (obj instanceof Area) && (!(obj instanceof SpaceShip)) && (obj.radius() <= (SpaceObject.Distance.MoonRadius.dm+10000L));
						}
						return false;
					}
				};
				break;
			}
		
		if((commands.size()<=1)
		||(filter==null)
		||commands.get(1).toString().equals("?")
		||commands.get(1).toString().equals(_("help")))
		{
			str.append(_("List what in space? Try one of the following:\n\r"));
			str.append(_("LIST SPACE ALL - List everything in space everywhere!!\n\r"));
			str.append(_("LIST SPACE WITHIN [DISTANCE] - List within distance of current planet.\n\r"));
			str.append(_("LIST SPACE AROUND [X],[Y],[Z] - List all within 1 solar system of coords.\n\r"));
			str.append(_("LIST SPACE AROUND [NAME] - List all within 1 solar system of named object.\n\r"));
			str.append(_("LIST SPACE AROUND [NAME] WITHIN [DISTANCE] - List all within [distance] of named object.\n\r"));
			str.append(_("\n\r[DISTANCE] can be in DM (decameters), KM (kilometers), AU (astro units), or SU (solar system units.\n\r"));
			str.append(_("Instead of LIST SPACE you can also specify BODIES, MOONS, STARS, or SPACESHIPS.\n\r"));
			return str.toString();
		}
		final List<SpaceObject> objs=new ArrayList<SpaceObject>();
		for(Enumeration<SpaceObject> objEnum=CMLib.map().getSpaceObjects();objEnum.hasMoreElements();)
		{
			final SpaceObject obj=objEnum.nextElement();
			if(!filter.passesFilter(obj))
				continue;
			objs.add(obj);
		}
		final String[] keywords=CMLib.lang().sessionTranslation(new String[]{"ALL","WITHIN","DISTANCE"});
		final String[] sortcols=CMLib.lang().sessionTranslation(new String[]{"TYPE","RADIUS","COORDINATES","SPEED","MASS","NAME","COORDSX","COORDSY","COORDSZ"});
		Long withinDistance=null;
		long[] centerPoint=null;
		final SpaceObject SO=CMLib.map().getSpaceObject(mob, false);
		for(int i=1;i<commands.size();i++)
		{
			String s=((String)commands.get(i)).toUpperCase();
			if(_("ALL").startsWith(s))
				continue;
			else
			if(_("AROUND").startsWith(s))
			{
				if(i<commands.size()-1)
				{
					i++;
					int end=i;
					while((end<commands.size()-1)&&(!CMStrings.contains(keywords, commands.get(end).toString().toUpperCase())))
						end++;
					if(end==i)
					{
						return _("\n\rBad AROUND parm: '@x1' -- no coordinates or object specified.\n\r","");
					}
					else
					{
						String around=CMParms.combine(commands,i,end);
						List<String> listStr=CMParms.parseCommas(around,true);
						long[] coords=null;
						if(listStr.size()==3)
						{
							long[] valL=new long[3];
							for(int x=0;x<3;x++)
							{
								Long newValue=CMLib.english().parseSpaceDistance(listStr.get(x));
								if(newValue==null)
									break;
								else
								{
									valL[i]=newValue.longValue();
									if(i==2) coords=valL;
								}
							}
						}
						if(coords==null)
						{
							SpaceObject SO2=CMLib.map().findSpaceObject(around, true);
							if(SO2==null)
								SO2=CMLib.map().findSpaceObject(around, true);
							if(SO2!=null)
								coords=SO2.coordinates();
						}
						if(coords==null)
						{
							return _("\n\rBad AROUND parm: '@x1' -- bad coordinates or object specified.\n\r",around);
						}
						centerPoint=coords;
						i=end-1;
					}
				}
				else
				{
					return _("\n\rBad AROUND parm: '@x1' -- no coordinates or object specified.\n\r","");
				}
			}
			else
			if(_("WITHIN").startsWith(s))
			{
				if(i<commands.size()-1)
				{
					i++;
					int end=i;
					while((end<commands.size()-1)&&(!CMStrings.contains(keywords, commands.get(end).toString().toUpperCase())))
						end++;
					if(end==i)
					{
						return _("\n\rBad WITHIN parm: '@x1' -- no valid distance specified.\n\r","");
					}
					else
					{
						String within=CMParms.combine(commands,i,end);
						Long distance=CMLib.english().parseSpaceDistance(within);
						if(distance==null)
						{
							return _("\n\rBad WITHIN parm: '@x1' -- no valid distance specified.\n\r",within);
						}
						withinDistance=distance;
						i=end-1;
					}
				}
				else
				{
					return _("\n\rBad WITHIN parm: '@x1' -- no distance specified.\n\r","");
				}
			}
			else
			if(_("ORDERBY").startsWith(s))
			{
				if(i<commands.size()-1)
				{
					i++;
					s=(String)commands.get(i);
					int end=i;
					while((end<commands.size()-1)&&(CMStrings.contains(sortcols, commands.get(end).toString().toUpperCase())))
						end++;
					if(end==i)
					{
						return _("\n\rBad ORDERBY parm: '@x1' ORDERBY -- no column specified.  Try @x2.\n\r",commands.get(i).toString(),CMParms.toStringList(sortcols));
					}
					for(int x=end-1;x>=i;x--)
					{
						final int[][] b=new int[][]{{0,1,2}};
						int dex=CMParms.indexOf(sortcols, commands.get(x).toString().toUpperCase());
						switch(dex)
						{
						case 0: Collections.sort(objs, new Comparator<SpaceObject>(){
							@Override public int compare(SpaceObject o1, SpaceObject o2)
							{
								return getSpaceObjectType(o1).compareTo(getSpaceObjectType(o2));
							}});
							break;
						case 1: Collections.sort(objs, new Comparator<SpaceObject>(){
							@Override public int compare(SpaceObject o1, SpaceObject o2)
							{
								return Long.valueOf(o1==null?0:o1.radius()).compareTo(Long.valueOf(o2==null?0:o2.radius()));
							}});
							break;
						case 3: Collections.sort(objs, new Comparator<SpaceObject>(){
							@Override public int compare(SpaceObject o1, SpaceObject o2)
							{
								return Long.valueOf(o1==null?0:o1.speed()).compareTo(Long.valueOf(o2==null?0:o2.speed()));
							}});
							break;
						case 4: Collections.sort(objs, new Comparator<SpaceObject>(){
							@Override public int compare(SpaceObject o1, SpaceObject o2)
							{
								return Long.valueOf(o1==null?0:o1.getMass()).compareTo(Long.valueOf(o2==null?0:o2.getMass()));
							}});
							break;
						case 5: Collections.sort(objs, new Comparator<SpaceObject>(){
							@Override public int compare(SpaceObject o1, SpaceObject o2)
							{
								return (o1==null?"":o1.name()).compareToIgnoreCase(o2==null?"":o2.name());
							}});
							break;
						case 8: b[0]=new int[]{2,0,1}; 
							//$FALL-THROUGH$
						case 7: if(x==7) b[0]=new int[]{1,2,0}; 
							//$FALL-THROUGH$
						case 6: 
							//$FALL-THROUGH$
						case 2: Collections.sort(objs, new Comparator<SpaceObject>(){
							@Override public int compare(SpaceObject o1, SpaceObject o2)
							{
								int i=Long.valueOf(o1==null?Long.MIN_VALUE:o1.coordinates()[b[0][0]]).compareTo(Long.valueOf(o2==null?Long.MIN_VALUE:o2.coordinates()[b[0][0]]));
								if(i!=0)
									i=Long.valueOf(o1==null?Long.MIN_VALUE:o1.coordinates()[b[0][1]]).compareTo(Long.valueOf(o2==null?Long.MIN_VALUE:o2.coordinates()[b[0][1]]));
								if(i!=0)
									i=Long.valueOf(o1==null?Long.MIN_VALUE:o1.coordinates()[b[0][2]]).compareTo(Long.valueOf(o2==null?Long.MIN_VALUE:o2.coordinates()[b[0][2]]));
								return i;
							}});
							break;
						}
					}
					i=end-1;
				}
				else
				{
					return _("\n\rBad ORDERBY parm: '@x1' ORDERBY -- no column specified.  Try @x2.\n\r","",CMParms.toStringList(sortcols));
				}
			}
		}
		
		if((centerPoint!=null)||(withinDistance!=null))
		{
			if(centerPoint==null)
			{
				if(SO!=null)
					centerPoint=SO.coordinates();
				else
					centerPoint=new long[]{0,0,0};
			}
			if(withinDistance==null)
				withinDistance=Long.valueOf(SpaceObject.Distance.SolarSystemRadius.dm+1000000);
			final List<SpaceObject> objs2=CMLib.map().getSpaceObjectsWithin(centerPoint, 0, withinDistance.longValue());
			for(final Iterator<SpaceObject> i=objs.iterator();i.hasNext();)
			{
				final SpaceObject obj=i.next();
				if(!objs2.contains(obj))
					i.remove();
			}
		}
		
		final int COL_LEN1, COL_LEN2, COL_LEN3, COL_LEN4, COL_LEN5;
		str.append(CMStrings.padRight(_("Typ"),COL_LEN1=ListingLibrary.ColFixer.fixColWidth(3.0,viewerS))+" ");
		str.append(CMStrings.padRight(_("Radius"),COL_LEN2=ListingLibrary.ColFixer.fixColWidth(7.0,viewerS))+" ");
		str.append(CMStrings.padRight(_("Coordinates"),COL_LEN3=ListingLibrary.ColFixer.fixColWidth(25.0,viewerS))+" ");
		str.append(CMStrings.padRight(_("Speed"),COL_LEN4=ListingLibrary.ColFixer.fixColWidth(10.0,viewerS))+" ");
		str.append(CMStrings.padRight(_("Mass"),COL_LEN5=ListingLibrary.ColFixer.fixColWidth(7.0,viewerS))+" ");
		str.append(_("Name\n\r"));
		for(SpaceObject obj : objs)
		{
			str.append(CMStrings.padRight(getSpaceObjectType(obj),COL_LEN1)+" ");
			str.append(CMStrings.padRight(CMLib.english().sizeDescShort(obj.radius()),COL_LEN2)+" ");
			str.append(CMStrings.padRight(CMLib.english().coordDescShort(obj.coordinates()),COL_LEN3)+" ");
			str.append(CMStrings.padRight(CMLib.english().speedDescShort(obj.speed()),COL_LEN4)+" ");
			str.append(CMStrings.padRight(shortenNumber(obj.getMass(),COL_LEN5),COL_LEN5)+" ");
			str.append(obj.name()+"\n\r");
		}
		return str.toString();
	}

	public String listExpired(MOB mob)
	{
		final StringBuilder buf=new StringBuilder("");
		if(CMProps.getBoolVar(CMProps.Bool.ACCOUNTEXPIRATION))
		{
			final String theWord=(CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>1)?"account":"character";
			final List<String> l=CMLib.login().getExpiredList();
			if(l.size()>0)
			{
				buf.append(_("\n\rThere are currently @x1 expired @x2s.\n\r",""+l.size(),theWord));
				buf.append(CMLib.lister().reallyList2Cols(mob,new IteratorEnumeration<String>(l.iterator())).toString());
				buf.append(_("\n\r\n\rUse EXPIRE command to alter them.^?^.\n\r"));
			}
			else
				buf.append(_("\n\rThere are no expired @x1s at this time.\n\r",theWord));
		}
		else
			buf.append(_("\n\rAccount expiration system is not enabled on this mud.\n\r"));
		return buf.toString();
	}

	public String listEnvResources(Session viewerS, String rest)
	{
		final List<String> parms=CMParms.parse(rest.toUpperCase());
		final boolean shortList=parms.contains("SHORT");
		if(shortList)
			return CMParms.toStringList(RawMaterial.CODES.NAMES());
		final StringBuilder str=new StringBuilder("");
		//for(String S : RawMaterial.CODES.NAMES())
		//	str.append(CMStrings.padRight(CMStrings.capitalizeAndLower(S.toLowerCase()),16));
		final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(15.0,viewerS);
		final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(10.0,viewerS);
		final int COL_LEN3=ListingLibrary.ColFixer.fixColWidth(3.0,viewerS);
		final int COL_LEN4=ListingLibrary.ColFixer.fixColWidth(4.0,viewerS);
		final int COL_LEN5=ListingLibrary.ColFixer.fixColWidth(3.0,viewerS);
		final int COL_LEN6=ListingLibrary.ColFixer.fixColWidth(36.0,viewerS);
		final int COL_LEN7=COL_LEN1+1+COL_LEN2+1+COL_LEN3+1+COL_LEN4+1+COL_LEN5+1;
		str.append(CMStrings.padRight(_("Resource"),COL_LEN1)+" ");
		str.append(CMStrings.padRight(_("Material"),COL_LEN2)+" ");
		str.append(CMStrings.padRight(_("Val"),COL_LEN3)+" ");
		str.append(CMStrings.padRight(_("Freq"),COL_LEN4)+" ");
		str.append(CMStrings.padRight(_("Str"),COL_LEN5)+" ");
		str.append(_("Locales\n\r"));
		for(final int i : RawMaterial.CODES.ALL())
		{
			final String resourceName=CMStrings.capitalizeAndLower(RawMaterial.CODES.NAME(i).toLowerCase());
			final String materialName=RawMaterial.Material.findByMask(i&RawMaterial.MATERIAL_MASK).noun().toLowerCase();
			if((rest.length()==0)
			||(resourceName.indexOf(rest)>=0)
			||(materialName.indexOf(rest)>=0))
			{
				str.append(CMStrings.padRight(resourceName,COL_LEN1+1));
				str.append(CMStrings.padRight(materialName,COL_LEN2+1));
				str.append(CMStrings.padRight(""+RawMaterial.CODES.VALUE(i),COL_LEN3+1));
				str.append(CMStrings.padRight(""+RawMaterial.CODES.FREQUENCY(i),COL_LEN4+1));
				str.append(CMStrings.padRight(""+RawMaterial.CODES.HARDNESS(i),COL_LEN5+1));
				StringBuilder locales=new StringBuilder("");
				for(final Enumeration e=CMClass.locales();e.hasMoreElements();)
				{
					final Room R=(Room)e.nextElement();
					if(!(R instanceof GridLocale))
						if((R.resourceChoices()!=null)&&(R.resourceChoices().contains(Integer.valueOf(i))))
							locales.append(R.ID()+" ");
				}
				while(locales.length()>COL_LEN6)
				{
					str.append(locales.toString().substring(0,COL_LEN6)+"\n\r"+CMStrings.padRight(" ",COL_LEN7));
					locales=new StringBuilder(locales.toString().substring(COL_LEN6));
				}
				str.append(locales.toString());
				str.append("\n\r");
			}
		}
		return str.toString();
	}

	public List<String> getMyCmdWords(MOB mob)
	{
		final Vector<String> V=new Vector<String>();
		for (final ListCmdEntry cmd : ListCmdEntry.values())
		{
			if((CMSecurity.isAllowedContainsAny(mob, cmd.flags))
			||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
				V.addElement(cmd.cmd[0]);
		}
		for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			final JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if((CMSecurity.isJournalAccessAllowed(mob,CMJ.NAME()))
			||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
				V.addElement(CMJ.NAME()+"S");
		}
		return V;
	}

	public ListCmdEntry getMyCmd(MOB mob, String s)
	{
		s=s.toUpperCase().trim();
		for(final ListCmdEntry cmd : ListCmdEntry.values())
		{
			for(int i2=0;i2<cmd.cmd.length;i2++)
			{
				if(cmd.cmd[i2].startsWith(s))
				{
					if((CMSecurity.isAllowedContainsAny(mob, cmd.flags))
					||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
					{
						return cmd;
					}
				}
			}
		}
		for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			final JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if(((CMJ.NAME()+"S").startsWith(s)||CMJ.NAME().equals(s)||CMJ.NAME().replace('_', ' ').equals(s))
			&&((CMSecurity.isJournalAccessAllowed(mob,CMJ.NAME()))
				||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN)))
					return ListCmdEntry.COMMANDJOURNAL;
		}
		return null;
	}

	public ListCmdEntry getAnyCmd(MOB mob)
	{
		for(final ListCmdEntry cmd : ListCmdEntry.values())
		{
			if((CMSecurity.isAllowedContainsAny(mob, cmd.flags))
			||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
			{
				return cmd;
			}
		}
		for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			final JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if((CMSecurity.isJournalAccessAllowed(mob,CMJ.NAME()))
			||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
				return ListCmdEntry.COMMANDJOURNAL;
		}
		return null;
	}

	public String listComponents(Session viewerS)
	{
		final StringBuilder buf=new StringBuilder("^xAll Defined Spells and required components: ^N\n\r");
		for(final String ID : CMLib.ableMapper().getAbilityComponentMap().keySet())
		{
			final List<AbilityComponent> DV=CMLib.ableMapper().getAbilityComponentMap().get(ID);
			if(DV!=null)
				buf.append(CMStrings.padRight(ID,20)+": "+CMLib.ableMapper().getAbilityComponentDesc(null,ID)+"\n\r");
		}
		if(buf.length()==0) return "None defined.";
		return buf.toString();
	}

	public String listExpertises(Session viewerS)
	{
		final StringBuilder buf=new StringBuilder("^xAll Defined Expertise Codes: ^N\n\r");
		final int COL_LEN=ListingLibrary.ColFixer.fixColWidth(20.0,viewerS);
		for(final Enumeration e=CMLib.expertises().definitions();e.hasMoreElements();)
		{
			final ExpertiseLibrary.ExpertiseDefinition def=(ExpertiseLibrary.ExpertiseDefinition)e.nextElement();
			buf.append(CMStrings.padRight("^Z"+def.ID,COL_LEN)+"^?: "+CMStrings.padRight(def.name,COL_LEN)+": "+CMLib.masking().maskDesc(def.allRequirements())+"\n\r");
		}
		if(buf.length()==0) return "None defined.";
		return buf.toString();
	}

	public String listTitles(Session viewerS)
	{
		final StringBuilder buf=new StringBuilder("^xAll Defined Auto-Titles: ^N\n\r");
		for(final Enumeration e=CMLib.titles().autoTitles();e.hasMoreElements();)
		{
			final String title=(String)e.nextElement();
			final String maskDesc=CMLib.masking().maskDesc(CMLib.titles().getAutoTitleMask(title));
			buf.append(CMStrings.padRight(title,30)+": "+maskDesc+"\n\r");
		}
		if(buf.length()==0) return "None defined.";
		return buf.toString();
	}

	public String listClanGovernments(Session viewerS, List commands)
	{
		final StringBuilder buf=new StringBuilder("^xAll Clan Governments: ^N\n\r");
		int glen=0;
		for(final ClanGovernment G : CMLib.clans().getStockGovernments())
			if(G.getName().length()>glen)
				glen=G.getName().length();
		final int SCREEN_LEN=ListingLibrary.ColFixer.fixColWidth(78.0,viewerS);
		for(final ClanGovernment G : CMLib.clans().getStockGovernments())
			buf.append(CMStrings.padRight(G.getName(),glen)+": "+CMStrings.limit(G.getShortDesc(),SCREEN_LEN-glen-2)+"\n\r");
		return buf.toString();
	}

	public String listClans(Session viewerS, List commands)
	{
		final StringBuilder buf=new StringBuilder("^xAll Clans: ^N\n\r");
		int clen=0;
		for(final Enumeration<Clan> c=CMLib.clans().clans();c.hasMoreElements();)
		{
			final Clan C=c.nextElement();
			if(C.clanID().length()>clen)
				clen=C.clanID().length();
		}
		final int SCREEN_LEN=ListingLibrary.ColFixer.fixColWidth(78.0,viewerS);
		for(final Enumeration<Clan> c=CMLib.clans().clans();c.hasMoreElements();)
		{
			final Clan C=c.nextElement();
			buf.append(CMStrings.padRight(C.clanID(),clen)+": "+CMStrings.limit(C.getMemberList().size()+" members",SCREEN_LEN-clen-2)+"\n\r");
		}
		return buf.toString();
	}

	public StringBuilder listContent(MOB mob, Vector commands)
	{
		commands.removeElementAt(0);
		Enumeration roomsToDo=null;
		final String rest=CMParms.combine(commands,0);
		if(rest.equalsIgnoreCase("area"))
			roomsToDo=mob.location().getArea().getMetroMap();
		else
		if(rest.trim().length()==0)
			roomsToDo=new XVector(mob.location()).elements();
		else
		{
			final Area A=CMLib.map().findArea(rest);
			if(A!=null)
				roomsToDo=A.getMetroMap();
			else
			{
				final Room R=CMLib.map().getRoom(rest);
				if(R!=null)
					roomsToDo=new XVector(mob.location()).elements();
				else
					return new StringBuilder("There's no such place as '"+rest+"'");
			}
		}
		final StringBuilder buf=new StringBuilder("");
		Room R=null;
		Room TR=null;
		Map<String,Room> set=null;
		final int SCREEN_LEN1=ListingLibrary.ColFixer.fixColWidth(15.0,mob);
		final int SCREEN_LEN2=ListingLibrary.ColFixer.fixColWidth(35.0,mob);
		final int SCREEN_LEN3=ListingLibrary.ColFixer.fixColWidth(3.0,mob);
		for(;roomsToDo.hasMoreElements();)
		{
			R=(Room)roomsToDo.nextElement();
			if(R.roomID().length()==0) continue;
			set=CMLib.database().DBReadRoomData(CMLib.map().getExtendedRoomID(R),false);
			if((set==null)||(set.size()==0))
				buf.append(_("'@x1' could not be read from the database!\n\r",CMLib.map().getExtendedRoomID(R)));
			else
			{
				TR=set.entrySet().iterator().next().getValue();
				CMLib.database().DBReadContent(TR.roomID(),TR,false);
				buf.append("\n\r^NRoomID: "+CMLib.map().getExtendedRoomID(TR)+"\n\r");
				for(int m=0;m<TR.numInhabitants();m++)
				{
					final MOB M=TR.fetchInhabitant(m);
					if(M==null) continue;
					buf.append("^M"+CMStrings.padRight(M.ID(),SCREEN_LEN1)+": "+CMStrings.padRight(M.displayText(),SCREEN_LEN2)+": "
								+CMStrings.padRight(M.phyStats().level()+"",SCREEN_LEN3)+": "
								+CMLib.flags().getAlignmentName(M)
								+"^N\n\r");
					for(int i=0;i<M.numItems();i++)
					{
						final Item I=M.getItem(i);
						if(I!=null)
							buf.append("    ^I"+CMStrings.padRight(I.ID(),SCREEN_LEN1)
									+": "+CMStrings.padRight((I.displayText().length()>0?I.displayText():I.Name()),SCREEN_LEN2)+": "
									+CMStrings.padRight(I.phyStats().level()+"",SCREEN_LEN3)+": "
									+"^N"+((I.container()!=null)?I.Name():"")+"\n\r");
					}
				}
				for(int i=0;i<TR.numItems();i++)
				{
					final Item I=TR.getItem(i);
					if(I!=null)
						buf.append("^I"+CMStrings.padRight(I.ID(),SCREEN_LEN1)+": "
								+CMStrings.padRight((I.displayText().length()>0?I.displayText():I.Name()),SCREEN_LEN2)+": "
								+CMStrings.padRight(I.phyStats().level()+"",SCREEN_LEN3)+": "
								+"^N"+((I.container()!=null)?I.Name():"")+"\n\r");
				}
				TR.destroy();
			}
		}
		return buf;
	}

	public void listPolls(MOB mob, Vector commands)
	{
		final Iterator<Poll> i=CMLib.polls().getPollList();
		if(!i.hasNext())
			mob.tell(_("\n\rNo polls available.  Fix that by entering CREATE POLL!"));
		else
		{
			final StringBuilder str=new StringBuilder("");
			int v=1;
			for(;i.hasNext();v++)
			{
				final Poll P=i.next();
				str.append(CMStrings.padRight(""+v,2)+": "+P.getName());
				if(!CMath.bset(P.getFlags(),Poll.FLAG_ACTIVE))
					str.append(_(" (inactive)"));
				else
				if(P.getExpiration()>0)
					str.append(_(" (expires: @x1)",CMLib.time().date2String(P.getExpiration())));
				str.append("\n\r");
			}
			mob.tell(str.toString());
		}
	}

	public void listLog(MOB mob, Vector commands)
	{
		final int pageBreak=((mob.playerStats()!=null)?mob.playerStats().getPageBreak():0);
		int lineNum=0;
		if(commands.size()<2)
		{
			final Log.LogReader log=Log.instance().getLogReader();
			String line=log.nextLine();
			while((line!=null)&&(mob.session()!=null)&&(!mob.session().isStopped()))
			{
				mob.session().rawPrintln(line);
				if((pageBreak>0)&&(lineNum>=pageBreak))
					if(!pause(mob.session()))
						break;
					else
						lineNum=0;
				lineNum++;
				line=log.nextLine();
			}
			log.close();
			return;
		}

		int start=0;
		final int logSize=Log.instance().numLines();
		int end=logSize;
		final Log.LogReader log=Log.instance().getLogReader();

		for(int i=1;i<commands.size();i++)
		{
			String s=(String)commands.elementAt(i);
			if((s.equalsIgnoreCase("front")||(s.equalsIgnoreCase("first"))||(s.equalsIgnoreCase("head")))
			&&(i<(commands.size()-1)))
			{
				s=(String)commands.elementAt(i+1);
				if(CMath.isInteger(s))
				{
					i++;
					end=CMath.s_int(s);
				}
				else
				{
					mob.tell(_("Bad @x1 parameter format after.",s));
					return;
				}
			}
			else
			if((s.equalsIgnoreCase("back")||(s.equalsIgnoreCase("last"))||(s.equalsIgnoreCase("tail")))
			&&(i<(commands.size()-1)))
			{
				s=(String)commands.elementAt(i+1);
				if(CMath.isInteger(s))
				{
					i++;
					start=(end-CMath.s_int(s))-1;
				}
				else
				{
					mob.tell(_("Bad @x1 parameter format after.",s));
					return;
				}
			}
			else
			if(s.equalsIgnoreCase("skip")
			&&(i<(commands.size()-1)))
			{
				s=(String)commands.elementAt(i+1);
				if(CMath.isInteger(s))
				{
					i++;
					start=start+CMath.s_int(s);
				}
				else
				{
					mob.tell(_("Bad @x1 parameter format after.",s));
					return;
				}
			}
		}
		if(end>=logSize) end=logSize;
		if(start<0) start=0;
		String line=log.nextLine();
		lineNum=0;
		int shownLineNum=0;
		while((line!=null)&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			if((lineNum>start)&&(lineNum<=end))
			{
				mob.session().rawPrintln(line);
				if((pageBreak>0)&&(shownLineNum>=pageBreak))
					if(!pause(mob.session()))
						break;
					else
						shownLineNum=0;
				shownLineNum++;
			}
			lineNum++;
			line=log.nextLine();
		}
		log.close();
	}

	public enum ListCmdEntry
	{
		UNLINKEDEXITS("UNLINKEDEXITS",new SecFlag[]{SecFlag.CMDEXITS,SecFlag.CMDROOMS,SecFlag.CMDAREAS}),
		ITEMS("ITEMS",new SecFlag[]{SecFlag.CMDITEMS}),
		ARMOR("ARMOR",new SecFlag[]{SecFlag.CMDITEMS}),
		ENVRESOURCES("ENVRESOURCES",new SecFlag[]{SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS}),
		WEAPONS("WEAPONS",new SecFlag[]{SecFlag.CMDITEMS}),
		MOBS("MOBS",new SecFlag[]{SecFlag.CMDMOBS}),
		ROOMS("ROOMS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		AREA("AREA",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		LOCALES("LOCALES",new SecFlag[]{SecFlag.CMDROOMS}),
		BEHAVIORS("BEHAVIORS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		EXITS("EXITS",new SecFlag[]{SecFlag.CMDEXITS}),
		RACES("RACES",new SecFlag[]{SecFlag.CMDRACES,SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS}),
		CLASSES("CLASSES",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDCLASSES}),
		STAFF("STAFF",new SecFlag[]{SecFlag.CMDAREAS}),
		SPELLS("SPELLS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		SONGS("SONGS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		PRAYERS("PRAYERS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		PROPERTIES("PROPERTIES",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		THIEFSKILLS("THIEFSKILLS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		COMMON("COMMON",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		JOURNALS("JOURNALS",new SecFlag[]{SecFlag.JOURNALS}),
		SKILLS("SKILLS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		QUESTS("QUESTS",new SecFlag[]{SecFlag.CMDQUESTS}),
		DISEASES("DISEASES",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		POISONS("POISONS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		TICKS("TICKS",new SecFlag[]{SecFlag.LISTADMIN}),
		MAGIC("MAGIC",new SecFlag[]{SecFlag.CMDITEMS}),
		TECH("TECH",new SecFlag[]{SecFlag.CMDITEMS}),
		CLANITEMS("CLANITEMS",new SecFlag[]{SecFlag.CMDITEMS,SecFlag.CMDCLANS}),
		COMMANDJOURNAL("COMMANDJOURNAL",new SecFlag[]{}), // blank, but used!
		REALESTATE("REALESTATE",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		NOPURGE("NOPURGE",new SecFlag[]{SecFlag.NOPURGE}),
		BANNED("BANNED",new SecFlag[]{SecFlag.BAN}),
		RACECATS("RACECATS",new SecFlag[]{SecFlag.CMDRACES,SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS}),
		LOG("LOG",new SecFlag[]{SecFlag.LISTADMIN}),
		USERS("USERS",new SecFlag[]{SecFlag.CMDPLAYERS,SecFlag.STAT}),
		LINKAGES("LINKAGES",new SecFlag[]{SecFlag.CMDAREAS}),
		REPORTS("REPORTS",new SecFlag[]{SecFlag.LISTADMIN}),
		THREADS("THREADS",new SecFlag[]{SecFlag.LISTADMIN}),
		RESOURCES("RESOURCES",new SecFlag[]{SecFlag.LOADUNLOAD}),
		ONEWAYDOORS("ONEWAYDOORS",new SecFlag[]{SecFlag.CMDEXITS,SecFlag.CMDROOMS,SecFlag.CMDAREAS}),
		CHANTS("CHANTS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		SUPERPOWERS(new String[]{"SUPERPOWERS","POWERS"},new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		COMPONENTS("COMPONENTS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.COMPONENTS}),
		EXPERTISES("EXPERTISES",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.EXPERTISES}),
		FACTIONS("FACTIONS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDFACTIONS}),
		MATERIALS("MATERIALS",new SecFlag[]{SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS}),
		OBJCOUNTERS("OBJCOUNTERS",new SecFlag[]{SecFlag.LISTADMIN}),
		POLLS("POLLS",new SecFlag[]{SecFlag.POLLS,SecFlag.LISTADMIN}),
		CONTENTS("CONTENTS",new SecFlag[]{SecFlag.CMDROOMS,SecFlag.CMDITEMS,SecFlag.CMDMOBS,SecFlag.CMDAREAS}),
		EXPIRES("EXPIRES",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		TITLES("TITLES",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.TITLES}),
		AREARESOURCES("AREARESOURCES",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		CONQUERED("CONQUERED",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		HOLIDAYS("HOLIDAYS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		RECIPES("RECIPES",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDRECIPES}),
		HELPFILEREQUESTS("HELPFILEREQUESTS",new SecFlag[]{SecFlag.LISTADMIN}),
		SCRIPTS("SCRIPTS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		ACCOUNTS("ACCOUNTS",new SecFlag[]{SecFlag.CMDPLAYERS,SecFlag.STAT}),
		GOVERNMENTS("GOVERNMENTS",new SecFlag[]{SecFlag.CMDCLANS}),
		CLANS("CLANS",new SecFlag[]{SecFlag.CMDCLANS}),
		DEBUGFLAG("DEBUGFLAG",new SecFlag[]{SecFlag.LISTADMIN}),
		DISABLEFLAG("DISABLEFLAG",new SecFlag[]{SecFlag.LISTADMIN}),
		ALLQUALIFYS("ALLQUALIFYS",new SecFlag[]{SecFlag.CMDABILITIES,SecFlag.LISTADMIN}),
		NEWS("NEWS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.JOURNALS,SecFlag.NEWS}),
		AREAS("AREAS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS,SecFlag.CMDROOMS}),
		SESSIONS("SESSIONS",new SecFlag[]{SecFlag.SESSIONS}),
		WORLD("WORLD",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS,SecFlag.CMDROOMS}),
		PLANETS("PLANETS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS,SecFlag.CMDROOMS}),
		SPACE(new String[]{"SPACE","BODIES","MOONS","STARS","SPACESHIPS"},new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS,SecFlag.CMDROOMS}),
		SPACESHIPAREAS("SPACESHIPAREAS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS,SecFlag.CMDROOMS}),
		CURRENTS("CURRENTS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS,SecFlag.CMDROOMS,SecFlag.CMDMOBS}),
		MANUFACTURERS("MANUFACTURERS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDITEMS}),
		TECHSKILLS("TECHSKILLS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		SOFTWARE("SOFTWARE",new SecFlag[]{SecFlag.CMDITEMS}),
		EXPIRED("EXPIRED",new SecFlag[]{SecFlag.CMDPLAYERS}),
		SQL("SQL",new SecFlag[]{SecFlag.CMDDATABASE})
		;
		public String[]			   cmd;
		public CMSecurity.SecGroup flags;
		private ListCmdEntry(String cmd, SecFlag[] flags)
		{
			this.cmd=new String[]{cmd};
			this.flags=new CMSecurity.SecGroup(flags);
		}
		private ListCmdEntry(String[] cmd, SecFlag[] flags)
		{
			this.cmd=cmd;
			this.flags=new CMSecurity.SecGroup(flags);
		}
	}

	public boolean pause(Session sess)
	{
		if((sess==null)||(sess.isStopped())) return false;
		sess.rawCharsOut("<pause - enter>".toCharArray());
		try
		{
			String s=sess.blockingIn(10 * 60 * 1000);
			if(s!=null)
			{
				s=s.toLowerCase();
				if(s.startsWith("qu")||s.startsWith("ex")||s.equals("x"))
					return false;
			}
		}catch(final Exception e){return false;}
		return !sess.isStopped();
	}

	public void listNews(MOB mob, Vector commands)
	{
		final String theRest=CMParms.combine(commands,1);
		final Item I=CMClass.getItem("StdJournal");
		I.setName(_("SYSTEM_NEWS"));
		I.setDescription(_("Enter `LIST NEWS [NUMBER]` to read an entry.%0D%0AEnter CREATE NEWS to add new entries. "));
		final CMMsg newMsg=CMClass.getMsg(mob,I,null,CMMsg.MSG_READ|CMMsg.MASK_ALWAYS,null,CMMsg.MSG_READ|CMMsg.MASK_ALWAYS,theRest,CMMsg.MSG_READ|CMMsg.MASK_ALWAYS,null);
		if(mob.location().okMessage(mob,newMsg)&&(I.okMessage(mob, newMsg)))
		{
			mob.location().send(mob,newMsg);
			I.executeMsg(mob,newMsg);
		}
	}

	public void listSql(MOB mob, String rest)
	{
		mob.tell(_("SQL Query: @x1",rest));
		try
		{
			final List<String[]> rows=CMLib.database().DBRawQuery(rest.replace('`','\''));
			final StringBuilder report=new StringBuilder("");
			for(final String[] row : rows)
				report.append(CMParms.toStringList(row)).append("\n\r");
			if(mob.session()==null) return;
			mob.session().rawPrint(report.toString());
		}
		catch(final Exception e)
		{
			mob.tell(_("SQL Query Error: @x1",e.getMessage()));
		}
	}

	private enum ListAreaStats
	{
		NAME("Name",30), AUTHOR("Auth",15), DESCRIPTION("Desc",50), ROOMS("Rooms",6), STATE("State",10), HIDDEN("Hiddn",6);
		public String shortName;
		public Integer len;
		private ListAreaStats(String shortName, int len)
		{
			this.shortName=shortName;
			this.len=Integer.valueOf(len);
		}
		public Comparable getFromArea(Area A)
		{
			switch(this)
			{
			case NAME: return A.Name();
			case HIDDEN: return ""+CMLib.flags().isHidden(A);
			case ROOMS: return Integer.valueOf(A.getProperRoomnumbers().roomCountAllAreas());
			case STATE: return A.getAreaState().name();
			case AUTHOR: return A.getAuthorID();
			case DESCRIPTION: return A.description().replace('\n', ' ').replace('\r', ' ');
			default: return "";
			}
		}
	}

	public Comparable getAreaStatFromSomewhere(Area A, String stat)
	{
		if(A==null)
			return null;
		stat=stat.toUpperCase().trim();
		final ListAreaStats ls=(ListAreaStats)CMath.s_valueOf(ListAreaStats.class, stat);
		final Area.Stats as=(Area.Stats)CMath.s_valueOf(Area.Stats.class, stat);
		if(ls != null)
			return ls.getFromArea(A);
		else
		if(as!=null)
			return Integer.valueOf(A.getAreaIStats()[as.ordinal()]);
		else
			return null;
	}

	public void listManufacturers(MOB mob, Vector commands)
	{
		final StringBuffer str=new StringBuffer("");
		str.append(CMStrings.padRight(_("Name"), 20)).append(" ");
		str.append(CMStrings.padRight(_("Tech"), 5)).append(" ");
		str.append(CMStrings.padRight(_("Eff."), 4)).append(" ");
		str.append(CMStrings.padRight(_("Rel."), 4)).append(" ");
		str.append("!");
		str.append(CMStrings.padRight(_("Name"), 20)).append(" ");
		str.append(CMStrings.padRight(_("Tech"), 5)).append(" ");
		str.append(CMStrings.padRight(_("Eff."), 4)).append(" ");
		str.append(CMStrings.padRight(_("Rel."), 4));
		str.append("\n\r");
		str.append(CMStrings.repeat("-", 75)).append("\n\r");
		final List<Manufacturer> l=new XVector<Manufacturer>(CMLib.tech().manufacterers());
		Collections.sort(l,new Comparator<Manufacturer>()
		{
			@Override public int compare(Manufacturer o1, Manufacturer o2)
			{
				return o1.name().compareToIgnoreCase(o2.name());
			}
		});
		for(final Iterator<Manufacturer> i =l.iterator();i.hasNext();)
		{
			Manufacturer M=i.next();
			str.append(CMStrings.padRight(M.name(), 20)).append(" ");
			str.append(CMStrings.padRight(M.getMinTechLevelDiff()+"-"+M.getMaxTechLevelDiff(), 5)).append(" ");
			str.append(CMStrings.padRight(Math.round(M.getEfficiencyPct()*100.0)+"%", 4)).append(" ");
			str.append(CMStrings.padRight(Math.round(M.getReliabilityPct()*100.0)+"%", 4)).append(" ");
			if(i.hasNext())
			{
				M=i.next();
				str.append("!");
				str.append(CMStrings.padRight(M.name(), 20)).append(" ");
				str.append(CMStrings.padRight(M.getMinTechLevelDiff()+"-"+M.getMaxTechLevelDiff(), 5)).append(" ");
				str.append(CMStrings.padRight(Math.round(M.getEfficiencyPct()*100.0)+"%", 4)).append(" ");
				str.append(CMStrings.padRight(Math.round(M.getReliabilityPct()*100.0)+"%", 4));
			}
			str.append("\n\r");
		}
		str.append("\n\r");
		if(mob.session()!=null)
			mob.session().rawPrint(str.toString());
	}

	public void listCurrents(MOB mob, Vector commands)
	{
		final StringBuffer str=new StringBuffer("");
		for(final String key : CMLib.tech().getMakeRegisteredKeys())
		{
			str.append(_("Registered key: @x1 : @x2\n\r",key,(CMLib.tech().isCurrentActive(key)?"Activated":"Suspended")));
			str.append(CMStrings.padRight(_("Name"), 30)).append(" ");
			str.append(CMStrings.padRight(_("Room"), 30)).append(" ");
			str.append("\n\r");
			str.append(CMStrings.repeat("-", 75)).append("\n\r");
			for(final Electronics e : CMLib.tech().getMakeRegisteredElectronics(key))
			{
				str.append(CMStrings.padRight(e.Name(), 30)).append(" ");
				str.append(CMStrings.padRight(CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(e)), 30)).append(" ");
				str.append("\n\r");
			}
			str.append("\n\r");
		}
		if(str.length()==0)
			str.append(_("No electronics found.\n\r"));
		if(mob.session()!=null)
			mob.session().rawPrint(str.toString());
	}

	public void listAreas(MOB mob, Vector commands, Filterer<Area> filter)
	{
		if(mob==null) return;
		commands.remove(0);
		List<String> sortBys=null;
		List<String> colNames=null;
		if(commands.size()>0)
		{
			List<String> addTos=null;
			while(commands.size()>0)
			{
				if(commands.get(0).toString().equalsIgnoreCase("sortby"))
				{
					commands.remove(0);
					sortBys=new Vector<String>();
					addTos=sortBys;
				}
				else
				if(commands.get(0).toString().equalsIgnoreCase("cols")||commands.get(0).toString().equalsIgnoreCase("columns"))
				{
					commands.remove(0);
					colNames=new Vector<String>();
					addTos=colNames;
				}
				else
				if(addTos!=null)
				{
					final String stat=commands.get(0).toString().toUpperCase().trim();
					final ListAreaStats ls=(ListAreaStats)CMath.s_valueOf(ListAreaStats.class, stat);
					final Area.Stats as=(Area.Stats)CMath.s_valueOf(Area.Stats.class, stat);
					if((ls==null)&&(as==null))
					{
						mob.tell(_("'@x1' is not recognized.  Try one of these: @x2, @x3",stat,CMParms.toStringList(ListAreaStats.values()),CMParms.toStringList(Area.Stats.values())));
						return;
					}
					addTos.add(stat);
					commands.remove(0);
				}
				else
				{
					mob.tell(_("'@x1' is not recognized.  Try 'columns' or 'sortby' followed by one or more of these: @x2, @x3",commands.get(0).toString(),CMParms.toStringList(ListAreaStats.values()),CMParms.toStringList(Area.Stats.values())));
					return;
				}
			}
		}
		final Vector<Triad<String,String,Integer>> columns=new Vector<Triad<String,String,Integer>>();
		if((colNames!=null)&&(colNames.size()>0))
		{
			for(final String newCol : colNames)
			{
				final ListAreaStats ls=(ListAreaStats)CMath.s_valueOf(ListAreaStats.class, newCol);
				final Area.Stats as=(Area.Stats)CMath.s_valueOf(Area.Stats.class, newCol);
				if(ls!=null)
					columns.add(new Triad<String,String,Integer>(ls.shortName,ls.name(),ls.len));
				else
				if(as!=null)
					columns.add(new Triad<String,String,Integer>(CMStrings.scrunchWord(CMStrings.capitalizeAndLower(newCol), 6),as.name(),Integer.valueOf(6)));
			}
		}
		else
		{
			//AREASTAT_DESCS
			columns.add(new Triad<String,String,Integer>(ListAreaStats.NAME.shortName,ListAreaStats.NAME.name(),ListAreaStats.NAME.len));
			columns.add(new Triad<String,String,Integer>(ListAreaStats.HIDDEN.shortName,ListAreaStats.HIDDEN.name(),ListAreaStats.HIDDEN.len));
			columns.add(new Triad<String,String,Integer>(ListAreaStats.ROOMS.shortName,ListAreaStats.ROOMS.name(),ListAreaStats.ROOMS.len));
			columns.add(new Triad<String,String,Integer>(ListAreaStats.STATE.shortName,ListAreaStats.STATE.name(),ListAreaStats.STATE.len));
			columns.add(new Triad<String,String,Integer>("Pop",Area.Stats.POPULATION.name(),Integer.valueOf(6)));
			columns.add(new Triad<String,String,Integer>("MedLv",Area.Stats.MED_LEVEL.name(),Integer.valueOf(6)));
		}

		final Session s=mob.session();
		final double wrap=(s==null)?78:s.getWrap();
		double totalCols=0;
		for(int i=0;i<columns.size();i++)
			totalCols+=columns.get(i).third.intValue();
		for(int i=0;i<columns.size();i++)
		{
			final double colVal=columns.get(i).third.intValue();
			final double pct=CMath.div(colVal,totalCols );
			final int newSize=(int)Math.round(Math.floor(CMath.mul(pct, wrap)));
			columns.get(i).third=Integer.valueOf(newSize);
		}

		final StringBuilder str=new StringBuilder("");
		for(final Triad<String,String,Integer> head : columns)
			str.append(CMStrings.padRight(head.first, head.third.intValue()));
		str.append("\n\r");
		final Triad<String,String,Integer> lastColomn=columns.get(columns.size()-1);
		Enumeration<Area> a;
		if(sortBys!=null)
		{
			final List<String> sortFields=sortBys;
			List<Area> sorted=new ArrayList<Area>();
			for(final Enumeration<Area> as=CMLib.map().areas();as.hasMoreElements();)
			{
				final Area A=as.nextElement();
				if((filter!=null)&&(!filter.passesFilter(A)))
					continue;
				sorted.add(A);
			}
			Collections.sort(sorted,new Comparator<Area>(){
				@Override
				public int compare(Area arg0, Area arg1) 
				{
					for(String sortField : sortFields)
					{
						Comparable val0=getAreaStatFromSomewhere(arg0,sortField);
						Comparable val1=getAreaStatFromSomewhere(arg1,sortField);
						int comp=1;
						if((val0==null)&&(val1==null))
							comp=0;
						else
						if(val0==null)
							comp=-1;
						else
						if(val1==null)
							comp=1;
						else
							comp=val0.compareTo(val1);
						if(comp!=0)
							return comp;
					}
					return 0;
				}
			});
			if(sorted.size()>0)
				a=new IteratorEnumeration<Area>(sorted.iterator());
			else
				a=CMLib.map().areas();
		}
		else
			a=CMLib.map().areas();
		for(;a.hasMoreElements();)
		{
			final Area A=a.nextElement();
			if((filter!=null)&&(!filter.passesFilter(A)))
				continue;
			for(final Triad<String,String,Integer> head : columns)
			{
				Object val =getAreaStatFromSomewhere(A,head.second);
				if(val==null) val="?";
				if(head==lastColomn)
					str.append(CMStrings.scrunchWord(val.toString(), head.third.intValue()-1));
				else
					str.append(CMStrings.padRight(CMStrings.scrunchWord(val.toString(), head.third.intValue()-1), head.third.intValue()));
			}
			str.append("\n\r");
		}
		if(s!=null)
			s.colorOnlyPrint(str.toString(), true);
	}

	public void listSessions(MOB mob, Vector commands)
	{
		String sort="";
		if((commands!=null)&&(commands.size()>1))
			sort=CMParms.combine(commands,1).trim().toUpperCase();
		final StringBuffer lines=new StringBuffer("\n\r^x");
		lines.append(CMStrings.padRight("#",3)+"| ");
		lines.append(CMStrings.padRight(_("Status"),9)+"| ");
		lines.append(CMStrings.padRight(_("Valid"),5)+"| ");
		lines.append(CMStrings.padRight(_("Name"),17)+"| ");
		lines.append(CMStrings.padRight(_("IP"),17)+"| ");
		lines.append(CMStrings.padRight(_("Idle"),17)+"^.^N\n\r");
		final Vector broken=new Vector();
		for(final Session S : CMLib.sessions().allIterable())
		{
			final String[] set=new String[6];
			set[0]=CMStrings.padRight(""+broken.size(),3)+"| ";
			set[1]=(S.isStopped()?"^H":"")+CMStrings.padRight(S.getStatus().toString(),9)+(S.isStopped()?"^?":"")+"| ";
			if (S.mob() != null)
			{
				set[2]=CMStrings.padRight(((S.mob().session()==S)?"Yes":"^HNO!^?"),5)+"| ";
				set[3]="^!"+CMStrings.padRight("^<LSTUSER^>"+S.mob().Name()+"^</LSTUSER^>",17)+"^?| ";
			}
			else
			{
				set[2]=CMStrings.padRight(_("N/A"),5)+"| ";
				set[3]=CMStrings.padRight(_("NAMELESS"),17)+"| ";
			}
			set[4]=CMStrings.padRight(S.getAddress(),17)+"| ";
			set[5]=CMStrings.padRight(CMLib.english().returnTime(S.getIdleMillis(),0)+"",17);
			broken.addElement(set);
		}
		Vector sorted=null;
		int sortNum=-1;
		if(sort.length()>0)
		{
			if("STATUS".startsWith(sort))
				sortNum=1;
			else
			if("VALID".startsWith(sort))
				sortNum=2;
			else
			if(("NAME".startsWith(sort))||("PLAYER".startsWith(sort)))
				sortNum=3;
			else
			if(("IP".startsWith(sort))||("ADDRESS".startsWith(sort)))
				sortNum=4;
			else
			if(("IDLE".startsWith(sort))||("MILLISECONDS".startsWith(sort)))
				sortNum=5;
		}
		if(sortNum<0)
			sorted=broken;
		else
		{
			sorted=new Vector();
			while(broken.size()>0)
			{
				int selected=0;
				for(int s=1;s<broken.size();s++)
				{
					final String[] S=(String[])broken.elementAt(s);
					if(S[sortNum].compareToIgnoreCase(((String[])broken.elementAt(selected))[sortNum])<0)
					   selected=s;
				}
				sorted.addElement(broken.elementAt(selected));
				broken.removeElementAt(selected);
			}
		}
		for(int s=0;s<sorted.size();s++)
		{
			final String[] S=(String[])sorted.elementAt(s);
			for (final String element : S)
				lines.append(element);
			lines.append("\n\r");
		}
		if(!mob.isMonster())
			mob.session().colorOnlyPrintln(lines.toString());
	}

	public void archonlist(MOB mob, Vector commands)
	{
		if(commands.size()==0)
		{
			mob.tell(_("List what?"));
			return;
		}

		final Session s=mob.session();
		if(s==null) return;

		final String listWord=((String)commands.firstElement()).toUpperCase();
		String rest=(commands.size()>1)?rest=CMParms.combine(commands,1):"";
		ListCmdEntry code=getMyCmd(mob, listWord);
		if((code==null)||(listWord.length()==0))
		{
			final List<String> V=getMyCmdWords(mob);
			if(V.size()==0)
				mob.tell(_("You are not allowed to use this command!"));
			else
			{
				final StringBuilder str=new StringBuilder("");
				for(int v=0;v<V.size();v++)
					if(V.get(v).length()>0)
					{
						str.append(V.get(v));
						if(v==(V.size()-2))
							str.append(_(", or "));
						else
						if(v<(V.size()-1))
							str.append(", ");
					}
				mob.tell(_("You cannot list '@x1'.  Try @x2.",listWord,str.toString()));
			}
			return;
		}
		switch(code)
		{
		case UNLINKEDEXITS:	s.wraplessPrintln(unlinkedExits(mob.session(),commands)); break;
		case ITEMS: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.basicItems()).toString()); break;
		case ARMOR: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.armor()).toString()); break;
		case ENVRESOURCES: s.wraplessPrintln(listEnvResources(mob.session(),rest)); break;
		case WEAPONS: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.weapons()).toString()); break;
		case MOBS: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.mobTypes()).toString()); break;
		case ROOMS: s.wraplessPrintln(roomDetails(mob.session(),mob.location().getArea().getMetroMap(),mob.location()).toString()); break;
		case AREA: s.wraplessPrintln(roomTypes(mob,mob.location().getArea().getMetroMap(),mob.location(),commands).toString()); break;
		case LOCALES: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.locales()).toString()); break;
		case BEHAVIORS: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.behaviors()).toString()); break;
		case EXITS: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.exits()).toString()); break;
		case RACES: s.wraplessPrintln(listRaces(s,CMClass.races(),rest).toString()); break;
		case CLASSES: s.wraplessPrintln(listCharClasses(s,CMClass.charClasses(),rest.equalsIgnoreCase("SHORT")).toString()); break;
		case STAFF: s.wraplessPrintln(listSubOps(mob.session()).toString()); break;
		case SPELLS: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_SPELL).toString()); break;
		case SONGS: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_SONG).toString()); break;
		case PRAYERS: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_PRAYER).toString()); break;
		case PROPERTIES: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_PROPERTY).toString()); break;
		case THIEFSKILLS: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_THIEF_SKILL).toString()); break;
		case COMMON: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_COMMON_SKILL).toString()); break;
		case JOURNALS: s.println(listJournals(mob.session()).toString()); break;
		case SKILLS: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_SKILL).toString()); break;
		case QUESTS: s.println(listQuests(mob.session()).toString()); break;
		case DISEASES: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_DISEASE).toString()); break;
		case POISONS: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_POISON).toString()); break;
		case TICKS: s.println(listTicks(mob.session(),CMParms.combine(commands,1)).toString()); break;
		case MAGIC: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.miscMagic()).toString()); break;
		case TECH: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.tech()).toString()); break;
		case CLANITEMS: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.clanItems()).toString()); break;
		case COMMANDJOURNAL: s.println(journalList(mob.session(),listWord).toString()); break;
		case REALESTATE: s.wraplessPrintln(roomPropertyDetails(mob.session(),mob.location().getArea(),rest).toString()); break;
		case NOPURGE:
		{
			final StringBuilder str=new StringBuilder("\n\rProtected players:\n\r");
			final List<String> protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
			if((protectedOnes!=null)&&(protectedOnes.size()>0))
			for(int b=0;b<protectedOnes.size();b++)
				str.append((b+1)+") "+(protectedOnes.get(b))+"\n\r");
			s.wraplessPrintln(str.toString());
			break;
		}
		case BANNED:
		{
			final StringBuilder str=new StringBuilder("\n\rBanned names/ips:\n\r");
			final List<String> banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
			if((banned!=null)&&(banned.size()>0))
			for(int b=0;b<banned.size();b++)
				str.append((b+1)+") "+(banned.get(b))+"\n\r");
			s.wraplessPrintln(str.toString());
			break;
		}
		case RACECATS: s.wraplessPrintln(listRaceCats(s,CMClass.races(),CMParms.containsIgnoreCase(commands,"SHORT")).toString()); break;
		case LOG: listLog(mob,commands); break;
		case USERS: listUsers(mob.session(),mob,commands); break;
		case LINKAGES: s.println(listLinkages(mob.session(),mob,rest).toString()); break;
		case REPORTS: s.println(listReports(mob.session(),mob).toString()); break;
		case THREADS: s.println(listThreads(mob.session(),mob,CMParms.containsIgnoreCase(commands,"SHORT"),CMParms.containsIgnoreCase(commands,"EXTEND")).toString()); break;
		case RESOURCES: s.println(listResources(mob,CMParms.combine(commands,1))); break;
		case ONEWAYDOORS: s.wraplessPrintln(reallyFindOneWays(mob.session(),commands)); break;
		case CHANTS: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_CHANT).toString()); break;
		case SUPERPOWERS: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_SUPERPOWER).toString()); break;
		case COMPONENTS: s.wraplessPrintln(listComponents(mob.session())); break;
		case EXPERTISES: s.wraplessPrintln(listExpertises(mob.session())); break;
		case FACTIONS: s.wraplessPrintln(CMLib.factions().listFactions()); break;
		case MATERIALS: s.wraplessPrintln(listMaterials()); break;
		case OBJCOUNTERS: s.println("\n\r^xCounter Report: NO LONGER AVAILABLE^.^N\n\r"); break;//+CMClass.getCounterReport()); break;
		case POLLS: listPolls(mob,commands); break;
		case CONTENTS: s.wraplessPrintln(listContent(mob,commands).toString()); break;
		case EXPIRES: s.wraplessPrintln(roomExpires(mob.session(),mob.location().getArea().getProperMap(),mob.location()).toString()); break;
		case TITLES: s.wraplessPrintln(listTitles(mob.session())); break;
		case AREARESOURCES: s.wraplessPrintln(roomResources(mob.session(),mob.location().getArea().getMetroMap(),mob.location()).toString()); break;
		case CONQUERED: s.wraplessPrintln(areaConquests(mob.session(),CMLib.map().areas()).toString()); break;
		case HOLIDAYS: s.wraplessPrintln(CMLib.quests().listHolidays(mob.location().getArea(),CMParms.combine(commands,1))); break;
		case RECIPES: s.wraplessPrintln(listRecipes(mob,CMParms.combine(commands,1))); break;
		case HELPFILEREQUESTS: s.wraplessPrint(listHelpFileRequests(mob,CMParms.combine(commands,1))); break;
		case SCRIPTS: s.wraplessPrintln(listScripts(mob.session(),mob,commands).toString()); break;
		case ACCOUNTS: listAccounts(mob.session(),mob,commands); break;
		case GOVERNMENTS: s.wraplessPrintln(listClanGovernments(mob.session(),commands)); break;
		case CLANS: s.wraplessPrintln(listClans(mob.session(),commands)); break;
		case DEBUGFLAG: s.println("\n\r^xDebug Settings: ^?^.^N\n\r"+CMParms.toStringList(new XVector<CMSecurity.DbgFlag>(CMSecurity.getDebugEnum()))+"\n\r"); break;
		case DISABLEFLAG: s.println("\n\r^xDisable Settings: ^?^.^N\n\r"+CMParms.toStringList(new XVector<CMSecurity.DisFlag>(CMSecurity.getDisablesEnum()))+"\n\r"); break;
		case ALLQUALIFYS: s.wraplessPrintln(listAllQualifies(mob.session(),commands).toString()); break;
		case NEWS: listNews(mob,commands); break;
		case AREAS: listAreas(mob, commands, WorldMap.mundaneAreaFilter); break;
		case SESSIONS: { listSessions(mob,commands); break; }
		case WORLD: listAreas(mob, commands, new WorldFilter(mob.location())); break;
		case PLANETS: listAreas(mob, commands, WorldMap.planetsAreaFilter); break;
		case SPACESHIPAREAS: listAreas(mob, commands, WorldMap.spaceShipsAreaFilter); break;
		case CURRENTS: listCurrents(mob, commands); break;
		case MANUFACTURERS: listManufacturers(mob, commands); break;
		case TECHSKILLS: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_TECH).toString()); break;
		case SOFTWARE: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.tech(new Filterer<Electronics>()
				{
					@Override public boolean passesFilter(Electronics obj) { return obj instanceof Software; }
				})).toString());
				break;
		case EXPIRED: s.wraplessPrintln(listExpired(mob)); break;
		case SPACE: s.wraplessPrintln(listSpace(mob,commands).toString()); 
				break;
		case SQL: listSql(mob,rest); break;
		default:
			s.println("List broke?!");
			break;
		}
	}

	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		List<Environmental> V=new Vector();
		commands.removeElementAt(0);
		String forWhat=null;
		if(commands.size()==0)
		{
			if(getAnyCmd(mob)!=null)
			{
				archonlist(mob,commands);
				return false;
			}
			V=CMLib.coffeeShops().getAllShopkeepers(mob.location(),mob);
		}
		else
		{
			final Vector origCommands=new XVector(commands);
			for(int c=commands.size()-2;c>=0;c--)
			{
				if(((String)commands.elementAt(c)).equalsIgnoreCase("for"))
				{
					forWhat=CMParms.combine(commands,c+1);
					for(int c1=commands.size()-1;c1>=c;c1--)
						commands.removeElementAt(c1);
					break;
				}
			}
			final String what=CMParms.combine(commands,0);
			final List<Environmental> V2=CMLib.coffeeShops().getAllShopkeepers(mob.location(),mob);
			Environmental shopkeeper=CMLib.english().fetchEnvironmental(V2,what,false);
			if((shopkeeper==null)&&(what.equals("shop")||what.equals("the shop")))
				for(int v=0;v<V2.size();v++)
					if(V2.get(v) instanceof Area)
					{ shopkeeper=V2.get(v); break;}
			if((shopkeeper!=null)
			&&(CMLib.coffeeShops().getShopKeeper(shopkeeper)!=null)
			&&(CMLib.flags().canBeSeenBy(shopkeeper,mob)))
				V.add(shopkeeper);
			else
			if(getAnyCmd(mob)!=null)
			{
				archonlist(mob,origCommands);
				return false;
			}
		}
		if(V.size()==0)
		{
			mob.tell(_("You don't see anyone here buying or selling."));
			return false;
		}
		for(int i=0;i<V.size();i++)
		{
			final Environmental shopkeeper=V.get(i);
			final ShopKeeper SHOP=CMLib.coffeeShops().getShopKeeper(shopkeeper);
			String str=_("<S-NAME> review(s) <T-YOUPOSS> inventory");
			if(SHOP instanceof Banker)
				str=_("<S-NAME> review(s) <S-HIS-HER> account with <T-NAMESELF>");
			else
			if(SHOP instanceof PostOffice)
				str=_("<S-NAME> check(s) <S-HIS-HER> postal box with <T-NAMESELF>");
			if(forWhat!=null)str+=_(" for '@x1'",forWhat);
			str+=".";
			final CMMsg newMsg=CMClass.getMsg(mob,shopkeeper,null,CMMsg.MSG_LIST,str);
			if(!mob.location().okMessage(mob,newMsg))
				return false;
			mob.location().send(mob,newMsg);
		}
		return false;
	}

	@Override public boolean canBeOrdered(){return true;}


}
