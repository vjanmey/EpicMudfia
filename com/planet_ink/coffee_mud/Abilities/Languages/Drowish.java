package com.planet_ink.coffee_mud.Abilities.Languages;
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
   Copyright 2000-2014 Lee Fox

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

public class Drowish extends StdLanguage
{
	@Override public String ID() { return "Drowish"; }
	private final static String localizedName = CMLib.lang()._("Drowish");
	@Override public String name() { return localizedName; }
	public static List<String[]> wordLists=null;
	public Drowish()
	{
		super();
	}
	@Override
	public List<String[]> translationVector(String language)
	{
		if(wordLists==null)
		{
			final String[] one={"a","e","i","o","á","é","í","ó"};
			final String[] two={"os","vi","ne","vo","li","eh","no","ai","by","et","ce","un","il"};
			final String[] three={"ána","cil","sar","tan","hel","loa","si'r","hep","yur","nol","hol","qua","éth"};
			final String[] four={"séya","qual","quel","lara","uqua","sana","yava","mas'se","yan'na","quettaparma","manna","manan","merme","carma","harno","harne","varno","essar","saira","cilta","veuma","norta","turme","saita"};
			final String[] five={"cuiva","cuina","nonwa","imire","nauta","cilta","entuc","norta","latin","lòtea","veuya","veuro","apama","hampa","nurta","firta","saira","holle","herwa","uquen","arcoa","calte","cemma","hanta","tan'en"};
			final String[] six={"mahtale","porisal'que","hairie","tararan","amba'rwa","lati'na","olòtie","amawil","apacen","yavinqua","apalume","lin'quil'ea","menelwa","alassea","nurmea","parmasse","ceniril","heldasse","imirin","eari'na","calaten'gew","lapselunga","ria'nna","eneques"};
			wordLists=new Vector<String[]>();
			wordLists.add(one);
			wordLists.add(two);
			wordLists.add(three);
			wordLists.add(four);
			wordLists.add(five);
			wordLists.add(six);
		}
		return wordLists;
	}
	private static final Hashtable<String,String> hashwords=new Hashtable<String,String>();
	@Override
	public Map<String, String> translationHash(String language)
	{
		if((hashwords!=null)&&(hashwords.size()>0))
			return hashwords;

		hashwords.put("NOBLE","c'rintri");
		hashwords.put("DAUGHTER","dalharil");
		hashwords.put("SON","dalharuk");
		hashwords.put("SEERESS","faez'un'arr");
		hashwords.put("MATRON","iharess");
		hashwords.put("PATRON","ilharn");
		hashwords.put("MISTRESS","jabbress");
		hashwords.put("MASTER","jabbuk");
		hashwords.put("HONORED","malla");
		hashwords.put("WARRIOR","sargtlin");
		hashwords.put("ROGUE","shebali");
		hashwords.put("PROSTITUTE","ssins d'aerth");
		hashwords.put("QUEEN","valsharess");
		hashwords.put("DROW","ilythiiri");
		hashwords.put("DRIDER","jilorbb");
		hashwords.put("ELVES","darthiir");
		hashwords.put("DRARVES","duergar");
		hashwords.put("GOBLINS","gol");
		hashwords.put("ILLITHID","haszak");
		hashwords.put("HUMAN","rivvil");
		hashwords.put("HUMANS","rivviln");
		hashwords.put("HALFLINGS","sakphul");
		hashwords.put("DRAGONS","");
		hashwords.put("HALF-ELVES","tu'rilthiir");
		hashwords.put("GNOMES","yingil");
		hashwords.put("ALTAR","orleggin");
		hashwords.put("CHALICE","orshal");
		hashwords.put("A","natha");
		hashwords.put("ABOUT","bauth");
		hashwords.put("ABOVE","phor");
		hashwords.put("ACCOMPLISH","xun");
		hashwords.put("ACCOMPLISHMENT","xundus");
		hashwords.put("ACCOMPLISHMENTS","xundussa");
		hashwords.put("ACHIEVMENT","xundus");
		hashwords.put("ACHIEVEMENT","xundussa");
		hashwords.put("ACROSS","naudal");
		hashwords.put("ADDITIONAL","mziln");
		hashwords.put("AFTER","p'luin");
		hashwords.put("AGAINST","qua'laen");
		hashwords.put("AGREE","qua'l");
		hashwords.put("AGREEMENT","inthigg");
		hashwords.put("AIM","ilindith");
		hashwords.put("ALERT","kyone");
		hashwords.put("ALIKE","esaph");
		hashwords.put("ALIVE","dro");
		hashwords.put("ALL","jal");
		hashwords.put("ALLURE","ssinss");
		hashwords.put("ALLY","abban");
		hashwords.put("ALONE","maglust");
		hashwords.put("ALSO","mziln");
		hashwords.put("ALTHOUGH","d'ril");
		hashwords.put("AM","uil");
		hashwords.put("AMBUSH","thalack'vel");
		hashwords.put("AMONG","wund");
		hashwords.put("AMULET","ilinsar");
		hashwords.put("AND","leuth");
		hashwords.put("ANOTHER","jalbyr");
		hashwords.put("ANY","jala");
		hashwords.put("ANYBODY","jalkhel");
		hashwords.put("ANYONE","jaluss");
		hashwords.put("APART","maglust");
		hashwords.put("APPRENTICE","wanre");
		hashwords.put("ARE","phuul");
		hashwords.put("ARGUMENT","qua'lealay");
		hashwords.put("ARM","da're");
		hashwords.put("ARMOR","ky'ostal");
		hashwords.put("AROUND","bauth");
		hashwords.put("ARREST","ply'uss");
		hashwords.put("ARROW","b'luth'ol");
		hashwords.put("AS","izil");
		hashwords.put("ASSASSIN","velg'larn");
		hashwords.put("ASSASSINATE","ol'elg");
		hashwords.put("ASSASSINATION","ol'elghinn");
		hashwords.put("AT","a");
		hashwords.put("ATTRACTIVE","ssin'urn");
		hashwords.put("AVOID","bautha");
		hashwords.put("AVOIDING","bauthin");
		hashwords.put("BACK","rath");
		hashwords.put("BACKS","ratha");
		hashwords.put("BACKSTAB","rath'elg");
		hashwords.put("BAND","akh");
		hashwords.put("BARRIER","kulggen");
		hashwords.put("BATTLE","thalack");
		hashwords.put("BE","tlu");
		hashwords.put("BEAUTY","ssin");
		hashwords.put("BEAUTIFUL","ssin'urn");
		hashwords.put("BECAUSE","p'wal");
		hashwords.put("BEEN","tlus");
		hashwords.put("BEFORE","p'los");
		hashwords.put("BEHIND","rathrae");
		hashwords.put("BELOW","harl");
		hashwords.put("BENEATH","harl");
		hashwords.put("BESIDE","tu'suul");
		hashwords.put("BEST","alurl");
		hashwords.put("BETTER","alur");
		hashwords.put("BETWEEN","tu'fyr");
		hashwords.put("BEWARE","sarn");
		hashwords.put("BEYOND","tu'jol");
		hashwords.put("BIRTH","ilhar");
		hashwords.put("BITE","tril");
		hashwords.put("BITTER","riknueth");
		hashwords.put("BLADE","velve");
		hashwords.put("BLESS","bel'la");
		hashwords.put("BLOCK","kulg");
		hashwords.put("BLOCKAGE","kulg");
		hashwords.put("BLOOD","vlos");
		hashwords.put("BRAZIER","linthre");
		hashwords.put("BREAK","harventh");
		hashwords.put("BOTHER","dalninuk");
		hashwords.put("BODY","kehl");
		hashwords.put("BOND","valm");
		hashwords.put("BOOK","zhuan'ol");
		hashwords.put("BOTH","tuth");
		hashwords.put("BOW","b'luthyrr");
		hashwords.put("BRAVERY","honglath");
		hashwords.put("BRIGHTNESS","ssussun");
		hashwords.put("BUT","jhal");
		hashwords.put("BY","a");
		hashwords.put("CALM","honglath");
		hashwords.put("CAPTIVE","kul'gobuss");
		hashwords.put("CAPTURE","ply'uss");
		hashwords.put("CARE","kyone");
		hashwords.put("CAREFUL","kyone");
		hashwords.put("CAREFULLY","kyona");
		hashwords.put("CARRION","iblith");
		hashwords.put("CAST","luth");
		hashwords.put("CATTLE","rothe");
		hashwords.put("CAULDRON","linth'el");
		hashwords.put("CAUTION","ne'kales");
		hashwords.put("CAVE","har'ol");
		hashwords.put("CAVERN","har'ol");
		hashwords.put("CHARM","ssinss");
		hashwords.put("CHEST","mamulen");
		hashwords.put("CHILD","dalhar");
		hashwords.put("CHILDREN","dalharen");
		hashwords.put("CITY","che'el");
		hashwords.put("CLIMB","z'orr");
		hashwords.put("CLOAK","piwafwi");
		hashwords.put("COINAGE","belaern");
		hashwords.put("COLD","inthuul");
		hashwords.put("COME","doer");
		hashwords.put("COMMAND","quarth");
		hashwords.put("COMMISSION","a'quarth");
		hashwords.put("COMMISSIONED","a'quarthus");
		hashwords.put("COMPLAIN","elg'car");
		hashwords.put("COMPLAINING","elg'carin");
		hashwords.put("COMPLETE","xundus");
		hashwords.put("COMRADE","abbil");
		hashwords.put("CONCEALMENT","veldrin");
		hashwords.put("CONFRONTATION","qua'laelay");
		hashwords.put("CONQUER","har'luth");
		hashwords.put("CONQUERING","ultrinnan");
		hashwords.put("CONQUEROR","ultrine");
		hashwords.put("CONSIDER","talinth");
		hashwords.put("CONSIDERING","talinthin");
		hashwords.put("CONSPIRACY","olis'inth");
		hashwords.put("CONTINUE","elendar");
		hashwords.put("CONTINUED","elendar");
		hashwords.put("CONTINUING","elendarin");
		hashwords.put("CONTRIBUTION","fielthal");
		hashwords.put("COUNCIL","talthalra");
		hashwords.put("COUP","olis'inthigg");
		hashwords.put("COWARD","rath'arg");
		hashwords.put("COWARDICE","rath'argh");
		hashwords.put("CREATE","beldro");
		hashwords.put("CUT","harventh");
		hashwords.put("DAGGER","velve");
		hashwords.put("DANGER","sreen");
		hashwords.put("DARK","olath");
		hashwords.put("DARKNESS","oloth");
		hashwords.put("DART","kyil");
		hashwords.put("DARTS","kyilen");
		hashwords.put("DEAD","elghinyrr");
		hashwords.put("DEATH","elghinn");
		hashwords.put("DECEIT","waerr'ess");
		hashwords.put("DEDICATE","bel'la");
		hashwords.put("DESPISE","phlith");
		hashwords.put("DESTINY","ul-ilindith");
		hashwords.put("DESTROY","elgg");
		hashwords.put("DID","xunus");
		hashwords.put("DIE","el");
		hashwords.put("DIFFERENT","endar");
		hashwords.put("DISAGREE","qua'lae");
		hashwords.put("DISAGREEMENT","qua'laelay");
		hashwords.put("DISCOVER","ragar");
		hashwords.put("DISHONOR","rath'argh");
		hashwords.put("DISTRUST","ne'kales");
		hashwords.put("DO","xun");
		hashwords.put("DODGE","bautha");
		hashwords.put("DODGING","bauthin");
		hashwords.put("DOING","xunin");
		hashwords.put("DOMINANCE","z'ress");
		hashwords.put("DONE","xunor");
		hashwords.put("DON'T","xuat");
		hashwords.put("DOOR","obsul");
		hashwords.put("DOWN","harl");
		hashwords.put("DRAGON","tagnik'zur");
		hashwords.put("DWELLING","el'lar");
		hashwords.put("EACH","weth");
		hashwords.put("EARTH","har'dro");
		hashwords.put("EAT","cal");
		hashwords.put("EFFORT","xund");
		hashwords.put("EITHER","usbyr");
		hashwords.put("ELECTRICITY","nizzre'");
		hashwords.put("ENCOUNTER","thalra");
		hashwords.put("ENDURE","elendar");
		hashwords.put("ENEMY","ogglin");
		hashwords.put("ESCAPE","do'bauth");
		hashwords.put("ESCAPING","do'bauthin");
		hashwords.put("ESP","z'talin");
		hashwords.put("EVERY","ril");
		hashwords.put("EVERYBODY","rilkhel");
		hashwords.put("EVERYONE","riluss");
		hashwords.put("EVERYTHING","rilbol");
		hashwords.put("EVIL","verin");
		hashwords.put("EXCREMENT","iblith");
		hashwords.put("EXPEDITION","z'hind");
		hashwords.put("EXTRA","mziln");
		hashwords.put("EYE","sol");
		hashwords.put("EYES","solen");
		hashwords.put("FACE","jindurn");
		hashwords.put("FACING","alust");
		hashwords.put("FATHER","ilharn");
		hashwords.put("FAVOR","elamshinae");
		hashwords.put("FAWN","s'lurpp");
		hashwords.put("FAWNING","s'luuppin");
		hashwords.put("FEARLESS","streeaka");
		hashwords.put("FEARLESSNESS","streeaka");
		hashwords.put("FEMALE","jalil");
		hashwords.put("FEW","stath");
		hashwords.put("FIGHTING","melee");
		hashwords.put("FIND","ragar");
		hashwords.put("FIRE","chath");
		hashwords.put("FLAME","chath");
		hashwords.put("FLATTER","s'lurpp");
		hashwords.put("FLATTERING","s'lurppin");
		hashwords.put("FLESH","siltrin");
		hashwords.put("FOOD","cahallin");
		hashwords.put("FOOL","wael");
		hashwords.put("FOOLISH","waela");
		hashwords.put("FOR","whol");
		hashwords.put("FORCE","z'ress");
		hashwords.put("FOREFRONT","alust");
		hashwords.put("FORGIVE","nelgeth");
		hashwords.put("FORGIVENESS","nelgetha");
		hashwords.put("FRIEND","abbil");
		hashwords.put("FROM","da'");
		hashwords.put("FUN","jivvin");
		hashwords.put("FUTURE","ulin");
		hashwords.put("GAP","obsul");
		hashwords.put("GEM","eoul");
		hashwords.put("GET","inbau");
		hashwords.put("GIFT","belbol");
		hashwords.put("GIVE","belbau");
		hashwords.put("GLASS","shanaal");
		hashwords.put("GO","alu");
		hashwords.put("GOAL","ilindith");
		hashwords.put("GOING","aluin");
		hashwords.put("GODDESS","quar'valsharess");
		hashwords.put("GONE","alus");
		hashwords.put("GOOD","bwael");
		hashwords.put("GRACE","elamshinae");
		hashwords.put("HOLY","elamshinae");
		hashwords.put("GRACEFUL","suliss'urn");
		hashwords.put("GRAVE","phalar");
		hashwords.put("TOMB","phalar");
		hashwords.put("GREED","ssinssrigg");
		hashwords.put("GROUP","akh");
		hashwords.put("GUARD","kyorl");
		hashwords.put("GUARD","kyorlin");
		hashwords.put("GUIDE","mrimm");
		hashwords.put("GUIDING","mrimmin");
		hashwords.put("HAD","inbalus");
		hashwords.put("HAG","elg'caress");
		hashwords.put("HANDSOME","ssin'urn");
		hashwords.put("HAVE","inbal");
		hashwords.put("HATE","phlith");
		hashwords.put("HE","uk");
		hashwords.put("HEAD","karliik");
		hashwords.put("HEART","xukuth");
		hashwords.put("HELP","xxizz");
		hashwords.put("HER","ilta");
		hashwords.put("HERE","ghil");
		hashwords.put("HERESY","og'elendar");
		hashwords.put("HERETIC","og'elend");
		hashwords.put("HERS","ilt");
		hashwords.put("HERSELF","iltan");
		hashwords.put("HIDDEN","velkyn");
		hashwords.put("HIGHEST","ultrin");
		hashwords.put("HIM","ukta");
		hashwords.put("HIMSELF","uktan");
		hashwords.put("HIS","ukt");
		hashwords.put("HIT","zotreth");
		hashwords.put("HITCH","kulg");
		hashwords.put("HOLD","mir");
		hashwords.put("HONOR","bel'la");
		hashwords.put("HOT","sseren");
		hashwords.put("HOUSE","qu'ellar");
		hashwords.put("HURL","luth");
		hashwords.put("I","usstan");
		hashwords.put("IF","ka");
		hashwords.put("IMPERIAL","valsharen");
		hashwords.put("IN","wun");
		hashwords.put("INCENSE","valsharen");
		hashwords.put("INN","el'inssrigg");
		hashwords.put("INSIDE","wu'suul");
		hashwords.put("INSPIRATION","mrimm");
		hashwords.put("INSPIRE","mrigg");
		hashwords.put("INSTITUTE","magthere");
		hashwords.put("INSTRUMENT","velnarin");
		hashwords.put("INTELLIGENCE","vel'xundussa");
		hashwords.put("INTELLIGENT","ne'kalsa");
		hashwords.put("INTERCOURSE","vith");
		hashwords.put("INTO","wund");
		hashwords.put("INVISIBLE","velkyn");
		hashwords.put("IS","zhah");
		hashwords.put("IT","ol");
		hashwords.put("ITS","olt");
		hashwords.put("ITSELF","oltan");
		hashwords.put("ITEM","bol");
		hashwords.put("JAVELIN","luth'ol");
		hashwords.put("JOIN","valm");
		hashwords.put("JOURNEY","z'hind");
		hashwords.put("KEY","mrim'ol");
		hashwords.put("KIDNAP","ply'usaerth");
		hashwords.put("KILL","elgg");
		hashwords.put("KNEEL","harl'il'cik");
		hashwords.put("KNIFE","velve");
		hashwords.put("KNOW","zhaun");
		hashwords.put("KNOWLEDGE","zhaunil");
		hashwords.put("LARGE","izznarg");
		hashwords.put("LEARN","zhaun");
		hashwords.put("LEG","da'ur");
		hashwords.put("LIFE","dro");
		hashwords.put("LIGHT","ssussun");
		hashwords.put("LIGHTNING","nizzre'");
		hashwords.put("LIKE","saph");
		hashwords.put("LOCATION","k'lar");
		hashwords.put("LOCK","mri'kul");
		hashwords.put("LOST","noamuth");
		hashwords.put("LOVE","ssinssrigg");
		hashwords.put("LOVER","d'ssinss");
		hashwords.put("LOVING","ssinssriggin");
		hashwords.put("LUST","ssinssrigg");
		hashwords.put("LUSTING","ssinssriggin");
		hashwords.put("MAGE","faern");
		hashwords.put("MAGIC","faer");
		hashwords.put("MAGICAL","faerl");
		hashwords.put("MALE","jaluk");
		hashwords.put("MANY","mzil");
		hashwords.put("MAY","xal");
		hashwords.put("ME","ussa");
		hashwords.put("MEDALLION","ilinsar");
		hashwords.put("MEET","thalra");
		hashwords.put("MEETING","talthalra");
		hashwords.put("MEMORY","zha'linth");
		hashwords.put("MINE","usst");
		hashwords.put("MONSTER","phindar");
		hashwords.put("MORE","mzild");
		hashwords.put("MOST","mzildst");
		hashwords.put("MOTHER","ilhar");
		hashwords.put("MUCH","mzilt");
		hashwords.put("MUSIC","ssinsuurul");
		hashwords.put("MY","ussta");
		hashwords.put("MYSELF","usstan");
		hashwords.put("NEITHER","nausbyr");
		hashwords.put("NEUTRAL","noalith");
		hashwords.put("NO","nau");
		hashwords.put("NOBODY","naukhel");
		hashwords.put("NONE","naust");
		hashwords.put("NOT","naut");
		hashwords.put("NOTHING","naubol");
		hashwords.put("NOW","nin");
		hashwords.put("OF","d'");
		hashwords.put("OFF","tir");
		hashwords.put("OFFAL","iblith");
		hashwords.put("OLD","zhaunth");
		hashwords.put("ON","pholor");
		hashwords.put("OPENING","obsul");
		hashwords.put("OPPONENT","ogglin");
		hashwords.put("OPPOSE","ogglir");
		hashwords.put("OPPOSING","ogglirin");
		hashwords.put("OPPOSITE","indarae");
		hashwords.put("OR","xor");
		hashwords.put("OTHER","byr");
		hashwords.put("OUR","udossta");
		hashwords.put("OURS","udosst");
		hashwords.put("OURSELVES","udosstan");
		hashwords.put("OUT","doeb");
		hashwords.put("OUTCAST","dobluth");
		hashwords.put("OUTSIDE","do'suul");
		hashwords.put("OVER","phor");
		hashwords.put("PAIN","jiv'undus");
		hashwords.put("PARDON","nelgetha");
		hashwords.put("PARIAH","dobluth");
		hashwords.put("PASSION","ssinssrigg");
		hashwords.put("PAST","zhahn");
		hashwords.put("PATH","colbauth");
		hashwords.put("PERHAPS","xal");
		hashwords.put("PIRACY","op'elgin");
		hashwords.put("PIRATE","op'elg");
		hashwords.put("PLACE","k'lar");
		hashwords.put("PLAN","inth");
		hashwords.put("PLANE","zik'den'vever");
		hashwords.put("PLATTER","lintaguth");
		hashwords.put("PLAY","jivvin");
		hashwords.put("PLOT","olis'inth");
		hashwords.put("POISON","elg'cahl");
		hashwords.put("POWER","z'ress");
		hashwords.put("PRAISE","bel'la");
		hashwords.put("PREVAIL","ultrinnan");
		hashwords.put("PRESENT","nin");
		hashwords.put("PRISON","kul'gobsula");
		hashwords.put("PRISONER","kul'gobuss");
		hashwords.put("PROFIT","belaern");
		hashwords.put("PUNCH","zotreth");
		hashwords.put("PUNISH","sarn'elgg");
		hashwords.put("RAID","thalackz'hind");
		hashwords.put("RANSOM","ply'usaerth");
		hashwords.put("RECKLESS","streeaka");
		hashwords.put("RECKLESSNESS","streeaka");
		hashwords.put("RECONNAISSANCE","vel'xundussa");
		hashwords.put("REMOVE","drewst");
		hashwords.put("RIDE","z'har");
		hashwords.put("RIVAL","ogglin");
		hashwords.put("RIVALING","ogglirin");
		hashwords.put("ROYAL","valsharen");
		hashwords.put("RUSE","golhyrr");
		hashwords.put("SACRED","orthae");
		hashwords.put("SAFE","sreen'aur");
		hashwords.put("SAFETY","sreen'aur");
		hashwords.put("SCHEME","inth");
		hashwords.put("SCHOOL","magthere");
		hashwords.put("SCOURGE","elgluth");
		hashwords.put("SCROLL","narkith");
		hashwords.put("SECURITY","vel'xundussa");
		hashwords.put("SEDUCTION","ssinss");
		hashwords.put("SEIZE","plynn");
		hashwords.put("SERVANT","wanre");
		hashwords.put("SEVER","harventh");
		hashwords.put("SEVERAL","blynol");
		hashwords.put("SEX","vith");
		hashwords.put("SHADOWS","veldrin");
		hashwords.put("SHADOW","veldr");
		hashwords.put("SHALL","zhal");
		hashwords.put("SHE","il");
		hashwords.put("SHIELD","kulggen");
		hashwords.put("SIDE","suul");
		hashwords.put("SILENCE","venorsh");
		hashwords.put("SILENT","venorik");
		hashwords.put("SINCE","yol");
		hashwords.put("SIMILAR","indar");
		hashwords.put("SISTER","dalninil");
		hashwords.put("SKIN","waess");
		hashwords.put("SLAVE","rothe");
		hashwords.put("SLAY","elgg");
		hashwords.put("SMALL","inlul");
		hashwords.put("SMART","ne'kalsa");
		hashwords.put("SNAG","ji");
		hashwords.put("SOME","fol");
		hashwords.put("SOMEBODY","folkhel");
		hashwords.put("SOMEONE","foluss");
		hashwords.put("SOMETHING","folbol");
		hashwords.put("SPEAR","luth'ol");
		hashwords.put("SPELL","faerz'undus");
		hashwords.put("SPELLBOOK","faerz'ol");
		hashwords.put("SPIDER","orbb");
		hashwords.put("SPY","vel'xunyrr");
		hashwords.put("STEAL","olplynir");
		hashwords.put("STEALTH","olist");
		hashwords.put("STRATAGEM","inth");
		hashwords.put("STRENGTH","z'ress");
		hashwords.put("STRIKE","zotreth");
		hashwords.put("STRIP","raldar");
		hashwords.put("STRIVING","xund");
		hashwords.put("SUBJUGATE","har'luth");
		hashwords.put("SUCH","folt");
		hashwords.put("SUICIDE","streea");
		hashwords.put("SUPERIOR","alur");
		hashwords.put("SUPREME","ultrine");
		hashwords.put("SURPRISE","brorn");
		hashwords.put("SURPRISES","brorna");
		hashwords.put("SURVIVAL","dro'xundus");
		hashwords.put("SURVIVE","dro'xun");
		hashwords.put("SWEET","ssinjin");
		hashwords.put("SWORD","velve");
		hashwords.put("SYCOPHANT","s'luppuk");
		hashwords.put("TAKE","plynn");
		hashwords.put("TAVERN","el'inssrigg");
		hashwords.put("TELEPATHY","z'talin");
		hashwords.put("TEMPLE","yath");
		hashwords.put("THAN","taga");
		hashwords.put("THANK","bel'la");
		hashwords.put("THAT","nindel");
		hashwords.put("THE","lil");
		hashwords.put("THEIR","ninta");
		hashwords.put("THEIRS","nint");
		hashwords.put("THEM","nina");
		hashwords.put("THEMSELVES","nintan");
		hashwords.put("THERE","gaer");
		hashwords.put("THESE","nindolen");
		hashwords.put("THEY","nind");
		hashwords.put("THEN","zhahn");
		hashwords.put("THIEF","olplyn");
		hashwords.put("THIEVING","op'elgin");
		hashwords.put("THING","bol");
		hashwords.put("THINK","talinth");
		hashwords.put("THIS","nindol");
		hashwords.put("THOSE","nindyn");
		hashwords.put("THROAT","rinteith");
		hashwords.put("THRONE","sharorr");
		hashwords.put("THROW","luth");
		hashwords.put("TIME","draeval");
		hashwords.put("TO","ulu");
		hashwords.put("TOGETHER","ul'naus");
		hashwords.put("TORTURE","jiv'elgg");
		hashwords.put("TOWARD","ulan");
		hashwords.put("TRADITIONAL","elend");
		hashwords.put("TRAITOR","og'elend");
		hashwords.put("TRAP","golhyrr");
		hashwords.put("TRAPDOOR","obsu'arl");
		hashwords.put("TREASON","og'elendar");
		hashwords.put("TREASURE","belaern");
		hashwords.put("TREATISE","zhuan'ol");
		hashwords.put("TREATY","inthigg");
		hashwords.put("TRICK","golhyrr");
		hashwords.put("TRIP","z'hind");
		hashwords.put("TRUST","khaless");
		hashwords.put("UNAWARE","waela");
		hashwords.put("UNCOVER","ragar");
		hashwords.put("UNDER","harl");
		hashwords.put("UNDERDARK","har'oloth");
		hashwords.put("UNKNOWN","noamuth");
		hashwords.put("UNSEEN","velkyn");
		hashwords.put("UNTIL","hwuen");
		hashwords.put("UP","phor");
		hashwords.put("UPON","pholor");
		hashwords.put("US","udossa");
		hashwords.put("USUAL","elend");
		hashwords.put("VALOR","sargh");
		hashwords.put("VICTORY","ultrinnan");
		hashwords.put("WAIT","kyorl");
		hashwords.put("WAITING","kyorlin");
		hashwords.put("WALK","z'hin");
		hashwords.put("WANDERER","noamuth");
		hashwords.put("WAR","thalack");
		hashwords.put("WARDS","ky'ov'aer");
		hashwords.put("WARINESS","kyona");
		hashwords.put("WARNING","sarn");
		hashwords.put("WARY","kyone");
		hashwords.put("WAS","zhahus");
		hashwords.put("WATCH","kyorl");
		hashwords.put("WATCHFUL","kyorl'urn");
		hashwords.put("WATCHING","kyorlin");
		hashwords.put("WATER","niar");
		hashwords.put("WAY","colbauth");
		hashwords.put("WE","udos");
		hashwords.put("WEALTH","belaern");
		hashwords.put("YOU","dos");
		hashwords.put("YOURS","dosst");
		hashwords.put("YOUR","dossta");
		hashwords.put("YOURSELF","dosstan");
		hashwords.put("EAST","luent");
		hashwords.put("NORTH","trezen");
		hashwords.put("NORTHEAST","trez'nt");
		hashwords.put("NORTHWEST","trez'in");
		hashwords.put("SOUTH","werneth");
		hashwords.put("SOUTHEAST","wern'nt");
		hashwords.put("SOUTHWEST","wern'in");
		hashwords.put("WEST","linoin");
		hashwords.put("ONE","uss");
		hashwords.put("TWO","draa");
		hashwords.put("THREE","llar");
		hashwords.put("FOUR","quen");
		hashwords.put("FIVE","huela");
		hashwords.put("SIX","rraun");
		hashwords.put("SEVEN","blyn");
		hashwords.put("EIGHT","lael");
		hashwords.put("NINE","thal");
		hashwords.put("TEN","szith");
		hashwords.put("1","uss");
		hashwords.put("2","draa");
		hashwords.put("3","llar");
		hashwords.put("4","quen");
		hashwords.put("5","huela");
		hashwords.put("6","rraun");
		hashwords.put("7","blyn");
		hashwords.put("8","lael");
		hashwords.put("9","thal");
		hashwords.put("10","szith");
		hashwords.put("ELEVEN","szithus");
		hashwords.put("TWELVE","szithdra");
		hashwords.put("THIRTEEN","szithla");
		hashwords.put("FOURTEEN","szithhuen");
		hashwords.put("FIFTEEN","szithuel");
		hashwords.put("SIXTEEN","szithraun");
		hashwords.put("SEVENTEEN","szithlyn");
		hashwords.put("EIGHTEEN","szithael");
		hashwords.put("NINETEEN","szithal");
		hashwords.put("TWENTY","draszith");
		hashwords.put("11","szithus");
		hashwords.put("12","szithdra");
		hashwords.put("13","szithla");
		hashwords.put("14","szithhuen");
		hashwords.put("15","szithuel");
		hashwords.put("16","szithraun");
		hashwords.put("17","szithlyn");
		hashwords.put("18","szithael");
		hashwords.put("19","szithal");
		hashwords.put("20","draszith");
		hashwords.put("FIFTY","hueszith");
		hashwords.put("50","hueszith");
		hashwords.put("RAVHEL","hundred");
		hashwords.put("SZITHREL","thousand");
		hashwords.put("FIRST","ust");
		hashwords.put("SECOND","drada");
		hashwords.put("THIRD","llarnbuss");
		hashwords.put("FOURTH","quenar");
		hashwords.put("FIFTH","huelar");
		hashwords.put("SIXTH","rraunar");
		hashwords.put("SEVENTH","blynar");
		hashwords.put("EIGHTH","laelar");
		hashwords.put("NINTH","thalar");
		hashwords.put("TENTH","szithar");
		hashwords.put("1ST","ust");
		hashwords.put("2ND","drada");
		hashwords.put("3RD","llarnbuss");
		hashwords.put("4TH","quenar");
		hashwords.put("5TH","huelar");
		hashwords.put("6TH","rraunar");
		hashwords.put("7TH","blynar");
		hashwords.put("8TH","laelar");
		hashwords.put("9TH","thalar");
		hashwords.put("10TH","szithar");
		hashwords.put("ELEVENTH","szithusar");
		hashwords.put("TWELFTH","szithudrar");
		hashwords.put("THIRTEENTH","szithlar");
		hashwords.put("FOURTEENTH","szithuenar");
		hashwords.put("FIFTEENTH","szithuelar");
		hashwords.put("SIXTEENTH","szithraunar");
		hashwords.put("SEVENTEENTH","szithlynar");
		hashwords.put("EIGHTEENTH","szithaelar");
		hashwords.put("NINETEENTH","szithalar");
		hashwords.put("TWENTIETH","draszithar");
		hashwords.put("11TH","szithusar");
		hashwords.put("12TH","szithudrar");
		hashwords.put("13TH","szithlar");
		hashwords.put("14TH","szithuenar");
		hashwords.put("15TH","szithuelar");
		hashwords.put("16TH","szithraunar");
		hashwords.put("17TH","szithlynar");
		hashwords.put("18TH","szithaelar");
		hashwords.put("19TH","szithalar");
		hashwords.put("20TH","draszithar");
		hashwords.put("FIFTIETH","hueszithar");
		hashwords.put("50TH","hueszithar");
		hashwords.put("HUNDREDTH","ravhelar");
		hashwords.put("THOUSANDTH","szithrelar");
//
//		These are words for which the Drow do not have a translation for since the concepts are lost to them.
//		Thus, they are translated straight.
//
		hashwords.put("JOY","joy");
		hashwords.put("COMPASSION","compassion");
		hashwords.put("HOPE","hope");
		hashwords.put("JUSTICE","justice");
		hashwords.put("MORAL","moral");
		hashwords.put("ETHIC","ethic");
		hashwords.put("ETHICS","ethics");
		hashwords.put("VIRTUE","virtue");
		hashwords.put("VIRTUOUS","virtuous");
		return hashwords;
		}
	}
