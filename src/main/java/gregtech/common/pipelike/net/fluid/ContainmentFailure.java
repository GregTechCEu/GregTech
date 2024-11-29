package gregtech.common.pipelike.net.fluid;

import gregtech.api.GTValues;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.api.fluids.attribute.FluidAttributes;
import gregtech.api.graphnet.pipenet.NodeLossResult;
import gregtech.api.graphnet.pipenet.physical.tile.IWorldPipeNetTile;
import gregtech.api.graphnet.traverseold.util.MultLossOperator;
import gregtech.api.util.EntityDamageUtil;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

@FunctionalInterface
public interface ContainmentFailure {

    ContainmentFailure FALLBACK = stack -> NodeLossResult.IDENTITY;

    EnumMap<FluidState, ContainmentFailure> STATE_FAILURES = new EnumMap<>(FluidState.class);

    Map<ResourceLocation, ContainmentFailure> ATTRIBUTE_FAILURES = new Object2ObjectOpenHashMap<>();

    static void registerFailure(FluidState state, ContainmentFailure failure) {
        STATE_FAILURES.put(state, failure);
    }

    static void registerFailure(FluidAttribute attribute, ContainmentFailure failure) {
        ATTRIBUTE_FAILURES.put(attribute.getResourceLocation(), failure);
    }

    static @NotNull ContainmentFailure getFailure(FluidState state) {
        return STATE_FAILURES.getOrDefault(state, FALLBACK);
    }

    static @NotNull ContainmentFailure getFailure(FluidAttribute attribute) {
        return ATTRIBUTE_FAILURES.getOrDefault(attribute.getResourceLocation(), FALLBACK);
    }

    static @NotNull ContainmentFailure getFailure(ResourceLocation attribute) {
        return ATTRIBUTE_FAILURES.getOrDefault(attribute, FALLBACK);
    }

    @NotNull
    NodeLossResult computeLossResult(FluidStack fluid);

    static void init() {
        registerFailure(FluidState.GAS, stack -> {
            if (GTValues.RNG.nextInt(8) == 0) {
                return new NodeLossResult(node -> {
                    IWorldPipeNetTile tile = node.getTileEntityNoLoading();
                    if (tile != null) {
                        tile.playLossSound();
                        tile.spawnParticles(EnumFacing.UP, EnumParticleTypes.SMOKE_NORMAL, 7 + GTValues.RNG.nextInt(2));
                        tile.dealAreaDamage(2, entity -> EntityDamageUtil.applyTemperatureDamage(entity,
                                stack.getFluid().getTemperature(stack), 1, 10));
                    }
                }, MultLossOperator.TENTHS[9]);
            } else {
                return new NodeLossResult(node -> {
                    node.getNet().getWorld().setBlockToAir(node.getEquivalencyData());
                    IWorldPipeNetTile tile = node.getTileEntityNoLoading();
                    if (tile != null) {
                        tile.playLossSound();
                        tile.visuallyExplode();
                        tile.spawnParticles(EnumFacing.UP, EnumParticleTypes.SMOKE_LARGE, 9 + GTValues.RNG.nextInt(3));
                        tile.dealAreaDamage(2, entity -> EntityDamageUtil.applyTemperatureDamage(entity,
                                stack.getFluid().getTemperature(stack), 1.5f, 15));
                    }
                }, MultLossOperator.TENTHS[2]);
            }
        });
        registerFailure(FluidState.LIQUID, stack -> new NodeLossResult(node -> {
            IWorldPipeNetTile tile = node.getTileEntityNoLoading();
            if (tile != null) {
                tile.playLossSound();
                for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                    int particles = GTValues.RNG.nextInt(5);
                    if (particles != 0) {
                        tile.spawnParticles(facing, EnumParticleTypes.DRIP_WATER, particles);
                    }
                }
                tile.dealAreaDamage(1, entity -> EntityDamageUtil.applyTemperatureDamage(entity,
                        stack.getFluid().getTemperature(stack), 2f, 20));
            }
        }, MultLossOperator.TENTHS[6]));
        registerFailure(FluidState.PLASMA, stack -> {
            if (GTValues.RNG.nextInt(4) == 0) {
                return new NodeLossResult(node -> {
                    IWorldPipeNetTile tile = node.getTileEntityNoLoading();
                    if (tile != null) {
                        tile.playLossSound();
                        tile.spawnParticles(EnumFacing.UP, EnumParticleTypes.SMOKE_NORMAL, 1 + GTValues.RNG.nextInt(2));
                        tile.dealAreaDamage(3, entity -> EntityDamageUtil.applyTemperatureDamage(entity,
                                stack.getFluid().getTemperature(stack), 1, 25));
                    }
                }, MultLossOperator.TENTHS[8]);
            } else {
                return new NodeLossResult(node -> {
                    node.getNet().getWorld().setBlockToAir(node.getEquivalencyData());
                    IWorldPipeNetTile tile = node.getTileEntityNoLoading();
                    if (tile != null) {
                        tile.playLossSound();
                        tile.visuallyExplode();
                        tile.spawnParticles(EnumFacing.UP, EnumParticleTypes.SMOKE_LARGE, 3 + GTValues.RNG.nextInt(3));
                        tile.dealAreaDamage(3, entity -> EntityDamageUtil.applyTemperatureDamage(entity,
                                stack.getFluid().getTemperature(stack), 1.5f, 30));
                    }
                }, MultLossOperator.TENTHS[2]);
            }
        });
        registerFailure(FluidAttributes.ACID, stack -> {
            if (GTValues.RNG.nextInt(10) == 0) {
                return new NodeLossResult(node -> {
                    IWorldPipeNetTile tile = node.getTileEntityNoLoading();
                    if (tile != null) {
                        tile.playLossSound();
                        boolean gaseous = stack.getFluid().isGaseous(stack);
                        tile.spawnParticles(gaseous ? EnumFacing.UP : EnumFacing.DOWN, EnumParticleTypes.CRIT_MAGIC,
                                3 + GTValues.RNG.nextInt(2));
                        tile.dealAreaDamage(gaseous ? 2 : 1,
                                entity -> EntityDamageUtil.applyChemicalDamage(entity, gaseous ? 2 : 3));
                    }
                }, MultLossOperator.TENTHS[9]);
            } else {
                return new NodeLossResult(node -> {
                    node.getNet().getWorld().setBlockToAir(node.getEquivalencyData());
                    IWorldPipeNetTile tile = node.getTileEntityNoLoading();
                    if (tile != null) {
                        tile.playLossSound();
                        boolean gaseous = stack.getFluid().isGaseous(stack);
                        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                            tile.spawnParticles(facing, EnumParticleTypes.CRIT_MAGIC, 3 + GTValues.RNG.nextInt(2));
                        }
                        tile.spawnParticles(gaseous ? EnumFacing.UP : EnumFacing.DOWN, EnumParticleTypes.CRIT_MAGIC,
                                6 + GTValues.RNG.nextInt(4));
                        tile.dealAreaDamage(gaseous ? 2 : 1,
                                entity -> EntityDamageUtil.applyChemicalDamage(entity, gaseous ? 3 : 4));
                    }
                }, MultLossOperator.EIGHTHS[6]);
            }
        });
    }
}
