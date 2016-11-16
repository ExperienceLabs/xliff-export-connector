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

import java.util.ArrayList;
import java.util.List;

import com.adobe.granite.translation.api.TranslationConstants.TranslationMethod;
import com.adobe.granite.translation.api.TranslationException;
import com.adobe.granite.translation.api.TranslationService;
import com.adobe.granite.translation.api.TranslationServiceFactory;
import com.adobe.granite.translation.core.TranslationCloudConfigUtil;
import com.adobe.granite.translation.core.common.AbstractTranslationServiceFactory;
import com.adobe.granite.translation.generic.xliff.connector.core.GenericXliffTranslationCloudConfig;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.html.HtmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Component(label = "GenericXliff Translation Connector Factory", metatype = true, immediate = true)
@Properties(value = {
    @Property(name = "service.description", value = "GenericXliff translation service"),
    @Property(name = TranslationServiceFactory.PROPERTY_TRANSLATION_FACTORY, value = "Experience labs",
            label = "GenericXliff Translation Factory Name", description = "The Unique ID associated with this "
                    + "Translation Factory Connector")})
public class GenericXliffTranslationServiceFactoryImpl extends AbstractTranslationServiceFactory implements
    TranslationServiceFactory {

    @Reference
    TranslationCloudConfigUtil cloudConfigUtil;

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    @Reference
    HtmlParser htmlParser;

    private List<TranslationMethod> supportedTranslationMethods;

    public GenericXliffTranslationServiceFactoryImpl() {
        supportedTranslationMethods = new ArrayList<TranslationMethod>();
        supportedTranslationMethods.add(TranslationMethod.HUMAN_TRANSLATION);
    }

    private static final Logger log = LoggerFactory.getLogger(GenericXliffTranslationServiceFactoryImpl.class);

    @Override
    public TranslationService createTranslationService(TranslationMethod translationMethod, String cloudConfigPath)
        throws TranslationException {
        log.debug("In function: getTranslationService()");
        GenericXliffTranslationCloudConfig cloudConfg =
            (GenericXliffTranslationCloudConfig) cloudConfigUtil.getCloudConfigObjectFromPath(
                GenericXliffTranslationCloudConfig.class, cloudConfigPath);
        String strXMLLoadPath = "";
        String strXMLStorePath = "";
        String xliffDocumentVersion = "";
        if (cloudConfg != null) {
            strXMLLoadPath = cloudConfg.getXMLLoadPath();
            strXMLStorePath = cloudConfg.getXMLStorePath();
            xliffDocumentVersion = cloudConfg.getXliffDocumentVersion();
        }
        return new GenericXliffTranslationServiceImpl(null, null, factoryName, strXMLLoadPath, strXMLStorePath,
            xliffDocumentVersion, null, htmlParser);
    }

    @Override
    public List<TranslationMethod> getSupportedTranslationMethods() {
        return supportedTranslationMethods;
    }

    @Override
    public Class<?> getServiceCloudConfigClass() {
        return GenericXliffTranslationCloudConfig.class;
    }

}