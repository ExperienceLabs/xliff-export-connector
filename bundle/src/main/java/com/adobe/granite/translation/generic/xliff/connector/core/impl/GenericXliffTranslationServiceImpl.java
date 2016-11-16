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
package com.adobe.granite.translation.generic.xliff.connector.core.impl;

import com.adobe.granite.comments.Comment;
import com.adobe.granite.comments.CommentCollection;
import com.adobe.granite.translation.api.TranslationObject;
import com.adobe.granite.translation.api.TranslationConstants;
import com.adobe.granite.translation.api.TranslationConstants.TranslationMethod;
import com.adobe.granite.translation.api.TranslationConstants.TranslationStatus;
import com.adobe.granite.translation.api.TranslationException;
import com.adobe.granite.translation.api.TranslationMetadata;
import com.adobe.granite.translation.api.TranslationResult;
import com.adobe.granite.translation.api.TranslationScope;
import com.adobe.granite.translation.api.TranslationService;
import com.adobe.granite.translation.api.TranslationConfig;
import com.adobe.granite.translation.api.TranslationState;
import com.adobe.granite.translation.core.common.AbstractTranslationService;
import com.adobe.granite.translation.generic.xliff.connector.core.GenericXliffTranslationCloudConfig;

import net.sf.okapi.lib.xliff2.core.CTag;
import net.sf.okapi.lib.xliff2.core.Fragment;
import net.sf.okapi.lib.xliff2.core.Segment;
import net.sf.okapi.lib.xliff2.core.StartFileData;
import net.sf.okapi.lib.xliff2.core.TagType;
import net.sf.okapi.lib.xliff2.core.Unit;
import net.sf.okapi.lib.xliff2.reader.Event;
import net.sf.okapi.lib.xliff2.reader.XLIFFReader;
import net.sf.okapi.lib.xliff2.writer.XLIFFWriter;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.filters.xliff.XLIFFFilter;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.commons.html.HtmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class GenericXliffTranslationServiceImpl extends AbstractTranslationService implements TranslationService {

    private static final String XLIFF_EXTENSION = ".xlf";
    private static final String XLIFF_VERSION_2 = "2.0";

    class TranslationJobDetails {
        String strName;
        String strDescprition;
        String strSourceLang;
        String strDestinationLang;
    };

    class SourceWithTranslation {
        String strSource;
        String strTranslation;
    };

    private static final String SERVICE_LABEL = "Experience labs";
    private static final String SERVICE_ATTRIBUTION = "Generic XLIFF Attribution";
    private String m_strXMLStorePath;
    private String m_strXMLLoadPath;
    private String m_xliffDocumentVersion;
    private HtmlParser m_htmlParser;

    private static final Logger log = LoggerFactory.getLogger(GenericXliffTranslationServiceImpl.class);

    private static String TRANSLATION_OBJECT_FILE_NODE = "translationObjectFile";
    private static String ATTRIBUTE_JOB_NAME = "name";
    private static String ATTRIBUTE_JOB_DESCRIPTION = "description";
    private static String ATTRIBUTE_JOB_SOURCE_LANG = "srcLang";
    private static String ATTRIBUTE_JOB_DEST_LANG = "destLang";
    private static String TRANSLATION_JOB_FILE_NAME = "translation_job_file";
    private static String TRANSLATION_OBJECT_PROPERTIES_NODE = "translationObjectProperties";
    public static String TRANSLATION_OBJECT_PROPERTY_NODE = "property";
    public static String TRANSLATION_OBJECT_VALUE_NODE = "value";
    public static String ATTRIBUTE_TRANSLATION_NODE_PATH = "nodePath";
    public static String ATTRIBUTE_TRANSLATION_PROPERTY_NAME = "propertyName";
    public static String ATTRIBUTE_TRANSLATION_MULTI_VALUE = "isMultiValue";
    private static final String UTF_8_ENCODING = "UTF-8";
    private static final String EMPTY_UNIT_ID = "0";

    class TranslationScopeImpl implements TranslationScope {

        @Override
        public int getWordCount() {
            return 0;
        }

        @Override
        public int getImageCount() {
            return 0;
        }

        @Override
        public int getVideoCount() {
            return 0;
        }

        @Override
        public Map<String, String> getFinalScope() {
            Map<String, String> newScope = new HashMap<String, String>();
            newScope.put("scope", "Not Implemented");
            return newScope;
        }

    }

    // Constructor
    public GenericXliffTranslationServiceImpl(Map<String, String> availableLanguageMap,
        Map<String, String> availableCategoryMap, String name, String strXMLLoadPath, String strXMLStorePath,
        String xliffDocumentVesion, TranslationConfig tc, HtmlParser htmlParser) {
        super(availableLanguageMap, availableCategoryMap, name, SERVICE_LABEL, SERVICE_ATTRIBUTION,
            GenericXliffTranslationCloudConfig.ROOT_PATH, TranslationMethod.HUMAN_TRANSLATION, tc);
        m_strXMLLoadPath = strXMLLoadPath;
        m_strXMLStorePath = strXMLStorePath;
        m_xliffDocumentVersion = xliffDocumentVesion;
        this.m_htmlParser = htmlParser;
        log.debug("Starting Constructor for: GenericXliffTranslationServiceImpl");
    }

    @Override
    public Map<String, String> supportedLanguages() {
        log.debug("In Function: supportedLanguages");
        if (availableLanguageMap.size() <= 0) {
            availableLanguageMap.put("en", "en");
            availableLanguageMap.put("de", "de");
            availableLanguageMap.put("fr", "fr");
            availableLanguageMap.put("ja", "ja");
            availableLanguageMap.put("ko", "ko");
        }
        return Collections.unmodifiableMap(availableLanguageMap);
    }

    @Override
    public boolean isDirectionSupported(String sourceLanguage, String targetLanguage) throws TranslationException {
        log.debug("In Function: isSupportedDirection");
        return true;
    }

    @Override
    public String detectLanguage(String toDetectSource, TranslationConstants.ContentType contentType)
        throws TranslationException {
        throw new TranslationException("This function is not implemented",
            TranslationException.ErrorCode.SERVICE_NOT_IMPLEMENTED);
    }

    @Override
    public TranslationResult translateString(String sourceString, String sourceLanguage, String targetLanguage,
        TranslationConstants.ContentType contentType, String contentCategory) throws TranslationException {
        throw new TranslationException("This function is not implemented",
            TranslationException.ErrorCode.SERVICE_NOT_IMPLEMENTED);
    }

    @Override
    public TranslationResult[] translateArray(String[] sourceStringArr, String sourceLanguage, String targetLanguage,
        TranslationConstants.ContentType contentType, String contentCategory) throws TranslationException {
        throw new TranslationException("This function is not implemented",
            TranslationException.ErrorCode.SERVICE_NOT_IMPLEMENTED);
    }

    @Override
    public TranslationResult[] getAllStoredTranslations(String sourceString, String sourceLanguage,
        String targetLanguage, TranslationConstants.ContentType contentType, String contentCategory, String userId,
        int maxTranslations) throws TranslationException {
        throw new TranslationException("This function is not implemented",
            TranslationException.ErrorCode.SERVICE_NOT_IMPLEMENTED);

    }

    @Override
    public void storeTranslation(String[] originalText, String sourceLanguage, String targetLanguage,
        String[] updatedTranslation, TranslationConstants.ContentType contentType, String contentCategory,
        String userId, int rating, String path) throws TranslationException {
        throw new TranslationException("This function is not implemented",
            TranslationException.ErrorCode.SERVICE_NOT_IMPLEMENTED);
    }

    @Override
    public void storeTranslation(String originalText, String sourceLanguage, String targetLanguage,
        String updatedTranslation, TranslationConstants.ContentType contentType, String contentCategory,
        String userId, int rating, String path) throws TranslationException {
        throw new TranslationException("This function is not implemented",
            TranslationException.ErrorCode.SERVICE_NOT_IMPLEMENTED);

    }

    @Override
    public String createTranslationJob(String name, String description, String strSourceLanguage,
        String strTargetLanguage, Date dueDate, TranslationState state, TranslationMetadata jobMetadata)
        throws TranslationException {
        String strTranslationJobID = generateUniqueID();
        // store all this information in XML file for future use
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            Document currentDoc = db.newDocument();
            // first add the basic information to the translation file node
            Element rootElement = currentDoc.createElement(TRANSLATION_OBJECT_FILE_NODE);
            rootElement.setAttribute(ATTRIBUTE_JOB_NAME, name);
            rootElement.setAttribute(ATTRIBUTE_JOB_DESCRIPTION, description);
            rootElement.setAttribute(ATTRIBUTE_JOB_SOURCE_LANG, strSourceLanguage);
            rootElement.setAttribute(ATTRIBUTE_JOB_DEST_LANG, strTargetLanguage);
            currentDoc.appendChild(rootElement);
            String strOutput = convertXMLDocToString(currentDoc);
            String strFilePath =
                getTranslationObjectFilePath(m_strXMLStorePath, strTranslationJobID, TRANSLATION_JOB_FILE_NAME,
                    ".xml");
            writeToFile(strFilePath, strOutput);
            return strTranslationJobID;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TranslationException("Error while creating Translation Job",
                TranslationException.ErrorCode.GENERAL_EXCEPTION);
        }
    }

    private TranslationJobDetails getTranslationJobDetails(String strBasePath, String strTranslationJobID)
        throws ParserConfigurationException, SAXException, IOException {
        TranslationJobDetails translationjob = null;
        String strFilePath =
            getTranslationObjectFilePath(strBasePath, strTranslationJobID, TRANSLATION_JOB_FILE_NAME, ".xml");
        File file = new File(strFilePath);
        if (file.exists()) {
            FileInputStream inputStream = new FileInputStream(file);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(inputStream);
            org.w3c.dom.Node rootNode = doc.getFirstChild();
            Element rootElement = (Element) rootNode;
            translationjob = new TranslationJobDetails();
            translationjob.strName = rootElement.getAttribute(ATTRIBUTE_JOB_NAME);
            translationjob.strDescprition = rootElement.getAttribute(ATTRIBUTE_JOB_DESCRIPTION);
            translationjob.strSourceLang = rootElement.getAttribute(ATTRIBUTE_JOB_SOURCE_LANG);
            translationjob.strDestinationLang = rootElement.getAttribute(ATTRIBUTE_JOB_DEST_LANG);
        }
        return translationjob;
    }

    private static String convertXMLDocToString(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        doc.setXmlStandalone(true);
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString();
    }

    @Override
    public TranslationScope getFinalScope(String strTranslationJobID) {
        return new TranslationScopeImpl();
    }

    @Override
    public TranslationStatus updateTranslationJobState(String strTranslationJobID, TranslationState state)
        throws TranslationException {
        if (state.getStatus() == TranslationStatus.SCOPE_REQUESTED) {
            throw new TranslationException("Scope is not supported",
                TranslationException.ErrorCode.SERVICE_NOT_IMPLEMENTED);
        }
        return state.getStatus();
    }

    @Override
    public TranslationStatus getTranslationJobStatus(String strTranslationJobID) throws TranslationException {
        return TranslationStatus.SCOPE_COMPLETED;
    }

    @Override
    public CommentCollection<Comment> getTranslationJobCommentCollection(String strTranslationJobID) {
        return null;
    }

    @Override
    public void addTranslationJobComment(String strTranslationJobID, Comment comment) throws TranslationException {
        throw new TranslationException("This function is not implemented",
            TranslationException.ErrorCode.SERVICE_NOT_IMPLEMENTED);
    }

    @Override
    public InputStream getTranslatedObject(String strTranslationJobID, TranslationObject translationObject)
        throws TranslationException {
        String strExtension = getTranslationObjectExtenstion(translationObject);
        String strFilePath =
            getTranslationObjectFilePath(m_strXMLLoadPath, strTranslationJobID, translationObject.getId(),
                strExtension);
        File file = new File(strFilePath);
        InputStream inputStream = null;
        if (file.exists()) {
            try {
                if (isTranslationObjectAssetType(translationObject)) {
                    // in this case just return the file input stream
                    inputStream = new FileInputStream(file);
                } else {
                    inputStream = getTranslatedXMLFromFile(strTranslationJobID, translationObject, file);
                }
            } catch (Exception ex) {
                throw new TranslationException("Translation Error while getting translated file",
                    TranslationException.ErrorCode.GENERAL_EXCEPTION);
            }
        } else {
            throw new TranslationException("Translation File not present",
                TranslationException.ErrorCode.GENERAL_EXCEPTION);
        }
        return inputStream;
    }

    private InputStream getTranslatedXMLFromFile(String strTranslationJobID, TranslationObject translationObject,
        File inputFile) throws TranslationException, ParserConfigurationException, SAXException, IOException,
        TransformerException {
        InputStream retStream = null;
        if (inputFile.exists()) {
            HashMap<String, ArrayList<SourceWithTranslation>> keyMap =
                new HashMap<String, ArrayList<SourceWithTranslation>>();

            if (m_xliffDocumentVersion.equals(XLIFF_VERSION_2)) {
                XLIFFReader reader = new XLIFFReader();
                reader.open(inputFile);
                // Loop through the reader events
                while (reader.hasNext()) {
                    Event event = reader.next();
                    // Do something: here print the source content
                    if (event.isUnit()) {
                        Unit unit = event.getUnit();
                        ArrayList<SourceWithTranslation> strList = new ArrayList<SourceWithTranslation>();
                        String strUnitID = unit.getId();
                        SourceWithTranslation val = new SourceWithTranslation();
                        StringBuilder strSource = new StringBuilder();
                        StringBuilder strTranslation = new StringBuilder();
                        for (Segment segment : unit.getSegments()) {
                            strSource.append(getDecodedTextBackFromSegment(unit, segment.getSource()));
                            strTranslation.append(getDecodedTextBackFromSegment(unit, segment.getTarget()));
                        }
                        val.strSource = strSource.toString();
                        val.strTranslation = strTranslation.toString();
                        strList.add(val);
                        keyMap.put(strUnitID, strList);
                    } else if (event.isStartFile()) {
                        StartFileData startFileData = event.getStartFileData();
                        String strFileID = startFileData.getId();
                        if (!strFileID.equals(translationObject.getId())) {
                            // not same, we should raise exception
                            reader.close();
                            throw new TranslationException("Translation File is incorrect",
                                TranslationException.ErrorCode.GENERAL_EXCEPTION);
                        }
                    }
                }
                reader.close();

            } else {
                XLIFFFilter xliffReader = new XLIFFFilter();
                InputStream inputStream = new FileInputStream(inputFile);
                TranslationJobDetails translationJobDetail =
                    getTranslationJobDetails(m_strXMLStorePath, strTranslationJobID);
                xliffReader.open(new RawDocument(inputStream, UTF_8_ENCODING, new LocaleId(
                    translationJobDetail.strSourceLang), new LocaleId(translationJobDetail.strDestinationLang)));

                while (xliffReader.hasNext()) {
                    net.sf.okapi.common.Event event = xliffReader.next();

                    if (event.isTextUnit()) {
                        TextUnit textUnit = (TextUnit) event.getTextUnit();
                        ArrayList<SourceWithTranslation> strList = new ArrayList<SourceWithTranslation>();
                        String strTextUnitID = textUnit.getId();
                        SourceWithTranslation val = new SourceWithTranslation();
                        val.strSource = textUnit.getSource().getFirstContent().getText();
                        val.strTranslation =
                            textUnit.getTarget(new LocaleId(translationJobDetail.strDestinationLang))
                                .getFirstContent().getText();
                        strList.add(val);
                        keyMap.put(strTextUnitID, strList);
                    } else if (event.isStartDocument()) {
                        StartDocument startDocumentData = event.getStartDocument();
                        String strDocumentID = startDocumentData.getId();
                        if (strDocumentID.equals(translationObject.getId())) {
                            // not same, we should raise exception
                            xliffReader.close();
                            throw new TranslationException("Translation File is incorrect",
                                TranslationException.ErrorCode.GENERAL_EXCEPTION);
                        }
                    }

                }
            }

            // we have all translation, now do the XML fitting
            InputStream inputStream = translationObject.getTranslationObjectInputStream();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(inputStream);
            org.w3c.dom.Node rootNode = doc.getFirstChild();
            NodeList rootChildNodeList = rootNode.getChildNodes();
            for (int rootIndex = 0; rootIndex < rootChildNodeList.getLength(); rootIndex++) {
                org.w3c.dom.Node propertiesNode = rootChildNodeList.item(rootIndex);
                if (propertiesNode != null && TRANSLATION_OBJECT_PROPERTIES_NODE.equals(propertiesNode.getNodeName())) {
                    // we need to start the process now
                    NodeList nodeList = propertiesNode.getChildNodes();
                    for (int index = 0; index < nodeList.getLength(); index++) {
                        org.w3c.dom.Node childNode = nodeList.item(index);
                        processPropertiesForTranslation(childNode, keyMap);
                    }
                }
            }
            inputStream.close();
            String strOutput = convertXMLDocToString(doc);
            retStream = new ByteArrayInputStream(strOutput.getBytes("UTF-8"));
        }
        return retStream;
    }

    private String getDecodedTextBackFromSegment(Unit unit, Fragment fragment) {
        String strPlainText = fragment.getPlainText();
        if (StringUtils.equals(strPlainText, fragment.getCodedText())) {
            return strPlainText;
        }
        StringBuilder strOutput = new StringBuilder();
        Iterator<Object> itr = fragment.iterator();
        while (itr.hasNext()) {
            Object obj = itr.next();
            if (obj instanceof String) {
                strOutput.append(obj.toString());
            } else if (obj instanceof CTag) {
                CTag cTag = (CTag) obj;
                if (cTag != null) {
                    strOutput.append(cTag.getData());
                }
            } else {
                log.info("Check why it came here {}", obj);
            }
        }
        return strOutput.toString();
    }

    private void processPropertiesForTranslation(Node childNode,
        HashMap<String, ArrayList<SourceWithTranslation>> keyMap) {
        if (childNode != null && TRANSLATION_OBJECT_PROPERTY_NODE.equals(childNode.getNodeName())) {
            Element childElement = (Element) childNode;
            String nodePath = childElement.getAttribute(ATTRIBUTE_TRANSLATION_NODE_PATH);
            String propertyName = childElement.getAttribute(ATTRIBUTE_TRANSLATION_PROPERTY_NAME);
            String isMultiValue = childElement.getAttribute(ATTRIBUTE_TRANSLATION_MULTI_VALUE);
            boolean bMultiValue = "true".equals(isMultiValue);
            String strUnitID = getXLIFFUnitID(nodePath, propertyName, isMultiValue);
            ArrayList<SourceWithTranslation> translationList = keyMap.get(strUnitID);
            if (translationList != null) {
                if (bMultiValue) {
                    // in this case we need to get the value in one order and
                    NodeList nodeList = childNode.getChildNodes();
                    for (int index = 0; index < nodeList.getLength(); index++) {
                        org.w3c.dom.Node valueNode = nodeList.item(index);
                        if (TRANSLATION_OBJECT_VALUE_NODE.equals(valueNode.getNodeName())) {
                            valueNode.setTextContent(translationList.get(index).strTranslation);
                        }
                    }
                } else {
                    childElement.setTextContent(translationList.get(0).strTranslation);
                }
            } else {
                log.error("Some Translation is missing in the File for path {}", strUnitID);
            }
        }
    }

    public static String writeToFile(String strFilePath, String strOutput) throws IOException {
        File file = new File(strFilePath);
        file.getParentFile().mkdirs();
        if (!file.exists()) {
            file.createNewFile();
        }
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()), UTF_8_ENCODING);
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(strOutput);
        bw.close();
        return file.getAbsolutePath();
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

    public static String getFileExtension(String strFileName, String strMimeType) {
        String strExtension = "";
        if (strFileName != null) {
            int index = strFileName.lastIndexOf("/");
            if (index != -1) {
                strFileName = strFileName.substring(index);
            }
            index = strFileName.lastIndexOf(".");
            if (index != -1) {
                strExtension = strFileName.substring(index);
            }
        }
        if (StringUtils.isEmpty(strExtension) && !StringUtils.isEmpty(strMimeType)) {
            // in case we are not able to find extension from filename, try mime type
            // image/jpeg will be .jpeg
            int index = strMimeType.lastIndexOf("/");
            if (index != -1) {
                strExtension = "." + strMimeType.substring(index + 1);
            }
        }
        return strExtension;
    }

    private boolean isTranslationObjectAssetType(TranslationObject translationObject) {
        String strTranslationObjectMimeType = translationObject.getMimeType();
        return !("text/html".equals(strTranslationObjectMimeType) || "text/xml".equals(strTranslationObjectMimeType));
    }

    @Override
    public String uploadTranslationObject(String strTranslationJobID, TranslationObject translationObject)
        throws TranslationException {
        String strTranslationObjectID = generateUniqueID();
        InputStream inputStream = translationObject.getTranslationObjectInputStream();
        String strExtension = getTranslationObjectExtenstion(translationObject);
        try {
            // save this inputstream now
            String strOutputFilePath =
                getTranslationObjectFilePath(m_strXMLStorePath, strTranslationJobID, strTranslationObjectID,
                    strExtension);
            File destFile = new File(strOutputFilePath);
            TranslationJobDetails jobDetails = getTranslationJobDetails(m_strXMLStorePath, strTranslationJobID);
            if (!isTranslationObjectAssetType(translationObject)) {
                // we should process XML and generate XLIFF now
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(inputStream);
                org.w3c.dom.Node rootNode = doc.getFirstChild();
                if (m_xliffDocumentVersion.equals(XLIFF_VERSION_2)) {
                    writeXliff2Document(destFile, jobDetails, strTranslationObjectID, rootNode);
                } else {
                    writeXliffDocument(strOutputFilePath, jobDetails, translationObject, rootNode);
                }
            } else {
                // write asset as it is
                writeStreamToFile(destFile, inputStream);
            }
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strTranslationObjectID;
    }

    private void writeXliff2Document(File destFile, TranslationJobDetails jobDetails, String strTranslationObjectID,
        org.w3c.dom.Node rootNode) {
        XLIFFWriter writer = new XLIFFWriter();
        writer.create(destFile, jobDetails.strSourceLang, jobDetails.strDestinationLang);
        writer.setWithOriginalData(true);
        StartFileData newFileData = new StartFileData(strTranslationObjectID);
        writer.writeStartFile(newFileData);
        processXMLNode(rootNode, writer, null);
        writer.writeEndFile();
        writer.close();
    }

    private void writeXliffDocument(String xmlStorePath, TranslationJobDetails jobDetails,
        TranslationObject transObj, org.w3c.dom.Node rootNode) {
        net.sf.okapi.common.filterwriter.XLIFFWriter xlffWriter = new net.sf.okapi.common.filterwriter.XLIFFWriter();
        xlffWriter.create(xmlStorePath, null, new LocaleId(jobDetails.strSourceLang), new LocaleId(
            jobDetails.strDestinationLang), transObj.getMimeType(), null, null);
        xlffWriter.writeStartFile(null, transObj.getMimeType(), null);
        processXMLNode(rootNode, null, xlffWriter);
        xlffWriter.writeEndFile();
        xlffWriter.close();
    }

    private String generateUniqueID() {
        String strID = UUID.randomUUID().toString();
        return convertStringToNumber(strID);
    }

    private String convertStringToNumber(String strText) {
        BigInteger bigInt = new BigInteger(strText.getBytes());
        return bigInt.toString();
    }

    private void processXMLNode(Node rootNode, XLIFFWriter writer,
        net.sf.okapi.common.filterwriter.XLIFFWriter xliffWriter) {
        boolean bEmpty = true;
        NodeList rootChildNodeList = rootNode.getChildNodes();
        for (int rootIndex = 0; rootIndex < rootChildNodeList.getLength(); rootIndex++) {
            org.w3c.dom.Node propertiesNode = rootChildNodeList.item(rootIndex);
            if (propertiesNode != null && TRANSLATION_OBJECT_PROPERTIES_NODE.equals(propertiesNode.getNodeName())) {
                // we need to start the process now
                NodeList nodeList = propertiesNode.getChildNodes();
                for (int index = 0; index < nodeList.getLength(); index++) {
                    org.w3c.dom.Node childNode = nodeList.item(index);
                    processChildProperty(childNode, writer, xliffWriter);
                    bEmpty = false;
                }
            }
        }
        if (bEmpty) {
            // add empty unit
            Unit unit = null;
            TextUnit textUnit = null;
            if (m_xliffDocumentVersion.equals(XLIFF_VERSION_2)) {
                unit = new Unit(EMPTY_UNIT_ID);
                appendSegment(unit, textUnit, "");
                writer.writeUnit(unit);
            } else {
                textUnit = new TextUnit(EMPTY_UNIT_ID);
                appendSegment(unit, textUnit, "");
                xliffWriter.writeTextUnit(textUnit, "");
            }

        }
    }

    private void processChildProperty(Node childNode, XLIFFWriter writer,
        net.sf.okapi.common.filterwriter.XLIFFWriter xliffWriter) {
        if (childNode != null && TRANSLATION_OBJECT_PROPERTY_NODE.equals(childNode.getNodeName())) {
            // first check if the path is within the root page or not
            Element childElement = (Element) childNode;
            String nodePath = childElement.getAttribute(ATTRIBUTE_TRANSLATION_NODE_PATH);
            String propertyName = childElement.getAttribute(ATTRIBUTE_TRANSLATION_PROPERTY_NAME);
            String isMultiValue = childElement.getAttribute(ATTRIBUTE_TRANSLATION_MULTI_VALUE);
            boolean bMultiValue = "true".equals(isMultiValue);
            String strUnitID = getXLIFFUnitID(nodePath, propertyName, isMultiValue);
            Unit unit = null;
            TextUnit textUnit = null;
            if (m_xliffDocumentVersion.equals(XLIFF_VERSION_2)) {
                unit = new Unit(strUnitID);
            } else {
                textUnit = new TextUnit(strUnitID);
            }

            if (bMultiValue) {
                // in this case we need to get the value in one order and
                NodeList nodeList = childNode.getChildNodes();
                for (int index = 0; index < nodeList.getLength(); index++) {
                    org.w3c.dom.Node valueNode = nodeList.item(index);
                    if (TRANSLATION_OBJECT_VALUE_NODE.equals(valueNode.getNodeName())) {
                        appendSegment(unit, textUnit, valueNode.getTextContent());
                    }
                }
            } else {
                appendSegment(unit, textUnit, childElement.getTextContent());
            }

            if (unit != null)
                writer.writeUnit(unit);
            else
                xliffWriter.writeTextUnit(textUnit);

        }
    }

    private void appendSegment(Unit unit, TextUnit textUnit, String textContent) {
        // first check if text Content has some markup or not
        NodeList childList = null;
        if (!StringUtils.isEmpty(textContent) && textContent.indexOf("<") != -1 && textContent.indexOf("/") != -1
                && textContent.indexOf(">") != -1) {
            // we need to process this text now
            childList = processXMLTextOutput(textContent);
        }
        if (unit != null) {
            Segment segment = unit.appendSegment();
            Fragment content = segment.getSource();
            if (childList == null || childList.getLength() == 0) {
                content.append(textContent);
            } else {
                // we have to add each child now
                processFragmentXMLChildList(content, childList);
            }
        } else {
            textUnit.getSource().append(textContent);
        }

    }

    private void processFragmentXMLChildList(Fragment content, NodeList childList) {
        for (int index = 0; index < childList.getLength(); index++) {
            Node childItem = childList.item(index);
            if (childItem.getNodeType() == Node.ELEMENT_NODE) {
                String strAttributeMap = "";
                String strTagName = childItem.getNodeName();
                // get all attributes
                NamedNodeMap attributes = childItem.getAttributes();
                if (attributes != null && attributes.getLength() > 0) {
                    for (int iCount = 0; iCount < attributes.getLength(); iCount++) {
                        Node attNode = attributes.item(iCount);
                        String strAttributeName = attNode.getNodeName();
                        String strAttValue = attNode.getNodeValue();
                        strAttributeMap += String.format("%s=\"%s\" ", strAttributeName, strAttValue);
                    }
                }
                NodeList newChildList = childItem.getChildNodes();
                String strKeyValue = "";
                if (newChildList != null && newChildList.getLength() > 0) {
                    if (StringUtils.isEmpty(strAttributeMap)) {
                        strKeyValue = String.format("<%s>", strTagName);
                    } else {
                        strKeyValue = String.format("<%s %s>", strTagName, strAttributeMap);
                    }
                    content.append(TagType.OPENING, null, strKeyValue, false);
                    processFragmentXMLChildList(content, newChildList);
                    strKeyValue = String.format("</%s>", strTagName);
                    content.append(TagType.CLOSING, null, strKeyValue, false);
                } else {
                    // this is stand alone tag
                    if (StringUtils.isEmpty(strAttributeMap)) {
                        strKeyValue = String.format("<%s/>", strTagName);
                    } else {
                        strKeyValue = String.format("<%s %s/>", strTagName, strAttributeMap);
                    }
                    content.append(TagType.STANDALONE, null, strKeyValue, false);
                }
            } else {
                content.append(childItem.getTextContent());
            }
        }
    }

    private NodeList processXMLTextOutput(String textContent) {
        String strXML = String.format("<root_item>%s</root_item>", textContent);
        try {
            Document document = m_htmlParser.parse(null, new ByteArrayInputStream(strXML.getBytes()), UTF_8_ENCODING);
            return document.getFirstChild().getChildNodes();
        } catch (Exception ex) {
            log.info("Error while processing rich text output {}", ex);
        }
        return null;
    }

    private String getXLIFFUnitID(String nodePath, String propertyName, String isMultiValue) {
        String strText = String.format("%s*%s*%s", nodePath, propertyName, isMultiValue);
        return convertStringToNumber(strText);
    }

    private String getTranslationObjectFilePath(String strXMLBasePath, String strTranslationJobID,
        String strTranslationObjectID, String strExtension) {
        File baseDir = new File(strXMLBasePath + File.separator + strTranslationJobID);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        return strXMLBasePath + File.separator + strTranslationJobID + File.separator + strTranslationObjectID
                + strExtension;
    }

    @Override
    public TranslationStatus updateTranslationObjectState(String strTranslationJobID,
        TranslationObject translationObject, TranslationState state) throws TranslationException {
        return state.getStatus();
    }

    @Override
    public TranslationStatus getTranslationObjectStatus(String strTranslationJobID,
        TranslationObject translationObject) throws TranslationException {
        String strExtension = getTranslationObjectExtenstion(translationObject);
        String strFilePath =
            getTranslationObjectFilePath(m_strXMLLoadPath, strTranslationJobID, translationObject.getId(),
                strExtension);

        File file = new File(strFilePath);
        TranslationStatus status =
            file.exists() ? TranslationStatus.TRANSLATED : TranslationStatus.TRANSLATION_IN_PROGRESS;
        if (!isTranslationObjectAssetType(translationObject) && file.exists()) {
            // we need to check if the file is valid enough or not
            status =
                isTranslationXLIFFV2FileValid(file, strTranslationJobID) ? TranslationStatus.TRANSLATED
                    : TranslationStatus.ERROR_UPDATE;
        }
        return status;
    }

    private boolean isTranslationXLIFFV1FileValid(File inputFile, String strTranslationJobID) {
        boolean bValid = true;
        try {

            XLIFFFilter reader = new XLIFFFilter();
            InputStream inputStream = new FileInputStream(inputFile);
            TranslationJobDetails translationJobDetail =
                getTranslationJobDetails(m_strXMLStorePath, strTranslationJobID);
            RawDocument rawDoc =
                new RawDocument(inputStream, "UTF-8", new LocaleId(translationJobDetail.strSourceLang), new LocaleId(
                    translationJobDetail.strDestinationLang));
            reader.open(rawDoc);
            while (reader.hasNext()) {
                net.sf.okapi.common.Event event = reader.next();

                if (event.isTextUnit()) {
                    TextUnit textUnit = (TextUnit) event.getTextUnit();
                    if (!textUnit.getId().equals(EMPTY_UNIT_ID)) {
                        if (textUnit.getTarget(new LocaleId(translationJobDetail.strDestinationLang)) == null
                                || textUnit.getTarget(new LocaleId(translationJobDetail.strDestinationLang))
                                    .isEmpty()) {
                            bValid = false;
                        }

                    }
                }
            }
        } catch (Exception ex) {
            bValid = false;
        }
        return bValid;
    }

    private boolean isTranslationXLIFFV2FileValid(File inputFile, String strTranslationJobID) {
        if (!m_xliffDocumentVersion.equals(XLIFF_VERSION_2)) {
            return isTranslationXLIFFV1FileValid(inputFile, strTranslationJobID);
        }
        boolean bValid = true;
        try {

            XLIFFReader reader = new XLIFFReader();
            reader.open(inputFile);
            while (reader.hasNext()) {
                Event event = reader.next();
                // Do something: here print the source content
                if (event.isUnit()) {
                    Unit unit = event.getUnit();
                    if (!unit.getId().equals(EMPTY_UNIT_ID)) {
                        for (Segment segment : unit.getSegments()) {
                            if (segment.getTarget() == null
                                    || StringUtils.isEmpty(segment.getTarget().getPlainText())) {
                                bValid = false;
                                break;
                            }
                        }
                    }
                }
            }
            reader.close();
        } catch (Exception ex) {
            bValid = false;
        }
        return bValid;
    }

    private String getTranslationObjectExtenstion(TranslationObject translationObject) {
        String strExtension = XLIFF_EXTENSION;
        String strTranslationObjectMimeType = translationObject.getMimeType();
        if (!("text/html".equals(strTranslationObjectMimeType) || "text/xml".equals(strTranslationObjectMimeType))) {
            // it is a Asset type, we just need to store it as it is
            strExtension =
                getFileExtension(translationObject.getTranslationObjectSourcePath(), translationObject.getMimeType());
        }
        return strExtension;
    }

    @Override
    public TranslationStatus[] updateTranslationObjectsState(String strTranslationJobID,
        TranslationObject[] translationObjects, TranslationState[] states) throws TranslationException {
        TranslationStatus[] retStatus = new TranslationStatus[states.length];
        for (int index = 0; index < states.length; index++) {
            retStatus[index] =
                updateTranslationObjectState(strTranslationJobID, translationObjects[index], states[index]);
        }
        return retStatus;
    }

    @Override
    public TranslationStatus[] getTranslationObjectsStatus(String strTranslationJobID,
        TranslationObject[] translationObjects) throws TranslationException {
        TranslationStatus[] retStatus = new TranslationStatus[translationObjects.length];
        for (int index = 0; index < translationObjects.length; index++) {
            retStatus[index] = getTranslationObjectStatus(strTranslationJobID, translationObjects[index]);
        }
        return retStatus;
    }

    @Override
    public CommentCollection<Comment> getTranslationObjectCommentCollection(String strTranslationJobID,
        TranslationObject translationObject) throws TranslationException {
        throw new TranslationException("This function is not implemented",
            TranslationException.ErrorCode.SERVICE_NOT_IMPLEMENTED);
    }

    @Override
    public void addTranslationObjectComment(String strTranslationJobID, TranslationObject translationObject,
        Comment comment) throws TranslationException {
        throw new TranslationException("This function is not implemented",
            TranslationException.ErrorCode.SERVICE_NOT_IMPLEMENTED);
    }

    @Override
    public void updateTranslationJobMetadata(String strTranslationJobID, TranslationMetadata jobMetadata,
        TranslationMethod translationMethod) throws TranslationException {
        throw new TranslationException("This function is not implemented",
            TranslationException.ErrorCode.SERVICE_NOT_IMPLEMENTED);
    }

}