package gregtech.api.util.oreglob;

import java.util.Collection;

/**
 * Compilation result for oreGlob.
 * <p>
 * OreGlobCompileResult contains OreGlob instance and array of reports. Report is
 * either an error or a warning, and a report contains string message and optional
 * position at the input string that the reported originated.
 * <p>
 * Whether the compilation failed or succeeded is determined by presence of error
 * reports. If there's one or more error report, then the result should be regarded
 * as failure. On failed compilation result, the OreGlob instance provided is expected
 * to match no inputs.
 */
public final class OreGlobCompileResult {

    private final OreGlob instance;
    private final Report[] reports;

    public OreGlobCompileResult(OreGlob instance, Collection<Report> reports) {
        this.instance = instance;
        this.reports = reports.stream().sorted().toArray(Report[]::new);
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

    public static final class Report implements Comparable<Report> {

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
            StringBuilder stb = new StringBuilder().append(error ? "[!] " : "[?] ");
            if (start >= 0) stb.append("[").append(start + 1).append("] ");
            return stb.append(message).toString();
        }

        @Override
        public int compareTo(Report o) {
            int i = Boolean.compare(o.error, this.error);
            return i != 0 ? i : Integer.compare(this.start, o.start);
        }
    }
}
