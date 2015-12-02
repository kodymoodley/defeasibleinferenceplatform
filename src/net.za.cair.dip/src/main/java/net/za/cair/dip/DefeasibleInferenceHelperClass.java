package net.za.cair.dip;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.api.ExplanationGenerator;
import org.semanticweb.owl.explanation.api.ExplanationGeneratorFactory;
import org.semanticweb.owl.explanation.api.ExplanationManager;
import org.semanticweb.owl.explanation.impl.blackbox.hst.HittingSetTree;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import net.za.cair.dip.model.Query;
import net.za.cair.dip.model.Rank;
import net.za.cair.dip.model.Ranking;

/*
 * Copyright (C) 2011, Centre for Artificial Intelligence Research
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Kody Moodley<br>
 * Centre for Artificial Intelligence Research<br>
 @SuppressWarnings("ucd")
 * UKZN and CSIR<br>
 * Date: 10-Oct-2011<br><br>
 * 
 * A utility class for DefeasibleInferenceComputer to specify helper functions used in that class
 * @param <E>
 * @param <E>
 */

public class DefeasibleInferenceHelperClass {
	
	private OWLReasonerFactory reasonerFactory;
	private Ranking ranking;
	private ArrayList<OWLAxiom> problematicRank;
	private boolean cBasisValid;
	private OWLDataFactory df;
	public int sizeOfCCompat;
	public int noOfAxiomsInCCompat;
	public boolean antecedentHasInfiniteRank;
	public boolean antecedentIsNonExceptional;
	public ArrayList<Rank> ccompat;
	public int keptAxiomsFromPRank;
	public int noOfJusts;
	public double avgSizeOfAJust;
	public int cbasisSize;
	public Set<OWLAxiom> cbasis;
	public int mincbasisSize;
	public boolean mincbasisTimeout;
	public HittingSetTree<OWLAxiom> hst;
	public int hsTreeSize;
	public int entailmentChecks;
	public int justsEntailmentChecks;
	
	//private ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
    
	public DefeasibleInferenceHelperClass(OWLReasonerFactory reasonerFactory, Ranking ranking){
		this.reasonerFactory = reasonerFactory;
		this.ranking = setRanking(ranking);
		this.df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		this.problematicRank = null;
		this.cBasisValid = false;
		this.sizeOfCCompat = 0;
		this.antecedentHasInfiniteRank = false;
		this.antecedentIsNonExceptional = false;
		mincbasisTimeout = false;
		mincbasisSize = 0;
		avgSizeOfAJust = 0.0;
		noOfJusts = 0;
		sizeOfCCompat = ranking.size();
		noOfAxiomsInCCompat = 0;
		for (Rank r: ranking.getRanking()){
			noOfAxiomsInCCompat += r.getAxioms().size();
		}
		keptAxiomsFromPRank = 0;
		entailmentChecks = 0;
		justsEntailmentChecks = 0;
		hsTreeSize = 0;
	}
	
	private Ranking setRanking(Ranking r){
		ArrayList<Rank> ranks = new ArrayList<Rank>();
		ArrayList<Rank> result = new ArrayList<Rank>();
		ranks.addAll(r.getRanking());
		
		for (Rank rank: ranks){
			ArrayList<OWLAxiom> currentRankAxioms = new ArrayList<OWLAxiom>();
			currentRankAxioms.addAll(rank.getAxioms());
			Rank tmp = new Rank(currentRankAxioms);
			result.add(tmp);
		}
		
		ArrayList<OWLAxiom> inf = new ArrayList<OWLAxiom>();
		inf.addAll(r.getInfiniteRank().getAxioms());
		
		Ranking finalResult = new Ranking(result);
		finalResult.setInfiniteRank(new Rank(inf));
		
		return finalResult;
	}
	
	
	
	
	private Set<OWLAxiom> getMinimallyRanked(Set<OWLAxiom> axioms){
		int i = getLowestRank(axioms);

		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		
		for (OWLAxiom a: axioms){
			if (rank(a) == i)
				result.add(a);
		}
		
		return result;
	}
	
