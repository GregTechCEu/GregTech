package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IMufflerHatch;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.util.GTUtility;
import gregtech.client.particle.VanillaParticleEffects;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityMufflerHatch extends MetaTileEntityMultiblockPart implements
                                        IMultiblockAbilityPart<IMufflerHatch>, ITieredMetaTileEntity, IMufflerHatch {

    private static final int MUFFLER_OBSTRUCTED = GregtechDataCodes.assignId();

    private boolean frontFaceFree;

    public MetaTileEntityMufflerHatch(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMufflerHatch(metaTileEntityId, getTier());
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            if (getController() instanceof MultiblockWithDisplayBase controller && controller.isActive()) {
                VanillaParticleEffects.mufflerEffect(this, controller.getMufflerParticle());
            }
        } else if (getOffsetTimer() % 10 == 0) {
            this.frontFaceFree = checkFrontFaceFree();
            writeCustomData(MUFFLER_OBSTRUCTED, buffer -> buffer.writeBoolean(this.frontFaceFree));
        }
    }

    /**
     * @return true if front face is free and contains only air blocks in 1x1 area
     */
    @Override
    public boolean isFrontFaceFree() {
        return frontFaceFree;
    }

    private boolean checkFrontFaceFree() {
        BlockPos frontPos = getPos().offset(getFrontFacing());
        IBlockState blockState = getWorld().getBlockState(frontPos);

        // break a snow layer if it exists, and if this machine is running
        if (getController() instanceof MultiblockWithDisplayBase controller && controller.isActive()) {
            if (GTUtility.tryBreakSnow(getWorld(), frontPos, blockState, true)) {
                return true;
            }
            return blockState.getBlock().isAir(blockState, getWorld(), frontPos);
        }
        return blockState.getBlock().isAir(blockState, getWorld(), frontPos) || GTUtility.isBlockSnow(blockState);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.frontFaceFree);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.frontFaceFree = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == MUFFLER_OBSTRUCTED) {
            this.frontFaceFree = buf.readBoolean();
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            Textures.MUFFLER_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.muffler_hatch.tooltip1"));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
        tooltip.add(TooltipHelper.BLINKING_RED + I18n.format("gregtech.machine.muffler_hatch.tooltip2"));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public MultiblockAbility<IMufflerHatch> getAbility() {
        return MultiblockAbility.MUFFLER_HATCH;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.add(this);
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }
}
