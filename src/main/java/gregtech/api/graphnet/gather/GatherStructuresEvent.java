package gregtech.api.graphnet.gather;

import gregtech.api.graphnet.pipenet.physical.IPipeStructure;

import net.minecraftforge.fml.common.eventhandler.Event;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Set;

public class GatherStructuresEvent<T extends IPipeStructure> extends Event {

    final Set<T> gathered = new ObjectOpenHashSet<>();

    final Class<T> structure;

    public GatherStructuresEvent(Class<T> structure) {
        this.structure = structure;
    }

    public void registerMaterialStructure(T materialStructure) {
        gathered.add(materialStructure);
    }

    public Set<T> getGathered() {
        return gathered;
    }

    public Class<T> getStructure() {
        return structure;
    }
}
