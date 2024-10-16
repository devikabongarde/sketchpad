import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.ArrayList;
import java.awt.geom.Line2D;
import java.awt.geom.Ellipse2D;

public class EnhancedDrawingApp extends JFrame implements ActionListener, MouseListener, MouseMotionListener {
    private String currentAction = "Freehand";
    private Color currentColor = Color.BLACK;
    private Color fillColor = new Color(0, 0, 0, 0); // Transparent fill color
    private int startX, startY, endX, endY;
    private BufferedImage canvasImage;
    private Graphics2D g2d;
    private static final int MIN_ERASER_SIZE = 10;
    private int eraserSize = 20; // Eraser size
    private int strokeThickness = 2; // Thickness for freehand tool
    private Shape currentShape;
    private ArrayList<Shape> shapes = new ArrayList<>();
    private ArrayList<Color> fillColors = new ArrayList<>();
    private ArrayList<BufferedImage> undoStack = new ArrayList<>();
    private ArrayList<BufferedImage> redoStack = new ArrayList<>();
    private Cursor customCursor;
    private JPanel canvasPanel;

    public EnhancedDrawingApp() {
        setTitle("Enhanced Drawing App");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize canvas
        canvasImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);
        g2d = canvasImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, canvasImage.getWidth(), canvasImage.getHeight());
        g2d.setColor(currentColor);
        g2d.setStroke(new BasicStroke(strokeThickness));

        canvasPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw the canvas image
                g.drawImage(canvasImage, 0, 0, getWidth(), getHeight(), null);

                // Draw shape previews
                if (currentShape != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(currentColor);
                    g2.setStroke(new BasicStroke(strokeThickness));
                    g2.draw(currentShape);

                    // Optionally, draw a semi-transparent fill color for the preview
                    if (fillColor.getAlpha() > 0) {
                        g2.setColor(fillColor);
                        g2.fill(currentShape);
                    }
                    g2.dispose();
                }
            }

        };

        canvasPanel.setPreferredSize(new Dimension(1920, 900));
        canvasPanel.addMouseListener(this);
        canvasPanel.addMouseMotionListener(this);

        // Create Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu editMenu = new JMenu("Edit");

        // Shape icons and options
        String[] shapeOptions = { "Freehand", "Line", "Rectangle", "Oval", "Text" };
        @SuppressWarnings("unused")
        String[] shapeIconPaths = { "/icons/freehand.png", "/icons/line.png", "/icons/rectangle.png", "/icons/oval.png",
                "/icons/text.png" };
        for (int i = 0; i < shapeOptions.length; i++) {
            JMenuItem item = new JMenuItem(shapeOptions[i]);
            // Optionally set icons if available
            item.setIcon(new ImageIcon(getClass().getResource(shapeIconPaths[i])));
            item.addActionListener(this);
            editMenu.add(item);
        }

        editMenu.addSeparator();

        // Color icons
        String[] colorNames = { "Black", "Red", "Green", "Blue", "Yellow", "Orange", "Pink", "Cyan", "Magenta", "Gray",
                "Dark Gray", "Light Gray", "White", "Brown", "Purple", "Violet", "Gold", "Silver", "Teal", "Lime" };
        Color[] colors = { Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE, Color.PINK,
                Color.CYAN,
                Color.MAGENTA, Color.GRAY, Color.DARK_GRAY, Color.LIGHT_GRAY, Color.WHITE, new Color(139, 69, 19),
                new Color(128, 0, 128), new Color(238, 130, 238), new Color(255, 215, 0), new Color(192, 192, 192),
                new Color(0, 128, 128), new Color(50, 205, 50) };

        for (int i = 0; i < colorNames.length; i++) {
            JMenuItem item = new JMenuItem(colorNames[i]);
            item.setIcon(new ColorIcon(colors[i]));
            item.addActionListener(this);
            editMenu.add(item);
        }

        editMenu.addSeparator();

        // Extra tools icons
        String[] toolOptions = { "Erase", "Color Picker", "Fill Color" };
        @SuppressWarnings("unused")
        String[] toolIconPaths = { "/icons/eraser.png", "/icons/colorpicker.png", "/icons/fillcolor.png" };
        for (int i = 0; i < toolOptions.length; i++) {
            JMenuItem item = new JMenuItem(toolOptions[i]);
            // Optionally set icons if available
            item.setIcon(new ImageIcon(getClass().getResource(toolIconPaths[i])));
            item.addActionListener(this);
            editMenu.add(item);
        }

        menuBar.add(editMenu);
        setJMenuBar(menuBar);

        // Bottom panel for Save, Reset, and Eraser Size and Thickness controls
        JPanel bottomPanel = new JPanel();

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(this);

        bottomPanel.add(saveButton);
        bottomPanel.add(resetButton);
        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(this);
        bottomPanel.add(undoButton);

        JButton redoButton = new JButton("Redo");
        redoButton.addActionListener(this);
        bottomPanel.add(redoButton);

        // Eraser Size Control
        JLabel eraserLabel = new JLabel("Eraser Size:");
        JSlider eraserSlider = new JSlider(MIN_ERASER_SIZE, 100, eraserSize);
        eraserSlider.setMajorTickSpacing(10);
        eraserSlider.setPaintTicks(true);
        eraserSlider.setPaintLabels(true);
        eraserSlider.addChangeListener(e -> eraserSize = eraserSlider.getValue());

        bottomPanel.add(eraserLabel);
        bottomPanel.add(eraserSlider);

        // Stroke Thickness Control
        JLabel thicknessLabel = new JLabel("Stroke Thickness:");
        JSlider thicknessSlider = new JSlider(1, 20, strokeThickness);
        thicknessSlider.setMajorTickSpacing(1);
        thicknessSlider.setPaintTicks(true);
        thicknessSlider.setPaintLabels(true);
        thicknessSlider.addChangeListener(e -> {
            strokeThickness = thicknessSlider.getValue();
            g2d.setStroke(new BasicStroke(strokeThickness));
        });

        bottomPanel.add(thicknessLabel);
        bottomPanel.add(thicknessSlider);

        add(canvasPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Set default cursor
        setCustomCursor(Cursor.CROSSHAIR_CURSOR);
        addKeyboardShortcuts();
    }

    private void addKeyboardShortcuts() {
    // Get the InputMap and ActionMap for the canvasPanel
    InputMap inputMap = canvasPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = canvasPanel.getActionMap();

    // Define keyboard shortcuts and corresponding actions

    // Undo (Ctrl + Z)
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "UndoAction");
    actionMap.put("UndoAction", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            undo();
        }
    });

    // Redo (Ctrl + Y)
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "RedoAction");
    actionMap.put("RedoAction", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            redo();
        }
    });

    // Save (Ctrl + S)
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "SaveAction");
    actionMap.put("SaveAction", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            saveDrawing();
        }
    });

    // Reset (Ctrl + R)
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), "ResetAction");
    actionMap.put("ResetAction", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            resetDrawing();
        }
    });

    // Select Freehand Tool (Ctrl + F)
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "FreehandAction");
    actionMap.put("FreehandAction", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            currentAction = "Freehand";
            setCustomCursor(Cursor.CROSSHAIR_CURSOR);
        }
    });

    // Select Line Tool (Ctrl + L)
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), "LineAction");
    actionMap.put("LineAction", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            currentAction = "Line";
            setCustomCursor(Cursor.CROSSHAIR_CURSOR);
        }
    });

    // Select Rectangle Tool (Ctrl + Shift + R)
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "RectangleAction");
    actionMap.put("RectangleAction", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            currentAction = "Rectangle";
            setCustomCursor(Cursor.CROSSHAIR_CURSOR);
        }
    });

    // Select Oval Tool (Ctrl + O)
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), "OvalAction");
    actionMap.put("OvalAction", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            currentAction = "Oval";
            setCustomCursor(Cursor.CROSSHAIR_CURSOR);
        }
    });

    // Eraser Tool (Ctrl + E)
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), "EraserAction");
    actionMap.put("EraserAction", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            currentAction = "Erase";
            setCustomCursor(Cursor.HAND_CURSOR);
        }
    });

    // Fill Color Tool (Ctrl + Shift + F)
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "FillColorAction");
    actionMap.put("FillColorAction", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            fillColor = JColorChooser.showDialog(EnhancedDrawingApp.this, "Choose Fill Color", fillColor);
        }
    });
}


    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch (command) {
            case "Freehand":
            case "Line":
            case "Rectangle":
            case "Oval":
            case "Text":
                currentAction = command;
                setCustomCursor(Cursor.CROSSHAIR_CURSOR);
                fillColor = new Color(0, 0, 0, 0); // Reset fill color to transparent
                break;

            case "Black":
            case "Red":
            case "Green":
            case "Blue":
            case "Yellow":
            case "Orange":
            case "Pink":
            case "Cyan":
            case "Magenta":
            case "Gray":
            case "Dark Gray":
            case "Light Gray":
            case "White":
            case "Brown":
            case "Purple":
            case "Violet":
            case "Gold":
            case "Silver":
            case "Teal":
            case "Lime":
                currentColor = getColorByName(command);
                g2d.setColor(currentColor);
                break;

            case "Save":
                saveDrawing();
                break;

            case "Reset":
                resetDrawing();
                break;

            case "Erase":
                currentAction = "Erase";
                setCustomCursor(Cursor.HAND_CURSOR);
                break;

            case "Color Picker":
                Color newColor = JColorChooser.showDialog(this, "Choose Color", currentColor);
                if (newColor != null) {
                    currentColor = newColor;
                    g2d.setColor(currentColor);
                }
                break;

            case "Fill Color":
                currentAction = "Fill Color";
                fillColor = JColorChooser.showDialog(this, "Choose Fill Color", fillColor);
                break;
            case "Undo":
                undo();
                break;

            case "Redo":
                redo();
                break;
            
        }
    }

    private Color getColorByName(String colorName) {
        switch (colorName) {
            case "Black":
                return Color.BLACK;
            case "Red":
                return Color.RED;
            case "Green":
                return Color.GREEN;
            case "Blue":
                return Color.BLUE;
            case "Yellow":
                return Color.YELLOW;
            case "Orange":
                return Color.ORANGE;
            case "Pink":
                return Color.PINK;
            case "Cyan":
                return Color.CYAN;
            case "Magenta":
                return Color.MAGENTA;
            case "Gray":
                return Color.GRAY;
            case "Dark Gray":
                return Color.DARK_GRAY;
            case "Light Gray":
                return Color.LIGHT_GRAY;
            case "White":
                return Color.WHITE;
            case "Brown":
                return new Color(139, 69, 19);
            case "Purple":
                return new Color(128, 0, 128);
            case "Violet":
                return new Color(238, 130, 238);
            case "Gold":
                return new Color(255, 215, 0);
            case "Silver":
                return new Color(192, 192, 192);
            case "Teal":
                return new Color(0, 128, 128);
            case "Lime":
                return new Color(50, 205, 50);
            default:
                return Color.BLACK;
        }
    }
    @SuppressWarnings("unused")
    private int getSelectedThickness() {
        return thicknessSlider.getValue(); // Assuming a JSlider for thickness selection
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (currentAction.equals("Text")) {
            startX = e.getX() * canvasImage.getWidth() / canvasPanel.getWidth();
            startY = e.getY() * canvasImage.getHeight() / canvasPanel.getHeight();
            // Add a text field to input the text
            JTextField textField = new JTextField();
            textField.setFont(new Font("Arial", Font.PLAIN, 24));
            int result = JOptionPane.showConfirmDialog(this, textField, "Enter Text", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String text = textField.getText();
                // Draw the text on the canvas
                g2d.setFont(new Font("Arial", Font.PLAIN, 24));
                g2d.drawString(text, startX, startY);
                canvasPanel.repaint();
            }
        }
        startX = e.getX() * canvasImage.getWidth() / canvasPanel.getWidth();
        startY = e.getY() * canvasImage.getHeight() / canvasPanel.getHeight();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (currentAction.equals("Text")) {
            // Do nothing
        }
        endX = e.getX() * canvasImage.getWidth() / canvasPanel.getWidth();
        endY = e.getY() * canvasImage.getHeight() / canvasPanel.getHeight();

        drawShape(); // Finalize the shape drawing

        // Reset the currentShape so that no preview is shown after the shape is
        // finalized
        currentShape = null;
        canvasPanel.repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Convert mouse coordinates to canvas coordinates
        endX = e.getX() * canvasImage.getWidth() / canvasPanel.getWidth();
        endY = e.getY() * canvasImage.getHeight() / canvasPanel.getHeight();

        // Update currentShape based on the selected action
        if (currentAction.equals("Freehand")) {
            drawLine(startX, startY, endX, endY);
            startX = endX;
            startY = endY;
        } else if (currentAction.equals("Erase")) {
            erase(endX, endY);
        } else if (currentAction.equals("Line")) {
            currentShape = new Line2D.Double(startX, startY, endX, endY);
        } else if (currentAction.equals("Rectangle")) {
            int x = Math.min(startX, endX);
            int y = Math.min(startY, endY);
            int width = Math.abs(startX - endX);
            int height = Math.abs(startY - endY);
            currentShape = new Rectangle(x, y, width, height);
        } else if (currentAction.equals("Oval")) {
            int x = Math.min(startX, endX);
            int y = Math.min(startY, endY);
            int width = Math.abs(startX - endX);
            int height = Math.abs(startY - endY);
            currentShape = new Ellipse2D.Double(x, y, width, height);
        }

        // Repaint the canvas to show the updated preview
        canvasPanel.repaint();
    }

    private void drawLine(int x1, int y1, int x2, int y2) {
        g2d.drawLine(x1, y1, x2, y2);
    }

    private void drawShape() {
        saveStateToUndoStack();
        switch (currentAction) {
            case "Line":
                currentShape = new Line2D.Double(startX, startY, endX, endY);
                break;
            case "Rectangle":
                currentShape = new Rectangle(Math.min(startX, endX), Math.min(startY, endY), Math.abs(startX - endX),
                        Math.abs(startY - endY));
                break;
            case "Oval":
                currentShape = new Ellipse2D.Double(Math.min(startX, endX), Math.min(startY, endY),
                        Math.abs(startX - endX), Math.abs(startY - endY));
                break;
        }

        // Draw the shape on the canvas
        if (currentShape != null) {
            g2d.setColor(currentColor);
            g2d.draw(currentShape);

            if (fillColor.getAlpha() > 0) {
                g2d.setColor(fillColor);
                g2d.fill(currentShape);
            }

            shapes.add(currentShape);
            fillColors.add(fillColor);

            currentShape = null;
            canvasPanel.repaint();
        }
    }

    private void erase(int x, int y) {
        saveStateToUndoStack();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(x - eraserSize / 2, y - eraserSize / 2, eraserSize, eraserSize);
    }

    private void saveStateToUndoStack() {
        BufferedImage currentState = new BufferedImage(canvasImage.getWidth(), canvasImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = currentState.createGraphics();
        g2.drawImage(canvasImage, 0, 0, null);
        g2.dispose();
        undoStack.add(currentState);
        redoStack.clear(); // Clear redo stack on new action
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            BufferedImage lastState = undoStack.remove(undoStack.size() - 1);
            redoStack.add(copyImage(canvasImage));
            canvasImage.getGraphics().drawImage(lastState, 0, 0, null);
            canvasPanel.repaint();
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            BufferedImage lastState = redoStack.remove(redoStack.size() - 1);
            undoStack.add(copyImage(canvasImage));
            canvasImage.getGraphics().drawImage(lastState, 0, 0, null);
            canvasPanel.repaint();
        }
    }

    private BufferedImage copyImage(BufferedImage image) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = copy.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return copy;
    }

    private void saveDrawing() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                ImageIO.write(canvasImage, "PNG", file);
                JOptionPane.showMessageDialog(this, "Drawing saved successfully!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void resetDrawing() {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, canvasImage.getWidth(), canvasImage.getHeight());
        g2d.setColor(currentColor);
        shapes.clear();
        fillColors.clear();
        canvasPanel.repaint();
    }

    private void setCustomCursor(int cursorType) {
        customCursor = Cursor.getPredefinedCursor(cursorType);
        canvasPanel.setCursor(customCursor);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (currentAction.equals("Fill Color")) {
            fillShapeOrBackground(e.getX(), e.getY());
        }
    }

    private void fillShapeOrBackground(int x, int y) {
        int adjustedX = x * canvasImage.getWidth() / canvasPanel.getWidth();
        int adjustedY = y * canvasImage.getHeight() / canvasPanel.getHeight();

        boolean shapeFilled = false;

        // Check if the click is inside any shape
        for (int i = 0; i < shapes.size(); i++) {
            Shape shape = shapes.get(i);
            if (shape.contains(adjustedX, adjustedY)) {
                Graphics2D g2 = canvasImage.createGraphics();
                g2.setColor(fillColor);
                g2.fill(shape);
                g2.dispose();
                canvasPanel.repaint();
                shapeFilled = true;
                break;
            }
        }

        // If no shape was clicked, fill the background
        if (!shapeFilled) {
            Graphics2D g2 = canvasImage.createGraphics();
            g2.setColor(fillColor);
            g2.fillRect(0, 0, canvasImage.getWidth(), canvasImage.getHeight());

            // Redraw all shapes after filling the background
            g2.setColor(Color.BLACK); // Set the color for the shapes' outline
            for (Shape shape : shapes) {
                g2.draw(shape); // Draw each shape with its outline color
            }

            g2.dispose();
            canvasPanel.repaint();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EnhancedDrawingApp drawingApp = new EnhancedDrawingApp();
            drawingApp.setVisible(true);
        });
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseMoved'");
    }

}

// Custom ColorIcon class for displaying color swatches in the menu
class ColorIcon implements Icon {
    private final int width = 16;
    private final int height = 16;
    private final Color color;

    public ColorIcon(Color color) {
        this.color = color;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }
}