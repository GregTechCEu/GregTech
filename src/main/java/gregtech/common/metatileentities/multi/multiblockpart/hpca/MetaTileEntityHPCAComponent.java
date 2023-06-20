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
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTTagType;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class MetaTileEntityHPCAComponent extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IHPCAComponentHatch>, IHPCAComponentHatch {

    private static final String NBT_DAMAGED = "Damaged";
    private static final NBTCondition NBT_DAMAGED_CONDITION = NBTCondition.create(NBTTagType.BOOLEAN, NBT_DAMAGED, true);

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
            data.setBoolean(NBT_DAMAGED, damaged);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (canBeDamaged()) {
            this.damaged = data.getBoolean(NBT_DAMAGED);
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
    public void initFromItemStackData(NBTTagCompound compound) {
        super.initFromItemStackData(compound);
        if (canBeDamaged()) {
            if (compound.hasKey(NBT_DAMAGED)) {
                setDamaged(compound.getBoolean(NBT_DAMAGED));
            }
        }
    }

    @Override
    public void writeItemStackData(NBTTagCompound compound) {
        super.writeItemStackData(compound);
        if (canBeDamaged() && damaged) {
            compound.setBoolean(NBT_DAMAGED, true);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void setRenderContextStack(ItemStack stack) {
        super.setRenderContextStack(stack);
        if (canBeDamaged() && stack != null && stack.hasTagCompound()) {
            NBTTagCompound compound = stack.getTagCompound();
            if (compound != null && compound.hasKey(NBT_DAMAGED)) {
                this.damaged = compound.getBoolean(NBT_DAMAGED);
            }
        }
    }

    // todo this doesn't work quite right
    @Override
    public String getMetaName() {
        if (canBeDamaged() && isDamaged()) {
            return super.getMetaName() + ".damaged";
        }
        return super.getMetaName();
    }

    /**
     * Used for recipes which input a damaged HPCA component, such as Arc Furnace recycling.
     * Use with {@link gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher#EQUAL_TO} for matching a damaged component,
     * and {@link gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher#NOT_PRESENT_OR_DEFAULT} for undamaged.
     */
    public static NBTCondition getDamagedCondition() {
        return NBT_DAMAGED_CONDITION;
    }
}
