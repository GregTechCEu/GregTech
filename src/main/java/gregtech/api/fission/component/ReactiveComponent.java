package gregtech.api.fission.component;

/**
 * A component which reacts with neutrons
 */
public interface ReactiveComponent extends FissionComponent {

    /**
     * @return if the component is able to react
     */
    boolean canReact();

    /**
     * @param amount the amount of incoming neutrons
     * @return the amount of neutrons that reacted
     */
    float react(float amount);

    /**
     * @return the amount of emitted heat per fission
     */
    float heatPerFission();
}
