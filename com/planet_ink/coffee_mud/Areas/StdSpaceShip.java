package com.planet_ink.coffee_mud.Areas;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject.BoundedCube;
import com.planet_ink.coffee_mud.core.interfaces.Places;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
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
public class StdSpaceShip implements Area, SpaceShip
{
	private static final long STALE_AIR_INTERVAL = 5 * 60 * 1000;

	protected static Climate climateObj=null;

	protected String[]  	xtraValues  	= null;
	protected volatile int	mass			= -1;
	protected SpaceObject	spaceSource 	= null;
	protected String		imageName   	= "";
	protected RoomnumberSet properRoomIDSet = null;
	protected TimeClock 	localClock  	= (TimeClock)CMClass.getCommon("DefaultTimeClock");
	protected String		currency		= "";
	private long			expirationDate  = 0;
	protected int			atmosphere		= RawMaterial.RESOURCE_AIR; // at least for awhile...
	protected boolean   	amDestroyed 	= false;
	protected String		name			= "a space ship";
	protected LocationRoom  savedDock   	= null;
	protected String		displayText 	= "";
	protected String		description 	= "";
	protected String		miscText		= "";
	protected long 			radius			= 50;
	protected double		omlCoeff		= SpaceObject.ATMOSPHERIC_DRAG_STREAMLINE + ((SpaceObject.ATMOSPHERIC_DRAG_BRICK-SpaceObject.ATMOSPHERIC_DRAG_STREAMLINE)/2.0);
	protected SVector<Room> myRooms 		= new SVector();
	protected State			flag			= State.ACTIVE;
	protected int			tickStatus  	= Tickable.STATUS_NOT;
	protected String		author  		= ""; // will be used for owner, I guess.
	protected PhyStats  	phyStats		= (PhyStats)CMClass.getCommon("DefaultPhyStats");
	protected PhyStats  	basePhyStats	= (PhyStats)CMClass.getCommon("DefaultPhyStats");
	protected Area 			me			 	= this;
	protected SpaceShip		shipItem		= null;
	protected volatile long nextStaleCheck	= System.currentTimeMillis() + STALE_AIR_INTERVAL;
	protected volatile long nextStaleWarn	= System.currentTimeMillis() + (60 * 1000);

	protected SVector<Ability>  		affects			= new SVector<Ability>(1);
	protected SVector<Behavior> 		behaviors		= new SVector<Behavior>(1);
	protected SVector<ScriptingEngine>  scripts			= new SVector<ScriptingEngine>(1);
	protected SLinkedList<Area> 		parents			= new SLinkedList<Area>();
	protected STreeMap<String,String>   blurbFlags		= new STreeMap<String,String>();
	protected List<Pair<Room,Integer>>	shipExitCache	= new SLinkedList<Pair<Room,Integer>>();
	protected Set<String> 				staleAirList	= new HashSet<String>();

	@Override public String ID(){    return "StdSpaceShip";}

	@Override public void initializeClass(){}
	@Override public LocationRoom getIsDocked(){ return (LocationRoom)CMLib.map().getRoom(savedDock);}
	@Override public void setClimateObj(Climate obj){climateObj=obj;}
	@Override
	public Climate getClimateObj()
	{
		if(climateObj==null)
		{
			climateObj=(Climate)CMClass.getCommon("DefaultClimate");
			climateObj.setCurrentWeatherType(Climate.WEATHER_CLEAR);
			climateObj.setNextWeatherType(Climate.WEATHER_CLEAR);
		}
		return climateObj;
	}
	@Override public double getOMLCoeff() { return omlCoeff; }
	@Override public void setOMLCoeff(double coeff) { omlCoeff=coeff; }
	@Override public void setAuthorID(String authorID){author=authorID;}
	@Override public String getAuthorID(){return author;}
	@Override public TimeClock getTimeObj(){return localClock;}
	@Override public void setTimeObj(TimeClock obj){localClock=obj;}
	@Override public void setCurrency(String newCurrency){currency=newCurrency;}
	@Override public String getCurrency(){return currency;}
	@Override public long expirationDate(){return expirationDate;}
	@Override public void setExpirationDate(long time){expirationDate=time;}
	@Override public int getAtmosphereCode() { return atmosphere; }
	@Override public void setAtmosphere(int resourceCode) { atmosphere=resourceCode; }
	@Override public int getAtmosphere() { return atmosphere==ATMOSPHERE_INHERIT?RawMaterial.RESOURCE_AIR:atmosphere; }
	@Override public long radius() { return radius; }
	@Override public void setRadius(long radius) { this.radius=radius; }
	@Override public long flags(){return 0;}
	@Override public SpaceObject knownSource(){return spaceSource;}
	@Override
	public void setKnownSource(SpaceObject O)
	{
		if((O instanceof SpaceShip)&&(((SpaceShip)O).getShipArea()==this))
			shipItem=(SpaceShip)O;
		else
			spaceSource=O;
	}
	@Override public long[] coordinates()  { return (shipItem!=null)?shipItem.coordinates():new long[3]; }
	@Override public void setCoords(long[] coords) { if (shipItem!=null) shipItem.setCoords(coords); }
	@Override public double[] direction() { return (shipItem!=null)?shipItem.direction():new double[2]; }
	@Override public double roll() { return (shipItem!=null)?shipItem.roll():0; }
	@Override public void setRoll(double dir) { if (shipItem!=null) shipItem.setRoll(dir); }
	@Override public void setDirection(double[] dir) { if (shipItem!=null) shipItem.setDirection(dir); }
	@Override public double[] facing() { return (shipItem!=null)?shipItem.facing():new double[2]; }
	@Override public void setFacing(double[] dir) { if (shipItem!=null) shipItem.setFacing(dir); }
	@Override public long speed() { return (shipItem!=null)?shipItem.speed():0; }
	@Override public void setSpeed(long v) { if (shipItem!=null) shipItem.setSpeed(v); }
	@Override public SpaceObject knownTarget() { return (shipItem!=null)?shipItem.knownTarget():null; }
	@Override public void setKnownTarget(SpaceObject O) { if (shipItem!=null) shipItem.setKnownTarget(O); }

