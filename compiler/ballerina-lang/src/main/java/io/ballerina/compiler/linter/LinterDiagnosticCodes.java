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
package io.ballerina.compiler.linter;

import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.Arrays;
import java.util.List;

import static io.ballerina.compiler.linter.LinterConstants.DESCRIPTION;
import static io.ballerina.compiler.linter.LinterConstants.NODE;

/**
 * This class contains a list of diagnostic codes for Linter.
 *
 * @since 2.0.0
 */
public enum LinterDiagnosticCodes implements DiagnosticCode {

    /* Internal */

    /* Hints */
    ADDITIONAL_PARENTHESES("BLH0100", "additional.parentheses", DiagnosticSeverity.HINT, List.of(DESCRIPTION, NODE)),

    /* Warnings */;


    private final String diagnosticId;
    private final String messageKey;
    private final DiagnosticSeverity severity;
    private final String[] detailKeys;

    LinterDiagnosticCodes(String diagnosticId, String messageKey, DiagnosticSeverity severity) {

        this.diagnosticId = diagnosticId;
        this.messageKey = messageKey;
        this.severity = severity;
        this.detailKeys = new String[0];
    }

    LinterDiagnosticCodes(String diagnosticId, String messageKey, DiagnosticSeverity severity,
                          List<String> detailKeys) {

        this.diagnosticId = diagnosticId;
        this.messageKey = messageKey;
        this.severity = severity;
        this.detailKeys = detailKeys.toArray(new String[0]);
    }

    @Override
    public DiagnosticSeverity severity() {
        return severity;
    }

    @Override
    public String diagnosticId() {
        return diagnosticId;
    }

    @Override
    public String messageKey() {
        return messageKey;
    }

    public String[] detailKeys() {
        return Arrays.copyOf(detailKeys, detailKeys.length);
    }

    public boolean equals(DiagnosticCode code) {
        return this.messageKey.equals(code.messageKey());
    }
}
