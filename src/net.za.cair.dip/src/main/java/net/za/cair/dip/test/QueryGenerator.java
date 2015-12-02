package net.za.cair.dip.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class QueryGenerator {
	private static ThreadMXBean mx;
	private static OWLAnnotationProperty defeasibleAnnotationProperty;
	private static IRI defeasibleIRI = IRI.create("http://cair.meraka.org.za/defeasible");
	
	private static boolean clash(HashMap<OWLClassExpression, OWLClassExpression> map, OWLClassExpression lhs, OWLClassExpression rhs){
		
		for (Entry<OWLClassExpression, OWLClassExpression> entry: map.entrySet()){
			if (entry.getKey().equals(lhs) && entry.getValue().equals(rhs))
				return true;
		}
		
		return false;
	}
	
	private static OWLClassExpression fetchConceptName(Set<OWLClass> classes, int idx){
		int count = 0;
		for (OWLClass cls: classes){
			if (count == idx)
				return cls;
			count++;
		}
		return null;
	}
	
	private static OWLObjectProperty fetchRoleName(Set<OWLObjectProperty> roles, int idx){
		int count = 0;
		for (OWLObjectProperty role: roles){
			if (count == idx)
				return role;
			count++;
		}
		return null;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws OWLException 
	 */
	public static void main(String[] args) throws IOException, OWLException {		
		mx = ManagementFactory.getThreadMXBean();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataF = manager.getOWLDataFactory();
		defeasibleAnnotationProperty = dataF.getOWLAnnotationProperty(defeasibleIRI);
		
		PrintWriter out = null;
		for (int i = 1;i <= 50;i++){
			File file = new File("Experiments/Percentage/0/Ontology"+i+"/ontology"+i+".owl");	
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
			Set<OWLAxiom> totalQueries = new HashSet<OWLAxiom>();
			
			//Defeasible Queries
			System.out.println(ontology.getLogicalAxiomCount());
			int numDQueries = (int) (ontology.getLogicalAxiomCount()*0.1/4);
			System.out.println(numDQueries);

			Set<OWLAxiom> defeasibleQueries = new HashSet<OWLAxiom>();
			HashMap<OWLClassExpression, OWLClassExpression> exp = new HashMap<OWLClassExpression, OWLClassExpression>();
			for (int j = 0; j < numDQueries;j++){
				OWLClassExpression lhsExp = null;
				OWLClassExpression rhsExp = null;
				Set<OWLClass> classes = ontology.getClassesInSignature();
				Set<OWLObjectProperty> roles = ontology.getObjectPropertiesInSignature();
				Random rGen = new Random();
				lhsExp = fetchConceptName(classes, rGen.nextInt(classes.size()));
				rhsExp = fetchConceptName(classes, rGen.nextInt(classes.size()));
				boolean done = false;
				while (!done){
					//Should I use a complex lhs and rhs?
					int complexLhs = rGen.nextInt(2);
					int complexRhs = rGen.nextInt(2);
					if (complexLhs == 1){
						//Should I use roles, disjunction or conjunction to make it complex?
						int constructor = rGen.nextInt(3);
						if (constructor == 0){//role
							//pick a role
							OWLObjectProperty role = fetchRoleName(roles, rGen.nextInt(roles.size()));
							lhsExp = dataF.getOWLObjectSomeValuesFrom(role, lhsExp);
						}
						else if (constructor == 1){//disjunction
							//pick another concept
							OWLClassExpression class2 = fetchConceptName(classes, rGen.nextInt(classes.size()));
							lhsExp = dataF.getOWLObjectUnionOf(lhsExp, class2);
						}
						else if (constructor == 2){//conjunction
							//pick another concept
							OWLClassExpression class2 = fetchConceptName(classes, rGen.nextInt(classes.size()));
							lhsExp = dataF.getOWLObjectIntersectionOf(lhsExp, class2);
						}
					}
					if (complexRhs == 1){
						//Should I use roles, disjunction or conjunction to make it complex?
						int constructor = rGen.nextInt(3);
						if (constructor == 0){//role
							//pick a role
							OWLObjectProperty role = fetchRoleName(roles, rGen.nextInt(roles.size()));
							rhsExp = dataF.getOWLObjectSomeValuesFrom(role, rhsExp);
						}
						else if (constructor == 1){//disjunction
							//pick another concept
							OWLClassExpression class2 = fetchConceptName(classes, rGen.nextInt(classes.size()));
							rhsExp = dataF.getOWLObjectUnionOf(rhsExp, class2);
						}
						else if (constructor == 2){//conjunction
							//pick another concept
							OWLClassExpression class2 = fetchConceptName(classes, rGen.nextInt(classes.size()));
							rhsExp = dataF.getOWLObjectIntersectionOf(rhsExp, class2);
						}
					}
					//make sure lhs not equal to rhs AND lhs subClsOf rhs not in defeasible queries already
					if (!lhsExp.equals(rhsExp) && !clash(exp, lhsExp, rhsExp)){
						exp.put(lhsExp, rhsExp);
						done = true;
					}
				}
				OWLAxiom tmp = dataF.getOWLSubClassOfAxiom(lhsExp, rhsExp);
				//add defeasible annotation to tmp
				Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>();
				annos.add(dataF.getOWLAnnotation(defeasibleAnnotationProperty, dataF.getOWLLiteral(true)));				
				defeasibleQueries.add(tmp.getNNF().getAnnotatedAxiom(annos));
			}
			totalQueries.addAll(defeasibleQueries);
	
			//Strict Queries
			/*int numSQueries = (int) (ontology.getLogicalAxiomCount()*0.1/2);
			out.println("- Generating " + numSQueries + " strict queries...");
			//int queryComplexityFactor = 0;
			Set<OWLAxiom> strictQueries = new HashSet<OWLAxiom>();
			HashMap<OWLClassExpression, OWLClassExpression> exp2 = new HashMap<OWLClassExpression, OWLClassExpression>();
			for (int j = 0; j < numSQueries;j++){
				OWLClassExpression lhsExp = null;
				OWLClassExpression rhsExp = null;
				Set<OWLClass> classes = ontology.getClassesInSignature();
				Set<OWLObjectProperty> roles = ontology.getObjectPropertiesInSignature();
				Random rGen = new Random();
				lhsExp = fetchConceptName(classes, rGen.nextInt(classes.size()));
				rhsExp = fetchConceptName(classes, rGen.nextInt(classes.size()));
				boolean done = false;
				while (!done){
					//Should I use complex lhs and rhs?
					int complexLhs = rGen.nextInt(2);
					int complexRhs = rGen.nextInt(2);
					if (complexLhs == 1){
						//Should I use roles, disjunction or conjunction to make it complex?
						int constructor = rGen.nextInt(3);
						if (constructor == 0){//role
							//pick a role
							OWLObjectProperty role = fetchRoleName(roles, rGen.nextInt(roles.size()));
							lhsExp = dataF.getOWLObjectSomeValuesFrom(role, lhsExp);
						}
						else if (constructor == 1){//disjunction
							//pick another concept
							OWLClassExpression class2 = fetchConceptName(classes, rGen.nextInt(classes.size()));
							lhsExp = dataF.getOWLObjectUnionOf(lhsExp, class2);
						}
						else if (constructor == 2){//conjunction
							//pick another concept
							OWLClassExpression class2 = fetchConceptName(classes, rGen.nextInt(classes.size()));
							lhsExp = dataF.getOWLObjectIntersectionOf(lhsExp, class2);
						}
					}
					if (complexRhs == 1){
						//Should I use roles, disjunction or conjunction to make it complex?
						int constructor = rGen.nextInt(3);
						if (constructor == 0){//role
							//pick a role
							OWLObjectProperty role = fetchRoleName(roles, rGen.nextInt(roles.size()));
							rhsExp = dataF.getOWLObjectSomeValuesFrom(role, rhsExp);
						}
						else if (constructor == 1){//disjunction
							//pick another concept
							OWLClassExpression class2 = fetchConceptName(classes, rGen.nextInt(classes.size()));
							rhsExp = dataF.getOWLObjectUnionOf(rhsExp, class2);
						}
						else if (constructor == 2){//conjunction
							//pick another concept
							OWLClassExpression class2 = fetchConceptName(classes, rGen.nextInt(classes.size()));
							rhsExp = dataF.getOWLObjectIntersectionOf(rhsExp, class2);
						}
					}
					//make sure lhs not equal to rhs AND lhs subClsOf rhs not in defeasible queries already
					if (!lhsExp.equals(rhsExp) && !clash(exp2, lhsExp, rhsExp)){
						exp2.put(lhsExp, rhsExp);
						done = true;
					}
				}		
				strictQueries.add(dataF.getOWLSubClassOfAxiom(lhsExp, rhsExp));
			}
			totalQueries.addAll(strictQueries);
			//Class Instance Queries
			/*int numAQueries = (int) (ontology.getLogicalAxiomCount()*0.1/3);
			out.println("- Generating " + numAQueries + " class instance queries...");
			Set<OWLAxiom> instanceQueries = new HashSet<OWLAxiom>();
			Set<OWLClassExpression> classExp = new HashSet<OWLClassExpression>();
			for (int j = 0; j < numAQueries;j++){
				//No instances in Bonattis/Luigis Data
			}*/
			
			//Set<OWLAxiom> totalQueries = new HashSet<OWLAxiom>();
			//out.println("- Query generation complete.");
			//out.println("- Saving queries as ontology...");
			
			IRI ontologyIRI = IRI.create("http://cair.meraka.org.za/ontologies/defOnto"+i+"_queries");
			OWLOntology currentOntology = manager.createOntology(ontologyIRI);
			manager.addAxioms(currentOntology, totalQueries);			
			File queriesFile = new File("Experiments/Percentage/0/Ontology"+i+"/TBoxQueries/onto"+i+"_queries.owl");
			OWLXMLOntologyFormat owlxmlFormat = new OWLXMLOntologyFormat();
			manager.saveOntology(currentOntology, owlxmlFormat, IRI.create(queriesFile.toURI()));	
			System.out.println("Ontology " + i + " processed.");
		}			
		System.out.println("Finished!");
	}
}
