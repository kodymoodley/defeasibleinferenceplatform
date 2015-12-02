package net.za.cair.dip.test;

import java.io.File;
import java.io.IOException;
import java.lang.management.ThreadMXBean;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class FullStrictDefOntologyGenerator {
	private static ThreadMXBean mx;
	private static IRI defeasibleIRI = IRI.create("http://cair.meraka.org.za/defeasible");
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws OWLException 
	 */
	public static void main(String[] args) throws IOException, OWLException {				
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataF = manager.getOWLDataFactory();
		OWLAnnotationProperty defProp = dataF.getOWLAnnotationProperty(defeasibleIRI);
		Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>();
		annos.add(dataF.getOWLAnnotation(defProp, dataF.getOWLLiteral(true)));
				
		for (int i = 75;i <= 90;i++){				
			Set<OWLAxiom> defAxioms = new HashSet<OWLAxiom>();
			File ontoFile = new File("test/10/Ontology"+i+"/ontology"+i+".owl");
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontoFile);
			System.out.println(i);
			/*for (OWLAxiom axiom: ontology.getAxioms()){
				if (axiom.isOfType(AxiomType.SUBCLASS_OF)){
					if (Utility.getInstance().isDefeasible(axiom)){
						defAxioms.add(axiom);
					}
					else{
						OWLAxiom tmp = axiom.getAnnotatedAxiom(annos);
						defAxioms.add(tmp);
					}
				}							
			}
			
			//Defeasible ontology
			IRI ontologyIRI = IRI.create("http://cair.meraka.org.za/ontologies/defOntology"+i);
		    OWLOntology currentOntology = manager.createOntology(ontologyIRI);		    
		    manager.addAxioms(currentOntology, defAxioms);		    		   
		    File defOntFile = new File("Experiments/OntGen/DefOntologies/Ontology"+i+"/ontology"+i+".owl");
		   	OWLXMLOntologyFormat owlxmlFormat = new OWLXMLOntologyFormat();
		   	manager.saveOntology(currentOntology, owlxmlFormat, IRI.create(defOntFile.toURI()));	*/	   						  		   			   					
		}			
		System.out.println("Finished!");
	}
}
