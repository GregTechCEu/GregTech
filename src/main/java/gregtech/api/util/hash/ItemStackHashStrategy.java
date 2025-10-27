package gregtech.api.util.hash;

import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.Hash;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A configurable generator of hashing strategies, allowing for consideration of select properties of {@link ItemStack}s
 * when considering equality.
 */
public interface ItemStackHashStrategy extends Hash.Strategy<ItemStack> {

    static Builder builder() {
        return new Builder();
    }

    ItemStackHashStrategy comparingAll = builder()
            .compareItem()
            .compareCount()
            .compareDamage()
            .compareTag()
            .build();

    ItemStackHashStrategy comparingAllButCount = builder()
            .compareItem()
            .compareDamage()
            .compareTag()
            .build();

    ItemStackHashStrategy comparingAllButNBT = builder()
            .compareItem()
            .compareDamage()
            .compareCount()
            .build();

    class Builder {

        private boolean item, count, damage, tag, meta = false;

        public Builder compareItem() {
            item = true;
            return this;
        }

        public Builder compareCount() {
            count = true;
            return this;
        }

        public Builder compareDamage() {
            damage = true;
            return this;
        }

        public Builder compareMetadata() {
            meta = true;
            return this;
        }

        public Builder compareTag() {
            tag = true;
            return this;
        }

        public ItemStackHashStrategy build() {
            return new ItemStackHashStrategy() {

                @Override
                public int hashCode(@Nullable ItemStack o) {
                    return o == null || o.isEmpty() ? 0 : Objects.hash(
                            item ? o.getItem() : null,
                            count ? o.getCount() : null,
                            damage ? o.getItemDamage() : null,
                            tag ? o.getTagCompound() : null,
                            meta ? o.getMetadata() : null);
                }

                @Override
                public boolean equals(@Nullable ItemStack a, @Nullable ItemStack b) {
                    if (a == null || a.isEmpty()) return b == null || b.isEmpty();
                    if (b == null || b.isEmpty()) return false;

                    return (!item || a.getItem() == b.getItem()) &&
                            (!count || a.getCount() == b.getCount()) &&
                            (!damage || a.getItemDamage() == b.getItemDamage()) &&
                            (!meta || a.getMetadata() == b.getMetadata()) &&
                            (!tag || Objects.equals(a.getTagCompound(), b.getTagCompound()));
                }
            };
        }
    }
}
