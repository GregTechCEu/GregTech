package gregtech.common.pipelike.block.optical;

import gregtech.api.graphnet.gather.GatherStructuresEvent;
import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;
import gregtech.api.graphnet.pipenet.physical.IBurnable;
import gregtech.api.graphnet.pipenet.physical.block.WorldPipeBlock;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.ToolHelper;

import gregtech.common.pipelike.handlers.OpticalNetHandler;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class OpticalPipeBlock extends WorldPipeBlock {

    public OpticalPipeBlock(OpticalStructure structure) {
        super(structure);
        setHarvestLevel(ToolClasses.WIRE_CUTTER, 1);
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
    protected @NotNull IPipeNetNodeHandler getHandler(IBlockAccess world, BlockPos pos) {
        return OpticalNetHandler.INSTANCE;
    }
}
