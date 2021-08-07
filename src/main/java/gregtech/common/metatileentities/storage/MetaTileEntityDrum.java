package gregtech.common.metatileentities.storage;

import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.recipes.ModHandler;
import gregtech.api.render.Textures;
import gregtech.api.unification.material.Material;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.GTUtility;
import gregtech.api.util.WatchedFluidTank;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import static gregtech.api.unification.material.info.MaterialFlags.FLAMMABLE;

public class MetaTileEntityDrum extends MetaTileEntity {
    private final int tankSize;
    private final Material material;
    private SyncFluidTank fluidTank;

    public MetaTileEntityDrum(ResourceLocation metaTileEntityId, Material material, int tankSize) {
        super(metaTileEntityId);
        this.tankSize = tankSize;
        this.material = material;
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
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
        return material.toString().contains("wood") ? "axe" : "pickaxe";
    }

    @Override
    public boolean hasFrontFacing() {
        return false;
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.fluidTank = new SyncFluidTank(tankSize);
        this.fluidInventory = fluidTank;
    }

    @Override
    public void initFromItemStackData(NBTTagCompound itemStack) {
        super.initFromItemStackData(itemStack);
        if (itemStack.hasKey(FluidHandlerItemStack.FLUID_NBT_KEY, Constants.NBT.TAG_COMPOUND)) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(itemStack.getCompoundTag(FluidHandlerItemStack.FLUID_NBT_KEY));
            fluidTank.setFluid(fluidStack);
            fluidTank.onContentsChanged();
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
        return new FluidHandlerItemStack(itemStack, tankSize) {
            @Override
            protected void setContainerToEmpty() {
                this.container.setTagCompound(null);
            }
        };
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
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == -200) {
            FluidStack fluidStack = null;
            if (buf.readBoolean()) {
                try {
                    NBTTagCompound tagCompound = buf.readCompoundTag();
                    fluidStack = FluidStack.loadFluidStackFromNBT(tagCompound);
                } catch (IOException ignored) {
                }
            }
            fluidTank.setFluid(fluidStack);
        }
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        return getWorld().isRemote || (FluidUtil.interactWithFluidHandler(playerIn, hand, fluidTank) && !playerIn.isSneaking());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        if (ModHandler.isMaterialWood(material)) {
            return Pair.of(Textures.WOODEN_DRUM.getParticleTexture(), getPaintingColor());
        } else {
            int color = ColourRGBA.multiply(
                    GTUtility.convertRGBtoOpaqueRGBA_CL(material.getMaterialRGB()),
                    GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColor()));
            color = GTUtility.convertOpaqueRGBA_CLtoRGB(color);
            return Pair.of(Textures.DRUM.getParticleTexture(), color);
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if (material.toString().contains("wood")) {
            ColourMultiplier multiplier = new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
            Textures.WOODEN_DRUM.render(renderState, translation, ArrayUtils.add(pipeline, multiplier), getFrontFacing());
        } else {
            ColourMultiplier multiplier = new ColourMultiplier(ColourRGBA.multiply(GTUtility.convertRGBtoOpaqueRGBA_CL(material.getMaterialRGB()), GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
            Textures.DRUM.render(renderState, translation, ArrayUtils.add(pipeline, multiplier), getFrontFacing());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", tankSize));

        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null && tagCompound.hasKey("Fluid", Constants.NBT.TAG_COMPOUND)) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tagCompound.getCompoundTag("Fluid"));
            if (fluidStack == null) return;
            tooltip.add(I18n.format("gregtech.machine.fluid_tank.fluid", fluidStack.amount, I18n.format(fluidStack.getUnlocalizedName())));
            String formula = FluidTooltipUtil.getFluidTooltip(fluidStack);
            if (formula != null)
                tooltip.add(TextFormatting.GRAY + formula);
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
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        ((FluidTank) this.fluidInventory).readFromNBT(data.getCompoundTag("FluidInventory"));
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false;
    }

    private class SyncFluidTank extends WatchedFluidTank {

        public SyncFluidTank(int capacity) {
            super(capacity);
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return !material.toString().contains("wood") && !material.hasFlag(FLAMMABLE) || fluid.getFluid().getTemperature() <= 325;
        }

        @Override
        protected void onFluidChanged(FluidStack newFluidStack, FluidStack oldFluidStack) {
            if (getWorld() != null && !getWorld().isRemote) {
                onContentsChangedOnServer(newFluidStack, oldFluidStack);
            }
        }

        private void onContentsChangedOnServer(FluidStack newFluid, FluidStack oldFluidStack) {

            if (newFluid != null && newFluid.isFluidEqual(oldFluidStack)) {
                writeCustomData(-201, buf -> buf.writeInt(newFluid.amount));
            } else {
                writeCustomData(-200, buf -> {
                    buf.writeBoolean(newFluid != null);
                    if (newFluid != null) {
                        NBTTagCompound tagCompound = new NBTTagCompound();
                        newFluid.writeToNBT(tagCompound);
                        buf.writeCompoundTag(tagCompound);
                    }
                });
            }
        }
    }
}
