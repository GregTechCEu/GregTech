package gregtech.common.metatileentities.storage;

import gregtech.api.storage.ICraftingStorage;
import gregtech.api.util.DummyContainer;
import gregtech.api.util.GTUtility;
import gregtech.common.inventory.IItemList;
import gregtech.common.inventory.itemsource.ItemSources;
import gregtech.common.inventory.itemsource.sources.TileItemSource;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CraftingRecipeLogic {

    private final World world;
    private final ItemSources itemSources;
    private final ItemStackHandler craftingGrid;
    private final ItemStack[] oldCraftingGrid = new ItemStack[9];
    private final InventoryCrafting inventoryCrafting;
    private final IInventory craftingResultInventory = new InventoryCraftResult();
    private ItemStack oldResult = ItemStack.EMPTY;
    private final CachedRecipeData cachedRecipeData;
    private final CraftingRecipeMemory recipeMemory;
    private IRecipe cachedRecipe = null;
    private int itemsCrafted = 0;
    public static short ALL_INGREDIENTS_PRESENT = 511;
    private short tintLocation = ALL_INGREDIENTS_PRESENT;

    public CraftingRecipeLogic(ICraftingStorage craftingStorage) {
        this.world = craftingStorage.getWorld();
        this.craftingGrid = craftingStorage.getCraftingGrid();
        this.recipeMemory = craftingStorage.getRecipeMemory();
        this.itemSources = new ItemSources(world);
        this.inventoryCrafting = new CraftingWrapper(craftingGrid);
        this.cachedRecipeData = new CachedRecipeData(craftingStorage.getInventory(), null, inventoryCrafting);
    }

    public ItemSources getItemSourceList() {
        return itemSources;
    }

    public IInventory getCraftingResultInventory() {
        return craftingResultInventory;
    }

    public int getItemsCraftedAmount() {
        return itemsCrafted;
    }

    public void setItemsCraftedAmount(int itemsCrafted) {
        this.itemsCrafted = itemsCrafted;
    }

    public void clearCraftingGrid() {
//        fillCraftingGrid(Collections.emptyMap());
    }

    public void updateInventory(IItemHandler handler) {
        this.cachedRecipeData.updateInventory(handler);
    }

//    public void fillCraftingGrid(Map<Integer, ItemStack> ingredients) {
//        for (int i = 0; i < craftingGrid.getSlots(); i++) {
//            craftingGrid.setStackInSlot(i, ingredients.getOrDefault(i + 1, ItemStack.EMPTY));
//        }
//    }

    public boolean performRecipe(EntityPlayer player) {
        if (!isRecipeValid()) {
            return false;
        }
        if (cachedRecipeData.attemptMatchRecipe() != ALL_INGREDIENTS_PRESENT || !cachedRecipeData.consumeRecipeItems()) {
            return false;
        }
        ForgeHooks.setCraftingPlayer(player);
        // todo right here is where tools get damaged (in UI)
        List<ItemStack> remainingItems = cachedRecipe.getRemainingItems(inventoryCrafting);
        ForgeHooks.setCraftingPlayer(null);
        for (int i = 0; i < remainingItems.size(); i++) {
            ItemStack itemStack = remainingItems.get(i);
            if (itemStack.isEmpty()) {
                continue;
            }

            ItemStack current = inventoryCrafting.getStackInSlot(i);
            inventoryCrafting.setInventorySlotContents(i, itemStack);
            if (!cachedRecipe.matches(inventoryCrafting, itemSources.getWorld())) {
                inventoryCrafting.setInventorySlotContents(i, current);
            }

            int remainingAmount = itemStack.getCount() - itemSources.insertItem(itemStack, itemStack.getCount(), false,
                    IItemList.InsertMode.HIGHEST_PRIORITY);
            if (remainingAmount > 0) {
                itemStack.setCount(remainingAmount);
                player.addItemStackToInventory(itemStack);
                if (itemStack.getCount() > 0) {
                    player.dropItem(itemStack, false, false);
                }
            }
        }
        return true;
    }

    public void handleItemCraft(ItemStack itemStack, EntityPlayer player) {
        itemStack.onCrafting(world, player, 1);
        itemStack.getItem().onCreated(itemStack, world, player);
        // if we're not simulated, fire the event, unlock recipe and add crafted items, and play sounds
        FMLCommonHandler.instance().firePlayerCraftingEvent(player, itemStack, inventoryCrafting);

        if (cachedRecipe != null && !cachedRecipe.isDynamic()) {
            player.unlockRecipes(Lists.newArrayList(cachedRecipe));
        }
        if (cachedRecipe != null) {
            ItemStack resultStack = cachedRecipe.getCraftingResult(inventoryCrafting);
            this.itemsCrafted += resultStack.getCount();
            recipeMemory.notifyRecipePerformed(craftingGrid, resultStack);
        }
    }

//    public void refreshOutputSlot() {
//        ItemStack itemStack = ItemStack.EMPTY;
//        if (cachedRecipe != null) {
//            itemStack = cachedRecipe.getCraftingResult(inventoryCrafting);
//        }
//        this.craftingResultInventory.setInventorySlotContents(0, itemStack);
//    }

    public boolean isRecipeValid() {
        return cachedRecipeData.getRecipe() != null && cachedRecipeData.matches(inventoryCrafting, this.world);
    }

    public void updateCurrentRecipe() {
        if (!cachedRecipeData.matches(inventoryCrafting, world) ||
                !ItemStack.areItemStacksEqual(oldResult, cachedRecipe.getCraftingResult(inventoryCrafting))) {
            IRecipe newRecipe = CraftingManager.findMatchingRecipe(inventoryCrafting, world);
            this.cachedRecipe = newRecipe;
            ItemStack resultStack = ItemStack.EMPTY;
            if (newRecipe != null) {
                resultStack = newRecipe.getCraftingResult(inventoryCrafting);
                oldResult = resultStack.copy();
            }
            this.craftingResultInventory.setInventorySlotContents(0, resultStack);
            this.cachedRecipeData.setRecipe(newRecipe);
        }
    }

    public void update() {
        // update item sources every tick for fast tinting updates
        itemSources.update();
        if (getCachedRecipeData().getRecipe() != null) {
            //todo fix tint location
//            tintLocation = getCachedRecipeData().attemptMatchRecipe();
        } else {
            tintLocation = ALL_INGREDIENTS_PRESENT;
        }
    }

    public short getTintLocations() {
        return tintLocation;
    }

//    public void checkNeighbourInventories(BlockPos blockPos) {
//        for (EnumFacing side : EnumFacing.VALUES) {
//            TileItemSource itemSource = new TileItemSource(world, blockPos, side);
//            this.itemSources.addItemHandler(itemSource);
//        }
//    }

    public CachedRecipeData getCachedRecipeData() {
        return this.cachedRecipeData;
    }

    private static class CraftingWrapper extends InventoryCrafting {

        IItemHandlerModifiable craftingHandler;

        public CraftingWrapper(IItemHandlerModifiable craftingHandler) {
            super(new DummyContainer(), 3, 3);
            this.craftingHandler = craftingHandler;
        }

        @Override
        public ItemStack getStackInRowAndColumn(int row, int column) {
            int index = row + (3 * column);
            return this.craftingHandler.getStackInSlot(index);
        }

        @Override
        public ItemStack getStackInSlot(int index) {
            return craftingHandler.getStackInSlot(index);
        }

        @Override
        public void setInventorySlotContents(int index, ItemStack stack) {
            craftingHandler.setStackInSlot(index, GTUtility.copy(1, stack));
        }
    }
}
