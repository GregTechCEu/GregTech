package gregtech.api.metatileentity;

import codechicken.lib.raytracer.CuboidRayTraceResult;

import gregtech.api.capability.impl.PseudoMultiSteamRecipeLogic;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.metatileentities.steam.SimpleSteamMetaTileEntity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

public class PseudoMultiSteamMachineMetaTileEntity extends SimpleSteamMetaTileEntity {
    private IBlockState targetBlockState;

    public IBlockState getTargetBlockState() {
        return targetBlockState;
    }
    public PseudoMultiSteamMachineMetaTileEntity(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, SteamProgressIndicator progressIndicator, ICubeRenderer renderer, boolean isBrickedCasing, boolean isHighPressure) {
        super(metaTileEntityId, recipeMap, progressIndicator, renderer, isBrickedCasing, isHighPressure);
        this.workableHandler = new PseudoMultiSteamRecipeLogic(this, recipeMap, isHighPressure, steamFluidTank, 1.0);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new PseudoMultiSteamMachineMetaTileEntity(metaTileEntityId, workableHandler.getRecipeMap(), progressIndicator, renderer, isBrickedCasing, isHighPressure);
    }

    public void checkAdjacentBlocks(){
        if(this.getWorld() == null || this.getWorld().isRemote) {
            targetBlockState = null;
            return;
        }

        //the traditional "back" side of this type of MTE is actually treated as its front for recipe purposes,
        //making wrench movement feel as though you are holding onto or manipulating the back side to point the MTE.
        targetBlockState = this.getWorld().getBlockState(this.getPos().offset(this.getFrontFacing().getOpposite()));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.checkAdjacentBlocks();
    }

    @Override
    public void onPlacement() {
        super.onPlacement();
        this.checkAdjacentBlocks();
    }

    @Override
    public void onNeighborChanged() {
        super.onNeighborChanged();
        this.checkAdjacentBlocks();
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        boolean wrenchClickSucceeded = super.onWrenchClick(playerIn, hand, facing, hitResult);
        if (wrenchClickSucceeded) this.checkAdjacentBlocks();
        return wrenchClickSucceeded;
    }

    @Override
    public boolean needsSneakToRotate() { return true; }
    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }
}
