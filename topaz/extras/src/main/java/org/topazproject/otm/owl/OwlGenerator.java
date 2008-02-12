/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.owl;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.mapping.java.FieldLoader;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.OWLXMLOntologyFormat;
import org.semanticweb.owl.io.RDFXMLOntologyFormat;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyFormat;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.vocab.XSDVocabulary;

/**
 * Class to generate OWL schema from Topaz class meta data information. OWLAPI
 * is utilized to generate the OWL triples.
 *
 * @author Amit Kapoor
 */
public class OwlGenerator {
  // Initialize logging
  private static final Log log = LogFactory.getLog(OwlGenerator.class);

  // Map of created OWL classes
  HashMap classMap;

  // Map of created OWL properties
  HashMap propertyMap;

  // Logical URI for generated ontology
  String logicalURI;

  // The OTM side of things
  SessionFactory otmFactory;

  // OWLAPI factories etc.
  OWLOntologyManager ontologyManager;
  OWLOntology ontology;
  OWLDataFactory factory;

  /**
   * Return true if the class metadata is a typed entity
   *
   * @param cm the class metadata
   *
   * @return true if cm is a typed entity
   */
  private boolean isTypedEntity(ClassMetadata cm) {
    if (cm == null)
      return false;
    if (cm.getType() == null)
      return false;

    return true;
  }

  /**
   * Return the list of super-classes
   *
   * @param sourceClass the starting point source class
   *
   * @return superClasses hash map of java super classes for a class
   */
  private HashMap getSuperClasses(Class sourceClass) {
    HashMap superClasses = new HashMap();
    Class clazz = sourceClass;
    while (clazz != Object.class) {
      clazz = clazz.getSuperclass();
      if (clazz != Object.class)
        superClasses.put(clazz.getName(), clazz);
    }

    return superClasses;
  }

  /**
   * Return the OWLClass associated with this metadata information
   *
   * @param type the type for the metadata
   * @param name the name for the class
   *
   * @return the OWLClass either from cache or newly created
   */
  private OWLClass getOWLClass(String type, String name) {
    OWLClass owlClass = (OWLClass)classMap.get(type);
    if (owlClass == null) {
      owlClass = factory.getOWLClass(URI.create(type));
      classMap.put(type, owlClass);
      log.debug("Cached OWL Class " + owlClass.getURI() + " created for " + name);
    }

    return owlClass;
  }

  /**
   * Return the OWLObjectProperty associated with this metadata information
   *
   * @param type the type for the metadata
   * @param name the name for the property
   *
   * @return the ObjectProperty either from cache or newly created
   */
  private OWLObjectProperty getOWLObjectProperty(String type, String name) {
    OWLObjectProperty objectProperty = (OWLObjectProperty)propertyMap.get(type);
    if (objectProperty == null) {
      objectProperty = factory.getOWLObjectProperty(URI.create(type));
      propertyMap.put(type, objectProperty);
      log.debug("Cached OWL Property Class " + objectProperty.getURI() + " created for " + name);
    }

    return objectProperty;
  }

  /**
   * Create the generator class with the SessionFactory and model
   * configuration.
   *
   * @param logicalURI the logical URI for the generated ontology
   * @param metaFactory where OTM class metadata exists
   */
  public OwlGenerator(String logicalURI, SessionFactory metaFactory) throws
    OtmException, OWLException {
      this.logicalURI = logicalURI;
      initialize(metaFactory);
    }

  /**
   * Initialize the OWL generator with the information needed.
   *
   * @param metaFactory where OTM class metadata exists
   */
  public void initialize(SessionFactory metaFactory) throws OtmException,OWLException {
    log.debug("Initialising OwlGenerator...\n");
    classMap = new HashMap();
    propertyMap = new HashMap();

    // Initialize input (OTM) side of things
    otmFactory = metaFactory;

    // Initialize output (OWLAPI) side of things
    ontologyManager = OWLManager.createOWLOntologyManager();
    ontology = ontologyManager.createOntology(URI.create(logicalURI));
    factory = ontologyManager.getOWLDataFactory();
  }

  /**
   * Try and save the OWL triples to a name graph
   *
   * @param graph the named graph where the OWL metadata is to be stored
   */
  public void save(ModelConfig graph) throws OtmException {
    Session otmSession = otmFactory.openSession();
    otmFactory.addModel(graph);
    otmFactory.getTripleStore().createModel(graph);
  }

