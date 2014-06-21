package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.ForumJournal;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
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
public class CMJournals extends StdLibrary implements JournalsLibrary
{
	@Override public String ID(){return "CMJournals";}
	public final int QUEUE_SIZE=100;
	protected final SHashtable<String,CommandJournal>	 commandJournals	= new SHashtable<String,CommandJournal>();
	protected final SHashtable<String,ForumJournal>	 	 forumJournals		= new SHashtable<String,ForumJournal>();
	protected final SHashtable<String,List<ForumJournal>>clanForums			= new SHashtable<String,List<ForumJournal>>();

	protected final static List<ForumJournal> emptyForums = new ReadOnlyVector<ForumJournal>(0);

	@SuppressWarnings("unchecked")
	protected Hashtable<String,JournalSummaryStats> getSummaryStats()
	{
		Hashtable<String,JournalSummaryStats> journalSummaryStats;
		journalSummaryStats= (Hashtable<String,JournalSummaryStats>)Resources.getResource("FORUM_JOURNAL_STATS");
		if(journalSummaryStats == null)
		{
			synchronized("FORUM_JOURNAL_STATS".intern())
			{
				journalSummaryStats= (Hashtable<String,JournalSummaryStats>)Resources.getResource("FORUM_JOURNAL_STATS");
				if(journalSummaryStats==null)
				{
					journalSummaryStats=new Hashtable<String,JournalSummaryStats>();
					Resources.submitResource("FORUM_JOURNAL_STATS", journalSummaryStats);
				}
			}
		}
		return journalSummaryStats;
	}

	@Override
	public JournalSummaryStats getJournalStats(ForumJournal journal)
	{
		if(journal == null)
			return null;
		final Hashtable<String,JournalSummaryStats> journalSummaryStats=getSummaryStats();
		JournalSummaryStats stats = journalSummaryStats.get(journal.NAME().toUpperCase().trim());
		if(stats == null)
		{
			synchronized(journal.NAME().intern())
			{
				stats = journalSummaryStats.get(journal.NAME().toUpperCase().trim());
				if(stats == null)
				{
					stats = new JournalSummaryStats();
					stats.name = journal.NAME();
					CMLib.database().DBReadJournalSummaryStats(stats);
					journalSummaryStats.put(journal.NAME().toUpperCase().trim(), stats);
				}
			}
		}
		return stats;
	}

	@Override
	public void clearJournalSummaryStats(ForumJournal journal)
	{
		if(journal == null)
			return;
		final Hashtable<String,JournalSummaryStats> journalSummaryStats=getSummaryStats();
		synchronized(journal.NAME().intern())
		{
			journalSummaryStats.remove(journal.NAME().toUpperCase().trim());
		}
	}

	@Override
	public int loadCommandJournals(String list)
	{
		clearCommandJournals();
		while(list.length()>0)
		{
			int x=list.indexOf(',');

			String item=null;
			if(x<0)
			{
				item=list.trim();
				list="";
			}
			else
			{
				item=list.substring(0,x).trim();
				list=list.substring(x+1);
			}
			x=item.indexOf(' ');
			final Hashtable<CommandJournalFlags,String> flags=new Hashtable<CommandJournalFlags,String>();
			String mask="";
			if(x>0)
			{
				mask=item.substring(x+1).trim();
				for(int pf=0;pf<CommandJournalFlags.values().length;pf++)
				{
					final String flag = CommandJournalFlags.values()[pf].toString();
					final int keyx=mask.toUpperCase().indexOf(flag);
					if(keyx>=0)
					{
						int keyy=mask.indexOf(' ',keyx+1);
						if(keyy<0) keyy=mask.length();
						if((keyx==0)||(Character.isWhitespace(mask.charAt(keyx-1))))
						{
							String parm=mask.substring(keyx+flag.length(),keyy).trim();
							if((parm.length()==0)||(parm.startsWith("=")))
							{
								if(parm.startsWith("=")) parm=parm.substring(1);
								flags.put(CommandJournalFlags.values()[pf],parm);
								mask=mask.substring(0,keyx).trim()+" "+mask.substring(keyy).trim();
							}
						}
					}
				}
				item=item.substring(0,x);
			}
			CMSecurity.registerJournal(item.toUpperCase().trim());
			commandJournals.put(item.toUpperCase().trim(),new CommandJournal(item.toUpperCase().trim(),mask,flags));
		}
		return commandJournals.size();
	}

