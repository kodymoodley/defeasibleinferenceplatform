package net.za.cair.dip.ui.list;

import net.za.cair.dip.ui.renderer.OWLAxiomsCellRenderer;
import net.za.cair.dip.util.Utility;

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

public class StrictAxiomList extends MList {
	private static final long serialVersionUID = 8151570315302342198L;
	public static final Color AXIOM_ROW_COLOR = new Color(255, 228, 225);   
	private static final Color DEFAULT_ROW_COLOR = new Color(240, 245, 240);
    private OWLModelManager modelManager;
	private OWLEditorKit editorKit;
    
	public StrictAxiomList(OWLEditorKit owlEditorKit, OWLModelManager manager) {  	
    	editorKit = owlEditorKit;
    	modelManager = manager;
    	setCellRenderer(new OWLAxiomsCellRenderer(editorKit));           
    }   
	
	public void setAxioms(Set<OWLAxiom> axiomSet) {
		Utility u = new Utility();
		System.out.println("stric:");
        List<Object> items = new ArrayList<Object>();
        Set<String> axioms = new HashSet<String>();
        for (OWLAxiom axiom : axiomSet) {
        	if (!axioms.contains(u.toString(axiom))) {
        		axioms.add(u.toString(axiom));
        		u.println(axiom);
        		items.add(new StrictAxiomListItem(axiom, modelManager, editorKit));
        	}
        }
        System.out.println();
        setListData(items.toArray());
    }
    
    protected Color getItemBackgroundColor(MListItem item) {
    	try{
    		StrictAxiomListItem tmp = (StrictAxiomListItem)item;
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
	
    protected List<MListButton> getButtons(Object value) {
    	StrictAxiomListItem d = (StrictAxiomListItem)value;
    	return d.getAdditionalButtons();
    }
    
}
