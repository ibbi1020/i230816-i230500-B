import java.util.*;

/**
 * ErrorHandler Class
 * Handles lexical errors during scanning.
 * CS4031 - Compiler Construction - Assignment 01
 */
public class ErrorHandler {
    
    /**
     * Inner class to store error information
     */
    private static class LexicalError {
        String errorType;
        int line;
        int column;
        String lexeme;
        String reason;
        
        LexicalError(String errorType, int line, int column, String lexeme, String reason) {
            this.errorType = errorType;
            this.line = line;
            this.column = column;
            this.lexeme = lexeme;
            this.reason = reason;
        }
        
        @Override
        public String toString() {
            return String.format("[ERROR] %s at Line %d, Col %d: '%s' - %s", 
                               errorType, line, column, lexeme, reason);
        }
    }
    
    private List<LexicalError> errors;
    private int errorCount;
    
    /**
     * Constructor
     */
    public ErrorHandler() {
        errors = new ArrayList<>();
        errorCount = 0;
    }
    
    /**
     * Report an invalid character error
     * @param ch Invalid character
     * @param line Line number
     * @param column Column number
     */
    public void reportInvalidCharacter(char ch, int line, int column) {
        String lexeme = String.valueOf(ch);
        String reason = "Invalid character '" + ch + "' (ASCII: " + (int)ch + ")";
        addError("INVALID_CHARACTER", line, column, lexeme, reason);
    }
    
    /**
     * Report a malformed literal error
     * @param lexeme The malformed literal
     * @param line Line number
     * @param column Column number
     * @param reason Specific reason
     */
    public void reportMalformedLiteral(String lexeme, int line, int column, String reason) {
        addError("MALFORMED_LITERAL", line, column, lexeme, reason);
    }
    
    /**
     * Report an invalid identifier error
     * @param lexeme The invalid identifier
     * @param line Line number
     * @param column Column number
     * @param reason Specific reason
     */
    public void reportInvalidIdentifier(String lexeme, int line, int column, String reason) {
        addError("INVALID_IDENTIFIER", line, column, lexeme, reason);
    }
    
    /**
     * Report an unclosed comment error
     * @param line Line number where comment started
     * @param column Column number where comment started
     */
    public void reportUnclosedComment(int line, int column) {
        addError("UNCLOSED_COMMENT", line, column, "#*...", "Multi-line comment not closed");
    }
    
    /**
     * Report an unterminated string error
     * @param line Line number
     * @param column Column number
     */
    public void reportUnterminatedString(int line, int column) {
        addError("UNTERMINATED_STRING", line, column, "\"...", "String literal not terminated");
    }
    
    /**
     * Report an unterminated character literal error
     * @param line Line number
     * @param column Column number
     */
    public void reportUnterminatedCharLiteral(int line, int column) {
        addError("UNTERMINATED_CHAR", line, column, "'...", "Character literal not terminated");
    }
    
    /**
     * Report an invalid escape sequence error
     * @param sequence The invalid escape sequence
     * @param line Line number
     * @param column Column number
     */
    public void reportInvalidEscapeSequence(String sequence, int line, int column) {
        addError("INVALID_ESCAPE", line, column, sequence, "Invalid escape sequence");
    }
    
    /**
     * Report identifier exceeding maximum length
     * @param lexeme The identifier
     * @param line Line number
     * @param column Column number
     */
    public void reportIdentifierTooLong(String lexeme, int line, int column) {
        addError("IDENTIFIER_TOO_LONG", line, column, lexeme, 
                "Identifier exceeds maximum length of 31 characters (length: " + lexeme.length() + ")");
    }
    
    /**
     * Add an error to the error list
     */
    private void addError(String errorType, int line, int column, String lexeme, String reason) {
        errors.add(new LexicalError(errorType, line, column, lexeme, reason));
        errorCount++;
    }
    
    /**
     * Check if any errors were reported
     * @return true if errors exist
     */
    public boolean hasErrors() {
        return errorCount > 0;
    }
    
    /**
     * Get the total number of errors
     * @return Error count
     */
    public int getErrorCount() {
        return errorCount;
    }
    
    /**
     * Print all errors
     */
    public void printErrors() {
        if (errors.isEmpty()) {
            return;
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("LEXICAL ERRORS DETECTED: " + errorCount);
        System.out.println("=".repeat(80));
        
        for (LexicalError error : errors) {
            System.out.println(error);
        }
        
        System.out.println("=".repeat(80));
    }
    
    /**
     * Get all errors as a list
     * @return List of error strings
     */
    public List<String> getErrors() {
        List<String> errorStrings = new ArrayList<>();
        for (LexicalError error : errors) {
            errorStrings.add(error.toString());
        }
        return errorStrings;
    }
    
    /**
     * Clear all errors
     */
    public void clear() {
        errors.clear();
        errorCount = 0;
    }
}
