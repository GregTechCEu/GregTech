package gregtech.api.util;

import java.util.Random;

/**
 * Modified Perlin Noise algorithm for improved performance
 */
public final class PerlinNoise {

    private final int[] permutation;
    private final int[] permutationHashed;

    public PerlinNoise(long seed) {
        Random random = new XSTR(seed);
        permutation = new int[512];
        permutationHashed = new int[512];

        for (int i = 0; i < 256; i++) {
            permutation[i] = i;
        }

        for (int i = 0; i < 256; i++) {
            int j = random.nextInt(256);
            int k = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = k;
        }

        for (int i = 0; i < permutation.length; i++) {
            permutationHashed[i] = permutation[i] & 0xF;
        }
    }

    public float noise(float x, float y, float z, int octaves, float persistence) {
        float total = 0;
        float frequency = 1;
        float amplitude = 1;
        float maxValue = 0;
        for (int i = 0; i < octaves; i++) {
            total += noise(x * frequency, y * frequency, z * frequency) * amplitude;

            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2;
        }
        return total / maxValue;
    }

    public float noise(float x, float y, float z) {
        int X = (int) (x) & 0xFF;
        int Y = (int) (y) & 0xFF;
        int Z = (int) (z) & 0xFF;
        x -= (int) x;
        y -= (int) y;
        z -= (int) z;
        float u = fade(x);
        float v = fade(y);
        float w = fade(z);
        int A = permutation[X] + Y;
        int AA = permutation[A] + Z;
        int AB = permutation[A + 1] + Z;
        int B = permutation[X + 1] + Y;
        int BA = permutation[B] + Z;
        int BB = permutation[B + 1] + Z;
        float res = lerp(w, lerp(v, lerp(u, grad(permutationHashed[AA], x, y, z),
                                grad(permutationHashed[BA], x - 1, y, z)),
                        lerp(u, grad(permutationHashed[AB], x, y - 1, z),
                                grad(permutationHashed[BB], x - 1, y - 1, z))),
                lerp(v, lerp(u, grad(permutationHashed[AA + 1], x, y, z - 1),
                                grad(permutationHashed[BA + 1], x - 1, y, z - 1)),
                        lerp(u, grad(permutationHashed[AB + 1], x, y - 1, z - 1),
                                grad(permutationHashed[BB + 1], x - 1, y - 1, z - 1))));
        return (res + 1.0F) / 2.0F;
    }

    private static float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }

    private static float grad(int hash, float x, float y, float z) {
        // not 1:1 with the original behavior, but is close enough, looks nicer in-game, and is much faster
        return ((hash & 0x1) != 0 ? -x : x) + ((hash & 0x2) != 0 ? -y : y) +
                ((hash & 0x4) != 0 ? ((hash == 0xC || hash == 0xE) ? -x : -z) : z);

        // original impl
        // float u = hash < 0x8 ? x : y;
        // float v = hash < 0x4 ? y : hash == 0xC || hash == 0xE ? x : z;
        // return ((hash & 1) == 0 ? u : -u) + ((hash & 2) == 0 ? v : -v);
    }
}
