package fr.VMiner.PCMBot;

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;

public class CustomListenableDirectedWeightedGraph extends ListenableUndirectedWeightedGraph {

	public CustomListenableDirectedWeightedGraph(Class arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	public CustomListenableDirectedWeightedGraph(WeightedGraph arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	public void setEdgeWeight(Edge e, double weight) {
	    super.setEdgeWeight(e, weight);
	    ((Edge)e).setWeight(weight);
	}

	
	

}
