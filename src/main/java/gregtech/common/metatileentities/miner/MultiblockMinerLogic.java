package gregtech.common.metatileentities.miner;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class MultiblockMinerLogic extends MinerLogic<MetaTileEntityLargeMiner> {

    private final int maximumChunkDiameter;

    private final MutableBlockPos mpos = new MutableBlockPos();

    private int currentChunkDiameter;

    private boolean chunkMode;
    private boolean silkTouchMode;

    // non-negative value to limit Y level
    private int yLimit;

    // bool config for repeating the operation after finished
    private boolean repeat;

    // flag for disabling ore replacement (if true, ores will be replaced with air instead of whatever block that was specified in the config)
    private boolean replaceOreWithAir;

    public MultiblockMinerLogic(@Nonnull MetaTileEntityLargeMiner largeMiner, int workFrequency, int maximumChunkDiameter) {
        super(largeMiner, workFrequency, maximumChunkDiameter * 16);
        this.currentChunkDiameter = this.maximumChunkDiameter = maximumChunkDiameter;
    }

    @Override
    protected void mine(@Nonnull MiningArea miningArea) {
        if (this.done && this.repeat) {
            miningArea.reset();
            this.done = false;
        }
        super.mine(miningArea);
    }

    @Nonnull
    @Override
    protected IBlockState getOreReplacement() {
        return this.replaceOreWithAir ? Blocks.AIR.getDefaultState() : super.getOreReplacement();
    }

    @Nonnull
    @Override
    protected MiningArea createMiningArea() {
        BlockPos origin = getOrigin();
        if (this.chunkMode) {
            int chunkRadius = this.currentChunkDiameter / 2;
            int originChunkX = (origin.getX() >> 4) - chunkRadius;
            int originChunkZ = (origin.getZ() >> 4) - chunkRadius;
            return new SimpleMiningArea((originChunkX) * 16,
                    origin.getY() - 1,
                    (originChunkZ) * 16,
                    (originChunkX + currentChunkDiameter) * 16,
                    getYLimit() > 0 ? origin.getY() - getYLimit() : Integer.MIN_VALUE,
                    (originChunkZ + currentChunkDiameter) * 16);
        } else {
            int radius = this.currentDiameter / 2;
            int startX = origin.getX() - radius;
            int startY = origin.getY() - 1;
            int startZ = origin.getZ() - radius;
            int endX = startX + this.currentDiameter;
            int endY = getYLimit() > 0 ? origin.getY() - getYLimit() : Integer.MIN_VALUE;
            int endZ = startZ + this.currentDiameter;
            return new SimpleMiningArea(startX, startY, startZ, endX, endY, endZ);
        }
    }

    @Nonnull
    @Override
    protected BlockPos getOrigin() {
        return this.mpos.setPos(this.mte.getPos()).move(this.mte.getFrontFacing().getOpposite());
    }

    public int getMaximumChunkDiameter() {
        return maximumChunkDiameter;
    }

    public int getCurrentChunkDiameter() {
        return currentChunkDiameter;
    }

    public void setCurrentChunkDiameter(int currentChunkDiameter) {
        if (isWorking()) return;
        currentChunkDiameter = Math.max(1, Math.min(currentChunkDiameter, getMaximumChunkDiameter()));
        setChunkMode(true);
        if (this.currentChunkDiameter != currentChunkDiameter) {
            this.currentChunkDiameter = currentChunkDiameter;
            this.rebuildMiningArea = true;
            this.mte.markDirty();
        }
    }

    @Override
    public void setCurrentDiameter(int currentDiameter) {
        if (isWorking()) return;
        setChunkMode(false);
        super.setCurrentDiameter(currentDiameter);
    }

    public boolean isChunkMode() {
        return this.chunkMode;
    }

    public void setChunkMode(boolean chunkMode) {
        if (isWorking() || this.chunkMode == chunkMode) return;
        this.chunkMode = chunkMode;
        this.rebuildMiningArea = true;
        this.mte.markDirty();
    }

    public boolean isSilkTouchMode() {
        return this.silkTouchMode;
    }

    public void setSilkTouchMode(boolean silkTouchMode) {
        if (isWorking() || this.silkTouchMode == silkTouchMode) return;
        this.silkTouchMode = silkTouchMode;
        this.mte.markDirty();
    }

    public int getYLimit() {
        return yLimit;
    }

    public void setYLimit(int yLimit) {
        if (isWorking() || this.yLimit == yLimit) return;
        this.yLimit = yLimit;
        this.rebuildMiningArea = true;
        this.mte.markDirty();
        if (this.isPreviewEnabled()) {
            updatePreview();
        }
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        if (isWorking() || this.repeat == repeat) return;
        this.repeat = repeat;
        this.mte.markDirty();
    }

    public boolean isReplaceOreWithAir() {
        return replaceOreWithAir;
    }

    public void setReplaceOreWithAir(boolean replaceOreWithAir) {
        if (isWorking() || this.replaceOreWithAir == replaceOreWithAir) return;
        this.replaceOreWithAir = replaceOreWithAir;
        this.mte.markDirty();
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound data) {
        if (this.chunkMode) data.setBoolean("chunkMode", true);
        if (this.silkTouchMode) data.setBoolean("silkTouch", true);
        data.setInteger("currentChunkDiameter", currentChunkDiameter);
        if (this.yLimit > 0) data.setInteger("yLimit", this.yLimit);
        if (this.repeat) data.setBoolean("repeat", true);
        if (this.replaceOreWithAir) data.setBoolean("replaceWithAir", true);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound data) {
        super.readFromNBT(data);
        this.chunkMode = data.getBoolean("chunkMode") || data.getBoolean("isChunkMode");
        this.silkTouchMode = data.getBoolean("silkTouch") || data.getBoolean("isSilkTouchMode");
        this.currentChunkDiameter = data.hasKey("currentChunkDiameter", Constants.NBT.TAG_INT) ?
                MathHelper.clamp(data.getInteger("currentChunkDiameter"), 1, getMaximumChunkDiameter()) :
                getMaximumChunkDiameter();
        this.yLimit = Math.max(0, data.getInteger("yLimit"));
        this.repeat = data.getBoolean("repeat");
        this.replaceOreWithAir = data.getBoolean("replaceWithAir");
    }
}
