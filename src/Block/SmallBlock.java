package Block;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * A single cell that makes up a falling Block.
 * Now uses a vertical sprite sheet in column 0.
 */
public class SmallBlock {
    public static final int SIZE = 48;

    private static final String SPRITESHEET_PATH = "/sprites/alt_blocksprites.png";
    private static BufferedImage sheet;
    private static BufferedImage[] sprites;

    private final int typeIndex;

    static {
        try {
            sheet = ImageIO.read(SmallBlock.class.getResourceAsStream(SPRITESHEET_PATH));
            int rows = sheet.getHeight() / SIZE;
            sprites = new BufferedImage[rows];
            for (int i = 0; i < rows; i++) {
                // slice from column 0 (x = 0)
                sprites[i] = sheet.getSubimage(
                        0,          // x offset
                        i * SIZE,   // y offset
                        SIZE,       // width
                        SIZE        // height
                );
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new RuntimeException("Failed to load small-block sprites from "
                    + SPRITESHEET_PATH, e);
        }
    }

    /**
     * @param typeIndex which row-index to use (0 ≤ typeIndex < sprites.length)
     */
    public SmallBlock(int typeIndex) {
        if (typeIndex < 0 || typeIndex >= sprites.length) {
            throw new IllegalArgumentException("Invalid SmallBlock type: " + typeIndex);
        }
        this.typeIndex = typeIndex;
    }

    /** Returns the 48×48 sprite for this block. */
    public BufferedImage getSprite() {
        return sprites[typeIndex];
    }

    /** Pixel size of this block. */
    public int getSize() {
        return SIZE;
    }

    /** How many distinct small-block types exist. */
    public static int getTypeCount() {
        return sprites.length;
    }

    /** Expose the typeIndex for matching/clearing logic. */
    public int getTypeIndex() {
        return typeIndex;
    }
}