package lexer;

import constants.TokenType;

import java.util.*;
import java.util.regex.*;

public class Tokenizer {
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "\\s*(?:" +
                    "(>=|<=|!=|<>|=|<|>)|" +
                    "(,)|" +
                    "(\\()|" +
                    "(\\))|" +
                    "(\\.)|" +
                    "(\\+)|" +
                    "(-)|" +
                    "(\\*)|" +
                    "(/)|" +
                    "(%)|" +
                    "('(?:''|[^'])*')|" +
                    "([0-9]+(?:\\.[0-9]+)?)|" +
                    "([A-Za-z_][A-Za-z0-9_]*)|" +
                    "(.)" +
                    ")"
    );

    private final Matcher matcher;
    private Token nextToken;

    public Tokenizer(String input) {
        matcher = TOKEN_PATTERN.matcher(input);
        advance();
    }

    private void advance() {
        if (!matcher.find()) {
            nextToken = new Token(TokenType.EOF, "");
            return;
        }

        String group;

        if ((group = matcher.group(1)) != null) {
            nextToken = switch (group) {
                case ">=" -> new Token(TokenType.GE, group);
                case "<=" -> new Token(TokenType.LE, group);
                case "!=" -> new Token(TokenType.NEQ, group);
                case "<>" -> new Token(TokenType.NEQ, group);
                case "="  -> new Token(TokenType.EQ, group);
                case "<"  -> new Token(TokenType.LT, group);
                case ">"  -> new Token(TokenType.GT, group);
                default   -> new Token(TokenType.EQ, group);
            };
        }
        else if ((group = matcher.group(2)) != null) nextToken = new Token(TokenType.COMMA, group);
        else if ((group = matcher.group(3)) != null) nextToken = new Token(TokenType.LPAREN, group);
        else if ((group = matcher.group(4)) != null) nextToken = new Token(TokenType.RPAREN, group);
        else if ((group = matcher.group(5)) != null) nextToken = new Token(TokenType.DOT, group);
        else if ((group = matcher.group(6)) != null) nextToken = new Token(TokenType.PLUS, group);
        else if ((group = matcher.group(7)) != null) nextToken = new Token(TokenType.MINUS, group);
        else if ((group = matcher.group(8)) != null) nextToken = new Token(TokenType.MUL, group);
        else if ((group = matcher.group(9)) != null) nextToken = new Token(TokenType.DIV, group);
        else if ((group = matcher.group(10)) != null) nextToken = new Token(TokenType.MOD, group);
        else if ((group = matcher.group(11)) != null) nextToken = new Token(TokenType.STRING, group);
        else if ((group = matcher.group(12)) != null) nextToken = new Token(TokenType.NUMBER, group);
        else if ((group = matcher.group(13)) != null) {
            String upper = group.toUpperCase(Locale.ROOT);
            nextToken = switch (upper) {
                case "AND" -> new Token(TokenType.AND, upper);
                case "OR"  -> new Token(TokenType.OR, upper);
                case "NOT" -> new Token(TokenType.NOT, upper);
                case "LIKE"-> new Token(TokenType.LIKE, upper);
                case "IN"  -> new Token(TokenType.IN, upper);
                case "AS"  -> new Token(TokenType.AS, upper);
                default    -> new Token(TokenType.IDENT, group);
            };
        }
        else {
            nextToken = new Token(TokenType.EOF, "");
        }
    }

    public Token peek() { return nextToken; }
    public Token next() { Token t = nextToken; advance(); return t; }

    public boolean match(TokenType type) {
        if (nextToken.type == type) {
            advance();
            return true;
        }
        return false;
    }
}
