package fr.VMiner.PCMBot;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.ucla.sspace.basis.StringBasisMapping;
import edu.ucla.sspace.common.Similarity;
import edu.ucla.sspace.lsa.LatentSemanticAnalysis;
import edu.ucla.sspace.matrix.NoTransform;
import edu.ucla.sspace.matrix.factorization.SingularValueDecompositionLibC;

import foreverse.ksynthesis.Heuristic;
import foreverse.ksynthesis.KSynthesisPlugin;
import foreverse.ksynthesis.SimpleHeuristic;

import foreverse.ksynthesis.metrics.SmithWatermanMetric;
import foreverse.ksynthesis.metrics.LevenshteinMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWaterman;

public class NCMetric {
	protected Levenshtein levenshtein;
	protected SmithWaterman smithWaterman;
	LatentSemanticAnalysis lsa;
	final String FRANCE_REQ = "cnxUseCase//France";
	final Integer freqThreshold = 1;
	final Integer maxLength = 20;
	final double cValThreshold = 1.5;
	List<Pattern> patternsList = new ArrayList<Pattern>();
	List<Integer> lengthList = new ArrayList<Integer>();
	// List<Integer> lengthList = new ArrayList<Integer>();
	// List<String> candidStrings = new ArrayList<String>();
	List<String> badStrings = new ArrayList<String>();
	Map<String, Candid> candidMap = new HashMap<String, Candid>();
	Map<String, ContextWord> contextMap = new HashMap<String, ContextWord>();
	Map<String, Double> topCValMap = new HashMap<String, Double>();
	Map<String, Double> topNCValMap = new HashMap<String, Double>();
	Map<Integer, HashMap<String, Integer>> mapByLen = new HashMap<Integer, HashMap<String, Integer>>();
	Map<FeatureCouple, Double> similarityMap = new HashMap<FeatureCouple, Double>();
	List<String> candidValList = new ArrayList<String>();
	List<String> candidTextList = new ArrayList<String>();
	// (Noun)+ Noun
	Pattern pattern0 = Pattern
			.compile("(([a-zA-Z0-9-]+)(/NN|/NNS|/NNP|/NNPS|/VBG)\\s+)+[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS)");

	// (Adj|Noun)+ Noun
	Pattern pattern1 = Pattern
			.compile("([a-zA-Z0-9-.]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)+[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS)");

	// (Adj|Noun)+ prepo (of|between|within|in)(Adj|Noun)+
		Pattern pattern2 = Pattern
				.compile("(([a-zA-Z0-9,.-]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)*([a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS|/VBG)\\s+))((of|between|within|in)(/IN))\\s+([a-zA-Z]+/DT\\s+)?([a-zA-Z0-9,.-]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)*([a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS|/VBG))");
		
	// Nb+ (adj|Noun)+
	Pattern pattern3 = Pattern
			.compile("[a-zA-Z0-9,.-]+(/CD)(\\s+(x/SYM)\\s+[a-zA-Z0-9-,.]+(/CD))*(\\s+([a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)*[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS))");
	Pattern pattern4 = Pattern
			.compile("[a-zA-Z0-9,.]+(/CD)((\\s+((%|°)/NN))|(\\s+(x/SYM)|\\s+(''/'')))(\\s+([a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)*[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS))");
	
	Pattern pattern = Pattern
			.compile("[a-zA-Z0-9,.-]+(/CD)\\s+([a-zA-Z0-9-.]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)*[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS)\\s+[a-zA-Z0-9,.-]+(/CD)\\s+([a-zA-Z0-9-.]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)*[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS)");

	// Nb ?(Adj|Noun)+ prepo (of|between|within|in) Nb ? (Adj|Noun)+
	Pattern pattern5 = Pattern
			.compile("(([a-zA-Z0-9,.-]+(/CD)((%|'')/SYM)?\\s+)?([a-zA-Z0-9,.-]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)*([a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS|/VBG)\\s+))((of|between|within|in)(/IN))\\s+([a-zA-Z]+/DT\\s+)?([a-zA-Z0-9,.-]+(/CD)((%|'')/SYM)?\\s+)?([a-zA-Z0-9,.-]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)*([a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS|/VBG))");

