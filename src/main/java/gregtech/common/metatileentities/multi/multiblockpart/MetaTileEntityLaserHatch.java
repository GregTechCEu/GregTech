package gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MetaTileEntityLaserHatch extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<ILaserContainer>, IDataInfoProvider {

    private final boolean isOutput;
    private final int tier;
    private final int amperage;
    private final ILaserContainer buffer;

    public MetaTileEntityLaserHatch(ResourceLocation metaTileEntityId, boolean isOutput, int tier, int amperage) {
        super(metaTileEntityId, tier);
        this.isOutput = isOutput;
        this.tier = tier;
        this.amperage = amperage;
        this.buffer = new LaserContainerHandler(this, tier, amperage);
    }


    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLaserHatch(metaTileEntityId, isOutput, tier, amperage);
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
        abilityList.add(this.buffer);
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

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        return Collections.singletonList(new TextComponentString(String.format("%d/%d EU", this.buffer.getEnergyStored(), this.buffer.getEnergyCapacity())));
    }
}
