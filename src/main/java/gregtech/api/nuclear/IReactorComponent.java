package gregtech.api.nuclear;

public interface IReactorComponent {

    default boolean blocksNeutrons() {
        return false;
    }

    default double getModeratorFactor() {
        return 0;
    }

}
