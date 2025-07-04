package Burner;

import Block.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;


/**
 * Coordinates input, game loop, block spawning, clearing, and power‐up activation.
 */
public class GameController implements KeyListener {
    private final GameGrid grid;
    private final BlockFactory blockFactory;
    private final Runnable repaintCallback;
    private final GamePanel gamePanel;
    private final GameLogic gameLogic;

    private Block currentBlock;
    private Block nextBlock;
    private boolean downPressed = false;
    private boolean blockActive = false;
    private double dropAccumulator = 0.0;

    private Timer timer;
    private JFrame gameOverFrame;

    private static volatile boolean globalGameOver = false;

    private static final int FRAME_DELAY = 16;                    // ~60 FPS
    private static final double NORMAL_SPEED = 1.0 / 400.0;        // rows per ms
    private static final double SOFT_DROP_MULT = 8.0;              // soft-drop factor

    // how many seconds since gameStart
    private int elapsedSeconds = 0;
    private Timer gameTimer;
    public GameController(GameGrid grid,
                          BlockFactory blockFactory,
                          Runnable repaintCallback,
                          GamePanel gamePanel,
                          GameLogic gameLogic) {
        this.grid = grid;
        this.blockFactory = blockFactory;
        this.repaintCallback = repaintCallback;
        this.gamePanel = gamePanel;
        this.gameLogic = gameLogic;
        this.gameLogic.setController(this);
    }

    /** so effects can find this controller */
    public GameLogic getGameLogic() {
        return gameLogic;
    }

