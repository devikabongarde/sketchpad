import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import javax.swing.filechooser.FileNameExtensionFilter;

public class EnhancedDrawingApp extends JFrame implements ActionListener, MouseListener, MouseMotionListener {
    private String currentAction = "Freehand";
    private Color currentColor = Color.BLACK;
    private int startX, startY, endX, endY;
    private BufferedImage canvasImage;
    private Graphics2D g2d;

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

        JPanel canvasPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(canvasImage, 0, 0, getWidth(), getHeight(), null);
            }
        };
        canvasPanel.setPreferredSize(new Dimension(1920, 900));
        canvasPanel.addMouseListener(this);
        canvasPanel.addMouseMotionListener(this);

        // Create Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu editMenu = new JMenu("Edit");

        // Shape icons
        String[] shapeOptions = {"Freehand", "Line", "Rectangle", "Oval", "Text"};
        String[] shapeIconPaths = {"/icons/freehand.png", "/icons/line.png", "/icons/rectangle.png", "/icons/oval.png", "/icons/text.png"};
        for (int i = 0; i < shapeOptions.length; i++) {
            JMenuItem item = new JMenuItem(shapeOptions[i], new ImageIcon(getClass().getResource(shapeIconPaths[i])));
            item.addActionListener(this);
            editMenu.add(item);
        }

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

        // Extra tools icons
        String[] toolOptions = {"Erase", "Color Picker"};
        String[] toolIconPaths = {"/icons/eraser.png", "/icons/colorpicker.png"};
        for (int i = 0; i < toolOptions.length; i++) {
            JMenuItem item = new JMenuItem(toolOptions[i], new ImageIcon(getClass().getResource(toolIconPaths[i])));
            item.addActionListener(this);
            editMenu.add(item);
        }

        menuBar.add(editMenu);
        setJMenuBar(menuBar);

        // Bottom panel for Save and Reset buttons
        JPanel bottomPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(this);
        bottomPanel.add(saveButton);
        bottomPanel.add(resetButton);

        add(canvasPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
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
                break;

            case "Color Picker":
                currentColor = JColorChooser.showDialog(this, "Choose Color", currentColor);
                g2d.setColor(currentColor);
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

    @Override
    public void mousePressed(MouseEvent e) {
        startX = e.getX();
        startY = e.getY();

        if (currentAction.equals("Text")) {
            String text = JOptionPane.showInputDialog("Enter text:");
            if (text != null) {
                g2d.drawString(text, startX, startY);
                repaint();
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        endX = e.getX();
        endY = e.getY();

        switch (currentAction) {
            case "Line":
                g2d.drawLine(startX, startY, endX, endY);
                break;
            case "Rectangle":
                g2d.drawRect(Math.min(startX, endX), Math.min(startY, endY),
                        Math.abs(endX - startX), Math.abs(endY - startY));
                g2d.fillRect(Math.min(startX, endX), Math.min(startY, endY),
                        Math.abs(endX - startX), Math.abs(endY - startY));
                break;
            case "Oval":
                g2d.drawOval(Math.min(startX, endX), Math.min(startY, endY),
                        Math.abs(endX - startX), Math.abs(endY - startY));
                g2d.fillOval(Math.min(startX, endX), Math.min(startY, endY),
                        Math.abs(endX - startX), Math.abs(endY - startY));
                break;
            case "Erase":
                g2d.setColor(Color.WHITE);
                g2d.fillRect(startX, startY, 10, 10);
                g2d.setColor(currentColor);
                break;
        }
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (currentAction.equals("Freehand")) {
            endX = e.getX();
            endY = e.getY();
            g2d.drawLine(startX, startY, endX, endY);
            startX = endX;
            startY = endY;
            repaint();
        }
    }

    private void saveDrawing() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Image");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                ImageIO.write(canvasImage, "png", new File(fileToSave.getAbsolutePath() + ".png"));
                JOptionPane.showMessageDialog(this, "Drawing saved successfully.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void resetDrawing() {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, canvasImage.getWidth(), canvasImage.getHeight());
        g2d.setColor(currentColor);
        repaint();
    }

    // Unused mouse event handlers
    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EnhancedDrawingApp app = new EnhancedDrawingApp();
            app.setVisible(true);
        });
    }

    // Helper class to create a solid color icon
    class ColorIcon implements Icon {
        private final int SIZE = 16;
        private Color color;

        public ColorIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x, y, SIZE, SIZE);
        }

        @Override
        public int getIconWidth() {
            return SIZE;
        }

        @Override
        public int getIconHeight() {
            return SIZE;
        }
    }
}
