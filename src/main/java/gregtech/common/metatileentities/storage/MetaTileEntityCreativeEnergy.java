package gregtech.common.metatileentities.storage;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.ILaserContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.LongSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static gregtech.api.GTValues.MAX;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_ACTIVE;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_IO_SPEED;

public class MetaTileEntityCreativeEnergy extends MetaTileEntity implements ILaserContainer, IControllable {

    private long voltage = GTValues.V[GTValues.ULV];
    private long amps = 1;

    private int setTier = 0;
    private boolean active = false;
    private boolean source = true;

    private long lastEnergyIOPerSec = 0;
    private long energyIOPerSec = 0;

    private long ampsReceived = 0;
    private boolean doExplosion = false;

    public MetaTileEntityCreativeEnergy() {
        super(GTUtility.gregtechId("infinite_energy"));
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] renderPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        Textures.VOLTAGE_CASINGS[14].render(renderState, translation, renderPipeline, Cuboid6.full);
        for (EnumFacing face : EnumFacing.VALUES) {
            Textures.INFINITE_EMITTER_FACE.renderSided(face, renderState, translation, pipeline);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[this.setTier].getParticleSprite(), this.getPaintingColorForRendering());
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCreativeEnergy();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(this);
        } else if (capability == GregtechTileCapabilities.CAPABILITY_LASER) {
            return GregtechTileCapabilities.CAPABILITY_LASER.cast(this);
        } else if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        } else {
            return super.getCapability(capability, side);
        }
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager panelSyncManager) {
        ModularPanel panel = GTGuis.createPanel(this, 176, 140);

        DoubleSyncValue tierSync = SyncHandlers.doubleNumber(() -> setTier, val -> {
            setTier = (int) val;
            voltage = GTValues.V[setTier];
        });
        LongSyncValue voltageSync = SyncHandlers.longNumber(() -> voltage, val -> {
            voltage = val;
            setTier = GTUtility.getTierByVoltage(voltage);
        });
        LongSyncValue ampSync = SyncHandlers.longNumber(() -> amps, val -> amps = val);
        BooleanSyncValue activeSync = SyncHandlers.bool(() -> active, this::setActive);
        BooleanSyncValue sourceSync = SyncHandlers.bool(() -> source, val -> {
            source = val;
            if (source) {
                voltage = GTValues.V[GTValues.ULV];
                amps = 1;
                setTier = GTValues.ULV;
            } else {
                voltage = GTValues.V[MAX];
                amps = Integer.MAX_VALUE;
                setTier = MAX;
            }
        });

        panel.child(Flow.column()
                .margin(7)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .childPadding(4)
                .child(new SliderWidget()
                        .widthRel(1.0f)
                        .sliderWidth(30)
                        .bounds(0, GTValues.V.length - 1)
                        .stopper(1)
                        .value(tierSync)
                        .background(new Rectangle()
                                .setColor(Color.GREY.darker(1))
                                .asIcon()
                                .margin(8, 0)
                                .height(4))
                        .stopperTexture(GuiTextures.BUTTON_CLEAN.asIcon()
                                .size(2, 8))
                        .sliderTexture(IDrawable.of(GuiTextures.BUTTON_CLEAN,
                                IKey.dynamic(() -> GTValues.VNF[(int) tierSync.getDoubleValue()]))))
                .child(IKey.lang("gregtech.creative.energy.voltage")
                        .asWidget())
                .child(new TextFieldWidget()
                        .widthRel(1.0f)
                        .height(16)
                        .value(voltageSync)
                        .setNumbersLong(() -> 0L, () -> Long.MAX_VALUE)
                        .setMaxLength(19)
                        .background(GTGuiTextures.DISPLAY))
                .child(IKey.lang("gregtech.creative.energy.amperage")
                        .asWidget())
                .child(Flow.row()
                        .widthRel(1.0f)
                        .coverChildrenHeight()
                        .child(new ButtonWidget<>()
                                .size(20)
                                .onMousePressed(mouse -> {
                                    long amps = ampSync.getLongValue();
                                    if (amps > 0) {
                                        ampSync.setLongValue(amps - 1);
                                    }
                                    return true;
                                })
                                .overlay(IKey.str("-"))
                                .addTooltipLine(IKey.lang("gregtech.creative.energy.amps_minus")))
                        .child(new TextFieldWidget()
                                .height(20)
                                .expanded()
                                .margin(4, 0)
                                .value(ampSync)
                                .setNumbersLong(() -> 0L, () -> Long.MAX_VALUE)
                                .setMaxLength(10)
                                .background(GTGuiTextures.DISPLAY))
                        .child(new ButtonWidget<>()
                                .size(20)
                                .onMousePressed(mouse -> {
                                    long amps = ampSync.getLongValue();
                                    if (amps < Long.MAX_VALUE) {
                                        ampSync.setLongValue(amps + 1);
                                    }
                                    return true;
                                })
                                .overlay(IKey.str("+"))
                                .addTooltipLine(IKey.lang("gregtech.creative.energy.amps_plus"))))
                .child(IKey.lang("gregtech.creative.energy.io",
                        () -> new Object[] { TextFormattingUtil.formatNumbers(lastEnergyIOPerSec) })
                        .asWidget())
                .child(Flow.row()
                        .coverChildrenHeight()
                        .child(new ToggleButton()
                                .size(77, 20)
                                .value(activeSync)
                                .overlay(IKey.lang(() -> activeSync.getBoolValue() ? "gregtech.creative.activity.on" :
                                        "gregtech.creative.activity.off")))
                        .child(new ToggleButton()
                                .size(77, 20)
                                .align(Alignment.CenterRight)
                                .value(sourceSync)
                                .overlay(IKey.lang(() -> sourceSync.getBoolValue() ? "gregtech.creative.energy.source" :
                                        "gregtech.creative.energy.sink")))));

        return panel;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!getWorld().isRemote) {
            writeCustomData(GregtechDataCodes.UPDATE_ACTIVE, buf -> buf.writeBoolean(active));
            markDirty();
        }
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.creative_tooltip.1") + TooltipHelper.RAINBOW +
                I18n.format("gregtech.creative_tooltip.2") + I18n.format("gregtech.creative_tooltip.3"));
    }

    @Override
    public long getOutputPerSec() {
        return lastEnergyIOPerSec;
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) return;
        if (getOffsetTimer() % 20 == 0) {
            this.setIOSpeed(energyIOPerSec);
            energyIOPerSec = 0;
            if (doExplosion) {
                getWorld().createExplosion(null, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5,
                        1, false);
                doExplosion = false;
            }
        }
        ampsReceived = 0;
        if (!active || !source || voltage <= 0 || amps <= 0) return;
        long ampsUsed = 0;
        for (EnumFacing facing : EnumFacing.values()) {
            EnumFacing opposite = facing.getOpposite();
            TileEntity tile = getNeighbor(facing);
            if (tile != null) {
                IEnergyContainer container = tile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER,
                        opposite);
                // Try to get laser capability
                if (container == null) {
                    container = tile.getCapability(GregtechTileCapabilities.CAPABILITY_LASER, opposite);
                }

                if (container == null || !container.inputsEnergy(opposite) || container.getEnergyCanBeInserted() == 0) {
                    continue;
                }

                ampsUsed += container.acceptEnergyFromNetwork(opposite, voltage, amps - ampsUsed);
                if (ampsUsed >= amps) {
                    break;
                }
            }
        }
        energyIOPerSec += ampsUsed * voltage;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setLong("Voltage", voltage);
        data.setLong("Amps", amps);
        data.setByte("Tier", (byte) setTier);
        data.setBoolean("Active", active);
        data.setBoolean("Source", source);
        data.setLong("EnergyIOPerSec", lastEnergyIOPerSec);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        voltage = data.getLong("Voltage");
        // Amps used to be an int, check for that
        if (data.hasKey("Amps", 3)) {
            amps = data.getInteger("Amps");
        } else {
            amps = data.getLong("Amps");
        }
        setTier = data.getByte("Tier");
        active = data.getBoolean("Active");
        source = data.getBoolean("Source");
        if (data.hasKey("EnergyIOPerSec"))
            lastEnergyIOPerSec = data.getLong("EnergyIOPerSec");
        super.readFromNBT(data);
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        if (source || !active || ampsReceived >= amps) {
            return 0;
        }
        if (voltage > this.voltage) {
            if (doExplosion)
                return 0;
            doExplosion = true;
            return Math.min(amperage, getInputAmperage() - ampsReceived);
        }
        long amperesAccepted = Math.min(amperage, getInputAmperage() - ampsReceived);
        if (amperesAccepted > 0) {
            ampsReceived += amperesAccepted;
            energyIOPerSec += amperesAccepted * voltage;
            return amperesAccepted;
        }
        return 0;
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        return !source;
    }

    @Override
    public boolean outputsEnergy(EnumFacing side) {
        return source;
    }

    @Override
    public long changeEnergy(long differenceAmount) {
        if (source || !active) {
            return 0;
        }
        energyIOPerSec += differenceAmount;
        return differenceAmount;
    }

    @Override
    public long getEnergyStored() {
        return 69;
    }

    @Override
    public long getEnergyCapacity() {
        return 420;
    }

    @Override
    public long getInputAmperage() {
        return source ? 0 : amps;
    }

    @Override
    public long getInputVoltage() {
        return source ? 0 : voltage;
    }

    @Override
    public long getOutputVoltage() {
        return source ? voltage : 0;
    }

    @Override
    public long getOutputAmperage() {
        return source ? amps : 0;
    }

    public void setIOSpeed(long energyIOPerSec) {
        if (this.lastEnergyIOPerSec != energyIOPerSec) {
            this.lastEnergyIOPerSec = energyIOPerSec;
            this.writeCustomData(UPDATE_IO_SPEED, packetBuffer -> packetBuffer.writeLong(energyIOPerSec));
        }
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_IO_SPEED) {
            this.lastEnergyIOPerSec = buf.readLong();
        } else if (dataId == UPDATE_ACTIVE) {
            this.active = buf.readBoolean();
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(active);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.active = buf.readBoolean();
    }

    @Override
    public boolean isWorkingEnabled() {
        return active;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        setActive(isWorkingAllowed);
    }
}
