package Burner;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class StartMenu extends JPanel {
    private BufferedImage bgImage;

    public interface MenuListener {
        void onStart();
    }

    public StartMenu(MenuListener listener) {
        // load the background image from your resources folder
        try {
            // put start_bg.png in src/main/resources/Burner/ or adjust path
            bgImage = ImageIO.read(getClass().getResourceAsStream("/sprites/BlockBurnerBackground.png"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Failed to load background image: " + e.getMessage());
        }

        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(1024, 768));
        setOpaque(true);
        setBackground(Color.BLACK);

        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.BOLD, 24));
        startButton.addActionListener(e -> listener.onStart());
        // Optional: make the button blend nicer over an image
        startButton.setContentAreaFilled(false);
        startButton.setOpaque(true);
        startButton.setBackground(new Color(120, 150, 150, 150)); // semi-transparent black

        add(startButton);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) {
            int imgW = bgImage.getWidth(this);
            int imgH = bgImage.getHeight(this);
            int panelW = getWidth();
            int panelH = getHeight();

            // scale factor to fit, preserving aspect ratio
            double scale = Math.min((double)panelW / imgW,
                    (double)panelH / imgH);

            int drawW = (int)(imgW * scale);
            int drawH = (int)(imgH * scale);
            int x = (panelW - drawW) / 2;
            int y = (panelH - drawH) / 2;

            g.drawImage(bgImage, x, y, drawW, drawH, this);
        }
    }
}
