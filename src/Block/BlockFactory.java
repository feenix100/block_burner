// BlockFactory.java
package Block;

import java.util.Random;

/**
 * BlockFactory creates both regular small blocks and special power-up blocks.
 */
public class BlockFactory {
    private final Random random = new Random();
    private static final double POWERUP_PROBABILITY = 0.5; // .xx % chance per cell

    /**
     * Construct a BlockFactory.
     */
    public BlockFactory() {
    }

    /**
     * Generate a new Block composed of 3 SmallBlock or PowerUpBlock cells.
     * @return A new Block instance.
     */
    public Block generateBlock() {
        SmallBlock[] cells = new SmallBlock[3];
        int normalTypes = SmallBlock.getTypeCount();
        for (int i = 0; i < cells.length; i++) {
            if (random.nextDouble() < POWERUP_PROBABILITY) {
                PowerUpType[] types = PowerUpType.values();
                PowerUpType choice = types[random.nextInt(types.length)];
                cells[i] = new PowerUpBlock(choice);
            } else {
                int index = random.nextInt(normalTypes);
                cells[i] = new SmallBlock(index);
            }
        }
        return new Block(cells);
    }
}