package net.za.cair.dip.transform;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.za.cair.dip.model.OntologyStructure;
import net.za.cair.dip.util.ManchesterOWLSyntaxOWLObjectRendererImpl;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class RankingHelperClass {
	private OWLReasonerFactory reasonerFactory;
	private OntologyStructure  structure;
	private OWLDataFactory df;
	public int unsatLHSDefeasibleSubsumptions;
	private ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	private final String PREFIX = "KodyMoodley";
		
	public RankingHelperClass(OWLReasonerFactory reasonerFactory, OntologyStructure structure){
		this.df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		this.reasonerFactory = reasonerFactory;
		this.structure = structure;
		this.unsatLHSDefeasibleSubsumptions = 0;
	}
	
	private OWLClassExpression getCorrespondingComplexConcept(Set<OWLAxiom> axioms, OWLClass c){
		//System.out.println("got here");
		for (OWLAxiom a: axioms){
			if (a.isOfType(AxiomType.EQUIVALENT_CLASSES)){
				OWLEquivalentClassesAxiom equiv = (OWLEquivalentClassesAxiom)a;
				Set<OWLSubClassOfAxiom> subs = equiv.asOWLSubClassOfAxioms();
				OWLSubClassOfAxiom sub = null;
				
				for (OWLSubClassOfAxiom s: subs)
					sub = s;
				
				if (renderer.render(sub.getSubClass()).equals(renderer.render(c))){
					//System.out.println(sub.getSubClass());
					//System.out.println(sub.getSuperClass());
					return sub.getSuperClass();
				}
				
				if (renderer.render(sub.getSuperClass()).equals(renderer.render(c))){
					//System.out.println(sub.getSuperClass());
					//System.out.println(sub.getSubClass());
					return sub.getSubClass();
				}
			}
		}
		//Should be impossible
		return null;
	}
	
	/** Optimisation for ranking procedure. Only returns the UNSATISFIABLE LHS concepts
	 *  of the defeasible axioms in the ontology. Only these could possibly be exceptional */
	
	public Set<OWLClassExpression> getPossibleExceptions() throws OWLOntologyCreationException, InconsistentOntologyException{
		Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
		
		Set<OWLAxiom> defeasible = structure.dBox.getAxioms();
		Set<OWLClassExpression> lhsConceptsOfDefeasibleSubsumptions = new HashSet<OWLClassExpression>();
		Set<OWLClassExpression> complexLHSConcepts = new HashSet<OWLClassExpression>();
		
		// Collect all (UNIQUE!) complex LHS Concepts
		
		for (OWLAxiom a: defeasible){
			if (a.isOfType(AxiomType.SUBCLASS_OF)){
				OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
				lhsConceptsOfDefeasibleSubsumptions.add(sub.getSubClass());
				if (sub.getSubClass().isAnonymous()){
					complexLHSConcepts.add(sub.getSubClass());
				}
			}
		}
		
		// Introduce names for them, add equivalence axioms to the ontology
		// linking them to their complex counterparts.
		
		Set<OWLAxiom> originalOntologyAxioms = new HashSet<OWLAxiom>();
		for (OWLAxiom a: structure.getOriginalOWLOntology().getAxioms()){
			if (!a.isOfType(AxiomType.ABoxAxiomTypes))					// Do not consider ABox axioms because it can lead to inconsistency
				originalOntologyAxioms.add(a);
		}
		
		Set<OWLAxiom> newOntologyAxioms = new HashSet<OWLAxiom>();
		for (OWLAxiom a: originalOntologyAxioms){
			newOntologyAxioms.add(a);
		}
		
		int count = 0;

		for (OWLClassExpression c: complexLHSConcepts){
			OWLEquivalentClassesAxiom equiv = df.getOWLEquivalentClassesAxiom(df.getOWLClass(IRI.create(this.PREFIX + count)), c);
			newOntologyAxioms.add(equiv);
			count++;
		}
		
		// Classify new ontology and obtain unsatisfiable class names.
		
		OWLOntology newOntology = OWLManager.createOWLOntologyManager().createOntology(newOntologyAxioms);
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(newOntology);
		Node<OWLClass> unsatClses = null;
		
		try {
			unsatClses = reasoner.getUnsatisfiableClasses();
		}
		catch (InconsistentOntologyException e) {
			// Ontology is classically inconsistent here. Plus we do not consider ABox axioms. Therefore, must be also preferentially inconsistent (TBox inconsistent).
			// Theorem 1: A defeasible ontology <T,D> (no ABox) is preferentially inconsistent iff T U D' is classically inconsistent (D' is classical translation of D).  
			throw new InconsistentOntologyException();			
		}
		
		// Identify the possible exceptions: the unsatisfiable concepts
		// that were names in the original ontology, plus the expressions
		// from the original ontology that are LINKED to NEWLY INTRODUCED
		// names.
		
		for (OWLClass c: unsatClses){
			if (!c.isOWLNothing()){
				//System.out.println(renderer.render(c));
				if (renderer.render(c).contains(this.PREFIX)){
					// We know that it is an introduced name
					OWLClassExpression tmp = getCorrespondingComplexConcept(newOntology.getAxioms(), c);
					result.add(tmp);
				}
				else{
					// Only add if it is a LHS concept of a defeasible subsumption
					if (lhsConceptsOfDefeasibleSubsumptions.contains(c))
						result.add(c);
				}
			}
		}
		/*System.out.println("Possible exceptions:");
		for (OWLClassExpression c: result){
			System.out.println(renderer.render(c));
		}*/
		this.unsatLHSDefeasibleSubsumptions = result.size();
		return result;
	}
	
	public Set<OWLClassExpression> getAllLHSConcepts(){
		Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
		
		Set<OWLAxiom> defeasible = structure.dBox.getAxioms();
		
		// Collect all (UNIQUE!) LHS Concepts
		for (OWLAxiom a: defeasible){
			if (a.isOfType(AxiomType.SUBCLASS_OF)){
				OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
				result.add(sub.getSubClass());
			}
		}
	
		return result;
	}
}
