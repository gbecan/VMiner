package fr.VMiner.PCMBot;

import java.awt.Container;
import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

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

public class FeatureTextClustering {

	private LatentSemanticAnalysis lsa;
	protected Levenshtein levenshtein;
	protected SmithWaterman smithWaterman;

	private Map<FeatureCouple, Double> similarityTextMap = new HashMap<FeatureCouple, Double>();
	CustomStringList textFeatures = new CustomStringList();

	public Map<List<String>, String> clusterTextFeatures(CustomStringList textFeat)
			throws IOException, InterruptedException, ClassNotFoundException {
		JFrame frame = new JFrame("Clustering textual Features");
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		textFeatures = textFeat;
	/*	for(String feature: valFeatures){
			//tag feature
			//extraire le dernier nom
			//convertir en singulier
		}*/
		SimFeatures();
		// On cree un panel � onglets
		JTabbedPane onglets = new JTabbedPane();

		TextFRGDemo FRGContentPane = new TextFRGDemo(similarityTextMap, textFeatures);
		FRGContentPane.setOpaque(true); // content panes must be opaque
		onglets.addTab("FRG", null, FRGContentPane,
				"Features relationship graph");

		ClusteringSimTextFeaturesDemo clusterContentPane = new ClusteringSimTextFeaturesDemo(
				similarityTextMap, textFeatures, FRGContentPane.getFeaturesTrans(),FRGContentPane.getLemmaFeatTrans(),FRGContentPane.getLemmaTextFeatures());
		clusterContentPane.setOpaque(true); // content panes must be opaque
		onglets.addTab("Cliques with threshold 0.7", null, clusterContentPane,
				null);

		Process p = Runtime.getRuntime().exec(
				"Rscript /home/user/Rtest/textClustersFIS.R");
		String s = null;
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				p.getInputStream()));

		BufferedReader stdError = new BufferedReader(new InputStreamReader(
				p.getErrorStream()));

		// read the output from the command
		System.out.println("Here is the standard output of the command:\n");
		while ((s = stdInput.readLine()) != null) {
			System.out.println(s);
		}
		// read any errors from the attempted command
		System.out
				.println("Here is the standard error of the command (if any):\n");
		while ((s = stdError.readLine()) != null) {
			System.out.println(s);
		}
		p.waitFor();
		Map<List<String>, String> FIS = getCommonPartByCluster(textFeatures,
				"/home/user/FinalSpace/VMiner/R statistics new/TextFeatures/clustersFIS.csv");
		Container contentPane = frame.getContentPane();
		contentPane.add(onglets);

		frame.pack();
		frame.setVisible(true);
		return FIS;
	}

	private Map<List<String>, String> getCommonPartByCluster(
			CustomStringList valFeatures, String csvFile)
			throws FileNotFoundException, IOException {

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";
		String itemSet;
		Map<List<String>, String> FIS = new HashMap<List<String>, String>();

		try {

			br = new BufferedReader(new FileReader(csvFile));
			line = br.readLine();

			while ((line = br.readLine()) != null) {

				String[] itemSplit;
				String[] itemSplit2;
				List<String> commonItemList = new ArrayList<String>();
				Pattern p1 = Pattern.compile("\\{[a-zA-Z0-9-x,.\\s]+\\}");
				System.out
						.println("\n****************************************Next cluster*******************************************\n");
				// use comma as separator
				String[] clusterFIS = line.split(cvsSplitBy);
				System.out.println("ClusterFIS [cluster= " + clusterFIS[1]
						+ " , FIS=" + clusterFIS[2] + "]\n");
				String value = clusterFIS[2].replace("list", "")
						.replace(".", " ").replaceAll("[\"()]", "");

				System.out.println("clusterFIS[2]: " + value);

				Matcher matcher = p1.matcher(value);
				while (matcher.find()) {
					itemSet = matcher.group();
					System.out.println("FIS: " + itemSet);
					itemSplit = itemSet.replaceAll("[{}]", "").split(",");
					Set<String> stringSet = new HashSet<String>(
							Arrays.asList(itemSplit));
					List<String> filteredFIS = new ArrayList<String>();
					filteredFIS.addAll(stringSet);
					System.out.println("filteredFIS: " + filteredFIS);
					System.out.println("filteredFIS split");

					for (Iterator<String> iterator = filteredFIS.iterator(); iterator
							.hasNext();) {
						String s = iterator.next();
						s = s.trim();
						if (isSubString(filteredFIS, s)) {
							iterator.remove();
						}
					}
					System.out.println("Final filteredFIS: " + filteredFIS);
					commonItemList = getMaxItem(filteredFIS);
					System.out.println("commonItem :" + commonItemList);

				}
				List<String> filteredCluster = new ArrayList<String>();
				String valueCluster = clusterFIS[1].replace("list", "")
						.replaceAll("[\"()]", "");

				System.out.println("clusterFIS[1]: " + valueCluster);

				itemSplit2 = valueCluster.split(",");
				Set<String> clusterSet = new HashSet<String>(
						Arrays.asList(itemSplit2));

				filteredCluster.addAll(clusterSet);
				System.out.println("filteredCluster: " + filteredCluster);
				System.out.println("filteredCluster split");
				for (String f : filteredCluster) {
					f = f.trim();
					System.out.println(f);
				}

				filteredCluster = trim(filteredCluster);
				System.out.println("filteredCluster :" + filteredCluster);
				if(commonItemList.size()==1){
				String	commonItem = commonItemList.get(0);
				FIS.put(filteredCluster, commonItem);
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Done");
		System.out.println("Final FIS :" + FIS);
		return FIS;
	}

	private List<String> trim(List<String> filteredCluster) {
		List<String> tempList = new ArrayList<String>();
		for (String f : filteredCluster) {
			f = f.trim();
			tempList.add(f);
		}
		return tempList;
	}

	private List<String> getMaxItem(List<String> filteredFIS) {
		int max = seekmax(filteredFIS);
		List<String> maxItem = new ArrayList<String>();
		for (String item : filteredFIS) {
			if (max == item.length()) {
				maxItem.add(item);
			}
		}
		return maxItem;
	}

	public int seekmax(List<String> filteredFIS) {
		int max = 0;
		for (String item : filteredFIS) {
			int size = item.length();
			if (size > max) {
				max = size;
			}
		}
		return max;
	}

	private boolean isSubString(List<String> filteredFIS, String s) {

		boolean isSubStr = false;
		for (String item : filteredFIS) {
			if (!item.equals(s) && item.contains(s)) {
				isSubStr = true;
			}
		}
		return isSubStr;
	}

	public void SimFeatures() throws IOException {
		System.out.println("\n ***********Levenshtein*******\n");
		levenshtein = new Levenshtein();
		Object[] array = textFeatures.toArray();

		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array.length; j++) {
				String source = array[i].toString();
				String target = array[j].toString();
				if (i != j) {
					FeatureCouple coupleKey = new FeatureCouple(source, target);
					if ((!similarityTextMap.containsKey(coupleKey))
							&& (coupleKey.existKeyReverse(similarityTextMap) == false)) {
						similarityTextMap.put(coupleKey, (double) levenshtein
								.getSimilarity(source, target));

					}
				}
			}
		}

		Set listKeys = similarityTextMap.keySet();
		Iterator iterator = listKeys.iterator();
		double sim;
		String source;
		String target;
		FeatureCouple key;
		// Parcourir les cl�s et afficher les entr�es de chaque cl�;

		System.out
				.println("\n *****************************Sim(Ri,Rj) >= 0.60********************************** \n");
		while (iterator.hasNext())

		{

			key = (FeatureCouple) iterator.next();
			sim = similarityTextMap.get(key);
			if (sim >= 0.60) {
				source = key.getFeatureSource();
				target = key.getFeatureTarget();

				System.out.println(key.getFeatureSource());
				System.out.println(key.getFeatureTarget());
				System.out.println("Levenshtein" + key.toString() + " = "
						+ similarityTextMap.get(key) + "\n");
			}

		}

	}

}
