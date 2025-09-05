package util;


import ast.Expr;

public class ExprPrinter {
    public static String print(Expr e) {
        return e == null ? "<none>" : e.toString();
    }
}
