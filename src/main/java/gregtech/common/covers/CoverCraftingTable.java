package gregtech.common.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.google.common.base.Preconditions;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.metatileentity.MetaTileEntity.clearInventory;

/**
 * Code from this class is mostly copied from {@link MetaTileEntityWorkbench}
 */
public class CoverCraftingTable extends CoverBase implements CoverWithUI, ITickable, ICraftingStorage {

    private final ItemStackHandler internalInventory = new ItemStackHandler(18);
    private final ItemStackHandler craftingGrid = new SingleItemStackHandler(9);
    private final ItemStackHandler toolInventory = new ToolItemStackHandler(9);

    private final CraftingRecipeMemory recipeMemory = new CraftingRecipeMemory(9);
    private CraftingRecipeLogic recipeLogic = null;
    private int itemsCrafted = 0;

    public CoverCraftingTable(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView, @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return true;
    }

    @Override
    public boolean shouldAutoConnectToPipes() {
        return false;
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation, IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        Textures.CRAFTING.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    private void createCraftingRecipeLogic() {
        this.recipeLogic = new CraftingRecipeLogic(this);
        this.recipeLogic.setItemsCraftedAmount(itemsCrafted);
        ItemSources itemSources = this.recipeLogic.getItemSourceList();
        itemSources.addItemHandler(new InventoryItemSource(getWorld(), toolInventory, -2));
        itemSources.addItemHandler(new InventoryItemSource(getWorld(), internalInventory, -1));
        this.recipeLogic.checkNeighbourInventories(getPos());
    }

    @Override
    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand, @NotNull CuboidRayTraceResult hitResult) {
        if (!playerIn.world.isRemote) {
            this.openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public @UnknownNullability World getWorld() {
        // this override is needed
        return super.getWorld();
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            if (recipeLogic == null) createCraftingRecipeLogic();
            getRecipeLogic().update();
        }
    }

    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        clearInventory(itemBuffer, internalInventory);
        clearInventory(itemBuffer, toolInventory);
    }

    private CraftingRecipeLogic getRecipeLogic() {
        Preconditions.checkState(getCoverable().getWorld() != null, "getRecipeResolver called too early");
        return recipeLogic;
    }

    @Override
    public @NotNull List<ItemStack> getDrops() {
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
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setTag("CraftingGridInventory", craftingGrid.serializeNBT());
        tagCompound.setTag("ToolInventory", toolInventory.serializeNBT());
        tagCompound.setTag("InternalInventory", internalInventory.serializeNBT());
        tagCompound.setInteger("ItemsCrafted", recipeLogic == null ? itemsCrafted : recipeLogic.getItemsCraftedAmount());
        tagCompound.setTag("RecipeMemory", recipeMemory.serializeNBT());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.craftingGrid.deserializeNBT(tagCompound.getCompoundTag("CraftingGridInventory"));
        this.toolInventory.deserializeNBT(tagCompound.getCompoundTag("ToolInventory"));
        this.internalInventory.deserializeNBT(tagCompound.getCompoundTag("InternalInventory"));
        this.itemsCrafted = tagCompound.getInteger("ItemsCrafted");
        this.recipeMemory.deserializeNBT(tagCompound.getCompoundTag("RecipeMemory"));
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
