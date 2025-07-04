package Burner;

import Block.*;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

/**
 * Handles block placement, gravity, matching-clears (including MidasBlock effects),
 * and banking power-ups.
 */
public class GameLogic {
    private final GameGrid grid;
    private static final int FALL_DELAY_MS = 200;

    private int penaltySpawnsRemaining = 0;

    // Inventory to store cleared power-up blocks
    private final PowerUpInventory powerUpInventory = new PowerUpInventory();

    private GameController controller;

    public void setController(GameController controller) {
        this.controller = controller;
    }
    public GameController getController() {
        return controller;
    }

    public GameLogic(GameGrid grid) {
        this.grid = grid;
    }

    // Expose the banked power-ups for rendering or activation.
    public PowerUpInventory getPowerUpInventory() {
        return powerUpInventory;
    }

    public GameGrid getGrid() {
        return grid;
    }
    // Animate gravity with a delay, then trigger clear-and-gravity loops.

    public void applyGravityWithDelay(Runnable repaint) {
        Timer timer = new Timer(FALL_DELAY_MS, null);
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean moved = false;
                for (int col = 0; col < grid.getColumns(); col++) {
                    for (int row = grid.getRows() - 2; row >= 0; row--) {
                        SmallBlock sb = grid.getCell(row, col);
                        if (sb != null && grid.isCellEmpty(row + 1, col)) {
                            grid.setCell(row + 1, col, sb);
                            grid.setCell(row, col, null);
                            moved = true;
                        }
                    }
                }
                if (moved) {
                    repaint.run();
                } else {
                    timer.stop();
                    if (clearAndApplyGravity(repaint)) {
                        applyGravityWithDelay(repaint);
                    }
                }
            }
        });
        timer.start();
    }

    /**
     * Clears matching groups and reapplies gravity until no more clears.
     * @return true if any blocks were cleared this cycle
     */
    public boolean clearAndApplyGravity(Runnable repaint) {
        boolean anyCleared = false;
        while (clearGroups()) {
            anyCleared = true;
            applyGravityWithDelay(repaint);
        }
        if (anyCleared && penaltySpawnsRemaining > 0) {
            // consume one budget and send penalties now
            spawnPenaltyBlocksOnOpponents();
            penaltySpawnsRemaining--;
        }
        return anyCleared;
    }

    /**
     * Detects and clears matching groups of 3 or more,
     * applies MidasBlock effects, and banks any PowerUpBlocks.
     * @return true if any group was cleared
     */
    private boolean clearGroups() {
        int rows = grid.getRows();
        int cols = grid.getColumns();
        boolean[][] toClear = new boolean[rows][cols];
        boolean found = false;

        // Mark all matches
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (markMatches(r, c, toClear)) {
                    found = true;
                }
            }
        }

        // If any matches, process clearing
        if (found) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (toClear[r][c]) {
                        SmallBlock sb = grid.getCell(r, c);
                        if (sb instanceof MidasBlock) {
                            // Goldify all neighbors before clearing
                            ((MidasBlock) sb).activateMidasEffect(grid, r, c);
                        } else if (sb instanceof PowerUpBlock) {
                            // Bank other power-ups
                            powerUpInventory.add((PowerUpBlock) sb);
                        }
                        // Clear this cell
                        grid.setCell(r, c, null);
                    }
                }
            }
        }
        return found;
    }

    /**
     * Checks for a 3-match starting at (r,c) in all directions
     * and marks toClear accordingly.
     */
    private boolean markMatches(int r, int c, boolean[][] toClear) {
        SmallBlock sb = grid.getCell(r, c);
        if (sb == null || sb instanceof MedusaBlock) return false;
        int type = sb.getTypeIndex();
        int rows = grid.getRows();
        int cols = grid.getColumns();
        boolean match = false;

        // Horizontal
        if (c <= cols - 3 && sameType(r, c + 1, type) && sameType(r, c + 2, type)) {
            toClear[r][c] = toClear[r][c + 1] = toClear[r][c + 2] = true;
            match = true;
        }
        // Vertical
        if (r <= rows - 3 && sameType(r + 1, c, type) && sameType(r + 2, c, type)) {
            toClear[r][c] = toClear[r + 1][c] = toClear[r + 2][c] = true;
            match = true;
        }
        // Diagonal down-right
        if (r <= rows - 3 && c <= cols - 3
                && sameType(r + 1, c + 1, type) && sameType(r + 2, c + 2, type)) {
            toClear[r][c] = toClear[r + 1][c + 1] = toClear[r + 2][c + 2] = true;
            match = true;
        }
        // Diagonal down-left
        if (r <= rows - 3 && c >= 2
                && sameType(r + 1, c - 1, type) && sameType(r + 2, c - 2, type)) {
            toClear[r][c] = toClear[r + 1][c - 1] = toClear[r + 2][c - 2] = true;
            match = true;
        }
        // Special patterns: columns 0-5 carry either {0,1,2,5,4,3} or {0,1,2,3,4,5}
        if (c == 0 && cols >= 6) {
            int[][] patterns = {
                    {0, 1, 5, 3, 4, 2},
                    {0, 1, 2, 3, 4, 5}
            };
            for (int[] pattern : patterns) {
                boolean sequence = true;
                for (int col = 0; col < 6; col++) {
                    SmallBlock cell = grid.getCell(r, col);
                    if (cell == null || cell instanceof MedusaBlock || cell.getTypeIndex() != pattern[col]) {
                        sequence = false;
                        break;
                    }
                }
                if (sequence) {
                    // Mark all six columns in this row for clearing
                    for (int col = 0; col < 6; col++) {
                        toClear[r][col] = true;
                    }
                    match = true;
                    // Trigger penalty on opponents
                    // Start penalty effect: immediate and next 10 spawns
                    penaltySpawnsRemaining = 10;
                    spawnPenaltyBlocksOnOpponents();
                    break;
                }
            }
        }

        //special pattern - diagonal pattern
        if (r == 17 && c == 0 && rows >= 18 && cols >= 6) {
            boolean diagonalPattern = true;
            for (int i = 0; i < 6; i++) {
                SmallBlock cell = grid.getCell(17 - i, i);
                if (cell == null
                        || cell instanceof MedusaBlock
                        || cell.getTypeIndex() != i) {
                    diagonalPattern = false;
                    break;
                }
            }
            if (diagonalPattern) {
                // mark all six blocks for clearing
                for (int i = 0; i < 6; i++) {
                    toClear[17 - i][i] = true;
                }
                match = true;
            }
        }
        return match;
    }
    // After detecting your special pattern, call this to spawn two “attack” blocks on each opponent.
    private void spawnPenaltyBlocksOnOpponents() {
        GameController self  = controller;
        GamePanel      panel = self.getGamePanel();
        Random         rnd   = new Random();

        for (GameController opponent : panel.getControllers()) {
            if (opponent == self) continue;

            GameGrid oppGrid = opponent.getGrid();
            int      rows    = oppGrid.getRows();
            int      cols    = oppGrid.getColumns();

            // 1) Find the topmost occupied row (global)
            int globalTop = -1;
            outer:
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (!oppGrid.isCellEmpty(r, c)) {
                        globalTop = r;
                        break outer;
                    }
                }
            }

            // 2) Compute spawnRow:
            //    - if there's at least one block, two rows above that
            //    - if grid is empty (globalTop == -1), two rows above bottom
            int spawnRow;
            if (globalTop >= 0) {
                spawnRow = globalTop - 2;
            } else {
                spawnRow = rows - 3;
            }
            // clamp so we never go above the top
            spawnRow = Math.max(0, spawnRow);

            // 3) Pick two distinct random columns
            Set<Integer> picks = new HashSet<>();
            while (picks.size() < 2) {
                picks.add(rnd.nextInt(cols));
            }

            // 4) Place one block in each picked column at spawnRow (or highest free cell above it)
            for (int c : picks) {
                int targetRow = spawnRow;
                while (targetRow > 0 && !oppGrid.isCellEmpty(targetRow, c)) {
                    targetRow--;
                }
                if (oppGrid.isCellEmpty(targetRow, c)) {
                    SmallBlock penalty = new SmallBlock(rnd.nextInt(6));
                    oppGrid.setCell(targetRow, c, penalty);
                }
            }

            // 5) Trigger gravity so they start falling
            opponent.getGameLogic().applyGravityWithDelay(panel::repaint);
        }
    }
    public void notifyBlockSpawned() {
        if (penaltySpawnsRemaining > 0) {
            spawnPenaltyBlocksOnOpponents();
            penaltySpawnsRemaining--;
        }
    }
    // Utility to compare typeIndex for matching.

    private boolean sameType(int r, int c, int typeIndex) {
        SmallBlock cell = grid.getCell(r, c);
        return cell != null && !(cell instanceof MedusaBlock) && cell.getTypeIndex() == typeIndex;
    }
}
