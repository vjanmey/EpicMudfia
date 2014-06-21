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

public class Elvish extends StdLanguage
{
	@Override public String ID() { return "Elvish"; }
	private final static String localizedName = CMLib.lang()._("Elvish");
	@Override public String name() { return localizedName; }
	public static List<String[]> wordLists=null;
	public Elvish()
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
			final String[] three={"ána","cil","sar","tan","hel","loa","sir","hep","yur","nol","hol","qua","éth"};
			final String[] four={"séya","qual","quel","lara","uqua","sana","yava","masse","yanna","quettaparma","manna","manan","merme","carma","harno","harne","varno","essar","saira","cilta","veuma","norta","turme","saita"};
			final String[] five={"cuiva","cuina","nonwa","imire","nauta","cilta","entuc","norta","latin","lòtea","veuya","veuro","apama","hampa","nurta","firta","saira","holle","herwa","uquen","arcoa","calte","cemma","hanta","tanen"};
			final String[] six={"mahtale","porisalque","hairie","tararan","ambarwa","latina","olòtie","amawil","apacen","yavinqua","apalume","linquilea","menelwa","alassea","nurmea","parmasse","ceniril","heldasse","imirin","earina","calatengew","lapselunga","rianna","eneques"};
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
		hashwords.put("ABANDON","avarta");
		hashwords.put("ABLE","pol");
		hashwords.put("ACCOMMODATE","camta");
		hashwords.put("ACT","car");
		hashwords.put("ADAPT","camta");
		hashwords.put("ADDRESS","tengessë");
		hashwords.put("AFFECT","peresta");
		hashwords.put("AFFECTED","persana");
		hashwords.put("AFFECTION","persanië");
		hashwords.put("AFFECTIONATE","méla");
		hashwords.put("AFTER","nó");
		hashwords.put("AGAINST","ara");
		hashwords.put("AGED","yeniquanta");
		hashwords.put("AGO","luina");
		hashwords.put("AID","resta");
		hashwords.put("ALIKE","óvëa");
		hashwords.put("ALONE","erinqua");
		hashwords.put("ALREADY","epello");
		hashwords.put("ALSO","yando");
		hashwords.put("ALTER","presto");
		hashwords.put("ALWAYS","illumë");
		hashwords.put("AMEN","násië");
		hashwords.put("AMONG","imíca");
		hashwords.put("AND","ar");
		hashwords.put("ANGLE","vennassë");
		hashwords.put("ANNIVERSARY","atyenárë");
		hashwords.put("ANSWER","dangweth");
		hashwords.put("APPARITION","ausa");
		hashwords.put("APPLIANCE","yungwa");
		hashwords.put("APPROACH","analendë");
		hashwords.put("APPROPRIATE","sati");
		hashwords.put("ARTICLE","maca");
		hashwords.put("AS","ier");
		hashwords.put("ASSEMBLE","yocar");
		hashwords.put("ATTACK","nalanta");
		hashwords.put("BEFORE","epë");
		hashwords.put("BEGETTER","ammë");
		hashwords.put("BEGIN","yesta");
		hashwords.put("BEGINNING","yessë");
		hashwords.put("BELT","quilta");
		hashwords.put("BESIDE","apa");
		hashwords.put("BETRAY","varta");
		hashwords.put("BETRAYER","varto");
		hashwords.put("BE","nauva");
		hashwords.put("BEYOND","lá");
		hashwords.put("BLADE","maica");
		hashwords.put("BLEND","ostimë");
		hashwords.put("BLESSED","manaquenta");
		hashwords.put("BLOOM","etelotya");
		hashwords.put("BLOSSOM","olótë");
		hashwords.put("BOOT","saipo");
		hashwords.put("BOUND","lanya");
		hashwords.put("BOUNDS","lanwa");
		hashwords.put("BOUNDARY","taica");
		hashwords.put("BROTH","salpa");
		hashwords.put("BURDEN","cólo");
		hashwords.put("BUT","ono");
		hashwords.put("CAN","ista");
		hashwords.put("CAT","miura");
		hashwords.put("CATCH","raita");
		hashwords.put("CENTRE","entë");
		hashwords.put("CENTER","entë");
		hashwords.put("CENTRAL","entya");
		hashwords.put("CERTAINLY","tancavë");
		hashwords.put("CHAIN","angwenda");
		hashwords.put("CHARACTER","indómë");
		hashwords.put("CHEERS","almien");
		hashwords.put("CHESS","arantyalmë");
		hashwords.put("CHESSBOARD","artapano");
		hashwords.put("CHOOSE","cil");
		hashwords.put("CHRIST","Elpino");
		hashwords.put("CIGARETTE","uscillë");
		hashwords.put("CITY","minassë");
		hashwords.put("CLEANSE","poita");
		hashwords.put("CLOAK","fanta");
		hashwords.put("CLOCK","lúma");
		hashwords.put("CLOSED","avalatya");
		hashwords.put("CLOSENESS","aquapahtië");
		hashwords.put("CLOTHE","hap");
		hashwords.put("CLOTHING","hampë");
		hashwords.put("COMING","tulessë");
		hashwords.put("COMPASSION","ófelmë");
		hashwords.put("COMPOSE","yocar");
		hashwords.put("COMPRESS","sanga");
		hashwords.put("CONCERN","apa");
		hashwords.put("CONCERNING","pá");
		hashwords.put("CONFIRM","tancata");
		hashwords.put("CONFRONTING","nimba");
		hashwords.put("CONSONANT","náva-tengwë");
		hashwords.put("CONSTRUCT","yocar");
		hashwords.put("CONVERSE","artaquet");
		hashwords.put("COPPER","calarus");
		hashwords.put("COURSE","ratta");
		hashwords.put("COVERING","vailë");
		hashwords.put("CURSE","racco");
		hashwords.put("DAILY","ilyarëa");
		hashwords.put("DANGER","raxalë");
		hashwords.put("DEBATE","artaquet");
		hashwords.put("DEBT","luhta");
		hashwords.put("DEBTOR","lucando");
		hashwords.put("DEEM","ndab");
		hashwords.put("DEER","arassë");
		hashwords.put("DEFENCE","ortírië");
		hashwords.put("DEFINED","lanwa");
		hashwords.put("DELIVER","eterúna");
		hashwords.put("DEMON","úmaia");
		hashwords.put("DENSE","sangwa");
		hashwords.put("DEPRIVED","racinë");
		hashwords.put("DESCRIBE","úna");
		hashwords.put("DESIRE","námië");
		hashwords.put("DESPISE","nattira");
		hashwords.put("DESTITUTE","úna");
		hashwords.put("DEVIL","úmaia");
		hashwords.put("DIFFICULTY","taryassë");
		hashwords.put("DIPHTHONG","ohlon");
		hashwords.put("DISTANCE","hayassë");
		hashwords.put("DISTANCING","hailë");
		hashwords.put("DISTURB","peresta");
		hashwords.put("DIVIDE","sati");
		hashwords.put("DIVINE","eruva");
		hashwords.put("DOOR","fendassë");
		hashwords.put("DOORWAY","fendassë");
		hashwords.put("DOWN","nat");
		hashwords.put("DRAIN","suhta");
		hashwords.put("DRINK","yulmë");
		hashwords.put("DRINKER","yulmo");
		hashwords.put("DRUM","rambillë");
		hashwords.put("DYNASTY","hilyalë");
		hashwords.put("ECCLESIASTIC","hostalya");
		hashwords.put("EDGE","lanca");
		hashwords.put("EIGHTH","toldëa");
		hashwords.put("ELDER","anyáro");
		hashwords.put("EMINENT","minya");
		hashwords.put("EMOTION","felmë");
		hashwords.put("EMPLOY","yuhta");
		hashwords.put("ENHANCE","han");
		hashwords.put("ENCLOSE","lanya");
		hashwords.put("END","lancassë");
		hashwords.put("ENDURE","larta");
		hashwords.put("ENFOLD","hampë");
		hashwords.put("ENLACED","raina");
		hashwords.put("ENQUIRY","minasurië");
		hashwords.put("ENTANGLED","rembina");
		hashwords.put("ENTER","mitta");
		hashwords.put("ENTRAP","remi");
		hashwords.put("ENTREATY","arcandë");
		hashwords.put("ERROR","mista");
		hashwords.put("ESTABLISH","tancata");
		hashwords.put("ETERNITY","oirë");
		hashwords.put("EVIL","ulka");
		hashwords.put("EXCAVATE","rosta");
		hashwords.put("EXCLUDED","satya");
		hashwords.put("EXILE","etelerro");
		hashwords.put("EXILED","etelenda");
		hashwords.put("EXTENSION","taima");
		hashwords.put("FACE","nívë");
		hashwords.put("FACING","nimba");
		hashwords.put("FASHION","eccat");
		hashwords.put("FASTENING","tanca");
		hashwords.put("FEEL","tenya");
		hashwords.put("FEELING","tendilë");
		hashwords.put("FENCE","ettelë");
		hashwords.put("FIFTH","lempëa");
		hashwords.put("FINGER","lepsë");
		hashwords.put("FINITE","lanwa");
		hashwords.put("FIRM","talya");
		hashwords.put("FIT","camta");
		hashwords.put("FLOOD","oloiya");
		hashwords.put("FLOWER","lotsë");
		hashwords.put("FLY","ramya");
		hashwords.put("FOLLOW","veuya");
		hashwords.put("FOLLOWER","veuro");
		hashwords.put("FOLLOWING","hilmë");
		hashwords.put("FOOD","sulpa");
		hashwords.put("FOOTSTOOL","sarassë");
		hashwords.put("FOR","rá");
		hashwords.put("FORCE","sahtië");
		hashwords.put("FOREBODE","apaquet");
		hashwords.put("FORESTER","apaquet");
		hashwords.put("FORGIVE","apsene");
		hashwords.put("FORLORN","úna");
		hashwords.put("FORSAKE","avarta");
		hashwords.put("FORT","minassë");
		hashwords.put("FORTRESS","arta");
		hashwords.put("FOUNTAIN","ehtelë");
		hashwords.put("FOURTH","cantëa");
		hashwords.put("FOXY","ruscu");
		hashwords.put("FROG","carpo");
		hashwords.put("FRONT","nívë");
		hashwords.put("FULL","penquanta");
		hashwords.put("FULLNESS","quantassë");
		hashwords.put("FUR","helet");
		hashwords.put("FUTURE","apalúmë");
		hashwords.put("GARMENT","hampë");
		hashwords.put("GATEWAY","fendassë");
		hashwords.put("GATHERING","hostalë");
		hashwords.put("GET","net");
		hashwords.put("GLOOM","nimbë");
		hashwords.put("GLORIFY","alcarya");
		hashwords.put("GNAW","nyanda");
		hashwords.put("GORGE","capië");
		hashwords.put("GRACE","erulisse");
		hashwords.put("GRACIOUS","raina");
		hashwords.put("GRAVE","sarca");
		hashwords.put("GREET","suila");
		hashwords.put("GREETING","suilië");
		hashwords.put("GROW","lauya");
		hashwords.put("HALLOW","airita");
		hashwords.put("HARASS","tarasta");
		hashwords.put("HARD","sarda");
		hashwords.put("HARP","tanta");
		hashwords.put("HARVEST","cermië");
		hashwords.put("HASSOCK","sarassë");
		hashwords.put("HAVE","inyen");
		hashwords.put("HAVEN","ciryapanda");
		hashwords.put("HE","së");
		hashwords.put("HEAVE","solto");
		hashwords.put("HEAVEN","eruman");
		hashwords.put("HEM","lanë");
		hashwords.put("HERO","salyon");
		hashwords.put("HOLD","hep");
		hashwords.put("HOMELESS","avamarwa");
		hashwords.put("HONOUR","han");
		hashwords.put("HOP","lapa");
		hashwords.put("HOPE","amatírë");
		hashwords.put("HORN","ramna");
		hashwords.put("HUNGRY","maita");
		hashwords.put("HUNT","fara");
		hashwords.put("HURL","hat");
		hashwords.put("HYMN","airelinna");
		hashwords.put("IF","ai");
		hashwords.put("IMPEDED","tapta");
		hashwords.put("IMPEL","or");
		hashwords.put("IMPELLED","horna");
		hashwords.put("IMPETUOUS","ascara");
		hashwords.put("IMPULSE","hroafelmë");
		hashwords.put("IN","mina");
		hashwords.put("INACTION","lacarë");
		hashwords.put("INADEQUATE","penya");
		hashwords.put("INCREASE","han");
		hashwords.put("INDICATE","tëa");
		hashwords.put("INDUCE","sahta");
		hashwords.put("INEVITABILITY","sangië");
		hashwords.put("INSPIRE","mihwesta");
		hashwords.put("INSULT","ehta");
		hashwords.put("INTEND","elya");
		hashwords.put("INTO","mitta");
		hashwords.put("INUNDATE","oloiya");
		hashwords.put("INWARDS","mitta");
		hashwords.put("ISLANDER","tolloquen");
		hashwords.put("JESUS","yésus");
		hashwords.put("JUDGE","nem");
		hashwords.put("JUDGEMENT","námië");
		hashwords.put("JUMP","lapa");
		hashwords.put("JOURNEY","lendë");
		hashwords.put("KEEP","hep");
		hashwords.put("KINDLE","calta");
		hashwords.put("KINGDOM","turindië");
		hashwords.put("KITCHEN","mastasan");
		hashwords.put("KNEAD","mascata");
		hashwords.put("LABOUR","arassë");
		hashwords.put("LACE","raiwë");
		hashwords.put("LACKING","penya");
		hashwords.put("LAMENTING","nainala");
		hashwords.put("LAST","larta");
		hashwords.put("LEAD","tulya");
		hashwords.put("LEAN","linya");
		hashwords.put("LEAP","cap");
		hashwords.put("LEARN","nolya");
		hashwords.put("LENITION","persanië");
		hashwords.put("LENITED","persana");
		hashwords.put("LIBRARY","parmassë");
		hashwords.put("LIBRARIAN","parmasson");
		hashwords.put("LIBRARIANS","parmassondi");
		hashwords.put("LIFE","cuivië");
		hashwords.put("LIKEHOOD","óvëassë");
		hashwords.put("LIMIT","taica");
		hashwords.put("LIMITED","lanya");
		hashwords.put("LIP","pé");
		hashwords.put("LIPS","peu");
		hashwords.put("LOFTY","ancassëa");
		hashwords.put("LONG-LIVED","yeniquanta");
		hashwords.put("LOVING","méla");
		hashwords.put("MANTLE","fanta");
		hashwords.put("MARIA","maría");
		hashwords.put("MARK","talca");
		hashwords.put("MAYBE","cé");
		hashwords.put("MEAGRE","linya");
		hashwords.put("MEAN","selya");
		hashwords.put("MEET","ovanta");
		hashwords.put("MIGHTY","taura");
		hashwords.put("MIND","sáma");
		hashwords.put("MIDDLE","entya");
		hashwords.put("MOTHER","amillë");
		hashwords.put("MOUTH","náva");
		hashwords.put("NECESSITY","sangië");
		hashwords.put("NET","raima");
		hashwords.put("NETTED","raina");
		hashwords.put("NETWORK","raimë");
		hashwords.put("NEWS","sinyar");
		hashwords.put("NINTH","nertëa");
		hashwords.put("NUMERAL","notessë");
		hashwords.put("OBSTINATE","taryalanca");
		hashwords.put("OIL","LIB");
		hashwords.put("OMNIFICENT","ilucara");
		hashwords.put("OMNIPOTENT","ilúvala");
		hashwords.put("OMNISCIENT","iluisa");
		hashwords.put("ON","apa");
		hashwords.put("ONE","mo");
		hashwords.put("OPEN","láta");
		hashwords.put("OPENING","latya");
		hashwords.put("OPENNESS","látië");
		hashwords.put("OPPOSED","ara");
		hashwords.put("OPPOSITE","ara");
		hashwords.put("OPPRESSION","sangarë");
		hashwords.put("OR","var");
		hashwords.put("OVER","terwa");
		hashwords.put("PASTURE","narassë");
		hashwords.put("PATH","rata");
		hashwords.put("PATHWAY","ratta");
		hashwords.put("PATRONAGE","ortírië");
		hashwords.put("PEACE","sívë");
		hashwords.put("PERHAPS","quíta");
		hashwords.put("PERMISSION","lávë");
		hashwords.put("PETITION","arcandë");
		hashwords.put("PHONETIC","lambelë");
		hashwords.put("PHONETICS","lambelë");
		hashwords.put("PICK","lepta");
		hashwords.put("PINETREE","sondë");
		hashwords.put("PINETREES","sondi");
		hashwords.put("PLEASE","iquista");
		hashwords.put("POET","lairemo");
		hashwords.put("POST","talca");
		hashwords.put("POUT","penga");
		hashwords.put("PRAY","arca");
		hashwords.put("PRAYER","arcandë");
		hashwords.put("PRESS","nir");
		hashwords.put("PRESSURE","sahtië");
		hashwords.put("PRICK","ehta");
		hashwords.put("PRIVACY","aquapahtië");
		hashwords.put("PRIVATE","satya");
		hashwords.put("PROMINENT","eteminya");
		hashwords.put("PROTECTION","ortírië");
		hashwords.put("PURPOSE","selya");
		hashwords.put("PURSUE","sac");
		hashwords.put("QUADRANGLE","cantil");
		hashwords.put("QUADRANGLES","cantildi");
		hashwords.put("QUADRANGULAR","cantilya");
		hashwords.put("QUESTION","maquetta");
		hashwords.put("QUESTIONS","maquetta");
		hashwords.put("RANSOM","nanwenda");
		hashwords.put("RAVINE","rissë");
		hashwords.put("RAVISH","amapta");
		hashwords.put("READ","cenda");
		hashwords.put("REAP","cer");
		hashwords.put("REAPING","cermë");
		hashwords.put("REASON","tyarwë");
		hashwords.put("REDEEMER","runando");
		hashwords.put("RED-HAIRED","russa");
		hashwords.put("REGARDS","pá");
		hashwords.put("REGULATIONS","namnasta");
		hashwords.put("RELEASE","lerya");
		hashwords.put("REMOVAL","hailë");
		hashwords.put("RESEMBLANCE","óvëassë ");
		hashwords.put("RESOLVE","indo");
		hashwords.put("RIDE","norta");
		hashwords.put("RIDGEPOLE","orpano");
		hashwords.put("RISE","tyulya");
		hashwords.put("RIVER-BED ","ratta");
		hashwords.put("RIVET","tanca");
		hashwords.put("ROBBERY","maptalë");
		hashwords.put("ROBE","vaimata");
		hashwords.put("RUSHING","ascara");
		hashwords.put("SAD","lemba");
		hashwords.put("SADNESS","nimbë");
		hashwords.put("SAIL","ramya");
		hashwords.put("SAINT","aimo");
		hashwords.put("SAPLING","nessornë");
		hashwords.put("SCRATCH","rimpë");
		hashwords.put("SCRIBE","tecindo");
		hashwords.put("SEARCH","sac");
		hashwords.put("SEAT","hamba");
		hashwords.put("SECOND","attëa");
		hashwords.put("SEEKING","surië");
		hashwords.put("SEEM","séya");
		hashwords.put("SEND","menta");
		hashwords.put("SENTIMENT","tendilë");
		hashwords.put("SEPARATE","satya");
		hashwords.put("SERVE","hilya");
		hashwords.put("SEVENTH","otsëa");
		hashwords.put("SHARPEN","laiceta");
		hashwords.put("SHE","së");
		hashwords.put("SHUT","pahta");
		hashwords.put("SIN","naiquë");
		hashwords.put("SINGLE","erya");
		hashwords.put("SIXTH","enquëa");
		hashwords.put("SMILING","raina");
		hashwords.put("SNARE","remma");
		hashwords.put("SO","sië");
		hashwords.put("SOFT","mussë");
		hashwords.put("SOMEBODY","mo");
		hashwords.put("SOUP","salpa");
		hashwords.put("SPRING","celwë");
		hashwords.put("SPLINTER","sacillë");
		hashwords.put("SPY","ettirno");
		hashwords.put("STAB","ehta");
		hashwords.put("STAFF","vandil");
		hashwords.put("STALWART","talya");
		hashwords.put("STAND","tyulya");
		hashwords.put("STARTLE","capta");
		hashwords.put("STATE","indo");
		hashwords.put("STAY","norta");
		hashwords.put("STEADY","tulunca");
		hashwords.put("STIFFNESS","taryassë");
		hashwords.put("STRAY","mistana");
		hashwords.put("STREET","ratta");
		hashwords.put("STRENGTHEN","antorya");
		hashwords.put("STRENGTHENING","antoryamë");
		hashwords.put("STRIDE","telconta");
		hashwords.put("STRIPPED","racinë");
		hashwords.put("STUDY","cenya");
		hashwords.put("SUCCESSION","hilyalë");
		hashwords.put("SUIT","camta");
		hashwords.put("SUPERIOR","orohalla");
		hashwords.put("SURGE","solto");
		hashwords.put("SURVIVE","vor");
		hashwords.put("SWARD","paswa");
		hashwords.put("SYMPATHY","ófelmë");
		hashwords.put("TABLE","paluhta");
		hashwords.put("TALL","ancassëa");
		hashwords.put("TAMBOURINE","rambil");
		hashwords.put("TASK","tarassë");
		hashwords.put("TEACH","saita");
		hashwords.put("TEMPTATION","úsahtië");
		hashwords.put("TENTH","quainëa");
		hashwords.put("THAT","sa");
		hashwords.put("THICK","sangwa");
		hashwords.put("THIN","linya");
		hashwords.put("THING","engwë");
		hashwords.put("THINK","sana");
		hashwords.put("THIRD","neldëa");
		hashwords.put("THIRSTY","soica");
		hashwords.put("THITHER","tanna");
		hashwords.put("THOUGHT","sanwë");
		hashwords.put("THREAD","lanya");
		hashwords.put("THRUST","nir");
		hashwords.put("THUS","sië");
		hashwords.put("TIDE","sóla");
		hashwords.put("TIGHT","sangwa");
		hashwords.put("TODAY","sinaurë");
		hashwords.put("TOMB","sarca");
		hashwords.put("TOMORROW","entaurë");
		hashwords.put("TOUCH","appa");
		hashwords.put("TOUCHING","apa");
		hashwords.put("TOUGH","sangwa");
		hashwords.put("TOUGHNESS","taryassë");
		hashwords.put("TRACK","vata");
		hashwords.put("TRAMPLE","vatta");
		hashwords.put("TRAP","remba");
		hashwords.put("TRAVERSE","tervanta");
		hashwords.put("TRESPASS","naicë");
		hashwords.put("TRESSURE","carrëa");
		hashwords.put("TRINITY","neldië");
		hashwords.put("TROLL","torco");
		hashwords.put("TROUBLE","tarasta");
		hashwords.put("THROW","hat");
		hashwords.put("TURRET","mindë");
		hashwords.put("TWELFTH","yunquë");
		hashwords.put("UNCOUNTED","únótëa");
		hashwords.put("UNICORN","eretildo");
		hashwords.put("UNITE","erta");
		hashwords.put("UNTIL","mennai");
		hashwords.put("UNWILL","avanir");
		hashwords.put("UNWISE","alasaila");
		hashwords.put("URGE","or");
		hashwords.put("URGENCY","sangië");
		hashwords.put("USE","yuhta");
		hashwords.put("USEFULLNESS","yungwë");
		hashwords.put("USED","yunca");
		hashwords.put("VALOUR","astal");
		hashwords.put("VASSAL","neuro");
		hashwords.put("VAST","taura");
		hashwords.put("VEIL","vasar");
		hashwords.put("VENGEANCE","atacarmë");
		hashwords.put("VERDIGRIS","lairus");
		hashwords.put("VICTORY","nangwë");
		hashwords.put("VICTOR","nacil");
		hashwords.put("VIOLENT","naraca");
		hashwords.put("VOWEL","óma-tengwë");
		hashwords.put("WAIN","lunca");
		hashwords.put("WAIT","larta");
		hashwords.put("WAKE","eccuita");
		hashwords.put("WANDER","ramya");
		hashwords.put("WANDERER","ranyar");
		hashwords.put("WANDERING","ranya");
		hashwords.put("WARE","maca");
		hashwords.put("WARM","lauta");
		hashwords.put("WAS","engë");
		hashwords.put("WATERFALL","lantasírë");
		hashwords.put("WATCH","cenda");
		hashwords.put("WAVE","solmë");
		hashwords.put("WHENCE","yallo");
		hashwords.put("WHERE","massë");
		hashwords.put("WHEREBY","yanen");
		hashwords.put("WHERETO","yanna");
		hashwords.put("WHITHER","manna");
		hashwords.put("WHOSE","yava");
		hashwords.put("WHY","manan");
		hashwords.put("WILL","mendë");
		hashwords.put("WINDOW","henet");
		hashwords.put("WINDOWS","henetsi");
		hashwords.put("WINE","miruva");
		hashwords.put("WINY","míruva");
		hashwords.put("WING","ramna");
		hashwords.put("WISE","saila");
		hashwords.put("WITH","as");
		hashwords.put("WITHOUT","ú");
		hashwords.put("WOMB","móna");
		hashwords.put("WOOD","toa");
		hashwords.put("WRITER","tecindo");
		hashwords.put("WRITING","sarmë");
		hashwords.put("WRONG","raicë");
		hashwords.put("YES","yé");
		hashwords.put("YESTERDAY","tellaurë");
		return hashwords;
		}
	}
