/* ===============================================================================
 * JFlex Scanner Specification for SimpleLang
 * CS4031 - Compiler Construction - Assignment 01
 * Team: i230816-i230500-B
 * =============================================================================== */

/* ===============================================================================
 * USER CODE SECTION
 * =============================================================================== */
import java.io.*;
import java.util.*;

%%

/* ===============================================================================
 * OPTIONS AND DECLARATIONS
 * =============================================================================== */

%class Yylex
%public
%unicode
%line
%column
%type Token

%{
    // Symbol table for identifiers
    private SymbolTable symbolTable = new SymbolTable();
    
    // Error handler
    private ErrorHandler errorHandler = new ErrorHandler();
    
    // Statistics
    private Map<TokenType, Integer> tokenCounts = new HashMap<>();
    private int totalTokens = 0;
    private int commentsRemoved = 0;
    private List<Token> tokens = new ArrayList<>();
    
    /**
     * Get current line number (1-based)
     */
    private int getLine() {
        return yyline + 1;
    }
    
    /**
     * Get current column number (1-based)
     */
    private int getColumn() {
        return yycolumn + 1;
    }
    
    /**
     * Create a token
     */
    private Token createToken(TokenType type, String lexeme) {
        Token token = new Token(type, lexeme, getLine(), getColumn());
        
        // Track statistics
        if (type != TokenType.WHITESPACE && 
            type != TokenType.COMMENT_SINGLE && 
            type != TokenType.COMMENT_MULTI) {
            tokens.add(token);
            totalTokens++;
            tokenCounts.put(type, tokenCounts.getOrDefault(type, 0) + 1);
            
            // Add identifiers to symbol table
            if (type == TokenType.IDENTIFIER) {
                symbolTable.addIdentifier(lexeme, getLine(), getColumn());
            }
        }
        
        // Track comments
        if (type == TokenType.COMMENT_SINGLE || type == TokenType.COMMENT_MULTI) {
            commentsRemoved++;
        }
        
        return token;
    }
    
    /**
     * Print all tokens
     */
    public void printTokens() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TOKENS (JFLEX)");
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
        System.out.println("SCANNING STATISTICS (JFLEX)");
        System.out.println("=".repeat(80));
        System.out.println("Total Tokens: " + totalTokens);
        System.out.println("Lines Processed: " + (yyline + 1));
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
            System.out.println("Usage: java Yylex <input-file>");
            return;
        }
        
        try {
            // Read input file
            String filename = args[0];
            Reader reader = new FileReader(filename);
            Yylex scanner = new Yylex(reader);
            
            // Scan all tokens
            Token token;
            while ((token = scanner.yylex()) != null) {
                if (token.getType() == TokenType.EOF) {
                    scanner.tokens.add(token);
                    scanner.totalTokens++;
                    scanner.tokenCounts.put(TokenType.EOF, 1);
                    break;
                }
            }
            
            // Print results
            scanner.printTokens();
            scanner.getSymbolTable().printSymbolTable();
            scanner.printStatistics();
            scanner.getErrorHandler().printErrors();
            
            reader.close();
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
%}

/* ===============================================================================
 * MACRO DEFINITIONS
 * =============================================================================== */

/* Whitespace */
WHITESPACE          = [ \t\r\n]+

