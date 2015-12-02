package net.za.cair.dip.experimentbench;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owl.explanation.api.ExplanationException;
//import org.semanticweb.HermiT.Reasoner;
//import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import net.za.cair.dip.DefeasibleInferenceComputer;
import net.za.cair.dip.model.Query;
import net.za.cair.dip.model.Rank;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.model.ReasoningType;
import net.za.cair.dip.transform.RationalRankingAlgorithm;
import net.za.cair.dip.util.ManchesterOWLSyntaxOWLObjectRendererImpl;

public class EntailmentTester {
	private static OWLOntology ontology;
	//private static OWLAxiom axiom;
	
	private static OWLAxiom parseToAxiom(String axiomStr, OWLOntologyManager ontologyManager){
		ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(ontologyManager.getOWLDataFactory(), axiomStr);
		parser.setOWLEntityChecker(new ShortFormEntityChecker(new BidirectionalShortFormProviderAdapter(ontologyManager, Collections.singleton(ontology), new SimpleShortFormProvider())));
		
		try {
			return parser.parseAxiom();
		} catch (ParserException e) {
			System.out.println("Error parsing axiom (arg 2).");
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String [] args) throws OWLOntologyCreationException, InterruptedException, ExecutionException, ExplanationException, TimeoutException{
		/*System.out.println();
		System.out.println("STANDARD RANKING TEST");
		System.out.println();*/
		
		ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	    
		/*********************** Create Ontology Manager **************************/
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();	
		File ontologyFile = new File("TestCases/moduleexample.owl");
		/*********************** Load Ontology (arg[0]) ************************************/
		ontology = null;
		try {ontology = ontologyManager.loadOntologyFromOntologyDocument(ontologyFile);}
		catch (OWLOntologyCreationException e) {
			System.out.println("Error loading ontology (arg 1).");
		}		
		
		ReasonerFactory reasonerFactory = new ReasonerFactory();
		RationalRankingAlgorithm rankingalg = new RationalRankingAlgorithm(reasonerFactory, ontology);
		Ranking r = null;
		try {
			r = rankingalg.computeRanking();
			System.out.println(r);
			
			for (int i = 0; i < r.getRanking().size();i++){
				System.out.println(r.getRanking().get(i));
			}
		} catch (OWLException e) {
			e.printStackTrace();
			System.out.println("error computing ranking.");
		}
		
		DefeasibleInferenceComputer dic = new DefeasibleInferenceComputer(reasonerFactory, r);

		//System.out.println();
		/*System.out.println("STANDARD ENTAILMENT TEST");
		System.out.println();
		
		for (OWLAxiom a: ontology.getLogicalAxioms()){
			if (!r.getInfiniteRank().getAxiomsAsSet().contains(a)){
				Query q = new Query(a, ReasoningType.RATIONAL, true);
				System.out.println(man.render(a)+ " is rationally entailed: " + dic.isEntailed(q));
			}
		}*/
		
		System.out.println();
		System.out.println("NON-STANDARD ENTAILMENT TEST");
		System.out.println();
		
		/*String test1 = "AvianRedBloodCell SubClassOf: hasPart some CellMembrane";
		String test2 = "MammalianRedBloodCell SubClassOf: hasPart some CellMembrane";
		String test3 = "AvianRedBloodCell SubClassOf: hasPart some CellMembrane";
		String test4 = "MammalianRedBloodCell SubClassOf: hasPart some CellMembrane";
		String test5 = "MammalianRedBloodCell SubClassOf: hasPart some Nucleus";*/
		String test6 = "MammalianSickleCell SubClassOf: not (hasPart some Nucleus)";
		
		/*OWLAxiom one = parseToAxiom(test1, ontologyManager);
		OWLAxiom two = parseToAxiom(test2, ontologyManager);
		OWLAxiom three = parseToAxiom(test3, ontologyManager);
		OWLAxiom four = parseToAxiom(test4, ontologyManager);
		OWLAxiom five = parseToAxiom(test5, ontologyManager);*/
		OWLAxiom six = parseToAxiom(test6, ontologyManager);
		
		/*Query q1 = new Query(one, ReasoningType.RATIONAL, true);
		Query q2 = new Query(two, ReasoningType.RATIONAL, true);
		Query q3 = new Query(three, ReasoningType.RELEVANT, true);
		Query q4 = new Query(four, ReasoningType.RELEVANT, true);
		Query q5 = new Query(five, ReasoningType.RELEVANT, true);*/
		Query q6 = new Query(six, ReasoningType.RELEVANT, true);
		Query q7 = new Query(six, ReasoningType.MIN_RELEVANT, true);
		Query q8 = new Query(six, ReasoningType.LEX_RELEVANT, true);
		Query q9 = new Query(six, ReasoningType.LEXICOGRAPHIC, true);
		
		/*System.out.println(man.render(one)+ " is rationally entailed: " + dic.isEntailed(q1));
		System.out.println(man.render(two)+ " is rationally entailed: " + dic.isEntailed(q2));
		System.out.println(man.render(three)+ " is relevantly entailed: " + dic.isEntailed(q3));
		System.out.println(man.render(four)+ " is relevantly entailed: " + dic.isEntailed(q4));
		System.out.println(man.render(five)+ " is relevantly entailed: " + dic.isEntailed(q5));*/
		System.out.println(man.render(six)+ " is relevantly entailed: " + dic.isEntailed(q6));
		//System.out.println(dic.ranking);
		System.out.println(man.render(six)+ " is minimal-relevantly entailed: " + dic.isEntailed(q7));
		System.out.println(man.render(six)+ " is lexical relevantly entailed: " + dic.isEntailed(q8));
		//System.out.println(dic.ranking);
		System.out.println(man.render(six)+ " is lexicographically entailed: " + dic.isEntailed(q9));
		
		
		
		//		//dic.generateRanking();
		//System.out.println(axiom);
		//System.out.println(algorithm);
		//System.out.println(defeasible);
		/*Query query = new Query(axiom, algorithm, defeasible);
		
		
		if (dic.isEntailed(query))
			System.out.println("true.");
		else
			System.out.println("false.");*/

	
		/*Set<OWLClassExpression> unsats = new HashSet<OWLClassExpression>();
		for (OWLAxiom a: ontology.getLogicalAxioms()){
			if (a.isOfType(AxiomType.SUBCLASS_OF)){
				OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
				unsats.add(sub.getSubClass());
			}
		}
		
		OntologyStructure o = new OntologyStructure(ontology);
		RationalRankingAlgorithm rankingalg = new RationalRankingAlgorithm(reasonerFactory, o, unsats);
		Ranking r = null;
		try {
			r = rankingalg.computeRanking();
			System.out.println(r);
		} catch (OWLException e) {
			e.printStackTrace();
			System.out.println("error computing ranking.");
		}
	
		//DefeasibleInferenceComputer dic = null;//new DefeasibleInferenceComputer(reasonerFactory, ontology);
		//dic.generateRanking();
		//System.out.println(axiom);
		//System.out.println(algorithm);
		//System.out.println(defeasible);
		/*Query query = new Query(axiom, algorithm, defeasible);
		
		
		if (dic.isEntailed(query))
			System.out.println("true.");
		else
			System.out.println("false.");*/
	}
}
