package gregtech.common.metatileentities.storage;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

public class MetaTileEntityCreativeEnergy extends MetaTileEntity implements IEnergyContainer {

    private long voltage = 0;
    private int amps = 1;

    private int setTier = 0;
    private boolean active = false;
    private boolean source = true;

    private long lastEnergyOutputPerSec = 0;
    private long lastEnergyInputPerSec = 0;
    private long energyOutputPerSec = 0;
    private long energyInputPerSec = 0;

    private long ampsReceived = 0;
    private boolean doExplosion = false;

    public MetaTileEntityCreativeEnergy() {
        super(new ResourceLocation(GTValues.MODID, "infinite_energy"));
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] renderPipeline = ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        Textures.VOLTAGE_CASINGS[14].render(renderState, translation, renderPipeline, Cuboid6.full);
        for (EnumFacing face : EnumFacing.VALUES) {
            Textures.INFINITE_EMITTER_FACE.renderSided(face, renderState, translation, pipeline);
        }
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[this.setTier].getParticleSprite(), this.getPaintingColorForRendering());
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCreativeEnergy();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER)
            return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.defaultBuilder()
                .widget(new CycleButtonWidget(7, 7, 30, 20, GTValues.VNF, () -> setTier, tier -> {
                    setTier = tier;
                    voltage = GTValues.V[setTier];
                }));
        builder.label(7, 32, "gregtech.creative.energy.voltage");
        builder.widget(new ImageWidget(7, 44, 156, 20, GuiTextures.DISPLAY));
        builder.widget(new TextFieldWidget2(9, 50, 152, 16, () -> String.valueOf(voltage), value -> {
            if (!value.isEmpty()) {
                voltage = Long.parseLong(value);
                setTier = GTUtility.getTierByVoltage(voltage);
            }
        }).setAllowedChars(TextFieldWidget2.NATURAL_NUMS).setMaxLength(19).setValidator(getTextFieldValidator()));

        builder.label(7, 74, "gregtech.creative.energy.amperage");
        builder.widget(new ClickButtonWidget(7, 87, 20, 20, "-", data -> amps = --amps == -1 ? 0 : amps));
        builder.widget(new ImageWidget(29, 87, 118, 20, GuiTextures.DISPLAY));
        builder.widget(new TextFieldWidget2(31, 93, 114, 16, () -> String.valueOf(amps), value -> {
            if (!value.isEmpty()) {
                amps = Integer.parseInt(value);
            }
        }).setMaxLength(10).setNumbersOnly(0, Integer.MAX_VALUE));
        builder.widget(new ClickButtonWidget(149, 87, 20, 20, "+", data -> {
            if (amps < Integer.MAX_VALUE) {
                amps++;
            }
        }));

        builder.dynamicLabel(7, 110, () -> "Energy I/O per sec: " + (source ? lastEnergyOutputPerSec : lastEnergyInputPerSec), 0x232323);

        builder.widget(new CycleButtonWidget(7, 139, 77, 20, () -> active, value -> active = value, "gregtech.creative.activity.off", "gregtech.creative.activity.on"));
        builder.widget(new CycleButtonWidget(85, 139, 77, 20, () -> source, value -> {
            source = value;
            if (source) {
                voltage = 0;
                amps = 0;
                setTier = 0;
            } else {
                voltage = GTValues.V[14];
                amps = Integer.MAX_VALUE;
                setTier = 14;
            }
        }, "Sink", "Source")); //TODO: localisation

        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public long getOutputPerSec() {
        return lastEnergyOutputPerSec;
    }

    @Override
    public void update() {
        super.update();
        if (getOffsetTimer() % 20 == 0) {
            lastEnergyOutputPerSec = energyOutputPerSec;
            lastEnergyInputPerSec = energyInputPerSec;
            energyOutputPerSec = 0;
            energyInputPerSec = 0;
            if (doExplosion) {
                getWorld().createExplosion(null, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5,
                        1, false);
                doExplosion = false;
            }
        }
        ampsReceived = 0;
        if (getWorld().isRemote || !active || !source || voltage <= 0 || amps <= 0) return;
        int ampsUsed = 0;
        for (EnumFacing facing : EnumFacing.values()) {
            EnumFacing opposite = facing.getOpposite();
            TileEntity tile = getWorld().getTileEntity(getPos().offset(facing));
            if (tile != null) {
                IEnergyContainer container = tile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, opposite);
                if (container == null || !container.inputsEnergy(opposite) || container.getEnergyCanBeInserted() == 0)
                    continue;
                ampsUsed += container.acceptEnergyFromNetwork(opposite, voltage, amps - ampsUsed);
                if (ampsUsed >= amps)
                    break;
            }
        }
        energyOutputPerSec += ampsUsed * voltage;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setLong("Voltage", voltage);
        data.setInteger("Amps", amps);
        data.setByte("Tier", (byte) setTier);
        data.setBoolean("Active", active);
        data.setBoolean("Source", source);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        voltage = data.getLong("Voltage");
        amps = data.getInteger("Amps");
        setTier = data.getByte("Tier");
        active = data.getBoolean("Active");
        source = data.getBoolean("Source");
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
            energyInputPerSec += amperesAccepted * voltage;
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
        energyInputPerSec += differenceAmount;
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

    public Function<String, String> getTextFieldValidator() {
        return val -> {
            if (val.isEmpty()) {
                return "0";
            }
            long num;
            try {
                num = Long.parseLong(val);
            } catch (NumberFormatException ignored) {
                return "0";
            }
            if (num < 0) {
                return "0";
            }
            return val;
        };
    }

}
