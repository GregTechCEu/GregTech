package gregtech.integration.jei.multiblock.infos;

import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockFusionCasing;
import gregtech.common.blocks.BlockGlassCasing;
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

public class FusionReactorInfo extends MultiblockInfoPage {

    private final int tier;

    public FusionReactorInfo(int tier) {
        this.tier = tier;
    }

    @Override
    public MultiblockControllerBase getController() {
        return MetaTileEntities.FUSION_REACTOR[tier - GTValues.LuV];
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo shapeInfo = MultiblockShapeInfo.builder()
                .aisle("###############", "######SGS######", "###############")
                .aisle("######DCD######", "####GG###GG####", "######UCU######")
                .aisle("####CC###CC####", "###n##NGN##e###", "####CC###CC####")
                .aisle("###C#######C###", "##sKwG###GwKs##", "###C#######C###")
                .aisle("##C#########C##", "#G#e#######n#G#", "##C#########C##")
                .aisle("##C#########C##", "#G#G#######G#G#", "##C#########C##")
                .aisle("#D###########D#", "W#E#########W#E", "#U###########U#")
                .aisle("#C###########C#", "G#G#########G#G", "#C###########C#")
                .aisle("#D###########D#", "W#E#########W#E", "#U###########U#")
                .aisle("##C#########C##", "#G#G#######G#G#", "##C#########C##")
                .aisle("##C#########C##", "#G#e#######n#G#", "##C#########C##")
                .aisle("###C#######C###", "##wKsG###GsKw##", "###C#######C###")
                .aisle("####CC###CC####", "###n##SGS##e###", "####CC###CC####")
                .aisle("######DCD######", "####GG###GG####", "######UCU######")
                .aisle("###############", "######NMN######", "###############")
                .where('M', MetaTileEntities.FUSION_REACTOR[tier - GTValues.LuV], EnumFacing.WEST)
                .where('C', MetaBlocks.FUSION_CASING.getState(getCasing(tier)))
                .where('G', MetaBlocks.TRANSPARENT_CASING.getState(
                        BlockGlassCasing.CasingType.FUSION_GLASS))
                .where('K', getCoil(tier))
                .where('W', MetaTileEntities.FLUID_EXPORT_HATCH[tier], EnumFacing.NORTH)
                .where('E', MetaTileEntities.FLUID_EXPORT_HATCH[tier], EnumFacing.SOUTH)
                .where('S', MetaTileEntities.FLUID_EXPORT_HATCH[tier], EnumFacing.EAST)
                .where('N', MetaTileEntities.FLUID_EXPORT_HATCH[tier], EnumFacing.WEST)
                .where('w', MetaTileEntities.ENERGY_INPUT_HATCH[tier], EnumFacing.WEST)
                .where('e', MetaTileEntities.ENERGY_INPUT_HATCH[tier], EnumFacing.SOUTH)
                .where('s', MetaTileEntities.ENERGY_INPUT_HATCH[tier], EnumFacing.EAST)
                .where('n', MetaTileEntities.ENERGY_INPUT_HATCH[tier], EnumFacing.NORTH)
                .where('U', MetaTileEntities.FLUID_IMPORT_HATCH[tier], EnumFacing.UP)
                .where('D', MetaTileEntities.FLUID_IMPORT_HATCH[tier], EnumFacing.DOWN)
                .where('#', Blocks.AIR.getDefaultState()).build();

        return Lists.newArrayList(shapeInfo);
    }

    private static BlockFusionCasing.CasingType getCasing(int tier) {
        if (tier == GTValues.LuV)
            return BlockFusionCasing.CasingType.FUSION_CASING;
        if (tier == GTValues.ZPM)
            return BlockFusionCasing.CasingType.FUSION_CASING_MK2;

        return BlockFusionCasing.CasingType.FUSION_CASING_MK3;
    }

    private static IBlockState getCoil(int tier) {
        if (tier == GTValues.LuV)
            return MetaBlocks.FUSION_CASING.getState(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL);
        return MetaBlocks.FUSION_CASING.getState(BlockFusionCasing.CasingType.FUSION_COIL);
    }

    @Override
    public String[] getDescription() {
        return new String[]{
                I18n.format(String.format("gregtech.multiblock.fusion_reactor_mk%d.description", tier + 1 - 6))
        };
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();

        ITextComponent tooltip = new TextComponentTranslation("gregtech.multiblock.preview.limit", 48).setStyle(new Style().setColor(TextFormatting.AQUA));
        addBlockTooltip(MetaBlocks.FUSION_CASING.getItemVariant(getCasing(tier)), tooltip);

        ITextComponent tooltip2;
        if (tier < GTValues.UV)
            tooltip2 = new TextComponentTranslation("gregtech.multiblock.preview.only", String.format("%s-%s", GTValues.VN[tier], GTValues.VN[GTValues.UV])).setStyle(new Style().setColor(TextFormatting.RED));
        else
            tooltip2 = new TextComponentTranslation("gregtech.multiblock.preview.only", GTValues.VN[tier]).setStyle(new Style().setColor(TextFormatting.RED));
        addBlockTooltip(MetaTileEntities.ENERGY_INPUT_HATCH[tier].getStackForm(), tooltip2);
    }
}
