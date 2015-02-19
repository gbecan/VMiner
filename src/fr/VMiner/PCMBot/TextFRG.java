package fr.VMiner.PCMBot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern; 
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;
import org.jgrapht.alg.BronKerboschCliqueFinder;
import org.jgrapht.ext.JGraphModelAdapter;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class TextFRG extends JPanel {
	private  final Color DEFAULT_BG_COLOR = Color.decode("#FAFBFF");
	private  final Dimension DEFAULT_SIZE = new Dimension(530, 320);
	private  List<Node> existingVertexs = new ArrayList<Node>();
	private JGraphModelAdapter m_jgAdapter;
	protected JGraph myGraph;
	private  int compteur = 1;
	private  List<Set<Node>> cliques;
	private  Map<String, List<String>> featureTags = new HashMap<String, List<String>>();
	private  Map<String, CustomStringList> featureTrans = new HashMap<String, CustomStringList>();
	private  Map<String, CustomStringList> lemmaFeatTrans = new HashMap<String, CustomStringList>();
	private  Map<String, String> lemmaTextFeatures = new HashMap<String, String>();
	List<Pattern> patternsList = new ArrayList<Pattern>();
	
	// Noun
	   Pattern pattern0 = Pattern.compile("[a-zA-Z0-9-.]+(/NN |/NNS|/FW)");
	
	// (Noun)+ Noun
			Pattern pattern1 = Pattern
					.compile("(([a-zA-Z0-9-.]+)(/NN|/NNS|/FW)\\s+)+[a-zA-Z0-9-.]+(/NN|/NNS|/FW)");
	// Noun
	   Pattern pat0 = Pattern.compile("[a-zA-Z0-9-]+(/NN |/NNS|/NNP )");
		
	// (Noun)+ Noun
		Pattern pat1 = Pattern
				.compile("(([a-zA-Z0-9-]+)(/NN|/NNS|/NNP|/NNPS|/VBG)\\s+)+[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS)");

		// (Adj|Noun)+ Noun
		Pattern pat2 = Pattern
				.compile("([a-zA-Z0-9-.]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)+[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS)");

		// (Adj|Noun)+ Noun
				Pattern pat = Pattern
						.compile("([a-zA-Z0-9-.]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)*[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS)\\s+[a-zA-Z0-9,.-]+(/CD)([a-zA-Z0-9-.]+(/NN|/NNS|/NNP|/NNPS|/JJ|/JJR|/RB|/VBN|/VBG)\\s+)*[a-zA-Z0-9-]+(/NN|/NNS|/NNP|/NNPS)");
		
	public TextFRG(Map<FeatureCouple, Double> similarityTextMap, CustomStringList textFeatures)
			throws IOException, ClassNotFoundException {
	/*	super(new GridLayout(1, 0));
		// create a JGraphT graph
		ListenableUndirectedWeightedGraph myAnalysisGraph = new ListenableUndirectedWeightedGraph(
				Edge.class);
		// create a visualization using JGraph, via an adapter
		m_jgAdapter = new JGraphModelAdapter(myAnalysisGraph);
		JGraph myGraph = new JGraph(m_jgAdapter);

		// add some sample data (graph manipulated via JGraphT)
		Set listKeys = similarityTextMap.keySet();
		Iterator iterator = listKeys.iterator();

		FeatureCouple key;
		while (iterator.hasNext()) {

			key = (FeatureCouple) iterator.next();
			Node source = seekVertex(key.getFeatureSource(), myAnalysisGraph);
			Node destination = seekVertex(key.getFeatureTarget(), myAnalysisGraph);
			myAnalysisGraph.addEdge(source, destination);
			myAnalysisGraph.setEdgeWeight(
					myAnalysisGraph.getEdge(source, destination),
					similarityTextMap.get(key));
			Edge edge = (Edge) myAnalysisGraph.getEdge(source, destination);
			edge.setWeight(similarityTextMap.get(key));

		}

		BronKerboschCliqueFinder finder = new BronKerboschCliqueFinder(
				myAnalysisGraph);
		cliques = (List<Set<Node>>) finder.getAllMaximalCliques();
		 System.out.println("clique1:"+cliques);*/
		// Initialize the tagger
		MaxentTagger tagger = new MaxentTagger(
				"taggers/bidirectional-distsim-wsj-0-18.tagger");

		String[] patterns = { "/NNPS", "/NNP", "/NNS", "/NN", "/JJR", "/JJ",
				"/DT", "/IN", "/VBN", "/VBG", "/VBP", "/CD", "/RB", "/SYM",
				"/''", "/TO" ,"/FW"};
		
		patternsList.add(pattern0);
		patternsList.add(pattern1);
		//patternsList.add(pat2);
		//patternsList.add(pat);
	
		StopWords stopwords = new StopWords();
		
	/*	for (String feature : textFeatures) {
			
			List<String> newFeatureTags = new ArrayList<String>();
			CustomStringList newFeatureTrans = new CustomStringList();
			String taggedFeature = tagger.tagString(feature);
			
			for (Pattern p : patternsList) {
				// System.out.println("\n"+p.toString()+"\n");
				Matcher matcher = p.matcher(taggedFeature);
				String nounGroupsTag;
				String nounGroupsTrans = null;
				int numGrp = 0;
				
				while (matcher.find()) {
					nounGroupsTag = matcher.group();
					// System.out.println(nounGroups + "\n");
					newFeatureTags.add(nounGroupsTag);

					for (int i = 0; i < patterns.length; i++) {
						nounGroupsTrans = nounGroupsTag
								.replaceAll(patterns[i], "");
						nounGroupsTag=nounGroupsTrans;
						System.out.println(nounGroupsTrans);
						
					}
					nounGroupsTrans = nounGroupsTrans.toLowerCase();
					if (p.equals(pat0)) {
						nounGroupsTag = nounGroupsTag.trim();
						nounGroupsTrans = nounGroupsTrans.trim();
						if (nounGroupsTag.contains("/NNS")) {
							nounGroupsTrans = convertToSingular(nounGroupsTrans);
						}
						stopwords.setStopWords("stoplists/en.txt");
						Set<String> stopList = stopwords.getStopWords();
						if (!stopList.contains(nounGroupsTrans)) {
							if (!newFeatureTrans.contains(nounGroupsTrans)) {
								newFeatureTrans.add(nounGroupsTrans);
							}
						}
					} else {
						if (!newFeatureTrans.contains(nounGroupsTrans)) {
							newFeatureTrans.add(nounGroupsTrans);
						}
					}
				

				}
			}

			featureTags.put(feature, newFeatureTags);
			featureTrans.put(feature, newFeatureTrans);

		}*/
		
		Lemmatizer lemmatizer = new Lemmatizer();
		String nounGroupsTag;
		String nounGroupsTrans = null;
		String lemmaTrans,lemmaFeature = null;
		int beginIndex, endIndex ;
		String trans;
		for (String feature : textFeatures) {

			List<String> newFeatureTags = new ArrayList<String>();
			CustomStringList newFeatureTrans = new CustomStringList();
			CustomStringList lemmaFeatureTrans = new CustomStringList(); 
			String taggedFeature = tagger.tagString(feature);
			//System.out.println("tagged features: "+taggedFeature);
			
			for (Pattern p : patternsList) {
				Matcher matcher = p.matcher(taggedFeature);
				while (matcher.find()) {
					nounGroupsTag = matcher.group();
					nounGroupsTag = nounGroupsTag.trim();
					newFeatureTags.add(nounGroupsTag);
					
					for (int i = 0; i < patterns.length; i++) {
						nounGroupsTrans = nounGroupsTag.replaceAll(patterns[i],"");
						nounGroupsTag=nounGroupsTrans;
						System.out.println(nounGroupsTrans);
					}
					System.out.println(nounGroupsTrans);
					nounGroupsTrans = nounGroupsTrans.toLowerCase().trim();
					
					stopwords.setStopWords("stoplists/en.txt");
					Set<String> stopList = stopwords.getStopWords();
					
					if (p.equals(pattern0)){
						if (!stopList.contains(nounGroupsTrans)) {
							if (!newFeatureTrans.contains(nounGroupsTrans)) { 
								newFeatureTrans.add(nounGroupsTrans);
							}
						}
						
					} 
					else {
						if (!newFeatureTrans.contains(nounGroupsTrans)) {
							newFeatureTrans.add(nounGroupsTrans);
						}
						
					}
					
					lemmaTrans = lemmatizer.lemmatizeText(nounGroupsTrans).trim();
					
					if(feature.contains(nounGroupsTrans)){
					trans = nounGroupsTrans;	
					}
					else{
					BreakIterator boundary = BreakIterator.getWordInstance();
					boundary.setText(nounGroupsTrans);
					String firstWord = PCMBotTest.printFirst(boundary, nounGroupsTrans);
					String lastWord = nounGroupsTrans.substring(nounGroupsTrans.lastIndexOf(" ") + 1);
					beginIndex = feature.indexOf(firstWord);
					endIndex = feature.indexOf(lastWord) + lastWord.length();
					trans = feature.substring(beginIndex, endIndex); 
					}
					
					if(lemmaTrans!=null && trans!=null){
					lemmaFeature = feature.replace(trans, lemmaTrans); 
					}
					else
					{
					lemmaFeature =feature;
					lemmaTrans = nounGroupsTrans;
					}
					if (p.equals(pattern0)){
						if (!stopList.contains(lemmaTrans)) {
							if (!lemmaFeatureTrans.contains(lemmaTrans)) {
								lemmaFeatureTrans.add(lemmaTrans);	
							}
						}
						
					}
					else{
						if (!lemmaFeatureTrans.contains(lemmaTrans)) {
							lemmaFeatureTrans.add(lemmaTrans);
						}
					}
					
					
					

				}
			}
			
			lemmaTextFeatures.put(feature, lemmaFeature);
			featureTags.put(feature, newFeatureTags);
			System.out.println(featureTags);
			if(newFeatureTrans!=null){
			featureTrans.put(feature, newFeatureTrans);
			}
			if(lemmaFeatureTrans!=null){
			lemmaFeatTrans.put(lemmaFeature, lemmaFeatureTrans);
			System.out.println("\ntagged features: "+taggedFeature);
			System.out.println("\nlemmaFeatTrans" + lemmaFeatureTrans+"\n");
			}
		}
		// System.out.println("cliqTags" + reqTags);
		System.out.println("lemmaTextFeatures" + lemmaTextFeatures+"\n"); 
		System.out.println("featureTrans" + featureTrans+"\n");
		System.out.println("lemmaFeatTrans" + lemmaFeatTrans+"\n");
		
		File dir = new File(
				"/home/user/FinalSpace/VMiner/R statistics new/TextFeatures/");
		if (!dir.exists()) {
			dir.mkdir();
		}
		String fileName = "featureTrans";
		File tagFile = new File(dir, fileName + ".csv");
		if (!tagFile.exists()) {
			tagFile.createNewFile();
		}
		//generateCsvFile("/home/user/FinalSpace/VMiner/R statistics new/TextFeatures/featureTrans.csv");
		fileName = "cluster1";
		tagFile = new File(dir, fileName + ".csv");
		if (!tagFile.exists()) {
			tagFile.createNewFile();
		}
		/*generateItemsetsCsvFile(
				"/home/user/FinalSpace/VMiner/R statistics new/TextFeatures/cluster1.csv",
				featureTrans);*/

		JScrollPane scrollPane = new JScrollPane(myGraph);
		add(scrollPane);

	}

	private String convertToSingular(String nounGroupsTrans) {
		// TODO Auto-generated method stub
		return null;
	}

	private void generateItemsetsCsvFile(String fileUrl,
			Map<String, CustomStringList> featureTrans) throws FileNotFoundException,
			IOException {

		CSV csv = new CSV();
		csv.open(new File(fileUrl));
		Set keys = featureTrans.keySet();
		Iterator it = keys.iterator();
		int i = 1, j = 1;
		CustomStringList colNames = new CustomStringList();

		csv.put(0, 0, "Features Trans");

		while (it.hasNext()) {
			String key = (String) it.next();
			List<String> itemsets = featureTrans.get(key);
			for (String item : itemsets) {
				if (!colNames.contains(item)) {
					csv.put(j, 0, item);
					colNames.add(item);
					j++;
				}
			}
		}
		System.out.println("Column Names:" + colNames+"\n");
		keys = featureTrans.keySet();
		it = keys.iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			csv.put(0, i, key);
			CustomStringList itemsets = (CustomStringList) featureTrans.get(key);

			for (String item : colNames) {
				if (itemsets.contains(item)) {

					csv.put(csv.getColum(item), i, "1");
				} else {

					csv.put(csv.getColum(item), i, "0");
				}
			}
			i++;
		}
		csv.save(new File(fileUrl), ';');
	}

	public Map<String, CustomStringList> getFeatureTrans() {
		return featureTrans;
	}

	private void generateCsvFile(String fileUrl) throws FileNotFoundException,
			IOException {

		CSV csv = new CSV();

		csv.open(new File(
				"/home/user/FinalSpace/VMiner/R statistics new/TextFeatures/featureTrans.csv"));

		csv.put(0, 0, "Features");
		csv.put(1, 0, "ItemSets");
		csv.put(2, 0, "Clusters");
		Set keys = featureTrans.keySet();
		Iterator it = keys.iterator();
		int i = 1;

		while (it.hasNext()) {
			String key = (String) it.next();
			csv.put(0, i, key);
			csv.put(1, i, featureTrans.get(key).toString());
			for (Set<Node> clique : cliques) {
				for (Node n : clique) {
					if (n.toString().equals(key.toString())) {
						csv.put(2, i, clique.toString());
						i++;
					}

				}
			}
		}
		csv.save(
				new File(
						"/home/user/FinalSpace/VMiner/R statistics new/TextFeatures/featureTrans.csv"),
				';');
	}

	 Node seekVertex(String nom,
			ListenableUndirectedWeightedGraph myAnalysisGraph) {

		boolean existe = false;
		Node newNode = null;

		for (Node n : existingVertexs) {
			if (n.getName().equals(nom) == true) {
				existe = true;
				newNode = n;
				break;
			}
		}
		if (existe == false) {
			Node v = new Node(nom);
			myAnalysisGraph.addVertex(v);
			existingVertexs.add(v);
			newNode = v;
		}
		return newNode;

	}

	private void adjustDisplaySettings(JGraph jg) {
		jg.setPreferredSize(DEFAULT_SIZE);

		Color c = DEFAULT_BG_COLOR;
		String colorStr = null;

		try {
			// colorStr = getParameter( "bgcolor" );
		} catch (Exception e) {
		}

		if (colorStr != null) {
			c = Color.decode(colorStr);
		}

		jg.setBackground(c);
	}

	private void positionVertexAt(Object vertex, int x, int y) {
		DefaultGraphCell cell = m_jgAdapter.getVertexCell(vertex);
		Map attr = cell.getAttributes();
		Rectangle b = (Rectangle) GraphConstants.getBounds(attr);

		GraphConstants.setBounds(attr, new Rectangle(x, y, b.width, b.height));

		Map cellAttr = new HashMap();
		cellAttr.put(cell, attr);
		m_jgAdapter.edit(cellAttr, null, null, null);
	}

	public List<Set<Node>> getCliques() {
		return cliques;
	}

	public Map<String, CustomStringList> getLemmaFeatTrans() {
		return lemmaFeatTrans;
	}

	public Map<String, String> getLemmaTextFeatures() {
		return lemmaTextFeatures;
	}
	

}
