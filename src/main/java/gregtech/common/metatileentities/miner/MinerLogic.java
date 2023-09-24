package gregtech.common.metatileentities.miner;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.BlockUtility;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class MinerLogic<MTE extends MetaTileEntity & IMiner> {

    /**
     * Maximum amount of blocks to scan in one tick
     */
    private static final int MAX_BLOCK_SCAN = 100;

    private static final Cuboid6 PIPE_CUBOID = new Cuboid6(4 / 16.0, 0.0, 4 / 16.0, 12 / 16.0, 1.0, 12 / 16.0);

    private static String oreReplacementConfigCache;
    private static IBlockState oreReplacement;

    @Nonnull
    @SuppressWarnings("deprecation")
    public static IBlockState getOreReplacement() {
        String config = ConfigHolder.machines.replaceMinedBlocksWith;
        if (Objects.equals(oreReplacementConfigCache, config)) {
            return oreReplacement;
        }

        oreReplacementConfigCache = config;

        String[] blockDescription = StringUtils.split(config, ":");
        String blockName = blockDescription.length <= 2 ? config : blockDescription[0] + ":" + blockDescription[1];
        Block block = Block.getBlockFromName(blockName);

        if (block == null) {
            GTLog.logger.error("Invalid configuration on entry 'machines/replaceMinedBlocksWith': Cannot find block with name '{}', using cobblestone as fallback.", blockName);
            return oreReplacement = Blocks.COBBLESTONE.getDefaultState();
        } else if (blockDescription.length <= 2 || blockDescription[2].isEmpty()) {
            return oreReplacement = block.getDefaultState();
        } else {
            try {
                return oreReplacement = block.getDefaultState().getBlock().getStateFromMeta(Integer.parseInt(blockDescription[2]));
            } catch (NumberFormatException ex) {
                GTLog.logger.error("Invalid configuration on entry 'machines/replaceMinedBlocksWith': Cannot parse metadata value '{}' as integer, using cobblestone as fallback.", blockDescription[2]);
                return oreReplacement = Blocks.COBBLESTONE.getDefaultState();
            }
        }
    }

    protected final MTE mte;

    private final int workFrequency;
    private final int maximumDiameter;

    private final MutableBlockPos mpos = new MutableBlockPos();

    protected int currentDiameter;

    private boolean done;
    private boolean workingEnabled = true;

    protected long nextBlock;

    // last mined ore block

    protected final MutableBlockPos lastMinedOre = new MutableBlockPos();
    protected boolean hasLastMinedOre;
    protected int minedOreCount;

    private int pipeLength;

    // transient values below (not saved)

    // flag indicating last insertion to inventory failed
    private boolean inventoryFull;
    // flag indicating scan area should be rebuilt
    protected boolean rebuildScanArea;
    // status of the last update; true means miner is working, false means no
    private boolean active;

    // scan area; essentially bottomless AABB.

    protected int startX;
    protected int startY;
    protected int startZ;
    protected int endX;
    protected int endZ;

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
        this.nextBlock = 0;
        this.hasLastMinedOre = false;
        this.pipeLength = 0;

        initBoundary();

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
     * Initialize block boundary for mining, i.e. {@link #startX}, {@link #startY}, {@link #startZ}, {@link #endX} and
     * {@link #endZ}
     */
    protected void initBoundary() {
        BlockPos origin = getOrigin();
        int radius = this.currentDiameter / 2;
        this.startX = origin.getX() - radius;
        this.startY = origin.getY();
        this.startZ = origin.getZ() - radius;
        this.endX = origin.getX() + radius;
        this.endZ = origin.getZ() + radius;
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
        if (!this.workingEnabled || this.done || !canOperate()) {
            return false;
        }

        if (this.rebuildScanArea) {
            this.rebuildScanArea = false;
            reset();
        }

        World world = mte.getWorld();
        for (int i = MAX_BLOCK_SCAN; i > 0; i--) {
            BlockPos pos = getBlockPosAt(this.nextBlock);
            if (pos == null || !world.isValid(pos)) {
                this.done = true;
                return false;
            }

            IBlockState state = world.getBlockState(pos);
            boolean isOrigin = alignsWithOrigin(pos);

            // skip unbreakable block / TE blocks
            if (state.getBlockHardness(world, pos) < 0 || state.getBlock().hasTileEntity(state)) {
                // center block (where mining pipes goes in) can be skipped by this, it'll probably look kind of janky
                // but it's 100x better than voiding bedrock
                if (isOrigin) incrementPipeLength();
                this.nextBlock++;
                continue;
            }

            boolean isOre = BlockUtility.isOre(state);
            if (!isOrigin && !isOre) {
                this.nextBlock++;
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
            world.setBlockState(pos, isOrigin ? Blocks.AIR.getDefaultState() : getOreReplacement());

            if (isOrigin) incrementPipeLength();

            this.nextBlock++;

            onMineOperation(pos, isOre, isOrigin);
            this.mte.markDirty();
            return true;
        }
        return true;
    }

    @Nullable
    protected final BlockPos getBlockPosAt(long index) {
        if (index < 0) return null;
        int sizeX = this.endX - this.startX;
        int sizeZ = this.endZ - this.startZ;
        if (sizeX <= 0 || sizeZ <= 0) return null;

        int x = this.startX + (int) (index % sizeX);
        index /= sizeX;
        int z = this.startZ + (int) (index % sizeZ);
        int y = this.startY - (int) (index / sizeZ);
        return this.mpos.setPos(x, y, z);
    }

    private boolean alignsWithOrigin(@Nonnull BlockPos pos) {
        BlockPos origin = getOrigin();
        return pos.getX() == origin.getX() && pos.getZ() == origin.getZ();
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
        this.mte.writeCustomData(GregtechDataCodes.PUMP_HEAD_LEVEL, b -> b.writeVarInt(pipeLength));
    }

    private void resetPipeLength() {
        if (this.pipeLength == 0) return;
        this.pipeLength = 0;
        this.mte.writeCustomData(GregtechDataCodes.PUMP_HEAD_LEVEL, b -> b.writeVarInt(pipeLength));
    }

    /**
     * renders the pipe beneath the miner
     */
    @SideOnly(Side.CLIENT)
    public void renderPipe(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.PIPE_IN_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
        for (int i = 0; i < this.pipeLength; i++) {
            translation.translate(0.0, -1.0, 0.0);
            this.mte.getPipeTexture().render(renderState, translation, pipeline, PIPE_CUBOID);
        }
    }

    /**
     * Write states to NBT. Call this method in {@link MetaTileEntity#writeToNBT(NBTTagCompound)}.
     */
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound data) {
        data.setInteger("currentDiameter", this.currentDiameter);
        data.setBoolean("done", this.done);
        data.setBoolean("workingEnabled", this.workingEnabled);
        data.setLong("nextBlock", this.nextBlock);

        if (this.hasLastMinedOre) {
            data.setInteger("lastMinedOreX", this.lastMinedOre.getX());
            data.setInteger("lastMinedOreY", this.lastMinedOre.getY());
            data.setInteger("lastMinedOreZ", this.lastMinedOre.getZ());
        }

        data.setInteger("minedOreCount", this.minedOreCount);
        data.setInteger("pipeLength", this.pipeLength);

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
            this.nextBlock = 0;

            this.hasLastMinedOre = false;
            this.minedOreCount = 0;
            this.pipeLength = data.getInteger("pipeLength");
            return;
        }

        this.currentDiameter = MathHelper.clamp(data.getInteger("currentDiameter"), 1, getMaximumDiameter());
        this.done = data.getBoolean("done");
        this.workingEnabled = data.getBoolean("workingEnabled");
        this.nextBlock = data.getLong("nextBlock");

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
    }

    /**
     * Write states to packet buffer. Call this method in {@link MetaTileEntity#writeInitialSyncData(PacketBuffer)}.
     */
    public void writeInitialSyncData(@Nonnull PacketBuffer buf) {
        buf.writeVarInt(this.pipeLength);
        buf.writeBoolean(this.workingEnabled);
    }

    /**
     * Read states from packet buffer. Call this method in {@link MetaTileEntity#receiveInitialSyncData(PacketBuffer)}.
     */
    public void receiveInitialSyncData(@Nonnull PacketBuffer buf) {
        this.pipeLength = buf.readVarInt();
        this.workingEnabled = buf.readBoolean();
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
        }
    }

    public void addMinerArea(@Nonnull List<ITextComponent> textList) {
        int diameter = getCurrentDiameter();
        // TODO
        textList.add(new TextComponentTranslation("gregtech.machine.miner.working_area", diameter, diameter));
    }

    public void addMinerWorkStatus(@Nonnull List<ITextComponent> textList) {
        if (isDone()) {
            textList.add(new TextComponentTranslation("gregtech.machine.miner.done")
                    .setStyle(new Style().setColor(TextFormatting.GREEN)));
        } else if (isWorking()) {
            textList.add(new TextComponentTranslation("gregtech.machine.miner.working")
                    .setStyle(new Style().setColor(TextFormatting.GOLD)));
        } else if (!isWorkingEnabled()) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.work_paused"));
        }
    }

    public void addInventoryStatus(@Nonnull List<ITextComponent> textList) {
        if (this.inventoryFull) {
            textList.add(new TextComponentTranslation("gregtech.machine.miner.invfull")
                    .setStyle(new Style().setColor(TextFormatting.RED)));
        }
    }

    public void addLastMinedBlock(@Nonnull List<ITextComponent> textList) {
        if (!this.hasLastMinedOre) return;
        textList.add(new TextComponentTranslation("gregtech.machine.miner.minex", this.lastMinedOre.getX()));
        textList.add(new TextComponentTranslation("gregtech.machine.miner.miney", this.lastMinedOre.getY()));
        textList.add(new TextComponentTranslation("gregtech.machine.miner.minez", this.lastMinedOre.getZ()));
    }
}
