package net.za.cair.dip.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.za.cair.dip.model.Ranking;
//import net.za.cair.dip.util.OntologyStructure;


//import org.semanticweb.HermiT.Reasoner;
//import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import net.za.cair.dip.model.*;

public class RankGenerator {
	private static ThreadMXBean mx;
	private static OWLOntologyManager annoManager = OWLManager.createOWLOntologyManager();
	private static IRI defeasibleIRI = IRI.create("http://cair.za.net/defeasible");
	public static OWLAnnotationProperty defeasibleAnnotationProperty = annoManager.getOWLDataFactory().getOWLAnnotationProperty(defeasibleIRI);
	
	public static Ranking computeRanking(OWLReasonerFactory reasonerFactory, OntologyStructure ontologyStructure) throws OWLException{
		//DefeasibleConsequenceChecker dcc = new PrototypicalChecker(reasonerFactory);
		//return dcc.computeRanking(ontologyStructure);
		return null;
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws OWLException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, OWLException, ClassNotFoundException {		
		mx = ManagementFactory.getThreadMXBean();
		
		PrintWriter out = null;
		String str = "";
	
		File dir = new File("gen/10");
		 if (dir.isDirectory()) {
			 for (int i = 1; i <= dir.listFiles().length;i++){
					File dir2 = new File("data/mowlcorp/files/incoherent/" + i);
					if (dir2.isDirectory()){				 // Make sure its the ontology folder
						
						//for (final File f : dir2.listFiles()) {
							File backgroundFile = new File("data/mowlcorp/files/incoherent/" + dir2.getName() + "/background.owl");
							File gciFile = new File("data/mowlcorp/files/incoherent/" + dir2.getName() + "/gci.owl");
							File unsatFile = new File("data/mowlcorp/files/incoherent/" + dir2.getName() + "/unsatLHS.owl");
							
							// 1. First determine by modules which axioms to be defeasible
							// 1a. Amalgamate gcis and background
							OWLOntologyManager manager1 = OWLManager.createOWLOntologyManager();
							OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
							OWLOntologyManager manager3 = OWLManager.createOWLOntologyManager();
							OWLOntology backgroundOnt = null;
							OWLOntology gciOnt = null;
							OWLOntology unsatOnt = null;
							boolean loadsuccess = false;
							
							try{
								backgroundOnt = manager1.loadOntologyFromOntologyDocument(backgroundFile);
								gciOnt = manager2.loadOntologyFromOntologyDocument(gciFile);
								unsatOnt = manager3.loadOntologyFromOntologyDocument(unsatFile);
								loadsuccess = true;
							}
							catch(Exception e){
								System.out.println("Could not load ontology");
							}
							
							if (loadsuccess){
								manager1.addAxioms(backgroundOnt, gciOnt.getAxioms());
								System.out.println("Ontologies loaded!");
								// 1b. Amalgamate individual modules per lhs
								SyntacticLocalityModuleExtractor slme = new SyntacticLocalityModuleExtractor(manager1, backgroundOnt, ModuleType.STAR);
								System.out.println("Ontology size before: " + backgroundOnt.getAxiomCount());
								Set<OWLAxiom> module = new HashSet<OWLAxiom>();
								for (OWLAxiom a: unsatOnt.getAxioms()){
									if (a.isOfType(AxiomType.SUBCLASS_OF)){
										OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
										OWLClassExpression lhs = sub.getSubClass();
										// 1c. Get individual module for sig(lhs)
										module.addAll(slme.extract(lhs.getSignature()));
									}
								}
								System.out.println("Ontology size after: " + module.size());
								
								// 1d. Reduce module axioms so that we have unique LHS concepts
								Set<OWLClassExpression> uniqueLHSs = new HashSet<OWLClassExpression>();
								int subCount = 0;
								
								for (OWLAxiom a: module){
									if (a.isOfType(AxiomType.SUBCLASS_OF)){
										subCount++;
										OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
										OWLClassExpression lhs = sub.getSubClass();
										uniqueLHSs.add(lhs);
									}
								}
								
								if (uniqueLHSs.size() < subCount){
									
									Set<OWLAxiom> uniqueLHSAxioms = new HashSet<OWLAxiom>();
									// 1di. Find RHSs for unique LHSs
									for (OWLClassExpression lhs: uniqueLHSs){
										// Get RHSs for this LHS
										Set<OWLClassExpression> rhss = new HashSet<OWLClassExpression>();
										for (OWLAxiom a: module){
											OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
											if (sub.getSubClass().equals(lhs))
												rhss.add(sub.getSuperClass());
										}
										// Get conjunction of RHSs
										OWLDataFactory df = OWLManager.getOWLDataFactory();
										OWLClassExpression conjOfRHSs = df.getOWLObjectIntersectionOf(rhss);
										// Construct single axiom from knowledge
										OWLSubClassOfAxiom result = df.getOWLSubClassOfAxiom(lhs, conjOfRHSs);
										uniqueLHSAxioms.add(result);
									}
									
									// 1dii. Remove all subclass axioms
									for (OWLAxiom a: module){
										if (a.isOfType(AxiomType.SUBCLASS_OF))
											module.remove(a);
									}
									
									// 1diii. Put subclass axioms back with unique lhs 
									module.addAll(uniqueLHSAxioms);
		
									System.out.println("Ontology size after (unique) reduction: " + module.size());
								}
								else{
									System.out.println("No reduction required");
								}
								
								// 1e. Uli's sanity check (check if T in <T,D> is coherent)
								Set<OWLAxiom> tAxioms = new HashSet<OWLAxiom>();
								for (OWLAxiom a: backgroundOnt.getAxioms()){
									if (!a.isOfType(AxiomType.SUBCLASS_OF)|| !module.contains(a))
										tAxioms.add(a);
								}
								OWLOntologyManager m = OWLManager.createOWLOntologyManager();
								OWLOntology tOnt = null;
								boolean loaded2 = false;
								try{
									tOnt = m.createOntology(tAxioms);
									loaded2 = true;
								}
								catch(Exception e){
									System.out.println("Could not create T ontology!");
								}
								
								if (loaded2){
									//Reasoner reasoner = new Reasoner(tOnt);
									
									/*if (reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom().size() > 0)
										System.out.println("We have trouble - T is incoherent!");*/
								
									// 1f. Make relevant axioms from backgroundOnt defeasible.
									
									// remove non-subclass module axioms
									for (OWLAxiom a: module){
										if (!a.isOfType(AxiomType.SUBCLASS_OF))
											module.remove(a);
									}
									
									// remove all subclass module axioms from backgroundOnt
									manager1.removeAxioms(backgroundOnt, module);
									
									// put back defeasible versions of the subclass module axioms
									OWLDataFactory df = OWLManager.getOWLDataFactory();
									OWLAnnotation anno = df.getOWLAnnotation(defeasibleAnnotationProperty, df.getOWLLiteral(true));
									Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>(Collections.singleton(anno));
									for (OWLAxiom a: module){
										if (a.isOfType(AxiomType.SUBCLASS_OF)){
											manager1.addAxiom(backgroundOnt, a.getAnnotatedAxiom(annos));
										}
									}
									
								}// loaded2
								
								 // 1g. Save defeasible axioms + background knowledge to new ontology
								 OWLOntologyFormat format = manager1.getOntologyFormat(backgroundOnt);
								 File processedOntologyFile = new File("data/mowlcorp/files/incoherent/" + dir2.getName() + "/processed.owl");
								 OWLOntologyManager pmanager = OWLManager.createOWLOntologyManager(); 
								 OWLOntology processedOntology = pmanager.createOntology();
								 pmanager.addAxioms(processedOntology, backgroundOnt.getAxioms());
								 pmanager.saveOntology(processedOntology, format, IRI.create(processedOntologyFile.toURI()));
								 System.out.println("Processed ontology " + dir2.getName());
								
							}//loadsuccess
					}//isDirectory
			 }//for each incoherent directory
		 }//rootdirectory incoherent ontologies
	}//main method
}//endofclass
		
		
		
		
		
		
		/*for (int j = 10;j <= 10;j++){
		for (int i = 1;i <= 50;i++){
				/*BufferedReader br = new BufferedReader(new FileReader("results/mowlcorp/results.txt"));
			    try {
			        StringBuilder sb = new StringBuilder();
			        String line = br.readLine();

			        boolean nextLineIsRank = false;
			        while (line != null) {
			        	if (line.endsWith("s")){
			        		nextLineIsRank = true;
			        	}
			        	else{
			        		if (nextLineIsRank){
			        			sb.append(line);
			        			sb.append("\r\n");
			        			nextLineIsRank = false;
			        		}			        		
			        	}
			        	line = br.readLine();
			        }
			        
			        String everything = sb.toString();
			        //System.out.println(everything);
			        out = new PrintWriter("Experiments/OntGen/results_ranks.txt");
					out.println(everything);
					out.close();
			    } finally {
			        br.close();
			    }*/
				
