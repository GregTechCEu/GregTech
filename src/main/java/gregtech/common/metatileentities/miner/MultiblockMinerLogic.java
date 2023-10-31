package gregtech.common.metatileentities.miner;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public class MultiblockMinerLogic extends MinerLogic<MetaTileEntityLargeMiner> {

    private final int maximumChunkDiameter;

    private final MutableBlockPos mpos = new MutableBlockPos();
    private final MutableBlockPos mpos2 = new MutableBlockPos();

    private int currentChunkDiameter;

    private boolean chunkMode;
    private boolean silkTouchMode;

    // non-negative value to limit Y level
    private int yLimit;

    // bool config for repeating the operation after finished
    private boolean repeat;

    // flag for disabling ore replacement (if true, ores will be replaced with air instead of whatever block that was specified in the config)
    private boolean disableReplacement;

    public MultiblockMinerLogic(@Nonnull MetaTileEntityLargeMiner largeMiner, int workFrequency, int maximumChunkDiameter) {
        super(largeMiner, workFrequency, maximumChunkDiameter * 16);
        this.currentChunkDiameter = this.maximumChunkDiameter = maximumChunkDiameter;
    }

    @Override
    protected void mine(@NotNull MiningArea miningArea) {
        if (this.done && this.repeat) {
            miningArea.reset();
            this.done = false;
        }
        super.mine(miningArea);
    }

    @NotNull
    @Override
    protected IBlockState getOreReplacement() {
        return this.disableReplacement ? Blocks.AIR.getDefaultState() : super.getOreReplacement();
    }

    @Nonnull
    @Override
    protected MiningArea createMiningArea() {
        BlockPos origin = getOrigin();
        if (this.chunkMode) {
            int chunkRadius = this.currentChunkDiameter / 2;
            int originChunkX = origin.getX() / 16 - chunkRadius;
            int originChunkZ = origin.getZ() / 16 - chunkRadius;
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
        if (this.currentChunkDiameter != currentChunkDiameter || !this.chunkMode) {
            this.chunkMode = true;
            this.currentChunkDiameter = currentChunkDiameter;
            this.rebuildMiningArea = false;
            this.mte.markDirty();
        }
    }

    @Override
    public void setCurrentDiameter(int currentDiameter) {
        if (isWorking()) return;
        if (this.chunkMode) {
            this.chunkMode = false;
            this.rebuildMiningArea = true;
            this.mte.markDirty();
        }
        super.setCurrentDiameter(currentDiameter);
    }

    public boolean isChunkMode() {
        return this.chunkMode;
    }

    public void setChunkMode(boolean isChunkMode) {
        if (isWorking()) return;
        this.chunkMode = isChunkMode;
        this.rebuildMiningArea = true;
        this.mte.markDirty();
    }

    public boolean isSilkTouchMode() {
        return this.silkTouchMode;
    }

    public void setSilkTouchMode(boolean isSilkTouchMode) {
        if (!isWorking()) {
            this.silkTouchMode = isSilkTouchMode;
        }
    }

    public int getYLimit() {
        return yLimit;
    }

    public void setYLimit(int yLimit) {
        if (yLimit != this.yLimit) {
            this.yLimit = yLimit;
            this.rebuildMiningArea = true;
            this.mte.markDirty();
            if (this.isPreviewEnabled()) {
                updatePreview();
            }
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

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound data) {
        if (this.chunkMode) data.setBoolean("chunkMode", true);
        if (this.silkTouchMode) data.setBoolean("silkTouch", true);
        data.setInteger("currentChunkDiameter", currentChunkDiameter);
        if (this.yLimit > 0) data.setInteger("yLimit", this.yLimit);
        if (this.repeat) data.setBoolean("repeat", true);
        if (this.disableReplacement) data.setBoolean("disableReplacement", true);
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
        this.disableReplacement = data.getBoolean("disableReplacement");
    }

    public void addDisplayText(@Nonnull List<ITextComponent> textList) {
        if (isChunkMode()) {
            int chunkDiameter = getCurrentChunkDiameter();
            textList.add(new TextComponentTranslation("gregtech.machine.miner.display.working_area.chunks",
                    chunkDiameter, chunkDiameter, previewWorkingAreaButton()));
        } else {
            int diameter = getCurrentDiameter();
            textList.add(new TextComponentTranslation("gregtech.machine.miner.display.working_area",
                    diameter, diameter, previewWorkingAreaButton()));
        }

        Object value;
        ITextComponent hoverText;
        if (this.getYLimit() > 0) {
            value = String.format("%,d", this.getYLimit());
            hoverText = new TextComponentTranslation("gregtech.machine.miner.display.y_limit.value_hover_tooltip", value);
        } else {
            value = new TextComponentTranslation("gregtech.machine.miner.display.y_limit.no_value");
            hoverText = new TextComponentTranslation("gregtech.machine.miner.display.y_limit.value_hover_tooltip.no_value");
        }
        textList.add(new TextComponentTranslation("gregtech.machine.miner.display.y_limit",
                yLimitButton(false), yLimitButton(true), value)
                .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))));

        textList.add(new TextComponentTranslation("gregtech.machine.miner.display.repeat",
                new TextComponentTranslation(this.isRepeat() ? "gregtech.machine.miner.display.repeat.enabled" :
                        "gregtech.machine.miner.display.repeat.disabled")
                        .setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                                this.isRepeat() ? "@!" + MinerUtil.DISPLAY_CLICK_REPEAT_DISABLE :
                                        "@!" + MinerUtil.DISPLAY_CLICK_REPEAT_ENABLE)))));

        if (this.getMiningArea() == null || !getCurrentBlock(this.getMiningArea(), this.mpos2)) {
            textList.add(new TextComponentTranslation("gregtech.machine.miner.display.done")
                    .setStyle(new Style().setColor(TextFormatting.GREEN)));
        } else if (isWorking()) {
            textList.add(new TextComponentTranslation("gregtech.machine.miner.display.working",
                    this.mpos2.getX(), this.mpos2.getY(), this.mpos2.getZ())
                    .setStyle(new Style().setColor(TextFormatting.GOLD)));
        } else if (!isWorkingEnabled()) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.work_paused"));
        }

        textList.add(new TextComponentTranslation("gregtech.machine.miner.display.stats.total_mined", this.minedOreCount));
        if (this.hasLastMinedOre) {
            textList.add(new TextComponentTranslation("gregtech.machine.miner.display.stats.last_mined",
                    this.lastMinedOre.getX(), this.lastMinedOre.getY(), this.lastMinedOre.getZ()));
        }
    }

    @Nonnull
    protected ITextComponent previewWorkingAreaButton() {
        return new TextComponentTranslation(this.isPreviewEnabled() ?
                "gregtech.machine.miner.display.working_area.hide_preview" :
                "gregtech.machine.miner.display.working_area.preview")
                .setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                        this.isPreviewEnabled() ? "@!" + MinerUtil.DISPLAY_CLICK_AREA_PREVIEW_HIDE :
                                "@!" + MinerUtil.DISPLAY_CLICK_AREA_PREVIEW)));
    }

    @Nonnull
    protected ITextComponent yLimitButton(boolean incr) {
        if (incr) {
            return this.getYLimit() == Integer.MAX_VALUE ?
                    new TextComponentTranslation("gregtech.machine.miner.display.y_limit.incr.disabled") :
                    new TextComponentTranslation("gregtech.machine.miner.display.y_limit.incr")
                            .setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "@!" + MinerUtil.DISPLAY_CLICK_Y_LIMIT_INCR)));
        } else {
            return this.getYLimit() <= 0 ?
                    new TextComponentTranslation("gregtech.machine.miner.display.y_limit.decr.disabled") :
                    new TextComponentTranslation("gregtech.machine.miner.display.y_limit.decr")
                            .setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "@!" + MinerUtil.DISPLAY_CLICK_Y_LIMIT_DECR)));
        }
    }
}
