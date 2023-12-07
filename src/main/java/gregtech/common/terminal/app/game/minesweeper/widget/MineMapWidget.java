package gregtech.common.terminal.app.game.minesweeper.widget;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.interpolate.RGBInterpolator;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class MineMapWidget extends Widget {

    private static final TextureArea COVERED = new TextureArea(
            new ResourceLocation("gregtech:textures/gui/terminal/minesweeper/covered.png"), 0, 0, 1, 1);
    private static final TextureArea FLAG = new TextureArea(
            new ResourceLocation("gregtech:textures/gui/terminal/minesweeper/flag.png"), 0, 0, 1, 1);
    private static final TextureArea BOMB = new TextureArea(
            new ResourceLocation("gregtech:textures/gui/terminal/minesweeper/bomb.png"), 0, 0, 1, 1);

    private static final TextureArea[] NUMBERS = {
            new TextureArea(new ResourceLocation("gregtech:textures/gui/terminal/minesweeper/blank.png"), 0, 0, 1, 1),
            new TextureArea(new ResourceLocation("gregtech:textures/gui/terminal/minesweeper/1.png"), 0, 0, 1, 1),
            new TextureArea(new ResourceLocation("gregtech:textures/gui/terminal/minesweeper/2.png"), 0, 0, 1, 1),
            new TextureArea(new ResourceLocation("gregtech:textures/gui/terminal/minesweeper/3.png"), 0, 0, 1, 1),
            new TextureArea(new ResourceLocation("gregtech:textures/gui/terminal/minesweeper/4.png"), 0, 0, 1, 1),
            new TextureArea(new ResourceLocation("gregtech:textures/gui/terminal/minesweeper/5.png"), 0, 0, 1, 1),
            new TextureArea(new ResourceLocation("gregtech:textures/gui/terminal/minesweeper/6.png"), 0, 0, 1, 1),
            new TextureArea(new ResourceLocation("gregtech:textures/gui/terminal/minesweeper/7.png"), 0, 0, 1, 1),
            new TextureArea(new ResourceLocation("gregtech:textures/gui/terminal/minesweeper/8.png"), 0, 0, 1, 1)
    };

    public int mineCount;
    public int flagsPlaced;

    private int width;
    private int height;

    private boolean isPrepared;

    private boolean[][] mines;
    private boolean[][] flags;
    private boolean[][] checkedSpaces;
    private int[][] generatedNumbers;

    private boolean isLost;
    private boolean isWon;
    private int timer = 0;
    private static final RGBInterpolator interpolator = new RGBInterpolator(5,
            (r, g, b) -> GlStateManager.color(r.floatValue(), g.floatValue(), b.floatValue()),
            (r, g, b) -> GlStateManager.color(0, 0, 0));

    public MineMapWidget(int width, int height, int mineCount) {
        super(333 / 2 - width * 8, 232 / 2 - height * 8, width * 16, height * 16);
        this.width = width;
        this.height = height;
        this.resetData();
        this.mineCount = mineCount;
    }

    public void resetData() {
        mines = new boolean[width][height];
        generatedNumbers = new int[width][height];
        checkedSpaces = new boolean[width][height];
        flags = new boolean[width][height];
        isLost = false;
        isWon = false;
        isPrepared = false;
        flagsPlaced = 0;
    }

    public void initMines(int startX, int startY) {
        int minesPlaced = 0;
        while (minesPlaced < mineCount) {
            for (; minesPlaced < mineCount; minesPlaced++) {
                placeMine(startX, startY);
            }

            // Are there any sections that we can't figure out what's inside?
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    boolean isTrapped = true;
                    // The weird ternaries here are making sure to not cause overflows
                    for (int xMod = i == 0 ? 0 : -1; xMod < (i == width - 1 ? 1 : 2); xMod++) {
                        for (int yMod = j == 0 ? 0 : -1; yMod < (j == height - 1 ? 1 : 2); yMod++) {
                            isTrapped &= mines[i + xMod][j + yMod];
                        }
                    }
                    if (isTrapped) {
                        // Yes, so just take out the middle
                        mines[i][j] = false;
                        minesPlaced--;
                    }
                }
            }
        }

        // Add to surrounding numbers for the mine
        // The weird ternaries here are making sure to not cause overflows
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (mines[x][y]) {
                    for (int xMod = x == 0 ? 0 : -1; xMod < (x == width - 1 ? 1 : 2); xMod++) {
                        for (int yMod = y == 0 ? 0 : -1; yMod < (y == height - 1 ? 1 : 2); yMod++) {
                            generatedNumbers[x + xMod][y + yMod]++;
                        }
                    }
                }
            }
        }
        isPrepared = true;
    }

    private void placeMine(int startX, int startY) {
        int x = (int) (Math.random() * width);
        int y = (int) (Math.random() * height);

        // The weird part to the right is making sure the player doesn't start on a numbered tile
        while (mines[x][y] || ((startX < x + 3 && startX > x - 3) && (startY < y + 3 && startY > y - 3))) {
            x = (int) (Math.random() * width);
            y = (int) (Math.random() * height);
        }
        mines[x][y] = true;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        timer++;
        if (isWon && !interpolator.isActivated()) { // Fancy colors :)
            interpolator.start();
        }
        if (!isWon && interpolator.isActivated()) {
            interpolator.stop();
        }
        interpolator.update();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (isLost && mines[i][j]) // If the player lost, show where the mines are.
                    BOMB.draw(i * 16 + getPosition().getX(), j * 16 + getPosition().getY(), 16, 16);
                else if (!checkedSpaces[i][j]) {
                    if (flags[i][j])
                        FLAG.draw(i * 16 + getPosition().getX(), j * 16 + getPosition().getY(), 16, 16);
                    else
                        COVERED.draw(i * 16 + getPosition().getX(), j * 16 + getPosition().getY(), 16, 16);
                } else if (!mines[i][j])
                    NUMBERS[generatedNumbers[i][j]].draw(i * 16 + getPosition().getX(), j * 16 + getPosition().getY(),
                            16, 16);
                else
                    BOMB.draw(i * 16 + getPosition().getX(), j * 16 + getPosition().getY(), 16, 16);
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isWon || isLost) {
            return false; // Don't let them interact now...
        }

        int gridX = (mouseX - getPosition().getX()) / 16;
        int gridY = (mouseY - getPosition().getY()) / 16;
        if (gridX >= width || gridY >= height || gridX < 0 || gridY < 0) {
            return false;
        }

        if (button == 0 && !flags[gridX][gridY]) {
            if (!isPrepared)
                initMines(gridX, gridY);
            if (generatedNumbers[gridX][gridY] == 0)
                uncoverSafeTiles(gridX, gridY);
            else
                checkedSpaces[gridX][gridY] = true;
            if (mines[gridX][gridY])
                isLost = true;
        } else if (button == 1 && !checkedSpaces[gridX][gridY]) {
            flags[gridX][gridY] = !flags[gridX][gridY];
            if (flags[gridX][gridY])
                flagsPlaced++;
            else
                flagsPlaced--;
        }

        return true;
    }

    private void uncoverSafeTiles(int x, int y) {
        checkedSpaces[x][y] = true;
        if (generatedNumbers[x][y] != 0)
            return;
        // Weird ternaries again for preventing ArrayIndexOutOfBounds exceptions
        for (int xMod = x == 0 ? 0 : -1; xMod < (x == width - 1 ? 1 : 2); xMod++) {
            for (int yMod = y == 0 ? 0 : -1; yMod < (y == height - 1 ? 1 : 2); yMod++) {
                if (!checkedSpaces[x + xMod][y + yMod])
                    uncoverSafeTiles(x + xMod, y + yMod);
            }
        }
    }

    public boolean hasLost() {
        return isLost;
    }

    public boolean hasWon() {
        if (!isPrepared)
            return false;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (mines[i][j] != flags[i][j] || checkedSpaces[i][j] == mines[i][j]) { // If there is an unchecked safe
                                                                                        // square, or an uncovered
                                                                                        // bomb...
                    return false;
                }
            }
        }
        return true;
    }

    public void notifyWon() {
        isWon = true;
    }
}
