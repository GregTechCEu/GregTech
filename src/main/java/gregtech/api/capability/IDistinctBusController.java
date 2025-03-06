package gregtech.api.capability;

public interface IDistinctBusController {

    default boolean canBeDistinct() {
        return true;
    }

    boolean isDistinct();

    void setDistinct(boolean isDistinct);
}
