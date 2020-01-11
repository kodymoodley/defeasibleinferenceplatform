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
	
	public Ranking(ArrayList<Rank> ranking){
		time = "";
		entailmentChecks = 0;
		this.ranking = ranking;
		this.infiniteRank = new Rank(new ArrayList<OWLAxiom>());
	}
	
	public Ranking(){
		time = "";
		entailmentChecks = 0;
		this.ranking = new ArrayList<Rank>();
		this.infiniteRank = new Rank(new ArrayList<OWLAxiom>());
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
			result += "---------------------\n";
		}
		//int size = this.ranking.size()-1;
		for (Rank rank: this.ranking){
			result += "---------------------\n";
			result += "Level " + rank.getIndex() + ":\n";
			result += "---------------------\n";
			for (OWLAxiom axiom: rank.getAxioms())
				result += man.render(axiom) + "\n";
			//result += "-------------\n";
			//size--;
		}
		result += "\n";
		return result;
	}
	
	public ArrayList<OWLAxiom> getRationalSet(int lastRank){
		ArrayList<OWLAxiom> result = new ArrayList<OWLAxiom>();
		for (int i = ranking.size()-1; i >= lastRank;i--){
			//System.out.println(i);
			Rank rank = ranking.get(i);
			//System.out.println(rank);
			result.addAll(rank.getAxioms());
		}
		return result;
	}
	
	public void setTime(double timing){
		time = "" + timing;
	}
	
	public void setChecks(int checks){
		entailmentChecks = checks;
	}
}
