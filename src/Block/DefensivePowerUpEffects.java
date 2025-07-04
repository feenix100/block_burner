package Block;

import Burner.GameGrid;
import Burner.GameLogic;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class DefensivePowerUpEffects {

    public static void doDefensiveOne(PowerUpBlock block, GameLogic logic) {
        // 1) Clear bottom row
        GameGrid grid = logic.getGrid();
        int rows = grid.getRows();
        int cols = grid.getColumns();
        int bottom = rows - 1;
        for (int c = 0; c < cols; c++) {
            grid.setCell(bottom, c, null);
        }
        // 2) Shift all rows above down by one
        for (int r = bottom - 1; r >= 0; r--) {
            for (int c = 0; c < cols; c++) {
                SmallBlock sb = grid.getCell(r, c);
                grid.setCell(r + 1, c, sb);
                grid.setCell(r, c, null);
            }
        }
    }

    public static void doDefensiveTwo(PowerUpBlock block, GameLogic logic) {
        GameGrid grid = logic.getGrid();
        int rows = grid.getRows();
        int cols = grid.getColumns();

        // 1) Find all unique colors in the grid
        Set<Integer> typeSet = new HashSet<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                SmallBlock sb = grid.getCell(r, c);
                if (sb != null) {
                    typeSet.add(sb.getTypeIndex());
                }
            }
        }
        if (typeSet.isEmpty()) {
            return; // nothing to remove
        }

        // 2) Choose one color at random
        List<Integer> types = new ArrayList<>(typeSet);
        Random rand = new Random();
        int chosenType = types.get(rand.nextInt(types.size()));

        // 3) Collect positions of that color
        List<int[]> positions = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                SmallBlock sb = grid.getCell(r, c);
                if (sb != null && sb.getTypeIndex() == chosenType) {
                    positions.add(new int[]{r, c});
                }
            }
        }
        if (positions.isEmpty()) {
            return;
        }

        // 4) Pick a random number of them to clear
        int count = rand.nextInt(positions.size()) + 1;
        Collections.shuffle(positions, rand);
        for (int i = 0; i < count; i++) {
            int[] pos = positions.get(i);
            grid.setCell(pos[0], pos[1], null);
        }

        // 5) Apply gravity: for each column, drop blocks down into empty space
        for (int c = 0; c < cols; c++) {
            int writeRow = rows - 1;
            for (int r = rows - 1; r >= 0; r--) {
                SmallBlock sb = grid.getCell(r, c);
                if (sb != null) {
                    grid.setCell(writeRow, c, sb);
                    if (writeRow != r) {
                        grid.setCell(r, c, null);
                    }
                    writeRow--;
                }
            }
        }
    }

    public static void doDefensiveThree(PowerUpBlock block, GameLogic logic) {
        logic.getController().addMidasBlock();
    }

    public static void doDefensiveFour(PowerUpBlock block, GameLogic logic) {
        GameGrid grid = logic.getGrid();
        int rows = grid.getRows();
        int cols = grid.getColumns();

        // Must have room for a 3×3 square
        if (rows < 3 || cols < 3) return;

        // Find all valid top-left starts where that cell is non-null
        List<int[]> validStarts = new ArrayList<>();
        for (int r = 0; r <= rows - 3; r++) {
            for (int c = 0; c <= cols - 3; c++) {
                if (grid.getCell(r, c) != null) {
                    validStarts.add(new int[]{r, c});
                }
            }
        }

        // If no occupied cell can serve as top-left, do nothing
        if (validStarts.isEmpty()) return;

        // Pick one at random
        Random rand = new Random();
        int[] start = validStarts.get(rand.nextInt(validStarts.size()));
        int startRow = start[0];
        int startCol = start[1];

        // Use the power-up’s sprite-row as the block type for color
        int typeIndex = block.getType().getSheetRow();

        // Fill the 3×3 area
        for (int dr = 0; dr < 3; dr++) {
            for (int dc = 0; dc < 3; dc++) {
                grid.setCell(startRow + dr, startCol + dc, new SmallBlock(typeIndex));
            }
        }
    }

    public static void doDefensiveFive(PowerUpBlock block, GameLogic logic) {
        GameGrid grid = logic.getGrid();
        int rows = grid.getRows();
        int cols = grid.getColumns();

        // 1) Collect positions of all locked MedusaBlocks
        List<int[]> medusaPositions = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                SmallBlock sb = grid.getCell(r, c);
                if (sb instanceof MedusaBlock) {
                    medusaPositions.add(new int[]{r, c});
                }
            }
        }

        // 2) If there are none, do nothing
        if (medusaPositions.isEmpty()) {
            return;
        }

        // 3) Shuffle and remove up to two
        Collections.shuffle(medusaPositions, new Random());
        int toRemove = Math.min(2, medusaPositions.size());
        for (int i = 0; i < toRemove; i++) {
            int[] pos = medusaPositions.get(i);
            grid.setCell(pos[0], pos[1], null);
        }

        // 4) Apply gravity: drop everything into empty slots
        for (int c = 0; c < cols; c++) {
            int writeRow = rows - 1;
            for (int r = rows - 1; r >= 0; r--) {
                SmallBlock sb = grid.getCell(r, c);
                if (sb != null) {
                    grid.setCell(writeRow, c, sb);
                    if (writeRow != r) {
                        grid.setCell(r, c, null);
                    }
                    writeRow--;
                }
            }
        }
    }

    public static void doDefensiveSix(PowerUpBlock block, GameLogic logic) {
        GameGrid grid = logic.getGrid();
        int rows = grid.getRows();
        int cols  = grid.getColumns();
        Random rand = new Random();

        // 1) collect all distinct types present in the locked grid
        Set<Integer> typeSet = new HashSet<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                SmallBlock sb = grid.getCell(r, c);
                if (sb != null) {
                    typeSet.add(sb.getTypeIndex());
                }
            }
        }
        if (typeSet.isEmpty()) return;

        // 2) pick one random type
        List<Integer> types = new ArrayList<>(typeSet);
        int chosenType = types.get(rand.nextInt(types.size()));

        // 3) locate up to 3 blocks of that type
        List<int[]> positions = new ArrayList<>();
        for (int r = 0; r < rows && positions.size() < 3; r++) {
            for (int c = 0; c < cols && positions.size() < 3; c++) {
                SmallBlock sb = grid.getCell(r, c);
                if (sb != null && sb.getTypeIndex() == chosenType) {
                    positions.add(new int[]{r, c});
                }
            }
        }
        if (positions.isEmpty()) return;

        // 4) remove & stash them, and remember which columns to collapse
        List<SmallBlock> stash = new ArrayList<>();
        Set<Integer> colsToCollapse = new HashSet<>();
        for (int[] pos : positions) {
            int r = pos[0], c = pos[1];
            stash.add(grid.getCell(r, c));
            colsToCollapse.add(c);
            grid.setCell(r, c, null);
        }

        // 5) collapse each affected column so that all blocks fall down
        for (int c : colsToCollapse) {
            int writeRow = rows - 1;
            for (int r = rows - 1; r >= 0; r--) {
                SmallBlock sb = grid.getCell(r, c);
                if (sb != null) {
                    grid.setCell(writeRow, c, sb);
                    if (writeRow != r) {
                        grid.setCell(r, c, null);
                    }
                    writeRow--;
                }
            }
        }

        // 6) re‐drop the 3 stashed blocks as a contiguous cluster on the bottom row
        int targetRow = rows - 1;
        int startCol  = rand.nextInt(Math.max(1, cols - stash.size() + 1));
        for (int i = 0; i < stash.size(); i++) {
            grid.setCell(targetRow, startCol + i, stash.get(i));
        }
    }

}