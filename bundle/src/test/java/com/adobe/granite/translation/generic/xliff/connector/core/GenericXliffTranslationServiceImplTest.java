/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2016 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/
/*************************************************************************
 *
 * Copyright 2016 Experience labs
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Experience labs. The intellectual and 
 * technical concepts contained herein are proprietary to Experience labs 
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Experience labs.
 *
 * Author - Praffull Jain
 * Email - praffull@experiencelabs.in
 *******************************************************************************/

package com.adobe.granite.translation.generic.xliff.connector.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.sling.commons.html.HtmlParser;
import org.apache.sling.commons.html.impl.HtmlParserImpl;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import com.adobe.granite.comments.Comment;
import com.adobe.granite.comments.CommentCollection;
import com.adobe.granite.translation.api.TranslationException;
import com.adobe.granite.translation.api.TranslationMetadata;
import com.adobe.granite.translation.api.TranslationObject;
import com.adobe.granite.translation.api.TranslationConfig;
import com.adobe.granite.translation.generic.xliff.connector.core.impl.GenericXliffTranslationServiceImpl;

class DummyTranslationObject implements TranslationObject {

    private InputStream m_inputStream = null;
    private String m_mimeType = null;

    @Override
    public CommentCollection<Comment> getCommentCollection() {
        return null;
    }

    @Override
    public String getId() {
        return "193078517002444699495823491690482376266004989030200051436551890747248761333858736039014";
    }

    public void setMimeType(String mimeType) {
        m_mimeType = mimeType;
    }

    @Override
    public String getMimeType() {
        return m_mimeType;
    }

    @Override
    public String getSourceVersion() {
        return null;
    }

    @Override
    public int getSupportingTranslationObjectsCount() {
        return 0;
    }

    @Override
    public Iterator<TranslationObject> getSupportingTranslationObjectsIterator() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public TranslationMetadata getTranslationJobMetadata() {
        return null;
    }

    public void setTranslationObjectInputStream(InputStream iStream) {
        m_inputStream = iStream;
    }

    @Override
    public InputStream getTranslationObjectInputStream() throws TranslationException {
        return m_inputStream;
    }

    @Override
    public String getTranslationObjectSourcePath() {
        return null;
    }

    @Override
    public String getTranslationObjectTargetPath() {
        return null;
    }

    @Override
    public InputStream getTranslatedObjectInputStream() {
        return null;
    }

}

public class GenericXliffTranslationServiceImplTest {

    private static final String INPUT_SAMPLE_XLIFF = "/input_sample.xlif";
    private static final String INPUT_SAMPLE_XML = "/input_sample.xml";
    private static final String OUTPUT_SAMPLE_XML = "/output_sample.xml";
    private static final String TRANSLATION_JOB_FILE_NAME = "translation_job_file";
    private static final String DUMMY_TRANSLATION_JOB_NAME = "dummy";
    private static final String DUMMY_TRANSLATION_JOB_DESCRIPTION = "This is a dummy translaion job.";
    private static final String ENGLISH_LANG_CODE = "en";
    private static final String FRENCH_LANG_CODE = "fr";
    private static final String XML_EXTENSION = ".xml";
    private static final String XLIFF_EXTENSION = ".xlf";
    private static final String DEFAULT_MIME_TYPE = "text/html";
    private static String XLIFF_VERSION_2 = "2.0";
    private static String XLIFF_VERSION_1 = "1.2";
    private GenericXliffTranslationServiceImpl genXliffTransServics;
    private HashMap<String, String> languageMap;
    private HashMap<String, String> categoryMap;
    private static Path m_storePath = null;
    private static Path m_importPath = null;
    private TranslationConfig transConfig;
    private HtmlParser m_htmlParser;

    @Before
    public void setup() throws Exception {

        languageMap = new HashMap<String, String>();
        categoryMap = new HashMap<String, String>();
        languageMap.put("dummy_el", "el");
        languageMap.put("dummy_fr", "fr");
        languageMap.put("dummy_en", "en");
        languageMap.put("dummy_de", "de");
        categoryMap.put("general", "GENERAL");
        categoryMap.put("clothing", "CLOTHIN");
        m_htmlParser = new HtmlParserImpl();

        transConfig = mock(TranslationConfig.class);
        when(transConfig.getLanguages()).thenReturn(languageMap);
        when(transConfig.getCategories()).thenReturn(categoryMap);

        if (m_storePath != null)
            deleteDir(new File(m_storePath.toString()));

        if (m_importPath != null)
            deleteDir(new File(m_importPath.toString()));

        m_storePath = Files.createTempDirectory("store");
        m_importPath = Files.createTempDirectory("import");

        genXliffTransServics =
            new GenericXliffTranslationServiceImpl(languageMap, categoryMap, null, m_importPath.toString(),
                m_storePath.toString(), XLIFF_VERSION_2, transConfig, m_htmlParser);
    }

