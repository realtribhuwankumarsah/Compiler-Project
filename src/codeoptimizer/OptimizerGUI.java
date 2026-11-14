package codeoptimizer;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class OptimizerGUI extends JFrame {
    private final JTextArea input = new JTextArea(); private final JTextArea output = new JTextArea(); private final JTextArea report = new JTextArea();
    private final JTextField outputVariables = new JTextField(); private final CodeOptimizer optimizer = new CodeOptimizer();
    private List<Instruction> current;
    public OptimizerGUI() {
        setTitle("Code Optimizer – Compiler Design Project"); setSize(950, 700); setLocationRelativeTo(null); setDefaultCloseOperation(EXIT_ON_CLOSE);
        input.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14)); output.setFont(input.getFont()); output.setEditable(false); report.setEditable(false); report.setRows(5);
        JLabel title = new JLabel("CODE OPTIMIZER", SwingConstants.CENTER); title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        JLabel sub = new JLabel("Compiler Design Project", SwingConstants.CENTER);
        JPanel header = new JPanel(new GridLayout(2, 1)); header.add(title); header.add(sub);
        JPanel top = new JPanel(new BorderLayout(5, 5)); top.add(new JLabel("Output Variables (protected):"), BorderLayout.WEST); top.add(outputVariables);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton foldingButton = new JButton("Constant Folding");
        JButton commonButton = new JButton("Common Sub-expression Elimination");
        JButton deadButton = new JButton("Dead Code Removal");
        JButton allButton = new JButton("Optimize All");
        JButton clearButton = new JButton("Clear");
        foldingButton.addActionListener(e -> action("Constant Folding"));
        commonButton.addActionListener(e -> action("Common Sub-expression Elimination"));
        deadButton.addActionListener(e -> action("Dead Code Removal"));
        allButton.addActionListener(e -> action("Optimize All"));
        clearButton.addActionListener(e -> action("Clear"));
        buttons.add(foldingButton); buttons.add(commonButton); buttons.add(deadButton); buttons.add(allButton); buttons.add(clearButton);
        JPanel left = new JPanel(new BorderLayout(5, 5)); left.add(new JLabel("Enter Intermediate Code"), BorderLayout.NORTH); left.add(new JScrollPane(input));
        JPanel right = new JPanel(new BorderLayout(5, 5)); right.add(new JLabel("Optimized Intermediate Code"), BorderLayout.NORTH); right.add(new JScrollPane(output));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right); split.setResizeWeight(.5);
        JPanel center = new JPanel(new BorderLayout(8, 8)); center.add(top, BorderLayout.NORTH); center.add(split, BorderLayout.CENTER); center.add(new JScrollPane(report), BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH); add(center, BorderLayout.CENTER); add(buttons, BorderLayout.SOUTH);
    }
    private void action(String name) {
        if (name.equals("Clear")) { input.setText(""); output.setText(""); report.setText(""); current = null; return; }
        try {
            if (input.getText().trim().isEmpty()) throw new IllegalArgumentException("Enter intermediate code before optimizing.");
            Set<String> vars = variables();
            if ((name.equals("Dead Code Removal") || name.equals("Optimize All")) && vars.isEmpty())
                throw new IllegalArgumentException("Enter at least one Output Variable before running " + name + ".");
            current = optimizer.parse(input.getText()); int before = current.size();
            if (name.equals("Constant Folding")) optimizer.constantFolding(current); else if (name.startsWith("Common")) optimizer.commonSubexpressionElimination(current); else if (name.equals("Dead Code Removal")) current = optimizer.deadCodeRemoval(current, vars); else current = optimizer.optimizeAll(current, vars);
            display(before, name);
        } catch (Exception ex) {
            ex.printStackTrace();
            String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            JOptionPane.showMessageDialog(this, message, "Optimization Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private Set<String> variables() { Set<String> set = new HashSet<>(); for (String s : outputVariables.getText().split(",")) if (!s.trim().isEmpty()) set.add(s.trim()); return set; }
    private void display(int before, String operation) { StringBuilder code = new StringBuilder(); for (Instruction i : current) code.append(i).append('\n'); output.setText(code.toString()); report.setText(operation + " completed.\nConstant expressions evaluated: " + optimizer.getFolded() + "\nCommon expressions eliminated: " + optimizer.getEliminated() + "\nDead assignments removed: " + optimizer.getRemoved() + "\nInstructions before: " + before + "\nInstructions after: " + current.size()); }
}
