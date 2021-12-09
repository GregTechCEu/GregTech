package gregtech.common.terminal.app.game.pong.widget;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import java.awt.*;
import java.util.function.Function;

public class PaddleWidget extends Widget {
    Function<PaddleWidget, Integer> controlSupplier;

    public PaddleWidget(int x, int y, int width, int height, Function<PaddleWidget, Integer> controlSupplier) {
        super(x, y, width, height);
        this.controlSupplier = controlSupplier;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        drawSolidRect(this.toRectangleBox().x - this.toRectangleBox().width / 2, this.toRectangleBox().y - this.toRectangleBox().height / 2, this.toRectangleBox().width, this.toRectangleBox().height, 0xFFFFFFFF);
    }

    @Override
    public void updateScreenOnFrame() {
        super.updateScreenOnFrame();
        if (this.getSelfPosition().getY() < 30) {
            this.setSelfPosition(new Position(this.getSelfPosition().getX(), 30));
        }
        if (this.getSelfPosition().getY() > 202) {
            this.setSelfPosition(new Position(this.getSelfPosition().getX(), 202));
        }
        int speed;
        switch (controlSupplier.apply(this)) {
            case 0:
                speed = 2;
                break;
            case 1:
                speed = -2;
                break;
            default:
                speed = 0;
        }
        this.addSelfPosition(0, speed);

    }

    public Rectangle toSelfRectangleBox() {
        return new Rectangle(this.getSelfPosition().x - this.toRectangleBox().width / 2 - 2, this.getSelfPosition().y - this.toRectangleBox().height / 2,
                this.toRectangleBox().width, this.toRectangleBox().height);
    }

}
