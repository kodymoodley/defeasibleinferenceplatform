package net.za.cair.dip.experimentbench;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.transform.RationalRankingAlgorithm;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class UniqueLHSExceptionalFinderMOWLRep { // NO_UCD (unused code)
	
	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException{		
		/*for (int i = 1; i <= 10;i++){
			System.out.println("Percentage Defeasibility: " + (i*10) + "%");
			System.out.println();*/
			
			FileWriter normalExceptionsW = new FileWriter("MOWLRep/normalExceptions.txt");
			PrintWriter normalExceptionsPW = new PrintWriter(normalExceptionsW);
			
			for (int j = 1; j <= 134;j++){
				
				/*************** Load the ranking ********************/
				System.out.print("loading the ranking for ontology " + j + "...");
				FileInputStream fileIn = new FileInputStream("MOWLRep/"+j+"/ontranking.bin");
		        ObjectInputStream in = new ObjectInputStream(fileIn);
		        Ranking ranking = null;
		        boolean failedLoading = true;
		        try{
		        	ranking = (Ranking) in.readObject();
		        	failedLoading = false;
		        	System.out.println("done.");
		        }
		        catch (Exception e){
		        	System.out.println("failed!");
		        	e.printStackTrace();
		        }
		        in.close();
		        
		        if (failedLoading){
			    	// Compute the ranking
			    	// 1. First load the ontology
			    	/*********************** Create Ontology Manager **************************/
					OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();	
					File ontologyFile = new File("MOWLRep/" + j + "/processed.owl");
					/*********************** Load Ontology (arg[0]) ************************************/
					OWLOntology ontology = null;
					try {
						System.out.print("loading ontology " + j + "...");
						ontology = ontologyManager.loadOntologyFromOntologyDocument(ontologyFile);
						System.out.println("done.");
					}
					catch (OWLOntologyCreationException e) {
						System.out.println("failed!");
					}		
					
			    	ReasonerFactory reasonerFactory = new ReasonerFactory();
					RationalRankingAlgorithm rankingalg = new RationalRankingAlgorithm(reasonerFactory, ontology);
					try {
						ranking = rankingalg.computeRanking();
						System.out.println(ranking);
					} catch (OWLException e) {
						System.out.println("Error computing ranking!");
						e.printStackTrace();
						System.exit(-1);
					}
			    }
		        
		        //Now get unique LHS concepts that are exceptional (not in rank 0)
		        
		        Set<OWLClassExpression> lhss = new HashSet<OWLClassExpression>();
		        for (int n = 0; n < ranking.getRanking().size();n++){
		        	if (n != ranking.getRanking().size()-1){
		        		Set<OWLAxiom> axioms = ranking.getRanking().get(n).getAxiomsAsSet();
		        		for (OWLAxiom a: axioms){
		        			OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
		        			lhss.add(sub.getSubClass());
		        		}
		        	}
		        }
		        
		        normalExceptionsPW.println(lhss.size());
			}
			
			normalExceptionsPW.close();
		}
}
