package gregtech.common.metatileentities.multi.multiblockpart.hpca;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IHPCAComponentHatch;
import gregtech.api.capability.IHPCAComputationProvider;
import gregtech.api.capability.IHPCACoolantProvider;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.blocks.BlockComputerCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class MetaTileEntityHPCAComponent extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IHPCAComponentHatch>, IHPCAComponentHatch {

    private boolean damaged;

    public MetaTileEntityHPCAComponent(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.ZPM);
    }

    public abstract boolean isAdvanced();

    public boolean doesAllowBridging() {
        return false;
    }

    public abstract SimpleOverlayRenderer getFrontOverlay();

    public SimpleOverlayRenderer getFrontActiveOverlay() {
        return getFrontOverlay();
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public MultiblockAbility<IHPCAComponentHatch> getAbility() {
        return MultiblockAbility.HPCA_COMPONENT;
    }

    @Override
    public void registerAbilities(List<IHPCAComponentHatch> abilityList) {
        abilityList.add(this);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer;
            var controller = getController();
            if (controller != null && controller.isActive()) {
                renderer = getFrontActiveOverlay();
            } else {
                renderer = getFrontOverlay();
            }
            if (renderer != null) {
                EnumFacing facing = getFrontFacing();
                // always render this outwards, in case it is not placed outwards in structure
                if (controller != null) {
                    facing = controller.getFrontFacing().rotateY();
                }
                renderer.renderSided(facing, renderState, translation, pipeline);
            }
        }
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        return isAdvanced() ? Textures.ADVANCED_COMPUTER_CASING : Textures.COMPUTER_CASING;
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        final int upkeepEUt = getUpkeepEUt();
        final int maxEUt = getMaxEUt();
        if (upkeepEUt != 0 && upkeepEUt != maxEUt) {
            tooltip.add(I18n.format("gregtech.machine.hpca.component_general.upkeep_eut", upkeepEUt));
        }
        if (maxEUt != 0) {
            tooltip.add(I18n.format("gregtech.machine.hpca.component_general.max_eut", maxEUt));
        }

        if (canBeDamaged() && isDamaged()) {
            tooltip.add(I18n.format("gregtech.machine.hpca.component_type.damaged"));
        } else {
            if (this instanceof IHPCACoolantProvider provider) {
                if (provider.isActiveCooler()) {
                    tooltip.add(I18n.format("gregtech.machine.hpca.component_type.cooler_active"));
                    tooltip.add(I18n.format("gregtech.machine.hpca.component_type.cooler_active_coolant",
                            provider.getMaxCoolantPerTick(), "Water")); // todo coolant
                } else {
                    tooltip.add(I18n.format("gregtech.machine.hpca.component_type.cooler_passive"));
                }
            }

            if (this instanceof IHPCAComputationProvider provider) {
                tooltip.add(I18n.format("gregtech.machine.hpca.component_type.computation_cwut", provider.getCWUPerTick()));
                tooltip.add(I18n.format("gregtech.machine.hpca.component_type.computation_cooling", provider.getCoolingPerTick()));
            }

            if (isBridge()) {
                tooltip.add(I18n.format("gregtech.machine.hpca.component_type.bridge"));
            }
        }

        // todo hide under shift?
        //if (TooltipHelper.isShiftDown()) {
        //
        //} else {
        //    tooltip.add(I18n.format(""));
        //}
    }

    // Since it is only wrench, we disable this to use the SHIFT tooltip for something else
    // todo keep this?
    @Override
    public boolean showToolUsages() {
        return false;
    }

    // Handle damaged state

    @Override
    public final boolean isBridge() {
        return doesAllowBridging() && !(canBeDamaged() && isDamaged());
    }

    @Override
    public boolean isDamaged() {
        return canBeDamaged() && damaged;
    }

    @Override
    public void setDamaged(boolean damaged) {
        if (!canBeDamaged()) return;
        if (this.damaged != damaged) {
            this.damaged = damaged;
            markDirty();
            if (getWorld() != null && !getWorld().isRemote) {
                writeCustomData(GregtechDataCodes.DAMAGE_STATE, buf -> buf.writeBoolean(damaged));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        if (canBeDamaged()) {
            data.setBoolean("Damaged", damaged);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (canBeDamaged()) {
            this.damaged = data.getBoolean("Damaged");
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        if (canBeDamaged()) {
            buf.writeBoolean(damaged);
        }
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        if (canBeDamaged()) {
            this.damaged = buf.readBoolean();
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (canBeDamaged() && dataId == GregtechDataCodes.DAMAGE_STATE) {
            this.damaged = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public boolean shouldDropWhenDestroyed() {
        return !(canBeDamaged() && isDamaged());
    }

    @Override
    public void getDrops(NonNullList<ItemStack> dropsList, @Nullable EntityPlayer harvester) {
        if (canBeDamaged() && isDamaged()) {
            if (isAdvanced()) {
                dropsList.add(MetaBlocks.COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.ADVANCED_COMPUTER_CASING));
            } else {
                dropsList.add(MetaBlocks.COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.COMPUTER_CASING));
            }
        }
    }

    @Override
    public String getMetaName() {
        if (canBeDamaged() && isDamaged()) {
            return super.getMetaName() + ".damaged";
        }
        return super.getMetaName();
    }
}
