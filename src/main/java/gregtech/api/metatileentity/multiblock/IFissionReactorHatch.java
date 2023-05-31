package gregtech.api.metatileentity.multiblock;

public interface IFissionReactorHatch {

    void checkValidity(int depth);

    boolean isValid();

    boolean setValid(boolean valid);

}
