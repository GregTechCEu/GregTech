package gregtech.integration.jei.multiblock.infos;

import com.google.common.collect.Lists;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

import java.util.List;

public class CokeOvenInfo extends MultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return MetaTileEntities.COKE_OVEN;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo shapeInfo = MultiblockShapeInfo.builder()
            .aisle("XXX", "XXX", "XXX")
            .aisle("XXX", "C#X", "XXX")
            .aisle("XXX", "XSX", "XXX")
            .where('X', MetaBlocks.METAL_CASING.getState(MetalCasingType.COKE_BRICKS))
            .where('C', MetaTileEntities.COKE_OVEN, EnumFacing.WEST)
            .where('S', MetaTileEntities.COKE_OVEN_HATCH, EnumFacing.SOUTH)
            .where('#', Blocks.AIR.getDefaultState())
            .build();
        return Lists.newArrayList(shapeInfo);
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gregtech.multiblock.coke_oven.description")};
    }
}
