package gregtech.common.terminal.app.game.maze.widget;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.common.terminal.app.game.maze.MazeApp;

public class PlayerWidget extends Widget {
    protected MazeApp app;
    public int posX;
    public int posY;
    public PlayerWidget(int posX, int posY, MazeApp app) {
        super(app.getRenderX(posX), app.getRenderY(posY), 10, 10);
        this.app = app;
        this.posX = posX;
        this.posY = posY;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        this.setSelfPosition(new Position(app.getRenderX(posX), app.getRenderY(posY)));
        drawSolidRect(this.getPosition().x, this.getPosition().y, 10, 10, 0xAAAAAAFF);
    }

    public void move(int deltaX, int deltaY) {
        this.posX += deltaX;
        this.posY += deltaY;
    }


    public void setGridPosition(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
    }
}
