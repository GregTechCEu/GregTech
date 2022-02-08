package gregtech.api.unification.material.properties;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.enchantment.Enchantment;

public class ToolProperty implements IMaterialProperty<ToolProperty> {

    /**
     * Speed of tools made from this Material.
     * <p>
     * Default:
     */
    private float toolSpeed;

    /**
     * Attack damage of tools made from this Material
     * <p>
     * Default:
     */
    private float toolAttackDamage;

    /**
     * Durability of tools made from this Material.
     * <p>
     * Default:
     */
    private int toolDurability;

    /**
     * Enchantability of tools made from this Material.
     * <p>
     * Default:
     */
    private int toolEnchantability;

    /**
     * If crafting tools should not be made from this material
     */
    private boolean ignoreCraftingTools;

    /**
     * Enchantment to be applied to tools made from this Material.
     * <p>
     * Default: none.
     */
    private final Object2IntMap<Enchantment> enchantments = new Object2IntArrayMap<>();

    public ToolProperty(float toolSpeed, float toolAttackDamage, int toolDurability, int toolEnchantability, boolean ignoreCraftingTools) {
        this.toolSpeed = toolSpeed;
        this.toolAttackDamage = toolAttackDamage;
        this.toolDurability = toolDurability;
        this.toolEnchantability = toolEnchantability;
        this.ignoreCraftingTools = ignoreCraftingTools;
    }

    /**
     * Default values constructor.
     */
    public ToolProperty() {
        this(1.0f, 1.0f, 100, 10, false);
    }

    public float getToolSpeed() {
        return toolSpeed;
    }

    public void setToolSpeed(float toolSpeed) {
        if (toolSpeed <= 0) throw new IllegalArgumentException("Tool Speed must be greater than zero!");
        this.toolSpeed = toolSpeed;
    }

    public float getToolAttackDamage() {
        return toolAttackDamage;
    }

    public void setToolAttackDamage(float toolAttackDamage) {
        if (toolAttackDamage <= 0) throw new IllegalArgumentException("Tool Attack Damage must be greater than zero!");
        this.toolAttackDamage = toolAttackDamage;
    }

    public int getToolDurability() {
        return toolDurability;
    }

    public void setToolDurability(int toolDurability) {
        if (toolDurability <= 0) throw new IllegalArgumentException("Tool Durability must be greater than zero!");
        this.toolDurability = toolDurability;
    }

    public int getToolEnchantability() {
        return toolEnchantability;
    }

    public void setToolEnchantability(int toolEnchantability) {
        if (toolEnchantability <= 0) throw new IllegalArgumentException("Tool Enchantability must be greater than zero!");
        this.toolEnchantability = toolEnchantability;
    }

    public boolean getShouldIgnoreCraftingTools() {
        return ignoreCraftingTools;
    }

    public void setShouldIgnoreCraftingTools(boolean ignore) {
        this.ignoreCraftingTools = ignore;
    }

    public Object2IntMap<Enchantment> getEnchantments() {
        return enchantments;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (!properties.hasProperty(PropertyKey.GEM)) properties.ensureSet(PropertyKey.INGOT, true);
    }

    public void addEnchantmentForTools(Enchantment enchantment, int level) {
        enchantments.put(enchantment, level);
    }
}
