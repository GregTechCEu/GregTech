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
import gregtech.api.items.toolitem.ToolMetaItem;
import gregtech.api.render.Textures;
import gregtech.common.inventory.itemsource.ItemSourceList;
import gregtech.common.inventory.itemsource.sources.InventoryItemSource;
import gregtech.common.metatileentities.storage.CraftingRecipeMemory;
import gregtech.common.metatileentities.storage.CraftingRecipeResolver;
import gregtech.common.metatileentities.storage.MetaTileEntityWorkbench;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

import static gregtech.api.metatileentity.MetaTileEntity.clearInventory;

/**
 * Code from this class is mostly copied from {@link MetaTileEntityWorkbench}
 */
public class CoverCraftingTable extends CoverBehavior implements CoverWithUI, ITickable {

    private final ItemStackHandler internalInventory = new ItemStackHandler(18);

    private final ItemStackHandler craftingGrid = new ItemStackHandler(9) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    private final ItemStackHandler toolInventory = new ItemStackHandler(9) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (!(stack.getItem() instanceof ToolMetaItem) &&
                    !(stack.getItem() instanceof ItemTool) &&
                    !(stack.isItemStackDamageable())) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }
    };

    private final CraftingRecipeMemory recipeMemory = new CraftingRecipeMemory(9);
    private CraftingRecipeResolver recipeResolver = null;
    private int itemsCrafted = 0;

    public CoverCraftingTable(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return true;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.CRAFTING.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    private void createRecipeResolver() {
        this.recipeResolver = new CraftingRecipeResolver(coverHolder.getWorld(), craftingGrid, recipeMemory);
        this.recipeResolver.setItemsCrafted(itemsCrafted);
        ItemSourceList itemSourceList = this.recipeResolver.getItemSourceList();
        itemSourceList.addItemHandler(InventoryItemSource.direct(coverHolder.getWorld(), toolInventory, -2));
        itemSourceList.addItemHandler(InventoryItemSource.direct(coverHolder.getWorld(), internalInventory, -1));
        this.recipeResolver.checkNeighbourInventories(coverHolder.getPos());
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
        if (!coverHolder.getWorld().isRemote && recipeResolver == null) {
            createRecipeResolver();
        }
        if (!coverHolder.getWorld().isRemote) {
            getRecipeResolver().update();
        }
    }

    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        clearInventory(itemBuffer, internalInventory);
        clearInventory(itemBuffer, toolInventory);
    }

    private CraftingRecipeResolver getRecipeResolver() {
        Preconditions.checkState(coverHolder.getWorld() != null, "getRecipeResolver called too early");
        return recipeResolver;
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 221)
                .bindPlayerInventory(player.inventory, 140);
        builder.label(5, 5, I18n.format("metaitem.cover.crafting.name"));

        builder.widget(MetaTileEntityWorkbench.createWorkbenchTab(getRecipeResolver(), craftingGrid, recipeMemory, toolInventory, internalInventory));

        return builder.build(this, player);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setTag("CraftingGridInventory", craftingGrid.serializeNBT());
        tagCompound.setTag("ToolInventory", toolInventory.serializeNBT());
        tagCompound.setTag("InternalInventory", internalInventory.serializeNBT());
        tagCompound.setInteger("ItemsCrafted", recipeResolver == null ? itemsCrafted : recipeResolver.getItemsCrafted());
        tagCompound.setTag("RecipeMemory", recipeMemory.serializeNBT());
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
}
