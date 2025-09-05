package ast;

import java.util.List;

public class ColumnExpr implements Expr {
    public final List<String> parts;
    public ColumnExpr(List<String> parts) { this.parts = parts; }
    public String toString() { return String.join(".", parts); }
}
