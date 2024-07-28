package gregtech.api.capability.data.query;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Contract;

import java.util.Set;

public final class DataAccessFormat {

    public static final DataAccessFormat STANDARD = create(DataQueryFormat.RECIPE);
    public static final DataAccessFormat COMPUTATION = create(DataQueryFormat.COMPUTATION);

    public static final DataAccessFormat UNIVERSAL = new DataAccessFormat(null);

    private final Set<DataQueryFormat> supportedFormats;

    public static DataAccessFormat create(DataQueryFormat... allowedFormats) {
        return new DataAccessFormat(new ObjectOpenHashSet<>(allowedFormats));
    }

    private DataAccessFormat(Set<DataQueryFormat> supportedFormats) {
        this.supportedFormats = supportedFormats;
    }

    @Contract("_ -> this")
    public DataAccessFormat support(DataQueryFormat format) {
        if (supportedFormats != null) this.supportedFormats.add(format);
        return this;
    }

    @Contract("_ -> this")
    public DataAccessFormat notSupport(DataQueryFormat format) {
        if (supportedFormats != null) this.supportedFormats.remove(format);
        return this;
    }

    public boolean supportsFormat(DataQueryFormat format) {
        if (supportedFormats == null) return true;
        else return supportedFormats.contains(format);
    }
}
