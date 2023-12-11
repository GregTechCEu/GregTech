package gregtech.loaders;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;

import net.minecraft.item.ItemStack;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Entry for a wood type and all of its associated items
 */
public final class WoodTypeEntry {

    @NotNull
    public final String modid;
    @NotNull
    public final String woodName;
    @NotNull
    public final ItemStack log;
    /**
     * if log -> charcoal recipes should be removed
     */
    public final boolean removeCharcoalRecipe;
    /**
     * if log -> charcoal recipes should be added
     */
    public final boolean addCharcoalRecipe;
    @NotNull
    public final ItemStack planks;
    @Nullable
    public final String planksRecipeName;
    @NotNull
    public final ItemStack door;
    @Nullable
    public final String doorRecipeName;
    @NotNull
    public final ItemStack slab;
    @Nullable
    public final String slabRecipeName;
    public final boolean addSlabCraftingRecipe;
    public final ItemStack fence;
    @Nullable
    public final String fenceRecipeName;
    @NotNull
    public final ItemStack fenceGate;
    @Nullable
    public final String fenceGateRecipeName;
    @NotNull
    public final ItemStack stairs;
    public final boolean addStairsCraftingRecipe;
    @NotNull
    public final ItemStack boat;
    @Nullable
    public final String boatRecipeName;
    public final Material material;

    public final boolean addLogOreDict;
    public final boolean addPlanksOreDict;
    public final boolean addDoorsOreDict;
    public final boolean addSlabsOreDict;
    public final boolean addFencesOreDict;
    public final boolean addFenceGatesOreDict;
    public final boolean addStairsOreDict;
    public final boolean addPlanksUnificationInfo;
    public final boolean addDoorsUnificationInfo;
    public final boolean addSlabsUnificationInfo;
    public final boolean addFencesUnificationInfo;
    public final boolean addFenceGatesUnificationInfo;
    public final boolean addStairsUnificationInfo;
    public final boolean addBoatsUnificationInfo;

    /**
     * @see WoodTypeEntry.Builder
     */
    private WoodTypeEntry(@NotNull String modid, @NotNull String woodName, @NotNull ItemStack log,
                          boolean removeCharcoalRecipe, boolean addCharcoalRecipe, @NotNull ItemStack planks,
                          @Nullable String planksRecipeName, @NotNull ItemStack door, @Nullable String doorRecipeName,
                          @NotNull ItemStack slab, @Nullable String slabRecipeName, boolean addSlabCraftingRecipe,
                          @NotNull ItemStack fence, @Nullable String fenceRecipeName,
                          @NotNull ItemStack fenceGate, @Nullable String fenceGateRecipeName, @NotNull ItemStack stairs,
                          boolean addStairsCraftingRecipe, @NotNull ItemStack boat, @Nullable String boatRecipeName,
                          @Nullable Material material, boolean addLogOreDict, boolean addPlanksOreDict,
                          boolean addDoorsOreDict, boolean addSlabsOreDict, boolean addFencesOreDict,
                          boolean addFenceGatesOreDict, boolean addStairsOreDict, boolean addPlanksUnificationInfo,
                          boolean addDoorsUnificationInfo, boolean addSlabsUnificationInfo,
                          boolean addFencesUnificationInfo, boolean addFenceGatesUnificationInfo,
                          boolean addStairsUnificationInfo, boolean addBoatsUnificationInfo) {
        this.modid = modid;
        this.woodName = woodName;
        this.log = log;
        this.removeCharcoalRecipe = removeCharcoalRecipe;
        this.addCharcoalRecipe = addCharcoalRecipe;
        this.planks = planks;
        this.planksRecipeName = planksRecipeName;
        this.door = door;
        this.doorRecipeName = doorRecipeName;
        this.slab = slab;
        this.slabRecipeName = slabRecipeName;
        this.addSlabCraftingRecipe = addSlabCraftingRecipe;
        this.fence = fence;
        this.fenceRecipeName = fenceRecipeName;
        this.fenceGate = fenceGate;
        this.fenceGateRecipeName = fenceGateRecipeName;
        this.stairs = stairs;
        this.addStairsCraftingRecipe = addStairsCraftingRecipe;
        this.boat = boat;
        this.boatRecipeName = boatRecipeName;
        this.material = material != null ? material : Materials.Wood;

        this.addLogOreDict = addLogOreDict;
        this.addPlanksOreDict = addPlanksOreDict;
        this.addDoorsOreDict = addDoorsOreDict;
        this.addSlabsOreDict = addSlabsOreDict;
        this.addFencesOreDict = addFencesOreDict;
        this.addFenceGatesOreDict = addFenceGatesOreDict;
        this.addStairsOreDict = addStairsOreDict;
        this.addPlanksUnificationInfo = addPlanksUnificationInfo;
        this.addDoorsUnificationInfo = addDoorsUnificationInfo;
        this.addSlabsUnificationInfo = addSlabsUnificationInfo;
        this.addFencesUnificationInfo = addFencesUnificationInfo;
        this.addFenceGatesUnificationInfo = addFenceGatesUnificationInfo;
        this.addStairsUnificationInfo = addStairsUnificationInfo;
        this.addBoatsUnificationInfo = addBoatsUnificationInfo;
    }

