package parser;


import ast.*;
import constants.SqlKeywords;
import constants.TokenType;
import lexer.Token;
import lexer.Tokenizer;

import java.util.*;

public class SqlParser {
    private final Tokenizer t;

    public SqlParser(String sql) {
        this.t = new Tokenizer(sql);
    }

    public Query parse() {
        Query q = new Query();
        consumeKeyword(SqlKeywords.SELECT);

        q.selectItems.addAll(parseSelectList());


        if (peekIsKeyword("FROM")) {
            consumeKeyword("FROM");
            while (true) {
                if (t.peek().type == TokenType.IDENT) {
                    q.fromTables.add(t.next().text);
                } else {
                    throw new RuntimeException("Expected table name in FROM clause, found: " + t.peek());
                }

                if (t.peek().type == TokenType.COMMA) {
                    t.next();
                    continue;
                }
                break;
            }
        }


        if (peekIsKeyword(SqlKeywords.WHERE)) {
            consumeKeyword(SqlKeywords.WHERE);
            q.where = parseExpression();
        } else {
            q.where = null;
        }

        return q;
    }

    private List<Query.SelectItem> parseSelectList() {
        List<Query.SelectItem> items = new ArrayList<>();

        while (true) {
            Expr expr;

            if (t.peek().type == TokenType.MUL) {

                t.next();
                expr = new ColumnExpr(Collections.singletonList("*"));
            } else {
                expr = parseExpression();
            }

            String alias = null;
            if (t.peek().type == TokenType.AS) {
                t.next();
                Token tk = t.peek();
                if (tk.type == TokenType.IDENT) {
                    alias = t.next().text;
                } else {
                    throw new RuntimeException("Expected identifier after AS");
                }
            } else if (t.peek().type == TokenType.IDENT) {
                String cand = t.peek().text;
                if (!isSqlKeyword(cand)) {
                    alias = t.next().text;
                }
            }

            items.add(new Query.SelectItem(expr, alias));

            if (t.peek().type == TokenType.COMMA) {
                t.next();
                continue;
            }

            break;
        }

        return items;
    }


    private Expr parseExpression() { return parseOr(); }

    private Expr parseOr() {
        Expr left = parseAnd();
        while (peekIsKeyword(SqlKeywords.OR)) {
            consumeKeyword(SqlKeywords.OR);
            Expr right = parseAnd();
            left = new BinaryExpr(left,"OR" , right);
        }
        return left;
    }

    private Expr parseAnd() {
        Expr left = parseNot();
        while (peekIsKeyword(SqlKeywords.AND)) {
            consumeKeyword(SqlKeywords.AND);
            Expr right = parseNot();
            left = new BinaryExpr( left,"AND", right);
        }
        return left;
    }

    private Expr parseNot() {
        if (peekIsKeyword(SqlKeywords.NOT) || t.peek().type == TokenType.NOT) {
            if (t.peek().type == TokenType.NOT) t.next(); else consumeKeyword(SqlKeywords.NOT);
            return new UnaryExpr("NOT", parseNot());
        }
        return parseComparison();
    }

    private Expr parseComparison() {
        Expr left = parseAdditive();
        while (true) {
            Token tk = t.peek();
            if (tk.type == TokenType.EQ || tk.type == TokenType.NEQ || tk.type == TokenType.LT ||
                    tk.type == TokenType.GT || tk.type == TokenType.LE || tk.type == TokenType.GE) {
                t.next();
                Expr right = parseAdditive();
                left = new BinaryExpr(left,tk.text,  right);
            } else if (peekIsKeyword(SqlKeywords.LIKE) || t.peek().type == TokenType.LIKE) {
                if (t.peek().type == TokenType.LIKE) t.next(); else consumeKeyword(SqlKeywords.LIKE);
                Expr right = parseAdditive();
                left = new BinaryExpr(left,"LIKE",  right);
            } else if (peekIsKeyword(SqlKeywords.IN) || t.peek().type == TokenType.IN) {
                if (t.peek().type == TokenType.IN) t.next(); else consumeKeyword(SqlKeywords.IN);
                expect(TokenType.LPAREN, "(");
                List<Expr> items = new ArrayList<>();
                if (t.peek().type != TokenType.RPAREN) {
                    while (true) {
                        items.add(parseExpression());
                        if (t.peek().type == TokenType.COMMA) { t.next(); continue; }
                        break;
                    }
                }
                expect(TokenType.RPAREN, ")");
                left = new InExpr(left, items);
            } else {
                break;
            }
        }
        return left;
    }

    private Expr parseAdditive() {
        Expr left = parseMultiplicative();
        while (true) {
            if (t.peek().type == TokenType.PLUS) {
                t.next();
                left = new BinaryExpr(left,"+",  parseMultiplicative());
            } else if (t.peek().type == TokenType.MINUS) {
                t.next();
                left = new BinaryExpr(left,"-",  parseMultiplicative());
            } else break;
        }
        return left;
    }