	@Override
	public boolean canReadMessage(JournalEntry entry, String srchMatch, MOB readerM, boolean ignorePrivileges)
	{
		if(entry==null)
			return false;
		final String to=entry.to;
		if((srchMatch!=null)
		&&(srchMatch.length()>0)
		&&((to.toLowerCase().indexOf(srchMatch)<0)
		&&(entry.from.toLowerCase().indexOf(srchMatch)<0)
		&&(entry.subj.toLowerCase().indexOf(srchMatch)<0)
		&&(entry.msg.toLowerCase().indexOf(srchMatch)<0)))
			return false;
		boolean priviledged=false;
		if(readerM!=null)
			priviledged=CMSecurity.isAllowedAnywhere(readerM,CMSecurity.SecFlag.JOURNALS)&&(!ignorePrivileges);
		if(to.equalsIgnoreCase("all")
		||((readerM!=null)
			&&(priviledged
				||to.equalsIgnoreCase(readerM.Name())
				||(to.toUpperCase().trim().startsWith("MASK=")&&(CMLib.masking().maskCheck(to.trim().substring(5),readerM,true))))))
			return true;
		return false;
	}

	@Override
	public int loadForumJournals(String list)
	{
		clearForumJournals();
		final List<ForumJournal> journals = parseForumJournals(list);
		for(final ForumJournal F : journals)
		{
			forumJournals.put(F.NAME().toUpperCase().trim(), F);
			CMSecurity.registerJournal(F.NAME().toUpperCase().trim());
		}
		return forumJournals.size();
	}

	@Override
	public List<ForumJournal> getClanForums(Clan clan)
	{
		if(clan == null)
			return null;
		return this.clanForums.get(clan.clanID());
	}
	@Override
	public void registerClanForum(Clan clan, String allClanForumDefs)
	{
		if(clan==null)
			return;
		this.clanForums.remove(clan.clanID());
		if(allClanForumDefs==null)
			return;
		final List<String> set=CMParms.parseCommas(allClanForumDefs,true);
		final StringBuilder myForumList=new StringBuilder("");
		for(String s : set)
		{
			s=s.trim();
			if(s.startsWith("["))
			{
				final int x=s.indexOf(']');
				final String cat=s.substring(1,x).trim();
				if(clan.getGovernment().getCategory().equalsIgnoreCase(cat))
				{
					s=s.substring(x+1).trim();
					s=CMStrings.replaceAll(s, "<CLANTYPE>", clan.getGovernmentName());
					s=CMStrings.replaceAll(s, "<CLANNAME>", clan.getName());
					s=CMStrings.replaceAll(s, ",", ".");
					if(myForumList.length()>0)
						myForumList.append(',');
					myForumList.append(s);
				}
			}
		}
		final List<ForumJournal> journals = parseForumJournals(myForumList.toString());
		if((journals!=null)&&(journals.size()>0))
			this.clanForums.put(clan.clanID(), journals);
	}

