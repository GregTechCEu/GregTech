package gregtech.common.terminal.app.game.pong;

import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.Position;
import gregtech.api.util.TwoDimensionalRayTracer;
import gregtech.common.terminal.app.game.pong.widget.BallWidget;
import gregtech.common.terminal.app.game.pong.widget.PaddleWidget;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class PongApp extends AbstractApplication {
    public static final TextureArea ICON = TextureArea.fullImage("textures/gui/terminal/pong/icon.png");

    private BallWidget ball;
    private int leftScore;
    private int rightScore;
    private List<PaddleWidget> paddles = new ArrayList<>();
    private List<Rectangle> solidObjects = new ArrayList<>();
    private int userInput = -1;
    private int timer = 0;

    public PongApp() {
        super("pong", ICON);
    }

    @Override
    public AbstractApplication createApp(TerminalOSWidget os, boolean isClient, NBTTagCompound nbt) {
        PongApp app = new PongApp();
        if (isClient) {
            app.setOs(os);
            app.addWidget(new ImageWidget(5, 5, 333 - 10, 232 - 10, TerminalTheme.COLOR_B_2));
            app.addWidget(new ImageWidget(333 / 2 - 4, 5, 6, 232 - 10, new ColorRectTexture(0xAAAAAAAA)));
            app.setBall(new BallWidget(333 / 2 - 1, 232 / 2 - 1));
            app.addWidget(new SimpleTextWidget(50, 20, "", 0xAAAAAA, () -> String.valueOf(app.getScore(true)), true));
            app.addWidget(new SimpleTextWidget(283, 20, "", 0xAAAAAA, () -> String.valueOf(app.getScore(false)), true));
            app.initPaddles();
        }
        return app;
    }

    @Override
    public boolean isClientSideApp() {
        return true;
    }

    public void setBall(BallWidget ball) {
        this.ball = ball;
        this.addWidget(ball);
    }

    public void initPaddles() {
        paddles.add(new PaddleWidget(20, 232 / 2 - 1, 4, 20, (PaddleWidget paddle) -> this.getUserInput()));
        paddles.add(new PaddleWidget(313, 232 / 2 - 1, 4, 20, this::simplePaddleAI));
        paddles.forEach(this::addWidget);
        this.solidObjects.add(new Rectangle(0, 0, 333, 10));
        this.solidObjects.add(new Rectangle(0, 222, 333, 10));
    }

    public void score(boolean side) {
        if (side) {
            leftScore++;
            ball.theta = (float) Math.PI;
        } else {
            rightScore++;
            ball.theta = (float) 0;
        }
        ball.theta += Math.random() * 0.2;
        ball.setSelfPosition(new Position(333 / 2 - 1, 232 / 2 - 1));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        timer++;
        if (Keyboard.isKeyDown(Keyboard.KEY_UP) ^ Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
            if(Keyboard.isKeyDown(Keyboard.KEY_UP))
                userInput = 1;
            else
                userInput = 0;
        } else {
            userInput = -1;
        }
        if (ball.getSelfPosition().getX() < 10) {
            this.score(false); // Right side gains a point
        } else if (ball.getSelfPosition().getX() > 323) {
            this.score(true); // Left side gains a point
        } else {
            paddles.forEach((paddle) -> solidObjects.add(new Rectangle(paddle.toSelfRectangleBox())));
            boolean interacted = false;
            for (Rectangle object : solidObjects) {
                TwoDimensionalRayTracer.TwoDimensionalRayTraceResult result = TwoDimensionalRayTracer.intersectBoxSegment(
                        new Vector2f(ball.getSelfPosition().x, ball.getSelfPosition().y),
                        new Vector2f((float) (Math.cos(ball.theta) * 6), (float) (Math.sin(ball.theta) * 6)),
                        new Vector2f((float) object.getCenterX(), (float) object.getCenterY()),
                        new Vector2f(4 + object.width / 2, 4 + object.height / 2));
                if (result != null && !interacted) {
                    ball.addSelfPosition((Math.cos(ball.theta) * 6 * result.time), (Math.sin(ball.theta) * 6 * result.time));

                    float angleMod = 0;
                    if(result.pos.y < object.getCenterY() - 2) {
                        angleMod -= Math.signum(result.normal.x) * 0.6;
                    } else if (result.pos.x > object.getCenterY() + 2) {
                        angleMod += Math.signum(result.normal.x) * 0.6;
                    }
                    ball.theta = (float) (Math.acos(result.normal.x) * 2 - ball.theta + Math.PI + angleMod) % (2 * Math.PI); // Reflects with a slight angle modification.

                    if(ball.theta > Math.PI / 2 - 0.5 && ball.theta < Math.PI / 2 + 0.5) {
                        if(ball.theta <= Math.PI / 2)
                            ball.theta = Math.PI / 2 - 0.51;
                        else
                            ball.theta = Math.PI / 2 + 0.51;
                    }
                    if(ball.theta > 3 * Math.PI / 2 - 0.5 && ball.theta < 3 * Math.PI / 2 + 0.5) {
                        if(ball.theta < 3 * Math.PI / 2)
                            ball.theta = 3 * Math.PI / 2 - 0.51;
                        else
                            ball.theta = 3 * Math.PI / 2 + 0.51;
                    }
                    ball.addSelfPosition((Math.cos(ball.theta) * 8 * (1 - result.time)), (Math.sin(ball.theta) * 8 * (1 - result.time)));
                    interacted = true;
                }
            }
            if(!interacted)
                ball.addSelfPosition((Math.cos(ball.theta) * 6), (Math.sin(ball.theta) * 6));
            solidObjects.remove(2); solidObjects.remove(2);
        }
    }

    public int simplePaddleAI(PaddleWidget paddle) {
        if(this.timer % 3 == 0)
            return -1;
        if((ball.getSelfPosition().getY() + 2 * paddle.getSelfPosition().getY()) / 3 < paddle.getSelfPosition().getY())
            return 1;
        else if ((ball.getSelfPosition().getY() + 2 * paddle.getSelfPosition().getY()) / 3 > paddle.getSelfPosition().getY())
            return 0;
        return -1;
    }

    public int getScore(boolean side) {
        return side ? leftScore : rightScore;
    }

    public int getUserInput() {
        return userInput;
    }
}
