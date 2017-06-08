package net.za.cair.dip.ui.view;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.za.cair.dip.DefeasibleInferenceComputer;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.model.ReasoningType;
import net.za.cair.dip.ui.list.DIPListCellRenderer;
import net.za.cair.dip.ui.list.DIPQueryResultsSection;
import net.za.cair.dip.ui.list.DIPQueryResultsSectionItem;

import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.OWLClassExpressionComparator;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponent;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponentMediator;
import org.protege.editor.owl.ui.view.Copyable;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;


/**
 * Author: Kody Moodley<br>
 * Centre for Artificial Intelligence Research (CAIR)<br>
 * Date: 20-Oct-2015<br><br>
 * <p/>
 * kmoodley@csir.co.za<br>
 * krr.meraka.org.za/~kmoodley<br><br>
 */
public class ResultsList extends MList implements LinkedObjectComponent, Copyable {

    /**
     * 
     */
    private static final long serialVersionUID = 8184853513690586368L;

    private OWLEditorKit owlEditorKit;

    private boolean showSuperClasses;

    private boolean showAncestorClasses;

    private boolean showDescendantClasses;

    private boolean showSubClasses;

    private boolean showInstances;

    private boolean showEquivalentClasses;

    private LinkedObjectComponentMediator mediator;

    private List<ChangeListener> copyListeners = new ArrayList<ChangeListener>();
    
    private ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();


    public ResultsList(OWLEditorKit owlEditorKit) {
        this.owlEditorKit = owlEditorKit;
        setCellRenderer(new DIPListCellRenderer(owlEditorKit));
        mediator = new LinkedObjectComponentMediator(owlEditorKit, this);

        getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                ChangeEvent ev = new ChangeEvent(ResultsList.this);
                for (ChangeListener l : copyListeners){
                    l.stateChanged(ev);
                }
            }
        });
    }


    public boolean isShowAncestorClasses() {
        return showAncestorClasses;
    }


    public void setShowAncestorClasses(boolean showAncestorClasses) {
        this.showAncestorClasses = showAncestorClasses;
    }


    public boolean isShowDescendantClasses() {
        return showDescendantClasses;
    }


    public void setShowDescendantClasses(boolean showDescendantClasses) {
        this.showDescendantClasses = showDescendantClasses;
    }


    public boolean isShowInstances() {
        return showInstances;
    }


    public void setShowInstances(boolean showInstances) {
        this.showInstances = showInstances;
    }


    public boolean isShowSubClasses() {
        return showSubClasses;
    }


    public void setShowSubClasses(boolean showSubClasses) {
        this.showSubClasses = showSubClasses;
    }


    public boolean isShowSuperClasses() {
        return showSuperClasses;
    }


    public void setShowSuperClasses(boolean showSuperClasses) {
        this.showSuperClasses = showSuperClasses;
    }


    public boolean isShowEquivalentClasses() {
        return showEquivalentClasses;
    }


    public void setShowEquivalentClasses(boolean showEquivalentClasses) {
        this.showEquivalentClasses = showEquivalentClasses;
    }


    private List<OWLClass> toSortedList(Set<OWLClass> clses) {
        OWLClassExpressionComparator descriptionComparator = new OWLClassExpressionComparator(owlEditorKit.getModelManager());
        List<OWLClass> list = new ArrayList<OWLClass>(clses);
        Collections.sort(list, descriptionComparator);
        return list;
    }


    public void setOWLClassExpression(OWLClassExpression description, ReasoningType algorithm, Ranking ranking) throws OWLOntologyCreationException {
        
    	List<Object> data = new ArrayList<Object>();
        OWLDataFactory factory = owlEditorKit.getOWLModelManager().getOWLDataFactory();
        //OWLReasoner reasoner = owlEditorKit.getModelManager().getReasoner();
        if (showSuperClasses) {
        	System.out.println("LHS: " + man.render(description));
        	System.out.println();
        	System.out.println("Ranking: ");
        	System.out.println(ranking);
        	System.out.println();
        	DefeasibleInferenceComputer dic = new DefeasibleInferenceComputer(owlEditorKit.getModelManager().getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory(), ranking);
        	OWLClassExpression cc = dic.getCCompatibility(description, algorithm, ranking);
        	dic.computeSuperClasses(description, cc, ranking.getInfiniteRank());
        	final List<OWLClass> results1 = new ArrayList<OWLClass>(dic.getSuperClassesStrict());
        	final List<OWLClass> results2 = new ArrayList<OWLClass>(dic.getSuperClassesTypical());
        	
        	int newSize1 = 0;
        	if (results1.size() > 0){
        		for (OWLClass superClass : results1) {
        			if (!superClass.isOWLThing()){
        				newSize1++;
        			}
        		}
        	}
        	
        	int newSize2 = 0;
        	if (results2.size() > 0){
        		for (OWLClass superClass : results2) {
        			if (!superClass.isOWLThing()){
        				newSize2++;
        			}
        		}
        	}
        	
        	if (newSize1 > 0){
        		data.add(new DIPQueryResultsSection("Strict super classes (" + newSize1 + ")"));
        		for (OWLClass superClass : results1) {
        			if (!superClass.isOWLThing()){
        				data.add(new DIPQueryResultsSectionItem(superClass, factory.getOWLSubClassOfAxiom(description, superClass)));
        			}
        		}
        	}
        	
        	if (newSize2 > 0){
        		data.add(new DIPQueryResultsSection("Typical super classes (" + newSize2 + ")"));
        		for (OWLClass superClass : results2) {
        			if (!superClass.isOWLThing()){
        				data.add(new DIPQueryResultsSectionItem(superClass, factory.getOWLSubClassOfAxiom(description, superClass)));
        			}
        		}
        	}
        	
        }
       // if (showSubClasses) {
            // flatten and filter out owl:Nothing
            /*OWLClass owlNothing = owlEditorKit.getOWLModelManager().getOWLDataFactory().getOWLNothing();
            DefeasibleInferenceComputer dic = new DefeasibleInferenceComputer(owlEditorKit.getModelManager().getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory(), ranking);
        	final List<OWLClass> results = toSortedList(dic.getSubClasses(description, algorithm, ranking));//toSortedList(reasoner.getSuperClasses(description, true).getFlattened());
            data.add(new DIPQueryResultsSection("Typical sub classes (" + results.size() + ")"));
            for (OWLClass subClass : results) {
                data.add(new DIPQueryResultsSectionItem(subClass, factory.getOWLSubClassOfAxiom(subClass, description)));
            }*/
        //}
        setListData(data.toArray());
    }


    protected List<MListButton> getButtons(Object value) {
        /*if (value instanceof DIPQueryResultsSectionItem) {
        	final OWLAxiom axiom = ((DIPQueryResultsSectionItem) value).getAxiom();
        	List<MListButton> buttons = new ArrayList<MListButton>();
        	/*buttons.add(new ExplainButton(new ActionListener() {
            	public void actionPerformed(ActionEvent e) {
            		ExplanationManager em = owlEditorKit.getOWLModelManager().getExplanationManager();
            		em.handleExplain((Frame) SwingUtilities.getAncestorOfClass(Frame.class, ResultsList.this), axiom);
            	}
            }));
           // return buttons;
        }
        else {*/
            return Collections.emptyList();
        //}
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