	public List<ForumJournal> parseForumJournals(String list)
	{
		final List<ForumJournal> journals = new Vector<ForumJournal>(1);
		while(list.length()>0)
		{
			int x=list.indexOf(',');
			String item=null;
			if(x<0)
			{
				item=list.trim();
				list="";
			}
			else
			{
				item=list.substring(0,x).trim();
				list=list.substring(x+1);
			}
			final Hashtable<ForumJournalFlags,String> flags=new Hashtable<ForumJournalFlags,String>();
			x=item.indexOf('=');
			if(x > 0)
			{
				int y=x;
				while((y>0)&&(!Character.isWhitespace(item.charAt(y))))
					y--;
				final String rest = item.toUpperCase().substring(y+1).trim();
				item=item.substring(0,y);
				final Vector<Integer> flagDexes = new Vector<Integer>();
				x=rest.indexOf('=');
				while(x > 0)
				{
					y=x;
					while((y>0)&&(!Character.isWhitespace(rest.charAt(y))))
						y--;
					if(y>0)
					{
						try
						{
							ForumJournalFlags.valueOf(rest.substring(y,x).toUpperCase().trim());
							flagDexes.addElement(Integer.valueOf(y));
						}
						catch(final Exception e){}
					}
					x=rest.indexOf('=',x+1);
				}
				flagDexes.addElement(Integer.valueOf(rest.length()));
				int lastStart=0;
				for(final Integer flagDex : flagDexes)
				{
					final String piece = rest.substring(lastStart,flagDex.intValue());
					lastStart=flagDex.intValue();
					x=piece.indexOf('=');
					try
					{
						final ForumJournalFlags flagVar = ForumJournalFlags.valueOf(piece.substring(0,x).toUpperCase().trim());
						final String flagVal = piece.substring(x+1);
						flags.put(flagVar, flagVal);
					}
					catch(final Exception e){}
				}
			}
			journals.add(new ForumJournal(item.trim(),flags));
		}
		return journals;
	}

	@Override
	@SuppressWarnings("unchecked")
	public HashSet<String> getArchonJournalNames()
	{
		HashSet<String> H = (HashSet<String>)Resources.getResource("ARCHON_ONLY_JOURNALS");
		if(H == null)
		{
			Item I=null;
			H=new HashSet<String>();
			for(final Enumeration<Item> e=CMClass.basicItems();e.hasMoreElements();)
			{
				I=e.nextElement();
				if((I instanceof ArchonOnly)
				&&(!I.isGeneric()))
					H.add(I.Name().toUpperCase().trim());
			}
			Resources.submitResource("ARCHON_ONLY_JOURNALS", H);
		}
		return H;
	}

	@Override
	public boolean isArchonJournalName(String journal)
	{
		if(getArchonJournalNames().contains(journal.toUpperCase().trim()))
			return true;
		return false;
	}

	@Override
	public String getScriptValue(MOB mob, String journal, String oldValue)
	{
		final CommandJournal CMJ=getCommandJournal(journal);
		if(CMJ==null) return oldValue;
		final String scriptFilename=CMJ.getScriptFilename();
		if((scriptFilename==null)||(scriptFilename.trim().length()==0)) return oldValue;
		final ScriptingEngine S=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
		S.setSavable(false);
		S.setVarScope("*");
		S.setScript("LOAD="+scriptFilename);
		S.setVar(mob.Name(),"VALUE", oldValue);
		final CMMsg msg2=CMClass.getMsg(mob,mob,null,CMMsg.MSG_OK_VISUAL,null,null,_("COMMANDJOURNAL_@x1",CMJ.NAME()));
		S.executeMsg(mob, msg2);
		S.dequeResponses();
		S.tick(mob,Tickable.TICKID_MOB);
		final String response=S.getVar("*","VALUE");
		if(response!=null) return response;
		return oldValue;
	}

	@Override public int getNumCommandJournals() { return commandJournals.size();    }

	@Override public Enumeration<CommandJournal> commandJournals(){ return commandJournals.elements();}

	@Override public CommandJournal getCommandJournal(String named) { return commandJournals.get(named.toUpperCase().trim());}

