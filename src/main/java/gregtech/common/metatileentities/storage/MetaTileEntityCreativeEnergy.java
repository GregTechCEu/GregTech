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
import gregtech.api.mui.IMetaTileEntityGuiHolder;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.api.util.GTUtility;
import gregtech.api.util.KeyUtil;
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
import net.minecraft.util.ResourceLocation;
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
import com.cleanroommc.modularui.api.value.IDoubleValue;
import com.cleanroommc.modularui.api.value.IIntValue;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
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
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_ACTIVE;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_IO_SPEED;

public class MetaTileEntityCreativeEnergy extends MetaTileEntity implements ILaserContainer, IControllable,
                                          IMetaTileEntityGuiHolder {

    private long sourceVoltage = GTValues.V[GTValues.ULV];
    private long sourceAmperage = 1;

    private long sinkVoltage = GTValues.V[GTValues.MAX];
    private long sinkAmperage = Integer.MAX_VALUE;

    private boolean workingEnabled = false;
    private boolean source = true;

    private long lastEnergyIOPerSec = 0;
    private long energyIOPerSec = 0;

    private long ampsReceived = 0;
    private boolean doExplosion = false;

    public MetaTileEntityCreativeEnergy(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCreativeEnergy(metaTileEntityId);
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
        int tier = GTUtility.getTierByVoltage(source ? sourceVoltage : sinkVoltage);
        return Pair.of(Textures.VOLTAGE_CASINGS[tier].getParticleSprite(), this.getPaintingColorForRendering());
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
    public @NotNull ModularPanel buildUI(MetaTileEntityGuiData guiData, PanelSyncManager panelSyncManager,
                                         UISettings settings) {
        LongSyncValue voltageSync = SyncHandlers.longNumber(() -> source ? sourceVoltage : sinkVoltage, val -> {
            if (source) {
                sourceVoltage = val;
            } else {
                sinkVoltage = val;
            }
        });
        LongSyncValue ampSync = new LongSyncValue(() -> source ? sourceAmperage : sinkAmperage, val -> {
            if (source) {
                sourceAmperage = val;
            } else {
                sinkAmperage = val;
            }
        });
        DynamicIntValue tierValue = new DynamicIntValue(() -> GTUtility.getTierByVoltage(voltageSync.getLongValue()),
                tier -> voltageSync.setLongValue(GTValues.V[tier]));
        BooleanSyncValue activeSync = new BooleanSyncValue(this::isWorkingEnabled, this::setWorkingEnabled);
        BooleanSyncValue sourceSync = new BooleanSyncValue(this::isSource, this::setSource);

        return GTGuis.createPanel(this, 176, 143)
                .child(Flow.column()
                        .margin(7)
                        // .crossAxisAlignment(Alignment.CrossAxis.START)
                        .childPadding(4)
                        .child(new SliderWidget()
                                .widthRel(1.0f)
                                .sliderWidth(30)
                                .bounds(0, GTValues.V.length - 1)
                                .stopper(1)
                                .value(tierValue)
                                .background(GTGuiTextures.FLUID_SLOT.asIcon()
                                        .margin(7, 0))
                                .sliderTexture(IDrawable.of(GuiTextures.BUTTON_CLEAN,
                                        IKey.dynamic(() -> GTValues.VNF[tierValue.getIntValue()]))))
                        .child(IKey.lang("gregtech.creative.energy.voltage")
                                .asWidget())
                        .child(new TextFieldWidget()
                                .widthRel(1.0f)
                                .height(20)
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
                                            if (amps == 0) return false;
                                            amps = Math.max(0, amps - GTUtility.getButtonIncrementValue());
                                            ampSync.setLongValue(amps);
                                            return true;
                                        })
                                        .overlay(KeyUtil.createMultiplierKey(false)))
                                .child(new TextFieldWidget()
                                        .height(20)
                                        .expanded()
                                        .margin(4, 0)
                                        .value(ampSync)
                                        .setNumbersLong(() -> 0L, () -> Long.MAX_VALUE)
                                        .setMaxLength(19)
                                        .background(GTGuiTextures.DISPLAY))
                                .child(new ButtonWidget<>()
                                        .size(20)
                                        .onMousePressed(mouse -> {
                                            long amps = ampSync.getLongValue();
                                            if (amps == Long.MAX_VALUE) return false;
                                            long canAdd = Long.MAX_VALUE - amps;
                                            amps += Math.min(GTUtility.getButtonIncrementValue(), canAdd);
                                            ampSync.setLongValue(amps);
                                            return true;
                                        })
                                        .overlay(KeyUtil.createMultiplierKey(true))))
                        .child(IKey.lang("gregtech.creative.energy.io",
                                () -> new Object[] { TextFormattingUtil.formatNumbers(lastEnergyIOPerSec) })
                                .asWidget())
                        .child(Flow.row()
                                .coverChildrenHeight()
                                .child(new ToggleButton()
                                        .size(77, 20)
                                        .value(activeSync)
                                        .overlay(IKey.lang(() -> activeSync.getBoolValue() ?
                                                "gregtech.creative.activity.on" :
                                                "gregtech.creative.activity.off")))
                                .child(new ToggleButton()
                                        .size(77, 20)
                                        .align(Alignment.CenterRight)
                                        .value(sourceSync)
                                        .overlay(IKey.lang(() -> sourceSync.getBoolValue() ?
                                                "gregtech.creative.energy.source" :
                                                "gregtech.creative.energy.sink")))));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(TooltipHelper.CREATIVE_TOOLTIP.get());
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
        if (!workingEnabled || !source || sourceVoltage <= 0 || sourceAmperage <= 0) return;
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

                ampsUsed += container.acceptEnergyFromNetwork(opposite, sourceVoltage, sourceAmperage - ampsUsed);
                if (ampsUsed >= sourceAmperage) {
                    break;
                }
            }
        }

        energyIOPerSec += ampsUsed * sourceVoltage;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setLong("SourceV", sourceVoltage);
        data.setLong("SourceA", sourceAmperage);
        data.setLong("SinkV", sinkVoltage);
        data.setLong("SinkA", sinkAmperage);
        data.setBoolean("Active", workingEnabled);
        data.setBoolean("Source", source);
        data.setLong("EnergyIOPerSec", lastEnergyIOPerSec);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        // Old format
        if (data.hasKey("Voltage")) {
            sourceVoltage = data.getLong("Voltage");
            sourceAmperage = data.getLong("Amps");
        } else {
            sourceVoltage = data.getLong("SourceV");
            sourceAmperage = data.getLong("SourceA");
            sinkVoltage = data.getLong("SinkV");
            sinkAmperage = data.getLong("SinkA");
        }

        workingEnabled = data.getBoolean("Active");
        source = data.getBoolean("Source");
        if (data.hasKey("EnergyIOPerSec"))
            lastEnergyIOPerSec = data.getLong("EnergyIOPerSec");
        super.readFromNBT(data);
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        if (source || !workingEnabled || ampsReceived >= sinkAmperage) {
            return 0;
        }

        if (voltage > this.sourceVoltage) {
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
        if (source || !workingEnabled) {
            return 0;
        }
        energyIOPerSec += differenceAmount;
        return differenceAmount;
    }

    @Override
    public long getEnergyStored() {
        return source ? Long.MAX_VALUE : 0;
    }

    @Override
    public long getEnergyCapacity() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getInputAmperage() {
        return source ? 0 : sinkAmperage;
    }

    @Override
    public long getInputVoltage() {
        return source ? 0 : sinkVoltage;
    }

    @Override
    public long getOutputVoltage() {
        return source ? sourceVoltage : 0;
    }

    @Override
    public long getOutputAmperage() {
        return source ? sourceAmperage : 0;
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
            this.workingEnabled = buf.readBoolean();
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(workingEnabled);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.workingEnabled = buf.readBoolean();
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        this.workingEnabled = isWorkingAllowed;
        if (!getWorld().isRemote) {
            writeCustomData(GregtechDataCodes.UPDATE_ACTIVE, buf -> buf.writeBoolean(workingEnabled));
            markDirty();
        }
    }

    public boolean isSource() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static class DynamicIntValue implements IIntValue<Integer>, IDoubleValue<Integer> {

        private final IntSupplier getter;
        private final IntConsumer setter;

        public DynamicIntValue(IntSupplier getter, IntConsumer setter) {
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public int getIntValue() {
            return this.getter.getAsInt();
        }

        @Override
        public void setIntValue(int val) {
            this.setter.accept(val);
        }

        @Override
        public double getDoubleValue() {
            return getIntValue();
        }

        @Override
        public void setDoubleValue(double val) {
            setIntValue((int) val);
        }

        @Override
        public Integer getValue() {
            return getIntValue();
        }

        @Override
        public void setValue(Integer value) {
            setIntValue(value);
        }

        @Override
        public Class<Integer> getValueType() {
            return Integer.class;
        }
    }
}
