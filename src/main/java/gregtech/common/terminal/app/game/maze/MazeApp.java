package gregtech.common.terminal.app.game.maze;

import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.common.terminal.app.game.maze.widget.EnemyWidget;
import gregtech.common.terminal.app.game.maze.widget.MazeWidget;
import gregtech.common.terminal.app.game.maze.widget.PlayerWidget;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MazeApp extends AbstractApplication {
    private int gameState = 0;
    private PlayerWidget player;
    private EnemyWidget enemy;
    private MazeWidget maze;
    private int timer = 0;
    private int mazesSolved = 0;
    private float speed = 25;
    private int lastPlayerInput = -2;
    public static int MAZE_SIZE = 9;
    private List<Integer> movementStore;
    private boolean lastPausePress;

    private List<Widget>[] FSM;

    public MazeApp() {
        super("maze");
    }

    public AbstractApplication initApp() {
        if (isClient) {
            movementStore = new ArrayList<>();
            FSM = new List[4];
            FSM[0] = new LinkedList<>();
            FSM[1] = new LinkedList<>();
            FSM[2] = new LinkedList<>();
            FSM[3] = new LinkedList<>();
            this.setOs(os);
            this.addWidget(new ImageWidget(5, 5, 333 - 10, 232 - 10, TerminalTheme.COLOR_B_2));
            // enemy 0: Title
            this.addWidget(new LabelWidget(333 / 2, 222 / 2 - 50, "terminal.maze.title", 0xFFFFFFFF).setXCentered(true), 0);
            this.addWidget(new ClickButtonWidget(323 / 2 - 10, 222 / 2 - 10, 30, 30, "terminal.maze.play",
                    (clickData -> {
                        this.setGameState(1);
                        this.resetGame();
                    })).setShouldClientCallback(true), 0);
            // GameState 1: Play
            this.setMaze(new MazeWidget());
            this.setPlayer(new PlayerWidget(0, 0, this));
            this.setEnemy(new EnemyWidget(-100, -100, this));
            // GameState 2: Pause
            this.addWidget(new ImageWidget(5, 5, 333 - 10, 232 - 10, new ColorRectTexture(0xFF000000)), 2, 3);
            this.addWidget(new ClickButtonWidget(323 / 2 - 10, 222 / 2 - 10, 50, 20, "terminal.maze.continue", (clickData) -> this.setGameState(1)).setShouldClientCallback(true), 2);
            this.addWidget(new LabelWidget(333 / 2, 222 / 2 - 50, "terminal.maze.pause", 0xFFFFFFFF).setXCentered(true), 2);
            // GameState 3: Death
            this.addWidget(new SimpleTextWidget(333 / 2, 232 / 2 - 40, "", 0xFFFFFFFF, () -> "terminal.maze.death.1", true), 3);
            this.addWidget(new SimpleTextWidget(333 / 2, 232 / 2 - 28, "terminal.maze.death.2", 0xFFFFFFFF, () -> String.valueOf(this.getMazesSolved()),true), 3);
            this.addWidget(new SimpleTextWidget(333 / 2, 232 / 2 - 16, "", 0xFFFFFFFF, () -> "terminal.maze.death.3", true), 3);
            this.addWidget(new ClickButtonWidget(323 / 2 - 10, 222 / 2 + 10, 40, 20, "terminal.maze.retry", (clickData -> {
                this.setGameState(1);
                this.setMazesSolved(0);
                MAZE_SIZE = 9;
                speed = 25;
                this.resetGame();
            })).setShouldClientCallback(true), 3);
        }
        return this;
    }

    public void addWidget(Widget widget, int... visibleStates) {
        this.addWidget(widget);
        for (int state : visibleStates) {
            FSM[state].add(widget);
        }
        widget.setVisible(Arrays.stream(visibleStates).allMatch(state->state==gameState));
    }

    public void setPlayer(PlayerWidget player) {
        this.player = player;
        this.addWidget(player, 1, 2, 3);
    }

    public void setMaze(MazeWidget maze) {
        this.maze = maze;
        this.addWidget(maze, 1, 2, 3);
    }

    public void setEnemy(EnemyWidget enemy) {
        this.enemy = enemy;
        this.addWidget(enemy, 1, 2, 3);
    }

    @Override
    public boolean canOpenMenuOnEdge() {
        return gameState != 1;
    }

    @Override
    public boolean isClientSideApp() {
        return true;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        int lastState = gameState;
        if (gameState == 1) {
            if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
                gameState = 2;
                lastPausePress = true;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) ^ Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
                    attemptMovePlayer(0); // Left
                else
                    attemptMovePlayer(1); // Right
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_UP) ^ Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                if (Keyboard.isKeyDown(Keyboard.KEY_UP))
                    attemptMovePlayer(2); // Up
                else
                    attemptMovePlayer(3); // Down
            }
            timer++;
            if (enemy.posX < 0 && timer % (speed * MAZE_SIZE - 1) < 1) {
                enemy.setGridPosition(0, 0);
            } else if (timer % speed < 1) {
                moveEnemy();
            }
            if (enemy.posX == player.posX && enemy.posY == player.posY) {
                gameState = 3;
            }
        }
        if (gameState == 2) {
            if(!Keyboard.isKeyDown(Keyboard.KEY_P))
                lastPausePress = false;
            if(Keyboard.isKeyDown(Keyboard.KEY_P) && !lastPausePress)
                gameState = 1;
        }
        if (gameState != lastState) {
            FSM[lastState].forEach(widget -> widget.setVisible(false));
            FSM[gameState].forEach(widget -> widget.setVisible(true));
        }
    }

    public int getGameState() {
        return gameState;
    }

    public void setGameState(int gameState) {
        if (gameState != this.gameState) {
            FSM[this.gameState].forEach(widget -> widget.setVisible(false));
            FSM[gameState].forEach(widget -> widget.setVisible(true));
        }
        this.gameState = gameState;
    }

    public int getRenderX(int posX) {
        return this.maze.getSelfPosition().x + posX * 10;
    }

    public int getRenderY(int posY) {
        return this.maze.getSelfPosition().y + posY * 10;
    }

    public int getTimer() {
        return timer;
    }

    private void attemptMovePlayer(int direction) {
        if (timer < lastPlayerInput + 2) {
            return;
        }
        lastPlayerInput = timer;

        // Did the player reach the end?
        if (player.posX == MAZE_SIZE - 1 && player.posY == MAZE_SIZE - 1 && direction == 3) {
            mazesSolved++;
            speed *= 0.95;
            if (mazesSolved % 4 == 0) {
                MAZE_SIZE += 2;
                speed *= 1.07;
            }
            resetGame();
            return;
        }

        if (direction == 0 && !maze.isThereWallAt(player.posX, player.posY, false)) {
            player.move(-1, 0);
            if (movementStore.size() > 0 && movementStore.get(movementStore.size() - 1) == 1) {
                movementStore.remove(movementStore.size() - 1);
            } else {
                movementStore.add(direction);
            }
        } else if (direction == 1 && !maze.isThereWallAt(player.posX + 1, player.posY, false)) {
            player.move(1, 0);
            if (movementStore.size() > 0 && movementStore.get(movementStore.size() - 1) == 0) {
                movementStore.remove(movementStore.size() - 1);
            } else {
                movementStore.add(direction);
            }
        } else if (direction == 2 && !maze.isThereWallAt(player.posX, player.posY, true)) {
            player.move(0, -1);
            if (movementStore.size() > 0 && movementStore.get(movementStore.size() - 1) == 3) {
                movementStore.remove(movementStore.size() - 1);
            } else {
                movementStore.add(direction);
            }
        } else if (direction == 3 && !maze.isThereWallAt(player.posX, player.posY + 1, true)) {
            player.move(0, 1);
            if (movementStore.size() > 0 && movementStore.get(movementStore.size() - 1) == 2) {
                movementStore.remove(movementStore.size() - 1);
            } else {
                movementStore.add(direction);
            }
        }
    }

    private void moveEnemy() { // Move enemy with the latest movements
        if (enemy.posX < 0 || movementStore.isEmpty())
            return;

        int direction = movementStore.get(0);
        if (direction == 0) {
            enemy.move(-1, 0);
        } else if (direction == 1) {
            enemy.move(1, 0);
        } else if (direction == 2) {
            enemy.move(0, -1);
        } else if (direction == 3) {
            enemy.move(0, 1);
        }
        movementStore.remove(0);
    }

    private void resetGame() {
        player.setGridPosition(0, 0);
        maze.recalculateSize();
        maze.initMaze();
        movementStore.clear();
        timer = 0;
        lastPlayerInput = -5;
        enemy.setGridPosition(-100, -100);
    }

    public int getMazesSolved() {
        return mazesSolved;
    }

    public void setMazesSolved(int mazesSolved) {
        this.mazesSolved = mazesSolved;
    }
}
