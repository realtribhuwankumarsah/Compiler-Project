package codeoptimizer;

public class Instruction {
    private final String destination;
    private String firstOperand;
    private String operator;
    private String secondOperand;

    public Instruction(String destination, String firstOperand, String operator, String secondOperand) {
        this.destination = destination;
        this.firstOperand = firstOperand;
        this.operator = operator;
        this.secondOperand = secondOperand;
    }

    public String getDestination() { return destination; }
    public String getFirstOperand() { return firstOperand; }
    public void setFirstOperand(String value) { firstOperand = value; }
    public String getOperator() { return operator; }
    public void setOperator(String value) { operator = value; }
    public String getSecondOperand() { return secondOperand; }
    public void setSecondOperand(String value) { secondOperand = value; }
    public boolean isBinary() { return operator != null; }

    @Override
    public String toString() {
        return isBinary() ? destination + " = " + firstOperand + " " + operator + " " + secondOperand
                : destination + " = " + firstOperand;
    }
}
