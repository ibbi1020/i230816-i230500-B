import java.util.*;

/**
 * ManualScanner - DFA-Based Lexical Analyzer
 * CS4031 - Compiler Construction - Assignment 01
 * Implements manual token recognition using minimized DFA transition tables
 */
public class ManualScanner {
    
    private String input;
    private int pos;
    private int line;
    private int column;
    
    private List<Token> tokens;
    private SymbolTable symbolTable;
    private ErrorHandler errorHandler;
    
    // Statistics
    private Map<TokenType, Integer> tokenCounts;
    private int totalTokens;
    private int linesProcessed;
    private int commentsRemoved;
    
    public ManualScanner(String input) {
        this.input = input;
        this.pos = 0;
        this.line = 1;
        this.column = 1;
        
        this.tokens = new ArrayList<>();
        this.symbolTable = new SymbolTable();
        this.errorHandler = new ErrorHandler();
        this.tokenCounts = new HashMap<>();
    }
    
    public List<Token> scan() {
        while (pos < input.length()) {
            Token token = nextToken();
            if (token != null) {
                // Ignore whitespace and comments in final list
                if (token.getType() != TokenType.WHITESPACE && 
                    token.getType() != TokenType.COMMENT_SINGLE && 
                    token.getType() != TokenType.COMMENT_MULTI) {
                    
                    tokens.add(token);
                    
                    if (token.getType() != TokenType.EOF) {
                        totalTokens++;
                        tokenCounts.put(token.getType(), tokenCounts.getOrDefault(token.getType(), 0) + 1);
                    }
                    
                    if (token.getType() == TokenType.IDENTIFIER) {
                        symbolTable.addIdentifier(token.getLexeme(), token.getLine(), token.getColumn());
                    }
                }
            }
        }
        
        // Add EOF token
        Token eofToken = new Token(TokenType.EOF, "", line, column);
        tokens.add(eofToken);
        tokenCounts.put(TokenType.EOF, 1);
        linesProcessed = line;
        return tokens;
    }
    
    private Token nextToken() {
        if (pos >= input.length()) return null;
        
        // 1. Comments
        Token comment = scanComments();
        if (comment != null) return comment;
        
        // 2. Operators
        Token op = scanMultiCharOperator();
        if (op != null) return op;
        
        // 3. Keywords
        Token kw = scanKeyword();
        if (kw != null) return kw;
        
        // 4. Booleans
        Token bool = scanBooleanLiteral();
        if (bool != null) return bool;
        
        // 5. Identifiers
        Token id = scanIdentifier();
        if (id != null) return id;
        
        // 6. Floats
        Token fl = scanFloatingPointLiteral();
        if (fl != null) return fl;
        
        // 7. Integers
        Token in = scanIntegerLiteral();
        if (in != null) return in;
        
        // 8. Strings (Strict + Error Fallback)
        Token str = scanStringLiteral();
        if (str != null) return str;
        
        // 9. Chars (Strict + Error Fallback)
        Token chLit = scanCharacterLiteral();
        if (chLit != null) return chLit;
        
        // 10. Single Char Ops
        Token single = scanSingleCharToken();
        if (single != null) return single;
        
        // 11. Whitespace
        Token ws = scanWhitespace();
        if (ws != null) return ws;
        
        // 12. Invalid Char (Fallback)
        int startLine = line;
        int startColumn = column;
        char ch = input.charAt(pos);
        errorHandler.reportInvalidCharacter(ch, startLine, startColumn);
        advance();
        return new Token(TokenType.ERROR, String.valueOf(ch), startLine, startColumn);
    }
    
    // --- SCANNERS ---

    private Token scanComments() {
        // Multi-line
        if (peek("#*")) {
            int startPos = pos; int startLine = line; int startColumn = column;
            advance(); advance();
            boolean closed = false;
            while (pos < input.length()) {
                if (peek("*#")) { advance(); advance(); closed = true; break; }
                advance();
            }
            String lexeme = input.substring(startPos, pos);
            if (!closed) {
                errorHandler.reportUnclosedComment(startLine, startColumn);
                return new Token(TokenType.ERROR, lexeme, startLine, startColumn);
            }
            commentsRemoved++;
            return new Token(TokenType.COMMENT_MULTI, lexeme, startLine, startColumn);
        }
        // Single-line
        if (peek("##")) {
            int startLine = line; int startColumn = column;
            advance(); advance();
            while (pos < input.length() && input.charAt(pos) != '\n') advance();
            commentsRemoved++;
            return new Token(TokenType.COMMENT_SINGLE, "comment", startLine, startColumn);
        }
        return null;
    }
    
