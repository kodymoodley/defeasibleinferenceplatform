package net.za.cair.dip.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
//import org.semanticweb.HermiT.Reasoner;
//import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import net.za.cair.dip.model.OntologyStructure;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.model.ReasoningType;
import net.za.cair.dip.util.ManchesterOWLSyntaxOWLObjectRendererImpl;

public class StoredRankIntegrityTester {
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

	public static void main(String [] args) throws OWLOntologyCreationException, IOException, ClassNotFoundException{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		//OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
		
		for (int j = 10;j <= 10;j++){
			FileWriter fw = new FileWriter("test/"+j*10+"/rankTimes.txt");
			PrintWriter pw = new PrintWriter(fw);
			long totalTime = 0;
			for (int i = 1;i <= 35;i++){
				//System.out.println("Ontology " + i);
				//System.out.println("------------------------------------------");
				//File file = new File("test/10/Ontology"+i+"/ranking.bin");
				FileInputStream fileIn = new FileInputStream("test/"+j*10+"/Ontology"+i+"/ranking.bin");
		        ObjectInputStream in = new ObjectInputStream(fileIn);
		        Ranking ranking = (Ranking) in.readObject();
		        
		        //Load ontology
		        //Get ratio of hidden strict info in DBox vs total Number of axioms.
		        File ontoFile = new File("test/"+j*10+"/Ontology"+i+"/ontology"+i+".owl");
		        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontoFile);		
		        int totalAxioms = ontology.getLogicalAxiomCount();
		        double t = totalAxioms * 1.0;
		        //Get hidden strict axioms in infinite rank
		        //First get all LHS of defeasible subsumptions
		        OntologyStructure os = new OntologyStructure(ontology);
		        Set<OWLAxiom> dbox = os.dBox.getAxioms();
		        Set<OWLClassExpression> lhs = new HashSet<OWLClassExpression>();
		        for (OWLAxiom a: dbox){
		        	OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
		        	lhs.add(sub.getSubClass());
		        }
		        //Now look at the LHS of the infinite rank axioms and see if they are in lhs
		        Set<OWLAxiom> hidden = new HashSet<OWLAxiom>();
		        for (OWLAxiom a: ranking.getInfiniteRank().getAxioms()){
		        	OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
		        	if (lhs.contains(sub.getSubClass())){
		        		hidden.add(a);
		        	}
		        }
		        int hiddenAxioms = hidden.size();
		        double h = hiddenAxioms * 1.0;
		        double ratio = h/t;
		        System.out.println(ratio);
		        /*double rankTime = Double.parseDouble(ranking.time);		        
		        pw.println(rankTime/60000);
		        
		        totalTime += rankTime;
		        
		        if (i == 35){
		        	pw.println(totalTime/35/60000);
		        }*/
		        
		        /*System.out.println("No of ranks:" + ranking.size());
		        for (Rank rank: ranking.getRanking())
		        	System.out.println("Size:" + rank.size());
		        System.out.println("Infinite rank size: " + ranking.getInfiniteRank().size());
		        
		        
		        System.out.println("Time to compute: " + ranking.time + "ms");
		        System.out.println("Entailment checks: " + ranking.entailmentChecks);
		        System.out.println("-------------------------------------------");*/
		        //System.out.println("Infinite rank size:" + ranking.getInfiniteRank().size());
		        
		        in.close();

			}
			pw.close();
			fw.close();
		}
	}
}