/* Comments */
SINGLE_LINE_COMMENT = "##"[^\n]*
MULTI_LINE_COMMENT  = "#*"([^*]|"*"+[^*#])*"*"+"#"

/* Letters and Digits */
UPPERCASE           = [A-Z]
LOWERCASE           = [a-z]
DIGIT               = [0-9]
UNDERSCORE          = "_"

/* Keywords */
KEYWORD             = "start"|"finish"|"loop"|"condition"|"declare"|"output"|"input"|"function"|"return"|"break"|"continue"|"else"

/* Boolean Literals */
BOOLEAN             = "true"|"false"

/* Identifiers */
IDENTIFIER          = {UPPERCASE}({LOWERCASE}|{DIGIT}|{UNDERSCORE}){0,30}

/* Integer Literals */
SIGN                = [+-]
INTEGER             = {SIGN}?{DIGIT}+

/* Floating-Point Literals */
FLOAT               = {SIGN}?{DIGIT}+"."{DIGIT}{1,6}([eE]{SIGN}?{DIGIT}+)?

/* String Literals */
STRING_CHAR         = [^\"\\\n]
ESCAPE_SEQ          = \\[\"\\\ntr]
STRING              = \"({STRING_CHAR}|{ESCAPE_SEQ})*\"

/* Character Literals */
CHAR_CHAR           = [^'\\\n]
CHAR_ESCAPE         = \\['\\\ntr]
CHAR                = '({CHAR_CHAR}|{CHAR_ESCAPE})'

/* Multi-character Operators */
OP_EXPONENT         = "**"
OP_EQUAL            = "=="
OP_NOT_EQUAL        = "!="
OP_LESS_EQUAL       = "<="
OP_GREATER_EQUAL    = ">="
OP_AND              = "&&"
OP_OR               = "||"
OP_PLUS_ASSIGN      = "+="
OP_MINUS_ASSIGN     = "-="
OP_MULTIPLY_ASSIGN  = "*="
OP_DIVIDE_ASSIGN    = "/="
OP_INCREMENT        = "++"
OP_DECREMENT        = "--"

%%

/* ===============================================================================
 * LEXICAL RULES
 * =============================================================================== */

/* Priority 1: Multi-line Comments */
{MULTI_LINE_COMMENT}    { return createToken(TokenType.COMMENT_MULTI, yytext()); }

/* Priority 2: Single-line Comments */
{SINGLE_LINE_COMMENT}   { return createToken(TokenType.COMMENT_SINGLE, yytext()); }

/* Priority 3: Multi-character Operators (before single-char to avoid ambiguity) */
{OP_EXPONENT}           { return createToken(TokenType.OP_EXPONENT, yytext()); }
{OP_EQUAL}              { return createToken(TokenType.OP_EQUAL, yytext()); }
{OP_NOT_EQUAL}          { return createToken(TokenType.OP_NOT_EQUAL, yytext()); }
{OP_LESS_EQUAL}         { return createToken(TokenType.OP_LESS_EQUAL, yytext()); }
{OP_GREATER_EQUAL}      { return createToken(TokenType.OP_GREATER_EQUAL, yytext()); }
{OP_AND}                { return createToken(TokenType.OP_AND, yytext()); }
{OP_OR}                 { return createToken(TokenType.OP_OR, yytext()); }
{OP_PLUS_ASSIGN}        { return createToken(TokenType.OP_PLUS_ASSIGN, yytext()); }
{OP_MINUS_ASSIGN}       { return createToken(TokenType.OP_MINUS_ASSIGN, yytext()); }
{OP_MULTIPLY_ASSIGN}    { return createToken(TokenType.OP_MULTIPLY_ASSIGN, yytext()); }
{OP_DIVIDE_ASSIGN}      { return createToken(TokenType.OP_DIVIDE_ASSIGN, yytext()); }
{OP_INCREMENT}          { return createToken(TokenType.OP_INCREMENT, yytext()); }
{OP_DECREMENT}          { return createToken(TokenType.OP_DECREMENT, yytext()); }

/* Priority 4: Keywords (must come before identifiers) */
"start"                 { return createToken(TokenType.KEYWORD_START, yytext()); }
"finish"                { return createToken(TokenType.KEYWORD_FINISH, yytext()); }
"loop"                  { return createToken(TokenType.KEYWORD_LOOP, yytext()); }
"condition"             { return createToken(TokenType.KEYWORD_CONDITION, yytext()); }
"declare"               { return createToken(TokenType.KEYWORD_DECLARE, yytext()); }
"output"                { return createToken(TokenType.KEYWORD_OUTPUT, yytext()); }
"input"                 { return createToken(TokenType.KEYWORD_INPUT, yytext()); }
"function"              { return createToken(TokenType.KEYWORD_FUNCTION, yytext()); }
"return"                { return createToken(TokenType.KEYWORD_RETURN, yytext()); }
"break"                 { return createToken(TokenType.KEYWORD_BREAK, yytext()); }
"continue"              { return createToken(TokenType.KEYWORD_CONTINUE, yytext()); }
"else"                  { return createToken(TokenType.KEYWORD_ELSE, yytext()); }

/* Priority 5: Boolean Literals (must come before identifiers) */
{BOOLEAN}               { return createToken(TokenType.BOOLEAN_LITERAL, yytext()); }

/* Priority 6: Identifiers */
{IDENTIFIER}            { 
                            String lexeme = yytext();
                            if (lexeme.length() > 31) {
                                errorHandler.reportIdentifierTooLong(lexeme, getLine(), getColumn());
                            }
                            return createToken(TokenType.IDENTIFIER, lexeme); 
                        }

/* Priority 7: Floating-Point Literals (must come before integers) */
{FLOAT}                 { return createToken(TokenType.FLOAT_LITERAL, yytext()); }

/* Priority 8: Integer Literals */
{INTEGER}               { return createToken(TokenType.INTEGER_LITERAL, yytext()); }

/* Priority 9: String Literals */
{STRING}                { return createToken(TokenType.STRING_LITERAL, yytext()); }

/* Priority 10: Character Literals */
{CHAR}                  { return createToken(TokenType.CHAR_LITERAL, yytext()); }

/* Priority 11: Single-character Operators and Punctuators */
"+"                     { return createToken(TokenType.OP_PLUS, yytext()); }
"-"                     { return createToken(TokenType.OP_MINUS, yytext()); }
"*"                     { return createToken(TokenType.OP_MULTIPLY, yytext()); }
"/"                     { return createToken(TokenType.OP_DIVIDE, yytext()); }
"%"                     { return createToken(TokenType.OP_MODULO, yytext()); }
"<"                     { return createToken(TokenType.OP_LESS, yytext()); }
">"                     { return createToken(TokenType.OP_GREATER, yytext()); }
"!"                     { return createToken(TokenType.OP_NOT, yytext()); }
"="                     { return createToken(TokenType.OP_ASSIGN, yytext()); }
"("                     { return createToken(TokenType.PUNC_LPAREN, yytext()); }
")"                     { return createToken(TokenType.PUNC_RPAREN, yytext()); }
"{"                     { return createToken(TokenType.PUNC_LBRACE, yytext()); }
"}"                     { return createToken(TokenType.PUNC_RBRACE, yytext()); }
"["                     { return createToken(TokenType.PUNC_LBRACKET, yytext()); }
"]"                     { return createToken(TokenType.PUNC_RBRACKET, yytext()); }
","                     { return createToken(TokenType.PUNC_COMMA, yytext()); }
";"                     { return createToken(TokenType.PUNC_SEMICOLON, yytext()); }
":"                     { return createToken(TokenType.PUNC_COLON, yytext()); }

/* Priority 12: Whitespace */
{WHITESPACE}            { return createToken(TokenType.WHITESPACE, yytext()); }

/* End of file */
<<EOF>>                 { return createToken(TokenType.EOF, ""); }

/* Error: Invalid character */
.                       { 
                            char ch = yytext().charAt(0);
                            errorHandler.reportInvalidCharacter(ch, getLine(), getColumn());
                            return createToken(TokenType.ERROR, yytext()); 
                        }
