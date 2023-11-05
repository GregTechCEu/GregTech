package gregtech.common.metatileentities.miner;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.BlockUtility;
import gregtech.api.util.GTTransferUtils;
import gregtech.common.metatileentities.miner.Miner.MinedBlockType;
import net.minecraft.block.material.Material;
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
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class MinerLogic<MTE extends MetaTileEntity & Miner> {

    protected final MTE mte;

    private final int workFrequency;
    private final int maximumDiameter;

    private final MutableBlockPos mpos = new MutableBlockPos();

    protected int currentDiameter;

    // flag indicating the miner has finished its action
    protected boolean done;

    private boolean workingEnabled = true;

    // pipe length used for rendering purposes
    private int pipeLength;

    // transient fields below (not saved to NBT)

    @Nullable
    private MiningArea miningArea;

    private boolean active;
    private int workTick;
    // flag indicating last insertion to inventory failed
    private boolean inventoryFull;
    // flag indicating mining area should be rebuilt
    protected boolean rebuildMiningArea;
    // flag for area preview
    private boolean preview;

    private boolean hasNotEnoughEnergy;

    // remote instances only, contains MiningArea instances deserialized from packet
    @Nullable
    private MiningArea previewArea;

    /**
     * Creates the general logic for all in-world ore block miners
     *
     * @param mte             the {@link MetaTileEntity} this logic belongs to
     * @param workFrequency   work frequency in ticks. In other words, the miner will operate every
     *                        {@code workFrequency} ticks; once a second in {@code 20}, every other tick in {@code 2},
     *                        and each tick in {@code 1}.
     * @param maximumDiameter the maximum diameter of a square the miner can mine in
     * @throws IllegalArgumentException if {@code workFrequency <= 0}
     * @throws NullPointerException     if {@code mte == null}
     */
    public MinerLogic(@Nonnull MTE mte, int workFrequency, int maximumDiameter) {
        if (workFrequency <= 0) throw new IllegalArgumentException("workFrequency <= 0");
        this.mte = Objects.requireNonNull(mte, "mte == null");
        this.workFrequency = workFrequency;
        this.currentDiameter = this.maximumDiameter = maximumDiameter;
    }

    public int getWorkFrequency() {
        return this.workFrequency;
    }

    public int getMaximumDiameter() {
        return this.maximumDiameter;
    }

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
            this.rebuildMiningArea = true;
            this.mte.markDirty();
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isInventoryFull() {
        return this.inventoryFull;
    }

    public int getPipeLength() {
        return this.pipeLength;
    }

    @Nullable
    public MiningArea getPreviewArea() {
        return this.previewArea;
    }

    public boolean isAtWorkTick() {
        if (this.workFrequency == 1) return true;
        return this.mte.getOffsetTimer() % this.workFrequency == this.workTick;
    }

    /**
     * <p>
     * Set next work tick to {@code workTick} after current tick. When called during {@link #update()}, this method
     * essentially sets next operation at {@code workTick} after. If not called, the next update happens
     * {@link #workFrequency} ticks after.
     * </p>
     * <p>
     * If {@code workTick} is greater or equal than {@link #workFrequency}, {@code workTick % workFrequency} will be
     * used instead.
     * </p>
     */
    protected void setNextWorkTick(int workTick) {
        this.workTick = (this.workTick + workTick) % this.workFrequency;
    }

    public double getWorkProgress() {
        if (!isWorking()) return 0;
        if (getWorkFrequency() < 2) return 1;
        return ((mte.getOffsetTimer() + getWorkFrequency() - workTick) % getWorkFrequency()) / (double) getWorkFrequency();
    }

    /**
     * @return whether working is enabled for the logic
     */
    public boolean isWorkingEnabled() {
        return this.workingEnabled;
    }

    /**
     * @param isWorkingEnabled the new state of the miner's ability to work: true to change to enabled, else false
     */
    public void setWorkingEnabled(boolean isWorkingEnabled) {
        if (this.workingEnabled != isWorkingEnabled) {
            this.workingEnabled = isWorkingEnabled;
            this.mte.markDirty();
            if (mte.getWorld() != null && !mte.getWorld().isRemote) {
                this.mte.writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
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

    /**
     * @return whether the miner is currently working
     */
    public boolean isWorking() {
        return this.miningArea != null && !this.done && isWorkingEnabled() && !hasNotEnoughEnergy();
    }

    public boolean hasNotEnoughEnergy() {
        return hasNotEnoughEnergy;
    }

    /**
     * @return origin position of the miner. Block boundary will be centered around this position, and mining pipes will
     * be rendered under this position.
     */
    @Nonnull
    protected BlockPos getOrigin() {
        return mte.getPos();
    }

    @Nullable
    public final MiningArea getMiningArea() {
        return this.miningArea;
    }

    /**
     * Create instance of {@link MiningArea} based on current state.
     *
     * @return new {@link MiningArea} instance
     */
    @Nonnull
    protected MiningArea createMiningArea() {
        BlockPos origin = getOrigin();
        int radius = this.currentDiameter / 2;
        int startX = origin.getX() - radius;
        int startZ = origin.getZ() - radius;
        return new SimpleMiningArea(startX, origin.getY() - 1, startZ,
                startX + this.currentDiameter, Integer.MIN_VALUE, startZ + this.currentDiameter);
    }

    /**
     * Recalculates the mining area and restarts the miner, if it was done
     */
    public void reset() {
        setPipeLength(0);
        rebuildMiningArea();

        this.mte.markDirty();
    }

    private void rebuildMiningArea() {
        this.rebuildMiningArea = false;
        this.miningArea = Objects.requireNonNull(createMiningArea(), "createMiningArea() returned null!");
        if (isPreviewEnabled()) {
            updatePreview();
        }
    }

    protected void updatePreview() {
        this.mte.writeCustomData(GregtechDataCodes.MINER_UPDATE_PREVIEW, this::writePreviewUpdatePacket);
    }

    /**
     * Performs the actual mining in world. Call this method every tick in update.
     */
    public void update() {
        if (this.mte.getWorld().isRemote) return;

        // rebuild scan area every tick regardless of miner status, for accurate preview
        if (this.rebuildMiningArea || this.miningArea == null) {
            rebuildMiningArea();
        }

        if (this.mte.drainMiningResources(MinedBlockType.ORE, true, true)) {
            this.hasNotEnoughEnergy = false;
        } else {
            this.hasNotEnoughEnergy = true;
            return;
        }

        mine(Objects.requireNonNull(this.miningArea));
        boolean active = !this.done;
        if (this.active != active) {
            this.active = active;
            this.mte.writeCustomData(GregtechDataCodes.MINER_UPDATE_ACTIVE, b -> b.writeBoolean(active));
        }
    }

    protected void mine(@Nonnull MiningArea miningArea) {
        if (this.done || !isAtWorkTick() || !this.workingEnabled || !this.mte.canOperate()) {
            return;
        }
        World world = mte.getWorld();
        BlockPos origin = getOrigin();
        MutableBlockPos pos = this.mpos;

        for (int i = MinerUtil.MAX_BLOCK_SCAN; i > 0; i--) {
            if (!getCurrentBlock(miningArea, pos)) {
                this.done = true;
                return;
            }

            IBlockState state = world.getBlockState(pos);
            boolean isOrigin = pos.getX() == origin.getX() && pos.getZ() == origin.getZ();

            // skip air, unbreakable blocks & TE blocks
            if (state.getMaterial() == Material.AIR ||
                    state.getBlockHardness(world, pos) < 0 ||
                    state.getBlock().hasTileEntity(state)) {
                if (isOrigin) { // TODO liquid tile check?
                    // center block (where mining pipes goes in) can be skipped by this, it'll probably look kind of janky
                    // but it's 100x better than voiding bedrock
                    if (!this.mte.drainMiningResources(MinedBlockType.NOTHING, true, false)) {
                        return;
                    }
                    setPipeLength(this.pipeLength + 1);
                }
                miningArea.nextBlock();
                continue;
            }

            boolean isOre = BlockUtility.isOre(state);
            if (!isOrigin && !isOre) {
                miningArea.nextBlock();
                continue;
            }

            if (!this.mte.drainMiningResources(isOre ? MinedBlockType.ORE : MinedBlockType.BLOCK, true, false)) {
                return;
            }

            NonNullList<ItemStack> blockDrops = NonNullList.create();
            this.mte.getRegularBlockDrops(blockDrops, world, pos, state);

            if (isOre) {
                IItemHandlerModifiable exportItems = mte.getExportItems();
                if (!GTTransferUtils.addItemsToItemHandler(exportItems, true, blockDrops)) {
                    this.inventoryFull = true;
                    return;
                }
                GTTransferUtils.addItemsToItemHandler(exportItems, false, blockDrops);
                this.inventoryFull = false;
            }
            world.setBlockState(pos, isOrigin ? Blocks.AIR.getDefaultState() : getOreReplacement());
            if (isOrigin) setPipeLength(this.pipeLength + 1);
            miningArea.nextBlock();
            this.mte.onMineOperation(pos, isOre, isOrigin);
            this.mte.markDirty();
            return;
        }
        setNextWorkTick(1); // scan next tick
    }

    @Nonnull
    protected IBlockState getOreReplacement() {
        return MinerUtil.getOreReplacement();
    }

    private void setPipeLength(int length) {
        if (this.pipeLength == length) return;
        this.pipeLength = length;
        this.mte.markDirty();
        this.mte.writeCustomData(GregtechDataCodes.PUMP_HEAD_LEVEL, b -> b.writeVarInt(length));
    }

    protected void writePreviewUpdatePacket(@Nonnull PacketBuffer buffer) {
        if (this.preview) {
            MiningArea miningArea = this.miningArea;
            if (miningArea != null) {
                buffer.writeBoolean(true);
                miningArea.writePreviewPacket(buffer);
                return;
            }
        }
        buffer.writeBoolean(false);
    }

    protected void readPreviewUpdatePacket(@Nonnull PacketBuffer buffer) {
        this.previewArea = buffer.readBoolean() ? readPreviewArea(buffer) : null;
    }

    @Nonnull
    protected MiningArea readPreviewArea(@Nonnull PacketBuffer buffer) {
        return SimpleMiningArea.readPreview(buffer);
    }

    /**
     * Get the block currently being mined by this miner. This method only works on server environment.
     *
     * @param mpos Mutable block position
     * @return {@code true} if the block exists (in which the {@code mpos} instance gets modified with the value, or
     * {@code false} if it does not exist
     */
    public boolean getCurrentBlock(@Nonnull MiningArea miningArea, @Nonnull MutableBlockPos mpos) {
        return miningArea.getCurrentBlockPos(mpos) && !mte.getWorld().isOutsideBuildHeight(mpos);
    }

    /**
     * Write states to NBT. Call this method in {@link MetaTileEntity#writeToNBT(NBTTagCompound)}.
     */
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound data) {
        data.setInteger("currentDiameter", this.currentDiameter);
        if (!this.workingEnabled) data.setBoolean("disabled", true);
        if (this.done) data.setBoolean("done", true);

        data.setInteger("pipeLength", this.pipeLength);

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
        this.rebuildMiningArea = false;

        if (data.hasKey("xPos", Constants.NBT.TAG_INT)) {
            // retro save compat
            this.currentDiameter = MathHelper.clamp(data.getInteger("currentRadius") * 2 + 1, 1, getMaximumDiameter());

            this.workingEnabled = data.getInteger("isWorkingEnabled") != 0;
            this.pipeLength = data.getInteger("pipeLength");
            return;
        }

        this.currentDiameter = MathHelper.clamp(data.getInteger("currentDiameter"), 1, getMaximumDiameter());
        this.workingEnabled = !data.getBoolean("disabled");
        this.done = data.getBoolean("done");

        this.pipeLength = Math.max(0, data.getInteger("pipeLength"));

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
        buf.writeBoolean(this.done);
        buf.writeBoolean(this.active);
        writePreviewUpdatePacket(buf);
    }

    /**
     * Read states from packet buffer. Call this method in {@link MetaTileEntity#receiveInitialSyncData(PacketBuffer)}.
     */
    public void receiveInitialSyncData(@Nonnull PacketBuffer buf) {
        this.pipeLength = buf.readVarInt();
        this.workingEnabled = buf.readBoolean();
        this.done = buf.readBoolean();
        this.active = buf.readBoolean();
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
            case GregtechDataCodes.WORKING_ENABLED -> {
                this.workingEnabled = buf.readBoolean();
                this.mte.scheduleRenderUpdate();
            }
            case GregtechDataCodes.MINER_UPDATE_PREVIEW -> {
                readPreviewUpdatePacket(buf);
            }
            case GregtechDataCodes.MINER_UPDATE_ACTIVE -> {
                this.active = buf.readBoolean();
                this.mte.scheduleRenderUpdate();
            }
        }
    }
}