	public void expirationJournalSweep()
	{
		setThreadStatus(serviceClient,"expiration journal sweeping");
		try
		{
			for(final Enumeration<CommandJournal> e=commandJournals();e.hasMoreElements();)
			{
				final CommandJournal CMJ=e.nextElement();
				final String num=CMJ.getFlag(CommandJournalFlags.EXPIRE);
				if((num!=null)&&(CMath.isNumber(num))&&(CMath.s_double(num)>0.0))
				{
					setThreadStatus(serviceClient,"updating journal "+CMJ.NAME());
					final List<JournalsLibrary.JournalEntry> items=CMLib.database().DBReadJournalMsgs(CMJ.JOURNAL_NAME());
					if(items!=null)
					for(int i=items.size()-1;i>=0;i--)
					{
						final JournalEntry entry=items.get(i);
						long compdate=entry.update;
						compdate=compdate+Math.round(CMath.mul(TimeManager.MILI_DAY,CMath.s_double(num)));
						if(System.currentTimeMillis()>compdate)
						{
							final String from=entry.from;
							final String message=entry.msg;
							Log.sysOut(Thread.currentThread().getName(),"Expired "+CMJ.NAME()+" from "+from+": "+message);
							CMLib.database().DBDeleteJournal(CMJ.JOURNAL_NAME(),entry.key);
						}
					}
					setThreadStatus(serviceClient,"command journal sweeping");
				}
			}
		}catch(final NoSuchElementException nse){}
		try
		{
			for(final Enumeration<ForumJournal> e=forumJournals();e.hasMoreElements();)
			{
				final ForumJournal FMJ=e.nextElement();
				final String num=FMJ.getFlag(CommandJournalFlags.EXPIRE);
				if((num!=null)&&(CMath.isNumber(num))&&(CMath.s_double(num)>0.0))
				{
					setThreadStatus(serviceClient,"updating journal "+FMJ.NAME());
					final List<JournalsLibrary.JournalEntry> items=CMLib.database().DBReadJournalMsgs(FMJ.NAME());
					if(items!=null)
					for(int i=items.size()-1;i>=0;i--)
					{
						final JournalEntry entry=items.get(i);
						if(!CMath.bset(entry.attributes, JournalEntry.ATTRIBUTE_PROTECTED))
						{
							long compdate=entry.update;
							compdate=compdate+Math.round(CMath.mul(TimeManager.MILI_DAY,CMath.s_double(num)));
							if(System.currentTimeMillis()>compdate)
							{
								final String from=entry.from;
								final String message=entry.msg;
								Log.debugOut(Thread.currentThread().getName(),"Expired "+FMJ.NAME()+" from "+from+": "+message);
								CMLib.database().DBDeleteJournal(FMJ.NAME(),entry.key);
							}
						}
					}
					setThreadStatus(serviceClient,"forum journal sweeping");
				}
			}
		}catch(final NoSuchElementException nse){}
	}

	@Override
	public boolean activate()
	{
		if(serviceClient==null)
		{
			name="THJournals"+Thread.currentThread().getThreadGroup().getName().charAt(0);
			serviceClient=CMLib.threads().startTickDown(this, Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK, MudHost.TIME_SAVETHREAD_SLEEP, 1);
		}
		return true;
	}

