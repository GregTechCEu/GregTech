package gregtech.integration.jei.multiblock.infos;

import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.BlockInfo;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.items.behaviors.TurbineRotorBehavior;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityRotorHolder;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeTurbine;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class LargeTurbineInfo extends MultiblockInfoPage {

    public final MetaTileEntityLargeTurbine turbine;

    public LargeTurbineInfo(MetaTileEntityLargeTurbine turbine) {
        this.turbine = turbine;
    }

    @Override
    public MultiblockControllerBase getController() {
        return turbine;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MetaTileEntityHolder holder = new MetaTileEntityHolder();
        holder.setMetaTileEntity(MetaTileEntities.ROTOR_HOLDER[2]);
        holder.getMetaTileEntity().setFrontFacing(EnumFacing.WEST);
        ItemStack rotorStack = MetaItems.TURBINE_ROTOR.getStackForm();
        //noinspection ConstantConditions
        TurbineRotorBehavior.getInstanceFor(rotorStack).setPartMaterial(rotorStack, Materials.Neutronium);
        ((MetaTileEntityRotorHolder) holder.getMetaTileEntity()).getRotorInventory().setStackInSlot(0, rotorStack);
        MultiblockShapeInfo.Builder shapeInfo = MultiblockShapeInfo.builder()
                .aisle("CCCC", "CIOC", "CCCC")
                .aisle("CCCC", "RGGE", "CCHC")
                .aisle("CCCC", "CSMC", "CCCC")
                .where('S', turbine, EnumFacing.WEST)
                .where('C', turbine.turbineType.casingState)
                .where('R', new BlockInfo(MetaBlocks.MACHINE.getDefaultState(), holder))
                .where('E', MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.EV], EnumFacing.NORTH)
                .where('G', turbine.turbineType.gearboxState)
                .where('I', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.HV], EnumFacing.EAST)
                .where('M', maintenanceIfEnabled(turbine.turbineType.casingState), EnumFacing.WEST);

                if (turbine.turbineType.hasOutputHatch)
                    shapeInfo.where('O', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.EV], EnumFacing.EAST);
                else
                    shapeInfo.where('O', turbine.turbineType.casingState);

                if (turbine.hasMufflerMechanics())
                    shapeInfo.where('H', MetaTileEntities.MUFFLER_HATCH[GTValues.LV], EnumFacing.UP);
                else
                    shapeInfo.where('H', turbine.turbineType.casingState);

        return Lists.newArrayList(shapeInfo.build());
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gregtech.multiblock.large_turbine.description")};
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();

        ITextComponent tooltip = new TextComponentTranslation("gregtech.multiblock.preview.clear_amount", 3, 3, 1).setStyle(new Style().setColor(TextFormatting.DARK_RED));
        for(MetaTileEntityRotorHolder rotor : MetaTileEntities.ROTOR_HOLDER) {
            addBlockTooltip(rotor.getStackForm(), tooltip);
        }

    }
}
