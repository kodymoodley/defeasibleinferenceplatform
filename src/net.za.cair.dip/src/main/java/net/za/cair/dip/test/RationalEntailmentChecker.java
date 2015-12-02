package net.za.cair.dip.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import net.za.cair.dip.DefeasibleInferenceComputer;
import net.za.cair.dip.model.Query;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.model.ReasoningType;




import org.semanticweb.owl.explanation.api.ExplanationException;
//import org.semanticweb.HermiT.Reasoner;
//import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

public class RationalEntailmentChecker{
	private static ThreadMXBean mx;
	//private static IRI defeasibleIRI = IRI.create("http://cair.meraka.org.za/defeasible");
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws OWLException 
	 * @throws ClassNotFoundException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws TimeoutException 
	 * @throws ExplanationException 
	 */
	public static void main(String[] args) throws IOException, OWLException, ClassNotFoundException, InterruptedException, ExecutionException, ExplanationException, TimeoutException {	
		ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		mx = ManagementFactory.getThreadMXBean();
		
		for (int j = 1; j <= 10;j++){
			System.out.println();
			System.out.println((j*10) + " percent");
			System.out.println("------------------");
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			FileWriter fw = new FileWriter("test/"+j*10+"/averageTimes_unmod.txt");
			PrintWriter pw = new PrintWriter(fw);
			long fullTotalTime = 0;
			for (int i = 1;i <= 35;i++){				
				System.out.print("Ontology " + i + ": ");				
				/***QUERIES***/
				Set<OWLAxiom> totalQueries = new HashSet<OWLAxiom>();
				File queriesFile = new File("test/"+(j*10)+"/Ontology"+i+"/TBoxQueries/onto"+i+"_queries.owl");							
				OWLOntology queryOntology = manager.loadOntologyFromOntologyDocument(queriesFile);			
				totalQueries.addAll(queryOntology.getAxioms());									
			
				/***RANKING***/
				//Ranking ranking = new Ranking();
				
				FileInputStream fis = new FileInputStream("test/"+j*10+"/Ontology"+i+"/ranking.bin");
	            ObjectInputStream ois = new ObjectInputStream(fis);
	            Ranking readRanking = (Ranking) ois.readObject();
	            ois.close();	            	           
			
	            //Load ontology and setup reasoner
	            File ontoFile = new File("test/"+(j*10)+"/Ontology"+i+"/ontology"+i+".owl");
				OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontoFile);
	            //OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();	    		
	    		DefeasibleInferenceComputer dic = null;//new DefeasibleInferenceComputer(reasonerFactory, ontology);
	    		
	    		long totalTime = 0;
	    		int index = 1;
	    		for (OWLAxiom axiom: totalQueries){
	    			Query query = new Query(axiom, ReasoningType.RATIONAL, readRanking, true);
	    			long start = mx.getCurrentThreadCpuTime();
	    			dic.isEntailed(query);
	    			long end = mx.getCurrentThreadCpuTime();
	    			totalTime += (end - start);
	    			System.out.print(index + " ");
	    			index++;
	    		}	    	
	    		System.out.println();
	    		double avgTime = totalTime / totalQueries.size();
	    		fullTotalTime += avgTime;  
	    		pw.println(avgTime/1000000);	    			    		
	    		if (i == 35){
	    			double totalAvg = fullTotalTime / 35;
	    			pw.println(totalAvg/1000000);
	    		}
			}				
			pw.close();
			fw.close();
		}
		
		System.out.println("Finished.");
		
	}
}
