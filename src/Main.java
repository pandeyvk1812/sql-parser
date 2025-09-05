import ast.Query;
import parser.SqlParser;

public class Main {
    public static void main(String[] args) {
        String[] tests = {
                "SELECT id, name, SUM(salary) AS total FROM employees WHERE age > 30 AND status = 'active'",
                "SELECT *, LOWER(email) aliasEmail FROM users WHERE (age >= 18 AND country = 'US') OR status = 'vip'",
                "SELECT id, price * quantity AS line_total FROM orders WHERE price * quantity > 1000 OR product IN ('A','B','C')",
                "SELECT name FROM people WHERE NOT (name LIKE 'J%') AND (score / 2 >= 50)"
        };

        for (String sql : tests) {
            System.out.println("INPUT: " + sql);
            SqlParser parser = new SqlParser(sql);
            try {
                Query q = parser.parse();
                System.out.println("PARSED: " + q);
            } catch (Exception ex) {
                System.out.println("PARSE ERROR: " + ex.getMessage());
            }
            System.out.println("----------------------------------------------------\n");
        }
    }
}
