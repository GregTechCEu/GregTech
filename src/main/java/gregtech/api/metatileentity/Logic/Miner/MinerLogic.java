package gregtech.api.metatileentity.Logic.Miner;


import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.MatchingMode;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.render.SimpleCubeRenderer;
import gregtech.api.render.Textures;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MinerLogic {

    private static final short MAX_SPEED = Short.MAX_VALUE;
    private static final byte POWER = 5;
    private static final byte TICK_TOLERANCE = 20;
    private static final double DIVIDEND = MAX_SPEED * Math.pow(TICK_TOLERANCE, POWER);

    private final MetaTileEntity metaTileEntity;
    private final IMiner miner;

    private final int fortune;
    private final int speed;
    private final int maximumRadius;

    private final SimpleCubeRenderer PIPE_TEXTURE;

    private final LinkedList<BlockPos> blocksToMine = new LinkedList<>();

    private final AtomicInteger x = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger y = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger z = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger startX = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger startZ = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger startY = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger tempY = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger mineX = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger mineZ = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger mineY = new AtomicInteger(Integer.MAX_VALUE);

    private int pipeLength = 0;
    private int currentRadius;
    private boolean isDone;
    private boolean isActive = true;

    /**
     * Creates the logic for all types of in-world ore block miners
     *
     * @param metaTileEntity the {@link MetaTileEntity} this logic belongs to
     * @param fortune the fortune amount to apply when mining ores
     * @param speed the speed in ticks per block mined
     * @param maximumRadius the maximum radius (square shaped) the miner can mine in
     */
    public MinerLogic(MetaTileEntity metaTileEntity, int fortune, int speed, int maximumRadius, SimpleCubeRenderer pipeTexture) {
        this.metaTileEntity = metaTileEntity;
        this.miner = (IMiner) metaTileEntity;
        this.fortune = fortune;
        this.speed = speed;
        this.currentRadius = maximumRadius;
        this.maximumRadius = maximumRadius;
        this.isDone = false;
        this.PIPE_TEXTURE = pipeTexture;
    }

    /**
     * Performs the actual mining in world
     * Call this method every tick in update
     */
    public void performMining() {
        // Needs to be server side
        if (metaTileEntity.getWorld().isRemote)
            return;

        // Inactive miners do nothing
        if (!this.isActive)
            return;

        // if the miner is finished, the target coordinates are invalid, or it cannot drain energy, stop
        if (isDone || checkCoordinatesInvalid(x, y, z) || !miner.drainEnergy(true)) {
            // if the miner is not finished and has invalid coordinates, get new and valid starting coordinates
            if (!isDone && checkCoordinatesInvalid(x, y, z))
                initPos(metaTileEntity.getPos(), currentRadius);

            // reset the miner's inventory and don't do anything else this time
            miner.resetInventory();
            return;
        }

        // do not mine anything if the inventory is full, preventing voiding
        if (miner.isInventoryFull())
            return;

        // actually drain the energy
        miner.drainEnergy(false);

        // drill a hole beneath the miner and extend the pipe downwards by one
        WorldServer world = (WorldServer) metaTileEntity.getWorld();
        if (mineY.get() < tempY.get()) {
            world.destroyBlock(new BlockPos(metaTileEntity.getPos().getX(), tempY.get(), metaTileEntity.getPos().getZ()), false);
            tempY.decrementAndGet();
            incrementPipeLength();
        }

        // check if the miner needs new blocks to mine and get them if needed
        checkBlocksToMine();

        // if there are blocks to mine and the correct amount of time has passed, do the mining
        if (metaTileEntity.getOffsetTimer() % this.speed == 0 && !blocksToMine.isEmpty()) {
            NonNullList<ItemStack> blockDrops = NonNullList.create();
            IBlockState blockState = metaTileEntity.getWorld().getBlockState(blocksToMine.getFirst());

            // if the block is not air, harvest it
            if (blockState != Blocks.AIR.getDefaultState()) {
                    /*small ores
                        if orePrefix of block in blockPos is small
                            applyTieredHammerNoRandomDrops...
                        else
                            current code...
                    */
                // get the block's drops. If it can fit in the inventory, move the previously mined position to the block
                // replace the ore block with cobblestone instead of breaking it to prevent mob spawning
                // remove the ore block's position from the mining queue
                blockState.getBlock().getDrops(blockDrops, world, blocksToMine.getFirst(), blockState, this.fortune);
                if (MetaTileEntity.addItemsToItemHandler(metaTileEntity.getExportItems(), true, blockDrops)) {
                    MetaTileEntity.addItemsToItemHandler(metaTileEntity.getExportItems(), false, blockDrops);
                    world.setBlockState(blocksToMine.getFirst(), Blocks.COBBLESTONE.getDefaultState());
                    mineX.set(blocksToMine.getFirst().getX());
                    mineZ.set(blocksToMine.getFirst().getZ());
                    mineY.set(blocksToMine.getFirst().getY());
                    blocksToMine.removeFirst();

                    // if the inventory was previously considered full, mark it as not since an item was able to fit
                    if (miner.isInventoryFull())
                        miner.setInventoryFull(false);
                } else {

                    // the ore block was not able to fit, so the inventory is considered full
                    miner.setInventoryFull(true);
                }
            } else {
                // the block attempted to mine was air, so remove it from the queue and move on
                blocksToMine.removeFirst();
            }
        } else if (blocksToMine.isEmpty()) {
            // there were no blocks to mine, so the current position is the previous position
            x.set(mineX.get());
            y.set(mineY.get());
            z.set(mineZ.get());

            // attempt to get more blocks to mine, if there are none, the miner is done mining
            blocksToMine.addAll(getBlocksToMine());
            if (blocksToMine.isEmpty()) {
                this.isDone = true;
                this.setActive(false);
            }
        }
    }

    /**
     * This method designates the starting position for mining blocks
     *
     * @param pos the {@link BlockPos} of the miner itself
     * @param currentRadius the currently set mining radius
     */
    private void initPos(BlockPos pos, int currentRadius) {
        x.set(pos.getX() - currentRadius);
        z.set(pos.getZ() - currentRadius);
        y.set(pos.getY() - 1);
        startX.set(pos.getX() - currentRadius);
        startZ.set(pos.getZ() - currentRadius);
        startY.set(pos.getY());
        tempY.set(pos.getY() - 1);
        mineX.set(pos.getX() - currentRadius);
        mineZ.set(pos.getZ() - currentRadius);
        mineY.set(pos.getY() - 1);
    }

    /**
     * Checks if the current coordinates are invalid
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return {@code true} if the coordinates are invalid, else false
     */
    private static boolean checkCoordinatesInvalid(AtomicInteger x, AtomicInteger y, AtomicInteger z) {
        return x.get() == Integer.MAX_VALUE && y.get() == Integer.MAX_VALUE && z.get() == Integer.MAX_VALUE;
    }

    /**
     * Checks whether there are any more blocks to mine, if there are currently none queued
     */
    public void checkBlocksToMine() {
        if (blocksToMine.isEmpty())
            blocksToMine.addAll(getBlocksToMine());
    }

    /**
     * Gets the blocks to mine
     * @return a {@link LinkedList} of {@link BlockPos} for each ore to mine
     */
    private LinkedList<BlockPos> getBlocksToMine() {
        LinkedList<BlockPos> blocks = new LinkedList<>();

        // determine how many blocks to retrieve this time
        double quotient = getQuotient(getMeanTickTime(metaTileEntity.getWorld()));
        int calcAmount = quotient < 1 ? 1 : (int) (Math.min(quotient, Short.MAX_VALUE));
        int calculated = 0;

        // keep getting blocks until the target amount is reached
        while (calculated < calcAmount) {
            // moving down the y-axis
            if (y.get() > 0) {
                // moving across the z-axis
                if (z.get() <= startZ.get() + currentRadius * 2) {
                    // check every block along the x-axis
                    if (x.get() <= startX.get() + currentRadius * 2) {
                        BlockPos blockPos = new BlockPos(x.get(), y.get(), z.get());
                        Block block = metaTileEntity.getWorld().getBlockState(blockPos).getBlock();
                        if (block.blockHardness >= 0 && metaTileEntity.getWorld().getTileEntity(blockPos) == null && GTUtility.isOre(block)) {
                            blocks.addLast(blockPos);
                        }
                        // move to the next x position
                        x.incrementAndGet();
                    } else {
                        // reset x and move to the next z layer
                        x.set(x.get() - currentRadius * 2);
                        z.incrementAndGet();
                    }
                } else {
                    // reset z and move to the next y layer
                    z.set(z.get() - currentRadius * 2);
                    y.decrementAndGet();
                }
            } else
                return blocks;

            // only count iterations where blocks were found
            if (!blocks.isEmpty())
                calculated++;
        }
        return blocks;
    }

    /**
     * gets the quotient for determining the amount of blocks to mine
     * @param base is a value used for calculation, intended to be the mean tick time of the world the miner is in
     * @return the quotient
     */
    private static double getQuotient(double base) {
        return DIVIDEND / Math.pow(base, POWER);
    }

    /**
     * @param values to find the mean of
     * @return the mean
     */
    private static long mean(long[] values) {
        long sum = 0L;
        for (long v : values)
            sum += v;
        return sum / values.length;
    }

    /**
     *
     * @param world the {@link World} the miner is in
     * @return the mean tick time
     */
    private static double getMeanTickTime(World world) {
        return mean(Objects.requireNonNull(world.getMinecraftServer()).tickTimeArray) * 1.0E-6D;
    }

    /**
     * Applies a fortune hammer to block drops based on a tier value, intended for small ores
     * @param random the random chance component for recipe ChancedOutputs
     * @param blockState the block being mined
     * @param drops where the drops are stored to
     * @param fortuneLevel the level of fortune used
     * @param player used to determine whether there is a fake player being used
     * @param map the recipemap from which to get the drops
     * @param tier the tier at which the operation is performed, used for calculating the chanced output boost
     */
    private static void applyTieredHammerNoRandomDrops(Random random, IBlockState blockState, List<ItemStack> drops, int fortuneLevel, EntityPlayer player, RecipeMap<?> map, int tier) {
        ItemStack itemStack = new ItemStack(blockState.getBlock(), 1, blockState.getBlock().getMetaFromState(blockState));
        Recipe recipe = map.findRecipe(Long.MAX_VALUE, Collections.singletonList(itemStack), Collections.emptyList(), 0, MatchingMode.IGNORE_FLUIDS);
        if (recipe != null && !recipe.getOutputs().isEmpty()) {
            drops.clear();
            for (ItemStack outputStack : recipe.getResultItemOutputs(Integer.MAX_VALUE, random, tier)) {
                outputStack = outputStack.copy();
                if (!(player instanceof FakePlayer) && OreDictUnifier.getPrefix(outputStack) == OrePrefix.crushed) {
                    if (fortuneLevel > 0) {
                        int i = Math.max(0, fortuneLevel - 1);
                        outputStack.grow(outputStack.getCount() * i);
                    }
                }
                drops.add(outputStack);
            }
        }
    }

    /**
     * Increments the pipe rendering length by one, signaling that the miner's y level has moved down by one
     */
    private void incrementPipeLength() {
        this.pipeLength++;
        this.metaTileEntity.writeCustomData(GregtechDataCodes.PUMP_HEAD_LEVEL, b -> b.writeInt(pipeLength));
        this.metaTileEntity.markDirty();
    }

    /**
     * renders the pipe beneath the miner
     */
    public void renderPipe(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.PIPE_IN_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
        for (int i = 0; i < this.pipeLength; i++) {
            translation.translate(0.0, -1.0, 0.0);
            PIPE_TEXTURE.render(renderState, translation, pipeline, IMiner.PIPE_CUBOID);
        }
    }

    /**
     * writes all needed values to NBT
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#writeToNBT(NBTTagCompound)} method
     */
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setTag("xPos", new NBTTagInt(x.get()));
        data.setTag("yPos", new NBTTagInt(y.get()));
        data.setTag("zPos", new NBTTagInt(z.get()));
        data.setTag("mxPos", new NBTTagInt(mineX.get()));
        data.setTag("myPos", new NBTTagInt(mineY.get()));
        data.setTag("mzPos", new NBTTagInt(mineZ.get()));
        data.setTag("sxPos", new NBTTagInt(startX.get()));
        data.setTag("syPos", new NBTTagInt(startY.get()));
        data.setTag("szPos", new NBTTagInt(startZ.get()));
        data.setTag("tempY", new NBTTagInt(tempY.get()));
        data.setTag("isActive", new NBTTagInt(this.isActive ? 1 : 0));
        data.setTag("pipeLength", new NBTTagInt(pipeLength));
        data.setTag("currentRadius", new NBTTagInt(currentRadius));
        data.setTag("isDone", new NBTTagInt(isDone ? 1 : 0));
        return data;
    }

    /**
     * reads all needed values from NBT
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#readFromNBT(NBTTagCompound)} method
     */
    public void readFromNBT(NBTTagCompound data) {
        x.set(data.getInteger("xPos"));
        y.set(data.getInteger("yPos"));
        z.set(data.getInteger("zPos"));
        mineX.set(data.getInteger("mxPos"));
        mineY.set(data.getInteger("myPos"));
        mineZ.set(data.getInteger("mzPos"));
        startX.set(data.getInteger("sxPos"));
        startY.set(data.getInteger("syPos"));
        startZ.set(data.getInteger("szPos"));
        tempY.set(data.getInteger("tempY"));
        setActive(data.getInteger("isActive") != 0);
        pipeLength = data.getInteger("pipeLength");
        this.currentRadius = data.getInteger("currentRadius");
        this.isDone = data.getInteger("isDone") != 0;
    }

    /**
     * writes all needed values to InitialSyncData
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#writeInitialSyncData(PacketBuffer)} method
     */
    public void writeInitialSyncData(PacketBuffer buf) {
        buf.writeInt(pipeLength);
        buf.writeBoolean(this.isActive);
    }

    /**
     * reads all needed values from InitialSyncData
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#receiveInitialSyncData(PacketBuffer)} method
     */
    public void receiveInitialSyncData(PacketBuffer buf) {
        this.pipeLength = buf.readInt();
        setActive(buf.readBoolean());
    }

    /**
     * reads all needed values from CustomData
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#receiveCustomData(int, PacketBuffer)} method
     */
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == GregtechDataCodes.PUMP_HEAD_LEVEL) {
            this.pipeLength = buf.readInt();
            metaTileEntity.getHolder().scheduleChunkForRenderUpdate();
        } else if (dataId == GregtechDataCodes.IS_WORKING) {
            setActive(buf.readBoolean());
            metaTileEntity.getHolder().scheduleChunkForRenderUpdate();
        }
    }

    /**
     *
     * @return the current x value
     */
    public AtomicInteger getX() {
        return x;
    }

    /**
     *
     * @return the current y value
     */
    public AtomicInteger getY() {
        return y;
    }

    /**
     *
     * @return the current z value
     */
    public AtomicInteger getZ() {
        return z;
    }

    /**
     *
     * @return the previously mined x value
     */
    public AtomicInteger getMineX() {
        return mineX;
    }

    /**
     *
     * @return the previously mined y value
     */
    public AtomicInteger getMineY() {
        return mineY;
    }

    /**
     *
     * @return the previously mined z value
     */
    public AtomicInteger getMineZ() {
        return mineZ;
    }

    /**
     *
     * @return the miner's maximum radius
     */
    public int getMaximumRadius() {
        return this.maximumRadius;
    }

    /**
     *
     * @return the miner's current radius
     */
    public int getCurrentRadius() {
        return this.currentRadius;
    }

    /**
     *
     * @param currentRadius the radius to set the miner to use
     */
    public void setCurrentRadius(int currentRadius) {
        this.currentRadius = currentRadius;
    }

    /**
     *
     * @return true if the miner is finished working
     */
    public boolean isDone() {
        return this.isDone;
    }

    /**
     *
     * @return true if the miner is actively working
     */
    public boolean isActive() {
        return this.isActive;
    }

    /**
     *
     * @param active the new state of the miner's activity: true to change to active, else false
     */
    public void setActive(boolean active) {
        this.isActive = active;
        this.metaTileEntity.markDirty();
        if (metaTileEntity.getWorld() != null && !metaTileEntity.getWorld().isRemote) {
            this.metaTileEntity.writeCustomData(GregtechDataCodes.IS_WORKING, buf -> buf.writeBoolean(active));
        }
    }

    /**
     *
     * @return the miner's fortune level
     */
    public int getFortune() {
        return this.fortune;
    }

    /**
     *
     * @return the miner's speed in ticks
     */
    public int getSpeed() {
        return this.speed;
    }
}
