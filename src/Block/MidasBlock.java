package Block;

import Burner.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * A special power-up block that turns adjacent blocks into gold.
 * Uses sprites from column 3, rows 0 through 2 of the sprite sheet.
 */
public class MidasBlock extends SmallBlock {
    private static final String SPRITESHEET_PATH = "/sprites/block_sprites.png";
    private static final int SPRITE_COLUMN = 3;       // use column 3
    private static BufferedImage sheet;
    private static BufferedImage[] sprites;

    static {
        try {
            sheet = ImageIO.read(MidasBlock.class.getResourceAsStream(SPRITESHEET_PATH));
            int rows = 3;  // rows 0, 1, and 2
            sprites = new BufferedImage[rows];
            for (int i = 0; i < rows; i++) {
                sprites[i] = sheet.getSubimage(
                        SPRITE_COLUMN * SIZE,  // x offset = column 3 * sprite size
                        i * SIZE,              // y offset = row i * sprite size
                        SIZE,                  // width  = sprite size
                        SIZE                   // height = sprite size
                );
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new RuntimeException("Failed to load MidasBlock sprites from "
                    + SPRITESHEET_PATH, e);
        }
    }

    /**
     * @param typeIndex which row-index to use (0 ≤ typeIndex < sprites.length)
     */
    public MidasBlock(int typeIndex) {
        super(typeIndex);
        if (typeIndex < 0 || typeIndex >= sprites.length) {
            throw new IllegalArgumentException("Invalid MidasBlock type: " + typeIndex);
        }
    }

    /** Override to return the MidasBlock-specific sprite. */
    @Override
    public BufferedImage getSprite() {
        return sprites[getTypeIndex()];
    }

    /** How many distinct MidasBlock types exist. */
    public static int getTypeCount() {
        return sprites.length;
    }

    /** Placeholder for the Midas power-up effect. */
    // In MidasBlock.java, import at top if not already:
// import java.awt.Point;

    public void activateMidasEffect(GameGrid grid, int row, int col) {
        // Offsets for the 8 surrounding cells
        int[][] dirs = {
                {-1,  0}, {1,  0},
                { 0, -1}, {0,  1},
                {-1, -1}, {-1,  1},
                { 1, -1}, {1,   1}
        };

        // Convert every valid neighbor into SmallBlock typeIndex=2
        for (int[] d : dirs) {
            int nr = row + d[0];
            int nc = col + d[1];
            if (nr >= 0 && nr < grid.getRows()
                    && nc >= 0 && nc < grid.getColumns()
                    && grid.getCell(nr, nc) != null) {
                grid.setCell(nr, nc, new SmallBlock(2));
            }
        }

        // Finally remove the MidasBlock itself
        grid.setCell(row, col, null);
    }

}
