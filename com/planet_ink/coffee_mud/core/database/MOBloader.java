package com.planet_ink.coffee_mud.core.database;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.SecFlag;
import com.planet_ink.coffee_mud.core.CMSecurity.SecGroup;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.database.DBConnector.DBPreparedBatchEntry;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.sql.*;
import java.util.*;
import java.util.Map.Entry;

/*
 * Copyright 2000-2014 Bo Zimmerman Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
public class MOBloader
{
	protected DBConnector DB=null;

	public MOBloader(DBConnector newDB)
	{
		DB=newDB;
	}
	protected Room emptyRoom=null;

	public String DBReadUserOnly(MOB mob)
	{
		if(mob.Name().length()==0) return null;
		String locID=null;
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+mob.Name()+"'");
			if(R.next())
			{
				final CharStats stats=mob.baseCharStats();
				final CharState state=mob.baseState();
				final PlayerStats pstats=(PlayerStats)CMClass.getCommon("DefaultPlayerStats");
				mob.setPlayerStats(pstats);
				final String username=DBConnections.getRes(R,"CMUSERID");
				final String password=DBConnections.getRes(R,"CMPASS");
				mob.setName(username);
				pstats.setPassword(password);
				stats.setMyClasses(DBConnections.getRes(R,"CMCLAS"));
				stats.setStat(CharStats.STAT_STRENGTH,CMath.s_int(DBConnections.getRes(R,"CMSTRE")));
				stats.setMyRace(CMClass.getRace(DBConnections.getRes(R,"CMRACE")));
				stats.setStat(CharStats.STAT_DEXTERITY,CMath.s_int(DBConnections.getRes(R,"CMDEXT")));
				stats.setStat(CharStats.STAT_CONSTITUTION,CMath.s_int(DBConnections.getRes(R,"CMCONS")));
				stats.setStat(CharStats.STAT_GENDER,DBConnections.getRes(R,"CMGEND").charAt(0));
				stats.setStat(CharStats.STAT_WISDOM,CMath.s_int(DBConnections.getRes(R,"CMWISD")));
				stats.setStat(CharStats.STAT_INTELLIGENCE,CMath.s_int(DBConnections.getRes(R,"CMINTE")));
				stats.setStat(CharStats.STAT_CHARISMA,CMath.s_int(DBConnections.getRes(R,"CMCHAR")));
				state.setHitPoints(CMath.s_int(DBConnections.getRes(R,"CMHITP")));
				stats.setMyLevels(DBConnections.getRes(R,"CMLEVL"));
				int level=0;
				for(int i=0;i<mob.baseCharStats().numClasses();i++)
					level+=stats.getClassLevel(mob.baseCharStats().getMyClass(i));
				mob.basePhyStats().setLevel(level);
				state.setMana(CMath.s_int(DBConnections.getRes(R,"CMMANA")));
				state.setMovement(CMath.s_int(DBConnections.getRes(R,"CMMOVE")));
				mob.setDescription(DBConnections.getRes(R,"CMDESC"));
				final int align=(CMath.s_int(DBConnections.getRes(R,"CMALIG")));
				if((CMLib.factions().getFaction(CMLib.factions().AlignID())!=null)&&(align>=0))
					CMLib.factions().setAlignmentOldRange(mob,align);
				mob.setExperience(CMath.s_int(DBConnections.getRes(R,"CMEXPE")));
				//mob.setExpNextLevel(CMath.s_int(DBConnections.getRes(R,"CMEXLV")));
				mob.setWorshipCharID(DBConnections.getRes(R,"CMWORS"));
				mob.setPractices(CMath.s_int(DBConnections.getRes(R,"CMPRAC")));
				mob.setTrains(CMath.s_int(DBConnections.getRes(R,"CMTRAI")));
				mob.setAgeMinutes(CMath.s_long(DBConnections.getRes(R,"CMAGEH")));
				mob.setMoney(CMath.s_int(DBConnections.getRes(R,"CMGOLD")));
				mob.setWimpHitPoint(CMath.s_int(DBConnections.getRes(R,"CMWIMP")));
				mob.setQuestPoint(CMath.s_int(DBConnections.getRes(R,"CMQUES")));
				String roomID=DBConnections.getRes(R,"CMROID");
				if(roomID==null) roomID="";
				final int x=roomID.indexOf("||");
				if(x>=0)
				{
					locID=roomID.substring(x+2);
					mob.setLocation(CMLib.map().getRoom(locID));
					roomID=roomID.substring(0,x);
				}
				mob.setStartRoom(CMLib.map().getRoom(roomID));
				pstats.setLastDateTime(CMath.s_long(DBConnections.getRes(R,"CMDATE")));
				pstats.setChannelMask((int)DBConnections.getLongRes(R,"CMCHAN"));
				mob.basePhyStats().setAttackAdjustment(CMath.s_int(DBConnections.getRes(R,"CMATTA")));
				mob.basePhyStats().setArmor(CMath.s_int(DBConnections.getRes(R,"CMAMOR")));
				mob.basePhyStats().setDamage(CMath.s_int(DBConnections.getRes(R,"CMDAMG")));
				mob.setBitmap(CMath.s_int(DBConnections.getRes(R,"CMBTMP")));
				mob.setLiegeID(DBConnections.getRes(R,"CMLEIG"));
				mob.basePhyStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
				mob.basePhyStats().setWeight((int)DBConnections.getLongRes(R,"CMWEIT"));
				pstats.setPrompt(DBConnections.getRes(R,"CMPRPT"));
				final String colorStr=DBConnections.getRes(R,"CMCOLR");
				if((colorStr!=null)&&(colorStr.length()>0)&&(!colorStr.equalsIgnoreCase("NULL"))) pstats.setColorStr(colorStr);
				pstats.setLastIP(DBConnections.getRes(R,"CMLSIP"));
				mob.setClan("", Integer.MIN_VALUE); // delete all sequence
				pstats.setEmail(DBConnections.getRes(R,"CMEMAL"));
				final String buf=DBConnections.getRes(R,"CMPFIL");
				pstats.setXML(buf);
				stats.setNonBaseStatsFromString(DBConnections.getRes(R,"CMSAVE"));
				List<String> V9=CMParms.parseSemicolons(CMLib.xml().returnXMLValue(buf,"TATTS"),true);
				for(final Enumeration<MOB.Tattoo> e=mob.tattoos();e.hasMoreElements();)
					mob.delTattoo(e.nextElement());
				for(int v=0;v<V9.size();v++)
					mob.addTattoo(parseTattoo(V9.get(v)));
				V9=CMParms.parseSemicolons(CMLib.xml().returnXMLValue(buf,"EDUS"),true);
				mob.delAllExpertises();
				for(int v=0;v<V9.size();v++)
					mob.addExpertise(V9.get(v));
				if(pstats.getBirthday()==null)
					stats.setStat(CharStats.STAT_AGE,
						pstats.initializeBirthday((int)Math.round(CMath.div(mob.getAgeMinutes(),60.0)),stats.getMyRace()));
				mob.setImage(CMLib.xml().returnXMLValue(buf,"IMG"));
				final List<XMLLibrary.XMLpiece> CleanXML=CMLib.xml().parseAllXML(DBConnections.getRes(R,"CMMXML"));
				R.close();
				if(pstats.getSavedPose().length()>0)
					mob.setDisplayText(pstats.getSavedPose());
				CMLib.coffeeMaker().setFactionFromXML(mob,CleanXML);
				if((CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>1)&&(pstats.getAccount()==null))
				{
					// yes, this can happen when you wiggle in and out of the account system.
					for(final Enumeration<PlayerAccount> a = CMLib.players().accounts(); a.hasMoreElements();)
					{
						final PlayerAccount pA=a.nextElement();
						if(pA.findPlayer(mob.Name())!=null)
						{
							pstats.setAccount(pA);
							break;
						}
					}
				}
			}
			R.close();
			R=D.query("SELECT * FROM CMCHCL WHERE CMUSERID='"+mob.Name()+"'");
			while(R.next())
			{
				final String clanID=DBConnections.getRes(R,"CMCLAN");
				final int clanRole = this.BuildClanMemberRole(R);
				final Clan C=CMLib.clans().getClan(clanID);
				if(C!=null) mob.setClan(C.clanID(), clanRole);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return locID;
	}

	public void DBRead(MOB mob)
	{
		if(mob.Name().length()==0) return;
		if(emptyRoom==null) emptyRoom=CMClass.getLocale("StdRoom");
		final int oldDisposition=mob.basePhyStats().disposition();
		mob.basePhyStats().setDisposition(PhyStats.IS_NOT_SEEN|PhyStats.IS_SNEAKING);
		mob.phyStats().setDisposition(PhyStats.IS_NOT_SEEN|PhyStats.IS_SNEAKING);
		CMLib.players().addPlayer(mob);
		final String oldLocID=DBReadUserOnly(mob);
		mob.recoverPhyStats();
		mob.recoverCharStats();
		Room oldLoc=mob.location();
		boolean inhab=false;
		if(oldLoc!=null) inhab=oldLoc.isInhabitant(mob);
		mob.setLocation(emptyRoom);
		DBConnection D=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHIT WHERE CMUSERID='"+mob.Name()+"'");
			final Hashtable<String,Item> itemNums=new Hashtable<String,Item>();
			final Hashtable<Item,String> itemLocs=new Hashtable<Item,String>();
			while(R.next())
			{
				final String itemNum=DBConnections.getRes(R,"CMITNM");
				final String itemID=DBConnections.getRes(R,"CMITID");
				final Item newItem=CMClass.getItem(itemID);
				if(newItem==null)
					Log.errOut("MOB","Couldn't find item '"+itemID+"'");
				else
				{
					itemNums.put(itemNum,newItem);
					boolean addToMOB=true;
					String text=DBConnections.getResQuietly(R,"CMITTX");
					int roomX;
					if(text.startsWith("<ROOM") && ((roomX=text.indexOf("/>"))>=0))
					{
						final String roomXML=text.substring(0,roomX+2);
						text=text.substring(roomX+2);
						newItem.setMiscText(text);
						final List<XMLLibrary.XMLpiece> xml=CMLib.xml().parseAllXML(roomXML);
						if((xml!=null)&&(xml.size()>0))
						{
							final String roomID=xml.get(0).parms.get("ID");
							final long expirationDate=CMath.s_long(xml.get(0).parms.get("EXPIRE"));
							if(roomID.startsWith("SPACE.") && (newItem instanceof SpaceShip))
							{
								CMLib.map().addObjectToSpace((SpaceShip)newItem,CMParms.toLongArray(CMParms.parseCommas(roomID.substring(6), true)));
								addToMOB=false;
							}
							else
							{
								final Room itemR=CMLib.map().getRoom(roomID);
								if(itemR!=null)
								{
									if((newItem instanceof SpaceShip)&&(itemR instanceof LocationRoom))
										((SpaceShip)newItem).dockHere((LocationRoom)itemR);
									else
										itemR.addItem(newItem);
									newItem.setExpirationDate(expirationDate);
									addToMOB=false;
								}
							}
						}
					}
					else
					{
						newItem.setMiscText(text);
					}
					if((oldLoc==null)&&(oldLocID!=null)
					&&(newItem instanceof SpaceShip))
					{
						final Area area=((SpaceShip)newItem).getShipArea();
						if(area != null)
							oldLoc=area.getRoom(oldLocID);
					}
					final String loc=DBConnections.getResQuietly(R,"CMITLO");
					if(loc.length()>0)
					{
						final Item container=itemNums.get(loc);
						if(container instanceof Container)
							newItem.setContainer((Container)container);
						else
							itemLocs.put(newItem,loc);
					}
					newItem.wearAt((int)DBConnections.getLongRes(R,"CMITWO"));
					newItem.setUsesRemaining((int)DBConnections.getLongRes(R,"CMITUR"));
					newItem.basePhyStats().setLevel((int)DBConnections.getLongRes(R,"CMITLV"));
					newItem.basePhyStats().setAbility((int)DBConnections.getLongRes(R,"CMITAB"));
					newItem.basePhyStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
					newItem.recoverPhyStats();
					if(addToMOB)
						mob.addItem(newItem);
					else
						mob.playerStats().getExtItems().addItem(newItem);
				}
			}
			for(final Enumeration<Item> e=itemLocs.keys();e.hasMoreElements();)
			{
				final Item keyItem=e.nextElement();
				final String location=itemLocs.get(keyItem);
				final Item container=itemNums.get(location);
				if(container instanceof Container)
				{
					keyItem.setContainer((Container)container);
					keyItem.recoverPhyStats();
					container.recoverPhyStats();
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		D=null;
		if(oldLoc!=null)
		{
			mob.setLocation(oldLoc);
			if(inhab&&(!oldLoc.isInhabitant(mob)))
				oldLoc.addInhabitant(mob);
		}
		// now grab the abilities
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHAB WHERE CMUSERID='"+mob.Name()+"'");
			while(R.next())
			{
				final String abilityID=DBConnections.getRes(R,"CMABID");
				int proficiency=(int)DBConnections.getLongRes(R,"CMABPF");
				if((proficiency==Integer.MIN_VALUE)||(proficiency==Integer.MIN_VALUE+1))
				{
					if(abilityID.equalsIgnoreCase("ScriptingEngine"))
					{
						if(CMClass.getCommon("DefaultScriptingEngine")==null)
							Log.errOut("MOB","Couldn't find scripting engine!");
						else
						{

							final String xml=DBConnections.getRes(R,"CMABTX");
							if(xml.length()>0)
								CMLib.coffeeMaker().setGenScripts(mob,CMLib.xml().parseAllXML(xml),true);
						}
					}
					else
					{
						final Behavior newBehavior=CMClass.getBehavior(abilityID);
						if(newBehavior==null)
							Log.errOut("MOB","Couldn't find behavior '"+abilityID+"'");
						else
						{
							newBehavior.setParms(DBConnections.getRes(R,"CMABTX"));
							mob.addBehavior(newBehavior);
						}
					}
				}
				else
				{
					final Ability newAbility=CMClass.getAbility(abilityID);
					if(newAbility==null)
						Log.errOut("MOB","Couldn't find ability '"+abilityID+"'");
					else
					{
						if((proficiency<0)||(proficiency==Integer.MAX_VALUE))
						{
							if(proficiency==Integer.MAX_VALUE)
							{
								newAbility.setProficiency(CMLib.ableMapper().getMaxProficiency(newAbility.ID()));
								mob.addNonUninvokableEffect(newAbility);
								newAbility.setMiscText(DBConnections.getRes(R,"CMABTX"));
							}else
							{
								proficiency=proficiency+200;
								newAbility.setProficiency(proficiency);
								newAbility.setMiscText(DBConnections.getRes(R,"CMABTX"));
								final Ability newAbility2=(Ability)newAbility.copyOf();
								mob.addNonUninvokableEffect(newAbility);
								mob.addAbility(newAbility2);
							}
						}else
						{
							newAbility.setProficiency(proficiency);
							newAbility.setMiscText(DBConnections.getRes(R,"CMABTX"));
							mob.addAbility(newAbility);
						}
					}
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		D=null;
		mob.basePhyStats().setDisposition(oldDisposition);
		mob.recoverCharStats();
		mob.recoverPhyStats();
		mob.recoverMaxState();
		mob.resetToMaxState();
		if(mob.baseCharStats()!=null)
		{
			mob.baseCharStats().getCurrentClass().startCharacter(mob,false,true);
			final int oldWeight=mob.basePhyStats().weight();
			final int oldHeight=mob.basePhyStats().height();
			mob.baseCharStats().getMyRace().startRacing(mob,true);
			if(oldWeight>0) mob.basePhyStats().setWeight(oldWeight);
			if(oldHeight>0) mob.basePhyStats().setHeight(oldHeight);
		}
		mob.recoverCharStats();
		mob.recoverPhyStats();
		mob.recoverMaxState();
		mob.resetToMaxState();
		CMLib.threads().suspendResumeRecurse(mob, false, true);
		// wont add if same name already exists
	}

	public List<String> getUserList()
	{
		DBConnection D=null;
		final Vector<String> V=new Vector<String>();
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHAR");
			if(R!=null) while(R.next())
			{
				final String username=DBConnections.getRes(R,"CMUSERID");
				V.addElement(username);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return V;
	}

	protected PlayerLibrary.ThinPlayer parseThinUser(ResultSet R)
	{
		try
		{
			final PlayerLibrary.ThinPlayer thisUser=new PlayerLibrary.ThinPlayer();
			thisUser.name=DBConnections.getRes(R,"CMUSERID");
			String cclass=DBConnections.getRes(R,"CMCLAS");
			final int x=cclass.lastIndexOf(';');
			CharClass C=null;
			if((x>0)&&(x<cclass.length()-2))
			{
				C=CMClass.getCharClass(cclass.substring(x+1));
				if(C!=null) cclass=C.name();
			}
			thisUser.charClass=(cclass);
			final String rrace=DBConnections.getRes(R,"CMRACE");
			final Race R2=CMClass.getRace(rrace);
			if(R2!=null)
				thisUser.race=(R2.name());
			else
				thisUser.race=rrace;
			final List<String> lvls=CMParms.parseSemicolons(DBConnections.getRes(R,"CMLEVL"), true);
			thisUser.level=0;
			for(final String lvl : lvls)
				thisUser.level+=CMath.s_int(lvl);
			thisUser.age=(int)DBConnections.getLongRes(R,"CMAGEH");
			final MOB M=CMLib.players().getPlayer(thisUser.name);
			if((M!=null)&&(M.lastTickedDateTime()>0))
				thisUser.last=M.lastTickedDateTime();
			else
				thisUser.last=DBConnections.getLongRes(R,"CMDATE");
			final String lsIP=DBConnections.getRes(R,"CMLSIP");
			thisUser.email=DBConnections.getRes(R,"CMEMAL");
			thisUser.ip=lsIP;
			thisUser.exp=CMath.s_int(DBConnections.getRes(R,"CMEXPE"));
			thisUser.expLvl=CMath.s_int(DBConnections.getRes(R,"CMEXLV"));
			return thisUser;
		}catch(final Exception e)
		{
			Log.errOut("MOBloader",e);
		}
		return null;
	}

	public PlayerLibrary.ThinPlayer getThinUser(String name)
	{
		DBConnection D=null;
		PlayerLibrary.ThinPlayer thisUser=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+name+"'");
			if(R!=null) while(R.next())
				thisUser=parseThinUser(R);
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return thisUser;
	}

	public List<PlayerLibrary.ThinPlayer> getExtendedUserList()
	{
		DBConnection D=null;
		final Vector<PlayerLibrary.ThinPlayer> allUsers=new Vector<PlayerLibrary.ThinPlayer>();
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHAR");
			if(R!=null) while(R.next())
			{
				final PlayerLibrary.ThinPlayer thisUser=parseThinUser(R);
				if(thisUser != null)
					allUsers.addElement(thisUser);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return allUsers;
	}

	public MOB.Tattoo parseTattoo(String tattoo)
	{
		if(tattoo==null)
			return new MOB.Tattoo("");
		int tickDown = 0;
		if((tattoo.length()>0)
		&&(Character.isDigit(tattoo.charAt(0))))
		{
			final int x=tattoo.indexOf(' ');
			if((x>0)
			&&(CMath.isNumber(tattoo.substring(0,x).trim())))
			{
				tickDown=CMath.s_int(tattoo.substring(0,x));
				tattoo=tattoo.substring(x+1).trim();
			}
		}
		return new MOB.Tattoo(tattoo, tickDown);
	}

	public List<PlayerLibrary.ThinPlayer> vassals(MOB mob, String liegeID)
	{
		DBConnection D=null;
		List<PlayerLibrary.ThinPlayer> list=new ArrayList<PlayerLibrary.ThinPlayer>();
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMLEIG='"+liegeID+"'");
			if(R!=null) 
			while(R.next())
				list.add(this.parseThinUser(R));
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return list;
	}

	public DVector worshippers(String deityID)
	{
		DBConnection D=null;
		final DVector DV=new DVector(4);
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMWORS='"+deityID+"'");
			if(R!=null) while(R.next())
			{
				final String username=DBConnections.getRes(R,"CMUSERID");
				String cclass=DBConnections.getRes(R,"CMCLAS");
				final int x=cclass.lastIndexOf(';');
				if((x>0)&&(x<cclass.length()-2)) cclass=CMClass.getCharClass(cclass.substring(x+1)).name();
				final String race=(CMClass.getRace(DBConnections.getRes(R,"CMRACE"))).name();
				final List<String> lvls=CMParms.parseSemicolons(DBConnections.getRes(R,"CMLEVL"), true);
				int level=0;
				for(final String lvl : lvls)
					level+=CMath.s_int(lvl);
				DV.addElement(username,
							  cclass,
							  ""+level,
							  race);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return DV;
	}

	public List<MOB> DBScanFollowers(MOB mob)
	{
		DBConnection D=null;
		final Vector<MOB> V=new Vector<MOB>();
		// now grab the followers
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHFO WHERE CMUSERID='"+mob.Name()+"'");
			while(R.next())
			{
				final String MOBID=DBConnections.getRes(R,"CMFOID");
				final MOB newMOB=CMClass.getMOB(MOBID);
				if(newMOB==null)
					Log.errOut("MOB","Couldn't find MOB '"+MOBID+"'");
				else
				{
					newMOB.setMiscText(DBConnections.getResQuietly(R,"CMFOTX"));
					newMOB.basePhyStats().setLevel(((int)DBConnections.getLongRes(R,"CMFOLV")));
					newMOB.basePhyStats().setAbility((int)DBConnections.getLongRes(R,"CMFOAB"));
					newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
					newMOB.recoverPhyStats();
					newMOB.recoverCharStats();
					newMOB.recoverMaxState();
					newMOB.resetToMaxState();
					V.addElement(newMOB);
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return V;
	}

	public void DBReadFollowers(MOB mob, boolean bringToLife)
	{
		Room location=mob.location();
		if(location==null) location=mob.getStartRoom();
		final List<MOB> V=DBScanFollowers(mob);
		for(int v=0;v<V.size();v++)
		{
			final MOB newMOB=V.get(v);
			final Room room=(location==null)?newMOB.getStartRoom():location;
			newMOB.setStartRoom(room);
			newMOB.setLocation(room);
			newMOB.setFollowing(mob);
			if((newMOB.getStartRoom()!=null)
			&&(CMLib.law().doesHavePriviledgesHere(mob,newMOB.getStartRoom()))
			&&((newMOB.location()==null)
					||(!CMLib.law().doesHavePriviledgesHere(mob,newMOB.location()))))
				newMOB.setLocation(newMOB.getStartRoom());
			if(bringToLife)
			{
				newMOB.bringToLife(mob.location(),true);
				mob.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,CMLib.lang()._("<S-NAME> appears!"));
			}
		}
	}

	public void DBUpdateEmail(MOB mob)
	{
		final PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;
		DB.update("UPDATE CMCHAR SET CMEMAL='"+pstats.getEmail()+"' WHERE CMUSERID='"+mob.Name()+"'");
	}

	private int BuildClanMemberRole(ResultSet R)
	{
		return (int)DBConnections.getLongRes(R,"CMCLRO");
	}

	private MemberRecord BuildClanMemberRecord(ResultSet R)
	{
		final String username=DB.getRes(R,"CMUSERID");
		final int clanRole = BuildClanMemberRole(R);
		int mobpvps=0;
		int playerpvps=0;
		final String stats=DB.getRes(R,"CMCLSTS");
		if(stats!=null)
		{
			final String[] splitstats=stats.split(";");
			if(splitstats.length>0) mobpvps=CMath.s_int(splitstats[0]);
			if(splitstats.length>1) playerpvps=CMath.s_int(splitstats[1]);
		}
		return new Clan.MemberRecord(username,clanRole,mobpvps,playerpvps);
	}

	public MemberRecord DBGetClanMember(String clan, String name)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHCL where CMCLAN='"+clan+"' and CMUSERID='"+name+"'");
			if(R!=null) while(R.next())
			{
				return BuildClanMemberRecord(R);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}

	public List<Clan.MemberRecord> DBClanMembers(String clan)
	{
		final List<Clan.MemberRecord> members = new Vector<Clan.MemberRecord>();
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHCL where CMCLAN='"+clan+"'");
			if(R!=null) while(R.next())
			{
				members.add(BuildClanMemberRecord(R));
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return members;
	}

	public void DBUpdateClanMembership(String name, String clan, int role)
	{
		final MOB M=CMLib.players().getPlayer(name);
		if(M!=null)
		{
			M.setClan(clan, role);
		}
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			if(role<0)
				DB.update("DELETE FROM CMCHCL WHERE CMUSERID='"+name+"' AND CMCLAN='"+clan+"'");
			else
			{
				final ResultSet R=D.query("SELECT * FROM CMCHCL where CMCLAN='"+clan+"' and CMUSERID='"+name+"'");
				if((R!=null) && (R.next()))
				{
					final int clanRole = BuildClanMemberRole(R);
					R.close();
					if(clanRole == role)
						return;
					D.update("UPDATE CMCHCL SET CMCLRO="+role+" where CMCLAN='"+clan+"' and CMUSERID='"+name+"'", 0);
				}
				else
				{
					D.update("INSERT INTO CMCHCL (CMUSERID, CMCLAN, CMCLRO, CMCLSTS) values ('"+name+"','"+clan+"',"+role+",'0;0')",0);
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public void DBUpdateClanKills(String clan, String name, int adjMobKills, int adjPlayerKills)
	{
		if(((adjMobKills==0)&&(adjPlayerKills==0))
		||(clan==null)
		||(name==null))
			return;

		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHCL where CMCLAN='"+clan+"' and CMUSERID='"+name+"'");
			MemberRecord M=null;
			if(R!=null)
			{
				if(R.next())
				{
					M=BuildClanMemberRecord(R);
					R.close();
					M.mobpvps+=adjMobKills;
					M.playerpvps+=adjPlayerKills;
					final String newStats=M.mobpvps+";"+M.playerpvps;
					D.update("UPDATE CMCHCL SET CMCLSTS='"+newStats+"' where CMCLAN='"+clan+"' and CMUSERID='"+name+"'", 0);
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public void DBUpdate(MOB mob)
	{
		DBUpdateJustMOB(mob);
		final PlayerStats pStats = mob.playerStats();
		if((mob.Name().length()==0)||(pStats==null))
			return;
		DBUpdateItems(mob);
		DBUpdateAbilities(mob);
		pStats.setLastUpdated(System.currentTimeMillis());
		final PlayerAccount account = pStats.getAccount();
		if(account != null)
		{
			DBUpdateAccount(account);
			account.setLastUpdated(System.currentTimeMillis());
		}
	}

	public void DBUpdatePassword(String name, String password)
	{
		name=CMStrings.capitalizeAndLower(name);
		DB.update("UPDATE CMCHAR SET CMPASS='"+password+"' WHERE CMUSERID='"+name.replace('\'', 'n')+"'");
	}

	private String getPlayerStatsXML(MOB mob)
	{
		final PlayerStats pstats=mob.playerStats();
		if(pstats==null) return "";
		final StringBuilder pfxml=new StringBuilder(pstats.getXML());
		if(mob.tattoos().hasMoreElements())
		{
			pfxml.append("<TATTS>");
			for(final Enumeration<MOB.Tattoo> e=mob.tattoos();e.hasMoreElements();)
				pfxml.append(e.nextElement().toString()+";");
			pfxml.append("</TATTS>");
		}
		if(mob.expertises().hasMoreElements())
		{
			pfxml.append("<EDUS>");
			for(final Enumeration<String> x=mob.expertises();x.hasMoreElements();)
				pfxml.append(x.nextElement()).append(';');
			pfxml.append("</EDUS>");
		}
		pfxml.append(CMLib.xml().convertXMLtoTag("IMG",mob.rawImage()));
		return pfxml.toString();
	}

	public void DBUpdateJustPlayerStats(MOB mob)
	{
		if(mob.Name().length()==0)
		{
			DBCreateCharacter(mob);
			return;
		}
		final PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;
		final String pfxml=getPlayerStatsXML(mob);
		DB.updateWithClobs("UPDATE CMCHAR SET CMPFIL=? WHERE CMUSERID='"+mob.Name()+"'", pfxml.toString());
	}

	public void DBUpdateJustMOB(MOB mob)
	{
		if(mob.Name().length()==0)
		{
			DBCreateCharacter(mob);
			return;
		}
		final PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;
		final String strStartRoomID=(mob.getStartRoom()!=null)?CMLib.map().getExtendedRoomID(mob.getStartRoom()):"";
		String strOtherRoomID=(mob.location()!=null)?CMLib.map().getExtendedRoomID(mob.location()):"";

		if((mob.location()!=null)
		&&(mob.location().getArea()!=null)
		&&(CMath.bset(mob.location().getArea().flags(),Area.FLAG_INSTANCE_PARENT)
			||CMath.bset(mob.location().getArea().flags(),Area.FLAG_INSTANCE_CHILD)))
			strOtherRoomID=strStartRoomID;

		final String pfxml=getPlayerStatsXML(mob);
		final StringBuilder cleanXML=new StringBuilder();
		cleanXML.append(CMLib.coffeeMaker().getFactionXML(mob));
		DB.updateWithClobs(
				 "UPDATE CMCHAR SET  CMPASS='"+pstats.getPasswordStr()+"'"
				+", CMCLAS='"+mob.baseCharStats().getMyClassesStr()+"'"
				+", CMSTRE="+mob.baseCharStats().getStat(CharStats.STAT_STRENGTH)
				+", CMRACE='"+mob.baseCharStats().getMyRace().ID()+"'"
				+", CMDEXT="+mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY)
				+", CMCONS="+mob.baseCharStats().getStat(CharStats.STAT_CONSTITUTION)
				+", CMGEND='"+((char)mob.baseCharStats().getStat(CharStats.STAT_GENDER))+"'"
				+", CMWISD="+mob.baseCharStats().getStat(CharStats.STAT_WISDOM)
				+", CMINTE="+mob.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE)
				+", CMCHAR="+mob.baseCharStats().getStat(CharStats.STAT_CHARISMA)
				+", CMHITP="+mob.baseState().getHitPoints()
				+", CMLEVL='"+mob.baseCharStats().getMyLevelsStr()+"'"
				+", CMMANA="+mob.baseState().getMana()
				+", CMMOVE="+mob.baseState().getMovement()
				+", CMALIG=-1"
				+", CMEXPE="+mob.getExperience()
				+", CMEXLV="+mob.getExpNextLevel()
				+", CMWORS='"+mob.getWorshipCharID()+"'"
				+", CMPRAC="+mob.getPractices()
				+", CMTRAI="+mob.getTrains()
				+", CMAGEH="+mob.getAgeMinutes()
				+", CMGOLD="+mob.getMoney()
				+", CMWIMP="+mob.getWimpHitPoint()
				+", CMQUES="+mob.getQuestPoint()
				+", CMROID='"+strStartRoomID+"||"+strOtherRoomID+"'"
				+", CMDATE='"+pstats.getLastDateTime()+"'"
				+", CMCHAN="+pstats.getChannelMask()
				+", CMATTA="+mob.basePhyStats().attackAdjustment()
				+", CMAMOR="+mob.basePhyStats().armor()
				+", CMDAMG="+mob.basePhyStats().damage()
				+", CMBTMP="+mob.getBitmap()
				+", CMLEIG='"+mob.getLiegeID()+"'"
				+", CMHEIT="+mob.basePhyStats().height()
				+", CMWEIT="+mob.basePhyStats().weight()
				+", CMPRPT=?"
				+", CMCOLR=?"
				+", CMLSIP='"+pstats.getLastIP()+"'"
				+", CMEMAL=?"
				+", CMPFIL=?"
				+", CMSAVE=?"
				+", CMMXML=?"
				+", CMDESC=?"
				+"  WHERE CMUSERID='"+mob.Name()+"'"
				,new String[][]{{pstats.getPrompt(),pstats.getColorStr(),pstats.getEmail(),
								 pfxml,mob.baseCharStats().getNonBaseStatsAsString(),cleanXML.toString(),mob.description()}});
		final List<String> clanStatements=new LinkedList<String>();
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHCL WHERE CMUSERID='"+mob.Name()+"'");
			final Set<String> savedClans=new HashSet<String>();
			if(R!=null)
			{
				while(R.next())
				{
					final String clanID=DB.getRes(R,"CMCLAN");
					final Pair<Clan,Integer> role=mob.getClanRole(clanID);
					if(role==null)
						clanStatements.add("DELETE FROM CMCHCL WHERE CMUSERID='"+mob.Name()+"' AND CMCLAN='"+clanID+"'");
					else
					{
						final MemberRecord M=BuildClanMemberRecord(R);
						if(role.second.intValue()!=M.role)
							clanStatements.add("UPDATE CMCHCL SET CMCLRO="+role+" where CMCLAN='"+clanID+"' and CMUSERID='"+mob.Name()+"'");
					}
					savedClans.add(clanID.toUpperCase());
				}
				R.close();
			}
			for(final Pair<Clan,Integer> p : mob.clans())
				if(!savedClans.contains(p.first.clanID().toUpperCase()))
					clanStatements.add("INSERT INTO CMCHCL (CMUSERID, CMCLAN, CMCLRO, CMCLSTS) values ('"+mob.Name()+"','"+p.first.clanID()+"',"+p.second.intValue()+",'0;0')");
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		if(clanStatements.size()>0)
			DB.update(clanStatements.toArray(new String[0]));
	}

	protected String getDBItemUpdateString(final MOB mob, final Item thisItem)
	{
		CMLib.catalog().updateCatalogIntegrity(thisItem);
		final String container=((thisItem.container()!=null)?(""+thisItem.container()):"");
		return "INSERT INTO CMCHIT (CMUSERID, CMITNM, CMITID, CMITTX, CMITLO, CMITWO, "
		+"CMITUR, CMITLV, CMITAB, CMHEIT"
		+") values ('"+mob.Name()+"','"+(thisItem)+"','"+thisItem.ID()+"',?,'"+container+"',"+thisItem.rawWornCode()+","
		+thisItem.usesRemaining()+","+thisItem.basePhyStats().level()+","+thisItem.basePhyStats().ability()+","
		+thisItem.basePhyStats().height()+")";
	}

	private List<DBPreparedBatchEntry> getDBItemUpdateStrings(MOB mob)
	{
		final HashSet<String> done=new HashSet<String>();
		final List<DBPreparedBatchEntry> strings=new LinkedList<DBPreparedBatchEntry>();
		for(int i=0;i<mob.numItems();i++)
		{
			final Item thisItem=mob.getItem(i);
			if((thisItem!=null)&&(!done.contains(""+thisItem))&&(thisItem.isSavable()))
			{
				CMLib.catalog().updateCatalogIntegrity(thisItem);
				final String sql=getDBItemUpdateString(mob,thisItem);
				strings.add(new DBPreparedBatchEntry(sql,thisItem.text()+" "));
				done.add(""+thisItem);
			}
		}
		final PlayerStats pStats=mob.playerStats();
		if(pStats !=null)
		{
			final ItemCollection coll=pStats.getExtItems();
			final List<Item> finalCollection=new LinkedList<Item>();
			final List<Item> extraItems=new LinkedList<Item>();
			for(int i=coll.numItems()-1;i>=0;i--)
			{
				final Item thisItem=coll.getItem(i);
				if((thisItem!=null)&&(!thisItem.amDestroyed()))
				{
					final Item cont=thisItem.ultimateContainer(null);
					if(cont.owner() instanceof Room)
						finalCollection.add(thisItem);
				}
			}
			for(final Item thisItem : finalCollection)
			{
				if(thisItem instanceof Container)
				{
					final List<Item> contents=((Container)thisItem).getContents();
					for(final Item I : contents)
						if(!finalCollection.contains(I))
							extraItems.add(I);
				}
			}
			finalCollection.addAll(extraItems);
			for(final Item thisItem : finalCollection)
			{
				if(!done.contains(""+thisItem))
				{
					CMLib.catalog().updateCatalogIntegrity(thisItem);
					final Item cont=thisItem.ultimateContainer(null);
					final String sql=getDBItemUpdateString(mob,thisItem);
					final String roomID=((cont.owner()==null)&&(thisItem instanceof SpaceShip)&&(CMLib.map().isObjectInSpace((SpaceShip)thisItem)))?
							("SPACE."+CMParms.toStringList(((SpaceShip)thisItem).coordinates())):CMLib.map().getExtendedRoomID((Room)cont.owner());
					final String text="<ROOM ID=\""+roomID+"\" EXPIRE="+thisItem.expirationDate()+" />"+thisItem.text();
					strings.add(new DBPreparedBatchEntry(sql,text));
					done.add(""+thisItem);
				}
			}
		}
		return strings;
	}

	public void DBUpdateItems(MOB mob)
	{
		if(mob.Name().length()==0) return;
		final List<DBPreparedBatchEntry> statements=new LinkedList<DBPreparedBatchEntry>();
		statements.add(new DBPreparedBatchEntry("DELETE FROM CMCHIT WHERE CMUSERID='"+mob.Name()+"'"));
		statements.addAll(getDBItemUpdateStrings(mob));
		DB.updateWithClobs(statements);
	}

	protected List<Pair<String,Integer>>[][] DBFindPrideWinners(int topThisMany, short scanCPUPercent, boolean players)
	{
		@SuppressWarnings("unchecked")
		final List<Pair<String,Integer>>[][] top=new Vector[TimeClock.TimePeriod.values().length][AccountStats.PrideStat.values().length];
		for(int x=0;x<top.length;x++)
			for(int y=0;y<top[x].length;y++)
				top[x][y]=new Vector<Pair<String,Integer>>(topThisMany+1);
		DBConnection D=null;
		final long msWait=Math.round(1000.0 * CMath.div(scanCPUPercent, 100));
		final long sleepAmount=1000 - msWait;
		try
		{
			long nextWaitAfter=System.currentTimeMillis() + msWait;
			final long now=System.currentTimeMillis();

			D=DB.DBFetch();
			ResultSet R;
			if(players)
				R=D.query("SELECT CMUSERID,CMPFIL FROM CMCHAR");
			else
				R=D.query("SELECT CMANAM,CMAXML FROM CMACCT");
			while((R!=null)&&(R.next()))
			{
				final String userID=DB.getRes(R, players?"CMUSERID":"CMANAM");
				String pxml;
				final MOB M=CMLib.players().getPlayer(userID);
				if((M!=null)&&(M.playerStats()!=null))
					pxml=M.playerStats().getXML();
				else
				if(players)
					pxml=DB.getRes(R, "CMPFIL");
				else
					pxml=DB.getRes(R, "CMAXML");
				final String[] pridePeriods=CMLib.xml().returnXMLValue(pxml, "NEXTPRIDEPERIODS").split(",");
				final String[] prideStats=CMLib.xml().returnXMLValue(pxml, "PRIDESTATS").split(";");
				final Pair<Long,int[]>[] allData = CMLib.players().parsePrideStats(pridePeriods, prideStats);
				for(final TimeClock.TimePeriod period : TimeClock.TimePeriod.values())
				{
					if(allData.length > period.ordinal())
					{
						final Pair<Long,int[]> p=allData[period.ordinal()];
						final List<Pair<String,Integer>>[] topPeriods=top[period.ordinal()];
						if((period==TimeClock.TimePeriod.ALLTIME)||(now < p.first.longValue()))
						{
							for(final AccountStats.PrideStat pride : AccountStats.PrideStat.values())
							{
								if((p.second.length>pride.ordinal())&&(p.second[pride.ordinal()]>0))
								{
									final int val=p.second[pride.ordinal()];
									final List<Pair<String,Integer>> topPrides=topPeriods[pride.ordinal()];
									final int oldSize=topPrides.size();
									for(int i=0;i<topPrides.size();i++)
										if(val >= topPrides.get(i).second.intValue())
										{
											topPrides.add(i, new Pair<String,Integer>(userID,Integer.valueOf(val)));
											while(topPrides.size()>topThisMany)
												topPrides.remove(topPrides.size()-1);
											break;
										}
									if((oldSize==topPrides.size())&&(topPrides.size()<topThisMany))
										topPrides.add(new Pair<String,Integer>(userID,Integer.valueOf(val)));
								}
							}
						}
					}
				}
				if((sleepAmount>0)&&(System.currentTimeMillis() > nextWaitAfter))
				{
					CMLib.s_sleep(sleepAmount);
					nextWaitAfter=System.currentTimeMillis() + msWait;
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return top;
	}

	public List<Pair<String,Integer>>[][] DBScanPridePlayerWinners(int topThisMany, short scanCPUPercent)
	{
		return DBFindPrideWinners(topThisMany,scanCPUPercent,true);
	}

	public List<Pair<String,Integer>>[][] DBScanPrideAccountWinners(int topThisMany, short scanCPUPercent)
	{
		return DBFindPrideWinners(topThisMany,scanCPUPercent,false);
	}

	// this method is unused, but is a good idea of how to collect riders, followers, carts, etc.
	protected void addFollowerDependent(PhysicalAgent P, DVector list, String parent)
	{
		if(P==null) return;
		if(list.contains(P)) return;
		if((P instanceof MOB)
		&&((!((MOB)P).isMonster())||(((MOB)P).isPossessing())))
			return;
		CMLib.catalog().updateCatalogIntegrity(P);
		final String myCode=""+(list.size()-1);
		list.addElement(P,CMClass.classID(P)+"#"+myCode+parent);
		if(P instanceof Rideable)
		{
			final Rideable R=(Rideable)P;
			for(int r=0;r<R.numRiders();r++)
				addFollowerDependent(R.fetchRider(r),list,"@"+myCode+"R");
		}
		if(P instanceof Container)
		{
			final Container C=(Container)P;
			final List<Item> contents=C.getContents();
			for(int c=0;c<contents.size();c++)
				addFollowerDependent(contents.get(c),list,"@"+myCode+"C");
		}

	}

	public void DBUpdateFollowers(MOB mob)
	{
		if((mob==null)||(mob.Name().length()==0)) return;
		final List<DBPreparedBatchEntry> statements=new LinkedList<DBPreparedBatchEntry>();
		statements.add(new DBPreparedBatchEntry("DELETE FROM CMCHFO WHERE CMUSERID='"+mob.Name()+"'"));
		for(int f=0;f<mob.numFollowers();f++)
		{
			final MOB thisMOB=mob.fetchFollower(f);
			if((thisMOB!=null)&&(thisMOB.isMonster())&&(!thisMOB.isPossessing()))
			{
				CMLib.catalog().updateCatalogIntegrity(thisMOB);
				final String sql="INSERT INTO CMCHFO (CMUSERID, CMFONM, CMFOID, CMFOTX, CMFOLV, CMFOAB"
				+") values ('"+mob.Name()+"',"+f+",'"+CMClass.classID(thisMOB)+"',?,"
				+thisMOB.basePhyStats().level()+","+thisMOB.basePhyStats().ability()+")";
				statements.add(new DBPreparedBatchEntry(sql,thisMOB.text()+" "));
			}
		}
		DB.updateWithClobs(statements);
	}

	public void DBNameChange(String oldName, String newName)
	{
		if((oldName==null)
		||(oldName.trim().length()==0)
		||(oldName.indexOf('\'')>=0)
		||(newName==null)
		||(newName.trim().length()==0)
		||(newName.indexOf('\'')>=0))
			return;
		DB.update("UPDATE CMCHAB SET CMUSERID='"+newName+"' WHERE CMUSERID='"+oldName+"'");
		DB.update("UPDATE CMCHAR SET CMUSERID='"+newName+"' WHERE CMUSERID='"+oldName+"'");
		DB.update("UPDATE CMCHAR SET CMWORS='"+newName+"' WHERE CMWORS='"+oldName+"'");
		DB.update("UPDATE CMCHAR SET CMLEIG='"+newName+"' WHERE CMLEIG='"+oldName+"'");
		DB.update("UPDATE CMCHCL SET CMUSERID='"+newName+"' WHERE CMUSERID='"+oldName+"'");

		DB.update("UPDATE CMCHFO SET CMUSERID='"+newName+"' WHERE CMUSERID='"+oldName+"'");
		DB.update("UPDATE CMCHIT SET CMUSERID='"+newName+"' WHERE CMUSERID='"+oldName+"'");
		DB.update("UPDATE CMJRNL SET CMFROM='"+newName+"' WHERE CMFROM='"+oldName+"'");
		DB.update("UPDATE CMJRNL SET CMTONM='"+newName+"' WHERE CMTONM='"+oldName+"'");
		DB.update("UPDATE CMPDAT SET CMPLID='"+newName+"' WHERE CMPLID='"+oldName+"'");
	}

	public void DBDelete(MOB mob, boolean deleteAssets)
	{
		if(mob.Name().length()==0) return;
		final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.PLAYERPURGES);
		for(int i=0;i<channels.size();i++)
			CMLib.commands().postChannel(channels.get(i),mob.clans(),mob.Name()+" has just been deleted.",true);
		CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_PURGES);
		DB.update("DELETE FROM CMCHAR WHERE CMUSERID='"+mob.Name()+"'");
		DB.update("DELETE FROM CMCHCL WHERE CMUSERID='"+mob.Name()+"'");
		mob.delAllItems(false);
		for(int i=0;i<mob.numItems();i++)
		{
			final Item I=mob.getItem(i);
			if(I!=null) I.setContainer(null);
		}
		mob.delAllItems(false);
		DBUpdateItems(mob);
		while(mob.numFollowers()>0)
		{
			final MOB follower=mob.fetchFollower(0);
			if(follower!=null) follower.setFollowing(null);
		}
		if(deleteAssets)
		{
			DBUpdateFollowers(mob);
		}
		mob.delAllAbilities();
		DBUpdateAbilities(mob);
		if(deleteAssets)
		{
			CMLib.database().DBDeletePlayerJournals(mob.Name());
			CMLib.database().DBDeletePlayerData(mob.Name());
		}
		final PlayerStats pstats = mob.playerStats();
		if(pstats!=null)
		{
			final PlayerAccount account = pstats.getAccount();
			if(account != null)
			{
				account.delPlayer(mob);
				DBUpdateAccount(account);
				account.setLastUpdated(System.currentTimeMillis());
			}
		}
		if(deleteAssets)
		{
			for(int q=0;q<CMLib.quests().numQuests();q++)
			{
				final Quest Q=CMLib.quests().fetchQuest(q);
				if(Q.wasWinner(mob.Name()))
					Q.declareWinner("-"+mob.Name());
			}
		}
	}

	public void DBUpdateAbilities(MOB mob)
	{
		if(mob.Name().length()==0) return;
		final List<DBPreparedBatchEntry> statements=new LinkedList<DBPreparedBatchEntry>();
		statements.add(new DBPreparedBatchEntry("DELETE FROM CMCHAB WHERE CMUSERID='"+mob.Name()+"'"));
		final HashSet<String> H=new HashSet<String>();
		for(int a=0;a<mob.numAbilities();a++)
		{
			final Ability thisAbility=mob.fetchAbility(a);
			if((thisAbility!=null)&&(thisAbility.isSavable()))
			{
				int proficiency=thisAbility.proficiency();
				final Ability effectA=mob.fetchEffect(thisAbility.ID());
				if(effectA!=null)
				{
					if((effectA.isSavable())&&(!effectA.canBeUninvoked())&&(!effectA.isAutoInvoked())) proficiency=proficiency-200;
				}
				H.add(thisAbility.ID());
				final String sql="INSERT INTO CMCHAB (CMUSERID, CMABID, CMABPF,CMABTX"
				+") values ('"+mob.Name()+"','"+thisAbility.ID()+"',"+proficiency+",?)";
				statements.add(new DBPreparedBatchEntry(sql,thisAbility.text()));
			}
		}
		for(final Enumeration<Ability> a=mob.personalEffects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(!H.contains(A.ID()))&&(A.isSavable())&&(!A.canBeUninvoked()))
			{
				final String sql="INSERT INTO CMCHAB (CMUSERID, CMABID, CMABPF,CMABTX"
				+") values ('"+mob.Name()+"','"+A.ID()+"',"+Integer.MAX_VALUE+",?)";
				statements.add(new DBPreparedBatchEntry(sql,A.text()));
			}
		}
		for(final Enumeration<Behavior> e=mob.behaviors();e.hasMoreElements();)
		{
			final Behavior B=e.nextElement();
			if((B!=null)&&(B.isSavable()))
			{
				final String sql="INSERT INTO CMCHAB (CMUSERID, CMABID, CMABPF,CMABTX"
				+") values ('"+mob.Name()+"','"+B.ID()+"',"+(Integer.MIN_VALUE+1)+",?"
				+")";
				statements.add(new DBPreparedBatchEntry(sql,B.getParms()));
			}
		}
		final String scriptStuff = CMLib.coffeeMaker().getGenScripts(mob,true);
		if(scriptStuff.length()>0)
		{
			final String sql="INSERT INTO CMCHAB (CMUSERID, CMABID, CMABPF,CMABTX"
			+") values ('"+mob.Name()+"','ScriptingEngine',"+(Integer.MIN_VALUE+1)+",?"
			+")";
			statements.add(new DBPreparedBatchEntry(sql,scriptStuff));
		}

		DB.updateWithClobs(statements);
	}

	public void DBCreateCharacter(MOB mob)
	{
		if(mob.Name().length()==0) return;
		final PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;
		DB.update("INSERT INTO CMCHAR (CMUSERID, CMPASS, CMCLAS, CMRACE, CMGEND "
				+") VALUES ('"+mob.Name()+"','"+pstats.getPasswordStr()+"','"+mob.baseCharStats().getMyClassesStr()
				+"','"+mob.baseCharStats().getMyRace().ID()+"','"+((char)mob.baseCharStats().getStat(CharStats.STAT_GENDER))
				+"')");
		final PlayerAccount account = pstats.getAccount();
		if(account != null)
		{
			account.addNewPlayer(mob);
			DBUpdateAccount(account);
			account.setLastUpdated(System.currentTimeMillis());
		}
	}

	public void DBUpdateAccount(PlayerAccount account)
	{
		if(account == null) return;
		final String characters = CMParms.toSemicolonList(account.getPlayers());
		DB.updateWithClobs("UPDATE CMACCT SET CMPASS='"+account.getPasswordStr()+"',  CMCHRS=?,  CMAXML=?  WHERE CMANAM='"+account.getAccountName()+"'",
				new String[][]{{characters,account.getXML()}});
	}

	public void DBDeleteAccount(PlayerAccount account)
	{
		if(account == null) return;
		DB.update("DELETE FROM CMACCT WHERE CMANAM='"+account.getAccountName()+"'");
	}

	public void DBCreateAccount(PlayerAccount account)
	{
		if(account == null) return;
		account.setAccountName(CMStrings.capitalizeAndLower(account.getAccountName()));
		final String characters = CMParms.toSemicolonList(account.getPlayers());
		DB.updateWithClobs("INSERT INTO CMACCT (CMANAM, CMPASS, CMCHRS, CMAXML) "
				+"VALUES ('"+account.getAccountName()+"','"+account.getPasswordStr()+"',?,?)",new String[][]{{characters,account.getXML()}});
	}

	public PlayerAccount MakeAccount(String username, ResultSet R) throws SQLException
	{
		PlayerAccount account = null;
		account = (PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
		final String password=DB.getRes(R,"CMPASS");
		final String chrs=DB.getRes(R,"CMCHRS");
		final String xml=DB.getRes(R,"CMAXML");
		final Vector<String> names = new Vector<String>();
		if(chrs!=null) names.addAll(CMParms.parseSemicolons(chrs,true));
		account.setAccountName(CMStrings.capitalizeAndLower(username));
		account.setPassword(password);
		account.setPlayerNames(names);
		account.setXML(xml);
		return account;
	}

	public PlayerAccount DBReadAccount(String Login)
	{
		DBConnection D=null;
		PlayerAccount account = null;
		try
		{
			// why in the hell is this a memory scan?
			// case insensitivity from databases configured almost
			// certainly by amateurs is the answer. That, and fakedb
			// doesn't understand 'LIKE'
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMACCT WHERE CMANAM='"+CMStrings.replaceAll(CMStrings.capitalizeAndLower(Login),"\'", "n")+"'");
			if(R!=null) while(R.next())
			{
				final String username=DB.getRes(R,"CMANAM");
				if(Login.equalsIgnoreCase(username))
					account = MakeAccount(username,R);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return account;
	}

	public List<PlayerAccount> DBListAccounts(String mask)
	{
		DBConnection D=null;
		PlayerAccount account = null;
		final Vector<PlayerAccount> accounts = new Vector<PlayerAccount>();
		if(mask!=null) mask=mask.toLowerCase();
		try
		{
			// why in the hell is this a memory scan?
			// case insensitivity from databases configured almost
			// certainly by amateurs is the answer. That, and fakedb
			// doesn't understand 'LIKE'
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMACCT");
			if(R!=null) while(R.next())
			{
				final String username=DB.getRes(R,"CMANAM");
				if((mask==null)||(mask.length()==0)||(username.toLowerCase().indexOf(mask)>=0))
				{
					account = MakeAccount(username,R);
					accounts.add(account);
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return accounts;
	}

	public List<String> DBExpiredCharNameSearch(Set<String> skipNames)
	{
		DBConnection D=null;
		String buf=null;
		final List<String> expiredPlayers = new ArrayList<String>();
		final long now=System.currentTimeMillis();
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHAR");
			if(R!=null) while(R.next())
			{
				final String username=DB.getRes(R,"CMUSERID");
				if((skipNames!=null)&&(skipNames.contains(username)))
					continue;
				buf=DBConnections.getRes(R,"CMPFIL");
				final String secGrps=CMLib.xml().restoreAngleBrackets(CMLib.xml().returnXMLValue(buf,"SECGRPS"));
				final SecGroup g=CMSecurity.instance().createGroup("", CMParms.parseSemicolons(secGrps,true));
				if(g.contains(SecFlag.NOEXPIRE, false))
					continue;
				long expiration;
				if(CMLib.xml().returnXMLValue(buf,"ACCTEXP").length()>0)
					expiration=CMath.s_long(CMLib.xml().returnXMLValue(buf,"ACCTEXP"));
				else
				{
					final Calendar C=Calendar.getInstance();
					C.add(Calendar.DATE,CMProps.getIntVar(CMProps.Int.TRIALDAYS));
					expiration=C.getTimeInMillis();
				}
				if(now>=expiration)
					expiredPlayers.add(username);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return expiredPlayers;
	}

	public PlayerLibrary.ThinnerPlayer DBUserSearch(String Login)
	{
		DBConnection D=null;
		String buf=null;
		PlayerLibrary.ThinnerPlayer thinPlayer = null;
		try
		{
			// why in the hell is this a memory scan?
			// case insensitivity from databases configured almost
			// certainly by amateurs is the answer. That, and fakedb
			// doesn't understand 'LIKE'
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+CMStrings.capitalizeAndLower(Login).replace('\'', 'n')+"'");
			if(R!=null) while(R.next())
			{
				final String username=DB.getRes(R,"CMUSERID");
				thinPlayer = new PlayerLibrary.ThinnerPlayer();
				final String password=DB.getRes(R,"CMPASS");
				final String email=DB.getRes(R,"CMEMAL");
				thinPlayer.name=username;
				thinPlayer.password=password;
				thinPlayer.email=email;
				// Acct Exp Code
				buf=DBConnections.getRes(R,"CMPFIL");
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		if((buf!=null)&&(thinPlayer!=null))
		{
			PlayerAccount acct = null;
			thinPlayer.accountName = CMLib.xml().returnXMLValue(buf,"ACCOUNT");
			if((thinPlayer.accountName!=null)&&(thinPlayer.accountName.length()>0))
				acct = CMLib.players().getLoadAccount(thinPlayer.accountName);
			if((acct != null)&&(CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>1))
				thinPlayer.expiration=acct.getAccountExpiration();
			else
			if(CMLib.xml().returnXMLValue(buf,"ACCTEXP").length()>0)
				thinPlayer.expiration=CMath.s_long(CMLib.xml().returnXMLValue(buf,"ACCTEXP"));
			else
			{
				final Calendar C=Calendar.getInstance();
				C.add(Calendar.DATE,CMProps.getIntVar(CMProps.Int.TRIALDAYS));
				thinPlayer.expiration=C.getTimeInMillis();
			}
		}
		return thinPlayer;
	}

	public String[] DBFetchEmailData(String name)
	{
		final String[] data=new String[2];
		for(final Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
		{
			final MOB M=e.nextElement();
			if((M.Name().equalsIgnoreCase(name))&&(M.playerStats()!=null))
			{
				data[0]=M.playerStats().getEmail();
				data[1]=""+((M.getBitmap()&MOB.ATT_AUTOFORWARD)==MOB.ATT_AUTOFORWARD);
				return data;
			}
		}
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+name.replace('\'', 'n')+"'");
			if(R!=null) while(R.next())
			{
				// String username=DB.getRes(R,"CMUSERID");
				final int btmp=CMath.s_int(DB.getRes(R,"CMBTMP"));
				final String temail=DB.getRes(R,"CMEMAL");
				data[0]=temail;
				data[1]=""+((btmp&MOB.ATT_AUTOFORWARD)==MOB.ATT_AUTOFORWARD);
				return data;
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}

	public String DBPlayerEmailSearch(String email)
	{
		DBConnection D=null;
		for(final Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
		{
			final MOB M=e.nextElement();
			if((M.playerStats()!=null)&&(M.playerStats().getEmail().equalsIgnoreCase(email))) return M.Name();
		}
		try
		{
			D=DB.DBFetch();
			email=DB.injectionClean(email.trim());
			ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMEMAL='"+email+"'");
			if(((R==null)||(!R.next()))&&(!CMStrings.isLowerCase(email)))
				R=D.query("SELECT * FROM CMCHAR WHERE CMEMAL='"+email.toLowerCase()+"'");
			if((R==null)||(!R.next()))
				R=D.query("SELECT * FROM CMCHAR WHERE CMEMAL LIKE '"+email+"'");
			if((R==null)||(!R.next()))
				R=D.query("SELECT * FROM CMCHAR");
			if(R!=null)
				while(R.next())
				{
					final String username=DB.getRes(R,"CMUSERID");
					final String temail=DB.getRes(R,"CMEMAL");
					if(temail.equalsIgnoreCase(email))
					{
						return username;
					}
				}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}
}
