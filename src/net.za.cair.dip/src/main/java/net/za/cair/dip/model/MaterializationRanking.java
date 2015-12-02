package net.za.cair.dip.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.za.cair.dip.util.ManchesterOWLSyntaxOWLObjectRendererImpl;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;


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

public class MaterializationRanking implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<MaterializationRank> matRanking;
	public ArrayList<OWLClassExpression> defaults;
	public MaterializationRank infiniteRank;
	
	public MaterializationRanking(ArrayList<MaterializationRank> matRanking){
		this.matRanking = matRanking;
		infiniteRank = new MaterializationRank(new ArrayList<OWLClassExpression>());
		defaults = new ArrayList<OWLClassExpression>();
		computeInfiniteRankIndex();
		computeDefaults();
		
	}
	
	public MaterializationRanking(){
		matRanking = new ArrayList<MaterializationRank>();
		infiniteRank = new MaterializationRank(new ArrayList<OWLClassExpression>());
		defaults = new ArrayList<OWLClassExpression>();
	}

	public Set<OWLClassExpression> getMaterializations(){
		Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
		for (MaterializationRank arr: matRanking){
			for (OWLClassExpression exp: arr.getExpressions()){
				result.add(exp);
			}
		}
		return result;
	}
	
	public void setInfiniteRank(MaterializationRank rank){
		infiniteRank = new MaterializationRank(rank.getExpressions());
	}
	
	private void computeInfiniteRankIndex(){
		for (int i = 0; i < matRanking.size(); i++){
			if (matRanking.get(i).isInfiniteRank()) {
			}
		}
	}
	
	private void computeDefaults(){
		defaults = new ArrayList<OWLClassExpression>();
		for (int i = matRanking.size()-1; i >= 0; i--){
			defaults.add(getDefault(i));
		}
	}
	
	private OWLClassExpression getDefault(int index){
		OWLDataFactory dataF = OWLManager.getOWLDataFactory();
		Set<OWLClassExpression> conjuncts = new HashSet<OWLClassExpression>();
		for (int i = index; i >= 0; i--){
			conjuncts.add(matRanking.get(i).getConjunction());
		}
		return dataF.getOWLObjectIntersectionOf(conjuncts);
	}
	
	public void add(MaterializationRank matRank, int index){
		this.matRanking.add(index, matRank);
		
		computeDefaults();
	}
	
	public void add(MaterializationRank matRank){
		this.matRanking.add(matRank);
		
		computeDefaults();
	}
	
	public void remove(int index){
		matRanking.remove(index);
	}
	
	public void remove(){
		//Last element
		int elementIndex = matRanking.size()-1;
		
		//First element
		//int elementIndex = 0;
		
		matRanking.remove(elementIndex);
	}

	public MaterializationRank getElement(int index){
		int reversedIndex = matRanking.size() - (index + 1);
		return matRanking.get(reversedIndex);
	}
	
	public MaterializationRank getElement(){
		//Last element
		int elementIndex = matRanking.size()-1;
		
		//First element
		//int elementIndex = 0;
		
		return getElement(elementIndex);
	}
		
	public ArrayList<MaterializationRank> getMaterializationRanking(){
		return matRanking;
	}
	
	public int size(){
		return matRanking.size();
	}
	
	public int fullSize(){
		if (infiniteRank.size() > 0)
			return matRanking.size()+1;
		else
			return matRanking.size();
	}
	
	public MaterializationRank getInfiniteRank(){
		return infiniteRank;
	}
	
	public OWLClassExpression getInfiniteRankConcept(){
		return infiniteRank.getConjunction();
	}
	
	public OWLClassExpression getDefaultClassExpression(int index){
		Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
		for (int i = index; i < matRanking.size(); i++){
			MaterializationRank tmp = this.getElement(i);
			if (!tmp.isInfiniteRank())
				result.add(tmp.getConjunction());
		}
		OWLDataFactory dataF = OWLManager.getOWLDataFactory();
		return dataF.getOWLObjectIntersectionOf(result);
	}
	
	public String toString(){
		ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		String result = "";
		if (infiniteRank.size() > 0){
			result += "----------------\n";
			result += "Level " + "\u221E" + ":\n";
			result += "----------------\n";
			for (OWLClassExpression cls: infiniteRank.getExpressions()){
				result += man.render(cls) + "\n";
			}
			result += "----------------\n";
		}
		int size = this.matRanking.size()-1;
		for (MaterializationRank rank: this.matRanking){
			result += "----------------\n";
			result += "Level " + size + ":\n";
			result += "----------------\n";
			for (OWLClassExpression cls: rank.getExpressions()){
				result += man.render(cls) + "\n";
			}
			size--;
		}
		result += "\n";
		return result;
	}
	
	public String getConceptRendering(){
		ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		String result = "";
		if (infiniteRank.size() > 0){
			result += "----------------\n";
			result += "Level " + "\u221E" + ":\n";
			result += "----------------\n";
			result += man.render(infiniteRank.getConjunction()) + "\n";
			result += "----------------\n";
		}
		int size = this.matRanking.size()-1;
		for (MaterializationRank rank: this.matRanking){
			result += "----------------\n";
			result += "Level " + size + ":\n";
			result += "----------------\n";
			result += man.render(rank.getConjunction()) + "\n";
			size--;
		}
		result += "\n";
		return result;
	}
	
	/*public String toString(){
		String result = "";
		int count = matRanking.size();
		for (MaterializationRank rank: matRanking){
			if (count == matRanking.size()){
				result += "Rank " + '\u221E' + ":\n";
			}
			else{
				result += "Rank " + count + ":\n";
			}
			count--;
			result += "-------\n";
			result += rank.toString() + "\n";
			result += "\n";
		}
		return result;
	}*/
}
