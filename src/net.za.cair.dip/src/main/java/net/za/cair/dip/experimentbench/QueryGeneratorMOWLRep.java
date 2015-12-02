package net.za.cair.dip.experimentbench;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.za.cair.dip.DefeasibleInferenceComputer;
import net.za.cair.dip.model.Rank;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.transform.RationalRankingAlgorithm;
import net.za.cair.dip.util.ManchesterOWLSyntaxOWLObjectRendererImpl;
import net.za.cair.dip.util.Utility;

import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class QueryGeneratorMOWLRep {
	
	private static OWLClassExpression randomlySelectConcept(Set<OWLClassExpression> set){
		Object [] arr = set.toArray();
		int len = arr.length;
		Random randomGenerator = new Random();
		
		int index = randomGenerator.nextInt(len);
		OWLClassExpression result = (OWLClassExpression)arr[index];
		return result;
	}
	
	public static void main(String[] args) throws IOException, OWLException {		
		ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataF = manager.getOWLDataFactory();
		
		//PrintWriter out = null;
		for (int i = 124;i <= 134;i++){
			/*************** Load the ranking *******************/
			System.out.print("loading the ranking for ontology " + i + "...");
			FileInputStream fileIn = new FileInputStream("MOWLRep/"+i+"/ontranking.bin");
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
	        System.out.println("asdas");
	        /****************************************************/	
			
	        if (failedLoading){
		    	// Compute the ranking
		    	// 1. First load the ontology
		    	/*********************** Create Ontology Manager **************************/
				OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();	
				File ontologyFile = new File("MOWLRep/" + i + "/processed.owl");
				/*********************** Load Ontology (arg[0]) ************************************/
				OWLOntology ontology = null;
				try {
					System.out.print("loading ontology " + i + "...");
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
				} catch (OWLException e) {
					System.out.println("Error computing ranking!");
					e.printStackTrace();
					System.exit(-1);
				}
		    }
	        
			Set<OWLAxiom> totalQueries = new HashSet<OWLAxiom>();
			// Get number of axioms, signature of the axioms, and LHS concepts of axioms in ranking
			Set<OWLClass> classes = new HashSet<OWLClass>();
			Set<OWLObjectProperty> roles = new HashSet<OWLObjectProperty>();
			
			Set<OWLClassExpression> lhs = new HashSet<OWLClassExpression>();
			int numOfAxioms = 0;
			
			for (int k = 0; k < ranking.getRanking().size();k++){
				numOfAxioms += ranking.getRanking().get(k).getAxioms().size();
				if (ranking.getRanking().size() == 1 || k != ranking.getRanking().size()-1){
					for (OWLAxiom a: ranking.getRanking().get(k).getAxioms()){
						OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
						lhs.add(sub.getSubClass());
						classes.addAll(a.getClassesInSignature());
						roles.addAll(a.getObjectPropertiesInSignature());
					}
				}
			}
			
			
			numOfAxioms += ranking.getInfiniteRank().getAxioms().size();
			for (OWLAxiom a: ranking.getInfiniteRank().getAxioms()){
				if (ranking.size() == 0 && a.isOfType(AxiomType.SUBCLASS_OF)){
					OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
					lhs.add(sub.getSubClass());
				}
				classes.addAll(a.getClassesInSignature());
				roles.addAll(a.getObjectPropertiesInSignature());
			}
			
			
			Set<OWLClassExpression> rhs = DOntologyGenerator.generateComplexConcepts(dataF, classes, roles);
			System.out.println("number of axioms: " + numOfAxioms);
			// 5% of number of axioms
			double numOfQueries = (numOfAxioms*5)/100;
			System.out.println("number of queries: " + numOfQueries);
			System.out.println("Number of LHSs: " + lhs.size());
			//for (OWLClassExpression clsL : lhs){
			//	System.out.println(man.render(clsL));
			//}
			//System.exit(-1);
			for (int k = 1; k <= numOfQueries;k++){
				OWLClassExpression l = dataF.getOWLThing();
				while (l.isOWLThing() || l.isOWLNothing()){
				//while (l.isOWLThing()){
					l = randomlySelectConcept(lhs);
				}
				System.out.println(man.render(l));
				OWLClassExpression r = dataF.getOWLThing();
				while (r.isOWLThing() || r.isOWLNothing()){
					r = randomlySelectConcept(rhs);
				}
				
				OWLSubClassOfAxiom axiom = dataF.getOWLSubClassOfAxiom(l, r);
				totalQueries.add(axiom);
			}
			
			IRI ontologyIRI = IRI.create("http://cair.za.net/ontologies/queries_"+ i);
			OWLOntology currentOntology = manager.createOntology(ontologyIRI);
			manager.addAxioms(currentOntology, totalQueries);			
			File queriesFile = new File("MOWLRep/"+i+"/queries.owl");
			OWLXMLOntologyFormat owlxmlFormat = new OWLXMLOntologyFormat();
			manager.saveOntology(currentOntology, owlxmlFormat, IRI.create(queriesFile.toURI()));	
			System.out.println("Ontology " + i + " processed.");
		}
		
		System.out.println("Finished!");
	}
}