    // *** FIX: String Literal Logic ***
    private Token scanStringLiteral() {
        if (input.charAt(pos) != '"') return null;
        
        int startPos = pos;
        int startLine = line;
        int startColumn = column;
        
        // PRE-CHECK: Is this a perfectly valid string?
        // We look ahead without moving 'pos' to see if it matches the strict regex
        boolean isValid = true;
        int tempPos = pos + 1; // skip "
        
        while (tempPos < input.length()) {
            char ch = input.charAt(tempPos);
            if (ch == '"') {
                tempPos++; // consume closing quote
                break; // Successfully closed
            }
            else if (ch == '\n' || ch == '\r') {
                isValid = false; break; // Error: Newline
            }
            else if (ch == '\\') {
                // Check escape
                if (tempPos + 1 >= input.length()) { isValid = false; break; }
                char next = input.charAt(tempPos + 1);
                if (next == '"' || next == '\\' || next == 'n' || next == 't' || next == 'r') {
                    tempPos += 2; // Valid escape
                } else {
                    isValid = false; break; // Invalid escape like \x
                }
            }
            else {
                tempPos++;
            }
        }
        
        // Check if we ran off the end
        if (tempPos > input.length() || (tempPos == input.length() && input.charAt(tempPos-1) != '"')) {
            isValid = false;
        }

        // EXECUTE:
        if (isValid) {
            // It is valid, so we consume it normally
            pos = startPos; line = startLine; column = startColumn;
            int length = tempPos - startPos;
            for (int i = 0; i < length; i++) advance();
            return new Token(TokenType.STRING_LITERAL, input.substring(startPos, pos), startLine, startColumn);
        } else {
            // It is invalid, so consume until newline (JFlex UntermString rule)
            pos = startPos; line = startLine; column = startColumn;
            advance(); // consume "
            while (pos < input.length()) {
                char ch = input.charAt(pos);
                if (ch == '\n' || ch == '\r') break;
                advance();
            }
            String lexeme = input.substring(startPos, pos);
            errorHandler.reportUnterminatedString(startLine, startColumn);
            return new Token(TokenType.ERROR, lexeme, startLine, startColumn);
        }
    }

    // *** FIX: Char Literal Logic ***
    private Token scanCharacterLiteral() {
        if (input.charAt(pos) != '\'') return null;
        
        int startPos = pos;
        int startLine = line;
        int startColumn = column;
        
        // PRE-CHECK
        boolean isValid = true;
        int tempPos = pos + 1;
        
        while (tempPos < input.length()) {
            char ch = input.charAt(tempPos);
            if (ch == '\'') {
                tempPos++; break; 
            }
            else if (ch == '\n' || ch == '\r') {
                isValid = false; break;
            }
            else if (ch == '\\') {
                if (tempPos + 1 >= input.length()) { isValid = false; break; }
                char next = input.charAt(tempPos + 1);
                if (next == '\'' || next == '\\' || next == 'n' || next == 't' || next == 'r') {
                    tempPos += 2;
                } else {
                    isValid = false; break;
                }
            }
            else {
                tempPos++;
            }
        }
        
        if (tempPos > input.length() || (tempPos == input.length() && input.charAt(tempPos-1) != '\'')) {
            isValid = false;
        }

        if (isValid) {
            pos = startPos; line = startLine; column = startColumn;
            int length = tempPos - startPos;
            for (int i = 0; i < length; i++) advance();
            return new Token(TokenType.CHAR_LITERAL, input.substring(startPos, pos), startLine, startColumn);
        } else {
            pos = startPos; line = startLine; column = startColumn;
            advance(); // consume '
            while (pos < input.length()) {
                char ch = input.charAt(pos);
                if (ch == '\n' || ch == '\r') break;
                advance();
            }
            String lexeme = input.substring(startPos, pos);
            errorHandler.reportUnterminatedCharLiteral(startLine, startColumn);
            return new Token(TokenType.ERROR, lexeme, startLine, startColumn);
        }
    }
    
