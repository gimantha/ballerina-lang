/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagnosticBasedCodeActionProvider;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.TextEdit;

import java.util.Collections;
import java.util.List;

/**
 * Code action to add readonly to the type.
 *
 * @since 2201.10.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddReadonlyCodeAction implements DiagnosticBasedCodeActionProvider {

    private static final String NAME = "Add readonly to the type";
    private static final String DIAGNOSTIC_CODE = "BCE3959";

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return DIAGNOSTIC_CODE.equals(diagnostic.diagnosticInfo().code())
                && CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        try {
            NonTerminalNode node = positionDetails.matchedNode();
            SemanticModel semanticModel = context.currentSemanticModel().orElseThrow();
            TypeSymbol typeSymbol = semanticModel.typeOf(node).orElseThrow();
            Location location = typeSymbol.getLocation().orElseThrow();
            List<TextEdit> textEdits = CodeActionUtil.getReadonlyTextEdits(location.lineRange(),
                    typeSymbol.typeKind() == TypeDescKind.UNION,
                    node.kind() == SyntaxKind.INDEXED_EXPRESSION && typeSymbol.typeKind() != TypeDescKind.ARRAY);
            return Collections.singletonList(CodeActionUtil.createCodeAction(
                    String.format(CommandConstants.ADD_READONLY, typeSymbol.signature()),
                    textEdits,
                    context.fileUri(),
                    CodeActionKind.QuickFix));
        } catch (RuntimeException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
