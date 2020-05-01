package net.za.cair.dip.transform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.za.cair.dip.model.OntologyStructure;
import net.za.cair.dip.model.Rank;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.util.ManchesterOWLSyntaxOWLObjectRendererImpl;
import net.za.cair.dip.util.Utility;

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
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
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
	
	public RankingHelperClass() {
		this.df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
	}
	
	private OWLClassExpression getCorrespondingComplexConcept(Set<OWLAxiom> axioms, OWLClass c){
		for (OWLAxiom a: axioms){
			if (a.isOfType(AxiomType.EQUIVALENT_CLASSES)){
				OWLEquivalentClassesAxiom equiv = (OWLEquivalentClassesAxiom)a;
				Set<OWLSubClassOfAxiom> subs = equiv.asOWLSubClassOfAxioms();
				OWLSubClassOfAxiom sub = null;
				
				for (OWLSubClassOfAxiom s: subs)
					sub = s;
				
				if (renderer.render(sub.getSubClass()).equals(renderer.render(c))){
					return sub.getSuperClass();
				}
				
				if (renderer.render(sub.getSuperClass()).equals(renderer.render(c))){
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

		this.unsatLHSDefeasibleSubsumptions = result.size();
		return result;
	}
	
	public Set<OWLClassExpression> getTotallyExceptionalStrictClasses() throws OWLOntologyCreationException{
		Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();		
		Set<OWLAxiom> strict_axioms = structure.bBox.getAxioms();
		Set<OWLAxiom> strict_tbox_axioms = new HashSet<OWLAxiom>();
		Set<OWLClassExpression> lhsConceptsOfStrictSubsumptions = new HashSet<OWLClassExpression>();
		Set<OWLClassExpression> complexLHSConcepts = new HashSet<OWLClassExpression>();
		
		// Collect all (UNIQUE!) complex LHS Concepts
		
		for (OWLAxiom a: strict_axioms){
			if (a.isOfType(AxiomType.TBoxAndRBoxAxiomTypes)) {
				strict_tbox_axioms.add(a);
				if (a.isOfType(AxiomType.SUBCLASS_OF)){
					OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
					lhsConceptsOfStrictSubsumptions.add(sub.getSubClass());
					if (sub.getSubClass().isAnonymous()){
						complexLHSConcepts.add(sub.getSubClass());
					}
				}
			}
			
		}
		
		// Introduce names for them, add equivalence axioms to the ontology
		// linking them to their complex counterparts.
		
		int count = 0;
		for (OWLClassExpression c: complexLHSConcepts){
			OWLEquivalentClassesAxiom equiv = df.getOWLEquivalentClassesAxiom(df.getOWLClass(IRI.create(this.PREFIX + count)), c);
			strict_tbox_axioms.add(equiv);
			count++;
		}
		
		// Classify new ontology and obtain unsatisfiable class names.
		OWLOntology newOntology = OWLManager.createOWLOntologyManager().createOntology(strict_tbox_axioms);
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
				if (renderer.render(c).contains(this.PREFIX)){
					// We know that it is an introduced name
					OWLClassExpression tmp = getCorrespondingComplexConcept(newOntology.getAxioms(), c);
					result.add(tmp);
				}
				else{
					// Only add if it is a LHS concept of a strict subsumption
					if (lhsConceptsOfStrictSubsumptions.contains(c))
						result.add(c);
				}
			}
		}
		
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
	
	public int getConceptRank(Set<OWLAxiom> t_star, ArrayList<ArrayList<OWLAxiom>> eTransforms, OWLClassExpression c, OWLReasonerFactory rf) throws OWLOntologyCreationException {
		int i = 0;
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology t_starOntology = manager.createOntology(t_star);
		OWLReasoner entailmentChecker = rf.createNonBufferingReasoner(t_starOntology);
		OWLClassExpression mat_e_i = Utility.getConjOfMateri(new HashSet<OWLAxiom>(eTransforms.get(i)));
		OWLClassExpression lhs = manager.getOWLDataFactory().getOWLObjectIntersectionOf(mat_e_i, c);
//		System.out.println();
//		System.out.print("i = " + i + ", T* = {");
//		for (OWLAxiom a: t_star) {
//			System.out.print(renderer.render(a) + ";");
//		}
//		System.out.println("}");
//		System.out.println("lhs = " + renderer.render(lhs));
		while (!entailmentChecker.isSatisfiable(lhs) && (i <= eTransforms.size()-1)){
			i = i + 1;
			
			if (i <= eTransforms.size()-1) {
				mat_e_i = Utility.getConjOfMateri(new HashSet<OWLAxiom>(eTransforms.get(i)));
				lhs = manager.getOWLDataFactory().getOWLObjectIntersectionOf(mat_e_i, c);	
			}
			else {
				lhs = c;
			}
			
//			System.out.print("i = " + i + ", T* = {");
//			for (OWLAxiom a: t_star) {
//				System.out.print(renderer.render(a) + ";");
//			}
//			System.out.println("}");
//			System.out.println("lhs = " + renderer.render(lhs));
//			System.out.println();
		}
		if ((i <= eTransforms.size()-1) && entailmentChecker.isSatisfiable(lhs)) {
		//if (entailmentChecker.isSatisfiable(lhs)){
			//System.out.println("normal*");
			return i;
		}
		else if ((i > eTransforms.size()-1) && !entailmentChecker.isSatisfiable(lhs)) {
			//System.out.println("infinite!");
			return -1;
		}
		else { // ((i > eTransforms.size()-1) && entailmentChecker.isSatisfiable(lhs)) {
			//System.out.println("normal*");
			return i;
		}
	}
	
	/** Optimisation for ranking procedure. Only returns the UNSATISFIABLE LHS concepts
	 *  of the defeasible axioms in the ontology. Only these could possibly be exceptional */
	
	public Set<OWLClassExpression> getPossibleExceptions(Set<OWLAxiom> strict, Set<OWLAxiom> allTBoxAxioms, OWLReasonerFactory rf) throws OWLOntologyCreationException, InconsistentOntologyException{
		Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
		Set<OWLClassExpression> lhsConceptsOfStrictSubsumptions = new HashSet<OWLClassExpression>();
		Set<OWLClassExpression> complexLHSConcepts = new HashSet<OWLClassExpression>();
		
		// Collect all (UNIQUE!) complex LHS Concepts
		
		for (OWLAxiom a: strict){
			if (a.isOfType(AxiomType.SUBCLASS_OF)){
				OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
				lhsConceptsOfStrictSubsumptions.add(sub.getSubClass());
				if (sub.getSubClass().isAnonymous()){
					complexLHSConcepts.add(sub.getSubClass());
				}
			}
		}
		
		// Introduce names for them, add equivalence axioms to the ontology
		// linking them to their complex counterparts.
		Set<OWLAxiom> originalOntologyAxioms = new HashSet<OWLAxiom>();
		originalOntologyAxioms.addAll(allTBoxAxioms);
		Set<OWLAxiom> newOntologyAxioms = new HashSet<OWLAxiom>();
		newOntologyAxioms.addAll(allTBoxAxioms);
		
		int count = 0;
		for (OWLClassExpression c: complexLHSConcepts){
			OWLEquivalentClassesAxiom equiv = df.getOWLEquivalentClassesAxiom(df.getOWLClass(IRI.create(this.PREFIX + count)), c);
			newOntologyAxioms.add(equiv);
			count++;
		}
		
		// Classify new ontology and obtain unsatisfiable class names.
		OWLOntology newOntology = OWLManager.createOWLOntologyManager().createOntology(newOntologyAxioms);
		OWLReasoner reasoner = rf.createNonBufferingReasoner(newOntology);
		Node<OWLClass> unsatClses = null;
		
		try {
			unsatClses = reasoner.getUnsatisfiableClasses();
		}
		catch (InconsistentOntologyException e) {
			// Ontology is classically inconsistent here. Plus we do not consider ABox axioms. Therefore, must be also preferentially inconsistent (TBox inconsistent).
			// Theorem 1: A strict ontology <T,D> (no ABox) is preferentially inconsistent iff T U D' is classically inconsistent (D' is classical translation of D).  
			throw new InconsistentOntologyException();			
		}
		
		// Identify the possible exceptions: the unsatisfiable concepts
		// that were names in the original ontology, plus the expressions
		// from the original ontology that are LINKED to NEWLY INTRODUCED
		// names.
		
		for (OWLClass c: unsatClses){
			if (!c.isOWLNothing()){
				
				if (renderer.render(c).contains(this.PREFIX)){
					// We know that it is an introduced name
					OWLClassExpression tmp = getCorrespondingComplexConcept(newOntology.getAxioms(), c);
					result.add(tmp);
				}
				else{
					// Only add if it is a LHS concept of a strict subsumption
					if (lhsConceptsOfStrictSubsumptions.contains(c)) {
						result.add(c);
					}
				}
			}
		}

		return result;
	}
	
	public Ranking enrichRankingWithStrictInclusions(Ranking oldRanking, Set<OWLAxiom> ontologyAxioms, OWLReasonerFactory rf) throws InconsistentOntologyException, OWLOntologyCreationException {		
		Ranking tmpRanking = new Ranking(oldRanking.getRanking());
		tmpRanking.setInfiniteRank(oldRanking.getInfiniteRank());		
		Utility u = new Utility();
		Set<OWLAxiom> defeasibleAxioms = new HashSet<OWLAxiom>();							// Defeasible axioms
		OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology(ontologyAxioms);
		Set<OWLAxiom> strictAxioms = new HashSet<OWLAxiom>();								// Strict axioms
		
		for (OWLAxiom a: ontology.getAxioms()) {
			if (u.isDefeasible(a)) {
				System.out.println("Defeasible: " + renderer.render(a));
				defeasibleAxioms.add(a);
			}
			else {
				System.out.println("Strict: " + renderer.render(a));
				strictAxioms.add(a);
			}
		}
		
		Set<OWLAxiom> tboxAxioms = ontology.getTBoxAxioms(Imports.EXCLUDED);				// TBox axioms
		ArrayList<OWLAxiom> infiniteRankAxioms = tmpRanking.getInfiniteRank().getAxioms();	// Infinite rank axioms
		Set<OWLAxiom> t_starAxioms = new HashSet<OWLAxiom>();
		t_starAxioms.addAll(strictAxioms);
		t_starAxioms.addAll(infiniteRankAxioms);
		ArrayList<ArrayList<OWLAxiom>> eTransforms = oldRanking.getETransforms();			// eTransforms
		int count = 0;
		for (ArrayList<OWLAxiom> arr: eTransforms) {
			System.out.println("E"+count+":");
			System.out.println("----");
			for (OWLAxiom a: arr) {
				System.out.println(renderer.render(a));
			}
			count++;
			System.out.println("----");
		}
		System.out.println();
		
		Set<OWLClassExpression> possibleExceptionalClasses = getPossibleExceptions(strictAxioms, tboxAxioms, rf);
		
		int rankC = -2;
		
		for (OWLClassExpression c: possibleExceptionalClasses) {			
			rankC = getConceptRank(t_starAxioms, eTransforms, c, rf);
			System.out.println("The rank of " + renderer.render(c) + " is: " + rankC);
			if (rankC >= 0) {
				Set<OWLAxiom> rank_axioms = tmpRanking.get(rankC).getAxiomsAsSet();
				OWLSubClassOfAxiom sub = df.getOWLSubClassOfAxiom(c, df.getOWLThing());
				rank_axioms.add(sub);
				tmpRanking.setRank(rank_axioms, rankC);
			}
			else {
				ArrayList<OWLAxiom> rank_axioms = tmpRanking.getInfiniteRank().getAxioms();
				OWLSubClassOfAxiom sub = df.getOWLSubClassOfAxiom(c, df.getOWLThing());
				rank_axioms.add(sub);
				tmpRanking.setInfiniteRank(new Rank(rank_axioms));
			}
		}
		
		return tmpRanking;
	}
}
