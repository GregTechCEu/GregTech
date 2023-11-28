package gregtech.api.util.world;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DummyWorld extends World {

    private static final WorldSettings DEFAULT_SETTINGS = new WorldSettings(
            1L, GameType.SURVIVAL, true, false, WorldType.DEFAULT);

    public static final DummyWorld INSTANCE = new DummyWorld();

    public DummyWorld() {
        super(new DummySaveHandler(), new WorldInfo(DEFAULT_SETTINGS, "DummyServer"), new WorldProviderSurface(),
                new Profiler(), false);
        // Guarantee the dimension ID was not reset by the provider
        this.provider.setDimension(Integer.MAX_VALUE);
        int providerDim = this.provider.getDimension();
        this.provider.setWorld(this);
        this.provider.setDimension(providerDim);
        this.chunkProvider = this.createChunkProvider();
        this.calculateInitialSkylight();
        this.calculateInitialWeather();
        this.getWorldBorder().setSize(30000000);
        // De-allocate lightUpdateBlockList, checkLightFor uses this
        ObfuscationReflectionHelper.setPrivateValue(World.class, this, null,
                FMLLaunchHandler.isDeobfuscatedEnvironment() ? "lightUpdateBlockList" : "field_72994_J");
    }

    @Override
    public void notifyNeighborsRespectDebug(@NotNull BlockPos pos, @NotNull Block blockType, boolean p_175722_3_) {
        // NOOP - do not trigger forge events
    }

    @Override
    public void notifyNeighborsOfStateChange(@NotNull BlockPos pos, @NotNull Block blockType, boolean updateObservers) {
        // NOOP - do not trigger forge events
    }

    @Override
    public void notifyNeighborsOfStateExcept(@NotNull BlockPos pos, @NotNull Block blockType,
                                             @NotNull EnumFacing skipSide) {
        // NOOP - do not trigger forge events
    }

    @Override
    public void markAndNotifyBlock(@NotNull BlockPos pos, @Nullable Chunk chunk, @NotNull IBlockState iblockstate,
                                   @NotNull IBlockState newState, int flags) {
        // NOOP - do not trigger forge events
    }

    @Override
    public void notifyBlockUpdate(@NotNull BlockPos pos, @NotNull IBlockState oldState, @NotNull IBlockState newState,
                                  int flags) {}

    @Override
    public void markBlockRangeForRenderUpdate(@NotNull BlockPos rangeMin, @NotNull BlockPos rangeMax) {}

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {}

    @Override
    public void updateObservingBlocksAt(@NotNull BlockPos pos, @NotNull Block blockType) {}

    @NotNull
    @Override
    protected IChunkProvider createChunkProvider() {
        return new DummyChunkProvider(this);
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return chunkProvider.isChunkGeneratedAt(x, z);
    }

    @Override
    // De-allocated lightUpdateBlockList, default return
    public boolean checkLightFor(@NotNull EnumSkyBlock lightType, @NotNull BlockPos pos) {
        return true;
    }
}
