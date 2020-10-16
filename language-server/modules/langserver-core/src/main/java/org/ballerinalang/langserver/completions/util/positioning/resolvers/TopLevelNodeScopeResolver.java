/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.util.positioning.resolvers;

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNode;

/**
 * Position resolver for the top level nodes.
 */
public class TopLevelNodeScopeResolver extends CursorPositionResolver {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCursorBeforeNode(BLangDiagnosticLocation nodePosition, TreeVisitor treeVisitor,
                                      LSContext completionContext, BLangNode node, BSymbol bSymbol) {
        Position cursorPos = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        int line = cursorPos.getLine();
        int col = cursorPos.getCharacter();
        BLangDiagnosticLocation zeroBasedPos = CommonUtil.toZeroBasedPosition(nodePosition);
        int nodeSLine = zeroBasedPos.getStartLine();
        int nodeSCol = zeroBasedPos.getStartColumn();

        if (line < nodeSLine || (line == nodeSLine && col <= nodeSCol)) {
            treeVisitor.forceTerminateVisitor();
            treeVisitor.setNextNode(bSymbol, node);
            return true;
        }

        return false;
    }
}
