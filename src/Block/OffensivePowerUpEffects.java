package Block;

import Burner.GameController;
import Burner.GameGrid;
import Burner.GameLogic;
import Burner.GamePanel;

import java.awt.Point;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

public class OffensivePowerUpEffects {

    public static void doOffensiveOne(PowerUpBlock block, GameLogic logic) {
        GameController self = logic.getController();
        GamePanel panel    = self.getGamePanel();
        Random random      = new Random();

        for (GameController opponent : panel.getControllers()) {
            if (opponent == self) continue;

            GameGrid grid = opponent.getGrid();
            int rows = grid.getRows();
            int cols = grid.getColumns();

            // 1) Shift every existing block up one row (top row is discarded)
            for (int r = 0; r < rows - 1; r++) {
                for (int c = 0; c < cols; c++) {
                    // Move block from row+1 to current row (may be null)
                    SmallBlock above = grid.getCell(r + 1, c);
                    grid.setCell(r, c, above);
                }
            }

            // 2) Fill the bottom row with new random-type blocks
            int bottom = rows - 1;
            for (int c = 0; c < cols; c++) {
                int randomType = random.nextInt(cols);  // assuming types = number of columns
                SmallBlock newBlock = new SmallBlock(randomType);
                grid.setCell(bottom, c, newBlock);
            }

            // 3) Repaint to reflect updates immediately
            panel.repaint();
        }
}

    public static void doOffensiveTwo(PowerUpBlock block, GameLogic logic) {
        GameController self = logic.getController();
        GamePanel panel = self.getGamePanel();
        Random random = new Random();

        for (GameController opponent : panel.getControllers()) {
            if (opponent == self) continue;

            GameGrid grid = opponent.getGrid();
            int rows = grid.getRows();
            int cols = grid.getColumns();

            // Collect all non-empty (locked) block positions
            List<Point> positions = new ArrayList<>();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    SmallBlock sb = grid.getCell(r, c);
                    if (sb != null) {
                        positions.add(new Point(r, c));
                    }
                }
            }
            if (positions.size() < 3) {
                panel.repaint();
                continue;
            }

            // Shuffle and pick three
            Collections.shuffle(positions, random);
            Point p1 = positions.get(0);
            Point p2 = positions.get(1);
            Point p3 = positions.get(2);

            SmallBlock b1 = grid.getCell(p1.x, p1.y);
            SmallBlock b2 = grid.getCell(p2.x, p2.y);
            SmallBlock b3 = grid.getCell(p3.x, p3.y);

            List<SmallBlock> blocks = Arrays.asList(b1, b2, b3);

            // Determine best rotation to minimize adjacent same-type blocks
            int[][] mappings = {{1, 2, 0}, {2, 0, 1}};
            int bestMap = 0;
            int bestCollisions = Integer.MAX_VALUE;
            for (int m = 0; m < mappings.length; m++) {
                int collisions = 0;
                for (int i = 0; i < 3; i++) {
                    SmallBlock blk = blocks.get(mappings[m][i]);
                    int type = blk.getTypeIndex();
                    Point pos = positions.get(i);
                    int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                    for (int[] d : dirs) {
                        int nr = pos.x + d[0], nc = pos.y + d[1];
                        if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                            SmallBlock neighbor = grid.getCell(nr, nc);
                            if (neighbor != null && neighbor.getTypeIndex() == type) {
                                collisions++;
                            }
                        }
                    }
                }
                if (collisions < bestCollisions) {
                    bestCollisions = collisions;
                    bestMap = m;
                }
            }

            // Apply swap
            for (int i = 0; i < 3; i++) {
                Point pos = positions.get(i);
                grid.setCell(pos.x, pos.y, blocks.get(mappings[bestMap][i]));
            }

            panel.repaint();
        }
    }

    public static void doOffensiveThree(PowerUpBlock block, GameLogic logic) {
        GameController self  = logic.getController();
        GamePanel      panel = self.getGamePanel();
        Random         random = new Random();

        for (GameController opponent : panel.getControllers()) {
            if (opponent == self) continue;

            GameGrid grid = opponent.getGrid();
            int rows = grid.getRows();
            int cols = grid.getColumns();

            // 1) Gather all non-empty (locked) cells
            List<Point> locked = new ArrayList<>();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (grid.getCell(r, c) != null) {
                        locked.add(new Point(r, c));
                    }
                }
            }

            // nothing to do if grid is empty
            if (locked.isEmpty()) {
                panel.repaint();
                continue;
            }

            // 2) Pick one at random and place a MedusaBlock there
            Point target = locked.get(random.nextInt(locked.size()));
            int   r      = target.x;
            int   c      = target.y;

            // choose one of the three Medusa-sprite variants at random
            MedusaBlock medusa = new MedusaBlock(random.nextInt(3));
            grid.setCell(r, c, medusa);

            // 3) Immediately apply its effect (turn neighbors into MedusaBlocks, then remove it)
            medusa.activateMedusaEffect(grid, r, c);

            // 4) Repaint so opponent sees the transformation
            panel.repaint();
        }
    }

    public static void doOffensiveFour(PowerUpBlock block, GameLogic logic) {
        // Get our controller & shared panel
        GameController self  = logic.getController();
        GamePanel      panel = self.getGamePanel();

        // For each opponent, override their nextBlock to be the Medusa bar
        for (GameController opponent : panel.getControllers()) {
            if (opponent == self) continue;
            opponent.addMedusaBlock();
        }
    }

    public static void doOffensiveFive(PowerUpBlock block, GameLogic logic) {
        // Get our controller and the shared panel
        GameController self  = logic.getController();
        GamePanel      panel = self.getGamePanel();

        // For each opponent, remove one stored power-up (if any)
        for (GameController opponent : panel.getControllers()) {
            if (opponent == self) continue;
            opponent.getPowerUpInventory().use();
        }

        // Refresh all boards so opponents see their inventory shrink
        panel.repaint();
    }

    public static void doOffensiveSix(PowerUpBlock block, GameLogic logic) {
        GameController self  = logic.getController();
        GamePanel      panel = self.getGamePanel();
        Random         rnd   = new Random();

        for (GameController opponent : panel.getControllers()) {
            if (opponent == self) continue;

            GameGrid grid = opponent.getGrid();
            int cols     = grid.getColumns();
            int rows     = grid.getRows();

            // pick two distinct random columns
            int c1 = rnd.nextInt(cols);
            int c2;
            do {
                c2 = rnd.nextInt(cols);
            } while (c2 == c1);

            for (int c : new int[] { c1, c2 }) {
                // find the first non-empty (locked) cell from the top
                int topLockedRow = -1;
                for (int r = 0; r < rows; r++) {
                    if (!grid.isCellEmpty(r, c)) {
                        topLockedRow = r;
                        break;
                    }
                }

                // compute spawn position two cells above that
                int spawnRow;
                if (topLockedRow == -1) {
                    // no locked blocks, so 2 above bottom row
                    spawnRow = rows - 3;
                } else {
                    spawnRow = topLockedRow - 2;
                }
                // clamp to valid range
                spawnRow = Math.max(0, Math.min(spawnRow, rows - 1));

                // spawn the Medusa block
                grid.setCell(spawnRow, c, new MedusaBlock(0));
            }

            // start falling immediately
            opponent.getGameLogic().applyGravityWithDelay(panel::repaint);
        }

        // final repaint to show placement
        panel.repaint();

    }


}