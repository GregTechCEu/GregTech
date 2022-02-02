package gregtech.api.worldgen.shape;

public interface IBlockGeneratorAccess {

    default boolean generateBlock(int x, int y, int z) {
        return generateBlock(x, y, z, true);
    }

    boolean generateBlock(int x, int y, int z, boolean withRandom);
}
