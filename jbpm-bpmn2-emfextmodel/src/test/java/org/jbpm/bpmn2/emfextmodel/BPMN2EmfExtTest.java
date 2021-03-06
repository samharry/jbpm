/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.bpmn2.emfextmodel;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.jboss.drools.DocumentRoot;
import org.jboss.drools.DroolsFactory;
import org.jboss.drools.DroolsPackage;
import org.jboss.drools.ElementParameters;
import org.jboss.drools.GlobalType;
import org.jboss.drools.ImportType;
import org.jboss.drools.MetadataType;
import org.jboss.drools.MetaentryType;
import org.jboss.drools.OnEntryScriptType;
import org.jboss.drools.OnExitScriptType;
import org.jboss.drools.Parameter;
import org.jboss.drools.ParameterValue;
import org.jboss.drools.ProcessAnalysisDataType;
import org.jboss.drools.Scenario;
import org.jboss.drools.ScenarioParameters;
import org.jboss.drools.TimeParameters;
import org.jboss.drools.TimeUnit;
import org.jboss.drools.UniformDistributionType;
import org.jboss.drools.util.DroolsResourceFactoryImpl;

import junit.framework.TestCase;

public class BPMN2EmfExtTest extends TestCase {
    private ResourceSet resourceSet;
    
    @Override
    protected void setUp() throws Exception {
        resourceSet = new ResourceSetImpl();
        
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put
            (Resource.Factory.Registry.DEFAULT_EXTENSION, 
             new DroolsResourceFactoryImpl());
        resourceSet.getPackageRegistry().put
            (DroolsPackage.eNS_URI, 
            		DroolsPackage.eINSTANCE);
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    @SuppressWarnings("unchecked")
    public void testProcessAnalysisData() throws Exception {
    	//write
    	XMLResource inResource = (XMLResource) resourceSet.createResource(URI.createURI("inputStream://dummyUriWithValidSuffix.xml"));
        inResource.getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
        inResource.setEncoding("UTF-8");
        
        DocumentRoot documentRoot = DroolsFactory.eINSTANCE.createDocumentRoot();
        
        ProcessAnalysisDataType processAnalysis = DroolsFactory.eINSTANCE.createProcessAnalysisDataType();
        Scenario defaultScenario = DroolsFactory.eINSTANCE.createScenario();
        defaultScenario.setId("default");
        defaultScenario.setName("Scenario");
        ScenarioParameters scenarioParams = DroolsFactory.eINSTANCE.createScenarioParameters();
        scenarioParams.setBaseTimeUnit(TimeUnit.S);
        defaultScenario.setScenarioParameters(scenarioParams);
        ElementParameters elementParams = DroolsFactory.eINSTANCE.createElementParameters();
        elementParams.setElementId("mytask");
        TimeParameters elementTimeParams = DroolsFactory.eINSTANCE.createTimeParameters();
        
        Parameter processingTimeParameter = DroolsFactory.eINSTANCE.createParameter();
        UniformDistributionType uniformDistrobutionType = DroolsFactory.eINSTANCE.createUniformDistributionType();
        uniformDistrobutionType.setMin(180.0);
        uniformDistrobutionType.setMax(600.0);
        processingTimeParameter.getParameterValue().add(uniformDistrobutionType);
        elementTimeParams.setProcessingTime(processingTimeParameter);
        elementParams.setTimeParameters(elementTimeParams);
        defaultScenario.getElementParameters().add(elementParams);
        processAnalysis.getScenario().add(defaultScenario);
        
        documentRoot.setProcessAnalysisData(processAnalysis);
        inResource.getContents().add(documentRoot);
        StringWriter stringWriter = new StringWriter();
        inResource.save(stringWriter, null);
        assertNotNull(stringWriter.getBuffer().toString());
        if(stringWriter.getBuffer().toString().length() < 1) {
            fail("generated xml is empty");
        }
        
    	
    	//read
        XMLResource outResource = (XMLResource) resourceSet.createResource(URI.createURI("inputStream://dummyUriWithValidSuffix.xml"));
        outResource.getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
        outResource.setEncoding("UTF-8");
        Map<String, Object> options = new HashMap<String, Object>();
        options.put( XMLResource.OPTION_ENCODING, "UTF-8" );
        InputStream is = new ByteArrayInputStream(stringWriter.getBuffer().toString().getBytes("UTF-8"));
        outResource.load(is, options);
        
        DocumentRoot outRoot = (DocumentRoot) outResource.getContents().get(0);
        assertNotNull(outRoot.getProcessAnalysisData());
        
        ProcessAnalysisDataType outAnalysisData = outRoot.getProcessAnalysisData();
        assertEquals(outAnalysisData.getScenario().size(), 1);
        Scenario outScenario = outAnalysisData.getScenario().get(0);
        assertEquals(outScenario.getId(), "default");
        assertEquals(outScenario.getName(), "Scenario");
        assertNotNull(outScenario.getScenarioParameters());
        assertNotNull(outScenario.getElementParameters());
        assertEquals(outScenario.getElementParameters().size(), 1);
        ElementParameters outElementParamType = outScenario.getElementParameters().get(0);
        assertNotNull(outElementParamType.getTimeParameters());
        TimeParameters outTimeParams = outElementParamType.getTimeParameters();
        assertNotNull(outTimeParams.getProcessingTime());
        assertEquals(outTimeParams.getProcessingTime().getParameterValue().size(), 1);
        UniformDistributionType outDistributionType = (UniformDistributionType) outTimeParams.getProcessingTime().getParameterValue().get(0);
        assertEquals(outDistributionType.getMax(), 600.0);
        assertEquals(outDistributionType.getMin(), 180.0);
    }
    
    @SuppressWarnings("unchecked")
	public void testMetadataElement() throws Exception {
    	// write
    	XMLResource inResource = (XMLResource) resourceSet.createResource(URI.createURI("inputStream://dummyUriWithValidSuffix.xml"));
        inResource.getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
        inResource.setEncoding("UTF-8");
        DocumentRoot documentRoot = DroolsFactory.eINSTANCE.createDocumentRoot();
        
        MetadataType metadataType =  DroolsFactory.eINSTANCE.createMetadataType();
        
        MetaentryType typeOne =  DroolsFactory.eINSTANCE.createMetaentryType();
        typeOne.setName("entry1");
        typeOne.setValue("value1");
        
        MetaentryType typeTwo =  DroolsFactory.eINSTANCE.createMetaentryType();
        typeTwo.setName("entry2");
        typeTwo.setValue("value2");
        
        metadataType.getMetaentry().add(typeOne);
        metadataType.getMetaentry().add(typeTwo);
        
        documentRoot.setMetadata(metadataType);
        inResource.getContents().add(documentRoot);
        
        StringWriter stringWriter = new StringWriter();
        inResource.save(stringWriter, null);
        assertNotNull(stringWriter.getBuffer().toString());
        if(stringWriter.getBuffer().toString().length() < 1) {
            fail("generated xml is empty");
        }
    	
        // read
        XMLResource outResource = (XMLResource) resourceSet.createResource(URI.createURI("inputStream://dummyUriWithValidSuffix.xml"));
        outResource.getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
        outResource.setEncoding("UTF-8");
        Map<String, Object> options = new HashMap<String, Object>();
        options.put( XMLResource.OPTION_ENCODING, "UTF-8" );
        InputStream is = new ByteArrayInputStream(stringWriter.getBuffer().toString().getBytes("UTF-8"));
        outResource.load(is, options);
        
        DocumentRoot outRoot = (DocumentRoot) outResource.getContents().get(0);
        assertNotNull(outRoot.getMetadata());
        MetadataType outMetadataType =  outRoot.getMetadata();
        assertTrue(outMetadataType.getMetaentry().size() == 2);
        MetaentryType outOne = (MetaentryType) outMetadataType.getMetaentry().get(0);
        MetaentryType outTwo = (MetaentryType) outMetadataType.getMetaentry().get(1);
        
        assertTrue(outOne.getName().equals("entry1"));
        assertTrue(outOne.getValue().equals("value1"));
        
        assertTrue(outTwo.getName().equals("entry2"));
        assertTrue(outTwo.getValue().equals("value2"));
        
    }
    
    public void testOnEntryScriptElement() throws Exception {
        // write
        XMLResource inResource = (XMLResource) resourceSet.createResource(URI.createURI("inputStream://dummyUriWithValidSuffix.xml"));
        inResource.getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
        inResource.setEncoding("UTF-8");
        DocumentRoot documentRoot = DroolsFactory.eINSTANCE.createDocumentRoot();
        OnEntryScriptType root = DroolsFactory.eINSTANCE.createOnEntryScriptType();
        root.setScript("script");
        root.setScriptFormat("format");
        documentRoot.setOnEntryScript(root);
        inResource.getContents().add(documentRoot);
        
        StringWriter stringWriter = new StringWriter();
        inResource.save(stringWriter, null);
        assertNotNull(stringWriter.getBuffer().toString());
        if(stringWriter.getBuffer().toString().length() < 1) {
            fail("generated xml is empty");
        }
        
        // read
        XMLResource outResource = (XMLResource) resourceSet.createResource(URI.createURI("inputStream://dummyUriWithValidSuffix.xml"));
        outResource.getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
        outResource.setEncoding("UTF-8");
        Map<String, Object> options = new HashMap<String, Object>();
        options.put( XMLResource.OPTION_ENCODING, "UTF-8" );
        InputStream is = new ByteArrayInputStream(stringWriter.getBuffer().toString().getBytes("UTF-8"));
        outResource.load(is, options);
        
        DocumentRoot outRoot = (DocumentRoot) outResource.getContents().get(0);
        assertNotNull(outRoot.getOnEntryScript());
        OnEntryScriptType scriptType = outRoot.getOnEntryScript();
        assertEquals("script", scriptType.getScript());
        assertEquals("format", scriptType.getScriptFormat());
    }
    
    public void testOnExitScriptElement() throws Exception {
        // write
        XMLResource inResource = (XMLResource) resourceSet.createResource(URI.createURI("inputStream://dummyUriWithValidSuffix.xml"));
        inResource.getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
        inResource.setEncoding("UTF-8");
        DocumentRoot documentRoot = DroolsFactory.eINSTANCE.createDocumentRoot();
        OnExitScriptType root = DroolsFactory.eINSTANCE.createOnExitScriptType();
        root.setScript("script");
        root.setScriptFormat("format");
        documentRoot.setOnExitScript(root);
        inResource.getContents().add(documentRoot);
        
        StringWriter stringWriter = new StringWriter();
        inResource.save(stringWriter, null);
        assertNotNull(stringWriter.getBuffer().toString());
        if(stringWriter.getBuffer().toString().length() < 1) {
            fail("generated xml is empty");
        }
        
        // read
        XMLResource outResource = (XMLResource) resourceSet.createResource(URI.createURI("inputStream://dummyUriWithValidSuffix.xml"));
        outResource.getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
        outResource.setEncoding("UTF-8");
        Map<String, Object> options = new HashMap<String, Object>();
        options.put( XMLResource.OPTION_ENCODING, "UTF-8" );
        InputStream is = new ByteArrayInputStream(stringWriter.getBuffer().toString().getBytes("UTF-8"));
        outResource.load(is, options);
        
        DocumentRoot outRoot = (DocumentRoot) outResource.getContents().get(0);
        assertNotNull(outRoot.getOnExitScript());
        OnExitScriptType scriptType = outRoot.getOnExitScript();
        assertEquals("script", scriptType.getScript());
        assertEquals("format", scriptType.getScriptFormat());
    }
    
    public void testImportElement() throws Exception {
        // write
        XMLResource inResource = (XMLResource) resourceSet.createResource(URI.createURI("inputStream://dummyUriWithValidSuffix.xml"));
        inResource.getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
        inResource.setEncoding("UTF-8");
        DocumentRoot documentRoot = DroolsFactory.eINSTANCE.createDocumentRoot();
        ImportType root = DroolsFactory.eINSTANCE.createImportType();
        root.setName("import");
        documentRoot.setImport(root);
        inResource.getContents().add(documentRoot);
        
        StringWriter stringWriter = new StringWriter();
        inResource.save(stringWriter, null);
        assertNotNull(stringWriter.getBuffer().toString());
        if(stringWriter.getBuffer().toString().length() < 1) {
            fail("generated xml is empty");
        }
        
        // read
        XMLResource outResource = (XMLResource) resourceSet.createResource(URI.createURI("inputStream://dummyUriWithValidSuffix.xml"));
        outResource.getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
        outResource.setEncoding("UTF-8");
        Map<String, Object> options = new HashMap<String, Object>();
        options.put( XMLResource.OPTION_ENCODING, "UTF-8" );
        InputStream is = new ByteArrayInputStream(stringWriter.getBuffer().toString().getBytes("UTF-8"));
        outResource.load(is, options);
        
        DocumentRoot outRoot = (DocumentRoot) outResource.getContents().get(0);
        assertNotNull(outRoot.getImport());
        ImportType importType = outRoot.getImport();
        assertEquals("import", importType.getName());
    }
    
    public void testGlobalElement() throws Exception {
        // write
        XMLResource inResource = (XMLResource) resourceSet.createResource(URI.createURI("inputStream://dummyUriWithValidSuffix.xml"));
        inResource.getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
        inResource.setEncoding("UTF-8");
        DocumentRoot documentRoot = DroolsFactory.eINSTANCE.createDocumentRoot();
        GlobalType root = DroolsFactory.eINSTANCE.createGlobalType();
        root.setIdentifier("identifier");
        root.setType("type");
        documentRoot.setGlobal(root);
        inResource.getContents().add(documentRoot);
        
        StringWriter stringWriter = new StringWriter();
        inResource.save(stringWriter, null);
        assertNotNull(stringWriter.getBuffer().toString());
        if(stringWriter.getBuffer().toString().length() < 1) {
            fail("generated xml is empty");
        }
        
        // read
        XMLResource outResource = (XMLResource) resourceSet.createResource(URI.createURI("inputStream://dummyUriWithValidSuffix.xml"));
        outResource.getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
        outResource.setEncoding("UTF-8");
        Map<String, Object> options = new HashMap<String, Object>();
        options.put( XMLResource.OPTION_ENCODING, "UTF-8" );
        InputStream is = new ByteArrayInputStream(stringWriter.getBuffer().toString().getBytes("UTF-8"));
        outResource.load(is, options);
        
        DocumentRoot outRoot = (DocumentRoot) outResource.getContents().get(0);
        assertNotNull(outRoot.getGlobal());
        GlobalType globalType = outRoot.getGlobal();
        assertEquals("identifier", globalType.getIdentifier());
        assertEquals("type", globalType.getType());
    }
    
}