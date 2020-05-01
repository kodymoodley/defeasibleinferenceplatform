package net.za.cair.dip.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.owl.model.cache.OWLExpressionUserCache;
import org.protege.editor.owl.model.inference.OWLReasonerManager;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import net.za.cair.dip.DefeasibleInferenceComputer;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.model.ReasoningType;
import net.za.cair.dip.transform.RankingHelperClass;
import net.za.cair.dip.transform.RationalRankingAlgorithm;
import net.za.cair.dip.util.ManchesterOWLSyntaxOWLObjectRendererImpl;
import net.za.cair.dip.util.Utility;

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
	private ExpressionEditor<OWLClassExpression> owlDescriptionEditor;
	private ResultsList resultsList;
	private ExceptionsList exceptionsList;
	private boolean isPreferentiallyConsistent;
	private boolean isTBoxConsistent;
	private JComboBox reasoningList; 
	private JButton executeButton;
	private JButton refreshButton;
	private OWLOntologyChangeListener ontListener;
	private boolean requiresRefresh;
	private Utility u;
	private Ranking ranking;
	private ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();

	protected void initialiseOWLView() throws Exception {
		setLayout(new BorderLayout(10, 10));
		JComponent editorPanel = createQueryPanel();
		JComponent resultsPanel = createResultsPanel();
		JComponent exceptionsPanel = createExceptionsPanel();
		resultsPanel.add(exceptionsPanel, BorderLayout.WEST);
		JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorPanel, resultsPanel);
		splitter.setDividerLocation(0.3);
		add(splitter, BorderLayout.CENTER);
		requiresRefresh = false;
		isTBoxConsistent = true;
		isPreferentiallyConsistent = true;
		u = new Utility();
		if (u.ontologyContainsRanking(getOWLModelManager().getActiveOntology())){
			ranking = u.loadRanking(getOWLModelManager().getActiveOntology(),getOWLModelManager().getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory());
			exceptionsList.setExceptionsList(ranking);
		}
		else {
			addDefeasibleDummyAxioms();
			computeRanking();
			exceptionsList.setExceptionsList(ranking);
			u.saveRanking(getOWLModelManager().getActiveOntology(), ranking, isPreferentiallyConsistent, getOWLModelManager().getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory());
		}
		
		ontListener = new OWLOntologyChangeListener() {
			@Override
			public void ontologiesChanged(List<? extends OWLOntologyChange> changes)
					throws OWLException {
				for (OWLOntologyChange ch: changes){
					if (ch.isAddAxiom() || ch.isRemoveAxiom() || ch.isAxiomChange()){
						if (ch.getAxiom().isLogicalAxiom()) {
							requiresRefresh = true;
						}
					}
				}
			}
		};

		getOWLModelManager().addOntologyChangeListener(ontListener);
	}

	@SuppressWarnings("unchecked")
	private JComponent createQueryPanel() {
		JPanel editorPanel = new JPanel(new BorderLayout());

		final OWLExpressionChecker<OWLClassExpression> checker = getOWLModelManager().getOWLExpressionCheckerFactory().getOWLClassExpressionChecker();
		owlDescriptionEditor = new ExpressionEditor<OWLClassExpression>(getOWLEditorKit(), checker);
		owlDescriptionEditor.addStatusChangedListener(new InputVerificationStatusChangedListener(){
			public void verifiedStatusChanged(boolean newState) {
				executeButton.setEnabled(newState);
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
		editorPanel.add(buttonHolder, BorderLayout.SOUTH);
		editorPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
				Color.LIGHT_GRAY), "Query (class expression)"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		return editorPanel;
	}

	private JComponent createResultsPanel() {
		JComponent resultsPanel = new JPanel(new BorderLayout(10, 10));
		resultsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
				Color.LIGHT_GRAY), "Results"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		resultsList = new ResultsList(getOWLEditorKit());
		resultsList.setShowSuperClasses(true);
		//resultsList.setShowSubClasses(true);
		JComponent mainResultPanel = new JPanel(new GridLayout(1,1));
		mainResultPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
				Color.LIGHT_GRAY), "Super Classes & Instances"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		mainResultPanel.add(ComponentFactory.createScrollPane(resultsList));
		resultsPanel.add(mainResultPanel, BorderLayout.CENTER);
		return resultsPanel;
	}

	private JComponent createExceptionsPanel() throws OWLOntologyCreationException {
		JComponent exceptionsPanel = new JPanel(new BorderLayout(10, 10));
		exceptionsPanel.setPreferredSize(new Dimension(500,500));
		exceptionsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
				Color.LIGHT_GRAY), "Exceptions"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		OWLReasonerManager reasonerManager = getOWLModelManager().getOWLReasonerManager();
		exceptionsList = new ExceptionsList(getOWLEditorKit(), getOWLModelManager().getOWLDataFactory(), reasonerManager.getCurrentReasonerFactory().getReasonerFactory());
		exceptionsPanel.add(ComponentFactory.createScrollPane(exceptionsList));
		refreshButton = new JButton(new AbstractAction("Refresh") {
			/**
			 * 
			 */
			private static final long serialVersionUID = -1833321282125901561L;

			public void actionPerformed(ActionEvent e) {
				computeRanking();
				try {
					exceptionsList.setExceptionsList(ranking);
				} catch (OWLOntologyCreationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		exceptionsPanel.add(refreshButton, BorderLayout.SOUTH);
		return exceptionsPanel;
	}

	protected void disposeOWLView() {
		getOWLModelManager().removeOntologyChangeListener(ontListener);   
	}

//	private boolean ontHasConsistencyAnnotation() {
//		Set<OWLAnnotation> annotations = getOWLModelManager().getActiveOntology().getAnnotations();
//		Utility u = new Utility();
//		for (OWLAnnotation an: annotations) {
//			if (an.getProperty().equals(u.consistencyAnnotationProperty)){
//				return true;
//			}
//		}		
//		return false;
//	}

	private void doQuery() {
		// Clear results
		resultsList.clear();

		try {
			// If ranking needs to be recomputed
			if (ranking == null || requiresRefresh) {
				System.out.println();
				System.out.println("Recomputing ranking...");
				System.out.println();
				
				addDefeasibleDummyAxioms();
				computeRanking();
										
				exceptionsList.setExceptionsList(ranking);
				u.saveRanking(getOWLModelManager().getActiveOntology(), ranking, isPreferentiallyConsistent, getOWLModelManager().getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory());
			}

			// If TBox is preferentially consistent
			if (isTBoxConsistent) {
				System.out.println();
				System.out.println("Is TBox consistent");

				// Sanity check: print ranking
				System.out.println();
				System.out.println("Ranking:");
				System.out.println(ranking);
				System.out.println();

				// First check preferential consistency. At this point we know for sure that the ontology is NOT TBox inconsistent.
				OWLReasonerManager reasonerManager = getOWLModelManager().getOWLReasonerManager();         	
				OWLClassExpression desc = owlDescriptionEditor.createObject();
				DefeasibleInferenceComputer dic = new DefeasibleInferenceComputer(reasonerManager.getCurrentReasonerFactory().getReasonerFactory(), ranking);
				boolean ontologyConsistent = dic.isConsistent(ranking);

				// If the ontology as a whole is consistent (including ABox if applicable)
				if (ontologyConsistent) {  
					System.out.println();
					System.out.println("Is preferentially consistent");
					System.out.println();

					// If I have a valid query (class expression)
					if (desc != null){
						OWLExpressionUserCache.getInstance(getOWLModelManager()).add(desc, owlDescriptionEditor.getText());
						ReasoningType algorithm = ReasoningType.NAME_TYPE_MAP.get((String)reasoningList.getSelectedItem());
						// compute all super classes and instances for the given class expression
						resultsList.setOWLClassExpression(desc, algorithm, ranking, null);
					}
				}
				else {
					// The TBox is preferentially consistent but when we add the ABox then it becomes preferentially inconsistent
					isPreferentiallyConsistent = false;
					System.out.println("Ontology is preferentially inconsistent!");
					JOptionPane.showMessageDialog(null, "The Individual assertions in your ontology conflict with the Class axioms.\n DIP will no longer be able to make useful inferences about this ontology.\n Please check your assertions and class definitions.", "DIP: Inconsistent Ontology Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
			else {
				System.out.println("Ontology is TBox inconsistent!");
				JOptionPane.showMessageDialog(null, "The Class definitions in your ontology are contradictory.\n DIP will no longer be able to make useful inferences about this ontology.\n Please check your class definitions.", "DIP: Inconsistent Ontology Warning", JOptionPane.WARNING_MESSAGE);
			}
		}
		catch (OWLException e) {
			System.out.println("Exception when executing query.");
			e.printStackTrace();
		}
	}
	
	private void addDefeasibleDummyAxioms() throws InconsistentOntologyException, OWLOntologyCreationException, OWLOntologyStorageException {
		Set<OWLAxiom> strictAxioms = new HashSet<OWLAxiom>();								// Strict axioms
		
		for (OWLAxiom a: getOWLModelManager().getActiveOntology().getAxioms()) {
			if (!u.isDefeasible(a)) {
				strictAxioms.add(a);
			}
		}
		
		Set<OWLAxiom> tboxAxioms = getOWLModelManager().getActiveOntology().getTBoxAxioms(Imports.EXCLUDED);				// TBox axioms
		RankingHelperClass rhc = new RankingHelperClass();
		Set<OWLClassExpression> strictExceptions = rhc.getPossibleExceptions(strictAxioms, tboxAxioms, getOWLModelManager().getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory());
		OWLDataFactory df = getOWLModelManager().getOWLDataFactory();
		
		for (OWLClassExpression c: strictExceptions) {
			OWLSubClassOfAxiom sub = df.getOWLSubClassOfAxiom(c, df.getOWLThing());
            Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>();
            annos.add(df.getOWLAnnotation(Utility.defeasibleAnnotationProperty, df.getOWLLiteral(true)));
            OWLAxiom annotated_axiom = sub.getAnnotatedAxiom(annos);
            AddAxiom addAxiom = new AddAxiom(getOWLModelManager().getActiveOntology(), annotated_axiom);		            
            getOWLModelManager().getActiveOntology().getOWLOntologyManager().applyChange(addAxiom);					
		}
		
		//getOWLModelManager().getActiveOntology().saveOntology();
	}

	private void computeRanking(){
		try {
			// Compute ranking
			RationalRankingAlgorithm rankingalg = new RationalRankingAlgorithm(getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory(), getOWLModelManager().getActiveOntology());
			ranking = rankingalg.computeRanking();
		} catch (OWLOntologyCreationException e) {
			System.out.println("Ontology creation exception.");
			e.printStackTrace();
		} catch (OWLException e) {
			System.out.println("Error computing ranking.");
			e.printStackTrace();
		}
		catch (InconsistentOntologyException e) {
			//This can only mean TBox inconsistency e.g. "A or not A SubClassOf Nothing"
			exceptionsList.clear();
			resultsList.clear();
			isTBoxConsistent = false;
			System.out.println("Ontology is TBox inconsistent!");
			JOptionPane.showMessageDialog(null, "The Class definitions in your ontology are contradictory.\n DIP will no longer be able to make useful inferences about this ontology.\n Please check your class definitions.", "DIP: Inconsistent Ontology Warning", JOptionPane.WARNING_MESSAGE);            
		}
	}

//	private void doAdd() {
//		try {
//			OWLClassExpression desc = owlDescriptionEditor.createObject();
//			OWLEntityCreationSet<OWLClass> creationSet = CreateDefinedClassPanel.showDialog(desc, getOWLEditorKit());
//			if (creationSet != null) {
//				List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>(creationSet.getOntologyChanges());
//				OWLDataFactory factory = getOWLModelManager().getOWLDataFactory();
//				OWLAxiom equiv = factory.getOWLEquivalentClassesAxiom(creationSet.getOWLEntity(), desc);
//				changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), equiv));
//				getOWLModelManager().applyChanges(changes);
//				if (isSynchronizing()){
//					getOWLEditorKit().getOWLWorkspace().getOWLSelectionModel().setSelectedEntity(creationSet.getOWLEntity());    
//				}
//			}
//		}
//		catch (OWLException e) {
//			System.out.println("Exception caught trying to parse query.");
//			e.printStackTrace();
//		}
//	}
}
