package components;

import info.lundin.math.Derive;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.concurrent.TimeUnit;

public class ProgressBarDemo21 extends JPanel implements ActionListener, PropertyChangeListener {

    private JProgressBar progressBar;
    private Task task;

    TextField txtFunction;
    TextField txtStartValue;
    TextField txtIter;
    TextField txtTol;

    TextArea display;
    Button btnGo;
    Button btnStop;
    Button btnContin;
    Button btnReset;
    
    Panel commonPanel;
    Panel btnPanel;
    Panel lblPanel;
    Panel txtPanel;
    Panel p4;
    Panel p5;
    Label lblEquation;
    Label lblStartVal;
    Label lblIterations;
    Label lblTolerance;
    Derive d = new Derive();
    BigDecimal xVal;
    BigDecimal xPrev;
    BigDecimal funcVal;
    BigDecimal b_a;
    BigDecimal tol;
    BigDecimal a;
    BigDecimal b;

    boolean loop = false;

    class Task extends SwingWorker<Void, Void> {

        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() {

            int k = 0, substringPos , maxIter ;
            String functionStr, derive;

            loop = true;
            btnGo.setEnabled(false);
            btnContin.setEnabled(false);

            
            try {
                display.append("\n");
                functionStr = txtFunction.getText().trim();

                if (functionStr.equals("")) {
                    throw new Exception("No equation given");
                }
                int progress ;
                //Initialize progress property.
                setProgress(0);
                xVal = new BigDecimal(txtStartValue.getText().trim());
                maxIter = Integer.parseInt(txtIter.getText().trim());

                if ((substringPos = functionStr.indexOf("=")) != -1) {
                    functionStr = functionStr.substring(0, substringPos) + "-(" + functionStr.substring(substringPos + 1, functionStr.length()) + ")";
                }

                long startTime = System.nanoTime();

                derive = d.diff(functionStr, "x")[0];
                Expression expression = new Expression(functionStr);
                Expression deriveExpression = new Expression(derive);

                tol = new BigDecimal(txtTol.getText().trim());
                funcVal = expression.with("x", xVal).eval();

                for (; k < maxIter && loop; k++) {
                    xPrev = xVal;
                    xVal = xVal.subtract(funcVal.divide(deriveExpression.with("x", xVal).eval(), MathContext.DECIMAL128));  //xVal - e.eval(s1, "x=" + xVal) / e.eval(derive, "x=" + xVal);
                    funcVal = expression.with("x", xVal).eval();
                    
                     //interacts with GUI
//                  ========================================================
                    progress = (int) (k + 1) * 100 / maxIter; 
                    setProgress(progress);
                    display.append((k + 1) + ". x= " + xVal + ";  f(x)= " + funcVal + ";  abs(b-a)= " + (xPrev.subtract(xVal)).abs().toString() + "\n");
//                  ========================================================

                    if ((xPrev.subtract(xVal)).abs().compareTo(tol) == -1) {
                        break;
                    }

                }
                setProgress(100);
                long estimatedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

                if (k >= maxIter) {
                    display.append("Elapsed Time= " + estimatedTime + " ms. Maximum iterations reached\n");
                } else {
                    display.append("Elapsed Time= " + estimatedTime + " ms.\n");
                }


            } catch (NumberFormatException _ex) {
                display.append("Error: please make sure  that a start value, the number of iterations \nand tolerance were given and are numbers \n\n");
            } catch (Exception exception) {
                display.append("Error: " + exception.getMessage() + "\n\n");
            }

            loop = false;

            return null;
        }

