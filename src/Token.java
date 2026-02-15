/**
 * Token Class
 * Represents a single token identified by the lexical analyzer.
 * CS4031 - Compiler Construction - Assignment 01
 */
public class Token {
    private TokenType type;
    private String lexeme;
    private int line;
    private int column;
    
    /**
     * Constructor
     * @param type The type of the token
     * @param lexeme The actual string value of the token
     * @param line Line number where token appears
     * @param column Column number where token starts
     */
    public Token(TokenType type, String lexeme, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }
    
    // Getters
    public TokenType getType() {
        return type;
    }
    
    public String getLexeme() {
        return lexeme;
    }
    
    public int getLine() {
        return line;
    }
    
    public int getColumn() {
        return column;
    }
    
    /**
     * Returns formatted string representation
     * Format: <TOKEN_TYPE, "lexeme", Line: X, Col: Y>
     */
    @Override
    public String toString() {
        return String.format("<%s, \"%s\", Line: %d, Col: %d>", 
                           type, lexeme, line, column);
    }
    
    /**
     * Returns simple string for debugging
     */
    public String toSimpleString() {
        return String.format("%s(\"%s\")", type, lexeme);
    }
}
