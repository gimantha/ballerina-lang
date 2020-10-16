/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.command.testgen.template;

import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

/**
 * Contains placeholders for the templates.
 *
 * @since 0.985.0
 */
public enum PlaceHolder {

    DECLARATIONS("declarations") {
        @Override
        public BLangDiagnosticLocation getPosition(BLangPackage bLangPackage) {
            final BLangDiagnosticLocation[] pos = {new BLangDiagnosticLocation(null, 0, 0, 0, 0)};
            //after imports
            bLangPackage.getImports().forEach(imp -> pos[0] = getMaximumPosition(imp.getPosition(), pos[0]));
            bLangPackage.getGlobalVariables().forEach(var -> pos[0] = getMaximumPosition(var.getPosition(), pos[0]));
            return zeroColumnPosition(pos[0]);
        }
    },

    CONTENT("content") {
        @Override
        public BLangDiagnosticLocation getPosition(BLangPackage bLangPackage) {
            final BLangDiagnosticLocation[] pos = {new BLangDiagnosticLocation(null, 0, 0, 0, 0)};
            bLangPackage.topLevelNodes.forEach(topLevelNode -> {
                if (topLevelNode instanceof BLangNode) {
                    pos[0] = getMaximumPosition(((BLangNode) topLevelNode).getPosition(), pos[0]);
                }
            });
            pos[0] = new BLangDiagnosticLocation(null, pos[0].getEndLine(), pos[0].getEndLine() + 1, 0, 0);
            return zeroColumnPosition(pos[0]);
        }
    },

    IMPORTS("imports") {
        @Override
        public BLangDiagnosticLocation getPosition(BLangPackage bLangPackage) {
            final BLangDiagnosticLocation[] pos = {new BLangDiagnosticLocation(null, 0, 0, 0, 0)};
            bLangPackage.getImports().forEach(pkg -> pos[0] = getMaximumPosition(pkg.getPosition(), pos[0]));
            return zeroColumnPosition(pos[0]);
        }
    },

    OTHER("_other_") {
        @Override
        public BLangDiagnosticLocation getPosition(BLangPackage bLangPackage) {
            throw new UnsupportedOperationException("Not supported!");
        }

        @Override
        public PlaceHolder get(String name) {
            this.name = name;
            return this;
        }
    };

    protected String name;

    PlaceHolder(String name) {
        this.name = name;
    }

    private static BLangDiagnosticLocation zeroColumnPosition(BLangDiagnosticLocation pos) {
        return new BLangDiagnosticLocation(pos.lineRange().filePath(),
                                           pos.lineRange().startLine().line(),
                                           pos.lineRange().endLine().line(), 0, 0);
    }

    private static BLangDiagnosticLocation getMaximumPosition(BLangDiagnosticLocation location1,
                                                              BLangDiagnosticLocation location2) {
        // handle null
        if (location1 == null) {
            return location2;
        } else if (location2 == null) {
            return location1;
        }
        if (location1.getEndLine() > location2.getEndLine()) {
            // location1.Line > location2.Line
            return location1;
        } else if (location1.getEndLine() < location2.getEndLine()) {
            // location1.Line < location2.Line
            return location2;
        } else {
            // location1.Line == location2.Line
            if (location1.getEndColumn() > location2.getEndColumn()) {
                // location1.Col > location2.Col
                return location1;
            } else {
                // location1.Col < location2.Col
                return location2;
            }
        }
    }

    public abstract BLangDiagnosticLocation getPosition(BLangPackage bLangPackage);

    public String getName() {
        return this.name;
    }

    public PlaceHolder get(String name) {
        throw new UnsupportedOperationException("Not supported!");
    }
}
