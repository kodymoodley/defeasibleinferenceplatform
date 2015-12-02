package net.za.cair.dip.experimentbench;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.za.cair.dip.DefeasibleInferenceComputer;
import net.za.cair.dip.model.Query;
import net.za.cair.dip.model.Rank;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.model.ReasoningType;
import net.za.cair.dip.util.Utility;

import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class EntailmentCheckerRCLC {
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException{
		OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		OWLAnnotationProperty def = Utility.defeasibleAnnotationProperty;
		OWLAnnotation ann = df.getOWLAnnotation(def, df.getOWLLiteral(true));
		Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>();
		annos.add(ann);
		
		for (int i = 10; i >= 1;i--){
			System.out.println("Percentage Defeasibility: " + (i*10) + "%");
			System.out.println();
			
			FileWriter avgEntailmentChecksW = new FileWriter("test/"+(i*10)+"/Avg_entailment_checks_LC.txt");
			PrintWriter avgEntailmentChecksPW = new PrintWriter(avgEntailmentChecksW);
			
			FileWriter numberOfStrictAxiomsW = new FileWriter("test/"+(i*10)+"/Number_of_strict_axioms.txt");
			PrintWriter numberOfStrictAxiomsPW = new PrintWriter(numberOfStrictAxiomsW);
			
			FileWriter numberOfDefAxiomsW = new FileWriter("test/"+(i*10)+"/Number_of_defeasible_axioms.txt");
			PrintWriter numberOfDefAxiomsPW = new PrintWriter(numberOfDefAxiomsW);
			
			for (int j = 1; j <= 35;j++){
				
				/*************** Load the ranking ********************/
				System.out.print("loading the ranking for ontology " + j + "...");
				FileInputStream fileIn = new FileInputStream("test/"+(i*10)+"/Ontology"+j+"/ontranking.bin");
		        ObjectInputStream in = new ObjectInputStream(fileIn);
		        Ranking ranking = null;
		        try{
		        	ranking = (Ranking) in.readObject();
		        	System.out.println("done.");
		        }
		        catch (Exception e){
		        	System.out.println("failed!");
		        	e.printStackTrace();
		        }
		        in.close();
		        
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
		        
		        /****************************************************/	
				
		        /*************** Setup the defeasible reasoner ******/
		        ReasonerFactory reasonerFactory = new ReasonerFactory();
				DefeasibleInferenceComputer dic_pass = new DefeasibleInferenceComputer(reasonerFactory, ranking);
				/****************************************************/
				
				/*************** Load the queries ********************/
				Set<OWLAxiom> queries = new HashSet<OWLAxiom>();
				// Load ontology containing queries
				OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();	
				File queriesFile = new File("test/"+(i*10)+"/Ontology"+j+"/queries.owl");
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
				double entailmentChecks = 0.0;
				
				System.out.println("performing entailment checks for ontology " + j + "...");
				System.out.println();
			
				Iterator<OWLAxiom> iter = queries.iterator();
				
				while (iter.hasNext()){
					Query currentQuery = new Query(iter.next(), ReasoningType.LEXICOGRAPHIC, true);
					dic_pass.isEntailed(currentQuery);
					entailmentChecks += dic_pass.entailmentChecksLC;
				}
				
				avgEntailmentChecksPW.println(entailmentChecks/queries.size());
				
				System.out.println("...completed entailment checks for ontology " + j + ".");
				System.out.println();
				
			}
			
			avgEntailmentChecksPW.close();
			numberOfStrictAxiomsPW.close();
			numberOfDefAxiomsPW.close();
		}
		System.out.println("FINISHED!");
	}
}
