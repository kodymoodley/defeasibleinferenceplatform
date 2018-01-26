package net.za.cair.dip.ui.list;

import net.za.cair.dip.model.Rank;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.model.ReasoningType;
import net.za.cair.dip.ui.renderer.OWLAxiomsCellRenderer;
import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.*;

import java.awt.*;
import java.util.*;
import java.util.List;

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
 * UKZN and CSIR<br>
 * Date: 10-Oct-2011<br><br>
 */

public class RankingAxiomsList extends MList {
	private static final long serialVersionUID = 8151570315302342198L;
	public static final Color AXIOM_ROW_COLOR = new Color(255, 228, 225);   
	private static final Color DEFAULT_ROW_COLOR = new Color(240, 245, 240);
    private OWLModelManager modelManager;
	private List<Object> data;
	private OWLEditorKit editorKit;
    
	public RankingAxiomsList(OWLEditorKit owlEditorKit, OWLModelManager manager) {  	
    	this.editorKit = owlEditorKit;
    	data = new ArrayList<Object>();
    	modelManager = manager;
    	setCellRenderer(new OWLAxiomsCellRenderer(editorKit));           
    }        
    
    protected Color getItemBackgroundColor(MListItem item) {
    	try{
    		RankingAxiomsListItem tmp = (RankingAxiomsListItem)item;
    		if (tmp.isDefeasible())
    			return AXIOM_ROW_COLOR;
    		else
    			return DEFAULT_ROW_COLOR;
    	}
    	catch (Exception e){
    		System.out.println("Casting exception");
    	}
    	return AXIOM_ROW_COLOR;
    }    
    
	public void clear(){
		data = new ArrayList<Object>();    		    							    			            
		setListData(data.toArray());       
    }
    
    @SuppressWarnings({ })
	public void displayAxiomRanking(Ranking ranking, OWLClassExpression query, ReasoningType type, boolean defeasible){
    	data = new ArrayList<Object>();
    	data.add(new RankingAxiomsListSection(type.getName()));
    	if (defeasible){
			data.add(new RankingAxiomsListSection("Query: " + modelManager.getRendering(query) + " (d)"));
		}
		else{
			data.add(new RankingAxiomsListSection("Query: " + modelManager.getRendering(query) + " (s)"));
			data.add(new RankingAxiomsListSection("Ranking not applicable for strict queries"));
		}
    	
    	if (ranking != null && ranking.fullSize() > 0){
    		int count = ranking.size();
    		
    		if (ranking.getInfiniteRank().size() > 0){
    			data.add(new RankingAxiomsListSection("Level " + "\u221E" + ":"));
    			for (OWLAxiom axiom: ranking.getInfiniteRank().getAxioms()){
    				data.add(new RankingAxiomsListItem(axiom, modelManager, editorKit));
    			}
    		}
    		
    		for (Rank dBoxRank: ranking.getRanking()){
    			data.add(new RankingAxiomsListSection("Level " + count + ":"));
    			for (OWLAxiom axiom: dBoxRank.getAxioms()){
    				data.add(new RankingAxiomsListItem(axiom, modelManager, editorKit));
    			}
    			count--;
    		}
    	}
    	
    	setListData(data.toArray());	   
    }
        
    protected List<MListButton> getButtons(Object value) {
    	return new ArrayList<MListButton>();
    }
}
