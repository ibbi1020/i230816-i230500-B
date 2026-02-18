import java.io.*;

%%

%class Yylex
%unicode
%line
%column
%type Token
%public

%{
    private Token symbol(TokenType type) {
        return new Token(type, yytext().toString(), yyline + 1, yycolumn + 1);
    }
    private Token symbol(TokenType type, String value) {
        return new Token(type, value, yyline + 1, yycolumn + 1);
    }
    private void error(String errorType, String message) {
        System.err.println("[ERROR] " + errorType + " at " + (yyline+1) + ":" + (yycolumn+1) + " - " + message);
    }
%}

%eofval{
  return null;
%eofval}

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
IntegerLiteral = [+-]?[0-9]+
FloatLiteral   = [+-]?[0-9]+\.[0-9]{1,6}([eE][+-]?[0-9]+)?
Identifier     = [A-Z][a-z0-9_]*

/* Valid Literals (Strict) */
StringLiteral  = \"([^\"\\\n\r]|\\[\"\\ntr])*\"
CharLiteral    = '([^'\\\n\r]|\\[\'\\ntr])'

/* Error Patterns (Lenient Catch-All) */
/* Matches a quote followed by anything NOT a newline. This catches unterminated strings AND strings with bad escapes */
UntermString   = \"[^\n\r]*
UntermChar     = '[^\n\r]*

CommentSingle  = ##[^\r\n]*
CommentMulti   = #\*([^*]|\*+[^#*])*\*+#

%%

/* 1. Comments & Whitespace */
{CommentSingle} { /* ignore */ }
{CommentMulti}  { /* ignore */ }
{WhiteSpace}    { /* ignore */ }

/* 2. Keywords */
"start"         { return symbol(TokenType.KEYWORD_START); }
"finish"        { return symbol(TokenType.KEYWORD_FINISH); }
"loop"          { return symbol(TokenType.KEYWORD_LOOP); }
"condition"     { return symbol(TokenType.KEYWORD_CONDITION); }
"declare"       { return symbol(TokenType.KEYWORD_DECLARE); }
"output"        { return symbol(TokenType.KEYWORD_OUTPUT); }
"input"         { return symbol(TokenType.KEYWORD_INPUT); }
"function"      { return symbol(TokenType.KEYWORD_FUNCTION); }
"return"        { return symbol(TokenType.KEYWORD_RETURN); }
"break"         { return symbol(TokenType.KEYWORD_BREAK); }
"continue"      { return symbol(TokenType.KEYWORD_CONTINUE); }
"else"          { return symbol(TokenType.KEYWORD_ELSE); }

/* 3. Boolean Literals */
"true"          { return symbol(TokenType.BOOLEAN_LITERAL); }
"false"         { return symbol(TokenType.BOOLEAN_LITERAL); }

/* 4. Operators */
"**"            { return symbol(TokenType.OP_EXPONENT); }
"=="            { return symbol(TokenType.OP_EQUAL); }
"!="            { return symbol(TokenType.OP_NOT_EQUAL); }
"<="            { return symbol(TokenType.OP_LESS_EQUAL); }
">="            { return symbol(TokenType.OP_GREATER_EQUAL); }
"&&"            { return symbol(TokenType.OP_AND); }
"||"            { return symbol(TokenType.OP_OR); }
"+="            { return symbol(TokenType.OP_PLUS_ASSIGN); }
"-="            { return symbol(TokenType.OP_MINUS_ASSIGN); }
"*="            { return symbol(TokenType.OP_MULTIPLY_ASSIGN); }
"/="            { return symbol(TokenType.OP_DIVIDE_ASSIGN); }
"++"            { return symbol(TokenType.OP_INCREMENT); }
"--"            { return symbol(TokenType.OP_DECREMENT); }
"+"             { return symbol(TokenType.OP_PLUS); }
"-"             { return symbol(TokenType.OP_MINUS); }
"*"             { return symbol(TokenType.OP_MULTIPLY); }
"/"             { return symbol(TokenType.OP_DIVIDE); }
"%"             { return symbol(TokenType.OP_MODULO); }
"<"             { return symbol(TokenType.OP_LESS); }
">"             { return symbol(TokenType.OP_GREATER); }
"!"             { return symbol(TokenType.OP_NOT); }
"="             { return symbol(TokenType.OP_ASSIGN); }

/* 5. Punctuators */
"("             { return symbol(TokenType.PUNC_LPAREN); }
")"             { return symbol(TokenType.PUNC_RPAREN); }
"{"             { return symbol(TokenType.PUNC_LBRACE); }
"}"             { return symbol(TokenType.PUNC_RBRACE); }
"["             { return symbol(TokenType.PUNC_LBRACKET); }
"]"             { return symbol(TokenType.PUNC_RBRACKET); }
","             { return symbol(TokenType.PUNC_COMMA); }
";"             { return symbol(TokenType.PUNC_SEMICOLON); }
":"             { return symbol(TokenType.PUNC_COLON); }

/* 6. Literals */
{FloatLiteral}    { return symbol(TokenType.FLOAT_LITERAL); }
{IntegerLiteral}  { return symbol(TokenType.INTEGER_LITERAL); }

/* Note: StringLiteral must come BEFORE UntermString to take precedence */
{StringLiteral}   { return symbol(TokenType.STRING_LITERAL); }
{CharLiteral}     { return symbol(TokenType.CHAR_LITERAL); }

/* 7. Identifiers */
{Identifier}      { 
    if (yylength() > 31) {
        error("IDENTIFIER_TOO_LONG", "Identifier too long");
        return symbol(TokenType.ERROR, yytext().toString());
    }
    return symbol(TokenType.IDENTIFIER); 
}

/* 8. Errors */
{UntermString} { error("UNTERMINATED_STRING", "String error"); return symbol(TokenType.ERROR, yytext().toString()); }
{UntermChar}   { error("UNTERMINATED_CHAR", "Char error"); return symbol(TokenType.ERROR, yytext().toString()); }
"#*"([^*]|\*+[^#*])* { error("UNCLOSED_COMMENT", "Multi-line comment error"); return symbol(TokenType.ERROR, yytext().toString()); }
[^] { error("INVALID_CHARACTER", "Invalid char: " + yytext().toString()); return symbol(TokenType.ERROR, yytext().toString()); }