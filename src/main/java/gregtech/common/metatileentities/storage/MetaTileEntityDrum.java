package gregtech.common.metatileentities.storage;

import gregtech.api.capability.IPropertyFluidFilter;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FilteredItemHandler;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.GTFluidHandlerItemStack;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_AUTO_OUTPUT;

public class MetaTileEntityDrum extends MetaTileEntity {

    private final int tankSize;
    private final Material material;
    private FilteredFluidHandler fluidTank;
    private IFluidHandler outputFluidInventory;
    private boolean isAutoOutput = false;
    private boolean allowInputfromOutput = false;

    public MetaTileEntityDrum(ResourceLocation metaTileEntityId, Material material, int tankSize) {
        super(metaTileEntityId);
        this.tankSize = tankSize;
        this.material = material;
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDrum(metaTileEntityId, material, tankSize);
    }

    @Override
    public String getHarvestTool() {
        return ModHandler.isMaterialWood(material) ? ToolClasses.AXE : ToolClasses.WRENCH;
    }

    @Override
    public boolean hasFrontFacing() {
        return false;
    }

    @Override
    protected void initializeInventory() {
        if (this.material == null) return; // call before field initialization, should be called later with fields set
        super.initializeInventory();
        IPropertyFluidFilter filter = this.material.getProperty(PropertyKey.FLUID_PIPE);
        if (filter == null) {
            throw new IllegalArgumentException(
                    String.format("Material %s requires FluidPipeProperty for Drums", material));
        }
        this.fluidInventory = this.fluidTank = new FilteredFluidHandler(tankSize).setFilter(filter);
        this.outputFluidInventory = new FluidHandlerProxy(new FluidTankList(false), fluidInventory);
    }

    @Override
    public void initFromItemStackData(NBTTagCompound itemStack) {
        super.initFromItemStackData(itemStack);
        if (itemStack.hasKey(FluidHandlerItemStack.FLUID_NBT_KEY, Constants.NBT.TAG_COMPOUND)) {
            FluidStack fluidStack = FluidStack
                    .loadFluidStackFromNBT(itemStack.getCompoundTag(FluidHandlerItemStack.FLUID_NBT_KEY));
            fluidTank.setFluid(fluidStack);
        }
    }

