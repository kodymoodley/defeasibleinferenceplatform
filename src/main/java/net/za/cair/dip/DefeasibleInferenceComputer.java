package net.za.cair.dip;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.paukov.combinatorics3.Generator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import net.za.cair.dip.model.Query;
import net.za.cair.dip.model.Rank;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.model.ReasoningType;
import net.za.cair.dip.util.ManchesterOWLSyntaxOWLObjectRendererImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

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
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 */

/**
 * Author: Kody Moodley<br>
 * Centre for Artificial Intelligence Research<br>
 * UKZN and CSIR<br>
 * Date: 10-Oct-2011<br><br>
 */

public class DefeasibleInferenceComputer {
	private OWLReasonerFactory reasonerFactory;
	private Ranking ranking;
	public int prunedRankingSize;
	public double avgSizeOfRankInPrunedRanking;
	public Ranking prunedRanking;
	public ArrayList<Rank> ccompat;
	public ArrayList<OWLAxiom> pRank;
	public int noOfRanksInCCompatLR;
	public int noOfRanksInCCompatMR;
	public int noOfRanksInCCompatBR;
	private Set<OWLClass> strictSuperClasses;
	private Set<OWLClass> typicalSuperClasses;
	public int axiomsInCCompatLR;
	public int axiomsInCCompatMR;
	public int axiomsInCCompatBR;
	public int entailmentChecksLR;
	public int entailmentChecksMR;
	public int entailmentChecksBR;
	public int entailmentChecksLC;
	public int entailmentChecksRC;
	public int entailmentChecksRelC;
	public boolean queryHasInfiniteRank;
	public boolean queryIsNonExceptional;
	public int noOfAxiomsKeptFromProblematicRank;
	public int cbasisSize;
	public int mincbasisSize;
	public int noOfJusts;
	public double avgSizeOfAJust;
	public boolean mincbasisTimeout;
	public int numStrictAxioms;
	public int numDefAxioms;
	public boolean lexrelanswer, basicrelanswer, minrelanswer;
	public long lexreltime = 0;
	public long basicreltime = 0;
	public long minreltime = 0;
	public int hsTreeSize = 0;
	public boolean rcResult;
	public boolean lcResult;

	private OWLDataFactory df;
	private DefeasibleInferenceHelperClass helperClass;
	private Set<Set<OWLAxiom>> multipleExtensions;
	private Set<Set<OWLNamedIndividual>> multipleExtensionInstances;
	private Set<OWLAxiom> singleExtension;
	private ArrayList<OWLClassExpression> lexicographicRanking;
	private Set<OWLAxiom> global_inconsBasis;
	private Set<OWLAxiom> global_minInconsBasis;
	private ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	
	public DefeasibleInferenceComputer(OWLReasonerFactory reasonerFactory){
		this.df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		this.reasonerFactory = reasonerFactory;		
		helperClass = new DefeasibleInferenceHelperClass(reasonerFactory);
		this.ranking = null;
	}
	
	public DefeasibleInferenceComputer(OWLReasonerFactory reasonerFactory, Ranking ranking){
		this.df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		this.ranking = new Ranking(ranking.getRanking());
		this.ranking.setInfiniteRank(ranking.getInfiniteRank());
		this.reasonerFactory = reasonerFactory;		
		helperClass = new DefeasibleInferenceHelperClass(reasonerFactory, this.ranking);
		this.prunedRanking = null;
		pRank = new ArrayList<OWLAxiom>();
	}

	public DefeasibleInferenceComputer(OWLReasonerFactory reasonerFactory, OWLOntology ontology){
		this.df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		this.reasonerFactory = reasonerFactory;
		helperClass = new DefeasibleInferenceHelperClass(reasonerFactory, this.ranking);
		this.ranking = null;
	}
	
	private Ranking setRanking(Ranking r){
		ArrayList<Rank> ranks = new ArrayList<Rank>();
		ArrayList<Rank> result = new ArrayList<Rank>();
		ranks.addAll(r.getRanking());
		
		for (Rank rank: ranks){
			ArrayList<OWLAxiom> currentRankAxioms = new ArrayList<OWLAxiom>();
			currentRankAxioms.addAll(rank.getAxioms());
			Rank tmp = new Rank(currentRankAxioms, rank.getIndex());
			result.add(tmp);
		}
		
		ArrayList<OWLAxiom> inf = new ArrayList<OWLAxiom>();
		inf.addAll(r.getInfiniteRank().getAxioms());
		Ranking finalResult = new Ranking(result);
		finalResult.setInfiniteRank(new Rank(inf));
		
		return finalResult;
	}
	
	public Ranking pruneIrrelevantAxioms(Query query) throws OWLOntologyCreationException{
		Ranking result = null;
		ArrayList<Rank> newRanks = new ArrayList<Rank>();
		OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)query.originalAxiom;
		Set<OWLEntity> signature = sub.getSignature();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology tmpOntology = null;
		Set<OWLAxiom> ontologyAxioms = new HashSet<OWLAxiom>();
		// Strict axioms
		for (OWLAxiom a: ranking.getInfiniteRank().getAxioms()){
			ontologyAxioms.add(a);
		}
		// Defeasible axioms
		for (Rank r: ranking.getRanking()){
			Set<OWLAxiom> tmpSet = new HashSet<OWLAxiom>();
			for (OWLAxiom a: r.getAxioms()){
				tmpSet.add(a);
			}
			ontologyAxioms.addAll(tmpSet);
		}
		
		tmpOntology = man.createOntology(ontologyAxioms);
		SyntacticLocalityModuleExtractor slme = new SyntacticLocalityModuleExtractor(man, tmpOntology, ModuleType.STAR);
		Set<OWLAxiom> starmodule = slme.extract(signature);
		
		// Now remove axioms not appearing in the module from each rank in the ranking
		// Starting with non-infinite rank axioms
		for (Rank r: ranking.getRanking()){
			ArrayList<OWLAxiom> tmpSet = new ArrayList<OWLAxiom>();
			for (OWLAxiom a: r.getAxioms()){
				if (starmodule.contains(a)){
					tmpSet.add(a);
				}
			}
			if (!tmpSet.isEmpty())
				newRanks.add(new Rank(tmpSet));
		}
		
		// Set infinite rank as well
		ArrayList<OWLAxiom> newInfRankAxioms = new ArrayList<OWLAxiom>();
		for (OWLAxiom a: ranking.getInfiniteRank().getAxioms()){
			if (starmodule.contains(a)){
				newInfRankAxioms.add(a);
			}
		}
		
