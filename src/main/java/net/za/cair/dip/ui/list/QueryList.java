package net.za.cair.dip.ui.list;

import net.za.cair.dip.ui.renderer.OWLAxiomsCellRenderer;
import net.za.cair.dip.util.Utility;

import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.core.ui.list.MListSectionHeader;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.*;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

import javax.swing.ListSelectionModel;

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

public class QueryList extends MList {
	private static final long serialVersionUID = 8151570315302342198L;
	public static final Color AXIOM_ROW_COLOR = new Color(255, 228, 225);   
	private static final Color DEFAULT_ROW_COLOR = new Color(240, 245, 240);
    public OWLModelManager modelManager;
	private OWLEditorKit editorKit;
	public static final Color HOVER_DEFEASIBLE_BUTTON_COLOR = new Color(255,228,225); 
    public static final Color SELECTED_DEFEASIBLE_BUTTON_COLOR = new Color(204,51,102);
    
	public QueryList(OWLEditorKit owlEditorKit, OWLModelManager manager) {  	
    	editorKit = owlEditorKit;
    	modelManager = manager;
    	setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	setCellRenderer(new OWLAxiomsCellRenderer(editorKit));           
    }   
	
	public void setAxioms(Set<OWLAxiom> axiomSet) {
        List<Object> items = new ArrayList<Object>();
        for (OWLAxiom axiom : axiomSet) {
        	items.add(new QueryListItem(axiom, modelManager, editorKit));
        }
        setListData(items.toArray());
        setFixedCellHeight(24);
    }
    
    protected Color getItemBackgroundColor(MListItem item) {
    	try{
    		QueryListItem tmp = (QueryListItem)item;
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
		setListData((new ArrayList<Object>()).toArray());       
    }

	public OWLAxiom getSelectedAxiom(){
        for (Object o : getSelectedValues()){
            if (o != null){
            	QueryListItem qli = (QueryListItem)o;
            	return qli.getOWLAxiom();
            }
        }
        return null;
    }
	
	@Override
    protected List<MListButton> getButtons(Object value) {
		if (value instanceof MListSectionHeader) {
			return getSectionButtons((MListSectionHeader) value);
		} else if (value instanceof MListItem) {
			List<MListButton> buttons = new ArrayList<MListButton>();
			buttons.addAll(this.getListItemButtons((MListItem) value));
			buttons.add(new CurrentQueryButton());
			return buttons;
		} else {
			return Collections.emptyList();
		}
	}
	
	private class CurrentQueryButton extends MListButton {

        private CurrentQueryButton() {
            super("Query", HOVER_DEFEASIBLE_BUTTON_COLOR, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	OWLAxiom selectedAxiom = getSelectedAxiom(); 
        			if (selectedAxiom != null){
        				OWLModelManager m = modelManager;
        				for (OWLAnnotationAssertionAxiom anno: m.getActiveOntology().getAnnotationAssertionAxioms(Utility.rampSelectedQueryIRI)){
        					m.getOWLOntologyManager().removeAxiom(m.getActiveOntology(), anno);
        				}
        			
        				OWLDataFactory df = m.getOWLDataFactory();
        				OWLAnnotationAssertionAxiom axiom = null;
        				Utility u = new Utility();
        				if (u.isDefeasible(selectedAxiom))
        					axiom = df.getOWLAnnotationAssertionAxiom(Utility.rampSelectedQueryProperty, Utility.rampSelectedQueryProperty.getIRI(), df.getOWLLiteral(u.addColon(m.getRendering(selectedAxiom))+"*",""));
        				else
        					axiom = df.getOWLAnnotationAssertionAxiom(Utility.rampSelectedQueryProperty, Utility.rampSelectedQueryProperty.getIRI(), df.getOWLLiteral(u.addColon(m.getRendering(selectedAxiom)),""));
        				
        				m.getOWLOntologyManager().addAxiom(m.getActiveOntology(), axiom);
        			}
                }
            });
        }

        @Override
        public void paintButtonContent(Graphics2D g) {        	
            int x = getBounds().x;
            int y = getBounds().y;
            char[] data = {'q'};            
            g.drawChars(data, 0, data.length, x+5, y+12);           
        }

        @Override
        public Color getBackground() {
           return Color.LIGHT_GRAY;
        }
    }
}