    @Override
    public void writeItemStackData(NBTTagCompound itemStack) {
        super.writeItemStackData(itemStack);
        FluidStack fluidStack = fluidTank.getFluid();
        if (fluidStack != null && fluidStack.amount > 0) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            fluidStack.writeToNBT(tagCompound);
            itemStack.setTag(FluidHandlerItemStack.FLUID_NBT_KEY, tagCompound);
        }
    }

    @Override
    public ICapabilityProvider initItemStackCapabilities(ItemStack itemStack) {
        return new GTFluidHandlerItemStack(itemStack, tankSize).setFilter(this.fluidTank.getFilter());
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        FluidStack fluidStack = fluidTank.getFluid();
        buf.writeBoolean(fluidStack != null);
        if (fluidStack != null) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            fluidStack.writeToNBT(tagCompound);
            buf.writeCompoundTag(tagCompound);
        }
        buf.writeBoolean(isAutoOutput);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        FluidStack fluidStack = null;
        if (buf.readBoolean()) {
            try {
                NBTTagCompound tagCompound = buf.readCompoundTag();
                fluidStack = FluidStack.loadFluidStackFromNBT(tagCompound);
            } catch (IOException ignored) {}
        }
        fluidTank.setFluid(fluidStack);
        isAutoOutput = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_AUTO_OUTPUT) {
            this.isAutoOutput = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if (isAutoOutput && getOffsetTimer() % 5 == 0) {
                pushFluidsIntoNearbyHandlers(EnumFacing.DOWN);
            }
        }
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (playerIn.getHeldItem(hand).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return getWorld().isRemote ||
                    (!playerIn.isSneaking() && FluidUtil.interactWithFluidHandler(playerIn, hand, fluidTank));
        }
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onSoftMalletClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                     CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (getWorld().isRemote) {
                scheduleRenderUpdate();
                return true;
            }
            playerIn.sendStatusMessage(new TextComponentTranslation(
                    "gregtech.machine.drum." + (isAutoOutput ? "disable" : "enable") +
                            "_output"),
                    true);
            toggleOutput(!isAutoOutput);
            return true;
        }
        return super.onScrewdriverClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing wrenchSide,
                                      CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (!getWorld().isRemote) {
                playerIn.sendStatusMessage(new TextComponentTranslation(
                        "gregtech.machine.basic.input_from_output_side." +
                                (allowInputfromOutput ? "allow" : "disallow")),
                        true);
                toggleInputFromOutput();
                return true;
            }
        }
        return super.onScrewdriverClick(playerIn, hand, wrenchSide, hitResult);
    }

    private boolean getAutoOutput() {
        return isAutoOutput;
    }

    private void toggleOutput(boolean output) {
        if (isAutoOutput == output) return;
        isAutoOutput = output;
        if (!getWorld().isRemote) {
            notifyBlockUpdate();
            writeCustomData(UPDATE_AUTO_OUTPUT, buf -> buf.writeBoolean(isAutoOutput));
            markDirty();
        }
    }

    private void toggleInputFromOutput() {
        allowInputfromOutput = !allowInputfromOutput;
        if (!getWorld().isRemote) {
            markDirty();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        if (ModHandler.isMaterialWood(material)) {
            return Pair.of(Textures.WOODEN_DRUM.getParticleTexture(), getPaintingColorForRendering());
        } else {
            int color = ColourRGBA.multiply(
                    GTUtility.convertRGBtoOpaqueRGBA_CL(material.getMaterialRGB()),
                    GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
            color = GTUtility.convertOpaqueRGBA_CLtoRGB(color);
            return Pair.of(Textures.DRUM.getParticleTexture(), color);
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if (ModHandler.isMaterialWood(material)) {
            ColourMultiplier multiplier = new ColourMultiplier(
                    GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
            Textures.WOODEN_DRUM.render(renderState, translation, ArrayUtils.add(pipeline, multiplier),
                    getFrontFacing());
        } else {
            ColourMultiplier multiplier = new ColourMultiplier(
                    ColourRGBA.multiply(GTUtility.convertRGBtoOpaqueRGBA_CL(material.getMaterialRGB()),
                            GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
            Textures.DRUM.render(renderState, translation, ArrayUtils.add(pipeline, multiplier), getFrontFacing());
            Textures.DRUM_OVERLAY.render(renderState, translation, pipeline);
        }

        if (isAutoOutput) {
            Textures.STEAM_VENT_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
        }
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", tankSize));
        FluidPipeProperties pipeProperties = material.getProperty(PropertyKey.FLUID_PIPE);
        pipeProperties.appendTooltips(tooltip, true, true);

        if (TooltipHelper.isShiftDown()) {
            tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
            tooltip.add(I18n.format("gregtech.tool_action.soft_mallet.auto_output_down"));
            tooltip.add(I18n.format("gregtech.tool_action.screwdriver.auto_output_covers"));
            tooltip.add(I18n.format("gregtech.tool_action.crowbar"));
        }

        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null && tagCompound.hasKey("Fluid", Constants.NBT.TAG_COMPOUND)) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tagCompound.getCompoundTag("Fluid"));
            if (fluidStack == null) return;
            tooltip.add(I18n.format("gregtech.machine.fluid_tank.fluid", fluidStack.amount,
                    fluidStack.getFluid().getLocalizedName(fluidStack)));
        }
    }

    // Override this so that we can control the "Hold SHIFT" tooltip manually
    @Override
    public boolean showToolUsages() {
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("FluidInventory", ((FluidTank) fluidInventory).writeToNBT(new NBTTagCompound()));
        data.setBoolean("AutoOutput", isAutoOutput);
        data.setBoolean("AllowInputFromOutput", allowInputfromOutput);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        ((FluidTank) this.fluidInventory).readFromNBT(data.getCompoundTag("FluidInventory"));
        isAutoOutput = data.getBoolean("AutoOutput");
        allowInputfromOutput = data.getBoolean("AllowInputFromOutput");
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            IFluidHandler fluidHandler = (side == EnumFacing.DOWN && allowInputfromOutput) ? outputFluidInventory :
                    fluidInventory;
            if (fluidHandler.getTankProperties().length > 0) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
            }
            return null;
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false;
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new FilteredItemHandler(this, 1).setFillPredicate(
                FilteredItemHandler.getCapabilityFilter(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY));
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new GTItemStackHandler(this, 1);
    }
}
