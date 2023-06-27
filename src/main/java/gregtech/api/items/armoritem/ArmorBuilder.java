package gregtech.api.items.armoritem;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.IRarity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public abstract class ArmorBuilder<T extends IGTArmor, U extends ArmorBuilder<T, U>> {

    protected final String domain, id;

    /* IGTToolDefinition held values, extended builders do not need to access these */
    private final EntityEquipmentSlot slot;
    private final List<IArmorBehavior> behaviors = new ArrayList<>();
    private double damageAbsorption;
    private final List<DamageSource> handledUnblockableSources = new ArrayList<>();
    private String armorTexture;
    private boolean canBreakWithDamage; // todo
    private boolean isEnchantable;
    private int enchantability;
    private IRarity rarity = EnumRarity.COMMON;

    protected ArmorBuilder(@NotNull String domain, @NotNull String id, @NotNull EntityEquipmentSlot slot) {
        this.domain = domain;
        this.id = id;
        this.slot = slot;
    }

    public U behaviors(IArmorBehavior... behaviors) {
        Collections.addAll(this.behaviors, behaviors);
        return cast(this);
    }

    public U texture(String armorTexture) {
        this.armorTexture = armorTexture;
        return cast(this);
    }

    public U rarity(IRarity rarity) {
        this.rarity = rarity;
        return cast(this);
    }

    public abstract Supplier<T> supply(IGTArmorDefinition definition);

    public abstract U cast(ArmorBuilder<T, U> builder);

    public final T build() {
        // todo more here?
        return supply(buildDefinition()).get();
    }

    protected final IGTArmorDefinition buildDefinition() {
        return new IGTArmorDefinition() {
            @Override
            public EntityEquipmentSlot getEquippedSlot() {
                return slot;
            }

            @Override
            public List<IArmorBehavior> getBehaviors() {
                return behaviors;
            }

            @Override
            public double getDamageAbsorption(EntityEquipmentSlot slot, @Nullable DamageSource damageSource) {
                return damageAbsorption;
            }

            @Override
            public List<DamageSource> handledUnblockableSources() {
                return handledUnblockableSources;
            }

            @Override
            public String getArmorTexture() {
                return armorTexture;
            }

            @Override
            public boolean canBreakWithDamage() {
                return canBreakWithDamage;
            }

            @Override
            public boolean isEnchantable() {
                return isEnchantable;
            }

            @Override
            public int getEnchantability() {
                return enchantability;
            }

            @Override
            public IRarity getRarity() {
                return rarity;
            }
        };
    }
}
