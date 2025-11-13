/*
 * DSI utilities
 *
 * Copyright (C) 2013-2023 Sebastiano Vigna
 *
 * This program and the accompanying materials are made available under the
 * terms of the GNU Lesser General Public License v2.1 or later,
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html,
 * or the Apache Software License 2.0, which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * SPDX-License-Identifier: LGPL-2.1-or-later OR Apache-2.0
 */
package gregtech.api.util.random;

import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.Random;
import java.util.SplittableRandom;

/**
 * A fast, all-purpose, rock-solid {@linkplain Random pseudorandom number generator}. It has
 * excellent speed, a state space (256 bits) that is large enough for any parallel application, and
 * it passes all tests we are aware of. In Java, it is slightly faster than a
 * {@code XoShiRo256StarStarRandom}. More information can be found at our
 * <a href="http://prng.di.unimi.it/">PRNG page</a>.
 *
 * <p>
 * Note that starting with Java 17 you can find this generator in <a href=
 * "https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/random/package-summary.html">{@code java.util.random}</a>.
 *
 * <p>
 * If you need to generate just floating-point numbers, {@code XoShiRo256PlusRandom} is slightly
 * faster. If you are tight on space, you might try {@code XoRoShiRo128PlusPlusRandom}.
 *
 * <p>
 * By using the supplied {@link #jump()} method it is possible to generate non-overlapping long
 * sequences for parallel computations; {@link #longJump()} makes it possible to create several
 * starting points, each providing several non-overlapping sequences, for distributed computations.
 * This class provides also a {@link #split()} method to support recursive parallel computations, in
 * the spirit of {@link SplittableRandom}.
 *
 * <p>
 * <strong>Warning</strong>: before release 2.6.3, the {@link #split()} method would not alter the
 * state of the caller, and it would return instances initialized in the same way if called multiple
 * times. This was a major mistake in the implementation and it has been fixed, but as a consequence
 * the output of the caller after a call to {@link #split()} is now different, and the result of
 * {@link #split()} is initialized in a different way.
 *
 * <p>
 * Note that this is not a {@linkplain SecureRandom secure generator}.
 *
 * @version 1.0
 * @see Random
 */

public class XoShiRo256PlusPlusRandom extends Random {

    private static final long serialVersionUID = 1L;
    /** The internal state of the algorithm. */
    private long s0, s1, s2, s3;

    protected XoShiRo256PlusPlusRandom(final long s0, final long s1, final long s2, final long s3) {
        this.s0 = s0;
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;
    }

    /** Creates a new generator seeded using {@link Util#randomSeed()}. */
    public XoShiRo256PlusPlusRandom() {
        this(Util.randomSeed());
    }

    /**
     * Creates a new generator using a given seed.
     *
     * @param seed a seed for the generator.
     */
    public XoShiRo256PlusPlusRandom(final long seed) {
        setSeed(seed);
    }

    /**
     * Returns a copy of this generator. The sequences produced by this generator and by the returned generator will be
     * identical.
     *
     * <p>
     * This method is particularly useful in conjunction with the {@link #jump()} (or {@link #longJump()}) method: by
     * calling repeatedly
     * {@link #jump() jump().copy()} over a generator it is possible to create several generators producing
     * non-overlapping sequences.
     *
     * @return a copy of this generator.
     */
    public @NotNull XoShiRo256PlusPlusRandom copy() {
        return new XoShiRo256PlusPlusRandom(s0, s1, s2, s3);
    }

    @Override
    public long nextLong() {
        final long t0 = s0;
        final long result = Long.rotateLeft(t0 + s3, 23) + t0;

        final long t = s1 << 17;

        s2 ^= t0;
        s3 ^= s1;
        s1 ^= s2;
        s0 ^= s3;

        s2 ^= t;

        s3 = Long.rotateLeft(s3, 45);

        return result;
    }

    @Override
    public int nextInt() {
        return (int) nextLong();
    }

    @Override
    public int nextInt(final int n) {
        return (int) nextLong(n);
    }

