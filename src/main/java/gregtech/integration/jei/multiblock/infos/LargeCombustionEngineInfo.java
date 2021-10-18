package gregtech.integration.jei.multiblock.infos;

import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class LargeCombustionEngineInfo extends MultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return MetaTileEntities.LARGE_COMBUSTION_ENGINE;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        return Lists.newArrayList(MultiblockShapeInfo.builder()
                .aisle("AAA", "ASA", "AAA")
                .aisle("CCC", "MGC", "CCC")
                .aisle("CCC", "FGC", "CHC")
                .aisle("CCC", "CEC", "CCC")
                .where('C', MetaBlocks.METAL_CASING.getState(MetalCasingType.TITANIUM_STABLE))
                .where('G', MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.TITANIUM_GEARBOX))
                .where('A', MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.ENGINE_INTAKE_CASING))
                .where('S', MetaTileEntities.LARGE_COMBUSTION_ENGINE, EnumFacing.NORTH)
                .where('F', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.EV], EnumFacing.WEST)
                .where('E', MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.EV], EnumFacing.SOUTH)
                .where('H', MetaTileEntities.MUFFLER_HATCH[GTValues.LV], EnumFacing.UP)
                .where('#', Blocks.AIR.getDefaultState())
                .where('M', maintenanceIfEnabled(MetaBlocks.METAL_CASING.getState(MetalCasingType.TITANIUM_STABLE)), EnumFacing.WEST)
                .build());
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gregtech.multiblock.large_combustion_engine.description")};
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();

        ITextComponent tooltip = new TextComponentTranslation("gregtech.multiblock.preview.clear_amount", 1, 1, 1).setStyle(new Style().setColor(TextFormatting.DARK_RED));
        addBlockTooltip(MetaBlocks.MULTIBLOCK_CASING.getItemVariant(BlockMultiblockCasing.MultiblockCasingType.ENGINE_INTAKE_CASING), tooltip);
    }

}
