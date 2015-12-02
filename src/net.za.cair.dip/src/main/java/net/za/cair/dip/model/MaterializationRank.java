package net.za.cair.dip.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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

public class MaterializationRank implements Serializable{
	/**
	 * 
	 */
	//private static DLSyntaxRenderer dlSyntaxRenderer = new DLSyntaxRenderer();
	private static final long serialVersionUID = 1L;
	private ArrayList<OWLClassExpression> matRank;
	private int index;
	private boolean isInfiniteRank;
	
	public MaterializationRank(ArrayList<OWLClassExpression> matRank, int index){
		this.matRank = matRank;
		this.index = index;
		this.isInfiniteRank = false;
	}
	
	public MaterializationRank(ArrayList<OWLClassExpression> matRank){
		this.matRank = matRank;
		this.isInfiniteRank = false;
		index = -1;
	}
	
	public void setRank(ArrayList<OWLClassExpression> matRank){
		this.matRank = matRank;
	}
	
	public int getIndex(){
		return index;
	}
	
	public ArrayList<OWLClassExpression> getExpressions(){
		return matRank;
	}
	
	public int size(){
		return matRank.size();
	}
	
	public OWLClassExpression getConjunction(){
		OWLDataFactory df = OWLManager.getOWLDataFactory();
		Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
		result.addAll(matRank);
		return df.getOWLObjectIntersectionOf(result); 
	}
	
	/*public String toString(){
		return dlSyntaxRenderer.render(getConjunction());		
	}*/
	
	public void setInfinite(boolean value){
		isInfiniteRank = value;
	}
	
	public boolean isInfiniteRank(){
		return isInfiniteRank;
	}
	
	/*private void writeObject(java.io.ObjectOutputStream out) throws IOException{
		out.writeObject(rank);
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
		in.readObject();
	}
	
	private void readObjectNoData() throws ObjectStreamException{
		 System.out.println("No data to read!");   	 
	}*/
}