package Burner;

import Block.*;

import java.awt.*;


//handles grid state
public class GameGrid {
    private final SmallBlock[][] grid;
    private final int rows;
    private final int columns;

    public GameGrid(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.grid = new SmallBlock[rows][columns];
    }

    public boolean isCellEmpty(int row, int col) {
        return grid[row][col] == null;
    }

    public SmallBlock getCell(int row, int col) {
        return grid[row][col]; // Return the SmallBlock object
    }

    public void setCell(int row, int col, SmallBlock smallBlock) {
        grid[row][col] = smallBlock;
    }

    public boolean isOutOfBounds(int row, int col) {
        return row < 0 || row >= rows || col < 0 || col >= columns;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public void clearAll() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                grid[row][col] = null;
            }
        }
    }
}
