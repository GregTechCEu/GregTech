package gregtech.common.terminal.app.game.pong.widget;

import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.util.Position;

import net.minecraft.util.ResourceLocation;

import org.lwjgl.util.vector.Vector2f;

public class BallWidget extends ImageWidget {

    public double theta;
    private double xAccurate;
    private double yAccurate;

    public BallWidget(int xPosition, int yPosition) {
        super(xPosition, yPosition, 8, 8,
                new TextureArea(new ResourceLocation("gregtech:textures/gui/widget/pong_ball.png"), 0, 0, 1, 1));
        theta = (Math.random() > 0.5 ? Math.PI : Math.PI / 2) + Math.random() * 0.2;
        xAccurate = xPosition;
        yAccurate = yPosition;
    }

    @Override
    public void setSelfPosition(Position selfPosition) {
        super.setSelfPosition(selfPosition);
        xAccurate = selfPosition.x;
        yAccurate = selfPosition.y;
    }

    @Override
    public Position addSelfPosition(int addX, int addY) {
        xAccurate += addX;
        yAccurate += addY;
        return super.addSelfPosition(addX, addY);
    }

    public void addSelfPosition(double addX, double addY) {
        xAccurate += addX;
        yAccurate += addY;
        super.setSelfPosition(new Position((int) xAccurate, (int) yAccurate));
    }

    public Vector2f getPreciseSelfPosition() {
        return new Vector2f((float) xAccurate, (float) yAccurate);
    }
}
