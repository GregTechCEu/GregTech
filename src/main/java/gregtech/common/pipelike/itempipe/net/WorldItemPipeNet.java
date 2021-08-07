package gregtech.common.pipelike.itempipe.net;

import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.unification.material.properties.ItemPipeProperties;
import net.minecraft.world.World;

public class WorldItemPipeNet extends WorldPipeNet<ItemPipeProperties, ItemPipeNet> {

    private static final String DATA_ID = "gregtech.item_pipe_net";

    public static WorldItemPipeNet getWorldPipeNet(World world) {
        WorldItemPipeNet netWorldData = (WorldItemPipeNet) world.loadData(WorldItemPipeNet.class, DATA_ID);
        if (netWorldData == null) {
            netWorldData = new WorldItemPipeNet(DATA_ID);
            world.setData(DATA_ID, netWorldData);
        }
        netWorldData.setWorldAndInit(world);
        return netWorldData;
    }

    public WorldItemPipeNet(String name) {
        super(name);
    }

    @Override
    protected ItemPipeNet createNetInstance() {
        return new ItemPipeNet(this);
    }
}
