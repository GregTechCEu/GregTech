package gregtech.common.pipelike.block.cable;

import gregtech.api.graphnet.pipenet.physical.IBurnable;
import gregtech.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.unification.material.registry.MaterialRegistry;

import gregtech.common.creativetab.GTCreativeTabs;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

public class CableBlock extends PipeMaterialBlock implements IBurnable {

    private static final Map<MaterialRegistry, Map<CableStructure, CableBlock>> CACHE = new Object2ObjectOpenHashMap<>();

    public CableBlock(CableStructure structure, MaterialRegistry registry) {
        super(structure, registry);
        CACHE.compute(registry, (k, v) -> {
            if (v == null) v = new Object2ObjectOpenHashMap<>();
            v.put(structure, this);
            return v;
        });
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH_CABLES);
    }

    @Override
    public String getToolClass() {
        return ToolClasses.WIRE_CUTTER;
    }

    @Override
    protected String getConnectLangKey() {
        return "gregtech.tool_action.wire_cutter.connect";
    }

    @Override
    public void partialBurn(IBlockState state, World world, BlockPos pos) {
        CableStructure structure = (CableStructure) getStructure();
        if (structure.partialBurnStructure() != null) {
            CableBlock newBlock = CACHE.get(registry).get(structure.partialBurnStructure());
            // noinspection deprecation
            world.setBlockState(pos, newBlock.getStateFromMeta(this.getMetaFromState(state)));
        }
    }
}
