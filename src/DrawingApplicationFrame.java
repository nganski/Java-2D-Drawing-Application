package pkg2ddrawingapplication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
/**
 *
 * @author Ngan
 */
public class DrawingApplicationFrame extends JFrame
{

    // Create the panels for the top of the application. One panel for each
    // line and one to contain both of those panels.
    private final JPanel line1 = new JPanel();
    private final JPanel line2 = new JPanel();
    private final JPanel topPanel;
    // create the widgets for the firstLine Panel.
    private final JComboBox<String> shapeSelection = new JComboBox<>(new String[] {"Line", "Oval", "Rectangle"});
    private final JButton color1Button = new JButton("1st Color...");
    private final JButton color2Button = new JButton("2nd Color...");
    private final JButton undoButton = new JButton("Undo");
    private final JButton clearButton = new JButton("Clear");
    
    private Color color1 = Color.BLACK;
    private Color color2 = Color.BLACK;
    //create the widgets for the secondLine Panel.
    private final JCheckBox fillCheckBox = new JCheckBox("Filled");
    private final JCheckBox gradientCheckBox = new JCheckBox("Use Gradient");
    private final JCheckBox dashCheckBox = new JCheckBox("Dashed");
    private final JSpinner lineWidthSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 99, 1));
    private final JSpinner dashLengthSpinner = new JSpinner(new SpinnerNumberModel(15, 1, 99, 1));
    
    // Variables for drawPanel.
    private final DrawPanel drawPanel = new DrawPanel();
    // add status label
    private final JLabel statusLabel = new JLabel("(0,0)");
    // Constructor for DrawingApplicationFrame
    public DrawingApplicationFrame()
    {
        this.topPanel = new JPanel();
        setTitle("Java 2D Drawings");
        // set panel colors 
        topPanel.setBackground(Color.CYAN);
        line1.setBackground(Color.CYAN);
        line2.setBackground(Color.CYAN);
        
        // add widgets to panels
        line1.setLayout(new FlowLayout(FlowLayout.CENTER));
        line1.add(new JLabel("Shapes:"));
        line1.add(shapeSelection);
        line1.add(color1Button);
        line1.add(color2Button);
        line1.add(undoButton);
        line1.add(clearButton);
        
        line2.setLayout(new FlowLayout(FlowLayout.CENTER));
        line2.add(new JLabel("Options:"));
        line2.add(fillCheckBox);
        line2.add(gradientCheckBox);
        line2.add(dashCheckBox);
        line2.add(new JLabel("Line Width:"));
        line2.add(lineWidthSpinner);
        line2.add(new JLabel("Dash Length:"));
        line2.add(dashLengthSpinner);
        
        // firstLine widgets
        topPanel.setLayout(new GridLayout(2, 1));
        topPanel.add(line1);
        // secondLine widgets
        topPanel.add(line2);
        // add top panel of two panels
        
        // add topPanel to North, drawPanel to Center, and statusLabel to South
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(drawPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        
        
        //add listeners and event handlers
        
        color1Button.addActionListener(e ->
                color1 = JColorChooser.showDialog(this, "Choose a Color", color1));
        
        color2Button.addActionListener(e ->
                color2 = JColorChooser.showDialog(this, "Choose a Color", color2));
        
        undoButton.addActionListener(e -> drawPanel.undoLastShape());

        clearButton.addActionListener(e -> drawPanel.clearShapes());
                     
    }

    // Create a private inner class for the DrawPanel.
    private class DrawPanel extends JPanel
    {
        private final ArrayList<MyShapes> shapes = new ArrayList<>();
        private MyShapes currentShape;

        
        public DrawPanel()
        {
            setBackground(Color.WHITE);
            
            MouseHandler handler = new MouseHandler();
            addMouseListener(handler);
            addMouseMotionListener(handler);
                
            setupKeyBindings();
        }
        
        public void clearShapes(){
            shapes.clear();
            repaint();
        }
        
        public void undoLastShape(){
            if(!shapes.isEmpty()){
                shapes.remove(shapes.size()-1);
                repaint();
            }
        }
        
        @Override
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            //loop through and draw each shape in the shapes arraylist
            for (MyShapes shape : shapes) {
                shape.draw(g2d);
            }
        }
        
        // keybinds to shortcuts -- undo, clear, save, load
        private void setupKeyBindings() {
            InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap am = this.getActionMap();

            // Ctrl+Z -> Undo
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");
            am.put("undo", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    undoLastShape();
                }
            });

            // C -> Clear
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "clear");
            am.put("clear", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    clearShapes();
                }
            });
            
            // Ctrl+S -> Save
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "save");
            am.put("save", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser chooser = new JFileChooser();
                    if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                        File file = chooser.getSelectedFile();
                        // Force .png extension
                        if (!file.getName().toLowerCase().endsWith(".png")) {
                            file = new File(file.getAbsolutePath() + ".png");
                        }

                        try {
                            BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                            Graphics2D g2 = image.createGraphics();
                            paint(g2);
                            g2.dispose();

                            ImageIO.write(image, "png", file);
                            System.out.println("Saved as PNG: " + file.getAbsolutePath());
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
      
        }
        
        
        private class MouseHandler extends MouseAdapter implements MouseMotionListener
        {
            private Point clickPoint;
            
            @Override
            public void mousePressed(MouseEvent event)
            {
                clickPoint = event.getPoint();
                Point end = new Point(clickPoint);
                
                int shapeType = shapeSelection.getSelectedIndex();
                boolean filled = fillCheckBox.isSelected();
                
                // set paint
                Paint paint = color1;
                
                if(gradientCheckBox.isSelected())
                {
                    paint = new GradientPaint(0, 0, color1, 50, 50, color2, true);
                }
                
                // set stroke
                int lineWidth = (Integer)lineWidthSpinner.getValue();
                float dashLen = ((Integer)dashLengthSpinner.getValue()).floatValue();
                Stroke stroke;
                
                if (dashCheckBox.isSelected())
                {
                    float[] dashLength = {dashLen};
                    
                    stroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, dashLength, 0);
                } else
                {
                    stroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                }
                
                // create shapes
                switch (shapeType)
                {
                    case 0 -> currentShape = new MyLine(clickPoint, end, paint, stroke);
                        
                    case 1 -> currentShape = new MyOval(clickPoint,end,paint, stroke, filled);
                        
                    case 2 -> currentShape = new MyRectangle(clickPoint, end, paint, stroke, filled);
                }
            
                shapes.add(currentShape);
                    
            }
            @Override
            public void mouseReleased(MouseEvent event)
            {
                currentShape.setEndPoint(event.getPoint());
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent event)
            {
               currentShape.setEndPoint(event.getPoint());
               repaint();
                
            }

            @Override
            public void mouseMoved(MouseEvent event)
            {
                clickPoint = event.getPoint();
                String position = "(" + event.getX() + "," + event.getY() + ")";
                statusLabel.setText(position);
            }
        }

    }
}
