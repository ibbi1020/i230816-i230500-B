# CS4031 Compiler Construction - Assignment 01  
## Lexical Analyzer (Scanner) Implementation

**Team Members:**  
- Student 1 - i230816  
- Student 2 - i230500  
- Section: B

---

## Language Overview

**Language Name:** SimpleLang  
**File Extension:** `.lang`

SimpleLang is a custom programming language designed for demonstrating lexical analysis concepts. It features keyword-based control structures, strongly-typed identifiers, and multiple literal types.

---

## 1. Keywords

SimpleLang has 12 reserved keywords:

| Keyword | Meaning | Usage Example |
|---------|---------|---------------|
| `start` | Program entry point | `start ... finish` |
| `finish` | Program termination | Marks end of program block |
| `loop` | Loop construct | `loop condition { ... }` |
| `condition` | Conditional statement | `condition X > 0 { ... }` |
| `declare` | Variable declaration | `declare X = 100` |
| `output` | Print output | `output "Hello"` |
| `input` | Read input | `input X` |
| `function` | Function definition | `function Add(A, B) { ... }` |
| `return` | Return from function | `return Result` |
| `break` | Exit loop | Exits innermost loop |
| `continue` | Skip to next iteration | Continues to next loop iteration |
| `else` | Alternative branch | `condition ... else { ... }` |

**Note:** All keywords are case-sensitive and must be lowercase.

---

## 2. Identifiers

**Rules:**
- **MUST** start with an **uppercase letter** (A-Z)
- Followed by **lowercase letters** (a-z), **digits** (0-9), or **underscores** (_)
- Maximum length: **31 characters**
- Case-sensitive

**Regular Expression:** `[A-Z][a-z0-9_]{0,30}`

**Valid Examples:**
```
X
MyVariable
Counter123
First_name
Temp_value_2
Student_id
MaxValue
```

**Invalid Examples:**
```
lowercase       # Starts with lowercase
_Variable       # Starts with underscore
123Number       # Starts with digit
very_long_identifier_name_that_exceeds_maximum_allowed_length  # > 31 chars
```

---

## 3. Literals

### 3.1 Integer Literals
- Optional sign (`+` or `-`)
- One or more digits

**Regex:** `[+-]?[0-9]+`

**Examples:**
```
123
-456
+789
0
-1
```

### 3.2 Floating-Point Literals
- Optional sign
- Integer part + decimal point + 1-6 decimal digits
- Optional exponent (e/E with optional sign)

**Regex:** `[+-]?[0-9]+\.[0-9]{1,6}([eE][+-]?[0-9]+)?`

**Examples:**
```
3.14159
-2.5
+0.001
1.5e10
3.14E-5
```

### 3.3 String Literals
- Enclosed in double quotes (`"..."`)
- Supports escape sequences: `\"`, `\\`, `\n`, `\t`, `\r`
- Cannot span multiple lines

**Regex:** `"([^"\\\n]|\\["\\ntr])*"`

**Examples:**
```
"Hello, World!"
"She said \"Hi\""
"Path: C:\\Users\\Documents"
"Line1\nLine2"
"Column1\tColumn2"
```

### 3.4 Character Literals
- Enclosed in single quotes (`'...'`)
- Single character or escape sequence
- Supports same escapes as strings

**Regex:** `'([^'\\\n]|\\['\\ntr])'`

**Examples:**
```
'A'
'5'
'\n'
'\''
'\\'
```

### 3.5 Boolean Literals
- Only two values: `true` or `false`
- Case-sensitive (must be lowercase)

**Examples:**
```
true
false
```

---

## 4. Operators

### 4.1 Arithmetic Operators

| Operator | Description | Precedence | Example |
|----------|-------------|------------|---------|
| `**` | Exponentiation | 1 (highest) | `2 ** 8` = 256 |
| `*` | Multiplication | 2 | `5 * 3` = 15 |
| `/` | Division | 2 | `10 / 2` = 5 |
| `%` | Modulo | 2 | `10 % 3` = 1 |
| `+` | Addition | 3 | `5 + 3` = 8 |
| `-` | Subtraction | 3 | `5 - 3` = 2 |

### 4.2 Relational Operators

