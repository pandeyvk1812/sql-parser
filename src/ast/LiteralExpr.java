package ast;

public class LiteralExpr implements Expr {
    public final Object value;
    public LiteralExpr(Object value) { this.value = value; }
    public String toString() { return value instanceof String ? "'" + value + "'" : value.toString(); }
}
