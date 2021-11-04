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

package io.ballerina;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Qualifiable;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AnnotAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.AnnotationAttachPointNode;
import io.ballerina.compiler.syntax.tree.AnnotationDeclarationNode;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.AsyncSendActionNode;
import io.ballerina.compiler.syntax.tree.BallerinaNameReferenceNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.BreakStatementNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ByteArrayLiteralNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.CommitActionNode;
import io.ballerina.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ComputedNameFieldNode;
import io.ballerina.compiler.syntax.tree.ConditionalExpressionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.ContinueStatementNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.DistinctTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.DoStatementNode;
import io.ballerina.compiler.syntax.tree.DoubleGTTokenNode;
import io.ballerina.compiler.syntax.tree.ElseBlockNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.ErrorBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ErrorMatchPatternNode;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.ExternalFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FailStatementNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldBindingPatternFullNode;
import io.ballerina.compiler.syntax.tree.FieldBindingPatternVarnameNode;
import io.ballerina.compiler.syntax.tree.FieldMatchPatternNode;
import io.ballerina.compiler.syntax.tree.FlushActionNode;
import io.ballerina.compiler.syntax.tree.ForEachStatementNode;
import io.ballerina.compiler.syntax.tree.ForkStatementNode;
import io.ballerina.compiler.syntax.tree.FromClauseNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.FunctionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.ImplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitAnonymousFunctionParameters;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.IncludedRecordParameterNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.InferredTypedescDefaultNode;
import io.ballerina.compiler.syntax.tree.InlineCodeReferenceNode;
import io.ballerina.compiler.syntax.tree.InterpolationNode;
import io.ballerina.compiler.syntax.tree.IntersectionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.JoinClauseNode;
import io.ballerina.compiler.syntax.tree.KeySpecifierNode;
import io.ballerina.compiler.syntax.tree.KeyTypeConstraintNode;
import io.ballerina.compiler.syntax.tree.LetClauseNode;
import io.ballerina.compiler.syntax.tree.LetExpressionNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.LimitClauseNode;
import io.ballerina.compiler.syntax.tree.ListBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ListMatchPatternNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.LocalTypeDefinitionStatementNode;
import io.ballerina.compiler.syntax.tree.LockStatementNode;
import io.ballerina.compiler.syntax.tree.MapTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.MappingBindingPatternNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingMatchPatternNode;
import io.ballerina.compiler.syntax.tree.MarkdownCodeBlockNode;
import io.ballerina.compiler.syntax.tree.MarkdownCodeLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MatchClauseNode;
import io.ballerina.compiler.syntax.tree.MatchGuardNode;
import io.ballerina.compiler.syntax.tree.MatchStatementNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleXMLNamespaceDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedArgBindingPatternNode;
import io.ballerina.compiler.syntax.tree.NamedArgMatchPatternNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarator;
import io.ballerina.compiler.syntax.tree.NilLiteralNode;
import io.ballerina.compiler.syntax.tree.NilTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ObjectConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.OnClauseNode;
import io.ballerina.compiler.syntax.tree.OnConflictClauseNode;
import io.ballerina.compiler.syntax.tree.OnFailClauseNode;
import io.ballerina.compiler.syntax.tree.OptionalFieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.OrderByClauseNode;
import io.ballerina.compiler.syntax.tree.OrderKeyNode;
import io.ballerina.compiler.syntax.tree.PanicStatementNode;
import io.ballerina.compiler.syntax.tree.ParameterizedTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParenthesisedTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.QueryActionNode;
import io.ballerina.compiler.syntax.tree.QueryConstructTypeNode;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.QueryPipelineNode;
import io.ballerina.compiler.syntax.tree.ReceiveActionNode;
import io.ballerina.compiler.syntax.tree.ReceiveFieldsNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordRestDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.RequiredExpressionNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.RestArgumentNode;
import io.ballerina.compiler.syntax.tree.RestBindingPatternNode;
import io.ballerina.compiler.syntax.tree.RestDescriptorNode;
import io.ballerina.compiler.syntax.tree.RestMatchPatternNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.RetryStatementNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RollbackStatementNode;
import io.ballerina.compiler.syntax.tree.SelectClauseNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SingletonTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SpreadFieldNode;
import io.ballerina.compiler.syntax.tree.StartActionNode;
import io.ballerina.compiler.syntax.tree.StreamTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.StreamTypeParamsNode;
import io.ballerina.compiler.syntax.tree.SyncSendActionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.TableTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TransactionStatementNode;
import io.ballerina.compiler.syntax.tree.TransactionalExpressionNode;
import io.ballerina.compiler.syntax.tree.TrapExpressionNode;
import io.ballerina.compiler.syntax.tree.TrippleGTTokenNode;
import io.ballerina.compiler.syntax.tree.TupleTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeCastExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeCastParamNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeParameterNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceTypeDescNode;
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.TypeofExpressionNode;
import io.ballerina.compiler.syntax.tree.UnaryExpressionNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.WaitActionNode;
import io.ballerina.compiler.syntax.tree.WaitFieldNode;
import io.ballerina.compiler.syntax.tree.WaitFieldsListNode;
import io.ballerina.compiler.syntax.tree.WhereClauseNode;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;
import io.ballerina.compiler.syntax.tree.WildcardBindingPatternNode;
import io.ballerina.compiler.syntax.tree.XMLAtomicNamePatternNode;
import io.ballerina.compiler.syntax.tree.XMLAttributeNode;
import io.ballerina.compiler.syntax.tree.XMLAttributeValue;
import io.ballerina.compiler.syntax.tree.XMLComment;
import io.ballerina.compiler.syntax.tree.XMLElementNode;
import io.ballerina.compiler.syntax.tree.XMLEmptyElementNode;
import io.ballerina.compiler.syntax.tree.XMLEndTagNode;
import io.ballerina.compiler.syntax.tree.XMLFilterExpressionNode;
import io.ballerina.compiler.syntax.tree.XMLNamePatternChainingNode;
import io.ballerina.compiler.syntax.tree.XMLNamespaceDeclarationNode;
import io.ballerina.compiler.syntax.tree.XMLProcessingInstruction;
import io.ballerina.compiler.syntax.tree.XMLQualifiedNameNode;
import io.ballerina.compiler.syntax.tree.XMLSimpleNameNode;
import io.ballerina.compiler.syntax.tree.XMLStartTagNode;
import io.ballerina.compiler.syntax.tree.XMLStepExpressionNode;
import io.ballerina.compiler.syntax.tree.XMLTextNode;
import io.ballerina.component.ActionInvocationNode;
import io.ballerina.component.EndPointNode;
import io.ballerina.component.ForStatementNode;
import io.ballerina.component.IfStatementNode;
import io.ballerina.component.Node;
import io.ballerina.projects.Document;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.utils.ParserUtil;
import org.eclipse.lsp4j.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

