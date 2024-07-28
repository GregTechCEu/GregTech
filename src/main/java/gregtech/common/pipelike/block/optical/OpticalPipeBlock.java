package gregtech.common.pipelike.block.optical;

import gregtech.api.graphnet.gather.GatherStructuresEvent;
import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;
import gregtech.api.graphnet.pipenet.physical.block.PipeActivableBlock;
import gregtech.api.graphnet.pipenet.physical.block.WorldPipeBlock;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.common.creativetab.GTCreativeTabs;
import gregtech.common.pipelike.handlers.OpticalNetHandler;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class OpticalPipeBlock extends PipeActivableBlock {

    public OpticalPipeBlock(OpticalStructure structure) {
        super(structure);
        setHarvestLevel(ToolClasses.WIRE_CUTTER, 1);
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH_PIPES);
    }

    public static Set<OpticalStructure> gatherStructures() {
        GatherStructuresEvent<OpticalStructure> event = new GatherStructuresEvent<>(OpticalStructure.class);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getGathered();
    }

    @Override
    public boolean isPipeTool(@NotNull ItemStack stack) {
        return ToolHelper.isTool(stack, ToolClasses.WIRE_CUTTER);
    }

    @Override
    protected String getConnectLangKey() {
        return "gregtech.tool_action.wire_cutter.connect";
    }

    @Override
    protected boolean allowsBlocking() {
        return false;
    }

    @Override
    protected @NotNull IPipeNetNodeHandler getHandler(IBlockAccess world, BlockPos pos) {
        return OpticalNetHandler.INSTANCE;
    }

    @Override
    protected @NotNull IPipeNetNodeHandler getHandler(@NotNull ItemStack stack) {
        return OpticalNetHandler.INSTANCE;
    }
}
