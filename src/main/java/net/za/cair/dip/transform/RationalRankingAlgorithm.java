package net.za.cair.dip.transform;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;

import net.za.cair.dip.model.OntologyStructure;
import net.za.cair.dip.model.Rank;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.util.ManchesterOWLSyntaxOWLObjectRendererImpl;
import net.za.cair.dip.util.Utility;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

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

public class RationalRankingAlgorithm{

	private OntologyStructure ontologyStructure;
	private OWLReasonerFactory reasonerFactory;
	private Set<OWLClassExpression> possibleExceptions;
	private RankingHelperClass helperClass;
	private ArrayList<ArrayList<OWLAxiom>> globalETransforms;
	private Ranking rankingTmp;
	private Rank infiniteRank;
	public int entailmentChecks; // NO_UCD (unused code)
	public int recursiveCount; // NO_UCD (unused code)
	public int noOfBrokenAxioms;  // NO_UCD (unused code)
	public int unsatLHSDefeasibleSubs;

	public RationalRankingAlgorithm(OWLReasonerFactory reasonerFactory, OntologyStructure ontologyStructure, Set<OWLClassExpression> possibleExceptions){ // NO_UCD (test only)
		this.ontologyStructure = ontologyStructure;
		this.reasonerFactory = reasonerFactory;
		this.possibleExceptions = possibleExceptions;
		rankingTmp = new Ranking();
		entailmentChecks = 0;
		recursiveCount = 0;
		noOfBrokenAxioms = 0;
		unsatLHSDefeasibleSubs = 0;
	}

	public RationalRankingAlgorithm(OWLReasonerFactory reasonerFactory, OWLOntology ontology) throws OWLOntologyCreationException{
		/*ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		System.out.println();
		int count = 0;
		for (OWLAxiom a: ontology.getLogicalAxioms()){
			if (Utility.isDefeasible(a)){
				count++;
			}		
	    }

		System.out.println();

		System.out.println();
		//System.out.println(ontology.getLogicalAxiomCount());*/

		// Print etransforms
		Utility u = new Utility();
		ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		System.out.println();
		System.out.println("Ontology Axioms:");
		System.out.println("----------------");
		for (OWLAxiom a: ontology.getLogicalAxioms()) {
			System.out.print(man.render(a));
			if (u.isDefeasible(a)){
				System.out.println("*");
			}	
			else {
				System.out.println();
			}
		}
		System.out.println();

		this.ontologyStructure = new OntologyStructure(ontology);

		this.reasonerFactory = reasonerFactory;
		helperClass = new RankingHelperClass(reasonerFactory, ontologyStructure);
		this.possibleExceptions = helperClass.getPossibleExceptions();

		rankingTmp = new Ranking();
		entailmentChecks = 0;
		recursiveCount = 0;
		noOfBrokenAxioms = 0;
		unsatLHSDefeasibleSubs = helperClass.unsatLHSDefeasibleSubsumptions;
	}

	public Ranking computeRanking() throws OWLException{
		ArrayList<ArrayList<OWLAxiom>> eT = new ArrayList<ArrayList<OWLAxiom>>();
		ArrayList<ArrayList<OWLAxiom>> dT = new ArrayList<ArrayList<OWLAxiom>>();

		AxiomETransformFactory eFactory = new AxiomETransformFactory(reasonerFactory, ontologyStructure, possibleExceptions);
		eT = eFactory.generateETransforms();
		globalETransforms = eFactory.getETransforms();

		// Print etransforms
		int c = 0;
		ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		/*System.out.println();
		System.out.println("E-Transforms:");
		System.out.println("-------------");
		for (ArrayList<OWLAxiom> trans: eT) {
			System.out.println(c + ":");
			for (OWLAxiom a: trans) {
				System.out.println(man.render(a));
			}
			c++;
			System.out.println();
		}
		System.out.println();
		System.out.println("Infinite Rank:");
		System.out.println("--------------");
		// Print infinite rank
		for (OWLAxiom a: eFactory.infiniteRank) {
			System.out.println(man.render(a));
		}
		System.out.println();*/

		//eT = removeEmptyRanks(eT); //remove empty ranks
		entailmentChecks = eFactory.entailmentChecks;
		recursiveCount = eFactory.recursiveCount;
		noOfBrokenAxioms = eFactory.noOfBrokenAxioms;

		ArrayList<ArrayList<OWLAxiom>> new_dT = new ArrayList<ArrayList<OWLAxiom>>();

		if (!eT.isEmpty()){
			DTransformFactory dFactory = new DTransformFactory(eT);
			dT = dFactory.getDTransforms();

			// Remove infinite rank
			for (ArrayList<OWLAxiom> tmp: dT){
				if (!tmp.equals(eFactory.infiniteRank))
					new_dT.add(tmp);
			}
		}

		// Create ranks
		for (int i = 0; i <= new_dT.size()-1;i++) {
			rankingTmp.add(new Rank(new_dT.get(i), i+1));
		}
		
		/*for (ArrayList<OWLAxiom> dTRank: new_dT){										//add other ranks
			rankingTmp.add(new Rank(dTRank));
		}*/

		/** Generate infinite rank **/
		Set<OWLAxiom> infiniteRankAxioms = new HashSet<OWLAxiom>();
		infiniteRankAxioms.addAll(eFactory.infiniteRank);
		infiniteRankAxioms.addAll(ontologyStructure.bBox.getAxioms());
		this.infiniteRank = new Rank(new ArrayList<OWLAxiom>(infiniteRankAxioms));
		rankingTmp.setInfiniteRank(this.infiniteRank);

		return rankingTmp;	
	}

	private ArrayList<ArrayList<OWLAxiom>> removeEmptyRanks(ArrayList<ArrayList<OWLAxiom>> set){
		ArrayList<ArrayList<OWLAxiom>> result = new ArrayList<ArrayList<OWLAxiom>>();
		for (ArrayList<OWLAxiom> tmp: set){
			if (!tmp.isEmpty()){
				result.add(tmp);
			}
		}
		return result;
	}

	public Ranking getRanking(){
		return rankingTmp;
	}

	public Rank getInfiniteRank(){
		return this.infiniteRank;
	}

	public OntologyStructure getOntologyStructure(){
		return ontologyStructure;
	}
	
	public ArrayList<ArrayList<OWLAxiom>> getETransforms(){
		return globalETransforms;
	}
}
