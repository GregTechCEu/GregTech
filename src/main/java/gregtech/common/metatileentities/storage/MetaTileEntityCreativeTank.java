package gregtech.common.metatileentities.storage;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.custom.QuantumStorageRenderer;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.mui.widget.GTFluidSlot;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankPropertiesWrapper;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityCreativeTank extends MetaTileEntityQuantumTank {

    private int mBPerCycle = 1;
    private int ticksPerCycle = 1;
    private boolean active = false;
    private FluidTank modifiableTank;

    public MetaTileEntityCreativeTank(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.MAX, -1);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.modifiableTank = new FluidTank(1);
        this.fluidTank = new CreativeFluidTank(this.modifiableTank);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.QUANTUM_STORAGE_RENDERER.renderMachine(renderState, translation,
                ArrayUtils.add(pipeline,
                        new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))),
                this);
        Textures.CREATIVE_CONTAINER_OVERLAY.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        if (this.getOutputFacing() != null) {
            Textures.PIPE_OUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
            if (!isConnected() && active) {
                Textures.FLUID_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
            }
        }
        QuantumStorageRenderer.renderTankFluid(renderState, translation, pipeline, this.fluidTank, getWorld(), getPos(),
                getFrontFacing());
        renderIndicatorOverlay(renderState, translation, pipeline);
    }

    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if (this.getWorld() != null && !GTUtility.isEmpty(this.fluidTank.getFluid()))
            QuantumStorageRenderer.renderTankAmount(x, y, z, frontFacing, 69);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCreativeTank(this.metaTileEntityId);
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
        return appendCreativeUI(GTGuis.createPanel(this, 176, 166), true,
                new BoolValue.Dynamic(() -> active, b -> active = b),
                new IntSyncValue(() -> mBPerCycle, v -> mBPerCycle = v),
                new IntSyncValue(() -> ticksPerCycle, v -> ticksPerCycle = v))
                        .child(IKey.lang("gregtech.creative.tank.fluid").asWidget()
                                .pos(7, 9))
                        .child(new GTFluidSlot()
                                .syncHandler(GTFluidSlot.sync(this.modifiableTank)
                                        .phantom(true)
                                        .showAmount(false, false))
                                .pos(36, 6));
    }

    @Override
    public void update() {
        super.update();
        if (ticksPerCycle == 0 || getOffsetTimer() % ticksPerCycle != 0 || fluidTank.getFluid() == null ||
                getWorld().isRemote || !active || isConnected())
            return;

        TileEntity tile = getNeighbor(getOutputFacing());
        if (tile != null) {
            IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                    getOutputFacing().getOpposite());
            if (fluidHandler == null || fluidHandler.getTankProperties().length == 0)
                return;

            fluidHandler.fill(fluidTank.drain(mBPerCycle, false), true);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("mBPerCycle", mBPerCycle);
        data.setInteger("TicksPerCycle", ticksPerCycle);
        data.setBoolean("Active", active);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        mBPerCycle = data.getInteger("mBPerCycle");
        ticksPerCycle = data.getInteger("TicksPerCycle");
        active = data.getBoolean("Active");
        super.readFromNBT(data);
    }

    @Override
    public void initFromItemStackData(NBTTagCompound itemStack) {
        super.initFromItemStackData(itemStack);
        mBPerCycle = itemStack.getInteger("mBPerCycle");
        ticksPerCycle = itemStack.getInteger("ticksPerCycle");
    }

    @Override
    public void writeItemStackData(NBTTagCompound itemStack) {
        super.writeItemStackData(itemStack);
        itemStack.setInteger("mBPerCycle", mBPerCycle);
        itemStack.setInteger("ticksPerCycle", ticksPerCycle);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.creative_tooltip.1") + TooltipHelper.RAINBOW +
                I18n.format("gregtech.creative_tooltip.2") + I18n.format("gregtech.creative_tooltip.3"));
        // do not append the normal tooltips
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.fluidTank);
        }
        return super.getCapability(capability, side);
    }

    private class CreativeFluidTank extends FluidTank {

        private final FluidTank internal;

        public CreativeFluidTank(FluidTank internal) {
            super(1);
            this.internal = internal;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            if (this.tankProperties == null) {
                this.tankProperties = new IFluidTankProperties[] {
                        new FluidTankPropertiesWrapper(this.internal) {

                            @Override
                            public int getCapacity() {
                                return mBPerCycle;
                            }

                            @Override
                            public FluidStack getContents() {
                                if (!active) return null;
                                return GTUtility.copy(getCapacity(), getFluid());
                            }

                            @Override
                            public boolean canDrainFluidType(FluidStack fluidStack) {
                                return active && super.canDrainFluidType(fluidStack);
                            }
                        }
                };
            }
            return this.tankProperties;
        }

        @Override
        public FluidStack getFluid() {
            return this.internal.getFluid();
        }

        @Override
        public int getFluidAmount() {
            return this.internal.getFluidAmount();
        }

        @Override
        public int getCapacity() {
            return mBPerCycle;
        }

        @Override
        public void setFluid(FluidStack fluid) {
            this.internal.setFluid(fluid);
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (!active || GTUtility.isEmpty(resource)) return null;
            return drain(resource.amount, doDrain);
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (!active) return null;
            FluidStack stack = this.internal.drain(maxDrain, false);
            return GTUtility.copy(Math.min(getCapacity(), maxDrain), stack);
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            return this.internal.writeToNBT(nbt);
        }

        @Override
        public FluidTank readFromNBT(NBTTagCompound nbt) {
            this.internal.readFromNBT(nbt);
            return this;
        }
    }
}