	// (adj|Noun)+ "at?(up|down)" to Nb Noun+
	Pattern pattern6 = Pattern
			.compile("([a-zA-Z0-9-.]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)+[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS)((\\s+(at/IN))?\\s+((up|down)/IN)\\s+(to/TO)\\s+)[a-zA-Z0-9,.-]+(/CD)(\\s+(x/SYM)\\s+[a-zA-Z0-9-,.]+(/CD))*(\\s+([a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)*[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS))");

	// Noun "expandable to" Nb
	/*Pattern pattern9 = Pattern
			.compile("([a-zA-Z0-9-.]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)*[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS)\\s+[a-zA-Z0-9,.-]+(/CD)((\\s+((%|�)/NN))|(\\s+(x/SYM)|\\s+(''/'')))?(\\s+[a-zA-Z0-9,.-]+(/CD)((\\s+((%|�)/NN))|(\\s+(x/SYM)|\\s+(''/'')))?)*(\\s+(expandable/JJ)\\s+(to/TO)\\s+)[a-zA-Z0-9,.-]+(/CD)((\\s+((%|�)/NN))|(\\s+(x/SYM)|\\s+(''/'')))?");
*/
	//Noun+ Nb
	Pattern pattern7 = Pattern
			.compile("([a-zA-Z0-9-.]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)*[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS)\\s+[a-zA-Z0-9,.-]+(/CD)");

	// ( Adj( ,|and))* adj Noun
	Pattern pattern8 = Pattern
			.compile("(([a-zA-Z0-9-]+(JJ|/JJR|/RB|/VBN|/VBG)\\s+)((,/,)|(and/CC))\\s+)*([a-zA-Z0-9-]+(JJ|/JJR|/RB|/VBN|/VBG)\\s+)([a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS|/CD)((%|'')/SYM)?\\s+)+([a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS)\\s+)+[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS)");

	// Noun "include" Noun+
	Pattern pattern9 = Pattern
			.compile("([a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)+[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS)(include/VB)((([a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG|/CD)((%|'')/SYM)?\\s+)*([a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS)\\s+))((,/,)|(and/CC))\\s+)*(([a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG|/CD)((%|'')/SYM)?\\s+)*([a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS)\\s+))");
	//Foreign words
	Pattern pattern10 = Pattern
			.compile("[a-zA-Z0-9-.]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG|/CD|/FW)\\s+(([a-zA-Z0-9-.]+(\\\\)(\\/)[a-zA-Z0-9-.]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG|/CD|/FW)|[a-zA-Z0-9-.]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG|/CD|/FW)|(--/:))\\s+)+[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS)");
	
	
	Pattern pattern31 = Pattern
			.compile("[a-zA-Z0-9,.-]+(/CD)(\\s+((--/:|,/,|./.)\\s+)?[a-zA-Z0-9,.-]+(/CD))*(\\s+(x/SYM)\\s+[a-zA-Z0-9-,.]+(/CD)(\\s+((--/:|,/,|./.)\\s+)?[a-zA-Z0-9,.-]+(/CD))*)*(\\s+([a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)*[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS))");
	
	Pattern pattern41 = Pattern
			.compile("[a-zA-Z0-9,.]+(/CD)(\\s+((--/:|,/,|./.)\\s+)?[a-zA-Z0-9,.-]+(/CD))*((\\s+((%|°)/NN))|(\\s+(x/SYM)|\\s+(''/'')))(\\s+([a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)*[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS))");
	Pattern pattern51 = Pattern
			.compile("(([a-zA-Z0-9,.-]+(/CD)(\\s+((--/:|,/,|./.)\\s+)?[a-zA-Z0-9,.-]+(/CD))*((%|'')/SYM)?\\s+)?([a-zA-Z0-9,.-]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)*([a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS|/VBG)\\s+))((of|between|within|in)(/IN))\\s+([a-zA-Z]+/DT\\s+)?([a-zA-Z0-9,.-]+(/CD)(\\s+((--/:|,/,|./.)\\s+)?[a-zA-Z0-9,.-]+(/CD))*((%|'')/SYM)?\\s+)?([a-zA-Z0-9,.-]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)*([a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS|/VBG))");

	// (adj|Noun)+ "at?(up|down)" to Nb Noun+
	Pattern pattern61 = Pattern
			.compile("([a-zA-Z0-9-.]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)+[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS)((\\s+(at/IN))?\\s+((up|down)/IN)\\s+(to/TO)\\s+)[a-zA-Z0-9,.-]+(/CD)(\\s+((--/:|,/,|./.)\\s+)?[a-zA-Z0-9,.-]+(/CD))*(\\s+(x/SYM)\\s+[a-zA-Z0-9-,.]+(/CD)(\\s+((--/:|,/,|./.)\\s+)?[a-zA-Z0-9,.-]+(/CD))*)*(\\s+([a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)*[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS))");
	
