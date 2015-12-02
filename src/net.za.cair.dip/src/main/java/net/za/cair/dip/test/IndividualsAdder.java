package net.za.cair.dip.test;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Random;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

public class IndividualsAdder{
	private static ThreadMXBean mx;
	private static IRI defeasibleIRI = IRI.create("http://cair.meraka.org.za/defeasible");
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws OWLException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, OWLException, ClassNotFoundException {	
		//ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		mx = ManagementFactory.getThreadMXBean();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataF = manager.getOWLDataFactory();
		dataF.getOWLAnnotationProperty(defeasibleIRI);
		StructuralReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		
		for (int j = 10; j >= 5;j--){
			for (int i = 1;i <= 50;i++){	
				File tBoxFile = new File("Experiments/"+(j*10)+"/Ontology"+i+"/ontology"+i+".owl");
				OWLOntology tBoxOntology = manager.loadOntologyFromOntologyDocument(tBoxFile);
				//Randomly select a class expression - introduce an individual assertion for this concept - do this 5 times
				for (int k = 0; k < 5;k++){
					Random randomGenerator = new Random();
					//tBoxOntology.getC
				}
				
				
				
				manager.saveOntology(tBoxOntology);
				OWLReasoner reasoner = reasonerFactory.createReasoner(tBoxOntology);
				System.out.println(reasoner.isConsistent());
			}
		}
		System.out.println("Finished.");
	}
}
