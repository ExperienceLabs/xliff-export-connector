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
import com.adobe.granite.translation.core.TranslationCloudConfigUtil;
import com.adobe.granite.translation.generic.xliff.connector.core.GenericXliffTranslationCloudConfig;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;

@Component
@Service(value = AdapterFactory.class)
public class GenericXliffTranslationAdapterFactory implements AdapterFactory {

    @Reference
    TranslationCloudConfigUtil cloudConfigUtil;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final Class<Resource> RESOURCE_CLASS = Resource.class;
    private static final Class<Node> NODE_CLASS = Node.class;
    private static final Class<GenericXliffTranslationCloudConfig> XLIFF_TRANSLATION_CLOUD_CONFIG_CLASS =
        GenericXliffTranslationCloudConfig.class;

    @Property(name = "adapters")
    protected static final String[] ADAPTER_CLASSES = {XLIFF_TRANSLATION_CLOUD_CONFIG_CLASS.getName()};

    @Property(name = "adaptables")
    protected static final String[] ADAPTABLE_CLASSES = {RESOURCE_CLASS.getName(), NODE_CLASS.getName()};

    // ---------- AdapterFactory -----------------------------------------------

    public <AdapterType> AdapterType getAdapter(Object adaptable, Class<AdapterType> type) {
        log.trace("In function: getAdapter(Object,Claass<AdapterType>");
        if (adaptable instanceof Resource) {
            return getAdapter((Resource) adaptable, type);
        }
        if (adaptable instanceof Node) {
            return getAdapter((Node) adaptable, type);
        }
        log.debug("Unable to handle adaptable {}", adaptable.getClass().getName());
        return null;
    }

    public void setTranslationCloudConfigUtil(TranslationCloudConfigUtil configUtil) {
        cloudConfigUtil = configUtil;
    }

    /*
     * Adapter for Resource
     */
    @SuppressWarnings("unchecked")
    private <AdapterType> AdapterType getAdapter(Resource resource, Class<AdapterType> type) {
        log.trace("In function: getAdapter(Resource,Class<AdapterType>");
        if (type == XLIFF_TRANSLATION_CLOUD_CONFIG_CLASS
                && cloudConfigUtil.isCloudConfigAppliedOnImmediateResource(resource,
                    GenericXliffTranslationCloudConfig.RESOURCE_TYPE)) {
            try {
                return (AdapterType) new GenericXliffTranslationCloudConfigImpl(resource);
            } catch (TranslationException te) {
                log.error(te.getMessage(), te);
                return null;
            }
        }
        log.debug("Unable to adapt to resource of type {}", type.getName());
        return null;

    }
}