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
import java.util.concurrent.TimeoutException;

import net.za.cair.dip.DefeasibleInferenceComputer;
import net.za.cair.dip.model.Query;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.model.ReasoningType;
import net.za.cair.dip.transform.RationalRankingAlgorithm;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
//import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.api.ExplanationException;
//import org.semanticweb.owl.explanation.impl.blackbox.HittingSetBuilder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

public class EntailmentCheckerLEXREL {
	
	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException, ExecutionException, ExplanationException, TimeoutException{
		ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	    
		/*OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		OWLAnnotationProperty def = Utility.defeasibleAnnotationProperty;
		OWLAnnotation ann = df.getOWLAnnotation(def, df.getOWLLiteral(true));
		Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>();
		annos.add(ann);*/
			
			/*FileWriter nonExceptionalQueryW = new FileWriter("MOWLRep/Avg_cases_of_nonexceptional_query.txt");
			PrintWriter nonExceptionalQueryPW = new PrintWriter(nonExceptionalQueryW);*/
			//int totalQueries = 0;
			for (int i = 10; i <= 10;i++){
				
				
				FileWriter avgQueryTimeLRW = new FileWriter("timeouts/" + (i*10) + "/Avg_query_time_LR.txt");
				PrintWriter avgQueryTimeLRPW = new PrintWriter(avgQueryTimeLRW);
				
				FileWriter avgQueryTimeMRW = new FileWriter("timeouts/" + (i*10) + "/Avg_query_time_MR.txt");
				PrintWriter avgQueryTimeMRPW = new PrintWriter(avgQueryTimeMRW);
				
				FileWriter avgQueryTimeBRW = new FileWriter("timeouts/" + (i*10) + "/Avg_query_time_BR.txt");
				PrintWriter avgQueryTimeBRPW = new PrintWriter(avgQueryTimeBRW);
				
				/*************************************************************************************************/
				FileWriter avgEntailmentChecksBRW = new FileWriter("timeouts/" + (i*10) + "/Avg_entailment_checks_BR.txt");
				PrintWriter avgEntailmentChecksBRPW = new PrintWriter(avgEntailmentChecksBRW);
				
				FileWriter avgEntailmentChecksLRW = new FileWriter("timeouts/" + (i*10) + "/Avg_entailment_checks_LR.txt");
				PrintWriter avgEntailmentChecksLRPW = new PrintWriter(avgEntailmentChecksLRW);
				
				FileWriter avgEntailmentChecksMRW = new FileWriter("timeouts/" + (i*10) + "/Avg_entailment_checks_MR.txt");
				PrintWriter avgEntailmentChecksMRPW = new PrintWriter(avgEntailmentChecksMRW);
				
				/*FileWriter numberOfTimeoutsW = new FileWriter("timeouts/" + (i*10) + "/Number_of_timeouts.txt");
				PrintWriter numberOfTimeoutsPW = new PrintWriter(numberOfTimeoutsW);*/
				
				FileWriter hsTreeSizeW = new FileWriter("timeouts/" + (i*10) + "/HST_Size.txt");
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
				
				FileWriter totalAxiomsInCCompatLRW = new FileWriter("timeouts/" + (i*10) + "/Avg_axioms_in_ccompat_LR.txt");
				PrintWriter totalAxiomsInCCompatLRPW = new PrintWriter(totalAxiomsInCCompatLRW);
				
				FileWriter totalAxiomsInCCompatMRW = new FileWriter("timeouts/" + (i*10) + "/Avg_axioms_in_ccompat_MR.txt");
				PrintWriter totalAxiomsInCCompatMRPW = new PrintWriter(totalAxiomsInCCompatMRW);
				
				FileWriter totalAxiomsInCCompatBRW = new FileWriter("timeouts/" + (i*10) + "/Avg_axioms_in_ccompat_BR.txt");
				PrintWriter totalAxiomsInCCompatBRPW = new PrintWriter(totalAxiomsInCCompatBRW);
				
				
				FileWriter yesAnswersLRW = new FileWriter("timeouts/" + (i*10) + "/Avg_number_of_yes_answers_LR.txt");
				PrintWriter yesAnswersLRPW = new PrintWriter(yesAnswersLRW);
				
				FileWriter yesAnswersMRW = new FileWriter("timeouts/" + (i*10) + "/Avg_number_of_yes_answers_MR.txt");
				PrintWriter yesAnswersMRPW = new PrintWriter(yesAnswersMRW);
				
				FileWriter yesAnswersBRW = new FileWriter("timeouts/" + (i*10) + "/Avg_number_of_yes_answers_BR.txt");
				PrintWriter yesAnswersBRPW = new PrintWriter(yesAnswersBRW);
				
				/*FileWriter problematicRankSizeW = new FileWriter("timeouts/" + (i*10) + "/Avg_problematic_rank_size.txt");
				PrintWriter problematicRankSizePW = new PrintWriter(problematicRankSizeW);*/
				
				FileWriter minCBasisSizeW = new FileWriter("timeouts/" + (i*10) + "/Avg_mincbasis_size.txt");
				PrintWriter minCBasisSizePW = new PrintWriter(minCBasisSizeW);
				
				FileWriter cBasisSizeW = new FileWriter("timeouts/" + (i*10) + "/Avg_cbasis_size.txt");
				PrintWriter cBasisSizePW = new PrintWriter(cBasisSizeW);
				
				FileWriter noOfJustsW = new FileWriter("timeouts/" + (i*10) + "/Avg_number_of_justifications.txt");
				PrintWriter noOfJustsPW = new PrintWriter(noOfJustsW);
				
				FileWriter avgSizeOfJustW = new FileWriter("timeouts/" + (i*10) + "/Avg_size_of_justification.txt");
				PrintWriter avgSizeOfJustPW = new PrintWriter(avgSizeOfJustW);
				
				
				System.out.println("Percentage Defeasibility: " + (i*10) + "%");
				System.out.println();
				File folder = new File("timeouts/" + (i*10));//path");
				int len = folder.listFiles().length - 18;
				
			for (int j = 1; j <= 3;j++){
				
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
				FileInputStream fileIn = new FileInputStream("timeouts/"+(i*10)+"/Ontology"+j+"/ontranking.bin");
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
					File ontologyFile = new File("timeouts/"+(i*10)+"/Ontology" + j + "/ontology" + j + ".owl");
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
			    System.out.print("loading the queries for ontology " + j + "...");
			    File queriesFile = new File("timeouts/"+(i*10)+"/Ontology" + j + "/queries.owl");
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
				int count = 0; 
				while (iter.hasNext() && count <= 6){
					count++;
				//for (OWLAxiom q: queries){
					//totalQueries++;
					OWLAxiom axiom = iter.next();
					//OWLSubClassOfAxiom s = (OWLSubClassOfAxiom)axiom;
					//System.out.println(man.render(s.getSubClass()));
					//if (s.getSubClass().toString().contains("C12") && s.getSubClass().toString().contains("R18") && s.getSubClass().toString().contains("C106")){
					//if (s.getSubClass().toString().contains("C200")){
						if (count == 4){
						System.out.println(man.render(axiom));
						checks++;
						System.out.print("check " + checks + "...");
						Query currentQuery = new Query(axiom, ReasoningType.LEX_RELEVANT, true);
						
						
						//ExecutorService executor = Executors.newSingleThreadExecutor();
						//Future<DefeasibleInferenceComputer> future = executor.submit(new EntailmentRunner(dic_pass, currentQuery));
						
						dic_pass.isEntailed(currentQuery);
						//boolean timeout = false;
						//DefeasibleInferenceComputer dic = null;
						//try{
						//	dic = future.get(30, TimeUnit.SECONDS);
						//}
						//catch (TimeoutException e){
						//	future.cancel(true);
						//	System.out.println("time out.");
						//	timeout = true;
						//	numTimeouts++;
						//} catch (InterruptedException e) {
						//	System.out.println("interrupted.");
							// TODO Auto-generated catch block
						//	e.printStackTrace();
						//}
						//catch (ExecutionException e){
						//	System.out.println("exec");
							//e.printStackTrace();
						//	timeout = true;
						//}
						//finally{
						//	future.cancel(true);
						//	executor.shutdownNow();
						//}
						
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
							
							//successfulChecks++;
							
							//strictTimedOutPW2.println(dic.numStrictAxioms);
							//defTimedOutPW2.println(dic.numDefAxioms);
							
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
							
							
							
						//}
						System.out.println("done!");
						}
						//else{
							//strictTimedOutPW2.println(dic.numStrictAxioms);
							//defTimedOutPW2.println(dic.numDefAxioms);
						//}
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
				avgQueryTimeLR = totalTimeLR/qSize;
				
				
				}
				else{
					//avgQueryTime = totalTime/qSize;
					avgQueryTimeLRPW.println("all checks timed out");
					avgQueryTimeMRPW.println("all checks timed out");
					avgQueryTimeBRPW.println("all checks timed out");
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
					totalAxiomsInCCompatLRPW.println("all checks timed out");
					totalAxiomsInCCompatMRPW.println("all checks timed out");
					totalAxiomsInCCompatBRPW.println("all checks timed out");
					
					// Avg number of positive inferences (non-infinite rank ones)
					yesAnswersLRPW.println("all checks timed out");
					yesAnswersMRPW.println("all checks timed out");
					yesAnswersBRPW.println("all checks timed out");
					
					// Avg problematic rank size
					//problematicRankSizePW.println(problematicRankAxioms/queries.size());
					
					// Avg minCBasis size
					minCBasisSizePW.println("all checks timed out");// = new PrintWriter(minCBasisSizeW);
					cBasisSizePW.println("all checks timed out");
					
					// Avg number of justs
					noOfJustsPW.println("all checks timed out");// = new PrintWriter(noOfJustsW);
					
					// Avg size of a just
					avgSizeOfJustPW.println("all checks timed out");// = new PrintWriter(avgSizeOfJustW);
				}
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
			//numberOfTimeoutsPW.close();
		
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
			
			//totalQueriesPW.close();
			}
			//totalQueriesPW.println(totalQueries);
			// Close the streams
			//problematicRankSizePW.close();
			
			//nonExceptionalQueryPW.close();
			
			
			
		
	}
}
