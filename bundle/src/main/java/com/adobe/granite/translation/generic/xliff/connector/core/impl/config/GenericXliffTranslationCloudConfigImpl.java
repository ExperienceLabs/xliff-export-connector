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
package com.adobe.granite.translation.generic.xliff.connector.core.impl.config;

import com.adobe.granite.translation.api.TranslationException;
import com.adobe.granite.translation.generic.xliff.connector.core.GenericXliffTranslationCloudConfig;

import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericXliffTranslationCloudConfigImpl implements GenericXliffTranslationCloudConfig {

    private static final Logger log = LoggerFactory.getLogger(GenericXliffTranslationCloudConfigImpl.class);

    private String translationXMLStorePath;
    private String translationXMLLoadPath;
    private String xliffDocumentVersion; 

    public GenericXliffTranslationCloudConfigImpl(Resource translationConfigResource) throws TranslationException {
        log.trace("Starting constructor: GenericXliffTranslationCloudConfigImpl");
        Resource configContent;
        if (JcrConstants.JCR_CONTENT.equals(translationConfigResource.getName())) {
            configContent = translationConfigResource;
        } else {
            configContent = translationConfigResource.getChild(JcrConstants.JCR_CONTENT);
        }

        if (configContent != null) {
            ValueMap properties = configContent.adaptTo(ValueMap.class);

            this.translationXMLStorePath = properties.get(PROPERTY_TRANSLATION_XML_STORE_PATH, "");
            this.translationXMLLoadPath = properties.get(PROPERTY_TRANSLATION_XML_LOAD_PATH, "");
            this.xliffDocumentVersion = properties.get(XLIFF_DOCUMENT_VERSION, "");

            if (log.isDebugEnabled()) {
                log.debug("Created GenericXliff Cloud Config with the following:");
                log.debug("translationXMLStorePath: {}", translationXMLStorePath);
                log.debug("translationXMLLoadPath: {}", translationXMLLoadPath);
                log.debug("xliffDocumentVersion: {}", xliffDocumentVersion);
            }
        } else {
            throw new TranslationException("Error getting Cloud Config credentials",
                TranslationException.ErrorCode.MISSING_CREDENTIALS);
        }
    }
    
    public String getXliffDocumentVersion(){
    	log.trace("In function: getXliffDocumentVersion");
    	return xliffDocumentVersion;
    }

    public String getXMLStorePath() {
        log.trace("In function: getXMLStorePath");
        return translationXMLStorePath;
    }

    public String getXMLLoadPath() {
        log.trace("In function: getXMLLoadPath");
        return translationXMLLoadPath;
    }
}