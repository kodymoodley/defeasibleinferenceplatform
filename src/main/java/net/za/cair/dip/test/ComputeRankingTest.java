package net.za.cair.dip.test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import net.za.cair.dip.model.OntologyStructure;
import net.za.cair.dip.model.Rank;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.transform.RationalRankingAlgorithm;


public class ComputeRankingTest {

	public static void main(String [] args) throws OWLException {		
		System.out.println(System.getProperty("user.dir"));
		final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("Penguin.owl"));
		System.out.println(ontology.getLogicalAxiomCount());

		/********** Preprocessing Stage ***********/
		OntologyStructure structure = new OntologyStructure(ontology);
		ReasonerFactory factory = new ReasonerFactory();
		OWLReasoner r = factory.createNonBufferingReasoner(ontology);
		//System.out.println(r.getUnsatisfiableClasses());
		Set<OWLClassExpression> c = new HashSet<OWLClassExpression>();
		for (OWLClass cl : r.getUnsatisfiableClasses()){
			c.add(cl);
		}
		//OWLReasoner reasoner=new Reasoner.createReasoner(ontology);
		//ReasoningType algorithm = ReasoningType.NAME_TYPE_MAP.get((String)reasoningList.getSelectedItem());
		RationalRankingAlgorithm rankingConstruction = new RationalRankingAlgorithm(factory, structure, c);
		Ranking ranking = rankingConstruction.computeRanking();
		Rank infiniteRank = rankingConstruction.getInfiniteRank();
		System.out.println(ranking.getRanking());
		//System.out.println(infiniteRank);
	}

}
