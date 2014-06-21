package com.planet_ink.coffee_mud.MOBS;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
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
@SuppressWarnings({"unchecked","rawtypes"})
public class StdShopKeeper extends StdMOB implements ShopKeeper
{
	@Override public String ID(){return "StdShopKeeper";}
	protected CoffeeShop shop=((CoffeeShop)CMClass.getCommon("DefaultCoffeeShop")).build(this);
	protected long     whatIsSoldMask=0;
	protected int      invResetRate=0;
	protected int      invResetTickDown=0;
	protected String   budget="";
	protected long     budgetRemaining=Long.MAX_VALUE/2;
	protected long     budgetMax=Long.MAX_VALUE/2;
	protected int      budgetTickDown=2;
	protected String   devalueRate="";
	protected String[] pricingAdjustments=new String[0];

	public StdShopKeeper()
	{
		super();
		username="a shopkeeper";
		setDescription("He\\`s pleased to be of assistance.");
		setDisplayText("A shopkeeper is waiting to serve you.");
		CMLib.factions().setAlignment(this,Faction.Align.GOOD);
		setMoney(0);
		basePhyStats.setWeight(150);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,16);
		baseCharStats().setStat(CharStats.STAT_CHARISMA,25);

		basePhyStats().setArmor(0);

		baseState.setHitPoints(1000);

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

	@Override
	public boolean isSold(int mask)
	{
		if(mask==0) return whatIsSoldMask==0;
		if((whatIsSoldMask&255)==mask)
			return true;
		return CMath.bset(whatIsSoldMask>>8, CMath.pow(2,mask-1));
	}
	@Override
	public void addSoldType(int mask)
	{
		if(mask==0)
			whatIsSoldMask=0;
		else
		{
			if((whatIsSoldMask>0)&&(whatIsSoldMask<256))
				whatIsSoldMask=(CMath.pow(2,whatIsSoldMask-1)<<8);

			for(int c=0;c<ShopKeeper.DEAL_CONFLICTS.length;c++)
				for(int c1=0;c1<ShopKeeper.DEAL_CONFLICTS[c].length;c1++)
					if(ShopKeeper.DEAL_CONFLICTS[c][c1]==mask)
					{
						for(c1=0;c1<ShopKeeper.DEAL_CONFLICTS[c].length;c1++)
							if((ShopKeeper.DEAL_CONFLICTS[c][c1]!=mask)
							&&(isSold(ShopKeeper.DEAL_CONFLICTS[c][c1])))
								addSoldType(-ShopKeeper.DEAL_CONFLICTS[c][c1]);
						break;
					}

			if(mask<0)
				whatIsSoldMask=CMath.unsetb(whatIsSoldMask,(CMath.pow(2,(-mask)-1)<<8));
			else
				whatIsSoldMask|=(CMath.pow(2,mask-1)<<8);
		}
	}
	@Override public long getWhatIsSoldMask(){return whatIsSoldMask;}
	@Override public void setWhatIsSoldMask(long newSellCode){whatIsSoldMask=newSellCode;}

	@Override
	protected void cloneFix(MOB E)
	{
		super.cloneFix(E);
		if(E instanceof StdShopKeeper)
			shop=((CoffeeShop)((StdShopKeeper)E).shop.copyOf()).build(this);
	}

	@Override public CoffeeShop getShop(){return shop;}

