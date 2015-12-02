package net.za.cair.dip.experimentbench;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import net.za.cair.dip.model.Rank;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.transform.RationalRankingAlgorithm;
import net.za.cair.dip.util.ManchesterOWLSyntaxOWLObjectRendererImpl;
import net.za.cair.dip.util.Utility;

import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class RankingGeneratorMOWLRep { // NO_UCD (unused code)
	//private static ThreadMXBean mx;
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException{
		OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		OWLAnnotationProperty def = Utility.defeasibleAnnotationProperty;
		OWLAnnotation ann = df.getOWLAnnotation(def, df.getOWLLiteral(true));
		Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>();
		annos.add(ann);
			
		FileWriter rankingTimeW = new FileWriter("MOWLRep/Total_ranking_time.txt");
		PrintWriter rankingTimePW = new PrintWriter(rankingTimeW);
		
		FileWriter recursionsW = new FileWriter("MOWLRep/Total_number_of_recursions.txt");
		PrintWriter recursionsPW = new PrintWriter(recursionsW);
		
		FileWriter numranksW = new FileWriter("MOWLRep/Total_number_of_ranks.txt");
		PrintWriter numranksPW = new PrintWriter(numranksW);
		
		FileWriter exceptionalityChecksW = new FileWriter("MOWLRep/Total_number_of_exceptionality_checks.txt");
		PrintWriter exceptionalityChecksPW = new PrintWriter(exceptionalityChecksW);
		
		FileWriter defeasibleAxiomsW = new FileWriter("MOWLRep/Total_number_of_defeasible_axioms.txt");
		PrintWriter defeasibleAxiomsPW = new PrintWriter(defeasibleAxiomsW);
		
		FileWriter hiddenStrictAxiomsW = new FileWriter("MOWLRep/Total_number_of_hidden_strict_axioms.txt");
		PrintWriter hiddenStrictAxiomsPW = new PrintWriter(hiddenStrictAxiomsW);
		
		FileWriter rank0SizeW = new FileWriter("MOWLRep/Size_of_rank_0.txt");
		PrintWriter rank0SizePW = new PrintWriter(rank0SizeW);
		
		FileWriter singleRankSizeW = new FileWriter("MOWLRep/Average_size_of_single_rank.txt");
		PrintWriter singleRankSizePW = new PrintWriter(singleRankSizeW);
		
		FileWriter 	hiddenStrictAxiomsPerW = new FileWriter("MOWLRep/Average_number_of_hidden_strict_axioms_per_recursion.txt");
		PrintWriter hiddenStrictAxiomsPerPW = new PrintWriter(hiddenStrictAxiomsPerW);
		
		FileWriter unsatLHSDefeasibleSubsW = new FileWriter("MOWLRep/Total_unsatisfiable_LHS_concepts_of_defeasible_subsumptions.txt");
		PrintWriter unsatLHSDefeasibleSubsPW = new PrintWriter(unsatLHSDefeasibleSubsW);
		
		for (int j = 1; j <= 134;j++){
			
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
			defeasibleAxiomsPW.println(rankingalg.getOntologyStructure().dBox.getAxioms().size());
			
			Ranking r = null;

				//System.out.println(mx.isCurrentThreadCpuTimeSupported());
				long time = 0;
				long start = System.currentTimeMillis();//mx.getCurrentThreadCpuTime();
				System.out.print("computing ranking for ontology " + j + "...");
				try {
					r = rankingalg.computeRanking();
					System.out.println("done.");
				} catch (OWLException e1) {
					System.out.println("failed!");
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				time = System.currentTimeMillis() - start;
				Double rankingTime = new Double(time);
					
				// Store ranking to file
				ObjectOutputStream oos = null;
				
				System.out.print("writing ranking for ontology " + j + " to file...");
				FileOutputStream fout = new FileOutputStream("MOWLRep/"+j+"/ontranking.bin");
				oos = new ObjectOutputStream(fout); 
				boolean failedWriting = true;
				try{
					oos.writeObject(r);
					failedWriting = false;
					System.out.println("done.");
				}
				catch (IOException ioe){
					System.out.println("failed!");
					//ioe.printStackTrace();
				}
				oos.close();
				
				if (!failedWriting){
					// Test integrity of stored ranking
					FileInputStream fileIn = new FileInputStream("MOWLRep/"+j+"/ontranking.bin");
				    ObjectInputStream in = new ObjectInputStream(fileIn);
				    Ranking ranking = null;
				    try {
				        System.out.print("testing integrity of stored ranking file...");
						ranking = (Ranking) in.readObject();
						if (ranking.size() > 0 || ranking.getInfiniteRank().getAxioms().size() > 0){
							System.out.println("passed.");
						}
						else{
							System.out.println("failed!");
						}
					} catch (ClassNotFoundException e) {
						System.out.println("failed!");
						e.printStackTrace();
					}
				    in.close();
				}	
			        
				rankingTimePW.println(rankingTime);
					
				unsatLHSDefeasibleSubsPW.println(rankingalg.unsatLHSDefeasibleSubs);
					
				numranksPW.println(r.size());

				int indexOfRank0 = r.size()-1;
				if (indexOfRank0 >= 0)
					rank0SizePW.println(r.get(indexOfRank0).size());
				else
					rank0SizePW.println(0);
				
				recursionsPW.println(rankingalg.recursiveCount);
				exceptionalityChecksPW.println(rankingalg.entailmentChecks);
				hiddenStrictAxiomsPW.println(rankingalg.noOfBrokenAxioms);
					
				double totalAxioms = 0;
				for (Rank rank: r.getRanking()){
					totalAxioms += rank.getAxioms().size();
				}
					
				double avg = 0.0;
				if (r.size() > 0){
					avg = totalAxioms/r.size();
					singleRankSizePW.println(avg);
				}
				else{
					singleRankSizePW.println(0);
				}
					
				if (rankingalg.recursiveCount > 0){
					double avgPer = rankingalg.noOfBrokenAxioms/rankingalg.recursiveCount;
					hiddenStrictAxiomsPerPW.println(avgPer);
				}
				else{
					hiddenStrictAxiomsPerPW.println(0.0);
				}
					
	
		}
		unsatLHSDefeasibleSubsPW.close();
		rankingTimePW.close();
		numranksPW.close();
		defeasibleAxiomsPW.close();
		rank0SizePW.close();
		recursionsPW.close();
		exceptionalityChecksPW.close();
		hiddenStrictAxiomsPW.close();
		singleRankSizePW.close();
		hiddenStrictAxiomsPerPW.close();
	}
}
