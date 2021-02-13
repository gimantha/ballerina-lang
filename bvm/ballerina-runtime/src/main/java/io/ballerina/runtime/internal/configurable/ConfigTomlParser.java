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

package io.ballerina.runtime.internal.configurable;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.configurable.exceptions.TomlException;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BTableType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.ListInitialValueEntry;
import io.ballerina.runtime.internal.values.TableValueImpl;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlArrayValueNode;
import io.ballerina.toml.semantic.ast.TomlBasicValueNode;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlNode;
import io.ballerina.toml.semantic.ast.TomlTableArrayNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.CONFIGURATION_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.DEFAULT_MODULE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.FIELD_TYPE_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_ADDITIONAL_FIELD_IN_RECORD;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_BYTE_RANGE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_MODULE_STRUCTURE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_TOML_FILE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_TOML_STRUCTURE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_TOML_STRUCTURE_TABLE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_VARIABLE_TYPE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.REQUIRED_FIELD_NOT_PROVIDED;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.SUBMODULE_DELIMITER;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.TABLE_KEY_NOT_PROVIDED;
import static io.ballerina.runtime.internal.util.RuntimeUtils.isByteLiteral;

/**
 * Toml parser for configurable implementation.
 *
 * @since 2.0.0
 */
public class ConfigTomlParser {

    private ConfigTomlParser() {
    }

    private static TomlTableNode getConfigurationData(Path configFilePath) {
        if (!Files.exists(configFilePath)) {
            return null;
        }
        ConfigToml configToml = new ConfigToml(configFilePath);
        return configToml.tomlAstNode();
    }

    public static void populateConfigMap(Path filePath, Map<Module, VariableKey[]> configurationData) {
        if (configurationData.isEmpty()) {
            return;
        }
        TomlTableNode tomlNode = getConfigurationData(filePath);
        if (tomlNode == null || tomlNode.entries().isEmpty()) {
            //No values provided at toml file
            return;
        }
        for (Map.Entry<Module, VariableKey[]> moduleEntry : configurationData.entrySet()) {
            TomlTableNode moduleNode = retrieveModuleNode(tomlNode, moduleEntry.getKey());
            if (moduleNode == null) {
                //Module could contain optional configurable variable
                continue;
            }
            for (VariableKey key : moduleEntry.getValue()) {
                if (!moduleNode.entries().containsKey(key.variable)) {
                    //It is an optional configurable variable
                    continue;
                }
                Object value = validateNodeAndExtractValue(key, moduleNode.entries());
                ConfigurableMap.put(key, value);
            }
        }
    }

    private static TomlTableNode retrieveModuleNode(TomlTableNode tomlNode, Module module) {
        String orgName = module.getOrg();
        String moduleName = module.getName();
        if (tomlNode.entries().containsKey(orgName)) {
            TomlNode orgNode = tomlNode.entries().get(orgName);
            checkModuleStructure(orgNode, orgName + "." + moduleName);
            tomlNode = (TomlTableNode) orgNode;
        }
        return moduleName.equals(DEFAULT_MODULE) ? tomlNode : extractModuleNode(tomlNode, moduleName);
    }

    private static Object validateNodeAndExtractValue(VariableKey key, Map<String, TopLevelNode> valueMap) {
        String variableName = key.variable;
        Type type = key.type;
        Object value;
        TomlNode tomlValue =  valueMap.get(variableName);
        variableName = key.module.getName() + ":" + variableName;
        switch (type.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.BYTE_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.STRING_TAG:
                value = retrievePrimitiveValue(tomlValue , variableName, type);
                break;
            case TypeTags.INTERSECTION_TAG:
                value = retrieveComplexValue((BIntersectionType) type, tomlValue, variableName);
                break;
            default:
                throw new TomlException(String.format(CONFIGURATION_NOT_SUPPORTED, type.toString(), variableName));
        }
        return value;
    }

