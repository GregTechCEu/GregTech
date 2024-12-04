package gregtech.api.fission.component;

/**
 * A component which reacts with neutrons
 */
public interface ReactiveComponent extends FissionComponent {

    /**
     * @param amount the amount of incoming neutrons
     * @return the amount of neutrons that reacted
     */
    int react(int amount);

    /**
     * @return the amount of emitted heat per fission
     */
    float heatPerFission();
}
