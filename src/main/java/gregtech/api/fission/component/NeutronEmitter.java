package gregtech.api.fission.component;

public interface NeutronEmitter extends FissionComponent {

    /**
     * @return the generated amount of neutrons
     */
    int generateNeutrons();
}
