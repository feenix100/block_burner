package Burner;

import Block.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class GamePanel extends JPanel {
    private static final int GRID_ROWS       = 18;
    private static final int GRID_COLS       = 6;
    private static final int PREVIEW_COLS    = 3;
    private static final int SPACING_Y       = 10;
    private static final int GAP             = 15;
    private static final int DIVIDER_STROKE  = 4;

    private GameController controller1;
    private GameController controller2;

    // new fields for global offset
    private int globalOffsetX = 50;
    private int globalOffsetY = 50;

    // Adjust the X offset of all drawing.
    public void setGlobalOffsetX(int x) {
        this.globalOffsetX = x;
        repaint();
    }
    // Adjust the Y offset of all drawing.
    public void setGlobalOffsetY(int y) {
        this.globalOffsetY = y;
        repaint();
    }
    public int getGlobalOffsetX() { return globalOffsetX; }
    public int getGlobalOffsetY() { return globalOffsetY; }


    public GamePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
    }

    public void setControllers(GameController c1, GameController c2) {
        this.controller1 = c1;
        this.controller2 = c2;

        int totalCols = PREVIEW_COLS + GRID_COLS + GRID_COLS + PREVIEW_COLS;
        int width     = totalCols * SmallBlock.SIZE;
        int height    = GRID_ROWS * SmallBlock.SIZE;
        setPreferredSize(new Dimension(width, height));
    }

    // Allow power-ups to target all controllers.
    public List<GameController> getControllers() {
        // Java 9+: you could also use List.of(controller1, controller2);
        return Arrays.asList(controller1, controller2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // shift everything by the current offsets
        Graphics2D g2 = (Graphics2D) g;
        g2.translate(globalOffsetX, globalOffsetY);

        int secs = controller1.getElapsedSeconds();
        String mmss = String.format("%02d:%02d", secs/60, secs%60);
        if (controller1 == null || controller2 == null) return;

        int pxPreview  = 0;
        int pxGrid1    = pxPreview + PREVIEW_COLS * SmallBlock.SIZE;
        int pxDivider  = pxGrid1   + GRID_COLS   * SmallBlock.SIZE;
        int pxGrid2    = pxDivider;
        int pxPreview2 = pxGrid2   + GRID_COLS   * SmallBlock.SIZE;

        int rightOfGrids = pxPreview2 + PREVIEW_COLS * SmallBlock.SIZE + GAP;

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString("Time: " + mmss, rightOfGrids, 20);

        // Player 1 UI
        drawPreview(controller1, g, pxPreview);
        drawStoredPowerUps(controller1, g, pxPreview);
        drawGrid(controller1.getGrid(), g, pxGrid1);
        drawActive(controller1, g, pxGrid1);

        // Divider
        drawDivider(g, pxDivider);

        // Player 2 UI
        drawGrid(controller2.getGrid(), g, pxGrid2);
        drawActive(controller2, g, pxGrid2);
        drawPreview(controller2, g, pxPreview2);
        drawStoredPowerUps(controller2, g, pxPreview2);
    }

    private void drawDivider(Graphics g, int x) {
        Graphics2D g2      = (Graphics2D) g;
        int height         = GRID_ROWS * SmallBlock.SIZE;
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(DIVIDER_STROKE));
        g2.drawLine(x, 0, x, height);
    }

    private void drawPreview(GameController controller, Graphics g, int offsetX) {
        GameGrid grid      = controller.getGrid();
        int panelW         = SmallBlock.SIZE * PREVIEW_COLS;
        int panelH         = grid.getRows() * SmallBlock.SIZE;

        // background + border
        g.setColor(Color.BLACK);
        g.fillRect(offsetX, 0, panelW, panelH);
        g.setColor(Color.WHITE);
        g.drawRect(offsetX, 0, panelW - 1, panelH - 1);

        Block next = controller.getNextBlock();
        if (next == null) return;

        int[][]     shape = next.getPositions();
        SmallBlock[] cells = next.getSmallBlocks();
        int baseX       = offsetX + (panelW - SmallBlock.SIZE) / 2;
        int baseY       = (PREVIEW_COLS * SmallBlock.SIZE - SmallBlock.SIZE * cells.length) / 2;

        for (int i = 0; i < cells.length; i++) {
            int px = baseX + shape[i][1] * SmallBlock.SIZE;
            int py = baseY + shape[i][0] * SmallBlock.SIZE;
            drawSmallBlock(g, cells[i], px, py);
        }
    }

    private void drawStoredPowerUps(GameController controller, Graphics g, int offsetX) {
        GameGrid grid      = controller.getGrid();
        PowerUpInventory inv = controller.getPowerUpInventory();
        List<PowerUpBlock> stored = inv.getStored();

        int panelW = SmallBlock.SIZE * PREVIEW_COLS;
        int invX    = offsetX;
        int invY    = SmallBlock.SIZE * PREVIEW_COLS + SPACING_Y;
        int invH    = grid.getRows() * SmallBlock.SIZE - invY;

        // background + border aligned to bottom of grid
        g.setColor(Color.BLACK);
        g.fillRect(invX, invY, panelW, invH);
        g.setColor(Color.WHITE);
        g.drawRect(invX, invY, panelW - 1, invH - 1);

        g.drawString("Power-Ups", invX + GAP, invY + GAP + g.getFontMetrics().getAscent());

        for (int i = 0; i < stored.size(); i++) {
            PowerUpBlock pu = stored.get(i);
            BufferedImage img = pu.getSprite();
            int x = invX + (panelW - SmallBlock.SIZE) / 2;
            int y = invY + GAP + (i + 1) * (SmallBlock.SIZE + GAP);
            g.drawImage(img, x, y, SmallBlock.SIZE, SmallBlock.SIZE, null);
        }
    }

    private void drawGrid(GameGrid grid, Graphics g, int offsetX) {
        for (int r = 0; r < grid.getRows(); r++) {
            for (int c = 0; c < grid.getColumns(); c++) {
                int x = offsetX + c * SmallBlock.SIZE;
                int y = r * SmallBlock.SIZE;
                SmallBlock cell = grid.getCell(r, c);
                if (cell != null) drawSmallBlock(g, cell, x, y);
                else {
                    g.setColor(Color.GRAY);
                    g.drawRect(x, y, SmallBlock.SIZE, SmallBlock.SIZE);
                }
            }
        }
    }

    private void drawActive(GameController controller, Graphics g, int offsetX) {
        Block active = controller.getCurrentBlock();
        if (active == null) return;
        int[][] shape = active.getPositions();
        SmallBlock[] cells = active.getSmallBlocks();
        for (int i = 0; i < cells.length; i++) {
            int x = offsetX + (active.getX() + shape[i][1]) * SmallBlock.SIZE;
            int y = (active.getY() + shape[i][0]) * SmallBlock.SIZE;
            drawSmallBlock(g, cells[i], x, y);
        }
    }

    private void drawSmallBlock(Graphics g, SmallBlock cell, int x, int y) {
        g.drawImage(
                cell.getSprite(), x, y,
                cell.getSize(), cell.getSize(),
                null
        );
    }
}