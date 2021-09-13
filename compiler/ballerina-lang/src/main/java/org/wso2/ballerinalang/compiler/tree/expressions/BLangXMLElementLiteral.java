/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.Name;
import org.ballerinalang.model.symbols.Symbol;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.XMLAttributeNode;
import org.ballerinalang.model.tree.expressions.XMLElementLiteralNode;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 0.94
 */
public class BLangXMLElementLiteral extends BLangExpression implements XMLElementLiteralNode {

    public BLangExpression startTagName;
    public BLangExpression endTagName;
    public List<BLangXMLAttribute> attributes;
    public List<BLangExpression> children;
    public Map<Name, BXMLNSSymbol> namespacesInScope;
    public List<BLangXMLNS> inlineNamespaces;
    public BXMLNSSymbol defaultNsSymbol;
    public boolean isRoot;
    public Scope scope;
    public List<BLangExpression> modifiedChildren;
    
    public BLangXMLElementLiteral() {
        attributes = new ArrayList<BLangXMLAttribute>();
        children = new ArrayList<BLangExpression>();
        namespacesInScope = new LinkedHashMap<Name, BXMLNSSymbol>();
        inlineNamespaces = new ArrayList<BLangXMLNS>();
    }

    @Override
    public ExpressionNode getStartTagName() {
        return startTagName;
    }

    @Override
    public void setStartTagName(ExpressionNode startTagName) {
        this.startTagName = (BLangExpression) startTagName;
    }

    @Override
    public ExpressionNode getEndTagName() {
        return endTagName;
    }

    @Override
    public void setEndTagName(ExpressionNode endTagName) {
        this.endTagName = (BLangExpression) endTagName;
    }

    @Override
    public List<BLangXMLAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public void addAttribute(XMLAttributeNode attribute) {
        this.attributes.add((BLangXMLAttribute) attribute);
    }

    @Override
    public List<BLangExpression> getContent() {
        return children;
    }

    @Override
    public void addChild(ExpressionNode expr) {
        this.children.add((BLangExpression) expr);
    }

    @Override
    public Map<Name, ? extends Symbol> getNamespaces() {
        return namespacesInScope;
    }

    @Override
    public void addNamespace(Name prefix, Symbol namespaceUri) {
        this.namespacesInScope.put(prefix, (BXMLNSSymbol) namespaceUri);
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R accept(BLangNodeTransformer<T, R> transformer, T props) {
        return transformer.transform(this, props);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.XML_ELEMENT_LITERAL;
    }

    @Override
    public String toString() {
        return "BLangXMLElementLiteral: <" + startTagName + (endTagName == null ? "" : "> </" + endTagName + "> ")
                + attributes + (children == null ? "" : children);
    }
}