| Operator | Description | Example |
|----------|-------------|---------|
| `==` | Equal to | `X == Y` |
| `!=` | Not equal to | `X != Y` |
| `<` | Less than | `X < 100` |
| `>` | Greater than | `X > 0` |
| `<=` | Less than or equal | `X <= 100` |
| `>=` | Greater than or equal | `X >= 0` |

### 4.3 Logical Operators

| Operator | Description | Precedence | Example |
|----------|-------------|------------|---------|
| `!` | Logical NOT | 1 | `!Flag` |
| `&&` | Logical AND | 2 | `X > 0 && Y < 100` |
| `||` | Logical OR | 3 | `A == 0 || B == 0` |

### 4.4 Assignment Operators

| Operator | Description | Example | Equivalent |
|----------|-------------|---------|------------|
| `=` | Simple assignment | `X = 100` | - |
| `+=` | Add and assign | `X += 10` | `X = X + 10` |
| `-=` | Subtract and assign | `X -= 5` | `X = X - 5` |
| `*=` | Multiply and assign | `X *= 2` | `X = X * 2` |
| `/=` | Divide and assign | `X /= 3` | `X = X / 3` |

### 4.5 Increment/Decrement Operators

| Operator | Description | Example |
|----------|-------------|---------|
| `++` | Increment by 1 | `X++` or `++X` |
| `--` | Decrement by 1 | `X--` or `--X` |

---

## 5. Punctuators

| Symbol | Name | Usage |
|--------|------|-------|
| `(` `)` | Parentheses | Function calls, grouping expressions |
| `{` `}` | Braces | Code blocks |
| `[` `]` | Brackets | Array indexing |
| `,` | Comma | Separator in lists |
| `;` | Semicolon | Statement terminator (optional) |
| `:` | Colon | Type annotations |

---

## 6. Comments

### 6.1 Single-Line Comments
- Start with `##`
- Continue until end of line

**Syntax:** `##[^\n]*`

**Example:**
```
## This is a single-line comment
declare X = 100  ## Inline comment
```

### 6.2 Multi-Line Comments
- Start with `#*`
- End with `*#`
- Can span multiple lines
- Cannot be nested

**Syntax:** `#*...*#`

**Example:**
```
#* This is a
   multi-line comment
   spanning several lines *#
```

---

## 7. Pattern Matching Priority

The scanner follows this priority order (highest to lowest):

1. **Multi-line comments** (`#*...*#`)
2. **Single-line comments** (`##...`)
3. **Multi-character operators** (`**`, `==`, `!=`, `<=`, `>=`, `&&`, `||`, `+=`, `-=`, `*=`, `/=`, `++`, `--`)
4. **Keywords** (`start`, `finish`, etc.)
5. **Boolean literals** (`true`, `false`)
6. **Identifiers** (`[A-Z][a-z0-9_]*`)
7. **Floating-point literals** (before integers to handle decimals)
8. **Integer literals**
9. **String literals**
10. **Character literals**
11. **Single-character operators and punctuators**
12. **Whitespace**

This ordering ensures correct token recognition (e.g., `==` is recognized as one token, not two `=` tokens).

---

## 8. Sample Programs

### Sample 1: Basic Arithmetic
```
start
    declare X = 10
    declare Y = 20
    declare Sum = X + Y
    declare Product = X * Y
    
    output "Sum: "
    output Sum
    output "Product: "
    output Product
finish
```

### Sample 2: Conditional Logic
```
start
    declare Age = 25
    
    condition Age >= 18
        output "Adult"
    else
        output "Minor"
    
    declare Score = 85
    condition Score >= 90
        output "Grade A"
    else
        condition Score >= 80
            output "Grade B"
        else
            output "Grade C"
finish
```

### Sample 3: Loop and Function
```
start
    ## Factorial function
    function Factorial(N)
        condition N <= 1
            return 1
        else
            declare Temp = Factorial(N - 1)
            return N * Temp
    
    ## Calculate factorial of 5
    declare Num = 5
    declare Result = Factorial(Num)
    
    output "Factorial of "
    output Num
    output " is "
    output Result
    
    ## Loop example
    declare I = 1
    loop I <= 10
        output I
        I++
finish
```

---

## 9. Compilation and Execution

### Prerequisites
- **Java JDK 8 or higher**
- **JFlex** (for Part 2 - JFlex implementation)

### Manual Scanner (Part 1)

**Compile:**
```bash
cd Ass-1/src
javac *.java
```

**Run:**
```bash
java ManualScanner <input-file.lang>
```