    private static Object retrieveComplexValue(BIntersectionType type, TomlNode tomlValue, String variableName) {
        Type effectiveType = type.getEffectiveType();
        Object value;
        switch (effectiveType.getTag()) {
            case TypeTags.ARRAY_TAG:
                value = retrieveArrayValues(tomlValue, variableName, (ArrayType) effectiveType);
                break;
            case TypeTags.RECORD_TYPE_TAG:
                checkTypeAndThrowError(tomlValue.kind(), variableName, effectiveType);
                value = retrieveRecordValues((TomlTableNode) tomlValue, variableName, type);
                break;
            case TypeTags.TABLE_TAG:
                checkTypeAndThrowError(tomlValue.kind(), variableName, effectiveType);
                value = retrieveTableValues((TomlTableArrayNode) tomlValue, variableName, (TableType) effectiveType);
                break;
            default:
                throw new TomlException(
                        String.format(CONFIGURATION_NOT_SUPPORTED, effectiveType.toString(), variableName));
        }
        return value;
    }

    private static Object retrievePrimitiveValue(TomlNode tomlValue, String variableName, Type type) {
        checkTomlValueStructure(tomlValue, TomlType.KEY_VALUE, variableName);
        tomlValue = ((TomlKeyValueNode) tomlValue).value();
        checkTypeAndThrowError(tomlValue.kind(), variableName, type);
        return getBalValue(variableName, type.getTag(), ((TomlBasicValueNode<?>) tomlValue).getValue());
    }

    private static Object retrieveArrayValues(TomlNode tomlValue, String variableName,
                                              ArrayType effectiveType) {
        checkTomlValueStructure(tomlValue, TomlType.KEY_VALUE, variableName);
        tomlValue = ((TomlKeyValueNode) tomlValue).value();
        checkTypeAndThrowError(tomlValue.kind(), variableName, effectiveType);
        Type elementType = effectiveType.getElementType();
        List<TomlValueNode> arrayList = ((TomlArrayValueNode) tomlValue).elements();
        if (!isPrimitiveType(elementType.getTag())) {
            //Remove after supporting all arrays
            throw new TomlException(String.format(CONFIGURATION_NOT_SUPPORTED, effectiveType.toString(), variableName));
        }
        return new ArrayValueImpl(effectiveType, arrayList.size(), createArray(variableName, arrayList, elementType));
    }

    private static void checkTomlValueStructure(TomlNode tomlValue, TomlType expectedType, String variableName) {
        if (tomlValue.kind() != expectedType) {
            throw new TomlException(String.format(INVALID_TOML_FILE + INVALID_TOML_STRUCTURE, variableName,
                    expectedType, tomlValue.kind()));
        }
    }

    private static ListInitialValueEntry.ExpressionEntry[] createArray(String variableName,
                                                                       List<TomlValueNode> arrayList,
                                                                       Type elementType) {
        int arraySize = arrayList.size();
        ListInitialValueEntry.ExpressionEntry[] arrayEntries =
                new ListInitialValueEntry.ExpressionEntry[arraySize];
        for (int i = 0; i < arraySize; i++) {
            String elementName = variableName + "[" + i + "]";
            TomlNode tomlNode = arrayList.get(i);
            checkTomlValueStructure(tomlNode, getEffectiveTomlType(elementType, elementName), elementName);
            TomlBasicValueNode<?> tomlBasicValueNode = (TomlBasicValueNode<?>) arrayList.get(i);
            Object value = tomlBasicValueNode.getValue();
            arrayEntries[i] =
                    new ListInitialValueEntry.ExpressionEntry(getBalValue(variableName, elementType.getTag(), value));
        }
        return arrayEntries;
    }

    private static boolean isPrimitiveType(int typeTag) {
        return ((typeTag == TypeTags.INT_TAG) || (typeTag == TypeTags.FLOAT_TAG) || (typeTag == TypeTags.STRING_TAG) ||
                (typeTag == TypeTags.BOOLEAN_TAG) || (typeTag == TypeTags.DECIMAL_TAG) ||
                (typeTag == TypeTags.BYTE_TAG));
    }

