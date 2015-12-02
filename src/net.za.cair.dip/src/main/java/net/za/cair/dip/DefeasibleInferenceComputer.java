package net.za.cair.dip;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import org.protege.editor.owl.ui.OWLClassExpressionComparator;
import org.semanticweb.owl.explanation.impl.blackbox.hst.HittingSetTree;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import net.za.cair.dip.model.Query;
import net.za.cair.dip.model.Rank;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.model.ReasoningType;
import net.za.cair.dip.util.ManchesterOWLSyntaxOWLObjectRendererImpl;

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
	
	public boolean queryHasInfiniteRank;//
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
	public HittingSetTree<OWLAxiom> hst;
	
	public DefeasibleInferenceComputer(OWLReasonerFactory reasonerFactory){ // NO_UCD (unused code)
		this.df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		this.reasonerFactory = reasonerFactory;		
		helperClass = new DefeasibleInferenceHelperClass(reasonerFactory, this.ranking);
		this.ranking = null;
	}
	
	public DefeasibleInferenceComputer(OWLReasonerFactory reasonerFactory, Ranking ranking){
		this.df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		this.ranking = setRanking(ranking);
		this.reasonerFactory = reasonerFactory;		
		helperClass = new DefeasibleInferenceHelperClass(reasonerFactory, this.ranking);
		this.prunedRanking = null;
		pRank = new ArrayList<OWLAxiom>();
	}

	public DefeasibleInferenceComputer(OWLReasonerFactory reasonerFactory, OWLOntology ontology){
		this.df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		this.reasonerFactory = reasonerFactory;
		//ontologyStructure = new OntologyStructure(ontology);
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
			Rank tmp = new Rank(currentRankAxioms);
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
	
	/*private double getAvgRankSize(Ranking ranking){
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		for (Rank rank: ranking.getRanking()){
			axioms.addAll(rank.getAxioms());
		}
		
		if (ranking.size() > 0){
			double rSize = (double)ranking.size();
			return axioms.size()/rSize;
		}
		else{
			return 0.0;
		}
	}*/
	
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
					
				if (query.algorithm.equals(ReasoningType.RELEVANT)){
					System.out.println("EXECUTING: BASIC RELEVANT CLOSURE");
					System.out.println();
					
					/**************** RELEVANT CLOSURE ******************/
					//Quick out if antecedent of query is not exceptional
					Ranking newRanking2 = setRanking(prunedRanking);
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
					if (!helperClass.isCBasisValid()){
						System.out.println("Antecedent is exceptional but CBasis is empty.");
						return true;
					}
					
					Ranking newRanking3 = setRanking(prunedRanking);
					return executeBasicRelevantClosure(query, modifiedQuery, cbasis, newRanking3);
				}
				
				else if (query.algorithm.equals(ReasoningType.MIN_RELEVANT)){
					System.out.println("EXECUTING: MINIMAL RELEVANT CLOSURE");
					System.out.println();
					
					/**************** MINIMAL RELEVANT CLOSURE ******************/
					//Quick out if antecedent of query is not exceptional
					/*Ranking newRanking9 = setRanking(prunedRanking);
					if (!helperClass.isExceptional(modifiedQuery, newRanking9)){
						return helperClass.executeNonExceptionalQuery(modifiedQuery, newRanking9);
					}*/
					
					// Calculate MinCBasis
					Set<OWLAxiom> mincbasis = new HashSet<OWLAxiom>();
					for (OWLAxiom a: helperClass.getMinCBasis(modifiedQuery)){
						mincbasis.add(a);
					}
					
					
					
					/** Quick out: if the axioms in the justifications for the exceptionality
					 *  are all contained in <T> then the query is totally exceptional.
					 */
					if (!helperClass.isCBasisValid()){
						System.out.println("Antecedent is exceptional but CBasis is empty.");
						return true;
					}
					Ranking newRanking4 = setRanking(prunedRanking);
					return executeBasicRelevantClosure(query, modifiedQuery, mincbasis, newRanking4);
				}
				
				else if (query.algorithm.equals(ReasoningType.LEX_RELEVANT)){
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
					Set<OWLAxiom> mincbasis = new HashSet<OWLAxiom>();
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
					lexreltime = 0;
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
				}
				
				else if (query.algorithm.equals(ReasoningType.LEXICOGRAPHIC)){
					/************** LEXICOGRAPHIC CLOSURE ****************/
					Ranking newRanking7 = setRanking(prunedRanking);
					lcResult = executeLexicographicClosure(query, modifiedQuery, new HashSet<OWLAxiom>(), newRanking7);
					return lcResult;
				}
				
				else{
					/************** RATIONAL CLOSURE ****************/
					Ranking newRanking8 = setRanking(prunedRanking);
					rcResult = executeRationalClosure(query, modifiedQuery, newRanking8);
					return rcResult;
				}
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
	
	
	
	
	private boolean executeRationalClosure(Query originalQuery, OWLSubClassOfAxiom modifiedQuery, Ranking ranking) throws OWLOntologyCreationException{
		entailmentChecksRC = 0;
		helperClass.resetEntChecks();
		/**** Background Knowledge <T> */
		Set<OWLAxiom> backgroundKnwldge = new HashSet<OWLAxiom>();
		backgroundKnwldge.addAll(ranking.getInfiniteRank().getAxioms());
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology tmpOntology = ontologyManager.createOntology(backgroundKnwldge);	
		OWLReasoner reasoner = reasonerFactory.createReasoner(tmpOntology);
		
		/** Ranks in the ranking <D> */
		ArrayList<Rank> ranks = new ArrayList<Rank>();ranks.addAll(ranking.getRanking());
		
		/*** If strict query **/
		if ((!originalQuery.defeasible)){
			OWLSubClassOfAxiom finalQuery = OWLManager.getOWLDataFactory().getOWLSubClassOfAxiom(modifiedQuery.getSubClass(), modifiedQuery.getSuperClass());
			entailmentChecksRC++;
			return reasoner.isEntailed(finalQuery);
		}
		
		/*** Reverse ranking order ****/
		int n = ranks.size();
	    for (int i = 0; i <= Math.floor((n-2)/2);i++){
	         Rank tmp = ranks.get(i);
	         ranks.set(i, ranks.get(n - 1 - i));
	         ranks.set(n - 1 - i, tmp);
		}
	    
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
	
	
	
	private boolean executeLexicographicClosure(Query originalQuery, OWLSubClassOfAxiom modifiedQuery, Set<OWLAxiom> minCBasis, Ranking ranking) throws OWLOntologyCreationException{
		entailmentChecksLC = 0;
		helperClass.resetEntChecks();
		/**** Background Knowledge <T> */
		Set<OWLAxiom> backgroundKnwldge = new HashSet<OWLAxiom>();
		backgroundKnwldge.addAll(ranking.getInfiniteRank().getAxioms());		
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology tmpOntology = ontologyManager.createOntology(backgroundKnwldge);	
		OWLReasoner reasoner = reasonerFactory.createReasoner(tmpOntology);
		
		/** Ranks in the ranking <D> */
		ArrayList<Rank> ranks = new ArrayList<Rank>();ranks.addAll(ranking.getRanking());
		
		/*** If strict query **/
		if ((!originalQuery.defeasible)){
			OWLSubClassOfAxiom finalQuery = OWLManager.getOWLDataFactory().getOWLSubClassOfAxiom(modifiedQuery.getSubClass(), modifiedQuery.getSuperClass());
			entailmentChecksLC++;
			return reasoner.isEntailed(finalQuery);
		}
		
		/*** Reverse ranking order ****/
		int n = ranks.size();
	    for (int i = 0; i <= Math.floor((n-2)/2);i++){
	         Rank tmp = ranks.get(i);
	         ranks.set(i, ranks.get(n - 1 - i));
	         ranks.set(n - 1 - i, tmp);
		}
	    
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
	
	private boolean executeBasicRelevantClosure(Query originalQuery, OWLSubClassOfAxiom modifiedQuery, Set<OWLAxiom> cbasis, Ranking ranking) throws OWLOntologyCreationException{
		entailmentChecksRelC = 0;
		helperClass.resetEntChecks();
		/**** Background Knowledge <T> */
		Set<OWLAxiom> backgroundKnwldge = new HashSet<OWLAxiom>();
		backgroundKnwldge.addAll(ranking.getInfiniteRank().getAxioms());		
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology tmpOntology = ontologyManager.createOntology(backgroundKnwldge);	
		OWLReasoner reasoner = reasonerFactory.createReasoner(tmpOntology);
		
		/** Ranks in the ranking <D> */
		ArrayList<Rank> ranks = new ArrayList<Rank>();
		for (Rank r: ranking.getRanking()){
			ArrayList<OWLAxiom> tmpSet = new ArrayList<OWLAxiom>();tmpSet.addAll(r.getAxioms());
			Rank tmp = new Rank(tmpSet);
			ranks.add(tmp);
		}
	
		/*** If strict query **/
		if ((!originalQuery.defeasible)){
			OWLSubClassOfAxiom finalQuery = OWLManager.getOWLDataFactory().getOWLSubClassOfAxiom(modifiedQuery.getSubClass(), modifiedQuery.getSuperClass());
			entailmentChecksRelC++;
			return reasoner.isEntailed(finalQuery);
		}
		
		/*** Reverse ranking order ****/
		int n = ranks.size();
	    for (int i = 0; i <= Math.floor((n-2)/2);i++){
	         Rank tmp = ranks.get(i);
	         ranks.set(i, ranks.get(n - 1 - i));
	         ranks.set(n - 1 - i, tmp);
		}
	    
	    /** Calculate Maximally-Relevant C-compatible subset of the ranking */
	    ArrayList<Rank> mrCCompatibleRanks = helperClass.getMRCCompatibleSubset(ranks, originalQuery, modifiedQuery, cbasis);
	    entailmentChecksRelC += helperClass.entailmentChecks;
	    /********************** Log some stuff *********************/
	    //ccompat = new ArrayList<Rank>();
	    //ccompat.addAll(helperClass.ccompat);
	    noOfRanksInCCompatMR = helperClass.sizeOfCCompat;
	    noOfRanksInCCompatBR = helperClass.sizeOfCCompat;
	    
		axiomsInCCompatMR = helperClass.noOfAxiomsInCCompat;
		axiomsInCCompatBR = helperClass.noOfAxiomsInCCompat;
		 
		queryHasInfiniteRank = helperClass.antecedentHasInfiniteRank;
		queryIsNonExceptional = helperClass.antecedentIsNonExceptional;
		/***********************************************************/
		
		
	    /**Antecedent of query is totally exceptional. Entailment test against Tbox**/
		if (mrCCompatibleRanks.size() == 0){
			entailmentChecksRelC++;
			return reasoner.isEntailed(modifiedQuery);
		}
		else{
			/** Core Case: defeasible query, more than one rank & antecedent is not totally exceptional */
			OWLClassExpression ranksInternalisation = helperClass.getInternalisation(mrCCompatibleRanks);
			OWLClassExpression lhs = df.getOWLObjectIntersectionOf(ranksInternalisation, modifiedQuery.getSubClass());
			OWLClassExpression rhs = modifiedQuery.getSuperClass();
			OWLSubClassOfAxiom query = df.getOWLSubClassOfAxiom(lhs, rhs);
			entailmentChecksRelC++;
			return reasoner.isEntailed(query);
		}
	}
	
	public OWLClassExpression getCCompatibility(OWLClassExpression cls, ReasoningType algorithm, Ranking ranking) throws OWLOntologyCreationException{		
		ArrayList<Rank> cCompatibleRanks = new ArrayList<Rank>();
	
		ArrayList<Rank> ranks = new ArrayList<Rank>();ranks.addAll(ranking.getRanking());
		
		/*** Reverse ranking order ****/
		int n = ranks.size();
		for (int i = 0; i <= Math.floor((n-2)/2);i++){
		   Rank tmp = ranks.get(i);
		   ranks.set(i, ranks.get(n - 1 - i));
		   ranks.set(n - 1 - i, tmp);
		}
		   
		/** Calculate C-compatible subset of the ranking */ 
		try {
			cCompatibleRanks = helperClass.getCCompatibleSubset(ranks, cls);
		} catch (OWLOntologyCreationException e) {
			System.out.println("Error identifying C-compatibility.");
			e.printStackTrace();
		}
		
		if (algorithm.equals(ReasoningType.LEXICOGRAPHIC)){
			System.out.println("Lexi");
			 ArrayList<OWLAxiom> newProblematicRank = new ArrayList<OWLAxiom>();
			 newProblematicRank.addAll(helperClass.getProblematicRank());
			 pRank = new ArrayList<OWLAxiom>();
			 pRank.addAll(helperClass.getProblematicRank());
			 OWLClassExpression lhs = null;
			if (cCompatibleRanks.size() == ranking.size() || (newProblematicRank.size() == 1)){
				// Either the antecedent of the query is not exceptional or the problematic rank has only one axiom.
				// In either case, we do not need to compute a LAC.
				System.out.println("No LAC");
				return helperClass.getInternalisation(cCompatibleRanks);				
			}
			else{
				System.out.println("LAC");
				// Core case, we need to compute a LAC
				// Compute lexically additive concept
				OWLClassExpression lac = helperClass.getLexicallyAdditiveConcept(cCompatibleRanks, newProblematicRank, cls);
				return df.getOWLObjectIntersectionOf(helperClass.getInternalisation(cCompatibleRanks), lac);
			}
		}
		else {
			System.out.println("Rat");
			return helperClass.getInternalisation(cCompatibleRanks);
		}	 
	}
	
	public void computeSuperClasses(OWLClassExpression cls, OWLClassExpression ccompat, Rank infRank) throws OWLOntologyCreationException{
		strictSuperClasses = new HashSet<OWLClass>();
		typicalSuperClasses = new HashSet<OWLClass>();
		
		/** First strict super classes */ 
		Set<OWLAxiom> strictKnwldge = new HashSet<OWLAxiom>();
		strictKnwldge.addAll(infRank.getAxioms());
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology tmpOntology = ontologyManager.createOntology(strictKnwldge);
		
		OWLReasoner reasoner = reasonerFactory.createReasoner(tmpOntology);
		
		strictSuperClasses = reasoner.getSuperClasses(cls, true).getFlattened();
		
		//if (!ccompat.isOWLThing()){
			/** Then typical super classes */
		 ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
			
		System.out.println(man.render(ccompat));
			OWLClassExpression newCls = ontologyManager.getOWLDataFactory().getOWLObjectIntersectionOf(ccompat, cls);
			typicalSuperClasses = reasoner.getSuperClasses(newCls, true).getFlattened();
			typicalSuperClasses.remove(cls.asOWLClass());
		//}
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
}
