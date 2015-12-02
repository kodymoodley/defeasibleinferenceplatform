package net.za.cair.dip.ui.list;

import net.za.cair.dip.util.Utility;

import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLAxiom;

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

public class RankingAxiomsListItem implements MListItem{
    private OWLAxiom axiom;
    private OWLModelManager modelManager;
    
    public RankingAxiomsListItem(OWLAxiom axiom, OWLModelManager modelManager, OWLEditorKit editorKit) {  
    	this.modelManager = modelManager;    	
        this.axiom = axiom;
    }            

    public OWLObject getOWLObject() {
        return axiom;
    }
    
    public OWLAxiom getOWLAxiom() {
        return axiom;
    }

    @Override
	public String toString() {
    	return modelManager.getRendering(axiom);
    }

    public boolean isEditable() {
        return true;
    }

    public void handleEdit() {
    }

    public boolean isDeleteable() {
        return true;
    }

    public boolean handleDelete(){    	    
    	return true;
    }

    public String getTooltip() {
        return null;
    }
    
    public boolean isDefeasible(){
    	Utility u = new Utility();
    	return u.isDefeasible(axiom);
    }
}
