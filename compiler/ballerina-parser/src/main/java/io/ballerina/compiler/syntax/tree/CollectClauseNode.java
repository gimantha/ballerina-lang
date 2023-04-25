package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;

import java.util.Objects;

public class CollectClauseNode extends ClauseNode {

    public CollectClauseNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token selectKeyword() {
        return childInBucket(0);
    }

    public ExpressionNode expression() {
        return childInBucket(1);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(NodeTransformer<T> visitor) {
        return visitor.transform(this);
    }

    @Override
    protected String[] childNames() {
        return new String[]{
                "selectKeyword",
                "expression"};
    }

    public CollectClauseNode modify(
            Token collectKeyword,
            ExpressionNode expression) {
        if (checkForReferenceEquality(
                collectKeyword,
                expression)) {
            return this;
        }

        return NodeFactory.createCollectClauseNode(
                collectKeyword,
                expression);
    }

    public CollectClauseNode.CollectClauseNodeModifier modify() {
        return new CollectClauseNode.CollectClauseNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class CollectClauseNodeModifier {
        private final CollectClauseNode oldNode;
        private Token selectKeyword;
        private ExpressionNode expression;

        public CollectClauseNodeModifier(CollectClauseNode oldNode) {
            this.oldNode = oldNode;
            this.selectKeyword = oldNode.selectKeyword();
            this.expression = oldNode.expression();
        }

        public CollectClauseNode.CollectClauseNodeModifier withSelectKeyword(
                Token selectKeyword) {
            Objects.requireNonNull(selectKeyword, "selectKeyword must not be null");
            this.selectKeyword = selectKeyword;
            return this;
        }

        public CollectClauseNode.CollectClauseNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public CollectClauseNode apply() {
            return oldNode.modify(
                    selectKeyword,
                    expression);
        }
    }
}
