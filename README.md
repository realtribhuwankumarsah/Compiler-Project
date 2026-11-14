# Code Optimizer – Compiler Design Project

A simple Java Swing application that optimizes Three-Address Code (TAC) using:

1. **Constant Folding** – evaluates arithmetic expressions containing only numeric constants.
2. **Common Sub-expression Elimination** – reuses a previous calculation when its operands have not changed.
3. **Dead Code Removal** – removes assignments that do not contribute to the output variables selected by the user.

## Requirements

- JDK 17 or newer
- Visual Studio Code (or another Java IDE)

No Lex, Yacc, Flex, external libraries, or build tools are required.

## Project structure

```text
Compiler Project/
├── README.md
└── src/
    └── codeoptimizer/
        ├── Main.java
        ├── OptimizerGUI.java
        ├── CodeOptimizer.java
        └── Instruction.java
```

## Compile

Run this command from the project folder:

```powershell
javac -d out src/codeoptimizer/*.java
```

## Run

```powershell
java -cp out codeoptimizer.Main
```

## Using the application

1. Enter one TAC instruction per line in the input area.
2. Enter the output variable(s) to preserve, separated by commas (for example, `result, total`).
3. Click an individual optimization button or **Optimize All**.
4. Read the optimized code and optimization report.

## Supported TAC format

```text
a = 10
t1 = a + b
t2 = 10 * 5
result = t1 + t2
```

Supported operators: `+`, `-`, `*`, `/`.

Operands may be variable names, integers, or decimal numbers, including signed constants such as `-5` and `+2`.

## Notes and limitations

- This is a small educational optimizer for straight-line assignment-based TAC, not a complete compiler.
- Dead Code Removal requires at least one output variable so the optimizer knows which results must be preserved.
- Division by zero is not folded; the original expression is left unchanged.
- The optimizer does not handle branches, loops, pointers, function calls, or side effects.
