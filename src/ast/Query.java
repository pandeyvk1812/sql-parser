package ast;

import java.util.ArrayList;
import java.util.List;

public class Query {
    public final List<SelectItem> selectItems = new ArrayList<>();
    public final List<String> fromTables = new ArrayList<>();

    public Expr where;

    public static class SelectItem {
        public final Expr expr;
        public final String alias;
        public SelectItem(Expr expr, String alias) {
            this.expr = expr; this.alias = alias;
        }
        public String toString() {
            return expr + (alias != null ? " AS " + alias : "");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(String.join(", ",
                selectItems.stream().map(Object::toString).toList()));
        if (!fromTables.isEmpty()) {
            sb.append(" FROM ").append(String.join(", ", fromTables));
        }
        if (where != null) {
            sb.append(" WHERE ").append(where);
        }
        return sb.toString();
    }
}
