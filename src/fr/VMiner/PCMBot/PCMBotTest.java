package fr.VMiner.PCMBot;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PCMBotTest {

	private static List<File> plFiles = new ArrayList<File>();
	private static Map<String, HashMap<String, Double>> topNCValMap = new HashMap<String, HashMap<String, Double>>();
	private static Map<String, List<String>> featuresMap = new HashMap<String, List<String>>();
	private static Map<String, List<String>> candidValMap = new HashMap<String, List<String>>();
	private static Map<String, List<String>> candidTextMap = new HashMap<String, List<String>>();
	private static Map<List<String>, String> valFIS = new HashMap<List<String>, String>();
	private static Map<List<String>, String> textFIS = new HashMap<List<String>, String>();
	private static List<String> existCommonItems = new ArrayList<String>();
	private static int pcmLinesCount = 0;
	private static CSV csv = new CSV();
	private static File dir = new File(
			"/home/user/FinalSpace/VMiner/R statistics new/");
	private static File pcmFile = new File(dir, "pcm.csv");

	private static int finalPcmLinesCount = 0;
	private static CSV finalCsv = new CSV();
	private static File finalPCM = new File(dir, "finalPCM.csv");
	private static int startingPoint = 0;

	public static void main(String args[]) throws Exception {
		seekFeaturesByFile();
		generatePCM();
		featuresValDiff();
		refactorValPCM();
		featuresTextDiff();
		refactorTextPCM();
		generateResultingPCM();
		refactorResultingPCM();
	}

	private static void refactorResultingPCM() throws IOException {
		// copy quantified features in final PCM
		Set<String> paths = featuresMap.keySet();

		while (startingPoint < pcmLinesCount) {
			if (!emptyLine(startingPoint)) {
				String feature = csv.get(0, startingPoint);
				finalCsv.put(0, finalPcmLinesCount, feature);
				for (String path : paths) {
					path = path.trim();
					String value = csv.get(csv.getColum(path), startingPoint);
					finalCsv.put(finalCsv.getColum(path), finalPcmLinesCount,
							value);
				}
				finalPcmLinesCount++;
			}
			startingPoint++;
		}
		finalCsv.save(finalPCM, ';');
	}

	private static boolean emptyLine(int startingPoint) {
		Set<String> paths = featuresMap.keySet();
		for (String path : paths) {
			path = path.trim();
			String value = csv.get(csv.getColum(path), startingPoint);
			if (!value.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private static void generateResultingPCM() throws FileNotFoundException,
			IOException {

		// CSV csv = new CSV();
		// File dir = new
		// File("/home/user/FinalSpace/VMiner/R statistics new/");
		if (!dir.exists()) {
			dir.mkdir();
		}
		// String fileName = "pcm";
		// File pcmFile = new File(dir, fileName + ".csv");
		if (!finalPCM.exists()) {
			finalPCM.createNewFile();
		}
		finalCsv.open(finalPCM);
		Set paths = featuresMap.keySet();
		Iterator it = paths.iterator();
		int i = 1, j = 1;
		CustomStringList lineNames = new CustomStringList();

		finalCsv.put(0, 0, "Features");
		while (it.hasNext()) {
			String path = (String) it.next();
			List<String> features = featuresMap.get(path);
			for (String feature : features) {
				if (!lineNames.contains(feature)) {
					finalCsv.put(0, i, feature);
					lineNames.add(feature);
					i++;
				}
			}
		}
		System.out.println("Line Names:" + lineNames + "\n");
		paths = featuresMap.keySet();
		it = paths.iterator();
		while (it.hasNext()) {
			String path = (String) it.next();
			path = path.trim();
			finalCsv.put(j, 0, path);
			List<String> features = featuresMap.get(path);
			for (String feature : lineNames) {
				if (features.contains(feature)) {

					finalCsv.put(j, finalCsv.getLine(feature), "1");
				} else {

					finalCsv.put(j, finalCsv.getLine(feature), "0");
				}
			}
			j++;
		}
		finalPcmLinesCount = i;
		finalCsv.save(finalPCM, ';');

	}

	private static void refactorTextPCM() throws IOException {
		Set<List<String>> textClusters;
		Set<String> textPaths;
		Iterator<List<String>> textItCluster;
		Iterator<String> textItPath;
		List<String> textFeatureList = new ArrayList<String>();
		

		textClusters = textFIS.keySet();
		textItCluster = textClusters.iterator();
		while (textItCluster.hasNext()) {
			List<String> cluster = textItCluster.next();
			String commonItem = textFIS.get(cluster);
			if (!existCommonItems.contains(commonItem)) {
				existCommonItems.add(commonItem);
				csv.put(0, pcmLinesCount, commonItem);
				pcmLinesCount = pcmLinesCount + 1;

				for (String feature : cluster) {
					if (!feature.equals(commonItem)) {

						String value = diff(feature, commonItem);
						textPaths = candidTextMap.keySet();
						textItPath = textPaths.iterator();
						while (textItPath.hasNext()) {
							String path = (String) textItPath.next();
							path = path.trim();
							System.out.println(path);
							textFeatureList = candidTextMap.get(path);
							System.out.println(textFeatureList);
							if (textFeatureList.contains(feature)) {
								System.out.println(path);
								csv.put(csv.getColum(path),
										csv.getLine(commonItem), value);
								// remove the feature from the initial
								// featuresMap
								// (remove redundancy from csv file)
								featuresMap.get(path).remove(feature);

							}

						}
					}
				}
			}
			else{
				Map<Point, String> tempMap = new HashMap<Point, String>();
				textPaths = candidTextMap.keySet();
				for (String feature : cluster) {
					if (!feature.equals(commonItem)) {

						String value = diff(feature, commonItem);
						textItPath = textPaths.iterator();
						while (textItPath.hasNext()) {
							String path = (String) textItPath.next();
							path = path.trim();
							System.out.println(path);
							textFeatureList = candidTextMap.get(path);
							System.out.println(textFeatureList);
							if (textFeatureList.contains(feature)) {
								System.out.println(path);
								Point point = new Point (csv.getColum(path), csv.getLine(commonItem));
								tempMap.put(point, value);
							}

						}
					}
				}
				if(!areDiffRows(tempMap)){
					Set<Point> points = tempMap.keySet();
					for (Point point : points) {
						int column =(int)point.getX();
						int row =(int)point.getY();
						String cellVal= csv.get(column,row);
						String newCellVal = tempMap.get(point);
						if(cellVal.equals("") && !newCellVal.equals("")){
							csv.put(column, row, newCellVal);
						}
					}
					
				}
			}
		}
		csv.save(pcmFile, ';');

	}



	private static boolean areDiffRows(Map<Point, String> tempMap) { 
		Set<Point> points = tempMap.keySet();
		for (Point point : points) {
			int column =(int)point.getX();
			int row =(int)point.getY();
			String cellVal= csv.get(column,row);
			String newCellVal = tempMap.get(point);
			if(!cellVal.equals(newCellVal) && !cellVal.equals("") && !newCellVal.equals("")){
				return true;
			}
		}
		return false;
	}

	private static void refactorValPCM() throws IOException {
		Set<List<String>> valClusters;
		Set<String> valPaths;
		Iterator<List<String>> valItCluster;
		Iterator<String> valItPath;
		/*
		 * CSV csv = new CSV(); File dir = new
		 * File("/home/user/FinalSpace/VMiner/R statistics new/"); if
		 * (!dir.exists()) { dir.mkdir(); }
		 */
		// String fileName = "pcm";
		// File pcmFile = new File(dir, fileName + ".csv");
		/*
		 * if (!pcmFile.exists()) { pcmFile.createNewFile(); }
		 */
		// csv.open(pcmFile);
		List<String> valFeatureList = new ArrayList<String>();

		valClusters = valFIS.keySet();
		valItCluster = valClusters.iterator();
		while (valItCluster.hasNext()) {
			List<String> cluster = valItCluster.next();
			String commonItem = valFIS.get(cluster);
			if (!existCommonItems.contains(commonItem)) {
			existCommonItems.add(commonItem);
			csv.put(0, pcmLinesCount, commonItem);
			pcmLinesCount = pcmLinesCount + 1;

			for (String feature : cluster) {
				if (!feature.equals(commonItem)) {

					String value = diff(feature, commonItem);
					valPaths = candidValMap.keySet();
					valItPath = valPaths.iterator();
					while (valItPath.hasNext()) {
						String path = (String) valItPath.next();
						path = path.trim();
						System.out.println(path);
						valFeatureList = candidValMap.get(path);
						System.out.println(valFeatureList);
						if (valFeatureList.contains(feature)) {
							System.out.println(path);
							csv.put(csv.getColum(path),
									csv.getLine(commonItem), value);
							// remove the feature from the initial featuresMap
							// (remove redundancy from csv file)
							featuresMap.get(path).remove(feature);

						}

					}
				}
			}
			}
			else{
				Map<Point, String> tempMap = new HashMap<Point, String>();
				valPaths = candidValMap.keySet();
				for (String feature : cluster) {
					if (!feature.equals(commonItem)) {

						String value = diff(feature, commonItem);
						valItPath = valPaths.iterator();
						while (valItPath.hasNext()) {
							String path = (String) valItPath.next();
							path = path.trim();
							System.out.println(path);
							valFeatureList = candidValMap.get(path);
							System.out.println(valFeatureList);
							if (valFeatureList.contains(feature)) {
								System.out.println(path);
								Point point = new Point (csv.getColum(path), csv.getLine(commonItem));
								tempMap.put(point, value);
							}

						}
					}
				}
				if(!areDiffRows(tempMap)){
					Set<Point> points = tempMap.keySet();
					for (Point point : points) {
						int column =(int)point.getX();
						int row =(int)point.getY();
						String cellVal= csv.get(column,row);
						String newCellVal = tempMap.get(point);
						if(cellVal.equals("") && !newCellVal.equals("")){
							csv.put(column, row, newCellVal);
						}
					}
					
				}
			}
		}
		csv.save(pcmFile, ';');

	}

	private static void featuresValDiff() throws ClassNotFoundException,
			IOException, InterruptedException {

		Set valPaths = candidValMap.keySet();
		Iterator valIt = valPaths.iterator();
		CustomStringList valFeatures = new CustomStringList();

		while (valIt.hasNext()) {
			String valPath = (String) valIt.next();
			List<String> features1 = candidValMap.get(valPath);
			for (String feature : features1) {
				if (!valFeatures.contains(feature)) {
					valFeatures.add(feature);
				}
			}
		}
		System.out.println("Quantified features:" + valFeatures + "\n");
		FeatureClustering clusFeat1 = new FeatureClustering();
		valFIS = clusFeat1.clusterFeatures(valFeatures);

	}

	private static void featuresTextDiff() throws ClassNotFoundException,
			IOException, InterruptedException {

		Set textPaths = candidTextMap.keySet();
		Iterator textIt = textPaths.iterator();
		CustomStringList textFeatures = new CustomStringList();

		while (textIt.hasNext()) {
			String textPath = (String) textIt.next();
			List<String> features2 = candidTextMap.get(textPath);
			for (String feature : features2) {
				if (!textFeatures.contains(feature)) {
					textFeatures.add(feature);
				}
			}
		}
		System.out.println("Textual features:" + textFeatures + "\n");
		FeatureTextClustering clusFeat2 = new FeatureTextClustering();
		textFIS = clusFeat2.clusterTextFeatures(textFeatures);
	}

	/*
	 * private static void generatePCM() throws FileNotFoundException,
	 * IOException {
	 * 
	 * CSV csv = new CSV(); File dir = new File(
	 * "/home/user/git final/familiar-language/FAMILIAR/R statistics new/"); if
	 * (!dir.exists()) { dir.mkdir(); } String fileName = "pcm"; File pcmFile =
	 * new File(dir, fileName + ".csv"); if (!pcmFile.exists()) {
	 * pcmFile.createNewFile(); } csv.open(pcmFile); Set paths =
	 * featuresMap.keySet(); Iterator it = paths.iterator(); int i = 1, j = 1;
	 * CustomStringList colNames = new CustomStringList();
	 * 
	 * csv.put(0, 0, "Features"); while (it.hasNext()) { String path = (String)
	 * it.next(); List<String> features = featuresMap.get(path); for (String
	 * feature : features) { if (!colNames.contains(feature)) { csv.put(j, 0,
	 * feature); colNames.add(feature); j++; } } }
	 * System.out.println("Column Names:" + colNames+"\n"); paths =
	 * featuresMap.keySet(); it = paths.iterator(); while (it.hasNext()) {
	 * String path = (String) it.next(); csv.put(0, i, path); List<String>
	 * features = featuresMap.get(path); for (String feature : colNames) { if
	 * (features.contains(feature)) {
	 * 
	 * csv.put(csv.getColum(feature), i, "1"); } else {
	 * 
	 * csv.put(csv.getColum(feature), i, "0"); } } i++; } csv.save(pcmFile,
	 * ';'); }
	 */
	private static void generatePCM() throws FileNotFoundException, IOException {

		// CSV csv = new CSV();
		// File dir = new
		// File("/home/user/FinalSpace/VMiner/R statistics new/");
		if (!dir.exists()) {
			dir.mkdir();
		}
		// String fileName = "pcm";
		// File pcmFile = new File(dir, fileName + ".csv");
		if (!pcmFile.exists()) {
			pcmFile.createNewFile();
		}
		csv.open(pcmFile);
		Set paths = featuresMap.keySet();
		Iterator it = paths.iterator();
		int i = 1, j = 1;
		CustomStringList lineNames = new CustomStringList();

		csv.put(0, 0, "Features");
		while (it.hasNext()) {
			String path = (String) it.next();
			List<String> features = featuresMap.get(path);
			for (String feature : features) {
				if (!lineNames.contains(feature)) {
					csv.put(0, i, feature);
					lineNames.add(feature);
					i++;
				}
			}
		}
		System.out.println("Line Names:" + lineNames + "\n");
		paths = featuresMap.keySet();
		it = paths.iterator();
		while (it.hasNext()) {
			String path = (String) it.next();
			path = path.trim();
			csv.put(j, 0, path);
			List<String> features = featuresMap.get(path);
			for (String feature : lineNames) {
				if (features.contains(feature)) {

					csv.put(j, csv.getLine(feature), "1");
				} else {

					csv.put(j, csv.getLine(feature), "0");
				}
			}
			j++;
		}
		pcmLinesCount = i;
		startingPoint = i;
		csv.save(pcmFile, ';');

	}

	private static void refactorPCM() throws IOException {
		Set<List<String>> valClusters;
		Set<String> valPaths;
		Iterator<List<String>> valItCluster;
		Iterator<String> valItPath;
		/*
		 * CSV csv = new CSV(); File dir = new
		 * File("/home/user/FinalSpace/VMiner/R statistics new/"); if
		 * (!dir.exists()) { dir.mkdir(); }
		 */
		// String fileName = "pcm";
		// File pcmFile = new File(dir, fileName + ".csv");
		/*
		 * if (!pcmFile.exists()) { pcmFile.createNewFile(); }
		 */
		// csv.open(pcmFile);
		List<String> valFeatureList = new ArrayList<String>();

		valClusters = valFIS.keySet();
		valItCluster = valClusters.iterator();
		while (valItCluster.hasNext()) {
			List<String> cluster = valItCluster.next();
			String commonItem = valFIS.get(cluster);
			csv.put(0, pcmLinesCount, commonItem);
			pcmLinesCount = pcmLinesCount + 1;

			for (String feature : cluster) {
				if (!feature.equals(commonItem)) {

					String value = diff(feature, commonItem);
					valPaths = candidValMap.keySet();
					valItPath = valPaths.iterator();
					while (valItPath.hasNext()) {
						String path = (String) valItPath.next();
						path = path.trim();
						System.out.println(path);
						valFeatureList = candidValMap.get(path);
						System.out.println(valFeatureList);
						if (valFeatureList.contains(feature)) {
							System.out.println(path);
							csv.put(csv.getColum(path),
									csv.getLine(commonItem), value);

						}

					}
				}
			}
		}

		Set<List<String>> textClusters;
		Set<String> textPaths;
		Iterator<List<String>> textItCluster;
		Iterator<String> textItPath;
		List<String> textFeatureList = new ArrayList<String>();

		textClusters = textFIS.keySet();
		textItCluster = textClusters.iterator();
		while (textItCluster.hasNext()) {
			List<String> cluster = textItCluster.next();
			String commonItem = textFIS.get(cluster);
			csv.put(0, pcmLinesCount, commonItem);
			pcmLinesCount = pcmLinesCount + 1;

			for (String feature : cluster) {
				if (!feature.equals(commonItem)) {

					String value = diff(feature, commonItem);
					textPaths = candidTextMap.keySet();
					textItPath = textPaths.iterator();
					while (textItPath.hasNext()) {
						String path = (String) textItPath.next();
						path = path.trim();
						System.out.println(path);
						textFeatureList = candidTextMap.get(path);
						System.out.println(textFeatureList);
						if (textFeatureList.contains(feature)) {
							System.out.println(path);
							csv.put(csv.getColum(path),
									csv.getLine(commonItem), value);

						}

					}
				}
			}
		}
		csv.save(pcmFile, ';');
	}

	private static String diff(String feature, String commonItem) {
		String value = "NA";

		if (!feature.contains(commonItem)) {

			BreakIterator boundary = BreakIterator.getWordInstance();
			boundary.setText(commonItem);
			String firstword = printFirst(boundary, commonItem);
			String lastWord = commonItem
					.substring(commonItem.lastIndexOf(" ") + 1);
			int beginIndex = feature.indexOf(firstword);
			int endIndex = feature.indexOf(lastWord) + lastWord.length();
			commonItem = feature.substring(beginIndex, endIndex);
		}
		if (feature.contains(commonItem)) {
			int begin = feature.indexOf(commonItem);
			int end = begin + commonItem.length();
			if (begin > 0 && begin <= end && end <= feature.length()) {
				value = feature.substring(0, begin - 1);
			}
			if ((begin == 0) && begin <= end && end <= feature.length()) {
				value = feature.substring(end + 1, feature.length());
			}
		}
		value = value.trim();
		if (value.equals("")) {
			value = "NA";
		}
		return value;
	}

	public static String printFirst(BreakIterator boundary, String source) {
		int start = boundary.first();
		int end = boundary.next();
		return (source.substring(start, end));
	}

	public static String printLast(BreakIterator boundary, String source) {
		int end = boundary.last();
		int start = boundary.previous();
		return (source.substring(start, end));
	}

	public static void seekFeaturesByFile() throws Exception {

		File path;
		Map<String, Double> topNCValFeatures;
		FilesManip manip = new FilesManip();
		plFiles = manip.scanDir("Laptop");
		// plFiles = manip.scanDir("Laptop");
		CustomStringList totalFeatures = new CustomStringList();

		for (int i = 0; i < plFiles.size(); i++) {
			path = plFiles.get(i);
			System.out.println("Path: " + path.getPath());
			NCMetric ncValue = new NCMetric();
			topNCValFeatures = ncValue.genFeatures(path.getPath());
			topNCValMap.put(path.getName(),
					(HashMap<String, Double>) topNCValFeatures);
			System.out.println("top NCVal Features: " + topNCValFeatures);
			System.out.println("top NCVal Features size: "
					+ topNCValFeatures.size());
			System.out.println("top NCVal Features by file: " + topNCValMap);

			List<String> candidValList = ncValue.getCandidValList();
			List<String> candidTextList = ncValue.getCandidTextList();

			candidValMap.put(path.getName(), candidValList);
			candidTextMap.put(path.getName(), candidTextList);

			System.out.println("Quantified features Map: " + candidValMap);
			System.out.println("Textual features Map: " + candidTextMap);
		}
		System.out.println("Quantified features Map: " + candidValMap);
		System.out.println("Textual features Map: " + candidTextMap);
		Set files = topNCValMap.keySet();
		Iterator it = files.iterator();
		String file;
		while (it.hasNext()) {
			file = (String) it.next();
			List<String> topFeatures = new ArrayList<String>();
			topFeatures.addAll(topNCValMap.get(file).keySet());
			featuresMap.put(file, topFeatures);
			totalFeatures.addAll(topFeatures);
		}
		System.out.println("features by file: " + featuresMap);

		/*
		 * totalFeatures.removeDuplicate();
		 * System.out.println("total Features: " + totalFeatures);
		 * System.out.println(totalFeatures.size());
		 */
	}

}