    private Token scanMultiCharOperator() {
        if (pos + 1 >= input.length()) return null;
        int startLine = line; int startColumn = column;
        String twoChar = input.substring(pos, pos+2);
        TokenType type = null;
        switch (twoChar) {
            case "**": type = TokenType.OP_EXPONENT; break;
            case "==": type = TokenType.OP_EQUAL; break;
            case "!=": type = TokenType.OP_NOT_EQUAL; break;
            case "<=": type = TokenType.OP_LESS_EQUAL; break;
            case ">=": type = TokenType.OP_GREATER_EQUAL; break;
            case "&&": type = TokenType.OP_AND; break;
            case "||": type = TokenType.OP_OR; break;
            case "+=": type = TokenType.OP_PLUS_ASSIGN; break;
            case "-=": type = TokenType.OP_MINUS_ASSIGN; break;
            case "*=": type = TokenType.OP_MULTIPLY_ASSIGN; break;
            case "/=": type = TokenType.OP_DIVIDE_ASSIGN; break;
            case "++": type = TokenType.OP_INCREMENT; break;
            case "--": type = TokenType.OP_DECREMENT; break;
        }
        if (type != null) { advance(); advance(); return new Token(type, twoChar, startLine, startColumn); }
        return null;
    }
    
    private Token scanKeyword() {
        if (!Character.isLowerCase(input.charAt(pos))) return null;
        int startPos = pos; int startLine = line; int startColumn = column;
        while (pos < input.length() && Character.isLowerCase(input.charAt(pos))) advance();
        String word = input.substring(startPos, pos);
        TokenType type = null;
        switch (word) {
            case "start": type = TokenType.KEYWORD_START; break;
            case "finish": type = TokenType.KEYWORD_FINISH; break;
            case "loop": type = TokenType.KEYWORD_LOOP; break;
            case "condition": type = TokenType.KEYWORD_CONDITION; break;
            case "declare": type = TokenType.KEYWORD_DECLARE; break;
            case "output": type = TokenType.KEYWORD_OUTPUT; break;
            case "input": type = TokenType.KEYWORD_INPUT; break;
            case "function": type = TokenType.KEYWORD_FUNCTION; break;
            case "return": type = TokenType.KEYWORD_RETURN; break;
            case "break": type = TokenType.KEYWORD_BREAK; break;
            case "continue": type = TokenType.KEYWORD_CONTINUE; break;
            case "else": type = TokenType.KEYWORD_ELSE; break;
        }
        if (type != null) return new Token(type, word, startLine, startColumn);
        reset(startPos, startLine, startColumn); return null;
    }
    
    private Token scanBooleanLiteral() {
        int startLine = line; int startColumn = column;
        if (peek("true")) { advance(4); return new Token(TokenType.BOOLEAN_LITERAL, "true", startLine, startColumn); }
        if (peek("false")) { advance(5); return new Token(TokenType.BOOLEAN_LITERAL, "false", startLine, startColumn); }
        return null;
    }
    
    private Token scanIdentifier() {
        if (!Character.isUpperCase(input.charAt(pos))) return null;
        int startPos = pos; int startLine = line; int startColumn = column;
        advance();
        while (pos < input.length()) {
            char ch = input.charAt(pos);
            if (Character.isLowerCase(ch) || Character.isDigit(ch) || ch == '_') advance();
            else break;
        }
        String lexeme = input.substring(startPos, pos);
        if (lexeme.length() > 31) {
            errorHandler.reportIdentifierTooLong(lexeme, startLine, startColumn);
            return new Token(TokenType.ERROR, lexeme, startLine, startColumn);
        }
        return new Token(TokenType.IDENTIFIER, lexeme, startLine, startColumn);
    }
    
