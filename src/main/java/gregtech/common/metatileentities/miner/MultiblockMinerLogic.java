package gregtech.common.metatileentities.miner;

import gregtech.api.GTValues;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class MultiblockMinerLogic extends MinerLogic<MetaTileEntityLargeMiner> {

    private final int fortune;
    private final RecipeMap<?> blockDropRecipeMap;
    private final int maximumChunkDiameter;

    private final MutableBlockPos mpos = new MutableBlockPos();
    private final MutableBlockPos mpos2 = new MutableBlockPos();

    private int currentChunkDiameter;

    private boolean chunkMode;
    private boolean silkTouchMode;

    public MultiblockMinerLogic(MetaTileEntityLargeMiner largeMiner, int fortune, int workFrequency, int maximumChunkDiameter,
                                RecipeMap<?> blockDropRecipeMap) {
        super(largeMiner, workFrequency, maximumChunkDiameter * 16);
        this.fortune = fortune;
        this.blockDropRecipeMap = blockDropRecipeMap;
        this.currentChunkDiameter = this.maximumChunkDiameter = maximumChunkDiameter;
    }

    @Override
    protected void getRegularBlockDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        if (this.silkTouchMode) {
            drops.add(ToolHelper.getSilkTouchDrop(state));
        } else if (applyTieredHammerDrops(GTUtility.toItem(state), drops) == 0) { // 3X the ore compared to the single blocks
            super.getRegularBlockDrops(drops, world, pos, state); // fallback
        }
    }

    @Nonnull
    @Override
    protected IMiningArea createMiningArea() {
        if (!this.chunkMode) return super.createMiningArea();
        BlockPos origin = getOrigin();
        int chunkRadius = this.currentChunkDiameter / 2;
        int originChunkX = origin.getX() / 16 - chunkRadius;
        int originChunkZ = origin.getZ() / 16 - chunkRadius;
        return new SimpleMiningArea((originChunkX) * 16,
                origin.getY() - 1,
                (originChunkZ) * 16,
                (originChunkX + currentChunkDiameter) * 16,
                getYLimit() > 0 ? origin.getY() - getYLimit() : Integer.MIN_VALUE,
                (originChunkZ + currentChunkDiameter) * 16);
    }

    @Nonnull
    @Override
    protected BlockPos getOrigin() {
        return this.mpos.setPos(this.mte.getPos()).move(this.mte.getFrontFacing().getOpposite());
    }

    public int getFortune() {
        return fortune;
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
            this.rebuildScanArea = false;
            this.mte.markDirty();
        }
    }

    @Override
    public void setCurrentDiameter(int currentDiameter) {
        if (isWorking()) return;
        if (this.chunkMode) {
            this.chunkMode = false;
            this.rebuildScanArea = true;
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
        this.rebuildScanArea = true;
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

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound data) {
        data.setBoolean("isChunkMode", chunkMode);
        data.setBoolean("isSilkTouchMode", silkTouchMode);
        data.setInteger("currentChunkDiameter", currentChunkDiameter);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound data) {
        this.chunkMode = data.getBoolean("isChunkMode");
        this.silkTouchMode = data.getBoolean("isSilkTouchMode");
        this.currentChunkDiameter = data.hasKey("currentChunkDiameter", Constants.NBT.TAG_INT) ?
                MathHelper.clamp(data.getInteger("currentChunkDiameter"), 1, getMaximumChunkDiameter()) :
                getMaximumChunkDiameter();
        super.readFromNBT(data);
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

        if (isDone()) {
            textList.add(new TextComponentTranslation("gregtech.machine.miner.display.done")
                    .setStyle(new Style().setColor(TextFormatting.GREEN)));
        } else if (isWorking()) {
            if (getCurrentBlock(this.mpos2)) {
                textList.add(new TextComponentTranslation("gregtech.machine.miner.display.working",
                        this.mpos2.getX(), this.mpos2.getY(), this.mpos2.getZ())
                        .setStyle(new Style().setColor(TextFormatting.GOLD)));
            }
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

    /**
     * Applies a fortune hammer to block drops based on a tier value.
     *
     * @param stack the item stack to check for recipes
     * @param drops where the drops are stored to
     * @return amount of items inserted to {@code drops}
     */
    protected int applyTieredHammerDrops(@Nonnull ItemStack stack, @Nonnull List<ItemStack> drops) {
        int energyTier = this.mte.getEnergyTier();
        Recipe recipe = this.blockDropRecipeMap.findRecipe(
                GTValues.V[energyTier],
                Collections.singletonList(stack),
                Collections.emptyList());
        if (recipe == null || recipe.getOutputs().isEmpty()) return 0;
        int c = 0;
        for (ItemStack output : recipe.getResultItemOutputs(GTUtility.getTierByVoltage(recipe.getEUt()), energyTier, this.blockDropRecipeMap)) {
            output = output.copy();
            if (this.fortune > 0 && OreDictUnifier.getPrefix(output) == OrePrefix.crushed) {
                output.grow(output.getCount() * this.fortune);
            }
            drops.add(output);
            c++;
        }
        return c;
    }
}
