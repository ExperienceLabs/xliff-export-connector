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

public interface GenericXliffTranslationCloudConfig {

    public static final String PROPERTY_TRANSLATION_XML_STORE_PATH = "xmlstorepath";
    public static final String PROPERTY_TRANSLATION_XML_LOAD_PATH = "xmlloadpath";
    public static final String XLIFF_DOCUMENT_VERSION = "xliffdocumentversion";

    public static final String RESOURCE_TYPE = "cq/translation/components/generic-xliff/xliff-cloudconfig";
    public static final String ROOT_PATH = "/etc/cloudservices/generic-xliff-translation";

    String getXMLStorePath();

    String getXMLLoadPath();
    
    String getXliffDocumentVersion();
}