	@Override
	public BoundedCube getBounds()
	{
		return new BoundedObject.BoundedCube(coordinates(),radius());
	}

	@Override
	public long getMass()
	{
		final long mass=this.mass;
		if(mass<0)
		{
			int newMass=phyStats().weight();
			for(final Enumeration<Room> r=getProperMap(); r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if(R!=null)
				{
					for(int i=0;i<R.numItems();i++)
					{
						final Item I=R.getItem(i);
						if(I!=null)
							newMass += I.phyStats().weight();
					}
					for(int i=0;i<R.numInhabitants();i++)
					{
						final MOB M=R.fetchInhabitant(i);
						if(M!=null)
							newMass += M.phyStats().weight();
					}
				}
			}
			this.mass=newMass;
		}
		return mass;
	}

	@Override
	public void destroy()
	{
		CMLib.map().delObjectInSpace(this);
		CMLib.map().registerWorldObjectDestroyed(this,null,this);
		phyStats=(PhyStats)CMClass.getCommon("DefaultPhyStats");
		spaceSource=null;
		basePhyStats=phyStats;
		miscText=null;
		imageName=null;
		affects=null;
		behaviors=null;
		scripts=null;
		author=null;
		currency=null;
		parents=new SLinkedList<Area>();
		climateObj=null;
		amDestroyed=true;
	}
	@Override public boolean amDestroyed(){return amDestroyed;}
	@Override
	public boolean isSavable()
	{
		return ((!amDestroyed)
				&& (!CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
				&& (CMLib.flags().isSavable(this)));
	}
	@Override public void setSavable(boolean truefalse){CMLib.flags().setSavable(this, truefalse);}
	@Override public int getClimateTypeCode(){return Places.CLIMASK_NORMAL;}
	@Override public int getClimateType() { return Places.CLIMASK_NORMAL; }
	@Override public void setClimateType(int newClimateType){}

	public StdSpaceShip()
	{
		super();
		//CMClass.bumpCounter(this,CMClass.CMObjectType.AREA);
		xtraValues=CMProps.getExtraStatCodesHolder(this);
	}
	//protected void finalize(){CMClass.unbumpCounter(this,CMClass.CMObjectType.AREA);}//removed for mem & perf
	@Override
	public String name()
	{
		if(phyStats().newName()!=null) return phyStats().newName();
		return name;
	}
	@Override
	public void setName(String newName)
	{
		name=newName;
		localClock.setLoadName(newName);
		CMLib.map().renamedArea(this);
	}
	@Override public String Name(){return name;}
	@Override
	public void renameSpaceShip(String newName)
	{
		final String oldName=Name();
		setName(newName);
		if(myRooms.size()>0)
			CMLib.map().renameRooms(this, oldName, this.myRooms);
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
	@Override
	public void recoverPhyStats()
	{
		basePhyStats.copyInto(phyStats);
		eachEffect(new EachApplicable<Ability>(){ @Override
		public final void apply(final Ability A)
		{
			if(A!=null) A.affectPhyStats(me,phyStats);
		}});
	}


	@Override public Area getShipArea() { return this; }
	@Override public void setShipArea(String xml) {}

	@Override
	public void setBasePhyStats(PhyStats newStats)
	{
		basePhyStats=(PhyStats)newStats.copyOf();
	}
	public void setNextWeatherType(int weatherCode){}
	public void setCurrentWeatherType(int weatherCode){}
	@Override public int getTheme(){return Area.THEME_TECHNOLOGY;}
	@Override public int getThemeCode(){return Area.THEME_TECHNOLOGY;}
	@Override public void setTheme(int level){}

	@Override public String image(){return imageName;}
	@Override public String rawImage(){return imageName;}
	@Override public void setImage(String newImage){imageName=newImage;}

	@Override public String getArchivePath(){return "";}
	@Override public void setArchivePath(String pathFile){}

	@Override
	public void setAreaState(State newState)
	{
		if((newState==State.ACTIVE)&&(!CMLib.threads().isTicking(this,Tickable.TICKID_AREA)))
			CMLib.threads().startTickDown(this,Tickable.TICKID_AREA,1);
		flag=newState;
	}
	@Override public State getAreaState(){return flag;}
	@Override public boolean amISubOp(String username){return false;}
	@Override public String getSubOpList(){return "";}
	@Override public void setSubOpList(String list){}
	@Override public void addSubOp(String username){}
	@Override public void delSubOp(String username){}
	@Override
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch(final Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdSpaceShip();
	}
	@Override public boolean isGeneric(){return false;}
	protected void cloneFix(StdSpaceShip ship)
	{
		me=this;
		basePhyStats=(PhyStats)ship.basePhyStats().copyOf();
		phyStats=(PhyStats)ship.phyStats().copyOf();

		affects=new SVector<Ability>(1);
		behaviors=new SVector<Behavior>(1);
		scripts=new SVector<ScriptingEngine>(1);
		parents=new SLinkedList<Area>();
		parents.addAll(ship.parents);
		for(final Enumeration<Behavior> e=ship.behaviors();e.hasMoreElements();)
		{
			final Behavior B=e.nextElement();
			if(B!=null)
				behaviors.addElement(B);
		}
		for(final Enumeration<Ability> a=ship.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A!=null)
				affects.addElement((Ability)A.copyOf());
		}
		ScriptingEngine SE=null;
		for(final Enumeration<ScriptingEngine> e=ship.scripts();e.hasMoreElements();)
		{
			SE=e.nextElement();
			if(SE!=null)
				addScript((ScriptingEngine)SE.copyOf());
		}
		setTimeObj((TimeClock)CMClass.getCommon("DefaultTimeClock"));
	}
	@Override
	public CMObject copyOf()
	{
		try
		{
			final StdSpaceShip E=(StdSpaceShip)this.clone();
			//CMClass.bumpCounter(E,CMClass.CMObjectType.AREA);//removed for mem & perf
			E.xtraValues=(xtraValues==null)?null:(String[])xtraValues.clone();
			E.cloneFix(this);
			return E;

		}
		catch(final CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}
	@Override public String displayText(){return displayText;}
	@Override public void setDisplayText(String newDisplayText){ displayText=newDisplayText; }
	@Override public String displayText(MOB viewerMob) { return displayText(); }
	@Override public String name(MOB viewerMob) { return name(); }

	@Override public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	@Override public String miscTextFormat(){return CMParms.FORMAT_UNDEFINED;}
	@Override
	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,true);
	}
	@Override
	public void setMiscText(String newMiscText)
	{
		miscText="";
		if(newMiscText.trim().length()>0)
			CMLib.coffeeMaker().setPropertiesStr(this,newMiscText,true);
	}

