package com.planet_ink.coffee_mud.Items.Basic;
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
public class StdContainer extends StdItem implements Container
{
	@Override public String ID(){	return "StdContainer";}
	protected boolean isLocked=false;
	protected boolean hasALock=false;
	protected boolean isOpen=true;
	protected boolean hasALid=false;
	protected int capacity=0;
	protected long containType=0;
	protected int openDelayTicks=30;

	public StdContainer()
	{
		super();
		setName("a container");
		setDisplayText("a nondescript container sits here.");
		setDescription("I`ll bet you could put stuff in it!");
		capacity=25;
		baseGoldValue=10;
		recoverPhyStats();
		material=RawMaterial.RESOURCE_COTTON;
	}

	@Override
	public int capacity()
	{
		return capacity;
	}
	@Override
	public void setCapacity(int newValue)
	{
		capacity=newValue;
	}

	@Override
	public int openDelayTicks()
	{
		return openDelayTicks;
	}
	
	@Override
	public void setOpenDelayTicks(int ticksToReset)
	{
		openDelayTicks = ticksToReset;
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(msg.amITarget(this))
		{
			final MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_INSTALL:
				if((!(this instanceof Technical))||(!(msg.tool() instanceof Technical)))
				{
					mob.tell(_("@x1 cannot be installed.",name()));
					return false;
				}
				//$FALL-THROUGH$
			case CMMsg.TYP_PUT:
				if(msg.tool() instanceof Item)
				{
					if(!CMLib.flags().isDroppable((Item)msg.tool()))
					{
						mob.tell(_("You can't seem to let go of @x1.",msg.tool().name()));
						return false;
					}
					final Item newitem=(Item)msg.tool();
					if(hasALid()&&(!isOpen()))
					{
						mob.tell(_("@x1 is closed.",name()));
						return false;
					}
					else
					if(newitem.amWearingAt(Wearable.WORN_WIELD))
					{
						mob.tell(_("You are already wielding that!"));
						return false;
					}
					else
					if(newitem.amWearingAt(Wearable.WORN_HELD))
					{
						mob.tell(_("You are holding that!"));
						return false;
					}
					else
					if(!newitem.amWearingAt(Wearable.IN_INVENTORY))
					{
						mob.tell(_("You are wearing that!"));
						return false;
					}
					else
					if(capacity<=0)
					{
						mob.tell(_("You can't put anything in @x1!",name()));
						return false;
					}
					else
					{
						if(!canContain(newitem))
						{
							mob.tell(_("You can't put @x1 in @x2.",newitem.name(),name()));
							return false;
						}
						else
						if(newitem.phyStats().weight()>capacity)
						{
							mob.tell(_("@x1 won't fit in @x2.",newitem.name(),name()));
							return false;
						}
						else
						if((recursiveWeight()+newitem.phyStats().weight())>capacity)
						{
							if(getContents().size()==0)
								mob.tell(_("@x1 is too small.",name()));
							else
							if((newitem instanceof Software) && (this instanceof Electronics.Computer))
								mob.tell(_("@x1 is out of memory.",name()));
							else
								mob.tell(_("@x1 is full.",name()));
							return false;
						}
						if((!msg.source().isMine(this))&&(msg.source().isMine(newitem)))
							if(!CMLib.commands().postDrop(msg.source(),newitem,true,true,true))
								return false;
						return true;
					}
				}
				break;
			case CMMsg.TYP_GET:
				if((msg.tool()!=null)
				&&(msg.tool() instanceof Item))
				{
					final Item newitem=(Item)msg.tool();
					if(newitem.container()==this)
					{
						if((!(CMLib.flags().canBeSeenBy(newitem,mob)||(newitem instanceof Light)))
						&&(amWearingAt(Wearable.IN_INVENTORY))
						&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS)))
						{
							mob.tell(mob,newitem,this,_("You can't see <T-NAME> in <O-NAME>."));
							return false;
						}
						else
						if(hasALid()&&(!isOpen()))
						{
							mob.tell(_("@x1 is closed.",name()));
							return false;
						}
						else
						if((mob.phyStats().level()<newitem.phyStats().level()-(10+(mob.phyStats().level()/5)))
						&&(!(mob instanceof ShopKeeper))
						&&(!mob.charStats().getMyRace().leveless())
						&&(!mob.charStats().getCurrentClass().leveless()))
						{
							mob.tell(_("@x1 is too powerful to endure possessing it.",newitem.name()));
							return false;
						}
						else
						if((newitem.recursiveWeight()>(mob.maxCarry()-mob.phyStats().weight()))&&(!mob.isMine(this)))
						{
							mob.tell(_("@x1 is too heavy.",newitem.name()));
							return false;
						}
						else
						if((newitem.numberOfItems()>(mob.maxItems()-mob.numItems()))&&(!mob.isMine(this)))
						{
							mob.tell(_("You can't carry that many items."));
							return false;
						}
						else
						if(!CMLib.flags().isGettable(newitem))
						{
							mob.tell(_("You can't get @x1.",newitem.name()));
							return false;
						}
						return true;
					}
					mob.tell(mob,newitem,this,_("You can't see <T-NAME> in <O-NAME>."));
					return false;
				}
				else
				if((recursiveWeight()>(mob.maxCarry()-mob.phyStats().weight()))&&(!mob.isMine(this)))
				{
					mob.tell(_("@x1 is too heavy.",name()));
					return false;
				}
				else
				if(((numberOfItems()>mob.maxItems()-mob.numItems()))&&(!mob.isMine(this)))
				{
					mob.tell(_("You can't carry that many items."));
					return false;
				}
				break;
			case CMMsg.TYP_REMOVE:
				if(msg.tool() instanceof Item)
				{
					final Item newitem=(Item)msg.tool();
					if(newitem.container()==this)
					{
						if((!CMLib.flags().canBeSeenBy(newitem,mob))
						&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS)))
						{
							mob.tell(_("You can't see that."));
							return false;
						}
						else
						if(hasALid()&&(!isOpen()))
						{
							mob.tell(_("@x1 is closed.",name()));
							return false;
						}
						else
							return true;
					}
					mob.tell(_("You don't see that here."));
					return false;
				}
				break;
			case CMMsg.TYP_CLOSE:
				if(isOpen)
				{
					if(!hasALid)
					{
						mob.tell(_("There is nothing to close on @x1.",name()));
						return false;
					}
					return true;
				}
				mob.tell(_("@x1 is already closed.",name()));
				return false;
			case CMMsg.TYP_OPEN:
				if(!hasALid)
				{
					mob.tell(_("There is nothing to open on @x1.",name()));
					return false;
				}
				if(isOpen)
				{
					mob.tell(_("@x1 is already open!",name()));
					return false;
				}
				if(isLocked)
				{
					mob.tell(_("@x1 is locked.",name()));
					return false;
				}
				return true;
			case CMMsg.TYP_LOCK:
			case CMMsg.TYP_UNLOCK:
				if(!hasALid)
				{
					mob.tell(_("There is nothing to lock or unlock on @x1.",name()));
					return false;
				}
				if(isOpen)
				{
					mob.tell(_("@x1 is open!",name()));
					return false;
				}
				else
				if(!hasALock)
				{
					mob.tell(_("There is no lock!"));
					return false;
				}
				else
				{
					if((!isLocked)&&(msg.targetMinor()==CMMsg.TYP_UNLOCK))
					{
						mob.tell(_("@x1 is not locked.",name()));
						return false;
					}
					else
					if((isLocked)&&(msg.targetMinor()==CMMsg.TYP_LOCK))
					{
						mob.tell(_("@x1 is already locked.",name()));
						return false;
					}
					else
					{
						for(int i=0;i<mob.numItems();i++)
						{
							final Item item=mob.getItem(i);
							if((item!=null)
							&&(item instanceof DoorKey)
							&&((DoorKey)item).getKey().equals(keyName())
							&&((item.container()==null)
							   ||((item.container().container()==null)
								  &&((item.container().containTypes()&Container.CONTAIN_KEYS)>0)))
							&&(CMLib.flags().canBeSeenBy(item,mob)))
								return true;
						}
						mob.tell(_("You don't have the key."));
						return false;
					}
				}
				//break;
				default:
					break;
			}
		}
		return true;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_EXIT_REOPEN)
		{
			setLidsNLocks(hasALid,!hasALid,hasALock,hasALock);
			return false;
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			final MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_PUT:
				if((msg.tool()!=null)
				&&(msg.tool() instanceof Item))
				{
					final Item newitem=(Item)msg.tool();
					newitem.setContainer(this);
					if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
					{
						mob.location().recoverRoomStats();
						newitem.recoverPhyStats();
					}
				}
				break;
			case CMMsg.TYP_CLOSE:
				if((!hasALid)||(!isOpen)) return;
				isOpen=false;
				break;
			case CMMsg.TYP_OPEN:
				if((!hasALid)||(isOpen)||(isLocked)) return;
				if((owner() instanceof Room)
				&&(!CMLib.flags().isGettable(this))
				&&(!CMLib.threads().isTicking(this,Tickable.TICKID_EXIT_REOPEN))
				&&(openDelayTicks>0))
					CMLib.threads().startTickDown(this,Tickable.TICKID_EXIT_REOPEN,openDelayTicks);
				isLocked=false;
				isOpen=true;
				break;
			case CMMsg.TYP_LOCK:
				if((!hasALid)||(!hasALock)||(isLocked)) return;
				isOpen=false;
				isLocked=true;
				break;
			case CMMsg.TYP_UNLOCK:
				if((!hasALid)||(!hasALock)||(isOpen)||(!isLocked))
					return;
				if((owner() instanceof Room)
				&&(!CMLib.flags().isGettable(this))
				&&(!CMLib.threads().isTicking(this,Tickable.TICKID_EXIT_REOPEN))
				&&(openDelayTicks>0))
					CMLib.threads().startTickDown(this,Tickable.TICKID_EXIT_REOPEN,openDelayTicks);
				isLocked=false;
				break;
			default:
				break;
			}
		}
		else
		if((msg.tool()==this)
		&&(msg.sourceMinor()==CMMsg.TYP_THROW)
		&&(msg.source()!=null)
		&&(msg.source().isMine(this)))
		{
			setContainer(null);
			final Room R=CMLib.map().roomLocation(msg.target());
			if(R!=null)
			{
				CMLib.utensils().recursiveDropMOB(msg.source(),R,this,this instanceof DeadBody);
				if(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_OPTIMIZE))
				{
					msg.source().location().recoverRoomStats();
					if(msg.source().location()!=R)
						R.recoverRoomStats();
				}
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override public long containTypes(){return containType;}

	@Override public void setContainTypes(long containTypes){containType=containTypes;}

	@Override
	public boolean canContain(Environmental E)
	{
		if (!(E instanceof Item)) return false;
		if(containType==0) return true;
		for(int i=0;i<Container.CONTAIN_DESCS.length;i++)
			if(CMath.isSet((int)containType,i))
				switch((int)CMath.pow(2,i))
				{
				case CONTAIN_LIQUID:
					if((((Item)E).material()&RawMaterial.MATERIAL_LIQUID)>0)
						return true;
					break;
				case CONTAIN_COINS:
					if(E instanceof Coins)
						return true;
					break;
				case CONTAIN_SWORDS:
					if((E instanceof Weapon)
					&&(((Weapon)E).weaponClassification()==Weapon.CLASS_SWORD))
						return true;
					break;
				case CONTAIN_DAGGERS:
					if((E instanceof Weapon)
					&&(((Weapon)E).weaponClassification()==Weapon.CLASS_DAGGER))
						return true;
					break;
				case CONTAIN_KEYS:
					if(E instanceof DoorKey)
						return true;
					break;
				case CONTAIN_DRINKABLES:
					if((E instanceof Drink)&&(E instanceof Item))
						return true;
					break;
				case CONTAIN_CLOTHES:
					if((E instanceof Armor)
					&&(((Armor)E).fitsOn(Wearable.WORN_ABOUT_BODY)
					   ||((Armor)E).fitsOn(Wearable.WORN_ARMS)
					   ||((Armor)E).fitsOn(Wearable.WORN_LEGS)
					   ||((Armor)E).fitsOn(Wearable.WORN_HEAD)
					   ||((Armor)E).fitsOn(Wearable.WORN_TORSO)
					   ||((Armor)E).fitsOn(Wearable.WORN_WAIST)))
						return true;
					break;
				case CONTAIN_FOOTWEAR:
					if((E instanceof Armor)
					&&(((Armor)E).fitsOn(Wearable.WORN_FEET)))
						return true;
					break;
				case CONTAIN_RAWMATERIALS:
					return (E instanceof RawMaterial);
				case CONTAIN_OTHERWEAPONS:
					if((E instanceof Weapon)
					&&(((Weapon)E).weaponClassification()!=Weapon.CLASS_SWORD)
					&&(((Weapon)E).weaponClassification()!=Weapon.CLASS_DAGGER))
						return true;
					break;
				case CONTAIN_ONEHANDWEAPONS:
					if((E instanceof Weapon)
					&&(((Weapon)E).rawLogicalAnd()==false))
						return true;
					break;
				case CONTAIN_BODIES:
					if(E instanceof DeadBody)
						return true;
					break;
				case CONTAIN_SMOKEABLES:
					if(E instanceof Item)
					{
						if((((Item)E).material()==RawMaterial.RESOURCE_PIPEWEED)
						||(((Item)E).material()==RawMaterial.RESOURCE_HERBS))
							return true;
					}
					break;
				case CONTAIN_CAGED:
					if(E instanceof CagedAnimal)
						return true;
					break;
				case CONTAIN_READABLES:
					if((E instanceof Item)
					&&(((Item)E).isReadable()))
						return true;
					break;
				case CONTAIN_SCROLLS:
					if(E instanceof Scroll)
						return true;
					break;
				case CONTAIN_SSCOMPONENTS:
					if(E instanceof ShipComponent)
						return true;
					break;
				}
		return false;
	}

	@Override public boolean isLocked(){return isLocked;}
	@Override public boolean hasALock(){return hasALock;}
	@Override public boolean isOpen(){return isOpen;}
	@Override public boolean hasALid(){return hasALid;}

	@Override
	public void setLidsNLocks(boolean newHasALid, boolean newIsOpen, boolean newHasALock, boolean newIsLocked)
	{
		hasALid=newHasALid;
		isOpen=newIsOpen;
		hasALock=newHasALock;
		isLocked=newIsLocked;
	}

	@Override
	public void setMiscText(String newMiscText)
	{
		miscText=newMiscText;
		if(!isGeneric()) setKeyName(miscText);
	}
	@Override
	public String keyName()
	{
		return miscText;
	}
	@Override
	public void setKeyName(String newKeyName)
	{
		miscText=newKeyName;
	}
	@Override
	public void emptyPlease(boolean flatten)
	{
		final ItemPossessor C=owner();
		if(C!=null)
		{
			Item I;
			if(flatten)
			{
				final List<Item> V=getContents();
				for(int v=0;v<V.size();v++)
				{
					I=V.get(v);
					I.setContainer(null);
				}
			}
			else
			for(final Enumeration<Item> e = C.items(); e.hasMoreElements();)
			{
				I=e.nextElement();
				if(I==null) continue;
				if(I.container()==this)
					I.setContainer(null);
			}
		}
	}
	@Override
	public boolean isInside(Item I)
	{
		if(I.container()==null) return false;
		if(I.container()==this) return true;
		if(I.container()==I) return false;
		return isInside(I.container());
	}

	@Override
	public int numberOfItems()
	{
		return getContents().size()+1;
	}

	@Override
	public int recursiveWeight()
	{
		int weight=phyStats().weight();
		if(owner()==null) return weight;
		if(owner() instanceof MOB)
		{
			final MOB M=(MOB)owner();
			for(int i=0;i<M.numItems();i++)
			{
				final Item thisItem=M.getItem(i);
				if((thisItem!=null)&&(thisItem!=this)&&(thisItem.ultimateContainer(this)==this))
					weight+=thisItem.phyStats().weight();
			}
		}
		else
		if(owner() instanceof Room)
		{
			final Room R=(Room)owner();
			for(int i=0;i<R.numItems();i++)
			{
				final Item thisItem=R.getItem(i);
				if((thisItem!=null)&&(thisItem!=this)&&(thisItem.ultimateContainer(this)==this))
					weight+=thisItem.phyStats().weight();
			}
		}
		return weight;
	}

	@Override
	public ReadOnlyList<Item> getContents()
	{
		final List<Item> V=new Vector<Item>();
		if(owner()!=null)
		{
			Item I;
			for(final Enumeration<Item> e = owner().items(); e.hasMoreElements();)
			{
				I=e.nextElement();
				if(I==null) continue;
				if(isInside(I))
					V.add(I);
			}
		}
		return new ReadOnlyList<Item>(V);
	}
}
