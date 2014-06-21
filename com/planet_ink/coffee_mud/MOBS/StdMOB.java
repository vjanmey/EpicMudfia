package com.planet_ink.coffee_mud.MOBS;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.FData;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Follower;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Tattoo;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.Entry;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
 Copyright 2000-2014 Bo Zimmerman

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, e\ither express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
//STOP-ECLIPSE-FORMATTING
// @formatter: off
@SuppressWarnings({ "unchecked", "rawtypes" })
public class StdMOB implements MOB
{
	@Override
	public String ID()
	{
		return "StdMOB";
	}

	public String				username		= "";

	protected CharStats			baseCharStats	= (CharStats) CMClass.getCommon("DefaultCharStats");
	protected CharStats			charStats		= (CharStats) CMClass.getCommon("DefaultCharStats");

	protected PhyStats			phyStats		= (PhyStats) CMClass.getCommon("DefaultPhyStats");
	protected PhyStats			basePhyStats	= (PhyStats) CMClass.getCommon("DefaultPhyStats");

	protected PlayerStats		playerStats		= null;

	protected boolean			amDestroyed		= false;
	protected boolean			removeFromGame	= false;
	protected boolean			amDead			= false;

	protected volatile Room		location		= null;
	protected volatile Room		lastLocation	= null;
	protected Rideable			riding			= null;

	protected volatile Session	mySession		= null;
	protected Object			description		= null;
	protected String			displayText		= "";
	protected String			rawImageName	= null;
	protected String			cachedImageName	= null;
	protected Object			miscText		= null;
	protected String[]			xtraValues		= null;

	// gained attributes
	protected int				experience		= 0;
	protected int				practices		= 0;
	protected int				trains			= 0;
	protected long				ageMinutes		= 0;
	protected int				money			= 0;
	protected double			moneyVariation	= 0.0;
	protected int				attributesBitmap= MOB.ATT_NOTEACH;
	protected String			databaseID		= "";

	protected int				tickCounter		= 0;
	protected int				recoverTickCter = 1;
	private long				expirationDate	= 0;
	private int					manaConsumeCter = CMLib.dice().roll(1, 10, 0);
	private volatile double		freeActions		= 0.0;

	// the core state values
	public CharState			curState		= (CharState) CMClass.getCommon("DefaultCharState");
	public CharState			maxState		= (CharState) CMClass.getCommon("DefaultCharState");
	public CharState			baseState		= (CharState) CMClass.getCommon("DefaultCharState");
	private long				lastTickedTime	= 0;
	private long				lastCommandTime	= System.currentTimeMillis();

	protected Room				possStartRoom   = null;
	protected String			worshipCharID	= "";
	protected String			liegeID			= "";
	protected int				wimpHitPoint	= 0;
	protected int				questPoint		= 0;
	protected MOB				victim			= null;
	protected MOB				amFollowing		= null;
	protected MOB				soulMate		= null;
	protected int				atRange			= -1;
	protected long				peaceTime		= 0;
	protected boolean			kickFlag		= false;
	protected MOB				me 				= this;

	protected int				tickStatus		= Tickable.STATUS_NOT;

	/* containers of items and attributes */
	protected 		   SVector<Item>		 	 inventory		= new SVector<Item>(1);
	protected 		   CMUniqSortSVec<Ability>	 abilitys		= new CMUniqSortSVec<Ability>(1);
	protected 		   int[]					 abilityUseTrig = new int[3];
	protected 		   STreeMap<String,int[][]>	 abilityUseCache= new STreeMap<String,int[][]>();
	protected 		   STreeMap<String,Integer>  expertises 	= new STreeMap<String,Integer>();
	protected 		   SVector<Ability>		 	 affects		= new SVector<Ability>(1);
	protected 		   CMUniqSortSVec<Behavior>	 behaviors		= new CMUniqSortSVec<Behavior>(1);
	protected 		   CMUniqSortSVec<Tattoo>	 tattoos		= new CMUniqSortSVec<Tattoo>(1);
	protected volatile SVector<Follower>	 	 followers		= null;
	protected 		   LinkedList<QMCommand> 	 commandQue		= new LinkedList<QMCommand>();
	protected 		   SVector<ScriptingEngine>	 scripts		= new SVector(1);
	protected volatile List<Ability>			 racialAffects	= null;
	protected volatile List<Ability>			 clanAffects	= null;
	protected 		   SHashtable<String, FData> factions 		= new SHashtable<String, FData>(1);
	protected volatile WeakReference<Item>		 possWieldedItem= null;
	protected volatile WeakReference<Item>	 	 possHeldItem	= null;

	protected		   OrderedMap<String,Pair<Clan,Integer>> 	clans = new OrderedMap<String,Pair<Clan,Integer>>();

	public StdMOB()
	{
		super();
		// CMClass.bumpCounter(this,CMClass.CMObjectType.MOB);//removed for mem & perf
		baseCharStats().setMyRace(CMClass.getRace("Human"));
		basePhyStats().setLevel(1);
		xtraValues = CMProps.getExtraStatCodesHolder(this);
	}

	@Override
	public long lastTickedDateTime()
	{
		return lastTickedTime;
	}

	@Override
	public void flagVariableEq()
	{
		lastTickedTime = -3;
	}

	@Override
	public long getAgeMinutes()
	{
		return ageMinutes;
	}

	@Override
	public int getPractices()
	{
		return practices;
	}

	@Override
	public int getExperience()
	{
		return experience;
	}

	@Override
	public int getExpNextLevel()
	{
		return CMLib.leveler().getLevelExperience(basePhyStats().level());
	}

	@Override
	public int getExpPrevLevel()
	{
		if (basePhyStats().level() <= 1)
			return 0;
		final int neededLowest = CMLib.leveler().getLevelExperience(basePhyStats().level() - 2);
		return neededLowest;
	}

	@Override
	public int getExpNeededDelevel()
	{
		if (basePhyStats().level() <= 1)
			return 0;
		if ((CMSecurity.isDisabled(CMSecurity.DisFlag.EXPERIENCE))
		|| (charStats().getCurrentClass().expless())
		|| (charStats().getMyRace().expless()))
			return 0;
		int ExpPrevLevel = getExpPrevLevel();
		if (ExpPrevLevel > getExperience())
			ExpPrevLevel = getExperience() - 1000;
		return getExperience() - ExpPrevLevel;
	}

