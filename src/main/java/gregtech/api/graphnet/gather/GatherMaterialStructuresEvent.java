package gregtech.api.graphnet.gather;

import gregtech.api.graphnet.pipenet.physical.IPipeMaterialStructure;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Set;

public class GatherMaterialStructuresEvent extends Event {

    final Set<IPipeMaterialStructure> gathered = new ObjectOpenHashSet<>();

    GatherMaterialStructuresEvent() {}

    public void registerMaterialStructure(IPipeMaterialStructure materialStructure) {
        gathered.add(materialStructure);
    }

    public Set<IPipeMaterialStructure> getGathered() {
        return gathered;
    }
}
