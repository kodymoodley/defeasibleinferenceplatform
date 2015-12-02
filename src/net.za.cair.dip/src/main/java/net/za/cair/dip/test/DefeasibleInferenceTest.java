package net.za.cair.dip.test;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
//import org.semanticweb.HermiT.Reasoner;
//import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import net.za.cair.dip.model.OntologyStructure;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.model.ReasoningType;
import net.za.cair.dip.transform.RationalRankingAlgorithm;
import net.za.cair.dip.util.ManchesterOWLSyntaxOWLObjectRendererImpl;

public class DefeasibleInferenceTest {
	private static OWLOntology ontology;
	private static OWLAxiom axiom;
	private static ReasoningType algorithm;
	private static boolean defeasible;
	
	private static OWLAxiom parseToAxiom(String axiomStr, OWLOntologyManager ontologyManager){
		//System.out.println(axiomStr);
		//String parseableString = axiomStr.substring(0, axiomStr.length());
		ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(ontologyManager.getOWLDataFactory(), axiomStr);
		parser.setOWLEntityChecker(new ShortFormEntityChecker(new BidirectionalShortFormProviderAdapter(ontologyManager, Collections.singleton(ontology), new SimpleShortFormProvider())));
		
		try {
			return parser.parseAxiom();//.parseAxiom();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			System.out.println("Error parsing axiom (arg 2).");
			e.printStackTrace();
			return null;
		}
	}
	
	private static boolean inputIntegrityCheckValid(String [] args){		
		if (args.length != 4){			
			System.out.println("4 arguments required.");
			return false;
		}
		else{
			/*********************** Create Ontology Manager **************************/
			OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();			
			/************************ Accept ontology path ****************************/
			File ontologyFile = new File(args[0]);
			/*********************** Load Ontology (arg[0]) ************************************/
			ontology = null;
			try {ontology = ontologyManager.loadOntologyFromOntologyDocument(ontologyFile);
			ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();}
			//for (OWLAxiom axiom: ontology.getAxioms())
				//System.out.println(man.render(axiom));
			//}
			catch (OWLOntologyCreationException e) {
				System.out.println("Error loading ontology (arg 1).");
				return false;
			}		
			/************** Parse string axiom to OWLAxiom (arg[1]) ****************************/
			axiom = parseToAxiom(args[1], ontology.getOWLOntologyManager());
			if (axiom == null)
				return false;						
			/************** Parse reasoning algorithm (arg[2]) ****************************/
			if (args[2].equals("r") || args[2].equals("R")){
				algorithm = ReasoningType.RATIONAL;
			}
			else if (args[2].equals("l") || args[2].equals("L")){
				algorithm = ReasoningType.LEXICOGRAPHIC;
			}
			else{
				System.out.println("Invalid algorithm specified (arg 3).");
				return false;
			}
			/************** Parse boolean defeasible flag (arg[3]) ****************************/
			if (args[3].equals("0")){
				defeasible = false;				
			}
			else if (args[3].equals("1")){
				defeasible = true;				
			}
			else{
				System.out.println("Invalid boolean defeasible flag (arg 4).");
				return false;
			}
			return true;	
		}
			 
	}

	public static void main(String [] args) throws OWLOntologyCreationException{
		/*if (!inputIntegrityCheckValid(args)){
			System.exit(-1);
		}*/
		/*********************** Create Ontology Manager **************************/
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();	
		File ontologyFile = new File("TestCases/validityexample.owl");
		/*********************** Load Ontology (arg[0]) ************************************/
		ontology = null;
		try {ontology = ontologyManager.loadOntologyFromOntologyDocument(ontologyFile);}
		catch (OWLOntologyCreationException e) {
			System.out.println("Error loading ontology (arg 1).");
		}		
					
		/*********************** Initialise DIC ***********************************/
		/********** (Computing ontology structure and ranking) ********************/
		ReasonerFactory reasonerFactory = new ReasonerFactory();
	
		Set<OWLClassExpression> unsats = new HashSet<OWLClassExpression>();
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
