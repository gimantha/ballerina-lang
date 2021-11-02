/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.compiler.linter.impl;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.diagnostics.Location;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnostic;
import org.wso2.ballerinalang.compiler.diagnostic.properties.BCollectionProperty;
import org.wso2.ballerinalang.compiler.diagnostic.properties.BNumericProperty;
import org.wso2.ballerinalang.compiler.diagnostic.properties.BStringProperty;
import org.wso2.ballerinalang.compiler.diagnostic.properties.BSymbolicProperty;
import org.wso2.ballerinalang.compiler.diagnostic.properties.NonCatProperty;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Helper class to create Linter diagnostics
 *
 * @since 2.0.0
 */
public final class LinterDiagnosticHelper {

    public static final String DOT = ".";
    public static final String MESSAGE = "message";
    public static final String DESCRIPTION = "description";
    private static final String LINTER_PROPERTIES = "compiler-linter";
    private static final ResourceBundle MESSAGES = ResourceBundle.getBundle(LINTER_PROPERTIES, Locale.getDefault());

    private LinterDiagnosticHelper() {
    }

    public static String getLinterProperty(DiagnosticCode code, String property, Object... args) {

        String prefix = code.severity().toString().toLowerCase(Locale.ROOT);
        String msgKey = MESSAGES.getString(prefix + DOT + code.messageKey() + DOT + property);
        return MessageFormat.format(msgKey, args);
    }

    public static Diagnostic createDiagnostic(DiagnosticCode code, Location location, String msg) {

        return createDiagnostic(code, location, msg, Collections.emptyList());
    }

    public static Diagnostic createDiagnostic(DiagnosticCode code, Location location, String msg, String description) {

        return createDiagnostic(code, location, msg, description, Collections.emptyList());
    }

    public static Diagnostic createDiagnostic(DiagnosticCode code, Location location, String msg, String description,
                                              List<Object> properties) {

        List<Object> newProperties = new ArrayList<>();
        newProperties.add(description);
        newProperties.addAll(properties);
        return createDiagnostic(code, location, msg, newProperties);
    }

    public static Diagnostic createDiagnostic(DiagnosticCode code, Location location, String msg,
                                              List<Object> properties) {

        DiagnosticInfo diagInfo = new DiagnosticInfo(code.diagnosticId(), code.messageKey(), code.severity());
        List<DiagnosticProperty<?>> argList = convertDiagArgsToProps(properties);
     //   if (argList.size() != code.detailKeys().length) {
    //        throw new IllegalArgumentException("Diagnostic code " + code.messageKey() + " has " +
    //                code.detailKeys().length + " properties, But found " + argList.size());
    //    }
        return new LinterDiagnostic(location, msg, diagInfo, code, argList);
    }

    private static List<DiagnosticProperty<?>> convertDiagArgsToProps(Collection<?> args) {

        List<DiagnosticProperty<?>> diagArgs = new ArrayList<>();
        for (Object arg : args) {
            DiagnosticProperty<?> dArg;
            if (arg instanceof Symbol) {
                dArg = new BSymbolicProperty((Symbol) arg);
            } else if (arg instanceof String) {
                dArg = new BStringProperty((String) arg);
            } else if (arg instanceof Number) {
                dArg = new BNumericProperty((Number) arg);
            } else if (arg instanceof Collection) {
                Collection<DiagnosticProperty<?>> diagProperties
                        = convertDiagArgsToProps((Collection<?>) arg);
                dArg = new BCollectionProperty(diagProperties);
            } else {
                dArg = new NonCatProperty(arg);
            }
            diagArgs.add(dArg);
        }
        return diagArgs;
    }

    /**
     * Wrapper for Compiler Diagnostics.
     *
     * @since 2.0.0
     */
    static class LinterDiagnostic extends BLangDiagnostic {

        public LinterDiagnostic(Location location, String msg, DiagnosticInfo diagnosticInfo,
                                DiagnosticCode diagnosticCode, List<DiagnosticProperty<?>> properties) {
            super(location, msg, diagnosticInfo, diagnosticCode, properties);
        }
    }

}