/**
 * Visitor to discover the program structure.
 *
 * @since 2.0.0
 */
public class ProgramAnalyzerNodeVisitor extends NodeVisitor {

    public static final long DEFAULT_LOOP_SIZE = 2;
    public static final String ENDPOINTS_KEY = "endpoints";
    public static final String ACTION_INVOCATION_KEY = "actionInvocations";

    private static final Logger logger = LoggerFactory.getLogger(ProgramAnalyzerNodeVisitor.class);

    private final HashMap<Integer, Object> variableMap;
    private final HashMap<Integer, Integer> referenceMap;
    private final HashMap<Integer, EndPointNode> endPointDeclarationMap;

    private final Node startNode;
    private Node currentNode;
    private SemanticModel model;
    private Document document;
    private String file;
    private boolean withinRange = false;
    private Range range;

    public ProgramAnalyzerNodeVisitor() {

        this.variableMap = new HashMap<>();
        this.referenceMap = new HashMap<>();
        this.endPointDeclarationMap = new HashMap<>();
        this.startNode = new Node();
        this.currentNode = this.startNode;
    }

    public void setSemanticModel(SemanticModel model) {

        this.model = model;
    }

    public void setDocument(Document document) {

        this.document = document;
    }

    public void setFile(String file) {

        this.file = file;
    }

    public void setRange(Range range) {

        this.range = range;
    }

    @Override
    public void visit(ModulePartNode modulePartNode) {

        for (int i = 0; i < modulePartNode.members().size(); i++) {
            modulePartNode.members().get(i).accept(this);
        }
    }

