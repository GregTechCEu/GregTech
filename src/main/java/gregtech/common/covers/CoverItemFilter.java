package gregtech.common.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.ItemHandlerDelegate;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.covers.filter.ItemFilter;
import gregtech.common.covers.filter.ItemFilterWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class CoverItemFilter extends CoverBehavior implements CoverWithUI {

    protected final String titleLocale;
    protected final SimpleOverlayRenderer texture;
    protected final ItemFilterWrapper itemFilter;
    protected ItemFilterMode filterMode = ItemFilterMode.FILTER_INSERT;
    protected ItemHandlerFiltered itemHandler;

    public CoverItemFilter(ICoverable coverHolder, EnumFacing attachedSide, String titleLocale, SimpleOverlayRenderer texture, ItemFilter itemFilter) {
        super(coverHolder, attachedSide);
        this.titleLocale = titleLocale;
        this.texture = texture;
        this.itemFilter = new ItemFilterWrapper(this);
        this.itemFilter.setItemFilter(itemFilter);
        this.itemFilter.setMaxStackSize(1);
    }

    public void setFilterMode(ItemFilterMode filterMode) {
        this.filterMode = filterMode;
        coverHolder.markDirty();
    }

    public ItemFilterMode getFilterMode() {
        return filterMode;
    }

    public ItemFilterWrapper getItemFilter() {
        return this.itemFilter;
    }

    @Override
    public boolean canAttach() {
        return coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, attachedSide) != null;
    }

    @Override
    public boolean canPipePassThrough() {
        return true;
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!playerIn.world.isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    public boolean testItemStack(ItemStack stack) {
        return itemFilter.testItemStack(stack);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        WidgetGroup filterGroup = new WidgetGroup();
        filterGroup.addWidget(new LabelWidget(10, 5, titleLocale));
        filterGroup.addWidget(new CycleButtonWidget(10, 20, 110, 20,
                GTUtility.mapToString(ItemFilterMode.values(), it -> it.localeName),
                () -> filterMode.ordinal(), (newMode) -> setFilterMode(ItemFilterMode.values()[newMode])));
        this.itemFilter.initUI(45, filterGroup::addWidget);
        this.itemFilter.blacklistUI(45, filterGroup::addWidget, () -> true);
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 105 + 82)
                .widget(filterGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 105)
                .build(this, player);
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        this.texture.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("FilterMode", filterMode.ordinal());
        tagCompound.setBoolean("IsBlacklist", this.itemFilter.isBlacklistFilter());
        NBTTagCompound filterComponent = new NBTTagCompound();
        this.itemFilter.getItemFilter().writeToNBT(filterComponent);
        tagCompound.setTag("Filter", filterComponent);

        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.filterMode = ItemFilterMode.values()[tagCompound.getInteger("FilterMode")];
        this.itemFilter.setBlacklistFilter(tagCompound.getBoolean("IsBlacklist"));
        this.itemFilter.getItemFilter().readFromNBT(tagCompound.getCompoundTag("Filter"));
    }

    @Override
    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (defaultValue == null) {
                return null;
            }
            IItemHandler delegate = (IItemHandler) defaultValue;
            if (itemHandler == null || itemHandler.delegate != delegate) {
                this.itemHandler = new ItemHandlerFiltered(delegate);
            }
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
        }
        return defaultValue;
    }

    private class ItemHandlerFiltered extends ItemHandlerDelegate {

        public ItemHandlerFiltered(IItemHandler delegate) {
            super(delegate);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (getFilterMode() == ItemFilterMode.FILTER_EXTRACT || !itemFilter.testItemStack(stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (getFilterMode() != ItemFilterMode.FILTER_INSERT) {
                ItemStack result = super.extractItem(slot, amount, true);
                if (result.isEmpty() || !itemFilter.testItemStack(result)) {
                    return ItemStack.EMPTY;
                }
                return simulate ? result : super.extractItem(slot, amount, false);
            }
            return super.extractItem(slot, amount, simulate);
        }
    }
}
