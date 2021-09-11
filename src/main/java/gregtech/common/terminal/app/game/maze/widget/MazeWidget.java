package gregtech.common.terminal.app.game.maze.widget;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import net.minecraft.util.math.Vec2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MazeWidget extends Widget {

    boolean[][] topWalls = new boolean[11][11];
    boolean[][] leftWalls = new boolean[11][11];
    boolean[][] includedSpots = new boolean[11][11];

    public MazeWidget() {
        super(333 / 2 - 55, 232 / 2 - 55, 110, 110);
        initMaze();
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        // Draw outer lines
        drawLines(createBorder(), 0xFFFFFFFF, 0xFFFFFFFF, 4);
        // Draw inner lines
        createInternalLines();
    }

    public List<Vec2f> createBorder() {
        List<Vec2f> result = new ArrayList<>();
        result.add(new Vec2f(getPosition().x, getPosition().y));
        result.add(new Vec2f(this.getSize().width + getPosition().x, getPosition().y));
        result.add(new Vec2f(this.getSize().width + getPosition().x, this.getSize().height + getPosition().y));
        result.add(new Vec2f(getPosition().x, this.getSize().height + getPosition().y));
        result.add(new Vec2f(getPosition().x, getPosition().y)); // Do this again so it's a connected square
        return result;
    }

    public boolean isThereWallAt(int x, int y, boolean onTops) {
        if (x > 10 || y > 10)
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
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 11; j++) {
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
        for (int i = 0; i < 11; i++) { // Fill array with walls so that they can be carved out
            for (int j = 0; j < 11; j++) {
                leftWalls[i][j] = true;
                topWalls[i][j] = true;
            }
        }

        includedSpots[5][5] = true; // The center is where the player starts
        // Improves maze randomization.
        List<Integer> positions = new ArrayList<>();
        for(int i = 0; i < 11 * 11; i++) {
            positions.add(i);
        }
        Collections.shuffle(positions);

        for (int position : positions) {
            if (!includedSpots[position / 11][position % 11]) {
                createPath(position / 11, position % 11, new boolean[11][11]);
            }
        }
    }

    // Wilson random walk maze generation
    public boolean createPath(int x, int y, boolean[][] walkedPaths) {
        if(walkedPaths[x][y]) {
            return false;
        }
        if(includedSpots[x][y]) {
            return true;
        }
        includedSpots[x][y] = true;
        walkedPaths[x][y] = true;
        // Find unoccupied directions
        // Left 0
        List<Integer> directions = new ArrayList<>();
        if (x != 0 && !walkedPaths[x - 1][y]) {
            directions.add(0);
        }
        // Right 1
        if (x != 10 && !walkedPaths[x + 1][y]) {
            directions.add(1);
        }
        // Up 2
        if (y != 0 && !walkedPaths[x][y - 1]) {
            directions.add(2);
        }
        // Down 3
        if (y != 10 && !walkedPaths[x][y + 1]) {
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
        includedSpots[x][y] = false;
        walkedPaths[x][y] = false;
        return false;
    }
}
