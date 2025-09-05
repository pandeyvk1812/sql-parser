package ast;

import java.util.List;
import java.util.stream.Collectors;

public class FuncCallExpr implements Expr {
    public final String name;
    public final List<Expr> args;

    public FuncCallExpr(String name, List<Expr> args) {
        this.name = name;
        this.args = args;
    }

    @Override
    public String toString() {
        String joined = args.stream().map(Object::toString).collect(Collectors.joining(", "));
        return name + "(" + joined + ")";
    }
}
