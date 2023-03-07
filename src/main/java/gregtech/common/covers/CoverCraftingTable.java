package gregtech.common.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.google.common.base.Preconditions;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.storage.ICraftingStorage;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.inventory.handlers.SingleItemStackHandler;
import gregtech.common.inventory.handlers.ToolItemStackHandler;
import gregtech.common.inventory.itemsource.ItemSources;
import gregtech.common.inventory.itemsource.sources.InventoryItemSource;
import gregtech.common.metatileentities.storage.CraftingRecipeLogic;
import gregtech.common.metatileentities.storage.CraftingRecipeMemory;
import gregtech.common.metatileentities.storage.MetaTileEntityWorkbench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.metatileentity.MetaTileEntity.clearInventory;

/**
 * Code from this class is mostly copied from {@link MetaTileEntityWorkbench}
 */
public class CoverCraftingTable extends CoverBehavior implements CoverWithUI, ITickable, ICraftingStorage {

    private final ItemStackHandler internalInventory = new ItemStackHandler(18);
    private final ItemStackHandler craftingGrid = new SingleItemStackHandler(9);
    private final ItemStackHandler toolInventory = new ToolItemStackHandler(9);

    private final CraftingRecipeMemory recipeMemory = new CraftingRecipeMemory(9);
    private CraftingRecipeLogic recipeLogic = null;
    private int itemsCrafted = 0;

    public CoverCraftingTable(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return true;
    }

    @Override
    public boolean shouldAutoConnect() {
        return false;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.CRAFTING.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    private void createCraftingRecipeLogic() {
        this.recipeLogic = new CraftingRecipeLogic(this);
        this.recipeLogic.setItemsCraftedAmount(itemsCrafted);
        ItemSources itemSources = this.recipeLogic.getItemSourceList();
        itemSources.addItemHandler(new InventoryItemSource(coverHolder.getWorld(), toolInventory, -2));
        itemSources.addItemHandler(new InventoryItemSource(coverHolder.getWorld(), internalInventory, -1));
        this.recipeLogic.checkNeighbourInventories(coverHolder.getPos());
    }

    @Override
    public EnumActionResult onRightClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!playerIn.world.isRemote) {
            this.openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        return EnumActionResult.FAIL;
    }

    @Override
    public void update() {
        if (!coverHolder.getWorld().isRemote && recipeLogic == null) {
            createCraftingRecipeLogic();
        }
        if (!coverHolder.getWorld().isRemote) {
            getRecipeLogic().update();
        }
    }

    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        clearInventory(itemBuffer, internalInventory);
        clearInventory(itemBuffer, toolInventory);
    }

    private CraftingRecipeLogic getRecipeLogic() {
        Preconditions.checkState(coverHolder.getWorld() != null, "getRecipeResolver called too early");
        return recipeLogic;
    }

    @Override
    public List<ItemStack> getDrops() {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (int i = 0; i < internalInventory.getSlots(); i++) {
            itemStacks.add(internalInventory.getStackInSlot(i));
        }
        for (int i = 0; i < toolInventory.getSlots(); i++) {
            itemStacks.add(toolInventory.getStackInSlot(i));
        }
        itemStacks.add(getPickItem());

        return itemStacks;
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 221)
                .bindPlayerInventory(player.inventory, 139);
        builder.label(5, 5, "metaitem.cover.crafting.name");

        builder.widget(MetaTileEntityWorkbench.createWorkbenchTab(recipeLogic, craftingGrid, recipeMemory, toolInventory, internalInventory));

        return builder.build(this, player);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setTag("CraftingGridInventory", craftingGrid.serializeNBT());
        tagCompound.setTag("ToolInventory", toolInventory.serializeNBT());
        tagCompound.setTag("InternalInventory", internalInventory.serializeNBT());
        tagCompound.setInteger("ItemsCrafted", recipeLogic == null ? itemsCrafted : recipeLogic.getItemsCraftedAmount());
        tagCompound.setTag("RecipeMemory", recipeMemory.serializeNBT());

        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.craftingGrid.deserializeNBT(tagCompound.getCompoundTag("CraftingGridInventory"));
        this.toolInventory.deserializeNBT(tagCompound.getCompoundTag("ToolInventory"));
        this.internalInventory.deserializeNBT(tagCompound.getCompoundTag("InternalInventory"));
        this.itemsCrafted = tagCompound.getInteger("ItemsCrafted");
        this.recipeMemory.deserializeNBT(tagCompound.getCompoundTag("RecipeMemory"));
    }

    @Override
    public World getWorld() {
        return coverHolder.getWorld();
    }

    @Override
    public ItemStackHandler getCraftingGrid() {
        return craftingGrid;
    }

    @Override
    public CraftingRecipeMemory getRecipeMemory() {
        return recipeMemory;
    }
}
