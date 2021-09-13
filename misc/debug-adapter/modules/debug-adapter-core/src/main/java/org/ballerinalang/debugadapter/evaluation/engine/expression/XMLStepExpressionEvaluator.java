/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.debugadapter.evaluation.engine.expression;

import com.sun.jdi.Value;
import io.ballerina.compiler.syntax.tree.XMLStepExpressionNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;
import org.ballerinalang.debugadapter.variable.BVariableType;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_DEBUGGER_RUNTIME_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_XML_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.GET_XML_STEP_RESULT_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_STRING_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;

/**
 * XML step expression evaluator implementation.
 *
 * @since 2.0.0
 */
public class XMLStepExpressionEvaluator extends Evaluator {

    private final XMLStepExpressionNode syntaxNode;
    private final Evaluator subExprEvaluator;

    public XMLStepExpressionEvaluator(EvaluationContext context, XMLStepExpressionNode stepExpressionNode,
                                      Evaluator subExprEvaluator) {
        super(context);
        this.syntaxNode = stepExpressionNode;
        this.subExprEvaluator = subExprEvaluator;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            // An xml step expression provides access to the children or descendants of an element, similar to a
            // location path in XPath. The static type of the sub expression must be a subtype of xml.
            BExpressionValue subExprResult = subExprEvaluator.evaluate();
            if (subExprResult.getType() != BVariableType.XML) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                        "step expressions are not supported on type '" + subExprResult.getType().getString() + "'"));
            }

            List<String> argTypeNames = new ArrayList<>();
            argTypeNames.add(B_XML_CLASS);
            argTypeNames.add(JAVA_STRING_CLASS);
            RuntimeStaticMethod getStepResultMethod = getRuntimeMethod(context, B_DEBUGGER_RUNTIME_CLASS,
                    GET_XML_STEP_RESULT_METHOD, argTypeNames);

            List<Value> argValues = new ArrayList<>();
            argValues.add(subExprResult.getJdiValue());
            argValues.add(EvaluationUtils.getAsJString(context, syntaxNode.xmlStepStart().toSourceCode().trim()));
            getStepResultMethod.setArgValues(argValues);
            return new BExpressionValue(context, getStepResultMethod.invokeSafely());
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }
}
