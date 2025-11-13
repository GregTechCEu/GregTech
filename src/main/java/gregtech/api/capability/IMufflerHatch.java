package gregtech.api.capability;

public interface IMufflerHatch {

    /**
     * @return true if front face is free and contains only air blocks in 1x1 area
     */
    boolean isFrontFaceFree();
}
