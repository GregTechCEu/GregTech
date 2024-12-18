package gregtech.api.fission;

public interface FissionReactorController {

    /**
     * Schedule a reactor runtime data recompute
     */
    void scheduleRuntimeRecompute();

    /**
     * @return if the controller is locked
     */
    boolean isLocked();
}
