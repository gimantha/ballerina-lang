/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.runtime.api.values;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.values.XmlItem;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * {@code BXMLItem} represents an XML Item in Ballerina.
 *
 * @since 2.0.0
 */
public interface BXmlItem extends BXml {

    String XMLNS_NS_URI_PREFIX = "{" + XMLConstants.XMLNS_ATTRIBUTE_NS_URI + "}";

    String XMLNS = "xmlns";

    BString XMLNS_PREFIX = StringUtils.fromString(XmlItem.XMLNS_NS_URI_PREFIX + XMLNS);

    QName getQName();

    void setQName(QName newQName);

    BXmlSequence getChildrenSeq();
}
