package gregtech.api.nuclear;

public interface IReactorState {

    /**
     * Checks the state of the reactor and determines which one to switch to for the next tick
     */
    IReactorState getNextState();

    /**
     * Used to determine if the reactor is still operational, and in case it isn't, stop making it evolve
     */
    default boolean isFailedState() {
        return false;
    }

    /**
     * Used to have special effects like explosions or meltdowns
     */
    void performWorldEffects();

    /**
     * Modifies the reactor's internal parameters to emulate evolution in time
     */
    void runStateEvolution();

}
