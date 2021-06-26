package gregtech.common.pipelike.laser.net;
import gregtech.api.pipenet.WorldPipeNet;
import net.minecraft.world.World;
import gregtech.common.pipelike.laser.tile.LaserProperties;
public class WorldLaserNet extends  WorldPipeNet<LaserProperties, LaserPipeNet> {
    private static final String DATA_ID = "gt.laser.net";

    public static WorldLaserNet getWorldENet(World world) {
        WorldLaserNet eNetWorldData = (WorldLaserNet) world.loadData(WorldLaserNet.class, DATA_ID);
        if (eNetWorldData == null) {
            eNetWorldData = new WorldLaserNet(DATA_ID);
            world.setData(DATA_ID, eNetWorldData);
        }
        eNetWorldData.setWorldAndInit(world);
        return eNetWorldData;
    }

    public WorldLaserNet(String name) {
        super(name);
    }

    @Override
    protected LaserPipeNet createNetInstance() {
        return new LaserPipeNet(this);
    }
}
