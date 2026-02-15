/**
 * TokenType Enum
 * Defines all token types for the custom programming language lexical analyzer.
 * CS4031 - Compiler Construction - Assignment 01
 */
public enum TokenType {
    // Keywords (case-sensitive)
    KEYWORD_START,
    KEYWORD_FINISH,
    KEYWORD_LOOP,
    KEYWORD_CONDITION,
    KEYWORD_DECLARE,
    KEYWORD_OUTPUT,
    KEYWORD_INPUT,
    KEYWORD_FUNCTION,
    KEYWORD_RETURN,
    KEYWORD_BREAK,
    KEYWORD_CONTINUE,
    KEYWORD_ELSE,
    
    // Identifiers
    IDENTIFIER,
    
    // Literals
    INTEGER_LITERAL,
    FLOAT_LITERAL,
    STRING_LITERAL,
    CHAR_LITERAL,
    BOOLEAN_LITERAL,
    
    // Arithmetic Operators
    OP_PLUS,              // +
    OP_MINUS,             // -
    OP_MULTIPLY,          // *
    OP_DIVIDE,            // /
    OP_MODULO,            // %
    OP_EXPONENT,          // **
    
    // Relational Operators
    OP_EQUAL,             // ==
    OP_NOT_EQUAL,         // !=
    OP_LESS_EQUAL,        // <=
    OP_GREATER_EQUAL,     // >=
    OP_LESS,              // <
    OP_GREATER,           // >
    
    // Logical Operators
    OP_AND,               // &&
    OP_OR,                // ||
    OP_NOT,               // !
    
    // Assignment Operators
    OP_ASSIGN,            // =
    OP_PLUS_ASSIGN,       // +=
    OP_MINUS_ASSIGN,      // -=
    OP_MULTIPLY_ASSIGN,   // *=
    OP_DIVIDE_ASSIGN,     // /=
    
    // Increment/Decrement
    OP_INCREMENT,         // ++
    OP_DECREMENT,         // --
    
    // Punctuators
    PUNC_LPAREN,          // (
    PUNC_RPAREN,          // )
    PUNC_LBRACE,          // {
    PUNC_RBRACE,          // }
    PUNC_LBRACKET,        // [
    PUNC_RBRACKET,        // ]
    PUNC_COMMA,           // ,
    PUNC_SEMICOLON,       // ;
    PUNC_COLON,           // :
    
    // Comments (for tracking purposes)
    COMMENT_SINGLE,
    COMMENT_MULTI,
    
    // Special
    WHITESPACE,
    EOF,
    ERROR
}