    private static Object retrieveRecordValues(TomlTableNode tomlValue, String variableName,
                                               BIntersectionType intersectionType) {
        RecordType effectiveType = (RecordType) intersectionType.getConstituentTypes().get(0);
        Map<String, Object> initialValueEntries = new HashMap<>();
        for (Map.Entry<String, TopLevelNode> tomlField : tomlValue.entries().entrySet()) {
            if (tomlField.getValue().kind() == TomlType.TABLE) {
                //Modify this check when we support complex record fields
                throw new TomlException(String.format(INVALID_TOML_FILE + INVALID_TOML_STRUCTURE_TABLE, variableName,
                        variableName + "." + tomlField.getKey()));
            }
            String fieldName = tomlField.getKey();
            Field field = effectiveType.getFields().get(fieldName);
            if (field == null) {
                throw new TomlException(String.format(INVALID_ADDITIONAL_FIELD_IN_RECORD, fieldName, variableName,
                        effectiveType.toString()));
            }
            Type fieldType = field.getFieldType();
            if (!isSupportedType(fieldType)) {
                throw new TomlException(
                        String.format(FIELD_TYPE_NOT_SUPPORTED, fieldType.toString(), variableName, effectiveType));
            }
            TomlNode value =  tomlField.getValue();
            Object objectValue;
            switch (fieldType.getTag()) {
                case TypeTags.ARRAY_TAG:
                    objectValue = retrieveArrayValues(value, variableName + "." + fieldName, (ArrayType) fieldType);
                    break;
                case TypeTags.INTERSECTION_TAG:
                    ArrayType arrayType = (ArrayType) ((IntersectionType) fieldType).getEffectiveType();
                    objectValue = retrieveArrayValues(value, variableName + "." + fieldName, arrayType);
                    break;
                default:
                    objectValue = retrievePrimitiveValue(value, variableName + "." + fieldName, fieldType);
            }
            initialValueEntries.put(fieldName, objectValue);
        }
        validateRequiredField(initialValueEntries, effectiveType, variableName);
        return ValueCreator
                .createReadonlyRecordValue(effectiveType.getPackage(), effectiveType.getName(), initialValueEntries);
    }

    private static void validateRequiredField(Map<String, Object> initialValueEntries, RecordType recordType,
                                              String variableName) {
        for (Map.Entry<String, Field> field : recordType.getFields().entrySet()) {
            String fieldName = field.getKey();
            if (SymbolFlags.isFlagOn(field.getValue().getFlags(), SymbolFlags.REQUIRED) &&
                    initialValueEntries.get(fieldName) == null) {
                throw new TomlException(
                        String.format(REQUIRED_FIELD_NOT_PROVIDED, fieldName, recordType.toString(), variableName));
            }
        }
    }

    private static boolean isSupportedType(Type type) {
        //Remove this check when we support all field types
        int typeTag = type.getTag();
        if (typeTag == TypeTags.INTERSECTION_TAG) {
            Type effectiveType = ((IntersectionType) type).getEffectiveType();
            if (effectiveType.getTag() != TypeTags.ARRAY_TAG) {
                return false;
            }
            typeTag = ((ArrayType) ((IntersectionType) type).getEffectiveType()).getElementType().getTag();
        } else if (typeTag == TypeTags.ARRAY_TAG) {
            typeTag = ((ArrayType) type).getElementType().getTag();
        }
        return isPrimitiveType(typeTag);
    }

    private static Object retrieveTableValues(TomlTableArrayNode tomlValue, String variableName,
                                              TableType tableType) {
        List<TomlTableNode> tableNodeList = tomlValue.children();
        Type constraintType = tableType.getConstrainedType();
        int tableSize = tableNodeList.size();
        ListInitialValueEntry.ExpressionEntry[] tableEntries = new ListInitialValueEntry.ExpressionEntry[tableSize];
        String[] keys = tableType.getFieldNames();
        for (int i = 0; i < tableSize; i++) {
            if (keys != null) {
                validateKeyField(tableNodeList.get(i), keys, tableType, variableName);
            }
            Object value = retrieveRecordValues(tableNodeList.get(i), variableName, (BIntersectionType) constraintType);
            tableEntries[i] = new ListInitialValueEntry.ExpressionEntry(value);
        }
        ArrayValue tableData =
                new ArrayValueImpl(TypeCreator.createArrayType(constraintType), tableSize, tableEntries);
        ArrayValue keyNames = keys == null ? (ArrayValue) ValueCreator.createArrayValue(new BString[]{}) :
                (ArrayValue) StringUtils.fromStringArray(keys);
        return new TableValueImpl<>((BTableType) tableType, tableData, keyNames);
    }