		// Construct new ranking and return the result
		result = new Ranking(newRanks);
		result.setInfiniteRank(new Rank(newInfRankAxioms));
		return result;
	}

	private void computeLexicographicRanking(Ranking tmpRanking) {
		// Initialize variable
		lexicographicRanking = new ArrayList<OWLClassExpression>(); 
		// Get hold of an OWLDataFactory to construct class expressions from OWL boolean operators
		OWLDataFactory dataF = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		// Set rank index to 1 (first level)
		int rankIndex = 1;
		// Done: we have run through all ranks
		boolean done = false;
		// While not done
		while (!done) {
			// If we have run through all ranks, set done to true
			if (rankIndex > tmpRanking.size()) {
				done = true;
			}
			else {
				// Get current rank
				Rank currRank = null; 
				for (Rank r: tmpRanking.getRanking()) {
					if (r.getIndex() == rankIndex) {
						currRank = r;
					}
				}
				
				// Get conjunction of materialisations for all axioms up to and including this rank
				OWLClassExpression fullCls = helperClass.getE_i(tmpRanking, rankIndex);
				// and add it to our lexicographic ranking set of class expressions
				lexicographicRanking.add(fullCls);
		
				OWLClassExpression fullClsPrev = null;
				// If there is another rank after this
				if (rankIndex+1 <= tmpRanking.size()) {
					// Get the conjunction of materialisations for up to and EXCLUDING this rank
					fullClsPrev = helperClass.getE_i(tmpRanking, rankIndex+1);
					
					// Loop through number of axioms in this rank (from highest to lowest number)
					for (int k = currRank.getAxioms().size()-1; k > 0; k--) {
						// Get all combinations of k axioms
						Iterator<List<OWLAxiom>> streamAx = Generator.combination(currRank.getAxioms()).simple(k).iterator();

						// Now construct disjunctions of conjunctions of these axioms 
						Set<OWLClassExpression> disjuncts = new HashSet<OWLClassExpression>();
						while (streamAx.hasNext()) {
							List<OWLAxiom> currCombination = streamAx.next();
							Set<OWLAxiom> setCurrCombination = new HashSet<OWLAxiom>();
							setCurrCombination.addAll(currCombination);
							OWLClassExpression tmp = helperClass.getInternalisation(setCurrCombination);
							disjuncts.add(tmp);
						}

						// Construct class expression from disjunctions of conjunctions of axioms
						OWLClassExpression currComb = dataF.getOWLObjectUnionOf(disjuncts);
						OWLClassExpression currCombFinal = dataF.getOWLObjectIntersectionOf(currComb, fullClsPrev);
						// add it to the lexicographic ranking set of class expressions
						lexicographicRanking.add(currCombFinal);
					}
				}
			
				// Now go to the next level of the ranking
				rankIndex++;
			}
		}
		
		System.out.println();
		System.out.println("Lexicographic Ranking:");
		System.out.println("----------------------");
		
		for (int l = 0; l < lexicographicRanking.size();l++) {
			System.out.println(man.render(lexicographicRanking.get(l)));
			System.out.println();
		}
		System.out.println();
	}
	
	public boolean hasSingleABoxExtension(Ranking rankingTmp, ReasoningType reasoning_algorithm) throws OWLOntologyCreationException {
		global_inconsBasis = new HashSet<OWLAxiom>();
		global_minInconsBasis = new HashSet<OWLAxiom>();
		
		if (reasoning_algorithm.equals(ReasoningType.RATIONAL)) {
			singleExtension = new HashSet<OWLAxiom>();
			/** Procedure: RationalExtension(K) */
			// Get all individuals and ABox axioms
			ArrayList<OWLIndividual> individuals = new ArrayList<OWLIndividual>();
			Set<OWLAxiom> abox = new HashSet<OWLAxiom>();
			for (OWLAxiom a: rankingTmp.getInfiniteRank().getAxioms()) {
				if (a.isOfType(AxiomType.ABoxAxiomTypes)) {
					individuals.addAll(a.getIndividualsInSignature());
					abox.add(a);
				}
			}

			int m = individuals.size();													// Number of individuals
			int n = rankingTmp.size();													// Highest eTransform index
			int j = 0;																	// Line 1
			Set<OWLAxiom> abox_D = new HashSet<OWLAxiom>();	abox_D.addAll(abox);		// Line 2
			do {																		// Line 3
				int i = 1;																// Line 4
				OWLAxiom currAssertion = df.getOWLClassAssertionAxiom(helperClass.getE_i(rankingTmp, i), individuals.get(j));
				while ((!isConsistent(abox, currAssertion, rankingTmp)) && (i <= n)) {	// Line 5
					i++;																// Line 6
					currAssertion = df.getOWLClassAssertionAxiom(helperClass.getE_i(rankingTmp, i), individuals.get(j));
				}
				if (i <= n)																// Line 7
					abox_D.add(currAssertion);											// Line 8
				j++;																	// Line 9
			} while (j < m);															// Line 10

			// If resulting abox_D is consistent then this is the only abox extension
			if (isConsistent(abox_D, rankingTmp)) {
				// assign abox_D to global variable (ABox extension)
				singleExtension.addAll(abox_D);
				return true;
			}
			else {
				return false;
			}
		}
		else if (reasoning_algorithm.equals(ReasoningType.LEXICOGRAPHIC)) {
			singleExtension = new HashSet<OWLAxiom>();
			/** Procedure: RationalExtension(K) */
			// Get all individuals and ABox axioms
			ArrayList<OWLIndividual> individuals = new ArrayList<OWLIndividual>();
			Set<OWLAxiom> abox = new HashSet<OWLAxiom>();
			for (OWLAxiom a: rankingTmp.getInfiniteRank().getAxioms()) {
				if (a.isOfType(AxiomType.ABoxAxiomTypes)) {
					individuals.addAll(a.getIndividualsInSignature());
					abox.add(a);
				}
			}

			// computeLexicographicRanks (combinations of axioms for each rank)
			computeLexicographicRanking(rankingTmp);

			int m = individuals.size();													// Number of individuals
			int n = lexicographicRanking.size()-1;										// Highest eTransform index
			int j = 0;																	// Line 1
			Set<OWLAxiom> abox_D = new HashSet<OWLAxiom>();	abox_D.addAll(abox);		// Line 2
			do {																		// Line 3
				int i = 0;																// Line 4
				OWLAxiom currAssertion = df.getOWLClassAssertionAxiom(lexicographicRanking.get(i), individuals.get(j));
				//System.out.println("a: " + currAssertion);
				while ((!isConsistent(abox, currAssertion, rankingTmp.getInfiniteRank())) && (i <= n)) {	// Line 5
					i++;																// Line 6
					currAssertion = df.getOWLClassAssertionAxiom(lexicographicRanking.get(i), individuals.get(j));
					//System.out.println("b: "  + currAssertion);
				}
				if (i <= n)																// Line 7
					abox_D.add(currAssertion);											// Line 8
				j++;																	// Line 9
			} while (j < m);															// Line 10

			// If resulting abox_D is consistent then this is the only abox extension
			if (isConsistent(abox_D, rankingTmp.getInfiniteRank())) {
				// assign abox_D to global variable (ABox extension)
				singleExtension.addAll(abox_D);

				System.out.println();
				System.out.println("Single Extension:");
				System.out.println("-----------------");
				for (OWLAxiom a: singleExtension) {
					System.out.println(man.render(a));
				}
				System.out.println();
				return true;
			}
			else {
				return false;
			}
		}
		else if (reasoning_algorithm.equals(ReasoningType.RELEVANT)) {
			// Basic Relevance Closure
			singleExtension = new HashSet<OWLAxiom>();
			/** Procedure: RationalExtension(K) */
			// Get all individuals and ABox axioms
			ArrayList<OWLIndividual> individuals = new ArrayList<OWLIndividual>();
			Set<OWLAxiom> abox = new HashSet<OWLAxiom>();
			for (OWLAxiom a: rankingTmp.getInfiniteRank().getAxioms()) {
				if (a.isOfType(AxiomType.ABoxAxiomTypes)) {
					individuals.addAll(a.getIndividualsInSignature());
					abox.add(a);
				}
			}
			
			// compute inconsistency basis
			global_inconsBasis = helperClass.getInconsistencyBasis();
			
			System.out.println();
			System.out.println("Inconsistency Basis:");
			System.out.println("--------------------");
			for (OWLAxiom a: global_inconsBasis) {
				System.out.println(man.render(a));
			}
			System.out.println();

			// if ontology is not inconsistent then there are no instances of exceptional or unsatisfiable classes
			// then we can just ask for instances of this class expression directly from classical reasoner

			if (global_inconsBasis.isEmpty()) {
				int m = individuals.size();													// Number of individuals
				int n = rankingTmp.size();													// Highest eTransform index
				int j = 0;																	// Line 1
				Set<OWLAxiom> abox_D = new HashSet<OWLAxiom>();	abox_D.addAll(abox);		// Line 2
				do {																		// Line 3															
					OWLAxiom currAssertion = df.getOWLClassAssertionAxiom(helperClass.getE_i(rankingTmp, 1), individuals.get(j));
					if (isConsistent(abox, currAssertion, rankingTmp))						// Line 5																														
						abox_D.add(currAssertion);											// Line 8
					j++;																	// Line 9
				} while (j < m);															// Line 10

				// If resulting abox_D is consistent then this is the only abox extension
				if (isConsistent(abox_D, rankingTmp)) {
					// assign abox_D to global variable (ABox extension)
					singleExtension.addAll(abox_D);
					return true;
				}
				else {
					return false;
				}
			}
			else {
				
				// ontology is inconsistent so it has a non-empty inconsistency-basis
				int m = individuals.size();													// Number of individuals
				int n = rankingTmp.size();													// Highest eTransform index
				int j = 0;																	// Line 1
				Set<OWLAxiom> abox_D = new HashSet<OWLAxiom>();	abox_D.addAll(abox);		// Line 2
				do {																		// Line 3
					int i = 1;																// Line 4
					OWLAxiom currAssertion = df.getOWLClassAssertionAxiom(helperClass.getE_i(rankingTmp, i, global_inconsBasis), individuals.get(j));
					while ((!isConsistent(abox, currAssertion, rankingTmp)) && (i <= n)) {		// Line 5
						i++;																// Line 6
						currAssertion = df.getOWLClassAssertionAxiom(helperClass.getE_i(rankingTmp, i, global_inconsBasis), individuals.get(j));
					}
					if (i <= n)																// Line 7
						abox_D.add(currAssertion);											// Line 8
					j++;																	// Line 9
				} while (j < m);															// Line 10

				// If resulting abox_D is consistent then this is the only abox extension
				if (isConsistent(abox_D, rankingTmp)) {
					// assign abox_D to global variable (ABox extension)
					singleExtension.addAll(abox_D);
					
					System.out.println();
					System.out.println("Single Extension:");
					System.out.println("-----------------");
					for (OWLAxiom a: singleExtension) {
						System.out.println(man.render(a));
					}
					System.out.println();
					
					return true;
				}
				else {
					return false;
				}
			}
		}
		else {
			// Minimal Relevance Closure
			singleExtension = new HashSet<OWLAxiom>();
			/** Procedure: RationalExtension(K) */
			// Get all individuals and ABox axioms
			ArrayList<OWLIndividual> individuals = new ArrayList<OWLIndividual>();
			Set<OWLAxiom> abox = new HashSet<OWLAxiom>();
			for (OWLAxiom a: rankingTmp.getInfiniteRank().getAxioms()) {
				if (a.isOfType(AxiomType.ABoxAxiomTypes)) {
					individuals.addAll(a.getIndividualsInSignature());
					abox.add(a);
				}
			}
			
			// compute MINIMAL inconsistency basis
			global_minInconsBasis = helperClass.getMinInconsistencyBasis();

			// if ontology is not inconsistent then there are no instances of exceptional or unsatisfiable classes
			// then we can just ask for instances of this class expression directly from classical reasoner

			if (global_minInconsBasis.isEmpty()) {
				int m = individuals.size();													// Number of individuals
				int n = rankingTmp.size();													// Highest eTransform index
				int j = 0;																	// Line 1
				Set<OWLAxiom> abox_D = new HashSet<OWLAxiom>();	abox_D.addAll(abox);		// Line 2
				do {																		// Line 3															
					OWLAxiom currAssertion = df.getOWLClassAssertionAxiom(helperClass.getE_i(rankingTmp, 1), individuals.get(j));
					if (isConsistent(abox, currAssertion, rankingTmp))						// Line 5																														
						abox_D.add(currAssertion);											// Line 8
					j++;																	// Line 9
				} while (j < m);															// Line 10

				// If resulting abox_D is consistent then this is the only abox extension
				if (isConsistent(abox_D, rankingTmp)) {
					// assign abox_D to global variable (ABox extension)
					singleExtension.addAll(abox_D);
					return true;
				}
				else {
					return false;
				}
			}
			else {
				// ontology is inconsistent so it has a non-empty inconsistency-basis
				int m = individuals.size();													// Number of individuals
				int n = rankingTmp.size();													// Highest eTransform index
				int j = 0;																	// Line 1
				Set<OWLAxiom> abox_D = new HashSet<OWLAxiom>();	abox_D.addAll(abox);		// Line 2
				do {																		// Line 3
					int i = 1;																// Line 4
					OWLAxiom currAssertion = df.getOWLClassAssertionAxiom(helperClass.getE_i(rankingTmp, i, global_minInconsBasis), individuals.get(j));
					while ((!isConsistent(abox, currAssertion, rankingTmp)) && (i <= n)) {	// Line 5
						i++;																// Line 6
						currAssertion = df.getOWLClassAssertionAxiom(helperClass.getE_i(rankingTmp, i, global_minInconsBasis), individuals.get(j));
					}
					if (i <= n)																// Line 7
						abox_D.add(currAssertion);											// Line 8
					j++;																	// Line 9
				} while (j < m);															// Line 10

				// If resulting abox_D is consistent then this is the only abox extension
				if (isConsistent(abox_D, rankingTmp)) {
					// assign abox_D to global variable (ABox extension)
					singleExtension.addAll(abox_D);
					return true;
				}
				else {
					return false;
				}
			}
		}
	}
	
	private Set<OWLAxiom> getABoxExtension(Ranking rkg, List<OWLIndividual> sequence, ReasoningType algorithm) throws OWLOntologyCreationException{
		/** Procedure: RationalExtension(K) */
		// Get ABox axioms
		Set<OWLAxiom> abox = new HashSet<OWLAxiom>();
		for (OWLAxiom a: rkg.getInfiniteRank().getAxioms()) {
			if (a.isOfType(AxiomType.ABoxAxiomTypes))
				abox.add(a);
		}

		if (algorithm.equals(ReasoningType.RATIONAL)) {
			int m = sequence.size();													// Number of individuals
			int n = rkg.size();															// Highest eTransform index
			int j = 0;																	// Line 1
			Set<OWLAxiom> abox_D = new HashSet<OWLAxiom>();	abox_D.addAll(abox);		// Line 2
			do {																		// Line 3
				int i = 1;																// Line 4
				OWLAxiom currAssertion = df.getOWLClassAssertionAxiom(helperClass.getE_i(rkg, i), sequence.get(j));
				while ((!isConsistent(abox_D, currAssertion, rkg)) && (i <= n)) {		// Line 5
					i++;																// Line 6
					currAssertion = df.getOWLClassAssertionAxiom(helperClass.getE_i(rkg, i), sequence.get(j));
				}
				if (i <= n)																// Line 7
					abox_D.add(currAssertion);											// Line 8
				j++;																	// Line 9
			} while (j < m);															// Line 10
			return abox_D;
		}
		else if (algorithm.equals(ReasoningType.LEXICOGRAPHIC)) {
			int m = sequence.size();													// Number of individuals
			int n = lexicographicRanking.size() - 1;									// Highest eTransform index
			int j = 0;																	// Line 1
			Set<OWLAxiom> abox_D = new HashSet<OWLAxiom>();	abox_D.addAll(abox);		// Line 2
			do {																		// Line 3
				int i = 0;																// Line 4
				OWLAxiom currAssertion = df.getOWLClassAssertionAxiom(lexicographicRanking.get(i), sequence.get(j));
				while ((!isConsistent(abox_D, currAssertion, rkg.getInfiniteRank())) && (i <= n)) {		// Line 5
					if (i < lexicographicRanking.size())
						currAssertion = df.getOWLClassAssertionAxiom(lexicographicRanking.get(i), sequence.get(j));
				}
				if (i <= n)																// Line 7
					abox_D.add(currAssertion);											// Line 8
				j++;																	// Line 9
			} while (j < m);															// Line 10
			return abox_D;
		}
		else if (algorithm.equals(ReasoningType.RELEVANT)) {
			if (global_inconsBasis.isEmpty()) {
				int m = sequence.size();													// Number of individuals
				int j = 0;																	// Line 1
				Set<OWLAxiom> abox_D = new HashSet<OWLAxiom>();	abox_D.addAll(abox);		// Line 2
				do {																		// Line 3															
					OWLAxiom currAssertion = df.getOWLClassAssertionAxiom(helperClass.getE_i(rkg, 1), sequence.get(j));
					if (isConsistent(abox_D, currAssertion, rkg))						// Line 5																														
						abox_D.add(currAssertion);											// Line 8
					j++;																	// Line 9
				} while (j < m);															// Line 10
				return abox_D;
			}
			else {
				int m = sequence.size();													// Number of individuals
				int n = rkg.size();															// Highest eTransform index
				int j = 0;																	// Line 1
				Set<OWLAxiom> abox_D = new HashSet<OWLAxiom>();	abox_D.addAll(abox);		// Line 2
				do {																		// Line 3
					int i = 1;																// Line 4
					OWLAxiom currAssertion = df.getOWLClassAssertionAxiom(helperClass.getE_i(rkg, i, global_inconsBasis), sequence.get(j));
					while ((!isConsistent(abox_D, currAssertion, rkg)) && (i <= n)) {		// Line 5
						i++;																// Line 6
						currAssertion = df.getOWLClassAssertionAxiom(helperClass.getE_i(rkg, i, global_inconsBasis), sequence.get(j));
					}
					if (i <= n)																// Line 7
						abox_D.add(currAssertion);											// Line 8
					j++;																	// Line 9
				} while (j < m);															// Line 10
				return abox_D;
			}
		}
		else {
			// Minimal Relevance Closure
			if (global_minInconsBasis.isEmpty()) {
				int m = sequence.size();													// Number of individuals
				int j = 0;																	// Line 1
				Set<OWLAxiom> abox_D = new HashSet<OWLAxiom>();	abox_D.addAll(abox);		// Line 2
				do {																		// Line 3															
					OWLAxiom currAssertion = df.getOWLClassAssertionAxiom(helperClass.getE_i(rkg, 1), sequence.get(j));
					if (isConsistent(abox_D, currAssertion, rkg))						// Line 5																														
						abox_D.add(currAssertion);											// Line 8
					j++;																	// Line 9
				} while (j < m);															// Line 10
				return abox_D;
			}
			else {
				int m = sequence.size();													// Number of individuals
				int n = rkg.size();															// Highest eTransform index
				int j = 0;																	// Line 1
				Set<OWLAxiom> abox_D = new HashSet<OWLAxiom>();	abox_D.addAll(abox);		// Line 2
				do {																		// Line 3
					int i = 1;																// Line 4
					OWLAxiom currAssertion = df.getOWLClassAssertionAxiom(helperClass.getE_i(rkg, i, global_minInconsBasis), sequence.get(j));
					while ((!isConsistent(abox_D, currAssertion, rkg)) && (i <= n)) {		// Line 5
						i++;																// Line 6
						currAssertion = df.getOWLClassAssertionAxiom(helperClass.getE_i(rkg, i, global_minInconsBasis), sequence.get(j));
					}
					if (i <= n)																// Line 7
						abox_D.add(currAssertion);											// Line 8
					j++;																	// Line 9
				} while (j < m);															// Line 10
				return abox_D;
			}
		}
	}
	
	public void computeMultipleExtensions(Ranking rkg, ReasoningType algorithm) throws OWLOntologyCreationException {
		multipleExtensions = new HashSet<Set<OWLAxiom>>();
		multipleExtensions.add(singleExtension);
		
		// Compute all possible sequences of individuals
		// 1. Get all individuals in ABox
		Set<OWLIndividual> individuals = new HashSet<OWLIndividual>();
		for (OWLAxiom a: rkg.getInfiniteRank().getAxioms()) {
			if (a.isOfType(AxiomType.ABoxAxiomTypes)) 
				individuals.addAll(a.getIndividualsInSignature());
		}
		
		// 2. Compute all sequences of individuals
		ArrayList<List<OWLIndividual>> sequences = new ArrayList<List<OWLIndividual>>();
		Iterator<List<OWLIndividual>> streamInd = Generator.permutation(individuals).simple().iterator();
		while (streamInd.hasNext()) {
			List<OWLIndividual> currSequence = streamInd.next();
			sequences.add(currSequence);
		}
		
		// 3. Compute ABox extension for each sequence
		for (List<OWLIndividual> seq: sequences) {
			Set<OWLAxiom> extension = getABoxExtension(rkg, seq, algorithm);
			multipleExtensions.add(extension);
		}
	}
	
	public void computeMultipleExtensionInstances(Ranking rkg, OWLClassExpression cls) throws OWLOntologyCreationException{
		multipleExtensionInstances = new HashSet<Set<OWLNamedIndividual>>();
		
		for (Set<OWLAxiom> extension: multipleExtensions) {
			OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
			Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
			axioms.addAll(extension);															// A_s
			axioms.addAll(rkg.getInfiniteRank().getAxiomsAsSet());								// T
			OWLOntology tmpOntology = ontologyManager.createOntology(axioms);
			OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(tmpOntology);
			multipleExtensionInstances.add(reasoner.getInstances(cls, false).getFlattened());
		}
		
	}
	
	private Set<OWLNamedIndividual> getIntersection(Set<Set<OWLNamedIndividual>> sets) {

	    Set<OWLNamedIndividual> firstSet = new HashSet<OWLNamedIndividual>();
	    Set<Set<OWLNamedIndividual>> otherSets = new HashSet<Set<OWLNamedIndividual>>();
	    
	    int count = 0;
	    for (Set<OWLNamedIndividual> s: sets) {
	    	if (count == 0) {
	    		firstSet.addAll(s);
	    	}
	    	else {
	    		otherSets.add(s);
	    	}
	    	count++;
	    }
	    
	    if (sets == null || sets.size() == 0)
	        return new HashSet<OWLNamedIndividual>();

	    Set<OWLNamedIndividual> intersection = new HashSet<OWLNamedIndividual>(firstSet);

	    for (Set<OWLNamedIndividual> c : otherSets) {
	        intersection.retainAll(c);
	    }
	    
	    return intersection;
	}
	
	public Set<OWLNamedIndividual> getPlausibleInstances(){
		Set<OWLNamedIndividual> result = new HashSet<OWLNamedIndividual>();
		for (Set<OWLNamedIndividual> setInd: multipleExtensionInstances)
			result.addAll(setInd);
		return result;
	}
	
	public Set<OWLNamedIndividual> getDefiniteInstances(){
		Set<Set<OWLNamedIndividual>> tmp = new HashSet<Set<OWLNamedIndividual>>();
		for (Set<OWLNamedIndividual> setInd: multipleExtensionInstances)
			tmp.add(setInd);
		
		return getIntersection(tmp);
	}
	
	public Set<Set<OWLAxiom>> getMultipleExtensions(){
		return multipleExtensions;
	}
	
	public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression cls) throws OWLOntologyCreationException{
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		axioms.addAll(singleExtension);								// A_D
		axioms.addAll(ranking.getInfiniteRank().getAxiomsAsSet());	// T

//		System.out.println();
//		System.out.println("ABOX EXTENSION + STRICT AXIOMS:");
//		System.out.println("-------------------------------");
//		for (OWLAxiom axom: axioms) {
//			System.out.println(man.render(axom));
//		}
//		System.out.println();

		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology tmpOntology = ontologyManager.createOntology(axioms);	
		//Reasoner reasoner = new Reasoner(reasonerFactory, tmpOntology);
//		System.out.println();
//		System.out.println("ABOX EXTENSION + STRICT AXIOMS: (AFTER!)");
//		System.out.println("----------------------------------------");
//		for (OWLAxiom a: tmpOntology.getAxioms()) {
//			System.out.println(man.render(a));
//		}
//		System.out.println();

		//Reasoner reasoner = new Reasoner(new Configuration(), tmpOntology);
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(tmpOntology);
		//reasoner.flush();

		/*Set<OWLNamedIndividual> individuals = new HashSet<OWLNamedIndividual>();

			for (OWLAxiom a: axioms) {
				if (a.isOfType(AxiomType.ABoxAxiomTypes)) {
					individuals.addAll(a.getIndividualsInSignature());
				}
			}

			for (OWLNamedIndividual ind: individuals) {
				if (man.render(ind).equals("tweety3")) {
					OWLAxiom testAx = df.getOWLClassAssertionAxiom(df.getOWLObjectComplementOf(cls), ind);
					//OWLAxiom testAx = df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLNothing());
					ontologyManager.addAxiom(tmpOntology, testAx);
					//System.out.print(man.render(testAx) + " : ");
					//boolean r = reasoner.isEntailed(testAx);
					//if (r)
					//	System.out.println("true");
					//else
					//	System.out.println("false");
				}
			}

			//System.out.println();
			//System.out.println("Equivalent classes: " + reasoner.getEquivalentClasses(cls));
			//System.out.println();

			if (reasoner.isConsistent())
				System.out.println("WTF");
			else
				System.out.println("WTF2");


			reasoner.flush();*/
		System.out.println();			
		System.out.println("Class:");
		System.out.println("------");
		System.out.println(man.render(cls));
		System.out.println();			
		return reasoner.getInstances(cls, false);



	}

	public boolean isConsistent(Ranking ranking) throws OWLOntologyCreationException {
		/**** Defeasible Ontology is Preferentially Inconsistent iff Infinite Rank is classically Inconsistent */		
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology tmpOntology = ontologyManager.createOntology(ranking.getInfiniteRank().getAxiomsAsSet());	
		//Reasoner reasoner = new Reasoner(new Configuration(), tmpOntology);
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(tmpOntology);
		return reasoner.isConsistent();
	}
	
	public boolean isConsistent(Set<OWLAxiom> abox, OWLAxiom assertion, Ranking ranking) throws OWLOntologyCreationException {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		axioms.addAll(abox);										// A_D
		axioms.add(assertion);										// E_i(a_j)
		axioms.addAll(ranking.getInfiniteRank().getAxiomsAsSet());	// T
		
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology tmpOntology = ontologyManager.createOntology(axioms);
		//Reasoner reasoner = new Reasoner(new Configuration(), tmpOntology);
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(tmpOntology);
		return reasoner.isConsistent();
	}
	
	// Lexicographic Closure
	public boolean isConsistent(Set<OWLAxiom> abox, OWLAxiom assertion, Rank infRank) throws OWLOntologyCreationException {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		axioms.addAll(abox);										// A_D
		axioms.add(assertion);										// E_i(a_j)
		axioms.addAll(infRank.getAxiomsAsSet());	// T
		
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology tmpOntology = ontologyManager.createOntology(axioms);
		//Reasoner reasoner = new Reasoner(new Configuration(), tmpOntology);
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(tmpOntology);
		return reasoner.isConsistent();
	}
	
	// Lexicographic Closure
	public boolean isConsistent(Set<OWLAxiom> abox, Rank infRank) throws OWLOntologyCreationException {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		axioms.addAll(abox);										// A_D
		axioms.addAll(infRank.getAxiomsAsSet());	// T
		
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology tmpOntology = ontologyManager.createOntology(axioms);	
		//Reasoner reasoner = new Reasoner(new Configuration(), tmpOntology);
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(tmpOntology);
		return reasoner.isConsistent();
	}
	
	public boolean isConsistent(Set<OWLAxiom> abox, Ranking ranking) throws OWLOntologyCreationException {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		axioms.addAll(abox);										// A_D
		axioms.addAll(ranking.getInfiniteRank().getAxiomsAsSet());	// T
		
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology tmpOntology = ontologyManager.createOntology(axioms);	
		//Reasoner reasoner = new Reasoner(new Configuration(), tmpOntology);
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(tmpOntology);
		return reasoner.isConsistent();
	}
	
	public boolean isEntailed(Query query) throws OWLOntologyCreationException{
		lexrelanswer = false;
		basicrelanswer = false;
		minrelanswer = false;
		rcResult = false;
		lcResult = false;
		
		entailmentChecksRC = 0;
		entailmentChecksLC = 0;
		
		noOfAxiomsKeptFromProblematicRank = 0;
		entailmentChecksLR = 0;
		entailmentChecksBR = 0;
		entailmentChecksMR = 0;
		
		this.prunedRanking = pruneIrrelevantAxioms(query);
		
		/*for (Rank r: prunedRanking.getRanking()){
			if (r.getAxioms().size() == 0){
				System.out.println("something fishy going on!!!");
				System.exit(-1);
			}
		}*/
		
		prunedRankingSize = prunedRanking.size();
		
		numStrictAxioms = 0;
		numDefAxioms = 0;
		Ranking newRankingTest = setRanking(prunedRanking);
		numStrictAxioms = newRankingTest.getInfiniteRank().size();
		for (Rank r: newRankingTest.getRanking()){
			numDefAxioms += r.getAxiomsAsSet().size();
		}
		
		Ranking newRanking = setRanking(prunedRanking);
		
		helperClass = new DefeasibleInferenceHelperClass(reasonerFactory, newRanking);
		
		if (query.isTBoxQuery){
			
			/********************** TBOX QUERY **********************/
			
			/****************** NON-PREFERENTIAL QUERY **************/
			//if (!query.algorithm.equals(ReasoningType.PREFERENTIAL)){
				
				OWLSubClassOfAxiom modifiedQuery = (OWLSubClassOfAxiom)query.originalAxiom;
					
				/*if (query.algorithm.equals(ReasoningType.RELEVANT)){
					//System.out.println("EXECUTING: BASIC RELEVANT CLOSURE");
					//System.out.println();
					
					/**************** RELEVANT CLOSURE ******************/
					//Quick out if antecedent of query is not exceptional
					/*Ranking newRanking2 = setRanking(prunedRanking);
					if (!helperClass.isExceptional(modifiedQuery, newRanking2)){
						return helperClass.executeNonExceptionalQuery(modifiedQuery, newRanking2);
					}
					
					// Calculate CBasis
					Set<OWLAxiom> cbasis = new HashSet<OWLAxiom>();
					for (OWLAxiom a: helperClass.getCBasis(modifiedQuery)){
						cbasis.add(a);
					}

					/** Quick out: if the axioms in the justifications for the exceptionality
					 *  are all contained in <T> then the query is totally exceptional.
					 */
					/*if (!helperClass.isCBasisValid()){
						//System.out.println("Antecedent is exceptional but CBasis is empty.");
						return true;
					}
					
					Ranking newRanking3 = setRanking(prunedRanking);
					return executeBasicRelevantClosure(query, modifiedQuery, cbasis, newRanking3);
				}*/
				
				/*else if (query.algorithm.equals(ReasoningType.MIN_RELEVANT)){
					//System.out.println("EXECUTING: MINIMAL RELEVANT CLOSURE");
					//System.out.println();
					
					/**************** MINIMAL RELEVANT CLOSURE ******************/
					//Quick out if antecedent of query is not exceptional
					/*Ranking newRanking9 = setRanking(prunedRanking);
					if (!helperClass.isExceptional(modifiedQuery, newRanking9)){
						return helperClass.executeNonExceptionalQuery(modifiedQuery, newRanking9);
					}*/
					
					// Calculate MinCBasis
					/*Set<OWLAxiom> mincbasis = new HashSet<OWLAxiom>();
					for (OWLAxiom a: helperClass.getMinCBasis(modifiedQuery)){
						mincbasis.add(a);
					}
					
					
					
					/** Quick out: if the axioms in the justifications for the exceptionality
					 *  are all contained in <T> then the query is totally exceptional.
					 */
					/*if (!helperClass.isCBasisValid()){
						//System.out.println("Antecedent is exceptional but CBasis is empty.");
						return true;
					}
					Ranking newRanking4 = setRanking(prunedRanking);
					return executeBasicRelevantClosure(query, modifiedQuery, mincbasis, newRanking4);
				}*/
				
				//else if (query.algorithm.equals(ReasoningType.LEX_RELEVANT)){
					//System.out.println("EXECUTING: LEXICALLY RELEVANT CLOSURE");
					//System.out.println();
					
					/**************** LEXICALLY RELEVANT CLOSURE ******************/
					//Quick out if antecedent of query is not exceptional
					//boolean isExceptional = false;
					//Ranking newRanking5 = setRanking(prunedRanking);
					//if (helperClass.isExceptional(modifiedQuery, newRanking5)){
					//	isExceptional = true;
						//return helperClass.executeNonExceptionalQuery(modifiedQuery, newRanking5);
					//}
					
					// Calculate MinCBasis
					/*Set<OWLAxiom> mincbasis = new HashSet<OWLAxiom>();
					Set<OWLAxiom> tmp = null;
					
					long justtime = System.currentTimeMillis();
					tmp = helperClass.getMinCBasis(modifiedQuery);
					justtime = System.currentTimeMillis() - justtime;
					entailmentChecksLR += helperClass.justsEntailmentChecks;
					entailmentChecksBR += helperClass.justsEntailmentChecks;
					entailmentChecksMR += helperClass.justsEntailmentChecks;
					hsTreeSize = helperClass.hsTreeSize;
					
					for (OWLAxiom a: tmp){
						mincbasis.add(a);
					}
					
					mincbasisSize = mincbasis.size();
					
					noOfJusts = helperClass.noOfJusts;
					avgSizeOfAJust = helperClass.avgSizeOfAJust;
					hst = helperClass.hst;
					Set<OWLAxiom> cbasis = new HashSet<OWLAxiom>();
					cbasis.addAll(helperClass.cbasis);
					cbasisSize = cbasis.size();
					
					//Execute lex-rel, basic-rel and min-rel closures now to save time.
					
					/** Quick out: if the axioms in the justifications for the exceptionality
					 *  are all contained in <T> then the query is totally exceptional.
					 */
					//if (!helperClass.isCBasisValid()){
						//System.out.println("Invalid Cbasis");
					//	return true;
					//}
					//int entChecksLR = 0;
					/*lexreltime = 0;
					long startlexreltime = System.currentTimeMillis();
					Ranking newRanking6 = setRanking(prunedRanking);
					lexrelanswer = executeLexicographicClosure(query, modifiedQuery, mincbasis, newRanking6);
					lexreltime = (System.currentTimeMillis() - startlexreltime) + justtime;
					entailmentChecksLR += entailmentChecksLC;
					
					minreltime = 0;
					long startminreltime = System.currentTimeMillis();
					Ranking newRanking8 = setRanking(prunedRanking);
					minrelanswer = executeBasicRelevantClosure(query, modifiedQuery, mincbasis, newRanking8);
					minreltime = (System.currentTimeMillis() - startminreltime) + justtime;
					entailmentChecksMR += entailmentChecksRelC;
					
					basicreltime = 0;
					long startbasicreltime = System.currentTimeMillis();
					Ranking newRanking7 = setRanking(prunedRanking);
					basicrelanswer = executeBasicRelevantClosure(query, modifiedQuery, cbasis, newRanking7);
					basicreltime = (System.currentTimeMillis() - startbasicreltime) + justtime;
					entailmentChecksBR += entailmentChecksRelC;
					
					
					return true;
				}*/
				
				/*else if (query.algorithm.equals(ReasoningType.LEXICOGRAPHIC)){
					/************** LEXICOGRAPHIC CLOSURE ****************/
					/*Ranking newRanking7 = setRanking(prunedRanking);
					lcResult = executeLexicographicClosure(query, modifiedQuery, new HashSet<OWLAxiom>(), newRanking7);
					return lcResult;
				}*/
				
				//else{
					/*System.out.println();
					System.out.println("---------------------------");
					System.out.println("Executing Rational Closure:");
					System.out.println("---------------------------");
					System.out.println();*/
					/************** RATIONAL CLOSURE ****************/
					Ranking newRanking8 = setRanking(prunedRanking);
					rcResult = executeRationalClosure(query, modifiedQuery, newRanking8);
					return rcResult;
				//}
			/*}
			else{
				
				/*************** PREFERENTIAL QUERY **************/

				// Procedure is not yet finalised.
				//return false;
				//OWLAxiom additionAxiom = AxiomManipulator.getPreferentialAdditionAxiom(query.originalAxiom);
				//OWLAxiom queryAxiom = AxiomManipulator.getPreferentialQueryAxiom(query.originalAxiom);
				//OWLSubClassOfAxiom modifiedQuery = (OWLSubClassOfAxiom)queryAxiom;		
				//return executeRationalClosure(query, modifiedQuery);
			//}
		}
		else{	
			/********************** ABOX QUERY **********************/
			// Procedure is not yet finalised.
			return false;
		}
	}
	
	private boolean executeLexicographicClosure(Query originalQuery, OWLSubClassOfAxiom modifiedQuery, Set<OWLAxiom> minCBasis, Ranking ranking) throws OWLOntologyCreationException{
		entailmentChecksLC = 0;
		helperClass.resetEntChecks();
		/**** Background Knowledge <T> */
		Set<OWLAxiom> backgroundKnwldge = new HashSet<OWLAxiom>();
		backgroundKnwldge.addAll(ranking.getInfiniteRank().getAxioms());		
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology tmpOntology = ontologyManager.createOntology(backgroundKnwldge);	
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(tmpOntology);
		
		/** Ranks in the ranking <D> */
		ArrayList<Rank> ranks = new ArrayList<Rank>();ranks.addAll(ranking.getRanking());
		
		/*** If strict query **/
		if ((!originalQuery.defeasible)){
			OWLSubClassOfAxiom finalQuery = OWLManager.getOWLDataFactory().getOWLSubClassOfAxiom(modifiedQuery.getSubClass(), modifiedQuery.getSuperClass());
			entailmentChecksLC++;
			return reasoner.isEntailed(finalQuery);
		}
		
		/*** Reverse ranking order ****/
		/*int n = ranks.size();
	    for (int i = 0; i <= Math.floor((n-2)/2);i++){
	         Rank tmp = ranks.get(i);
	         ranks.set(i, ranks.get(n - 1 - i));
	         ranks.set(n - 1 - i, tmp);
		}*/
	    
	    /** Calculate C-compatible subset of the ranking */ 
	    ArrayList<Rank> cCompatibleRanks = helperClass.getCCompatibleSubset(ranks, originalQuery, modifiedQuery);
	    entailmentChecksLC += helperClass.entailmentChecks;
	    /********************** Log some stuff *********************/
	    ccompat = new ArrayList<Rank>();
	    ccompat.addAll(helperClass.ccompat);
	    noOfRanksInCCompatLR = helperClass.sizeOfCCompat;
		axiomsInCCompatLR = helperClass.noOfAxiomsInCCompat;
		queryHasInfiniteRank = helperClass.antecedentHasInfiniteRank;
		queryIsNonExceptional = helperClass.antecedentIsNonExceptional;
		/***********************************************************/
		
	    // If we have a min CBasis to optimise the computation
	    OWLClassExpression irrelevantKnowledgeConcept = null;
	    ArrayList<OWLAxiom> newProblematicRank = new ArrayList<OWLAxiom>();
	    newProblematicRank.addAll(helperClass.getProblematicRank());
	    pRank = new ArrayList<OWLAxiom>();
	    pRank.addAll(helperClass.getProblematicRank());
	    
	    //System.out.println("problematic rank size: " + newProblematicRank.size());
		
	    if (minCBasis.isEmpty()){// Unoptimised
			irrelevantKnowledgeConcept = df.getOWLThing();
		}
		else{					 // Optimised
		    Set<OWLAxiom> irr = new HashSet<OWLAxiom>();
			irr.addAll(helperClass.getProblematicRank());
			irr.removeAll(minCBasis);
			
			axiomsInCCompatLR += irr.size(); //For lexically relevant closure
			
			irrelevantKnowledgeConcept = helperClass.getInternalisation(irr);
			newProblematicRank.retainAll(minCBasis);
		}
	    
	    /**Antecedent of query is totally exceptional. Entailment test against Tbox**/
		if (cCompatibleRanks.size() == 0){
			entailmentChecksLC++; 
			return reasoner.isEntailed(modifiedQuery);
		}
		else{
			// Rank of antecedent is not infinite
			OWLClassExpression ranksInternalisation = df.getOWLObjectIntersectionOf(irrelevantKnowledgeConcept, helperClass.getInternalisation(cCompatibleRanks)); // Internalisation of the compatible ranks
			OWLClassExpression lhs = null;
			OWLClassExpression rhs = modifiedQuery.getSuperClass();
			
			if (cCompatibleRanks.size() == ranking.size() || (newProblematicRank.size() == 1)){
				// Either the antecedent of the query is not exceptional or the problematic rank has only one axiom.
				// In either case, we do not need to compute a LAC.
						
				lhs = df.getOWLObjectIntersectionOf(ranksInternalisation, modifiedQuery.getSubClass());				
			}
			else{
				// Core case, we need to compute a LAC
				// Compute lexically additive concept
				helperClass.resetEntChecks();
				OWLClassExpression lac = helperClass.getLexicallyAdditiveConcept(cCompatibleRanks, newProblematicRank, modifiedQuery.getSubClass());
				entailmentChecksLC += helperClass.entailmentChecks;
				/************ Log num of conjuncts in each disjunct of LAC *******************/
				noOfAxiomsKeptFromProblematicRank = helperClass.keptAxiomsFromPRank;
				axiomsInCCompatLR += noOfAxiomsKeptFromProblematicRank;
				/*****************************************************************************/
				lhs = df.getOWLObjectIntersectionOf(ranksInternalisation, modifiedQuery.getSubClass(), lac);
			}
			
			// Perform entailment check
			OWLSubClassOfAxiom query = df.getOWLSubClassOfAxiom(lhs, rhs);
			entailmentChecksLC++;
			return reasoner.isEntailed(query);
		}
	}
		
	private boolean executeRationalClosure(Query originalQuery, OWLSubClassOfAxiom modifiedQuery, Ranking ranking) throws OWLOntologyCreationException{
		entailmentChecksRC = 0;
		helperClass.resetEntChecks();
		/**** Background Knowledge <T> */
		Set<OWLAxiom> backgroundKnwldge = new HashSet<OWLAxiom>();
		backgroundKnwldge.addAll(ranking.getInfiniteRank().getAxioms());
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology tmpOntology = ontologyManager.createOntology(backgroundKnwldge);	
		//Reasoner reasoner = new Reasoner(new Configuration(), tmpOntology);
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(tmpOntology);
		
		/** Ranks in the ranking <D> */
		ArrayList<Rank> ranks = new ArrayList<Rank>();ranks.addAll(ranking.getRanking());
		
		/*** If strict query **/
		if ((!originalQuery.defeasible)){
			OWLSubClassOfAxiom finalQuery = OWLManager.getOWLDataFactory().getOWLSubClassOfAxiom(modifiedQuery.getSubClass(), modifiedQuery.getSuperClass());
			entailmentChecksRC++;
			return reasoner.isEntailed(finalQuery);
		}
		
		/*** Reverse ranking order ****/
		/*int n = ranks.size();
	    for (int i = 0; i <= Math.floor((n-2)/2);i++){
	         Rank tmp = ranks.get(i);
	         ranks.set(i, ranks.get(n - 1 - i));
	         ranks.set(n - 1 - i, tmp);
		}*/
	    
	    /** Calculate C-compatible subset of the ranking */ 
	    ArrayList<Rank> cCompatibleRanks = helperClass.getCCompatibleSubset(ranks, originalQuery, modifiedQuery);
	    entailmentChecksRC += helperClass.entailmentChecks;
		/********************** Log some stuff *********************/
	    ccompat = new ArrayList<Rank>();
	    ccompat.addAll(helperClass.ccompat);
	    noOfRanksInCCompatLR = helperClass.sizeOfCCompat;
		axiomsInCCompatLR = helperClass.noOfAxiomsInCCompat;
		queryHasInfiniteRank = helperClass.antecedentHasInfiniteRank;
		queryIsNonExceptional = helperClass.antecedentIsNonExceptional;
		/***********************************************************/
		
		/**Antecedent of query is totally exceptional. Entailment test against Tbox**/
		if (cCompatibleRanks.size() == 0){
			entailmentChecksRC++;
			return reasoner.isEntailed(modifiedQuery);
		}
		else{
			/** Core Case: defeasible query, more than one rank & antecedent is not totally exceptional */
			OWLClassExpression ranksInternalisation = helperClass.getInternalisation(cCompatibleRanks);
			OWLClassExpression lhs = df.getOWLObjectIntersectionOf(ranksInternalisation, modifiedQuery.getSubClass());
			OWLClassExpression rhs = modifiedQuery.getSuperClass();
			OWLSubClassOfAxiom query = df.getOWLSubClassOfAxiom(lhs, rhs);
			entailmentChecksRC++;
			return reasoner.isEntailed(query);
		}
	}	
	
	public void computeSuperClasses(OWLClassExpression cls, OWLClassExpression ccompat, Rank infRank) throws OWLOntologyCreationException, InconsistentOntologyException{
		strictSuperClasses = new HashSet<OWLClass>();
		typicalSuperClasses = new HashSet<OWLClass>();
		
		Set<OWLAxiom> strictKnwldge = new HashSet<OWLAxiom>();
		for (OWLAxiom ax: infRank.getAxioms()) {
			if (!ax.isOfType(AxiomType.ABoxAxiomTypes))
				strictKnwldge.add(ax);
		}
		 		 
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology tmpOntology = ontologyManager.createOntology(strictKnwldge);
		
		//Reasoner reasoner = new Reasoner(new Configuration(), tmpOntology);
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(tmpOntology);
		
		strictSuperClasses = new HashSet<OWLClass>(reasoner.getSuperClasses(cls, false).getFlattened());
		
		/*System.out.println();
		System.out.println("Strict:");
		for (OWLClass c: strictSuperClasses) {
			System.out.println(c);
		}
		System.out.println();*/
		
		
		OWLAxiom axiom = ontologyManager.getOWLDataFactory().getOWLSubClassOfAxiom(ontologyManager.getOWLDataFactory().getOWLThing(), ccompat);
		ontologyManager.addAxiom(tmpOntology, axiom);
		 
		typicalSuperClasses = new HashSet<OWLClass>(reasoner.getSuperClasses(cls, false).getFlattened());
		
		/*System.out.println("typical:");
		for (OWLClass c: typicalSuperClasses) {
			System.out.println(c);
		}
		System.out.println();*/
		
		for (OWLClass c: strictSuperClasses){
			typicalSuperClasses.remove(c);
		}
		
		try{
			if (!cls.isAnonymous()) {
				typicalSuperClasses.remove(cls.asOWLClass());
			}
		}
		catch (Exception e){
			System.out.println("Not an OWL Class!");
		}
	}
	
	public Set<OWLClass> getSuperClassesStrict(){
		return strictSuperClasses;
	}
	
	public Set<OWLClass> getSuperClassesTypical(){
		return typicalSuperClasses;
	}
	
	public List<OWLClass> getSubClasses(){
		return new ArrayList<OWLClass>();
	}
	
	public OWLClassExpression getCCompatibility(OWLClassExpression cls, ReasoningType algorithm, Ranking ranking) throws OWLOntologyCreationException{	
		ArrayList<Rank> cCompatibleRanks = new ArrayList<Rank>();	
		ArrayList<Rank> ranks = new ArrayList<Rank>();ranks.addAll(ranking.getRanking());
		
		/*** Reverse ranking order ****/
		/*int n = ranks.size();
		for (int i = 0; i <= Math.floor((n-2)/2);i++){
		   Rank tmp = ranks.get(i);
		   ranks.set(i, ranks.get(n - 1 - i));
		   ranks.set(n - 1 - i, tmp);
		}*/
		   		
		/** Calculate C-compatible subset of the ranking */ 
		/** This should be calculated for all closures except the Relevant closures */
		if (!algorithm.equals(ReasoningType.RELEVANT) && !algorithm.equals(ReasoningType.MIN_RELEVANT)){
			try {
				cCompatibleRanks = helperClass.getCCompatibleSubset(ranks, cls);
			} catch (OWLOntologyCreationException e) {
				System.out.println("Error identifying C-compatibility.");
				e.printStackTrace();
			}
		}
		
		/** Additional work for Lexicographic closure */
		if (algorithm.equals(ReasoningType.LEXICOGRAPHIC)){
			System.out.println();
			System.out.println("Lexicographic Closure");
			System.out.println();
			ArrayList<OWLAxiom> newProblematicRank = new ArrayList<OWLAxiom>();
			newProblematicRank.addAll(helperClass.getProblematicRank());

			pRank = new ArrayList<OWLAxiom>();
			pRank.addAll(helperClass.getProblematicRank());
			if (cCompatibleRanks.size() == ranking.size() || (newProblematicRank.size() == 1)){				
				return helperClass.getInternalisation(cCompatibleRanks);				
			}
			else{
				OWLClassExpression lac = helperClass.getLexicallyAdditiveConcept(cCompatibleRanks, newProblematicRank, cls);
				return df.getOWLObjectIntersectionOf(helperClass.getInternalisation(cCompatibleRanks), lac);
			}
		}
		/** Additional work for Basic Relevant closure */
		else if (algorithm.equals(ReasoningType.RELEVANT)){
			System.out.println();
			System.out.println("Basic Relevant Closure");
			System.out.println();
			Set<OWLAxiom> cbasis = helperClass.getCBasis(cls);
			ArrayList<Rank> mrCCompatibleRanks = helperClass.getMRCCompatibleSubset(ranks, cls, cbasis);	
			return helperClass.getInternalisation(mrCCompatibleRanks);
		}
		/** Additional work for Minimal Relevant closure */
		else if (algorithm.equals(ReasoningType.MIN_RELEVANT)){
			System.out.println();
			System.out.println("Minimal Relevant Closure");
			System.out.println();
			Set<OWLAxiom> cbasis = helperClass.getMinCBasis(cls);		
			ArrayList<Rank> mrCCompatibleRanks = helperClass.getMRCCompatibleSubset(ranks, cls, cbasis);			
			return helperClass.getInternalisation(mrCCompatibleRanks);
		}
		else {
			System.out.println();
			System.out.println("Rational Closure");
			System.out.println();

			return helperClass.getInternalisation(cCompatibleRanks);
		}	 	
	}
	
	public Set<OWLAxiom> getSingleExtension(){
		return singleExtension;
	}
	
	public Ranking getRanking(){
		return ranking;
	}
	
	public Set<OWLAxiom> getInconsBasis(){
		return global_inconsBasis;
	}
	
	public Set<OWLAxiom> getMinInconsBasis(){
		return global_minInconsBasis;
	}
}
