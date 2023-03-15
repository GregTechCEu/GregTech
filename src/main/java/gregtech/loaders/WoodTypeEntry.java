package gregtech.loaders;

import com.google.common.base.Preconditions;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Entry for a wood type and all of its associated items
 */
public final class WoodTypeEntry {

    private final String modid;
    private final String woodName;
    private final ItemStack log;
    private final boolean removeCharcoal;
    private final ItemStack planks;
    private final String planksRecipeName;
    private final ItemStack door;
    private final String doorRecipeName;
    private final ItemStack slab;
    private final ItemStack fence;
    private final String fenceRecipeName;
    private final ItemStack fenceGate;
    private final String fenceGateRecipeName;
    private final ItemStack stairs;
    private final ItemStack boat;
    private final String boatRecipeName;

    /**
     * @see WoodTypeEntry.Builder
     */
    private WoodTypeEntry(@Nonnull String modid, @Nonnull String woodName, ItemStack log, boolean removeCharcoal,
                          @Nonnull ItemStack planks, @Nullable String planksRecipeName, @Nonnull ItemStack door,
                          @Nullable String doorRecipeName, @Nonnull ItemStack slab, @Nonnull ItemStack fence,
                          @Nullable String fenceRecipeName, @Nonnull ItemStack fenceGate, @Nullable String fenceGateRecipeName,
                          @Nonnull ItemStack stairs, @Nonnull ItemStack boat, @Nullable String boatRecipeName) {
        this.modid = modid;
        this.woodName = woodName;
        this.log = log;
        this.removeCharcoal = removeCharcoal;
        this.planks = planks;
        this.planksRecipeName = planksRecipeName;
        this.door = door;
        this.doorRecipeName = doorRecipeName;
        this.slab = slab;
        this.fence = fence;
        this.fenceRecipeName = fenceRecipeName;
        this.fenceGate = fenceGate;
        this.fenceGateRecipeName = fenceGateRecipeName;
        this.stairs = stairs;
        this.boat = boat;
        this.boatRecipeName = boatRecipeName;
    }

    @Nonnull
    public String getModid() {
        return modid;
    }

    @Nonnull
    public String getWoodName() {
        return woodName;
    }

    @Nonnull
    public ItemStack getLog() {
        return log;
    }

    /**
     * @return if log -> charcoal recipes should be removed
     */
    public boolean shouldRemoveCharcoal() {
        return removeCharcoal;
    }

    @Nonnull
    public ItemStack getPlanks() {
        return planks;
    }

    @Nullable
    public String getPlanksRecipeName() {
        return planksRecipeName;
    }

    @Nonnull
    public ItemStack getDoor() {
        return door;
    }

    @Nullable
    public String getDoorRecipeName() {
        return doorRecipeName;
    }

    @Nonnull
    public ItemStack getSlab() {
        return slab;
    }

    @Nonnull
    public ItemStack getFence() {
        return fence;
    }

    @Nullable
    public String getFenceRecipeName() {
        return fenceRecipeName;
    }

    @Nonnull
    public ItemStack getFenceGate() {
        return fenceGate;
    }

    @Nullable
    public String getFenceGateRecipeName() {
        return fenceGateRecipeName;
    }

    @Nonnull
    public ItemStack getStairs() {
        return stairs;
    }

    @Nonnull
    public ItemStack getBoat() {
        return boat;
    }

    @Nullable
    public String getBoatRecipeName() {
        return boatRecipeName;
    }

    public static class Builder {

        private final String modid;
        private final String woodName;

        private ItemStack log = ItemStack.EMPTY;
        private boolean removeCharcoal;
        private ItemStack planks = ItemStack.EMPTY;
        private String planksRecipeName;
        private ItemStack door = ItemStack.EMPTY;
        private String doorRecipeName;
        private ItemStack slab = ItemStack.EMPTY;
        private ItemStack fence = ItemStack.EMPTY;
        private String fenceRecipeName;
        private ItemStack fenceGate = ItemStack.EMPTY;
        private String fenceGateRecipeName;
        private ItemStack stairs = ItemStack.EMPTY;
        private ItemStack boat = ItemStack.EMPTY;
        private String boatRecipeName;

        /**
         * @param modid the modid adding recipes for the wood
         * @param woodName the name of the wood
         */
        public Builder(@Nonnull String modid, @Nonnull String woodName) {
            Preconditions.checkArgument(!modid.isEmpty(), "Modid cannot be empty.");
            Preconditions.checkArgument(!woodName.isEmpty(), "Wood name cannot be empty.");
            this.modid = modid;
            this.woodName = woodName;
        }

        /**
         * Add an entry for logs
         * @param log the log to add
         * @param removeCharcoal if log -> charcoal recipes should be removed if the config is enabled
         * @return this
         */
        public Builder log(@Nonnull ItemStack log, boolean removeCharcoal) {
            this.log = log;
            this.removeCharcoal = removeCharcoal;
            return this;
        }

        /**
         * Add an entry for planks
         * @param planks the planks to add
         * @param planksRecipeName the recipe for crafting the planks
         * @return this
         */
        public Builder planks(@Nonnull ItemStack planks, @Nullable String planksRecipeName) {
            this.planks = planks;
            this.planksRecipeName = planksRecipeName;
            return this;
        }

        /**
         * Add an entry for a door
         * @param door the door to add
         * @param doorRecipeName the recipe name for crafting the door
         * @return this
         */
        public Builder door(@Nonnull ItemStack door, @Nullable String doorRecipeName) {
            this.door = door;
            this.doorRecipeName = doorRecipeName;
            return this;
        }

        /**
         * Add an entry for a slab
         * @param slab the slab to add
         * @return this
         */
        public Builder slab(@Nonnull ItemStack slab) {
            this.slab = slab;
            return this;
        }

        /**
         * Add an entry for a fence
         * @param fence the fence to add
         * @param fenceRecipeName the recipe name for crafting the fence
         * @return this
         */
        public Builder fence(@Nonnull ItemStack fence, @Nullable String fenceRecipeName) {
            this.fence = fence;
            this.fenceRecipeName = fenceRecipeName;
            return this;
        }

        /**
         * Add an entry for a fence gate
         * @param fenceGate the fence gate to add
         * @param fenceGateRecipeName the recipe name for crafting the fence gate
         * @return this
         */
        public Builder fenceGate(@Nonnull ItemStack fenceGate, @Nullable String fenceGateRecipeName) {
            this.fenceGate = fenceGate;
            this.fenceGateRecipeName = fenceGateRecipeName;
            return this;
        }

        /**
         * Add an entry for stairs
         * @param stairs the stairs to add
         * @return this
         */
        public Builder stairs(@Nonnull ItemStack stairs) {
            this.stairs = stairs;
            return this;
        }

        /**
         * Add an entry for a boat
         * @param boat the boat to add
         * @param boatRecipeName the recipe name for crafting the boat
         * @return this
         */
        public Builder boat(@Nonnull ItemStack boat, @Nullable String boatRecipeName) {
            this.boat = boat;
            this.boatRecipeName = boatRecipeName;
            return this;
        }

        /**
         * @return a new wood type entry, if valid
         */
        @Nonnull
        public WoodTypeEntry build() {
            Preconditions.checkArgument(!planks.isEmpty(), "Planks cannot be empty.");
            return new WoodTypeEntry(modid, woodName, log, removeCharcoal, planks, planksRecipeName, door, doorRecipeName,
                    slab, fence, fenceRecipeName, fenceGate, fenceGateRecipeName, stairs, boat, boatRecipeName);
        }
    }
}