				//Load Ontology
				/*File file = new File("Experiments/"+j*10+"/Ontology"+i+"/ontology"+i+".owl");					
				OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
			
				//Compute Ranking
				OntologyStructure structure = new OntologyStructure(ontology);
					
				ReasonerFactory reasonerFactory = new ReasonerFactory();//HermiT
				//PelletReasonerFactory reasonerFactory = new PelletReasonerFactory();//Pellet
				AlgorithmDelegator ad = new AlgorithmDelegator(reasonerFactory, structure, null, ReasoningType.PROTOTYPICAL1);
				long time = 0;
				long start = mx.getCurrentThreadCpuTime();
				ad.computeRanking();
				time = mx.getCurrentThreadCpuTime() - start;
				Double rankingTime = new Double(time);
				
				int count = 1;
				System.out.println();
				Ranking ranking = ad.ranking;	
				for (Rank rank: ranking.getRanking()){
	            	System.out.print(rank.size());
	            }
				System.out.println();
				FileOutputStream fos = new FileOutputStream("Experiments/"+j*10+"/Ontology"+i+"/ranking.bin");
	            ObjectOutputStream oos = new ObjectOutputStream(fos);
	            oos.writeObject(ranking);
	            oos.close();
	            
	            FileInputStream fis = new FileInputStream("Experiments/"+j*10+"/Ontology"+i+"/ranking.bin");
	            ObjectInputStream ois = new ObjectInputStream(fis);
	            Ranking readRanking = (Ranking) ois.readObject();
	            System.out.println((readRanking.size()) == ranking.size());
	            for (Rank rank: readRanking.getRanking()){
	            	System.out.print(rank.size());
	            }
	            System.out.println();
	            System.out.println((readRanking.size()) == ranking.size());
	            System.out.println();
	            ois.close();
	            
				/*IRI ontologyIRI = null;
				OWLOntology currentOntology = null;
				File rankFile = null;
				OWLXMLOntologyFormat owlxmlFormat = null;
				for (Rank rank: ranking.getRanking()){
					ontologyIRI = IRI.create("http://cair.meraka.org.za/ontologies/defOnt"+i+"_rank"+count);
				    currentOntology = manager.createOntology(ontologyIRI);
				    Set<OWLAxiom> set = new HashSet<OWLAxiom>();
				    set.addAll(rank.getAxioms());
				    manager.addAxioms(currentOntology, set);
				    rankFile = new File("Experiments/10/Ontology"+i+"/Ranks/onto"+i+"_rank"+count+".owl");
				   	owlxmlFormat = new OWLXMLOntologyFormat();
				   	manager.saveOntology(currentOntology, owlxmlFormat, IRI.create(rankFile.toURI()));
				   	count++;
				}*/				
				/*System.out.println("Ontology " + i);
				str += rankingTime/1000000000 + "\r\n";						
			}			
			out = new PrintWriter("Experiments/"+j*10+"/rankingTimes.txt");
			out.println(str);
			out.close();
			System.out.println("Finished!");
		}
	}*/

