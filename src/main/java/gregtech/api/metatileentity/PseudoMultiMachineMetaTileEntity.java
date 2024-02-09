package gregtech.api.metatileentity;

import codechicken.lib.raytracer.CuboidRayTraceResult;

import gregtech.api.capability.impl.PseudoMultiRecipeLogic;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public class PseudoMultiMachineMetaTileEntity extends SimpleMachineMetaTileEntity {
    private IBlockState targetBlockState;

    public IBlockState getTargetBlockState() {
        return targetBlockState;
    }
    public PseudoMultiMachineMetaTileEntity(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, boolean hasFrontFacing, Function<Integer, Integer> tankScalingFunction) {
        super(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, tankScalingFunction);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new PseudoMultiMachineMetaTileEntity(this.metaTileEntityId, this.workable.getRecipeMap(), this.renderer, this.getTier(), this.hasFrontFacing(), this.getTankScalingFunction());
    }

    @Override
    protected PseudoMultiRecipeLogic createWorkable(RecipeMap<?> recipeMap) {
        return new PseudoMultiRecipeLogic(this, recipeMap, () -> this.energyContainer);
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
}
