package gregtech.api.util;

import net.minecraft.world.World;

public interface IFirstTickTask {

    void handleFirstTick();

    World getWorld();
}
