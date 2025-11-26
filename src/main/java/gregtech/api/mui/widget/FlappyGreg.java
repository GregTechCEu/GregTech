package gregtech.api.mui.widget;

import gregtech.api.GTValues;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.sync.SingleActionSyncHandler;
import gregtech.api.util.Rectangle;
import gregtech.client.utils.RenderUtil;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Area;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

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
    protected final List<Rectangle> obstacles = new ObjectArrayList<>(MAX_OBSTACLES * 2);
    protected float obstacleWidth;

    protected static final float GREG_SIZE = 18.0f;
    protected Rectangle gregArea = new Rectangle();
    protected float gregYSpeed = TERMINAL_VELOCITY;

    @Nullable
    protected SingleActionSyncHandler syncHandler;
    protected boolean showStart = true;
    protected boolean setup = false;
    protected boolean init = false;
    protected boolean won = false;
    protected boolean collided = false;

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
        if (!setup) {
            setup = true;
            // Has to be in onResized instead of onInit since it depends on the size of the widget.
            initializeState();
        }
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof SingleActionSyncHandler;
    }

    /**
     * Set the action to take on the server when the client finishes the game.
     */
    public FlappyGreg onFinish(@NotNull Runnable onFinish) {
        if (this.syncHandler == null) {
            this.syncHandler = new SingleActionSyncHandler();
        }

        syncHandler.serverAction(onFinish);
        setSyncHandler(syncHandler);
        return this;
    }

    @SideOnly(Side.CLIENT)
    protected void onFinish() {
        collided = false;
        if (syncHandler != null) {
            syncHandler.notifyServer();
        }
    }

    protected boolean shouldUpdateGame() {
        return !init && !won && !collided && GTValues.isClientSide();
    }

    protected void initializeState() {
        collided = false;
        obstacles.clear();

        Area area = getArea();
        int width = area.width;
        int height = area.height;

        gregArea = new Rectangle((width / 3.0f) - (GREG_SIZE / 2.0f), (height / 2.0f) - (GREG_SIZE / 2.0f), GREG_SIZE);

        float lastXPos = width * 0.95f;
        this.obstacleWidth = width / 20.0f;
        for (int i = 0; i < MAX_OBSTACLES; i++) {
            Rectangle top = new Rectangle();
            obstacles.add(top);

            Rectangle bottom = new Rectangle();
            obstacles.add(bottom);

            top.setX(lastXPos);
            bottom.setX(lastXPos);
            top.setWidth(obstacleWidth);
            bottom.setWidth(obstacleWidth);

            final float topYEnd = (height * 0.55f) - (height * 0.20f * GTValues.RNG.nextFloat());
            top.setHeight(topYEnd);

            final float bottomYStart = topYEnd + (gregArea.getHeight() * 1.75f);
            bottom.setHeight(height - bottomYStart);
            bottom.setY(bottomYStart);

            lastXPos += (width / 2.5f) + ((width / 15.0f) * GTValues.RNG.nextFloat());
        }

        init = true;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!shouldUpdateGame()) return;

        updateGregPosition();
        checkObstacleCollisions();
        if (!collided) {
            updateObstaclePositions();
            checkWinState();
        }
    }

    @SideOnly(Side.CLIENT)
    protected void updateGregPosition() {
        if (gregArea.getY() < (getArea().height - gregArea.getHeight())) {
            gregArea.setY(gregArea.getY() + gregYSpeed);
        }

        if (gregYSpeed < TERMINAL_VELOCITY) {
            gregYSpeed = Math.min(gregYSpeed + GRAVITY, TERMINAL_VELOCITY);
        }
    }

    @SideOnly(Side.CLIENT)
    protected void checkObstacleCollisions() {
        for (Rectangle obstacle : obstacles) {
            if (obstacle.collides(gregArea, OBSTACLE_MOVEMENT_SPEED)) {
                collided = true;
                break;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    protected void updateObstaclePositions() {
        for (Rectangle obstacle : obstacles) {
            obstacle.decrementX(OBSTACLE_MOVEMENT_SPEED);
        }
    }

    @SideOnly(Side.CLIENT)
    protected void checkWinState() {
        if (obstacles.isEmpty()) return;

        Rectangle obstacle = obstacles.get(obstacles.size() - 1);
        if ((obstacle.getX() + obstacleWidth + WIN_DISTANCE) <= gregArea.getX()) {
            won = true;
            collided = false;
            onFinish();
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        Area area = getArea();
        int screenX = area.x;
        int screenY = area.y;
        int width = area.width;
        int height = area.height;

        // Background
        GuiDraw.drawRect(0, 0, width, height, BACKGROUND_COLOR);

        // Obstacles
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        RenderUtil.applyScissor(screenX, screenY, width, height);
        for (Rectangle obstacle : obstacles) {
            GuiDraw.drawRect(obstacle.getX(), obstacle.getY(), obstacle.getWidth(), obstacle.getHeight(),
                    OBSTACLE_COLOR);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // "Character"
        GTGuiTextures.GREGTECH_LOGO.draw(gregArea.getX(), gregArea.getY(), gregArea.getWidth(), gregArea.getHeight());

        if (showStart) {
            WidgetTheme theme = widgetTheme.getTheme();
            startMessage.draw(context, 0, (height / 2), width, 9, theme);
        }

        if (collided) {
            GlStateManager.enableBlend();
            GuiDraw.drawRect(0, 0, width, height, 0x75FFFFFF & BACKGROUND_COLOR);
            GlStateManager.disableBlend();

            WidgetTheme theme = widgetTheme.getTheme();
            deathMessage.draw(context, 0, (height / 2) - 5, width, 9, theme);
            respawnMessage.draw(context, 0, (height / 2) + 5, width, 9, theme);
        }

        if (won) {
            WidgetTheme theme = widgetTheme.getTheme();
            wonMessage.draw(context, 0, (height / 2), width, 9, theme);
        }
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (showStart) {
            showStart = false;
        }

        if (init) {
            init = false;
        }

        if (collided) {
            collided = false;
            initializeState();
        }

        if (shouldUpdateGame()) {
            gregYSpeed = -CLICK_IMPULSE;
            return Result.SUCCESS;
        } else {
            return Result.IGNORE;
        }
    }
}
