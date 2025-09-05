package ast;

import java.util.List;
import java.util.stream.Collectors;

public class InExpr implements Expr {
    public final Expr left;
    public final List<Expr> items;

    public InExpr(Expr left, List<Expr> items) {
        this.left = left;
        this.items = items;
    }

    @Override
    public String toString() {
        String joined = items.stream().map(Object::toString).collect(Collectors.joining(", "));
        return "(" + left + " IN (" + joined + "))";
    }
}
