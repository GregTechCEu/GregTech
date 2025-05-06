package gregtech.common.metatileentities.multi.fission;

import gregtech.api.capability.IFissionRodPort;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityFissionFuelRod extends MetaTileEntityMultiblockPart
                                          implements IMultiblockAbilityPart<IFissionRodPort>, IFissionRodPort {

    @NotNull
    protected final IFissionRodPort.RodType type;

    public MetaTileEntityFissionFuelRod(ResourceLocation metaTileEntityId, int tier,
                                        IFissionRodPort.@NotNull RodType type) {
        super(metaTileEntityId, tier);
        this.type = type;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFissionFuelRod(metaTileEntityId, getTier(), type);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        // if (shouldRenderOverlay()) {
        //
        // }
    }

    @Override
    public MultiblockAbility<IFissionRodPort> getAbility() {
        return MultiblockAbility.FISSION_ROD_PORT;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.add(this);
    }

    @Override
    public @NotNull RodType getRodType() {
        return type;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        type.addInformation(stack, world, tooltip, advanced);
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}
