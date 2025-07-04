package Block;

import Burner.GameGrid;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * A special power-up block that turns adjacent blocks into Medusa blocks.
 * Uses sprites at:
 *   col 3, row 3 → typeIndex 0
 *   col 3, row 4 → typeIndex 1
 *   col 2, row 5 → typeIndex 2
 */
public class MedusaBlock extends SmallBlock {
    private static final String SPRITESHEET_PATH = "/sprites/block_sprites.png";
    private static final int SIZE = SmallBlock.SIZE;
    private static BufferedImage sheet;
    private static BufferedImage[] sprites;

    static {
        try {
            sheet = ImageIO.read(MedusaBlock.class
                    .getResourceAsStream(SPRITESHEET_PATH));
            sprites = new BufferedImage[3];
            // col 3, row 3
            sprites[0] = sheet.getSubimage(3 * SIZE, 3 * SIZE, SIZE, SIZE);
            // col 3, row 4
            sprites[1] = sheet.getSubimage(3 * SIZE, 4 * SIZE, SIZE, SIZE);
            // col 2, row 5
            sprites[2] = sheet.getSubimage(3 * SIZE, 5 * SIZE, SIZE, SIZE);
        } catch (IOException | IllegalArgumentException e) {
            throw new RuntimeException(
                    "Failed to load MedusaBlock sprites from " + SPRITESHEET_PATH, e);
        }
    }

    /**
     * @param typeIndex which sprite to use (0 ≤ typeIndex < sprites.length)
     */
    public MedusaBlock(int typeIndex) {
        super(typeIndex);
        if (typeIndex < 0 || typeIndex >= sprites.length) {
            throw new IllegalArgumentException("Invalid MedusaBlock type: " + typeIndex);
        }
    }

    @Override
    public BufferedImage getSprite() {
        return sprites[getTypeIndex()];
    }

    /** How many distinct MedusaBlock types exist. */
    public static int getTypeCount() {
        return sprites.length;
    }

    /**
     * Converts all 8 neighbors into MedusaBlocks of the same type,
     * then removes itself.
     */
    public void activateMedusaEffect(GameGrid grid, int row, int col) {
        int[][] dirs = {
                {-1,  0}, {1,  0},
                { 0, -1}, {0,  1},
                {-1, -1}, {-1,  1},
                { 1, -1}, {1,   1}
        };
        for (int[] d : dirs) {
            int nr = row + d[0], nc = col + d[1];
            if (nr >= 0 && nr < grid.getRows()
                    && nc >= 0 && nc < grid.getColumns()
                    && grid.getCell(nr, nc) != null) {
                grid.setCell(nr, nc, new MedusaBlock(getTypeIndex()));
            }
        }
        // remove this MedusaBlock
        grid.setCell(row, col, null);
    }
}
