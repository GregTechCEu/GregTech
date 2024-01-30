package gregtech.common.metatileentities.miner;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.BlockUtility;
import gregtech.common.entities.MiningPipeEntity;
import gregtech.common.metatileentities.miner.Miner.MinedBlockType;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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
    private int workDelay;
    // flag indicating mining area should be rebuilt
    protected boolean rebuildMiningArea;
    // flag for area preview
    private boolean preview;
    private int prevPipeLength;

    private boolean hasNotEnoughEnergy;

    // remote instances only, contains MiningArea instances deserialized from packet
    @Nullable
    private MiningArea previewArea;

    private final List<MiningPipeEntity<MTE>> pipeEntities = new ArrayList<>();

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
    public MinerLogic(@NotNull MTE mte, int workFrequency, int maximumDiameter) {
        if (workFrequency <= 0) throw new IllegalArgumentException("workFrequency <= 0");
        this.mte = Objects.requireNonNull(mte, "mte == null");
        this.workDelay = this.workFrequency = workFrequency;
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

    public int getPipeLength() {
        return this.pipeLength;
    }

    @Nullable
    public MiningArea getPreviewArea() {
        return this.previewArea;
    }

    public double getWorkProgress() {
        if (!isWorking()) return 0;
        if (getWorkFrequency() == 2) return 1;
        return 1 - (double) workDelay / getWorkFrequency();
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

    public boolean isDone() {
        return done;
    }

    /**
     * @return origin position of the miner. Block boundary will be centered around this position, and mining pipes will
     *         be rendered under this position.
     */
    @NotNull
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
    @NotNull
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
        this.done = false;
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
        if (!this.mte.getWorld().isRemote) {
            // rebuild scan area every tick regardless of miner status, for accurate preview
            if (this.rebuildMiningArea || this.miningArea == null) {
                reset();
            }

            updateLogic();
        }

        if (this.prevPipeLength != this.pipeLength) {
            this.prevPipeLength = this.pipeLength;
            int pipeIndex = 0;
            int y = this.getOrigin().getY();
            int yEnd = y - this.pipeLength;
            MiningPipeEntity<MTE> entity = null;

            while (y > yEnd) { // divide segments every 16 blocks, aligned with Y position
                int length = y % 16;
                if (length == 0) length = 16;
                length = Math.min(length, y - yEnd);

                entity = this.pipeEntities.size() > pipeIndex ? this.pipeEntities.get(pipeIndex) : null;

                if (entity == null || !entity.isEntityAlive()) {
                    entity = new MiningPipeEntity<>(this.mte, this.getOrigin());
                    if (pipeIndex < this.pipeEntities.size()) this.pipeEntities.set(pipeIndex, entity);
                    else this.pipeEntities.add(entity);
                    this.mte.getWorld().spawnEntity(entity);
                }

                entity.y = y;
                entity.length = length;
                entity.end = false;
                y -= length;
                pipeIndex++;
            }

            if (entity != null) entity.end = true;

            for (int i = this.pipeEntities.size() - 1; i >= pipeIndex; i--) {
                this.pipeEntities.remove(i).setDead();
            }
        }
    }

    protected void updateLogic() {
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

    protected void mine(@NotNull MiningArea miningArea) {
        if (this.done || --this.workDelay > 0) return;
        this.workDelay = this.workFrequency;
        if (!this.workingEnabled || !this.mte.canOperate()) return;

        World world = mte.getWorld();
        BlockPos origin = getOrigin();
        MutableBlockPos pos = this.mpos;

        for (int i = MinerUtil.MAX_BLOCK_SCAN; i > 0; i--) {
            if (!miningArea.getCurrentBlockPos(pos) || mte.getWorld().isOutsideBuildHeight(pos)) {
                this.done = true;
                return;
            }
            IBlockState state = world.getBlockState(pos);
            boolean isOrigin = pos.getX() == origin.getX() && pos.getZ() == origin.getZ();

            // skip air, unbreakable blocks, TE blocks & liquids
            if (state.getMaterial() == Material.AIR ||
                    state.getBlockHardness(world, pos) < 0 ||
                    state.getBlock().hasTileEntity(state) ||
                    state.getMaterial().isLiquid()) {
                miningArea.nextBlock();
                if (!isOrigin) {
                    continue;
                }
                // center block (where mining pipes goes in) can be skipped by this, it'll probably look kind of janky
                // but it's 100x better than voiding bedrock
                if (!this.mte.drainMiningResources(MinedBlockType.NOTHING, true, false)) {
                    return;
                }
                setPipeLength(this.pipeLength + 1);
                this.workDelay /= 2;
                return;
            }

            boolean isOre = BlockUtility.isOre(state);
            if (!isOrigin && !isOre) {
                miningArea.nextBlock();
                continue;
            }

            if (!this.mte.drainMiningResources(isOre ? MinedBlockType.ORE : MinedBlockType.BLOCK, true, false)) {
                return;
            }

            if (isOre && !this.mte.collectBlockDrops(world, pos, state)) {
                return;
            }
            world.setBlockState(pos, isOrigin ? Blocks.AIR.getDefaultState() : getOreReplacement());
            if (isOrigin) setPipeLength(this.pipeLength + 1);
            miningArea.nextBlock();
            this.mte.onMineOperation(pos, isOre, isOrigin);
            this.mte.markDirty();
            return;

        }
        this.workDelay = 1; // re-scan next tick
    }

    @NotNull
    protected IBlockState getOreReplacement() {
        return MinerUtil.getOreReplacement();
    }

    private void setPipeLength(int length) {
        if (this.pipeLength == length) return;
        this.pipeLength = length;
        this.mte.markDirty();
        this.mte.writeCustomData(GregtechDataCodes.PUMP_HEAD_LEVEL, b -> b.writeVarInt(length));
    }

    protected void writePreviewUpdatePacket(@NotNull PacketBuffer buffer) {
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

    protected void readPreviewUpdatePacket(@NotNull PacketBuffer buffer) {
        this.previewArea = buffer.readBoolean() ? readPreviewArea(buffer) : null;
    }

    @NotNull
    protected MiningArea readPreviewArea(@NotNull PacketBuffer buffer) {
        return SimpleMiningArea.readPreview(buffer);
    }

    /**
     * Write states to NBT. Call this method in {@link MetaTileEntity#writeToNBT(NBTTagCompound)}.
     */
    @NotNull
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound data) {
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
    public void readFromNBT(@NotNull NBTTagCompound data) {
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
        // noinspection ConstantValue
        if (this.miningArea != null) {
            this.miningArea.read(data);
        }
    }

    /**
     * Write states to packet buffer. Call this method in {@link MetaTileEntity#writeInitialSyncData(PacketBuffer)}.
     */
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        buf.writeVarInt(this.pipeLength);
        buf.writeBoolean(this.workingEnabled);
        buf.writeBoolean(this.done);
        buf.writeBoolean(this.active);
        writePreviewUpdatePacket(buf);
    }

    /**
     * Read states from packet buffer. Call this method in {@link MetaTileEntity#receiveInitialSyncData(PacketBuffer)}.
     */
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        this.pipeLength = buf.readVarInt();
        this.workingEnabled = buf.readBoolean();
        this.done = buf.readBoolean();
        this.active = buf.readBoolean();
        readPreviewUpdatePacket(buf);
    }

    /**
     * Callback for handling custom data packet sent by miner logic. Call this method in
     * {@link MetaTileEntity#receiveCustomData(int, PacketBuffer)}.
     */
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        if (dataId == GregtechDataCodes.PUMP_HEAD_LEVEL) {
            this.pipeLength = buf.readVarInt();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.workingEnabled = buf.readBoolean();
            this.mte.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.MINER_UPDATE_PREVIEW) {
            readPreviewUpdatePacket(buf);
        } else if (dataId == GregtechDataCodes.MINER_UPDATE_ACTIVE) {
            this.active = buf.readBoolean();
            this.mte.scheduleRenderUpdate();
        }
    }
}
