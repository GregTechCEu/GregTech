/**
 * This package contains {@link java.util.Random} implementations from
 * <a href="https://github.com/vigna/dsiutils">DSI Utilities</a>.
 * <p>
 * Currently, {@link gregtech.api.util.random.XoShiRo256PlusPlusRandom} is exposed as {@code public} and should be
 * superior to {@link java.util.Random} in effectively all scenarios.
 * <p>
 * Additionally, {@link gregtech.api.util.random.SplitMix64Random} is exposed for seeding
 * {@link gregtech.api.util.random.XoShiRo256PlusPlusRandom} externally. It should not be used as a general-purpose
 * PRNG.
 */
package gregtech.api.util.random;
