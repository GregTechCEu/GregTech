package gregtech.common.terminal.app.game.maze;

import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.common.terminal.app.game.maze.widget.EnemyWidget;
import gregtech.common.terminal.app.game.maze.widget.MazeWidget;
import gregtech.common.terminal.app.game.maze.widget.PlayerWidget;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;
import scala.swing.event.Key;

import java.util.ArrayList;
import java.util.List;

public class MazeApp extends AbstractApplication {
    private int gamestate = 0;
    private PlayerWidget player;
    private EnemyWidget enemy;
    private MazeWidget maze;
    private int timer = 0;
    private int mazesSolved = 0;
    private float speed = 25;
    private int lastPlayerInput = -2;
    public static int MAZE_SIZE = 9;
    private List<Integer> movementStore = new ArrayList<>();
    private boolean lastPausePress;

    public MazeApp() {
        super("maze");
    }

    public AbstractApplication createAppInstance(TerminalOSWidget os, boolean isClient, NBTTagCompound nbt) {
        MazeApp app = new MazeApp();
        if (isClient) {
            app.setOs(os);
            app.addWidget(new ImageWidget(5, 5, 333 - 10, 232 - 10, TerminalTheme.COLOR_B_2));
            // Gamestate 0: Title
            app.addWidget(new LabelWidget(333 / 2, 222 / 2 - 50, "Theseus's Escape", 0xFFFFFFFF).setXCentered(true).setVisibilitySupplier(() -> app.getGamestate() == 0));
            app.addWidget(new ClickButtonWidget(323 / 2 - 10, 222 / 2 - 10, 30, 30, "Play",
                    (clickData -> {
                        app.setGamestate(1);
                        app.resetGame();
                    }))
                    .setShouldClientCallback(true).setVisibilitySupplier(() -> app.getGamestate() == 0));
            // Gamestate 1: Play
            app.setMaze((MazeWidget) new MazeWidget().setVisibilitySupplier(() -> app.getGamestate() >= 1));
            app.setPlayer((PlayerWidget) new PlayerWidget(0, 0, app).setVisibilitySupplier(() -> app.getGamestate() >= 1));
            app.setEnemy((EnemyWidget) new EnemyWidget(-100, -100, app).setVisibilitySupplier(() -> app.getGamestate() >= 1));
            // Gamestate 2: Pause
            app.addWidget(new ImageWidget(5, 5, 333 - 10, 232 - 10, new ColorRectTexture(0xFF000000)).setVisibilitySupplier(() -> app.getGamestate() > 1));
            app.addWidget(new ClickButtonWidget(323 / 2 - 10, 222 / 2 - 10, 50, 20, "Continue", (clickData) -> app.setGamestate(1)).setVisibilitySupplier(() -> app.getGamestate() == 2));
            app.addWidget(new LabelWidget(333 / 2, 222 / 2 - 50, "Game Paused", 0xFFFFFFFF).setXCentered(true).setVisibilitySupplier(() -> app.getGamestate() == 2));
            // Gamestate 3: Death
            app.addWidget(new SimpleTextWidget(333 / 2, 232 / 2 - 40, "", 0xFFFFFFFF, () -> "Oh no! You were eaten by the Minotaur!", true).setVisibilitySupplier(() -> app.getGamestate() == 3));
            app.addWidget(new SimpleTextWidget(333 / 2, 232 / 2 - 28, "", 0xFFFFFFFF, () -> "You got through " + app.getMazesSolved() + " mazes before losing.", true).setVisibilitySupplier(() -> app.getGamestate() == 3));
            app.addWidget(new SimpleTextWidget(333 / 2, 232 / 2 - 16, "", 0xFFFFFFFF, () -> "Try again?", true).setVisibilitySupplier(() -> app.getGamestate() == 3));
            app.addWidget(new ClickButtonWidget(323 / 2 - 10, 222 / 2 + 10, 40, 20, "Retry", (clickData -> {
                app.setGamestate(1);
                app.setMazesSolved(0);
                MAZE_SIZE = 9;
                speed = 25;
                app.resetGame();
            })).setShouldClientCallback(true).setVisibilitySupplier(() -> app.getGamestate() == 3));


        }
        return app;
    }

    public void setPlayer(PlayerWidget player) {
        this.player = player;
        this.addWidget(player);
    }

    public void setMaze(MazeWidget maze) {
        this.maze = maze;
        this.addWidget(maze);
    }

    public void setEnemy(EnemyWidget enemy) {
        this.enemy = enemy;
        this.addWidget(enemy);
    }

    @Override
    public boolean isClientSideApp() {
        return true;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (gamestate == 1) {
            if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
                gamestate = 2;
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
                gamestate = 3;
            }
        }
        if (gamestate == 2) {
            if(!Keyboard.isKeyDown(Keyboard.KEY_P))
                lastPausePress = false;
            if(Keyboard.isKeyDown(Keyboard.KEY_P) && !lastPausePress)
                gamestate = 1;
        }
    }

    public int getGamestate() {
        return gamestate;
    }

    public void setGamestate(int gamestate) {
        this.gamestate = gamestate;
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
