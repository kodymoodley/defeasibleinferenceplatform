package net.za.cair.dip.experimentbench;

import java.io.BufferedWriter;
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
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

public class EntailmentCheckerMOWLRepLEXREL {
	
	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException, ExecutionException, ExplanationException, TimeoutException{
		ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	    
		/*OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		OWLAnnotationProperty def = Utility.defeasibleAnnotationProperty;
		OWLAnnotation ann = df.getOWLAnnotation(def, df.getOWLLiteral(true));
		Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>();
		annos.add(ann);*/
		
			
		
		
		FileWriter avgQueryTimeLRW = new FileWriter("MOWLRep/Avg_query_time_LR.txt");
		PrintWriter avgQueryTimeLRPW = new PrintWriter(avgQueryTimeLRW);
		
		FileWriter avgQueryTimeMRW = new FileWriter("MOWLRep/Avg_query_time_MR.txt");
		PrintWriter avgQueryTimeMRPW = new PrintWriter(avgQueryTimeMRW);
		
		FileWriter avgQueryTimeBRW = new FileWriter("MOWLRep/Avg_query_time_BR.txt");
		PrintWriter avgQueryTimeBRPW = new PrintWriter(avgQueryTimeBRW);
		
		/*************************************************************************************************/
		FileWriter avgEntailmentChecksBRW = new FileWriter("MOWLRep/Avg_entailment_checks_BR.txt");
		PrintWriter avgEntailmentChecksBRPW = new PrintWriter(avgEntailmentChecksBRW);
		
		FileWriter avgEntailmentChecksLRW = new FileWriter("MOWLRep/Avg_entailment_checks_LR.txt");
		PrintWriter avgEntailmentChecksLRPW = new PrintWriter(avgEntailmentChecksLRW);
		
		FileWriter avgEntailmentChecksMRW = new FileWriter("MOWLRep/Avg_entailment_checks_MR.txt");
		PrintWriter avgEntailmentChecksMRPW = new PrintWriter(avgEntailmentChecksMRW);
		
		FileWriter numberOfTimeoutsW = new FileWriter("MOWLRep/Number_of_timeouts.txt");
		PrintWriter numberOfTimeoutsPW = new PrintWriter(numberOfTimeoutsW);
		
		FileWriter hsTreeSizeW = new FileWriter("MOWLRep/HST_Size.txt");
		PrintWriter hsTreeSizePW = new PrintWriter(hsTreeSizeW);
		
		/*FileWriter totalQueriesW = new FileWriter("MOWLRep/Total_queries.txt");
		PrintWriter totalQueriesPW = new PrintWriter(totalQueriesW);*/
		
		/**************************************************************************************************/
		
		/*FileWriter infiniteRankQueryW = new FileWriter("MOWLRep/Avg_cases_of_infinite_rank_query.txt");
		PrintWriter infiniteRankQueryPW = new PrintWriter(infiniteRankQueryW);
		
		FileWriter prunedRankingSizeW = new FileWriter("MOWLRep/Avg_pruned_ranking_size.txt");
		PrintWriter prunedRankingSizePW = new PrintWriter(prunedRankingSizeW);
		
		FileWriter sizeOfPrunedRankW = new FileWriter("MOWLRep/Avg_size_of_pruned_rank.txt");
		PrintWriter sizeOfPrunedRankPW = new PrintWriter(sizeOfPrunedRankW);
		
		FileWriter ranksInCCompatW = new FileWriter("MOWLRep/Avg_ranks_in_ccompat.txt");
		PrintWriter ranksInCCompatPW = new PrintWriter(ranksInCCompatW);*/
		
		FileWriter totalAxiomsInCCompatLRW = new FileWriter("MOWLRep/Avg_axioms_in_ccompat_LR.txt");
		PrintWriter totalAxiomsInCCompatLRPW = new PrintWriter(totalAxiomsInCCompatLRW);
		
		FileWriter totalAxiomsInCCompatMRW = new FileWriter("MOWLRep/Avg_axioms_in_ccompat_MR.txt");
		PrintWriter totalAxiomsInCCompatMRPW = new PrintWriter(totalAxiomsInCCompatMRW);
		
		FileWriter totalAxiomsInCCompatBRW = new FileWriter("MOWLRep/Avg_axioms_in_ccompat_BR.txt");
		PrintWriter totalAxiomsInCCompatBRPW = new PrintWriter(totalAxiomsInCCompatBRW);
		
		
		FileWriter yesAnswersLRW = new FileWriter("MOWLRep/Avg_number_of_yes_answers_LR.txt");
		PrintWriter yesAnswersLRPW = new PrintWriter(yesAnswersLRW);
		
		FileWriter yesAnswersMRW = new FileWriter("MOWLRep/Avg_number_of_yes_answers_MR.txt");
		PrintWriter yesAnswersMRPW = new PrintWriter(yesAnswersMRW);
		
		FileWriter yesAnswersBRW = new FileWriter("MOWLRep/Avg_number_of_yes_answers_BR.txt");
		PrintWriter yesAnswersBRPW = new PrintWriter(yesAnswersBRW);
		
		/*FileWriter problematicRankSizeW = new FileWriter("MOWLRep/Avg_problematic_rank_size.txt");
		PrintWriter problematicRankSizePW = new PrintWriter(problematicRankSizeW);*/
		
		FileWriter minCBasisSizeW = new FileWriter("MOWLRep/Avg_mincbasis_size.txt");
		PrintWriter minCBasisSizePW = new PrintWriter(minCBasisSizeW);
		
		FileWriter cBasisSizeW = new FileWriter("MOWLRep/Avg_cbasis_size.txt");
		PrintWriter cBasisSizePW = new PrintWriter(cBasisSizeW);
		
		FileWriter noOfJustsW = new FileWriter("MOWLRep/Avg_number_of_justifications.txt");
		PrintWriter noOfJustsPW = new PrintWriter(noOfJustsW);
		
		FileWriter avgSizeOfJustW = new FileWriter("MOWLRep/Avg_size_of_justification.txt");
		PrintWriter avgSizeOfJustPW = new PrintWriter(avgSizeOfJustW);
		
			
			
			/*FileWriter nonExceptionalQueryW = new FileWriter("MOWLRep/Avg_cases_of_nonexceptional_query.txt");
			PrintWriter nonExceptionalQueryPW = new PrintWriter(nonExceptionalQueryW);*/
			//int totalQueries = 0;
			for (int j = 134; j <= 134;j++){
				
				
				
				
				
				
				
				
				
				/*************** Load the ranking *******************/
				/*FileWriter strictTimedOutW = new FileWriter("timeouts/strict_axioms_in_timedout_cases.txt", true);
				PrintWriter strictTimedOutPW = new PrintWriter(new BufferedWriter(strictTimedOutW));
				
				FileWriter defTimedOutW = new FileWriter("timeouts/defeasible_axioms_in_timedout_cases.txt", true);
				PrintWriter defTimedOutPW = new PrintWriter(new BufferedWriter(defTimedOutW));*/
				
				/*FileWriter strictTimedOutW2 = new FileWriter("nontimeouts/strict_axioms_in_nontimedout_cases.txt", true);
				PrintWriter strictTimedOutPW2 = new PrintWriter(new BufferedWriter(strictTimedOutW2));
				
				FileWriter defTimedOutW2 = new FileWriter("nontimeouts/defeasible_axioms_in_nontimedout_cases.txt", true);
				PrintWriter defTimedOutPW2 = new PrintWriter(new BufferedWriter(defTimedOutW2));*/
				
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
		       // System.out.println(ranking);
		        /****************************************************/	
				
		        /*************** Setup the defeasible reasoner ******/
		        ReasonerFactory reasonerFactory = new ReasonerFactory();
				DefeasibleInferenceComputer dic_pass = new DefeasibleInferenceComputer(reasonerFactory, ranking);
				/****************************************************/
				
				/*************** Load the queries ********************/
				Set<OWLAxiom> queries = new HashSet<OWLAxiom>();
				// Load ontology containing queries
				OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();	
				File queriesFile = new File("MOWLRep/"+j+"/queries.owl");
				OWLOntology queriesOntology = null;
				try {System.out.print("loading queries for ontology " + j + "...");
					queriesOntology = ontologyManager.loadOntologyFromOntologyDocument(queriesFile);
					System.out.println("done.");
				}
				catch (OWLOntologyCreationException e) {
					System.out.println("failed!");
				}		
				queries.addAll(queriesOntology.getLogicalAxioms());
				/*****************************************************/
				
				/*************** Perform the entailment checks *******/
				// Logging variables //
				//double prunedRankingSize = 0.0;
				//double sizeOfRankInPrunedRanking = 0.0;
				//double ranksInCCompat = 0.0;
				double axiomsInCCompatLR = 0.0;
				double axiomsInCCompatMR = 0.0;
				double axiomsInCCompatBR = 0.0;
				
				double entailmentChecksLR = 0.0;
				double entailmentChecksMR = 0.0;
				double entailmentChecksBR = 0.0;
				
				
				double hsTreeSize = 0.0;
				//double noOfInfiniteRankQueries = 0.0;
				//double noOfNonExceptionalQueries = 0.0;
				double yesAnswersLR = 0.0;
				double yesAnswersMR = 0.0;
				double yesAnswersBR = 0.0;
				//double problematicRankAxioms = 0.0;
				double numJusts = 0.0;
				double justSize = 0.0;
				double mincbasisSize = 0.0;
				double cbasisSize = 0.0;
				//******************//
				
				
				long totalTimeLR = 0;
				long totalTimeMR = 0;
				long totalTimeBR = 0;
				double numTimeouts = 0.0;
				//long start = System.currentTimeMillis();
				System.out.println("performing entailment checks for ontology " + j + "...");
				System.out.println();
				int successfulChecks = 0;
				int checks = 0;
				Iterator<OWLAxiom> iter = queries.iterator();
				
				while (iter.hasNext() && checks < 10){
				//for (OWLAxiom q: queries){
					//totalQueries++;
					checks++;
					OWLAxiom axiom = iter.next();
					OWLSubClassOfAxiom s = (OWLSubClassOfAxiom)axiom;
					if (!s.getSubClass().isOWLNothing()){
					//if (checks > 1){
					System.out.print("check " + checks + "...");
					Query currentQuery = new Query(axiom, ReasoningType.LEX_RELEVANT, true);
					
					//ExecutorService executor = Executors.newSingleThreadExecutor();
					//Future<DefeasibleInferenceComputer> future = executor.submit(new EntailmentRunner(dic_pass, currentQuery));
					
					//boolean timeout = false;
					//DefeasibleInferenceComputer dic = null;
					//try{
					//long currentTime = 0;
					//long startTime = System.currentTimeMillis(); 
					dic_pass.isEntailed(currentQuery);
					//currentTime = System.currentTimeMillis() - startTime;
					
					//if (!timeout){
						hsTreeSize = dic_pass.hsTreeSize;
						
						entailmentChecksLR = dic_pass.entailmentChecksLR;
						entailmentChecksBR = dic_pass.entailmentChecksBR;
						entailmentChecksMR = dic_pass.entailmentChecksMR;
						
						totalTimeLR = dic_pass.lexreltime;
						totalTimeMR = dic_pass.minreltime;
						totalTimeBR = dic_pass.basicreltime;
						
						axiomsInCCompatLR = dic_pass.axiomsInCCompatLR + dic_pass.noOfAxiomsKeptFromProblematicRank;
						axiomsInCCompatMR = dic_pass.axiomsInCCompatMR;
						axiomsInCCompatBR = dic_pass.axiomsInCCompatBR;
						
						numJusts = dic_pass.noOfJusts;
						justSize = dic_pass.avgSizeOfAJust;
						mincbasisSize = dic_pass.mincbasisSize;
						cbasisSize = dic_pass.cbasisSize;
						
						successfulChecks++;
						
						//strictTimedOutPW2.println(dic_pass.numStrictAxioms);
						//defTimedOutPW2.println(dic_pass.numDefAxioms);
						
						if (!dic_pass.queryHasInfiniteRank){
							if (dic_pass.basicrelanswer){
								//System.out.println("yes.");
								//yesAnswersBR++;
								yesAnswersBRPW.println("yes");
							}
							else{
								yesAnswersBRPW.println("no");
							}
							
							if (dic_pass.lexrelanswer){
								//System.out.println("yes.");
								//yesAnswersLR++;
								yesAnswersLRPW.println("yes");
							}
							else{
								yesAnswersLRPW.println("no");
							}
							
							if (dic_pass.minrelanswer){
								//System.out.println("yes.");
								//yesAnswersMR++;
								yesAnswersMRPW.println("yes");
							}
							else{
								yesAnswersMRPW.println("no");
							}
						}
						
						
						
						avgQueryTimeLRPW.println(totalTimeLR);
						
						//avgQueryTimeMR = totalTimeMR/qSize;
						avgQueryTimeMRPW.println(totalTimeMR);
						
						//avgQueryTimeBR = totalTimeBR/qSize;
						avgQueryTimeBRPW.println(totalTimeBR);
						
						
						hsTreeSizePW.println(hsTreeSize);
						
						avgEntailmentChecksLRPW.println(entailmentChecksLR);
						avgEntailmentChecksBRPW.println(entailmentChecksBR);
						avgEntailmentChecksMRPW.println(entailmentChecksMR);
						
						// Avg infinite rank cases
						//infiniteRankQueryPW.println((noOfInfiniteRankQueries/queries.size())*100);
						
						// Avg non-exceptional cases
						//nonExceptionalQueryPW.println((noOfNonExceptionalQueries/queries.size())*100);
						
						// Avg pruned ranking size
						//prunedRankingSizePW.println(prunedRankingSize/queries.size());
						
						// Avg size of a rank in a pruned ranking
						//sizeOfPrunedRankPW.println(sizeOfRankInPrunedRanking/queries.size());
						
						// Avg number of ranks in a CCompatible subset of the ranking
						//ranksInCCompatPW.println(ranksInCCompat/queries.size());
						
						// Avg number of axioms in a CCompatible subset of the ranking
						totalAxiomsInCCompatLRPW.println(axiomsInCCompatLR);
						
						totalAxiomsInCCompatMRPW.println(axiomsInCCompatMR);
						
						totalAxiomsInCCompatBRPW.println(axiomsInCCompatBR);
						
						// Avg number of positive inferences (non-infinite rank ones)
						//(yesAnswersLR/qSize)*100);
						
						//yesAnswersMRPW.println((yesAnswersMR/qSize)*100);
						
						//yesAnswersBRPW.println((yesAnswersBR/qSize)*100);
						
						// Avg problematic rank size
						//problematicRankSizePW.println(problematicRankAxioms/queries.size());
						
						// Avg minCBasis size
						minCBasisSizePW.println(mincbasisSize);// = new PrintWriter(minCBasisSizeW);
						
						cBasisSizePW.println(cbasisSize);
						
						// Avg number of justs
						noOfJustsPW.println(numJusts);// = new PrintWriter(noOfJustsW);
						
						// Avg size of a just
						avgSizeOfJustPW.println(justSize);// = new PrintWriter(avgSizeOfJustW);
						
						
						
					}
					System.out.println("done!");
					//else{
						//strictTimedOutPW2.println(dic.numStrictAxioms);
						//defTimedOutPW2.println(dic.numDefAxioms);
					//}
				}
				
				//numberOfTimeoutsPW.println((numTimeouts/checks)*100);
	    		
				System.out.println("...completed entailment checks for ontology " + j + ".");
				System.out.println();
				/******************** Log results ****************************/
				// Avg query time
				/*double avgQueryTimeLR = 0.0;
				double avgQueryTimeMR = 0.0;
				double avgQueryTimeBR = 0.0;
				double qSize = (double) successfulChecks;//queries.size();
				if (successfulChecks > 0){
				avgQueryTimeLR = totalTimeLR/qSize;*/
				
				
				/*****************************************************/
				java.util.Date date= new java.util.Date();
				System.out.println(new Timestamp(date.getTime()));
				System.out.println();
				
				
			}
			
			avgQueryTimeLRPW.close();
			avgQueryTimeMRPW.close();
			avgQueryTimeBRPW.close();
			
			avgEntailmentChecksBRPW.close();
			avgEntailmentChecksLRPW.close();
			avgEntailmentChecksMRPW.close();
			hsTreeSizePW.close();
			numberOfTimeoutsPW.close();
		
			//infiniteRankQueryPW.close();
			
			//prunedRankingSizePW.close();
			
			//sizeOfPrunedRankPW.close();
			
			//ranksInCCompatPW.close();
			
			totalAxiomsInCCompatLRPW.close();
			totalAxiomsInCCompatMRPW.close();
			totalAxiomsInCCompatBRPW.close();
			
			yesAnswersLRPW.close();
			yesAnswersMRPW.close();
			yesAnswersBRPW.close();
			
			minCBasisSizePW.close();// = new PrintWriter(minCBasisSizeW);
			
			cBasisSizePW.close();
			
			noOfJustsPW.close();// = new PrintWriter(noOfJustsW);
			
			avgSizeOfJustPW.close();// = new PrintWriter(avgSizeOfJustW);
			//totalQueriesPW.println(totalQueries);
			// Close the streams
			//problematicRankSizePW.close();
			
			//nonExceptionalQueryPW.close();
			
			
			
			//totalQueriesPW.close();
			
		
	}
}
