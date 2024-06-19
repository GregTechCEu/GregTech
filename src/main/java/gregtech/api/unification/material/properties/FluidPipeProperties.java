package gregtech.api.unification.material.properties;

import gregtech.api.GTValues;
import gregtech.api.capability.IPropertyFluidFilter;
import gregtech.api.fluids.FluidConstants;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.AttributedFluid;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.api.fluids.attribute.FluidAttributes;
import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.util.EntityDamageUtil;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;

import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class FluidPipeProperties implements IMaterialProperty, IPropertyFluidFilter, INodeData<FluidPipeProperties> {

    private final Object2BooleanMap<FluidAttribute> containmentPredicate = new Object2BooleanOpenHashMap<>();

    private int throughput;
    private final int tanks;

    private int maxFluidTemperature;
    private boolean gasProof;
    private boolean cryoProof;
    private boolean plasmaProof;

    public FluidPipeProperties(int maxFluidTemperature, int throughput, boolean gasProof, boolean acidProof,
                               boolean cryoProof, boolean plasmaProof) {
        this(maxFluidTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof, 1);
    }

    /**
     * Should only be called from
     * {@link gregtech.common.pipelike.fluidpipe.FluidPipeType#modifyProperties(FluidPipeProperties)}
     */
    public FluidPipeProperties(int maxFluidTemperature, int throughput, boolean gasProof, boolean acidProof,
                               boolean cryoProof, boolean plasmaProof, int tanks) {
        this.maxFluidTemperature = maxFluidTemperature;
        this.throughput = throughput;
        this.gasProof = gasProof;
        if (acidProof) setCanContain(FluidAttributes.ACID, true);
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
        this.tanks = tanks;
    }

    /**
     * Default property constructor.
     */
    public FluidPipeProperties() {
        this(300, 1, false, false, false, false);
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (!properties.hasProperty(PropertyKey.WOOD)) {
            properties.ensureSet(PropertyKey.INGOT, true);
        }

        if (properties.hasProperty(PropertyKey.ITEM_PIPE)) {
            throw new IllegalStateException(
                    "Material " + properties.getMaterial() +
                            " has both Fluid and Item Pipe Property, which is not allowed!");
        }
    }

    public int getTanks() {
        return tanks;
    }

    public int getThroughput() {
        return throughput;
    }

    public void setThroughput(int throughput) {
        this.throughput = throughput;
    }

    @Override
    public int getMaxFluidTemperature() {
        return maxFluidTemperature;
    }

    public void setMaxFluidTemperature(int maxFluidTemperature) {
        this.maxFluidTemperature = maxFluidTemperature;
    }

    @Override
    public boolean canContain(@NotNull FluidState state) {
        return switch (state) {
            case LIQUID -> true;
            case GAS -> gasProof;
            case PLASMA -> plasmaProof;
        };
    }

    @Override
    public boolean canContain(@NotNull FluidAttribute attribute) {
        return containmentPredicate.getBoolean(attribute);
    }

    @Override
    public void setCanContain(@NotNull FluidAttribute attribute, boolean canContain) {
        this.containmentPredicate.put(attribute, canContain);
    }

    @Override
    public @NotNull @UnmodifiableView Collection<@NotNull FluidAttribute> getContainedAttributes() {
        return containmentPredicate.keySet();
    }

    public boolean isGasProof() {
        return gasProof;
    }

    public void setGasProof(boolean gasProof) {
        this.gasProof = gasProof;
    }

    public boolean isAcidProof() {
        return canContain(FluidAttributes.ACID);
    }

    public boolean isCryoProof() {
        return cryoProof;
    }

    public void setCryoProof(boolean cryoProof) {
        this.cryoProof = cryoProof;
    }

    public boolean isPlasmaProof() {
        return plasmaProof;
    }

    public void setPlasmaProof(boolean plasmaProof) {
        this.plasmaProof = plasmaProof;
    }

    @Override
    public int getChannelMaxCount() {
        return this.tanks;
    }

    public PipeLossResult determineFluidPassthroughResult(@NotNull FluidStack stack, World world, BlockPos pos) {
        Fluid fluid = stack.getFluid();

        boolean burning = this.getMaxFluidTemperature() < fluid.getTemperature(stack);
        boolean leaking = !this.isGasProof() && fluid.isGaseous(stack);
        boolean shattering = !this.isCryoProof() && fluid.getTemperature() < FluidConstants.CRYOGENIC_FLUID_THRESHOLD;
        boolean corroding = false;
        boolean melting = false;

        if (fluid instanceof AttributedFluid attributedFluid) {
            FluidState state = attributedFluid.getState();
            if (!this.canContain(state)) {
                leaking = state == FluidState.GAS;
                melting = state == FluidState.PLASMA;
            }

            // carrying plasmas which are too hot when plasma proof does not burn pipes
            if (burning && state == FluidState.PLASMA && this.canContain(FluidState.PLASMA)) {
                burning = false;
            }

            for (FluidAttribute attribute : attributedFluid.getAttributes()) {
                if (!this.canContain(attribute)) {
                    // corrodes if the pipe can't handle the attribute, even if it's not an acid
                    corroding = true;
                }
            }
        }

        if (burning || leaking || corroding || shattering || melting) {
            return determineDestroyPipeResults(stack, burning, leaking, corroding, shattering, melting, world, pos);
        } else return new PipeLossResult(n -> {}, 1d);
    }

    public PipeLossResult determineDestroyPipeResults(FluidStack stack, boolean isBurning, boolean isLeaking,
                                                      boolean isCorroding, boolean isShattering,
                                                      boolean isMelting, World world, BlockPos pos) {
        List<Runnable> particleActions = new ObjectArrayList<>();
        Consumer<TileEntityFluidPipe> damageAction = tile -> {};
        Runnable destructionAction = () -> {};
        double mult = 1;

        if (isLeaking) {
            particleActions.add(() -> TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP,
                    EnumParticleTypes.SMOKE_NORMAL, 7 + GTValues.RNG.nextInt(2)));

            // voids 10%
            mult *= 0.9;

            // apply heat damage in area surrounding the pipe
            damageAction = tile -> tile.dealDamage(2, entity -> EntityDamageUtil.applyTemperatureDamage(entity,
                    stack.getFluid().getTemperature(stack), 2.0F, 10));

            // chance to do a small explosion
            if (GTValues.RNG.nextInt(isBurning ? 3 : 7) == 0) {
                destructionAction = () -> world.setBlockToAir(pos);
                if (!world.isRemote) {
                    particleActions.add(() -> ((WorldServer) world).spawnParticle(EnumParticleTypes.SMOKE_LARGE,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.0));
                }
                particleActions
                        .add(() -> world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                1.0f + GTValues.RNG.nextFloat(), false));
            }
        }

        if (isCorroding) {
            particleActions.add(() -> TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP,
                    EnumParticleTypes.CRIT_MAGIC, 3 + GTValues.RNG.nextInt(2)));

            // voids 25%
            mult *= 0.75;

            // apply chemical damage in area surrounding the pipe
            damageAction = tile -> tile.dealDamage(1, entity -> EntityDamageUtil.applyChemicalDamage(entity, 2));

            // 1/10 chance to void everything and destroy the pipe
            if (GTValues.RNG.nextInt(10) == 0) {
                mult = 0;
                destructionAction = () -> world.setBlockToAir(pos);
            }
        }

        if (isBurning || isMelting) {
            particleActions.add(() -> TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP,
                    EnumParticleTypes.FLAME, (isMelting ? 7 : 3) + GTValues.RNG.nextInt(2)));

            // voids 75%
            mult *= 0.25;

            // 1/4 chance to burn everything around it
            if (GTValues.RNG.nextInt(4) == 0) {
                TileEntityFluidPipe.setNeighboursToFire(world, pos);
            }

            // apply heat damage in area surrounding the pipe
            damageAction = tile -> tile.dealDamage(2, entity -> EntityDamageUtil.applyTemperatureDamage(entity,
                    stack.getFluid().getTemperature(stack), 2.0F, 10));

            // 1/10 chance to void everything and burn the pipe
            if (GTValues.RNG.nextInt(10) == 0) {
                mult = 0;
                destructionAction = () -> world.setBlockState(pos, Blocks.FIRE.getDefaultState());
            }
        }

        if (isShattering) {
            particleActions.add(() -> TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP,
                    EnumParticleTypes.CLOUD, 3 + GTValues.RNG.nextInt(2)));

            // voids 75%
            mult *= 0.75;

            // apply frost damage in area surrounding the pipe
            damageAction = tile -> tile.dealDamage(2, entity -> EntityDamageUtil.applyTemperatureDamage(entity,
                    stack.getFluid().getTemperature(stack), 2.0F, 10));

            // 1/10 chance to void everything and freeze the pipe
            if (GTValues.RNG.nextInt(10) == 0) {
                mult = 0;
                destructionAction = () -> world.setBlockState(pos, Blocks.ICE.getDefaultState());
            }
        }
        Runnable finalDestructionAction = destructionAction;
        Consumer<TileEntityFluidPipe> finalDamageAction = damageAction;
        return new PipeLossResult(nodeG -> {
            // only do 'extra' actions if the node already holds its MTE.
            // don't go fetch it from the world as this 9 times out of 10 means loading a chunk unnecessarily.
            IPipeTile<?, ?, ?> tile = nodeG.getHeldMTEUnsafe();
            if (tile instanceof TileEntityFluidPipe pipe) {
                pipe.playDamageSound();
                particleActions.forEach(Runnable::run);
                finalDamageAction.accept(pipe);
            }
            finalDestructionAction.run();
        }, mult);
    }

    @Override
    public FluidPipeProperties getMinData(Set<FluidPipeProperties> datas) {
        int maxFluidTemperature = this.getMaxFluidTemperature();
        int throughput = this.getThroughput();
        boolean gasProof = this.isGasProof();
        boolean acidProof = this.isAcidProof();
        boolean cryoProof = this.isCryoProof();
        boolean plasmaProof = this.isPlasmaProof();
        for (FluidPipeProperties data : datas) {
            maxFluidTemperature = Math.min(maxFluidTemperature, data.getMaxFluidTemperature());
            throughput = Math.min(throughput, data.getThroughput());
            gasProof &= data.isGasProof();
            acidProof &= data.isAcidProof();
            cryoProof &= data.isCryoProof();
            plasmaProof &= data.isPlasmaProof();
        }
        return new FluidPipeProperties(maxFluidTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FluidPipeProperties that)) return false;
        return getThroughput() == that.getThroughput() &&
                getTanks() == that.getTanks() &&
                getMaxFluidTemperature() == that.getMaxFluidTemperature() &&
                isGasProof() == that.isGasProof() &&
                isCryoProof() == that.isCryoProof() &&
                isPlasmaProof() == that.isPlasmaProof() &&
                containmentPredicate.equals(that.containmentPredicate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getThroughput(), getTanks(), getMaxFluidTemperature(), gasProof, cryoProof, plasmaProof,
                containmentPredicate);
    }

    @Override
    public String toString() {
        return "FluidPipeProperties{" +
                "throughput=" + throughput +
                ", tanks=" + tanks +
                ", maxFluidTemperature=" + maxFluidTemperature +
                ", gasProof=" + gasProof +
                ", cryoProof=" + cryoProof +
                ", plasmaProof=" + plasmaProof +
                ", containmentPredicate=" + containmentPredicate +
                '}';
    }

    public static class PipeLossResult extends Tuple<Consumer<NetNode<?, ?, ?>>, Double> {

        public PipeLossResult(Consumer<NetNode<?, ?, ?>> postAction, Double lossFunction) {
            super(postAction, lossFunction);
        }

        public Consumer<NetNode<?, ?, ?>> getPostAction() {
            return this.getFirst();
        }

        public Double getLossFunction() {
            return this.getSecond();
        }
    }
}