    private static void validateKeyField(TomlTableNode recordTable, String[] fieldNames, Type tableType,
                                         String variableName) {
        for (String key : fieldNames) {
            if (recordTable.entries().get(key) == null) {
                throw new TomlException(
                        String.format(TABLE_KEY_NOT_PROVIDED, key, tableType.toString(), variableName));
            }
        }
    }

    private static Object getBalValue(String variableName, int typeTag, Object tomlValue) {
        if (typeTag == TypeTags.BYTE_TAG) {
            int value = ((Long) tomlValue).intValue();
            if (!isByteLiteral(value)) {
                throw new TomlException(String.format(INVALID_BYTE_RANGE, variableName, value));
            }
            return value;
        }
        if (typeTag == TypeTags.DECIMAL_TAG) {
            return ValueCreator.createDecimalValue(BigDecimal.valueOf((Double) tomlValue));
        }
        if (typeTag == TypeTags.STRING_TAG) {
            String stringVal = (String) tomlValue;
            ConfigSecurityUtils.handleEncryptedValues(variableName, stringVal);
            return StringUtils.fromString(stringVal);
        }
        return tomlValue;
    }

    private static void checkTypeAndThrowError(TomlType actualType, String variableName, Type expectedType) {
        TomlType expectedTomlType = getEffectiveTomlType(expectedType, variableName);
        if (actualType == expectedTomlType) {
            return;
        }
        throw new TomlException(INVALID_TOML_FILE + String.format(INVALID_VARIABLE_TYPE, variableName,
                expectedType.toString(), expectedTomlType.name(), actualType.name()));
    }

    private static TomlType getEffectiveTomlType(Type expectedType, String variableName) {
        TomlType tomlType;
        switch (expectedType.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.BYTE_TAG:
                tomlType = TomlType.INTEGER;
                break;
            case TypeTags.BOOLEAN_TAG:
                tomlType = TomlType.BOOLEAN;
                break;
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
                tomlType = TomlType.DOUBLE;
                break;
            case TypeTags.STRING_TAG:
                tomlType = TomlType.STRING;
                break;
            case TypeTags.ARRAY_TAG:
                tomlType = TomlType.ARRAY;
                break;
            case TypeTags.RECORD_TYPE_TAG:
                tomlType = TomlType.TABLE;
                break;
            case TypeTags.TABLE_TAG:
                tomlType = TomlType.TABLE_ARRAY;
                break;
            default:
                throw new TomlException(String.format(CONFIGURATION_NOT_SUPPORTED, expectedType.toString(),
                        variableName));
        }
        return tomlType;
    }

    private static TomlTableNode extractModuleNode(TomlTableNode orgNode, String moduleName) {
        if (orgNode == null) {
            return orgNode;
        }
        TomlTableNode moduleNode = orgNode;
        TomlNode module;
        int subModuleIndex = moduleName.indexOf(SUBMODULE_DELIMITER);
        if (subModuleIndex == -1) {
            module = orgNode.entries().get(moduleName);
            checkModuleStructure(module, moduleName);
            moduleNode = (TomlTableNode) module;
        } else if (subModuleIndex != moduleName.length()) {
            String parent = moduleName.substring(0, subModuleIndex);
            String submodule = moduleName.substring(subModuleIndex + 1);
            module = moduleNode.entries().get(parent);
            checkModuleStructure(module, moduleName);
            moduleNode = extractModuleNode((TomlTableNode) module, submodule);
        }
        return moduleNode;
    }

    private static void checkModuleStructure(TomlNode moduleNode, String moduleName) {
        if (moduleNode != null && moduleNode.kind() != TomlType.TABLE) {
            throw new TomlException(
                    String.format(INVALID_TOML_FILE + INVALID_MODULE_STRUCTURE, moduleName, moduleName));
        }
    }

}