**Example:**
```bash
java ManualScanner ../tests/test1.lang
```

**Output:**
The scanner produces:
1. **Token List:** All recognized tokens with format `<TYPE, "lexeme", Line: X, Col: Y>`
2. **Symbol Table:** All identifiers with first occurrence and frequency
3. **Statistics:** Total tokens, lines processed, comments removed, token counts by type
4. **Errors:** Any lexical errors detected with location and description

### JFlex Scanner (Part 2)

**Generate Scanner:**
```bash
cd Ass-1/src
jflex Scanner.flex
```

**Compile:**
```bash
javac *.java
```

**Run:**
```bash
java -cp . Yylex <input-file.lang>
```

---

## 10. Project Structure

```
i230816-i230500-B/
├── src/
│   ├── ManualScanner.java      # Manual DFA-based scanner
│   ├── Token.java               # Token representation
│   ├── TokenType.java           # Token type enumeration
│   ├── SymbolTable.java         # Identifier symbol table
│   ├── ErrorHandler.java        # Error detection & reporting
│   ├── Scanner.flex             # JFlex specification
│   └── Yylex.java               # JFlex-generated scanner
├── tests/
│   ├── test1.lang               # All valid tokens
│   ├── test2.lang               # Complex expressions
│   ├── test3.lang               # Strings/chars with escapes
│   ├── test4.lang               # Lexical errors
│   ├── test5.lang               # Comment edge cases
│   └── TestResults.txt          # Test execution results
├── docs/
│   ├── Automata_Design.pdf      # NFA/DFA diagrams & tables
│   ├── Comparison.pdf           # Manual vs JFlex comparison
│   └── LanguageGrammar.txt      # Complete grammar specification
└── README.md                    # This file
```

---

## 11. Implementation Details

### Token Format
All tokens follow this format:
```
<TOKEN_TYPE, "lexeme", Line: X, Col: Y>
```

**Example:**
```
<KEYWORD_DECLARE, "declare", Line: 5, Col: 1>
<IDENTIFIER, "Counter", Line: 5, Col: 9>
<OP_ASSIGN, "=", Line: 5, Col: 17>
<INTEGER_LITERAL, "100", Line: 5, Col: 19>
```

### Symbol Table
Tracks all identifiers with:
- **Name:** Identifier string
- **Type:** TOKEN_TYPE (always IDENTIFIER)
- **First Line:** Line of first occurrence
- **First Column:** Column of first occurrence
- **Frequency:** Total number of occurrences

### Error Handling
The scanner detects and reports:
- **Invalid characters** (@ $ ~ etc.)
- **Unterminated strings/characters**
- **Invalid escape sequences**
- **Unclosed comments**
- **Identifiers too long** (> 31 chars)
- **Malformed literals**

Errors are reported with location and reason, but scanning continues (error recovery).

---

## 12. Testing

Five comprehensive test files are provided:

1. **test1.lang:** All valid token types (keywords, operators, literals, etc.)
2. **test2.lang:** Complex nested expressions and control flow
3. **test3.lang:** String and character literals with all escape sequences
4. **test4.lang:** Various lexical errors to test error handling
5. **test5.lang:** Comment edge cases (nested-looking, special chars, etc.)

Run each test file through both scanners and compare results.

---

## 13. Features

### Manual Scanner (Part 1)
✓ DFA-based token recognition using minimized transition tables  
✓ Longest match principle  
✓ Correct pattern matching priority  
✓ Line and column tracking  
✓ Whitespace handling (preserved in strings)  
✓ Symbol table with frequency tracking  
✓ Comprehensive error detection and recovery  
✓ Detailed statistics output  

### JFlex Scanner (Part 2)
✓ Declarative pattern matching  
✓ Same token recognition as manual scanner  
✓ Equivalent priority ordering  
✓ Compatible token format  
✓ Performance comparison with manual implementation  

---

## 14. References

- CS4031 Compiler Construction Course Materials
- JFlex Manual: https://jflex.de/manual.html
- Automata Theory: NFA → DFA conversion and minimization
- Python `automata-lib` library for DFA verification

---

## Contact

For questions or issues, contact:
- i230816@nu.edu.pk
- i230500@nu.edu.pk

---

**Last Updated:** January 2025  
**Course:** CS4031 - Compiler Construction  
**Institution:** FAST NUCES
