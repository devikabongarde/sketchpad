import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.ArrayList;

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
    private Cursor customCursor;

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

        JPanel canvasPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw the canvas image
                g.drawImage(canvasImage, 0, 0, getWidth(), getHeight(), null);

                // Draw shape previews (like MS Paint)
                if (currentShape != null && (currentAction.equals("Line") || currentAction.equals("Rectangle") || currentAction.equals("Oval"))) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(currentColor);
                    g2.setStroke(new BasicStroke(strokeThickness));
                    g2.draw(currentShape);
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
        String[] shapeOptions = {"Freehand", "Line", "Rectangle", "Oval", "Text"};
        @SuppressWarnings("unused")
        String[] shapeIconPaths = {"/icons/freehand.png", "/icons/line.png", "/icons/rectangle.png", "/icons/oval.png", "/icons/text.png"};
        for (int i = 0; i < shapeOptions.length; i++) {
            JMenuItem item = new JMenuItem(shapeOptions[i]);
            // Optionally set icons if available
            item.setIcon(new ImageIcon(getClass().getResource(shapeIconPaths[i])));
            item.addActionListener(this);
            editMenu.add(item);
        }

        editMenu.addSeparator();

        // Color icons
        String[] colorNames = {"Black", "Red", "Green", "Blue", "Yellow", "Orange", "Pink", "Cyan", "Magenta", "Gray",
                "Dark Gray", "Light Gray", "White", "Brown", "Purple", "Violet", "Gold", "Silver", "Teal", "Lime"};
        Color[] colors = {Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE, Color.PINK, Color.CYAN,
                Color.MAGENTA, Color.GRAY, Color.DARK_GRAY, Color.LIGHT_GRAY, Color.WHITE, new Color(139, 69, 19),
                new Color(128, 0, 128), new Color(238, 130, 238), new Color(255, 215, 0), new Color(192, 192, 192),
                new Color(0, 128, 128), new Color(50, 205, 50)};

        for (int i = 0; i < colorNames.length; i++) {
            JMenuItem item = new JMenuItem(colorNames[i]);
            item.setIcon(new ColorIcon(colors[i]));
            item.addActionListener(this);
            editMenu.add(item);
        }

        editMenu.addSeparator();

        // Extra tools icons
        String[] toolOptions = {"Erase", "Color Picker", "Fill Color"};
        @SuppressWarnings("unused")
        String[] toolIconPaths = {"/icons/eraser.png", "/icons/colorpicker.png", "/icons/fillcolor.png"};
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
                break;

            case "Black": case "Red": case "Green": case "Blue": case "Yellow": case "Orange": case "Pink":
            case "Cyan": case "Magenta": case "Gray": case "Dark Gray": case "Light Gray": case "White":
            case "Brown": case "Purple": case "Violet": case "Gold": case "Silver": case "Teal": case "Lime":
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
                setCustomCursor(Cursor.HAND_CURSOR); // Pass the int constant
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
        }
    }

    private Color getColorByName(String colorName) {
        switch (colorName) {
            case "Black": return Color.BLACK;
            case "Red": return Color.RED;
            case "Green": return Color.GREEN;
            case "Blue": return Color.BLUE;
            case "Yellow": return Color.YELLOW;
            case "Orange": return Color.ORANGE;
            case "Pink": return Color.PINK;
            case "Cyan": return Color.CYAN;
            case "Magenta": return Color.MAGENTA;
            case "Gray": return Color.GRAY;
            case "Dark Gray": return Color.DARK_GRAY;
            case "Light Gray": return Color.LIGHT_GRAY;
            case "White": return Color.WHITE;
            case "Brown": return new Color(139, 69, 19);
            case "Purple": return new Color(128, 0, 128);
            case "Violet": return new Color(238, 130, 238);
            case "Gold": return new Color(255, 215, 0);
            case "Silver": return new Color(192, 192, 192);
            case "Teal": return new Color(0, 128, 128);
            case "Lime": return new Color(50, 205, 50);
            default: return Color.BLACK;
        }
    }

    private void setCustomCursor(int cursorType) {
        customCursor = Cursor.getPredefinedCursor(cursorType);
        setCursor(customCursor);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        startX = e.getX();
        startY = e.getY();

        if (currentAction.equals("Text")) {
            String text = JOptionPane.showInputDialog("Enter text:");
            if (text != null) {
                g2d.setColor(currentColor); // Ensure text color is set
                g2d.setFont(new Font("Arial", Font.PLAIN, 20));
                g2d.drawString(text, startX, startY);
                repaint();
            }
        }

        // Start the erasing process
        if (currentAction.equals("Erase")) {
            eraseAt(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        endX = e.getX();
        endY = e.getY();

        switch (currentAction) {
            case "Freehand":
                // Nothing needed here as freehand is handled in mouseDragged
                break;

            case "Line":
                g2d.setStroke(new BasicStroke(strokeThickness));
                g2d.drawLine(startX, startY, endX, endY);
                currentShape = null;
                repaint();
                break;

            case "Rectangle":
                int rectX = Math.min(startX, endX);
                int rectY = Math.min(startY, endY);
                int rectWidth = Math.abs(endX - startX);
                int rectHeight = Math.abs(endY - startY);
                g2d.setStroke(new BasicStroke(strokeThickness));
                g2d.drawRect(rectX, rectY, rectWidth, rectHeight);
                currentShape = null;
                repaint();
                break;

            case "Oval":
                int ovalX = Math.min(startX, endX);
                int ovalY = Math.min(startY, endY);
                int ovalWidth = Math.abs(endX - startX);
                int ovalHeight = Math.abs(endY - startY);
                g2d.setStroke(new BasicStroke(strokeThickness));
                g2d.drawOval(ovalX, ovalY, ovalWidth, ovalHeight);
                currentShape = null;
                repaint();
                break;

            case "Fill Color":
                fillShape(e.getX(), e.getY());
                break;

            case "Erase":
                // Erasing is handled in mouseDragged
                break;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        endX = e.getX();
        endY = e.getY();

        switch (currentAction) {
            case "Freehand":
                g2d.setStroke(new BasicStroke(strokeThickness));
                g2d.drawLine(startX, startY, endX, endY);
                startX = endX;
                startY = endY;
                break;

            case "Erase":
                eraseAt(e.getX(), e.getY());
                break;

            case "Line":
            case "Rectangle":
            case "Oval":
                // Update the current shape for live preview
                updateCurrentShape();
                break;
        }

        repaint();
    }

    private void updateCurrentShape() {
        switch (currentAction) {
            case "Line":
                currentShape = new java.awt.geom.Line2D.Float(startX, startY, endX, endY);
                break;

            case "Rectangle":
                int rectX = Math.min(startX, endX);
                int rectY = Math.min(startY, endY);
                int rectWidth = Math.abs(endX - startX);
                int rectHeight = Math.abs(endY - startY);
                currentShape = new Rectangle(rectX, rectY, rectWidth, rectHeight);
                break;

            case "Oval":
                int ovalX = Math.min(startX, endX);
                int ovalY = Math.min(startY, endY);
                int ovalWidth = Math.abs(endX - startX);
                int ovalHeight = Math.abs(endY - startY);
                currentShape = new java.awt.geom.Ellipse2D.Float(ovalX, ovalY, ovalWidth, ovalHeight);
                break;
        }
    }

    private void eraseAt(int x, int y) {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(x - eraserSize / 2, y - eraserSize / 2, eraserSize, eraserSize);
    }

    private void fillShape(int x, int y) {
        // Create a temporary image to perform flood fill
        BufferedImage tempImage = new BufferedImage(canvasImage.getWidth(), canvasImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D gTemp = tempImage.createGraphics();
        gTemp.drawImage(canvasImage, 0, 0, null);
        gTemp.dispose();

        // Get the target color at the clicked point
        int targetColor = tempImage.getRGB(x, y);
        int replacementColor = fillColor.getRGB();

        if (targetColor != replacementColor) {
            floodFill(tempImage, x, y, targetColor, replacementColor);
            g2d.drawImage(tempImage, 0, 0, null);
            repaint();
        }
    }

    private void floodFill(BufferedImage img, int x, int y, int targetColor, int replacementColor) {
        if (x < 0 || x >= img.getWidth() || y < 0 || y >= img.getHeight()) return;
        if (targetColor == replacementColor) return;

        ArrayList<Point> stack = new ArrayList<>();
        stack.add(new Point(x, y));

        while (!stack.isEmpty()) {
            Point p = stack.remove(stack.size() - 1);
            int px = p.x;
            int py = p.y;

            if (px < 0 || px >= img.getWidth() || py < 0 || py >= img.getHeight()) continue;
            if (img.getRGB(px, py) != targetColor) continue;

            img.setRGB(px, py, replacementColor);

            stack.add(new Point(px + 1, py));
            stack.add(new Point(px - 1, py));
            stack.add(new Point(px, py + 1));
            stack.add(new Point(px, py - 1));
        }
    }

    private void saveDrawing() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png");
        fileChooser.setFileFilter(filter);

        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // Ensure the file has a .png extension
            if (!file.getName().toLowerCase().endsWith(".png")) {
                file = new File(file.getParentFile(), file.getName() + ".png");
            }
            try {
                ImageIO.write(canvasImage, "png", file);
                JOptionPane.showMessageDialog(this, "Image saved successfully.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to save the image.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void resetDrawing() {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, canvasImage.getWidth(), canvasImage.getHeight());
        g2d.setColor(currentColor);
        g2d.setStroke(new BasicStroke(strokeThickness));
        shapes.clear(); // Clear the list of shapes
        fillColors.clear(); // Clear the list of fill colors
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Optional: Implement shape size demo or other hover effects
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    // Custom color icon class for menu items
    private class ColorIcon implements Icon {
        private final Color color;
        private final int size = 16;

        public ColorIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x, y, size, size);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, size, size);
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EnhancedDrawingApp app = new EnhancedDrawingApp();
            app.pack();
            app.setLocationRelativeTo(null);
            app.setVisible(true);
        });
    }
}
