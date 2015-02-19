package fr.VMiner.PCMBot;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;

import org.jgrapht.Graph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;
import org.jgrapht.alg.BronKerboschCliqueFinder;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.csvreader.CsvReader;
import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.graph.JGraphSimpleLayout;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class ClusteringSimFeatures extends JPanel{
	 private static final Color DEFAULT_BG_COLOR = Color.decode( "#FAFBFF" );
	 private static final Dimension DEFAULT_SIZE = new Dimension( 530, 320 );
	 private  static  List<Node> existingVertexs = new ArrayList<Node>();
	 private JGraphModelAdapter m_jgAdapter;
	 protected JGraph myGraph;
	 private static int compteur=1;
	 private static List<Set<Node>> cliques;
	 
	 
	 public ClusteringSimFeatures(Map<FeatureCouple, Double> similarityMap,CustomStringList valFeatures, Map<String,CustomStringList> featureTrans) throws IOException, ClassNotFoundException {
		    super(new GridLayout(1, 0));
			// create a JGraphT graph
			 ListenableUndirectedWeightedGraph  myAnalysisGraph = new ListenableUndirectedWeightedGraph( Edge.class );
			// create a visualization using JGraph, via an adapter
		        m_jgAdapter = new JGraphModelAdapter( myAnalysisGraph );
		        
		       
		        JGraph myGraph = new JGraph( m_jgAdapter );

		      
		        
		     // add some sample data (graph manipulated via JGraphT)
		        
		        Set listKeys=similarityMap.keySet();
				Iterator iterator =listKeys.iterator();
				double weight;
				FeatureCouple key;
				while(iterator.hasNext())
				{
				
					    key= (FeatureCouple) iterator.next();
					    weight = similarityMap.get(key);
						Node source = seekVertex(key.getFeatureSource(),myAnalysisGraph);
						Node destination = seekVertex(key.getFeatureTarget(),myAnalysisGraph);
						if(weight >= 0.60){
						myAnalysisGraph.addEdge(source, destination);
						myAnalysisGraph.setEdgeWeight(myAnalysisGraph.getEdge(source, destination), weight);
						}
				}
	 
				
				BronKerboschCliqueFinder finder =  new BronKerboschCliqueFinder(myAnalysisGraph);
				//tester le cast � l'execution
				cliques = (List<Set<Node>>) finder.getAllMaximalCliques();
				//Clusters_Weight_Sup2 clusters = new Clusters_Weight_Sup2();
				//Product p1 =new Product("P1");
				//clusters.addClusters(p1, cliques);
				//Collection<Set<Node>> cliques2 =clusters.getCliques_weight_sup2().get(p1);
				//for (Set<Node> clique : cliques2){
				
				 System.out.println("clique2:"+cliques);
				 
			updateCsvFile("/home/user/FinalSpace/VMiner/R statistics new/ValFeatures/featureTrans.csv"); 
			CsvFileByCluster (featureTrans, cliques);
				 
				 
				
				/*final JGraphSimpleLayout graphLayout =
					    new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_RANDOM, 100, 100);
					final JGraphFacade graphFacade = new JGraphFacade(myGraph);
					graphLayout.run(graphFacade);
					final Map nestedMap = graphFacade.createNestedMap(true, true);
					myGraph.getGraphLayoutCache().edit(nestedMap);*/
					
				   JScrollPane scrollPane = new JScrollPane(myGraph);
				    add(scrollPane);
				
			    
		 }
		  
	 public static boolean isSubStringFeature(Set<Node> clique, Node feature){
		 boolean isSubStr = false;
		 Set<Node> features = new HashSet<Node>();
		 features.add(feature);
		 Set<Node> remainSet = new HashSet<Node>(clique); 
		 remainSet.removeAll(features);
		 
		 String featureName = feature.getName()+" ";
		 for (Iterator<Node> iter = remainSet.iterator(); iter.hasNext(); ) {
			    Node n = iter.next();
			    String nodeName = n.getName()+" ";
			    if(nodeName.contains(featureName) || featureName.contains(nodeName)){
			    isSubStr = true;
			    break;
			    } 
			} 
		 
		 return isSubStr;
	 }
	 
	 public static boolean isClusterContainingSubString(Set<Node> clique){
		 boolean isSubStr = false;
		 for(Node n: clique){
			 isSubStr = isSubStringFeature(clique,n);
			 if(isSubStr == true){
				return true;
			 }
		 }
		 return isSubStr;
	 }
	 
	 private static void CsvFileByCluster (Map<String,CustomStringList> featureTrans, List<Set<Node>> cliques) throws FileNotFoundException, IOException{
		
		 int i,j,k=1;
	
		 
		 for (Set<Node> clique : cliques){
			 if(clique.size()!=1 && !isClusterContainingSubString(clique)){
			  i=1;j=1;
			  CustomStringList colNames = new CustomStringList();
			  CSV csv = new CSV();
			  
			  File dir=new File("/home/user/FinalSpace/VMiner/R statistics new/ValFeatures/");
			  if(!dir.exists()){
			  dir.mkdir();}
			  String fileName="cluster2."+k;
			  File tagFile=new File(dir,fileName+".csv");
			  if(!tagFile.exists()){
			  tagFile.createNewFile();
			  }
			  
			  //String fileUrl ="/home/user/git/familiar-language/FAMILIAR/R statistics/cluster2."+k+".csv";
			  csv.open(tagFile, ';');
			  csv.put(0,0, "Features Trans");
			  
			  for(Node n : clique){
			  CustomStringList itemsets = featureTrans.get(n.toString()); 
			  for(String item : itemsets){
				  if(!colNames.contains(item)){
				  csv.put(j,0,item);
				  colNames.add(item);
				  j++;
				  } 
			  	  }
			  }
			  
			  for(Node n : clique){
				  CustomStringList itemsets = (CustomStringList) featureTrans.get(n.toString()); 
					  
					  csv.put(0,i,n.toString());
					  
					  for(String item : colNames){
						  if(itemsets.contains(item)){
							 
							  csv.put(csv.getColum(item),i,"1");  
						  }
						  else{
							  
							  csv.put(csv.getColum(item),i,"0");  
						  }
					  }
					  i++;
			  }
				 
				
				 csv.save(tagFile,';');
				  
				k++;	
			  }
		 
		 }  
		 
	 	}

		 
	 private static void updateCsvFile(String fileUrl) throws IOException {
			// TODO Auto-generated method stub
		 CSV csv = new CSV();
		 int i = 1;
		 
		 csv.open(new File("/home/user/FinalSpace/VMiner/R statistics new/ValFeatures/featureTrans.csv"),';');
		 
		 while(i<csv.rows()){
			 
			String featureID = csv.get(0, i);
			
			 for (Set<Node> clique : cliques){
				 
				  for(Node n : clique){
					  
					  //System.out.println(n.getName()+" "+reqID.toString());
					  
					 
			    	if (n.getName().equals(featureID.toString())){
			    		String clusters = csv.get(2, i);
			    		System.out.print(clusters);
			    		//clusters.concat(", "+clique.toString());
			    		if(!(clique.toString()).equals("")){
			    			clusters+=", "+clique.toString();
				    		 csv.put(2, i, clusters); 

			    		}
			    	
			        }
			    	}
			    	
				  }
			 i++;
		}
		csv.save(new File("/home/user/FinalSpace/VMiner/R statistics new/ValFeatures/featureTrans.csv"),';');
		}




static  Node seekVertex(String nom, ListenableUndirectedWeightedGraph myAnalysisGraph) {
	
	 boolean existe = false;
	 Node newNode = null;
	 
	 for (Node n :existingVertexs){
			if (n.getName().equals(nom) == true){
				existe= true;
				 newNode = n;
				break;
				}
		}
			if(existe == false)
			{
			Node v = new Node (nom);
			myAnalysisGraph.addVertex(v);
			existingVertexs.add(v);
			 newNode = v;
			}
			return newNode;
			
		}

		

		
private void adjustDisplaySettings( JGraph jg ) {
       jg.setPreferredSize( DEFAULT_SIZE );

       Color  c        = DEFAULT_BG_COLOR;
       String colorStr = null;

       try {
          // colorStr = getParameter( "bgcolor" );
       }
        catch( Exception e ) {}

       if( colorStr != null ) {
           c = Color.decode( colorStr );
       }

       jg.setBackground( c );
   }


   private void positionVertexAt( Object vertex, int x, int y ) {
       DefaultGraphCell cell = m_jgAdapter.getVertexCell( vertex );
       Map              attr = cell.getAttributes(  );
       Rectangle        b    = (Rectangle) GraphConstants.getBounds( attr );

       GraphConstants.setBounds( attr, new Rectangle( x, y, b.width, b.height ) );

       Map cellAttr = new HashMap(  );
       cellAttr.put( cell, attr );
       m_jgAdapter.edit(cellAttr, null, null, null);
   }
   public List<Set<Node>> getCliques() {
		return cliques;
	}
}


