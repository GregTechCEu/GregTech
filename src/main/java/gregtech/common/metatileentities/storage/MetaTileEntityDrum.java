package gregtech.common.metatileentities.storage;

import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.ThermalFluidHandlerItemStack;
import gregtech.api.fluids.MaterialFluid;
import gregtech.api.fluids.fluidType.FluidType;
import gregtech.api.fluids.fluidType.FluidTypes;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_AUTO_OUTPUT;
import static gregtech.api.recipes.ModHandler.isMaterialWood;
import static gregtech.api.unification.material.info.MaterialFlags.FLAMMABLE;

public class MetaTileEntityDrum extends MetaTileEntity {

    private final int tankSize;
    private final Material material;
    private FilteredFluidHandler fluidTank;
    private boolean isAutoOutput = false;

    public MetaTileEntityDrum(ResourceLocation metaTileEntityId, Material material, int tankSize) {
        super(metaTileEntityId);
        this.tankSize = tankSize;
        this.material = material;
        if (!isMaterialWood(material) && !this.material.hasProperty(PropertyKey.FLUID_PIPE)) {
            throw new IllegalArgumentException(String.format("Material %s requires FluidPipePropety for Drums", material));
        }
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDrum(metaTileEntityId, material, tankSize);
    }

    @Override
    public int getLightOpacity() {
        return 1;
    }

    @Override
    public int getActualComparatorValue() {
        FluidTank fluidTank = this.fluidTank;
        int fluidAmount = fluidTank.getFluidAmount();
        int maxCapacity = fluidTank.getCapacity();
        float f = fluidAmount / (maxCapacity * 1.0f);
        return MathHelper.floor(f * 14.0f) + (fluidAmount > 0 ? 1 : 0);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public String getHarvestTool() {
        return isMaterialWood(material) ? ToolClasses.AXE : ToolClasses.WRENCH;
    }

    @Override
    public boolean hasFrontFacing() {
        return false;
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.fluidTank = new FilteredFluidHandler(tankSize)
                .setFillPredicate(stack -> {
                    if (stack == null || stack.getFluid() == null) return false;

                    Fluid fluid = stack.getFluid();
                    if (isMaterialWood(material)) {
                        boolean meetsGTRequirements = true;
                        if (fluid instanceof MaterialFluid) {
                            FluidType fluidType = ((MaterialFluid) fluid).getFluidType();
                            meetsGTRequirements = fluidType != FluidTypes.ACID && fluidType != FluidTypes.PLASMA;
                        }
                        return fluid.getTemperature() <= 340 && !fluid.isGaseous() && meetsGTRequirements;
                    }

                    FluidPipeProperties pipeProperties = material.getProperty(PropertyKey.FLUID_PIPE);
                    if (fluid.getTemperature() > pipeProperties.getMaxFluidTemperature()) return false;
                    // fluids less than 120K are cryogenic
                    if (fluid.getTemperature() < 120 && !pipeProperties.isCryoProof()) return false;
                    if (fluid.isGaseous() && !pipeProperties.isGasProof()) return false;

                    if (fluid instanceof MaterialFluid) {
                        FluidType fluidType = ((MaterialFluid) fluid).getFluidType();
                        if (fluidType == FluidTypes.ACID && !pipeProperties.isAcidProof()) return false;
                        if (fluidType == FluidTypes.PLASMA && !pipeProperties.isPlasmaProof()) return false;
                    }
                    return true;
                });
        this.fluidInventory = fluidTank;
    }

    @Override
    public void initFromItemStackData(NBTTagCompound itemStack) {
        super.initFromItemStackData(itemStack);
        if (itemStack.hasKey(FluidHandlerItemStack.FLUID_NBT_KEY, Constants.NBT.TAG_COMPOUND)) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(itemStack.getCompoundTag(FluidHandlerItemStack.FLUID_NBT_KEY));
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
        if (isMaterialWood(material) || material.hasFlag(FLAMMABLE)) {
            return new ThermalFluidHandlerItemStack(itemStack, tankSize, 340, false, false, false, false);
        }

        FluidPipeProperties pipeProperties = material.getProperty(PropertyKey.FLUID_PIPE);
        return new ThermalFluidHandlerItemStack(itemStack, tankSize,
                pipeProperties.getMaxFluidTemperature(),
                pipeProperties.isGasProof(),
                pipeProperties.isAcidProof(),
                pipeProperties.isCryoProof(),
                pipeProperties.isPlasmaProof());
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
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
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        FluidStack fluidStack = null;
        if (buf.readBoolean()) {
            try {
                NBTTagCompound tagCompound = buf.readCompoundTag();
                fluidStack = FluidStack.loadFluidStackFromNBT(tagCompound);
            } catch (IOException ignored) {
            }
        }
        fluidTank.setFluid(fluidStack);
        isAutoOutput = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
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
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        return getWorld().isRemote || (!playerIn.isSneaking() && FluidUtil.interactWithFluidHandler(playerIn, hand, fluidTank));
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing wrenchSide, CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (getWorld().isRemote) {
                scheduleRenderUpdate();
                return true;
            }
            playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.drum." + (isAutoOutput ? "disable" : "enable") + "_output"));
            toggleOutput();
            return true;
        }
        return super.onScrewdriverClick(playerIn, hand, wrenchSide, hitResult);
    }

    private void toggleOutput() {
        isAutoOutput = !isAutoOutput;
        if (!getWorld().isRemote) {
            notifyBlockUpdate();
            writeCustomData(UPDATE_AUTO_OUTPUT, buf -> buf.writeBoolean(isAutoOutput));
            markDirty();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        if(isMaterialWood(material)) {
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
        if(isMaterialWood(material)) {
            ColourMultiplier multiplier = new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
            Textures.WOODEN_DRUM.render(renderState, translation, ArrayUtils.add(pipeline, multiplier), getFrontFacing());
        } else {
            ColourMultiplier multiplier = new ColourMultiplier(ColourRGBA.multiply(GTUtility.convertRGBtoOpaqueRGBA_CL(material.getMaterialRGB()), GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
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

        if (ModHandler.isMaterialWood(material)) {
            tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", 340));
            tooltip.add(I18n.format("gregtech.fluid_pipe.not_gas_proof"));
        } else {
            FluidPipeProperties pipeProperties = material.getProperty(PropertyKey.FLUID_PIPE);
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", pipeProperties.getMaxFluidTemperature()));
                if (pipeProperties.isGasProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.gas_proof"));
                if (pipeProperties.isAcidProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.acid_proof"));
                if (pipeProperties.isCryoProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.cryo_proof"));
                if (pipeProperties.isPlasmaProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.plasma_proof"));
            } else {
                tooltip.add(I18n.format("gregtech.tooltip.fluid_pipe_hold_shift"));
            }
        }

        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null && tagCompound.hasKey("Fluid", Constants.NBT.TAG_COMPOUND)) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tagCompound.getCompoundTag("Fluid"));
            if (fluidStack == null) return;
            tooltip.add(I18n.format("gregtech.machine.fluid_tank.fluid", fluidStack.amount, I18n.format(fluidStack.getUnlocalizedName())));
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("FluidInventory", ((FluidTank) fluidInventory).writeToNBT(new NBTTagCompound()));
        data.setBoolean("AutoOutput", isAutoOutput);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        ((FluidTank) this.fluidInventory).readFromNBT(data.getCompoundTag("FluidInventory"));
        isAutoOutput = data.getBoolean("AutoOutput");
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false;
    }

}
