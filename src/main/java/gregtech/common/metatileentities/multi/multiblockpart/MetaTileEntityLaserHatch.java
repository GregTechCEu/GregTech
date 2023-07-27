package gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserContainer;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityActiveTransformer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class MetaTileEntityLaserHatch extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<ILaserContainer> {

    private final boolean isOutput;
    private LaserHatchWrapper wrapper;

    public MetaTileEntityLaserHatch(ResourceLocation metaTileEntityId, boolean isOutput) {
        super(metaTileEntityId, GTValues.LuV);
        this.isOutput = isOutput;
        this.wrapper = new LaserHatchWrapper(this, null);
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        calculateLaserContainer(controllerBase);
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        this.wrapper = new LaserHatchWrapper(this, null);
    }

    private void calculateLaserContainer(MultiblockControllerBase controllerBase) {
        if (isOutput) {
            if (controllerBase instanceof MetaTileEntityActiveTransformer activeTransformer) {
                wrapper.setBufferSupplier(activeTransformer::getWrapper);
            }
        } else {
            wrapper.setBufferSupplier(this::inputContainerSupplier);
        }
    }

    private ILaserContainer inputContainerSupplier() {
        EnumFacing side = getFrontFacing();
        TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(side));
        EnumFacing oppositeSide = side.getOpposite();
        if (tileEntity != null && tileEntity.hasCapability(GregtechTileCapabilities.CAPABILITY_LASER, oppositeSide)) {
            return tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_LASER, oppositeSide);
        }
        return null;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLaserHatch(metaTileEntityId, isOutput);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    @Override
    public MultiblockAbility<ILaserContainer> getAbility() {
        return isOutput ? MultiblockAbility.OUTPUT_LASER : MultiblockAbility.INPUT_LASER;
    }

    @Override
    public void registerAbilities(List<ILaserContainer> abilityList) {
        calculateLaserContainer(null);
        abilityList.add(this.wrapper);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            if (isOutput) {
                Textures.LASER_SOURCE.renderSided(getFrontFacing(), renderState, translation, pipeline);
            } else {
                Textures.LASER_TARGET.renderSided(getFrontFacing(), renderState, translation, pipeline);
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        if (isOutput) {
            tooltip.add(I18n.format("gregtech.machine.laser_hatch.source.tooltip1"));
            tooltip.add(I18n.format("gregtech.machine.laser_hatch.source.tooltip2"));
        } else {
            tooltip.add(I18n.format("gregtech.machine.laser_hatch.target.tooltip1"));
            tooltip.add(I18n.format("gregtech.machine.laser_hatch.target.tooltip2"));
        }
        tooltip.add(I18n.format("gregtech.universal.disabled"));
    }

    private static class LaserHatchWrapper extends MTETrait implements ILaserContainer {

        @Nullable
        private Supplier<ILaserContainer> bufferSupplier;

        /**
         * Create a new MetaTileEntity trait.
         *
         * @param metaTileEntity the MetaTileEntity to reference, and add the trait to
         */
        public LaserHatchWrapper(@NotNull MetaTileEntity metaTileEntity, @Nullable Supplier<ILaserContainer> bufferSupplier) {
            super(metaTileEntity);
            this.bufferSupplier = bufferSupplier;
        }

        @Override
        public long changeEnergy(long amount, @Nonnull Collection<ILaserContainer> seen) {
            seen.add(this);
            ILaserContainer buffer = getBuffer();
            if (buffer == null || seen.contains(buffer)) {
                return 0;
            } else {
                return buffer.changeEnergy(amount, seen);
            }
        }

        @Override
        public long getEnergyStored(@Nonnull Collection<ILaserContainer> seen) {
            seen.add(this);
            ILaserContainer buffer = getBuffer();
            if (buffer == null || seen.contains(buffer)) {
                return 0;
            } else {
                return buffer.getEnergyStored(seen);
            }
        }

        @Override
        public long getEnergyCapacity(@Nonnull Collection<ILaserContainer> seen) {
            seen.add(this);
            ILaserContainer buffer = getBuffer();
            if (buffer == null || seen.contains(buffer)) {
                return 0;
            } else {
                return buffer.getEnergyCapacity(seen);
            }
        }

        @Nullable
        private ILaserContainer getBuffer() {
            if (bufferSupplier == null) {
                return null;
            } else {
                return bufferSupplier.get();
            }
        }

        @NotNull
        @Override
        public String getName() {
            return "LaserContainer";
        }

        @Override
        public <T> T getCapability(Capability<T> capability) {
            if (capability == GregtechTileCapabilities.CAPABILITY_LASER) {
                return GregtechTileCapabilities.CAPABILITY_LASER.cast(this);
            }
            return null;
        }

        public void setBufferSupplier(@Nullable Supplier<ILaserContainer> bufferSupplier) {
            this.bufferSupplier = bufferSupplier;
        }
    }
}
