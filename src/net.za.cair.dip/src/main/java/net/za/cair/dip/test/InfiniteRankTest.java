package net.za.cair.dip.test;

import java.io.File;
import java.io.IOException;
import net.za.cair.dip.util.Utility;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
//import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

public class InfiniteRankTest{
	//private static ThreadMXBean mx;
	private static IRI defeasibleIRI = IRI.create("http://cair.meraka.org.za/defeasible");
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws OWLException 
	 */
	public static void main(String[] args) throws IOException, OWLException {	
		//new ManchesterOWLSyntaxOWLObjectRendererImpl();
		//mx = ManagementFactory.getThreadMXBean();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataF = manager.getOWLDataFactory();
		dataF.getOWLAnnotationProperty(defeasibleIRI);
		File ontologyFile = null;
		for (int per = 9;per <= 9; per++){
			System.out.println("per:" + per);
			for (int i = 1; i <= 50; i++){
				ontologyFile = new File("Experiments/Percentage/"+(per*10)+"/Ontology"+i+"/ontology"+i+".owl");
				OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
				int def = 0;
				int str = 0;
				for (OWLAxiom axiom: ontology.getAxioms()){
					Utility u = new Utility();
					if (u.isDefeasible(axiom)){
						def++;
					}
					else{
						str++;
					}
				}
				int total = def+str;
				double den = (double)total;
				double num = (double)def;
				//double den = (double)str;
				System.out.print(def + " - "); System.out.println(str);
				//System.out.println(num/den);// System.out.print(def);
			}
		}
		/*for (int i = 1;i <= 90;i++){		
			//File ontologyFile = new File("Experiments/OntGen/DefOntologies/Ontology"+i+"/ontology"+i+".owl");
			//OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
			//System.out.println(ontology.getAxiomCount());
			Ranking ranking = new Ranking();
			boolean done = false;
			int ontIndex = 1;
			int count = 0;
			int sizeOfInfRank = 0;
			while (!done){
				File ontologyFile = new File("Experiments/OntGen/DefOntologies/Ontology"+i+"/Ranks/onto"+i+"_rank"+ontIndex+".owl");
				if (ontologyFile.exists()){
					count++;
					/*OWLOntology tmp = manager.loadOntologyFromOntologyDocument(ontologyFile);
					ArrayList<OWLAxiom> arr = new ArrayList<OWLAxiom>();
					arr.addAll(tmp.getAxioms());
					Rank rank = new Rank(arr);
					int c = 0;
					for (OWLAxiom a: rank.getAxioms()){
						if (Utility.getInstance().isDefeasible(a))
							c++;
					}
					sizeOfInfRank = c;
					ranking.add(rank);*/
					//ontIndex++;
				/*}
				else{
					done = true;
				}
			}
			
			System.out.println(count);
			//System.out.println(sizeOfInfRank);
			
		}			
		System.out.println("Finished!");*/
	}
}