	Pattern pattern71 = Pattern
			.compile("([a-zA-Z0-9-.]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)*[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS)\\s+[a-zA-Z0-9,.-]+(/CD)(\\s+((--/:|,/,|./.)\\s+)?[a-zA-Z0-9,.-]+(/CD))* ");
	
	public void filter(String fileName) throws Exception {

		String candidName, candidTag, candidSW;
		Candid candid;
		Integer length = 0, freq;
		StopWords stopwords = new StopWords();
		String[] patterns = { "/NNPS", "/NNP", "/NNS", "/NN", "/JJR", "/JJ",
				"/DT", "/IN", "/VBN", "/VBG", "/VBP", "/CD", "/RB", "/SYM",
				"/''", "/TO" , "/FW","/,", "/.","-/:","--/", "\\\\"};

	/*	patternsList.add(pattern0);
		patternsList.add(pattern1);
		patternsList.add(pattern2);
		patternsList.add(pattern3);
		patternsList.add(pattern4);
		patternsList.add(pattern5);
		patternsList.add(pattern6);
		patternsList.add(pattern7);
		patternsList.add(pattern10);*/
		
		patternsList.add(pattern0);
		patternsList.add(pattern1);
		patternsList.add(pattern2);
		patternsList.add(pattern31);
		patternsList.add(pattern41);
		patternsList.add(pattern51);
		patternsList.add(pattern61);
		patternsList.add(pattern71);
		//patternsList.add(pattern10);
		
		MaxentTagger tagger = new MaxentTagger(
				"taggers/bidirectional-distsim-wsj-0-18.tagger");

		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			StringBuilder sb = new StringBuilder();
			String line;
			Pattern pluralPattern = Pattern.compile("[a-zA-Z0-9,.-]+(/NNS)");
			
			while ((line = br.readLine()) != null) {
				System.out
						.println("**************************************New Line***********************************");

				String taggedLine = tagger.tagString(line
						.replaceAll("[()]", ""));
				System.out.println(taggedLine + "\n");
				List<String> candidList = new ArrayList<String>();
				
				for (Pattern p : patternsList) {
					Matcher matcher = p.matcher(taggedLine);
					while (matcher.find()) {
						candidName = matcher.group();
						candidTag = candidName;
						System.out.println("plural candidTag:"+ candidTag + "\n");
						if(candidTag.contains("/NNS")){
							Matcher pluralMatcher = pluralPattern.matcher(candidTag);
							while (pluralMatcher.find()) {
								String taggedPlural = pluralMatcher.group();
								String plural = taggedPlural.replace("/NNS", "");
								String	taggedSingular = convertToSingular(plural);
								candidTag = candidTag.replace(taggedPlural, taggedSingular);
							}
						}
						System.out.println("Singular candidTag:"+ candidTag + "\n");
						candidName = candidTag;
						// delete tag from candidate name
						for (int i = 0; i < patterns.length; i++) {
							candidName = candidName.replaceAll(patterns[i], "");
						}
						//System.out.println(candidName + "\n");
						candidName = candidName.toLowerCase();
						//itemSet = itemSet.replace(",", ".");
						candidName = candidName.replace(",", ".");
						candidName = candidName.replaceAll("(\\s+)?--(\\s+)?", "-");
						candidName = candidName.replace("°", "");
						candidName = candidName.replace("'", "");
						System.out.println(candidName + "\n");
						/*
						 * if (p.equals(pattern0)) { candidTag =
						 * candidTag.trim(); candidName = candidName.trim(); if
						 * (candidTag.contains("/NNS")) { candidName =
						 * convertToSingular(candidName); }
						 * candidList.add(candidName); } else {
						 */
					
						if (!candidList.contains(candidName)) {
							candidList.add(candidName);
							if (!p.equals(pattern0) && !p.equals(pattern1)&& !p.equals(pattern2)&& !p.equals(pattern51)){
								if (!candidValList.contains(candidName)) {
									candidValList.add(candidName);
								}
							}
							if (p.equals(pattern0) || p.equals(pattern1)){
								if (!candidTextList.contains(candidName)) {
									candidTextList.add(candidName);
								}
							}
							// }
						}
					}
					System.out.println("candid List"+ candidList + "\n");
					System.out.println("candid Val List"+ candidValList + "\n");
					System.out.println("candid Text List"+ candidTextList + "\n");
				}
				// add a candidate if length < = maxLength
				for (String str : candidList) {
					System.out.println(str);
					if (!candidMap.containsKey(str)) {
						candidSW = stopwords.removeStopWords(str);
						length = candidSW.split(" ").length;
						// System.out.println(length);
						/*
						 * System.out.println("Name :" + candidName + "\n" +
						 * "Name without Stop words: " + candidSW + " " + length
						 * + "\n\n");
						 */
						if (length <= maxLength) {
							candid = new Candid(candidSW, length);
							candid.getTriple().setFreq(1);
							candidMap.put(str, candid);
							/*
							 * System.out.println("frequence : " +
							 * candidMap.get(str).getTriple().getFreq() + "\n");
							 */
						}

					} else {
						candidMap.get(str).getTriple().increFreq();
						/*
						 * System.out.println("frequence : " +
						 * candidMap.get(str).getTriple().getFreq() + "\n");
						 */
					}

				}
				System.out.println("cand List final: " + candidList + "\n");
				System.out.println(candidMap.keySet());

				candidList.clear();
				System.out.println("cand List final: " + candidList + "\n");

			}
		} finally {
			br.close();
		}
		System.out
				.println("**************************************before filter***********************************");
		// System.out.println(candidStrings);
		// System.out.println(candidStrings.size());
		System.out.println(candidMap);
		System.out.println(candidMap.size());
		int len;
		Set keys = candidMap.keySet();
		Iterator it = keys.iterator();
		String key;
		while (it.hasNext()) {
			key = (String) it.next();
			if (candidMap.get(key).getTriple().getFreq() < freqThreshold) {
				badStrings.add(key);

			} else {
				len = candidMap.get(key).getLen();
				if (!lengthList.contains(len)) {
					lengthList.add(len);
				}
			}
		}
		for (String s : badStrings) {
			candidMap.remove(s);
			// candidStrings.remove(s);
		}
		System.out
				.println("**************************************after filter***********************************");
		// System.out.println(candidStrings);
		// System.out.println(candidStrings.size());
		Set keys2 = candidMap.keySet();
		System.out.println(keys2);
		System.out.println(candidMap);
		System.out.println(candidMap.size());

	}

	public String convertToSingular(String str) {
		if (str.length() > 0 && str.charAt(str.length() - 1) == 's') {
			str = str.substring(0, str.length() - 1);
		}
		return str+"/NN";
	}

	public void filterByLenght() {
		HashMap<String, Integer> candidLenMap;
		Set keys = candidMap.keySet();
		String key;
		System.out
				.println("**************************************a list for each length***********************************");
		for (Integer l : lengthList) {
			candidLenMap = new HashMap<String, Integer>();
			Iterator it = keys.iterator();
			while (it.hasNext()) {
				key = (String) it.next();
				if (candidMap.get(key).getLen() == l) {
					candidLenMap.put(key, candidMap.get(key).getTriple()
							.getFreq());
				}

			}
			System.out.println("List for length =" + l + ":" + candidLenMap);
			mapByLen.put(l, candidLenMap);

		}
		System.out
				.println("**************************************the parent list***********************************");
		System.out.println(mapByLen);
		System.out.println(mapByLen.size());

	}

	public void computeMaxLenCVal() {
		System.out
				.println("**************************************compute Max Len CVal***********************************");
		int max = Collections.max(lengthList);
		System.out.println("Max len =" + max);
		HashMap<String, Integer> maxStrings = mapByLen.get(max);
		Integer freqLonger, freqLongerNested;
		Set keys = maxStrings.keySet();
		Iterator it = keys.iterator();
		String key;
		while (it.hasNext()) {
			key = (String) it.next();
			// System.out.println(key);

			candidMap.get(key).setCValueMaxLen();
			// System.out.println("CValue: " + candidMap.get(key).getcVal());

			freqLonger = candidMap.get(key).getTriple().getFreq();
			// System.out.println("freqLonger: " + freqLonger);

			freqLongerNested = candidMap.get(key).getTriple().getFreqNested();
			// System.out.println("freqLongerNested: " + freqLongerNested);

			reviseAllSubStr(key, freqLonger, freqLongerNested, max - 1);
			computeCVal(max - 1);

			/*
			 * System.out.println(
			 * "**************************************MaxLenCValues***********************************************"
			 * ); System.out.println(candidMap);
			 */
			// computeSubStringsCVal(key, freqLonger);

		}

	}

	public void reviseAllSubStr(String longerStr, Integer freqLonger,
			Integer freqLongerNested, int max) {
		// max>0
		while (max >= 1) {
			if (mapByLen.containsKey(max)) {
				HashMap<String, Integer> subStrByLen = mapByLen.get(max);
				Set keys = subStrByLen.keySet();
				Iterator it = keys.iterator();
				String key;

				while (it.hasNext()) {
					key = (String) it.next();
					/*
					 * System.out.println("\t\t\t" + key);
					 * System.out.println("\t\t\t" + "freqNested avant: " +
					 * candidMap.get(key).getTriple().getFreqNested());
					 * System.out.println("\t\t\t" + "freqLonger avant: " +
					 * candidMap.get(key).getTriple().getLongerNb());
					 */
					if (longerStr.toLowerCase().contains(key.toLowerCase())) {

						candidMap.get(key).getTriple()
								.increFreqNested(freqLonger, freqLongerNested);
						/*
						 * System.out.println("\t\t\t" + "freqNested: " +
						 * candidMap.get(key).getTriple() .getFreqNested());
						 */

						candidMap.get(key).getTriple().increLongerNb();
						/*
						 * System.out.println("\t\t\t" + "freqLonger: " +
						 * candidMap.get(key).getTriple().getLongerNb());
						 */
					}
				}
				max--;
			} else {
				max--;
			}
		}

	}

	public void computeCVal(int max) {
		System.out
				.println("**************************************compute CVal***********************************");
		while (max >= 1) {
			if (mapByLen.containsKey(max)) {
				HashMap<String, Integer> subStrByLen = mapByLen.get(max);
				Set keys = subStrByLen.keySet();
				Iterator it = keys.iterator();
				String key;
				Integer freqLonger, freqLongerNested;
				while (it.hasNext()) {
					key = (String) it.next();
					/*
					 * System.out.println("\t" + key);
					 * System.out.println("\tlen: " +
					 * candidMap.get(key).getLen());
					 * System.out.println("\tfreq: " +
					 * candidMap.get(key).getTriple().getFreq());
					 */
					if (candidMap.get(key).getTriple().getLongerNb() == 0) {
						/*
						 * System.out.println(key +
						 * "**********is not nested**********");
						 */
						candidMap.get(key).setCValueMaxLen();
					} else {
						/*
						 * System.out.println(key +
						 * "*************is nested************");
						 * System.out.println("\tfreqNested: " +
						 * candidMap.get(key).getTriple() .getFreqNested());
						 * System.out.println("\tlongerNb: " +
						 * candidMap.get(key).getTriple().getLongerNb());
						 */
						candidMap.get(key).setCValue();
					}

					/*
					 * System.out.println("\t\t" + "CVal: " +
					 * candidMap.get(key).getcVal());
					 */
					freqLonger = candidMap.get(key).getTriple().getFreq();
					freqLongerNested = candidMap.get(key).getTriple()
							.getFreqNested();
					/*
					 * System.out.println("\t\t" + "freqLonger: " + freqLonger);
					 * System.out.println("\t\t" + "freqLongerNested: " +
					 * freqLongerNested);
					 */
					reviseAllSubStr(key, freqLonger, freqLongerNested, max - 1);

				}
				max--;
			} else {
				max--;
			}
		}
	}

	public void filterTopCval() {

		Set keys = candidMap.keySet();
		Iterator it = keys.iterator();
		String key;
		Double cval;
		while (it.hasNext()) {
			key = (String) it.next();
			cval = candidMap.get(key).getcVal();
			if (cval >= cValThreshold) {
				topCValMap.put(key, cval);
			}
		}
		ValueComparator bvc = new ValueComparator(topCValMap);
		TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);
		System.out.println("unsorted map: " + topCValMap);
		sorted_map.putAll(topCValMap);
		System.out.println("results: " + sorted_map);
	}

	public void filterTopNCval() {

		Set keys = candidMap.keySet();
		Iterator it = keys.iterator();
		String key;
		Double cval;
		while (it.hasNext()) {
			key = (String) it.next();
			topNCValMap.put(key, candidMap.get(key).getNcVal());
		}

		ValueComparator bvc = new ValueComparator(topNCValMap);
		TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);
		System.out.println("unsorted map: " + topNCValMap);
		sorted_map.putAll(topCValMap);
		System.out.println("results: " + sorted_map);
	}

	public void extractContextWord() {
		Set keys = topCValMap.keySet();
		Iterator it = keys.iterator();
		String t, w;
		while (it.hasNext()) {
			t = (String) it.next();
			String[] subWords = t.split(" ");
			for (int i = 0; i < subWords.length; i++) {
				w = subWords[i];
				ContextWord contextWord = new ContextWord();
				if (!contextMap.containsKey(w)) {
					contextMap.put(w, contextWord);
				}

			}
		}
	}

	public void computeWeights() {

		Set<String> words = contextMap.keySet();
		Set<String> terms = topCValMap.keySet();
		double weight;

		int i = 0;
		for (String w : words) {
			for (String t : terms) {
				if (t.toLowerCase().contains(w.toLowerCase())) {
					i++;
				}
			}
			weight = (double) i / (topCValMap.size());
			contextMap.get(w).setWeight(weight);
			contextMap.get(w).setNbTerms(i);
			i = 0;
		}

		/*
		 * while (it_w.hasNext()) { w = (String) it_w.next(); while
		 * (it_t.hasNext()) { t = (String) it_t.next(); if
		 * (t.toLowerCase().contains(w.toLowerCase())) { i++; } } weight =
		 * (double) i / (topCValMap.size());
		 * contextMap.get(w).setWeight(weight); i = 0; it_t = terms.iterator();
		 * }
		 */
		System.out.println("taille termes:" + topCValMap.size());
	}

	public void computeNCVal() {

		Set<String> words = contextMap.keySet();
		Set<String> terms = topCValMap.keySet();
		double weight;
		double freqContext;
		Candid candid;

		for (String t : terms) {
			candid = candidMap.get(t);
			/*
			 * System.out.println(t); System.out.println("NVal: " +
			 * candid.getnVal());
			 */
			for (String w : words) {
				// System.out.println(w);
				if (t.toLowerCase().contains(w.toLowerCase())) {
					freqContext = (double) StringUtils.countMatches(t, w)
							/ (double) candid.getLen();
					weight = contextMap.get(w).getWeight();
					candid.increNVal(freqContext, weight);
					/*
					 * System.out.println("freqContext: " + freqContext +
					 * " weight: " + weight); System.out.println("NVal: " +
					 * candid.getnVal());
					 */
				}
			}
			candidMap.get(t).setNcVal();
		}

		for (Iterator<Map.Entry<String, Candid>> it = candidMap.entrySet()
				.iterator(); it.hasNext();) {
			Map.Entry<String, Candid> x = it.next();
			String str = x.getKey();
			if (!topCValMap.containsKey(str)) {
				it.remove();
				System.out.println("Key : " + str + " Removed.");
			}
		}

	}

	private Map sortMap(Map aMap) {
		Map myMap = new HashMap();
		TreeSet set = new TreeSet(new Comparator() {
			public int compare(Object obj, Object obj1) {
				Double val1 = (Double) ((Map.Entry) obj).getValue();
				Double val2 = (Double) ((Map.Entry) obj1).getValue();
				return val1.compareTo(val2);
			}
		});

		set.addAll(aMap.entrySet());

		for (Iterator it = set.iterator(); it.hasNext();) {
			Map.Entry myMapEntry = (Map.Entry) it.next();
			myMap.put(myMapEntry.getKey(), myMapEntry.getValue());
		}

		return myMap;
	}

	/*
	 * public void computeSubStringsCVal(String longer, Integer freqLonger, int
	 * max) { HashMap<String, Integer> subStrings; Set keys; String key;
	 * Iterator it; max=max-1;
	 * 
	 * while (max > 0) {
	 * 
	 * subStrings = mapByLen.get(max); keys = subStrings.keySet(); it =
	 * keys.iterator(); while (it.hasNext()) { key = (String) it.next(); if
	 * (longer.toLowerCase().contains(key.toLowerCase())) {
	 * candidMap.get(key).getTriple().increFreqNested(freqLonger);
	 * candidMap.get(key).getTriple().increLongerNb(); } else {
	 * //System.out.println("it does not contain"); } } max--; } }
	 */
	public String readFile(String fileName) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}

	public void clusterFeaturesLSA() throws IOException {
		System.out.println("\n***********LSA*******\n");
		Set<String> featureKeys = topNCValMap.keySet();
		Iterator<String> it = featureKeys.iterator();
		String featureKey;
		// cluster tempDescrips
		System.out.println(topNCValMap.size());
		lsa = new LatentSemanticAnalysis(true, 115, new NoTransform(),
				new SingularValueDecompositionLibC(), false,
				new StringBasisMapping());
		while (it.hasNext()) {
			featureKey = it.next();
			lsa.processDocument(new BufferedReader(new StringReader(featureKey
					.toLowerCase())));
		}
		lsa.processSpace(System.getProperties());
		Object[] array = featureKeys.toArray();

		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array.length; j++) {
				String source = array[i].toString();
				String target = array[j].toString();
				if (i != j) {
					FeatureCouple coupleKey = new FeatureCouple(source, target);
					if ((!similarityMap.containsKey(coupleKey))
							&& (coupleKey.existKeyReverse(similarityMap) == false)) {
						similarityMap.put(coupleKey, Similarity.getSimilarity(
								Similarity.SimType.COSINE,
								lsa.getDocumentVector(i),
								lsa.getDocumentVector(j)));

					}
				}
			}
		}

		Set listKeys = similarityMap.keySet();
		Iterator iterator = listKeys.iterator();
		double sim;
		String source;
		String target;
		FeatureCouple key;
		// Parcourir les cl�s et afficher les entr�es de chaque cl�;

		System.out
				.println("\n *****************************Sim(Ri,Rj) >= 0.30********************************** \n");
		while (iterator.hasNext())

		{

			key = (FeatureCouple) iterator.next();
			sim = similarityMap.get(key);
			if (sim > 0.30) {
				source = key.getFeatureSource();
				target = key.getFeatureTarget();

				System.out.println(key.getFeatureSource());
				System.out.println(key.getFeatureTarget());
				System.out.println("COSINE" + key.toString() + " = "
						+ similarityMap.get(key) + "\n");
			}

		}

	}

	public void clusterFeaturesSmithWaterman() throws IOException {
		System.out.println("\n ***********SmithWaterman*******\n");
		smithWaterman = new SmithWaterman();
		Set<String> featureKeys = topNCValMap.keySet();
		System.out.println(featureKeys.size());
		Object[] array = featureKeys.toArray();

		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array.length; j++) {
				String source = array[i].toString();
				String target = array[j].toString();
				if (i != j) {
					FeatureCouple coupleKey = new FeatureCouple(source, target);
					if ((!similarityMap.containsKey(coupleKey))
							&& (coupleKey.existKeyReverse(similarityMap) == false)) {
						// similarityMap.put(coupleKey, (double)
						// smithWaterman.getSimilarity(source, target));

					}
				}
			}
		}

		Set listKeys = similarityMap.keySet();
		Iterator iterator = listKeys.iterator();
		double sim;
		String source;
		String target;
		FeatureCouple key;
		// Parcourir les cl�s et afficher les entr�es de chaque cl�;

		System.out
				.println("\n *****************************Sim(Ri,Rj) >= 0.30********************************** \n");
		while (iterator.hasNext())

		{

			key = (FeatureCouple) iterator.next();
			sim = similarityMap.get(key);
			if (sim > 0.30) {
				source = key.getFeatureSource();
				target = key.getFeatureTarget();

				System.out.println(key.getFeatureSource());
				System.out.println(key.getFeatureTarget());
				System.out.println("SmithWaterman" + key.toString() + " = "
						+ similarityMap.get(key) + "\n");
			}

		}

	}

	public void clusterFeaturesLevenshtein() throws IOException {
		System.out.println("\n ***********Levenshtein*******\n");
		levenshtein = new Levenshtein();
		Set<String> featureKeys = topNCValMap.keySet();
		System.out.println(featureKeys.size());
		Object[] array = featureKeys.toArray();

		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array.length; j++) {
				String source = array[i].toString();
				String target = array[j].toString();
				if (i != j) {
					FeatureCouple coupleKey = new FeatureCouple(source, target);
					if ((!similarityMap.containsKey(coupleKey))
							&& (coupleKey.existKeyReverse(similarityMap) == false)) {
						similarityMap.put(coupleKey, (double) levenshtein
								.getSimilarity(source, target));

					}
				}
			}
		}

		Set listKeys = similarityMap.keySet();
		Iterator iterator = listKeys.iterator();
		double sim;
		String source;
		String target;
		FeatureCouple key;
		// Parcourir les cl�s et afficher les entr�es de chaque cl�;

		System.out
				.println("\n *****************************Sim(Ri,Rj) >= 0.40********************************** \n");
		while (iterator.hasNext())

		{

			key = (FeatureCouple) iterator.next();
			sim = similarityMap.get(key);
			if (sim > 0.30) {
				source = key.getFeatureSource();
				target = key.getFeatureTarget();

				System.out.println(key.getFeatureSource());
				System.out.println(key.getFeatureTarget());
				System.out.println("Levenshtein" + key.toString() + " = "
						+ similarityMap.get(key) + "\n");
			}

		}

	}

	public void tagFile(String fileName) throws IOException,
			ClassNotFoundException {
		String[] patterns = { "/NNPS", "/NNP", "/NNS", "/NN", "/JJR", "/JJ",
				"/DT", "/IN", "/VBN", "/VBG", "/VBP", "/CD" };

		/*
		 * patternsList.add(pattern1); patternsList.add(pattern2);
		 * 
		 * patternsList.add(pattern3); patternsList.add(pattern4);
		 * patternsList.add(pattern0);
		 */

		MaxentTagger tagger = new MaxentTagger(
				"taggers/bidirectional-distsim-wsj-0-18.tagger");

		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				System.out.println(line + "\n");
				String taggedLine = tagger.tagString(line);
				System.out.println(taggedLine + "\n");
				for (Pattern p : patternsList) {
					Matcher matcher = p.matcher(taggedLine);
					while (matcher.find()) {
						String candidName = matcher.group();
						System.out.println(candidName + "\n");
					}
				}
				line = br.readLine();
			}

		} finally {
			br.close();
		}
	}

	public Map<String, Double> genFeatures(String file) throws Exception {
		mapByLen.clear();
		candidMap.clear();
		contextMap.clear();
		topNCValMap.clear();
		String text = readFile(file);
		System.out.println(text);
		filter(file);
		filterByLenght();
		System.out.println(mapByLen);
		System.out.println(candidMap);
		computeMaxLenCVal();
		System.out
				.println("****************************Calcul CValue************************");
		System.out.println(candidMap);
		filterTopCval();
		extractContextWord();
		computeWeights();
		computeNCVal();
		System.out
				.println("\n****************************Calcul NCValue************************\n");
		System.out.println(contextMap);
		System.out.println(contextMap.size());
		System.out.println(candidMap);
		System.out.println(candidMap.size());
		Set keys = candidMap.keySet();
		System.out.println("candid map: " + keys);
		filterTopNCval();
		System.out.println(topNCValMap.size());
		filterTopValFeatures();
		filterTopTextFeatures();
		return topNCValMap;

	}

	
	private void filterTopTextFeatures() {
		
		List<String> tempTextList= new ArrayList<String>();
		Set<String> featureKeys = topNCValMap.keySet();
		
		for(String feature: candidTextList){
			if(featureKeys.contains(feature)){
				tempTextList.add(feature);
			}
		}
		candidTextList.clear();
		candidTextList.addAll(tempTextList);
		
	}

	private void filterTopValFeatures() {
		
		List<String> tempValList= new ArrayList<String>();
		Set<String> featureKeys = topNCValMap.keySet();
		
		for(String feature: candidValList){
			if(featureKeys.contains(feature)){
				tempValList.add(feature);
			}
		}
		candidValList.clear();
		candidValList.addAll(tempValList);
	}

	public List<String> getCandidValList() {
		return candidValList;
	}
	
	public List<String> getCandidTextList() {
		return candidTextList;
	}
	
	/*
	 * public static void main(String args[]) throws Exception {
	 * 
	 * //Camera product line
	 * 
	 * genFeatures("Camera//test"); //genFeatures("Camera//Nikon - D3200 DSLR");
	 * //genFeatures("CameraPL//Nikon - D3300 DSLR");
	 * //genFeatures("CameraPL//Nikon - D7000 DSLR");
	 * //genFeatures("CameraPL//Canon - EOS Rebel T5 DSLR"); }
	 */
}
