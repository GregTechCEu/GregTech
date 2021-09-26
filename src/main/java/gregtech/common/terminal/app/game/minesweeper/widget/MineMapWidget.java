package gregtech.common.terminal.app.game.minesweeper.widget;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;

public class MineMapWidget extends Widget {

    int mineCount;
    int width;
    int height;
    boolean[][] mines;
    boolean[][] flags;
    boolean[][] checkedSpaces;
    int[][] generatedNumbers;

    public MineMapWidget(int width, int height, int mineCount) {
        super(333 / 2 - width * 8, 232 / 2 - height * 8, width * 16, height * 16);
        this.width = width;
        this.height = height;
        mines = new boolean[width][height];
        generatedNumbers = new int[width][height];
        checkedSpaces = new boolean[width][height];
        flags = new boolean[width][height];

        this.mineCount = mineCount;
    }

    public void initMines(int startX, int startY) {
        for(int minesPlaced = 0; minesPlaced < mineCount; minesPlaced++) {
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * height);

            // The weird part to the right is making sure the player doesn't start on a numbered tile
            while (!mines[x][y] && (startX < x + 2 && startX > x - 2) && (startY < y + 2 && startY > y - 2)) {
                x = (int) (Math.random() * width);
                y = (int) (Math.random() * height);
            }
            mines[x][y] = true;

            // Add to surrounding numbers for the mine
            for(int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    try {
                        generatedNumbers[x + i][y + j]++;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        // Lol don't do anything this is expected
                    }
                }
            }
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (!checkedSpaces[i][j]) {
                    // Draw uncovered tile
                } else if (!mines[i][j]) {
                    if (generatedNumbers[i][j] > 0) {
                        // Draw number
                    } else {
                        // Draw blank
                    }
                } else {
                    // Draw mine
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        int gridX = mouseX / 16;
        int gridY = mouseY / 16;

        if (button == 0 && !flags[gridX][gridY]) {
            checkedSpaces[gridX][gridY] = true;
        } else if (button == 1) {
            flags[gridX][gridY] = !flags[gridX][gridY];
        }

        return false;
    }
}