	@Override
	public void destroy()
	{
		super.destroy();
		getShop().destroyStoreInventory();
	}
	@Override public String storeKeeperString(){ return CMLib.coffeeShops().storeKeeperString(getShop()); }
	@Override public boolean doISellThis(Environmental thisThang){ return CMLib.coffeeShops().doISellThis(thisThang,this); }
	protected Area getStartArea()
	{
		Area A=CMLib.map().getStartArea(this);
		if(A==null) CMLib.map().areaLocation(this);
		if(A==null) A=CMLib.map().areas().nextElement();
		return A;
	}
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)&&(isGeneric()))
		{
			if((--invResetTickDown)<=0)
			{
				invResetTickDown=finalInvResetRate(); // we should now be at a positive number.
				if(invResetTickDown<=0)
					invResetTickDown=Ability.TICKS_FOREVER;
				else
				{
					shop.emptyAllShelves();
					if(miscText!=null)
					{
						String shoptext;
						if(CMProps.getBoolVar(CMProps.Bool.MOBCOMPRESS) && (miscText instanceof byte[]))
							shoptext=CMLib.coffeeMaker().getGenMOBTextUnpacked(this,CMLib.encoder().decompressString((byte[])miscText));
						else
							shoptext=CMLib.coffeeMaker().getGenMOBTextUnpacked(this,CMStrings.bytesToStr(miscText));
						final List<XMLLibrary.XMLpiece> xml=CMLib.xml().parseAllXML(shoptext);
						if(xml!=null)
						{
							CMLib.coffeeMaker().populateShops(this,xml);
							recoverPhyStats();
							recoverCharStats();
						}
					}
				}
			}
			if((--budgetTickDown)<=0)
			{
				budgetTickDown=100;
				budgetRemaining=Long.MAX_VALUE/2;
				String s=finalBudget();
				final Vector<String> V=CMParms.parse(s.trim().toUpperCase());
				if(V.size()>0)
				{
					if(V.firstElement().equals("0"))
						budgetRemaining=0;
					else
					{
						budgetRemaining=CMath.s_long(V.firstElement());
						if(budgetRemaining==0)
							budgetRemaining=Long.MAX_VALUE/2;
					}
					s="DAY";
					if(V.size()>1) s=V.lastElement().toUpperCase();
					if(s.startsWith("DAY"))
						budgetTickDown=CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY);
					else
					if(location()!=null)
					{
						if(s.startsWith("HOUR"))
							budgetTickDown=CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY)/location().getArea().getTimeObj().getHoursInDay();
						else
						if(s.startsWith("WEEK"))
							budgetTickDown=location().getArea().getTimeObj().getDaysInWeek()*CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY);
						else
						if(s.startsWith("MONTH"))
							budgetTickDown=location().getArea().getTimeObj().getDaysInMonth()*CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY);
						else
						if(s.startsWith("YEAR"))
							budgetTickDown=location().getArea().getTimeObj().getDaysInMonth()*location().getArea().getTimeObj().getMonthsInYear()*CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY);
					}
				}
				budgetMax=budgetRemaining;
			}
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
			{
				if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
					return false;
				if(CMLib.coffeeShops().standardSellEvaluation(this,msg.source(),msg.tool(),this,budgetRemaining,budgetMax,msg.targetMinor()==CMMsg.TYP_SELL))
					return super.okMessage(myHost,msg);
				return false;
			}
			case CMMsg.TYP_BID:
			{
				CMLib.commands().postSay(this,msg.source(),_("I'm afraid my prices are firm."),false,false);
				return false;
			}
			case CMMsg.TYP_BUY:
			case CMMsg.TYP_VIEW:
			{
				if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
					return false;
				if((msg.targetMinor()==CMMsg.TYP_BUY)&&(msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
					return false;
				if(CMLib.coffeeShops().standardBuyEvaluation(this,msg.source(),msg.tool(),this,msg.targetMinor()==CMMsg.TYP_BUY))
					return super.okMessage(myHost,msg);
				return false;
			}
			case CMMsg.TYP_LIST:
			{
				if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
					return false;
				return super.okMessage(myHost,msg);
			}
			default:
				break;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			final MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					if((msg.tool()!=null)
					&&((CMSecurity.isAllowed(msg.source(),location(),CMSecurity.SecFlag.ORDER)
						||(CMLib.law().doesHavePriviledgesHere(msg.source(),getStartRoom()))
						||(CMSecurity.isAllowed(msg.source(),location(),CMSecurity.SecFlag.CMDMOBS)&&(isMonster()))
						||(CMSecurity.isAllowed(msg.source(),location(),CMSecurity.SecFlag.CMDROOMS)&&(isMonster()))))
					&&((doISellThis(msg.tool()))||(isSold(DEAL_INVENTORYONLY))))
					{
						CMLib.commands().postSay(this,msg.source(),_("Yes, I will now sell @x1.",msg.tool().name()),false,false);
						getShop().addStoreInventory(msg.tool(),1,-1);
						if(isGeneric()) text();
						return;
					}
				}
				super.executeMsg(myHost,msg);
				break;
			case CMMsg.TYP_VALUE:
			{
				super.executeMsg(myHost,msg);
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
					CMLib.commands().postSay(this,mob,_("I'll give you @x1 for @x2.",CMLib.beanCounter().nameCurrencyShort(this,CMLib.coffeeShops().pawningPrice(this,mob,msg.tool(),this).absoluteGoldPrice),msg.tool().name()),true,false);
				break;
			}
			case CMMsg.TYP_SELL: // sell TO -- this is a shopkeeper purchasing from a player
			{
				super.executeMsg(myHost,msg);
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					final double paid=CMLib.coffeeShops().transactPawn(this,msg.source(),this,msg.tool());
					if(paid>Double.MIN_VALUE)
					{
						budgetRemaining=budgetRemaining-Math.round(paid);
						if(mySession!=null)
							mySession.stdPrintln(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
						if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
							mob.location().recoverRoomStats();
						if(isGeneric()) text();
					}
				}
				break;
			}
			case CMMsg.TYP_VIEW:
			{
				super.executeMsg(myHost,msg);
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					if((msg.tool()!=null)&&(getShop().doIHaveThisInStock("$"+msg.tool().Name()+"$",mob)))
						CMLib.commands().postSay(this,msg.source(),CMLib.coffeeShops().getViewDescription(msg.tool()),true,false);
				}
				break;
			}
			case CMMsg.TYP_BUY: // buy-from -- this is a player buying from a shopkeeper
			{
				super.executeMsg(myHost,msg);
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					final MOB mobFor=CMLib.coffeeShops().parseBuyingFor(msg.source(),msg.targetMessage());
					if((msg.tool()!=null)
					&&(getShop().doIHaveThisInStock("$"+msg.tool().Name()+"$",mobFor))
					&&(location()!=null))
					{
						final Environmental item=getShop().getStock("$"+msg.tool().Name()+"$",mobFor);
						if(item!=null) CMLib.coffeeShops().transactMoneyOnly(this,msg.source(),this,item,!isMonster());

						final List<Environmental> products=getShop().removeSellableProduct("$"+msg.tool().Name()+"$",mobFor);
						if(products.size()==0) break;
						final Environmental product=products.get(0);

						if(product instanceof Item)
						{
							if(!CMLib.coffeeShops().purchaseItems((Item)product,products,this,mobFor))
								return;
						}
						else
						if(product instanceof MOB)
						{
							if(CMLib.coffeeShops().purchaseMOB((MOB)product,this,this,mobFor))
							{
								msg.modify(msg.source(),msg.target(),product,msg.sourceCode(),msg.sourceMessage(),msg.targetCode(),msg.targetMessage(),msg.othersCode(),msg.othersMessage());
								product.executeMsg(myHost,msg);
							}
						}
						else
						if(product instanceof Ability)
							CMLib.coffeeShops().purchaseAbility((Ability)product,this,this,mobFor);

						if(mySession!=null)
							mySession.stdPrintln(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
						if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
							mob.location().recoverRoomStats();
					}
				}
				break;
			}
			case CMMsg.TYP_LIST:
			{
				super.executeMsg(myHost,msg);
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					final String forMask=CMLib.coffeeShops().getListForMask(msg.targetMessage());
					List<Environmental> inventory=new XVector(getShop().getStoreInventory());
					inventory=CMLib.coffeeShops().addRealEstateTitles(inventory,mob,getShop(),getStartRoom());
					final int limit=CMParms.getParmInt(finalPrejudiceFactors(),"LIMIT",0);
					final String s=CMLib.coffeeShops().getListInventory(this,mob,inventory,limit,this,forMask);
					if(s.length()>0)
						mob.tell(s);
				}
				break;
			}
			default:
				super.executeMsg(myHost,msg);
				break;
			}
		}
		else
			super.executeMsg(myHost,msg);
	}

	@Override
	public String finalPrejudiceFactors()
	{
		if(prejudiceFactors().length()>0) return prejudiceFactors();
		return getStartArea().finalPrejudiceFactors();
	}
	@Override public String prejudiceFactors(){return CMStrings.bytesToStr(miscText);}
	@Override public void setPrejudiceFactors(String factors){miscText=factors;}

	@Override
	public String finalIgnoreMask()
	{
		if(ignoreMask().length()>0) return ignoreMask();
		return getStartArea().finalIgnoreMask();
	}
	@Override public String ignoreMask(){return "";}
	@Override public void setIgnoreMask(String factors){}

	@Override
	public String[] finalItemPricingAdjustments()
	{
		if((itemPricingAdjustments()!=null)&&(itemPricingAdjustments().length>0))
			return itemPricingAdjustments();
		return getStartArea().finalItemPricingAdjustments();
	}
	@Override public String[] itemPricingAdjustments(){ return pricingAdjustments;}
	@Override public void setItemPricingAdjustments(String[] factors){pricingAdjustments=factors;}

	@Override
	public String finalBudget()
	{
		if(budget().length()>0) return budget();
		return getStartArea().finalBudget();
	}
	@Override public String budget(){return budget;}
	@Override public void setBudget(String factors){budget=factors; budgetTickDown=0;}

	@Override
	public String finalDevalueRate()
	{
		if(devalueRate().length()>0) return devalueRate();
		return getStartArea().finalDevalueRate();
	}
	@Override public String devalueRate(){return devalueRate;}
	@Override public void setDevalueRate(String factors){devalueRate=factors;}

	@Override
	public int finalInvResetRate()
	{
		if(invResetRate()!=0) return invResetRate();
		return getStartArea().finalInvResetRate();
	}
	@Override public int invResetRate(){return invResetRate;}
	@Override public void setInvResetRate(int ticks){invResetRate=ticks; invResetTickDown=0;}

}
