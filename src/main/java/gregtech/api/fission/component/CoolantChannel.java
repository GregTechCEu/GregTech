package gregtech.api.fission.component;

public interface CoolantChannel extends FissionComponent {

    /**
     * @param heat the site to apply cooling to
     * @return the amount of heat cooled
     */
    float applyCooling(float heat);

    /**
     * @return the passive heat from the coolant
     */
    float coolantHeat();

    /**
     * @param neutrons the neutrons produced by a site to adjust
     * @return the adjusted value
     */
    float adjustNeutrons(float neutrons);
}