	@Override public String description() { return description;}
	@Override public void setDescription(String newDescription) { description=newDescription;}
	@Override public String description(MOB viewerMob) { return description(); }

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		MsgListener N=null;
		for(int b=0;b<numBehaviors();b++)
		{
			N=fetchBehavior(b);
			if((N!=null)&&(!N.okMessage(this,msg)))
				return false;
		}
		for(int s=0;s<numScripts();s++)
		{
			N=fetchScript(s);
			if((N!=null)&&(!N.okMessage(this,msg)))
				return false;
		}
		for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
		{
			N=a.nextElement();
			if((N!=null)&&(!N.okMessage(this,msg)))
				return false;
		}

		if((flag==State.FROZEN)
		||(flag==State.STOPPED)
		||(!CMLib.flags().allowsMovement(this)))
		{
			if((msg.sourceMinor()==CMMsg.TYP_ENTER)
			||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_FLEE))
				return false;
		}
		if((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MAGIC))
		||(CMath.bset(msg.targetMajor(),CMMsg.MASK_MAGIC))
		||(CMath.bset(msg.othersMajor(),CMMsg.MASK_MAGIC)))
		{
			Room room=null;
			if((msg.target()!=null)
			&&(msg.target() instanceof MOB)
			&&(((MOB)msg.target()).location()!=null))
				room=((MOB)msg.target()).location();
			else
			if((msg.source()!=null)
			&&(msg.source().location()!=null))
				room=msg.source().location();
			if(room!=null)
			{
				if(room.getArea()==this)
					room.showHappens(CMMsg.MSG_OK_VISUAL,_("Magic doesn't seem to work here."));
				else
					room.showHappens(CMMsg.MSG_OK_VISUAL,_("Magic doesn't seem to work there."));
			}

			return false;
		}
		return true;
	}

	protected Enumeration<String> allBlurbFlags()
	{
		final MultiEnumeration<String> multiEnum = new MultiEnumeration<String>(areaBlurbFlags());
		for(final Iterator<Area> i=getParentsIterator();i.hasNext();)
			multiEnum.addEnumeration(i.next().areaBlurbFlags());
		return multiEnum;
	}

	@Override
	public String getBlurbFlag(String flag)
	{
		if((flag==null)||(flag.trim().length()==0))
			return null;
		return blurbFlags.get(flag.toUpperCase().trim());
	}
	@Override public int numBlurbFlags(){return blurbFlags.size();}
	@Override
	public int numAllBlurbFlags()
	{
		int num=numBlurbFlags();
		for(final Iterator<Area> i=getParentsIterator();i.hasNext();)
			num += i.next().numAllBlurbFlags();
		return num;
	}
	@Override
	public Enumeration<String> areaBlurbFlags()
	{
		return new IteratorEnumeration<String>(blurbFlags.keySet().iterator());
	}

	@Override
	public void addBlurbFlag(String flagPlusDesc)
	{
		if(flagPlusDesc==null) return;
		flagPlusDesc=flagPlusDesc.trim();
		if(flagPlusDesc.length()==0) return;
		final int x=flagPlusDesc.indexOf(' ');
		String flag=null;
		if(x>=0)
		{
			flag=flagPlusDesc.substring(0,x).toUpperCase();
			flagPlusDesc=flagPlusDesc.substring(x).trim();
		}
		else
		{
			flag=flagPlusDesc.toUpperCase().trim();
			flagPlusDesc="";
		}
		if(getBlurbFlag(flag)==null)
			blurbFlags.put(flag,flagPlusDesc);
	}
	@Override
	public void delBlurbFlag(String flagOnly)
	{
		if(flagOnly==null) return;
		flagOnly=flagOnly.toUpperCase().trim();
		if(flagOnly.length()==0) return;
		blurbFlags.remove(flagOnly);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		eachBehavior(new EachApplicable<Behavior>(){ @Override
		public final void apply(final Behavior B)
		{
			B.executeMsg(me, msg);
		} });
		eachScript(new EachApplicable<ScriptingEngine>(){ @Override
		public final void apply(final ScriptingEngine S)
		{
			S.executeMsg(me, msg);
		} });
		eachEffect(new EachApplicable<Ability>(){ @Override
		public final void apply(final Ability A)
		{
			A.executeMsg(me,msg);
		}});
		if((msg.sourceMinor()==CMMsg.TYP_DROP)||(msg.sourceMinor()==CMMsg.TYP_GET))
			mass=-1;

		if(msg.amITarget(this))
		{
			if((msg.targetMinor()==CMMsg.TYP_ACTIVATE)&&(CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
			{
				final String[] parts=msg.targetMessage().split(" ");
				final TechCommand command=TechCommand.findCommand(parts);
				if(command!=null)
				{
					final Object[] parms=command.confirmAndTranslate(parts);
					if(parms!=null)
					{
						if((command==Technical.TechCommand.AIRREFRESH)&&(staleAirList.size()>0))
						{
							final double pct=((Double)parms[0]).doubleValue();
							final int atmoResource=((Integer)parms[1]).intValue();
							int numToClear=(int)Math.round(CMath.mul(staleAirList.size(),pct));
							while((numToClear>0)&&(staleAirList.size()>0))
							{
								final String roomID=staleAirList.iterator().next();
								staleAirList.remove(roomID);
								changeRoomAir(getRoom(roomID),null,atmoResource);
								numToClear--;
							}
							changeRoomAir(getRandomMetroRoom(),null,atmoResource);
							for(final Pair<Room,Integer> p  : shipExitCache)
								changeRoomAir(p.first,null,atmoResource);
						}
					}
				}
			}
		}
	}

	@Override public Enumeration<Room> getCompleteMap(){return getProperMap();}
	public List<Room> getMetroCollection(){return new ReadOnlyList(myRooms);}

	public int[] addMaskAndReturn(int[] one, int[] two)
	{
		if(one.length!=two.length)
			return one;
		final int[] returnable=new int[one.length];
		for(int o=0;o<one.length;o++)
			returnable[o]=one[o]+two[o];
		return returnable;
	}

	@Override public int getTickStatus(){ return tickStatus;}

	protected boolean changeRoomAir(Room R, Room notifyRoom, int atmoResource)
	{
		if(R==null)
			return false;
		if(R.getAtmosphere()!=atmoResource)
		{
			if(atmoResource==0)
			{
				R.showHappens(CMMsg.MSG_OK_ACTION, _("@x1 rushes out of the room.",RawMaterial.CODES.NAME(R.getAtmosphere()).toLowerCase()));
				if((notifyRoom!=null)&&(notifyRoom!=R))
					notifyRoom.showHappens(CMMsg.MSG_OK_ACTION, _("@x1 rushes out of the room.",RawMaterial.CODES.NAME(R.getAtmosphere()).toLowerCase()));
			}
			else
			{
				R.showHappens(CMMsg.MSG_OK_ACTION, _("@x1 rushes into the room.",RawMaterial.CODES.NAME(atmoResource).toLowerCase()));
				if((notifyRoom!=null)&&(notifyRoom!=R))
					notifyRoom.showHappens(CMMsg.MSG_OK_ACTION, _("@x1 rushes into the room.",RawMaterial.CODES.NAME(atmoResource).toLowerCase()));
			}
			if(atmoResource==getAtmosphere())
				R.setAtmosphere(-1);
			else
				R.setAtmosphere(atmoResource);
			return true;
		}
		return false;
	}

	protected void moveAtmosphereOut(Set<Room> doneRooms, Room startRoom, int atmo)
	{
		final LinkedList<Room> toDoRooms=new LinkedList<Room>();
		toDoRooms.add(startRoom);
		while(toDoRooms.size()>0)
		{
			final Room R=toDoRooms.removeFirst();
			doneRooms.add(R);
			staleAirList.remove(R.roomID());
			if(changeRoomAir(R,startRoom,atmo))
				break;
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			{
				final Room R2=R.getRoomInDir(d);
				final Exit E2=R.getExitInDir(d);
				if((R2!=null)&&(R2.getArea()==R.getArea())&&(E2!=null)&&(E2.isOpen())&&(!doneRooms.contains(R2)))
					toDoRooms.add(R2);
			}
		}
	}

	protected void doAtmosphereChanges()
	{
		final Set<Room> doneRooms=new HashSet<Room>();
		for(final Pair<Room,Integer> p : shipExitCache)
		{
			final Room R=p.first;
			final Exit E=R.getExitInDir(p.second.intValue());
			if((E!=null)&&(E.isOpen()))
			{
				final Room exitRoom=R;
				final Room otherRoom=R.getRoomInDir(p.second.intValue());
				final int atmo=otherRoom.getAtmosphere();
				moveAtmosphereOut(doneRooms,exitRoom,atmo);
			}
		}
		if((System.currentTimeMillis() > nextStaleWarn)&&(staleAirList.size()>0))
		{
			nextStaleWarn = System.currentTimeMillis() + (60 * 1000);
			for(final Enumeration<Room> r=getProperMap();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if((staleAirList.contains(R.roomID()))
				&&(R.numInhabitants()>0)
				&&(R.numPCInhabitants()>0))
				{
					final int atmo=R.getAtmosphere();
					if(atmo>0)
					for(int i=0;i<R.numInhabitants();i++)
					{
						final MOB M=R.fetchInhabitant(i);
						if((M!=null)
						&&(!M.isMonster())
						&&(!CMLib.flags().canBreatheThis(M,RawMaterial.RESOURCE_NOTHING)))
							M.tell(_("The @x1 is seaming a bit stale.",RawMaterial.CODES.NAME(atmo).toLowerCase()));
					}
				}
			}
		}
		if(System.currentTimeMillis() >= nextStaleCheck)
		{
			nextStaleCheck=System.currentTimeMillis()+STALE_AIR_INTERVAL;
			for(final Enumeration<Room> r=getProperMap();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if(!staleAirList.contains(R.roomID()))
					staleAirList.add(R.roomID());
				else
					R.setAtmosphere(RawMaterial.RESOURCE_NOTHING); // WE NOW HAVE A VACUUM HERE!!!
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(flag==State.STOPPED) return false;
		tickStatus=Tickable.STATUS_START;
		if(tickID==Tickable.TICKID_AREA)
		{
			getTimeObj().tick(this,tickID);
			tickStatus=Tickable.STATUS_BEHAVIOR;
			eachBehavior(new EachApplicable<Behavior>(){ @Override
			public final void apply(final Behavior B)
			{
				B.tick(ticking,tickID);
			} });
			tickStatus=Tickable.STATUS_SCRIPT;
			eachScript(new EachApplicable<ScriptingEngine>(){ @Override
			public final void apply(final ScriptingEngine S)
			{
				S.tick(ticking,tickID);
			} });
			tickStatus=Tickable.STATUS_AFFECT;
			eachEffect(new EachApplicable<Ability>(){ @Override
			public final void apply(final Ability A)
			{
				if(!A.tick(ticking,tickID))
					A.unInvoke();
			}});
			doAtmosphereChanges();
		}
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}

	public String getWeatherDescription(){return "There is no weather here.";}
	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		if(phyStats().sensesMask()>0)
			affectableStats.setSensesMask(affectableStats.sensesMask()|phyStats().sensesMask());
		final int disposition=phyStats().disposition()
			&((~(PhyStats.IS_SLEEPING|PhyStats.IS_HIDDEN)));
		if(disposition>0)
			affectableStats.setDisposition(affectableStats.disposition()|disposition);
		affectableStats.setWeight(affectableStats.weight()+phyStats().weight());
	}
	@Override
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{}
	@Override
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}

	@Override
	public void addNonUninvokableEffect(Ability to)
	{
		if(to==null) return;
		if(fetchEffect(to.ID())!=null) return;
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	@Override
	public void addEffect(Ability to)
	{
		if(to==null) return;
		if(fetchEffect(to.ID())!=null) return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	@Override
	public void delEffect(Ability to)
	{
		final int size=affects.size();
		affects.removeElement(to);
		if(affects.size()<size)
			to.setAffectedOne(null);
	}
	@Override
	public void eachEffect(final EachApplicable<Ability> applier)
	{
		final List<Ability> affects=this.affects;
		if(affects==null) return;
		try
		{
			for(int a=0;a<affects.size();a++)
			{
				final Ability A=affects.get(a);
				if(A!=null) applier.apply(A);
			}
		}
		catch(final ArrayIndexOutOfBoundsException e){}
	}
	@Override
	public void delAllEffects(boolean unInvoke)
	{
		for(int a=numEffects()-1;a>=0;a--)
		{
			final Ability A=fetchEffect(a);
			if(A!=null)
			{
				if(unInvoke) A.unInvoke();
				A.setAffectedOne(null);
			}
		}
		affects.clear();
	}
	@Override
	public int numEffects()
	{
		return affects.size();
	}

	@Override public Enumeration<Ability> effects(){return (affects==null)?EmptyEnumeration.INSTANCE:affects.elements();}

	@Override
	public Ability fetchEffect(int index)
	{
		try
		{
			return affects.elementAt(index);
		}
		catch(final java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	@Override
	public Ability fetchEffect(String ID)
	{
		for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A.ID().equals(ID)))
			   return A;
		}
		return null;
	}

	@Override public void fillInAreaRooms() { }

	@Override
	public boolean inMyMetroArea(Area A)
	{
		if(A==this) return true;
		return false;
	}
	@Override public void fillInAreaRoom(Room R){}
	@Override
	public void dockHere(LocationRoom roomR)
	{
		if(roomR==null) return;
		savedDock=roomR;
		CMLib.map().delObjectInSpace(getShipSpaceObject());
		shipExitCache.clear();
		for(final Enumeration<Room> r=getProperMap();r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				if((R.getRawExit(d)!=null)
				&&((R.rawDoors()[d]==null)||(R.rawDoors()[d].getArea()!=this)))
				{
					R.rawDoors()[d]=roomR;
					shipExitCache.add(new Pair<Room,Integer>(R,Integer.valueOf(d)));
				}
		}
	}
	@Override
	public void unDock(boolean toSpace)
	{
		if(getIsDocked()==null) return;
		final Room dock=getIsDocked();
		for(int i=0;i<dock.numItems();i++)
		{
			final Item I=dock.getItem(i);
			if(I.Name().equals(Name()))
				I.destroy();
		}
		for(final Enumeration<Room> e=getProperMap();e.hasMoreElements();)
		{
			final Room R=e.nextElement();
			if(R!=null)
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				if(R.rawDoors()[d]==dock)
					R.rawDoors()[d]=null;
			}
		}
		shipExitCache.clear();
		savedDock=null;
	}

	@Override
	public SpaceObject getShipSpaceObject()
	{
		return shipItem;
	}

	@Override
	public RoomnumberSet getCachedRoomnumbers()
	{
		final RoomnumberSet set=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		synchronized(myRooms)
		{
			Room R=null;
			for(int p=myRooms.size()-1;p>=0;p--)
			{
				R=myRooms.elementAt(p);
				if(R.roomID().length()>0)
					set.add(R.roomID());
			}
		}
		return set;
	}
	@Override
	public RoomnumberSet getProperRoomnumbers()
	{
		if(properRoomIDSet==null)
			properRoomIDSet=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		return properRoomIDSet;
	}

	@Override
	public String getNewRoomID(Room startRoom, int direction)
	{
		int highest=Integer.MIN_VALUE;
		int lowest=Integer.MAX_VALUE;
		final CMIntegerGrouper set=(CMIntegerGrouper)CMClass.getCommon("DefaultCMIntegerGrouper");
		try
		{
			String roomID=null;
			int newnum=0;
			final String name=Name().toUpperCase();
			if(!CMLib.flags().isSavable(this))
			{
				for(final Enumeration<String> i=getProperRoomnumbers().getRoomIDs();i.hasMoreElements();)
				{
					roomID=i.nextElement();
					if((roomID.length()>0)&&(roomID.startsWith(name+"#")))
					{
						roomID=roomID.substring(name.length()+1);
						if(CMath.isInteger(roomID))
						{
							newnum=CMath.s_int(roomID);
							if(newnum>=0)
							{
								if(newnum>=highest)    highest=newnum;
								if(newnum<=lowest) lowest=newnum;
								set.addx(newnum);
							}
						}
					}
				}
			}
			for(final Enumeration i=CMLib.map().roomIDs();i.hasMoreElements();)
			{
				roomID=(String)i.nextElement();
				if((roomID.length()>0)&&(roomID.startsWith(name+"#")))
				{
					roomID=roomID.substring(name.length()+1);
					if(CMath.isInteger(roomID))
					{
						newnum=CMath.s_int(roomID);
						if(newnum>=0)
						{
							if(newnum>=highest)    highest=newnum;
							if(newnum<=lowest) lowest=newnum;
							set.addx(newnum);
						}
					}
				}
			}
		}catch(final NoSuchElementException e){}
		if(highest<0)
		{
			if(!CMLib.flags().isSavable(this))
			{
				for(int i=0;i<Integer.MAX_VALUE;i++)
				{
					if((getRoom(Name()+"#"+i))==null)
						return Name()+"#"+i;
				}
			}
			for(int i=0;i<Integer.MAX_VALUE;i++)
			{
				if((CMLib.map().getRoom(Name()+"#"+i))==null)
					return Name()+"#"+i;
			}
		}
		if(lowest>highest)
		{
			lowest=highest+1;
		}
		if(!CMLib.flags().isSavable(this))
		{
			for(int i=lowest;i<=highest+1000;i++)
			{
				if((!set.contains(i))
				&&(getRoom(Name()+"#"+i)==null))
					return Name()+"#"+i;
			}
		}
		for(int i=lowest;i<=highest+1000;i++)
		{
			if((!set.contains(i))
			&&(CMLib.map().getRoom(Name()+"#"+i)==null))
				return Name()+"#"+i;
		}
		return Name()+"#"+(int)Math.round(Math.random()*Integer.MAX_VALUE);
	}

	/** Manipulation of Behavior objects, which includes
	 * movement, speech, spellcasting, etc, etc.*/
	@Override
	public void addBehavior(Behavior to)
	{
		if(to==null) return;
		for(int b=0;b<numBehaviors();b++)
		{
			final Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equals(to.ID())))
				return;
		}
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
		final boolean didSomething=(behaviors!=null)&&(behaviors.size()>0);
		if(didSomething) behaviors.clear();
		behaviors=null;
		if(didSomething && ((scripts==null)||(scripts.size()==0)))
			CMLib.threads().deleteTick(this,Tickable.TICKID_ROOM_BEHAVIOR);
	}
	@Override
	public int numBehaviors()
	{
		return behaviors.size();
	}
	@Override public Enumeration<Behavior> behaviors() { return behaviors.elements();}
	@Override public int maxRange(){return Integer.MAX_VALUE;}
	@Override public int minRange(){return Integer.MIN_VALUE;}

	@Override public int[] getAreaIStats(){return new int[Area.Stats.values().length];}
	@Override public StringBuffer getAreaStats(){    return new StringBuffer(description());}

	@Override
	public Behavior fetchBehavior(int index)
	{
		try
		{
			return behaviors.elementAt(index);
		}
		catch(final java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	@Override
	public Behavior fetchBehavior(String ID)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			final Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equalsIgnoreCase(ID)))
				return B;
		}
		return null;
	}
	@Override
	public void eachBehavior(final EachApplicable<Behavior> applier)
	{
		final List<Behavior> behaviors=this.behaviors;
		if(behaviors!=null)
		try
		{
			for(int a=0;a<behaviors.size();a++)
			{
				final Behavior B=behaviors.get(a);
				if(B!=null) applier.apply(B);
			}
		}
		catch(final ArrayIndexOutOfBoundsException e){}
	}

	/** Manipulation of the scripts list */
	@Override
	public void addScript(ScriptingEngine S)
	{
		if(S==null) return;
		if(!scripts.contains(S))
		{
			for(final ScriptingEngine S2 : scripts)
				if((S2!=null)&&(S2.getScript().equalsIgnoreCase(S.getScript())))
					return;
			scripts.addElement(S);
		}
	}
	@Override
	public void delScript(ScriptingEngine S)
	{
		scripts.removeElement(S);
	}
	@Override
	public void delAllScripts()
	{
		final boolean didSomething=(scripts!=null)&&(scripts.size()>0);
		if(didSomething) scripts.clear();
		scripts=null;
		if(didSomething && ((behaviors==null)||(behaviors.size()==0)))
		  CMLib.threads().deleteTick(this,Tickable.TICKID_ITEM_BEHAVIOR);
	}
	@Override public int numScripts(){return (scripts==null)?0:scripts.size();}
	@Override public Enumeration<ScriptingEngine> scripts() { return (scripts==null)?EmptyEnumeration.INSTANCE:scripts.elements();}
	@Override public ScriptingEngine fetchScript(int x){try{return scripts.elementAt(x);}catch(final Exception e){} return null;}
	@Override
	public void eachScript(final EachApplicable<ScriptingEngine> applier)
	{
		final List<ScriptingEngine> scripts=this.scripts;
		if(scripts!=null)
		try
		{
			for(int a=0;a<scripts.size();a++)
			{
				final ScriptingEngine S=scripts.get(a);
				if(S!=null) applier.apply(S);
			}
		}
		catch(final ArrayIndexOutOfBoundsException e){}
	}

	@Override
	public void addProperRoom(Room R)
	{
		if(R==null) return;
		if(R.getArea()!=this)
		{
			R.setArea(this);
			return;
		}
		if(!CMLib.flags().isSavable(this))
			CMLib.flags().setSavable(R,false);
		synchronized(myRooms)
		{
			if(!myRooms.contains(R))
			{
				addProperRoomnumber(R.roomID());
				Room R2=null;
				for(int i=0;i<myRooms.size();i++)
				{
					R2=myRooms.elementAt(i);
					if(R2.roomID().compareToIgnoreCase(R.roomID())>=0)
					{
						if(R2.ID().compareToIgnoreCase(R.roomID())==0)
							myRooms.setElementAt(R,i);
						else
							myRooms.insertElementAt(R,i);
						return;
					}
				}
				myRooms.addElement(R);
			}
		}
	}

	@Override
	public void delProperRoom(Room R)
	{
		if(R==null) return;
		if(R instanceof GridLocale)
			((GridLocale)R).clearGrid(null);
		synchronized(myRooms)
		{
			if(myRooms.removeElement(R))
				delProperRoomnumber(R.roomID());
		}
	}

	@Override
	public void addProperRoomnumber(String roomID)
	{
		if((roomID!=null)&&(roomID.length()>0))
			getProperRoomnumbers().add(roomID);
	}
	@Override
	public void delProperRoomnumber(String roomID)
	{
		if((roomID!=null)&&(roomID.length()>0))
			getProperRoomnumbers().remove(roomID);
	}
	@Override
	public boolean isRoom(Room R)
	{
		if(R==null) return false;
		return myRooms.contains(R);
	}

	@Override
	public Room getRoom(String roomID)
	{
		if(myRooms.size()==0) return null;
		synchronized(myRooms)
		{
			int start=0;
			int end=myRooms.size()-1;
			while(start<=end)
			{
				final int mid=(end+start)/2;
				final int comp=myRooms.elementAt(mid).roomID().compareToIgnoreCase(roomID);
				if(comp==0)
					return myRooms.elementAt(mid);
				else
				if(comp>0)
					end=mid-1;
				else
					start=mid+1;

			}
		}
		return null;
	}

	@Override public int metroSize(){return properSize();}
	@Override
	public int properSize()
	{
		synchronized(myRooms)
		{
			return myRooms.size();
		}
	}
	@Override
	public int numberOfProperIDedRooms()
	{
		int num=0;
		for(final Enumeration<Room> e=getProperMap();e.hasMoreElements();)
		{
			final Room R=e.nextElement();
			if(R.roomID().length()>0)
				if(R instanceof GridLocale)
					num+=((GridLocale)R).xGridSize()*((GridLocale)R).yGridSize();
				else
					num++;
		}
		return num;
	}
	@Override public Room getRandomMetroRoom(){return getRandomProperRoom();}
	@Override
	public Room getRandomProperRoom()
	{
		synchronized(myRooms)
		{
			if(properSize()==0) return null;
			final Room R=myRooms.elementAt(CMLib.dice().roll(1,properSize(),-1));
			if(R instanceof GridLocale) return ((GridLocale)R).getRandomGridChild();
			return R;
		}
	}
	@Override public boolean isProperlyEmpty(){ return getProperRoomnumbers().isEmpty(); }
	@Override public void setProperRoomnumbers(RoomnumberSet set){ properRoomIDSet=set;}
	public RoomnumberSet getMetroRoomnumbers(){return getProperRoomnumbers();}
	@Override public Enumeration<Room> getMetroMap(){return getProperMap();}
	@Override public void addMetroRoomnumber(String roomID){}
	@Override public void delMetroRoomnumber(String roomID){}
	@Override public void addMetroRoom(Room R){}
	@Override public void delMetroRoom(Room R){}
	@Override
	public Enumeration<Room> getProperMap()
	{
		synchronized(myRooms)
		{
			return myRooms.elements();
		}
	}
	@Override public Enumeration<Room> getFilledProperMap() { return getProperMap();}
	@Override public Enumeration<String> subOps(){ return EmptyEnumeration.INSTANCE;}

	// Children
	@Override public Enumeration<Area> getChildren() {return EmptyEnumeration.INSTANCE; }
	@Override public Area getChild(String named) { return null;}
	@Override public boolean isChild(Area named) { return false;}
	@Override public boolean isChild(String named) { return false;}
	@Override public void addChild(Area area) {}
	@Override public void removeChild(Area area) {}
	@Override public boolean canChild(Area area) { return false;}

	public SLinkedList<Area> loadAreas(Collection<String> loadableSet)
	{
		final SLinkedList<Area> finalSet = new SLinkedList<Area>();
		for (final String areaName : loadableSet)
		{
			final Area A = CMLib.map().getArea(areaName);
			if (A == null)
				continue;
			finalSet.add(A);
		}
		return finalSet;
	}

	protected final Iterator<Area> getParentsIterator()
	{
		return parents.iterator();
	}

	protected final Iterator<Area> getParentsReverseIterator()
	{
		return parents.descendingIterator();
	}

	@Override
	public Enumeration<Area> getParents()
	{
		return new IteratorEnumeration<Area>(parents.iterator());
	}

	@Override
	public List<Area> getParentsRecurse()
	{
		final LinkedList<Area> V=new LinkedList<Area>();
		for(final Iterator<Area> a=getParentsIterator();a.hasNext();)
		{
			final Area A=a.next();
			V.add(A);
			V.addAll(A.getParentsRecurse());
		}
		return V;
	}

	@Override
	public Area getParent(String named)
	{
		for(final Iterator<Area> a=getParentsIterator();a.hasNext();)
		{
			final Area A=a.next();
			if((A.name().equalsIgnoreCase(named))
			||(A.Name().equalsIgnoreCase(named)))
			   return A;
		}
		return null;
	}

	@Override
	public boolean isParent(Area area)
	{
		for(final Iterator<Area> a=getParentsIterator();a.hasNext();)
		{
			final Area A=a.next();
			if(A == area)
			   return true;
		}
		return false;
	}

	@Override
	public boolean isParent(String named)
	{
		for(final Iterator<Area> a=getParentsIterator();a.hasNext();)
		{
			final Area A=a.next();
			if((A.name().equalsIgnoreCase(named))
			||(A.Name().equalsIgnoreCase(named)))
				return true;
		}
		return false;
	}

	@Override
	public void addParent(Area area)
	{
		if(!canParent(area))
			return;
		for(final Iterator<Area> i=getParentsIterator(); i.hasNext();)
		{
			final Area A=i.next();
			if(A.Name().equalsIgnoreCase(area.Name()))
			{
				parents.remove(A);
				break;
			}
		}
		parents.add(area);
	}

	@Override
	public void removeParent(Area area)
	{
		if(isParent(area))
			parents.remove(area);
	}

	@Override
	public boolean canParent(Area area)
	{
		return true;
	}

	@Override public String prejudiceFactors(){return "";}
	@Override public void setPrejudiceFactors(String factors){}
	public final static String[] empty=new String[0];
	@Override public String[] itemPricingAdjustments(){return empty;}
	@Override public void setItemPricingAdjustments(String[] factors){}
	@Override public String ignoreMask(){return "";}
	@Override public void setIgnoreMask(String factors){}
	@Override public String budget(){return "";}
	@Override public void setBudget(String factors){}
	@Override public String devalueRate(){return "";}
	@Override public void setDevalueRate(String factors){}
	@Override public int invResetRate(){return 0;}
	@Override public void setInvResetRate(int ticks){}
	@Override public int finalInvResetRate(){ return 0;}
	@Override public String finalPrejudiceFactors(){ return "";}
	@Override public String finalIgnoreMask(){ return "";}
	@Override public String[] finalItemPricingAdjustments(){ return empty;}
	@Override public String finalBudget(){ return "";}
	@Override public String finalDevalueRate(){ return "";}

	@Override public String _(final String str, final String ... xs) { return CMLib.lang().fullSessionTranslation(str, xs); }
	@Override public int getSaveStatIndex(){return getStatCodes().length;}
	private static final String[] CODES={"CLASS","CLIMATE","DESCRIPTION","TEXT","THEME","BLURBS","OMLCOEFF","RADIUS"};
	@Override public String[] getStatCodes(){return CODES;}
	@Override public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	protected int getCodeNum(String code)
	{
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	@Override
	public String getStat(String code)
	{
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+getClimateTypeCode();
		case 2: return description();
		case 3: return text();
		case 4: return ""+getThemeCode();
		case 5: return ""+CMLib.xml().getXMLList(blurbFlags.toStringVector(" "));
		case 6: return ""+getOMLCoeff();
		case 7: return ""+radius();
		}
		return "";
	}
	@Override
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setClimateType(CMath.s_parseBitIntExpression(Places.CLIMATE_DESCS,val)); break;
		case 2: setDescription(val); break;
		case 3: setMiscText(val); break;
		case 4: setTheme(CMath.s_parseBitIntExpression(Area.THEME_BIT_NAMES,val)); break;
		case 5:
		{
			if(val.startsWith("+"))
				addBlurbFlag(val.substring(1));
			else
			if(val.startsWith("-"))
				delBlurbFlag(val.substring(1));
			else
			{
				blurbFlags=new STreeMap<String,String>();
				final List<String> V=CMLib.xml().parseXMLList(val);
				for(final String s : V)
				{
					final int x=s.indexOf(' ');
					if(x<0)
						blurbFlags.put(s,"");
					else
						blurbFlags.put(s.substring(0,x),s.substring(x+1));
				}
			}
			break;
		}
		case 6: setOMLCoeff(CMath.s_double(val)); break;
		case 7: setRadius(CMath.s_long(val)); break;
		}
	}
	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdSpaceShip)) return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}