  /**
   * Save the OWL triples to named physical URI
   *
   * @param physicalURI the physical URI for storing the generated ontology
   */
  public void save(String physicalURI) throws OWLException {
    RDFXMLOntologyFormat format = new RDFXMLOntologyFormat();
    format.addPrefixNamespaceMapping("topaz", "http://rdf.topazproject.org/RDF/");
    format.addPrefixNamespaceMapping("plos", "http://rdf.plos.org/RDF/");
    format.addPrefixNamespaceMapping("annotea", "http://www.w3.org/2000/10/annotation-ns#");
    ontologyManager.setOntologyFormat(ontology, format);
    ontologyManager.saveOntology(ontology, URI.create(physicalURI));
  }

  /**
   * Generate class relationships only
   */
  public void generateClasses() throws OtmException, OWLException {
    // Loop over all classes in factory
    for (ClassMetadata<?> cm: otmFactory.listClassMetadata()) {
      log.debug("Parsing OTM Class metadata: " + cm.getName());
      String type = cm.getType();
      // Ignore anonymous classes
      if (type == null) {
        continue;
      }

      // Get the corresponding OWL class
      OWLClass owlClass = getOWLClass(type,cm.getName());
      ontologyManager.applyChange(new AddAxiom(ontology, factory.getOWLDeclarationAxiom(owlClass)));
      HashMap superClasses = getSuperClasses(cm.getSourceClass());

      // Create sub-class relationships
      for (Class c: (Collection<Class>)superClasses.values()) {
        ClassMetadata<?> scm = otmFactory.getClassMetadata(c);
        if (!isTypedEntity(scm))
          continue;
        log.debug("Processing super-class: " + scm.getName());
        OWLClass superOwlClass = getOWLClass(scm.getType(),scm.getName());

        // Because of pre-computed types.
        if (superOwlClass == owlClass)
          continue;

        ontologyManager.applyChange(new AddAxiom(ontology,
              factory.getOWLSubClassAxiom(owlClass, superOwlClass)));
        log.debug("Making " + owlClass.getURI() + " subClass of " + superOwlClass.getURI());
      }
    }
  }

  /**
   * Generate the property restrictions on each class
   */
  public void generateClassProperties() throws OtmException, OWLException {
    // Loop over all classes in factory
    for (ClassMetadata<?> cm: otmFactory.listClassMetadata()) {
      System.out.println("Parsing OTM Class metadata: " + cm.getName());
      String type = cm.getType();
      // Ignore anonymous classes
      if (type == null) {
        continue;
      }

      // Get the corresponding OWL class
      OWLClass owlClass = getOWLClass(type,cm.getName());
      HashMap superClasses = getSuperClasses(cm.getSourceClass());

      // Now let's iterate over the fields to define 'restrictions' on properties
      for (Mapper m: cm.getFields()) {
        System.out.println("Processing field: " + m.getName());
        // If not annotated
        if (m.getUri() == null)
          continue;

        // See if this field really belongs to our class/type
        Field f = ((FieldLoader)m.getLoader()).getField();
        log.debug("Field Class extracted: " + f);
        Class clazz = f.getDeclaringClass();
        if (clazz != cm.getSourceClass()) {
          // See if it exists in super classes
          Class c = (Class)superClasses.get(clazz.getName());
          if (c != null) {
            ClassMetadata<?> fCm = otmFactory.getClassMetadata(c);
            if (isTypedEntity(fCm))
              continue;
          }
        }

        AddAxiom addAxiom = null;
        if (m.isAssociation() && (m.getRdfType() != null)) {
          System.out.println("Processing association field: " + m.getName());
          OWLObjectProperty objectProperty = getOWLObjectProperty(m.getUri(), m.getName());
          if (!m.hasInverseUri()) {
            OWLClass range = getOWLClass(m.getRdfType(), null);
            OWLDescription rangeRestriction = factory.getOWLObjectAllRestriction(objectProperty, range);
            addAxiom = new AddAxiom(ontology, factory.getOWLSubClassAxiom(owlClass, rangeRestriction));
          } else {
            OWLClass domain = getOWLClass(m.getRdfType(), null);
            OWLDescription rangeRestriction = factory.getOWLObjectAllRestriction(objectProperty, owlClass);
            addAxiom = new AddAxiom(ontology, factory.getOWLSubClassAxiom(domain, rangeRestriction));
          }
        } else {
          System.out.println("Processing data field: " + m.getName());
          OWLDataProperty dataProperty = factory.getOWLDataProperty(URI.create(m.getUri()));
          OWLDataRange typeRange = null;
          if (m.getDataType() != null)
            typeRange = factory.getOWLDataType(URI.create(m.getDataType()));
          else
            typeRange = factory.getOWLDataType(XSDVocabulary.ANY_TYPE.getURI());
          OWLDescription rangeRestriction = factory.getOWLDataAllRestriction(dataProperty, typeRange);

          addAxiom = new AddAxiom(ontology, factory.getOWLSubClassAxiom(owlClass, rangeRestriction));
        }

        ontologyManager.applyChange(addAxiom);
      }
    }
  }
}
