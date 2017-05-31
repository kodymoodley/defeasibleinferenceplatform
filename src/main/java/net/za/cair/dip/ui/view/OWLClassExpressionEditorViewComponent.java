package net.za.cair.dip.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import net.za.cair.dip.model.Rank;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.model.ReasoningType;
import net.za.cair.dip.transform.RationalRankingAlgorithm;
import net.za.cair.dip.util.ManchesterOWLSyntaxOWLObjectRendererImpl;
import net.za.cair.dip.util.Utility;



//import org.apache.log4j.Logger;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.owl.model.cache.OWLExpressionUserCache;
import org.protege.editor.owl.model.entity.OWLEntityCreationSet;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.inference.OWLReasonerManager;
import org.protege.editor.owl.model.inference.ReasonerUtilities;
import org.protege.editor.owl.ui.CreateDefinedClassPanel;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Author: Kody Moodley<br>
 * Centre for Artificial Intelligence Research (CAIR)<br>
 * Date: 20-Oct-2015<br><br>
 * <p/>
 * kmoodley@csir.co.za<br>
 * krr.meraka.org.za/~kmoodley<br><br>
 */
public class OWLClassExpressionEditorViewComponent extends AbstractOWLViewComponent {
    private static final long serialVersionUID = 8268241587271333587L;

    //Logger log = Logger.getLogger(OWLClassExpressionEditorViewComponent.class);

    private ExpressionEditor<OWLClassExpression> owlDescriptionEditor;

    private ResultsList resultsList;
    private ExceptionsList exceptionsList;


    //private JCheckBox showSuperClassesCheckBox;
    
    private JComboBox reasoningList; 
    
    //private JComboBox roleList; 

    //private JCheckBox showSubClassesCheckBox;

    private JButton executeButton;
    private JButton refreshButton;
    //private JButton addButton;
    
    private OWLOntologyChangeListener ontListener;

    //private OWLModelManagerListener listener;

    //private boolean requiresRefresh = false;
    
    private Ranking ranking;
    
    private ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	
    
    protected void initialiseOWLView() throws Exception {
        setLayout(new BorderLayout(10, 10));

        JComponent editorPanel = createQueryPanel();
        JComponent resultsPanel = createResultsPanel();
        JComponent exceptionsPanel = createExceptionsPanel();
        
        //JComponent optionsBox = createOptionsBox();
        resultsPanel.add(exceptionsPanel, BorderLayout.WEST);
        //resultsPanel.add(optionsBox, BorderLayout.EAST);
        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorPanel, resultsPanel);
        splitter.setDividerLocation(0.3);

        add(splitter, BorderLayout.CENTER);

        //updateGUI();
        
        
        loadRanking();
        
        /*listener = new OWLModelManagerListener() {
            public void handleChange(OWLModelManagerChangeEvent event) {
                if (event.isType(EventType.ONTOLOGY_SAVED)){
                	saveRanking();
                }
            }
        };*/
        
