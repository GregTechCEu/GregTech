package gregtech.api.nuclear.fission.states;

import gregtech.api.nuclear.fission.FissionReactor;

public class ReactorStateRampUpBegin extends ReactorStateBase {

    public ReactorStateRampUpBegin(FissionReactor reactor) {
        super(reactor);
    }

    @Override
    public IReactorState getNextState() {
        if (this.reactor.temperature > this.reactor.maxTemperature) {
            // Cooling insufficient -> Reactor overheats
            return new ReactorStateOverheating(this.reactor);
        } else if (relaxedEquals(this.reactor.powerProductionFactor, 0.6 * this.reactor.maxPower, 0.1) && this.reactor.controlRodInsertion == this.reactor.criticalRodInsertion) {
            // Reactor is brought to 60% of full power slowly, the half power state is important to let the reactor reach neutron equilibrium
            return new ReactorStateHalfPower(this.reactor);
        } else if (this.reactor.powerProductionFactor > 0.6 * this.reactor.maxPower) {
            // If the rise in power is too fast the reactor risks stalling
            return new ReactorStateStalling(this.reactor);
        } else {
            return this;
        }
    }

    @Override
    public void runStateEvolution() {
        double speedFactor = 1.05D + (this.reactor.criticalRodInsertion - this.reactor.controlRodInsertion)/15.D;

        // Reactor is ramping up, power increase speed depends on how much the rods have been withdrawn
        this.reactor.powerProductionFactor *= speedFactor;
        // Neutron poisoning increases due to power increasing
        this.reactor.neutronPoisoning = Math.max(this.reactor.neutronPoisoning * speedFactor, 1.D);
        //Temperature increases while ramping up
        this.reactor.temperature = responseFunction(this.reactor.maxTemperature, this.reactor.temperature, this.reactor.criticalCoolantFlow(), this.reactor.coolantFlowRate);

        this.reactor.fuelDepletion -= Math.max(0.D, this.reactor.powerProductionFactor/this.reactor.maxPower);
    }
}
