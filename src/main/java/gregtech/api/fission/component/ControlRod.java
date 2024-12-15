package gregtech.api.fission.component;

public interface ControlRod extends FissionComponent {

    /**
     * @param neutrons the neutrons produced by a site to adjust
     * @return the adjusted value
     */
    float adjustNeutrons(float neutrons);

    /**
     * @param heat the heat produced by a site to adjust
     * @return the adjusted value
     */
    float adjustHeat(float heat);
}
