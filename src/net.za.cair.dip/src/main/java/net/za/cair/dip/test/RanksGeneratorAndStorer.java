package net.za.cair.dip.test;

import java.io.File;
import java.util.Collections;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
//import org.semanticweb.HermiT.Reasoner;
//import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import net.za.cair.dip.DefeasibleInferenceComputer;
import net.za.cair.dip.model.ReasoningType;
import net.za.cair.dip.util.ManchesterOWLSyntaxOWLObjectRendererImpl;

public class RanksGeneratorAndStorer {
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
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		//OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
		
		/*for (int j = 10;j <= 10;j++){
			for (int i = 1;i <= 50;i++){	
				System.out.println(j*10 + "% - Ontology " + i);
				File file = new File("test/"+j*10+"/Ontology"+i+"/ontology"+i+".owl");					
				OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
				DefeasibleInferenceComputer dic = new DefeasibleInferenceComputer(reasonerFactory, ontology);
				dic.generateRanking();				
				try{
					FileOutputStream fout = new FileOutputStream("test/"+j*10+"/Ontology"+i+"/ranking.bin");
					ObjectOutputStream oos = new ObjectOutputStream(fout);   
					oos.writeObject(dic.ranking);
					oos.close();
					System.out.println("Done");
			    }catch(Exception ex){ex.printStackTrace();}			
			}
		}*/
		
		
		for (int j = 10;j <= 10;j++){
			for (int i = 1;i <= 1;i++){	
				System.out.println(j*10 + "% - Ontology " + i);
				File file = new File("test/"+j*10+"/Ontology"+i+"/ontology"+i+".owl");					
				OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
				DefeasibleInferenceComputer dic = null;//new DefeasibleInferenceComputer(reasonerFactory, ontology);
				//dic.generateRanking();				
				/*try{
					FileOutputStream fout = new FileOutputStream("test/"+j*10+"/Ontology"+i+"/ranking.bin");
					ObjectOutputStream oos = new ObjectOutputStream(fout);   
					oos.writeObject(dic.ranking);
					oos.close();
					System.out.println("Done");
			    }catch(Exception ex){ex.printStackTrace();}	*/		
			}
		}
		
		/*for (int j = 8;j <= 8;j++){
			for (int i = 1;i <= 35;i++){	
				System.out.println(j*10 + "% - Ontology " + i);
				File file = new File("test/"+j*10+"/Ontology"+i+"/ontology"+i+".owl");					
				OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
				DefeasibleInferenceComputer dic = new DefeasibleInferenceComputer(reasonerFactory, ontology);
				dic.generateRanking();				
				try{
					FileOutputStream fout = new FileOutputStream("test/"+j*10+"/Ontology"+i+"/ranking.bin");
					ObjectOutputStream oos = new ObjectOutputStream(fout);   
					oos.writeObject(dic.ranking);
					oos.close();
					System.out.println("Done");
			    }catch(Exception ex){ex.printStackTrace();}			
			}
		}*/
		
		/*zfor (int j = 7;j <= 7;j++){
			for (int i = 45;i <= 50;i++){	
				System.out.println(j*10 + "% - Ontology " + i);
				File file = new File("test/"+j*10+"/Ontology"+i+"/ontology"+i+".owl");					
				OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
				DefeasibleInferenceComputer dic = new DefeasibleInferenceComputer(reasonerFactory, ontology);
				dic.generateRanking();				
				try{
					FileOutputStream fout = new FileOutputStream("test/"+j*10+"/Ontology"+i+"/ranking.bin");
					ObjectOutputStream oos = new ObjectOutputStream(fout);   
					oos.writeObject(dic.ranking);
					oos.close();
					System.out.println("Done");
			    }catch(Exception ex){ex.printStackTrace();}			
			}
		}*/
	}
}
