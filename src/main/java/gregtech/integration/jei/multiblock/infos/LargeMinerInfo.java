
package gregtech.integration.jei.multiblock.infos;

import com.google.common.collect.Lists;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityLargeMiner;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

import java.util.List;


public class LargeMinerInfo extends MultiblockInfoPage {

    private final MetaTileEntityLargeMiner largeMiner;

    public LargeMinerInfo(MetaTileEntityLargeMiner largeMiner) {
        this.largeMiner = largeMiner;
    }

    @Override
    public MultiblockControllerBase getController() {
        return largeMiner;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo shapeInfo = MultiblockShapeInfo.builder()
                .aisle("OPP", "#F#", "#F#", "#F#", "###", "###", "###")
                .aisle("PPP", "FPF", "FPF", "FPF", "#F#", "#F#", "#F#")
                .aisle("ESI", "#F#", "#F#", "#F#", "###", "###", "###")
                .where('S', largeMiner, EnumFacing.WEST)
                .where('P', largeMiner.getCasingState())
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[4], EnumFacing.WEST)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[0], EnumFacing.EAST)
                .where('I', MetaTileEntities.FLUID_IMPORT_HATCH[0], EnumFacing.WEST)
                .where('F', MetaBlocks.FRAMES.get(largeMiner.getMaterial()).getDefaultState())
                .where('#', Blocks.AIR.getDefaultState())
                .build();
        return Lists.newArrayList(shapeInfo);
    }

    @Override
    public String[] getDescription() {
        //small ore: return new String[]{I18n.format("gregtech.machine.miner.multi.description", largeMiner.getChunkRadius(), largeMiner.getChunkRadius(), largeMiner.getRomanNumeralString())};
        return new String[]{I18n.format("gregtech.machine.miner.multi.description", largeMiner.getMaxChunkRadius(), largeMiner.getMaxChunkRadius())};

    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
