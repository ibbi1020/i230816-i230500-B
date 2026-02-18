# SimpleLang: Lexical Analysis System Documentation

**Course:** CS4031 – Compiler Construction  
**Assignment:** 01 – Lexical Analyzer Implementation  
**Academic Institution:** FAST NUCES - Spring 2026

## Team Identification
- **Student 1:** Ibraheem Farooq (i230816)
- **Student 2:** Rehan Akram (i230500)
- **Section:** B

## 1. Introduction and Scope
**SimpleLang** is a domain-specific programming language engineered to demonstrate the fundamental principles of lexical analysis within the compiler pipeline. This system serves as the initial phase of the compilation process, tasked with transforming raw source code into a stream of discrete tokens. The implementation focuses on robust pattern matching, adherence to the **Longest Match Principle**, and comprehensive error recovery mechanisms.

**File Specification:** Source files utilize the `.lang` extension.

## 2. Keywords and Semantic Definitions
The SimpleLang lexicon incorporates 12 reserved keywords, which are case-sensitive and must be represented in lowercase. These keywords constitute the core control structures and operational semantics of the language.

| Keyword | Functional Description |
| : | : |
| `start` | Orchestrates the primary execution entry point. |
| `finish` | Signals the structural termination of a program block. |
| `loop` | Facilitates iterative control flow based on predicates. |
| `condition` | Implements decision-making branching logic. |
| `declare` | Allocates memory and associates identifiers with initial values. |
| `output` | Interfaces with the standard output stream for data visualization. |
| `input` | Facilitates interactive data retrieval from the standard input stream. |
| `function` | Defines modular sub-routines and functional abstractions. |
| `return` | Transfers control back to the invoking context with an optional payload. |
| `break` | Abruptly terminates the execution of the innermost loop structure. |
| `continue` | Skips remaining statements in a loop iteration, resuming at the header. |
| `else` | Provides a mutually exclusive branch for conditional logic. |

## 3. Lexical Specifications

### 3.1 Identifiers
Identifiers provide unique nomenclature for variables and functions. SimpleLang enforces a rigid naming convention to mitigate lexical ambiguity.
- **Formation Constraints:**
  - Must commence with a literal **uppercase character** (`A-Z`).
  - Subsequent sequences may include **lowercase characters** (`a-z`), **numeric digits** (`0-9`), or **underscores** (`_`).
  - **Temporal Limit:** A maximum length of 31 characters is strictly enforced.
- **Formal Grammar (Regex):** `[A-Z][a-z0-9_]{0,30}`

### 3.2 Literals
The scanner categorizes constants into five distinct literal classes:

*   **Integer Literals:** Numeric sequences with optional polarity indicators.
    *   *Regex:* `[+-]?[0-9]+`
*   **Floating-Point Literals:** Real-valued constants supporting decimal fractions and scientific exponential notation.
    *   *Constraint:* Precision is limited to six decimal places.
    *   *Regex:* `[+-]?[0-9]+\.[0-9]{1,6}([eE][+-]?[0-9]+)?`
*   **String Literals:** Character sequences delimited by double quotes (`"`). The system supports robust escape sequence interpolation (`\"`, `\\`, `\n`, `\t`, `\r`).
    *   *Regex:* `"([^"\\\n]|\\["\\ntr])*"`
*   **Character Literals:** Scalar characters encapsulated in single quotes (`'`).
    *   *Regex:* `'([^'\\\n]|\\['\\ntr])'`
*   **Boolean Literals:** Finite set containing `true` and `false`.

## 4. Operator Precedence and Hierarchy
The lexical analyzer recognizes multi-character operators with higher priority than single-character tokens to ensure correct interpretation of complex expressions.

### 4.1 Arithmetic Operations
| Precedence | Operator | Description |
| : | : | : |
| 1 | `**` | Exponentiation |
| 2 | `*`, `/`, `%` | Multiplicative Operations |
| 3 | `+`, `-` | Additive Operations |

### 4.2 Logical and Relational Operations
| Precedence | Category | Operators |
| : | : | : |
| 1 | **Unary Logic** | `!` |
| N/A | **Relational** | `==`, `!=`, `<`, `>`, `<=`, `>=` |
| 2 | **Binary Logic** | `&&` |
| 3 | **Binary Logic** | `||` |

## 5. Comment Syntax and Preprocessing
SimpleLang supports two distinct comment formats, which are stripped during the lexical analysis phase:
- **Single-line Comments:** Initiated by `##`. All subsequent characters on the same line are designated as non-tokenizable text.
- **Multi-line Comments:** Delimited by the `#*` (start) and `*#` (end) sequences. These may span multiple lines, facilitating extensive documentation within the source code.

## 6. Implementation Architecture

### 6.1 Manual DFA Implementation (Part 1)
The primary scanner utilizes a **Deterministic Finite Automaton (DFA)** derived from regular expressions. The state-transition logic is implemented within `ManualScanner.java`, employing an adjacency matrix/table approach for O(1) state transitions. This implementation ensures lexical accuracy through rigorous backtracking and longest-match validation.

### 5.2 JFlex Specification (Part 2)
As a comparative baseline, a **JFlex** specification (`Scanner.flex`) is provided. This model-driven approach automates the generation of the transition matrix, serving to validate the lexical correctness of the manual implementation.

## 7. Operation and Deployment

### 7.1 Prerequisites
- **Java SE Development Kit:** Version 8 or higher.
- **JFlex Interpreter:** Required only for modifying lexical rules.

### 7.2 Execution Workflow
1. **Compilation Phase:**
   ```bash
   cd src
   javac *.java
   ```
2. **Scanner Execution (Manual):**
   ```bash
   java ManualScanner ../tests/test1.lang
   ```
3. **Scanner Execution (Automated):**
   ```bash
   java Yylex ../tests/test1.lang
   ```

## 8. Sample Code Demonstrations

### 8.1 Arithmetic and Declaration
```simplelang
start
    declare Factor = 10.5
    declare Result = Factor ** 2
    output Result
finish
```

### 8.2 Functional Recursion
```simplelang
start
    function GCD(A, B)
        condition B == 0
            return A
        else
            return GCD(B, A % B)
        finish
    finish
finish
```

### 8.3 Iterative Convergence
```simplelang
start
    declare Counter = 1
    loop Counter <= 100
        output "Sequence Value: "
        output Counter
        Counter++
    finish
finish
```