        /*
         * Executed in event dispatch thread
         */
        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            btnContin.setEnabled(true);
            btnGo.setEnabled(true);
//            taskOutput.append("Done!\n");
        }
    } //end class

    
    
    public ProgressBarDemo21() {
        
        super(new BorderLayout());
        txtFunction = new TextField();
        txtStartValue = new TextField();
        txtIter = new TextField("10");
        txtTol = new TextField();
        display = new TextArea(5, 30);
        
        btnGo = new Button("Start");
        btnStop = new Button("Stop");
        btnContin = new Button("Continue");
        btnReset=new Button("Clear");
        
        btnGo.setActionCommand("go");
        btnStop.setActionCommand("stop");
        btnContin.setActionCommand("continue");
        btnReset.setActionCommand("reset");
        
        
        btnGo.addActionListener(this);
        btnStop.addActionListener(this);
        btnContin.addActionListener(this);
        btnReset.addActionListener(this);
        
        commonPanel = new Panel();
        btnPanel = new Panel();
        p4 = new Panel();
        lblPanel = new Panel();
        txtPanel = new Panel();
        p5 = new Panel();

        commonPanel.setBackground(Color.lightGray);
        btnPanel.setBackground(Color.lightGray);
        lblPanel.setBackground(Color.lightGray);
        txtPanel.setBackground(Color.lightGray);
        p4.setBackground(Color.lightGray);
        p5.setBackground(Color.lightGray);

        txtFunction.setBackground(Color.white);
        txtStartValue.setBackground(Color.white);
        txtIter.setBackground(Color.white);
        display.setBackground(Color.white);

        lblEquation = new Label("Equation, f(x) = g(x)");
        lblStartVal = new Label("Start value");
        lblIterations = new Label("Number of iterations");
        lblTolerance = new Label("Tolerance");

        commonPanel.setLayout(new GridLayout(5, 1));
        btnPanel.setLayout(new GridLayout(1, 5)); //for btns
        lblPanel.setLayout(new GridLayout(1, 3)); //labels for startValue value and iterations
        txtPanel.setLayout(new GridLayout(1, 4)); //txtfields for startValue value , iterations, tolerance

        lblPanel.add(lblStartVal);
        lblPanel.add(lblIterations);
        lblPanel.add(lblTolerance);
        txtPanel.add(txtStartValue);
        txtPanel.add(txtIter);
        txtPanel.add(txtTol);
        btnPanel.add(p4);
        btnPanel.add(btnGo);
        btnPanel.add(btnStop);
        btnPanel.add(btnContin);
        btnPanel.add(btnReset);

        btnPanel.add(p5);
        commonPanel.add(lblEquation);
        commonPanel.add(txtFunction);
        commonPanel.add(lblPanel);
        commonPanel.add(txtPanel);
        commonPanel.add(btnPanel);
        
        
        
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        add( commonPanel, BorderLayout.PAGE_START);
        add( new JScrollPane(display), BorderLayout.CENTER);
        add( progressBar, BorderLayout.PAGE_END);
    }
    
    
    
    /**
     * Invoked when the user presses the start button.
     * 
     */
    public void actionPerformed(ActionEvent evt) {

        if ("go".equals(evt.getActionCommand())) {

//            progressBar.setIndeterminate(true);
            //Instances of javax.swing.SwingWorker are not reusuable, so
            //we create new instances as needed.
            task = new Task();
            task.addPropertyChangeListener(this);
            task.execute();

        } else if ("stop".equals(evt.getActionCommand())) {
            loop = false;
        } else if ("continue".equals(evt.getActionCommand())) {
            txtStartValue.setText(String.valueOf(xVal));
            task = new Task();
            task.addPropertyChangeListener(this);
            task.execute();
        } else if ("reset".equals(evt.getActionCommand())) {
            display.setText(""+'\u0000'); //ACSII code of 0 is '\u0000'
            txtFunction.setText(""+'\u0000');
            txtTol.setText(""+'\u0000');
            txtStartValue.setText(""+'\u0000');
        }

    }

    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setIndeterminate(false);
            progressBar.setValue(progress);
        }
    }

    /**
     * Create the GUI and show it. As with all GUI code, this must run on the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Newton Method");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Create and set up the content pane.
        JComponent newContentPane = new ProgressBarDemo21();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
        
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