    /** so effects can find the panel & its other controllers */
    public GamePanel getGamePanel() {
        return gamePanel;
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    /** Initialize and start the game loop. */
    public void startGame() {
        gamePanel.addKeyListener(this);
        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();

        spawnNewBlock();
        blockActive = true;

        timer = new Timer(FRAME_DELAY, e -> gameLoop());
        elapsedSeconds = 0;
        gameTimer = new Timer(1000, e -> {
            elapsedSeconds++;
            // repaint both panels to show updated time
            SwingUtilities.invokeLater(repaintCallback);
        });
        gameTimer.start();

        timer.start();
    }

    /** Core loop: handle timed descent, locking, clears, and spawning. */
    private void gameLoop() {
        if (globalGameOver) {
            timer.stop();
            return;
        }
        // calculate drop amount (soft‐drop vs. normal)
        double speed = NORMAL_SPEED * (downPressed ? SOFT_DROP_MULT : 1.2);
        dropAccumulator += speed * FRAME_DELAY;

        // drop loop
        while (dropAccumulator >= 1.0) {
            if (blockActive && !moveBlockDown()) {
                // lock into grid…
                placeBlockInGrid();

                // …then fire off any Midas effects on newly‐placed cells
                triggerMidasEffects();

                blockActive = false;
                gameLogic.clearAndApplyGravity(repaintCallback);

            }
            dropAccumulator -= 1.0;
        }

        // spawn a new piece if needed
        if (!blockActive) {
            spawnNewBlock();
            blockActive = true;
        }

        // repaint the panel each frame
        repaintCallback.run();
    }


    /** Fetch nextPiece if null and advance current/next blocks. */
    private void spawnNewBlock() {
        if (nextBlock == null) {
            nextBlock = blockFactory.generateBlock();

        }
        currentBlock = nextBlock;
        currentBlock.setPosition(grid.getColumns()/2 - 1, 0);
        nextBlock = blockFactory.generateBlock();

    }

    public void addMidasBlock() {
        // Always take row 0, 1, and 2 from column 3 (size 48px each)
        SmallBlock[] cells = new SmallBlock[] {
                new MidasBlock(0),  // col 3, row 0 → top
                new MidasBlock(1),  // col 3, row 1 → middle
                new MidasBlock(2)   // col 3, row 2 → bottom
        };
        Block midas = new Block(cells);

        // Override next piece with our fixed‐order midas bar
        this.nextBlock = midas;

        // Repaint preview immediately
        repaintCallback.run();
    }

    public void addMedusaBlock() {
        SmallBlock[] cells = new SmallBlock[] {
                new MedusaBlock(0),  // col 3, row 3 → top
                new MedusaBlock(1),  // col 3, row 4 → middle
                new MedusaBlock(2)   // col 2, row 5 → bottom
        };
        Block medusa = new Block(cells);
        this.nextBlock = medusa;
        repaintCallback.run();
    }

    private void triggerMidasEffects() {
        int[][] positions = currentBlock.getPositions();
        for (int[] pos : positions) {
            int r = currentBlock.getY() + pos[0];
            int c = currentBlock.getX() + pos[1];
            SmallBlock sb = grid.getCell(r, c);
            if (sb instanceof MidasBlock) {
                ((MidasBlock) sb).activateMidasEffect(grid, r, c);
            }
        }
    }

    /** Try to move down; return false if blocked. */
    private boolean moveBlockDown() {
        if (currentBlock == null) return false;
        if (canMoveDown()) {
            currentBlock.moveDown();
            return true;
        }
        // lock and clear if can't move
        placeBlockInGrid();
        return false;
    }

    /** Hard drop to bottom on 'D'. */
    public void drop() {
        while (moveBlockDown()) {}
        blockActive = false;

        gameLogic.applyGravityWithDelay(() -> {
            blockActive = true;
        });
    }

    /** Activate stored PowerUpBlock on SPACE. */
    public void activatePowerUp() {
        PowerUpBlock pu = gameLogic.getPowerUpInventory().use();
        if (pu != null) {
            pu.activate(gameLogic);
            repaintCallback.run();
        }
    }

    /** Move current block left if possible. */
    public void moveBlockLeft() {
        if (blockActive && canMoveLeft()) {
            currentBlock.moveLeft();
            repaintCallback.run();
        }
    }

    /** Move current block right if possible. */
    public void moveBlockRight() {
        if (blockActive && canMoveRight()) {
            currentBlock.moveRight();
            repaintCallback.run();
        }
    }

    /** Place each SmallBlock into the grid, then check game-over. */
    private void placeBlockInGrid() {
        int[][] positions = currentBlock.getPositions();
        SmallBlock[] cells   = currentBlock.getSmallBlocks();

        for (int i = 0; i < positions.length; i++) {
            int row = currentBlock.getY() + positions[i][0];
            int col = currentBlock.getX() + positions[i][1];
            if (!grid.isOutOfBounds(row, col)) {
                SmallBlock cell = cells[i];
                grid.setCell(row, col, cell);

                // → right here: if it's a MidasBlock, trigger its effect
                if (cell instanceof MidasBlock) {
                    ((MidasBlock)cell).activateMidasEffect(grid, row, col);
                }
                if (cell instanceof MedusaBlock) {
                    ((MedusaBlock)cell).activateMedusaEffect(grid, row, col);
                }
            }
        }

        checkGameOver();
    }

    private boolean canMoveDown() {
        for (int[] pos : currentBlock.getPositions()) {
            int row = currentBlock.getY() + pos[0] + 1;
            int col = currentBlock.getX() + pos[1];
            if (grid.isOutOfBounds(row, col) || !grid.isCellEmpty(row, col)) {
                return false;
            }
        }
        return true;
    }

    private boolean canMoveLeft() {
        for (int[] pos : currentBlock.getPositions()) {
            int row = currentBlock.getY() + pos[0];
            int col = currentBlock.getX() + pos[1] - 1;
            if (grid.isOutOfBounds(row, col) || !grid.isCellEmpty(row, col)) {
                return false;
            }
        }
        return true;
    }

    private boolean canMoveRight() {
        for (int[] pos : currentBlock.getPositions()) {
            int row = currentBlock.getY() + pos[0];
            int col = currentBlock.getX() + pos[1] + 1;
            if (grid.isOutOfBounds(row, col) || !grid.isCellEmpty(row, col)) {
                return false;
            }
        }
        return true;
    }

    public GameGrid getGrid()      { return grid;         }
    public Block getCurrentBlock() { return currentBlock; }
    public Block getNextBlock()    { return nextBlock;    }

    /** Expose inventory for rendering in GamePanel */
    public PowerUpInventory getPowerUpInventory() { return gameLogic.getPowerUpInventory(); }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            downPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            downPressed = false;
        }
    }

    @Override public void keyTyped(KeyEvent e) {}

    /** Show game-over dialog and offer restart or exit. */
    public void checkGameOver() {
        for (int col = 0; col < grid.getColumns(); col++) {
            if (!grid.isCellEmpty(0, col)) {
                gameOver();
                return;
            }
        }
    }

    private void gameOver() {
        // Prevent running twice
        if (globalGameOver) return;
        globalGameOver = true;
        gameTimer.stop();
        if (timer != null) timer.stop();
        SwingUtilities.invokeLater(() -> {
            // If somehow a frame already exists and is visible, bail out
            if (gameOverFrame != null && gameOverFrame.isShowing()) {
                return;
            }
            gameOverFrame = new JFrame("Game Over");
            gameOverFrame.setSize(800, 1050);
            gameOverFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            try {
                BufferedImage bgImg = ImageIO.read(
                        getClass().getResource("/sprites/endscreen.png")
                );
                JLabel background = new JLabel(new ImageIcon(bgImg));
                background.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
                JButton exitBtn = new JButton("Exit");
                exitBtn.addActionListener(e -> System.exit(0));
                background.add(exitBtn);
                gameOverFrame.setContentPane(background);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                JPanel fallback = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
                fallback.add(new JLabel("Game Over!"));
                JButton exitBtn = new JButton("Exit");
                exitBtn.addActionListener(e -> System.exit(0));
                fallback.add(exitBtn);
                gameOverFrame.setContentPane(fallback);
            }

            gameOverFrame.setLocationRelativeTo(null);
            gameOverFrame.setVisible(true);
        });
    }
    private void restartGame() {
        if (timer != null) timer.stop();
        if (gameTimer != null) gameTimer.stop();
        grid.clearAll();
        nextBlock = null;
        startGame();
    }
}
