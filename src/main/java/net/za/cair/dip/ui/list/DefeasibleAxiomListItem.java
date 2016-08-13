package net.za.cair.dip.ui.list;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.za.cair.dip.util.Utility;

import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;

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

public class DefeasibleAxiomListItem extends AbstractOWLFrameSectionRow<OWLOntology, OWLAxiom, OWLAxiom> implements MListItem{
    private OWLAxiom axiom;
    private OWLModelManager modelManager;
    
    /********************* Kody Moodley, CAIR, South Africa ***************************/
    private OWLAnnotationProperty defeasibleAnnotationProperty = getOWLDataFactory().getOWLAnnotationProperty(defeasibleIRI);
    
    public static final Color HOVER_DEFEASIBLE_BUTTON_COLOR = new Color(255,228,225);
    
    public static final Color SELECTED_DEFEASIBLE_BUTTON_COLOR = new Color(204,51,102);
    /**********************************************************************************/
    
    public DefeasibleAxiomListItem(OWLAxiom axiom, OWLModelManager modelManager, OWLEditorKit editorKit) {  
    	super(editorKit,null, modelManager.getActiveOntology(), modelManager.getActiveOntology(), axiom);
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
        return false;
    }

    public void handleEdit() {
    }

    public boolean isDeleteable() {
        return false;
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

	public List<OWLAxiom> getManipulatableObjects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected OWLObjectEditor<OWLAxiom> getObjectEditor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected OWLAxiom createAxiom(OWLAxiom editedObject) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/********************* Kody Moodley, CAIR, South Africa ***************************/
    private List<MListButton> defeasibleButton = new ArrayList<MListButton>();

    public List<MListButton> getAdditionalButtons() {
    	if (!this.isInferred() && (this.axiom.isOfType(AxiomType.SUBCLASS_OF))){
    		if(defeasibleButton.isEmpty()){
    			defeasibleButton.add(new DefeasibleAxiomButton());
    		}
    		return defeasibleButton;
    	}
    	else
    		return Collections.emptyList();
    }

    private static IRI defeasibleIRI = IRI.create("http://cair.za.net/defeasible");
    
    private void toggleDefeasible(OWLAxiom axiom, OWLOntology ontology) {
    	//System.out.println("toggling");
        OWLAxiom toAdd = null;
        if(isDefeasibleAxiom(axiom)) {
        	//System.out.println("is defeasible");
            Set<OWLAnnotation> toRemove = new HashSet<OWLAnnotation>();
            for(OWLAnnotation anno : axiom.getAnnotations()) {
                if(anno.getProperty().equals(defeasibleAnnotationProperty)) {
                    toRemove.add(anno);
                }
            }
            Set<OWLAnnotation> allAnnos = new HashSet<OWLAnnotation>();
            allAnnos.addAll(axiom.getAnnotations());
            allAnnos.removeAll(toRemove);
            toAdd = axiom.getAxiomWithoutAnnotations();
            toAdd = toAdd.getAnnotatedAxiom(allAnnos);
        }
        else {
        	//System.out.println("not defeasible");
            Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>();
            annos.add(getOWLDataFactory().getOWLAnnotation(defeasibleAnnotationProperty, getOWLDataFactory().getOWLLiteral(true)));
            toAdd = axiom.getAnnotatedAxiom(annos);
        }

        
        /*List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        changes.add(new AddAxiom(ontology, toAdd));
        changes.add(new RemoveAxiom(ontology, axiom));*/
        getOWLModelManager().getOWLOntologyManager().addAxiom(getOWLModelManager().getActiveOntology(), toAdd);
        getOWLModelManager().getOWLOntologyManager().removeAxiom(getOWLModelManager().getActiveOntology(), axiom);
       /* try {
			//getOWLModelManager().getOWLOntologyManager().saveOntology(getOWLModelManager().getActiveOntology());
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			System.out.println("cannot save");
			e.printStackTrace();
		}*/
    }

    public boolean isDefeasibleAxiom(OWLAxiom axiom) {
        return !axiom.getAnnotations(defeasibleAnnotationProperty).isEmpty();
    }

    private class DefeasibleAxiomButton extends MListButton {

        private DefeasibleAxiomButton() {
            super("Defeasible", HOVER_DEFEASIBLE_BUTTON_COLOR, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    toggleDefeasible(getAxiom(), getOntology());
                }
            });
        }

        @Override
        public void paintButtonContent(Graphics2D g) {        	
            int x = getBounds().x;
            int y = getBounds().y;
            char[] data = {'d'};            
            g.drawChars(data, 0, data.length, x+5, y+12);           
        }

        @Override
        public Color getBackground() {
            if(isDefeasibleAxiom(getAxiom())) {
                return SELECTED_DEFEASIBLE_BUTTON_COLOR;
            }
            else {
                return Color.LIGHT_GRAY;
            }

        }
    }
    
    /**********************************************************************************/
}
