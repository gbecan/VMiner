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

public class ClusteringSimDescDemo extends JPanel implements ActionListener{
	 private int newNodeSuffix = 1;
	 private ClusteringSimDesc graphPanel;
	 List<Set<Node>> cliques;
	 
	public ClusteringSimDescDemo(Map<DescCouple, Double> similarityMap,Map<String,String> reqMap, Map<String,CustomStringList> reqTrans) throws IOException, ClassNotFoundException{
		 super(new BorderLayout());
		 // Create the components.
		 
		    graphPanel = new ClusteringSimDesc(similarityMap,reqMap, reqTrans);
		    cliques = graphPanel.getCliques();
		    
		    // Lay everything out.
		    graphPanel.setPreferredSize(new Dimension(300, 150));
		    add(graphPanel, BorderLayout.CENTER);


	 }
	 public List<Set<Node>> getCliques() {
		return cliques;
	}
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
