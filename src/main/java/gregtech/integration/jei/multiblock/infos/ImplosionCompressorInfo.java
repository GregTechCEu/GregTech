package gregtech.integration.jei.multiblock.infos;

import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
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

public class ImplosionCompressorInfo extends MultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return MetaTileEntities.IMPLOSION_COMPRESSOR;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo shapeInfo = MultiblockShapeInfo.builder()
            .aisle("XXX", "XBX", "XXX")
            .aisle("XXX", "C#E", "XXX")
            .aisle("XXX", "XIX", "XXX")
            .where('C', MetaTileEntities.IMPLOSION_COMPRESSOR, EnumFacing.WEST)
            .where('X', MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID))
            .where('#', Blocks.AIR.getDefaultState())
            .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.HV], EnumFacing.SOUTH)
            .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.HV], EnumFacing.EAST)
            .where('B', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.HV], EnumFacing.NORTH)
            .build();
        return Lists.newArrayList(shapeInfo);
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gregtech.multiblock.implosion_compressor.description")};
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        ITextComponent tooltip = new TextComponentTranslation("gregtech.multiblock.preview.limit", 14).setStyle(new Style().setColor(TextFormatting.AQUA));
        addBlockTooltip(MetaBlocks.METAL_CASING.getItemVariant(MetalCasingType.STEEL_SOLID), tooltip);
    }
}
