package gregtech.api.mui.widget;

import gregtech.api.GTValues;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.GTUtility;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Area;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Rectangle2D;
import java.util.List;

public class FlappyGreg extends Widget<FlappyGreg> implements Interactable {

    protected static final int BACKGROUND_COLOR = Color.BLACK.main;
    protected static final float WIN_DISTANCE = 25.0f;

    protected static final float CLICK_IMPULSE = 1.75f;
    protected static final float TERMINAL_VELOCITY = 2.0f;
    protected static final float GRAVITY = 0.25f;

    protected static final int MAX_OBSTACLES = 5;
    protected static final float OBSTACLE_MOVEMENT_SPEED = 2.0f;
    protected static final int OBSTACLE_COLOR = Color.GREEN.main;
    protected final List<Rectangle2D.Float> obstacles = new ObjectArrayList<>(MAX_OBSTACLES * 2);
    protected float obstacleWidth;

    protected static final float GREG_SIZE = 18.0f;
    protected Rectangle2D.Float gregArea = new Rectangle2D.Float();
    protected float gregYSpeed = TERMINAL_VELOCITY;

    @Nullable
    protected Runnable action;
    @NotNull
    protected GameState gameState = GameState.INIT;

    protected IKey startMessage;
    protected IKey deathMessage;
    protected IKey respawnMessage;
    protected IKey wonMessage;

    @Override
    public void onInit() {
        if (!GTValues.isClientSide()) return;

        startMessage = IKey.lang("gregtech.machine.maintenance_hatch.fools.start")
                .style(TextFormatting.WHITE);
        deathMessage = IKey.lang("gregtech.machine.maintenance_hatch.fools.dead")
                .style(TextFormatting.BOLD, TextFormatting.WHITE);
        respawnMessage = IKey.lang("gregtech.machine.maintenance_hatch.fools.respawn")
                .style(TextFormatting.UNDERLINE, TextFormatting.WHITE);
        wonMessage = IKey.lang("gregtech.machine.maintenance_hatch.fools.won")
                .style(TextFormatting.GREEN);
    }

    @Override
    public void onResized() {
        super.onResized();
        if (!GTValues.isClientSide()) return;
        if (gameState == GameState.INIT) {
            // Has to be in onResized instead of onInit since it depends on the size of the widget.
            initializeState();
        }
    }

    public FlappyGreg onFinish(@NotNull Runnable action) {
        this.action = action;
        return this;
    }

    @SideOnly(Side.CLIENT)
    protected void onFinish() {
        if (action != null) {
            action.run();
        }
    }

    protected boolean shouldUpdateGame() {
        return gameState == GameState.RUNNING && GTValues.isClientSide();
    }

    protected void initializeState() {
        obstacles.clear();

        Area area = getArea();
        int width = area.width;
        int height = area.height;

        gregArea.setRect((width / 3.0f) - (GREG_SIZE / 2.0f), (height / 2.0f) - (GREG_SIZE / 2.0f), GREG_SIZE,
                GREG_SIZE);

        float lastXPos = width * 0.95f;
        this.obstacleWidth = width / 20.0f;
        for (int i = 0; i < MAX_OBSTACLES; i++) {
            Rectangle2D.Float top = new Rectangle2D.Float();
            obstacles.add(top);

            Rectangle2D.Float bottom = new Rectangle2D.Float();
            obstacles.add(bottom);

            top.x = lastXPos;
            bottom.x = lastXPos;
            top.width = obstacleWidth;
            bottom.width = obstacleWidth;

            final float topYEnd = (height * 0.55f) - (height * 0.20f * GTValues.RNG.nextFloat());
            top.height = topYEnd;

            final float bottomYStart = topYEnd + (gregArea.height * 1.75f);
            bottom.height = height - bottomYStart;
            bottom.y = bottomYStart;

            lastXPos += (width / 2.5f) + ((width / 15.0f) * GTValues.RNG.nextFloat());
        }

        gameState = GameState.INIT;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!shouldUpdateGame()) return;

        updateGregPosition();
        checkObstacleCollisions();
        if (gameState != GameState.COLLIDED) {
            updateObstaclePositions();
            checkWinState();
        }
    }

    @SideOnly(Side.CLIENT)
    protected void updateGregPosition() {
        if (gregArea.getY() < (getArea().height - gregArea.getHeight())) {
            gregArea.y = gregArea.y + gregYSpeed;
        }

        if (gregYSpeed < TERMINAL_VELOCITY) {
            gregYSpeed = Math.min(gregYSpeed + GRAVITY, TERMINAL_VELOCITY);
        }
    }

    @SideOnly(Side.CLIENT)
    protected void checkObstacleCollisions() {
        Rectangle2D.Float gregTest = new Rectangle2D.Float();
        gregTest.setRect(gregArea);
        gregTest.x += OBSTACLE_MOVEMENT_SPEED;

        for (Rectangle2D.Float obstacle : obstacles) {
            if (GTUtility.rectanglesCollide(obstacle, gregTest)) {
                gameState = GameState.COLLIDED;
                break;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    protected void updateObstaclePositions() {
        for (Rectangle2D.Float obstacle : obstacles) {
            obstacle.x -= OBSTACLE_MOVEMENT_SPEED;
        }
    }

    @SideOnly(Side.CLIENT)
    protected void checkWinState() {
        if (obstacles.isEmpty()) return;

        Rectangle2D.Float obstacle = obstacles.get(obstacles.size() - 1);
        if ((obstacle.x + obstacleWidth + WIN_DISTANCE) <= gregArea.getX()) {
            gameState = GameState.FINISHED;
            onFinish();
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        Area area = getArea();
        int width = area.width;
        int height = area.height;

        // Background
        GuiDraw.drawRect(0, 0, width, height, BACKGROUND_COLOR);

        // Obstacles
        Stencil.applyAtZero(area, context);
        for (Rectangle2D.Float obstacle : obstacles) {
            GuiDraw.drawRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height, OBSTACLE_COLOR);
        }
        Stencil.remove();

        // "Character"
        GTGuiTextures.GREGTECH_LOGO.draw(gregArea.x, gregArea.y, gregArea.width, gregArea.height);

        WidgetTheme theme = widgetTheme.getTheme();
        switch (gameState) {
            case INIT -> startMessage.draw(context, 0, (height / 2), width, 9, theme);
            case COLLIDED -> {
                GlStateManager.enableBlend();
                GuiDraw.drawRect(0, 0, width, height, 0x75FFFFFF & BACKGROUND_COLOR);
                GlStateManager.disableBlend();

                deathMessage.draw(context, 0, (height / 2) - 5, width, 9, theme);
                respawnMessage.draw(context, 0, (height / 2) + 5, width, 9, theme);
            }
            case FINISHED -> wonMessage.draw(context, 0, (height / 2), width, 9, theme);
        }
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (gameState == GameState.FINISHED) return Result.IGNORE;

        switch (gameState) {
            case INIT -> {
                impulseGreg();
                gameState = GameState.RUNNING;
            }
            case RUNNING -> impulseGreg();
            case COLLIDED -> initializeState();
        }

        return Result.SUCCESS;
    }

    protected void impulseGreg() {
        gregYSpeed = -CLICK_IMPULSE;
    }

    protected enum GameState {
        INIT,
        RUNNING,
        COLLIDED,
        FINISHED
    }
}
