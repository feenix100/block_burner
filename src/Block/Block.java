package Block;

public class Block {
    private int[][] positions;       // Relative positions of the smaller blocks
    private final SmallBlock[] smallBlocks; // Array of SmallBlock objects
    private int x;                   // Current x position of the block
    private int y;                   // Current y position of the block

    public Block(SmallBlock[] smallBlocks) {
        this.smallBlocks = smallBlocks;
        this.positions = new int[][]{{0, 0}, {1, 0}, {2, 0}}; // Default vertical layout
        this.x = 0;
        this.y = 0;
    }

    public SmallBlock[] getSmallBlocks() {
        return smallBlocks;
    }

    public int[][] getPositions() {
        return positions;
    }

    public void setPositions(int[][] newPositions) {
        this.positions = newPositions;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }



    // Cycle the small blocks (swap their order)
    public void cycleColors() {
        SmallBlock temp = smallBlocks[0];
        smallBlocks[0] = smallBlocks[1];
        smallBlocks[1] = smallBlocks[2];
        smallBlocks[2] = temp;
    }

    // Move the block down
    public void moveDown() {
        y++;
    }

    // Move the block left
    public void moveLeft() {
        x--;
    }

    // Move the block right
    public void moveRight() {
        x++;
    }
}
