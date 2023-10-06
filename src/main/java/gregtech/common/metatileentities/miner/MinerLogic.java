package gregtech.common.metatileentities.miner;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.BlockUtility;
import gregtech.api.util.GTTransferUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Objects;

public class MinerLogic<MTE extends MetaTileEntity & IMiner> {

    protected final MTE mte;

    private final int workFrequency;
    private final int maximumDiameter;

    private final MutableBlockPos mpos = new MutableBlockPos();

    protected int currentDiameter;

    private boolean done;
    private boolean workingEnabled = true;

    // last mined ore block
    protected final MutableBlockPos lastMinedOre = new MutableBlockPos();
    protected boolean hasLastMinedOre;
    // number of ores processed so far
    protected int minedOreCount;

    // pipe length used for rendering purposes
    private int pipeLength;

    // non-negative value to limit Y level
    private int yLimit;

    // bool config for repeating the operation after finished
    private boolean repeat;

    // transient values below (not saved)

    @Nullable
    private IMiningArea miningArea;

    // flag indicating last insertion to inventory failed
    private boolean inventoryFull;
    // flag indicating scan area should be rebuilt
    protected boolean rebuildScanArea;
    // status of the last update; true means miner is working, false means no
    private boolean active;

    private boolean preview;

    // remote instance only, contains IMiningArea instances deserialized from packet
    @Nullable
    private IMiningArea previewArea;

    /**
     * Creates the general logic for all in-world ore block miners
     *
     * @param mte             the {@link MetaTileEntity} this logic belongs to
     * @param workFrequency   work frequency in ticks; value of <=1 means the miner operates each tick, 2 means the miner
     *                        operates every other tick and so on
     * @param maximumDiameter the maximum diameter of a square the miner can mine in
     */
    public MinerLogic(@Nonnull MTE mte, int workFrequency, int maximumDiameter) {
        this.mte = mte;
        this.workFrequency = workFrequency;
        this.currentDiameter = this.maximumDiameter = maximumDiameter;
    }

    /**
     * @return the miner's speed in ticks
     */
    public int getWorkFrequency() {
        return this.workFrequency;
    }

    /**
     * @return the miner's maximum diameter
     */
    public int getMaximumDiameter() {
        return this.maximumDiameter;
    }

    /**
     * @return the miner's current diameter
     */
    public int getCurrentDiameter() {
        return this.currentDiameter;
    }

    /**
     * @param currentDiameter the radius to set the miner to use
     */
    public void setCurrentDiameter(int currentDiameter) {
        if (isWorking()) return;
        currentDiameter = Math.max(1, Math.min(currentDiameter, getMaximumDiameter()));
        if (this.currentDiameter != currentDiameter) {
            this.currentDiameter = currentDiameter;
            this.rebuildScanArea = true;
            this.mte.markDirty();
        }
    }

    /**
     * @return true if the miner is finished working
     */
    public boolean isDone() {
        return this.done;
    }

    /**
     * @return true if the miner is active
     */
    public boolean isActive() {
        return this.workingEnabled && this.active;
    }

    /**
     * @return whether working is enabled for the logic
     */
    public boolean isWorkingEnabled() {
        return this.workingEnabled;
    }

    public boolean isInventoryFull() {
        return this.inventoryFull;
    }

    public int getPipeLength() {
        return this.pipeLength;
    }

    @Nullable
    public IMiningArea getPreviewArea() {
        return this.previewArea;
    }

    /**
     * @param isWorkingEnabled the new state of the miner's ability to work: true to change to enabled, else false
     */
    public void setWorkingEnabled(boolean isWorkingEnabled) {
        if (this.workingEnabled != isWorkingEnabled) {
            this.workingEnabled = isWorkingEnabled;
            this.mte.markDirty();
            if (mte.getWorld() != null && !mte.getWorld().isRemote) {
                if (!isWorkingEnabled) reset();
                this.mte.writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
            }
        }
    }

    public int getYLimit() {
        return yLimit;
    }

    public void setYLimit(int yLimit) {
        if (yLimit != this.yLimit) {
            this.yLimit = yLimit;
            this.rebuildScanArea = true;
            this.mte.markDirty();
            if (this.preview) {
                updatePreview();
            }
        }
    }