    @NotNull
    public UnificationEntry getStick() {
        return new UnificationEntry(OrePrefix.stick, this.material);
    }

    public static class Builder {

        private final String modid;
        private final String woodName;

        private ItemStack log = ItemStack.EMPTY;
        private boolean removeCharcoalRecipe;
        private boolean addCharcoalRecipe;
        private ItemStack planks = ItemStack.EMPTY;
        private String planksRecipeName;
        private ItemStack door = ItemStack.EMPTY;
        private String doorRecipeName;
        private ItemStack slab = ItemStack.EMPTY;
        private String slabRecipeName;
        private boolean addSlabsCraftingRecipe;
        private ItemStack fence = ItemStack.EMPTY;
        private String fenceRecipeName;
        private ItemStack fenceGate = ItemStack.EMPTY;
        private String fenceGateRecipeName;
        private ItemStack stairs = ItemStack.EMPTY;
        private boolean addStairsCraftingRecipe;
        private ItemStack boat = ItemStack.EMPTY;
        private String boatRecipeName;
        @Nullable
        private Material material = null;

        private boolean addLogOreDict;
        private boolean addPlanksOreDict;
        private boolean addDoorsOreDict;
        private boolean addSlabsOreDict;
        private boolean addFencesOreDict;
        private boolean addFenceGatesOreDict;
        private boolean addStairsOreDict;

        private boolean addPlanksUnificationInfo;
        private boolean addDoorsUnificationInfo;
        private boolean addSlabsUnificationInfo;
        private boolean addFencesUnificationInfo;
        private boolean addFenceGatesUnificationInfo;
        private boolean addStairsUnificationInfo;
        private boolean addBoatsUnificationInfo;

        /**
         * @param modid    the modid adding recipes for the wood
         * @param woodName the name of the wood
         */
        public Builder(@NotNull String modid, @NotNull String woodName) {
            Preconditions.checkArgument(!modid.isEmpty(), "Modid cannot be empty.");
            Preconditions.checkArgument(!woodName.isEmpty(), "Wood name cannot be empty.");
            this.modid = modid;
            this.woodName = woodName;
        }

        /**
         * Add an entry for logs
         *
         * @param log the log to add
         * @return this
         */
        public Builder log(@NotNull ItemStack log) {
            this.log = log;
            return this;
        }

        /**
         * Remove log -> charcoal recipe if the config is enabled
         *
         * @return this
         */
        public Builder removeCharcoalRecipe() {
            this.removeCharcoalRecipe = true;
            return this;
        }

        /**
         * Add log -> charcoal recipe if the config is disabled
         *
         * @return this
         */
        public Builder addCharcoalRecipe() {
            this.addCharcoalRecipe = true;
            return this;
        }

        /**
         * Add an entry for planks
         *
         * @param planks           the planks to add
         * @param planksRecipeName the recipe for crafting the planks
         * @return this
         */
        public Builder planks(@NotNull ItemStack planks, @Nullable String planksRecipeName) {
            this.planks = planks;
            this.planksRecipeName = planksRecipeName;
            return this;
        }

        /**
         * Add an entry for a door
         *
         * @param door           the door to add
         * @param doorRecipeName the recipe name for crafting the door
         * @return this
         */
        public Builder door(@NotNull ItemStack door, @Nullable String doorRecipeName) {
            this.door = door;
            this.doorRecipeName = doorRecipeName;
            return this;
        }

        /**
         * Add an entry for a slab
         *
         * @param slab the slab to add
         * @return this
         */
        public Builder slab(@NotNull ItemStack slab, @Nullable String slabRecipeName) {
            this.slab = slab;
            this.slabRecipeName = slabRecipeName;
            return this;
        }

        /**
         * Add crafting recipe for slab
         *
         * @return this
         */
        public Builder addSlabRecipe() {
            this.addSlabsCraftingRecipe = true;
            return this;
        }

        /**
         * Add an entry for a fence
         *
         * @param fence           the fence to add
         * @param fenceRecipeName the recipe name for crafting the fence
         * @return this
         */
        public Builder fence(@NotNull ItemStack fence, @Nullable String fenceRecipeName) {
            this.fence = fence;
            this.fenceRecipeName = fenceRecipeName;
            return this;
        }