    private Token scanFloatingPointLiteral() {
        int savePos = pos; int saveLine = line; int saveColumn = column;
        int startLine = line; int startColumn = column;
        
        if (input.charAt(pos) == '+' || input.charAt(pos) == '-') advance();
        if (pos >= input.length() || !Character.isDigit(input.charAt(pos))) { reset(savePos, saveLine, saveColumn); return null; }
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) advance();
        if (pos >= input.length() || input.charAt(pos) != '.') { reset(savePos, saveLine, saveColumn); return null; }
        advance(); // .
        if (pos >= input.length() || !Character.isDigit(input.charAt(pos))) { reset(savePos, saveLine, saveColumn); return null; }
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) advance();
        
        if (pos < input.length() && (input.charAt(pos) == 'e' || input.charAt(pos) == 'E')) {
            int ePos = pos; int eLine = line; int eCol = column;
            advance();
            if (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-')) advance();
            if (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                while (pos < input.length() && Character.isDigit(input.charAt(pos))) advance();
            } else { reset(ePos, eLine, eCol); }
        }
        return new Token(TokenType.FLOAT_LITERAL, input.substring(savePos, pos), startLine, startColumn);
    }
    
    private Token scanIntegerLiteral() {
        int startPos = pos; int startLine = line; int startColumn = column;
        if (input.charAt(pos) == '+' || input.charAt(pos) == '-') advance();
        if (pos >= input.length() || !Character.isDigit(input.charAt(pos))) { reset(startPos, startLine, startColumn); return null; }
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) advance();
        return new Token(TokenType.INTEGER_LITERAL, input.substring(startPos, pos), startLine, startColumn);
    }
    
    private Token scanSingleCharToken() {
        int startLine = line; int startColumn = column;
        char ch = input.charAt(pos);
        TokenType type = null;
        switch (ch) {
            case '+': type = TokenType.OP_PLUS; break;
            case '-': type = TokenType.OP_MINUS; break;
            case '*': type = TokenType.OP_MULTIPLY; break;
            case '/': type = TokenType.OP_DIVIDE; break;
            case '%': type = TokenType.OP_MODULO; break;
            case '<': type = TokenType.OP_LESS; break;
            case '>': type = TokenType.OP_GREATER; break;
            case '!': type = TokenType.OP_NOT; break;
            case '=': type = TokenType.OP_ASSIGN; break;
            case '(': type = TokenType.PUNC_LPAREN; break;
            case ')': type = TokenType.PUNC_RPAREN; break;
            case '{': type = TokenType.PUNC_LBRACE; break;
            case '}': type = TokenType.PUNC_RBRACE; break;
            case '[': type = TokenType.PUNC_LBRACKET; break;
            case ']': type = TokenType.PUNC_RBRACKET; break;
            case ',': type = TokenType.PUNC_COMMA; break;
            case ';': type = TokenType.PUNC_SEMICOLON; break;
            case ':': type = TokenType.PUNC_COLON; break;
        }
        if (type != null) { advance(); return new Token(type, String.valueOf(ch), startLine, startColumn); }
        return null;
    }
    
    private Token scanWhitespace() {
        if (!Character.isWhitespace(input.charAt(pos))) return null;
        int startPos = pos; int startLine = line; int startColumn = column;
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) advance();
        return new Token(TokenType.WHITESPACE, input.substring(startPos, pos), startLine, startColumn);
    }
    
    private boolean peek(String str) {
        if (pos + str.length() > input.length()) return false;
        return input.substring(pos, pos + str.length()).equals(str);
    }
    private void advance() {
        if (pos < input.length()) {
            char ch = input.charAt(pos);
            if (ch == '\n') { line++; column = 1; } else { column++; }
            pos++;
        }
    }
    private void advance(int count) { for (int i=0; i<count; i++) advance(); }
    private void reset(int p, int l, int c) { pos = p; line = l; column = c; }
    
    public SymbolTable getSymbolTable() { return symbolTable; }
    public ErrorHandler getErrorHandler() { return errorHandler; }
    public void printTokens() { for (Token t : tokens) System.out.println(t); }
    public void printStatistics() { 
        System.out.println("Total Tokens: " + totalTokens); 
        System.out.println("Lines: " + linesProcessed);
        System.out.println("Comments: " + commentsRemoved);
    }
}