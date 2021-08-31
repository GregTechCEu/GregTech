package gregtech.common.terminal.app.game.pong.widget;

import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.util.Position;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

public class BallWidget extends ImageWidget {
    public double theta;
    private double xAccurate;
    private double yAccurate;

    public BallWidget(int xPosition, int yPosition) {
        super(xPosition, yPosition, 8, 8, new TextureArea(new ResourceLocation("gregtech:textures/gui/widget/pong_ball.png"), 0, 0, 8, 8));
        theta = (Math.PI / 2);
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
        this.setSelfPosition(new Position((int) xAccurate, (int) yAccurate));
    }

    public Pair<Double, Double> getDoubleSelfPosition() {
        return Pair.of(xAccurate, yAccurate);
    }
}
