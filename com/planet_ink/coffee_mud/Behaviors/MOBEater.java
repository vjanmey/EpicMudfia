package com.planet_ink.coffee_mud.Behaviors;
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
   Copyright 2000-2014 Mike Rundell

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
public class MOBEater extends ActiveTicker
{
	@Override public String ID(){return "MOBEater";}
	@Override protected int canImproveCode(){return Behavior.CAN_MOBS;}
	@Override public long flags() { return super.flags()|Behavior.FLAG_POTENTIALLYAUTODEATHING; }
	protected Room Stomach = null;
	protected int digestDown=4;
	protected Room lastKnownLocation=null;
	protected int chanceToEat = 5;
	protected int pctAcidHp = 50;

	@Override
	public String accountForYourself()
	{
		return "creature eating";
	}

	public MOBEater()
	{
		super();
		minTicks=4; maxTicks=8; chance=50;
		tickReset();
	}

	@Override
	public void setParms(String parms)
	{
		super.setParms(parms);
		pctAcidHp=CMParms.getParmInt(parms,"acidpct",50);
	}

	@Override
	public void startBehavior(PhysicalAgent forMe)
	{
		if(Stomach==null)
			Stomach = CMClass.getLocale("StdRoom");
		if((forMe!=null)&&(forMe instanceof MOB))
		{
			lastKnownLocation=((MOB)forMe).location();
			if(lastKnownLocation!=null)
				Stomach.setArea(lastKnownLocation.getArea());
			Stomach.setDisplayText(_("The Stomach of @x1",forMe.name()));
			Stomach.setName(_("the stomach of @x1",forMe.name()));
			Stomach.setDescription(_("You are in the stomach of @x1.  It is wet with digestive acids, and the walls are grinding you to a pulp.  You have been Swallowed whole and are being digested.",forMe.name()));
			Stomach.addNonUninvokableEffect(CMClass.getAbility("Prop_NoRecall"));
			Stomach.addNonUninvokableEffect(CMClass.getAbility("Prop_NoTeleportOut"));
		}
	}

	public void kill()
	{
		if(lastKnownLocation==null) return;
		if(Stomach==null) return;

		// ===== move all inhabitants to the dragons location
		// ===== loop through all inhabitants of the stomach
		final Vector these=new Vector();
		for (int x=0;x<Stomach.numInhabitants();x++)
		{
			// ===== get the tasty morsels
			final MOB TastyMorsel = Stomach.fetchInhabitant(x);
			if(TastyMorsel!=null)
				these.addElement(TastyMorsel);
		}

		// =====move the inventory of the stomach to the room
		for (int y=0;y<Stomach.numItems();y++)
		{
			final Item PartiallyDigestedItem = Stomach.getItem(y);
			if((PartiallyDigestedItem!=null)&&(PartiallyDigestedItem.container()==null))
				these.addElement(PartiallyDigestedItem);
		}
		for(int i=0;i<these.size();i++)
		{
			if(these.elementAt(i) instanceof Item)
				lastKnownLocation.moveItemTo((Item)these.elementAt(i),ItemPossessor.Expire.Player_Drop);
			else
			if(these.elementAt(i) instanceof MOB)
				lastKnownLocation.bringMobHere((MOB)these.elementAt(i),false);
		}
		Stomach.recoverPhyStats();
		lastKnownLocation.recoverRoomStats();
		lastKnownLocation=null;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(ticking==null) return true;
		if(!(ticking instanceof MOB)) return true;

		final MOB mob=(MOB)ticking;
		if(mob.location()!=null)
			lastKnownLocation=mob.location();

		if((--digestDown)<=0)
		{
			digestDown=4;
			digestTastyMorsels(mob);
		}

		if((canAct(ticking,tickID))
		&&(((MOB)ticking).isInCombat())
		&&(!mob.amDead()))
			trySwallowWhole(mob);
		return true;
	}
	@Override
	public void executeMsg(Environmental mob, CMMsg msg)
	{
		if((mob instanceof MOB)
		&&(msg.amISource((MOB)mob))
		&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
			kill();
		super.executeMsg(mob,msg);
	}
	protected boolean trySwallowWhole(MOB mob)
	{
		if(Stomach==null) return true;
		if (CMLib.flags().aliveAwakeMobile(mob,true)
			&&(mob.rangeToTarget()==0)
			&&(CMLib.flags().canHear(mob)||CMLib.flags().canSee(mob)||CMLib.flags().canSmell(mob)))
		{
			final MOB TastyMorsel = mob.getVictim();
			if(TastyMorsel==null) return true;
			if (TastyMorsel.baseWeight()<(mob.phyStats().weight()/2))
			{
				// ===== The player has been eaten.
				// ===== move the tasty morsel to the stomach
				final CMMsg EatMsg=CMClass.getMsg(mob,
										   TastyMorsel,
										   null,
										   CMMsg.MSG_EAT,
										   CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,
										   CMMsg.MSG_NOISYMOVEMENT,
										   _("<S-NAME> swallow(es) <T-NAMESELF> WHOLE!"));
				if(mob.location().okMessage(TastyMorsel,EatMsg))
				{
					mob.location().send(TastyMorsel,EatMsg);
					if(EatMsg.value()==0)
					{
						Stomach.bringMobHere(TastyMorsel,false);
						final CMMsg enterMsg=CMClass.getMsg(TastyMorsel,Stomach,null,CMMsg.MSG_ENTER,Stomach.description(),CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,_("<S-NAME> slide(s) down the gullet into the stomach!"));
						Stomach.send(TastyMorsel,enterMsg);
					}
				}
			}
		}
		return true;
	}

	protected boolean digestTastyMorsels(MOB mob)
	{
		if(Stomach==null) return true;
		// ===== loop through all inhabitants of the stomach
		final int morselCount = Stomach.numInhabitants();
		for (int x=0;x<morselCount;x++)
		{
			// ===== get a tasty morsel
			final MOB TastyMorsel = Stomach.fetchInhabitant(x);
			if (TastyMorsel != null)
			{
				final CMMsg DigestMsg=CMClass.getMsg(mob,
										   TastyMorsel,
										   null,
										   CMMsg.MASK_ALWAYS|CMMsg.TYP_ACID,
										   _("<S-NAME> digest(s) <T-NAMESELF>!!"));
				// no OKaffectS, since the dragon is not in his own stomach.
				Stomach.send(mob,DigestMsg);
				int damage=(int)Math.round(TastyMorsel.curState().getHitPoints() * CMath.div(pctAcidHp, 100));
				if(damage<(TastyMorsel.phyStats().level()+6)) damage=TastyMorsel.curState().getHitPoints()+1;
				if(DigestMsg.value()!=0) damage=damage/2;
				CMLib.combat().postDamage(mob,TastyMorsel,null,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_ACID,Weapon.TYPE_MELTING,"The stomach acid <DAMAGE> <T-NAME>!");
			}
		}
		return true;
	}
}
