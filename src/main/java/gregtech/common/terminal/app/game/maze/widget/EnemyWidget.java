package gregtech.common.terminal.app.game.maze.widget;

import gregtech.api.gui.IRenderContext;
import gregtech.api.util.Position;
import gregtech.common.terminal.app.game.maze.MazeApp;

public class EnemyWidget extends PlayerWidget {

    public EnemyWidget(int posX, int posY, MazeApp app) {
        super(posX, posY, app);
    }

    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        this.setSelfPosition(new Position(app.getRenderX(posX), app.getRenderY(posY)));
        drawSolidRect(this.getPosition().x, this.getPosition().y, 10, 10, 0xFFFFAAAA);
    }
}
