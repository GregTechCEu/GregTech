package gregtech.common.metatileentities.multiblock.hpca.helper;

import gregtech.api.capability.IHPCAComponentHatch;
import gregtech.api.capability.IHPCAComputationProvider;
import gregtech.api.capability.IHPCACoolantProvider;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityHPCA.HPCAGridHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class HPCAHelper {

    public static HPCAGridHandler gridBuilder(UnaryOperator<GridHandlerBuilder> b) {
        return b.apply(new GridHandlerBuilder()).build();
    }

    public static IHPCAComputationProvider getComputation(int cwuPerTick, int coolingPerTick, int upkeepEUt,
                                                          int maxEUt) {
        return new HPCAComputationProviderTestImpl(upkeepEUt, maxEUt, cwuPerTick, coolingPerTick);
    }

    public static IHPCACoolantProvider getCooler(int coolingPerTick, int upkeepEUt, int maxEUt) {
        return new HPCACoolantProviderTestImpl(upkeepEUt, maxEUt, coolingPerTick);
    }

    public static class GridHandlerBuilder {

        private int numComputation;
        private int numCooler;
        private UnaryOperator<CoolerBuilder> coolerBuilder;
        private UnaryOperator<ComputationBuilder> computationBuilder;

        private GridHandlerBuilder() {/**/}

        public GridHandlerBuilder numComputation(int num) {
            numComputation = num;
            return this;
        }

        public GridHandlerBuilder numComputation(Supplier<Integer> s) {
            numComputation = s.get();
            return this;
        }

        public GridHandlerBuilder numCooler(int num) {
            numCooler = num;
            return this;
        }

        public GridHandlerBuilder numCooler(Supplier<Integer> s) {
            numCooler = s.get();
            return this;
        }

        public GridHandlerBuilder computationBuilder(UnaryOperator<ComputationBuilder> b) {
            computationBuilder = b;
            return this;
        }

        public GridHandlerBuilder coolerBuilder(UnaryOperator<CoolerBuilder> b) {
            coolerBuilder = b;
            return this;
        }

        private HPCAGridHandler build() {
            HPCAGridHandler handler = new HPCAGridHandler(null);
            List<IHPCAComponentHatch> components = new ArrayList<>(numComputation + numCooler);
            for (int i = 0; i < numCooler; i++) {
                components.add(coolerBuilder.apply(new CoolerBuilder()).build());
            }
            for (int i = 0; i < numComputation; i++) {
                components.add(computationBuilder.apply(new ComputationBuilder()).build());
            }
            handler.onStructureForm(components);
            return handler;
        }
    }

    public static abstract class ComponentBuilder<T, U extends ComponentBuilder<T, U>> {

        protected int upkeepEUt;
        protected int maxEUt;

        public U EUt(int eut) {
            this.upkeepEUt = eut;
            this.maxEUt = eut;
            return cast(this);
        }

        public U EUt(int upkeepEUt, int maxEUt) {
            this.upkeepEUt = upkeepEUt;
            this.maxEUt = maxEUt;
            return cast(this);
        }

        public U EUt(Supplier<Integer> s) {
            this.upkeepEUt = Math.max(1, s.get());
            this.maxEUt = Math.max(1, s.get());
            return cast(this);
        }

        public U EUt(Supplier<Integer> s1, Supplier<Integer> s2) {
            this.upkeepEUt = s1.get();
            this.maxEUt = s2.get();
            return cast(this);
        }

        protected abstract T build();

        protected abstract U cast(ComponentBuilder<T, U> b);
    }

    public static class ComputationBuilder extends ComponentBuilder<IHPCAComputationProvider, ComputationBuilder> {

        private int cwuPerTick;
        private int coolingPerTick;

        public ComputationBuilder CWUt(int cwut) {
            this.cwuPerTick = cwut;
            return this;
        }

        public ComputationBuilder CWUt(Supplier<Integer> s) {
            this.cwuPerTick = Math.max(1, s.get());
            return this;
        }

        public ComputationBuilder coolingPerTick(int coolingPerTick) {
            this.coolingPerTick = coolingPerTick;
            return this;
        }

        public ComputationBuilder coolingPerTick(Supplier<Integer> s) {
            this.coolingPerTick = Math.max(1, s.get());
            return this;
        }

        @Override
        protected IHPCAComputationProvider build() {
            return new HPCAComputationProviderTestImpl(upkeepEUt, maxEUt, cwuPerTick, coolingPerTick);
        }

        @Override
        protected ComputationBuilder cast(ComponentBuilder<IHPCAComputationProvider, ComputationBuilder> b) {
            return (ComputationBuilder) b;
        }
    }

    public static class CoolerBuilder extends ComponentBuilder<IHPCACoolantProvider, CoolerBuilder> {

        private int coolingAmount;

        public CoolerBuilder coolingAmount(int coolingAmount) {
            this.coolingAmount = coolingAmount;
            return this;
        }

        public CoolerBuilder coolingAmount(Supplier<Integer> s) {
            this.coolingAmount = Math.max(1, s.get());
            return this;
        }

        @Override
        protected IHPCACoolantProvider build() {
            return new HPCACoolantProviderTestImpl(upkeepEUt, maxEUt, coolingAmount);
        }

        @Override
        protected CoolerBuilder cast(ComponentBuilder<IHPCACoolantProvider, CoolerBuilder> b) {
            return (CoolerBuilder) b;
        }
    }
}
