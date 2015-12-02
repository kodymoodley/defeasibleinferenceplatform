package net.za.cair.dip.test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.za.cair.dip.DefeasibleInferenceComputer;
import net.za.cair.dip.model.OntologyStructure;
import net.za.cair.dip.model.ReasoningType;
import net.za.cair.dip.util.AxiomManipulator;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.*;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

public class ClosureCheckerTest {
	//private static ThreadMXBean mx;
	private static final String Defeasible_IRI = "http://www.cair.za.net/ontologyAnnotationProperties/defeasible";
	private static final String Ontology_IRI = "http://www.cair.za.net/ontology";
	
	public static void main(String[] args) throws IOException, OWLException, ClassNotFoundException, ParserException {	
		ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		
		IRI ont_iri = IRI.create(Ontology_IRI);
		IRI def_iri = IRI.create(Defeasible_IRI);
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataF = manager.getOWLDataFactory();
		
		Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>();
		OWLAnnotationProperty annoProp = dataF.getOWLAnnotationProperty(def_iri);
		OWLAnnotationValue val = dataF.getOWLLiteral(true);
		OWLAnnotation def_anno = dataF.getOWLAnnotation(annoProp, val);
		annos.add(def_anno);
		
		OWLOntology ontology = manager.createOntology(ont_iri);
		
		IRI class1_iri = IRI.create(Ontology_IRI+ "#B");
		IRI class2_iri = IRI.create(Ontology_IRI+ "#F");
		IRI class3_iri = IRI.create(Ontology_IRI+ "#W");
		IRI class4_iri = IRI.create(Ontology_IRI+ "#P");
		IRI class5_iri = IRI.create(Ontology_IRI+ "#JP");
		
		OWLClass class1 = dataF.getOWLClass(class1_iri);
		OWLClass class2 = dataF.getOWLClass(class2_iri);
		OWLClass class3 = dataF.getOWLClass(class3_iri);
		OWLClass class4 = dataF.getOWLClass(class4_iri);
		OWLClass class5 = dataF.getOWLClass(class5_iri);
		
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		OWLSubClassOfAxiom sub1 = dataF.getOWLSubClassOfAxiom(class1, class2, annos);//dataF.getOWLObjectIntersectionOf(class1,dataF.getOWLObjectComplementOf(class4)), class2, annos);
		axioms.add(sub1);
		OWLSubClassOfAxiom sub2 = dataF.getOWLSubClassOfAxiom(class1, class3, annos);
		axioms.add(sub2);
		OWLSubClassOfAxiom sub3 = dataF.getOWLSubClassOfAxiom(class4, dataF.getOWLObjectComplementOf(class2), annos);
		axioms.add(sub3);
		OWLSubClassOfAxiom sub4 = dataF.getOWLSubClassOfAxiom(class4, class1, annos);
		axioms.add(sub4);
		OWLSubClassOfAxiom sub5 = dataF.getOWLSubClassOfAxiom(class5, class4, annos);
		axioms.add(sub5);
		OWLSubClassOfAxiom sub6 = dataF.getOWLSubClassOfAxiom(class5, class2, annos);
		axioms.add(sub6);
		
		Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
		for (OWLAxiom axiom: axioms){
			OWLClassExpression current = AxiomManipulator.getMaterialization(axiom);
			if (current != null)
				result.add(current);
		}
		
		manager.addAxioms(ontology, axioms);
		
		OWLAxiom query = sub6;//dataF.getOWLSubClassOfAxiom(class1, dataF.getOWLObjectComplementOf(class2));
		//OWLSubClassOfAxiom subClassAxiomQuery = (OWLSubClassOfAxiom)query;
		//OWLClassExpression exceptionalityConcept = dataF.getOWLObjectComplementOf(subClassAxiomQuery.getSubClass());
		//OWLSubClassOfAxiom querymodified = dataF.getOWLSubClassOfAxiom(dataF.getOWLThing(), exceptionalityConcept);
		
		System.out.println();
		System.out.println("Entailment: " + man.render(query));
		System.out.println();
		
		System.out.println("Axioms:");
		System.out.println("-------");
		System.out.println();
		System.out.println(man.render(sub1));
		System.out.println(man.render(sub2));
		System.out.println(man.render(sub3));
		System.out.println(man.render(sub4));
		System.out.println(man.render(sub5));
		System.out.println(man.render(sub6));
		System.out.println();
		
		/** JUSTIFICATION TESTING **/
		/*ReasonerFactory reasonerFactory = new ReasonerFactory();
		ExplanationGeneratorFactory<OWLAxiom> regFac = ExplanationManager.createExplanationGeneratorFactory(reasonerFactory);

		ExplanationGenerator<OWLAxiom> justGen = regFac.createExplanationGenerator(ontology);
		Set<Explanation<OWLAxiom>> justifications = justGen.getExplanations(querymodified);
		
		System.out.println("No. of justifications: " + justifications.size());
		System.out.println();
		
		int justCount = 1;
		for (Explanation<OWLAxiom> e: justifications){
			
			System.out.println("Justification:" + justCount + ":");
			System.out.println();
			
			for (OWLAxiom a: e.getAxioms()){
				System.out.println(man.render(a));
			}
			
			justCount++;
			System.out.println();
		}
		
		System.out.println("Finished.");*/
		
		/** CLOSURE TESTER **/
		//ReasonerFactory hermitF = new ReasonerFactory();
		OntologyStructure structure = new OntologyStructure(ontology);
		ReasoningType algorithm = ReasoningType.NAME_TYPE_MAP.get("Relevant Closure");
		//RankingConstruction rankingConstruction = null;//new RankingConstruction(query, hermitF, structure, algorithm);
		//Query fullQuery = new Query(query, algorithm, rankingConstruction, true);
		DefeasibleInferenceComputer dic = null;//new DefeasibleInferenceComputer(hermitF, structure);
		
		System.out.println("Original Ranking:\n");
		
		//System.out.println(rankingConstruction.getRanking());
		
		System.out.println("Materialization Ranking:");

		//System.out.println(rankingConstruction.getMaterialRanking());
		
		System.out.println();
		
		//if (dic.isEntailed(fullQuery))
			System.out.println("TRUE");
		//else
			System.out.println("FALSE");
		
		System.out.println();
	}
}
