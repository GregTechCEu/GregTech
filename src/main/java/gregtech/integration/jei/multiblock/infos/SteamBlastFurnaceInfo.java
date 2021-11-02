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

public class SteamBlastFurnaceInfo extends MultiblockInfoPage {
    @Override
    public MultiblockControllerBase getController() {
        return MetaTileEntities.STEAM_BLAST_FURNACE;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();

        if(ConfigHolder.U.steelSteamMultiblocks) {
            shapeInfo.add(MultiblockShapeInfo.builder()
                    .aisle("XXX", "XSX", "XXX", "XXX")
                    .aisle("XXX", "I#O", "X#X", "X#X")
                    .aisle("XXX", "XYX", "XXX", "XXX")
                    .where('Y', MetaTileEntities.STEAM_BLAST_FURNACE, EnumFacing.WEST)
                    .where('X', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                    .where('#', Blocks.AIR.getDefaultState())
                    .where('I', MetaTileEntities.STEAM_IMPORT_BUS, EnumFacing.NORTH)
                    .where('O', MetaTileEntities.STEAM_EXPORT_BUS, EnumFacing.SOUTH)
                    .where('S', MetaTileEntities.STEAM_HATCH, EnumFacing.EAST)
                    .build());
        } else {
            shapeInfo.add(MultiblockShapeInfo.builder()
                    .aisle("XXX", "XSX", "XXX", "XXX")
                    .aisle("XXX", "I#O", "X#X", "X#X")
                    .aisle("XXX", "XYX", "XXX", "XXX")
                    .where('Y', MetaTileEntities.STEAM_BLAST_FURNACE, EnumFacing.WEST)
                    .where('X', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.BRONZE_BRICKS))
                    .where('#', Blocks.AIR.getDefaultState())
                    .where('I', MetaTileEntities.STEAM_IMPORT_BUS, EnumFacing.NORTH)
                    .where('O', MetaTileEntities.STEAM_EXPORT_BUS, EnumFacing.SOUTH)
                    .where('S', MetaTileEntities.STEAM_HATCH, EnumFacing.EAST)
                    .build());
        }
        return Lists.newArrayList(shapeInfo);
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gregtech.multiblock.steam_blast_furnace.description")};
    }
}
