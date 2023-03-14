package gregtech.loaders;

import com.google.common.base.Preconditions;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Entry for a wood type and all of its associated items
 */
public final class WoodTypeEntry {

    public static final Collection<WoodTypeEntry> ENTRIES = new ArrayList<>();

    private final String woodName;
    private final ItemStack planks;
    private final ItemStack log;
    private final ItemStack door;
    private final ItemStack slab;
    private final ItemStack fence;
    private final ItemStack fenceGate;
    private final ItemStack stairs;
    private final ItemStack boat;
    private final boolean removeRecipes;
    /**
     * @param woodName      the name of the wood type (e.g. "oak")
     * @param planks        the planks form, required to be non-empty
     * @param log           the log form
     * @param door          the door form
     * @param slab          the slab form
     * @param fence         the fence form
     * @param fenceGate     the fence gate form
     * @param stairs        the stairs form
     * @param boat          the boat form
     * @param removeRecipes if this entry should have recipes removed
     */
    public WoodTypeEntry(@Nonnull String woodName, @Nonnull ItemStack planks, @Nonnull ItemStack log,
                         @Nonnull ItemStack door, @Nonnull ItemStack slab, @Nonnull ItemStack fence,
                         @Nonnull ItemStack fenceGate, @Nonnull ItemStack stairs, @Nonnull ItemStack boat,
                         boolean removeRecipes) {
        Preconditions.checkArgument(!woodName.isEmpty(), "WoodTypeEntry name must be non-empty.");
        Preconditions.checkArgument(!planks.isEmpty(), "WoodTypeEntry planks must be non-empty.");
        this.woodName = woodName;
        this.planks = planks;
        this.log = log;
        this.door = door;
        this.slab = slab;
        this.fence = fence;
        this.fenceGate = fenceGate;
        this.stairs = stairs;
        this.boat = boat;
        this.removeRecipes = removeRecipes;
        ENTRIES.add(this);
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static void registerDefaultEntries() {
        new WoodTypeEntry("oak", new ItemStack(Blocks.PLANKS),
                new ItemStack(Blocks.LOG), new ItemStack(Items.OAK_DOOR),
                new ItemStack(Blocks.WOODEN_SLAB), new ItemStack(Blocks.OAK_FENCE),
                new ItemStack(Blocks.OAK_FENCE_GATE), new ItemStack(Blocks.OAK_STAIRS),
                new ItemStack(Items.BOAT), true);

        new WoodTypeEntry("spruce", new ItemStack(Blocks.PLANKS, 1, 1),
                new ItemStack(Blocks.LOG, 1, 1), new ItemStack(Items.SPRUCE_DOOR),
                new ItemStack(Blocks.WOODEN_SLAB, 1, 1), new ItemStack(Blocks.SPRUCE_FENCE),
                new ItemStack(Blocks.SPRUCE_FENCE_GATE), new ItemStack(Blocks.SPRUCE_STAIRS),
                new ItemStack(Items.SPRUCE_BOAT), true);

        new WoodTypeEntry("birch", new ItemStack(Blocks.PLANKS, 1, 2),
                new ItemStack(Blocks.LOG, 1, 2), new ItemStack(Items.BIRCH_DOOR),
                new ItemStack(Blocks.WOODEN_SLAB, 1, 2), new ItemStack(Blocks.BIRCH_FENCE),
                new ItemStack(Blocks.BIRCH_FENCE_GATE), new ItemStack(Blocks.BIRCH_STAIRS),
                new ItemStack(Items.BIRCH_BOAT), true);

        new WoodTypeEntry("jungle", new ItemStack(Blocks.PLANKS, 1, 3),
                new ItemStack(Blocks.LOG, 1, 3), new ItemStack(Items.JUNGLE_DOOR),
                new ItemStack(Blocks.WOODEN_SLAB, 1, 3), new ItemStack(Blocks.JUNGLE_FENCE),
                new ItemStack(Blocks.JUNGLE_FENCE_GATE), new ItemStack(Blocks.JUNGLE_STAIRS),
                new ItemStack(Items.JUNGLE_BOAT), true);

        new WoodTypeEntry("acacia", new ItemStack(Blocks.PLANKS, 1, 4),
                new ItemStack(Blocks.LOG2), new ItemStack(Items.ACACIA_DOOR),
                new ItemStack(Blocks.WOODEN_SLAB, 1, 4), new ItemStack(Blocks.ACACIA_FENCE),
                new ItemStack(Blocks.ACACIA_FENCE_GATE), new ItemStack(Blocks.ACACIA_STAIRS),
                new ItemStack(Items.ACACIA_BOAT), true);

        new WoodTypeEntry("dark_oak", new ItemStack(Blocks.PLANKS, 1, 5),
                new ItemStack(Blocks.LOG2, 1, 1), new ItemStack(Items.DARK_OAK_DOOR),
                new ItemStack(Blocks.WOODEN_SLAB, 1, 5), new ItemStack(Blocks.DARK_OAK_FENCE),
                new ItemStack(Blocks.DARK_OAK_FENCE_GATE), new ItemStack(Blocks.DARK_OAK_STAIRS),
                new ItemStack(Items.DARK_OAK_BOAT), true);
    }

    @Nonnull
    public String getWoodName() {
        return this.woodName;
    }

    @Nonnull
    public ItemStack getPlanks() {
        return this.planks;
    }

    @Nonnull
    public ItemStack getLog() {
        return this.log;
    }

    @Nonnull
    public ItemStack getDoor() {
        return this.door;
    }

    @Nonnull
    public ItemStack getSlab() {
        return this.slab;
    }

    @Nonnull
    public ItemStack getFence() {
        return this.fence;
    }

    @Nonnull
    public ItemStack getFenceGate() {
        return this.fenceGate;
    }

    @Nonnull
    public ItemStack getStairs() {
        return this.stairs;
    }

    @Nonnull
    public ItemStack getBoat() {
        return this.boat;
    }

    /**
     * @return if this entry should have any recipe removal performed
     */
    public boolean shouldRemoveRecipes() {
        return this.removeRecipes;
    }
}
