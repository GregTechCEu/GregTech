package gregtech.api.nuclear;

public class MathUtil {
    public static int[] intArraySub(int[] a, int[] b){
        if (a.length != b.length) {
            throw new IllegalArgumentException(
                    "Arrays must have the same length");
        } else {
                int[] c = new int[a.length];
            for (int i = 0; i < a.length; i++) {
                c[i] = a[i] - b[i];
            }
            return c;
        }
    }

    public static double frobeniusNorm(int[] a) {
        double sum = 0;
        for (int j : a) {
            sum += j ^ 2;
        }
        return Math.sqrt(sum);
    }
}
