/**
 * Material Properties are a replacement for the old nested hierarchy of Material Types
 * (IngotMaterial, DustMaterial, etc.).<br>
 * <br>
 * <p>
 * To create your own Material Property, it should follow this general structure:
 * 
 * <pre>
 * {@code
 *     public class MyProperty extends IMaterialProperty<MyProperty> {
 *
 *         ... private, non-final fields ...
 *
 *         public MyProperty(...) {
 *             ...
 *         }
 *
 *         // Default Value Constructor is REQUIRED!
 *         public MyProperty() {
 *
 *         }
 *
 *         ... getters and setters ...
 *
 *         public void verifyProperty(MaterialProperties properties) {
 *             // Ensure required types for THIS material are set here.
 *             // For example, IngotProperty ensures that DustProperty is
 *             // set for this Material here. Example:
 *             properties.ensureSet(PropertyKey.DUST, true);
 *
 *             // True must be set here, to re-verify the Properties once
 *             // a new Property has been added.
 *
 *
 *             // You can then check for conflicting Property keys. For example,
 *             // a Material cannot have both Item and Fluid Pipes.
 *
 *
 *             // You can also enforce properties onto other Materials. For example,
 *             // OreProperty requires that its "washedIn" Material have a FluidProperty.
 *             // Like above, ensure you call "ensureSet(Key, true)" to re-verify.
 *         }
 *     }
 * }
 * </pre>
 * 
 * <br>
 * <br>
 * <p>
 * You must also create a new {@link gregtech.api.unification.material.properties.PropertyKey} Object, like:
 * 
 * <pre>
 * {@code
 *     public static final PropertyKey<MyProperty> MY_PROPERTY = new PropertyKey<>("my_property", MyProperty.class);
 * }</code>
 *
 * @author dan
 * @since GTCEu 2.0.0
 */
package gregtech.api.unification.material.properties;
