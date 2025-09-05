package ast;

public class UnaryExpr implements Expr {
    public final String op;
    public final Expr expr;

    public UnaryExpr(String op, Expr expr) {
        this.op = op;
        this.expr = expr;
    }

    @Override
    public String toString() {
        return "(" + op + " " + expr + ")";
    }
}