    /**
     * Returns a pseudorandom uniformly distributed {@code long} value
     * between 0 (inclusive) and the specified value (exclusive), drawn from
     * this random number generator's sequence. The algorithm used to generate
     * the value guarantees that the result is uniform, provided that the
     * sequence of 64-bit values produced by this generator is.
     *
     * @param n the positive bound on the random number to be returned.
     * @return the next pseudorandom {@code long} value between {@code 0} (inclusive) and {@code n} (exclusive).
     */
    public long nextLong(final long n) {
        if (n <= 0) throw new IllegalArgumentException("illegal bound " + n + " (must be positive)");
        long t = nextLong();
        final long nMinus1 = n - 1;
        // Rejection-based algorithm to get uniform integers in the general case
        for (long u = t >>> 1; u + nMinus1 - (t = u % n) < 0; u = nextLong() >>> 1);
        return t;
    }

    @Override
    public double nextDouble() {
        return (nextLong() >>> 11) * 0x1.0p-53;
    }

    /**
     * Returns the next pseudorandom, uniformly distributed
     * {@code double} value between {@code 0.0} and
     * {@code 1.0} from this random number generator's sequence,
     * using a fast multiplication-free method which, however,
     * can provide only 52 significant bits.
     *
     * <p>
     * This method is faster than {@link #nextDouble()}, but it
     * can return only dyadic rationals of the form <var>k</var> / 2<sup>&minus;52</sup>,
     * instead of the standard <var>k</var> / 2<sup>&minus;53</sup>. Before
     * version 2.4.1, this was actually the standard implementation of
     * {@link #nextDouble()}, so you can use this method if you need to
     * reproduce exactly results obtained using previous versions.
     *
     * <p>
     * The only difference between the output of this method and that of
     * {@link #nextDouble()} is an additional least significant bit set in half of the
     * returned values. For most applications, this difference is negligible.
     *
     * @return the next pseudorandom, uniformly distributed {@code double}
     *         value between {@code 0.0} and {@code 1.0} from this
     *         random number generator's sequence, using 52 significant bits only.
     */
    public double nextDoubleFast() {
        return Double.longBitsToDouble(0x3FFL << 52 | nextLong() >>> 12) - 1.0;
    }

    @Override
    public float nextFloat() {
        return (nextLong() >>> 40) * 0x1.0p-24f;
    }

    @Override
    public boolean nextBoolean() {
        return nextLong() < 0;
    }

    @Override
    public void nextBytes(final byte @NotNull [] bytes) {
        int i = bytes.length, n = 0;
        while (i != 0) {
            n = Math.min(i, 8);
            for (long bits = nextLong(); n-- != 0; bits >>= 8) bytes[--i] = (byte) bits;
        }
    }

    private static final long[] JUMP = { 0x180ec6d33cfd0abaL, 0xd5a61266f0c9392cL, 0xa9582618e03fc9aaL,
            0x39abdc4529b1661cL };

    protected XoShiRo256PlusPlusRandom jump(final long[] jump) {
        long s0 = 0;
        long s1 = 0;
        long s2 = 0;
        long s3 = 0;
        for (final long element : jump)
            for (int b = 0; b < 64; b++) {
                if ((element & 1L << b) != 0) {
                    s0 ^= this.s0;
                    s1 ^= this.s1;
                    s2 ^= this.s2;
                    s3 ^= this.s3;
                }
                nextLong();
            }

        this.s0 = s0;
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;
        return this;
    }

    /**
     * The jump function for this generator. It is equivalent to 2<sup>128</sup>
     * calls to {@link #nextLong()}; it can be used to generate 2<sup>128</sup>
     * non-overlapping subsequences for parallel computations.
     *
     * @return this generator.
     * @see #copy()
     */

    public @NotNull XoShiRo256PlusPlusRandom jump() {
        return jump(JUMP);
    }

    private static final long[] LONG_JUMP = { 0x76e15d3efefdcbbfL, 0xc5004e441c522fb3L, 0x77710069854ee241L,
            0x39109bb02acbe635L };

    /**
     * The long-jump function for this generator. It is equivalent to 2<sup>192</sup>
     * calls to {@link #nextLong()}; it can be used to generate 2<sup>64</sup> starting points,
     * from each of which {@link #jump()} will generate 2<sup>64</sup> non-overlapping
     * subsequences for parallel distributed computations.
     *
     * @return this generator.
     * @see #copy()
     */

