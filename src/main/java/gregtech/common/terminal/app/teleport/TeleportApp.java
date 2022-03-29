package gregtech.common.terminal.app.teleport;

import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.os.SystemCall;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.TeleportHandler;
import gregtech.common.entities.PortalEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;

public class TeleportApp extends AbstractApplication {

    private int coordinateX = 0;
    private int coordinateY = 1;
    private int coordinateZ = 0;

    private int dimension = 0;

    public TeleportApp(){
        super("teleport");
    }

    @Override
    public AbstractApplication initApp(){
        this.addWidget(new ImageWidget(10, 100, 75, 8, new ColorRectTexture(TerminalTheme.COLOR_B_2.getColor())));
        this.addWidget(new ImageWidget(10, 60, 75, 8, new ColorRectTexture(TerminalTheme.COLOR_B_2.getColor())));
        this.addWidget(new ImageWidget(10, 40, 75, 8, new ColorRectTexture(TerminalTheme.COLOR_B_2.getColor())));
        this.addWidget(new ImageWidget(10, 20, 75, 8, new ColorRectTexture(TerminalTheme.COLOR_B_2.getColor())));
        this.addWidget(new TextFieldWidget2(10, 100, 75, 16, () -> String.valueOf(dimension), value -> {
            if (!value.isEmpty()) {
                dimension = Integer.parseInt(value);
            }
        }).setMaxLength(9).setNumbersOnly(-30000000, 30000000));
        this.addWidget(new TextFieldWidget2(10, 60, 75, 16, () -> String.valueOf(coordinateZ), value -> {
            if (!value.isEmpty()) {
                coordinateZ = Integer.parseInt(value);
            }
        }).setMaxLength(9).setNumbersOnly(-30000000, 30000000));
        this.addWidget(new TextFieldWidget2(10, 40, 75, 16, () -> String.valueOf(coordinateY), value -> {
            if (!value.isEmpty()) {
                coordinateY = Integer.parseInt(value);
            }
        }).setMaxLength(9).setNumbersOnly(1, 255));
        this.addWidget(new TextFieldWidget2(10, 20, 75, 16, () -> String.valueOf(coordinateX), value -> {
            if (!value.isEmpty()) {
                coordinateX = Integer.parseInt(value);
            }
        }).setMaxLength(9).setNumbersOnly(-30000000, 30000000));

        this.addWidget(new ClickButtonWidget(20, 140, 50, 50, "Engage", data -> this.SpawnPortals()));

        return this;
    }

    /**
     * Creates two portals, one 5 blocks in front of the player targeting the other portal, the other at the destination targeting the first portal
     */
    public void SpawnPortals(){
        Vec3d position = new Vec3d(
                gui.entityPlayer.getPosition().getX() + gui.entityPlayer.getLookVec().x * 5,
                gui.entityPlayer.getPosition().getY(),
                gui.entityPlayer.getPosition().getZ() + gui.entityPlayer.getLookVec().z * 5
        );

        PortalEntity portal1 = new PortalEntity(gui.entityPlayer.getEntityWorld(), position.x, position.y, position.z);
        portal1.setRotation(gui.entityPlayer.rotationYaw, 0.F);

        PortalEntity portal2 = new PortalEntity(gui.entityPlayer.getEntityWorld(), coordinateX, coordinateY, coordinateZ);
        portal2.setRotation(gui.entityPlayer.rotationYaw, 0.F);

        portal1.setTargetCoordinates(dimension, coordinateX, coordinateY, coordinateZ);
        portal2.setTargetCoordinates(gui.entityPlayer.dimension, position.x, position.y, position.z);

        gui.entityPlayer.getEntityWorld().spawnEntity(portal1);
        Chunk destination = TeleportHandler.getWorldByDimensionID(dimension).getChunkProvider().provideChunk(coordinateX >> 4, coordinateZ >> 4);
        TeleportHandler.getWorldByDimensionID(dimension).spawnEntity(portal2);
        TeleportHandler.getWorldByDimensionID(dimension).getChunkProvider().queueUnload(destination);

        SystemCall.SHUT_DOWN.call(getOs(), isClient);

    }

}
