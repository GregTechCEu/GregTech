package gregtech.api.pipenet.longdist;

import gregtech.common.pipelike.fluidpipe.longdistance.LDFluidPipeType;
import gregtech.common.pipelike.itempipe.longdistance.LDItemPipeType;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This class defines a long distance pipe type. This class MUST be a singleton class!
 */
public abstract class LongDistancePipeType {

    private static final Object2ObjectOpenHashMap<String, LongDistancePipeType> PIPE_TYPES = new Object2ObjectOpenHashMap<>();

    private final String name;

    protected LongDistancePipeType(String name) {
        this.name = Objects.requireNonNull(name);
        if (PIPE_TYPES.containsKey(name)) {
            throw new IllegalArgumentException("Pipe Type with name " + name + " already exists!");
        }
        for (LongDistancePipeType pipeType : PIPE_TYPES.values()) {
            if (this.getClass() == pipeType.getClass()) {
                throw new IllegalStateException("Duplicate Pipe Type " + name + " and " + pipeType.name);
            }
        }
        PIPE_TYPES.put(name, this);
    }

    public static LDFluidPipeType fluid() {
        return LDFluidPipeType.INSTANCE;
    }

    public static LDItemPipeType item() {
        return LDItemPipeType.INSTANCE;
    }

    public static LongDistancePipeType getPipeType(String name) {
        return PIPE_TYPES.get(name);
    }

    public boolean isValidPart(ILDNetworkPart networkPart) {
        return networkPart != null && networkPart.getPipeType() == this;
    }

    /**
     * @return The minimum required distance (not pipe count between endpoints) between to endpoints to work.
     */
    public int getMinLength() {
        return 0;
    }

    public boolean satisfiesMinLength(ILDEndpoint endpoint1, ILDEndpoint endpoint2) {
        BlockPos p = endpoint2.pos();
        return endpoint1 != endpoint2 && endpoint1.pos().getDistance(p.getX(), p.getY(), p.getZ()) >= getMinLength();
    }

    @NotNull
    public LongDistanceNetwork createNetwork(LongDistanceNetwork.WorldData worldData) {
        return new LongDistanceNetwork(this, worldData);
    }

    public final LongDistanceNetwork createNetwork(World world) {
        return createNetwork(LongDistanceNetwork.WorldData.get(world));
    }

    public final String getName() {
        return name;
    }
}