	private int rank(OWLAxiom a){
		ArrayList<Rank> ranks = new ArrayList<Rank>(ranking.getRanking());
		/*** Reverse ranking order ****/
		int n = ranks.size();
	    for (int i = 0; i <= Math.floor((n-2)/2);i++){
	         Rank tmp = ranks.get(i);
	         ranks.set(i, ranks.get(n - 1 - i));
	         ranks.set(n - 1 - i, tmp);
		}
	    
		for (Rank r: ranks){
			if (r.getAxioms().contains(a))
				return ranks.indexOf(r);
		}
		
		return Integer.MAX_VALUE;
	}
	
	private int getLowestRank(Set<OWLAxiom> axioms){
		int lowest = Integer.MAX_VALUE;
		
		for (OWLAxiom a: axioms){
			int tmp = rank(a);
			if (tmp < lowest)
				lowest = tmp;
		}
		
		return lowest;
	}
	
	public Set<OWLAxiom> getCBasis(OWLSubClassOfAxiom axiom) throws OWLOntologyCreationException{
		// Get hold of an explanation generator factory
		ExplanationGeneratorFactory<OWLAxiom> genFac = ExplanationManager.createExplanationGeneratorFactory(reasonerFactory);
		// Create an ontology holding all the axioms in our ranking
		Set<OWLAxiom> ontologyAxioms = new HashSet<OWLAxiom>();
		ontologyAxioms.addAll(ranking.getAxiomsMinusInfiniteRank());
		ontologyAxioms.addAll(ranking.getInfiniteRank().getAxiomsAsSet());
		OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology(ontologyAxioms);
		
		// Create an actual explanation generator for our ontology using the factory
		ExplanationGenerator<OWLAxiom> gen = genFac.createExplanationGenerator(ontology);

		OWLSubClassOfAxiom entailment = df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLObjectComplementOf(axiom.getSubClass()));
		
		// Get explanations for exceptionality
		Set<Explanation<OWLAxiom>> expl = gen.getExplanations(entailment);
		
		/*** Log some stuff ***/
		noOfJusts = expl.size();
		/**********************/
		
		// CBasis is the union of all justifications
		double totalAxioms = 0.0;
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		for (Explanation<OWLAxiom> exp: expl){
			result.addAll(exp.getAxioms());
			totalAxioms += exp.getAxioms().size();
		}
		
		/*** Log some stuff ***/
		avgSizeOfAJust = totalAxioms/expl.size();
		cbasisSize = result.size();
		/**********************/
		
		
		// Filter out the TBox axioms
		result.removeAll(ranking.getInfiniteRank().getAxiomsAsSet());
				
		// Check validity of MinCBasis
		if (!result.isEmpty())
			cBasisValid = true;
		
