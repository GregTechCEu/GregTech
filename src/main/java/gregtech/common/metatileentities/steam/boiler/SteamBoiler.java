package gregtech.common.metatileentities.steam.boiler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.*;
import gregtech.api.gui.widgets.ProgressWidget.MoveType;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.ModHandler;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.SimpleSidedCubeRenderer;
import gregtech.api.render.Textures;
import gregtech.api.util.GTUtility;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;

public abstract class SteamBoiler extends MetaTileEntity {

    private static final EnumFacing[] STEAM_PUSH_DIRECTIONS = ArrayUtils.add(EnumFacing.HORIZONTALS, EnumFacing.UP);
    public static final int BOILING_CYCLE_LENGTH = 25;
    public static final int HIGH_PRESSURE_BOILING_CYCLE_LENGTH = 10;

    public final TextureArea BRONZE_BACKGROUND_TEXTURE;
    public final TextureArea BRONZE_SLOT_BACKGROUND_TEXTURE;

    public final TextureArea SLOT_FURNACE_BACKGROUND;

    protected final boolean isHighPressure;
    protected final int baseSteamOutput;
    private final OrientedOverlayRenderer renderer;

    protected FluidTank waterFluidTank;
    protected FluidTank steamFluidTank;

    private int fuelBurnTimeLeft;
    private int fuelMaxBurnTime;
    private int currentTemperature;
    private boolean hasNoWater;
    private int timeBeforeCoolingDown;

    private boolean isBurning;
    private boolean wasBurningAndNeedsUpdate;
    private ItemStackHandler containerInventory;

    public SteamBoiler(ResourceLocation metaTileEntityId, boolean isHighPressure, OrientedOverlayRenderer renderer, int baseSteamOutput) {
        super(metaTileEntityId);
        this.renderer = renderer;
        this.isHighPressure = isHighPressure;
        this.baseSteamOutput = baseSteamOutput;
        BRONZE_BACKGROUND_TEXTURE = getGuiTexture("%s_gui");
        BRONZE_SLOT_BACKGROUND_TEXTURE = getGuiTexture("slot_%s");
        SLOT_FURNACE_BACKGROUND = getGuiTexture("slot_%s_furnace_background");
        this.containerInventory = new ItemStackHandler(2);
        this.setPaintingColor(0xFFFFFF);
    }

