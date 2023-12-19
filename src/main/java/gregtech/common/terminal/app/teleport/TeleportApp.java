package gregtech.common.terminal.app.teleport;

import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.widgets.*;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.os.SystemCall;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.TeleportHandler;
import gregtech.common.entities.PortalEntity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;

public class TeleportApp extends AbstractApplication {

    private int coordinateX = 0;
    private int coordinateY = 1;
    private int coordinateZ = 0;

    private int dimension = 0;

    public TeleportApp() {
        super("teleport");
    }

    @Override
    public AbstractApplication initApp() {
        if (nbt != null && nbt.hasKey("LastTeleport")) {
            BlockPos pos = BlockPos.fromLong(nbt.getLong("LastTeleport"));
            this.coordinateX = pos.getX();
            this.coordinateY = pos.getY();
            this.coordinateZ = pos.getZ();
            this.dimension = nbt.getShort("LastDim");
        }

        // background
        this.addWidget(new ImageWidget(5, 5, 323, 212, new ColorRectTexture(TerminalTheme.COLOR_B_2.getColor())));
        int textFieldColor = TerminalTheme.COLOR_B_2.getColor();
        textFieldColor &= 0xFFFFFF; // remove alpha
        textFieldColor |= (200 << 24); // alpha 175
        // text field backgrounds
        this.addWidget(new ImageWidget(9, 104, 77, 10, new ColorRectTexture(textFieldColor)));
        this.addWidget(new ImageWidget(9, 64, 77, 10, new ColorRectTexture(textFieldColor)));
        this.addWidget(new ImageWidget(9, 44, 77, 10, new ColorRectTexture(textFieldColor)));
        this.addWidget(new ImageWidget(9, 24, 77, 10, new ColorRectTexture(textFieldColor)));
        // text field labels
        this.addWidget(new LabelWidget(10, 15, "X: ", 0xFFFFFF));
        this.addWidget(new LabelWidget(10, 35, "Y: ", 0xFFFFFF));
        this.addWidget(new LabelWidget(10, 55, "Z: ", 0xFFFFFF));
        this.addWidget(
                new SimpleTextWidget(10, 95, "terminal.teleporter.dimension", 0xFFFFFF, () -> "").setCenter(false));

        this.addWidget(new TextFieldWidget2(10, 105, 75, 16, () -> String.valueOf(dimension), value -> {
            if (!value.isEmpty()) {
                dimension = Integer.parseInt(value);
            }
        }).setMaxLength(9).setNumbersOnly(Short.MIN_VALUE, Short.MAX_VALUE));
        this.addWidget(new TextFieldWidget2(10, 65, 75, 16, () -> String.valueOf(coordinateZ), value -> {
            if (!value.isEmpty()) {
                coordinateZ = Integer.parseInt(value);
            }
        }).setMaxLength(9).setNumbersOnly(-30000000, 30000000));
        this.addWidget(new TextFieldWidget2(10, 45, 75, 16, () -> String.valueOf(coordinateY), value -> {
            if (!value.isEmpty()) {
                coordinateY = Integer.parseInt(value);
            }
        }).setMaxLength(9).setNumbersOnly(1, 255));
        this.addWidget(new TextFieldWidget2(10, 25, 75, 16, () -> String.valueOf(coordinateX), value -> {
            if (!value.isEmpty()) {
                coordinateX = Integer.parseInt(value);
            }
        }).setMaxLength(9).setNumbersOnly(-30000000, 30000000));

        this.addWidget(new ClickButtonWidget(15, 140, 65, 20, "terminal.teleporter.spawn_portal",
                data -> this.spawnPortals()));

        return this;
    }

    @Override
    public NBTTagCompound closeApp() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong("LastTeleport", new BlockPos(coordinateX, coordinateY, coordinateZ).toLong());
        nbt.setShort("LastDim", (short) dimension);
        return nbt;
    }

    /**
     * Creates two portals, one 5 blocks in front of the player targeting the other portal, the other at the destination
     * targeting the first portal
     */
    public void spawnPortals() {
        Vec3d position = new Vec3d(
                gui.entityPlayer.getPosition().getX() + gui.entityPlayer.getLookVec().x * 5,
                gui.entityPlayer.getPosition().getY(),
                gui.entityPlayer.getPosition().getZ() + gui.entityPlayer.getLookVec().z * 5);

        PortalEntity portal1 = new PortalEntity(gui.entityPlayer.getEntityWorld(), position.x, position.y, position.z);
        portal1.setRotation(gui.entityPlayer.rotationYaw, 0.F);

        PortalEntity portal2 = new PortalEntity(gui.entityPlayer.getEntityWorld(), coordinateX, coordinateY,
                coordinateZ);
        portal2.setRotation(gui.entityPlayer.rotationYaw, 0.F);

        portal1.setTargetCoordinates(dimension, coordinateX, coordinateY, coordinateZ);
        portal2.setTargetCoordinates(gui.entityPlayer.dimension, position.x, position.y, position.z);

        gui.entityPlayer.getEntityWorld().spawnEntity(portal1);
        Chunk destination = TeleportHandler.getWorldByDimensionID(dimension).getChunkProvider()
                .provideChunk(coordinateX >> 4, coordinateZ >> 4);
        TeleportHandler.getWorldByDimensionID(dimension).spawnEntity(portal2);
        TeleportHandler.getWorldByDimensionID(dimension).getChunkProvider().queueUnload(destination);

        SystemCall.SHUT_DOWN.call(getOs(), isClient);
    }
}
