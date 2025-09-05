package ast;

import ast.Expr;

public class BinaryExpr implements Expr {
    private final Expr left;
    private final String op;
    private final Expr right;

    public BinaryExpr(Expr left, String op, Expr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public String toString() {
        return "(" + left.toString() + " " + op + " " + right.toString() + ")";
    }
}
