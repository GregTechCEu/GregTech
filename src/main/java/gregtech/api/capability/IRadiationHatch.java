package gregtech.api.capability;

public interface IRadiationHatch {
    /**
     * @return the radiation amount in (TODO Units)
     **/
    float getRadValue();


    /**
     * @return if this hatch is creative
     */
    boolean isCreative();
}
