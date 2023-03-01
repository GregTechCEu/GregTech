package gregtech.api.util.oreglob;

/**
 * Compilation result for oreGlob.
 * <p>
 *
 */
public final class OreGlobCompileResult {

    private final OreGlob instance;
    private final Report[] reports;

    public OreGlobCompileResult(OreGlob instance, Report[] reports) {
        this.instance = instance;
        this.reports = reports;
    }

    public OreGlob getInstance() {
        return instance;
    }

    public Report[] getReports() {
        return reports;
    }

    public boolean hasError() {
        for (Report report : reports) {
            if (report.isError()) return true;
        }
        return false;
    }

    public static final class Report {
        private final String message;
        private final boolean error;
        private final int start;
        private final int len;

        public Report(String message, boolean error) {
            this(message, error, -1, 0);
        }

        public Report(String message, boolean error, int start, int len) {
            this.message = message;
            this.error = error;
            this.start = start;
            this.len = len;
        }

        public String getMessage() {
            return message;
        }

        public boolean isError() {
            return error;
        }

        public int getStart() {
            return start;
        }

        public int getLength() {
            return len;
        }

        @Override
        public String toString() {
            return start < 0 ? message : "[" + start + "] " + message;
        }
    }
}
