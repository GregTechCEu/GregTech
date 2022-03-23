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

    private int CoordinateX = 0;
    private int CoordinateY = 0;
    private int CoordinateZ = 0;

    private int Dimension = 0;

    public TeleportApp(){
        super("teleport");
    }

    @Override
    public AbstractApplication initApp(){
        this.addWidget(new ImageWidget(10, 100, 75, 8, new ColorRectTexture(TerminalTheme.COLOR_B_2.getColor())));
        this.addWidget(new ImageWidget(10, 60, 75, 8, new ColorRectTexture(TerminalTheme.COLOR_B_2.getColor())));
        this.addWidget(new ImageWidget(10, 40, 75, 8, new ColorRectTexture(TerminalTheme.COLOR_B_2.getColor())));
        this.addWidget(new ImageWidget(10, 20, 75, 8, new ColorRectTexture(TerminalTheme.COLOR_B_2.getColor())));
        this.addWidget(new TextFieldWidget2(10, 100, 75, 16, () -> String.valueOf(Dimension), value -> {
            if (!value.isEmpty()) {
                Dimension = Integer.parseInt(value);
            }
        }).setMaxLength(9).setNumbersOnly(-30000000, 30000000));
        this.addWidget(new TextFieldWidget2(10, 60, 75, 16, () -> String.valueOf(CoordinateZ), value -> {
            if (!value.isEmpty()) {
                CoordinateZ = Integer.parseInt(value);
            }
        }).setMaxLength(9).setNumbersOnly(-30000000, 30000000));
        this.addWidget(new TextFieldWidget2(10, 40, 75, 16, () -> String.valueOf(CoordinateY), value -> {
            if (!value.isEmpty()) {
                CoordinateY = Integer.parseInt(value);
            }
        }).setMaxLength(9).setNumbersOnly(0, 255));
        this.addWidget(new TextFieldWidget2(10, 20, 75, 16, () -> String.valueOf(CoordinateX), value -> {
            if (!value.isEmpty()) {
                CoordinateX = Integer.parseInt(value);
            }
        }).setMaxLength(9).setNumbersOnly(-30000000, 30000000));

        this.addWidget(new ClickButtonWidget(20, 140, 50, 50, "Engage", data -> this.SpawnPortals()));

        return this;
    }

    public void SpawnPortals(){
        /*
        Creates two portals, one 5 blocks in front of the player targeting the other portal, the other at the destination targeting the first portal
         */
        Vec3d position = new Vec3d(
                gui.entityPlayer.getPosition().getX() + gui.entityPlayer.getLookVec().x * 5,
                gui.entityPlayer.getPosition().getY(),
                gui.entityPlayer.getPosition().getZ() + gui.entityPlayer.getLookVec().z * 5
        );

        PortalEntity portal1 = new PortalEntity(gui.entityPlayer.getEntityWorld(), position.x, position.y, position.z);
        portal1.setRotation(gui.entityPlayer.rotationYaw, 0.F);

        PortalEntity portal2 = new PortalEntity(gui.entityPlayer.getEntityWorld(), CoordinateX, CoordinateY, CoordinateZ);
        portal2.setRotation(gui.entityPlayer.rotationYaw, 0.F);

        portal1.setTargetCoordinates(Dimension, CoordinateX, CoordinateY, CoordinateZ);
        portal2.setTargetCoordinates(gui.entityPlayer.dimension, position.x, position.y, position.z);

        gui.entityPlayer.getEntityWorld().spawnEntity(portal1);
        Chunk destination = TeleportHandler.getWorldByDimensionID(Dimension).getChunkProvider().provideChunk(CoordinateX >> 4, CoordinateZ >> 4);
        TeleportHandler.getWorldByDimensionID(Dimension).spawnEntity(portal2);
        TeleportHandler.getWorldByDimensionID(Dimension).getChunkProvider().queueUnload(destination);

        SystemCall.SHUT_DOWN.call(getOs(), isClient);

    }

}
