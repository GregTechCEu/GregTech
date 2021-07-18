package gregtech.integration.jei.multiblock.infos;

import com.google.common.collect.Lists;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.BlockSteamCasing.SteamCasingType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

import java.util.List;
import java.util.stream.Stream;

public class PrimitivePumpInfo extends MultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return MetaTileEntities.PRIMITIVE_WATER_PUMP;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        return Lists.newArrayList(MultiblockShapeInfo.builder()
                .aisle("SXX", "#F#", "#F#")
                .aisle("XXX", "###", "#F#")
                .aisle("XHX", "F#F", "FFF")
                .aisle("XXX", "#F#", "#F#")
                .where('S', MetaTileEntities.PRIMITIVE_WATER_PUMP, EnumFacing.WEST)
                .where('X', MetaBlocks.STEAM_CASING.getState(SteamCasingType.PUMP_DECK))
                .where('H', MetaTileEntities.PUMP_OUTPUT_HATCH, EnumFacing.UP)
                .where('F', MetaBlocks.FRAMES.get(Materials.Wood).getBlockState().getBaseState())
                .where('#', Blocks.AIR.getDefaultState())
                .build());
    }

    @Override
    public String[] getDescription() {
        return Stream.of(
                new String[]{I18n.format("gregtech.multiblock.primitive_water_pump.description")},
                I18n.format("gregtech.multiblock.primitive_water_pump.extra1").split("/n"),
                I18n.format("gregtech.multiblock.primitive_water_pump.extra2").split("/n")
        ).flatMap(Stream::of).toArray(String[]::new);
    }
}
