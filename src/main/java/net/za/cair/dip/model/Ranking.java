package net.za.cair.dip.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;

import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

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

public class Ranking implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Rank> ranking;
	private Rank infiniteRank;
	public String time;
	public int entailmentChecks;
	private Set<OWLAxiom> axiomsWithUninstantiableLHSs;
	private Set<OWLClassExpression> uninstantiableClasses;
	private ArrayList<ArrayList<OWLAxiom>> eTransforms;
	
	public Ranking(ArrayList<Rank> ranking){
		this.axiomsWithUninstantiableLHSs = new HashSet<OWLAxiom>();
		this.uninstantiableClasses = new HashSet<OWLClassExpression>();
		this.eTransforms = new ArrayList<ArrayList<OWLAxiom>>();
		time = "";
		entailmentChecks = 0;
		this.ranking = new ArrayList<Rank>();
		for (Rank r: ranking) {
			Rank tmp = new Rank(r.getAxioms(), r.getIndex());
			this.ranking.add(r.getIndex(), tmp);
		}
		this.infiniteRank = new Rank(new ArrayList<OWLAxiom>());
	}
	
	public Ranking(){
		this.axiomsWithUninstantiableLHSs = new HashSet<OWLAxiom>();
		time = "";
		entailmentChecks = 0;
		this.ranking = new ArrayList<Rank>();
		this.infiniteRank = new Rank(new ArrayList<OWLAxiom>());
		this.eTransforms = new ArrayList<ArrayList<OWLAxiom>>();
	}
	
	public Set<OWLAxiom> getAxioms(){
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		for (Rank rank: this.ranking){
			for (OWLAxiom axiom: rank.getAxioms()){
				result.add(axiom);
			}
		}
		
		result.addAll(this.infiniteRank.getAxioms());
		return result;
	}
	
	public Set<OWLAxiom> getAxiomsMinusInfiniteRank(){
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		for (Rank rank: this.ranking){
			for (OWLAxiom axiom: rank.getAxioms()){
				result.add(axiom);
			}
		}
		
		return result;
	}
	
	public void add(Rank rank, int index){
		ranking.add(index, rank);
	}
	
	public void add(Rank rank){
		ranking.add(rank);
	}
	
	public void remove(int index){
		ranking.remove(index);
	}
	
	public void remove(){
		int elementIndex = ranking.size()-1;
		ranking.remove(elementIndex);
	}
	
	public void setRank(Set<OWLAxiom> axioms, int index){
		ranking.set(index, new Rank(new ArrayList<OWLAxiom>(axioms), index));
	}

	public Rank get(int index){
		return ranking.get(index);
	}
	
	public Rank get(){
		int elementIndex = ranking.size()-1;
		return get(elementIndex);
	}
	
	public MaterializationRanking materializationRanking(){
		ArrayList<MaterializationRank> matRanks = new ArrayList<MaterializationRank>();
		for (Rank rank: this.ranking){
			matRanks.add(rank.materialConversion());
		}
		
		MaterializationRanking tmp = new MaterializationRanking(matRanks);
		tmp.setInfiniteRank(this.infiniteRank.materialConversion());
		return tmp;
	}
	
	public void setInfiniteRank(Rank rank){
		this.infiniteRank = new Rank(new ArrayList<OWLAxiom>());
		this.infiniteRank.setRank(rank.getAxioms());
	}
	
	public Rank getInfiniteRank(){
		return infiniteRank;
	}
		
	public ArrayList<Rank> getRanking(){
		return ranking;
	}
	
	public int size(){
		return ranking.size();
	}
	
	public int fullSize(){
		if (infiniteRank.size() > 0)
			return ranking.size()+1;
		else
			return ranking.size();
	}
	
	public String toString(){
		ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		String result = "";
		if (infiniteRank.size() > 0){
			result += "---------------------\n";
			result += "Level " + "\u221E" + ":\n";
			result += "---------------------\n";
			for (OWLAxiom axiom: infiniteRank.getAxioms())
				result += man.render(axiom) + "\n";
			result += "\n";
		}

		for (Rank rank: this.ranking){
			result += "---------------------\n";
			result += "Level " + rank.getIndex() + ":\n";
			result += "---------------------\n";
			for (OWLAxiom axiom: rank.getAxioms())
				result += man.render(axiom) + "\n";
		}
		result += "\n";
		return result;
	}
	
	public ArrayList<OWLAxiom> getRationalSet(int lastRank){
		ArrayList<OWLAxiom> result = new ArrayList<OWLAxiom>();
		for (int i = ranking.size()-1; i >= lastRank;i--){
			Rank rank = ranking.get(i);
			result.addAll(rank.getAxioms());
		}
		return result;
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
	
	public void setTime(double timing){
		time = "" + timing;
	}
	
	public void setChecks(int checks){
		entailmentChecks = checks;
	}
	
	public void setUninstantiable(Set<OWLAxiom> axioms) {
		axiomsWithUninstantiableLHSs = new HashSet<OWLAxiom>();
		axiomsWithUninstantiableLHSs.addAll(axioms);
	}
	
	public void setUninstantiableClasses(Set<OWLClassExpression> classes) {
		uninstantiableClasses = new HashSet<OWLClassExpression>();
		uninstantiableClasses.addAll(classes);
	}
	
	public Set<OWLAxiom> getUninstantiable() {
		return axiomsWithUninstantiableLHSs;
	}
	
	public Set<OWLClassExpression> getUninstantiableClasses() {
		return uninstantiableClasses;
	}
	
	public void setETransforms(ArrayList<ArrayList<OWLAxiom>> eTransforms) {
		this.eTransforms = removeEmptyRanks(eTransforms);
	}
	
	public ArrayList<ArrayList<OWLAxiom>> getETransforms() {
		return eTransforms;
	}
}
