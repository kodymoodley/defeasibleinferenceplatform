package net.za.cair.dip.experimentbench;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.Timestamp;
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
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class RankingGenerator { // NO_UCD (unused code)
	//private static ThreadMXBean mx;
	
	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException{
		//mx = ManagementFactory.getThreadMXBean();
		
		ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	    
		OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		OWLAnnotationProperty def = Utility.defeasibleAnnotationProperty;
		OWLAnnotation ann = df.getOWLAnnotation(def, df.getOWLLiteral(true));
		Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>();
		annos.add(ann);
		
		for (int i = 1; i <= 1;i++){
			System.out.println("Percentage Defeasibility: " + (i*10) + "%");
			System.out.println();
			FileWriter rankingTimeW = new FileWriter("test2/"+(i*10)+"/Total_ranking_time.txt");
			PrintWriter rankingTimePW = new PrintWriter(rankingTimeW);
			
			FileWriter recursionsW = new FileWriter("test2/"+(i*10)+"/Total_number_of_recursions.txt");
			PrintWriter recursionsPW = new PrintWriter(recursionsW);
			
			FileWriter numranksW = new FileWriter("test2/"+(i*10)+"/Total_number_of_ranks.txt");
			PrintWriter numranksPW = new PrintWriter(numranksW);
			
			FileWriter exceptionalityChecksW = new FileWriter("test2/"+(i*10)+"/Total_number_of_exceptionality_checks.txt");
			PrintWriter exceptionalityChecksPW = new PrintWriter(exceptionalityChecksW);
			
			FileWriter defeasibleAxiomsW = new FileWriter("test2/"+(i*10)+"/Total_number_of_defeasible_axioms.txt");
			PrintWriter defeasibleAxiomsPW = new PrintWriter(defeasibleAxiomsW);
			
			FileWriter hiddenStrictAxiomsW = new FileWriter("test2/"+(i*10)+"/Total_number_of_hidden_strict_axioms.txt");
			PrintWriter hiddenStrictAxiomsPW = new PrintWriter(hiddenStrictAxiomsW);
			
			FileWriter rank0SizeW = new FileWriter("test2/"+(i*10)+"/Size_of_rank_0.txt");
			PrintWriter rank0SizePW = new PrintWriter(rank0SizeW);
			
			FileWriter singleRankSizeW = new FileWriter("test2/"+(i*10)+"/Average_size_of_single_rank.txt");
			PrintWriter singleRankSizePW = new PrintWriter(singleRankSizeW);
			
			FileWriter 	hiddenStrictAxiomsPerW = new FileWriter("test2/"+(i*10)+"/Average_number_of_hidden_strict_axioms_per_recursion.txt");
			PrintWriter hiddenStrictAxiomsPerPW = new PrintWriter(hiddenStrictAxiomsPerW);
			
			FileWriter unsatLHSDefeasibleSubsW = new FileWriter("test2/"+(i*10)+"/Total_unsatisfiable_LHS_concepts_of_defeasible_subsumptions.txt");
			PrintWriter unsatLHSDefeasibleSubsPW = new PrintWriter(unsatLHSDefeasibleSubsW);
			
			FileWriter normalExceptionsW = new FileWriter("test2/"+(i*10)+"/normalExceptions.txt");
			PrintWriter normalExceptionsPW = new PrintWriter(normalExceptionsW);
			
			for (int j = 1; j <= 35;j++){
				
				/*********************** Create Ontology Manager **************************/
				OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();	
				File ontologyFile = new File("test2/" + (i*10) + "/Ontology" + j + "/ontology" + j + ".owl");
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
				try {
					//System.out.println(mx.isCurrentThreadCpuTimeSupported());
					long time = 0;
					long start = System.currentTimeMillis();//mx.getCurrentThreadCpuTime();
					System.out.print("computing ranking for ontology " + j + "...");
					r = rankingalg.computeRanking();
					time = System.currentTimeMillis() - start;
					Double rankingTime = new Double(time);
					System.out.print("done at: ");
					java.util.Date date= new java.util.Date();
					System.out.println(new Timestamp(date.getTime()));
					System.out.println();
					System.out.println("ranks: " + r.size());

			        //Now get unique LHS concepts that are exceptional (not in rank 0)
			        
			        Set<OWLClassExpression> lhss = new HashSet<OWLClassExpression>();
			        for (int n = 0; n < r.getRanking().size();n++){
			        	if (n != r.getRanking().size()-1){
			        		Set<OWLAxiom> axioms = r.getRanking().get(n).getAxiomsAsSet();
			        		for (OWLAxiom a: axioms){
			        			OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
			        			lhss.add(sub.getSubClass());
			        		}
			        	}
			        }
			        
			        normalExceptionsPW.println(lhss.size());
					
					// Store ranking to file
					try{
						System.out.print("writing ranking for ontology " + j + " to file...");
						FileOutputStream fout = new FileOutputStream("test2/"+(i*10)+"/Ontology"+j+"/ontranking.bin");
						ObjectOutputStream oos = new ObjectOutputStream(fout);   
						oos.writeObject(r);
						oos.close();
						System.out.println("done.");
				    }
					catch(Exception ex){
						System.out.println("failed!");
				    	//ex.printStackTrace();
				    }	
					
					// Test integrity of stored ranking
					FileInputStream fileIn = new FileInputStream("test2/"+(i*10)+"/Ontology"+j+"/ontranking.bin");
			        ObjectInputStream in = new ObjectInputStream(fileIn);
			        Ranking ranking = null;
			        try {
			        	System.out.print("testing integrity of stored ranking file...");
						ranking = (Ranking) in.readObject();
						if (ranking.size() > 0){
							System.out.println("passed.");
						}
					} catch (ClassNotFoundException e) {
						System.out.println("failed!");
						e.printStackTrace();
					}
			        in.close();
			        
					
					
					
					rankingTimePW.println(rankingTime/1000);
					
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
					
					double avg = totalAxioms/r.size();
					singleRankSizePW.println(avg);
					
					if (rankingalg.recursiveCount > 0){
						double avgPer = rankingalg.noOfBrokenAxioms/rankingalg.recursiveCount;
						hiddenStrictAxiomsPerPW.println(avgPer);
					}
					else{
						hiddenStrictAxiomsPerPW.println(0.0);
					}
					
				} catch (OWLException e) {
					System.out.println("something went wrong!");
					e.printStackTrace();
				}
				
				/*System.out.println(j+":");
				boolean inconsistent = true;
				OWLOntologyManager ontManager = OWLManager.createOWLOntologyManager();
				File file = new File("gen/" + (i*10) + "/ontology" + j + "/ontology" + j + ".owl");
				IRI ontologyIRI = IRI.create(file.toURI());
				OWLXMLOntologyFormat owlxmlFormat = new OWLXMLOntologyFormat();
				OWLOntology ontology = null;*/
				
				
				
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
			normalExceptionsPW.close();
		}
	}
}
