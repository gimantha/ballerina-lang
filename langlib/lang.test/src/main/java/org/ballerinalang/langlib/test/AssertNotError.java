/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.test;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.internal.TypeChecker;

/**
 * Native implementation of assertNotError(anydata|error value).
 *
 * @since 1.3.0
 */
public final class AssertNotError {
    private AssertNotError() {
    }

    public static void assertNotError(Object value) {
        if (TypeUtils.getImpliedType(TypeChecker.getType(value)).getTag() == TypeTags.ERROR_TAG) {
            throw ErrorCreator.createError(StringUtils.fromString("{ballerina/lang.test}AssertionError"),
                                           StringUtils.fromString("expected a non-error type"));
        }
    }
}
