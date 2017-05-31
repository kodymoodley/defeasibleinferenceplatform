package net.za.cair.dip.ui.view;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.za.cair.dip.model.Rank;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.ui.list.DIPListCellRenderer;
import net.za.cair.dip.ui.list.DIPQueryResultsSection;
import net.za.cair.dip.ui.list.DIPQueryResultsSectionItem;
import net.za.cair.dip.util.ManchesterOWLSyntaxOWLObjectRendererImpl;

import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponent;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponentMediator;
import org.protege.editor.owl.ui.view.Copyable;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;


/**
 * Author: Kody Moodley<br>
 * Centre for Artificial Intelligence Research (CAIR)<br>
 * Date: 20-Oct-2015<br><br>
 * <p/>
 * kmoodley@csir.co.za<br>
 * krr.meraka.org.za/~kmoodley<br><br>
 */
public class ExceptionsList extends MList implements LinkedObjectComponent, Copyable {

    /**
     * 
     */
    private static final long serialVersionUID = 8184853513690586368L;

    private OWLEditorKit owlEditorKit;

    private LinkedObjectComponentMediator mediator;

    private List<ChangeListener> copyListeners = new ArrayList<ChangeListener>();

    private ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	

    public ExceptionsList(OWLEditorKit owlEditorKit) {
        this.owlEditorKit = owlEditorKit;
        //setCellRenderer(new IndividualsRenderer(owlEditorKit));
        
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


    public void setExceptionsList(Ranking ranking) throws OWLOntologyCreationException {
        List<Object> data = new ArrayList<Object>();
        
        //System.out.println(ranking);
        int rankIdx = 1;
        Iterator<Rank> rIter = ranking.getRanking().iterator();
        boolean done = false;
        while (rIter.hasNext() && !done){
        	if (rankIdx == ranking.size()){
        		done = true;
        	}
        	else{
        		Rank rank = rIter.next();
        		data.add(new DIPQueryResultsSection("Level " + rankIdx));
        		Set<OWLClassExpression> lhss = new HashSet<OWLClassExpression>();
        		for (OWLAxiom a : rank.getAxioms()) {
        			OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
        			lhss.add(sub.getSubClass());
        			
        		}
        		for (OWLClassExpression c : lhss) {
        			data.add(new DIPQueryResultsSectionItem(c));
        		}
        		rankIdx++;
        	}
        	
        }
       
        setListData(data.toArray());
    }


    protected List<MListButton> getButtons(Object value) {
    	return Collections.emptyList();
       /* if (value instanceof DIPQueryResultsSectionItem) {
        	final OWLAxiom axiom = ((DIPQueryResultsSectionItem) value).getAxiom();
        	List<MListButton> buttons = new ArrayList<MListButton>();
        	buttons.add(new ExplainButton(new ActionListener() {
            	public void actionPerformed(ActionEvent e) {
            		ExplanationManager em = owlEditorKit.getOWLModelManager().getExplanationManager();
            		em.handleExplain((Frame) SwingUtilities.getAncestorOfClass(Frame.class, ExceptionsList.this), axiom);
            	}
            }));
            return buttons;
        }
        else {
            return Collections.emptyList();
        }*/
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
}
