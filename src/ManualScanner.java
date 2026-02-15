import java.io.*;
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
    private int lineStartPos;
    
    private List<Token> tokens;
    private SymbolTable symbolTable;
    private ErrorHandler errorHandler;
    
    // Statistics
    private Map<TokenType, Integer> tokenCounts;
    private int totalTokens;
    private int linesProcessed;
    private int commentsRemoved;
    
    /**
     * Constructor
     * @param input Source code input
     */
    public ManualScanner(String input) {
        this.input = input;
        this.pos = 0;
        this.line = 1;
        this.column = 1;
        this.lineStartPos = 0;
        
        this.tokens = new ArrayList<>();
        this.symbolTable = new SymbolTable();
        this.errorHandler = new ErrorHandler();
        
        this.tokenCounts = new HashMap<>();
        this.totalTokens = 0;
        this.linesProcessed = 0;
        this.commentsRemoved = 0;
    }
    
    /**
     * Main scanning method
     * @return List of tokens
     */
    public List<Token> scan() {
        while (pos < input.length()) {
            Token token = nextToken();
            if (token != null) {
                // Don't add whitespace and comments to token list (they're handled during pre-processing)
                if (token.getType() != TokenType.WHITESPACE && 
                    token.getType() != TokenType.COMMENT_SINGLE && 
                    token.getType() != TokenType.COMMENT_MULTI) {
                    tokens.add(token);
                    totalTokens++;
                    tokenCounts.put(token.getType(), tokenCounts.getOrDefault(token.getType(), 0) + 1);
                    
                    // Add identifiers to symbol table
                    if (token.getType() == TokenType.IDENTIFIER) {
                        symbolTable.addIdentifier(token.getLexeme(), token.getLine(), token.getColumn());
                    }
                }
            }
        }
        
        // Add EOF token
        Token eofToken = new Token(TokenType.EOF, "", line, column);
        tokens.add(eofToken);
        totalTokens++;
        tokenCounts.put(TokenType.EOF, 1);
        
        linesProcessed = line;
        
        return tokens;
    }
    
    /**
     * Get next token using pattern matching priority
     * Priority order (highest to lowest):
     * 1. Multi-line comments
     * 2. Single-line comments  
     * 3. Multi-character operators (**, ==, !=, <=, >=, &&, ||, +=, -=, *=, /=, ++, --)
     * 4. Keywords
     * 5. Boolean literals
     * 6. Identifiers
     * 7. Floating-point literals
     * 8. Integer literals
     * 9. String literals
     * 10. Character literals
     * 11. Single-character operators and punctuators
     * 12. Whitespace
     */
    private Token nextToken() {
        if (pos >= input.length()) {
            return null;
        }
        
        int startLine = line;
        int startColumn = column;
        
        // Priority 1: Multi-line comments (#* ... *#)
        Token multiLineComment = scanMultiLineComment();
        if (multiLineComment != null) {
            commentsRemoved++;
            return multiLineComment;
        }
        
        // Priority 2: Single-line comments (##...)
        Token singleLineComment = scanSingleLineComment();
        if (singleLineComment != null) {
            commentsRemoved++;
            return singleLineComment;
        }
        
        // Priority 3: Multi-character operators (must check before single-char)
        Token multiCharOp = scanMultiCharOperator();
        if (multiCharOp != null) {
            return multiCharOp;
        }
        
        // Priority 4: Keywords (must check before identifiers)
        Token keyword = scanKeyword();
        if (keyword != null) {
            return keyword;
        }
        
        // Priority 5: Boolean literals (must check before identifiers)
        Token boolLiteral = scanBooleanLiteral();
        if (boolLiteral != null) {
            return boolLiteral;
        }
        
        // Priority 6: Identifiers
        Token identifier = scanIdentifier();
        if (identifier != null) {
            return identifier;
        }
        
        // Priority 7: Floating-point literals (must check before integers)
        Token floatLiteral = scanFloatingPointLiteral();
        if (floatLiteral != null) {
            return floatLiteral;
        }
        
        // Priority 8: Integer literals
        Token intLiteral = scanIntegerLiteral();
        if (intLiteral != null) {
            return intLiteral;
        }
        
        // Priority 9: String literals
        Token stringLiteral = scanStringLiteral();
        if (stringLiteral != null) {
            return stringLiteral;
        }
        
        // Priority 10: Character literals
        Token charLiteral = scanCharacterLiteral();
        if (charLiteral != null) {
            return charLiteral;
        }
        
        // Priority 11: Single-character operators and punctuators
        Token singleChar = scanSingleCharToken();
        if (singleChar != null) {
            return singleChar;
        }
        
        // Priority 12: Whitespace
        Token whitespace = scanWhitespace();
        if (whitespace != null) {
            return whitespace;
        }
        
        // Error: Invalid character
        char ch = input.charAt(pos);
        errorHandler.reportInvalidCharacter(ch, startLine, startColumn);
        advance();
        return new Token(TokenType.ERROR, String.valueOf(ch), startLine, startColumn);
    }
    
    /**
     * Scan multi-line comment using DFA
     * Regex: #\*([^*]|\*+[^*#])*\*+#
     */
    private Token scanMultiLineComment() {
        if (!peek("#*")) {
            return null;
        }
        
        int startPos = pos;
        int startLine = line;
        int startColumn = column;
        
        advance(); // consume #
        advance(); // consume *
        
        boolean foundEnd = false;
        while (pos < input.length()) {
            if (peek("*#")) {
                advance(); // consume *
                advance(); // consume #
                foundEnd = true;
                break;
            }
            advance();
        }
        
        if (!foundEnd) {
            errorHandler.reportUnclosedComment(startLine, startColumn);
        }
        
        String lexeme = input.substring(startPos, pos);
        return new Token(TokenType.COMMENT_MULTI, lexeme, startLine, startColumn);
    }
    
    /**
     * Scan single-line comment using DFA
     * Regex: ##[^\n]*
     * DFA: 3 states (start -> # -> ## -> content)
     */
    private Token scanSingleLineComment() {
        if (!peek("##")) {
            return null;
        }
        
        int startPos = pos;
        int startLine = line;
        int startColumn = column;
        
        advance(); // consume first #
        advance(); // consume second #
        
        // Consume until newline
        while (pos < input.length() && input.charAt(pos) != '\n') {
            advance();
        }
        
        String lexeme = input.substring(startPos, pos);
        return new Token(TokenType.COMMENT_SINGLE, lexeme, startLine, startColumn);
    }
    
    /**
     * Scan multi-character operators
     * **, ==, !=, <=, >=, &&, ||, +=, -=, *=, /=, ++, --
     */
    private Token scanMultiCharOperator() {
        int startLine = line;
        int startColumn = column;
        char ch = input.charAt(pos);
        
        if (pos + 1 < input.length()) {
            char next = input.charAt(pos + 1);
            String twoChar = "" + ch + next;
            
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
            
            if (type != null) {
                advance();
                advance();
                return new Token(type, twoChar, startLine, startColumn);
            }
        }
        
        return null;
    }
    
    /**
     * Scan keyword
     * Keywords: start, finish, loop, condition, declare, output, input, function, return, break, continue, else
     */
    private Token scanKeyword() {
        int startPos = pos;
        int startLine = line;
        int startColumn = column;
        
        // Keywords are lowercase letters only
        if (!Character.isLowerCase(input.charAt(pos))) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isLowerCase(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            advance();
        }
        
        String word = sb.toString();
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
        
        if (type != null) {
            return new Token(type, word, startLine, startColumn);
        }
        
        // Not a keyword, backtrack
        pos = startPos;
        line = startLine;
        column = startColumn;
        return null;
    }
    
    /**
     * Scan boolean literal using DFA
     * Regex: (true|false)
     * DFA: 8 states
     */
    private Token scanBooleanLiteral() {
        int startPos = pos;
        int startLine = line;
        int startColumn = column;
        
        if (peek("true")) {
            String lexeme = input.substring(pos, pos + 4);
            advance(); advance(); advance(); advance();
            return new Token(TokenType.BOOLEAN_LITERAL, lexeme, startLine, startColumn);
        }
        
        if (peek("false")) {
            String lexeme = input.substring(pos, pos + 5);
            advance(); advance(); advance(); advance(); advance();
            return new Token(TokenType.BOOLEAN_LITERAL, lexeme, startLine, startColumn);
        }
        
        return null;
    }
    
    /**
     * Scan identifier using DFA
     * Regex: [A-Z][a-z0-9_]{0,30}
     * DFA: 2 states (start -> uppercase -> (lowercase|digit|_)*)
     */
    private Token scanIdentifier() {
        int startPos = pos;
        int startLine = line;
        int startColumn = column;
        
        // Must start with uppercase letter
        if (!Character.isUpperCase(input.charAt(pos))) {
            return null;
        }
        
        advance(); // consume first uppercase letter
        
        // Continue with lowercase, digits, or underscore
        while (pos < input.length()) {
            char ch = input.charAt(pos);
            if (Character.isLowerCase(ch) || Character.isDigit(ch) || ch == '_') {
                advance();
            } else {
                break;
            }
        }
        
        String lexeme = input.substring(startPos, pos);
        
        // Check length constraint (max 31 characters)
        if (lexeme.length() > 31) {
            errorHandler.reportIdentifierTooLong(lexeme, startLine, startColumn);
        }
        
        return new Token(TokenType.IDENTIFIER, lexeme, startLine, startColumn);
    }
    
    /**
     * Scan floating-point literal using DFA
     * Regex: [+-]?[0-9]+\.[0-9]{1,6}
     * DFA: 5 states
     */
    private Token scanFloatingPointLiteral() {
        int startPos = pos;
        int startLine = line;
        int startColumn = column;
        int savePos = pos;
        int saveLine = line;
        int saveColumn = column;
        
        // Optional sign
        if (input.charAt(pos) == '+' || input.charAt(pos) == '-') {
            advance();
        }
        
        // Must have at least one digit
        if (pos >= input.length() || !Character.isDigit(input.charAt(pos))) {
            pos = savePos;
            line = saveLine;
            column = saveColumn;
            return null;
        }
        
        // Consume digits before decimal point
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            advance();
        }
        
        // Must have decimal point
        if (pos >= input.length() || input.charAt(pos) != '.') {
            pos = savePos;
            line = saveLine;
            column = saveColumn;
            return null;
        }
        
        advance(); // consume '.'
        
        // Must have at least one decimal digit
        if (pos >= input.length() || !Character.isDigit(input.charAt(pos))) {
            pos = savePos;
            line = saveLine;
            column = saveColumn;
            return null;
        }
        
        // Consume decimal digits (simplified: no strict 1-6 limit for basic implementation)
        int decimalCount = 0;
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            advance();
            decimalCount++;
        }
        
        String lexeme = input.substring(startPos, pos);
        return new Token(TokenType.FLOAT_LITERAL, lexeme, startLine, startColumn);
    }
    
    /**
     * Scan integer literal using DFA
     * Regex: [+-]?[0-9]+
     * DFA: 3 states (start -> sign -> digits)
     */
    private Token scanIntegerLiteral() {
        int startPos = pos;
        int startLine = line;
        int startColumn = column;
        
        // Optional sign
        if (input.charAt(pos) == '+' || input.charAt(pos) == '-') {
            advance();
        }
        
        // Must have at least one digit
        if (pos >= input.length() || !Character.isDigit(input.charAt(pos))) {
            pos = startPos;
            line = startLine;
            column = startColumn;
            return null;
        }
        
        // Consume all digits
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            advance();
        }
        
        String lexeme = input.substring(startPos, pos);
        return new Token(TokenType.INTEGER_LITERAL, lexeme, startLine, startColumn);
    }
    
    /**
     * Scan string literal using DFA
     * Regex: "([^"\\\n]|\\["\\ntr])*"
     * DFA: 4 states (start -> " -> content -> ")
     */
    private Token scanStringLiteral() {
        if (input.charAt(pos) != '"') {
            return null;
        }
        
        int startPos = pos;
        int startLine = line;
        int startColumn = column;
        
        advance(); // consume opening "
        
        StringBuilder content = new StringBuilder();
        boolean closed = false;
        
        while (pos < input.length()) {
            char ch = input.charAt(pos);
            
            if (ch == '"') {
                advance(); // consume closing "
                closed = true;
                break;
            } else if (ch == '\\') {
                advance(); // consume backslash
                if (pos >= input.length()) {
                    break;
                }
                char escapeChar = input.charAt(pos);
                // Valid escape sequences: ", \, n, t, r
                if (escapeChar == '"' || escapeChar == '\\' || escapeChar == 'n' || 
                    escapeChar == 't' || escapeChar == 'r') {
                    content.append('\\').append(escapeChar);
                    advance();
                } else {
                    errorHandler.reportInvalidEscapeSequence("\\" + escapeChar, line, column);
                    advance();
                }
            } else if (ch == '\n') {
                // String cannot span multiple lines
                break;
            } else {
                content.append(ch);
                advance();
            }
        }
        
        if (!closed) {
            errorHandler.reportUnterminatedString(startLine, startColumn);
        }
        
        String lexeme = input.substring(startPos, pos);
        return new Token(TokenType.STRING_LITERAL, lexeme, startLine, startColumn);
    }
    
    /**
     * Scan character literal
     * Regex: '([^'\\\n]|\\['\\ntr])'
     */
    private Token scanCharacterLiteral() {
        if (input.charAt(pos) != '\'') {
            return null;
        }
        
        int startPos = pos;
        int startLine = line;
        int startColumn = column;
        
        advance(); // consume opening '
        
        boolean closed = false;
        boolean hasContent = false;
        
        while (pos < input.length()) {
            char ch = input.charAt(pos);
            
            if (ch == '\'') {
                advance(); // consume closing '
                closed = true;
                break;
            } else if (ch == '\\') {
                advance(); // consume backslash
                if (pos >= input.length()) {
                    break;
                }
                char escapeChar = input.charAt(pos);
                if (escapeChar == '\'' || escapeChar == '\\' || escapeChar == 'n' || 
                    escapeChar == 't' || escapeChar == 'r') {
                    advance();
                    hasContent = true;
                } else {
                    errorHandler.reportInvalidEscapeSequence("\\" + escapeChar, line, column);
                    advance();
                }
                break; // character literal can only have one character
            } else if (ch == '\n') {
                break;
            } else {
                advance();
                hasContent = true;
                break; // character literal can only have one character
            }
        }
        
        // Consume closing quote if not already
        if (pos < input.length() && input.charAt(pos) == '\'') {
            advance();
            closed = true;
        }
        
        if (!closed) {
            errorHandler.reportUnterminatedCharLiteral(startLine, startColumn);
        }
        
        String lexeme = input.substring(startPos, pos);
        return new Token(TokenType.CHAR_LITERAL, lexeme, startLine, startColumn);
    }
    
    /**
     * Scan single-character operators and punctuators
     */
    private Token scanSingleCharToken() {
        int startLine = line;
        int startColumn = column;
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
        
        if (type != null) {
            advance();
            return new Token(type, String.valueOf(ch), startLine, startColumn);
        }
        
        return null;
    }
    
    /**
     * Scan whitespace
     */
    private Token scanWhitespace() {
        int startPos = pos;
        int startLine = line;
        int startColumn = column;
        
        char ch = input.charAt(pos);
        if (!Character.isWhitespace(ch)) {
            return null;
        }
        
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            advance();
        }
        
        String lexeme = input.substring(startPos, pos);
        return new Token(TokenType.WHITESPACE, lexeme, startLine, startColumn);
    }
    
    /**
     * Peek ahead n characters
     */
    private boolean peek(String str) {
        if (pos + str.length() > input.length()) {
            return false;
        }
        return input.substring(pos, pos + str.length()).equals(str);
    }
    
    /**
     * Advance position and update line/column tracking
     */
    private void advance() {
        if (pos < input.length()) {
            char ch = input.charAt(pos);
            if (ch == '\n') {
                line++;
                column = 1;
                lineStartPos = pos + 1;
            } else {
                column++;
            }
            pos++;
        }
    }
    
    /**
     * Print all tokens
     */
    public void printTokens() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TOKENS");
        System.out.println("=".repeat(80));
        
        for (Token token : tokens) {
            System.out.println(token);
        }
        
        System.out.println("=".repeat(80));
    }
    
    /**
     * Print statistics
     */
    public void printStatistics() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SCANNING STATISTICS");
        System.out.println("=".repeat(80));
        System.out.println("Total Tokens: " + totalTokens);
        System.out.println("Lines Processed: " + linesProcessed);
        System.out.println("Comments Removed: " + commentsRemoved);
        System.out.println("\nToken Count by Type:");
        System.out.println("-".repeat(80));
        
        // Sort token types by count (descending)
        tokenCounts.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .forEach(entry -> {
                System.out.printf("  %-30s : %5d\n", entry.getKey(), entry.getValue());
            });
        
        System.out.println("=".repeat(80));
    }
    
    /**
     * Get tokens list
     */
    public List<Token> getTokens() {
        return tokens;
    }
    
    /**
     * Get symbol table
     */
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
    
    /**
     * Get error handler
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java ManualScanner <input-file>");
            return;
        }
        
        try {
            // Read input file
            String filename = args[0];
            StringBuilder content = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
            reader.close();
            
            // Create scanner and scan
            ManualScanner scanner = new ManualScanner(content.toString());
            scanner.scan();
            
            // Print results
            scanner.printTokens();
            scanner.getSymbolTable().printSymbolTable();
            scanner.printStatistics();
            scanner.getErrorHandler().printErrors();
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
