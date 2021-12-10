package gregtech.integration.jei.multiblock.infos;

import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class ProcessingArrayInfo extends MultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return MetaTileEntities.PROCESSING_ARRAY;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo shapeInfo = MultiblockShapeInfo.builder()
                .aisle("XXX", "XEX", "XXX")
                .aisle("OXI", "X#X", "XXX")
                .aisle("XMX", "XSX", "XQX")
                .where('S', MetaTileEntities.PROCESSING_ARRAY, EnumFacing.WEST)
                .where('M', MetaTileEntities.MACHINE_HATCH, EnumFacing.WEST)
                .where('X', getCasingState())
                .where('#', Blocks.AIR.getDefaultState())
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.LV], EnumFacing.SOUTH)
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.LV],EnumFacing.EAST)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.LV], EnumFacing.NORTH)
                .where('Q', maintenanceIfEnabled(getCasingState()), EnumFacing.WEST)
                .build();

        return Lists.newArrayList(shapeInfo);
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST);
    }


    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gregtech.multiblock.processing_array.description")};
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();

        //ITextComponent maxLimitTooltip = new TextComponentTranslation("Max Limit", 1).setStyle(new Style().setColor(TextFormatting.RED));
        //addBlockTooltip(MetaTileEntities.MACHINE_HATCH.getStackForm(), maxLimitTooltip);

        ITextComponent minLimitTooltip = new TextComponentTranslation("gregtech.multiblock.preview.limit", 11).setStyle(new Style().setColor(TextFormatting.AQUA));
        addBlockTooltip(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST), minLimitTooltip);

    }
}
