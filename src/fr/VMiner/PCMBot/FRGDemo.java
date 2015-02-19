package fr.VMiner.PCMBot;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

public class FRGDemo extends JPanel implements ActionListener{
	 private int newNodeSuffix = 1;
	 private FRG graphPanel;
	 List<Set<Node>> cliques;
	 Map<String,CustomStringList> featureTrans;
	
	public FRGDemo(Map<FeatureCouple, Double> similarityMap, CustomStringList valFeatures) throws IOException, ClassNotFoundException{
		 super(new BorderLayout());
		 // Create the components.
		 
		    graphPanel = new FRG(similarityMap, valFeatures);
		    cliques = graphPanel.getCliques();
		    featureTrans=graphPanel.getFeatureTrans();
		    // Lay everything out.
		    graphPanel.setPreferredSize(new Dimension(300, 150));
		    add(graphPanel, BorderLayout.CENTER);


	 }
	public Map<String, CustomStringList> getFeaturesTrans() {
		return featureTrans;
	}
	public List<Set<Node>> getCliques() {
		return cliques;
	}
	 public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
