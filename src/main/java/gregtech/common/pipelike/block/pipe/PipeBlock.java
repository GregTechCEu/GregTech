package gregtech.common.pipelike.block.pipe;

import gregtech.api.graphnet.gather.GatherStructuresEvent;
import gregtech.api.graphnet.pipenet.physical.IBurnable;
import gregtech.api.graphnet.pipenet.physical.IFreezable;
import gregtech.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import gregtech.api.unification.material.registry.MaterialRegistry;

import net.minecraftforge.common.MinecraftForge;

import java.util.Set;

public class PipeBlock extends PipeMaterialBlock implements IBurnable, IFreezable {

    public PipeBlock(PipeStructure structure, MaterialRegistry registry) {
        super(structure, registry);
    }

    public static Set<PipeStructure> gatherStructures() {
        GatherStructuresEvent<PipeStructure> event = new GatherStructuresEvent<>(PipeStructure.class);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getGathered();
    }
}
