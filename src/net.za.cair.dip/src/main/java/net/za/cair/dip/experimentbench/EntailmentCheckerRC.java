package net.za.cair.dip.experimentbench;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.za.cair.dip.DefeasibleInferenceComputer;
import net.za.cair.dip.model.Query;
import net.za.cair.dip.model.Rank;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.model.ReasoningType;
import net.za.cair.dip.transform.RationalRankingAlgorithm;
import net.za.cair.dip.util.Utility;

import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
//import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.api.ExplanationException;
//import org.semanticweb.owl.explanation.impl.blackbox.HittingSetBuilder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class EntailmentCheckerRC {
	
	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException, ExecutionException, ExplanationException, TimeoutException{
		OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		OWLAnnotationProperty def = Utility.defeasibleAnnotationProperty;
		OWLAnnotation ann = df.getOWLAnnotation(def, df.getOWLLiteral(true));
		Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>();
		annos.add(ann);
		
		for (int i = 1; i <= 10;i++){
			System.out.println("Percentage Defeasibility: " + (i*10) + "%");
			System.out.println();
			
		FileWriter avgQueryTimeRCW = new FileWriter("test2/"+(i*10)+"/Avg_query_time_RC.txt");
		PrintWriter avgQueryTimeRCPW = new PrintWriter(avgQueryTimeRCW);
			
		FileWriter avgEntailmentChecksRCW = new FileWriter("test2/"+(i*10)+"/Avg_entailment_checks_RC.txt");
		PrintWriter avgEntailmentChecksRCPW = new PrintWriter(avgEntailmentChecksRCW);
		
		FileWriter totalAxiomsInCCompatRCW = new FileWriter("test2/"+(i*10)+"/Avg_axioms_in_ccompat_RC.txt");
		PrintWriter totalAxiomsInCCompatRCPW = new PrintWriter(totalAxiomsInCCompatRCW);
		
		FileWriter yesAnswersRCW = new FileWriter("test2/"+(i*10)+"/Avg_number_of_yes_answers_RC.txt");
		PrintWriter yesAnswersRCPW = new PrintWriter(yesAnswersRCW);

		FileWriter infiniteRankQueryW = new FileWriter("test2/"+(i*10)+"/Avg_cases_of_infinite_rank_query.txt");
		PrintWriter infiniteRankQueryPW = new PrintWriter(infiniteRankQueryW);
			
		FileWriter prunedRankingSizeW = new FileWriter("test2/"+(i*10)+"/Avg_pruned_ranking_size.txt");
		PrintWriter prunedRankingSizePW = new PrintWriter(prunedRankingSizeW);
			
		FileWriter sizeOfPrunedRankW = new FileWriter("test2/"+(i*10)+"/Avg_size_of_pruned_rank.txt");
		PrintWriter sizeOfPrunedRankPW = new PrintWriter(sizeOfPrunedRankW);
			
		FileWriter ranksInCCompatW = new FileWriter("test2/"+(i*10)+"/Avg_ranks_in_ccompat.txt");
		PrintWriter ranksInCCompatPW = new PrintWriter(ranksInCCompatW);
		
		FileWriter numberOfStrictAxiomsW = new FileWriter("test2/"+(i*10)+"/Number_of_strict_axioms.txt");
		PrintWriter numberOfStrictAxiomsPW = new PrintWriter(numberOfStrictAxiomsW);
		
		FileWriter numberOfDefAxiomsW = new FileWriter("test2/"+(i*10)+"/Number_of_defeasible_axioms.txt");
		PrintWriter numberOfDefAxiomsPW = new PrintWriter(numberOfDefAxiomsW);
		
		for (int j = 1; j <= 35;j++){
			/*************** Load the ranking *******************/	
			System.out.print("loading the ranking for ontology " + j + "...");
			FileInputStream fileIn = new FileInputStream("test2/"+(i*10)+"/Ontology"+j+"/ontranking.bin");
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
		    }
		    in.close();
		    
		    if (failedLoading){
		    	// Compute the ranking
		    	// 1. First load the ontology
		    	/*********************** Create Ontology Manager **************************/
				OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();	
				File ontologyFile = new File("test2/"+(i*10)+"/Ontology" + j + "/ontology" + j + ".owl");
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
		    
		    Iterator<Rank> rankIter = ranking.getRanking().iterator();
	        
	        int defeasibleAxioms = 0;
	        int strictAxioms = 0;
	        Utility u = new Utility();
	        while (rankIter.hasNext()){
	        	Rank r = rankIter.next();
	        	Iterator<OWLAxiom> axiomIter = r.getAxioms().iterator();
	        	while (axiomIter.hasNext()){
	        		OWLAxiom a = axiomIter.next();
	        		if (u.isDefeasible(a))
	        			defeasibleAxioms++;
	        		else
	        			strictAxioms++;
	        	}
	        }
	        
	        strictAxioms += ranking.getInfiniteRank().getAxiomsAsSet().size();
	        
	        numberOfDefAxiomsPW.println(defeasibleAxioms);
	        numberOfStrictAxiomsPW.println(strictAxioms);
				
		    /*************** Setup the defeasible reasoner ******/
		    ReasonerFactory reasonerFactory = new ReasonerFactory();
		    DefeasibleInferenceComputer dic = new DefeasibleInferenceComputer(reasonerFactory, ranking);
			/****************************************************/
				
			/*************** Load the queries ********************/
		    System.out.print("loading the queries for ontology " + j + "...");
		    File queriesFile = new File("test2/"+(i*10)+"/Ontology" + j + "/queries.owl");
		    OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();	
			/*********************** Load Ontology (arg[0]) ************************************/
			OWLOntology qOnt = null;
			Set<OWLAxiom> queries = new HashSet<OWLAxiom>();
			try {
				System.out.print("loading ontology " + j + "...");
				qOnt = ontologyManager.loadOntologyFromOntologyDocument(queriesFile);
				queries.addAll(qOnt.getAxioms());
				System.out.println("done.");
			}
			catch (OWLOntologyCreationException e) {
				System.out.println("failed!");
			}		
			/*****************************************************/
				
			/*************** Perform the entailment checks *******/
			// Logging variables //
			double prunedRankingSize = 0.0;
			double sizeOfRankInPrunedRanking = 0.0;
			double ranksInCCompat = 0.0;	
			double entailmentChecks = 0.0;	
			double noOfInfiniteRankQueries = 0.0;
			double yesAnswers = 0.0;
			double axiomsInCCompat = 0.0;
			//******************//
		
			long totalTime = 0;
			System.out.println("performing entailment checks for ontology " + j + "...");
			System.out.println();
			
			System.out.println("Number of queries: " + queries.size());
			System.out.println();
			Iterator<OWLAxiom> iter = queries.iterator();
			int count = 0;
			while (iter.hasNext()){
				count++;
				System.out.print("check " + count + "...");
				Query currentQuery = new Query(iter.next(), ReasoningType.RATIONAL, true);
								
				long startTime = System.currentTimeMillis();
				long currentTime = 0; 
				boolean result = dic.isEntailed(currentQuery);
				currentTime = System.currentTimeMillis() - startTime;
			
				entailmentChecks += dic.entailmentChecksRC; 
				totalTime += currentTime;
				axiomsInCCompat += dic.axiomsInCCompatLR;
				
				if (result)
					yesAnswers++;
					
				if (dic.queryHasInfiniteRank)
					noOfInfiniteRankQueries++;
			
				prunedRankingSize += dic.prunedRankingSize;
				sizeOfRankInPrunedRanking += dic.avgSizeOfRankInPrunedRanking;
				ranksInCCompat += dic.noOfRanksInCCompatLR;
				
				System.out.println("done!");
			}

			System.out.print("...completed entailment checks for ontology " + j + " at: ");
			System.out.println();
			/******************** Log results ****************************/
			double avgQueryTime = 0.0;
			double qSize = count;
			
			avgQueryTime = totalTime/qSize;
			
			// Avg query time
			avgQueryTimeRCPW.println(avgQueryTime);
			
			// Avg entailment checks
			avgEntailmentChecksRCPW.println(entailmentChecks/qSize);
			
			// Avg infinite rank cases
			infiniteRankQueryPW.println((noOfInfiniteRankQueries/qSize)*100);
				
			// Avg pruned ranking size
			prunedRankingSizePW.println(prunedRankingSize/qSize);
				
			// Avg size of a rank in a pruned ranking
			sizeOfPrunedRankPW.println(sizeOfRankInPrunedRanking/qSize);
				
			// Avg number of ranks in a CCompatible subset of the ranking
			ranksInCCompatPW.println(ranksInCCompat/qSize);
				
			// Avg number of axioms in a CCompatible subset of the ranking
			totalAxiomsInCCompatRCPW.println(axiomsInCCompat/qSize);
						
			// Avg number of positive inferences (non-infinite rank ones)
			yesAnswersRCPW.println((yesAnswers/qSize)*100);
				
			java.util.Date date= new java.util.Date();
			System.out.println(new Timestamp(date.getTime()));
			System.out.println();
		}
		// Close the streams
		
		avgQueryTimeRCPW.close();
		avgEntailmentChecksRCPW.close();
		infiniteRankQueryPW.close();	
		prunedRankingSizePW.close();	
		sizeOfPrunedRankPW.close();	
		ranksInCCompatPW.close();	
		totalAxiomsInCCompatRCPW.close();	
		yesAnswersRCPW.close();
		numberOfStrictAxiomsPW.close();
		numberOfDefAxiomsPW.close();
		}
	}
}
