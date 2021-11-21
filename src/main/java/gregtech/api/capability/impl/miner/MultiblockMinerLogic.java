package gregtech.api.capability.impl.miner;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.render.ICubeRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

public class MultiblockMinerLogic extends MinerLogic {

    private static final int CHUNK_LENGTH = 16;

    private final RecipeMap<?> blockDropRecipeMap;

    private int voltageTier;
    private int overclockAmount = 0;

    private boolean isChunkMode;
    private boolean isSilkTouchMode;

    /**
     * Creates the logic for multiblock ore block miners
     *
     * @param metaTileEntity the {@link MetaTileEntity} this logic belongs to
     * @param fortune        the fortune amount to apply when mining ores
     * @param speed          the speed in ticks per block mined
     * @param maximumRadius  the maximum radius (square shaped) the miner can mine in
     */
    public MultiblockMinerLogic(MetaTileEntity metaTileEntity, int fortune, int speed, int maximumRadius, ICubeRenderer pipeTexture, RecipeMap<?> blockDropRecipeMap) {
        super(metaTileEntity, fortune, speed, maximumRadius, pipeTexture);
        this.blockDropRecipeMap = blockDropRecipeMap;
        this.setActive(false);
    }

    @Override
    protected boolean drainStorages(boolean simulate) {
        return super.drainStorages(simulate) &&  miner.drainFluid(simulate);
    }

    @Override
    protected void getSmallOreBlockDrops(NonNullList<ItemStack> blockDrops, WorldServer world, BlockPos blockToMine, IBlockState blockState) {
        // Small ores: use (fortune bonus + overclockAmount) value here for fortune, since every overclock increases the yield for small ores
        super.getSmallOreBlockDrops(blockDrops, world, blockToMine, blockState);
    }

    @Override
    protected void getRegularBlockDrops(NonNullList<ItemStack> blockDrops, WorldServer world, BlockPos blockToMine, IBlockState blockState) {
        if (!isSilkTouchMode) // 3X the ore compared to the single blocks
            applyTieredHammerNoRandomDrops(world.rand, blockState, blockDrops, 3, this.blockDropRecipeMap, this.voltageTier);
        else
            super.getRegularBlockDrops(blockDrops, world, blockToMine, blockState);
    }

    @Override
    public void initPos(BlockPos pos, int currentRadius) {
        if (!isChunkMode) {
            super.initPos(pos, currentRadius);
        } else {
            WorldServer world = (WorldServer) this.metaTileEntity.getWorld();
            Chunk origin = world.getChunk(this.metaTileEntity.getPos());
            ChunkPos startPos = (world.getChunk(origin.x - currentRadius / CHUNK_LENGTH, origin.z - currentRadius / CHUNK_LENGTH)).getPos();
            getX().set(startPos.getXStart());
            getY().set(this.metaTileEntity.getPos().getY() - 1);
            getZ().set(startPos.getZStart());
            getStartX().set(startPos.getXStart());
            getStartY().set(this.metaTileEntity.getPos().getY());
            getStartZ().set(startPos.getZStart());
            getMineX().set(startPos.getXStart());
            getMineY().set(this.metaTileEntity.getPos().getY() - 1);
            getMineZ().set(startPos.getZStart());
            getPipeY().set(this.metaTileEntity.getPos().getY() - 1);
        }
    }

    public void setVoltageTier(int tier) {
        this.voltageTier = tier;
    }

    public void setOverclockAmount(int amount) {
        this.overclockAmount = amount;
    }

    public int getOverclockAmount() {
        return this.overclockAmount;
    }

    public boolean isChunkMode() {
        return this.isChunkMode;
    }

    public void setChunkMode(boolean isChunkMode) {
        if (!isActive()) {
            getX().set(Integer.MAX_VALUE);
            getY().set(Integer.MAX_VALUE);
            getZ().set(Integer.MAX_VALUE);
            this.isChunkMode = isChunkMode;
        }
    }

    public boolean isSilkTouchMode() {
        return this.isSilkTouchMode;
    }

    public void setSilkTouchMode(boolean isSilkTouchMode) {
        if (!isActive())
            this.isSilkTouchMode = isSilkTouchMode;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setBoolean("isChunkMode", isChunkMode);
        data.setBoolean("isSilkTouchMode", isSilkTouchMode);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        this.isChunkMode = data.getBoolean("isChunkMode");
        this.isSilkTouchMode = data.getBoolean("isSilkTouchMode");
        super.readFromNBT(data);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        buf.writeBoolean(this.isChunkMode);
        buf.writeBoolean(this.isSilkTouchMode);
        super.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        this.isChunkMode = buf.readBoolean();
        this.isSilkTouchMode = buf.readBoolean();
        super.receiveInitialSyncData(buf);
    }
}
