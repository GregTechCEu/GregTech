package gregtech.common.metatileentities.multi.electric;

import static gregtech.api.util.RelativeDirection.*;

import java.util.List;

import gregtech.api.metatileentity.GCYMRecipeMapMultiblockController;

import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockLargeMultiblockCasing;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.MetaBlocks;

public class MetaTileEntityLargeAssembler extends GCYMRecipeMapMultiblockController {

    public MetaTileEntityLargeAssembler(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, determineRecipeMaps());
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityLargeAssembler(this.metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(FRONT, UP, RIGHT)
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "CAX", "CCX").setRepeatable(3)
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "XAX", "#XX")
                .aisle("XXX", "STX", "#XX")
                .aisle("XXX", "XAX", "#XX")
                .aisle("XXX", "XXX", "XXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(40)
                        .or(autoAbilities(false, true, true, true, true, true, true))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setExactLimit(1)))
                .where('C', states(getCasingState2()))
                .where('T', tieredCasing().or(air()))
                .where('A', air())
                .where('#', any())
                .build();
    }

    private static IBlockState getCasingState() {
        return MetaBlocks.LARGE_MULTIBLOCK_CASING.getState(BlockLargeMultiblockCasing.CasingType.ASSEMBLING_CASING);
    }

    private static IBlockState getCasingState2() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.TEMPERED_GLASS);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.ASSEMBLING_CASING;
    }

    @Override
    protected @NotNull OrientedOverlayRenderer getFrontOverlay() {
        return Textures.LARGE_ASSEMBLER_OVERLAY;
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gcym.tooltip.max_energy_hatches", 1));
    }

    private static @NotNull RecipeMap<?> @NotNull [] determineRecipeMaps() {
        RecipeMap<?> cuisineAssemblerMap = RecipeMap.getByName("cuisine_assembler");
        return new RecipeMap<?>[] { RecipeMaps.ASSEMBLER_RECIPES };
    }
}
