package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.multiblock.RecipeMapSteamMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.RecipeRunner;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

import org.jetbrains.annotations.NotNull;

public class SteamMultiblockRecipeLogic extends DistributedRecipeLogic {

    private IMultipleTankHandler steamFluidTank;
    private IFluidTank steamFluidTankCombined;

    // EU per mB
    private final double conversionRate;

    public SteamMultiblockRecipeLogic(RecipeMapSteamMultiblockController tileEntity, RecipeMap<?> recipeMap,
                                      IMultipleTankHandler steamFluidTank, double conversionRate) {
        super(tileEntity, recipeMap, false);
        this.steamFluidTank = steamFluidTank;
        this.conversionRate = conversionRate;
        combineSteamTanks();
    }

    @Override
    public @NotNull RecipeMapSteamMultiblockController getMetaTileEntity() {
        return (RecipeMapSteamMultiblockController) metaTileEntity;
    }

    public IFluidTank getSteamFluidTankCombined() {
        combineSteamTanks();
        return steamFluidTankCombined;
    }

    protected IMultipleTankHandler getSteamFluidTank() {
        return getMetaTileEntity().getSteamFluidTank();
    }

    private void combineSteamTanks() {
        steamFluidTank = getSteamFluidTank();
        if (steamFluidTank == null)
            steamFluidTankCombined = new FluidTank(0);
        else {
            int capacity = steamFluidTank.getTanks() * 64000;
            steamFluidTankCombined = new FluidTank(capacity);
            steamFluidTankCombined.fill(steamFluidTank.drain(capacity, false), true);
        }
    }

    @Override
    public void update() {
        // Fixes an annoying GTCE bug in AbstractRecipeLogic
        RecipeMapSteamMultiblockController controller = (RecipeMapSteamMultiblockController) metaTileEntity;
        if (isActive && !controller.isStructureFormed()) {
            progressTime = 0;
            wasActiveAndNeedsUpdate = true;
        }

        combineSteamTanks();
        super.update();
    }

    protected long getEnergyStored() {
        combineSteamTanks();
        return (long) Math.ceil(steamFluidTankCombined.getFluidAmount() * conversionRate);
    }

    protected long getEnergyCapacity() {
        combineSteamTanks();
        return (long) Math.floor(steamFluidTankCombined.getCapacity() * conversionRate);
    }

    @Override
    protected boolean drawEnergy(long recipeEUt, boolean simulate) {
        combineSteamTanks();
        int resultDraw = GTUtility.safeCastLongToInt((long) Math.ceil(recipeEUt / conversionRate));
        return resultDraw >= 0 && steamFluidTankCombined.getFluidAmount() >= resultDraw &&
                steamFluidTank.drain(resultDraw, !simulate) != null;
    }

    @Override
    protected boolean produceEnergy(long eu, boolean simulate) {
        return true;
    }

    @Override
    public long getMaxVoltageIn() {
        return GTValues.V[GTValues.LV];
    }

    @Override
    public long getMaxVoltageOut() {
        return 0;
    }

    @Override
    public long getMaxAmperageIn() {
        return 2;
    }

    @Override
    public long getMaxAmperageOut() {
        return 0;
    }

    @Override
    public long getMaxOverclockVoltage(boolean generatingRecipe) {
        return 0;
    }

    @Override
    protected boolean canSubtick() {
        return false;
    }

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe) {
        RecipeMapSteamMultiblockController controller = getMetaTileEntity();
        if (controller.checkRecipe(recipe, false)) {
            controller.checkRecipe(recipe, true);
            return super.checkRecipe(recipe);
        }
        return false;
    }

    @Override
    protected void attemptRecipeCompletion(RecipeRunner runner) {
        super.attemptRecipeCompletion(runner);
        ventSteam();
    }

    private void ventSteam() {
        BlockPos machinePos = metaTileEntity.getPos();
        EnumFacing ventingSide = metaTileEntity.getFrontFacing();
        BlockPos ventingBlockPos = machinePos.offset(ventingSide);
        IBlockState blockOnPos = metaTileEntity.getWorld().getBlockState(ventingBlockPos);
        if (blockOnPos.getCollisionBoundingBox(metaTileEntity.getWorld(), ventingBlockPos) == Block.NULL_AABB) {
            performVentingAnimation(machinePos, ventingSide);
        } else if (blockOnPos.getBlock() == Blocks.SNOW_LAYER && blockOnPos.getValue(BlockSnow.LAYERS) == 1) {
            performVentingAnimation(machinePos, ventingSide);
            metaTileEntity.getWorld().destroyBlock(ventingBlockPos, false);
        }
    }

    private void performVentingAnimation(BlockPos machinePos, EnumFacing ventingSide) {
        WorldServer world = (WorldServer) metaTileEntity.getWorld();
        double posX = machinePos.getX() + 0.5 + ventingSide.getXOffset() * 0.6;
        double posY = machinePos.getY() + 0.5 + ventingSide.getYOffset() * 0.6;
        double posZ = machinePos.getZ() + 0.5 + ventingSide.getZOffset() * 0.6;

        world.spawnParticle(EnumParticleTypes.CLOUD, posX, posY, posZ,
                7 + GTValues.RNG.nextInt(3),
                ventingSide.getXOffset() / 2.0,
                ventingSide.getYOffset() / 2.0,
                ventingSide.getZOffset() / 2.0, 0.1);
        if (ConfigHolder.machines.machineSounds && !metaTileEntity.isMuffled()) {
            world.playSound(null, posX, posY, posZ, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0f,
                    1.0f);
        }
    }
}