        /**
         * Add an entry for a fence gate
         *
         * @param fenceGate           the fence gate to add
         * @param fenceGateRecipeName the recipe name for crafting the fence gate
         * @return this
         */
        public Builder fenceGate(@NotNull ItemStack fenceGate, @Nullable String fenceGateRecipeName) {
            this.fenceGate = fenceGate;
            this.fenceGateRecipeName = fenceGateRecipeName;
            return this;
        }

        /**
         * Add an entry for stairs
         *
         * @param stairs the stairs to add
         * @return this
         */
        public Builder stairs(@NotNull ItemStack stairs) {
            this.stairs = stairs;
            return this;
        }

        /**
         * Add crafting recipe for stairs
         *
         * @return this
         */
        public Builder addStairsRecipe() {
            this.addStairsCraftingRecipe = true;
            return this;
        }

        /**
         * Add an entry for a boat
         *
         * @param boat           the boat to add
         * @param boatRecipeName the recipe name for crafting the boat
         * @return this
         */
        public Builder boat(@NotNull ItemStack boat, @Nullable String boatRecipeName) {
            this.boat = boat;
            this.boatRecipeName = boatRecipeName;
            return this;
        }

        /**
         * Specify material for wood entry. If not provided, {@link Materials#Wood} will be used
         *
         * @param material material for wood entry
         * @return this
         */
        public Builder material(@NotNull Material material) {
            this.material = material;
            return this;
        }

        /**
         * Register all possible ore dictionary for wood entry.
         *
         * @return this
         */
        public Builder registerAllOres() {
            return registerOre(true, true, true, true, true, true, true);
        }

        /**
         * Register all possible unification info for wood entry.
         *
         * @return this
         */
        public Builder registerAllUnificationInfo() {
            return registerUnificationInfo(true, true, true, true, true, true, true);
        }

        /**
         * Register ore dictionary for wood entry.
         *
         * @param log       whether to add ore dictionary for logs
         * @param planks    whether to add ore dictionary for planks
         * @param door      whether to add ore dictionary for doors
         * @param slab      whether to add ore dictionary for slab
         * @param fence     whether to add ore dictionary for fences
         * @param fenceGate whether to add ore dictionary for fence gates
         * @param stairs    whether to add ore dictionary for stairs
         * @return this
         */
        public Builder registerOre(boolean log, boolean planks, boolean door, boolean slab, boolean fence,
                                   boolean fenceGate, boolean stairs) {
            this.addLogOreDict = log;
            this.addPlanksOreDict = planks;
            this.addDoorsOreDict = door;
            this.addSlabsOreDict = slab;
            this.addFencesOreDict = fence;
            this.addFenceGatesOreDict = fenceGate;
            this.addStairsOreDict = stairs;
            return this;
        }

        /**
         * Register unification info for wood entry.
         *
         * @param planks    whether to add unification info for planks
         * @param door      whether to add unification info for doors
         * @param slab      whether to add unification info for slab
         * @param fence     whether to add unification info for fences
         * @param fenceGate whether to add unification info for fence gates
         * @param stairs    whether to add unification info for stairs
         * @param boat      whether to add unification info for boats
         * @return this
         */
        public Builder registerUnificationInfo(boolean planks, boolean door, boolean slab, boolean fence,
                                               boolean fenceGate, boolean stairs, boolean boat) {
            this.addPlanksUnificationInfo = planks;
            this.addDoorsUnificationInfo = door;
            this.addSlabsUnificationInfo = slab;
            this.addFencesUnificationInfo = fence;
            this.addFenceGatesUnificationInfo = fenceGate;
            this.addStairsUnificationInfo = stairs;
            this.addBoatsUnificationInfo = boat;
            return this;
        }

        /**
         * @return a new wood type entry, if valid
         */
        @NotNull
        public WoodTypeEntry build() {
            Preconditions.checkArgument(!planks.isEmpty(), "Planks cannot be empty.");
            return new WoodTypeEntry(modid, woodName, log, removeCharcoalRecipe, addCharcoalRecipe, planks,
                    planksRecipeName, door, doorRecipeName, slab, slabRecipeName, addSlabsCraftingRecipe, fence,
                    fenceRecipeName,
                    fenceGate, fenceGateRecipeName, stairs, addStairsCraftingRecipe, boat, boatRecipeName, material,
                    addLogOreDict, addPlanksOreDict, addDoorsOreDict, addSlabsOreDict, addFencesOreDict,
                    addFenceGatesOreDict, addStairsOreDict, addPlanksUnificationInfo, addDoorsUnificationInfo,
                    addSlabsUnificationInfo, addFencesUnificationInfo, addFenceGatesUnificationInfo,
                    addStairsUnificationInfo, addBoatsUnificationInfo);
        }
    }
}
