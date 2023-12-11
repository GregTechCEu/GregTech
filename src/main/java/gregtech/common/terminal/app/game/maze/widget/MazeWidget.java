package gregtech.common.terminal.app.game.maze.widget;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

import net.minecraft.util.math.Vec2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static gregtech.common.terminal.app.game.maze.MazeApp.MAZE_SIZE;

public class MazeWidget extends Widget {

    boolean[][] topWalls = new boolean[MAZE_SIZE][MAZE_SIZE];
    boolean[][] leftWalls = new boolean[MAZE_SIZE][MAZE_SIZE];
    boolean[][] includedSpots;
    private int squaresChecked;

    public MazeWidget() {
        super(333 / 2 - (MAZE_SIZE * 5), 232 / 2 - (MAZE_SIZE * 5), MAZE_SIZE * 10, MAZE_SIZE * 10);
        initMaze();
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        // Draw outer lines
        createBorder();
        // Draw inner lines
        createInternalLines();
    }

    public void recalculateSize() {
        this.setSelfPosition(new Position(333 / 2 - (MAZE_SIZE * 5), 232 / 2 - (MAZE_SIZE * 5)));
        this.setSize(new Size(MAZE_SIZE * 10, MAZE_SIZE * 10));
        topWalls = new boolean[MAZE_SIZE][MAZE_SIZE];
        leftWalls = new boolean[MAZE_SIZE][MAZE_SIZE];
    }

    public void createBorder() {
        List<Vec2f> lineBuffer = new ArrayList<>();
        lineBuffer.add(new Vec2f(getPosition().x + 10, getPosition().y));
        lineBuffer.add(new Vec2f(this.getSize().width + getPosition().x, getPosition().y));
        lineBuffer.add(new Vec2f(this.getSize().width + getPosition().x, this.getSize().height + getPosition().y + 2)); // Corrects
                                                                                                                        // for
                                                                                                                        // line
                                                                                                                        // width
                                                                                                                        // misalignment
        drawLines(lineBuffer, 0xFFFFFFFF, 0xFFFFFFFF, 4);
        lineBuffer.clear();
        lineBuffer.add(new Vec2f(this.getSize().width + getPosition().x - 10, this.getSize().height + getPosition().y));
        lineBuffer.add(new Vec2f(getPosition().x, this.getSize().height + getPosition().y));
        lineBuffer.add(new Vec2f(getPosition().x, getPosition().y - 2));
        drawLines(lineBuffer, 0xFFFFFFFF, 0xFFFFFFFF, 4);
    }

    public boolean isThereWallAt(int x, int y, boolean onTops) {
        if (x >= MAZE_SIZE || y >= MAZE_SIZE)
            return true;
        if (x < 0 || y < 0)
            return true;
        if ((x == 0 && !onTops) || (y == 0 && onTops))
            return true;
        if (onTops) {
            return topWalls[x][y];
        } else {
            return leftWalls[x][y];
        }
    }

    public void createInternalLines() {
        for (int i = 0; i < MAZE_SIZE; i++) {
            for (int j = 0; j < MAZE_SIZE; j++) {
                List<Vec2f> list = new ArrayList<>();
                if (j != 0 && isThereWallAt(i, j, true)) {
                    list.add(new Vec2f(getPosition().x + 10 * i, getPosition().y + 10 * j));
                    list.add(new Vec2f(getPosition().x + 10 * (i + 1), getPosition().y + 10 * j));
                    drawLines(list, 0xFFFFFFFF, 0xFFFFFFFF, 2);
                    list.clear();
                }
                if (i != 0 && isThereWallAt(i, j, false)) {
                    list.add(new Vec2f(getPosition().x + 10 * i, getPosition().y + 10 * j));
                    list.add(new Vec2f(getPosition().x + 10 * i, getPosition().y + 10 * (j + 1)));
                    drawLines(list, 0xFFFFFFFF, 0xFFFFFFFF, 2);
                }
            }
        }
    }

    public void initMaze() {
        includedSpots = new boolean[MAZE_SIZE][MAZE_SIZE];
        for (int i = 0; i < MAZE_SIZE; i++) { // Fill array with walls so that they can be carved out
            for (int j = 0; j < MAZE_SIZE; j++) {
                leftWalls[i][j] = true;
                topWalls[i][j] = true;
            }
        }

        includedSpots[(int) (Math.random() * MAZE_SIZE)][(int) (Math.random() * MAZE_SIZE)] = true; // Can seed our
                                                                                                    // particular maze
        // Improves maze randomization.
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < MAZE_SIZE * MAZE_SIZE; i++) {
            positions.add(i);
        }
        Collections.shuffle(positions);

        for (int position : positions) {
            if (!includedSpots[position / MAZE_SIZE][position % MAZE_SIZE]) {
                do {
                    resetStuckCounter();
                } while (!this.createPath(position / MAZE_SIZE, position % MAZE_SIZE,
                        new boolean[MAZE_SIZE][MAZE_SIZE]));
            }
        }
    }

    // Wilson random walk maze generation
    public boolean createPath(int x, int y, boolean[][] walkedPaths) {
        squaresChecked++;
        if (squaresChecked > 20000) // Probably stuck.
            return false;
        if (walkedPaths[x][y])
            return false;
        if (this.includedSpots[x][y])
            return true;
        this.includedSpots[x][y] = true;
        walkedPaths[x][y] = true;
        // Find unoccupied directions
        // Left 0
        List<Integer> directions = new ArrayList<>();
        if (x != 0 && !walkedPaths[x - 1][y]) {
            directions.add(0);
        }
        // Right 1
        if (x != MAZE_SIZE - 1 && !walkedPaths[x + 1][y]) {
            directions.add(1);
        }
        // Up 2
        if (y != 0 && !walkedPaths[x][y - 1]) {
            directions.add(2);
        }
        // Down 3
        if (y != MAZE_SIZE - 1 && !walkedPaths[x][y + 1]) {
            directions.add(3);
        }
        Collections.shuffle(directions);
        // Select one
        while (directions.size() > 0) {
            int direction = directions.get(directions.size() - 1);
            // Use direction to create new coordinates
            int newX = x;
            int newY = y;
            if (direction == 0) {
                newX--;
            } else if (direction == 1) {
                newX++;
            } else if (direction == 2) {
                newY--;
            } else if (direction == 3) {
                newY++;
            }
            if (createPath(newX, newY, walkedPaths)) {
                // Delete walls and return
                if (direction == 0) {
                    leftWalls[x][y] = false;
                } else if (direction == 1) {
                    leftWalls[x + 1][y] = false;
                } else if (direction == 2) {
                    topWalls[x][y] = false;
                } else if (direction == 3) {
                    topWalls[x][y + 1] = false;
                }
                return true;
            } else {
                directions.remove(directions.size() - 1);
            }
        }
        // Reset current position
        this.includedSpots[x][y] = false;
        walkedPaths[x][y] = false;
        return false;
    }

    public void resetStuckCounter() {
        squaresChecked = 0;
    }
}
