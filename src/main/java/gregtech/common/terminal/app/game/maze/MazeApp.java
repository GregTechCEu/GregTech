package gregtech.common.terminal.app.game.maze;

import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.common.terminal.app.game.maze.widget.MazeWidget;
import gregtech.common.terminal.app.game.maze.widget.PlayerWidget;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;

public class MazeApp extends AbstractApplication {
    public static final TextureArea ICON = TextureArea.fullImage("textures/gui/terminal/maze/icon.png");
    private int gamestate = 0;
    private PlayerWidget player;
    private MazeWidget maze;

    public MazeApp() {
        super("maze", ICON);
    }

    @Override
    public AbstractApplication createApp(TerminalOSWidget os, boolean isClient, NBTTagCompound nbt) {
        MazeApp app = new MazeApp();
        if (isClient) {
            app.setOs(os);
            app.addWidget(new ImageWidget(5, 5, 333 - 10, 232 - 10, TerminalTheme.COLOR_B_2));
            // Gamestate 0: Title
            app.addWidget(new ClickButtonWidget(323 / 2 - 10, 222 / 2 - 10, 30, 30, "Play", (clickData -> app.setGamestate(1))).setShouldClientCallback(true).setVisibilitySupplier(() -> app.getGamestate() == 0));
            // Gamestate 1: Play
            app.setMaze((MazeWidget) new MazeWidget().setVisibilitySupplier(() -> app.getGamestate() == 1));
            app.setPlayer((PlayerWidget) new PlayerWidget(5, 5, app).setVisibilitySupplier(() -> app.getGamestate() == 1));
            // Gamestate 2: Pause
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

    @Override
    public boolean isClientSideApp() {
        return true;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (gamestate == 1) {
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

    private void attemptMovePlayer(int direction) {
        if (direction == 0 && !maze.isThereWallAt(player.posX, player.posY, false)) {
            player.move(-1, 0);
        } else if (direction == 1 && !maze.isThereWallAt(player.posX + 1, player.posY, false)) {
            player.move(1, 0);
        } else if (direction == 2 && !maze.isThereWallAt(player.posX, player.posY, true)) {
            player.move(0, -1);
        } else if (direction == 3 && !maze.isThereWallAt(player.posX, player.posY + 1, true)) {
            player.move(0, 1);
        }
    }
}
