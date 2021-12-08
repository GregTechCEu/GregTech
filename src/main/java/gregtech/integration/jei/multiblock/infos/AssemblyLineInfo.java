package gregtech.integration.jei.multiblock.infos;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.BlockGlassCasing;
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

import java.util.ArrayList;
import java.util.List;

// TODO Refactor this Class
public class AssemblyLineInfo extends MultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return MetaTileEntities.ASSEMBLY_LINE;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapes = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder();
            builder.aisle("COC", "RTR", "GAG", "#Y#");
            for (int num = 0; num < 3 + i; num++) {
                if (num == 4 || num == 9) builder.aisle("FIf", "RTR", "GAG", "#Y#");
                else builder.aisle("CIC", "RTR", "GAG", "#Y#");
            }
            builder.aisle("CIM", "RTR", "GSG", "#Y#")
                    .where('S', MetaTileEntities.ASSEMBLY_LINE, EnumFacing.WEST)
                    .where('C', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                    .where('F', MetaTileEntities.FLUID_IMPORT_HATCH[4], EnumFacing.SOUTH)
                    .where('f', MetaTileEntities.FLUID_IMPORT_HATCH[4], EnumFacing.NORTH)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[4], EnumFacing.DOWN)
                    .where('Y', MetaTileEntities.ENERGY_INPUT_HATCH[4], EnumFacing.UP)
                    .where('I', MetaTileEntities.ITEM_IMPORT_BUS[0], EnumFacing.DOWN)
                    .where('M', maintenanceIfEnabled(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID)), EnumFacing.NORTH)
                    .where('G', MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING))
                    .where('A', MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLY_CONTROL))
                    .where('R', MetaBlocks.TRANSPARENT_CASING.getState(
                            BlockGlassCasing.CasingType.TEMPERED_GLASS))
                    .where('T', MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLY_LINE_CASING))
                    .where('#', Blocks.AIR.getDefaultState());
            shapes.add(builder.build());
        }
        return shapes;
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gregtech.multiblock.assembly_line.description")};
    }

    @Override
    public float getDefaultZoom() {
        return 0.7f;
    }

    @Override
    protected void generateBlockTooltips() {
        ITextComponent outputTooltip = new TextComponentTranslation(
                "gregtech.multiblock.preview.only_location",
                new TextComponentTranslation("gregtech.multiblock.preview.location_end"))
                .setStyle(new Style().setColor(TextFormatting.RED));

        ITextComponent inputTooltip = new TextComponentTranslation(
                "gregtech.multiblock.preview.only",
                new TextComponentTranslation("gregtech.machine.item_bus.import.ulv.name"))
                .setStyle(new Style().setColor(TextFormatting.RED));

        for (int i = 0; i < GTValues.UHV + 1; i++) {
            addBlockTooltip(MetaTileEntities.ITEM_EXPORT_BUS[i].getStackForm(), defaultText);
            addBlockTooltip(MetaTileEntities.ITEM_EXPORT_BUS[i].getStackForm(), outputTooltip);
            addBlockTooltip(MetaTileEntities.FLUID_EXPORT_HATCH[i].getStackForm(), defaultText);
            addBlockTooltip(MetaTileEntities.FLUID_IMPORT_HATCH[i].getStackForm(), defaultText);
        }
        addBlockTooltip(MetaTileEntities.ITEM_IMPORT_BUS[0].getStackForm(), inputTooltip);
    }
}
