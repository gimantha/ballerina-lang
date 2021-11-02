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
package io.ballerina.compiler.linter.impl;

import io.ballerina.compiler.linter.impl.codeactions.RemoveParenthesesCodeAction;
import io.ballerina.compiler.linter.impl.rules.ParenthesesUsageRule;
import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;
import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;
import io.ballerina.projects.plugins.codeaction.CodeAction;
import org.ballerinalang.compiler.BLangCompilerException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * A built-in compiler plugin, that implements Language Linter rules.
 *
 * @since 2.0.0
 */
public class CompilerLinter extends CompilerPlugin {

    private static final List<Class<? extends LinterRule>> RULES = new ArrayList<>();
    private static final List<Class<? extends CodeAction>> CODE_ACTIONS = new ArrayList<>();

    static {
        RULES.add(ParenthesesUsageRule.class);

        CODE_ACTIONS.add(RemoveParenthesesCodeAction.class);
    }

    @Override
    public void init(CompilerPluginContext pluginContext) {

        pluginContext.addCodeAnalyzer(new LinterCodeAnalyzer());
        registerCodeActions(pluginContext);
    }

    private void registerCodeActions(CompilerPluginContext pluginContext) {
        for (Class<? extends CodeAction> codeActionClass : CODE_ACTIONS) {
            try {
                final CodeAction codeAction = codeActionClass.getDeclaredConstructor().newInstance();
                pluginContext.addCodeAction(codeAction);
            } catch (NoSuchMethodException | InstantiationException |
                    IllegalAccessException | InvocationTargetException e) {
                throw new BLangCompilerException("Failed to load linter code action :"
                        + codeActionClass.getCanonicalName(), e);
            }
        }
    }

    /**
     * The place where linter rule registers.
     *
     * @since 2.0.0
     */
    static class LinterCodeAnalyzer extends CodeAnalyzer {

        @Override
        public void init(CodeAnalysisContext analysisContext) {
            registerLinterRules(analysisContext);
        }

        private void registerLinterRules(CodeAnalysisContext ctx) {

            for (Class<? extends LinterRule> ruleClass : RULES) {
                try {
                    final LinterRule linterRule = ruleClass.getDeclaredConstructor().newInstance();
                    ctx.addSyntaxNodeAnalysisTask(linterRule, linterRule.getSyntaxKinds());
                } catch (NoSuchMethodException | InstantiationException |
                        IllegalAccessException | InvocationTargetException e) {
                    throw new BLangCompilerException("Failed to load linter plugin :" + ruleClass.getCanonicalName(), e);
                }
            }
        }
    }
}
