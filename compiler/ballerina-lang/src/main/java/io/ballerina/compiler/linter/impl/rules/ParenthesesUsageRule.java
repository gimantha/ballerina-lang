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
package io.ballerina.compiler.linter.impl.rules;

import io.ballerina.compiler.linter.LinterDiagnosticCodes;
import io.ballerina.compiler.linter.impl.LinterDiagnosticHelper;
import io.ballerina.compiler.linter.impl.LinterRule;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;

import java.util.List;

import static io.ballerina.compiler.linter.impl.LinterDiagnosticHelper.MESSAGE;

/**
 * Detect overuse of parentheses.
 *
 * @since 2.0.0
 */
public class ParenthesesUsageRule extends LinterRule {

    private static final String IF = "if";
    private static final String WHILE = "while";
    private static final List<SyntaxKind> SYNTAX_KINDS = List.of(SyntaxKind.BRACED_EXPRESSION, SyntaxKind.BRACED_ACTION);
    private static final LinterDiagnosticCodes CODE = LinterDiagnosticCodes.ADDITIONAL_PARENTHESES;
    private static final String DESCRIPTION_CONDITIONAL_STMT = "description.conditional.stmt";
    private static final String DESCRIPTION_NESTED = "description.nested";

    @Override
    public List<SyntaxKind> getSyntaxKinds() {
        return SYNTAX_KINDS;
    }

    @Override
    public void perform(SyntaxNodeAnalysisContext ctx) {

        validateBracedExpressionNode(ctx, (BracedExpressionNode) ctx.node());
    }

    private void validateBracedExpressionNode(SyntaxNodeAnalysisContext ctx, BracedExpressionNode node) {

        final SyntaxKind parentKind = node.parent().kind();
        if (SyntaxKind.IF_ELSE_STATEMENT == parentKind) {
            ctx.reportDiagnostic(LinterDiagnosticHelper.createDiagnostic(CODE, node.location(),
                    LinterDiagnosticHelper.getLinterProperty(CODE, MESSAGE),
                    LinterDiagnosticHelper.getLinterProperty(CODE, DESCRIPTION_CONDITIONAL_STMT, IF),
                    List.of(node)));
        } else if (SyntaxKind.WHILE_STATEMENT == parentKind) {
            ctx.reportDiagnostic(LinterDiagnosticHelper.createDiagnostic(CODE, node.location(),
                    LinterDiagnosticHelper.getLinterProperty(CODE, MESSAGE),
                    LinterDiagnosticHelper.getLinterProperty(CODE, DESCRIPTION_CONDITIONAL_STMT, WHILE),
                    List.of(node)));
        } else if (parentKind == SyntaxKind.FUNCTION_SIGNATURE) {
            FunctionSignatureNode parent = (FunctionSignatureNode) node.parent();
        } else if (parentKind == SyntaxKind.BRACED_EXPRESSION || parentKind == SyntaxKind.BRACED_ACTION) {

            // TODO : Avoid multiple hints, but reports duplicates.
            Node currentParent = node.parent();
            Node lastBracedNode = node.parent();
            while (currentParent.kind() == SyntaxKind.BRACED_EXPRESSION
                    || currentParent.kind() == SyntaxKind.BRACED_ACTION) {
                lastBracedNode = currentParent;
                currentParent = currentParent.parent();
            }
            ctx.reportDiagnostic(LinterDiagnosticHelper.createDiagnostic(CODE, lastBracedNode.location(),
                    LinterDiagnosticHelper.getLinterProperty(CODE, MESSAGE),
                    LinterDiagnosticHelper.getLinterProperty(CODE, DESCRIPTION_NESTED),
                    List.of(lastBracedNode)));
        }
    }
}
