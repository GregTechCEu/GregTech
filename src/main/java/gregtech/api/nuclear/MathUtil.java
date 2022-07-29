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

    public static double l2norm(int[] a) {
        int sum = 0;
        for (int j : a) {
            sum += j * j;
        }
        return Math.sqrt(sum);
    }
}
