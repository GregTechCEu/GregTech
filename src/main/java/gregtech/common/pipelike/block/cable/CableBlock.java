package gregtech.common.pipelike.block.cable;

import gregtech.api.graphnet.gather.GatherStructuresEvent;
import gregtech.api.graphnet.pipenet.physical.IBurnable;
import gregtech.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.unification.material.registry.MaterialRegistry;

import gregtech.common.creativetab.GTCreativeTabs;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

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
    public boolean isPipeTool(@NotNull ItemStack stack) {
        return ToolHelper.isTool(stack, ToolClasses.WIRE_CUTTER);
    }

    @Override
    protected String getConnectLangKey() {
        return "gregtech.tool_action.wire_cutter.connect";
    }

    public static Set<CableStructure> gatherStructures() {
        GatherStructuresEvent<CableStructure> event = new GatherStructuresEvent<>(CableStructure.class);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getGathered();
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
