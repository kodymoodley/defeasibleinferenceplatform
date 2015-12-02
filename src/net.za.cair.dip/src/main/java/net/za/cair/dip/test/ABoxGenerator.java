package net.za.cair.dip.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.za.cair.dip.DefeasibleInferenceComputer;
import net.za.cair.dip.model.Query;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.model.ReasoningType;

//import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

public class ABoxGenerator {
	private static ThreadMXBean mx;
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws OWLException 
	 * @throws ClassNotFoundException 
	 */
	
	private static int randInt(Random r, int min, int max){
		int randomNum = r.nextInt((max-min)+1) + min;
		return randomNum;
	}
	
	private static OWLObjectProperty getRandomRole(Random r, OWLDataFactory dataF, OWLOntology ontology){
		Set<OWLObjectProperty> ops = ontology.getObjectPropertiesInSignature();
		int randomNumber = randInt(r, 1, ops.size());
		int index = 1;
		for (OWLObjectProperty op: ops){
			if (index == randomNumber)
				return op;
			index++;
		}
		return null;
	}
	
	private static OWLNamedIndividual getRandomInd(Random r, OWLDataFactory dataF, OWLOntology ontology){
		Set<OWLNamedIndividual> inds = ontology.getIndividualsInSignature();//.getObjectPropertiesInSignature();
		int randomNumber = randInt(r, 1, inds.size());
		int index = 1;
		for (OWLNamedIndividual ind: inds){
			if (index == randomNumber)
				return ind;
			index++;
		}
		return null;
	}
	
	public static void main(String[] args) throws IOException, OWLException, ClassNotFoundException {	
		Random r = new Random();
		ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		mx = ManagementFactory.getThreadMXBean();
		
		for (int j = 1; j <= 10;j++){
			System.out.println();
			System.out.println((j*10) + " percent");
			System.out.println("------------------");
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLDataFactory dataF = manager.getOWLDataFactory();
			for (int i = 1;i <= 35;i++){				
				System.out.print("Ontology " + i + ": ");			
				//Decide on a size of the final generated ABox
				File tBoxFile = new File("test/"+(j*10)+"/Ontology"+i+"/ontology"+i+".owl");							
				OWLOntology tBox = manager.loadOntologyFromOntologyDocument(tBoxFile);			
				//manager.removeAxioms(tBox, tBox.getABoxAxioms(true));
				//manager.saveOntology(tBox);
				Set<OWLClassAssertionAxiom> ca = new HashSet<OWLClassAssertionAxiom>();
				int indivCount = 0;
				for (OWLAxiom a: tBox.getAxioms()){
					OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
					OWLNamedIndividual ind = dataF.getOWLNamedIndividual(IRI.create(tBox.getOntologyID().getOntologyIRI() + "#i"+indivCount));
					OWLClassAssertionAxiom clsAssAx = dataF.getOWLClassAssertionAxiom(sub.getSubClass(),ind);
					ca.add(clsAssAx);
					indivCount++;
				}
				//Add to ontology
				manager.addAxioms(tBox, ca);
				manager.saveOntology(tBox);
				
				int noRoleAssertions = tBox.getAxiomCount()/4;
				System.out.println("noRoleAssertions: " + noRoleAssertions);
				
				Set<OWLObjectPropertyAssertionAxiom> ra = new HashSet<OWLObjectPropertyAssertionAxiom>();

				boolean done = false;
				while (!done){
					//choose a role at random
					OWLObjectProperty role = getRandomRole(r,dataF,tBox);
					//choose two individuals at random
					OWLNamedIndividual ind1 = getRandomInd(r,dataF,tBox);
					OWLNamedIndividual ind2 = getRandomInd(r,dataF,tBox);
					//create a role assertion axiom
					OWLObjectPropertyAssertionAxiom roleAssertion = dataF.getOWLObjectPropertyAssertionAxiom(role, ind1, ind2);
					ra.add(roleAssertion);
					if (ra.size() == noRoleAssertions){
						done = true;
					}
				}
				//add it to the ontology
				manager.addAxioms(tBox, ra);
				manager.saveOntology(tBox);
			}
			System.out.println("Finished.");
		}
	}
	
	
}
	