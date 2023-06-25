package gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.ILaserContainer;
import gregtech.api.capability.impl.LaserContainerHandler;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityLaserHatch extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<ILaserContainer>, IDataInfoProvider {
    private final boolean isOutput;
    private final ILaserContainer laserContainer;
    public MetaTileEntityLaserHatch(ResourceLocation metaTileEntityId, boolean isOutput) {
        super(metaTileEntityId, GTValues.ZPM);
        this.isOutput = isOutput;
        this.laserContainer = new LaserContainerHandler(this, GTValues.V[GTValues.UV] * 4, isOutput);
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
    public MultiblockAbility<ILaserContainer> getAbility() {
        return isOutput ? MultiblockAbility.OUTPUT_LASER : MultiblockAbility.INPUT_LASER;
    }

    @Override
    public void registerAbilities(List<ILaserContainer> abilityList) {
        abilityList.add(laserContainer);
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

    // TODO: add tooltips
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", laserContainer.getEnergyCapacity()));
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> info = new ArrayList<>();
        info.add(new TextComponentTranslation("behavior.tricorder.energy_container_storage", laserContainer.getEnergyStored(), laserContainer.getEnergyCapacity()));
        return info;
    }
}
