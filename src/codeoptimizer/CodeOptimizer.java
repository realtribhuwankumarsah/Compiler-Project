package codeoptimizer;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeOptimizer {
    private int folded;
    private int eliminated;
    private int removed;

    private static final String NUMBER_TEXT = "[-+]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)";
    private static final String VARIABLE_TEXT = "[A-Za-z_][A-Za-z0-9_]*";
    private static final Pattern NUMBER = Pattern.compile(NUMBER_TEXT);
    private static final Pattern VARIABLE = Pattern.compile(VARIABLE_TEXT);
    private static final Pattern BINARY_EXPRESSION = Pattern.compile(
            "^\\s*(" + NUMBER_TEXT + "|" + VARIABLE_TEXT + ")\\s*([+\\-*/])\\s*(" + NUMBER_TEXT + "|" + VARIABLE_TEXT + ")\\s*$");

    /** Converts each non-empty line of three-address code into an Instruction. */
    public List<Instruction> parse(String text) {
        List<Instruction> result = new ArrayList<>();
        String[] lines = text.split("\\R", -1);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] assignment = line.split("\\s*=\\s*", -1);
            if (assignment.length != 2 || !VARIABLE.matcher(assignment[0].trim()).matches()) {
                throw new IllegalArgumentException("Invalid instruction at line " + (i + 1) + ": " + line);
            }

            String destination = assignment[0].trim();
            String expression = assignment[1].trim();
            Matcher binary = BINARY_EXPRESSION.matcher(expression);

            if (binary.matches()) {
                result.add(new Instruction(destination, binary.group(1), binary.group(2), binary.group(3)));
            } else if (isOperand(expression)) {
                result.add(new Instruction(destination, expression, null, null));
            } else {
                throw new IllegalArgumentException("Invalid instruction at line " + (i + 1) + ": " + line);
            }
        }
        return result;
    }

    private boolean isOperand(String value) {
        String operand = value.trim();
        return NUMBER.matcher(operand).matches() || VARIABLE.matcher(operand).matches();
    }

    private boolean isNumber(String value) {
        return NUMBER.matcher(value.trim()).matches();
    }

    private String formatNumber(double value) {
        if (value == Math.rint(value) && value <= Long.MAX_VALUE && value >= Long.MIN_VALUE) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }

    public List<Instruction> constantFolding(List<Instruction> input) {
        folded = 0;
        for (Instruction instruction : input) {
            if (instruction.isBinary()
                    && isNumber(instruction.getFirstOperand())
                    && isNumber(instruction.getSecondOperand())) {

                double first = Double.parseDouble(instruction.getFirstOperand());
                double second = Double.parseDouble(instruction.getSecondOperand());

                if (instruction.getOperator().equals("/") && second == 0.0) {
                    continue;
                }

                double value;
                switch (instruction.getOperator()) {
                    case "+": value = first + second; break;
                    case "-": value = first - second; break;
                    case "*": value = first * second; break;
                    case "/": value = first / second; break;
                    default: continue;
                }

                instruction.setFirstOperand(formatNumber(value));
                instruction.setOperator(null);
                instruction.setSecondOperand(null);
                folded++;
            }
        }
        return input;
    }

    /**
     * Reuses an earlier expression only while its operands and result variable
     * still have the same values. Reassigning a variable invalidates expressions
     * that depend on its old value, as well as expressions stored in that variable.
     */
    public List<Instruction> commonSubexpressionElimination(List<Instruction> input) {
        eliminated = 0;
        Map<String, String> expressions = new HashMap<>();
        Map<String, Integer> versions = new HashMap<>();

        for (Instruction instruction : input) {
            String destination = instruction.getDestination();
            String expressionKey = null;
            boolean repeated = false;

            if (instruction.isBinary()) {
                expressionKey = expressionKey(instruction, versions);
                String previousResult = expressions.get(expressionKey);
                if (previousResult != null) {
                    instruction.setFirstOperand(previousResult);
                    instruction.setOperator(null);
                    instruction.setSecondOperand(null);
                    repeated = true;
                    eliminated++;
                }
            }

            // The assignment changes the destination's previous value.
            int oldVersion = versions.getOrDefault(destination, 0);
            String oldOperandKey = destination + "@" + oldVersion;
            expressions.entrySet().removeIf(entry ->
                    entry.getValue().equals(destination)
                            || keyUsesOperandVersion(entry.getKey(), oldOperandKey));

            versions.put(destination, oldVersion + 1);

            // Cache a newly computed expression only after invalidation.
            if (expressionKey != null && !repeated) {
                expressions.put(expressionKey, destination);
            }
        }
        return input;
    }

    private String expressionKey(Instruction instruction, Map<String, Integer> versions) {
        String left = operandKey(instruction.getFirstOperand(), versions);
        String right = operandKey(instruction.getSecondOperand(), versions);
        return left + "|" + instruction.getOperator() + "|" + right;
    }

    private String operandKey(String operand, Map<String, Integer> versions) {
        if (isNumber(operand)) {
            return operand;
        }
        return operand + "@" + versions.getOrDefault(operand, 0);
    }

    private boolean keyUsesOperandVersion(String key, String operandVersion) {
        return key.startsWith(operandVersion + "|")
                || key.contains("|" + operandVersion + "|")
                || key.endsWith("|" + operandVersion);
    }

    /** Removes assignments that do not contribute to one of the requested outputs. */
    public List<Instruction> deadCodeRemoval(List<Instruction> input, Set<String> outputs) {
        if (outputs == null || outputs.isEmpty()) {
            throw new IllegalArgumentException("Enter at least one Output Variable before dead code removal.");
        }

        removed = 0;
        Set<String> needed = new HashSet<>(outputs);
        List<Instruction> kept = new ArrayList<>();

        for (int i = input.size() - 1; i >= 0; i--) {
            Instruction instruction = input.get(i);
            if (needed.contains(instruction.getDestination())) {
                kept.add(0, instruction);
                needed.remove(instruction.getDestination());

                if (!isNumber(instruction.getFirstOperand())) {
                    needed.add(instruction.getFirstOperand());
                }
                if (instruction.isBinary() && !isNumber(instruction.getSecondOperand())) {
                    needed.add(instruction.getSecondOperand());
                }
            } else {
                removed++;
            }
        }
        return kept;
    }

    public List<Instruction> optimizeAll(List<Instruction> input, Set<String> outputs) {
        constantFolding(input);
        commonSubexpressionElimination(input);
        return deadCodeRemoval(input, outputs);
    }

    public int getFolded() { return folded; }
    public int getEliminated() { return eliminated; }
    public int getRemoved() { return removed; }
}
