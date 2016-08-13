package net.za.cair.dip.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.za.cair.dip.util.AxiomManipulator;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

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

public class Query {
	
	public OWLAxiom originalAxiom;
	public OWLAxiom queryReadyAxiom;
	private Ranking ranking;
	public boolean isValid, isTBoxQuery, defeasible;
	public OWLClassExpression antecedent, consequent;
	public OWLNamedIndividual individual;
	public Set<OWLNamedIndividual> individuals;
	public ReasoningType algorithm;
	
	public Query(OWLAxiom axiom, ReasoningType algorithm, boolean defeasible){
		this.originalAxiom = axiom;		
		this.algorithm = algorithm;
		this.defeasible = defeasible;
		this.isTBoxQuery = true;
	}
	
	public Query(OWLAxiom axiom, ReasoningType algorithm, Ranking ranking, boolean defeasible) throws OWLOntologyCreationException{
		originalAxiom = axiom;
		this.defeasible = defeasible;
		this.algorithm = algorithm;
		this.isTBoxQuery = originalAxiom.isOfType(AxiomType.TBoxAxiomTypes);
		this.ranking = ranking;
				
		if (originalAxiom.isOfType(AxiomType.SUBCLASS_OF)){
			OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)originalAxiom;
			antecedent = sub.getSubClass();
			consequent = sub.getSuperClass();
			
			isValid = true;
		}
		else if (originalAxiom.isOfType(AxiomType.DISJOINT_CLASSES)){
			isValid = true;
			preprocessQuery();
		} 
		else if (originalAxiom.isOfType(AxiomType.EQUIVALENT_CLASSES)){
			isValid = true;
			preprocessQuery();
		} 
		else if (originalAxiom.isOfType(AxiomType.CLASS_ASSERTION)){
			isValid = true;
			preprocessQuery();
		} 
		else{
			isValid = false;
		}
	}
	
	private Ranking module(OWLAxiom axiom, Ranking r) throws OWLOntologyCreationException{
		Set<OWLAxiom> rAxioms = r.getAxioms();OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		SyntacticLocalityModuleExtractor slme = new SyntacticLocalityModuleExtractor(manager, manager.createOntology(rAxioms), ModuleType.STAR);
		Set<OWLAxiom> module = slme.extract(axiom.getSignature());
		ArrayList<Rank> ranks = new ArrayList<Rank>();
		for (Rank rank: r.getRanking()){
			Set<OWLAxiom> intersection = new HashSet<OWLAxiom>();
			for (OWLAxiom a: rank.getAxioms()){
				intersection.add(a);
			}
			intersection.retainAll(module);
			if (!intersection.isEmpty())
				ranks.add(new Rank(new ArrayList<OWLAxiom>(intersection)));
		}
		Rank inf = r.getInfiniteRank();
		Set<OWLAxiom> infAx = new HashSet<OWLAxiom>();
		for (OWLAxiom a: inf.getAxioms()){
			infAx.add(a);
		}
		infAx.retainAll(module);
		Ranking result = new Ranking(ranks);
		result.setInfiniteRank(new Rank(new ArrayList<OWLAxiom>(infAx)));
		return result;
	}
	
	private void preprocessQuery(){
		if (originalAxiom.isOfType(AxiomType.ABoxAxiomTypes)){
			isTBoxQuery = false;
		}
		else{
			isTBoxQuery = true;
		}
		
		if (originalAxiom.isOfType(AxiomType.SUBCLASS_OF)){
			queryReadyAxiom = originalAxiom;
		}		
		else if (originalAxiom.isOfType(AxiomType.EQUIVALENT_CLASSES)){
			if (defeasible){
				 queryReadyAxiom = AxiomManipulator.getDEquivToSubAxiom(originalAxiom);
				 OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)queryReadyAxiom;
				 antecedent = sub.getSubClass();
				consequent = sub.getSuperClass();
			}
			else{
				//Strict
				queryReadyAxiom = originalAxiom;
			}
		}
		else if (originalAxiom.isOfType(AxiomType.DISJOINT_CLASSES)){
			if (defeasible){
				 queryReadyAxiom = AxiomManipulator.getDDisjToSubAxiom(originalAxiom);
				 OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)queryReadyAxiom;
				 antecedent = sub.getSubClass();
				 consequent = sub.getSuperClass();
			}
			else{
				//Strict
				queryReadyAxiom = originalAxiom;
			}
		}
		else if (originalAxiom.isOfType(AxiomType.CLASS_ASSERTION)){
			OWLClassAssertionAxiom clsAssertion = (OWLClassAssertionAxiom)originalAxiom;
			OWLSubClassOfAxiom sub = clsAssertion.asOWLSubClassOfAxiom();
			antecedent = sub.getSubClass();
			consequent = sub.getSuperClass();
			individuals = antecedent.getIndividualsInSignature();
			
			if (individuals.size() == 1){
				OWLNamedIndividual [] s = new OWLNamedIndividual[1];
				individuals.toArray(s);
				individual = s[0];
			}
		}
	}
}