	@Override
	public int getExpNeededLevel()
	{
		if ((CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL) > 0)
		&& (CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL) <= basePhyStats().level()))
			return Integer.MAX_VALUE;
		if ((CMSecurity.isDisabled(CMSecurity.DisFlag.EXPERIENCE))
		|| (charStats().getCurrentClass().expless())
		|| (charStats().getMyRace().expless()))
			return Integer.MAX_VALUE;
		int ExpNextLevel = getExpNextLevel();
		if (ExpNextLevel < getExperience())
			ExpNextLevel = getExperience() + 1000;
		return ExpNextLevel - getExperience();
	}

	@Override
	public int getTrains()
	{
		return trains;
	}

	@Override
	public int getMoney()
	{
		return money;
	}

	@Override
	public double getMoneyVariation()
	{
		return moneyVariation;
	}

	@Override
	public int getBitmap()
	{
		return attributesBitmap;
	}

	@Override
	public void setAgeMinutes(long newVal)
	{
		ageMinutes = newVal;
	}

	@Override
	public void setExperience(int newVal)
	{
		experience = newVal;
	}

	@Override
	public void setExpNextLevel(int newVal)
	{
	}

	@Override
	public void setPractices(int newVal)
	{
		practices = newVal;
	}

	@Override
	public void setTrains(int newVal)
	{
		trains = newVal;
	}

	@Override
	public void setMoney(int newVal)
	{
		money = newVal;
	}

	@Override
	public void setMoneyVariation(double newVal)
	{
		moneyVariation = newVal;
	}

	@Override
	public void setBitmap(int newVal)
	{
		attributesBitmap = newVal;
	}

	@Override
	public String getFactionListing()
	{
		final StringBuffer msg = new StringBuffer();
		for (final Enumeration e = fetchFactions(); e.hasMoreElements();)
		{
			final Faction F = CMLib.factions().getFaction((String) e.nextElement());
			if(F!=null)
				msg.append(F.name() + "(" + fetchFaction(F.factionID()) + ");");
		}
		return msg.toString();
	}

	@Override
	public String getLiegeID()
	{
		return liegeID;
	}

	@Override
	public String getWorshipCharID()
	{
		return worshipCharID;
	}

	@Override
	public int getWimpHitPoint()
	{
		return wimpHitPoint;
	}

	@Override
	public int getQuestPoint()
	{
		return questPoint;
	}

	@Override
	public void setLiegeID(String newVal)
	{
		liegeID = newVal;
	}

	@Override
	public void setWorshipCharID(String newVal)
	{
		worshipCharID = newVal;
	}

	@Override
	public void setWimpHitPoint(int newVal)
	{
		wimpHitPoint = newVal;
	}

	@Override
	public void setQuestPoint(int newVal)
	{
		questPoint = newVal;
	}

	@Override
	public Deity getMyDeity()
	{
		if (getWorshipCharID().length() == 0)
			return null;
		final Deity bob = CMLib.map().getDeity(getWorshipCharID());
		if (bob == null)
			setWorshipCharID("");
		return bob;
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch (final Exception e)
		{
			Log.errOut(ID(), e);
		}
		return new StdMOB();
	}

	@Override
	public Room getStartRoom()
	{
		return CMLib.map().getRoom(possStartRoom);
	}

	@Override
	public void setStartRoom(Room room)
	{
		possStartRoom = room;
	}

	@Override
	public void setDatabaseID(String id)
	{
		databaseID = id;
	}

	@Override
	public boolean canSaveDatabaseID()
	{
		return true;
	}

	@Override
	public String databaseID()
	{
		return databaseID;
	}

	@Override
	public String Name()
	{
		return username;
	}

	@Override
	public void setName(String newName)
	{
		username = newName;
	}

	@Override
	public String name()
	{
		if (phyStats().newName() != null)
			return phyStats().newName();
		return username;
	}

	@Override
	public String titledName()
	{
		if ((playerStats == null) || (playerStats.getTitles().size() == 0))
			return name();
		return CMStrings.replaceAll(playerStats.getActiveTitle(), "*", Name());
	}

	@Override
	public String genericName()
	{
		if ((charStats().getStat(CharStats.STAT_AGE) > 0)&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.ALL_AGEING)))
			return charStats().ageName().toLowerCase() + " " + charStats().raceName().toLowerCase();
		return charStats().raceName().toLowerCase();
	}

	@Override
	public String image()
	{
		if (cachedImageName == null)
		{
			if ((rawImageName != null) && (rawImageName.length() > 0))
				cachedImageName = rawImageName;
			else
				cachedImageName = CMLib.protocol().getDefaultMXPImage(this);
		}
		if (!baseCharStats().getMyRace().name().equalsIgnoreCase(charStats().raceName()))
			return CMLib.protocol().getDefaultMXPImage(this);
		if (cachedImageName == null)
			return "";
		return cachedImageName;
	}

	@Override
	public String rawImage()
	{
		if (rawImageName == null)
			return "";
		return rawImageName;
	}

	@Override
	public void setImage(String newImage)
	{
		if ((newImage == null) || (newImage.trim().length() == 0))
			rawImageName = null;
		else
			rawImageName = newImage;
		if ((cachedImageName != null) && (!cachedImageName.equals(newImage)))
			cachedImageName = null;
	}

	@Override
	public long expirationDate()
	{
		return expirationDate;
	}

	@Override
	public void setExpirationDate(long time)
	{
		expirationDate = time;
	}

	// protected void finalize()
	// CMClass.unbumpCounter(this,CMClass.CMObjectType.MOB); }//removed for mem
	// & perf
	@Override
	public final boolean amDestroyed()
	{
		return this.amDestroyed;
	}

	protected void cloneFix(MOB M)
	{
		if (M == null)
			return;
		me=this;
		if (!isGeneric())
		{
			final PhyStats oldBase=(PhyStats)basePhyStats.copyOf();
			M.basePhyStats().copyInto(basePhyStats);
			basePhyStats.setAbility(oldBase.ability());
			basePhyStats.setRejuv(oldBase.rejuv());
			basePhyStats.setLevel(oldBase.level());
			M.phyStats().copyInto(phyStats);
			phyStats.setAbility(oldBase.ability());
			phyStats.setRejuv(oldBase.rejuv());
			phyStats.setLevel(oldBase.level());
		}
		else
		{
			basePhyStats = (PhyStats) M.basePhyStats().copyOf();
			phyStats = (PhyStats) M.phyStats().copyOf();
		}
		affects	= new SVector<Ability>();
		baseCharStats = (CharStats) M.baseCharStats().copyOf();
		charStats = (CharStats) M.charStats().copyOf();
		baseState = (CharState) M.baseState().copyOf();
		curState = (CharState) M.curState().copyOf();
		maxState = (CharState) M.maxState().copyOf();
		removeFromGame = false;

		inventory= new SVector<Item>();
		abilitys= new CMUniqSortSVec<Ability>();
		abilityUseTrig = new int[3];
		abilityUseCache= new STreeMap<String,int[][]>();
		behaviors= new CMUniqSortSVec<Behavior>();
		tattoos	= new CMUniqSortSVec<Tattoo>();
		expertises = new STreeMap<String,Integer>();
		followers = null;
		commandQue = new LinkedList<QMCommand>();
		scripts	= new SVector();
		racialAffects = null;
		clanAffects	= null;
		factions = new SHashtable<String, FData>(1);
		possWieldedItem= null;
		possHeldItem = null;
		clans.clear();

		for(final Pair<Clan,Integer> p : M.clans())
		{
			setClan(p.first.clanID(), p.second.intValue());
		}
		for(final Enumeration<String> e=M.fetchFactions();e.hasMoreElements();)
		{
			final String fac=e.nextElement();
			addFaction(fac, M.fetchFaction(fac));
		}
		for(final Enumeration<Tattoo> e=M.tattoos();e.hasMoreElements();)
		{
			final Tattoo t=e.nextElement();
			addTattoo(t.copyOf());
		}
		for(final Enumeration<String> s=M.expertises();s.hasMoreElements();)
			addExpertise(s.nextElement());

		Item I = null;
		for (int i = 0; i < M.numItems(); i++)
		{
			I = M.getItem(i);
			if (I != null)
				addItem((Item) I.copyOf());
		}
		Item I2 = null;
		for (int i = 0; i < numItems(); i++)
		{
			I = getItem(i);
			if ((I != null)
			&& (I.container() != null)
			&& (!isMine(I.container())))
				for (final Enumeration<Item> e = M.items(); e.hasMoreElements();)
				{
					I2 = e.nextElement();
					if ((I2 == I.container()) && (I2 instanceof Container))
					{
						I.setContainer((Container) I2);
						break;
					}
				}
		}
		Ability A = null;
		for (int i = 0; i < M.numAbilities(); i++)
		{
			A = M.fetchAbility(i);
			if (A != null)
				addAbility((Ability) A.copyOf());
		}
		for (final Enumeration<Ability> a = M.personalEffects(); a.hasMoreElements();)
		{
			A = a.nextElement();
			if (A != null)
			{
				A = (Ability) A.copyOf();
				addEffect(A);
				if (A.canBeUninvoked())
				{
					A.unInvoke();
					delEffect(A);
				}
			}
		}
		for (final Enumeration<Behavior> e = M.behaviors(); e.hasMoreElements();)
		{
			final Behavior B = e.nextElement();
			if (B != null) // iteration during a clone would just be messed up.
				behaviors.addElement((Behavior) B.copyOf());
		}
		ScriptingEngine SE = null;
		for (final Enumeration<ScriptingEngine> e = M.scripts(); e.hasMoreElements();)
		{
			SE = e.nextElement();
			if (SE != null)
				addScript((ScriptingEngine) SE.copyOf());
		}
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final StdMOB E = (StdMOB) this.clone();
			// CMClass.bumpCounter(E,CMClass.CMObjectType.MOB);//removed for mem
			// & perf
			E.xtraValues = (xtraValues == null) ? null : (String[]) xtraValues.clone();
			E.cloneFix(this);
			CMLib.catalog().newInstance(this);
			return E;
		}
		catch (final CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	@Override
	public boolean isGeneric()
	{
		return false;
	}

	@Override
	public PhyStats phyStats()
	{
		return phyStats;
	}

	@Override
	public PhyStats basePhyStats()
	{
		return basePhyStats;
	}

	private final EachApplicable<Item> recoverPhyStatsItemApplier=new EachApplicable<Item>()
	{
		@Override
		public final void apply(final Item I)
		{
			I.recoverPhyStats();
			I.affectPhyStats(me, phyStats);
		}
	};
	private final EachApplicable<Ability> recoverPhyStatsAffectApplier=new EachApplicable<Ability>()
	{
		@Override
		public final void apply(final Ability A)
		{
			A.affectPhyStats(me, phyStats);
		}
	};

	@Override
	public void recoverPhyStats()
	{
		basePhyStats.copyInto(phyStats);
		if (location() != null)
			location().affectPhyStats(this, phyStats);
		if(getMoney()>0)
			phyStats().setWeight(phyStats().weight() + (int) Math.round(CMath.div(getMoney(), 100.0)));
		final Rideable riding = riding();
		if (riding != null)
			riding.affectPhyStats(this, phyStats);
		final Deity deity = getMyDeity();
		if (deity != null)
			deity.affectPhyStats(this, phyStats);
		final CharStats cStats = charStats;
		if (cStats != null)
		{
			for (int c = 0; c < cStats.numClasses(); c++)
				cStats.getMyClass(c).affectPhyStats(this, phyStats);
			cStats.getMyRace().affectPhyStats(this, phyStats);
		}
		eachItem(recoverPhyStatsItemApplier);
		eachEffect(recoverPhyStatsAffectApplier);
		for (final Enumeration e = factions.elements(); e.hasMoreElements();)
			((Faction.FData) e.nextElement()).affectPhyStats(this, phyStats);
		/* the follower light exception */
		if (!CMLib.flags().isLightSource(this))
		{
			for (final Enumeration<Follower> f = followers(); f.hasMoreElements();)
				if (CMLib.flags().isLightSource(f.nextElement().follower))
					phyStats.setDisposition(phyStats().disposition() | PhyStats.IS_LIGHTSOURCE);
		}
	}

	@Override
	public void setBasePhyStats(PhyStats newStats)
	{
		basePhyStats = (PhyStats) newStats.copyOf();
	}

	@Override
	public int baseWeight()
	{
		if (charStats().getMyRace() == baseCharStats().getMyRace())
			return basePhyStats().weight() + charStats().getStat(CharStats.STAT_WEIGHTADJ);
		return charStats().getMyRace().lightestWeight() + charStats().getStat(CharStats.STAT_WEIGHTADJ)
				+ charStats().getMyRace().weightVariance();
	}

	@Override
	public int maxCarry()
	{
		if (CMSecurity.isAllowed(this, location(), CMSecurity.SecFlag.CARRYALL))
			return Integer.MAX_VALUE / 2;
		final double str = charStats().getStat(CharStats.STAT_STRENGTH);
		final double bodyWeight = baseWeight();
		return (int) Math.round(bodyWeight + ((str + 10.0) * str * bodyWeight / 150.0) + (str * 5.0));
	}

	@Override
	public int maxItems()
	{
		if (CMSecurity.isAllowed(this, location(), CMSecurity.SecFlag.CARRYALL))
			return Integer.MAX_VALUE / 2;
		return (2 * Wearable.CODES.TOTAL()) + (2 * charStats().getStat(CharStats.STAT_DEXTERITY))
				+ (2 * phyStats().level());
	}

	@Override
	public int maxFollowers()
	{
		return ((int) Math.round(CMath.div(charStats().getStat(CharStats.STAT_CHARISMA) - 8, 4.0)) + 1);
	}

	@Override
	public int totalFollowers()
	{
		int total = 0;
		try
		{
			for (final Enumeration<Follower> f = followers(); f.hasMoreElements();)
				total += f.nextElement().follower.totalFollowers();
		}
		catch (final Exception t){}
		return total;
	}

	@Override
	public CharStats baseCharStats()
	{
		return baseCharStats;
	}

	@Override
	public CharStats charStats()
	{
		return charStats;
	}

	private final EachApplicable<Item> recoverCharStatsItemApplier=new EachApplicable<Item>()
	{
		@Override
		public final void apply(final Item I)
		{
			I.affectCharStats(me, charStats);
		}
	};
	private final EachApplicable<Ability> recoverCharStatsAffectApplier=new EachApplicable<Ability>()
	{
		@Override
		public final void apply(final Ability A)
		{
			A.affectCharStats(me, charStats);
		}
	};

	@Override
	public void recoverCharStats()
	{
		baseCharStats.setClassLevel(baseCharStats.getCurrentClass(), basePhyStats().level()
				- baseCharStats().combinedSubLevels());
		baseCharStats().copyInto(charStats);

		final Rideable riding = riding();
		if (riding != null)
			riding.affectCharStats(this, charStats);
		final Deity deity = getMyDeity();
		if (deity != null)
			deity.affectCharStats(this, charStats);

		final int num = charStats.numClasses();
		for (int c = 0; c < num; c++)
			charStats.getMyClass(c).affectCharStats(this, charStats);
		charStats.getMyRace().affectCharStats(this, charStats);
		baseCharStats.getMyRace().agingAffects(this, baseCharStats, charStats);
		eachEffect(recoverCharStatsAffectApplier);
		eachItem(recoverCharStatsItemApplier);
		if (location() != null)
			location().affectCharStats(this, charStats);
		for (final Enumeration e = factions.elements(); e.hasMoreElements();)
			((Faction.FData) e.nextElement()).affectCharStats(this, charStats);
		if ((playerStats != null) && (soulMate == null) && (playerStats.getHygiene() >= PlayerStats.HYGIENE_DELIMIT))
		{
			final int chaAdjust = (int) (playerStats.getHygiene() / PlayerStats.HYGIENE_DELIMIT);
			if ((charStats.getStat(CharStats.STAT_CHARISMA) / 2) > chaAdjust)
				charStats.setStat(CharStats.STAT_CHARISMA, charStats.getStat(CharStats.STAT_CHARISMA) - chaAdjust);
			else
				charStats.setStat(CharStats.STAT_CHARISMA, charStats.getStat(CharStats.STAT_CHARISMA) / 2);
		}
	}

	@Override
	public void setBaseCharStats(CharStats newBaseCharStats)
	{
		baseCharStats = (CharStats) newBaseCharStats.copyOf();
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		if (affected instanceof Room)
		{
			if (CMLib.flags().isLightSource(this))
			{
				if (CMLib.flags().isInDark(affected))
					affectableStats.setDisposition(affectableStats.disposition() - PhyStats.IS_DARK);
				affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_LIGHTSOURCE);
			}
		}
	}

	@Override
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{
	}

	@Override
	public boolean isMarriedToLiege()
	{
		if (getLiegeID().length() == 0)
			return false;
		if (getLiegeID().equals(Name()))
			return false;
		final MOB M = CMLib.players().getLoadPlayer(getLiegeID());
		if (M == null)
		{
			setLiegeID("");
			return false;
		}
		if (M.getLiegeID().equals(Name()))
			return true;
		return false;
	}

	@Override
	public CharState curState()
	{
		return curState;
	}

	@Override
	public CharState maxState()
	{
		return maxState;
	}

	@Override
	public CharState baseState()
	{
		return baseState;
	}

	@Override
	public PlayerStats playerStats()
	{
		if ((playerStats == null) && (soulMate != null))
			return soulMate.playerStats();
		return playerStats;
	}

	@Override
	public void setPlayerStats(PlayerStats newStats)
	{
		playerStats = newStats;
	}

	@Override
	public void setBaseState(CharState newState)
	{
		baseState = (CharState) newState.copyOf();
		maxState = (CharState) newState.copyOf();
	}

	@Override
	public void resetToMaxState()
	{
		recoverMaxState();
		maxState.copyInto(curState);
	}

	private final EachApplicable<Item> recoverMaxStateItemApplier=new EachApplicable<Item>()
	{
		@Override
		public final void apply(final Item I)
		{
			I.affectCharState(me, maxState);
		}
	};
	private final EachApplicable<Ability> recoverMaxStateAffectApplier=new EachApplicable<Ability>()
	{
		@Override
		public final void apply(final Ability A)
		{
			A.affectCharState(me, maxState);
		}
	};

	@Override
	public void recoverMaxState()
	{
		baseState.copyInto(maxState);
		if (charStats.getMyRace() != null)
			charStats.getMyRace().affectCharState(this, maxState);
		final Rideable riding = riding();
		if (riding != null)
			riding.affectCharState(this, maxState);
		final int num = charStats.numClasses();
		for (int c = 0; c < num; c++)
			charStats.getMyClass(c).affectCharState(this, maxState);
		eachEffect(recoverMaxStateAffectApplier);
		eachItem(recoverMaxStateItemApplier);
		for (final Enumeration e = factions.elements(); e.hasMoreElements();)
			((Faction.FData) e.nextElement()).affectCharState(this, maxState);
		if (location() != null)
			location().affectCharState(this, maxState);
	}

	@Override
	public boolean amDead()
	{
		return amDead || removeFromGame;
	}

	@Override
	public boolean amActive()
	{
		return !removeFromGame;
	}

	@Override
	public void dispossess(boolean giveMsg)
	{
		final MOB mate = soulMate();
		if (mate == null)
			return;
		if (mate.soulMate() != null)
			mate.dispossess(giveMsg);
		final Session s = session();
		if (s != null)
		{
			s.setMob(mate);
			mate.setSession(s);
			setSession(null);
			if (giveMsg)
				CMLib.commands().postLook(mate, true);
			setSoulMate(null);
		}
	}

	@Override
	public void destroy()
	{
		CMLib.map().registerWorldObjectDestroyed(null, getStartRoom(), this);
		try
		{
			CMLib.catalog().changeCatalogUsage(this, false);
		}
		catch (final Exception t){}
		if ((CMSecurity.isDebugging(CMSecurity.DbgFlag.MISSINGKIDS))
		&& (fetchEffect("Age") != null)
		&& CMath.isInteger(fetchEffect("Age").text())
		&& (CMath.s_long(fetchEffect("Age").text()) > Short.MAX_VALUE))
			Log.debugOut("MISSKIDS", new Exception(Name() + " went missing form " + CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(this))));
		if (soulMate() != null)
			dispossess(false);
		final MOB possessor = CMLib.utensils().getMobPossessingAnother(this);
		if (possessor != null)
			possessor.dispossess(false);
		if (session() != null)
		{
			session().stopSession(false, false, false);
			CMLib.s_sleep(1000);
		}
		if(playerStats!=null)
			CMLib.players().changePlayersLocation(this,null);
		removeFromGame(session() != null, true);
		delAllBehaviors();
		delAllEffects(false);
		delAllAbilities();
		delAllItems(true);
		delAllExpertises();
		delAllScripts();
		if (kickFlag)
			CMLib.threads().deleteTick(this, -1);
		kickFlag = false;
		clans.clear();
		clanAffects=null;
		charStats = baseCharStats;
		phyStats = basePhyStats;
		playerStats = null;
		location = null;
		lastLocation = null;
		riding = null;
		mySession = null;
		rawImageName = null;
		cachedImageName = null;
		inventory.setSize(0);
		followers = null;
		abilitys.setSize(0);
		abilityUseCache.clear();
		affects.setSize(0);
		behaviors.setSize(0);
		tattoos.setSize(0);
		expertises.clear();
		factions.clear();
		commandQue.clear();
		scripts.setSize(0);
		curState = maxState;
		worshipCharID = "";
		liegeID = "";
		victim = null;
		amFollowing = null;
		soulMate = null;
		possStartRoom = null;
		amDestroyed = true;
	}

	@Override
	public void removeFromGame(boolean preserveFollowers, boolean killSession)
	{
		removeFromGame = true;
		if ((location != null) && (location.isInhabitant(this)))
		{
			location().delInhabitant(this);
			if ((session() != null) && (!CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN)))
				location().showOthers(this, null, CMMsg.MSG_OK_ACTION, _("<S-NAME> vanish(es) in a puff of smoke."));
		}
		if(playerStats!=null)
			CMLib.players().changePlayersLocation(this,null);
		setFollowing(null);
		final PairVector<MOB,Integer> oldFollowers = new PairVector<MOB,Integer>();
		while (numFollowers() > 0)
		{
			final MOB follower = fetchFollower(0);
			if (follower != null)
			{
				if ((follower.isMonster()) && (!follower.isPossessing()))
					oldFollowers.addElement(follower, Integer.valueOf(fetchFollowerOrder(follower)));
				follower.setFollowing(null);
				delFollower(follower);
			}
		}

		if (preserveFollowers)
		{
			for (int f = 0; f < oldFollowers.size(); f++)
			{
				final MOB follower = oldFollowers.getFirst(f);
				if (follower.location() != null)
				{
					final MOB newFol = (MOB) follower.copyOf();
					newFol.basePhyStats().setRejuv(PhyStats.NO_REJUV);
					newFol.text();
					follower.killMeDead(false);
					addFollower(newFol, oldFollowers.getSecond(f).intValue());
				}
			}
			if (killSession && (session() != null))
				session().stopSession(false, false, false);
		}
		setRiding(null);
	}

	@Override
	public void bringToLife()
	{
		amDead = false;
		removeFromGame = false;

		// will ensure no duplicate ticks, this obj, this id
		kickFlag = true;
		CMLib.threads().startTickDown(this, Tickable.TICKID_MOB, 1);
		if (tickStatus == Tickable.STATUS_NOT)
		{
			final boolean isImMobile=CMath.bset(phyStats.sensesMask(), PhyStats.CAN_NOT_MOVE);
			try
			{
				phyStats.setSensesMask(phyStats.sensesMask()|PhyStats.CAN_NOT_MOVE);
				tick(this, Tickable.TICKID_MOB); // slap on the butt
			}
			finally
			{
				phyStats.setSensesMask(CMath.dobit(phyStats.sensesMask(),PhyStats.CAN_NOT_MOVE,isImMobile));
			}
		}
	}

	@Override
	public void bringToLife(Room newLocation, boolean resetStats)
	{
		amDead = false;
		if ((miscText != null) && (resetStats) && (isGeneric()))
		{
			if (CMProps.getBoolVar(CMProps.Bool.MOBCOMPRESS) && (miscText instanceof byte[]))
				CMLib.coffeeMaker().resetGenMOB(this,
						CMLib.coffeeMaker().getGenMOBTextUnpacked(this,
								CMLib.encoder().decompressString((byte[]) miscText)));
			else
			CMLib.coffeeMaker().resetGenMOB(this,
					CMLib.coffeeMaker().getGenMOBTextUnpacked(this, CMStrings.bytesToStr(miscText)));
		}
		if (CMLib.map().getStartRoom(this) == null)
			setStartRoom(isMonster() ? newLocation : CMLib.login().getDefaultStartRoom(this));
		setLocation(newLocation);
		if (location() == null)
		{
			setLocation(CMLib.map().getStartRoom(this));
			if (location() == null)
			{
				Log.errOut("StdMOB", username + " cannot get a location.");
				return;
			}
		}
		if (!location().isInhabitant(this))
			location().addInhabitant(this);
		removeFromGame = false;

		if(session()!=null)
		{
			final Area area=CMLib.map().areaLocation(location());
			if(area!=null)
				CMLib.login().moveSessionToCorrectThreadGroup(session(), area.getTheme());
		}

		// will ensure no duplicate ticks, this obj, this id
		kickFlag = true;
		CMLib.threads().startTickDown(this, Tickable.TICKID_MOB, 1);

		Ability A = null;
		for (int a = 0; a < numAbilities(); a++)
		{
			A = fetchAbility(a);
			if (A != null)
				A.autoInvocation(this);
		}
		if (location() == null)
		{
			Log.errOut("StdMOB", name() + " of " + CMLib.map().getExtendedRoomID(newLocation) + " was auto-destroyed!");
			destroy();
			return;
		}
		CMLib.factions().updatePlayerFactions(this, location());
		if (tickStatus == Tickable.STATUS_NOT)
		{
			final boolean isImMobile=CMath.bset(phyStats.sensesMask(), PhyStats.CAN_NOT_MOVE);
			try
			{
				phyStats.setSensesMask(phyStats.sensesMask()|PhyStats.CAN_NOT_MOVE);
				tick(this, Tickable.TICKID_MOB); // slap on the butt
			}
			catch (final Exception t)
			{
				t.printStackTrace();
			}
			finally
			{
				phyStats.setSensesMask(CMath.dobit(phyStats.sensesMask(), PhyStats.CAN_NOT_MOVE, isImMobile));
			}
		}
		if (location() == null)
		{
			Log.errOut("StdMOB", name() + " of " + CMLib.map().getExtendedRoomID(newLocation)
					+ " was auto-destroyed by its tick!!");
			destroy();
			return;
		}

		location().recoverRoomStats();
		if ((!isGeneric()) && (resetStats))
		{
			resetToMaxState();
		}

		if (location() == null)
		{
			Log.errOut("StdMOB", name() + " of " + CMLib.map().getExtendedRoomID(newLocation)
					+ " was auto-destroyed by its room recover!!");
			destroy();
			return;
		}

		if (isMonster())
		{
			final Item dropItem = CMLib.catalog().getDropItem(this, true);
			if (dropItem != null)
				addItem(dropItem);
		}

		CMLib.map().registerWorldObjectLoaded(null, getStartRoom(), this);
		location().show(this, null, CMMsg.MSG_BRINGTOLIFE, null);
		if (CMLib.flags().isSleeping(this))
			tell(_("(You are asleep)"));
		else
			CMLib.commands().postLook(this, true);
		inventory.trimToSize();
		abilitys.trimToSize();
		affects.trimToSize();
		behaviors.trimToSize();
	}

	@Override
	public boolean isInCombat()
	{
		if (victim == null)
			return false;
		try
		{
			final Room vicR = victim.location();
			if ((vicR == null) || (location() == null) || (vicR != location()) || (victim.amDead()))
			{
				if ((victim instanceof StdMOB) && (((StdMOB) victim).victim == this))
					victim.setVictim(null);
				setVictim(null);
				return false;
			}
			return true;
		}
		catch (final NullPointerException n){}
		return false;
	}

	protected boolean isEitherOfUsDead(final MOB mob)
	{
		if (location() == null)
			return true;
		if (mob.location() == null)
			return true;
		if (mob.amDead())
			return true;
		if (mob.curState().getHitPoints() <= 0)
			return true;
		if (amDead())
			return true;
		if (curState().getHitPoints() <= 0)
			return true;
		return false;
	}

	protected boolean isPermissableToFight(final MOB mob)
	{
		if (mob == null)
			return false;
		final boolean targetIsMonster = mob.isMonster();
		final boolean iAmMonster = isMonster();
		if (targetIsMonster)
		{
			final MOB fol = mob.amFollowing();
			if ((fol != null) && (!isEitherOfUsDead(fol)))
				if (!isPermissableToFight(fol))
					return false;
		}
		if (iAmMonster)
		{
			final MOB fol = amFollowing();
			if ((fol != null) && (!isEitherOfUsDead(fol)))
				if (!fol.mayIFight(mob))
					return false;
		}
		if (CMLib.flags().isUnattackable(mob))
			return false;
		if (targetIsMonster || iAmMonster)
			return true;
		if ((mob.soulMate() != null) || (soulMate() != null))
			return true;
		if (mob == this)
			return true;
		if (CMProps.getVar(CMProps.Str.PKILL).startsWith("ALWAYS"))
			return true;
		if (CMProps.getVar(CMProps.Str.PKILL).startsWith("NEVER"))
			return false;
		if (CMLib.clans().isAtClanWar(this, mob))
			return true;
		if (CMath.bset(getBitmap(), MOB.ATT_PLAYERKILL))
		{
			if (CMSecurity.isAllowed(this, location(), CMSecurity.SecFlag.PKILL) || (CMath.bset(mob.getBitmap(), MOB.ATT_PLAYERKILL)))
				return true;
			return false;
		}
		else
		if (CMath.bset(mob.getBitmap(), MOB.ATT_PLAYERKILL))
		{
			if (CMSecurity.isAllowed(mob, location(), CMSecurity.SecFlag.PKILL) || (CMath.bset(getBitmap(), MOB.ATT_PLAYERKILL)))
				return true;
			return false;
		}
		else
			return false;
	}

	@Override
	public boolean mayIFight(final MOB mob)
	{
		if (mob == null)
			return false;
		if (isEitherOfUsDead(mob))
			return false;
		return isPermissableToFight(mob);
	}

	@Override
	public boolean mayPhysicallyAttack(MOB mob)
	{
		if ((!mayIFight(mob))
		|| (location() != mob.location())
		|| (!CMLib.flags().isInTheGame(this, false))
		|| (!CMLib.flags().isInTheGame(mob, false)))
			return false;
		return true;
	}

	@Override
	public void setAtRange(int newRange)
	{
		atRange = newRange;
	}

	@Override
	public int rangeToTarget()
	{
		return atRange;
	}

	@Override
	public int maxRange()
	{
		return maxRange(null);
	}

	@Override
	public int minRange()
	{
		return maxRange(null);
	}

	@Override
	public int maxRange(Environmental tool)
	{
		int max = 0;
		if (tool != null)
			max = tool.maxRange();
		if ((location() != null) && (location().maxRange() < max))
			max = location().maxRange();
		return max;
	}

	@Override
	public int minRange(Environmental tool)
	{
		if (tool != null)
			return tool.minRange();
		return 0;
	}

	@Override
	public void makePeace()
	{
		final MOB myVictim = victim;
		setVictim(null);
		for (int f = 0; f < numFollowers(); f++)
		{
			final MOB M = fetchFollower(f);
			if ((M != null) && (M.isInCombat()))
				M.makePeace();
		}
		if (myVictim != null)
		{
			final MOB oldVictim = myVictim.getVictim();
			if (oldVictim == this)
				myVictim.makePeace();
		}
	}

	@Override
	public MOB getVictim()
	{
		if (!isInCombat())
			return null;
		return victim;
	}

	@Override
	public void setVictim(MOB mob)
	{
		if (mob == null)
		{
			setAtRange(-1);
			if (victim != null)
				synchronized (commandQue)
				{
					commandQue.clear();
				}
		}
		if (victim == mob)
			return;
		if (mob == this)
			return;
		victim = mob;
		recoverPhyStats();
		recoverCharStats();
		recoverMaxState();
		if (mob != null)
		{
			if ((mob.location() == null) || (location() == null) || (mob.amDead()) || (amDead())
			|| (mob.location() != location()) || (!location().isInhabitant(this))
			|| (!location().isInhabitant(mob)))
			{
				if (victim != null)
					victim.setVictim(null);
				victim = null;
				setAtRange(-1);
			}
			else
			{
				if (Log.combatChannelOn())
				{
					final Item I = fetchWieldedItem();
					final Item VI = mob.fetchWieldedItem();
					Log.combatOut("STRT", Name() + ":" + phyStats().getCombatStats() + ":"
							+ curState().getCombatStats() + ":" + ((I == null) ? "null" : I.name()) + ":" + mob.Name()
							+ ":" + mob.phyStats().getCombatStats() + ":" + mob.curState().getCombatStats() + ":"
							+ ((VI == null) ? "null" : VI.name()));

				}
				mob.recoverCharStats();
				mob.recoverPhyStats();
				mob.recoverMaxState();
			}
		}
	}

	@Override
	public DeadBody killMeDead(boolean createBody)
	{
		final Room deathRoom;
		if (isMonster())
			deathRoom = location();
		else
			deathRoom = CMLib.login().getDefaultBodyRoom(this);
		if (location() != null)
			location().delInhabitant(this);
		DeadBody Body = null;
		if (createBody)
		{
			Body = charStats().getMyRace().getCorpseContainer(this, deathRoom);
			if ((Body != null) && (playerStats() != null))
				playerStats().getExtItems().addItem(Body);
		}
		amDead = true;
		makePeace();
		setRiding(null);
		synchronized (commandQue)
		{
			commandQue.clear();
		}
		Ability A = null;
		for (int a = numEffects() - 1; a >= 0; a--)
		{
			A = fetchEffect(a);
			if (A != null)
				A.unInvoke();
		}
		setLocation(null);
		if (isMonster())
		{
			while (numFollowers() > 0)
			{
				final MOB follower = fetchFollower(0);
				if (follower != null)
				{
					follower.setFollowing(null);
					delFollower(follower);
				}
			}
			setFollowing(null);
		}
		if ((!isMonster()) && (soulMate() == null))
			bringToLife(CMLib.login().getDefaultDeathRoom(this), true);
		if (deathRoom != null)
			deathRoom.recoverRoomStats();
		return Body;
	}

	@Override
	public Room location()
	{
		if (location == null)
			return lastLocation;
		return location;
	}

	@Override
	public void setLocation(Room newRoom)
	{
		lastLocation = location;
		location = newRoom;
		if((playerStats != null) && (lastLocation != newRoom))
			CMLib.players().changePlayersLocation(this,newRoom);
	}

	@Override
	public Rideable riding()
	{
		return riding;
	}

	@Override
	public void setRiding(Rideable ride)
	{
		final Rideable amRiding = riding();
		if ((ride != null) && (amRiding != null) && (amRiding == ride) && (amRiding.amRiding(this)))
			return;
		if ((amRiding != null) && (amRiding.amRiding(this)))
			amRiding.delRider(this);
		riding = ride;
		if ((ride != null) && (!ride.amRiding(this)))
			ride.addRider(this);
	}

	@Override
	public final Session session()
	{
		return mySession == null ? null : mySession.isFake() ? null : mySession;
	}

	@Override
	public void setSession(Session newSession)
	{
		mySession = newSession;
		setBitmap(getBitmap());
	}

	@Override
	public Weapon myNaturalWeapon()
	{
		final Weapon W;
		if ((charStats() != null) && (charStats().getMyRace() != null))
			W = charStats().getMyRace().myNaturalWeapon();
		else
			W = CMClass.getWeapon("Natural");
		if (W.subjectToWearAndTear())
			W.setUsesRemaining(100);
		return W;
	}

	@Override
	public String name(MOB viewer)
	{
		if (CMProps.getBoolVar(CMProps.Bool.INTRODUCTIONSYSTEM) && (playerStats() != null) && (viewer != null)
		&& (viewer.playerStats() != null) && (!viewer.playerStats().isIntroducedTo(Name())))
			return CMLib.english().startWithAorAn(genericName()).toLowerCase();
		return name();
	}

	@Override
	public String displayText(MOB viewerMob)
	{
		if ((displayText.length() == 0)
		|| (!name(viewerMob).equals(Name()))
		|| (!titledName().equals(Name()))
		|| (CMLib.flags().isSleeping(this))
		|| (CMLib.flags().isSitting(this))
		|| (riding() != null)
		|| ((amFollowing() != null) && (amFollowing().fetchFollowerOrder(this) > 0))
		|| ((this instanceof Rideable)
				&& (((Rideable) this).numRiders() > 0)
				&& CMLib.flags().hasSeenContents(this))
		|| (isInCombat()))
		{
			StringBuffer sendBack = null;
			if (!name(viewerMob).equals(Name()))
				sendBack = new StringBuffer(name(viewerMob));
			else
				sendBack = new StringBuffer(titledName());
			sendBack.append(" ");
			sendBack.append(_(CMLib.flags().dispositionString(this, CMFlagLibrary.flag_is)+" here"));
			if (riding() != null)
			{
				sendBack.append(" " + riding().stateString(this) + " ");
				if (riding() == viewerMob)
					sendBack.append(_("YOU"));
				else
				if (!CMLib.flags().canBeSeenBy(riding(), viewerMob))
				{
					if (riding() instanceof Item)
						sendBack.append(_("something"));
					else
						sendBack.append(_("someone"));
				}
				else
					sendBack.append(riding().name());
			}
			else
			if ((this instanceof Rideable)
			&& (((Rideable) this).numRiders() > 0)
			&& (((Rideable) this).stateStringSubject(((Rideable) this).fetchRider(0)).length() > 0))
			{
				final Rideable me = (Rideable) this.me;
				final String first = me.stateStringSubject(me.fetchRider(0));
				sendBack.append(" " + first + " ");
				for (int r = 0; r < me.numRiders(); r++)
				{
					final Rider rider = me.fetchRider(r);
					if ((rider != null) && (me.stateStringSubject(rider).equals(first)))
					{
						if (r > 0)
						{
							sendBack.append(", ");
							if (r == me.numRiders() - 1)
								sendBack.append("and ");
						}
						if (rider == viewerMob)
							sendBack.append("you");
						else
						if (!CMLib.flags().canBeSeenBy(riding(), viewerMob))
						{
							if (riding() instanceof Item)
								sendBack.append(_("something"));
							else
								sendBack.append(_("someone"));
						}
						else
							sendBack.append(rider.name());
					}

				}
			}
			if ((isInCombat()) && (CMLib.flags().canMove(this)) && (!CMLib.flags().isSleeping(this)))
			{
				if (getVictim() == viewerMob)
					sendBack.append(_(" fighting YOU"));
				else
				if (!CMLib.flags().canBeSeenBy(getVictim(), viewerMob))
					sendBack.append(_(" fighting someone"));
				else
					sendBack.append(_(" fighting @x1",getVictim().name()));
			}
			if ((amFollowing() != null) && (amFollowing().fetchFollowerOrder(this) > 0))
			{
				final List<MOB> whoseAhead = CMLib.combat().getFormationFollowed(this);
				if ((whoseAhead != null) && (whoseAhead.size() > 0))
				{
					sendBack.append(_(", behind "));
					for (int v = 0; v < whoseAhead.size(); v++)
					{
						final MOB ahead = whoseAhead.get(v);
						if (v > 0)
						{
							sendBack.append(", ");
							if (v == whoseAhead.size() - 1)
								sendBack.append(_("and "));
						}
						if (ahead == viewerMob)
							sendBack.append(_("you"));
						else
						if (!CMLib.flags().canBeSeenBy(ahead, viewerMob))
							sendBack.append(_("someone"));
						else
							sendBack.append(ahead.name());
					}
				}
			}
			sendBack.append(".");
			return sendBack.toString();
		}
		return displayText;
	}

	@Override
	public String displayText()
	{
		return displayText;
	}

	@Override
	public void setDisplayText(String newDisplayText)
	{
		displayText = newDisplayText;
	}

	@Override
	public String description()
	{
		if (description == null)
			return "";
		else
		if (description instanceof byte[])
		{
			final byte[] descriptionBytes = (byte[]) description;
			if (descriptionBytes.length == 0)
				return "";
			if (CMProps.getBoolVar(CMProps.Bool.MOBDCOMPRESS))
				return CMLib.encoder().decompressString(descriptionBytes);
			else
				return CMStrings.bytesToStr(descriptionBytes);
		}
		else
			return (String) description;
	}

	@Override
	public String description(MOB viewerMob)
	{
		return description();
	}

	@Override
	public void setDescription(String newDescription)
	{
		if (newDescription.length() == 0)
			description = null;
		else
		if (CMProps.getBoolVar(CMProps.Bool.MOBDCOMPRESS))
			description = CMLib.encoder().compressString(newDescription);
		else
			description = newDescription;
	}

	@Override
	public void setMiscText(String newText)
	{
		if (newText.length() == 0)
			miscText = null;
		else
		if (CMProps.getBoolVar(CMProps.Bool.MOBCOMPRESS))
			miscText = CMLib.encoder().compressString(newText);
		else
			miscText = newText;
	}

	@Override
	public String text()
	{
		if (miscText == null)
			return "";
		else
		if (miscText instanceof byte[])
		{
			final byte[] miscTextBytes = (byte[]) miscText;
			if (miscTextBytes.length == 0)
				return "";
			if (CMProps.getBoolVar(CMProps.Bool.MOBCOMPRESS))
				return CMLib.encoder().decompressString(miscTextBytes);
			else
				return CMStrings.bytesToStr(miscTextBytes);
		}
		else
			return (String) miscText;
	}

	@Override
	public String miscTextFormat()
	{
		return CMParms.FORMAT_UNDEFINED;
	}

	@Override
	public String healthText(MOB viewer)
	{
		final String mxp = "^<!ENTITY vicmaxhp \"" + maxState().getHitPoints() + "\"^>^<!ENTITY vichp \""
				+ curState().getHitPoints() + "\"^>^<Health^>^<HealthText \"" + CMStrings.removeColors(name(viewer)) + "\"^>";
		if ((charStats() != null) && (charStats().getMyRace() != null))
			return mxp + charStats().getMyRace().healthText(viewer, this) + "^</HealthText^>";
		return mxp + CMLib.combat().standardMobCondition(viewer, this) + "^</HealthText^>";
	}

	@Override
	public double actions()
	{
		return freeActions;
	}

	@Override
	public void setActions(double remain)
	{
		freeActions = remain;
	}

	@Override
	public int commandQueSize()
	{
		return commandQue.size();
	}

	@Override
	public void clearCommandQueue()
	{
		commandQue.clear();
	}

	@Override
	public boolean dequeCommand()
	{
		while ((!removeFromGame) && (!amDestroyed) && ((session() == null) || (!session().isStopped())))
		{
			QMCommand doCommand = null;
			synchronized (commandQue)
			{
				if (commandQue.size() == 0)
					return false;
				QMCommand cmd = commandQue.getFirst();
				final double diff = actions() - cmd.actionDelay;
				if (diff >= 0.0)
				{
					final long nextTime = lastCommandTime + Math.round(cmd.actionDelay / phyStats().speed() * CMProps.getTickMillisD());
					if ((System.currentTimeMillis() < nextTime) && (session() != null))
						return false;
					cmd = commandQue.removeFirst();
					setActions(diff);
					doCommand = cmd;
				}
			}
			if (doCommand != null)
			{
				lastCommandTime = System.currentTimeMillis();
				doCommand(doCommand.commandObj, doCommand.commandVector, doCommand.metaFlags);
				synchronized (commandQue)
				{
					if (commandQue.size() > 0)
					{
						final QMCommand cmd = commandQue.getFirst();
						final Object O = cmd.commandObj;
						cmd.actionDelay = calculateActionDelay(O, cmd.commandVector, 0.0);
					}
					else
						return false;
					return true;
				}
			}

			QMCommand cmd = null;
			synchronized (commandQue)
			{
				if (commandQue.size() == 0)
					return false;
				cmd = commandQue.getFirst();
				if ((cmd == null) || (System.currentTimeMillis() < cmd.nextCheck))
					return false;
			}

			final double diff = actions() - cmd.actionDelay;
			final Object O = cmd.commandObj;
			final Vector commands = new XVector(cmd.commandVector);
			cmd.nextCheck = cmd.nextCheck + 1000;
			cmd.seconds += 1;
			final int secondsElapsed = cmd.seconds;
			final int metaFlags = cmd.metaFlags;
			try
			{
				if (O instanceof Command)
				{
					if (!((Command) O).preExecute(this, commands, metaFlags, secondsElapsed, -diff))
					{
						commandQue.remove(cmd);
						return true;
					}
				}
				else
				if (O instanceof Ability)
				{
					if (!CMLib.english().preEvoke(this, commands, secondsElapsed, -diff))
					{
						commandQue.remove(cmd);
						return true;
					}
				}
			}
			catch (final Exception e)
			{
				return false;
			}
		}
		return false;
	}

	@Override
	public void doCommand(List commands, int metaFlags)
	{
		final CMObject O = CMLib.english().findCommand(this, commands);
		if (O != null)
			doCommand(O, commands, metaFlags);
		else
			CMLib.commands().handleUnknownCommand(this, commands);
	}

	protected void doCommand(Object O, List commands, int metaFlags)
	{
		try
		{
			if (O instanceof Command)
				((Command) O).execute(this, new XVector(commands), metaFlags);
			else
			if (O instanceof Social)
				((Social) O).invoke(this, new XVector(commands), null, false);
			else
			if (O instanceof Ability)
				CMLib.english().evoke(this, new XVector(commands));
			else
				CMLib.commands().handleUnknownCommand(this, commands);
		}
		catch (final java.io.IOException io)
		{
			Log.errOut("StdMOB", CMParms.toStringList(commands));
			if (io.getMessage() != null)
				Log.errOut("StdMOB", io.getMessage());
			else
				Log.errOut("StdMOB", io);
			tell(_("Oops!"));
		}
		catch (final Exception e)
		{
			Log.errOut("StdMOB", CMParms.toStringList(commands));
			Log.errOut("StdMOB", e);
			tell(_("Oops!"));
		}
	}

	protected double calculateActionDelay(Object command, List<String> commands, double tickDelay)
	{
		if (tickDelay <= 0.0)
		{
			if (command == null)
			{
				tell(_("Huh?!"));
				return -1.0;
			}
			if (command instanceof Command)
				tickDelay = ((Command) command).checkedActionsCost(this, commands);
			else
			if (command instanceof Ability)
				tickDelay = ((Ability) command).checkedCastingCost(this, commands);
			else
				tickDelay = 1.0;
		}
		return tickDelay;
	}

	@Override
	public void prequeCommand(Vector commands, int metaFlags, double tickDelay)
	{
		if (commands == null)
			return;
		final CMObject O = CMLib.english().findCommand(this, commands);
		if (O == null)
		{
			CMLib.commands().handleUnknownCommand(this, commands);
			return;
		}
		tickDelay = calculateActionDelay(O, commands, tickDelay);
		if (tickDelay < 0.0)
			return;
		if (tickDelay == 0.0)
			doCommand(O, commands, metaFlags);
		else
			synchronized (commandQue)
			{
				final QMCommand cmd = new QMCommand();
				cmd.nextCheck = System.currentTimeMillis() - 1;
				cmd.seconds = -1;
				cmd.actionDelay = tickDelay;
				cmd.metaFlags = metaFlags;
				cmd.commandObj = O;
				cmd.execTime = 0;
				cmd.commandVector = commands;
				commandQue.addFirst(cmd);
			}
		dequeCommand();
	}

	@Override
	public void enqueCommand(List<String> commands, int metaFlags, double tickDelay)
	{
		if (commands == null)
			return;
		final CMObject O = CMLib.english().findCommand(this, commands);
		if((O == null)
		||((O instanceof Ability)
			&&CMath.bset(metaFlags, Command.METAFLAG_ORDER)
			&&CMath.bset(((Ability)O).flags(), Ability.FLAG_NOORDERING)))
		{
			CMLib.commands().handleUnknownCommand(this, commands);
			return;
		}
		tickDelay = calculateActionDelay(O, commands, tickDelay);
		if (tickDelay < 0.0)
			return;
		if (tickDelay == 0.0)
			doCommand(commands, metaFlags);
		else
		synchronized (commandQue)
		{
			final QMCommand cmd = new QMCommand();
			cmd.nextCheck = System.currentTimeMillis() - 1;
			cmd.seconds = -1;
			cmd.actionDelay = tickDelay;
			cmd.metaFlags = metaFlags;
			cmd.execTime = 0;
			cmd.commandObj = O;
			cmd.commandVector = commands;
			commandQue.addLast(cmd);
		}
		dequeCommand();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final Deity deity = getMyDeity();
		if ((deity != null) && (deity != this) && (!deity.okMessage(this, msg)))
			return false;

		final CharStats cStats = charStats;
		if (cStats != null)
		{
			for (int c = 0; c < cStats.numClasses(); c++)
				if (!cStats.getMyClass(c).okMessage(this, msg))
					return false;
			if (!cStats.getMyRace().okMessage(this, msg))
				return false;
		}

		// the order here is significant (between eff and item -- see focus)
		for (final Enumeration<Ability> a = effects(); a.hasMoreElements();)
		{
			final Ability A = a.nextElement();
			if (!A.okMessage(this, msg))
				return false;
		}

		for (final Enumeration<Item> i = items(); i.hasMoreElements();)
		{
			final Item I = i.nextElement();
			if (!I.okMessage(this, msg))
				return false;
		}

		for (final Enumeration<Behavior> b = behaviors(); b.hasMoreElements();)
		{
			final Behavior B = b.nextElement();
			if (!B.okMessage(this, msg))
				return false;
		}

		for (final Enumeration<ScriptingEngine> s = scripts(); s.hasMoreElements();)
		{
			final ScriptingEngine S = s.nextElement();
			if (!S.okMessage(this, msg))
				return false;
		}

		for (final Enumeration e = factions.elements(); e.hasMoreElements();)
		{
			final Faction.FData fD = (Faction.FData) e.nextElement();
			if (!fD.getFaction().okMessage(this, msg))
				return false;
			if (!fD.okMessage(this, msg))
				return false;
		}

		final MOB srcM = msg.source();
		if ((msg.sourceCode() != CMMsg.NO_EFFECT) && (msg.amISource(this)))
		{
			if ((msg.sourceMinor() == CMMsg.TYP_DEATH) && (CMSecurity.isAllowed(this, location(), CMSecurity.SecFlag.IMMORT)))
			{
				curState().setHitPoints(1);
				if ((msg.tool() != null) && (msg.tool() != this) && (msg.tool() instanceof MOB))
					((MOB) msg.tool()).tell(_("@x1 is immortal, and can not die.",name((MOB)msg.tool())));
				tell(_("You are immortal, and can not die."));
				return false;
			}

			if (!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			{
				final int srcCode = msg.sourceMajor();
				final int srcMinor = msg.sourceMinor();
				if (amDead())
				{
					tell(_("You are DEAD!"));
					return false;
				}

				if (CMath.bset(srcCode, CMMsg.MASK_MALICIOUS))
				{
					if ((msg.target() != this) && (msg.target() != null) && (msg.target() instanceof MOB))
					{
						final MOB target = (MOB) msg.target();
						if ((amFollowing() != null) && (target == amFollowing()))
						{
							tell(_("You like @x1 too much.",amFollowing().charStats().himher()));
							return false;
						}
						if ((getLiegeID().length() > 0) && (target.Name().equals(getLiegeID())))
						{
							if (isMarriedToLiege())
								tell(_("You are married to '@x1'!",getLiegeID()));
							else
								tell(_("You are serving '@x1'!",getLiegeID()));
							return false;
						}
						CMLib.combat().establishRange(this, (MOB) msg.target(), msg.tool());
					}
				}

				if (CMath.bset(srcCode, CMMsg.MASK_EYES))
				{
					if (CMLib.flags().isSleeping(this))
					{
						tell(_("Not while you are sleeping."));
						return false;
					}
					if (!(msg.target() instanceof Room))
						if (!CMLib.flags().canBeSeenBy(msg.target(), this))
						{
							if (msg.target() instanceof Item)
								tell(_("You don't see @x1 here.",((Item)msg.target()).name(this)));
							else
								tell(_("You can't see that!"));
							return false;
						}
				}
				if (CMath.bset(srcCode, CMMsg.MASK_MOUTH))
				{
					if (((srcMinor != CMMsg.TYP_LIST) || srcM.amDead() || CMLib.flags().isSleeping(srcM))
					&& (!CMLib.flags().aliveAwakeMobile(this, false)))
						return false;
					if (CMath.bset(srcCode, CMMsg.MASK_SOUND))
					{
						if ((msg.tool() == null)
						|| (!(msg.tool() instanceof Ability))
						|| (!((Ability) msg.tool()).isNowAnAutoEffect()))
						{
							if (CMLib.flags().isSleeping(this))
							{
								tell(_("Not while you are sleeping."));
								return false;
							}
							if (!CMLib.flags().canSpeak(this))
							{
								tell(_("You can't make sounds!"));
								return false;
							}
							if (CMLib.flags().isAnimalIntelligence(this))
							{
								tell(_("You aren't smart enough to speak."));
								return false;
							}
						}
					}
					else
					{
						if ((!CMLib.flags().canBeSeenBy(msg.target(), this))
						&& (!(isMine(msg.target())
						&& (msg.target() instanceof Item))))
						{
							srcM.tell(_("You don't see '@x1' here.",((Item)msg.target()).name(this)));
							return false;
						}
						if (!CMLib.flags().canTaste(this))
						{
							if ((msg.sourceMinor()==CMMsg.TYP_EAT)||(msg.sourceMinor()==CMMsg.TYP_DRINK))
								tell(_("You can't eat or drink."));
							else
								tell(_("Your mouth is out of order."));
							return false;
						}
					}
				}
				if (CMath.bset(srcCode, CMMsg.MASK_HANDS))
				{
					if ((!CMLib.flags().canBeSeenBy(msg.target(), this))
					&& (!(isMine(msg.target()) && (msg.target() instanceof Item)))
					&& (!((isInCombat()) && (msg.target() == victim)))
					&& (CMath.bset(msg.targetMajor(), CMMsg.MASK_HANDS)))
					{
						srcM.tell(_("You don't see '@x1' here.",((Physical)msg.target()).name(this)));
						return false;
					}
					if (!CMLib.flags().aliveAwakeMobile(this, false))
						return false;

					if ((CMLib.flags().isSitting(this))
					&& (msg.sourceMinor() != CMMsg.TYP_SITMOVE)
					&& (msg.sourceMinor() != CMMsg.TYP_BUY)
					&& (msg.sourceMinor() != CMMsg.TYP_BID)
					&& (msg.targetMinor() != CMMsg.TYP_OK_VISUAL)
					&& ((msg.sourceMessage() != null) || (msg.othersMessage() != null))
					&& (((!CMLib.utensils().reachableItem(this, msg.target())) || (!CMLib.utensils().reachableItem(this, msg.tool())))
						&& (location() != null)
						&& (!CMath.bset(location().phyStats().sensesMask(), PhyStats.SENSE_ROOMCRUNCHEDIN))))
					{
						tell(_("You need to stand up!"));
						return false;
					}
				}

				if (CMath.bset(srcCode, CMMsg.MASK_MOVE))
				{
					boolean sitting = CMLib.flags().isSitting(this);
					if ((sitting) && ((msg.sourceMinor() == CMMsg.TYP_LEAVE) || (msg.sourceMinor() == CMMsg.TYP_ENTER)))
						sitting = false;

					if (((CMLib.flags().isSleeping(this)) || (sitting))
					&& (msg.sourceMinor() != CMMsg.TYP_STAND)
					&& (msg.sourceMinor() != CMMsg.TYP_SITMOVE)
					&& (msg.sourceMinor() != CMMsg.TYP_SIT)
					&& (msg.sourceMinor() != CMMsg.TYP_SLEEP))
					{
						tell(_("You need to stand up!"));
						if ((msg.sourceMinor() != CMMsg.TYP_WEAPONATTACK) && (msg.sourceMinor() != CMMsg.TYP_THROW))
							return false;
					}
					if (!CMLib.flags().canMove(this))
					{
						tell(_("You can't move!"));
						return false;
					}
				}

				// limb check
				switch (msg.targetMinor())
				{
				case CMMsg.TYP_PULL:
				case CMMsg.TYP_PUSH:
				case CMMsg.TYP_GET:
				case CMMsg.TYP_REMOVE:
				case CMMsg.TYP_OPEN:
				case CMMsg.TYP_CLOSE:
					if (charStats().getBodyPart(Race.BODY_ARM) == 0)
					{
						tell(_("You need arms to do that."));
						return false;
					}
					break;
				case CMMsg.TYP_DELICATE_HANDS_ACT:
					if ((charStats().getBodyPart(Race.BODY_HAND) == 0) && (msg.othersMinor() != CMMsg.NO_EFFECT))
					{
						tell(_("You need hands to do that."));
						return false;
					}
					break;
				case CMMsg.TYP_JUSTICE:
					if ((charStats().getBodyPart(Race.BODY_HAND) == 0) && (msg.target() instanceof Item))
					{
						tell(_("You need hands to do that."));
						return false;
					}
					break;
				case CMMsg.TYP_FILL:
				case CMMsg.TYP_GIVE:
				case CMMsg.TYP_HANDS:
				case CMMsg.TYP_LOCK:
				case CMMsg.TYP_PUT:
				case CMMsg.TYP_INSTALL:
				case CMMsg.TYP_REPAIR:
				case CMMsg.TYP_ENHANCE:
				case CMMsg.TYP_UNLOCK:
				case CMMsg.TYP_WRITE:
					if (charStats().getBodyPart(Race.BODY_HAND) == 0)
					{
						tell(_("You need hands to do that."));
						return false;
					}
					break;
				case CMMsg.TYP_DRINK:
					if (charStats().getBodyPart(Race.BODY_HAND) == 0)
					{
						if ((msg.target() != null) && (isMine(msg.target())))
						{
							tell(_("You need hands to do that."));
							return false;
						}
					}
					break;
				}

				// activity check
				switch (msg.sourceMinor())
				{
				case CMMsg.TYP_WEAR:
				case CMMsg.TYP_HOLD:
				case CMMsg.TYP_WIELD:
				case CMMsg.TYP_REMOVE:
					possWieldedItem = null;
					possHeldItem = null;
					break;
				case CMMsg.TYP_JUSTICE:
					if ((msg.target() != null) && (isInCombat()) && (msg.target() instanceof Item))
					{
						tell(_("Not while you are fighting!"));
						return false;
					}
					break;
				case CMMsg.TYP_THROW:
					if (charStats().getBodyPart(Race.BODY_ARM) == 0)
					{
						tell(_("You need arms to do that."));
						return false;
					}
					break;
				case CMMsg.TYP_OPEN:
				case CMMsg.TYP_CLOSE:
					if (isInCombat())
					{
						if ((msg.target() != null) && ((msg.target() instanceof Exit) || (srcM.isMine(msg.target()))))
							break;
						tell(_("Not while you are fighting!"));
						return false;
					}
					break;
				case CMMsg.TYP_LEAVE:
					if ((isInCombat())
					&& (location() != null)
					&& (!msg.sourceMajor(CMMsg.MASK_MAGIC)))
						for (final Enumeration<MOB> m = location().inhabitants(); m.hasMoreElements();)
						{
							final MOB M = m.nextElement();
							if ((M != this) && (M.getVictim() == this) && (CMLib.flags().aliveAwakeMobile(M, true))
									&& (CMLib.flags().canSenseEnteringLeaving(srcM, M)))
							{
								tell(_("Not while you are fighting!"));
								return false;
							}
						}
					break;
				case CMMsg.TYP_BUY:
				case CMMsg.TYP_BID:
				case CMMsg.TYP_DELICATE_HANDS_ACT:
				case CMMsg.TYP_FILL:
				case CMMsg.TYP_LIST:
				case CMMsg.TYP_LOCK:
				case CMMsg.TYP_SIT:
				case CMMsg.TYP_SLEEP:
				case CMMsg.TYP_UNLOCK:
				case CMMsg.TYP_VALUE:
				case CMMsg.TYP_SELL:
				case CMMsg.TYP_VIEW:
				case CMMsg.TYP_READ:
					if (isInCombat() && (!msg.sourceMajor(CMMsg.MASK_MAGIC)))
					{
						tell(_("Not while you are fighting!"));
						return false;
					}
					break;
				case CMMsg.TYP_REBUKE:
					if ((msg.target() == null) || (!(msg.target() instanceof Deity)))
					{
						if (msg.target() != null)
						{
							if ((msg.target() instanceof MOB)
							&& (!CMLib.flags().canBeHeardSpeakingBy(this, (MOB) msg.target())))
							{
								tell(_("@x1 can't hear you!",((Physical)msg.target()).name(this)));
								return false;
							}
							else
							if ((msg.target() instanceof MOB)
							&& (((MOB) msg.target()).amFollowing() == srcM)
							&& (srcM.isFollowedBy((MOB) msg.target())))
							{
								// should work.
							}
							else
							if ((!((msg.target() instanceof MOB) && (((MOB) msg.target()).getLiegeID().equals(Name()))))
							&& (!msg.target().Name().equals(getLiegeID())))
							{
								tell(_("@x1 does not serve you, and you do not serve @x2.",((Physical)msg.target()).name(this),((Physical)msg.target()).name(this)));
								return false;
							}
							else
							if ((msg.target() instanceof MOB)
							&& (((MOB) msg.target()).getLiegeID().equals(Name()))
							&& (getLiegeID().equals(msg.target().Name()))
							&& (((MOB) msg.target()).isMarriedToLiege()))
							{
								tell(_("You cannot rebuke @x1.  You must get an annulment or a divorce.",((Physical)msg.target()).name(this)));
								return false;
							}
						}
						else
						if (getLiegeID().length() == 0)
						{
							tell(_("You aren't serving anyone!"));
							return false;
						}
					}
					else
					if (getWorshipCharID().length() == 0)
					{
						tell(_("You aren't worshipping anyone!"));
						return false;
					}
					break;
				case CMMsg.TYP_SERVE:
					if (msg.target() == null)
						return false;
					if (msg.target() == this)
					{
						tell(_("You can't serve yourself!"));
						return false;
					}
					if (msg.target() instanceof Deity)
						break;
					if ((msg.target() instanceof MOB)
							&& (!CMLib.flags().canBeHeardSpeakingBy(this, (MOB) msg.target())))
					{
						tell(_("@x1 can't hear you!",((Physical)msg.target()).name(this)));
						return false;
					}
					if (getLiegeID().length() > 0)
					{
						tell(_("You are already serving '@x1'.",getLiegeID()));
						return false;
					}
					if ((msg.target() instanceof MOB) && (((MOB) msg.target()).getLiegeID().equals(Name())))
					{
						tell(_("You can not serve each other!"));
						return false;
					}
					break;
				case CMMsg.TYP_CAST_SPELL:
					if (charStats().getStat(CharStats.STAT_INTELLIGENCE) < 5)
					{
						tell(_("You aren't smart enough to do magic."));
						return false;
					}
					break;
				default:
					break;
				}
			}
		}

		if ((msg.sourceCode() != CMMsg.NO_EFFECT)
		&& (msg.amISource(this))
		&& (msg.target() != null)
		&& (msg.target() != this)
		&& (!CMath.bset(msg.sourceMajor(), CMMsg.MASK_ALWAYS))
		&& (msg.target() instanceof MOB)
		&& (location() != null)
		&& (location() == ((MOB) msg.target()).location()))
		{
			final MOB trgM = (MOB) msg.target();
			// and now, the consequences of range
			if (((msg.targetMinor() == CMMsg.TYP_WEAPONATTACK) && (rangeToTarget() > maxRange(msg.tool())))
			|| ((msg.sourceMinor() == CMMsg.TYP_THROW) && (rangeToTarget() > 2) && (maxRange(msg.tool()) <= 0)))
			{
				final String newstr = "<S-NAME> advance(s) at ";
				msg.modify(this, trgM, null, CMMsg.MSG_ADVANCE, newstr + trgM.name(this), CMMsg.MSG_ADVANCE, newstr + "you", CMMsg.MSG_ADVANCE, newstr + trgM.name());
				final boolean ok = location().okMessage(this, msg);
				if (ok)
					setAtRange(rangeToTarget() - 1);
				if (victim != null)
				{
					victim.setAtRange(rangeToTarget());
					victim.recoverPhyStats();
				}
				else
					setAtRange(-1);
				recoverPhyStats();
				return ok;
			}
			else
			if (msg.targetMinor() == CMMsg.TYP_RETREAT)
			{
				if (curState().getMovement() < 25)
				{
					tell(_("You are too tired."));
					return false;
				}
				if ((location() != null) && (rangeToTarget() >= location().maxRange()))
				{
					tell(_("You cannot retreat any further."));
					return false;
				}
				curState().adjMovement(-25, maxState());
				setAtRange(rangeToTarget() + 1);
				if (victim != null)
				{
					victim.setAtRange(rangeToTarget());
					victim.recoverPhyStats();
				}
				else
					setAtRange(-1);
				recoverPhyStats();
			}
			else
			if ((msg.tool() != null) && (msg.sourceMinor() != CMMsg.TYP_BUY)
			&& (msg.sourceMinor() != CMMsg.TYP_BID)
			&& (msg.sourceMinor() != CMMsg.TYP_SELL)
			&& (msg.sourceMinor() != CMMsg.TYP_VIEW))
			{
				int useRange = -1;
				final Environmental tool = msg.tool();
				if (getVictim() != null)
				{
					if (getVictim() == trgM)
						useRange = rangeToTarget();
					else
					{
						if (trgM.getVictim() == this)
							useRange = trgM.rangeToTarget();
						else
							useRange = maxRange(tool);
					}
				}
				if ((useRange >= 0) && (maxRange(tool) < useRange))
				{
					srcM.tell(_("You are too far away from @x1 to use @x2.",trgM.name(srcM),tool.name()));
					return false;
				}
				else
				if ((useRange >= 0) && (minRange(tool) > useRange))
				{
					srcM.tell(_("You are too close to @x1 to use @x2.",trgM.name(srcM),tool.name()));
					if ((msg.targetMinor() == CMMsg.TYP_WEAPONATTACK)
					&& (tool instanceof Weapon)
					&& (!((Weapon) tool).amWearingAt(Wearable.IN_INVENTORY)))
						CMLib.commands().postRemove(this, (Weapon) msg.tool(), false);
					return false;
				}
			}
		}

		if ((msg.targetMinor() != CMMsg.NO_EFFECT) && (msg.amITarget(this)))
		{
			if ((amDead()) || (location() == null))
				return false;
			if (CMath.bset(msg.targetMajor(), CMMsg.MASK_MALICIOUS))
			{
				if (Log.combatChannelOn())
					Log.combatOut(srcM.Name() + ":" + Name() + ":" + CMMsg.TYPE_DESCS[msg.targetMinor()] + ":"
							+ ((msg.tool() != null) ? msg.tool().Name() : "null"));

				if ((msg.amISource(this))
				&& (!msg.sourceMajor(CMMsg.MASK_ALWAYS))
				&& ((msg.tool() == null) || (!(msg.tool() instanceof Ability)) || (!((Ability) msg.tool()).isNowAnAutoEffect())))
				{
					srcM.tell(_("You like yourself too much."));
					if (victim == this)
					{
						victim = null;
						setAtRange(-1);
					}
					return false;
				}

				if ((!mayIFight(srcM))
				&& ((!(msg.tool() instanceof Ability))
					|| (((((Ability) msg.tool()).classificationCode() & Ability.ALL_ACODES) != Ability.ACODE_POISON)
						&& ((((Ability) msg.tool()).classificationCode() & Ability.ALL_ACODES) != Ability.ACODE_DISEASE))
					|| ((srcM == this) && (srcM.isMonster()))))
				{
					srcM.tell(_("You may not attack @x1.",name(srcM)));
					srcM.setVictim(null);
					if (victim == srcM)
						setVictim(null);
					return false;
				}

				if ((srcM != this)
				&& (!isMonster())
				&& (!srcM.isMonster())
				&& (soulMate() == null)
				&& (srcM.soulMate() == null)
				&& (CMath.abs(srcM.phyStats().level() - phyStats().level()) > CMProps.getPKillLevelDiff())
				&& (!CMSecurity.isAllowed(this, location(), CMSecurity.SecFlag.PKILL))
				&& (!CMSecurity.isAllowed(srcM, srcM.location(), CMSecurity.SecFlag.PKILL))
				&& ((!(msg.tool() instanceof Ability)) || (((Ability) msg.tool()).classificationCode() & Ability.ALL_ACODES) != Ability.ACODE_DISEASE))
				{
					srcM.tell(_("That is not EVEN a fair fight."));
					srcM.setVictim(null);
					if (victim == srcM)
						setVictim(null);
					return false;
				}

				if ((amFollowing() == srcM) && (!(msg.tool() instanceof DiseaseAffect)))
					setFollowing(null);

				if (isInCombat())
				{
					if ((rangeToTarget() > 0)
					&& (getVictim() != srcM)
					&& (srcM.getVictim() == this)
					&& (srcM.rangeToTarget() == 0))
					{
						setVictim(srcM);
						setAtRange(0);
					}
				}

				if ((msg.targetMinor() != CMMsg.TYP_WEAPONATTACK) && (msg.value() <= 0))
				{
					int chanceToFail = Integer.MIN_VALUE;
					for (final int c : CharStats.CODES.SAVING_THROWS())
						if (msg.targetMinor() == CharStats.CODES.CMMSGMAP(c))
						{
							chanceToFail = charStats().getSave(c);
							break;
						}
					if (chanceToFail > Integer.MIN_VALUE)
					{
						final int diff = (phyStats().level() - srcM.phyStats().level());
						final int diffSign = diff < 0 ? -1 : 1;
						chanceToFail += (diffSign * (diff * diff));
						if (chanceToFail < 5)
							chanceToFail = 5;
						else
						if (chanceToFail > 95)
							chanceToFail = 95;

						if (CMLib.dice().rollPercentage() < chanceToFail)
						{
							CMLib.combat().resistanceMsgs(msg, srcM, this);
							msg.setValue(msg.value() + 1);
						}
					}
				}
			}

			if ((rangeToTarget() >= 0) && (!isInCombat()))
				setAtRange(-1);

			switch (msg.targetMinor())
			{
			case CMMsg.TYP_CLOSE:
			case CMMsg.TYP_DRINK:
			case CMMsg.TYP_DROP:
			case CMMsg.TYP_EAT:
			case CMMsg.TYP_FILL:
			case CMMsg.TYP_GET:
			case CMMsg.TYP_HOLD:
			case CMMsg.TYP_REMOVE:
			case CMMsg.TYP_LOCK:
			case CMMsg.TYP_OPEN:
			case CMMsg.TYP_PUT:
			case CMMsg.TYP_POUR:
			case CMMsg.TYP_UNLOCK:
			case CMMsg.TYP_WEAR:
			case CMMsg.TYP_WIELD:
				srcM.tell(srcM, this, null, _("You can't do that to <T-NAMESELF>."));
				return false;
			case CMMsg.TYP_TEACH:
				if((msg.target() instanceof MOB)
				&&(!CMLib.expertises().canBeTaught(msg.source(), (MOB)msg.target(), msg.tool(), msg.targetMessage())))
					return false;
				break;
			case CMMsg.TYP_PULL:
				if ((!CMLib.flags().isBoundOrHeld(this)) && (!CMLib.flags().isSleeping(this)))
				{
					srcM.tell(srcM, this, null, _("You can't do that to <T-NAMESELF>."));
					return false;
				}
				if (phyStats().weight() > (srcM.maxCarry() / 2))
				{
					srcM.tell(srcM, this, null, _("<T-NAME> is too big for you to pull."));
					return false;
				}
				break;
			case CMMsg.TYP_PUSH:
				if ((!CMLib.flags().isBoundOrHeld(this)) && (!CMLib.flags().isSleeping(this)))
				{
					srcM.tell(srcM, this, null, _("You can't do that to <T-NAMESELF>."));
					return false;
				}
				if (phyStats().weight() > srcM.maxCarry())
				{
					srcM.tell(srcM, this, null, _("<T-NAME> is too heavy for you to push."));
					return false;
				}
				break;
			case CMMsg.TYP_MOUNT:
			case CMMsg.TYP_DISMOUNT:
				if (!(this instanceof Rideable))
				{
					srcM.tell(srcM, this, null, _("You can't do that to <T-NAMESELF>."));
					return false;
				}
				break;
			case CMMsg.TYP_GIVE:
				if (msg.tool() == null)
					return false;
				if (!(msg.tool() instanceof Item))
					return false;
				if (CMSecurity.isAllowed(this, location(), CMSecurity.SecFlag.ORDER)
				|| (CMSecurity.isAllowed(this, location(), CMSecurity.SecFlag.CMDMOBS) && (isMonster()))
				|| (CMSecurity.isAllowed(this, location(), CMSecurity.SecFlag.CMDROOMS) && (isMonster())))
					return true;
				if ((getWearPositions(Wearable.WORN_ARMS) == 0) && (!CMath.bset(msg.targetMajor(), CMMsg.MASK_ALWAYS)))
				{
					srcM.tell(_("@x1 is unable to accept that from you.",name(srcM)));
					return false;
				}
				if ((!CMLib.flags().canBeSeenBy(msg.tool(), this))
						&& (!CMath.bset(msg.targetMajor(), CMMsg.MASK_ALWAYS)))
				{
					srcM.tell(_("@x1 can't see what you are giving.",name(srcM)));
					return false;
				}
				final int GC = msg.targetMajor() & CMMsg.MASK_ALWAYS;
				CMMsg msg2 = CMClass.getMsg(srcM, msg.tool(), null, CMMsg.MSG_DROP | CMMsg.MASK_INTERMSG, null,
						CMMsg.MSG_DROP | CMMsg.MASK_INTERMSG, null, CMMsg.MSG_DROP | CMMsg.MASK_INTERMSG, null);
				if (!location().okMessage(srcM, msg2))
					return false;
				if ((msg.target() != null) && (msg.target() instanceof MOB))
				{
					msg2 = CMClass.getMsg((MOB) msg.target(), msg.tool(), null,
							GC | CMMsg.MSG_GET | CMMsg.MASK_INTERMSG, null,
							GC | CMMsg.MSG_GET | CMMsg.MASK_INTERMSG, null,
							GC | CMMsg.MSG_GET | CMMsg.MASK_INTERMSG, null);
					if (!location().okMessage(msg.target(), msg2))
					{
						srcM.tell(_("@x1 cannot seem to accept @x2.",((Physical)msg.target()).name(srcM),((Physical)msg.tool()).name(this)));
						return false;
					}
				}
				break;
			case CMMsg.TYP_FOLLOW:
				if (totalFollowers() + srcM.totalFollowers() >= maxFollowers())
				{
					srcM.tell(_("@x1 can't accept any more followers.",name(srcM)));
					return false;
				}
				if ((CMProps.getIntVar(CMProps.Int.FOLLOWLEVELDIFF) > 0)
				&& (!isMonster())
				&& (!srcM.isMonster())
				&& (!CMSecurity.isAllowed(this, location(), CMSecurity.SecFlag.ORDER))
				&& (!CMSecurity.isAllowed(srcM, srcM.location(), CMSecurity.SecFlag.ORDER)))
				{
					if (phyStats.level() > (srcM.phyStats().level() + CMProps.getIntVar(CMProps.Int.FOLLOWLEVELDIFF)))
					{
						srcM.tell(_("@x1 is too advanced for you.",name(srcM)));
						return false;
					}
					if (phyStats.level() < (srcM.phyStats().level() - CMProps.getIntVar(CMProps.Int.FOLLOWLEVELDIFF)))
					{
						srcM.tell(_("@x1 is too inexperienced for you.",name(srcM)));
						return false;
					}
				}
				break;
			}
		}
		if ((srcM != this) && (msg.target() != this))
		{
			if ((msg.othersMinor() == CMMsg.TYP_DEATH) && (msg.sourceMinor() == CMMsg.TYP_DEATH))
			{
				if ((followers != null)
				&& (followers.contains(srcM))
				&& (CMLib.dice().rollPercentage() == 1)
				&& (fetchEffect("Disease_Depression") == null)
				&& (!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
				{
					final Ability A = CMClass.getAbility("Disease_Depression");
					if (A != null)
						A.invoke(this, this, true, 0);
				}
			}
		}
		return true;
	}

	@Override
	public void tell(final MOB source, final Environmental target, final Environmental tool, final String msg)
	{
		final Session S = mySession;
		if ((S != null) && (msg != null))
			S.stdPrintln(source, target, tool, msg);
	}

	@Override
	public void tell(final String msg)
	{
		tell(this, this, null, msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final Deity deity = getMyDeity();
		if (deity != null)
			deity.executeMsg(this, msg);

		final CharStats cStats = charStats;
		if (cStats != null)
		{
			for (int c = 0; c < cStats.numClasses(); c++)
				cStats.getMyClass(c).executeMsg(this, msg);
			cStats.getMyRace().executeMsg(this, msg);
		}

		eachBehavior(new EachApplicable<Behavior>()
		{
			@Override
			public final void apply(final Behavior B)
			{
				B.executeMsg(me, msg);
			}
		});
		eachScript(new EachApplicable<ScriptingEngine>()
		{
			@Override
			public final void apply(final ScriptingEngine S)
			{
				S.executeMsg(me, msg);
			}
		});

		final MOB srcM = msg.source();

		final boolean asleep = CMLib.flags().isSleeping(this);
		final boolean canseesrc = CMLib.flags().canBeSeenBy(srcM, this);
		final boolean canhearsrc = (msg.targetMinor() == CMMsg.TYP_SPEAK) ?
									CMLib.flags().canBeHeardSpeakingBy(srcM,this) :
									CMLib.flags().canBeHeardMovingBy(srcM, this);

		// first do special cases...
		if (msg.amITarget(this) && (!amDead))
			switch (msg.targetMinor())
			{
			case CMMsg.TYP_HEALING:
				CMLib.combat().handleBeingHealed(msg);
				break;
			case CMMsg.TYP_SNIFF:
				CMLib.commands().handleBeingSniffed(msg);
				break;
			case CMMsg.TYP_DAMAGE:
				CMLib.combat().handleBeingDamaged(msg);
				break;
			case CMMsg.TYP_TEACH:
				if(msg.target() instanceof MOB)
					CMLib.expertises().handleBeingTaught(msg.source(), (MOB)msg.target(), msg.tool(), msg.targetMessage());
				break;
			default:
				break;
			}

		// now go on to source activities
		if ((msg.sourceCode() != CMMsg.NO_EFFECT) && (msg.amISource(this)))
		{
			if ((CMath.bset(msg.sourceMajor(), CMMsg.MASK_MALICIOUS))
				&& (msg.target() instanceof MOB)
				&& (getVictim() != msg.target())
				&& ((!CMath.bset(msg.sourceMajor(), CMMsg.MASK_ALWAYS)) || (!(msg.tool() instanceof DiseaseAffect))))
			{
				CMLib.combat().establishRange(this, (MOB) msg.target(), msg.tool());
				if ((msg.tool() instanceof Weapon)
				|| (msg.sourceMinor() == CMMsg.TYP_WEAPONATTACK)
				|| (!CMLib.flags().aliveAwakeMobileUnbound((MOB) msg.target(), true)))
				{
					setVictim((MOB) msg.target());
				}
			}

			switch (msg.sourceMinor())
			{
			case CMMsg.TYP_LIFE:
				CMLib.commands().handleComeToLife(this, msg);
				break;
			case CMMsg.TYP_PANIC:
				CMLib.commands().postFlee(this, "");
				break;
			case CMMsg.TYP_EXPCHANGE:
				CMLib.leveler().handleExperienceChange(msg);
				break;
			case CMMsg.TYP_FACTIONCHANGE:
				if (msg.othersMessage() != null)
				{
					if ((msg.value() == Integer.MAX_VALUE) || (msg.value() == Integer.MIN_VALUE))
						removeFaction(msg.othersMessage());
					else
						adjustFaction(msg.othersMessage(), msg.value());
				}
				break;
			case CMMsg.TYP_DEATH:
				CMLib.combat().handleDeath(msg);
				break;
			case CMMsg.TYP_REBUKE:
				if (((msg.target() == null) && (getLiegeID().length() > 0))
				|| ((msg.target() != null) && (msg.target().Name().equals(getLiegeID())) && (!isMarriedToLiege())))
					setLiegeID("");
				tell(this, msg.target(), msg.tool(), msg.sourceMessage());
				break;
			case CMMsg.TYP_SERVE:
				if ((msg.target() != null) && (!(msg.target() instanceof Deity)))
					setLiegeID(msg.target().Name());
				tell(this, msg.target(), msg.tool(), msg.sourceMessage());
				break;
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
				if (msg.target() == this)
					CMLib.commands().handleBeingLookedAt(msg);
				break;
			case CMMsg.TYP_READ:
				if ((CMLib.flags().canBeSeenBy(this, srcM)) && (msg.amITarget(this)))
					srcM.tell(_("There is nothing written on @x1",name(srcM)));
				break;
			case CMMsg.TYP_SIT:
				CMLib.commands().handleSit(msg);
				break;
			case CMMsg.TYP_SLEEP:
				CMLib.commands().handleSleep(msg);
				break;
			case CMMsg.TYP_QUIT:
				tell(srcM, msg.target(), msg.tool(), msg.sourceMessage());
				break;
			case CMMsg.TYP_STAND:
				CMLib.commands().handleStand(msg);
				break;
			case CMMsg.TYP_RECALL:
				CMLib.commands().handleRecall(msg);
				break;
			case CMMsg.TYP_FOLLOW:
				if ((msg.target() != null) && (msg.target() instanceof MOB))
				{
					setFollowing((MOB) msg.target());
					tell(srcM, msg.target(), msg.tool(), msg.sourceMessage());
				}
				break;
			case CMMsg.TYP_NOFOLLOW:
				setFollowing(null);
				tell(srcM, msg.target(), msg.tool(), msg.sourceMessage());
				break;
			default:
				// you pretty much always know what you are doing, if you can do
				// it.
				if (!CMath.bset(msg.sourceMajor(), CMMsg.MASK_CNTRLMSG))
					tell(srcM, msg.target(), msg.tool(), msg.sourceMessage());
				break;
			}
		}
		else
		if ((msg.targetMinor() != CMMsg.NO_EFFECT) && (msg.amITarget(this)))
		{
			final int targetMajor = msg.targetMajor();
			switch (msg.targetMinor())
			{
			case CMMsg.TYP_HEALING:
			case CMMsg.TYP_DAMAGE:
				// handled as special cases above
				break;
			case CMMsg.TYP_GIVE:
				if (msg.tool() instanceof Item)
					CMLib.commands().handleBeingGivenTo(msg);
				break;
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
				if (CMLib.flags().canBeSeenBy(this, srcM))
					CMLib.commands().handleBeingLookedAt(msg);
				break;
			case CMMsg.TYP_REBUKE:
				if ((srcM.Name().equals(getLiegeID()) && (!isMarriedToLiege())))
					setLiegeID("");
				break;
			case CMMsg.TYP_SPEAK:
				if ((CMProps.getBoolVar(CMProps.Bool.INTRODUCTIONSYSTEM)) && (!asleep) && (canhearsrc))
					CMLib.commands().handleIntroductions(srcM, this, msg.targetMessage());
				break;
			default:
				if ((CMath.bset(msg.targetMajor(), CMMsg.MASK_MALICIOUS)) && (!amDead))
					CMLib.combat().handleBeingAssaulted(msg);
				else
				if (CMath.bset(targetMajor, CMMsg.MASK_CHANNEL))
				{
					final int channelCode = msg.targetMinor() - CMMsg.TYP_CHANNEL;
					if ((playerStats() != null)
					&& (!CMath.bset(getBitmap(), MOB.ATT_QUIET))
					&& (!CMath.isSet(playerStats().getChannelMask(), channelCode)))
						tell(srcM, msg.target(), msg.tool(), msg.targetMessage());
				}
				break;
			}

			// now do the says
			if ((CMath.bset(targetMajor, CMMsg.MASK_SOUND)) && (canhearsrc) && (!asleep))
			{
				if ((msg.targetMinor() == CMMsg.TYP_SPEAK)
				&& (srcM != null)
				&& (playerStats() != null)
				&& (!srcM.isMonster())
				&& (CMLib.flags().canBeHeardSpeakingBy(srcM, this)))
					playerStats().setReplyTo(srcM, PlayerStats.REPLY_SAY);

				tell(srcM, msg.target(), msg.tool(), msg.targetMessage());
			}
			else
			if ((CMath.bset(targetMajor, CMMsg.MASK_ALWAYS))
			|| (msg.targetMinor() == CMMsg.TYP_DAMAGE)
			|| (msg.targetMinor() == CMMsg.TYP_HEALING))
				tell(srcM, msg.target(), msg.tool(), msg.targetMessage());
			else
			if ((CMath.bset(targetMajor, CMMsg.MASK_EYES)) && ((!asleep) && (canseesrc)))
				tell(srcM, msg.target(), msg.tool(), msg.targetMessage());
			else
			if (CMath.bset(msg.targetMajor(), CMMsg.MASK_MALICIOUS))
				tell(srcM, msg.target(), msg.tool(), msg.targetMessage());
			else
			if (((CMath.bset(targetMajor, CMMsg.MASK_HANDS))
				|| (CMath.bset(targetMajor, CMMsg.MASK_MOVE))
				|| ((CMath.bset(targetMajor, CMMsg.MASK_MOUTH) && (!CMath.bset(targetMajor, CMMsg.MASK_SOUND)))))
			&& (!asleep) && ((canhearsrc) || (canseesrc)))
				tell(srcM, msg.target(), msg.tool(), msg.targetMessage());
		}
		else
		if ((msg.othersCode() != CMMsg.NO_EFFECT) && (!msg.amISource(this)) && (!msg.amITarget(this)))
		{
			final int othersMajor = msg.othersMajor();
			final int othersMinor = msg.othersMinor();

			if (CMath.bset(msg.othersMajor(), CMMsg.MASK_MALICIOUS)
			&& (msg.target() instanceof MOB)
			&& ((!CMath.bset(msg.sourceMajor(), CMMsg.MASK_ALWAYS)) || (!(msg.tool() instanceof DiseaseAffect))))
				CMLib.combat().makeFollowersFight(this, (MOB) msg.target(), srcM);

			if ((othersMinor == CMMsg.TYP_ENTER) // exceptions to movement
			|| (othersMinor == CMMsg.TYP_FLEE)
			|| (othersMinor == CMMsg.TYP_LEAVE))
			{
				if (((!asleep) || (msg.othersMinor() == CMMsg.TYP_ENTER))
				&& (CMLib.flags().canSenseEnteringLeaving(srcM, this)))
					tell(srcM, msg.target(), msg.tool(), msg.othersMessage());
			}
			else
			if (CMath.bset(othersMajor, CMMsg.MASK_CHANNEL))
			{
				final int channelCode = ((msg.othersCode() - CMMsg.MASK_CHANNEL) - CMMsg.TYP_CHANNEL);
				if ((playerStats() != null)
				&& (!CMath.bset(getBitmap(), MOB.ATT_QUIET))
				&& (!CMath.isSet(playerStats().getChannelMask(), channelCode)))
					tell(srcM, msg.target(), msg.tool(), msg.othersMessage());
			}
			else
			if ((CMath.bset(othersMajor, CMMsg.MASK_SOUND)) && (!asleep) && (canhearsrc))
			{
				if ((CMProps.getBoolVar(CMProps.Bool.INTRODUCTIONSYSTEM))
				&& (msg.othersMinor() == CMMsg.TYP_SPEAK))
					CMLib.commands().handleIntroductions(srcM, this, msg.othersMessage());
				tell(srcM, msg.target(), msg.tool(), msg.othersMessage());
			}
			else
			if (((CMath.bset(othersMajor, CMMsg.MASK_EYES))
				|| (CMath.bset(othersMajor, CMMsg.MASK_HANDS))
				|| (CMath.bset(othersMajor, CMMsg.MASK_ALWAYS)))
			&& (!CMath.bset(msg.othersMajor(), CMMsg.MASK_CNTRLMSG))
			&& ((!asleep) && (canseesrc)))
			{
				tell(srcM, msg.target(), msg.tool(), msg.othersMessage());
			}
			else
			if (((CMath.bset(othersMajor, CMMsg.MASK_MOVE))
				|| ((CMath.bset(othersMajor, CMMsg.MASK_MOUTH)) && (!CMath.bset(othersMajor, CMMsg.MASK_SOUND))))
			&& (!asleep) && ((canseesrc) || (canhearsrc)))
				tell(srcM, msg.target(), msg.tool(), msg.othersMessage());

			if ((msg.othersMinor() == CMMsg.TYP_DEATH)
			&& (msg.sourceMinor() == CMMsg.TYP_DEATH)
			&& (location() != null))
				CMLib.combat().handleObserveDeath(this, victim, msg);
			else
			if (msg.sourceMinor() == CMMsg.TYP_LIFE)
				CMLib.commands().handleObserveComesToLife(this, srcM, msg);
		}

		// the order here is significant (between eff and item -- see focus)
		eachItem(new EachApplicable<Item>()
		{
			@Override
			public final void apply(final Item I)
			{
				I.executeMsg(me, msg);
			}
		});

		eachEffect(new EachApplicable<Ability>()
		{
			@Override
			public final void apply(final Ability A)
			{
				A.executeMsg(me, msg);
			}
		});

		for (final Enumeration e = factions.elements(); e.hasMoreElements();)
		{
			final Faction.FData fD = (Faction.FData) e.nextElement();
			fD.getFaction().executeMsg(this, msg);
			fD.executeMsg(this, msg);
		}
	}

	@Override
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
	}

	@Override
	public int getTickStatus()
	{
		return tickStatus;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if (removeFromGame)
			return false;
		tickStatus = Tickable.STATUS_START;
		if (tickID == Tickable.TICKID_MOB)
		{
			final boolean isMonster = isMonster();
			if (amDead)
			{
				boolean isOk = !removeFromGame;
				tickStatus = Tickable.STATUS_DEAD;
				if (isMonster)
				{
					if ((phyStats().rejuv() != PhyStats.NO_REJUV) && (basePhyStats().rejuv() > 0))
					{
						phyStats().setRejuv(phyStats().rejuv() - 1);
						if ((phyStats().rejuv() < 0) || (CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN)))
						{
							tickStatus = Tickable.STATUS_REBIRTH;
							cloneFix(CMClass.getMOBPrototype(ID()));
							bringToLife(CMLib.map().getStartRoom(this), true);
							final Room room = location();
							if (room != null)
							{
								final Area A=room.getArea();
								if ((lastTickedTime < 0)
								&& room.getMobility()
								&& (A.getAreaState() != Area.State.FROZEN)
								&& (A.getAreaState() != Area.State.STOPPED))
									lastTickedTime = CMLib.utensils().processVariableEquipment(this);
								room.showOthers(this, null, CMMsg.MSG_OK_ACTION, _("<S-NAME> appears!"));
							}
						}
					}
					else
					{
						tickStatus = Tickable.STATUS_END;
						if (soulMate() == null)
							destroy();
						isOk = false;
					}
				}
				tickStatus = Tickable.STATUS_NOT;
				lastTickedTime = System.currentTimeMillis();
				return isOk;
			}
			else
			if (location() != null)
			{
				final Room R=location();
				final Area A=R.getArea();
				// handle variable equipment!
				if ((lastTickedTime < 0)
				&& isMonster && R.getMobility()
				&& (A.getAreaState() != Area.State.FROZEN)
				&& (A.getAreaState() != Area.State.STOPPED))
				{
					if (lastTickedTime == -1)
						lastTickedTime = CMLib.utensils().processVariableEquipment(this);
					else
						lastTickedTime++;
				}

				tickStatus = Tickable.STATUS_ALIVE;

				if((CMProps.getIntVar(CMProps.Int.COMBATSYSTEM) == CombatLibrary.COMBAT_TURNBASED) && isInCombat())
				{
					if(CMLib.combat().doTurnBasedCombat(this,R,A))
					{
						if (lastTickedTime >= 0)
							lastTickedTime = System.currentTimeMillis();
						tickStatus = Tickable.STATUS_NOT;
						return !removeFromGame;
					}
				}
				else
				{
					if (commandQueSize() == 0)
						setActions(actions() - Math.floor(actions()));
					setActions(actions() + (CMLib.flags().isSitting(this) ? phyStats().speed() / 2.0 : phyStats().speed()));
				}

				if ((--recoverTickCter) <= 0)
				{
					CMLib.combat().recoverTick(this);
					recoverTickCter = CMProps.getIntVar(CMProps.Int.RECOVERRATE) * CharState.REAL_TICK_ADJUST_FACTOR;
				}
				if (!isMonster)
					CMLib.combat().expendEnergy(this, false);

				if(!CMLib.flags().isGolem(this))
				{
					if (!CMLib.flags().canBreathe(this))
					{
						R.show(this, this, CMMsg.MSG_OK_VISUAL, ("^Z<S-NAME> can't breathe!^.^?") + CMLib.protocol().msp("choke.wav", 10));
						CMLib.combat().postDamage(this, this, null,
								(int) Math.round(CMath.mul(Math.random(), basePhyStats().level() + 2)),
								CMMsg.MASK_ALWAYS | CMMsg.TYP_WATER, -1, null);
					}
					else
					if(!CMLib.flags().canBreatheHere(this,R))
					{
						final int atmo=R.getAtmosphere();
						if((atmo&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
						{
							R.show(this, this, CMMsg.MSG_OK_VISUAL, ("^Z<S-NAME> <S-IS-ARE> drowning in "+RawMaterial.CODES.NAME(atmo).toLowerCase()+"!^.^?") + CMLib.protocol().msp("choke.wav", 10));
							CMLib.combat().postDamage(this, this, null, (int) Math.round(CMath.mul(Math.random(), basePhyStats().level() + 2)), CMMsg.MASK_ALWAYS | CMMsg.TYP_WATER, -1, null);
						}
						else
						{
							R.show(this, this, CMMsg.MSG_OK_VISUAL, ("^Z<S-NAME> <S-IS-ARE> choking on "+RawMaterial.CODES.NAME(atmo).toLowerCase()+"!^.^?") + CMLib.protocol().msp("choke.wav", 10));
							CMLib.combat().postDamage(this, this, null, (int) Math.round(CMath.mul(Math.random(), basePhyStats().level() + 2)), CMMsg.MASK_ALWAYS | CMMsg.TYP_GAS, -1, null);
						}
					}
				}

				if (isInCombat())
				{
					if (CMProps.getIntVar(CMProps.Int.COMBATSYSTEM) == CombatLibrary.COMBAT_DEFAULT)
						setActions(actions() + 1.0); // bonus action is employed in default system
					tickStatus = Tickable.STATUS_FIGHT;
					peaceTime = 0;
					if(CMLib.flags().canAutoAttack(this))
						CMLib.combat().tickCombat(this);
				}
				else
				{
					peaceTime += CMProps.getTickMillis();
					if (CMath.bset(getBitmap(), MOB.ATT_AUTODRAW)
					&& (peaceTime >= START_SHEATH_TIME)
					&& (peaceTime < END_SHEATH_TIME) && (CMLib.flags().aliveAwakeMobileUnbound(this, true)))
						CMLib.commands().postSheath(this, true);
				}

				tickStatus = Tickable.STATUS_OTHER;
				if((!isMonster)&&(maxState().getFatigue()>Long.MIN_VALUE/2))
				{
					if (CMLib.flags().isSleeping(this))
						curState().adjFatigue(-CharState.REST_PER_SLEEP, maxState());
					else // rest/sit isn't here because fatigue is sleepiness, not exhaustion per se
					if (!CMSecurity.isAllowed(this, R, CMSecurity.SecFlag.IMMORT))
					{
						curState().adjFatigue(Math.round(CMProps.getTickMillis()), maxState());
						if (curState().getFatigue() > CharState.FATIGUED_MILLIS)
						{
							final boolean smallChance=(CMLib.dice().rollPercentage() == 1);
							if(smallChance && (!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
							{
								final Ability theYawns = CMClass.getAbility("Disease_Yawning");
								if (theYawns != null)
									theYawns.invoke(this, this, true, 0);
							}
							if (smallChance && curState().getFatigue() > (CharState.FATIGUED_EXHAUSTED_MILLIS))
							{
								R.show(this, null, CMMsg.MSG_OK_ACTION, _("<S-NAME> fall(s) asleep from exhaustion!!"));
								basePhyStats().setDisposition(basePhyStats().disposition() | PhyStats.IS_SLEEPING);
								phyStats().setDisposition(phyStats().disposition() | PhyStats.IS_SLEEPING);
							}
						}
					}
				}
				else
				while ((!amDead())  && (!amDestroyed) && dequeCommand())
				{
				}

				final Rideable riding = riding();
				if ((riding != null) && (CMLib.map().roomLocation(riding) != R))
					setRiding(null);

				if ((!isMonster) && (soulMate() == null))
				{
					CMLib.coffeeTables().bump(this, CoffeeTableRow.STAT_TICKSONLINE);
					if (((++tickCounter) * CMProps.getTickMillis()) >= AGE_MILLIS_THRESHOLD)
					{
						tickCounter = 0;
						if (inventory != null)
							inventory.trimToSize();
						if (affects != null)
							affects.trimToSize();
						if (abilitys != null)
							abilitys.trimToSize();
						if (followers != null)
							followers.trimToSize();
						CMLib.commands().tickAging(this, AGE_MILLIS_THRESHOLD);
					}
				}
			}

			tickStatus = Tickable.STATUS_AFFECT;
			eachEffect(new EachApplicable<Ability>()
			{
				@Override
				public final void apply(final Ability A)
				{
					if (!A.tick(ticking, tickID))
						A.unInvoke();
				}
			});

			manaConsumeCter = CMLib.commands().tickManaConsumption(this, manaConsumeCter);

			tickStatus = Tickable.STATUS_BEHAVIOR;
			eachBehavior(new EachApplicable<Behavior>()
			{
				@Override
				public final void apply(final Behavior B)
				{
					B.tick(ticking, tickID);
				}
			});
			tickStatus = Tickable.STATUS_SCRIPT;
			eachScript(new EachApplicable<ScriptingEngine>()
			{
				@Override
				public final void apply(final ScriptingEngine S)
				{
					S.tick(ticking, tickID);
				}
			});
			if (isMonster)
			{
				for (final Enumeration<Faction.FData> t = factions.elements(); t.hasMoreElements();)
				{
					final Faction.FData T = t.nextElement();
					if (T.requiresUpdating())
					{
						final String factionID = T.getFaction().factionID();
						final Faction F = CMLib.factions().getFaction(factionID);
						if (F != null)
						{
							final int oldValue = T.value();
							F.updateFactionData(this, T);
							T.setValue(oldValue);
						}
						else
							removeFaction(factionID);
					}
				}
			}

			tickStatus = Tickable.STATUS_OTHER;
			for (final Enumeration<Faction.FData> t = factions.elements(); t.hasMoreElements();)
			{
				final Faction.FData T = t.nextElement();
				T.tick(ticking, tickID);
			}

			final CharStats cStats = charStats();
			final int num = cStats.numClasses();
			tickStatus = Tickable.STATUS_CLASS;
			for (int c = 0; c < num; c++)
				cStats.getMyClass(c).tick(ticking, tickID);
			tickStatus = Tickable.STATUS_RACE;
			cStats.getMyRace().tick(ticking, tickID);
			tickStatus = Tickable.STATUS_END;

			for (final Tattoo tattoo : tattoos)
				if ((tattoo != null) && (tattoo.tickDown > 0))
				{
					if (tattoo.tickDown == 1)
						delTattoo(tattoo);
					else
						tattoo.tickDown--;
				}
		}

		if (lastTickedTime >= 0)
			lastTickedTime = System.currentTimeMillis();
		tickStatus = Tickable.STATUS_NOT;
		return !removeFromGame;
	}

	@Override
	public boolean isPlayer()
	{
		return playerStats != null;
	}

	@Override
	public boolean isMonster()
	{
		return (mySession == null) || (mySession.isFake());
	}

	@Override
	public boolean isPossessing()
	{
		try
		{
			for (final Session S : CMLib.sessions().allIterable())
				if ((S.mob() != null) && (S.mob().soulMate() == this))
					return true;
		}
		catch (final Exception e){}
		return false;
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public void addItem(Item item)
	{
		if ((item != null) && (!item.amDestroyed()))
		{
			item.setOwner(this);
			inventory.addElement(item);
			item.recoverPhyStats();
		}
	}

	@Override
	public void addItem(Item item, ItemPossessor.Expire expire)
	{
		addItem(item);
	}

	@Override
	public void delItem(Item item)
	{
		inventory.removeElement(item);
		item.recoverPhyStats();
	}

	@Override
	public void delAllItems(boolean destroy)
	{
		if (destroy)
			for (int i = numItems() - 1; i >= 0; i--)
			{
				final Item I = getItem(i);
				if (I != null)
				{
					// since were deleting you AND all your peers, no need for
					// Item to do it.
					I.setOwner(null);
					I.destroy();
				}
			}
		inventory.clear();
	}

	@Override
	public int numItems()
	{
		return inventory.size();
	}

	@Override
	public Enumeration<Item> items()
	{
		return inventory.elements();
	}

	@Override
	public boolean isContent(Item I)
	{
		return inventory.contains(I);
	}

	@Override
	public List<Item> findItems(Item goodLocation, String itemName)
	{
		if (inventory.size() == 0)
			return new Vector<Item>(1);
		List<Item> items = CMLib.english().fetchAvailableItems(inventory, itemName, goodLocation, Wearable.FILTER_ANY,
				true);
		if (items.size() == 0)
			items = CMLib.english().fetchAvailableItems(inventory, itemName, goodLocation, Wearable.FILTER_ANY, false);
		return items;
	}

	@Override
	public Item getItem(int index)
	{
		try
		{
			return inventory.elementAt(index);
		}
		catch (final java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}

	@Override
	public void eachItem(final EachApplicable<Item> applier)
	{
		final List<Item> contents = this.inventory;
		if (contents != null)
			try
			{
				for (int a = 0; a < contents.size(); a++)
				{
					final Item I = contents.get(a);
					if (I != null)
						applier.apply(I);
				}
			}
			catch (final ArrayIndexOutOfBoundsException e){}
	}

	@Override
	public Item getRandomItem()
	{
		if (numItems() == 0)
			return null;
		return getItem(CMLib.dice().roll(1, numItems(), -1));
	}

	public Item fetchFromInventory(Item goodLocation, String itemName, Filterer<Environmental> filter, boolean respectLocationAndWornCode)
	{
		if (inventory.size() == 0)
			return null;
		final SVector inv = inventory;
		Item item = null;
		if (respectLocationAndWornCode)
		{
			item = CMLib.english().fetchAvailableItem(inv, itemName, goodLocation, filter, true);
			if (item == null)
				item = CMLib.english().fetchAvailableItem(inv, itemName, goodLocation, filter, false);
		}
		else
		{
			item = (Item) CMLib.english().fetchEnvironmental(inv, itemName, true);
			if (item == null)
				item = (Item) CMLib.english().fetchEnvironmental(inv, itemName, false);
		}
		return item;
	}

	@Override
	public Item findItem(String itemName)
	{
		return fetchFromInventory(null, itemName, Wearable.FILTER_ANY, false);
	}

	@Override
	public Item findItem(Item goodLocation, String itemName)
	{
		return fetchFromInventory(goodLocation, itemName, Wearable.FILTER_ANY, true);
	}

	@Override
	public Item fetchItem(Item goodLocation, Filterer<Environmental> filter, String itemName)
	{
		return fetchFromInventory(goodLocation, itemName, filter, true);
	}

	@Override
	public List<Item> findItems(final String itemName)
	{
		if (inventory.size() > 0)
		{
			List V = CMLib.english().fetchEnvironmentals(inventory, itemName, true);
			if ((V != null) && (V.size() > 0))
				return V;
			V = CMLib.english().fetchEnvironmentals(inventory, itemName, false);
			if (V != null)
				return V;
		}
		return new Vector(1);
	}

	@Override
	public void addFollower(MOB follower, int order)
	{
		if (follower != null)
		{
			if (followers == null)
				followers = new SVector<Follower>();
			else
				for (final Follower F : followers)
					if (F.follower == follower)
					{
						F.marchingOrder = order;
						return;
					}
			followers.add(new Follower(follower, order));
		}
	}

	@Override
	public void delFollower(final MOB follower)
	{
		if ((follower != null) && (followers != null))
		{
			for (final Follower F : followers)
				if (F.follower == follower)
					followers.remove(F);
		}
	}

	@Override
	public int numFollowers()
	{
		return (followers == null) ? 0 : followers.size();
	}

	@Override
	public Enumeration<Follower> followers()
	{
		return (followers == null) ? EmptyEnumeration.INSTANCE : followers.elements();
	}

	@Override
	public int fetchFollowerOrder(final MOB thisOne)
	{
		for (final Enumeration<Follower> f = followers(); f.hasMoreElements();)
		{
			final Follower F = f.nextElement();
			if (F.follower == thisOne)
				return F.marchingOrder;
		}
		return -1;
	}

	@Override
	public MOB fetchFollower(final String named)
	{
		if (followers == null)
			return null;
		final List<MOB> list = new ConvertingList<Follower, MOB>(followers, Follower.converter);
		MOB mob = (MOB) CMLib.english().fetchEnvironmental(list, named, true);
		if (mob == null)
			mob = (MOB) CMLib.english().fetchEnvironmental(list, named, false);
		return mob;
	}

	@Override
	public MOB fetchFollower(final int index)
	{
		try
		{
			if (followers == null)
				return null;
			return followers.get(index).follower;
		}
		catch (final java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}

	@Override
	public boolean isFollowedBy(final MOB thisOne)
	{
		for (final Enumeration<Follower> f = followers(); f.hasMoreElements();)
		{
			final Follower F = f.nextElement();
			if (F.follower == thisOne)
				return true;
		}
		return false;
	}

	@Override
	public boolean willFollowOrdersOf(MOB mob)
	{
		if ((amFollowing() == mob)
		|| ((isMonster() && CMSecurity.isAllowed(mob, location(), CMSecurity.SecFlag.ORDER)))
		|| (getLiegeID().equals(mob.Name()))
		|| (CMLib.law().doesOwnThisProperty(mob, CMLib.map().getStartRoom(this))))
			return true;
		if ((!isMonster())
		&& (CMSecurity.isAllowedEverywhere(mob, CMSecurity.SecFlag.ORDER))
		&& ((!CMSecurity.isASysOp(this)) || CMSecurity.isASysOp(mob)))
			return true;
		for(final Triad<Clan,Integer,Integer> t : CMLib.clans().findCommonRivalrousClans(this, mob))
		{
			final Clan C=t.first;
			final int myRole=t.second.intValue();
			final int hisRole=t.third.intValue();
			if ((C.getAuthority(hisRole, Clan.Function.ORDER_UNDERLINGS) != Clan.Authority.CAN_NOT_DO)
			&& (C.doesOutRank(hisRole, myRole)))
				return true;
			else
			if ((isMonster())
			&& (C.getAuthority(hisRole, Clan.Function.ORDER_CONQUERED) != Clan.Authority.CAN_NOT_DO)
			&& (getStartRoom() != null))
			{
				final LegalBehavior B = CMLib.law().getLegalBehavior(getStartRoom());
				if ((B != null) && (mob.getClanRole(B.rulingOrganization())!=null))
					return true;
			}
		}
		return false;
	}

	@Override
	public MOB amUltimatelyFollowing()
	{
		MOB following = amFollowing;
		if (following == null)
			return null;
		final HashSet seen = new HashSet();
		while ((following != null) && (following.amFollowing() != null) && (!seen.contains(following)))
		{
			seen.add(following);
			following = following.amFollowing();
		}
		return following;
	}

	@Override
	public MOB amFollowing()
	{
		final MOB following = amFollowing;
		if (following != null)
		{
			if (!following.isFollowedBy(this))
				amFollowing = null;
		}
		return amFollowing;
	}

	@Override
	public void setFollowing(MOB mob)
	{
		if ((amFollowing != null) && (amFollowing != mob))
		{
			if (amFollowing.isFollowedBy(this))
				amFollowing.delFollower(this);
		}
		if (mob != null)
		{
			if (!mob.isFollowedBy(this))
				mob.addFollower(this, -1);
		}
		amFollowing = mob;
	}

	@Override
	public Set<MOB> getRideBuddies(Set<MOB> list)
	{
		if (list == null)
			return list;
		if (!list.contains(this))
			list.add(this);
		if (riding() != null)
			riding().getRideBuddies(list);
		return list;
	}

	@Override
	public Set<MOB> getGroupMembers(Set<MOB> list)
	{
		if (list == null)
			return list;
		if (!list.contains(this))
			list.add(this);
		final MOB following = amFollowing();
		if ((following != null) && (!list.contains(following)))
			following.getGroupMembers(list);
		for (final Enumeration<Follower> f = followers(); f.hasMoreElements();)
		{
			final Follower F = f.nextElement();
			if ((F.follower != null) && (!list.contains(F.follower)))
				F.follower.getGroupMembers(list);
		}
		return list;
	}

	@Override
	public boolean isSavable()
	{
		if ((!isMonster()) && (soulMate() == null))
			return false;
		if (!CMLib.flags().isSavable(this))
			return false;
		if (CMLib.utensils().getMobPossessingAnother(this) != null)
			return false;
		final MOB followed = amFollowing();
		if (followed != null)
			if (!followed.isMonster())
				return false;
		return true;

	}

	@Override
	public void setSavable(boolean truefalse)
	{
		CMLib.flags().setSavable(this, truefalse);
	}

	@Override
	public MOB soulMate()
	{
		return soulMate;
	}

	@Override
	public void setSoulMate(MOB mob)
	{
		soulMate = mob;
	}

	@Override
	public void addAbility(Ability to)
	{
		if (to == null)
			return;
		if(abilitys.find(to.ID())!=null)
			return;
		abilitys.addElement(to);
	}

	@Override
	public void delAbility(Ability to)
	{
		abilitys.removeElement(to);
	}

	@Override
	public void delAllAbilities()
	{
		abilitys.clear();
		abilityUseCache.clear();
	}

	@Override
	public int numAbilities()
	{
		return abilitys.size();
	}

	@Override
	public Enumeration<Ability> abilities()
	{
		return abilitys.elements();
	}

	@Override
	public Enumeration<Ability> allAbilities()
	{
		final MultiListEnumeration multi = new MultiListEnumeration(new List[] { abilitys,
				charStats().getMyRace().racialAbilities(this) });
		for(final Pair<Clan,Integer> p : clans())
			multi.addEnumeration(p.first.clanAbilities(this));
		return multi;
	}

	@Override
	public int numAllAbilities()
	{
		int size=abilitys.size() + charStats().getMyRace().racialAbilities(this).size();
		for(final Pair<Clan,Integer> p : clans())
			size+=p.first.clanAbilities(this).size();
		return size;
	}

	@Override
	public Ability fetchRandomAbility()
	{
		if (numAllAbilities() == 0)
			return null;
		return fetchAbility(CMLib.dice().roll(1, numAllAbilities(), -1));
	}

	@Override
	public Ability fetchAbility(int index)
	{
		try
		{
			if (index < abilitys.size())
				return abilitys.elementAt(index);
			final List<Ability> racialAbilities = charStats().getMyRace().racialAbilities(this);
			if (index < abilitys.size() + racialAbilities.size())
				return racialAbilities.get(index - abilitys.size());
			index-=(abilitys.size() + racialAbilities.size());
			for(final Pair<Clan,Integer> p : clans())
			{
				final SearchIDList<Ability> list = p.first.clanAbilities(this);
				if(index<list.size())
					return list.get(index);
				index-=list.size();
			}
		}
		catch (final java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}

	@Override
	public Ability fetchAbility(String ID)
	{
		Ability A=abilitys.find(ID);
		if(A!=null) return A;
		for(final Pair<Clan,Integer> p : clans())
		{
			A=p.first.clanAbilities(this).find(ID);
			if(A!=null) return A;
		}
		final Race R = charStats().getMyRace();
		A=R.racialAbilities(this).find(ID);
		if(A!=null) return A;
		for (final Enumeration<Ability> a = allAbilities(); a.hasMoreElements();)
		{
			A = a.nextElement();
			if (A.Name().equalsIgnoreCase(ID))
				return A;
		}
		return null;
	}

	@Override
	public Ability findAbility(String ID)
	{
		final Race R = charStats().getMyRace();
		Ability A = (Ability) CMLib.english().fetchEnvironmental(abilitys, ID, true);
		if (A == null)
			A = (Ability) CMLib.english().fetchEnvironmental(R.racialAbilities(this), ID, true);
		if (A == null)
			for(final Pair<Clan,Integer> p : clans())
			{
				A = (Ability) CMLib.english().fetchEnvironmental(p.first.clanAbilities(this), ID, true);
				if(A!=null) return A;
			}
		if (A == null)
			A = (Ability) CMLib.english().fetchEnvironmental(abilitys, ID, false);
		if (A == null)
			A = (Ability) CMLib.english().fetchEnvironmental(R.racialAbilities(this), ID, false);
		if (A == null)
			for(final Pair<Clan,Integer> p : clans())
			{
				A = (Ability) CMLib.english().fetchEnvironmental(p.first.clanAbilities(this), ID, false);
				if(A!=null) return A;
			}
		if (A == null)
			A = fetchAbility(ID);
		return A;
	}

	protected final List<Ability> racialEffects()
	{
		if (racialAffects == null)
			racialAffects = charStats.getMyRace().racialEffects(this);
		return racialAffects;
	}

	protected final List<Ability> clanEffects()
	{
		List<Ability> affects=clanAffects;
		if (affects == null)
		{
			final Iterator<Pair<Clan,Integer>> c=clans().iterator();
			if(!c.hasNext())
				affects = CMLib.clans().getDefaultGovernment().getClanLevelEffects(this, null, null);
			else
			{
				final ReadOnlyMultiList<Ability> effects=new ReadOnlyMultiList<Ability>();
				for(;c.hasNext();)
					effects.addList(c.next().first.clanEffects(this));
				affects=effects;
			}
			clanAffects=affects;
		}
		return affects;
	}
	@Override
	public Iterable<Pair<Clan, Integer>> clans()
	{
		return this.clans;
	}

	@Override
	public Pair<Clan, Integer> getClanRole(String clanID)
	{
		if((clanID==null)||(clanID.length()==0))
			return null;
		return clans.get(clanID);
	}

	@Override
	public void setClan(String clanID, int role)
	{
		if((clanID==null)||(clanID.length()==0))
		{
			if(role==Integer.MIN_VALUE)
			{
				clans.clear();
				clanAffects=null;
			}
			return;
		}
		if(role<0)
		{
			final Pair<Clan,Integer> p=clans.get(clanID);
			if(p!=null)
			{
				clans.remove(clanID);
				clanAffects=null;
			}
		}
		else
		{
			Pair<Clan,Integer> p=clans.get(clanID);
			if(p==null)
			{
				final Clan C=CMLib.clans().getClan(clanID);
				if(C==null)
					Log.errOut("StdMOB","Unknown clan: "+clanID+" on "+Name()+" in "+CMLib.map().getExtendedRoomID(location()));
				else
				{
					p=new Pair<Clan,Integer>(C,Integer.valueOf(role));
					clans.put(clanID, p);
					clanAffects=null;
				}
			}
			else
			{
				if(p.second.intValue()!=role)
					p.second=Integer.valueOf(role);
				clans.put(clanID, p);
			}
		}
	}

	@Override
	public void addNonUninvokableEffect(Ability to)
	{
		if (to == null)
			return;
		if (fetchEffect(to.ID()) != null)
			return;
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}

	@Override
	public void addPriorityEffect(Ability to)
	{
		if (to == null)
			return;
		if (fetchEffect(to.ID()) != null)
			return;
		if (affects.size() == 0)
			affects.addElement(to);
		else
			affects.insertElementAt(to, 0);
		to.setAffectedOne(this);
	}

	@Override
	public void addEffect(Ability to)
	{
		if (to == null)
			return;
		if (fetchEffect(to.ID()) != null)
			return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}

	@Override
	public void delEffect(Ability to)
	{
		if (affects.removeElement(to))
			to.setAffectedOne(null);
	}

	@Override
	public void eachEffect(final EachApplicable<Ability> applier)
	{
		final List<Ability> affects = this.affects;
		if (affects != null)
			try
			{
				for (int a = 0; a < affects.size(); a++)
					applier.apply(affects.get(a));
			}
			catch (final ArrayIndexOutOfBoundsException e){}
		final List<Ability> racialEffects = racialEffects();
		try
		{
			if (racialEffects.size() > 0)
				for (final Ability A : racialEffects)
					applier.apply(A);
		}
		catch (final ArrayIndexOutOfBoundsException e){}
		try
		{
			for (final Ability A : clanEffects())
				applier.apply(A);
		}
		catch (final ArrayIndexOutOfBoundsException e){}
	}

	@Override
	public void delAllEffects(boolean unInvoke)
	{
		for (int a = numEffects() - 1; a >= 0; a--)
		{
			final Ability A = fetchEffect(a);
			if (A != null)
			{
				if (unInvoke)
					A.unInvoke();
				A.setAffectedOne(null);
			}
		}
		affects.clear();
	}

	@Override
	public int numAllEffects()
	{
		int size=affects.size() + charStats().getMyRace().numRacialEffects(this);
		for(final Pair<Clan,Integer> p : clans())
			size+=p.first.numClanEffects(this);
		return size;
	}

	@Override
	public int numEffects()
	{
		return affects.size();
	}

	@Override
	public Ability fetchEffect(int index)
	{
		try
		{
			if (index < affects.size())
				return affects.elementAt(index);
			if (index < abilitys.size() + charStats().getMyRace().numRacialEffects(this))
				return racialEffects().get(index - affects.size());
			return clanEffects().get(index - affects.size() - racialEffects().size());
		}
		catch (final java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}

	@Override
	public Ability fetchEffect(String ID)
	{
		for (final Enumeration<Ability> a = effects(); a.hasMoreElements();)
		{
			final Ability A = a.nextElement();
			if (A.ID().equals(ID))
				return A;
		}
		return null;
	}

	@Override
	public Enumeration<Ability> personalEffects()
	{
		return affects.elements();
	}

	@Override
	public Enumeration<Ability> effects()
	{
		return new MultiListEnumeration(new List[] { affects, racialEffects(), clanEffects() });
	}

	/**
	 * Manipulation of Behavior objects, which includes movement, speech,
	 * spellcasting, etc, etc.
	 */
	@Override
	public void addBehavior(Behavior to)
	{
		if (to == null)
			return;
		if (fetchBehavior(to.ID()) != null)
			return;
		to.startBehavior(this);
		behaviors.addElement(to);
	}

	@Override
	public void delBehavior(Behavior to)
	{
		behaviors.removeElement(to);
	}

	@Override
	public void delAllBehaviors()
	{
		behaviors.clear();
	}

	@Override
	public int numBehaviors()
	{
		return behaviors.size();
	}

	@Override
	public Enumeration<Behavior> behaviors()
	{
		return behaviors.elements();
	}

	@Override
	public Behavior fetchBehavior(int index)
	{
		try
		{
			return behaviors.elementAt(index);
		}
		catch (final java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}

	@Override
	public Behavior fetchBehavior(String ID)
	{
		return behaviors.find(ID);
	}

	@Override
	public void eachBehavior(final EachApplicable<Behavior> applier)
	{
		final List<Behavior> behaviors = this.behaviors;
		if (behaviors != null)
			try
			{
				for (int a = 0; a < behaviors.size(); a++)
					applier.apply(behaviors.get(a));
			}
			catch (final ArrayIndexOutOfBoundsException e){}
	}

	@Override
	public int[][] getAbilityUsageCache(final String abilityID)
	{
		int[][] ableCache=abilityUseCache.get(abilityID);
		if(ableCache==null)
		{
			ableCache=new int[Ability.CACHEINDEX_TOTAL][];
			abilityUseCache.put(abilityID, ableCache);
		}
		if((phyStats().level()!=abilityUseTrig[0])
		||(charStats().getCurrentClassLevel()!=abilityUseTrig[1])
		||(charStats().getCurrentClass().hashCode()!=abilityUseTrig[2]))
		{
			clearAbilityUsageCache();
			abilityUseTrig[0]=phyStats().level();
			abilityUseTrig[1]=charStats().getCurrentClassLevel();
			abilityUseTrig[2]=charStats().getCurrentClass().hashCode();
		}
		return ableCache;
	}

	private void clearAbilityUsageCache()
	{
		Arrays.fill(abilityUseTrig, 0);
		abilityUseCache.clear();
	}

	@Override public void addExpertise(String code)
	{
		final Entry<String,Integer> p=CMath.getStringFollowedByNumber(code, true);
		final String key=p.getKey().toUpperCase();
		final Integer oldNum=expertises.get(key);
		if((oldNum==null) || ((p.getValue()!=null) && (oldNum.intValue()<p.getValue().intValue())))
		{
			expertises.put(key, p.getValue());
			clearAbilityUsageCache();
		}
	}

	@Override public void delExpertise(String baseCode)
	{
		if(baseCode==null) return;
		if(expertises.remove(baseCode.toUpperCase())==null)
		{
			final Entry<String,Integer> p=CMath.getStringFollowedByNumber(baseCode, true);
			if(expertises.remove(p.getKey().toUpperCase())!=null)
				clearAbilityUsageCache();
		}
		else
			clearAbilityUsageCache();
	}

	@Override public Pair<String, Integer> fetchExpertise(final String baseCode)
	{
		if(baseCode==null) return null;
		final Entry<String,Integer> p=CMath.getStringFollowedByNumber(baseCode, true);
		final String key=p.getKey().toUpperCase();
		final Integer num=expertises.get(key);
		if((expertises.containsKey(key))
		&&((num==null)||(p.getValue()==null)||(p.getValue().intValue()<=num.intValue())))
		{
			final Integer i=(p.getValue()==null)?num:p.getValue();
			return new Pair<String,Integer>(key,i);
		}
		return null;
	}

	@Override public void delAllExpertises()
	{
		if(expertises.size()>0)
		{
			expertises.clear();
			clearAbilityUsageCache();
		}
	}

	@Override public Enumeration<String> expertises()
	{
		return new Enumeration<String>()
		{
			final Iterator<Entry<String,Integer>> i=expertises.entrySet().iterator();
			@Override public boolean hasMoreElements() { return i.hasNext(); }
			@Override public String nextElement()
			{
				final Entry<String,Integer> s=i.next();
				if(s.getValue()==null)
					return s.getKey();
				return s.getKey()+s.getValue().toString();
			}
		};
	}


	/** Manipulation of the scripts list */
	@Override
	public void addScript(ScriptingEngine S)
	{
		if (S == null)
			return;
		if (!scripts.contains(S))
		{
			for (final ScriptingEngine S2 : scripts)
				if (S2.getScript().equalsIgnoreCase(S.getScript()))
					return;
			scripts.addElement(S);
		}
	}

	@Override
	public void delScript(ScriptingEngine S)
	{
		if (S == null)
			return;
		scripts.removeElement(S);
	}

	@Override
	public void delAllScripts()
	{
		scripts.clear();
	}

	@Override
	public int numScripts()
	{
		return (scripts == null) ? 0 : scripts.size();
	}

	@Override
	public Enumeration<ScriptingEngine> scripts()
	{
		return (scripts == null) ? EmptyEnumeration.INSTANCE : scripts.elements();
	}

	@Override
	public ScriptingEngine fetchScript(int x)
	{
		try
		{
			return scripts.elementAt(x);
		}
		catch (final Exception e){}
		return null;
	}

	@Override
	public void eachScript(final EachApplicable<ScriptingEngine> applier)
	{
		final List<ScriptingEngine> scripts = this.scripts;
		if (scripts != null)
			try
			{
				for (int a = 0; a < scripts.size(); a++)
				{
					final ScriptingEngine S = scripts.get(a);
					if (S != null)
						applier.apply(S);
				}
			}
			catch (final ArrayIndexOutOfBoundsException e){}
	}

	/** Manipulation of the tatoo list */
	@Override
	public void addTattoo(Tattoo of)
	{
		if ((of == null) || (of.tattooName == null) || (of.tattooName.length() == 0)
				|| findTattoo(of.tattooName) != null)
			return;
		tattoos.addElement(of);
	}

	@Override
	public void delTattoo(Tattoo of)
	{
		if ((of == null) || (of.tattooName == null) || (of.tattooName.length() == 0))
			return;
		final Tattoo tat = findTattoo(of.tattooName);
		if (tat == null)
			return;
		tattoos.remove(tat);
	}

	@Override
	public Enumeration<Tattoo> tattoos()
	{
		return tattoos.elements();
	}

	@Override
	public Tattoo findTattoo(String of)
	{
		if ((of == null) || (of.length() == 0))
			return null;
		return tattoos.find(of.trim());
	}

	/** Manipulation of the factions list */
	@Override
	public void addFaction(String which, int start)
	{
		which = which.toUpperCase();
		final Faction F = CMLib.factions().getFaction(which);
		if (F == null)
			return;
		if (start > F.maximum())
			start = F.maximum();
		if (start < F.minimum())
			start = F.minimum();
		which = F.factionID().toUpperCase();
		Faction.FData data = factions.get(which);
		if (data == null)
		{
			data = F.makeFactionData(this);
			factions.put(which, data);
		}
		data.setValue(start);
	}

	@Override
	public void adjustFaction(String which, int amount)
	{
		which = which.toUpperCase();
		final Faction F = CMLib.factions().getFaction(which);
		if (F == null)
			return;
		which = F.factionID().toUpperCase();
		if (!factions.containsKey(which))
			addFaction(which, amount);
		else
			addFaction(which, fetchFaction(which) + amount);
	}

	@Override
	public Enumeration<String> fetchFactions()
	{
		return factions.keys();
	}

	@Override
	public int fetchFaction(String which)
	{
		final Faction.FData data = factions.get(which.toUpperCase());
		if (data == null)
			return Integer.MAX_VALUE;
		return data.value();
	}

	@Override
	public void removeFaction(String which)
	{
		factions.remove(which.toUpperCase());
	}

	@Override
	public void copyFactions(MOB source)
	{
		for (final Enumeration e = source.fetchFactions(); e.hasMoreElements();)
		{
			final String fID = (String) e.nextElement();
			addFaction(fID, source.fetchFaction(fID));
		}
	}

	@Override
	public boolean hasFaction(String which)
	{
		final Faction F = CMLib.factions().getFaction(which);
		if (F == null)
			return false;
		return factions.containsKey(F.factionID().toUpperCase());
	}

	@Override
	public List<String> fetchFactionRanges()
	{
		final Vector<String> V = new Vector<String>(factions.size());
		for (final Enumeration e = fetchFactions(); e.hasMoreElements();)
		{
			final Faction F = CMLib.factions().getFaction((String) e.nextElement());
			if (F == null)
				continue;
			final Faction.FRange FR = CMLib.factions().getRange(F.factionID(), fetchFaction(F.factionID()));
			if (FR != null)
				V.addElement(FR.codeName());
		}
		return V;
	}

	@Override
	public int freeWearPositions(long wornCode, short belowLayer, short layerAttributes)
	{
		int x = getWearPositions(wornCode);
		if (x <= 0)
			return 0;
		x -= fetchWornItems(wornCode, belowLayer, layerAttributes).size();
		if (x <= 0)
			return 0;
		return x;
	}

	@Override
	public int getWearPositions(long wornCode)
	{
		if ((charStats().getWearableRestrictionsBitmap() & wornCode) > 0)
			return 0;
		if (wornCode == Wearable.WORN_FLOATING_NEARBY)
			return 6;
		int total;
		int add = 0;
		boolean found = false;
		for (int i = 0; i < Race.BODY_WEARGRID.length; i++)
		{
			if ((Race.BODY_WEARGRID[i][0] > 0)
			&& ((Race.BODY_WEARGRID[i][0] & wornCode) == wornCode))
			{
				found = true;
				total = charStats().getBodyPart(i);
				if (Race.BODY_WEARGRID[i][1] < 0)
				{
					if (total > 0)
						return 0;
				}
				else
				if (total < 1)
				{
					return 0;
				}
				else
				if (i == Race.BODY_HAND)
				{
					// casting is ok here since these are all originals
					// that fall below the int/long fall.
					if (wornCode > Integer.MAX_VALUE)
						add += total;
					else
						switch ((int) wornCode)
						{
						case (int) Wearable.WORN_HANDS:
							if (total < 2)
								add += 1;
							else
								add += total / 2;
							break;
						case (int) Wearable.WORN_WIELD:
						case (int) Wearable.WORN_RIGHT_FINGER:
						case (int) Wearable.WORN_RIGHT_WRIST:
							add += 1;
							break;
						case (int) Wearable.WORN_HELD:
						case (int) Wearable.WORN_LEFT_FINGER:
						case (int) Wearable.WORN_LEFT_WRIST:
							add += total - 1;
							break;
						default:
							add += total;
							break;
						}
				}
				else
				{
					final int num = total / ((int) Race.BODY_WEARGRID[i][1]);
					if (num < 1)
						add += 1;
					else
						add += num;
				}
			}
		}
		if (!found)
			return 1;
		return add;
	}

	@Override
	public List<Item> fetchWornItems(long wornCode, short aboveOrAroundLayer, short layerAttributes)
	{
		final Vector<Item> V = new Vector();
		final boolean equalOk = (layerAttributes & Armor.LAYERMASK_MULTIWEAR) > 0;
		int lay = 0;
		for (final Enumeration<Item> i = items(); i.hasMoreElements();)
		{
			final Item thisItem = i.nextElement();
			if (thisItem.amWearingAt(wornCode))
			{
				if (thisItem instanceof Armor)
				{
					lay = ((Armor) thisItem).getClothingLayer();
					if (lay >= (aboveOrAroundLayer - 1))
					{
						if (((lay > aboveOrAroundLayer - 2)
							&& (lay < aboveOrAroundLayer + 2)
							&& ((!equalOk) || ((((Armor) thisItem).getLayerAttributes() & Armor.LAYERMASK_MULTIWEAR) == 0)))
						|| (lay > aboveOrAroundLayer))
							V.addElement(thisItem);
					}
				}
				else
					V.addElement(thisItem);
			}
		}
		return V;
	}

	@Override
	public boolean hasOnlyGoldInInventory()
	{
		for (int i = 0; i < numItems(); i++)
		{
			final Item I = getItem(i);
			if (I.amWearingAt(Wearable.IN_INVENTORY)
			&& ((I.container() == null) || (I.ultimateContainer(null).amWearingAt(Wearable.IN_INVENTORY)))
			&& (!(I instanceof Coins)))
				return false;
		}
		return true;
	}

	@Override
	public Item fetchFirstWornItem(long wornCode)
	{
		for (final Enumeration<Item> i = items(); i.hasMoreElements();)
		{
			final Item thisItem = i.nextElement();
			if (thisItem.amWearingAt(wornCode))
				return thisItem;
		}
		return null;
	}

	@Override
	public Item fetchWieldedItem()
	{
		final WeakReference<Item> wieldRef = possWieldedItem;
		if (wieldRef != null)
		{
			final Item I = wieldRef.get();
			if (I == null)
				return null;
			if ((I.owner() == this) && (I.amWearingAt(Wearable.WORN_WIELD)) && (!I.amDestroyed())
					&& (I.container() == null))
				return I;
			possWieldedItem = null;
		}
		for (final Enumeration<Item> i = items(); i.hasMoreElements();)
		{
			final Item I = i.nextElement();
			if ((I != null) && (I.owner() == this) && (I.amWearingAt(Wearable.WORN_WIELD)) && (I.container() == null))
			{
				possWieldedItem = new WeakReference(I);
				return I;
			}
		}
		possWieldedItem = new WeakReference(null);
		return null;
	}

	@Override
	public Item fetchHeldItem()
	{
		final WeakReference<Item> heldRef = possHeldItem;
		if (heldRef != null)
		{
			final Item I = heldRef.get();
			if (I == null)
				return null;
			if ((I.owner() == this)
			&& (I.amWearingAt(Wearable.WORN_HELD))
			&& (!I.amDestroyed())
			&& (I.container() == null))
				return I;
			possHeldItem = null;
		}
		for (final Enumeration<Item> i = items(); i.hasMoreElements();)
		{
			final Item I = i.nextElement();
			if ((I != null)
			&& (I.owner() == this)
			&& (I.amWearingAt(Wearable.WORN_HELD))
			&& (I.container() == null))
			{
				possHeldItem = new WeakReference(I);
				return I;
			}
		}
		possHeldItem = new WeakReference(null);
		return null;
	}

	@Override
	public boolean isMine(Environmental env)
	{
		if (env instanceof Item)
		{
			if (inventory.contains(env))
				return true;
			return false;
		}
		else
		if (env instanceof MOB)
		{
			if (isFollowedBy((MOB) env))
				return true;
			return false;
		}
		else
		if (env instanceof Ability)
		{
			if (abilitys.find(env.ID())==env)
				return true;
			if (affects.contains(env))
				return true;
			return false;
		}
		return false;
	}

	@Override
	public void moveItemTo(Item container, ItemPossessor.Expire expire, Move... moveFlags)
	{
		moveItemTo(container);
	}

	@Override
	public void moveItemTo(Item container)
	{
		// caller is responsible for recovering any env
		// stat changes!
		if (CMLib.flags().isHidden(container))
			container.basePhyStats().setDisposition( container.basePhyStats().disposition() & ((int) PhyStats.ALLMASK - PhyStats.IS_HIDDEN));

		// ensure its out of its previous place
		Environmental owner = location();
		if (container.owner() != null)
		{
			owner = container.owner();
			if (container.owner() instanceof Room)
				((Room) container.owner()).delItem(container);
			else
			if (container.owner() instanceof MOB)
				((MOB) container.owner()).delItem(container);
		}
		location().delItem(container);

		container.unWear();

		if (!isMine(container))
			addItem(container);
		container.recoverPhyStats();

		boolean nothingDone = true;
		boolean doBugFix = true;
		while (doBugFix || !nothingDone)
		{
			doBugFix = false;
			nothingDone = true;
			if (owner instanceof Room)
			{
				final Room R = (Room) owner;
				for (final Enumeration<Item> i = R.items(); i.hasMoreElements();)
				{
					final Item thisItem = i.nextElement();
					if (thisItem.container() == container)
					{
						moveItemTo(thisItem);
						nothingDone = false;
						break;
					}
				}
			}
			else
			if (owner instanceof MOB)
			{
				final MOB M = (MOB) owner;
				for (final Enumeration<Item> i = M.items(); i.hasMoreElements();)
				{
					final Item thisItem = i.nextElement();
					if (thisItem.container() == container)
					{
						moveItemTo(thisItem);
						nothingDone = false;
						break;
					}
				}
			}
		}
	}

	@Override
	public String _(final String str, final String ... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	protected static String[] CODES = { "CLASS", "LEVEL", "ABILITY", "TEXT" };

	@Override
	public String getStat(String code)
	{
		switch (getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return "" + basePhyStats().level();
		case 2:
			return "" + basePhyStats().ability();
		case 3:
			return text();
		}
		return "";
	}

	@Override
	public void setStat(String code, String val)
	{
		switch (getCodeNum(code))
		{
		case 0:
			return;
		case 1:
			basePhyStats().setLevel(CMath.s_parseIntExpression(val));
			break;
		case 2:
			basePhyStats().setAbility(CMath.s_parseIntExpression(val));
			break;
		case 3:
			setMiscText(val);
			break;
		}
	}

	@Override
	public int getSaveStatIndex()
	{
		return (xtraValues == null) ? getStatCodes().length : getStatCodes().length - xtraValues.length;
	}

	@Override
	public String[] getStatCodes()
	{
		return CODES;
	}

	@Override
	public boolean isStat(String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	protected int getCodeNum(String code)
	{
		for (int i = 0; i < CODES.length; i++)
			if (code.equalsIgnoreCase(CODES[i]))
				return i;
		return -1;
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if (!(E instanceof StdMOB))
			return false;
		final String[] codes = getStatCodes();
		for (int i = 0; i < codes.length; i++)
			if (!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}
