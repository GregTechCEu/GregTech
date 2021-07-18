package gregtech.integration.jei.multiblock.infos;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockWireCoil.CoilType;
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

public class MultiSmelterInfo extends MultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return MetaTileEntities.MULTI_FURNACE;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        for (CoilType coilType : CoilType.values()) {
            shapeInfo.add(MultiblockShapeInfo.builder()
                .aisle("IXX", "CCC", "XXX")
                .aisle("SXE", "C#C", "XXX")
                .aisle("OXX", "CCC", "XXX")
                .where('X', MetaBlocks.METAL_CASING.getState(MetalCasingType.INVAR_HEATPROOF))
                .where('C', MetaBlocks.WIRE_COIL.getState(coilType))
                .where('S', MetaTileEntities.MULTI_FURNACE, EnumFacing.WEST)
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.MV], EnumFacing.EAST)
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.LV], EnumFacing.WEST)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.LV], EnumFacing.WEST)
                .where('#', Blocks.AIR.getDefaultState())
                .build());
        }
        return shapeInfo;
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gregtech.multiblock.multi_smelter.description")};
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        ITextComponent tooltip = new TextComponentTranslation("gregtech.multiblock.preview.limit", 9).setStyle(new Style().setColor(TextFormatting.AQUA));
        addBlockTooltip(MetaBlocks.METAL_CASING.getItemVariant(MetalCasingType.INVAR_HEATPROOF), tooltip);
    }
}