	@Override public boolean tick(Tickable ticking, int tickID)
	{
		tickStatus=Tickable.STATUS_ALIVE;
		try
		{
			if((!CMSecurity.isDisabled(CMSecurity.DisFlag.SAVETHREAD))
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.JOURNALTHREAD)))
			{
				isDebugging=CMSecurity.isDebugging(DbgFlag.JOURNALTHREAD);
				tickStatus=Tickable.STATUS_ALIVE;
				expirationJournalSweep();
				setThreadStatus(serviceClient,"sleeping");
			}
		}
		finally
		{
			tickStatus=Tickable.STATUS_NOT;
		}
		return true;
	}

	private void clearCommandJournals()
	{
		commandJournals.clear();
	}

	@Override public int getNumForumJournals() { return forumJournals.size();    }

	@Override public Enumeration<ForumJournal> forumJournals(){ return forumJournals.elements();}

	@Override
	public ForumJournal getForumJournal(String named)
	{
		return forumJournals.get(named.toUpperCase().trim());
	}

	@Override
	public ForumJournal getForumJournal(String named, Clan clan)
	{
		if(named==null)
			return null;

		named=named.toUpperCase().trim();
		if(forumJournals.containsKey(named))
			return forumJournals.get(named);

		if(clan!=null)
		{
			final List<ForumJournal> clanJournals=this.clanForums.get(clan.clanID());
			if(clanJournals!=null)
			{
				for (final ForumJournal CJ : clanJournals)
				{
					if(CJ.NAME().equalsIgnoreCase(named))
						return CJ;
				}
			}
		}
		return null;
	}

	private void clearForumJournals()
	{
		forumJournals.clear();
		Resources.removeResource("FORUM_JOURNAL_STATS");
	}

	@Override
	public boolean shutdown()
	{
		clearCommandJournals();
		clearForumJournals();
		if(CMLib.threads().isTicking(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK))
		{
			CMLib.threads().deleteTick(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK);
			serviceClient=null;
		}
		return true;
	}

	@Override
	public MsgMkrResolution makeMessage(final MOB mob, final String messageTitle, final List<String> vbuf, boolean autoAdd) throws IOException
	{
		final Session sess=mob.session();
		if((sess == null )||(sess.isStopped()))
			return MsgMkrResolution.CANCELFILE;
		final boolean canExtEdit=((mob.session()!=null)&&(mob.session().getClientTelnetMode(Session.TELNET_GMCP)));
		final String help=
			"^HCoffeeMud Message Maker Options:^N\n\r"+
			"^XA)^.^Wdd new lines (go into ADD mode)\n\r"+
			"^XD)^.^Welete one or more lines\n\r"+
			"^XL)^.^Wist the entire text file\n\r"+
			"^XI)^.^Wnsert a line\n\r"+
			"^XE)^.^Wdit a line\n\r"+
			"^XR)^.^Weplace text in the file\n\r"+
			"^XS)^.^Wave the file\n\r"+
			(canExtEdit?"^XW)^.^Write over using GMCP\n\r":"")+
			"^XQ)^.^Wuit without saving";

		final String addModeMessage="^ZYou are now in Add Text mode.\n\r^ZEnter . on a blank line to exit.^.^N";
		mob.tell(_("^HCoffeeMud Message Maker^N"));
		boolean menuMode=!autoAdd;
		if(autoAdd) sess.println(addModeMessage);
		while((mob.session()!=null)&&(!sess.isStopped()))
		{
			sess.setAfkFlag(false);
			if(!menuMode)
			{
				final String line=sess.prompt("^X"+CMStrings.padRight(""+vbuf.size(),3)+")^.^N ","");
				if(line.trim().equals("."))
					menuMode=true;
				else
					vbuf.add(line);
			}
			else
			{
				final LinkedList<String> paramsOut=new LinkedList<String>();
				final String option=sess.choose(_("^HMenu ^N(?/A/D/L/I/E/R/S/Q@x1)^H: ^N",(canExtEdit?"/W":"")),_("ADLIERSQ?@x1",(canExtEdit?"W":"")),"?",-1,paramsOut);
				final String paramAll=(paramsOut.size()>0)?CMParms.combine(paramsOut,0):null;
				final String param1=(paramsOut.size()>0)?paramsOut.getFirst():null;
				final String param2=(paramsOut.size()>1)?CMParms.combine(paramsOut,1):null;
				switch(option.charAt(0))
				{
				case 'S':
					if(((paramAll!=null)&&(paramAll.equalsIgnoreCase("Y")))
					||(sess.confirm(_("Save and exit, are you sure (N/y)? "),_("N"))))
					{
						return MsgMkrResolution.SAVEFILE;
					}
					break;
				case 'Q':
					if(((paramAll!=null)&&(paramAll.equalsIgnoreCase("Y")))
					||(sess.confirm(_("Quit without saving (N/y)? "),_("N"))))
						return MsgMkrResolution.CANCELFILE;
					break;
				case 'R':
				{
					if(vbuf.size()==0)
						mob.tell(_("The file is empty!"));
					else
					{
						String line=param1;
						if(line==null)
							line=sess.prompt(_("Text to search for (case sensitive): "),"");
						if(line.length()>0)
						{
							String str=param2;
							if(str==null)
								str=sess.prompt(_("Text to replace it with: "),"");
							for(int i=0;i<vbuf.size();i++)
								vbuf.set(i,CMStrings.replaceAll(vbuf.get(i),line,str));
						}
						else
							mob.tell(_("(aborted)"));
					}
					break;
				}
				case 'E':
				{
					if(vbuf.size()==0)
						mob.tell(_("The file is empty!"));
					else
					{
						String line=param1;
						if(line==null)
							line=sess.prompt(_("Line to edit (0-@x1): ",""+(vbuf.size()-1)),"");
						if((CMath.isInteger(line))&&(CMath.s_int(line)>=0)&&(CMath.s_int(line)<(vbuf.size())))
						{
							final int ln=CMath.s_int(line);
							mob.tell(_("Current: \n\r@x1) @x2",CMStrings.padRight(""+ln,3),vbuf.get(ln)));
							String str=param2;
							if(str==null)
								str=sess.prompt(_("Rewrite: \n\r"));
							if(str.length()==0)
								mob.tell(_("(no change)"));
							else
								vbuf.set(ln,str);
						}
						else
							mob.tell(_("'@x1' is not a valid line number.",line));
					}
					break;
				}
				case 'D':
				{
					if(vbuf.size()==0)
						mob.tell(_("The file is empty!"));
					else
					{
						String line=paramAll;
						if(line==null)
							line=sess.prompt(_("Line to delete (0-@x1): ",""+(vbuf.size()-1)),"");
						if((CMath.isInteger(line))&&(CMath.s_int(line)>=0)&&(CMath.s_int(line)<(vbuf.size())))
						{
							final int ln=CMath.s_int(line);
							vbuf.remove(ln);
							mob.tell(_("Line @x1 deleted.",""+ln));
						}
						else
							mob.tell(_("'@x1' is not a valid line number.",""+line));
					}
					break;
				}
				case '?': mob.tell(help); break;
				case 'A': mob.tell(addModeMessage);
						  menuMode=false;
						  break;
				case 'W':
				{
					if(mob.session()!=null)
					{
						StringBuilder oldDoc=new StringBuilder();
						for(final String s : vbuf)
							oldDoc.append(s).append("\n");
						vbuf.clear();
						mob.session().sendGMCPEvent("IRE.Composer.Edit", "{\"title\":\""+MiniJSON.toJSONString(messageTitle)+"\",\"text\":\""+MiniJSON.toJSONString(oldDoc.toString())+"\"}");
						oldDoc=null;
						final String newText=mob.session().prompt(_("Re-Enter the whole doc using your GMCP editor.\n\rIf the editor has not popped up, just hit enter and QUIT Without Saving immediately.\n\rProceed: "));
						final String[] newDoc=newText.split("\\\\n");
						for(final String s : newDoc)
							vbuf.add(s);
						if(newDoc.length>1)
						{
							mob.tell(_("\n\r^HNew text successfully imported.^N"));
						}
					}
					break;
				}
				case 'L':
				{
					final StringBuffer list=new StringBuffer(messageTitle+"\n\r");
					for(int v=0;v<vbuf.size();v++)
						list.append(CMLib.coffeeFilter().colorOnlyFilter("^X"+CMStrings.padRight(""+v,3)+")^.^N ",sess)+vbuf.get(v)+"\n\r");
					sess.rawPrint(list.toString());
					break;
				}
				case 'I':
				{
					if(vbuf.size()==0)
						mob.tell(_("The file is empty!"));
					else
					{
						String line=param1;
						if(line==null)
							line=sess.prompt(_("Line to insert before (0-@x1): ",""+(vbuf.size()-1)),"");
						if((CMath.isInteger(line))&&(CMath.s_int(line)>=0)&&(CMath.s_int(line)<(vbuf.size())))
						{
							final int ln=CMath.s_int(line);
							String str=param2;
							if(str==null)
								str=sess.prompt(_("Enter text to insert here.\n\r: "));
							vbuf.add(ln,str);
						}
						else
							mob.tell(_("'@x1' is not a valid line number.",""+line));
					}
					break;
				}
				}
			}

		}
		return MsgMkrResolution.CANCELFILE;
	}

	@Override
	public boolean subscribeToJournal(String journalName, String userName, boolean saveMailingList)
	{
		boolean updateMailingLists=false;
		if((CMProps.getVar(CMProps.Str.MAILBOX).length()>0)
		&&(CMLib.players().playerExists(userName)||CMLib.players().accountExists(userName)))
		{
			final Map<String, List<String>> lists=Resources.getCachedMultiLists("mailinglists.txt",true);
			List<String> mylist=lists.get(journalName);
			if(mylist==null)
			{
				mylist=new Vector<String>();
				lists.put(journalName,mylist);
			}
			boolean found=false;
			for(int l=0;l<mylist.size();l++)
				if(mylist.get(l).equalsIgnoreCase(userName))
					found=true;
			if(!found)
			{
				mylist.add(userName);
				updateMailingLists=true;
				if(CMProps.getBoolVar(CMProps.Bool.EMAILFORWARDING))
				{
					String subscribeTitle="Subscribed";
					String subscribedMsg="You are now subscribed to "+journalName+". To unsubscribe, send an email with a subject of unsubscribe.";
					final String[] msgs =CMProps.getListVar(CMProps.StrList.SUBSCRIPTION_STRS);
					if((msgs!=null)&&(msgs.length>0))
					{
						if(msgs[0].length()>0)
							subscribeTitle = CMLib.coffeeFilter().fullInFilter(CMStrings.replaceAll(msgs[0],"<NAME>",journalName));
						if((msgs.length>0) && (msgs[1].length()>0))
							subscribedMsg = CMLib.coffeeFilter().fullInFilter(CMStrings.replaceAll(msgs[1],"<NAME>",journalName));
					}
					CMLib.database().DBWriteJournalEmail(CMProps.getVar(CMProps.Str.MAILBOX),journalName,journalName,userName,subscribeTitle,subscribedMsg);
				}
			}
		}
		if(updateMailingLists && saveMailingList)
		{
			Resources.updateCachedMultiLists("mailinglists.txt");
		}
		return updateMailingLists;
	}

	@Override
	public boolean unsubscribeFromJournal(String journalName, String userName, boolean saveMailingList)
	{
		boolean updateMailingLists = false;
		if(CMProps.getVar(CMProps.Str.MAILBOX).length()==0)
			return false;

		final Map<String, List<String>> lists=Resources.getCachedMultiLists("mailinglists.txt",true);
		final List<String> mylist=lists.get(journalName);
		if(mylist==null) return false;
		for(int l=mylist.size()-1;l>=0;l--)
			if(mylist.get(l).equalsIgnoreCase(userName))
			{
				mylist.remove(l);
				updateMailingLists=true;
				if(CMProps.getBoolVar(CMProps.Bool.EMAILFORWARDING))
				{
					String unsubscribeTitle="Un-Subscribed";
					String unsubscribedMsg="You are no longer subscribed to "+journalName+". To subscribe again, send an email with a subject of subscribe.";
					final String[] msgs =CMProps.getListVar(CMProps.StrList.SUBSCRIPTION_STRS);
					if((msgs!=null)&&(msgs.length>2))
					{
						if(msgs[2].length()>0)
							unsubscribeTitle = CMLib.coffeeFilter().fullInFilter(CMStrings.replaceAll(msgs[2],"<NAME>",journalName));
						if((msgs.length>3) && (msgs[1].length()>0))
							unsubscribedMsg = CMLib.coffeeFilter().fullInFilter(CMStrings.replaceAll(msgs[3],"<NAME>",journalName));
					}
					CMLib.database().DBWriteJournalEmail(CMProps.getVar(CMProps.Str.MAILBOX),journalName,journalName,userName,unsubscribeTitle,unsubscribedMsg);
				}
			}
		if(updateMailingLists && saveMailingList)
		{
			Resources.updateCachedMultiLists("mailinglists.txt");
		}
		return updateMailingLists;
	}
}
