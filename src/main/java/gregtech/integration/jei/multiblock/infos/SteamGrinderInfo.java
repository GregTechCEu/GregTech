package gregtech.integration.jei.multiblock.infos;

import com.google.common.collect.Lists;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.List;

public class SteamGrinderInfo extends MultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return MetaTileEntities.STEAM_GRINDER;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();

        if (ConfigHolder.U.steelSteamMultiblocks) {
            shapeInfo.add(MultiblockShapeInfo.builder()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "X#X", "XXX")
                    .aisle("XHX", "ISO", "XXX")
                    .where('S', MetaTileEntities.STEAM_GRINDER, EnumFacing.WEST)
                    .where('X', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                    .where('I', MetaTileEntities.STEAM_IMPORT_BUS, EnumFacing.WEST)
                    .where('O', MetaTileEntities.STEAM_EXPORT_BUS, EnumFacing.WEST)
                    .where('H', MetaTileEntities.STEAM_HATCH, EnumFacing.WEST)
                    .where('#', Blocks.AIR.getDefaultState())
                    .build());
        } else {
            shapeInfo.add(MultiblockShapeInfo.builder()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "X#X", "XXX")
                    .aisle("XHX", "ISO", "XXX")
                    .where('S', MetaTileEntities.STEAM_GRINDER, EnumFacing.WEST)
                    .where('X', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.BRONZE_BRICKS))
                    .where('I', MetaTileEntities.STEAM_IMPORT_BUS, EnumFacing.WEST)
                    .where('O', MetaTileEntities.STEAM_EXPORT_BUS, EnumFacing.WEST)
                    .where('H', MetaTileEntities.STEAM_HATCH, EnumFacing.WEST)
                    .where('#', Blocks.AIR.getDefaultState())
                    .build());
        }

        return Lists.newArrayList(shapeInfo);
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gregtech.multiblock.steam_grinder.description")};
    }
}
