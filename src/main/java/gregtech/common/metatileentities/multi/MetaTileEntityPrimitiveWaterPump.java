package gregtech.common.metatileentities.multi;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.BlockSteamCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityFluidHatch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.*;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Predicate;

public class MetaTileEntityPrimitiveWaterPump extends MultiblockControllerBase {

    private IFluidTank waterTank;
    private int biomeModifier = 0;
    private int hatchModifier = 0;

    public MetaTileEntityPrimitiveWaterPump(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        resetTileAbilities();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityPrimitiveWaterPump(metaTileEntityId);
    }

    @Override
    public void update() {
        super.update();
        if (getOffsetTimer() % 20 == 0 && !getWorld().isRemote && isStructureFormed()) {
            if (biomeModifier == 0) {
                biomeModifier = getAmountForBiome(getWorld().getBiome(getPos()));
            }
            waterTank.fill(Materials.Water.getFluid(biomeModifier * hatchModifier), true);
        }
    }

    private static int getAmountForBiome(Biome biome) {
        Class<? extends Biome> biomeClass = biome.getBiomeClass();
        if (biomeClass == BiomeOcean.class || biomeClass == BiomeRiver.class) {
            return 1000;
        } else if (biomeClass == BiomeSwamp.class) {
            return 800;
        } else if (biomeClass == BiomeJungle.class) {
            return 350;
        } else if (biomeClass == BiomeSnow.class) {
            return 300;
        } else if (biomeClass == BiomePlains.class || biomeClass == BiomeForest.class) {
            return 250;
        } else if (biomeClass == BiomeTaiga.class) {
            return 175;
        } else if (biomeClass == BiomeBeach.class) {
            return 170;
        } else {
            return 100;
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    protected void updateFormedValid() {

    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
    }

    private void initializeAbilities() {
        List<IFluidTank> tanks = getAbilities(MultiblockAbility.PUMP_FLUID_HATCH);
        if (tanks == null || tanks.size() == 0) {
            tanks = getAbilities(MultiblockAbility.EXPORT_FLUIDS);
            this.hatchModifier = tanks.get(0).getCapacity() == 8000 ? 2 : 4;
        } else {
            this.hatchModifier = 1;
        }
        this.waterTank = tanks.get(0);
    }

    private void resetTileAbilities() {
        this.waterTank = new FluidTank(0);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXXX", "**F*", "**F*")
                .aisle("XXHX", "F**F", "FFFF")
                .aisle("SXXX", "**F*", "**F*")
                .where('S', selfPredicate())
                .where('X', statePredicate(MetaBlocks.STEAM_CASING.getState(BlockSteamCasing.SteamCasingType.PUMP_DECK)))
                .where('F', statePredicate(MetaBlocks.FRAMES.get(Materials.Wood).getBlockState().getBaseState()))
                .where('H', hatchPredicate())
                .where('*', (x) -> true)
                .build();
    }

    private static Predicate<BlockWorldState> hatchPredicate() {
        return tilePredicate((state, tile) -> {
            if (tile instanceof IMultiblockAbilityPart<?>) {
                IMultiblockAbilityPart<?> abilityPart = (IMultiblockAbilityPart<?>) tile;
                if (abilityPart.getAbility() == MultiblockAbility.PUMP_FLUID_HATCH) return true;
                if (abilityPart.getAbility() == MultiblockAbility.EXPORT_FLUIDS) {
                    return ((MetaTileEntityFluidHatch) tile).getTier() <= 1;
                }
            }
            return false;
        });
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.PRIMITIVE_PUMP;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.PRIMITIVE_PUMP_OVERLAY;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().render(renderState, translation, pipeline, getFrontFacing(), true);
    }
}