    public boolean isPreviewEnabled() {
        return preview;
    }

    public void setPreviewEnabled(boolean previewEnabled) {
        if (this.preview != previewEnabled) {
            this.preview = previewEnabled;
            updatePreview();
        }
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        if (this.repeat != repeat) {
            this.repeat = repeat;
            this.mte.markDirty();
        }
    }

    /**
     * @return whether the miner is currently working
     */
    public boolean isWorking() {
        return active && workingEnabled;
    }

    /**
     * Recalculates the mining area and restarts the miner, if it was done
     */
    public void reset() {
        this.hasLastMinedOre = false;
        this.pipeLength = 0;

        this.miningArea = Objects.requireNonNull(createMiningArea(), "createMiningArea() returned null!");

        if (this.done) {
            this.setWorkingEnabled(false);
            this.done = false;
        }
        resetPipeLength();
        this.mte.markDirty();
    }

    /**
     * @return origin position of the miner. Block boundary will be centered around this position, and mining pipes will
     * be rendered under this position.
     */
    @Nonnull
    protected BlockPos getOrigin() {
        return mte.getPos();
    }

    /**
     * Create instance of {@link IMiningArea} based on current state.
     *
     * @return new {@link IMiningArea} instance
     */
    @Nonnull
    protected IMiningArea createMiningArea() {
        BlockPos origin = getOrigin();
        int radius = this.currentDiameter / 2;
        int startX = origin.getX() - radius;
        int startY = origin.getY() - 1;
        int startZ = origin.getZ() - radius;
        int endX = startX + this.currentDiameter;
        int endY = this.yLimit > 0 ? origin.getY() - this.yLimit : Integer.MIN_VALUE;
        int endZ = startZ + this.currentDiameter;
        return new SimpleMiningArea(startX, startY, startZ, endX, endY, endZ);
    }

    @Nonnull
    protected IMiningArea readPreviewArea(@Nonnull PacketBuffer buffer) {
        return SimpleMiningArea.readPreview(buffer);
    }

    /**
     * Performs the actual mining in world. Call this method every tick in update.
     */
    public void update() {
        if (this.mte.getWorld().isRemote ||
                (this.workFrequency >= 2 && this.mte.getOffsetTimer() % this.workFrequency != 0)) {
            return;
        }

        boolean active = mine();
        if (this.active != active) {
            this.active = active;
            this.mte.writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
        }
    }

