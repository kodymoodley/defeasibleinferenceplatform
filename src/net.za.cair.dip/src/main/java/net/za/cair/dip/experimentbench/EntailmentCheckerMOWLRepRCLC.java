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

public class EntailmentCheckerMOWLRepRCLC {
	
	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException, ExecutionException, ExplanationException, TimeoutException{
		OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		OWLAnnotationProperty def = Utility.defeasibleAnnotationProperty;
		OWLAnnotation ann = df.getOWLAnnotation(def, df.getOWLLiteral(true));
		Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>();
		annos.add(ann);
		
		/*FileWriter avgQueryTimeRCW = new FileWriter("MOWLRep/Avg_query_time_RC.txt");
		PrintWriter avgQueryTimeRCPW = new PrintWriter(avgQueryTimeRCW);*/
			
		FileWriter avgQueryTimeLCW = new FileWriter("MOWLRep/Avg_query_time_LC.txt");
		PrintWriter avgQueryTimeLCPW = new PrintWriter(avgQueryTimeLCW);
			
		/*FileWriter avgEntailmentChecksRCW = new FileWriter("MOWLRep/Avg_entailment_checks_RC.txt");
		PrintWriter avgEntailmentChecksRCPW = new PrintWriter(avgEntailmentChecksRCW);*/
			
		FileWriter avgEntailmentChecksLCW = new FileWriter("MOWLRep/Avg_entailment_checks_LC.txt");
		PrintWriter avgEntailmentChecksLCPW = new PrintWriter(avgEntailmentChecksLCW);
		
		/*FileWriter totalAxiomsInCCompatRCW = new FileWriter("MOWLRep/Avg_axioms_in_ccompat_RC.txt");
		PrintWriter totalAxiomsInCCompatRCPW = new PrintWriter(totalAxiomsInCCompatRCW);*/
			
		FileWriter totalAxiomsInCCompatLCW = new FileWriter("MOWLRep/Avg_axioms_in_ccompat_LC.txt");
		PrintWriter totalAxiomsInCCompatLCPW = new PrintWriter(totalAxiomsInCCompatLCW);
		
		/*FileWriter yesAnswersRCW = new FileWriter("MOWLRep/Avg_number_of_yes_answers_RC.txt");
		PrintWriter yesAnswersRCPW = new PrintWriter(yesAnswersRCW);*/
			
		FileWriter yesAnswersLCW = new FileWriter("MOWLRep/Avg_number_of_yes_answers_LC.txt");
		PrintWriter yesAnswersLCPW = new PrintWriter(yesAnswersLCW);
		
		FileWriter timeoutsW = new FileWriter("MOWLRep/timeouts_LC.txt");
		PrintWriter timeoutsPW = new PrintWriter(timeoutsW);
		
			
		/*FileWriter infiniteRankQueryW = new FileWriter("MOWLRep/Avg_cases_of_infinite_rank_query.txt");
		PrintWriter infiniteRankQueryPW = new PrintWriter(infiniteRankQueryW);
			
		FileWriter prunedRankingSizeW = new FileWriter("MOWLRep/Avg_pruned_ranking_size.txt");
		PrintWriter prunedRankingSizePW = new PrintWriter(prunedRankingSizeW);
			
		FileWriter sizeOfPrunedRankW = new FileWriter("MOWLRep/Avg_size_of_pruned_rank.txt");
		PrintWriter sizeOfPrunedRankPW = new PrintWriter(sizeOfPrunedRankW);
			
		FileWriter ranksInCCompatW = new FileWriter("MOWLRep/Avg_ranks_in_ccompat.txt");
		PrintWriter ranksInCCompatPW = new PrintWriter(ranksInCCompatW);
	
		FileWriter nonExceptionalQueryW = new FileWriter("MOWLRep/Avg_cases_of_nonexceptional_query.txt");
		PrintWriter nonExceptionalQueryPW = new PrintWriter(nonExceptionalQueryW);
		
		FileWriter numberOfStrictAxiomsW = new FileWriter("MOWLRep/Number_of_strict_axioms.txt");
		PrintWriter numberOfStrictAxiomsPW = new PrintWriter(numberOfStrictAxiomsW);
		
		FileWriter numberOfDefAxiomsW = new FileWriter("MOWLRep/Number_of_defeasible_axioms.txt");
		PrintWriter numberOfDefAxiomsPW = new PrintWriter(numberOfDefAxiomsW);*/
		
		FileWriter problematicRankSizeW = new FileWriter("MOWLRep/Problematic_rank_size.txt");
		PrintWriter problematicRankSizePW = new PrintWriter(problematicRankSizeW);
		//int totalQ = 0;
		for (int j = 134; j <= 134;j++){
			/*************** Load the ranking *******************/	
			System.out.print("loading the ranking for ontology " + j + "...");
			FileInputStream fileIn = new FileInputStream("MOWLRep/"+j+"/ontranking.bin");
		    ObjectInputStream in = new ObjectInputStream(fileIn);
		    Ranking ranking = null;
		    boolean failedLoading = true;
		    try{
		        ranking = (Ranking) in.readObject();
		        failedLoading = false;
		        System.out.println("done.");
		        //System.out.println(ranking);
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
		    
		    /*Iterator<Rank> rankIter = ranking.getRanking().iterator();
	        
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
	        numberOfStrictAxiomsPW.println(strictAxioms);*/
	        
		    /****************************************************/	
				
		    /*************** Setup the defeasible reasoner ******/
		    //System.out.println("here");
		    ReasonerFactory reasonerFactory = new ReasonerFactory();
		    DefeasibleInferenceComputer dic_pass = new DefeasibleInferenceComputer(reasonerFactory, ranking);
		    //System.out.println("here2");
			/****************************************************/
				
			/*************** Load the queries ********************/
		    System.out.print("loading the queries for ontology " + j + "...");
		    File queriesFile = new File("MOWLRep/" + j + "/queries.owl");
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
			//double prunedRankingSize = 0.0;
			//double sizeOfRankInPrunedRanking = 0.0;
			//double ranksInCCompat = 0.0;	
			double entailmentChecks = 0.0;	
			//double noOfInfiniteRankQueries = 0.0;
			//double noOfNonExceptionalQueries = 0.0;
			double yesAnswers = 0.0;
			double problematicRankAxioms = 0.0;
			double axiomsInCCompat = 0.0;
			double timedOutProblematic = 0.0;
			//******************//
				
				
			long totalTime = 0;
			System.out.println("performing entailment checks for ontology " + j + "...");
			System.out.println();
			
			System.out.println("Number of queries: " + queries.size());
			System.out.println();
			Iterator<OWLAxiom> iter = queries.iterator();
			int count = 0;
			double numTimeouts = 0.0;
			int processedQueryCount = 0;
			while (iter.hasNext() && count < 3){
				count++;
				System.out.print("check " + count + "...");
				Query currentQuery = new Query(iter.next(), ReasoningType.LEXICOGRAPHIC, true);
				
				//ExecutorService executor = Executors.newSingleThreadExecutor();
				//Future<DefeasibleInferenceComputer> future = executor.submit(new EntailmentRunner(dic_pass, currentQuery));
				long currentTime = 0;
				long startTime = System.currentTimeMillis(); 
				dic_pass.isEntailed(currentQuery);
				currentTime = System.currentTimeMillis() - startTime;
				
				/*boolean timeout = false;
				DefeasibleInferenceComputer dic = null;
				long startTime = System.currentTimeMillis();
				long currentTime = 0; 
				try{
					dic = future.get(100, TimeUnit.SECONDS);
					currentTime = System.currentTimeMillis() - startTime;
				}
				catch (TimeoutException e){
					future.cancel(true);
					System.out.println("time out.");
					timeout = true;
					numTimeouts++;
				} catch (InterruptedException e) {
					System.out.println("interrupted.");
					e.printStackTrace();
				}
				executor.shutdownNow();*/
				
				//boolean result = dic.isEntailed(currentQuery);
				//boolean result = false;
				//if (!timeout && !dic.queryIsNonExceptional){
					processedQueryCount++;
					entailmentChecks += dic_pass.entailmentChecksLC; 
					totalTime += currentTime;
					axiomsInCCompat += dic_pass.axiomsInCCompatLR;
				
					if (dic_pass.lcResult)
						yesAnswers++;
					
					/*if (dic.queryHasInfiniteRank)
						noOfInfiniteRankQueries++;
				
					if (dic.queryIsNonExceptional)
						noOfNonExceptionalQueries++;*/
				
					//prunedRankingSize += dic.prunedRankingSize;
					//sizeOfRankInPrunedRanking += dic.avgSizeOfRankInPrunedRanking;
					//ranksInCCompat += dic.noOfRanksInCCompatLR;
				
					problematicRankAxioms += dic_pass.pRank.size();
				//}
				System.out.println("done!");
			}
			//totalQ += count;//queries.size();	
			System.out.print("...completed entailment checks for ontology " + j + " at: ");
			System.out.println();
			/******************** Log results ****************************/
			double avgQueryTime = 0.0;
			double qSize = processedQueryCount;//(double) queries.size();
			
			avgQueryTime = totalTime/qSize;
			
			avgQueryTimeLCPW.println(avgQueryTime);
			
			avgEntailmentChecksLCPW.println(entailmentChecks/qSize);
			
			timeoutsPW.println((numTimeouts/count)*100);
			// Avg infinite rank cases
			//infiniteRankQueryPW.println((noOfInfiniteRankQueries/qSize)*100);
				
			// Avg non-exceptional cases
			//nonExceptionalQueryPW.println((noOfNonExceptionalQueries/qSize)*100);
				
			// Avg pruned ranking size
			//prunedRankingSizePW.println(prunedRankingSize/qSize);
				
			// Avg size of a rank in a pruned ranking
			//sizeOfPrunedRankPW.println(sizeOfRankInPrunedRanking/qSize);
				
			// Avg number of ranks in a CCompatible subset of the ranking
			//ranksInCCompatPW.println(ranksInCCompat/qSize);
				
			// Avg number of axioms in a CCompatible subset of the ranking
			totalAxiomsInCCompatLCPW.println(axiomsInCCompat/qSize);
						
			// Avg number of positive inferences (non-infinite rank ones)
			yesAnswersLCPW.println((yesAnswers/qSize)*100);
				
			// Avg problematic rank size
			problematicRankSizePW.println(problematicRankAxioms/qSize);
			
			
			java.util.Date date= new java.util.Date();
			System.out.println(new Timestamp(date.getTime()));
			System.out.println();
		}
		//System.out.println("total queries: " + totalQ);
		// Close the streams
		problematicRankSizePW.close();
			
		//nonExceptionalQueryPW.close();	
		
		//avgQueryTimeRCPW.close();
		avgQueryTimeLCPW.close();
		timeoutsPW.close();
		
		avgEntailmentChecksLCPW.close();
		//avgEntailmentChecksRCPW.close();
		
		//infiniteRankQueryPW.close();
			
		//prunedRankingSizePW.close();
			
		//sizeOfPrunedRankPW.close();
			
		//ranksInCCompatPW.close();
			
		//totalAxiomsInCCompatRCPW.close();
		
		totalAxiomsInCCompatLCPW.close();
			
		//yesAnswersRCPW.close();
		yesAnswersLCPW.close();
		
		//numberOfStrictAxiomsPW.close();
		
		//numberOfDefAxiomsPW.close();
	}
}
