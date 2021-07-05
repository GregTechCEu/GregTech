package gregtech.integration.jei.multiblock.infos;

import com.google.common.collect.Lists;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockFusionCoil;
import gregtech.common.blocks.BlockMachineCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

import java.util.List;

public class FusionReactorInfo extends MultiblockInfoPage {

    private final int tier;

    public FusionReactorInfo(int tier) {
        this.tier = tier;
    }

    @Override
    public MultiblockControllerBase getController() {
        return MetaTileEntities.FUSION_REACTOR[tier];
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo shapeInfo = MultiblockShapeInfo.builder()
                .aisle("###############", "######NCN######", "###############")
                .aisle("######DCD######", "####CCcccCC####", "######UCU######")
                .aisle("####CC###CC####", "###sccNMNccs###", "####CC###CC####")
                .aisle("###C#######C###", "##wcnC###Cnce##", "###C#######C###")
                .aisle("##C#########C##", "#Cce#######wcC#", "##C#########C##")
                .aisle("##C#########C##", "#CcC#######CcC#", "##C#########C##")
                .aisle("#D###########D#", "WcE#########WcE", "#U###########U#")
                .aisle("#C###########C#", "CcC#########CcC", "#C###########C#")
                .aisle("#D###########D#", "WcE#########WcE", "#U###########U#")
                .aisle("##C#########C##", "#CcC#######CcC#", "##C#########C##")
                .aisle("##C#########C##", "#Cce#######wcC#", "##C#########C##")
                .aisle("###C#######C###", "##wcsC###Csce##", "###C#######C###")
                .aisle("####CC###CC####", "###nccSCSccn###", "####CC###CC####")
                .aisle("######DCD######", "####CCcccCC####", "######UCU######")
                .aisle("###############", "######NCN######", "###############")
                .where('M', MetaTileEntities.FUSION_REACTOR[tier], EnumFacing.SOUTH)
                .where('C', getCasing(tier))
                .where('c', getCoil(tier))
                .where('W', MetaTileEntities.FLUID_EXPORT_HATCH[6 + tier], EnumFacing.WEST)
                .where('E', MetaTileEntities.FLUID_EXPORT_HATCH[6 + tier], EnumFacing.EAST)
                .where('S', MetaTileEntities.FLUID_EXPORT_HATCH[6 + tier], EnumFacing.SOUTH)
                .where('N', MetaTileEntities.FLUID_EXPORT_HATCH[6 + tier], EnumFacing.NORTH)
                .where('w', MetaTileEntities.ENERGY_INPUT_HATCH[6 + tier], EnumFacing.WEST)
                .where('e', MetaTileEntities.ENERGY_INPUT_HATCH[6 + tier], EnumFacing.EAST)
                .where('s', MetaTileEntities.ENERGY_INPUT_HATCH[6 + tier], EnumFacing.SOUTH)
                .where('n', MetaTileEntities.ENERGY_INPUT_HATCH[6 + tier], EnumFacing.NORTH)
                .where('U', MetaTileEntities.FLUID_IMPORT_HATCH[6 + tier], EnumFacing.UP)
                .where('D', MetaTileEntities.FLUID_IMPORT_HATCH[6 + tier], EnumFacing.DOWN)
                .where('#', Blocks.AIR.getDefaultState()).build();

        return Lists.newArrayList(shapeInfo);
    }

    private static IBlockState getCasing(int tier) {
        switch(tier) {
            case 0:
                return MetaBlocks.MACHINE_CASING.getState(BlockMachineCasing.MachineCasingType.LuV);
            case 1:
                return MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.FUSION_CASING);
            default:
                return MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.FUSION_CASING_MK2);
        }
    }

    private static IBlockState getCoil(int tier) {
        if (tier == 0)
            return MetaBlocks.FUSION_COIL.getState(BlockFusionCoil.CoilType.SUPERCONDUCTOR);
        return MetaBlocks.FUSION_COIL.getState(BlockFusionCoil.CoilType.FUSION_COIL);
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format(String.format("gregtech.multiblock.fusion_reactor_mk%d.description", tier + 1))
        };
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