        /*ontListener = new OWLOntologyChangeListener() {
            
			@Override
			public void ontologiesChanged(List<? extends OWLOntologyChange> changes)
					throws OWLException {
				boolean logicalChange = false;
				for (OWLOntologyChange ch: changes){
					if (ch.isAddAxiom() || ch.isRemoveAxiom() || ch.isAxiomChange()){
						if (ch.getAxiom().isLogicalAxiom())
							logicalChange = true;
					}
					
				}
				
				if (logicalChange){
					
				}
				// TODO Auto-generated method stub
				
			}
        };

        //getOWLModelManager().addListener(listener);
        getOWLModelManager().addOntologyChangeListener(ontListener);

        /*addHierarchyListener(new HierarchyListener(){
            public void hierarchyChanged(HierarchyEvent event) {
                if (requiresRefresh && isShowing()){
                    //doQuery();
                }
            }
        });*/
    }

    private JComponent createQueryPanel() {
        JPanel editorPanel = new JPanel(new BorderLayout());

        final OWLExpressionChecker<OWLClassExpression> checker = getOWLModelManager().getOWLExpressionCheckerFactory().getOWLClassExpressionChecker();
        owlDescriptionEditor = new ExpressionEditor<OWLClassExpression>(getOWLEditorKit(), checker);
        owlDescriptionEditor.addStatusChangedListener(new InputVerificationStatusChangedListener(){
            public void verifiedStatusChanged(boolean newState) {
                executeButton.setEnabled(newState);
                //addButton.setEnabled(newState);
            }
        });
        owlDescriptionEditor.setPreferredSize(new Dimension(100, 50));

        editorPanel.add(ComponentFactory.createScrollPane(owlDescriptionEditor), BorderLayout.CENTER);
        JPanel buttonHolder = new JPanel(new FlowLayout(FlowLayout.LEFT));
        executeButton = new JButton(new AbstractAction("Execute") {
            /**
             * 
             */
            private static final long serialVersionUID = -1833321282125901561L;

            public void actionPerformed(ActionEvent e) {
                doQuery();
            }
        });

        /*addButton = new JButton(new AbstractAction("Save"){
            /**
             * 
             */
            /*private static final long serialVersionUID = -6050625862820344594L;

            public void actionPerformed(ActionEvent event) {
                doAdd();
            }
        });*/
        
        //Object [] roles = getOWLModelManager().getActiveOntology().getObjectPropertiesInSignature().toArray();
        
        //ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        /*roleList = new JComboBox();
        if (roles.length > 0){
        String [] roleStr = new String[roles.length];
        
        for (int i = 0; i < roles.length;i++){
        	roleStr[i] = man.render((OWLObject)roles[i]);
        }*/
        
        //roleList = new JComboBox(roleStr);
        //}
        /*roleList.setRenderer(new BasicComboBoxRenderer(){
    		private static final long serialVersionUID = 3125429064480964846L;

			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    			setText(man.render((OWLObject)value)); 	
    			return this;
    		}  
    	});*/
        
        buttonHolder.add(executeButton);
        
        /** List of Defeasible Reasoning Algorithms **/
        reasoningList = new JComboBox(ReasoningType.REASONING_TYPE_NAMES);
    	reasoningList.setRenderer(new BasicComboBoxRenderer(){
    		/**
			 * 
			 */
			private static final long serialVersionUID = 3125429064480964846L;

			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    			setText(value.toString()); 	
    			return this;
    		}  
    	});
    	
    	buttonHolder.add(reasoningList);
    	//buttonHolder.add(roleList);
         
        editorPanel.add(buttonHolder, BorderLayout.SOUTH);
        editorPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
                Color.LIGHT_GRAY), "Query (class expression)"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        return editorPanel;
    }

    //private JComponent createExceptionsPanel() {
        /*JComponent resultsPanel = new JPanel(new BorderLayout(10, 10));
        //resultsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
        //        Color.LIGHT_GRAY), "Query results"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        resultsList = new ExceptionsList(getOWLEditorKit());
        resultsList.setShowSubClasses(true);
        resultsPanel.add(ComponentFactory.createScrollPane(resultsList));
        return resultsPanel;*/
    //}


    private JComponent createResultsPanel() {
        JComponent resultsPanel = new JPanel(new BorderLayout(10, 10));
        resultsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
                Color.LIGHT_GRAY), "Results"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        resultsList = new ResultsList(getOWLEditorKit());
        resultsList.setShowSuperClasses(true);
        //resultsList.setShowSubClasses(true);
        JComponent mainResultPanel = new JPanel(new GridLayout(1,1));
        mainResultPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
                Color.LIGHT_GRAY), "Defeasible Super Classes"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        mainResultPanel.add(ComponentFactory.createScrollPane(resultsList));
        
        resultsPanel.add(mainResultPanel, BorderLayout.CENTER);
        return resultsPanel;
    }
    
    private JComponent createExceptionsPanel() {
        JComponent exceptionsPanel = new JPanel(new BorderLayout(10, 10));
        exceptionsPanel.setPreferredSize(new Dimension(200,500));
        exceptionsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
                Color.LIGHT_GRAY), "Exceptions"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        exceptionsList = new ExceptionsList(getOWLEditorKit());
        exceptionsPanel.add(ComponentFactory.createScrollPane(exceptionsList));
        //JPanel buttonHolder = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshButton = new JButton(new AbstractAction("Refresh") {
            /**
             * 
             */
            private static final long serialVersionUID = -1833321282125901561L;

            public void actionPerformed(ActionEvent e) {
                computeRanking();
                saveRanking();
            }
        });
        
        exceptionsPanel.add(refreshButton, BorderLayout.SOUTH);
        return exceptionsPanel;
    }


    /*private JComponent createOptionsBox() {
        Box optionsBox = new Box(BoxLayout.Y_AXIS);
        showSuperClassesCheckBox = new JCheckBox(new AbstractAction("Super classes") {
            /**
             * 
             */
            /*private static final long serialVersionUID = 1531417504526875891L;

            public void actionPerformed(ActionEvent e) {
                resultsList.setShowSuperClasses(showSuperClassesCheckBox.isSelected());
                doQuery();
            }
        });
        optionsBox.add(showSuperClassesCheckBox);
        optionsBox.add(Box.createVerticalStrut(3));

        showSubClassesCheckBox = new JCheckBox(new AbstractAction("Subclasses") {
            private static final long serialVersionUID = 696913194074753412L;

            public void actionPerformed(ActionEvent e) {
                resultsList.setShowSubClasses(showSubClassesCheckBox.isSelected());
                doQuery();
            }
        });
        optionsBox.add(showSubClassesCheckBox);
        optionsBox.add(Box.createVerticalStrut(3));

        return optionsBox;
    }*/


    protected void disposeOWLView() {
        //getOWLModelManager().removeListener(listener);
        getOWLModelManager().removeOntologyChangeListener(ontListener);
        
    }


    /*private void updateGUI() {
        showSuperClassesCheckBox.setSelected(resultsList.isShowSuperClasses());
        showSubClassesCheckBox.setSelected(resultsList.isShowSubClasses());
    }*/

    private void loadRanking() {
    	//System.out.println("loading ranking");
    	OWLOntology theOntology = getOWLModelManager().getActiveOntology();
    	Set<OWLAxiom> logicalAxioms = new HashSet<OWLAxiom>(theOntology.getLogicalAxioms());
    	
    	Set<OWLAxiom> strictAxioms = new HashSet<OWLAxiom>();
    	Set<OWLAxiom> rankedAxioms = new HashSet<OWLAxiom>();
    	
    	Utility u = new Utility();
    	Iterator<OWLAxiom> iter = logicalAxioms.iterator();
    	
    	boolean done = false;
    	while (iter.hasNext() && !done){
    		OWLAxiom tmp = iter.next();
    		if (u.isDefeasible(tmp)){
    			if (!u.hasRankAnnotation(tmp)){
    				done = true;
    			}
    			else{
    				//core case
    				rankedAxioms.add(tmp);
    			}
    		}
    		else{
    			strictAxioms.add(tmp);
    		}
    	}
    	
    	if (rankedAxioms.isEmpty()){//NO EXISTING/STORED RANKING
    		computeRanking(); 
    		saveRanking();
    	}
    	else{ //THERE IS A STORED RANKING
    		ArrayList<Rank> ranks = new ArrayList<Rank>();
    	
    		int lowest = theOntology.getAxiomCount();
    		int highest = -1;
    		for (OWLAxiom axiom: rankedAxioms){
    			int idx = u.getRankIndex(axiom);
    			if (idx < lowest)
    				lowest = idx;
    			if (idx > highest)
    				highest = idx;
    		
    			ranks.add(new Rank(new ArrayList<OWLAxiom>(Collections.singleton(axiom)), idx));
    		}
    	
    		ranking = u.mergeRanks(ranks, lowest, highest);
    		if (ranking.getAxioms().isEmpty()){
    			computeRanking();
    			saveRanking();
    		}
    		else{
    			ranking.setInfiniteRank(new Rank(new ArrayList<OWLAxiom>(strictAxioms)));
    			saveRanking();
    		}
    	}
    	
    	try {
			exceptionsList.setExceptionsList(ranking);
		} 
    	catch (OWLOntologyCreationException e) {
			System.out.println("Exception caught trying to display exceptions");
			e.printStackTrace();
		}
    }
    
    private void saveRanking() {
    	OWLOntologyManager man = getOWLModelManager().getOWLOntologyManager();
    	OWLOntology theOntology = getOWLModelManager().getActiveOntology();
    	Utility u = new Utility();
    	int count = 0;
    	for (Rank rank: ranking.getRanking()){
    		for (OWLAxiom a: rank.getAxioms()){
    			Set<OWLAnnotation> annos = a.getAnnotations();
    			Set<OWLAnnotation> newAnnos = new HashSet<OWLAnnotation>();
    			for (OWLAnnotation an: annos){
    				if (!an.getProperty().equals(u.rankAnnotationProperty)){
    					newAnnos.add(an);
    				}
    			}
    			
    			
    			OWLAxiom aTmp = a.getAxiomWithoutAnnotations();
    			OWLAnnotation anno = getOWLModelManager().getOWLDataFactory().getOWLAnnotation(u.rankAnnotationProperty, getOWLModelManager().getOWLDataFactory().getOWLLiteral(count));
    			newAnnos.add(anno);
    			
    			man.removeAxiom(theOntology, a);
    			man.addAxiom(theOntology, aTmp.getAnnotatedAxiom(newAnnos));
    			
    		}
    		count++;
    	}
    }
    
    private void doQuery() {
    	//System.out.println("Doing query");
        //if (isShowing()){
        	//System.out.println("isShowing");
            try {
            	OWLReasonerManager reasonerManager = getOWLModelManager().getOWLReasonerManager();
            	//ReasonerUtilities.warnUserIfReasonerIsNotConfigured(this, reasonerManager);

                OWLClassExpression desc = owlDescriptionEditor.createObject();
                if (desc != null){
                    OWLExpressionUserCache.getInstance(getOWLModelManager()).add(desc, owlDescriptionEditor.getText());
                    ReasoningType algorithm = ReasoningType.NAME_TYPE_MAP.get((String)reasoningList.getSelectedItem());
                    resultsList.setOWLClassExpression(desc, algorithm, ranking);
                }
            }
            catch (OWLException e) {
            	System.out.println("Exception caught trying to do the query.");
            	e.printStackTrace();
            }
           // requiresRefresh = false;
        //}
        //else{
        //    requiresRefresh = true;
        //}
    }
    
    private void computeRanking() {
    	//System.out.println("computing ranking");
    	try {
    		//System.out.println("computing ranking");
			RationalRankingAlgorithm rankingalg = new RationalRankingAlgorithm(getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory(), getOWLModelManager().getActiveOntology());
			ranking = rankingalg.computeRanking();
		} catch (OWLOntologyCreationException e) {
			System.out.println("Ontology creation exception.");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLException e) {
			System.out.println("Error computing ranking.");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	try {
			exceptionsList.setExceptionsList(ranking);
		} catch (OWLOntologyCreationException e) {
			System.out.println("Exception caught trying to display exceptions");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    


    private void doAdd() {
        try {
            OWLClassExpression desc = owlDescriptionEditor.createObject();
            OWLEntityCreationSet<OWLClass> creationSet = CreateDefinedClassPanel.showDialog(desc, getOWLEditorKit());
            if (creationSet != null) {
            	List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>(creationSet.getOntologyChanges());
            	OWLDataFactory factory = getOWLModelManager().getOWLDataFactory();
            	OWLAxiom equiv = factory.getOWLEquivalentClassesAxiom(creationSet.getOWLEntity(), desc);
            	changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), equiv));
                getOWLModelManager().applyChanges(changes);
                if (isSynchronizing()){
                    getOWLEditorKit().getOWLWorkspace().getOWLSelectionModel().setSelectedEntity(creationSet.getOWLEntity());    
                }
            }
        }
        catch (OWLException e) {
        	System.out.println("Exception caught trying to parse query.");
        	e.printStackTrace();
        }
    }
}
