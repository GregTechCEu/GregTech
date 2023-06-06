package gregtech.api.metatileentity.multiblock;

public interface IFissionReactorHatch {

    /**
     *
     * @param depth The depth of the reactor that needs checking
     * @return      If the channel directly below the hatch is valid or not
     */
    boolean checkValidity(int depth);

    boolean isValid();

    void setValid(boolean valid);

}
