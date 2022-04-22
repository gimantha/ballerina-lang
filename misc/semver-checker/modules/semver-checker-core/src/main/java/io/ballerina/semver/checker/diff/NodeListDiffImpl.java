/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semver.checker.diff;

import io.ballerina.compiler.syntax.tree.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Base implementation of changes in Ballerina syntax tree node lists.
 *
 * @param <T> node type
 * @since 2201.2.0
 */
public class NodeListDiffImpl<T extends Node> implements NodeListDiff<List<T>> {

    protected final List<T> newNodes;
    protected final List<T> oldNodes;
    protected DiffType diffType;
    protected CompatibilityLevel compatibilityLevel;
    protected final List<NodeDiffImpl<? extends Node>> childDiffs;
    private String message;

    public NodeListDiffImpl(List<T> newNodes, List<T> oldNodes) {
        this(newNodes, oldNodes, DiffType.UNKNOWN, CompatibilityLevel.UNKNOWN);
    }

    public NodeListDiffImpl(List<T> newNodes, List<T> oldNodes, DiffType diffType,
                            CompatibilityLevel compatibilityLevel) {
        this.newNodes = newNodes;
        this.oldNodes = oldNodes;
        this.diffType = diffType;
        this.compatibilityLevel = compatibilityLevel;
        this.childDiffs = new ArrayList<>();
        this.message = null;
    }

    @Override
    public List<T> getNewNodes() {
        return newNodes;
    }

    @Override
    public List<T> getOldNodes() {
        return oldNodes;
    }

    @Override
    public DiffType getType() {
        return diffType;
    }

    @Override
    public void setType(DiffType diffType) {
        this.diffType = diffType;
    }

    @Override
    public CompatibilityLevel getCompatibilityLevel() {
        return compatibilityLevel;
    }

    @Override
    public void setCompatibilityLevel(CompatibilityLevel compatibilityLevel) {
        this.compatibilityLevel = compatibilityLevel;
    }

    @Override
    public void computeCompatibilityLevel() {
        if (compatibilityLevel == CompatibilityLevel.UNKNOWN) {
            compatibilityLevel = childDiffs.stream()
                    .map(NodeDiffImpl::getCompatibilityLevel)
                    .max(Comparator.comparingInt(CompatibilityLevel::getRank))
                    .orElse(CompatibilityLevel.UNKNOWN);
        }
    }

    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<NodeDiffImpl<? extends Node>> getChildDiffs() {
        return Collections.unmodifiableList(childDiffs);
    }

    public void addChildDiff(NodeDiffImpl<Node> childDiff) {
        this.childDiffs.add(childDiff);
    }

    public void addChildDiffs(List<NodeDiffImpl<Node>> childDiffs) {
        this.childDiffs.addAll(childDiffs);
    }

    /**
     * Derives the parent {@link CompatibilityLevel} of a given source (syntax node) based on the compatibility
     * information of its child elements.
     *
     * @param childCompatibilities compatibility information of the child elements
     * @return parent {@link CompatibilityLevel}
     */
    protected static CompatibilityLevel getUnifiedCompatibility(CompatibilityLevel... childCompatibilities) {
        return Arrays.stream(childCompatibilities)
                .max(Comparator.comparingInt(CompatibilityLevel::getRank))
                .orElse(CompatibilityLevel.UNKNOWN);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (childDiffs == null || childDiffs.isEmpty()) {
            sb.append("compatibility: ").append(compatibilityLevel.toString()).append(", ");
            if (message != null) {
                sb.append("description: ").append(message).append(System.lineSeparator());
                return sb.toString();
            }
        } else {
            childDiffs.forEach(diff -> sb.append(diff.toString()));
        }

        return sb.toString();
    }
}
