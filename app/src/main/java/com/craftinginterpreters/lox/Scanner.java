package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // we are at the beginning of the next lexeme
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // handle single character lexemes
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;

            // handle single/double lexemes
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;

            // slashes are special since they are division and comments
            case '/':
                if (match('/')) {
                    // comments extend until the end of the line
                    while (peek() != '\n' && !isAtEnd())
                        advance();

                    // save the comment
                    comment();

                } else if (match('*')) {
                    // save the block comment
                    block_comment();

                } else {
                    addToken(TokenType.SLASH);
                }
                break;

            // handle whitespace
            case ' ':
            case '\r':
            case '\t':
                // ignore whitespace
                break;
            case '\n':
                line++;
                break;

            // handle literals
            case '"':
                string();
                break;

            default:
                // handle numeric literals
                if (isDigit(c)) {
                    number();

                    // handle reserved words
                } else if (isAlpha(c)) {
                    identifier();

                    // handle unhandled
                } else {
                    Lox.error(line, "Unexpected character");
                }
                break;
        }
    }

    private void string() {
        // advance until we find the closing double quote
        // this appears to allow multi-line string literals
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string (missing closing double quote");
            return;
        }

        // consume the closing double quote
        advance();

        // trim the surrounding quotes
        // this is where we would handle unescaping other chars like \n
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private void number() {
        while (isDigit(peek()))
            advance();

        // look for a fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            // consume the decimal point
            advance();

            while (isDigit(peek()))
                advance();
        }

        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void comment() {
        addToken(TokenType.COMMENT, source.substring(start, current));
    }

    private void block_comment() {
        // comments extend until reading closing */
        // If next char isn't * and we aren't at the end, or if next char and next next
        // char aren't */, advance
        while (!isAtEnd()) {
            // break loop if we found the closing block
            if (peek() == '*' && peekNext() == '/')
                break;
            if (peek() == '\n') {
                line++;
            }
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated block comment (missing closing */)");
            return;
        }

        if (peek() == '*' && peekNext() == '/') {
            // consume the closing */
            advance();
            advance();

            // trim the surrounding quotes
            // this is where we would handle unescaping other chars like \n
            String value = source.substring(start + 2, current - 2);
            addToken(TokenType.BLOCK_COMMENT, value.trim());

        } else {
            Lox.error(line, "Malformed block comment (missing closing */)");
            return;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek()))
            advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null)
            type = TokenType.IDENTIFIER;
        addToken(type);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        // returns current character then consumes it by incrementing
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected) {
        // TODO understand this better. Debug.
        // I don't understand how charAt(current) checks the next char.
        // I think it's because advance() does: current++ and increments after getting
        // the char at current
        // This is like a conditional advance().
        // Only consume the current character if it's what we are looking for.
        // if at the end or we have a character that doesn't match expected, then we
        // don't have a match
        // i.e. if expected is '=' then we are trying to discern > from >=

        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;

        // only consume the character if we get a match
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        // peeks 1 char beyond next char
        // TODO could we rewrite peek() to default to current char but allow an offset?
        if (current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
