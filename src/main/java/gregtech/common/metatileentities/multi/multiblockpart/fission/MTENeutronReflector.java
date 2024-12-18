package gregtech.common.metatileentities.multi.multiblockpart.fission;

import gregtech.api.fission.component.ComponentDirection;
import gregtech.api.fission.component.FissionComponent;
import gregtech.api.fission.component.FissionComponentData;
import gregtech.api.fission.reactor.ReactorPathWalker;
import gregtech.api.fission.reactor.pathdata.NeutronPathData;
import gregtech.api.fission.reactor.pathdata.ReactivityPathData;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class MTENeutronReflector extends AbstractMTEFissionComponent<FissionComponentData> {

    private final DirectionState[] directions = new DirectionState[ComponentDirection.VALUES.length];
    private int inputs;
    private int outputs;

    public MTENeutronReflector(@NotNull ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        Arrays.fill(directions, DirectionState.NONE);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MTENeutronReflector(metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(@NotNull EntityPlayer entityPlayer) {
        return ModularUI.defaultBuilder()
                .label(5, 5, getMetaFullName())
                .widget(new CycleButtonWidget(176 / 2 - 9 - 36, 40, 18, 18,
                        DirectionState.class,
                        () -> directions[ComponentDirection.LEFT.ordinal()],
                        v -> updateDirection(v, ComponentDirection.LEFT)))
                .widget(new CycleButtonWidget(176 / 2 + 9 + 18, 40, 18, 18,
                        DirectionState.class,
                        () -> directions[ComponentDirection.RIGHT.ordinal()],
                        v -> updateDirection(v, ComponentDirection.RIGHT)))
                .widget(new CycleButtonWidget(176 / 2 - 9, 5 + 9, 18, 18,
                        DirectionState.class,
                        () -> directions[ComponentDirection.UP.ordinal()],
                        v -> updateDirection(v, ComponentDirection.UP)))
                .widget(new CycleButtonWidget(176 / 2 - 9, 84 - 18 - 9, 18, 18,
                        DirectionState.class,
                        () -> directions[ComponentDirection.DOWN.ordinal()],
                        v -> updateDirection(v, ComponentDirection.DOWN)))
                .bindPlayerInventory(entityPlayer.inventory)
                .build(getHolder(), entityPlayer);
    }

    private void updateDirection(@NotNull DirectionState state, @NotNull ComponentDirection direction) {
        if (isLocked()) {
            return;
        }
        int i = direction.ordinal();

        DirectionState existing = directions[i];
        switch (existing) {
            case IN -> inputs--;
            case OUT -> outputs--;
        }

        directions[i] = state;
        switch (state) {
            case IN -> inputs++;
            case OUT -> outputs++;
        }
    }

    @Override
    public void reduceDurability(int amount) {}

    @Override
    public int durability() {
        return 0;
    }

    @Override
    public void processNeutronPath(@NotNull ReactorPathWalker walker, @NotNull List<NeutronPathData> neutronData,
                                   @NotNull List<ReactivityPathData> reactivityData, @NotNull FissionComponent source,
                                   @NotNull ComponentDirection direction, int r, int c, float neutrons) {
        int index = direction.ordinal();
        if (directions[index] != DirectionState.IN) {
            // neutron hits an internal wall, is thrown away
            return;
        }

        float nextNeutrons = inputs < outputs ? neutrons / outputs : neutrons;
        for (int i = 0; i < directions.length; i++) {
            if (directions[i] == DirectionState.OUT) {
                // neutron is redirected to a new direction
                walker.walkPath(neutronData, reactivityData, source, ComponentDirection.VALUES[i], r, c, nextNeutrons);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        byte[] arr = new byte[ComponentDirection.VALUES.length];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (byte) directions[i].ordinal();
        }
        data.setByteArray("directions", arr);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        byte[] arr = data.getByteArray("directions");
        for (int i = 0; i < arr.length; i++) {
            updateDirection(DirectionState.VALUES[arr[i]], ComponentDirection.VALUES[i]);
        }
    }

    private enum DirectionState implements IStringSerializable {

        IN,
        OUT,
        NONE;

        public static final DirectionState[] VALUES = values();

        @Override
        public @NotNull String getName() {
            return name();
        }
    }
}
