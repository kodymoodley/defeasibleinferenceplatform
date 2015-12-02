package net.za.cair.dip.experimentbench;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class DOntologyGenerator
{
  private static OWLAnnotationProperty defeasibleAnnotationProperty;
  private static IRI defeasibleIRI = IRI.create("http://www.cair.za.net/ontologyAnnotationProperties/defeasible");
  
  public static Set<OWLClassExpression> generateComplexConcepts(OWLDataFactory dataF, Set<OWLClass> classes, Set<OWLObjectProperty> roles)
  {
    Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
    Random randomGenerator = new Random();
    for (OWLClass cls : classes)
    {
    	//System.out.println(cls);
      OWLClassExpression generatedExpression = null;
      int complex1 = randomGenerator.nextInt(10);
      if (complex1 < 3)
      {
        int complex2 = randomGenerator.nextInt(5);
        
        OWLObjectProperty chosenRole = null;
        int roleIdx = 0;
       // System.out.println("NUMBER OF ROLES: " + roles.size());
        
        if (roles.size() == 0)
        	roleIdx = 0;
        else
        	roleIdx = randomGenerator.nextInt(roles.size());
        
        int count = 0;
        
        for (OWLObjectProperty r : roles)
        {
          if (count == roleIdx) {
            chosenRole = r;
          }
          count++;
        }
        
        if (complex2 == 0) {
        	if (chosenRole == null)
        		generatedExpression = cls;
        	else
        		generatedExpression = dataF.getOWLObjectAllValuesFrom(chosenRole, cls);
        }
        else {
        	if (chosenRole == null)
        		generatedExpression = cls;
        	else
        		generatedExpression = dataF.getOWLObjectSomeValuesFrom(chosenRole, cls);
        }
      }
      else if (complex1 >= 3)
      {
        int complex3 = randomGenerator.nextInt(3);
        if (complex3 == 0)
        {
          generatedExpression = dataF.getOWLObjectComplementOf(cls);
        }
        else
        {
          OWLClassExpression chosenConcept = null;
          int conceptIdx = randomGenerator.nextInt(classes.size());
          
          int count = 0;
          for (OWLClassExpression c : classes)
          {
            if (count == conceptIdx) {
              chosenConcept = c;
            }
            count++;
          }
          if (complex3 == 1) {
            generatedExpression = dataF.getOWLObjectIntersectionOf(new OWLClassExpression[] { cls, chosenConcept });
          } else if (complex3 == 2) {
            generatedExpression = dataF.getOWLObjectUnionOf(new OWLClassExpression[] { cls, chosenConcept });
          }
        }
      }
      result.add(generatedExpression);
    }
    return result;
  }
  
  private static Set<OWLAxiom> generateDefeasibleCluster(Set<OWLClassExpression> classes, Random rand, OWLDataFactory dataF, OWLClassExpression a, OWLClassExpression b, OWLClassExpression c)
  {
    Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>();
    annos.add(dataF.getOWLAnnotation(defeasibleAnnotationProperty, dataF.getOWLLiteral(true)));
    
    int pattern = rand.nextInt(4);
    
    Set<OWLAxiom> result = new HashSet<OWLAxiom>();
    if (pattern == 0)
    {
      OWLAxiom axiom1 = dataF.getOWLSubClassOfAxiom(a, b);
      result.add(axiom1.getNNF().getAnnotatedAxiom(annos));
      OWLAxiom axiom2 = dataF.getOWLSubClassOfAxiom(b, c);
      result.add(axiom2.getNNF().getAnnotatedAxiom(annos));
      OWLAxiom axiom3 = dataF.getOWLSubClassOfAxiom(a, dataF.getOWLObjectComplementOf(c));
      result.add(axiom3.getNNF().getAnnotatedAxiom(annos));
    }
    else if (pattern == 1)
    {
      OWLAxiom axiom1 = dataF.getOWLSubClassOfAxiom(a, b);
      result.add(axiom1.getNNF().getAnnotatedAxiom(annos));
      OWLAxiom axiom2 = dataF.getOWLSubClassOfAxiom(dataF.getOWLObjectIntersectionOf(new OWLClassExpression[] { a, c }), dataF.getOWLObjectComplementOf(b));
      result.add(axiom2.getNNF().getAnnotatedAxiom(annos));
    }
    else
    {
      OWLAxiom axiom4;
      if (pattern == 2)
      {
        OWLAxiom axiom1 = dataF.getOWLSubClassOfAxiom(a, b);
        result.add(axiom1.getNNF().getAnnotatedAxiom(annos));
        OWLAxiom axiom2 = dataF.getOWLSubClassOfAxiom(b, c);
        result.add(axiom2.getNNF().getAnnotatedAxiom(annos));
        OWLAxiom axiom3 = dataF.getOWLSubClassOfAxiom(a, dataF.getOWLObjectComplementOf(c));
        result.add(axiom3.getNNF().getAnnotatedAxiom(annos));
        
        OWLClassExpression chosenConcept = null;
        
        int conceptIdx = rand.nextInt(classes.size());
        
        int count = 0;
        for (OWLClassExpression cls : classes)
        {
          if (count == conceptIdx) {
            chosenConcept = cls;
          }
          count++;
        }
        axiom4 = dataF.getOWLSubClassOfAxiom(chosenConcept, a);
        result.add(axiom4.getNNF().getAnnotatedAxiom(annos));
        Object axiom5 = dataF.getOWLSubClassOfAxiom(chosenConcept, c);
        result.add(((OWLAxiom)axiom5).getNNF().getAnnotatedAxiom(annos));
      }
      else if (pattern == 3)
      {
        OWLAxiom axiom1 = dataF.getOWLSubClassOfAxiom(a, b);
        result.add(axiom1.getNNF().getAnnotatedAxiom(annos));
        OWLAxiom axiom2 = dataF.getOWLSubClassOfAxiom(dataF.getOWLObjectIntersectionOf(new OWLClassExpression[] { a, c }), dataF.getOWLObjectComplementOf(b));
        result.add(axiom2.getNNF().getAnnotatedAxiom(annos));
        
        OWLClassExpression chosenConcept = null;
        
        int conceptIdx = rand.nextInt(classes.size());
        
        int count = 0;
        for (OWLClassExpression cls : classes)
        {
          if (count == conceptIdx) {
            chosenConcept = cls;
          }
          count++;
        }
        OWLAxiom axiom3 = dataF.getOWLSubClassOfAxiom(dataF.getOWLObjectIntersectionOf(new OWLClassExpression[] { a, c, chosenConcept }), b);
        result.add(axiom3.getNNF().getAnnotatedAxiom(annos));
      }
    }
    return result;
  }
  
  public Set<OWLOntology> generateOntologies(int nconcepts, int nroles, int nindividuals, int axioms, int defeasibility, int nontologies){
	  Set<OWLOntology> result = new HashSet<OWLOntology>();
	  return result;
  }
  
  public static void main(String[] args)
    throws IOException, OWLException
  {
    boolean parameterError = false;
    int nconcepts = 0;
    int nroles = 0;
    int nindividuals = 0;
    int naxioms = 0;
    int defeasibility = 0;
    int nontologies = 0;
    try
    {
      if (args.length == 6)
      {
        int count = 0;
        String[] arrayOfString = args;int j = args.length;
        for (int i = 0; i < j; i++)
        {
          String argument = arrayOfString[i];
          int tmp = Integer.parseInt(argument);
          if (count == 0) {
            if (tmp > 0)
            {
              nontologies = tmp;
            }
            else
            {
              parameterError = true;
              System.out.println("Invalid parameter(s) specified.");
            }
          }
          if (count == 1)
          {
            if (tmp >= 2)
            {
              naxioms = tmp;
            }
            else
            {
              parameterError = true;
              System.out.println("Invalid parameter(s) specified.");
            }
          }
          else if (count == 2)
          {
            if (tmp > 2)
            {
              nconcepts = tmp;
            }
            else
            {
              parameterError = true;
              System.out.println("Invalid parameter(s) specified.");
            }
          }
          else if (count == 3)
          {
            if (tmp > 0)
            {
              nroles = tmp;
            }
            else
            {
              parameterError = true;
              System.out.println("Invalid parameter(s) specified.");
            }
          }
          else if (count == 4)
          {
            if (tmp >= 0)
            {
              nindividuals = tmp;
            }
            else
            {
              parameterError = true;
              System.out.println("Invalid parameter(s) specified.");
            }
          }
          else if (count == 5) {
            if ((tmp >= 0) && (tmp <= 100) && (tmp % 10 == 0))
            {
              defeasibility = tmp;
            }
            else
            {
              parameterError = true;
              System.out.println("Invalid defeasibility percentage.");
            }
          }
          count++;
        }
      }
      else
      {
        System.out.println("Invalid number of parameters.");
        parameterError = true;
      }
    }
    catch (InvalidParameterException ipe)
    {
      parameterError = true;
      System.out.println("Invalid parameter(s) specified.");
    }
    double percentage_validity = defeasibility / 100.0D * naxioms;
    if (percentage_validity < 1.0D)
    {
      parameterError = true;
      System.out.println("Invalid parameter(s) specified.");
    }
    if (!parameterError)
    {
      File file = null;
      OWLXMLOntologyFormat owlxmlFormat = new OWLXMLOntologyFormat();
      for (int i = 1; i <= nontologies; i++)
      {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = manager.getOWLDataFactory();
        defeasibleAnnotationProperty = df.getOWLAnnotationProperty(defeasibleIRI);
        
        IRI ontologyIRI = IRI.create("http://cair.meraka.org.za/ontologies/ontology" + i);
        OWLOntology currentOntology = manager.createOntology(ontologyIRI);
        int numberOfClasses = nconcepts;
        double dratio = defeasibility / 100.0;
        int defAxioms = (int)(dratio * naxioms);
        int strictAxioms = naxioms - defAxioms;
        
        int numberOfRoles = nroles;
        int numberOfIndividuals = nindividuals;
        
        Set<OWLClass> classes = new HashSet<OWLClass>();
        for (int j = 0; j < numberOfClasses; j++) {
          classes.add(df.getOWLClass(IRI.create(ontologyIRI + "#C" + j)));
        }
        Set<OWLObjectProperty> roles = new HashSet<OWLObjectProperty>();
        for (int k = 0; k < numberOfRoles; k++) {
          roles.add(df.getOWLObjectProperty(IRI.create(ontologyIRI + "#R" + k)));
        }
        Set<OWLClassExpression> classExp = new HashSet<OWLClassExpression>();
        classExp.addAll(classes);
        classExp.addAll(generateComplexConcepts(df, classes, roles));
        
        Set<OWLIndividual> objects = new HashSet<OWLIndividual>();
        for (int l = 0; l < numberOfIndividuals; l++) {
          objects.add(df.getOWLNamedIndividual(IRI.create(ontologyIRI + "#I" + l)));
        }
        Random randomGenerator = new Random();
        Set<OWLAxiom> ontologyAxioms = new HashSet<OWLAxiom>();
        int defAxiomSize = 0;
        if (defAxioms >= 3) {
          defAxiomSize = defAxioms / 3;
        } else {
          defAxiomSize = 1;
        }
        OWLClassExpression c;
        for (int k = 0; k < defAxiomSize; k++)
        {
          int concept1Idx = 0;
          int concept2Idx = 0;
          int concept3Idx = 0;
          boolean done = false;
          while (!done)
          {
            concept1Idx = randomGenerator.nextInt(classExp.size());
            concept2Idx = randomGenerator.nextInt(classExp.size());
            concept3Idx = randomGenerator.nextInt(classExp.size());
            if ((concept1Idx != concept2Idx) && (concept1Idx != concept3Idx) && (concept2Idx != concept3Idx)) {
              done = true;
            }
          }
          OWLClassExpression conceptA = null;
          OWLClassExpression conceptB = null;
          OWLClassExpression conceptC = null;
          int count = 0;
          for (Iterator<OWLClassExpression> localIterator = classExp.iterator(); localIterator.hasNext();)
          {
            c = (OWLClassExpression)localIterator.next();
            if (count == concept1Idx) {
              conceptA = c;
            }
            if (count == concept2Idx) {
              conceptB = c;
            }
            if (count == concept3Idx) {
              conceptC = c;
            }
            count++;
          }
          ontologyAxioms.addAll(generateDefeasibleCluster(classExp, randomGenerator, df, conceptA, conceptB, conceptC));
        }
        int size = 0;
        if ((nindividuals > 0) && (nindividuals <= 10)) {
          size = strictAxioms;
        } else {
          size = (int)(strictAxioms * 0.8D);
        }
        for (int j = 0; j < size; j++)
        {
          int lhs = 0;
          int rhs = 0;
          boolean done = false;
          while (!done)
          {
            lhs = randomGenerator.nextInt(classExp.size());
            rhs = randomGenerator.nextInt(classExp.size());
            if (lhs != rhs) {
              done = true;
            }
          }
          OWLClassExpression conceptA = null;
          OWLClassExpression conceptB = null;
          int count = 0;
          for (OWLClassExpression c1 : classExp)
          {
            if (count == lhs) {
              conceptA = c1;
            }
            if (count == rhs) {
              conceptB = c1;
            }
            count++;
          }
          int axiomTypeChoice = randomGenerator.nextInt(size);
          if ((axiomTypeChoice > size * 0.6D) && (axiomTypeChoice <= size * 0.9D)) {
            ontologyAxioms.add(df.getOWLDisjointClassesAxiom(new OWLClassExpression[] { conceptA, conceptB }));
          } else if (axiomTypeChoice > size * 0.9D) {
            ontologyAxioms.add(df.getOWLEquivalentClassesAxiom(conceptA, conceptB));
          } else if ((axiomTypeChoice > 0) && (axiomTypeChoice <= size * 0.6D)) {
            ontologyAxioms.add(df.getOWLSubClassOfAxiom(conceptA, conceptB));
          }
        }
        int size2 = 0;
        if ((nindividuals > 0) && (nindividuals <= 10)) {
          size2 = 5;
        } else {
          size2 = (int)(strictAxioms * 0.2D);
        }
        for (int l = 0; l < size2; l++)
        {
          int classIdx = 0;
          int objIdx = 0;
          classIdx = randomGenerator.nextInt(classExp.size());
          objIdx = randomGenerator.nextInt(objects.size());
          OWLClassExpression concept = null;
          OWLIndividual object = null;
          int count = 0;
          for (OWLClassExpression c2 : classExp)
          {
            if (count == classIdx) {
              concept = c2;
            }
            count++;
          }
          count = 0;
          for (OWLIndividual ind : objects)
          {
            if (count == objIdx) {
              object = ind;
            }
            count++;
          }
          ontologyAxioms.add(df.getOWLClassAssertionAxiom(concept, object));
        }
        manager.addAxioms(currentOntology, ontologyAxioms);
        file = new File("Generated/Ontology" + i + ".owl");
        manager.saveOntology(currentOntology, owlxmlFormat, IRI.create(file.toURI()));
        System.out.println("Ontology" + i + " processed");
      }
    }
  }
}