		// Return CBasis
		return result;
	}
	
	public Set<OWLAxiom> getMinCBasis(OWLSubClassOfAxiom axiom) throws OWLOntologyCreationException{
		// Get hold of an explanation generator factory
		ExplanationGeneratorFactory<OWLAxiom> genFac = ExplanationManager.createExplanationGeneratorFactory(reasonerFactory);
		// Create an ontology holding all the axioms in our ranking
		Set<OWLAxiom> ontologyAxioms = new HashSet<OWLAxiom>();
		ontologyAxioms.addAll(ranking.getAxiomsMinusInfiniteRank());
		ontologyAxioms.addAll(ranking.getInfiniteRank().getAxiomsAsSet());
		OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology(ontologyAxioms);
		
		// Create an actual explanation generator for our ontology using the factory
		ExplanationGenerator<OWLAxiom> gen = genFac.createExplanationGenerator(ontology);	
		// Transform input axiom into exceptionality axiom
		OWLSubClassOfAxiom entailment = df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLObjectComplementOf(axiom.getSubClass()));
		//Set<Explanation<OWLAxiom>> expl = null;
		Set<OWLAxiom> result = new HashSet<OWLAxiom>(); //mincbasis
		Set<OWLAxiom> result2 = new HashSet<OWLAxiom>(); //cbasis
	
		// Get explanations for exceptionality
		Set<Explanation<OWLAxiom>> expl = null;
		expl = gen.getExplanations(entailment);
		hsTreeSize = gen.getHST().getTreeSize();
		justsEntailmentChecks = gen.getEntailmentChecks();
		
		/*** Log some stuff ***/
		noOfJusts = expl.size();
		/**********************/
		
		// MinCBasis is the union of all the minimally ranked
		// axioms in each justification.
		double totalAxioms = 0.0;
		
		for (Explanation<OWLAxiom> exp: expl){
			Set<OWLAxiom> tmp = new HashSet<OWLAxiom>();
			tmp.addAll(exp.getAxioms());
			result.addAll(getMinimallyRanked(tmp));
			result2.addAll(tmp);
			totalAxioms += exp.getAxioms().size();
		}
		
		/*** Log some stuff ***/
		if (noOfJusts > 0)
			avgSizeOfAJust = totalAxioms/expl.size();
		else
			avgSizeOfAJust = 0;
		
		mincbasisSize = result.size();
		cbasisSize = result2.size();
		//System.out.println(cbasisSize);
		cbasis = new HashSet<OWLAxiom>();
		cbasis.addAll(result2);
		/**********************/
		
		// Filter out the TBox axioms
		result.removeAll(ranking.getInfiniteRank().getAxiomsAsSet());
		
		// Check validity of MinCBasis
		if (!result.isEmpty())
			cBasisValid = true;
		
		// Return MinCBasis
		return result;
	}
	
	public ArrayList<Rank> getMRCCompatibleSubset(ArrayList<Rank> ranksVar, Query originalQuery, OWLSubClassOfAxiom modifiedQuery, Set<OWLAxiom> cbasis) throws OWLOntologyCreationException{
		ArrayList<Rank> ranks = new ArrayList<Rank>();
		for (Rank r: ranksVar){
			ArrayList<OWLAxiom> newRankAxioms = new ArrayList<OWLAxiom>();
			for (OWLAxiom a: r.getAxioms()){
				newRankAxioms.add(a);
			}
			Rank tmp = new Rank(newRankAxioms);
			ranks.add(tmp);
		}
		 /**Maximally Relevant C-Compatibility calculation**/
		int rankIndex = 0;
		
		while (antecedentExceptional(modifiedQuery.getSubClass(), ranks, ranking.getInfiniteRank().getAxiomsAsSet()) && rankIndex < ranking.size()){
			Rank r = ranks.get(rankIndex);
					
			/** Remove only those axioms from this rank which 
			 *  appear in the CBasis.
			 * */
		
			// Step 1: remove all axioms from the rank which appear in the cbasis
			Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
			for (OWLAxiom a: r.getAxiomsAsSet()){
				axioms.add(a);
			}
			
			axioms.removeAll(cbasis);
			
			/*System.out.println();
			System.out.println("Removing from rank: " + rankIndex);
			for (OWLAxiom a: axioms){
				System.out.println(renderer.render(a));
			}
			System.out.println();*/
			
			//if (axioms.isEmpty()){
			//	ranks.remove(rankIndex);
			//	if (ranks.isEmpty()){
			//		return new ArrayList<Rank>();
			//	}
			//}
			//else{
				r.setRank(new ArrayList<OWLAxiom>(axioms));
				ranks.set(rankIndex, r);
			//}
			
			
			
			// Step 2: create a new rank from the resulting set
			//Rank tmpRank = new Rank(new ArrayList<OWLAxiom>(axioms));
			// Step 3: get the index of the current rank
			//int index = ranks.indexOf(currentRank);
			//System.out.println(rankIndex);
			// Step 4: replace the old rank with the modified one
			//ranks.set(ranks.indexOf(currentRank), currentRank);
			
			rankIndex++;
		}
		
		/************** Log some stuff ***************/
		if (rankIndex == ranking.size()){
			antecedentHasInfiniteRank = true;
		}
		if ((rankIndex == 0) && (ranking.size() > 0)){
			antecedentIsNonExceptional = true;
		}
		
		ArrayList<Rank> newRanks = new ArrayList<Rank>();
		for (Rank r: ranks){
			if (!r.getAxioms().isEmpty()){
				Rank tmp = new Rank(r.getAxioms());
				newRanks.add(tmp);
			}
		}
		
		sizeOfCCompat = newRanks.size();
		
		noOfAxiomsInCCompat = 0;
		for (Rank r: newRanks){
			noOfAxiomsInCCompat += r.getAxioms().size();
		}
		/********************************************/
		ccompat = new ArrayList<Rank>();
		ccompat.addAll(newRanks);

		return newRanks;
	}
	
	/*private Set<ArrayList<OWLAxiom>> getLexicalisation(ArrayList<OWLAxiom> rank){
		Set<ArrayList<OWLAxiom>> result = new HashSet<ArrayList<OWLAxiom>>();
		ICombinatoricsVector<OWLAxiom> initialArrayList = Factory.createVector(rank);
	    Generator<OWLAxiom> gen = Factory.createSubSetGenerator(initialArrayList);
	    Factory.createSubSetGenerator(arg0, arg1)
	    Factory.createSimpleCombinationGenerator(initialVector, 3);
	    for (ICombinatoricsVector<OWLAxiom> combination : gen) {
	    	result.add(new ArrayList<OWLAxiom>(combination.getVector()));
	    }
	    return result;
	}*/
	
	/*private OWLClassExpression getCandidateLAC(Set<ArrayList<OWLAxiom>> lexicalisation, int k){
		OWLDataFactory dataF = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		
		/** Compute candidate Lexically Additive Concept */
		
		/*Set<OWLClassExpression> disjunctsOfLAC = new HashSet<OWLClassExpression>();
		Set<Set<OWLAxiom>> keepingKAxioms = new HashSet<Set<OWLAxiom>>(); 
				
		// Get the sequence in the lexicalisation that corresponds to keeping k axioms
		for (ArrayList<OWLAxiom> current: lexicalisation){
			if (k == current.size()){
				Set<OWLAxiom> curr = new HashSet<OWLAxiom>();
				for (OWLAxiom a: current){
					curr.add(a);
				}
				keepingKAxioms.add(curr);
			}
		}
				
		// Collect all the disjuncts of the candidate LAC from the isolated sequence
		for (Set<OWLAxiom> set: keepingKAxioms){
			OWLClassExpression tmpCls = getInternalisation(set);
			disjunctsOfLAC.add(tmpCls);
		}
				
		// Identify the candidate LAC
		return dataF.getOWLObjectUnionOf(disjunctsOfLAC);		
	}*/
	
	private OWLClassExpression getCandidateLAC(ArrayList<OWLAxiom> pRank, int k){
		OWLDataFactory dataF = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		ArrayList<OWLAxiom> tmpArr = new ArrayList<OWLAxiom>();tmpArr.addAll(pRank);
		//Set<ArrayList<OWLAxiom>> result = new HashSet<ArrayList<OWLAxiom>>();
		ICombinatoricsVector<OWLAxiom> initialArrayList = Factory.createVector(tmpArr);
	    Generator<OWLAxiom> gen =  Factory.createSimpleCombinationGenerator(initialArrayList, k);//(initialArrayList);
	   
	    Set<OWLClassExpression> disjuncts = new HashSet<OWLClassExpression>();
	    for (ICombinatoricsVector<OWLAxiom> combination : gen) {
	    	OWLClassExpression tmp = getInternalisation(new HashSet<OWLAxiom>(combination.getVector()));
	    	disjuncts.add(tmp);
	    }
	    
	    return dataF.getOWLObjectUnionOf(disjuncts);	
	}
	
	public OWLClassExpression getInternalisation(Set<OWLAxiom> axioms){
		OWLDataFactory dataF = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		Set<OWLClassExpression> indAxiomInternalisations = new HashSet<OWLClassExpression>();
		for (OWLAxiom a: axioms){
			indAxiomInternalisations.add(getInternalisation(a));
		}
		return dataF.getOWLObjectIntersectionOf(indAxiomInternalisations);
	}
	
	public OWLClassExpression getInternalisation(List<Rank> ranks){
		OWLDataFactory dataF = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		Set<OWLClassExpression> indRankInternalisations = new HashSet<OWLClassExpression>();
		for (Rank r: ranks){
			indRankInternalisations.add(getInternalisation(r.getAxiomsAsSet()));
		}
		return dataF.getOWLObjectIntersectionOf(indRankInternalisations);
	}
	
	public OWLClassExpression getInternalisation(OWLAxiom axiom){
		OWLDataFactory dataF = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		if (axiom.isOfType(AxiomType.SUBCLASS_OF)){
			OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)axiom;
			OWLClassExpression result = dataF.getOWLObjectUnionOf(dataF.getOWLObjectComplementOf(sub.getSubClass()), sub.getSuperClass());
			return result;
		}
		System.out.println("WARNING: attempt to internalise a non-subclass axiom...");
		System.out.println("...watch for null pointer exceptions!");
		return null;
	}
	
	public OWLClassExpression getLexicallyAdditiveConcept(List<Rank> compat, ArrayList<OWLAxiom> rank, OWLClassExpression subcls) throws OWLOntologyCreationException{
		OWLDataFactory dataF = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		
		/** Compute the Lexically Additive Concept */
		
		//Initialise stuff
		ArrayList<OWLAxiom> problematicRank = new ArrayList<OWLAxiom>();
		for (OWLAxiom a: rank){
			problematicRank.add(a);
		}
		
		ArrayList<Rank> cCompatibleSubset = new ArrayList<Rank>();
		for (Rank r: compat){
			ArrayList<OWLAxiom> rankAxioms = new ArrayList<OWLAxiom>();
			for (OWLAxiom a: r.getAxioms()){
				rankAxioms.add(a);
			}
			Rank tmp = new Rank(rankAxioms);
			cCompatibleSubset.add(tmp);
		}
		
		// Internalisation of compatible rank and lexicalisation of problematic rank
		OWLClassExpression cCompatibleConcept = getInternalisation(cCompatibleSubset);
		//Set<ArrayList<OWLAxiom>> lexicalisationPRank = getLexicalisation(problematicRank);
		
		// Initialise k
		
		int k = problematicRank.size() - 1;
		//ArrayList<OWLAxiom> prr = new ArrayList<OWLAxiom>();
		//if (k > 100){
		//	k = 100;
			
		//	Iterator<OWLAxiom> iter = problematicRank.iterator();
		////	while (prr.size() < k+1){
		//		prr.add(iter.next());
		//	}
		//}
		
		// Compute candidate LAC (all ways of keeping k axioms)
		//System.out.print("All ways of keeping " + k + "...");
		OWLClassExpression candidateLAC = getCandidateLAC(problematicRank, k);
		//OWLClassExpression candidateLAC = getCandidateLAC(prr, k);
		
		//System.out.println("done!");
		OWLClassExpression candidateConcept = dataF.getOWLObjectIntersectionOf(cCompatibleConcept, candidateLAC);
		
		while (antecedentExceptional(subcls, candidateConcept, ranking.getInfiniteRank().getAxiomsAsSet())){
			k--;
			
			if (k == 0)	// Can't keep any axioms from the problematic rank
				return dataF.getOWLThing();
			
			//System.out.print("All ways of keeping " + k + "...");
			candidateLAC = getCandidateLAC(problematicRank, k);
			//candidateLAC = getCandidateLAC(prr, k);
			
			//System.out.println("done!");
			candidateConcept = dataF.getOWLObjectIntersectionOf(cCompatibleConcept, candidateLAC);
		}
		
		keptAxiomsFromPRank = k;
		
		//System.out.println("LAC: " + renderer.render(candidateLAC));
		return candidateLAC;
	}
	
	public ArrayList<Rank> getCCompatibleSubset(ArrayList<Rank> ranksVar, OWLClassExpression clsEx) throws OWLOntologyCreationException{ // NO_UCD (use default)
		ArrayList<Rank> ranks = new ArrayList<Rank>();
		for (Rank r: ranksVar){
			ArrayList<OWLAxiom> newRankAxioms = new ArrayList<OWLAxiom>();
			for (OWLAxiom a: r.getAxioms()){
				newRankAxioms.add(a);
			}
			Rank tmp = new Rank(newRankAxioms);
			ranks.add(tmp);
		}
		 /**C-Compatibility calculation**/
		int rankIndex = 0;
		ArrayList<OWLAxiom> pRank = new ArrayList<OWLAxiom>();
		while (antecedentExceptional(clsEx, ranks, ranking.getInfiniteRank().getAxiomsAsSet()) && rankIndex < ranking.size()){
			Iterator<Rank> iter = ranks.iterator();
			Rank currentRank = null;
			while (currentRank == null && iter.hasNext()){
				currentRank = iter.next();
			}
			
			// Maintain a reference to the last removed rank
			pRank = currentRank.getAxioms();
			
			// Remove the rank
			ranks.remove(currentRank);
			
			rankIndex++;
		}
		
		/************** Log some stuff ***************/
		if (rankIndex == ranking.size()){
			antecedentHasInfiniteRank = true;
		}
		if ((rankIndex == 0) && (ranking.size() > 0)){
			antecedentIsNonExceptional = true;
		}
		
		sizeOfCCompat = ranks.size();
		
		noOfAxiomsInCCompat = 0;
		for (Rank r: ranks){
			noOfAxiomsInCCompat += r.getAxioms().size();
		}
		/********************************************/
		ccompat = new ArrayList<Rank>();
		ccompat.addAll(ranks);
		// Log the problematic rank (the one just before C-Compatibility) 
		problematicRank = new ArrayList<OWLAxiom>(pRank);
		return ranks;
	}
	
	public ArrayList<Rank> getCCompatibleSubset(ArrayList<Rank> ranksVar, Query originalQuery, OWLSubClassOfAxiom modifiedQuery) throws OWLOntologyCreationException{ // NO_UCD (use default)
		ArrayList<Rank> ranks = new ArrayList<Rank>();
		for (Rank r: ranksVar){
			ArrayList<OWLAxiom> newRankAxioms = new ArrayList<OWLAxiom>();
			for (OWLAxiom a: r.getAxioms()){
				newRankAxioms.add(a);
			}
			Rank tmp = new Rank(newRankAxioms);
			ranks.add(tmp);
		}
		 /**C-Compatibility calculation**/
		int rankIndex = 0;
		ArrayList<OWLAxiom> pRank = new ArrayList<OWLAxiom>();
		while (antecedentExceptional(modifiedQuery.getSubClass(), ranks, ranking.getInfiniteRank().getAxiomsAsSet()) && rankIndex < ranking.size()){
			Iterator<Rank> iter = ranks.iterator();
			Rank currentRank = null;
			while (currentRank == null && iter.hasNext()){
				currentRank = iter.next();
			}
			
			// Maintain a reference to the last removed rank
			pRank = currentRank.getAxioms();
			
			// Remove the rank
			ranks.remove(currentRank);
			
			rankIndex++;
		}
		
		/************** Log some stuff ***************/
		if (rankIndex == ranking.size()){
			antecedentHasInfiniteRank = true;
		}
		if ((rankIndex == 0) && (ranking.size() > 0)){
			antecedentIsNonExceptional = true;
		}
		
		sizeOfCCompat = ranks.size();
		
		noOfAxiomsInCCompat = 0;
		for (Rank r: ranks){
			noOfAxiomsInCCompat += r.getAxioms().size();
		}
		/********************************************/
		ccompat = new ArrayList<Rank>();
		ccompat.addAll(ranks);
		// Log the problematic rank (the one just before C-Compatibility) 
		problematicRank = new ArrayList<OWLAxiom>(pRank);
		return ranks;
	}
	
	private boolean antecedentExceptional(OWLClassExpression antec, ArrayList<Rank> ranks, Set<OWLAxiom> background) throws OWLOntologyCreationException{
		/**** Background Knowledge <T> */
		Set<OWLAxiom> backgroundKnwldge = new HashSet<OWLAxiom>();
		backgroundKnwldge.addAll(background);		
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology tmpOntology = null;		
		tmpOntology = ontologyManager.createOntology(backgroundKnwldge);
		
		// Initialise reasoner with <T>
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(tmpOntology);
		
		// Calculate internalisation of ranks + antecedent of query
		OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		OWLClassExpression rhs = df.getOWLNothing();
		OWLClassExpression ranksInternalisation = getInternalisation(ranks);
		OWLClassExpression lhs = df.getOWLObjectIntersectionOf(ranksInternalisation, antec);
		OWLSubClassOfAxiom query = df.getOWLSubClassOfAxiom(lhs, rhs);
		
		// Perform entailment check
		entailmentChecks++;
		return reasoner.isEntailed(query);
	}
	
	
	private boolean antecedentExceptional(OWLClassExpression antec, OWLClassExpression ranksInternalisation, Set<OWLAxiom> background) throws OWLOntologyCreationException{
		/**** Background Knowledge <T> */
		Set<OWLAxiom> backgroundKnwldge = new HashSet<OWLAxiom>();
		backgroundKnwldge.addAll(background);		
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology tmpOntology = null;
		tmpOntology = ontologyManager.createOntology(backgroundKnwldge);
		
		// Initialise reasoner with <T>
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(tmpOntology);
		
		// Calculate query
		OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		OWLSubClassOfAxiom query = df.getOWLSubClassOfAxiom(ranksInternalisation, df.getOWLNothing());

		// Perform entailment check
		entailmentChecks++;
		return reasoner.isEntailed(query);
	}
	
	public boolean isExceptional(OWLSubClassOfAxiom q, Ranking ranking) throws OWLOntologyCreationException{
		/**** Background Knowledge <T> */
		Set<OWLAxiom> backgroundKnwldge = new HashSet<OWLAxiom>();
		backgroundKnwldge.addAll(ranking.getInfiniteRank().getAxioms());		
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology tmpOntology = null;		
		tmpOntology = ontologyManager.createOntology(backgroundKnwldge);
		
		// Initialise reasoner with <T>
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(tmpOntology);
		
		// Calculate internalisation of ranks + antecedent of query
		OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		OWLClassExpression rhs = df.getOWLNothing();
		OWLClassExpression ranksInternalisation = getInternalisation(ranking.getRanking());
		OWLClassExpression lhs = df.getOWLObjectIntersectionOf(ranksInternalisation, q.getSubClass());
		OWLSubClassOfAxiom query = df.getOWLSubClassOfAxiom(lhs, rhs);
		
		// Perform entailment check
		entailmentChecks++;
		return reasoner.isEntailed(query);
	}
	
	public boolean executeNonExceptionalQuery(OWLSubClassOfAxiom q, Ranking ranking) throws OWLOntologyCreationException{
		/**** Background Knowledge <T> */
		Set<OWLAxiom> backgroundKnwldge = new HashSet<OWLAxiom>();
		backgroundKnwldge.addAll(ranking.getInfiniteRank().getAxioms());		
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology tmpOntology = null;		
		tmpOntology = ontologyManager.createOntology(backgroundKnwldge);
		
		// Initialise reasoner with <T>
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(tmpOntology);
		
		// Calculate internalisation of ranks + antecedent of query
		OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		OWLClassExpression rhs = q.getSuperClass();
		OWLClassExpression ranksInternalisation = getInternalisation(ranking.getRanking());
		OWLClassExpression lhs = df.getOWLObjectIntersectionOf(ranksInternalisation, q.getSubClass());
		OWLSubClassOfAxiom query = df.getOWLSubClassOfAxiom(lhs, rhs);
		
		// Perform entailment check
		entailmentChecks++;
		return reasoner.isEntailed(query);
	}
	
	public boolean isCBasisValid(){
		return cBasisValid;
	}
	
	
	
	public ArrayList<OWLAxiom> getProblematicRank(){
		return problematicRank;
	}
	
	public void resetEntChecks(){
		entailmentChecks = 0;
	}
}
