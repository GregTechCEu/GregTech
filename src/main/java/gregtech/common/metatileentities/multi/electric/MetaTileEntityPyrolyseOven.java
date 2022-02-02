package gregtech.common.metatileentities.multi.electric;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMachineCasing.MachineCasingType;
import gregtech.common.blocks.BlockWireCoil.CoilType;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityPyrolyseOven extends RecipeMapMultiblockController {

    private int coilTier;

    public MetaTileEntityPyrolyseOven(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.PYROLYSE_RECIPES);
        this.recipeMapWorkable = new PyrolyseOvenWorkableHandler(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityPyrolyseOven(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("CCC", "C#C", "CCC")
                .aisle("CCC", "C#C", "CCC")
                .aisle("XXX", "XSX", "XXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(6).or(autoAbilities()))
                .where('C', heatingCoils())
                .where('#', air())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.VOLTAGE_CASINGS[0];
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.MACHINE_CASING.getState(MachineCasingType.ULV);
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.PYROLYSE_OVEN_OVERLAY;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        Object type = context.get("CoilType");
        if (type instanceof CoilType)
            this.coilTier = ((CoilType) type).ordinal();
        else
            this.coilTier = 0;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed())
            textList.add(new TextComponentTranslation("gregtech.multiblock.pyrolyse_oven.speed", coilTier == 0 ? 75 : 50 * (coilTier + 1)));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.pyrolyse_oven.tooltip.1"));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.coilTier = -1;
    }

    protected int getCoilTier() {
        return this.coilTier;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    private class PyrolyseOvenWorkableHandler extends MultiblockRecipeLogic {

        public PyrolyseOvenWorkableHandler(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        protected int[] performOverclocking(Recipe recipe) {
            int[] overclock = super.performOverclocking(recipe);

            int coilTier = ((MetaTileEntityPyrolyseOven) metaTileEntity).getCoilTier();
            if (coilTier == -1)
                return overclock;

            if (coilTier == 0) overclock[1] *= 5.0 / 4; // 25% slower with cupronickel (coilTier = 0)
            else overclock[1] *= 2.0f / (coilTier + 1); // each coil above kanthal (coilTier = 1) is 50% faster

            overclock[1] = Math.max(1, overclock[1]);

            return overclock;
        }
    }
}
