/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.compiler.linter.impl.codeactions;

import io.ballerina.compiler.linter.LinterDiagnosticCodes;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.plugins.codeaction.CodeAction;
import io.ballerina.projects.plugins.codeaction.CodeActionArgument;
import io.ballerina.projects.plugins.codeaction.CodeActionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionInfo;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocumentChange;
import io.ballerina.tools.text.TextEdit;
import io.ballerina.tools.text.TextRange;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @since 2.0.0
 */
public class RemoveParenthesesCodeAction implements CodeAction {

    public static final String NODE = "node";

    @Override
    public List<String> supportedDiagnosticCodes() {
        return List.of(LinterDiagnosticCodes.ADDITIONAL_PARENTHESES.diagnosticId());
    }

    @Override
    public Optional<CodeActionInfo> codeActionInfo(CodeActionContext context) {

        if (context.diagnostic().properties().isEmpty()) {
            return Optional.empty();
        }

        Node node = ((DiagnosticProperty<Node>) context.diagnostic().properties().get(1)).value();
        if (node == null) {
            return Optional.empty();
        }

        List<CodeActionArgument> args = List.of(CodeActionArgument.from(NODE, node.lineRange()));
        return Optional.of(CodeActionInfo.from("Remove Additional Parentheses", args));
    }

    @Override
    public List<DocumentEdit> execute(CodeActionExecutionContext context) {
        LineRange lineRange = context.arguments().get(0).valueAs(LineRange.class);

        SyntaxTree syntaxTree = context.currentDocument().syntaxTree();
        TextDocument textDocument = syntaxTree.textDocument();
        int start = textDocument.textPositionFrom(lineRange.startLine());
        int endOffset = textDocument.textPositionFrom(lineRange.endLine());

        final NonTerminalNode node = ((ModulePartNode) syntaxTree.rootNode()).findNode(
                TextRange.from(start, endOffset - start), true);
        Node internNode;
        if (node.kind() == SyntaxKind.BRACED_EXPRESSION || node.kind() == SyntaxKind.BRACED_ACTION) {
            final BracedExpressionNode bracedExpressionNode = (BracedExpressionNode) node;
            internNode = bracedExpressionNode.expression();
        } else {
            return Collections.emptyList();
        }
        TextEdit edit = TextEdit.from(TextRange.from(start, endOffset - start), internNode.toSourceCode());
        TextDocumentChange textDocumentChange = TextDocumentChange.from(List.of(edit).toArray(new TextEdit[0]));
        TextDocument modified = syntaxTree.textDocument().apply(textDocumentChange);
        DocumentEdit documentEdit = new DocumentEdit(context.fileUri(), SyntaxTree.from(modified));
        return List.of(documentEdit);
    }

    @Override
    public String name() {
        return "remove_parentheses";
    }
}
