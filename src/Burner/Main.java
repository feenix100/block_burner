package Burner;

import Block.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.KeyEvent;

public class Main {

    private JFrame frame;
    private GamePanel panel;
    private GameController controller1;
    private GameController controller2;

    public Main() {
        frame = new JFrame("Block Burner - 2 Player");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // Show start menu first
        StartMenu menu = new StartMenu(this::startGame);
        frame.setContentPane(menu);
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    private void startGame() {
        // Initialize game grids and factories
        GameGrid grid1 = new GameGrid(18, 6);
        GameGrid grid2 = new GameGrid(18, 6);
        BlockFactory factory = new BlockFactory();

        // Set up game panel
        panel = new GamePanel();
        panel.setBackground(Color.BLACK);

        // Create controllers
        controller1 = new GameController(
                grid1, factory,
                panel::repaint,
                panel,
                new GameLogic(grid1)
        );
        controller2 = new GameController(
                grid2, factory,
                panel::repaint,
                panel,
                new GameLogic(grid2)
        );
        panel.setControllers(controller1, controller2);

        // Input mappings
        panel.addKeyListener(new InputHandler(
                controller2,
                KeyEvent.VK_LEFT,
                KeyEvent.VK_RIGHT,
                KeyEvent.VK_DOWN,
                KeyEvent.VK_SPACE,
                KeyEvent.VK_UP
        ));
        panel.addKeyListener(new InputHandler(
                controller1,
                KeyEvent.VK_A,
                KeyEvent.VK_D,
                KeyEvent.VK_S,
                KeyEvent.VK_Q,
                KeyEvent.VK_W
        ));

        // Settings menu
        JMenuBar menuBar = new JMenuBar();
        JMenu settingsMenu = new JMenu("Settings");

        menuBar.add(settingsMenu);
        frame.setJMenuBar(menuBar);

        // Switch to game panel
        frame.setContentPane(panel);
        frame.revalidate();
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);
        panel.requestFocusInWindow();

        // Start game loops
        controller1.startGame();
        controller2.startGame();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
