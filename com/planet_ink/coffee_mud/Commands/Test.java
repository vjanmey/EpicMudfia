package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.CompiledOperation;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject.BoundedCube;
import com.planet_ink.coffee_mud.core.exceptions.HTTPServerException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.interfaces.WebMacro;
import com.planet_ink.miniweb.http.HTTPMethod;
import com.planet_ink.miniweb.http.MultiPartData;
import com.planet_ink.miniweb.interfaces.HTTPRequest;





import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/*
* <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
* <p>Portions Copyright (c) 2004-2014 Bo Zimmerman</p>

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
public class Test extends StdCommand
{
	public Test(){}

	private final String[] access=_i(new String[]{"Test"});
	@Override public String[] getAccessWords(){return access;}

	public static final String[] spells={"Spell_Blur","Spell_ResistMagicMissiles"};
	public static String semiSpellList=null;
	public static String semiSpellList()
	{
		if(semiSpellList!=null) return semiSpellList;
		final StringBuffer str=new StringBuffer("");
		for (final String spell : spells)
			str.append(spell+";");
		semiSpellList=str.toString();
		return semiSpellList;
	}

	public static final String[] maliciousspells={"Spell_Blindness","Spell_Mute"};
	public static String maliciousSemiSpellList=null;
	public static String maliciousSemiSpellList()
	{
		if(maliciousSemiSpellList!=null) return maliciousSemiSpellList;
		final StringBuffer str=new StringBuffer("");
		for (final String maliciousspell : maliciousspells)
			str.append(maliciousspell+";");
		maliciousSemiSpellList=str.toString();
		return maliciousSemiSpellList;
	}

	public boolean isAllAdjusted(MOB mob)
	{
		if(mob.phyStats().ability()<10)
			return false;
		if(mob.charStats().getStat(CharStats.STAT_GENDER)!='F')
			return false;
		if(!mob.charStats().getCurrentClass().ID().equals("Fighter"))
			return false;
		if(mob.charStats().getStat(CharStats.STAT_CHARISMA)<18)
			return false;
		if(mob.maxState().getMana()<1000)
			return false;
		return true;
	}
	public boolean isAnyAdjusted(MOB mob)
	{
		if(mob.phyStats().ability()>=10)
			return true;
		if(mob.charStats().getStat(CharStats.STAT_GENDER)=='F')
			return true;
		if(mob.charStats().getCurrentClass().ID().equals("Fighter"))
			return true;
		if(mob.charStats().getStat(CharStats.STAT_CHARISMA)>=18)
			return true;
		if(mob.maxState().getMana()>=1000)
			return true;
		return false;
	}


	public void giveAbility(Physical P, Ability A)
	{
		final Ability A2=((Ability)A.copyOf());
		A2.setMiscText(A.text());
		P.addNonUninvokableEffect(A2);
	}

	public boolean testResistance(MOB mob)
	{
		final Item I=CMClass.getWeapon("Dagger");
		mob.curState().setHitPoints(mob.maxState().getHitPoints());
		int curHitPoints=mob.curState().getHitPoints();
		CMLib.combat().postDamage(mob,mob,I,5,CMMsg.MSG_WEAPONATTACK,Weapon.TYPE_PIERCING,"<S-NAME> <DAMAGE> <T-NAME>.");
		if(mob.curState().getHitPoints()<curHitPoints-3)
			return false;
		curHitPoints=mob.curState().getHitPoints();
		CMLib.factions().setAlignmentOldRange(mob,0);
		final Ability A=CMClass.getAbility("Prayer_DispelEvil");
		A.invoke(mob,mob,true,1);
		if(mob.curState().getHitPoints()<curHitPoints)
			return false;
		curHitPoints=mob.curState().getHitPoints();
		if(mob.charStats().getSave(CharStats.STAT_SAVE_ACID)<30)
			return false;
		return true;
	}


	public Item[] giveTo(Item I, Ability A, MOB mob1, MOB mob2, int code)
	{
		final Item[] IS=new Item[2];
		final Item I1=(Item)I.copyOf();
		if(A!=null)
			giveAbility(I1,A);
		if(code<2)
		{
			mob1.addItem(I1);
			if(code==1) I1.wearEvenIfImpossible(mob1);
		}
		else
		{
			mob1.location().addItem(I1);
			if((I1 instanceof Rideable)&&(code==2))
				mob1.setRiding((Rideable)I1);
		}

		IS[0]=I1;

		final Item I2=(Item)I.copyOf();
		if(A!=null)
			giveAbility(I2,A);
		if(mob2!=null)
		{
			if(code<2)
			{
				mob2.addItem(I2);
				if(code==1) I2.wearEvenIfImpossible(mob2);
			}
			else
			{
				mob2.location().addItem(I2);
				if((I2 instanceof Rideable)&&(code==2))
					mob2.setRiding((Rideable)I2);
			}
		}
		IS[1]=I2;
		mob1.location().recoverRoomStats();
		return IS;
	}

	public boolean spellCheck(String[] spells, MOB mob)
	{
		for (final String spell : spells)
			if(mob.fetchAbility(spell)==null)
				return false;
		return true;
	}

	public boolean effectCheck(String[] spells, MOB mob)
	{
		for (final String spell : spells)
			if(mob.fetchEffect(spell)==null)
				return false;
		return true;
	}

	public void reset(MOB[] mobs,MOB[] backups, Room R, Item[] IS,Room R2)
	{
		R2.delAllEffects(true);
		if(IS!=null)
		{
			if(IS[0]!=null)
				IS[0].destroy();
			if(IS[1]!=null)
				IS[1].destroy();
		}
		if(mobs[0]!=null)
			mobs[0].destroy();
		if(mobs[1]!=null)
			mobs[1].destroy();
		R.recoverRoomStats();
		mobs[0]=CMClass.getMOB("StdMOB");
		mobs[0].baseCharStats().setMyRace(CMClass.getRace("Dwarf"));
		mobs[0].setName(_("A Dwarf"));
		mobs[0].recoverCharStats();
		backups[0]=(MOB)mobs[0].copyOf();
		mobs[1]=CMClass.getMOB("StdMOB");
		mobs[1].setName(_("A Human"));
		mobs[1].baseCharStats().setMyRace(CMClass.getRace("Human"));
		mobs[1].recoverCharStats();
		backups[0]=(MOB)mobs[1].copyOf();

		mobs[0].bringToLife(R,true);
		mobs[1].bringToLife(R,true);
	}

	public int[] recoverMath(int level, int con, int inte, int wis, int str, boolean isHungry, boolean isThirsty, boolean isFatigued, boolean isSleeping, boolean isSittingOrRiding,boolean isFlying,boolean isSwimming)
	{
		/*	# @x1=stat(con/str/int-wis), @x2=level, @x3=hungry?1:0, @x4=thirsty?1:0, @x5=fatigued?0:1 # @x6=asleep?1:0, @x7=sitorride?1:0, @x8=flying?0:1, @x9=swimming?0:1 */
		final LinkedList<CompiledOperation> stateHitPointRecoverFormula = CMath.compileMathExpression("5+(((@x1 - (@xx*@x3/2.0) - (@xx*@x4/2.0))*@x2/9.0) + (@xx*@x6*.5) + (@xx/4.0*@x7) - (@xx/2.0*@x9))");
		final LinkedList<CompiledOperation> stateManaRecoverFormula = CMath.compileMathExpression("25+(((@x1 - (@xx*@x3/2.0) - (@xx*@x4/2.0) - (@xx*@x5/2.0))*@x2/50.0) + (@xx*@x6*.5) + (@xx/4.0*@x7) - (@xx/2.0*@x9))");
		final LinkedList<CompiledOperation> stateMovesRecoverFormula = CMath.compileMathExpression("25+(((@x1 - (@xx*@x3/2.0) - (@xx*@x4/2.0) - (@xx*@x5/2.0))*@x2/10.0) + (@xx*@x6*.5) + (@xx/4.0*@x7) + (@xx/4.0*@x8) - (@xx/2.0*@x9))");
		final double[] vals=new double[]{
			con,
			level,
			isHungry?1.0:0.0,
			isThirsty?1.0:0.0,
			isFatigued?1.0:0.0,
			isSleeping?1.0:0.0,
			isSittingOrRiding?1.0:0.0,
			isFlying?1.0:0.0,
			isSwimming?1.0:0.0
		};
		final int[] v=new int[3];
		v[0]= (int)Math.round(CMath.parseMathExpression(stateHitPointRecoverFormula, vals, 0.0));

		vals[0]=((inte+wis));
		v[1]= (int)Math.round(CMath.parseMathExpression(stateManaRecoverFormula, vals, 0.0));

		vals[0]=str;
		v[2]= (int)Math.round(CMath.parseMathExpression(stateMovesRecoverFormula, vals, 0.0));
		return v;
	}

	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()>1)
		{
			final String what=((String)commands.elementAt(1)).toUpperCase().trim();
			//String rest=CMParms.combine(commands,2);
			if(what.equalsIgnoreCase("levelxptest"))
			{
				for(int i=0;i<100;i++)
					CMLib.leveler().getLevelExperience(CMLib.dice().roll(1,100,0));
				final MOB M=CMClass.getMOB("StdMOB");
				M.setExperience(0);
				for(int i=1;i<100;i++)
				{
					M.basePhyStats().setLevel(i);
					M.phyStats().setLevel(i);
					M.baseCharStats().setClassLevel(M.baseCharStats().getCurrentClass(),i);
					M.charStats().setClassLevel(M.baseCharStats().getCurrentClass(),i);
					final int level=M.basePhyStats().level();
					int xp=0;
					final String s=i+") "+M.getExperience()+"/"+M.getExpNextLevel()+"/"+M.getExpNeededLevel()+": ";
					while(level==M.basePhyStats().level())
					{
						xp+=10;
						CMLib.leveler().gainExperience(M,null,"",10,true);
					}
					mob.tell(s+xp);
				}
			}
			else
			if(what.equalsIgnoreCase("levelcharts"))
			{
				final StringBuffer str=new StringBuffer("");
				for(int i=0;i<110;i++)
					str.append(i+"="+CMLib.leveler().getLevelExperience(i)+"\r");
				mob.tell(str.toString());
			}
			else
			if(what.equalsIgnoreCase("timsdeconstruction"))
			{
				mob.tell(_("Checking..."));
				final String theRest=CMParms.combine(commands,2).toUpperCase();
				for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if(A instanceof ItemCraftor)
					{
						final ItemCraftor I=(ItemCraftor)A;
						if((theRest.length()==0)||(I.ID().toUpperCase().indexOf(theRest)>=0))
						{
							final List<ItemCraftor.ItemKeyPair> set=I.craftAllItemSets(false);
							for(final ItemCraftor.ItemKeyPair KP : set)
							{
								if((KP.item instanceof Armor)||(KP.item instanceof Weapon))
								{
									final int newLevel=CMLib.itemBuilder().timsLevelCalculator(KP.item);
									if((newLevel < Math.round(KP.item.basePhyStats().level() * .7))
									||(newLevel > Math.round(KP.item.basePhyStats().level() * 1.3)))
										mob.tell(KP.item.name()+": "+KP.item.basePhyStats().level()+"!="+newLevel);
								}
							}
						}
					}
				}
			}
			else
			if(what.equalsIgnoreCase("recover"))
			{
				for(final int level : new int[]{1,10,50,90})
				{
					int hp;
					int mana;
					int move;
					int stat;
					switch(level)
					{
					default:
					case 1:
						hp=20;
						mana=100;
						move=100;
						stat=15;
						break;
					case 10:
						hp=120;
						mana=150;
						move=190;
						stat=17;
						break;
					case 50:
						hp=500;
						mana=420;
						move=520;
						stat=18;
						break;
					case 90:
						hp=1200;
						mana=700;
						move=1000;
						stat=18;
						break;
					}
					final StringBuilder str=new StringBuilder("level: "+level+"\n\r");
					int[] resp;
					resp=recoverMath(level,stat,stat,stat-4,stat,/*Hun*/false,/*Thirs*/false,/*Fatig*/false,/*Sleep*/false,/*Sit*/false,/*Fly*/false,/*Swim*/false);
					str.append(_("standing: hpticks=@x1,  manaticks=@x2,  moveticks=@x3\n\r",""+(hp/resp[0]),""+(mana/resp[1]),""+(move/resp[2])));
					resp=recoverMath(level,stat,stat,stat-4,stat,/*Hun*/true,/*Thirs*/false,/*Fatig*/false,/*Sleep*/false,/*Sit*/false,/*Fly*/false,/*Swim*/false);
					str.append(_("hungry: hpticks=@x1,  manaticks=@x2,  moveticks=@x3\n\r",""+(hp/resp[0]),""+(mana/resp[1]),""+(move/resp[2])));
					resp=recoverMath(level,stat,stat,stat-4,stat,/*Hun*/false,/*Thirs*/true,/*Fatig*/false,/*Sleep*/false,/*Sit*/false,/*Fly*/false,/*Swim*/false);
					str.append(_("thirsty: hpticks=@x1,  manaticks=@x2,  moveticks=@x3\n\r",""+(hp/resp[0]),""+(mana/resp[1]),""+(move/resp[2])));
					resp=recoverMath(level,stat,stat,stat-4,stat,/*Hun*/false,/*Thirs*/false,/*Fatig*/true,/*Sleep*/false,/*Sit*/false,/*Fly*/false,/*Swim*/false);
					str.append(_("fatigued: hpticks=@x1,  manaticks=@x2,  moveticks=@x3\n\r",""+(hp/resp[0]),""+(mana/resp[1]),""+(move/resp[2])));
					resp=recoverMath(level,stat,stat,stat-4,stat,/*Hun*/false,/*Thirs*/false,/*Fatig*/false,/*Sleep*/true,/*Sit*/false,/*Fly*/false,/*Swim*/false);
					str.append(_("sleep: hpticks=@x1,  manaticks=@x2,  moveticks=@x3\n\r",""+(hp/resp[0]),""+(mana/resp[1]),""+(move/resp[2])));
					resp=recoverMath(level,stat,stat,stat-4,stat,/*Hun*/false,/*Thirs*/false,/*Fatig*/false,/*Sleep*/false,/*Sit*/true,/*Fly*/false,/*Swim*/false);
					str.append(_("sitting: hpticks=@x1,  manaticks=@x2,  moveticks=@x3\n\r",""+(hp/resp[0]),""+(mana/resp[1]),""+(move/resp[2])));
					resp=recoverMath(level,stat,stat,stat-4,stat,/*Hun*/false,/*Thirs*/false,/*Fatig*/false,/*Sleep*/false,/*Sit*/false,/*Fly*/true,/*Swim*/false);
					str.append(_("flying: hpticks=@x1,  manaticks=@x2,  moveticks=@x3\n\r",""+(hp/resp[0]),""+(mana/resp[1]),""+(move/resp[2])));
					resp=recoverMath(level,stat,stat,stat-4,stat,/*Hun*/false,/*Thirs*/false,/*Fatig*/false,/*Sleep*/false,/*Sit*/false,/*Fly*/false,/*Swim*/true);
					str.append(_("swimming: hpticks=@x1,  manaticks=@x2,  moveticks=@x3\n\r",""+(hp/resp[0]),""+(mana/resp[1]),""+(move/resp[2])));
					str.append("\n\r");
					mob.tell(str.toString());
				}
			}
			else
			if(what.equalsIgnoreCase("deconstruction"))
			{
				mob.tell(_("Building item sets..."));
				final Hashtable<ItemCraftor,List<ItemCraftor.ItemKeyPair>> allSets=new Hashtable();
				for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if(A instanceof ItemCraftor)
					{
						final ItemCraftor I=(ItemCraftor)A;
						allSets.put(I,I.craftAllItemSets(false));
					}
				}
				mob.tell(_("Sorting..."));
				for(final ItemCraftor I : allSets.keySet())
				{
					final List<ItemCraftor.ItemKeyPair> allItems=allSets.get(I);
					for(final ItemCraftor.ItemKeyPair P : allItems)
					{
						if(P.item.material()!=RawMaterial.RESOURCE_WHITE_GOLD)
						for(final ItemCraftor oI : allSets.keySet())
						{
							if(oI.supportsDeconstruction())
							{
								if(!oI.mayICraft(P.item))
								{
									if(oI==I)
									{
										Log.sysOut("INFO",P.item.name()+" can't even be built by "+oI.ID());
									}
								}
								else
								{
									if(oI!=I)
										Log.sysOut("INFO",P.item.name()+", owned by "+I.ID()+" can also be built by "+oI.ID());
								}
							}
						}
					}
				}
			}
			else
			if(what.equalsIgnoreCase("statcreationspeed"))
			{
				int times=CMath.s_int(CMParms.combine(commands,2));
				if(times<=0) times=9999999;
				mob.tell(_("times=@x1",""+times));
				Object newStats=null;
				long time=System.currentTimeMillis();
				for(int i=0;i<times;i++)
					newStats=mob.basePhyStats().copyOf();
				mob.tell(_("PhyStats CopyOf took :@x1",""+(System.currentTimeMillis()-time)));
				time=System.currentTimeMillis();
				for(int i=0;i<times;i++)
					mob.basePhyStats().copyInto((PhyStats)newStats);
				mob.tell(_("PhyStats CopyInto took :@x1",""+(System.currentTimeMillis()-time)));

				time=System.currentTimeMillis();
				for(int i=0;i<times;i++)
					newStats=mob.baseCharStats().copyOf();
				mob.tell(_("CharStats CopyOf took :@x1",""+(System.currentTimeMillis()-time)));
				time=System.currentTimeMillis();
				for(int i=0;i<times;i++)
					mob.baseCharStats().copyInto((CharStats)newStats);
				mob.tell(_("CharStats CopyInto took :@x1",""+(System.currentTimeMillis()-time)));

				time=System.currentTimeMillis();
				for(int i=0;i<times;i++)
					newStats=mob.maxState().copyOf();
				mob.tell(_("CharState CopyOf took :@x1",""+(System.currentTimeMillis()-time)));
				time=System.currentTimeMillis();
				for(int i=0;i<times;i++)
					mob.maxState().copyInto((CharState)newStats);
				mob.tell(_("CharState CopyInto took :@x1",""+(System.currentTimeMillis()-time)));
			}
			else
			if(what.equalsIgnoreCase("randomroompick"))
			{
				final int num=CMath.s_int(CMParms.combine(commands,2));
				int numNull=0;
				for(int i=0;i<num;i++)
					if(mob.location().getArea().getRandomProperRoom()==null)
						numNull++;
				mob.tell(_("Picked @x1/@x2 rooms in this area.",""+(num-numNull),""+num));
			}
			else
			if(what.equalsIgnoreCase("randomnames"))
			{
				final int num=CMath.s_int(CMParms.combine(commands,2));
				StringBuilder str=new StringBuilder("");
				for(int i=0;i<num;i++)
					str.append(CMLib.login().generateRandomName(3, 8)).append(", ");
				if(mob.session()!=null)
					mob.session().rawPrint(str.toString()+"\n");
			}
			else
			if(what.equalsIgnoreCase("edrecipe"))
			{
				final boolean save = CMParms.combine(commands,2).equalsIgnoreCase("save");
				for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
				{
					final Ability A=e.nextElement();
					if(A instanceof ItemCraftor)
					{
						final ItemCraftor iA=(ItemCraftor)A;
						if(iA.parametersFormat().length()>0)
							CMLib.ableParms().testRecipeParsing(iA.parametersFile(),iA.parametersFormat(),save);
					}
				}
			}
			else
			if(what.equalsIgnoreCase("scriptable"))
			{
				final Area A=CMClass.getAreaType("StdArea");
				A.setName(_("UNKNOWNAREA"));
				final Room R=CMClass.getLocale("WoodRoom");
				R.setRoomID("UNKNOWN1");
				R.setArea(A);
				final MOB M=CMClass.getMOB("GenShopkeeper");
				M.setName(_("Shoppy"));
				final ShopKeeper SK=(ShopKeeper)M;
				Item I=CMClass.getWeapon("Dagger");
				SK.getShop().addStoreInventory(I,10,5);
				I=CMClass.getWeapon("Shortsword");
				SK.getShop().addStoreInventory(I,10,5);
				SK.setInvResetRate(999999999);
				final Room R2=CMClass.getLocale("WoodRoom");
				R2.setRoomID("UNKNOWN2");
				R2.setArea(A);
				R.rawDoors()[Directions.NORTH]=R2;
				R2.rawDoors()[Directions.SOUTH]=R;
				R.setRawExit(Directions.NORTH,CMClass.getExit("Open"));
				R2.setRawExit(Directions.SOUTH,CMClass.getExit("Open"));
				final Behavior B=CMClass.getBehavior("Scriptable");
				B.setParms("LOAD=progs/scriptableTest.script");
				M.addBehavior(B);
				M.text();
				M.bringToLife(R,true);
				M.setStartRoom(null);
				final ScriptingEngine S=(ScriptingEngine)M.fetchBehavior("Scriptable");
				for(int i=0;i<1000;i++)
				{
					try{ Thread.sleep(1000); } catch(final Exception e){}
					if(S.getVar("Shoppy","ERRORS").length()>0)
						break;
				}
				mob.tell(_("Successes: @x1",S.getVar("Shoppy","SUCCESS")));
				mob.tell(_("\n\rUntested: @x1",S.getVar("Shoppy","UNTESTED")));
				mob.tell(_("\n\rErrors: @x1",S.getVar("Shoppy","ERRORS")));
				M.destroy();
				R2.destroy();
				R.destroy();
				A.destroy();
				Resources.removeResource("PARSEDPRG: LOAD=progs/scriptableTest.script");
			}
			else
			if(what.equalsIgnoreCase("mudhourstil"))
			{
				final String startDate=CMParms.combine(commands,2);
				final int x=startDate.indexOf('-');
				final int mudmonth=CMath.s_int(startDate.substring(0,x));
				final int mudday=CMath.s_int(startDate.substring(x+1));
				final TimeClock C=(TimeClock)CMClass.getCommon("DefaultTimeClock");
				final TimeClock NOW=mob.location().getArea().getTimeObj();
				C.setMonth(mudmonth);
				C.setDayOfMonth(mudday);
				C.setHourOfDay(0);
				if((mudmonth<NOW.getMonth())
				||((mudmonth==NOW.getMonth())&&(mudday<NOW.getDayOfMonth())))
					C.setYear(NOW.getYear()+1);
				else
					C.setYear(NOW.getYear());
				final long millidiff=C.deriveMillisAfter(NOW);
				mob.tell(_("MilliDiff=@x1",""+millidiff));
				return true;
			}
			else
			if(what.equalsIgnoreCase("horsedraggers"))
			{
				final MOB M=CMClass.getMOB("GenMOB");
				M.setName(_("MrRider"));
				M.setDisplayText(_("MrRider is here"));
				final Behavior B=CMClass.getBehavior("Mobile");
				B.setParms("min=1 max=1 chance=99 wander");
				M.addBehavior(B);
				M.bringToLife(mob.location(),true);
				final MOB M2=CMClass.getMOB("GenRideable");
				M2.setName(_("a pack horse"));
				M2.setDisplayText(_("a pack horse is here"));
				M2.bringToLife(mob.location(),true);
				M.setRiding((Rideable)M2);
				final Behavior B2=CMClass.getBehavior("Scriptable");
				B2.setParms("RAND_PROG 100;IF !ISHERE(nondescript);MPECHO LOST MY CONTAINER $d $D!; GOSSIP LOST MY CONTAINER! $d $D; MPPURGE $i;ENDIF;~;");
				M2.addBehavior(B2);
				final Item I=CMClass.getBasicItem("LockableContainer");
				mob.location().addItem(I,ItemPossessor.Expire.Player_Drop);
				I.setRiding((Rideable)M2);
			}

			Ability A2=null;
			Item I=null;
			CMMsg msg=null;
			Command C=null;
			Item IS[]=new Item[2];
			final Room R=mob.location();
			final Room upRoom=R.rawDoors()[Directions.UP];
			final Exit upExit=R.getRawExit(Directions.UP);
			final Room R2=CMClass.getLocale("StoneRoom");
			R2.setArea(R.getArea());
			R.setRawExit(Directions.UP,CMClass.getExit("Open"));
			R2.setRawExit(Directions.DOWN,CMClass.getExit("Open"));
			R.rawDoors()[Directions.UP]=R2;
			R2.rawDoors()[Directions.DOWN]=R;
			final MOB[] mobs=new MOB[2];
			final MOB[] backups=new MOB[2];
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_HaveEnabler"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability HaveEnabler=CMClass.getAbility("Prop_HaveEnabler");
				HaveEnabler.setMiscText(semiSpellList());
				mob.tell(_("Test#1-1: @x1",HaveEnabler.accountForYourself()));
				IS=giveTo(CMClass.getWeapon("Sword"),HaveEnabler,mobs[0],null,0);
				if(!spellCheck(spells,mobs[0])){ mob.tell(_("Error1-1")); return false;}
				IS[0].unWear();
				R.moveItemTo(IS[0],ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
				R.recoverRoomStats();
				if(spellCheck(spells,mobs[0])){ mob.tell(_("Error1-2")); return false;}

				reset(mobs,backups,R,IS,R2);
				HaveEnabler.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
				mob.tell(_("Test#1-2: @x1",HaveEnabler.accountForYourself()));
				IS=giveTo(CMClass.getWeapon("Sword"),HaveEnabler,mobs[0],mobs[1],0);
				if(!spellCheck(spells,mobs[0])){ mob.tell(_("Error1-3")); return false;}
				if(spellCheck(spells,mobs[1])){ mob.tell(_("Error1-4")); return false;}
				IS[0].unWear();
				IS[1].unWear();
				R.moveItemTo(IS[0],ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
				R.moveItemTo(IS[1],ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
				R.recoverRoomStats();
				if(spellCheck(spells,mobs[0])){ mob.tell(_("Error1-5")); return false;}
				if(spellCheck(spells,mobs[1])){ mob.tell(_("Error1-6")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_HaveSpellCast"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability HaveSpellCast=CMClass.getAbility("Prop_HaveSpellCast");
				HaveSpellCast.setMiscText(semiSpellList());
				mob.tell(_("Test#2-1: @x1",HaveSpellCast.accountForYourself()));
				IS=giveTo(CMClass.getWeapon("Sword"),HaveSpellCast,mobs[0],null,0);
				if(!effectCheck(spells,mobs[0])){ mob.tell(_("Error2-1")); return false;}
				IS[0].unWear();
				R.moveItemTo(IS[0],ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
				R.recoverRoomStats();
				if(effectCheck(spells,mobs[0])){ mob.tell(_("Error2-2")); return false;}

				reset(mobs,backups,R,IS,R2);
				HaveSpellCast.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
				mob.tell(_("Test#2-2: @x1",HaveSpellCast.accountForYourself()));
				IS=giveTo(CMClass.getWeapon("Sword"),HaveSpellCast,mobs[0],mobs[1],0);
				if(!effectCheck(spells,mobs[0])){ mob.tell(_("Error2-3")); return false;}
				if(effectCheck(spells,mobs[1])){ mob.tell(_("Error2-4")); return false;}
				IS[0].unWear();
				IS[1].unWear();
				R.moveItemTo(IS[0],ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
				R.moveItemTo(IS[1],ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
				R.recoverRoomStats();
				if(effectCheck(spells,mobs[0])){ mob.tell(_("Error2-5")); return false;}
				if(effectCheck(spells,mobs[1])){ mob.tell(_("Error2-6")); return false;}

				reset(mobs,backups,R,IS,R2);
				HaveSpellCast.setMiscText(semiSpellList()+"MASK=-Human");
				mob.tell(_("Test#2-3: @x1",HaveSpellCast.accountForYourself()));
				IS=giveTo(CMClass.getWeapon("Sword"),HaveSpellCast,mobs[0],mobs[1],0);
				if(!effectCheck(spells,mobs[0])){ mob.tell(_("Error2-7")); return false;}
				if(effectCheck(spells,mobs[1])){ mob.tell(_("Error2-8")); return false;}
				IS[0].unWear();
				IS[1].unWear();
				R.moveItemTo(IS[0],ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
				R.moveItemTo(IS[1],ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
				R.recoverRoomStats();
				if(effectCheck(spells,mobs[0])){ mob.tell(_("Error2-9")); return false;}
				if(effectCheck(spells,mobs[1])){ mob.tell(_("Error2-10")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_WearEnabler"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability WearEnabler=CMClass.getAbility("Prop_WearEnabler");
				WearEnabler.setMiscText(semiSpellList());
				mob.tell(_("Test#3-1: @x1",WearEnabler.accountForYourself()));
				IS=giveTo(CMClass.getWeapon("Sword"),WearEnabler,mobs[0],null,1);
				if(!spellCheck(spells,mobs[0])){ mob.tell(_("Error3-1")); return false;}
				IS[0].unWear();
				R.recoverRoomStats();
				if(spellCheck(spells,mobs[0])){ mob.tell(_("Error3-2")); return false;}

				reset(mobs,backups,R,IS,R2);
				WearEnabler.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
				mob.tell(_("Test#3-2: @x1",WearEnabler.accountForYourself()));
				IS=giveTo(CMClass.getWeapon("Sword"),WearEnabler,mobs[0],mobs[1],1);
				if(!spellCheck(spells,mobs[0])){ mob.tell(_("Error3-3")); return false;}
				if(spellCheck(spells,mobs[1])){ mob.tell(_("Error3-4")); return false;}
				IS[0].unWear();
				IS[1].unWear();
				R.recoverRoomStats();
				if(spellCheck(spells,mobs[0])){ mob.tell(_("Error3-5")); return false;}
				if(spellCheck(spells,mobs[1])){ mob.tell(_("Error3-6")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_WearSpellCast"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability WearSpellCast=CMClass.getAbility("Prop_WearSpellCast");
				WearSpellCast.setMiscText(semiSpellList());
				mob.tell(_("Test#4-1: @x1",WearSpellCast.accountForYourself()));
				IS=giveTo(CMClass.getWeapon("Sword"),WearSpellCast,mobs[0],null,1);
				if(!effectCheck(spells,mobs[0])){ mob.tell(_("Error4-1")); return false;}
				IS[0].unWear();
				R.recoverRoomStats();
				if(effectCheck(spells,mobs[0])){ mob.tell(_("Error4-2")); return false;}

				reset(mobs,backups,R,IS,R2);
				WearSpellCast.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
				mob.tell(_("Test#4-2: @x1",WearSpellCast.accountForYourself()));
				IS=giveTo(CMClass.getWeapon("Sword"),WearSpellCast,mobs[0],mobs[1],1);
				if(!effectCheck(spells,mobs[0])){ mob.tell(_("Error4-3")); return false;}
				if(effectCheck(spells,mobs[1])){ mob.tell(_("Error4-4")); return false;}
				IS[0].unWear();
				IS[1].unWear();
				R.recoverRoomStats();
				if(effectCheck(spells,mobs[0])){ mob.tell(_("Error4-5")); return false;}
				if(effectCheck(spells,mobs[1])){ mob.tell(_("Error4-6")); return false;}

				reset(mobs,backups,R,IS,R2);
				WearSpellCast.setMiscText(semiSpellList()+"MASK=-Human");
				mob.tell(_("Test#4-3: @x1",WearSpellCast.accountForYourself()));
				IS=giveTo(CMClass.getWeapon("Sword"),WearSpellCast,mobs[0],mobs[1],1);
				if(!effectCheck(spells,mobs[0])){ mob.tell(_("Error4-7")); return false;}
				if(effectCheck(spells,mobs[1])){ mob.tell(_("Error4-8")); return false;}
				IS[0].unWear();
				IS[1].unWear();
				R.recoverRoomStats();
				if(effectCheck(spells,mobs[0])){ mob.tell(_("Error4-9")); return false;}
				if(effectCheck(spells,mobs[1])){ mob.tell(_("Error4-10")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_RideEnabler"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability RideEnabler=CMClass.getAbility("Prop_RideEnabler");
				RideEnabler.setMiscText(semiSpellList());
				mob.tell(_("Test#5-1: @x1",RideEnabler.accountForYourself()));
				IS=giveTo(CMClass.getItem("Boat"),RideEnabler,mobs[0],null,2);
				if(!spellCheck(spells,mobs[0])){ mob.tell(_("Error5-1")); return false;}
				mobs[0].setRiding(null);
				R.recoverRoomStats();
				if(spellCheck(spells,mobs[0])){ mob.tell(_("Error5-2")); return false;}

				reset(mobs,backups,R,IS,R2);
				RideEnabler.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
				mob.tell(_("Test#5-2: @x1",RideEnabler.accountForYourself()));
				IS=giveTo(CMClass.getItem("Boat"),RideEnabler,mobs[0],mobs[1],2);
				if(!spellCheck(spells,mobs[0])){ mob.tell(_("Error5-3")); return false;}
				if(spellCheck(spells,mobs[1])){ mob.tell(_("Error5-4")); return false;}
				mobs[0].setRiding(null);
				mobs[1].setRiding(null);
				R.recoverRoomStats();
				if(spellCheck(spells,mobs[0])){ mob.tell(_("Error5-5")); return false;}
				if(spellCheck(spells,mobs[1])){ mob.tell(_("Error5-6")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_RideSpellCast"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability RideSpellCast=CMClass.getAbility("Prop_RideSpellCast");
				RideSpellCast.setMiscText(semiSpellList());
			   // mob.tell(_("Test#6-1: @x1",RideSpellCast.accountForYourself()));
				IS=giveTo(CMClass.getItem("Boat"),RideSpellCast,mobs[0],null,2);
				if(!effectCheck(spells,mobs[0])){ mob.tell(_("Error6-1")); return false;}
				mobs[0].setRiding(null);
				R.recoverRoomStats();
				if(effectCheck(spells,mobs[0])){ mob.tell(_("Error6-2")); return false;}

				reset(mobs,backups,R,IS,R2);
				RideSpellCast.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
				mob.tell(_("Test#6-2: @x1",RideSpellCast.accountForYourself()));
				IS=giveTo(CMClass.getItem("Boat"),RideSpellCast,mobs[0],mobs[1],2);
				if(!effectCheck(spells,mobs[0])){ mob.tell(_("Error6-3")); return false;}
				if(effectCheck(spells,mobs[1])){ mob.tell(_("Error6-4")); return false;}
				mobs[0].setRiding(null);
				mobs[1].setRiding(null);
				R.recoverRoomStats();
				if(effectCheck(spells,mobs[0])){ mob.tell(_("Error6-5")); return false;}
				if(effectCheck(spells,mobs[1])){ mob.tell(_("Error6-6")); return false;}

				reset(mobs,backups,R,IS,R2);
				RideSpellCast.setMiscText(semiSpellList()+"MASK=-Human");
				mob.tell(_("Test#6-3: @x1",RideSpellCast.accountForYourself()));
				IS=giveTo(CMClass.getItem("Boat"),RideSpellCast,mobs[0],mobs[1],2);
				if(!effectCheck(spells,mobs[0])){ mob.tell(_("Error6-7")); return false;}
				if(effectCheck(spells,mobs[1])){ mob.tell(_("Error6-8")); return false;}
				mobs[0].setRiding(null);
				mobs[1].setRiding(null);
				R.recoverRoomStats();
				if(effectCheck(spells,mobs[0])){ mob.tell(_("Error6-9")); return false;}
				if(effectCheck(spells,mobs[1])){ mob.tell(_("Error6-10")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_HereSpellCast"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability HereSpellCast=CMClass.getAbility("Prop_HereSpellCast");
				HereSpellCast.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
				mob.tell(_("Test#7-1: @x1",HereSpellCast.accountForYourself()));
				A2=((Ability)HereSpellCast.copyOf());
				A2.setMiscText((HereSpellCast).text());
				R2.addNonUninvokableEffect(A2);
				R2.recoverRoomStats();
				CMLib.tracking().walk(mobs[0],Directions.UP,false,false);
				if(!effectCheck(spells,mobs[0])){ mob.tell(_("Error7-1")); return false;}
				CMLib.tracking().walk(mobs[0],Directions.DOWN,false,false);
				if(effectCheck(spells,mobs[0])){ mob.tell(_("Error7-2")); return false;}

				reset(mobs,backups,R,IS,R2);
				HereSpellCast.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
				mob.tell(_("Test#7-2: @x1",HereSpellCast.accountForYourself()));
				A2=((Ability)HereSpellCast.copyOf());
				A2.setMiscText((HereSpellCast).text());
				R2.addNonUninvokableEffect(A2);
				R2.recoverRoomStats();
				CMLib.tracking().walk(mobs[0],Directions.UP,false,false);
				CMLib.tracking().walk(mobs[1],Directions.UP,false,false);
				if(!effectCheck(spells,mobs[0])){ mob.tell(_("Error7-3")); return false;}
				if(effectCheck(spells,mobs[1])){ mob.tell(_("Error7-4")); return false;}
				CMLib.tracking().walk(mobs[0],Directions.DOWN,false,false);
				CMLib.tracking().walk(mobs[1],Directions.DOWN,false,false);
				if(effectCheck(spells,mobs[0])){ mob.tell(_("Error7-5")); return false;}
				if(effectCheck(spells,mobs[1])){ mob.tell(_("Error7-6")); return false;}

				reset(mobs,backups,R,IS,R2);
				HereSpellCast.setMiscText(semiSpellList()+"MASK=-Human");
				mob.tell(_("Test#7-3: @x1",HereSpellCast.accountForYourself()));
				A2=((Ability)HereSpellCast.copyOf());
				A2.setMiscText((HereSpellCast).text());
				R2.addNonUninvokableEffect(A2);
				R2.recoverRoomStats();
				CMLib.tracking().walk(mobs[0],Directions.UP,false,false);
				CMLib.tracking().walk(mobs[1],Directions.UP,false,false);
				if(!effectCheck(spells,mobs[0])){ mob.tell(_("Error7-7")); return false;}
				if(effectCheck(spells,mobs[1])){ mob.tell(_("Error7-8")); return false;}
				CMLib.tracking().walk(mobs[0],Directions.DOWN,false,false);
				CMLib.tracking().walk(mobs[1],Directions.DOWN,false,false);
				if(effectCheck(spells,mobs[0])){ mob.tell(_("Error7-9")); return false;}
				if(effectCheck(spells,mobs[1])){ mob.tell(_("Error7-10")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_SpellAdder"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability SpellAdder=CMClass.getAbility("Prop_SpellAdder");
				SpellAdder.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
				mob.tell(_("Test#8-1: @x1",SpellAdder.accountForYourself()));
				R2.addNonUninvokableEffect(SpellAdder);
				R2.recoverRoomStats();
				CMLib.tracking().walk(mobs[0],Directions.UP,false,false);
				if(!effectCheck(spells,mobs[0])){ mob.tell(_("Error8-1")); return false;}
				CMLib.tracking().walk(mobs[0],Directions.DOWN,false,false);
				if(effectCheck(spells,mobs[0])){ mob.tell(_("Error8-2")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_UseSpellCast"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability UseSpellCast=CMClass.getAbility("Prop_UseSpellCast"); // put IN
				UseSpellCast.setMiscText(semiSpellList());
				mob.tell(_("Test#9-1: @x1",UseSpellCast.accountForYourself()));
				IS=giveTo(CMClass.getItem("SmallSack"),UseSpellCast,mobs[0],null,0);
				I=CMClass.getItem("StdFood");
				mobs[0].addItem(I);
				C=CMClass.getCommand("Put");
				C.execute(mobs[0],new XVector("Put","Food","Sack"),metaFlags);
				if(!effectCheck(spells,mobs[0])){ mob.tell(_("Error9-1")); return false;}
				R.recoverRoomStats();

				reset(mobs,backups,R,IS,R2);
				UseSpellCast.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
				mob.tell(_("Test#9-2: @x1",UseSpellCast.accountForYourself()));
				IS=giveTo(CMClass.getItem("SmallSack"),UseSpellCast,mobs[0],mobs[1],0);
				I=CMClass.getItem("StdFood");
				mobs[0].addItem(I);
				C=CMClass.getCommand("Put");
				C.execute(mobs[0],new XVector("Put","Food","Sack"),metaFlags);
				if(!effectCheck(spells,mobs[0])){ mob.tell(_("Error9-2")); return false;}
				I=CMClass.getItem("StdFood");
				mobs[1].addItem(I);
				C=CMClass.getCommand("Put");
				C.execute(mobs[1],new XVector("Put","Food","Sack"),metaFlags);
				if(effectCheck(spells,mobs[1])){ mob.tell(_("Error9-3")); return false;}
				R.recoverRoomStats();
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_UseSpellCast2"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability UseSpellCast2=CMClass.getAbility("Prop_UseSpellCast2"); // EAT
				UseSpellCast2.setMiscText(semiSpellList());
				mob.tell(_("Test#10-1: @x1",UseSpellCast2.accountForYourself()));
				IS=giveTo(CMClass.getItem("StdFood"),UseSpellCast2,mobs[0],null,0);
				C=CMClass.getCommand("Eat");
				C.execute(mobs[0],new XVector("Eat","ALL"),metaFlags);
				if(!effectCheck(spells,mobs[0])){ mob.tell(_("Error10-1")); return false;}
				R.recoverRoomStats();

				reset(mobs,backups,R,IS,R2);
				UseSpellCast2.setMiscText(semiSpellList()+"MASK=-RACE +Dwarf");
				mob.tell(_("Test#10-2: @x1",UseSpellCast2.accountForYourself()));
				IS=giveTo(CMClass.getItem("StdFood"),UseSpellCast2,mobs[0],mobs[1],0);
				C=CMClass.getCommand("Eat");
				C.execute(mobs[0],new XVector("Eat","ALL"),metaFlags);
				if(!effectCheck(spells,mobs[0])){ mob.tell(_("Error10-2")); return false;}
				C=CMClass.getCommand("Eat");
				C.execute(mobs[1],new XVector("Eat","ALL"),metaFlags);
				if(effectCheck(spells,mobs[1])){ mob.tell(_("Error10-3")); return false;}
				R.recoverRoomStats();
			}
			if(what.equalsIgnoreCase("metaflags")||what.equalsIgnoreCase("all"))
			{
				final StringBuffer str=new StringBuffer("");
				if(CMath.bset(metaFlags,Command.METAFLAG_AS))
					str.append(_(" AS "));
				if(CMath.bset(metaFlags,Command.METAFLAG_FORCED))
					str.append(_(" FORCED "));
				if(CMath.bset(metaFlags,Command.METAFLAG_MPFORCED))
					str.append(_(" MPFORCED "));
				if(CMath.bset(metaFlags,Command.METAFLAG_ORDER))
					str.append(_(" ORDERED "));
				if(CMath.bset(metaFlags,Command.METAFLAG_POSSESSED))
					str.append(_(" POSSESSED "));
				if(CMath.bset(metaFlags,Command.METAFLAG_SNOOPED))
					str.append(_(" SNOOPED "));
				mob.tell(str.toString());
			}
			if(what.equalsIgnoreCase("cmparms")||what.equalsIgnoreCase("all"))
			{
				List<String> V=CMParms.parseAny("blah~BLAH~BLAH!",'~',true);
				if(V.size()!=3){ mob.tell(_("Error cmparms-1")); return false;}
				if(!CMParms.combineWithX(V, "~", 0).equals("blah~BLAH~BLAH!~")){ mob.tell(_("Error cmparms-2")); return false;}

				V=CMParms.parseAny("blah~~",'~',true);
				if(V.size()!=1){ mob.tell(_("Error cmparms-3")); return false;}
				if(!V.get(0).equals("blah")){ mob.tell(_("Error cmparms-4")); return false;}

				V=CMParms.parseAny("blah~~",'~',false);
				if(V.size()!=3){ mob.tell(_("Error cmparms-5")); return false;}
				if(!CMParms.combineWithX(V, "~", 0).equals("blah~~~")){ mob.tell(_("Error cmparms-6")); return false;}

				V=CMParms.parseAny("blah~~BLAH~~BLAH!","~~",true);
				if(V.size()!=3){ mob.tell(_("Error cmparms-7")); return false;}
				if(!CMParms.combineWithX(V, "~~", 0).equals("blah~~BLAH~~BLAH!~~")){ mob.tell(_("Error cmparms-8")); return false;}

				V=CMParms.parseAny("blah~~~~","~~",true);
				if(V.size()!=1){ mob.tell(_("Error cmparms-9")); return false;}
				if(!V.get(0).equals("blah")){ mob.tell(_("Error cmparms-10")); return false;}

				V=CMParms.parseAny("blah~~~~","~~",false);
				if(V.size()!=3){ mob.tell(_("Error cmparms-11")); return false;}
				if(!CMParms.combineWithX(V, "~~", 0).equals("blah~~~~~~")){ mob.tell(_("Error cmparms-12")); return false;}

				V=CMParms.parseSentences("blah. blahblah. poo");
				if(V.size()!=3){ mob.tell(_("Error cmparms-13")); return false;}
				if(!V.get(0).equals("blah.")){ mob.tell(_("Error cmparms-14:@x1",V.get(0))); return false;}
				if(!V.get(1).equals("blahblah.")){ mob.tell(_("Error cmparms-15:@x1",V.get(1))); return false;}
				if(!V.get(2).equals("poo")){ mob.tell(_("Error cmparms-16:@x1",V.get(2))); return false;}

				V=CMParms.parseAny("blah~BLAH~BLAH!~",'~',true);
				if(V.size()!=3){ mob.tell(_("Error cmparms-17")); return false;}
				if(!CMParms.combineWithX(V, "~", 0).equals("blah~BLAH~BLAH!~")){ mob.tell(_("Error cmparms-18")); return false;}

				V=CMParms.parseAny("blah~~BLAH~~BLAH!~~","~~",true);
				if(V.size()!=3){ mob.tell(_("Error cmparms-19")); return false;}
				if(!CMParms.combineWithX(V, "~~", 0).equals("blah~~BLAH~~BLAH!~~")){ mob.tell(_("Error cmparms-20")); return false;}

				V=CMParms.parseAny("blah~BLAH~BLAH!~",'~',false);
				if(V.size()!=4){ mob.tell(_("Error cmparms-21")); return false;}
				if(!CMParms.combineWithX(V, "~", 0).equals("blah~BLAH~BLAH!~~")){ mob.tell(_("Error cmparms-22")); return false;}

				V=CMParms.parseAny("blah~~BLAH~~BLAH!~~","~~",false);
				if(V.size()!=4){ mob.tell(_("Error cmparms-23")); return false;}
				if(!CMParms.combineWithX(V, "~~", 0).equals("blah~~BLAH~~BLAH!~~~~")){ mob.tell(_("Error cmparms-24")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_FightSpellCast"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability FightSpellCast=CMClass.getAbility("Prop_FightSpellCast");
				FightSpellCast.setMiscText(maliciousSemiSpellList());
				mob.tell(_("Test#11-1: @x1",FightSpellCast.accountForYourself()));
				IS=giveTo(CMClass.getWeapon("Sword"),FightSpellCast,mobs[0],null,1);
				if(effectCheck(maliciousspells,mobs[1])){ mob.tell(_("Error11-1")); return false;}
				if(effectCheck(maliciousspells,mobs[0])){ mob.tell(_("Error11-2")); return false;}
				for(int i=0;i<100;i++)
				{
					mobs[1].curState().setHitPoints(1000);
					mobs[0].curState().setHitPoints(1000);
					CMLib.combat().postAttack(mobs[0],mobs[1],mobs[0].fetchWieldedItem());
					if(effectCheck(maliciousspells,mobs[1]))
						break;
				}
				if(!effectCheck(maliciousspells,mobs[1])){ mob.tell(_("Error11-3")); return false;}
				R.recoverRoomStats();

				reset(mobs,backups,R,IS,R2);
				FightSpellCast.setMiscText(maliciousSemiSpellList()+"MASK=-RACE +Human");
				mob.tell(_("Test#11-2: @x1",FightSpellCast.accountForYourself()));
				IS=giveTo(CMClass.getWeapon("Sword"),FightSpellCast,mobs[1],null,1);
				if(effectCheck(maliciousspells,mobs[1])){ mob.tell(_("Error11-4")); return false;}
				if(effectCheck(maliciousspells,mobs[0])){ mob.tell(_("Error11-5")); return false;}
				for(int i=0;i<100;i++)
				{
					mobs[1].curState().setHitPoints(1000);
					mobs[0].curState().setHitPoints(1000);
					CMLib.combat().postAttack(mobs[1],mobs[0],mobs[1].fetchWieldedItem());
					if(effectCheck(maliciousspells,mobs[1]))
						break;
				}
				if(effectCheck(maliciousspells,mobs[1])){ mob.tell(_("Error11-6")); return false;}
				R.recoverRoomStats();

				reset(mobs,backups,R,IS,R2);
				FightSpellCast.setMiscText(maliciousSemiSpellList()+"MASK=-RACE +Human");
				mob.tell(_("Test#11-3: @x1",FightSpellCast.accountForYourself()));
				IS=giveTo(CMClass.getWeapon("Sword"),FightSpellCast,mobs[0],null,1);
				if(effectCheck(maliciousspells,mobs[1])){ mob.tell(_("Error11-7")); return false;}
				if(effectCheck(maliciousspells,mobs[0])){ mob.tell(_("Error11-8")); return false;}
				for(int i=0;i<100;i++)
				{
					mobs[1].curState().setHitPoints(1000);
					mobs[0].curState().setHitPoints(1000);
					CMLib.combat().postAttack(mobs[0],mobs[1],mobs[0].fetchWieldedItem());
					if(effectCheck(maliciousspells,mobs[1]))
						break;
				}
				if(!effectCheck(maliciousspells,mobs[1])){ mob.tell(_("Error11-9")); return false;}
				R.recoverRoomStats();
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_HaveZapper"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability HaveZapper=CMClass.getAbility("Prop_HaveZapper");
				HaveZapper.setMiscText("-RACE +Dwarf");
				mob.tell(_("Test#12-1: @x1",HaveZapper.accountForYourself()));
				IS=giveTo(CMClass.getWeapon("Sword"),HaveZapper,mobs[0],mobs[1],2);
				CMLib.commands().postGet(mobs[0],null,IS[0],false);
				CMLib.commands().postGet(mobs[1],null,IS[1],false);
				if(!mobs[0].isMine(IS[0])){ mob.tell(_("Error12-1")); return false;}
				if(mobs[1].isMine(IS[1])){ mob.tell(_("Error12-2")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_RideZapper"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability RideZapper=CMClass.getAbility("Prop_RideZapper");
				RideZapper.setMiscText("-RACE +Dwarf");
				mob.tell(_("Test#13-1: @x1",RideZapper.accountForYourself()));
				IS=giveTo(CMClass.getItem("Boat"),RideZapper,mobs[0],mobs[1],3);
				msg=CMClass.getMsg(mobs[0],IS[0],null,CMMsg.MSG_MOUNT,_("<S-NAME> mount(s) <T-NAMESELF>."));
				if(R.okMessage(mobs[0],msg)) R.send(mobs[0],msg);
				msg=CMClass.getMsg(mobs[1],IS[1],null,CMMsg.MSG_MOUNT,_("<S-NAME> mount(s) <T-NAMESELF>."));
				if(R.okMessage(mobs[1],msg)) R.send(mobs[1],msg);
				if(mobs[0].riding()!=IS[0]){ mob.tell(_("Error13-1")); return false;}
				if(mobs[1].riding()==IS[1]){ mob.tell(_("Error13-2")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_WearZapper"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability WearZapper=CMClass.getAbility("Prop_WearZapper");
				WearZapper.setMiscText("-RACE +Dwarf");
				mob.tell(_("Test#14-1: @x1",WearZapper.accountForYourself()));
				IS=giveTo(CMClass.getWeapon("Sword"),WearZapper,mobs[0],mobs[1],0);
				msg=CMClass.getMsg(mobs[0],IS[0],null,CMMsg.MSG_WIELD,_("<S-NAME> wield(s) <T-NAMESELF>."));
				if(R.okMessage(mobs[0],msg)) R.send(mobs[0],msg);
				msg=CMClass.getMsg(mobs[1],IS[1],null,CMMsg.MSG_WIELD,_("<S-NAME> wield(s) <T-NAMESELF>."));
				if(R.okMessage(mobs[1],msg)) R.send(mobs[1],msg);
				if(IS[0].amWearingAt(Wearable.IN_INVENTORY)){ mob.tell(_("Error14-1")); return false;}
				if(!IS[1].amWearingAt(Wearable.IN_INVENTORY)){ mob.tell(_("Error14-2")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_Resistance"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability Resistance=CMClass.getAbility("Prop_Resistance");
				Resistance.setMiscText("pierce 100% holy 100% acid 30%");
				mob.tell(_("Test#15-1: @x1",Resistance.accountForYourself()));
				if(testResistance(mobs[0])){ mob.tell(_("Error15-1")); return false;}
				giveAbility(mobs[0],Resistance);
				R.recoverRoomStats();
				if(!testResistance(mobs[0])){ mob.tell(_("Error15-2")); return false;}

				reset(mobs,backups,R,IS,R2);
				Resistance.setMiscText("pierce 100% holy 100% acid 30% MASK=-RACE +DWARF");
				mob.tell(_("Test#15-2: @x1",Resistance.accountForYourself()));
				if(testResistance(mobs[0])){ mob.tell(_("Error15-3")); return false;}
				if(testResistance(mobs[1])){ mob.tell(_("Error15-4")); return false;}
				giveAbility(mobs[0],Resistance);
				giveAbility(mobs[1],Resistance);
				R.recoverRoomStats();
				if(!testResistance(mobs[0])){ mob.tell(_("Error15-5")); return false;}
				if(testResistance(mobs[1])){ mob.tell(_("Error15-6")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_HaveResister"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability HaveResister=CMClass.getAbility("Prop_HaveResister");
				HaveResister.setMiscText("pierce 100% holy 100% acid 30%");
				mob.tell(_("Test#16-1: @x1",HaveResister.accountForYourself()));
				if(testResistance(mobs[0])){ mob.tell(_("Error16-1")); return false;}
				IS=giveTo(CMClass.getItem("SmallSack"),HaveResister,mobs[0],null,0);
				R.recoverRoomStats();
				if(!testResistance(mobs[0])){ mob.tell(_("Error16-2")); return false;}
				IS[0].unWear();
				R.moveItemTo(IS[0],ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
				R.recoverRoomStats();
				if(testResistance(mobs[0])){ mob.tell(_("Error16-3")); return false;}

				reset(mobs,backups,R,IS,R2);
				HaveResister.setMiscText("pierce 100% holy 100% acid 30% MASK=-RACE +DWARF");
				mob.tell(_("Test#16-2: @x1",HaveResister.accountForYourself()));
				if(testResistance(mobs[0])){ mob.tell(_("Error16-4")); return false;}
				if(testResistance(mobs[1])){ mob.tell(_("Error16-5")); return false;}
				IS=giveTo(CMClass.getItem("SmallSack"),HaveResister,mobs[0],mobs[1],0);
				R.recoverRoomStats();
				if(!testResistance(mobs[0])){ mob.tell(_("Error16-6")); return false;}
				if(testResistance(mobs[1])){ mob.tell(_("Error16-7")); return false;}
				IS[0].unWear();
				IS[1].unWear();
				R.moveItemTo(IS[0],ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
				R.moveItemTo(IS[1],ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
				R.recoverRoomStats();
				if(testResistance(mobs[0])){ mob.tell(_("Error16-8")); return false;}
				if(testResistance(mobs[1])){ mob.tell(_("Error16-9")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_WearResister"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability WearResister=CMClass.getAbility("Prop_WearResister");
				WearResister.setMiscText("pierce 100% holy 100% acid 30%");
				mob.tell(_("Test#17-1: @x1",WearResister.accountForYourself()));
				if(testResistance(mobs[0])){ mob.tell(_("Error17-1")); return false;}
				IS=giveTo(CMClass.getWeapon("Sword"),WearResister,mobs[0],null,1);
				R.recoverRoomStats();
				if(!testResistance(mobs[0])){ mob.tell(_("Error17-2")); return false;}
				IS[0].unWear();
				R.recoverRoomStats();
				if(testResistance(mobs[0])){ mob.tell(_("Error17-3")); return false;}

				reset(mobs,backups,R,IS,R2);
				WearResister.setMiscText("pierce 100% holy 100% acid 30% MASK=-RACE +DWARF");
				mob.tell(_("Test#17-2: @x1",WearResister.accountForYourself()));
				if(testResistance(mobs[0])){ mob.tell(_("Error17-4")); return false;}
				if(testResistance(mobs[1])){ mob.tell(_("Error17-5")); return false;}
				IS=giveTo(CMClass.getWeapon("Sword"),WearResister,mobs[0],mobs[1],1);
				R.recoverRoomStats();
				if(!testResistance(mobs[0])){ mob.tell(_("Error17-6")); return false;}
				if(testResistance(mobs[1])){ mob.tell(_("Error17-7")); return false;}
				IS[0].unWear();
				IS[1].unWear();
				R.recoverRoomStats();
				if(testResistance(mobs[0])){ mob.tell(_("Error17-8")); return false;}
				if(testResistance(mobs[1])){ mob.tell(_("Error17-9")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_RideResister"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability RideResister=CMClass.getAbility("Prop_RideResister");
				RideResister.setMiscText("pierce 100% holy 100% acid 30%");
				mob.tell(_("Test#18-1: @x1",RideResister.accountForYourself()));
				if(testResistance(mobs[0])){ mob.tell(_("Error18-1")); return false;}
				IS=giveTo(CMClass.getItem("Boat"),RideResister,mobs[0],null,2);
				R.recoverRoomStats();
				if(!testResistance(mobs[0])){ mob.tell(_("Error18-2")); return false;}
				mobs[0].setRiding(null);
				R.recoverRoomStats();
				if(testResistance(mobs[0])){ mob.tell(_("Error18-3")); return false;}

				reset(mobs,backups,R,IS,R2);
				RideResister.setMiscText("pierce 100% holy 100% acid 30% MASK=-RACE +DWARF");
				mob.tell(_("Test#18-2: @x1",RideResister.accountForYourself()));
				if(testResistance(mobs[0])){ mob.tell(_("Error18-4")); return false;}
				if(testResistance(mobs[1])){ mob.tell(_("Error18-5")); return false;}
				IS=giveTo(CMClass.getItem("Boat"),RideResister,mobs[0],mobs[1],2);
				R.recoverRoomStats();
				if(!testResistance(mobs[0])){ mob.tell(_("Error18-6")); return false;}
				if(testResistance(mobs[1])){ mob.tell(_("Error18-7")); return false;}
				mobs[0].setRiding(null);
				mobs[1].setRiding(null);
				R.recoverRoomStats();
				if(testResistance(mobs[0])){ mob.tell(_("Error18-8")); return false;}
				if(testResistance(mobs[1])){ mob.tell(_("Error18-9")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_HaveAdjuster"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability HaveAdjuster=CMClass.getAbility("Prop_HaveAdjuster");
				HaveAdjuster.setMiscText("abi+10 gen=F class=Fighter cha+10 man+1000");
				mob.tell(_("Test#19-1: @x1",HaveAdjuster.accountForYourself()));
				if(isAnyAdjusted(mobs[0])){ mob.tell(_("Error19-1")); return false;}
				IS=giveTo(CMClass.getItem("SmallSack"),HaveAdjuster,mobs[0],null,0);
				R.recoverRoomStats();
				if(!isAllAdjusted(mobs[0])){ mob.tell(_("Error19-2")); return false;}
				IS[0].unWear();
				R.moveItemTo(IS[0],ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
				R.recoverRoomStats();
				if(isAnyAdjusted(mobs[0])){ mob.tell(_("Error19-3")); return false;}

				HaveAdjuster.setMiscText("abi+10 gen=F class=Fighter cha+10 man+1000 MASK=-RACE +Dwarf");
				mob.tell(_("Test#19-2: @x1",HaveAdjuster.accountForYourself()));
				if(isAnyAdjusted(mobs[0])){ mob.tell(_("Error19-4")); return false;}
				if(isAnyAdjusted(mobs[1])){ mob.tell(_("Error19-5")); return false;}
				IS=giveTo(CMClass.getItem("SmallSack"),HaveAdjuster,mobs[0],mobs[1],0);
				R.recoverRoomStats();
				if(!isAllAdjusted(mobs[0])){ mob.tell(_("Error19-6")); return false;}
				if(isAnyAdjusted(mobs[1])){ mob.tell(_("Error19-7")); return false;}
				IS[0].unWear();
				IS[1].unWear();
				R.moveItemTo(IS[0],ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
				R.moveItemTo(IS[1],ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
				R.recoverRoomStats();
				if(isAnyAdjusted(mobs[0])){ mob.tell(_("Error19-8")); return false;}
				if(isAnyAdjusted(mobs[0])){ mob.tell(_("Error19-9")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_WearAdjuster"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability WearAdjuster=CMClass.getAbility("Prop_WearAdjuster");
				WearAdjuster.setMiscText("abi+10 gen=F class=Fighter cha+10 man+1000");
				mob.tell(_("Test#20-1: @x1",WearAdjuster.accountForYourself()));
				if(isAnyAdjusted(mobs[0])){ mob.tell(_("Error20-1")); return false;}
				IS=giveTo(CMClass.getItem("SmallSack"),WearAdjuster,mobs[0],null,1);
				R.recoverRoomStats();
				if(!isAllAdjusted(mobs[0])){ mob.tell(_("Error20-2")); return false;}
				IS[0].unWear();
				R.recoverRoomStats();
				if(isAnyAdjusted(mobs[0])){ mob.tell(_("Error20-3")); return false;}

				WearAdjuster.setMiscText("abi+10 gen=F class=Fighter cha+10 man+1000 MASK=-RACE +Dwarf");
				mob.tell(_("Test#20-1: @x1",WearAdjuster.accountForYourself()));
				if(isAnyAdjusted(mobs[0])){ mob.tell(_("Error20-4")); return false;}
				if(isAnyAdjusted(mobs[1])){ mob.tell(_("Error20-5")); return false;}
				IS=giveTo(CMClass.getItem("SmallSack"),WearAdjuster,mobs[0],mobs[1],1);
				R.recoverRoomStats();
				if(!isAllAdjusted(mobs[0])){ mob.tell(_("Error20-6")); return false;}
				if(isAnyAdjusted(mobs[1])){ mob.tell(_("Error20-7")); return false;}
				IS[0].unWear();
				IS[1].unWear();
				R.recoverRoomStats();
				if(isAnyAdjusted(mobs[0])){ mob.tell(_("Error20-8")); return false;}
				if(isAnyAdjusted(mobs[0])){ mob.tell(_("Error20-9")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_RideAdjuster"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability RideAdjuster=CMClass.getAbility("Prop_RideAdjuster");
				RideAdjuster.setMiscText("abi+10 gen=F class=Fighter cha+10 man+1000");
				mob.tell(_("Test#21-1: @x1",RideAdjuster.accountForYourself()));
				if(isAnyAdjusted(mobs[0])){ mob.tell(_("Error21-1")); return false;}
				IS=giveTo(CMClass.getItem("Boat"),RideAdjuster,mobs[0],null,2);
				R.recoverRoomStats();
				if(!isAllAdjusted(mobs[0])){ mob.tell(_("Error21-2")); return false;}
				mobs[0].setRiding(null);
				R.recoverRoomStats();
				if(isAnyAdjusted(mobs[0])){ mob.tell(_("Error21-3")); return false;}

				RideAdjuster.setMiscText("abi+10 gen=F class=Fighter cha+10 man+1000 MASK=-RACE +Dwarf");
				mob.tell(_("Test#21-1: @x1",RideAdjuster.accountForYourself()));
				if(isAnyAdjusted(mobs[0])){ mob.tell(_("Error21-4")); return false;}
				if(isAnyAdjusted(mobs[1])){ mob.tell(_("Error21-5")); return false;}
				IS=giveTo(CMClass.getItem("Boat"),RideAdjuster,mobs[0],mobs[1],2);
				R.recoverRoomStats();
				if(!isAllAdjusted(mobs[0])){ mob.tell(_("Error21-6")); return false;}
				if(isAnyAdjusted(mobs[1])){ mob.tell(_("Error21-7")); return false;}
				mobs[0].setRiding(null);
				mobs[1].setRiding(null);
				R.recoverRoomStats();
				if(isAnyAdjusted(mobs[0])){ mob.tell(_("Error21-8")); return false;}
				if(isAnyAdjusted(mobs[0])){ mob.tell(_("Error21-9")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_HereAdjuster"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability HereAdjuster=CMClass.getAbility("Prop_HereAdjuster");
				HereAdjuster.setMiscText("abi+10 gen=F class=Fighter cha+10 man+1000");
				mob.tell(_("Test#22-1: @x1",HereAdjuster.accountForYourself()));
				A2=((Ability)HereAdjuster.copyOf());
				A2.setMiscText((HereAdjuster).text());
				R2.addNonUninvokableEffect(A2);
				R2.recoverRoomStats();
				if(isAnyAdjusted(mobs[0])){ mob.tell(_("Error22-0")); return false;}
				CMLib.tracking().walk(mobs[0],Directions.UP,false,false);
				R2.recoverRoomStats();
				if(!isAllAdjusted(mobs[0])){ mob.tell(_("Error22-1")); return false;}
				CMLib.tracking().walk(mobs[0],Directions.DOWN,false,false);
				R2.recoverRoomStats();
				if(isAnyAdjusted(mobs[0])){ mob.tell(_("Error22-2")); return false;}

				reset(mobs,backups,R,IS,R2);
				HereAdjuster.setMiscText("abi+10 gen=F class=Fighter cha+10 man+1000 MASK=-RACE +Dwarf");
				mob.tell(_("Test#22-2: @x1",HereAdjuster.accountForYourself()));
				A2=((Ability)HereAdjuster.copyOf());
				A2.setMiscText((HereAdjuster).text());
				R2.addNonUninvokableEffect(A2);
				R2.recoverRoomStats();
				CMLib.tracking().walk(mobs[0],Directions.UP,false,false);
				CMLib.tracking().walk(mobs[1],Directions.UP,false,false);
				R2.recoverRoomStats();
				if(!isAllAdjusted(mobs[0])){ mob.tell(_("Error22-3")); return false;}
				if(isAnyAdjusted(mobs[1])){ mob.tell(_("Error22-4")); return false;}
				CMLib.tracking().walk(mobs[0],Directions.DOWN,false,false);
				CMLib.tracking().walk(mobs[1],Directions.DOWN,false,false);
				R2.recoverRoomStats();
				if(isAnyAdjusted(mobs[0])){ mob.tell(_("Error22-5")); return false;}
				if(isAnyAdjusted(mobs[1])){ mob.tell(_("Error22-6")); return false;}

				reset(mobs,backups,R,IS,R2);
				HereAdjuster.setMiscText("abi+10 gen=F class=Fighter cha+10 man+1000 MASK=-Human");
				mob.tell(_("Test#22-3: @x1",HereAdjuster.accountForYourself()));
				A2=((Ability)HereAdjuster.copyOf());
				A2.setMiscText((HereAdjuster).text());
				R2.addNonUninvokableEffect(A2);
				R2.recoverRoomStats();
				CMLib.tracking().walk(mobs[0],Directions.UP,false,false);
				CMLib.tracking().walk(mobs[1],Directions.UP,false,false);
				R2.recoverRoomStats();
				if(!isAllAdjusted(mobs[0])){ mob.tell(_("Error22-7")); return false;}
				if(isAnyAdjusted(mobs[1])){ mob.tell(_("Error22-8")); return false;}
				CMLib.tracking().walk(mobs[0],Directions.DOWN,false,false);
				CMLib.tracking().walk(mobs[1],Directions.DOWN,false,false);
				R2.recoverRoomStats();
				if(isAnyAdjusted(mobs[0])){ mob.tell(_("Error22-9")); return false;}
				if(isAnyAdjusted(mobs[1])){ mob.tell(_("Error22-10")); return false;}
			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_ReqAlignments"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability reqA=CMClass.getAbility("Prop_ReqAlignments");
				reqA.setMiscText("NOFOL -EVIL");
				R2.addNonUninvokableEffect(reqA);

				CMLib.factions().setAlignment(mobs[0],Faction.Align.EVIL);

				CMLib.tracking().walk(mobs[0],Directions.UP,false,false);
				if(mobs[0].location() == R2)
				{ mob.tell(_("Error23-1")); return false;}

				CMLib.factions().setAlignment(mobs[0],Faction.Align.NEUTRAL);
				CMLib.tracking().walk(mobs[0],Directions.UP,false,false);
				if(mobs[0].location() != R2)
				{ mob.tell(_("Error23-2")); return false;}

				R.bringMobHere(mobs[0],false);

				reqA.setMiscText("NOFOL -ALL +EVIL");
				CMLib.factions().setAlignment(mobs[0],Faction.Align.NEUTRAL);

				CMLib.tracking().walk(mobs[0],Directions.UP,false,false);
				if(mobs[0].location() == R2)
				{ mob.tell(_("Error23-3")); return false;}

				CMLib.factions().setAlignment(mobs[0],Faction.Align.EVIL);

				CMLib.tracking().walk(mobs[0],Directions.UP,false,false);
				if(mobs[0].location() != R2)
				{ mob.tell(_("Error23-4")); return false;}


			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_ReqCapacity"))
			||what.equalsIgnoreCase("all"))
			{
				reset(mobs,backups,R,IS,R2);
				final Ability reqA=CMClass.getAbility("Prop_ReqCapacity");
				reqA.setMiscText("people=1 weight=100 items=1");
				R2.addNonUninvokableEffect(reqA);
				IS=giveTo(CMClass.getWeapon("Sword"),null,mobs[0],mobs[1],0);

				CMLib.tracking().walk(mobs[0],Directions.UP,false,false);
				if(mobs[0].location() != R2)
				{ mob.tell(_("Error24-1")); return false;}

				CMLib.tracking().walk(mobs[1],Directions.UP,false,false);
				if(mobs[1].location() == R2)
				{ mob.tell(_("Error24-2")); return false;}


				mobs[0].moveItemTo(IS[0]);
				mobs[0].moveItemTo(IS[1]);

				msg=CMClass.getMsg(mobs[0],IS[0],null,CMMsg.MSG_DROP,null);
				if(!R2.okMessage(mobs[0], msg))
				{ mob.tell(_("Error24-3")); return false;}
				R2.send(mobs[0], msg);

				msg=CMClass.getMsg(mobs[0],IS[1],null,CMMsg.MSG_DROP,null);
				if(R2.okMessage(mobs[0], msg))
				{ mob.tell(_("Error24-4")); return false;}

			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_ReqClasses")))
			{

			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_ReqEntry")))
			{

			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_ReqHeight")))
			{

			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_ReqLevels")))
			{

			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_ReqNoMOB")))
			{

			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_ReqPKill")))
			{

			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_ReqRaces")))
			{

			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_ReqStat")))
			{

			}
			if((what.equalsIgnoreCase("all_properties"))
			||(what.equalsIgnoreCase("Prop_ReqTattoo")))
			{

			}
			if((what.equalsIgnoreCase("all")||what.equalsIgnoreCase("rtree"))&&(mob.session()!=null))
			{
				final long t1=System.currentTimeMillis();
				final RTree tree=new RTree();
				final List<BoundedObject> origSet=new Vector<BoundedObject>();
				final List<long[]> samples=new Vector<long[]>();
				final Random r=new Random(System.currentTimeMillis());
				for(int g=0;g<15;g++)
				{
					long gcenterX=r.nextLong();
					if(gcenterX<0) gcenterX=gcenterX*-1;
					long gcenterY=r.nextLong();
					if(gcenterY<0) gcenterY=gcenterY*-1;
					long gcenterZ=r.nextLong();
					if(gcenterZ<0) gcenterZ=gcenterZ*-1;
					final long grp=g;
					for(int i=0;i<100;i++)
					{
						final int dist=r.nextInt(0x0fff);
						final BoundedObject.BoundedCube cube=new BoundedObject.BoundedCube(gcenterX-dist,gcenterX+dist,gcenterY-dist,gcenterY+dist,gcenterZ-dist,gcenterZ+dist);
						final int num=i;
						final BoundedObject obj=new BoundedObject()
						{
							@Override
							public BoundedCube getBounds()
							{
								return cube;
							}
							@Override public String toString() { return "g"+grp+"#"+num; }
						};
						origSet.add(obj);
					}
					samples.add(new long[]{gcenterX,gcenterY,gcenterZ});
				}
				final List<BoundedObject> setToAdd=new Vector<BoundedObject>(origSet.size());
				final List<BoundedObject> randomSet=new Vector<BoundedObject>(origSet.size());
				setToAdd.addAll(origSet);
				while(setToAdd.size()>0)
				{
					final BoundedObject O=setToAdd.remove(r.nextInt(setToAdd.size()));
					if((tree.contains(O))||(tree.leafSearch(O)))
					{ mob.tell(_("Error25-0")); return false;}
					tree.insert(O);
					if((!tree.contains(O))||(!tree.leafSearch(O)))
					{ mob.tell(_("Error25-0.1")); return false;}
					randomSet.add(O);
				}
				long totalSize=0;
				for(int gnum=0; gnum<samples.size();gnum++)
				{
					setToAdd.clear();
					final long[] pt=samples.get(gnum);
					tree.query(setToAdd,pt[0],pt[1],pt[2]);
					totalSize+=setToAdd.size();
				}
				mob.tell(_("Average set size=@x1, time=@x2, count=@x3",""+(totalSize/samples.size()),""+((System.currentTimeMillis()-t1)),""+tree.count()));
				for(final BoundedObject O : origSet)
				{
					if((!tree.contains(O))||(!tree.leafSearch(O)))
					{ mob.tell(_("Error25-0.2")); return false;}
				}
				for(int gnum=0; gnum<samples.size();gnum++)
				{
					setToAdd.clear();
					final long[] pt=samples.get(gnum);
					tree.query(setToAdd,pt[0],pt[1],pt[2]);
					if(setToAdd.size()!=100)
					{ mob.tell(_("Error25-1")); return false;}
					for(int i=0;i<setToAdd.size();i++)
						if(!setToAdd.get(i).toString().startsWith("g"+gnum+"#"))
						{ mob.tell(_("Error25-1.1")); return false;}
				}
				for(int gnum=0; gnum<samples.size();gnum++)
				{
					setToAdd.clear();
					final long[] pt=samples.get(gnum);
					tree.query(setToAdd,pt[0],pt[1],pt[2]);
					for(int i2=0;i2<setToAdd.size();i2++)
					{
						final BoundedObject O2=setToAdd.get(i2);
						if((!tree.contains(O2))||(!tree.leafSearch(O2)))
						{ mob.tell(_("Error25-1.99#@x1/@x2/@x3",""+gnum,""+i2,""+setToAdd.size())); return false;}
					}
					for(int i2=0;i2<setToAdd.size();i2++) // remove dups
					{
						final BoundedObject O2=setToAdd.get(i2);
						for(int i3=setToAdd.size()-1;i3>i2;i3--) // remove dups
							if(setToAdd.get(i3)==O2)
							{
								setToAdd.remove(i3);
								System.out.println("Removed Dup");
							}
					}
					for(int i=0;i<setToAdd.size();i++)
					{
						final int ct=tree.count();
						final BoundedObject O=setToAdd.get(i);
						if((!tree.contains(O))&&(!tree.leafSearch(O)))
						{ mob.tell(_("Error25-2#@x1/@x2",""+gnum,""+i)); return false;}
						if(!tree.remove(O))
						{ mob.tell(_("Error25-3#@x1/@x2",""+gnum,""+i)); return false;}
						for(int i2=i+1;i2<setToAdd.size();i2++)
						{
							final BoundedObject O2=setToAdd.get(i2);
							if((!tree.contains(O2))&&(!tree.leafSearch(O2)))
							{ mob.tell(_("Error25-3.2#@x1/@x2/@x3/@x4",""+gnum,""+i,""+i2,""+setToAdd.size())); return false;}
						}
						if(tree.contains(O))
						{ mob.tell(_("Error25-4#@x1/@x2",""+gnum,""+i)); return false;}
						final List<BoundedObject> dblCheck=new Vector<BoundedObject>(setToAdd.size()-i);
						tree.query(dblCheck,pt[0],pt[1],pt[2]);
						if(dblCheck.contains(O))
						{ mob.tell(_("Error25-5#@x1/@x2",""+gnum,""+i)); return false;}
						if(tree.leafSearch(O))
						{ mob.tell(_("Error25-6#@x1/@x2",""+gnum,""+i)); return false;}
						if(tree.count()!=ct-1)
						{ mob.tell(_("Error25-7#@x1/@x2:@x3/@x4",""+gnum,""+i,""+tree.count(),""+(ct-1))); return false;}
					}
				}

			}
			if((what.equalsIgnoreCase("all"))||(what.equalsIgnoreCase("escapefilterbug")))
			{
				String str=_("@x1@x2^<CHANNEL \"TEST\"^>You TEST 'message'^</CHANNEL^>^N^.",ColorLibrary.COLOR_GREY,ColorLibrary.COLOR_BGGREEN);
				str=CMLib.coffeeFilter().fullOutFilter(mob.session(), mob, mob, null, null, str, false);
				str=CMLib.coffeeFilter().fullOutFilter(mob.session(), mob, mob, null, null, str, false);
				str=CMLib.coffeeFilter().fullOutFilter(mob.session(), mob, mob, null, null, str, false);
				mob.tell(str);
			}
			if((what.equalsIgnoreCase("all"))
			||(what.equalsIgnoreCase("clans")))
			{
				reset(mobs,backups,R,IS,R2);
				mobs[0].setPlayerStats((PlayerStats)CMClass.getCommon("DefaultPlayerStats"));
				mobs[1].setPlayerStats((PlayerStats)CMClass.getCommon("DefaultPlayerStats"));
				final Session S1=(Session)CMClass.getCommon("FakeSession");
				final Session S2=(Session)CMClass.getCommon("FakeSession");
				S1.initializeSession(null,Thread.currentThread().getThreadGroup().getName(), "MEMORY");
				S2.initializeSession(null,Thread.currentThread().getThreadGroup().getName(), "MEMORY");
				mobs[0].setSession(S1);
				mobs[1].setSession(S2);
				try
				{
					S1.getPreviousCMD().add("Y");
					S1.getPreviousCMD().add("TESTCLAN");
					S1.getPreviousCMD().add("Y");

				}
				finally
				{
					mobs[0].setSession(null);
					mobs[1].setSession(null);
					mobs[0].setPlayerStats(null);
					mobs[1].setPlayerStats(null);
				}
			}
			reset(mobs,backups,R,IS,R2);
			CMLib.map().emptyRoom(R2,null);
			R2.destroy();
			R.rawDoors()[Directions.UP]=upRoom;
			R.setRawExit(Directions.UP,upExit);
			mobs[0].destroy();
			mobs[1].destroy();
			R.recoverRoomStats();
			mob.tell(_("Test(s) passed or completed."));
		}
		else
			mob.tell(_("Test what?"));
		return false;
	}

	@Override public boolean canBeOrdered(){return false;}
	@Override public boolean securityCheck(MOB mob){return CMSecurity.isASysOp(mob);}
}
