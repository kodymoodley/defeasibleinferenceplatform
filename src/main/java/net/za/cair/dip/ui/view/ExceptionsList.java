package net.za.cair.dip.ui.view;

import net.za.cair.dip.model.Rank;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.ui.list.DIPListCellRenderer;
import net.za.cair.dip.ui.list.DIPQueryResultsSection;
import net.za.cair.dip.ui.list.DIPQueryResultsSectionItem;
import net.za.cair.dip.util.Utility;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponent;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponentMediator;
import org.protege.editor.owl.ui.view.Copyable;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;


/**
 * Author: Kody Moodley<br>
 * Centre for Artificial Intelligence Research (CAIR)<br>
 * Date: 20-Oct-2015<br><br>
 * <p/>
 * kmoodley@csir.co.za<br>
 * krr.meraka.org.za/~kmoodley<br><br>
 */

public class ExceptionsList extends MList implements LinkedObjectComponent, Copyable {
	private static final long serialVersionUID = 8184853513690586368L;
	private LinkedObjectComponentMediator mediator;
	private List<ChangeListener> copyListeners = new ArrayList<ChangeListener>();
	private ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	private Set<OWLAxiom> ontologyAxioms;
	private OWLDataFactory dataF = new OWLDataFactoryImpl();
	public static final Color AXIOM_ROW_COLOR = new Color(209, 233, 255);   
	private static final Color DEFAULT_ROW_COLOR = new Color(240, 245, 240);
	
	public ExceptionsList(OWLEditorKit owlEditorKit, OWLDataFactory df, OWLReasonerFactory reasonerFactory) {
		this.ontologyAxioms = owlEditorKit.getOWLModelManager().getActiveOntology().getAxioms();
		setCellRenderer(new DIPListCellRenderer(owlEditorKit));
		mediator = new LinkedObjectComponentMediator(owlEditorKit, this);

		getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				ChangeEvent ev = new ChangeEvent(ExceptionsList.this);
				for (ChangeListener l : copyListeners){
					l.stateChanged(ev);
				}
			}
		});
	}
	
	public void clear() {
		List<Object> data = new ArrayList<Object>();
		setListData(data.toArray());
	}
	
	public void setExceptionsList(Ranking ranking) throws OWLOntologyCreationException {		
		List<Object> data = new ArrayList<Object>();
		Utility u = new Utility();
		//Ranking newRanking = rhc.enrichRankingWithStrictInclusions(ranking, ontologyAxioms, reasonerFactory);
		ArrayList<Rank> ranks = ranking.getRanking();
		
		// Display the ranked exceptions list (the normal exceptions)
		if (ranking.size() > 1) { // If there are exceptions
			for (Rank r: ranks) { 
				if (r.getIndex() > 0) { // Skip non-exceptions (the first rank)
					data.add(new DIPQueryResultsSection("Level " + (r.getIndex())));
					Set<OWLClassExpression> lhss = new HashSet<OWLClassExpression>();
					
					for (OWLAxiom a : r.getAxioms()) {
						OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
						lhss.add(sub.getSubClass());
					}
					
					for (OWLClassExpression c : lhss) {
						data.add(new DIPQueryResultsSectionItem(c));
					}
				}
			}
		}
		
		Set<OWLClassExpression> uninstant = new HashSet<OWLClassExpression>();
		// If there are total exceptions
		data.add(new DIPQueryResultsSection("Uninstantiable"));
		int numStrictExceptions = 0;
		if (ranking.getInfiniteRank().size() > 0) {
			for (OWLAxiom a : ranking.getInfiniteRank().getAxioms()) {
				
				// If there are defeasible subclass axioms in the infinite rank,
				// it means that they filtered into it by repeatedly applying
				// the exceptionality procedure
				if (a.isOfType(AxiomType.SUBCLASS_OF) && u.isDefeasible(a)) {					
					OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
					OWLClassExpression lhs = sub.getSubClass();
					uninstant.add(lhs);
					//data.add(new DIPQueryResultsSectionItem(lhs));
					numStrictExceptions++;
				}    			
			}
		}
		
		for (OWLClassExpression c: ranking.getUninstantiableClasses()) {
			uninstant.add(c);
			//System.out.println("GOTSOMEKODY");
			//man.render(c);
			numStrictExceptions++;
			//data.add(new DIPQueryResultsSectionItem(c));
		}
		
		for (OWLClassExpression cls: uninstant) {
			data.add(new DIPQueryResultsSectionItem(cls));
		}

		// Display all the totally exceptional LHS classes
		if (numStrictExceptions == 0) {
			data.remove(data.size()-1);
		}

		setListData(data.toArray());
	}


	protected List<MListButton> getButtons(Object value) {
		return Collections.emptyList();
	}


	public JComponent getComponent() {
		return this;
	}


	public OWLObject getLinkedObject() {
		return mediator.getLinkedObject();
	}


	public Point getMouseCellLocation() {
		Rectangle r = getMouseCellRect();
		if (r == null) {
			return null;
		}
		Point mousePos = getMousePosition();
		if (mousePos == null) {
			return null;
		}
		return new Point(mousePos.x - r.x, mousePos.y - r.y);
	}


	public Rectangle getMouseCellRect() {
		Point mousePos = getMousePosition();
		if (mousePos == null) {
			return null;
		}
		int sel = locationToIndex(mousePos);
		if (sel == -1) {
			return null;
		}
		return getCellBounds(sel, sel);
	}


	public void setLinkedObject(OWLObject object) {
		mediator.setLinkedObject(object);
	}


	public boolean canCopy() {
		return getSelectedIndices().length > 0;
	}


	public List<OWLObject> getObjectsToCopy() {
		List<OWLObject> copyObjects = new ArrayList<OWLObject>();
		for (Object sel : getSelectedValues()){
			if (sel instanceof DIPQueryResultsSectionItem){
				copyObjects.add(((DIPQueryResultsSectionItem)sel).getOWLObject());
			}
		}
		return copyObjects;
	}


	public void addChangeListener(ChangeListener changeListener) {
		copyListeners.add(changeListener);
	}


	public void removeChangeListener(ChangeListener changeListener) {
		copyListeners.remove(changeListener);
	}
	
	protected Color getItemBackgroundColor(MListItem item) {
    	return AXIOM_ROW_COLOR;
    } 
}