    private boolean mine() {
        if (!this.workingEnabled || (!this.repeat && this.done) || !canOperate()) {
            return false;
        }

        if (this.rebuildScanArea || this.miningArea == null) {
            this.rebuildScanArea = false;
            reset();
        }

        IMiningArea miningArea = Objects.requireNonNull(this.miningArea);

        if (this.repeat && this.done) {
            this.done = false;
            miningArea.reset();
        }

        World world = mte.getWorld();
        BlockPos origin = getOrigin();
        for (int i = MinerUtil.MAX_BLOCK_SCAN; i > 0; i--) {
            MutableBlockPos pos = this.mpos;
            if (!miningArea.getCurrentBlockPos(pos) || !world.isValid(pos) || (this.yLimit > 0 && origin.getY() - yLimit > pos.getY())) {
                if (this.repeat) {
                    miningArea.reset();
                } else {
                    this.done = true;
                }
                return false;
            }

            IBlockState state = world.getBlockState(pos);
            boolean isOrigin = pos.getX() == origin.getX() && pos.getZ() == origin.getZ();

            // skip unbreakable block / TE blocks
            if (state.getBlockHardness(world, pos) < 0 || state.getBlock().hasTileEntity(state)) {
                // center block (where mining pipes goes in) can be skipped by this, it'll probably look kind of janky
                // but it's 100x better than voiding bedrock
                if (isOrigin) incrementPipeLength();
                miningArea.nextBlock();
                continue;
            }

            boolean isOre = BlockUtility.isOre(state);
            if (!isOrigin && !isOre) {
                miningArea.nextBlock();
                continue;
            }

            NonNullList<ItemStack> blockDrops = NonNullList.create();
            getRegularBlockDrops(blockDrops, world, pos, state);

            if (isOre) {
                IItemHandlerModifiable exportItems = mte.getExportItems();
                if (!GTTransferUtils.addItemsToItemHandler(exportItems, true, blockDrops)) {
                    this.inventoryFull = true;
                    return false;
                }
                GTTransferUtils.addItemsToItemHandler(exportItems, false, blockDrops);
                this.inventoryFull = false;
                this.lastMinedOre.setPos(pos);
                this.minedOreCount++;
            }
            this.mte.drainMiningResources(false);
            world.setBlockState(pos, isOrigin ? Blocks.AIR.getDefaultState() : MinerUtil.getOreReplacement());
            if (isOrigin) incrementPipeLength();
            miningArea.nextBlock();
            onMineOperation(pos, isOre, isOrigin);
            this.mte.markDirty();
            return true;
        }
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean canOperate() {
        return this.mte.drainMiningResources(true);
    }

    /**
     * Called after each block is mined.
     *
     * @param pos      Position of the block mined
     * @param isOre    Whether it was ore block
     * @param isOrigin Whether it was origin (the block mining pipe goes in)
     */
    protected void onMineOperation(@Nonnull BlockPos pos, boolean isOre, boolean isOrigin) {}

    /**
     * called to handle mining regular ores and blocks
     *
     * @param drops the List of items to fill after the operation
     * @param world the {@link WorldServer} the miner is in
     * @param pos   the {@link BlockPos} of the block being mined
     * @param state the {@link IBlockState} of the block being mined
     */
    protected void getRegularBlockDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        state.getBlock().getDrops(drops, world, pos, state, 0); // regular ores do not get fortune applied
    }

    private void incrementPipeLength() {
        this.pipeLength++;
        this.mte.markDirty();
        this.mte.writeCustomData(GregtechDataCodes.PUMP_HEAD_LEVEL, b -> b.writeVarInt(pipeLength));
    }

    private void resetPipeLength() {
        if (this.pipeLength == 0) return;
        this.pipeLength = 0;
        this.mte.markDirty();
        this.mte.writeCustomData(GregtechDataCodes.PUMP_HEAD_LEVEL, b -> b.writeVarInt(pipeLength));
    }

    private void updatePreview() {
        this.mte.writeCustomData(GregtechDataCodes.MINER_UPDATE_PREVIEW, this::writePreviewUpdatePacket);
    }

    private void writePreviewUpdatePacket(@Nonnull PacketBuffer buffer) {
        if (!this.preview) {
            buffer.writeBoolean(false);
            return;
        }
        buffer.writeBoolean(true);
        if (this.miningArea == null) {
            this.miningArea = Objects.requireNonNull(createMiningArea(), "createMiningArea() returned null!");
        }
        this.miningArea.writePreviewPacket(buffer);
        buffer.writeVarInt(this.yLimit);
    }

    private void readPreviewUpdatePacket(@Nonnull PacketBuffer buffer) {
        if (!buffer.readBoolean()) {
            this.previewArea = null;
            return;
        }
        this.previewArea = readPreviewArea(buffer);
        this.yLimit = buffer.readVarInt();
    }

    /**
     * Get the block currently being mined by this miner.
     *
     * @param mpos Mutable block position
     * @return {@code true} if the block exists (in which the {@code mpos} instance gets modified with the value, or
     * {@code false} if it does not exist
     */
    public boolean getCurrentBlock(@Nonnull MutableBlockPos mpos) {
        return this.miningArea != null && this.miningArea.getCurrentBlockPos(mpos);
    }

