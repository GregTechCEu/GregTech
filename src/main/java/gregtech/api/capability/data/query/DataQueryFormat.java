package gregtech.api.capability.data.query;

public final class DataQueryFormat {

    public static final DataQueryFormat RECIPE = create();
    public static final DataQueryFormat COMPUTATION = create();

    @SuppressWarnings("InstantiationOfUtilityClass")
    public static DataQueryFormat create() {
        return new DataQueryFormat();
    }

    private DataQueryFormat() {}
}
