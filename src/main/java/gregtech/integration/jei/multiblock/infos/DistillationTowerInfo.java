package gregtech.integration.jei.multiblock.infos;

import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMetalCasing;
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

public class DistillationTowerInfo extends MultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return MetaTileEntities.DISTILLATION_TOWER;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        return Lists.newArrayList(MultiblockShapeInfo.builder()
                .aisle("XXX", "XXX", "XXX", "XXX", "XXX", "XXX")
                .aisle("XFX", "X#X", "X#X", "X#X", "X#X", "XXX")
                .aisle("ESI", "XMH", "XXH", "XXH", "XXH", "XXH")
                .where('#', Blocks.AIR.getDefaultState())
                .where('X', MetaBlocks.METAL_CASING.getState(MetalCasingType.STAINLESS_CLEAN))
                .where('S', MetaTileEntities.DISTILLATION_TOWER, EnumFacing.WEST)
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.EV], EnumFacing.WEST)
                .where('I', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.EV], EnumFacing.WEST)
                .where('F', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.EV], EnumFacing.DOWN)
                .where('H', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.EV], EnumFacing.WEST)
                .where('M', maintenanceIfEnabled(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN)), EnumFacing.WEST)
                .build());
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gregtech.multiblock.distillation_tower.description")};
    }

    @Override
    public float getDefaultZoom() {
        return 0.7f;
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        ITextComponent tooltip = new TextComponentTranslation("gregtech.multiblock.preview.limit_per_layer", 1).setStyle(new Style().setColor(TextFormatting.DARK_RED));
        ITextComponent inputTooltip = new TextComponentTranslation("gregtech.multiblock.preview.only_location", I18n.format("gregtech.multiblock.preview.location.b_l")).setStyle(new Style().setColor(TextFormatting.DARK_RED));
        ITextComponent outputTooltip = new TextComponentTranslation("gregtech.multiblock.preview.distillation_multi_fluid").setStyle(new Style().setColor(TextFormatting.RED));

        for (int i = 0; i < GTValues.UHV + 1; i++) {
            addBlockTooltip(MetaTileEntities.FLUID_EXPORT_HATCH[i].getStackForm(), tooltip);
            addBlockTooltip(MetaTileEntities.FLUID_EXPORT_HATCH[i].getStackForm(), outputTooltip);
            addBlockTooltip(MetaTileEntities.FLUID_IMPORT_HATCH[i].getStackForm(), inputTooltip);
        }
    }
}
