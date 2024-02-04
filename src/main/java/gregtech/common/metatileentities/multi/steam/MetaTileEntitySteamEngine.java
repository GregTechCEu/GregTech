package gregtech.common.metatileentities.multi.steam;

import java.util.List;

import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockLargeMultiblockCasing;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.FuelMultiblockController;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;

public class MetaTileEntitySteamEngine extends FuelMultiblockController {

    public MetaTileEntitySteamEngine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.STEAM_TURBINE_FUELS, GTValues.MV);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntitySteamEngine(metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("#XX", "XEX", "#XX")
                .aisle("XXX", "XGX", "XMX")
                .aisle("#XX", "XGX", "#XX")
                .aisle("#XX", "#SX", "#XX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(18)
                        .or(autoAbilities(false, true, true, true, true, true, false)))
                .where('G', states(getCasingState2()))
                .where('E', energyOutputPredicate())
                .where('M', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where('#', any())
                .build();
    }

    private static TraceabilityPredicate energyOutputPredicate() {
        return metaTileEntities(MultiblockAbility.REGISTRY.get(MultiblockAbility.OUTPUT_ENERGY).stream().filter(mte -> {
            IEnergyContainer container = mte.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
            return container != null && container.getOutputVoltage() <= GTValues.V[GTValues.MV];
        }).toArray(MetaTileEntity[]::new))
                .addTooltip("gregtech.multiblock.pattern.error.limited.1", GTValues.VN[GTValues.LV])
                .addTooltip("gregtech.multiblock.pattern.error.limited.0", GTValues.VN[GTValues.MV]);
    }

    private static IBlockState getCasingState() {
        return MetaBlocks.LARGE_MULTIBLOCK_CASING.getState(BlockLargeMultiblockCasing.CasingType.STEAM_CASING);
    }

    private static IBlockState getCasingState2() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.BRONZE_GEARBOX);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gcym.machine.steam_engine.tooltip.1", GTValues.VNF[GTValues.MV]));
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.STEAM_CASING;
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return Textures.STEAM_ENGINE_OVERLAY;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    @Override
    public void runMufflerEffect(float xPos, float yPos, float zPos, float xSpd, float ySpd, float zSpd) {
        this.getWorld().spawnParticle(EnumParticleTypes.CLOUD, xPos, yPos, zPos, xSpd, ySpd, zSpd);
    }
}