    @Test
    public void testUploadXliffv2Document() throws TranslationException {
        String trnsJobId =
            genXliffTransServics.createTranslationJob(DUMMY_TRANSLATION_JOB_NAME, DUMMY_TRANSLATION_JOB_DESCRIPTION,
                ENGLISH_LANG_CODE, FRENCH_LANG_CODE, null, null, null);
        DummyTranslationObject dummyObject = new DummyTranslationObject();
        InputStream iStream = getClass().getResourceAsStream(INPUT_SAMPLE_XML);
        dummyObject.setMimeType(DEFAULT_MIME_TYPE);
        dummyObject.setTranslationObjectInputStream(iStream);
        String transObjId = genXliffTransServics.uploadTranslationObject(trnsJobId, dummyObject);
        String xliffFilePathStr = m_storePath + "/" + trnsJobId + "/" + transObjId + XLIFF_EXTENSION;
        Path xliffFilePath = Paths.get(xliffFilePathStr);
        File file = new File(xliffFilePath.toString());
        if (file.length() == 0) {
            assertTrue(false);
        }
        assertTrue(Files.exists(xliffFilePath));

    }

    private String getStringFromStream(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String read;

        while ((read = br.readLine()) != null) {
            // System.out.println(read);
            sb.append(read);
        }

        return sb.toString();
    }

    @Test
    public void testGetTranslatedObjectXliffv2Document() throws TranslationException, IOException {
        String trnsJobId =
            genXliffTransServics.createTranslationJob(DUMMY_TRANSLATION_JOB_NAME, DUMMY_TRANSLATION_JOB_DESCRIPTION,
                ENGLISH_LANG_CODE, FRENCH_LANG_CODE, null, null, null);
        DummyTranslationObject dummyObject = new DummyTranslationObject();
        InputStream iXLIFFStream = getClass().getResourceAsStream(INPUT_SAMPLE_XLIFF);
        InputStream iXMLStream = getClass().getResourceAsStream(INPUT_SAMPLE_XML);
        InputStream iOutputXMLStream = getClass().getResourceAsStream(OUTPUT_SAMPLE_XML);
        String strOutputXML = getStringFromStream(iOutputXMLStream);
        String xliffFilePathStr = m_importPath + "/" + trnsJobId + "/" + dummyObject.getId() + XLIFF_EXTENSION;
        File file = new File(xliffFilePathStr);
        file.getParentFile().mkdirs();
        if (!file.exists()) {
            file.createNewFile();
        }
        writeStreamToFile(file, iXLIFFStream);
        dummyObject.setTranslationObjectInputStream(iXMLStream);
        dummyObject.setMimeType(DEFAULT_MIME_TYPE);
        InputStream iXMLFileStream = genXliffTransServics.getTranslatedObject(trnsJobId, dummyObject);
        String strCompareOutputXML = getStringFromStream(iXMLFileStream);
        assertTrue(strCompareOutputXML.equals(strOutputXML));
        file.delete();
    }

    private static void writeStreamToFile(File destFile, InputStream inputStream) throws IOException {
        try {
            byte[] buffer = new byte[8 * 1024];
            OutputStream output = new FileOutputStream(destFile.getAbsolutePath());
            try {
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            } finally {
                output.close();
            }
        } finally {
            inputStream.close();
        }

    }

    @Test
    public void testUploadXliffv1Document() throws TranslationException {
        GenericXliffTranslationServiceImpl genXliffTransServicsV1 =
            new GenericXliffTranslationServiceImpl(languageMap, categoryMap, null, m_importPath.toString(),
                m_storePath.toString(), XLIFF_VERSION_1, transConfig, m_htmlParser);
        String trnsJobId =
            genXliffTransServicsV1.createTranslationJob(DUMMY_TRANSLATION_JOB_NAME,
                DUMMY_TRANSLATION_JOB_DESCRIPTION, ENGLISH_LANG_CODE, FRENCH_LANG_CODE, null, null, null);
        DummyTranslationObject dummyObject = new DummyTranslationObject();
        InputStream iStream = getClass().getResourceAsStream(INPUT_SAMPLE_XML);
        dummyObject.setMimeType(DEFAULT_MIME_TYPE);
        dummyObject.setTranslationObjectInputStream(iStream);
        String transObjId = genXliffTransServicsV1.uploadTranslationObject(trnsJobId, dummyObject);
        String xliffFilePathStr = m_storePath + "/" + trnsJobId + "/" + transObjId + XLIFF_EXTENSION;
        Path xliffFilePath = Paths.get(xliffFilePathStr);
        File file = new File(xliffFilePath.toString());
        if (file.length() == 0) {
            assertTrue(false);
        }
        assertTrue(Files.exists(xliffFilePath));

    }

    @AfterClass
    public static void cleanup() {
        File storeFolder = new File(m_storePath.toString());
        File importFolder = new File(m_importPath.toString());
        deleteDir(storeFolder);
        deleteDir(importFolder);
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    @Test
    public void testCreateTranslationJob() throws TranslationException {
        String trnsJobId =
            genXliffTransServics.createTranslationJob(DUMMY_TRANSLATION_JOB_NAME, DUMMY_TRANSLATION_JOB_DESCRIPTION,
                ENGLISH_LANG_CODE, FRENCH_LANG_CODE, null, null, null);
        String trnsJobXmlFilePathStr =
            m_storePath + "/" + trnsJobId + "/" + TRANSLATION_JOB_FILE_NAME + XML_EXTENSION;
        Path trnsJobXmlFilePath = Paths.get(trnsJobXmlFilePathStr);
        File file = new File(trnsJobXmlFilePath.toString());
        if (file.length() == 0) {
            assertTrue(false);
        }
        assertTrue(Files.exists(trnsJobXmlFilePath));
    }
}
