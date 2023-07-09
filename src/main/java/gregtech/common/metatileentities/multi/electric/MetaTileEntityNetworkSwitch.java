package gregtech.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.IOpticalComputationHatch;
import gregtech.api.capability.IOpticalComputationProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockComputerCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MetaTileEntityNetworkSwitch extends MetaTileEntityDataBank implements IOpticalComputationProvider {

    private final MultipleComputationHandler computationHandler = new MultipleComputationHandler();

    public MetaTileEntityNetworkSwitch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityNetworkSwitch(metaTileEntityId);
    }

    @Override
    protected int calculateEnergyUsage() {
        int receivers = getAbilities(MultiblockAbility.COMPUTATION_DATA_RECEPTION).size();
        int transmitters = getAbilities(MultiblockAbility.COMPUTATION_DATA_TRANSMISSION).size();
        return GTValues.VA[GTValues.IV] * (receivers + transmitters);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        computationHandler.onStructureForm(
                getAbilities(MultiblockAbility.COMPUTATION_DATA_RECEPTION),
                getAbilities(MultiblockAbility.COMPUTATION_DATA_TRANSMISSION));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        computationHandler.reset();
    }

    @Override
    protected int getEnergyUsage() {
        return isStructureFormed() ? computationHandler.getEUt() : 0;
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return isActive() && !hasNotEnoughEnergy ? computationHandler.requestCWUt(cwut, simulate) : 0;
    }

    @Override
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return isStructureFormed() ? computationHandler.getMaxCWUt() : 0;
    }

    // allows chaining Network Switches together
    @Override
    public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return true;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "XAX", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('S', selfPredicate())
                .where('A', states(getAdvancedState()))
                .where('X', states(getCasingState()).setMinGlobalLimited(7)
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1, 1))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1))
                        .or(abilities(MultiblockAbility.COMPUTATION_DATA_RECEPTION).setMinGlobalLimited(1, 2))
                        .or(abilities(MultiblockAbility.COMPUTATION_DATA_TRANSMISSION).setMinGlobalLimited(1, 1)))
                .build();
    }

    private static @NotNull IBlockState getCasingState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.COMPUTER_CASING);
    }

    private static @NotNull IBlockState getAdvancedState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.ADVANCED_COMPUTER_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.COMPUTER_CASING;
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return Textures.NETWORK_SWITCH_OVERLAY;
    }

    @Override
    protected void renderTextures(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), this.isActive(), this.isWorkingEnabled());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.network_switch.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.network_switch.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.network_switch.tooltip.3"));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed()) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.computation.max", computationHandler.getMaxCWUt()));
            if (computationHandler.hasNonBridgingConnections()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.computation.non_bridging")
                        .setStyle(new Style().setColor(TextFormatting.RED)
                                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new TextComponentTranslation("gregtech.multiblock.computation.non_bridging.detailed")))));
            }
        }
    }

    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        if (isStructureFormed() && computationHandler.hasNonBridgingConnections()) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.computation.non_bridging.detailed")
                    .setStyle(new Style().setColor(TextFormatting.RED)));
        }
    }

    /** Handles computation load across multiple receivers and to multiple transmitters. */
    private static class MultipleComputationHandler {

        // providers in the NS provide distributable computation to the NS
        private final Set<IOpticalComputationHatch> providers = new HashSet<>();
        // transmitters in the NS give computation to other multis
        private final Set<IOpticalComputationHatch> transmitters = new HashSet<>();

        private int EUt;

        private void onStructureForm(Collection<IOpticalComputationHatch> providers, Collection<IOpticalComputationHatch> transmitters) {
            reset();
            this.providers.addAll(providers);
            this.transmitters.addAll(transmitters);
            this.EUt = (providers.size() + transmitters.size()) * GTValues.VA[GTValues.IV];
        }

        private void reset() {
            providers.clear();
            transmitters.clear();
            EUt = 0;
        }

        private int requestCWUt(int cwut, boolean simulate) {
            int allocatedCWUt = 0;
            Collection<IOpticalComputationProvider> seen = new ArrayList<>();
            for (var provider : providers) {
                if (!provider.canBridge()) continue;
                int allocated = provider.requestCWUt(cwut, simulate, seen);
                allocatedCWUt += allocated;
                cwut -= allocated;
                if (cwut == 0) break;
            }
            return allocatedCWUt;
        }

        /** The max CWU/t that this Network Switch can provide, combining all its inputs. */
        private int getMaxCWUt() {
            int maximumCWUt = 0;
            for (var provider : providers) {
                if (!provider.canBridge()) continue;
                maximumCWUt += provider.getMaxCWUt();
            }
            return maximumCWUt;
        }

        /** The EU/t cost of this Network Switch given the attached providers and transmitters. */
        private int getEUt() {
            return EUt;
        }

        /** Test if any of the provider hatches do not allow bridging */
        private boolean hasNonBridgingConnections() {
            for (var provider : providers) {
                if (!provider.canBridge()) {
                    return true;
                }
            }
            return false;
        }
    }
}