    /**
     * Write states to NBT. Call this method in {@link MetaTileEntity#writeToNBT(NBTTagCompound)}.
     */
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound data) {
        data.setInteger("currentDiameter", this.currentDiameter);
        if (this.done) data.setBoolean("done", true);
        if (!this.workingEnabled) data.setBoolean("disabled", true);

        if (this.hasLastMinedOre) {
            data.setInteger("lastMinedOreX", this.lastMinedOre.getX());
            data.setInteger("lastMinedOreY", this.lastMinedOre.getY());
            data.setInteger("lastMinedOreZ", this.lastMinedOre.getZ());
        }

        data.setInteger("minedOreCount", this.minedOreCount);
        data.setInteger("pipeLength", this.pipeLength);

        if (this.yLimit > 0) data.setInteger("yLimit", this.yLimit);
        if (this.repeat) data.setBoolean("repeat", true);

        if (this.miningArea != null) {
            this.miningArea.write(data);
        }

        return data;
    }

    /**
     * Read states from NBT. Call this method in {@link MetaTileEntity#readFromNBT(NBTTagCompound)}.
     */
    public void readFromNBT(@Nonnull NBTTagCompound data) {
        this.inventoryFull = false;
        this.active = false;
        this.rebuildScanArea = true;

        if (data.hasKey("xPos", Constants.NBT.TAG_INT)) {
            // retro save compat
            this.currentDiameter = MathHelper.clamp(data.getInteger("currentRadius") * 2 + 1, 1, getMaximumDiameter());

            this.done = data.getInteger("isDone") != 0;
            this.workingEnabled = data.getInteger("isWorkingEnabled") != 0;

            this.hasLastMinedOre = false;
            this.minedOreCount = 0;
            this.pipeLength = data.getInteger("pipeLength");
            return;
        }

        this.currentDiameter = MathHelper.clamp(data.getInteger("currentDiameter"), 1, getMaximumDiameter());
        this.done = data.getBoolean("done");
        this.workingEnabled = !data.getBoolean("disabled");

        if (data.hasKey("lastMinedOreX", Constants.NBT.TAG_INT)) {
            this.lastMinedOre.setPos(data.getInteger("lastMinedOreX"),
                    data.getInteger("lastMinedOreY"),
                    data.getInteger("lastMinedOreZ"));
            this.hasLastMinedOre = true;
        } else {
            this.hasLastMinedOre = false;
        }

        this.minedOreCount = Math.max(0, data.getInteger("minedOreCount"));
        this.pipeLength = Math.max(0, data.getInteger("pipeLength"));

        this.yLimit = Math.max(0, data.getInteger("yLimit"));
        this.repeat = data.getBoolean("repeat");

        this.miningArea = createMiningArea();
        // Shouldn't be a problem but whatever
        //noinspection ConstantValue
        if (this.miningArea != null) {
            this.miningArea.read(data);
        }
    }

    /**
     * Write states to packet buffer. Call this method in {@link MetaTileEntity#writeInitialSyncData(PacketBuffer)}.
     */
    public void writeInitialSyncData(@Nonnull PacketBuffer buf) {
        buf.writeVarInt(this.pipeLength);
        buf.writeBoolean(this.workingEnabled);
        writePreviewUpdatePacket(buf);
    }

    /**
     * Read states from packet buffer. Call this method in {@link MetaTileEntity#receiveInitialSyncData(PacketBuffer)}.
     */
    public void receiveInitialSyncData(@Nonnull PacketBuffer buf) {
        this.pipeLength = buf.readVarInt();
        this.workingEnabled = buf.readBoolean();
        readPreviewUpdatePacket(buf);
    }

    /**
     * Callback for handling custom data packet sent by miner logic. Call this method in {@link MetaTileEntity#receiveCustomData(int, PacketBuffer)}.
     */
    public void receiveCustomData(int dataId, @Nonnull PacketBuffer buf) {
        switch (dataId) {
            case GregtechDataCodes.PUMP_HEAD_LEVEL -> {
                this.pipeLength = buf.readVarInt();
                this.mte.scheduleRenderUpdate();
            }
            case GregtechDataCodes.WORKABLE_ACTIVE -> {
                this.active = buf.readBoolean();
                this.mte.scheduleRenderUpdate();
            }
            case GregtechDataCodes.WORKING_ENABLED -> {
                this.workingEnabled = buf.readBoolean();
                this.mte.scheduleRenderUpdate();
            }
            case GregtechDataCodes.MINER_UPDATE_PREVIEW -> {
                readPreviewUpdatePacket(buf);
            }
        }
    }
}
