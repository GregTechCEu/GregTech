package gregtech.api.capability;

public interface IMultiblockController {

    boolean isStructureFormed(String name);

    default boolean isStructureObstructed() {
        return false;
    }
}