    @SideOnly(Side.CLIENT)
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        if (isHighPressure) {
            return Textures.STEAM_BRICKED_CASING_STEEL;
        } else {
            return Textures.STEAM_BRICKED_CASING_BRONZE;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getBaseRenderer().getParticleSprite(), getPaintingColor());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        getBaseRenderer().render(renderState, translation, colouredPipeline);
        renderer.render(renderState, translation, pipeline, getFrontFacing(), isBurning());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("FuelBurnTimeLeft", fuelBurnTimeLeft);
        data.setInteger("FuelMaxBurnTime", fuelMaxBurnTime);
        data.setInteger("CurrentTemperature", currentTemperature);
        data.setBoolean("HasNoWater", hasNoWater);
        data.setTag("ContainerInventory", containerInventory.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.fuelBurnTimeLeft = data.getInteger("FuelBurnTimeLeft");
        this.fuelMaxBurnTime = data.getInteger("FuelMaxBurnTime");
        this.currentTemperature = data.getInteger("CurrentTemperature");
        this.hasNoWater = data.getBoolean("HasNoWater");
        this.containerInventory.deserializeNBT(data.getCompoundTag("ContainerInventory"));
        this.isBurning = fuelBurnTimeLeft > 0;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isBurning);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isBurning = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 100) {
            this.isBurning = buf.readBoolean();
            getHolder().scheduleChunkForRenderUpdate();
        }
    }

    public void setFuelMaxBurnTime(int fuelMaxBurnTime) {
        this.fuelMaxBurnTime = fuelMaxBurnTime;
        this.fuelBurnTimeLeft = fuelMaxBurnTime;
        if (!getWorld().isRemote) {
            markDirty();
        }
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            updateCurrentTemperature();
            generateSteam();

            if (getOffsetTimer() % 5 == 0) {
                fillInternalTankFromFluidContainer(containerInventory, containerInventory, 0, 1);
                pushFluidsIntoNearbyHandlers(STEAM_PUSH_DIRECTIONS);
            }

            if (fuelMaxBurnTime <= 0) {
                tryConsumeNewFuel();
                if (fuelBurnTimeLeft > 0) {
                    if (wasBurningAndNeedsUpdate) {
                        this.wasBurningAndNeedsUpdate = false;
                    } else setBurning(true);
                }
            }

            if (wasBurningAndNeedsUpdate) {
                this.wasBurningAndNeedsUpdate = false;
                setBurning(false);
            }
        }
    }

    private void updateCurrentTemperature() {
        if (fuelMaxBurnTime > 0) {
            if (getOffsetTimer() % 12 == 0) {
                if (fuelBurnTimeLeft % 2 == 0 && currentTemperature < getMaxTemperate())
                    currentTemperature++;
                fuelBurnTimeLeft -= isHighPressure ? 2 : 1;
                if (fuelBurnTimeLeft == 0) {
                    this.fuelMaxBurnTime = 0;
                    this.timeBeforeCoolingDown = 40;
                    //boiler has no fuel now, so queue burning state update
                    this.wasBurningAndNeedsUpdate = true;
                }
            }
        } else if (timeBeforeCoolingDown == 0) {
            if (currentTemperature > 0)
                currentTemperature--;
        } else --timeBeforeCoolingDown;
    }

    private void generateSteam() {
        if(currentTemperature >= 100) {
            if (getOffsetTimer() % getBoilingCycleLength() == 0) {
                int fillAmount = (int) (baseSteamOutput * (currentTemperature / (getMaxTemperate() * 1.0)));
                boolean hasDrainedWater = waterFluidTank.drain(1, true) != null;
                int filledSteam = 0;
                if (hasDrainedWater) {
                    filledSteam = steamFluidTank.fill(ModHandler.getSteam(fillAmount), true);
                }
                if (this.hasNoWater && hasDrainedWater) {
                    getWorld().setBlockToAir(getPos());
                    getWorld().createExplosion(null,
                        getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5,
                        2.0f, true);
                } else this.hasNoWater = !hasDrainedWater;
                if (filledSteam == 0 && hasDrainedWater) {
                    getWorld().playSound(null, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5,
                        SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    steamFluidTank.drain(4000, true);
                }
            }
        } else {
            this.hasNoWater = false;
        }
    }

    public boolean isBurning() {
        return isBurning;
    }

    public void setBurning(boolean burning) {
        this.isBurning = burning;
        if (!getWorld().isRemote) {
            markDirty();
            writeCustomData(100, buf -> buf.writeBoolean(burning));
        }
    }

    protected abstract void tryConsumeNewFuel();

    public int getMaxTemperate() {
        return isHighPressure ? 1000 : 500;
    }

    public double getTemperaturePercent() {
        return currentTemperature / (getMaxTemperate() * 1.0);
    }

    public double getFuelLeftPercent() {
        return fuelMaxBurnTime == 0 ? 0.0 : fuelBurnTimeLeft / (fuelMaxBurnTime * 1.0);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        this.waterFluidTank = new FilteredFluidHandler(16000).setFillPredicate(ModHandler::isWater);
        return new FluidTankList(false, waterFluidTank);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        this.steamFluidTank = new FluidTank(16000);
        return new FluidTankList(false, steamFluidTank);
    }

    protected TextureArea getGuiTexture(String pathTemplate) {
        String type = isHighPressure ? "steel" : "bronze";
        return TextureArea.fullImage(String.format("textures/gui/steam/%s/%s.png",
            type, pathTemplate.replace("%s", type)));
    }

    public ModularUI.Builder createUITemplate(EntityPlayer player) {
        return ModularUI.builder(BRONZE_BACKGROUND_TEXTURE, 176, 166)
            .widget(new LabelWidget(6, 6, getMetaFullName()))

            .widget(new ProgressWidget(this::getTemperaturePercent, 97, 26, 10, 54)
                .setProgressBar(getGuiTexture("bar_%s_empty"),
                    getGuiTexture("bar_heat"),
                    MoveType.VERTICAL))

            .widget(new TankWidget(waterFluidTank, 84, 26, 10, 54)
                .setBackgroundTexture(getGuiTexture("bar_%s_empty")))
            .widget(new TankWidget(steamFluidTank, 71, 26, 10, 54)
                .setBackgroundTexture(getGuiTexture("bar_%s_empty")))

            .widget(new FluidContainerSlotWidget(containerInventory, 0, 44, 26, true)
                .setBackgroundTexture(BRONZE_SLOT_BACKGROUND_TEXTURE, getGuiTexture("overlay_%s_in")))
            .widget(new SlotWidget(containerInventory, 1, 44, 62, true, false)
                .setBackgroundTexture(BRONZE_SLOT_BACKGROUND_TEXTURE, getGuiTexture("overlay_%s_out")))
            .widget(new ImageWidget(44, 44, 18, 18)
                .setImage(getGuiTexture("overlay_%s_fluid_container")))

            .bindPlayerInventory(player.inventory, BRONZE_SLOT_BACKGROUND_TEXTURE);
    }

    private int getBoilingCycleLength() {
        return isHighPressure ? HIGH_PRESSURE_BOILING_CYCLE_LENGTH : BOILING_CYCLE_LENGTH;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.steam_boiler.tooltip_produces", baseSteamOutput, getBoilingCycleLength()));
    }
}
