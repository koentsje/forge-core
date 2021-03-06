/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.spec.javaee.rest;

import static org.jboss.forge.spec.javaee.rest.ResourceGeneratorUtil.getEntityTable;
import static org.jboss.forge.spec.javaee.rest.ResourceGeneratorUtil.getIdClause;
import static org.jboss.forge.spec.javaee.rest.ResourceGeneratorUtil.getJpqlEntityVariable;
import static org.jboss.forge.spec.javaee.rest.ResourceGeneratorUtil.getOrderClause;
import static org.jboss.forge.spec.javaee.rest.ResourceGeneratorUtil.getSelectExpression;
import static org.jboss.forge.spec.javaee.rest.ResourceGeneratorUtil.resolveIdGetterName;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.spec.javaee.events.RestGeneratedResources;
import org.jboss.forge.spec.javaee.util.FreemarkerTemplateProcessor;

/**
 * A JAX-RS resource generator that uses JPA entities directly in the created REST resources.
 */
public class EntityBasedResourceGenerator
{
   @Inject
   FreemarkerTemplateProcessor processor;

   @Inject
   JavaSourceFacet java;

   @Inject
   ResourceGeneratorUtil utility;

   public JavaClass generateFrom(JavaClass entity, String idType, String contentType,
            RestGeneratedResources generatedResourcesEvent) throws FileNotFoundException
   {
      if (!entity.hasAnnotation(XmlRootElement.class))
      {
         entity.addAnnotation(XmlRootElement.class);
         java.saveJavaSource(entity);
      }
      
      String idGetterName = resolveIdGetterName(entity);
      String persistenceUnitName = utility.getPersistenceUnitName();
      String entityTable = getEntityTable(entity);
      String selectExpression = getSelectExpression(entity, entityTable);
      String idClause = getIdClause(entity, entityTable);
      String orderClause = getOrderClause(entity, getJpqlEntityVariable(entityTable));
      String resourcePath = utility.getResourcePath(entityTable);

      Map<Object, Object> map = new HashMap<Object, Object>();
      map.put("entity", entity);
      map.put("idType", idType);
      map.put("getIdStatement", idGetterName);
      map.put("contentType", contentType);
      map.put("persistenceUnitName", persistenceUnitName);
      map.put("entityTable", entityTable);
      map.put("selectExpression", selectExpression);
      map.put("idClause", idClause);
      map.put("orderClause", orderClause);
      map.put("resourcePath", resourcePath);

      String output = processor.processTemplate(map, "org/jboss/forge/rest/Endpoint.jv");
      JavaClass resource = JavaParser.parse(JavaClass.class, output);
      resource.addImport(entity.getQualifiedName());
      resource.setPackage(utility.getPackageName());
      return resource;
   }

}