    private Expr parseMultiplicative() {
        Expr left = parseUnary();
        while (true) {
            if (t.peek().type == TokenType.MUL) {
                t.next();
                left = new BinaryExpr(left,"*",  parseUnary());
            } else if (t.peek().type == TokenType.DIV) {
                t.next();
                left = new BinaryExpr(left,"/", parseUnary());
            } else if (t.peek().type == TokenType.MOD) {
                t.next();
                left = new BinaryExpr(left,"%",  parseUnary());
            } else break;
        }
        return left;
    }

    private Expr parseUnary() {
        if (t.peek().type == TokenType.PLUS) {
            t.next();
            return new UnaryExpr("+", parseUnary());
        } else if (t.peek().type == TokenType.MINUS) {
            t.next();
            return new UnaryExpr("-", parseUnary());
        } else if (peekIsKeyword(SqlKeywords.NOT) || t.peek().type == TokenType.NOT) {
            if (t.peek().type == TokenType.NOT) t.next(); else consumeKeyword(SqlKeywords.NOT);
            return new UnaryExpr("NOT", parseUnary());
        }
        return parsePrimary();
    }

    private Expr parsePrimary() {
        Token tk = t.peek();
        if (tk.type == TokenType.NUMBER) {
            t.next();
            String txt = tk.text;
            if (txt.contains(".")) return new LiteralExpr(Double.parseDouble(txt));
            try { return new LiteralExpr(Long.parseLong(txt)); }
            catch (NumberFormatException ex) { return new LiteralExpr(Double.parseDouble(txt)); }
        }
        if (tk.type == TokenType.STRING) {
            t.next();
            String s = tk.text;
            if (s.length() >= 2 && s.charAt(0) == '\'' && s.charAt(s.length()-1) == '\'') {
                String inner = s.substring(1, s.length()-1).replace("''", "'");
                return new LiteralExpr(inner);
            } else return new LiteralExpr(s);
        }
        if (tk.type == TokenType.LPAREN) {
            t.next();
            Expr e = parseExpression();
            expect(TokenType.RPAREN, ")");
            return e;
        }
        if (tk.type == TokenType.IDENT) {
            String name = t.next().text;
            if (t.peek().type == TokenType.LPAREN) {
                t.next();
                List<Expr> args = new ArrayList<>();
                if (t.peek().type != TokenType.RPAREN) {
                    while (true) {
                        args.add(parseExpression());
                        if (t.peek().type == TokenType.COMMA) { t.next(); continue; }
                        break;
                    }
                }
                expect(TokenType.RPAREN, ")");
                return new FuncCallExpr(name, args);
            } else {
                List<String> parts = new ArrayList<>();
                parts.add(name);
                while (t.peek().type == TokenType.DOT) {
                    t.next();
                    if (t.peek().type == TokenType.IDENT) parts.add(t.next().text);
                    else throw new RuntimeException("Expected identifier after '.'");
                }
                return new ColumnExpr(parts);
            }
        }
        throw new RuntimeException("Unexpected token: " + tk);
    }


    private void expect(TokenType type, String display) {
        Token tk = t.peek();
        if (tk.type != type) throw new RuntimeException("Expected " + display + " but found " + tk);
        t.next();
    }

    private boolean peekIsKeyword(String kw) {
        Token p = t.peek();
        if (p == null) return false;
        if (p.type == TokenType.IDENT) return p.text.equalsIgnoreCase(kw);
        switch (kw.toUpperCase(Locale.ROOT)) {
            case "AND": return p.type == TokenType.AND;
            case "OR": return p.type == TokenType.OR;
            case "NOT": return p.type == TokenType.NOT;
            case "LIKE": return p.type == TokenType.LIKE;
            case "IN": return p.type == TokenType.IN;
            case "AS": return p.type == TokenType.AS;
            case "FROM": return p.type == TokenType.IDENT && p.text.equalsIgnoreCase("FROM");
            case "WHERE": return p.type == TokenType.IDENT && p.text.equalsIgnoreCase("WHERE");
            case "SELECT": return p.type == TokenType.IDENT && p.text.equalsIgnoreCase("SELECT");
            default: return false;
        }
    }

    private void consumeKeyword(String kw) {
        Token p = t.peek();
        if (p == null) throw new RuntimeException("Expected keyword " + kw + " but got EOF");
        if (p.type == TokenType.IDENT && p.text.equalsIgnoreCase(kw)) { t.next(); return; }
        switch (kw.toUpperCase(Locale.ROOT)) {
            case "AND": if (p.type == TokenType.AND) { t.next(); return; } break;
            case "OR": if (p.type == TokenType.OR) { t.next(); return; } break;
            case "NOT": if (p.type == TokenType.NOT) { t.next(); return; } break;
            case "LIKE": if (p.type == TokenType.LIKE) { t.next(); return; } break;
            case "IN": if (p.type == TokenType.IN) { t.next(); return; } break;
            case "AS": if (p.type == TokenType.AS) { t.next(); return; } break;
            default: break;
        }
        throw new RuntimeException("Expected keyword '" + kw + "' but found " + p);
    }

    private boolean isSqlKeyword(String ident) {
        if (ident == null) return false;
        switch (ident.toUpperCase(Locale.ROOT)) {
            case "SELECT":
            case "FROM":
            case "WHERE":
            case "AS":
            case "AND":
            case "OR":
            case "NOT":
            case "LIKE":
            case "IN":
                return true;
            default:
                return false;
        }
    }
}
