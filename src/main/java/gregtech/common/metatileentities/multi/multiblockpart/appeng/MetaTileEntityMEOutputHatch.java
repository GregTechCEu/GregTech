package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.mui.drawable.GTObjectDrawable;
import gregtech.api.util.FluidStackHashStrategy;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.GTLog;
import gregtech.api.util.KeyUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.IWrappedStack;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.drawable.text.RichText;
import com.cleanroommc.modularui.utils.serialization.IByteBufDeserializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityMEOutputHatch extends MetaTileEntityMEOutputBase<IAEFluidStack, FluidStack>
                                         implements IMultiblockAbilityPart<IFluidTank> {

    public final static String FLUID_BUFFER_TAG = "FluidBuffer";

    public MetaTileEntityMEOutputHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.EV, IFluidStorageChannel.class);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMEOutputHatch(this.metaTileEntityId);
    }

    @Override
    protected @NotNull IByteBufDeserializer<IWrappedStack<IAEFluidStack, FluidStack>> getDeserializer() {
        return WrappedFluidStack::fromPacket;
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void addStackLine(@NotNull RichText text,
                                @NotNull IWrappedStack<IAEFluidStack, FluidStack> wrappedStack) {
        FluidStack stack = wrappedStack.getDefinition();
        text.add(new GTObjectDrawable(stack, 0)
                .asIcon()
                .asHoverable()
                .tooltip(tooltip -> {
                    tooltip.addLine(KeyUtil.fluid(stack));
                    FluidTooltipUtil.handleFluidTooltip(tooltip, stack);
                }));
        text.space();
        text.addLine(KeyUtil.number(TextFormatting.WHITE, wrappedStack.getStackSize(), "L"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        NBTTagList nbtList = new NBTTagList();
        for (IWrappedStack<IAEFluidStack, FluidStack> stack : internalBuffer) {
            NBTTagCompound stackTag = new NBTTagCompound();
            stack.writeToNBT(stackTag);
            nbtList.appendTag(stackTag);
        }
        data.setTag(FLUID_BUFFER_TAG, nbtList);

        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        for (NBTBase tag : data.getTagList(FLUID_BUFFER_TAG, Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound tagCompound = (NBTTagCompound) tag;

            WrappedFluidStack stack;
            // Migrate from AEFluidStacks to WrappedFluidStacks
            if (tagCompound.getBoolean("wrapped")) {
                stack = WrappedFluidStack.fromNBT(tagCompound);
            } else {
                stack = WrappedFluidStack.fromFluidStack(FluidStack.loadFluidStackFromNBT(tagCompound),
                        data.getLong("Cnt"));
            }

            if (stack == null) {
                GTLog.logger.error("Error reading ME Output Hatch buffer tag list");
            } else {
                internalBuffer.add(stack);
            }
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            if (isOnline()) {
                Textures.ME_OUTPUT_HATCH_ACTIVE.renderSided(getFrontFacing(), renderState, translation, pipeline);
            } else {
                Textures.ME_OUTPUT_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.fluid_hatch.export.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.fluid_export.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.fluid_export.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.me.extra_connections.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    public MultiblockAbility<IFluidTank> getAbility() {
        return MultiblockAbility.EXPORT_FLUIDS;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.add(new InaccessibleInfiniteTank(this, this.internalBuffer, this.getController()));
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        if (controllerBase instanceof MultiblockWithDisplayBase multiblockWithDisplayBase) {
            multiblockWithDisplayBase.enableFluidInfSink();
        }
    }

    private static class InaccessibleInfiniteTank extends InaccessibleInfiniteHandler<IAEFluidStack, FluidStack>
                                                  implements IFluidTank {

        public InaccessibleInfiniteTank(@NotNull MetaTileEntity holder,
                                        @NotNull List<IWrappedStack<IAEFluidStack, FluidStack>> internalBuffer,
                                        @NotNull MetaTileEntity mte) {
            super(holder, internalBuffer, mte, FluidStackHashStrategy.comparingAllButAmount());
        }

        @Nullable
        @Override
        public FluidStack getFluid() {
            return null;
        }

        @Override
        public int getFluidAmount() {
            return 0;
        }

        @Override
        public int getCapacity() {
            return Integer.MAX_VALUE - 1;
        }

        @Override
        public FluidTankInfo getInfo() {
            return null;
        }

        @Override
        public int fill(@Nullable FluidStack stackToInsert, boolean doFill) {
            if (stackToInsert == null || stackToInsert.amount < 1) {
                return 0;
            }

            if (doFill) {
                add(stackToInsert, stackToInsert.amount);
                this.trigger();
            }

            return stackToInsert.amount;
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return null;
        }

        @Override
        protected @NotNull IWrappedStack<IAEFluidStack, FluidStack> wrapStack(@NotNull FluidStack stack, long amount) {
            return WrappedFluidStack.fromFluidStack(stack, amount);
        }
    }
}