    public @NotNull XoShiRo256PlusPlusRandom longJump() {
        return jump(LONG_JUMP);
    }

    /**
     * Returns a new instance that shares no mutable state
     * with this instance. The sequence generated by the new instance
     * depends deterministically from the state of this instance,
     * but the probability that the sequence generated by this
     * instance and by the new instance overlap is negligible.
     *
     * <p>
     * <strong>Warning</strong>: before release 2.6.3, this method
     * would not alter the state of the caller, and it would return instances initialized in the same
     * way if called multiple times. This was a major mistake in the implementation and it has been fixed,
     * but as a consequence the output of this instance after a call to this method is
     * now different, and the returned instance is initialized in a different way.
     *
     * @return the new instance.
     */
    public @NotNull XoShiRo256PlusPlusRandom split() {
        nextLong();
        final XoShiRo256PlusPlusRandom split = copy();

        long h0 = split.s0;
        long h1 = split.s1;
        long h2 = split.s2;
        long h3 = split.s3;

        // A round of SpookyHash ShortMix
        h2 = Long.rotateLeft(h2, 50);
        h2 += h3;
        h0 ^= h2;
        h3 = Long.rotateLeft(h3, 52);
        h3 += h0;
        h1 ^= h3;
        h0 = Long.rotateLeft(h0, 30);
        h0 += h1;
        h2 ^= h0;
        h1 = Long.rotateLeft(h1, 41);
        h1 += h2;
        h3 ^= h1;
        h2 = Long.rotateLeft(h2, 54);
        h2 += h3;
        h0 ^= h2;
        h3 = Long.rotateLeft(h3, 48);
        h3 += h0;
        h1 ^= h3;
        h0 = Long.rotateLeft(h0, 38);
        h0 += h1;
        h2 ^= h0;
        h1 = Long.rotateLeft(h1, 37);
        h1 += h2;
        h3 ^= h1;
        h2 = Long.rotateLeft(h2, 62);
        h2 += h3;
        h0 ^= h2;
        h3 = Long.rotateLeft(h3, 34);
        h3 += h0;
        h1 ^= h3;
        h0 = Long.rotateLeft(h0, 5);
        h0 += h1;
        h2 ^= h0;
        h1 = Long.rotateLeft(h1, 36);
        h1 += h2;
        h3 ^= h1;

        split.s0 = h0;
        split.s1 = h1;
        split.s2 = h2;
        split.s3 = h3;
        return split;
    }

    /**
     * Sets the seed of this generator.
     *
     * <p>
     * The argument will be used to seed a {@link SplitMix64Random}, whose output
     * will in turn be used to seed this generator. This approach makes &ldquo;warmup&rdquo; unnecessary,
     * and makes the probability of starting from a state
     * with a large fraction of bits set to zero astronomically small.
     *
     * @param seed a seed for this generator.
     */
    @Override
    public void setSeed(final long seed) {
        final SplitMix64Random r = new SplitMix64Random(seed);
        s0 = r.nextLong();
        s1 = r.nextLong();
        s2 = r.nextLong();
        s3 = r.nextLong();
    }

    /**
     * Sets the state of this generator.
     *
     * <p>
     * The internal state of the generator will be reset, and the state array filled with the provided array.
     *
     * @param state an array of 4 longs; at least one must be nonzero.
     */
    public void setState(final long @NotNull [] state) {
        if (state.length != 4) throw new IllegalArgumentException(
                "The argument array contains " + state.length + " longs instead of " + 4);
        s0 = state[0];
        s1 = state[1];
        s2 = state[2];
        s3 = state[3];
    }

    /**
     * Sets the state of this generator.
     *
     * <p>
     * The internal state of the generator will be reset, and the state array filled with the provided array.
     *
     * @param state0 one of of 4 longs; at least one must be nonzero.
     * @param state1 one of of 4 longs; at least one must be nonzero.
     * @param state2 one of of 4 longs; at least one must be nonzero.
     * @param state3 one of of 4 longs; at least one must be nonzero.
     */
    public void setState(final long state0, final long state1, final long state2, final long state3) {
        s0 = state0;
        s1 = state1;
        s2 = state2;
        s3 = state3;
    }
}
