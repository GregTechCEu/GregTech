package gregtech.api.storage;

import gregtech.common.metatileentities.storage.CraftingRecipeMemory;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

public interface ICraftingStorage {
    World getWorld();

    ItemStackHandler getCraftingGrid();

    CraftingRecipeMemory getRecipeMemory();
}
