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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class MultiblockMinerLogic extends MinerLogic<MetaTileEntityLargeMiner> {

    private final int fortune;
    private final RecipeMap<?> blockDropRecipeMap;
    private final int maximumChunkDiameter;

    private final BlockPos.MutableBlockPos mpos2 = new BlockPos.MutableBlockPos();

    private int currentChunkDiameter;

    private boolean chunkMode;
    private boolean silkTouchMode;

    public MultiblockMinerLogic(MetaTileEntityLargeMiner largeMiner, int fortune, int workFrequency, int maximumChunkDiameter,
                                RecipeMap<?> blockDropRecipeMap) {
        super(largeMiner, workFrequency, maximumChunkDiameter * 16 / 2);
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

    @Override
    protected void initBoundary() {
        if (!this.chunkMode) {
            super.initBoundary();
            return;
        }

        BlockPos origin = getOrigin();
        int originChunkX = origin.getX() / 16;
        int originChunkZ = origin.getZ() / 16;

        this.startX = (originChunkX - currentChunkDiameter) * 16;
        this.startY = origin.getY();
        this.startZ = (originChunkZ - currentChunkDiameter) * 16;
        this.endX = (originChunkX + currentChunkDiameter) * 16 + 15;
        this.endZ = (originChunkZ + currentChunkDiameter) * 16 + 15;
    }

    @Nonnull
    @Override
    protected BlockPos getOrigin() {
        return this.mpos2.setPos(this.mte.getPos()).move(this.mte.getFrontFacing().getOpposite());
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

    @Override
    public void addMinerArea(@Nonnull List<ITextComponent> textList) {
        if (isChunkMode()) {
            int chunkDiameter = getCurrentChunkDiameter();
            textList.add(new TextComponentTranslation("gregtech.machine.miner.working_area_chunks", chunkDiameter, chunkDiameter));
        } else {
            int diameter = getCurrentDiameter();
            textList.add(new TextComponentTranslation("gregtech.machine.miner.working_area", diameter, diameter));
        }
    }

    /**
     * Applies a fortune hammer to block drops based on a tier value, intended for small ores
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