    @Override
    public void visit(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {

        Optional<Symbol> symbol = model.symbol(moduleVariableDeclarationNode);
        if (symbol.isEmpty()) {
            return;
        }

        if (moduleVariableDeclarationNode.initializer().isEmpty()) {
            return;
        }

        if (isClientObject(symbol.get())) {
            resolveEndPoint(symbol.get(), moduleVariableDeclarationNode.initializer().get());
        }

        if (isClientObject(symbol.get()) || isRecordObject(symbol.get())) {
            Optional<ExpressionNode> expressionNode = moduleVariableDeclarationNode.initializer();

            if (expressionNode.isPresent() && expressionNode.get().kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
                this.registerVariableRef(getEndpointReference(symbol.get()), moduleVariableDeclarationNode);
            }
        } else if ((symbol.get().kind() == SymbolKind.VARIABLE) &&
                (moduleVariableDeclarationNode.initializer().get().kind() == SyntaxKind.REMOTE_METHOD_CALL_ACTION)) {
            this.registerVariableRef(moduleVariableDeclarationNode.typedBindingPattern().bindingPattern().
                            location().lineRange().hashCode(),
                    moduleVariableDeclarationNode);
        }

        if (moduleVariableDeclarationNode.typedBindingPattern().bindingPattern().kind() ==
                SyntaxKind.CAPTURE_BINDING_PATTERN) {

            Optional<Symbol> initSymbol = model.symbol(moduleVariableDeclarationNode.initializer().get());

            Integer key = moduleVariableDeclarationNode.typedBindingPattern().bindingPattern().
                    location().lineRange().hashCode();
            SyntaxKind initializerKind = moduleVariableDeclarationNode.initializer().get().kind();
            if (initSymbol.isPresent() && initializerKind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                putReference(key, getEndpointReference(initSymbol.get()));
            } else if (initializerKind == SyntaxKind.NUMERIC_LITERAL) {
                this.registerVariableRef(key, moduleVariableDeclarationNode);
            } else if (initializerKind == SyntaxKind.STRING_LITERAL) {
                this.registerVariableRef(key, moduleVariableDeclarationNode);
            }
        }
        visitSyntaxNode(moduleVariableDeclarationNode);
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {

        LineRange lineRange = functionDefinitionNode.lineRange();
        if (functionDefinitionNode.syntaxTree().filePath().equals(file)
                && range.getStart().getLine() == lineRange.startLine().line()
                && range.getStart().getCharacter() == lineRange.startLine().offset()
                && range.getEnd().getLine() == lineRange.endLine().line()
                && range.getEnd().getCharacter() == lineRange.endLine().offset()
        ) {
            withinRange = true;
        }

        this.visitSyntaxNode(functionDefinitionNode);
        withinRange = false;
    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {

        Optional<Symbol> symbol = model.symbol(variableDeclarationNode);
        if (symbol.isEmpty()) {
            return;
        }

        if (variableDeclarationNode.initializer().isEmpty()) {
            return;
        }

        if (isClientObject(symbol.get())) {
            resolveEndPoint(symbol.get(), variableDeclarationNode.initializer().get());
        }

        if (isClientObject(symbol.get()) || isRecordObject(symbol.get())) {
            Optional<ExpressionNode> expressionNode = variableDeclarationNode.initializer();

            if (expressionNode.isPresent() && expressionNode.get().kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
                this.registerVariableRef(getEndpointReference(symbol.get()), variableDeclarationNode);
            }
        } else if ((symbol.get().kind() == SymbolKind.VARIABLE) &&
                (variableDeclarationNode.initializer().get().kind() == SyntaxKind.REMOTE_METHOD_CALL_ACTION)) {
            this.registerVariableRef(variableDeclarationNode.typedBindingPattern().bindingPattern().
                            location().lineRange().hashCode(),
                    variableDeclarationNode);
        }

        if (variableDeclarationNode.typedBindingPattern().bindingPattern().kind() ==
                SyntaxKind.CAPTURE_BINDING_PATTERN) {

            Optional<Symbol> initSymbol = model.symbol(variableDeclarationNode.initializer().get());

            Integer key = variableDeclarationNode.typedBindingPattern().bindingPattern().
                    location().lineRange().hashCode();
            SyntaxKind initializerKind = variableDeclarationNode.initializer().get().kind();
            if (initSymbol.isPresent() && initializerKind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                putReference(key, getEndpointReference(initSymbol.get()));
            } else if (initializerKind == SyntaxKind.NUMERIC_LITERAL) {
                this.registerVariableRef(key, variableDeclarationNode);
            } else if (initializerKind == SyntaxKind.STRING_LITERAL) {
                this.registerVariableRef(key, variableDeclarationNode);
            }
        }

        visitSyntaxNode(variableDeclarationNode);
    }

    @Override
    public void visit(IfElseStatementNode ifElseStatementNode) {

        IfStatementNode ifStatementNode = new IfStatementNode();
        Node currentParentNode = this.currentNode;

        Node ifBodyNode = new Node();
        this.currentNode = ifBodyNode;

        visitSyntaxNode(ifElseStatementNode.ifBody());

        ifStatementNode.setIfBody(ifBodyNode.getNextNode());

        Node elseBodyNode = new Node();
        this.currentNode = elseBodyNode;

        if (ifElseStatementNode.elseBody().isPresent()) {
            visitSyntaxNode(ifElseStatementNode.elseBody().get());
            ifStatementNode.setElseBody(elseBodyNode.getNextNode());
        }

        this.currentNode = currentParentNode;
        this.setChildNode(ifStatementNode);
    }

    @Override
    public void visit(ForEachStatementNode forEachStatementNode) {

        io.ballerina.compiler.syntax.tree.Node node = forEachStatementNode.actionOrExpressionNode();
        long iterationsCount = DEFAULT_LOOP_SIZE;

        if (node.kind() == SyntaxKind.BRACED_EXPRESSION) {
            BracedExpressionNode bracedExpressionNode = (BracedExpressionNode) node;
            node = bracedExpressionNode.expression();

            if (node.kind() == SyntaxKind.BINARY_EXPRESSION) {
                if (((BinaryExpressionNode) node).rhsExpr().kind() == SyntaxKind.NUMERIC_LITERAL &&
                        ((BinaryExpressionNode) node).lhsExpr().kind() == SyntaxKind.NUMERIC_LITERAL) {
                    long rhsValue = Long.parseLong(((BinaryExpressionNode) node).rhsExpr().toString().trim());
                    long lhsValue = Long.parseLong(((BinaryExpressionNode) node).lhsExpr().toString().trim());
                    iterationsCount = rhsValue - lhsValue + 1;
                }
            }
        }

        ForStatementNode forStatementNode = new ForStatementNode(iterationsCount);
        Node currentParentNode = this.currentNode;

        Node forBodyNode = new Node();
        this.currentNode = forBodyNode;

        visitSyntaxNode(forEachStatementNode.blockStatement());
        forStatementNode.setForBody(forBodyNode.getNextNode());

        this.currentNode = currentParentNode;

        if (forEachStatementNode.blockStatement() != null) {
            this.setChildNode(forStatementNode);
        }

        visitSyntaxNode(forEachStatementNode);
    }

    @Override
    public void visit(WhileStatementNode whileStatementNode) {

        ForStatementNode forStatementNode = new ForStatementNode(DEFAULT_LOOP_SIZE);
        Node currentParentNode = this.currentNode;

        Node forBodyNode = new Node();
        this.currentNode = forBodyNode;

        visitSyntaxNode(whileStatementNode.whileBody());
        forStatementNode.setForBody(forBodyNode.getNextNode());

        this.currentNode = currentParentNode;
        if (forStatementNode.getForBody() != null) {
            this.setChildNode(forStatementNode);
        }
    }

    @Override
    public void visit(RemoteMethodCallActionNode remoteMethodCallActionNode) {

        resolveActionInvocation(remoteMethodCallActionNode);
        visitSyntaxNode(remoteMethodCallActionNode);
    }

    @Override
    public void visit(AssignmentStatementNode assignmentStatementNode) {

        if (assignmentStatementNode.varRef().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE &&
                assignmentStatementNode.expression().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {

            Optional<Symbol> referenceSymbol = model.symbol(assignmentStatementNode.varRef());

            if (referenceSymbol.isEmpty()) {
                return;
            }

            Optional<Symbol> expressionSymbol = model.symbol(assignmentStatementNode.expression());

            if (expressionSymbol.isEmpty()) {
                return;
            }

            putReference(getEndpointReference(referenceSymbol.get()),
                    getEndpointReference(expressionSymbol.get()));
        }

        visitSyntaxNode(assignmentStatementNode);
    }

    private void setChildNode(Node node) {

        this.currentNode.setNextNode(node);
        this.currentNode = node;
    }

    private Integer resolveReference(Integer key) {

        Integer result = -1;
        while (this.referenceMap.containsKey(key)) {
            key = this.referenceMap.get(key);
            result = key;
        }

        return result;
    }

    private void putReference(Integer key, Integer value) {

        if (this.referenceMap.containsKey(key)) {
            logger.error(("Replacing existing value=" +
                    this.referenceMap.get(key) +
                    " for key=" + key + " with value="
                    + value + ". Report with project files").replaceAll("[\r\n]", ""));
        }
        this.referenceMap.put(key, value);
    }

    private void registerVariableRef(Integer key, Object value) {

        if (this.variableMap.containsKey(key)) {
            logger.error(("Replacing existing value=" + this.referenceMap.get(key) + " for key=" + key + " with value="
                    + value + ". Report with project files").replaceAll("[\r\n]", ""));
        }
        this.variableMap.put(key, value);
    }

    private void resolveActionInvocation(RemoteMethodCallActionNode remoteMethodCallActionNode) {

        String actionName = remoteMethodCallActionNode.methodName().toString();
        String actionPath = null;
        if (remoteMethodCallActionNode.arguments().size() > 0) {
            SeparatedNodeList<FunctionArgumentNode> arguments = remoteMethodCallActionNode.arguments();
            FunctionArgumentNode functionArgumentNode = arguments.get(0);
            if (functionArgumentNode.kind() == SyntaxKind.POSITIONAL_ARG) {
                SyntaxKind parameterKind = ((PositionalArgumentNode) functionArgumentNode).expression().kind();
                if (parameterKind == SyntaxKind.STRING_LITERAL) {
                    actionPath = remoteMethodCallActionNode.arguments().get(0).toString();
                    if (actionPath.startsWith("\"") && actionPath.endsWith("\"")) {
                        actionPath = actionPath.substring(1, actionPath.length() - 1);
                    }
                } else if (parameterKind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                    Optional<Symbol> expressionSymbol = model.symbol(remoteMethodCallActionNode.expression());
                    if (expressionSymbol.isPresent()) {
                        int hashCode = getEndpointReference(expressionSymbol.get());
                        VariableDeclarationNode variableDeclarationNode = (VariableDeclarationNode)
                                variableMap.get(hashCode);
                        if (variableDeclarationNode != null) {
                            if (variableDeclarationNode.initializer().isPresent()) {
                                ExpressionNode token = variableDeclarationNode.initializer().get();
                                if (token.kind() == SyntaxKind.STRING_LITERAL) {
                                    actionPath = ((BasicLiteralNode) token).literalToken().text();
                                    actionPath = actionPath.substring(1, actionPath.length() - 1);
                                }
                            }
                        }
                    }
                }
            }
        }

        LineRange actionPos = (remoteMethodCallActionNode).lineRange();

        LineRange actionExpRange = remoteMethodCallActionNode.expression().lineRange();
        LinePosition actionStartPos = actionExpRange.startLine();
        LinePosition actionEndPos = actionExpRange.endLine();
        LinePosition actionExpPos =
                LinePosition.from(actionStartPos.line(), (actionStartPos.offset() + actionEndPos.offset()) / 2);

        Optional<Symbol> symbol = model.symbol(document, actionExpPos);
        if (symbol.isPresent()) {
            Integer endPointRef = getEndpointReference(symbol.get());
            if (!this.variableMap.containsKey(endPointRef)) {
                endPointRef = resolveReference(endPointRef);
            }

            if (withinRange) {
                String pos = actionPos.filePath() + "/" + actionPos;
                ActionInvocationNode actionNode = new ActionInvocationNode(endPointRef, actionName, actionPath, pos);
                this.currentNode.setNextNode(actionNode);
                this.setChildNode(actionNode);
            }
        }
    }

    private Integer getEndpointReference(Symbol symbol) {

        Optional<Location> location = symbol.getLocation();
        if (location.isEmpty()) {
            return -1;
        }
        LineRange lineRange = location.get().lineRange();
        String ref = lineRange.filePath() + lineRange.startLine() + lineRange.endLine();
        return Objects.hashCode(ref);
    }

    private void resolveEndPoint(Symbol symbol, ExpressionNode expressionNode) {

        String url = null;

        if (expressionNode.kind() == SyntaxKind.IMPLICIT_NEW_EXPRESSION) {
            ImplicitNewExpressionNode node = (ImplicitNewExpressionNode) expressionNode;
            if (node.parenthesizedArgList().isPresent() && !node.parenthesizedArgList().get().arguments().isEmpty()) {
                SeparatedNodeList<FunctionArgumentNode> arguments = node.parenthesizedArgList().get().arguments();
                url = findBaseUrl(arguments);
            }
        } else if (expressionNode.kind() == SyntaxKind.EXPLICIT_NEW_EXPRESSION) {
            ExplicitNewExpressionNode node = (ExplicitNewExpressionNode) expressionNode;
            if (!node.parenthesizedArgList().arguments().isEmpty()) {
                UrlFinder urlFinder = new UrlFinder();
                node.accept(urlFinder);
                url = extractURL(urlFinder.getToken());
            }
        } else if (expressionNode.kind() == SyntaxKind.CHECK_EXPRESSION) {
            ExpressionNode expNode = ((CheckExpressionNode) expressionNode).expression();
            resolveEndPoint(symbol, expNode);
            return;
        } else {
            return;
        }

        TypeSymbol typeSymbol = getTypeDescriptor(((VariableSymbol) symbol).typeDescriptor());

        String signature = typeSymbol.signature();
        Optional<ModuleSymbol> typeSymbolModule = typeSymbol.getModule();
        if (typeSymbolModule.isEmpty() || symbol.getLocation().isEmpty()) {
            return;
        }

        EndPointNode endpoint = new EndPointNode(typeSymbolModule.get().id().orgName() +
                "/" + typeSymbolModule.get().id().moduleName(),
                signature.substring(signature.
                        lastIndexOf(":") + 1), url, symbol.getLocation().get().lineRange());
        this.endPointDeclarationMap.put(getEndpointReference(symbol),
                endpoint);
    }

    private String findBaseUrl(SeparatedNodeList<FunctionArgumentNode> arguments) {
        // Assuming that the first argument is the base URL.
        for (FunctionArgumentNode argumentNode : arguments) {
            UrlFinder urlFinder = new UrlFinder();
            argumentNode.accept(urlFinder);

            String url = urlFinder.getToken();
            if (url.isEmpty()) {
                continue;
            }
            if (argumentNode.kind() == SyntaxKind.POSITIONAL_ARG &&
                    ((PositionalArgumentNode) argumentNode).expression().kind() == SyntaxKind.STRING_LITERAL) {
                visitSyntaxNode(argumentNode);
                return extractURL(url);
            } else if (argumentNode.kind() == SyntaxKind.NAMED_ARG &&
                    ((NamedArgumentNode) argumentNode).expression().kind() == SyntaxKind.STRING_LITERAL) {
                visitSyntaxNode(argumentNode);
                return extractURL(url);
            }
        }
        return null;
    }

    private String extractURL(String arg) {

        if (arg.startsWith("\"") && arg.endsWith("\"")) {
            return arg.substring(1, arg.length() - 1);
        } else {
            return arg;
        }
    }

    private boolean isClientObject(Symbol symbol) {

        if (symbol.kind() != SymbolKind.VARIABLE) {
            return false;
        }

        TypeSymbol typeSymbol = getTypeDescriptor(((VariableSymbol) symbol).typeDescriptor());

        return typeSymbol instanceof Qualifiable && ((Qualifiable) typeSymbol).qualifiers().contains(Qualifier.CLIENT);
    }

    private boolean isRecordObject(Symbol symbol) {

        return getTypeKind(symbol) == TypeDescKind.RECORD;
    }

    private TypeDescKind getTypeKind(Symbol symbol) {

        if (symbol.kind() != SymbolKind.VARIABLE) {
            return null;
        }

        TypeSymbol typeSymbol = ((VariableSymbol) symbol).typeDescriptor();
        TypeDescKind kind = typeSymbol.typeKind();

        if (kind == null) {
            return null;
        }

        if (kind == TypeDescKind.TYPE_REFERENCE) {
            return ((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor().typeKind();
        }
        return typeSymbol.typeKind();
    }

    private TypeSymbol getTypeDescriptor(TypeSymbol typeSymbol) {

        TypeDescKind kind = typeSymbol.typeKind();

        if (kind == null) {
            return typeSymbol;
        }

        if (kind == TypeDescKind.TYPE_REFERENCE) {
            return ((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor();
        }
        return typeSymbol;
    }

    public HashMap<String, Object> getActionInvocations() {

        ParserUtil.getReducedTree(this.startNode);

        HashMap<String, Object> invocationInfo = new HashMap<>();
        invocationInfo.put(ENDPOINTS_KEY, this.endPointDeclarationMap);
        invocationInfo.put(ACTION_INVOCATION_KEY, this.startNode);
        return invocationInfo;
    }
}
