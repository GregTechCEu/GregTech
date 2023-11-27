package gregtech.common.metatileentities.multi.steam;

import gregtech.api.capability.impl.SteamMultiWorkable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapSteamMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockFireboxCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntitySteamOven extends RecipeMapSteamMultiblockController {

    private static final int MAX_PARALLELS = 8;

    public MetaTileEntitySteamOven(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.FURNACE_RECIPES, CONVERSION_RATE);
        this.recipeMapWorkable = new SteamMultiWorkable(this, CONVERSION_RATE);
        this.recipeMapWorkable.setParallelLimit(MAX_PARALLELS);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySteamOven(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "CCC", "#C#")
                .aisle("XXX", "C#C", "#C#")
                .aisle("XXX", "CSC", "#C#")
                .where('S', selfPredicate())
                .where('X', states(getFireboxState())
                        .or(autoAbilities(true, false, false, false, false).setMinGlobalLimited(1)
                                .setMaxGlobalLimited(3)))
                .where('C', states(getCasingState()).setMinGlobalLimited(6)
                        .or(autoAbilities(false, false, true, true, false)))
                .where('#', any())
                .build();
    }

    public IBlockState getCasingState() {
        return ConfigHolder.machines.steelSteamMultiblocks ?
                MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID) :
                MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.BRONZE_BRICKS);
    }

    public IBlockState getFireboxState() {
        return ConfigHolder.machines.steelSteamMultiblocks ?
                MetaBlocks.BOILER_FIREBOX_CASING.getState(BlockFireboxCasing.FireboxCasingType.STEEL_FIREBOX) :
                MetaBlocks.BOILER_FIREBOX_CASING.getState(BlockFireboxCasing.FireboxCasingType.BRONZE_FIREBOX);
    }

    private boolean isFireboxPart(IMultiblockPart sourcePart) {
        return isStructureFormed() && (((MetaTileEntity) sourcePart).getPos().getY() < getPos().getY());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (ConfigHolder.machines.steelSteamMultiblocks) {
            if (sourcePart != null && isFireboxPart(sourcePart)) {
                return lastActive ? Textures.STEEL_FIREBOX_ACTIVE : Textures.STEEL_FIREBOX;
            }
            return Textures.SOLID_STEEL_CASING;

        } else {
            if (sourcePart != null && isFireboxPart(sourcePart)) {
                return lastActive ? Textures.BRONZE_FIREBOX_ACTIVE : Textures.BRONZE_FIREBOX;
            }
            return Textures.BRONZE_PLATED_BRICKS;
        }
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.ELECTRIC_FURNACE_OVERLAY;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public int getItemOutputLimit() {
        return 1;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.multiblock.steam_.duration_modifier"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.parallel", MAX_PARALLELS));
        tooltip.add(TooltipHelper.BLINKING_ORANGE + I18n.format("gregtech.multiblock.require_steam_parts"));
    }
}
