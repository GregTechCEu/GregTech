package gregtech.common.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.common.widget.CycleButtonWidget;
import com.cleanroommc.modularui.common.widget.TextWidget;
import gregtech.api.capability.impl.ItemHandlerDelegate;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GregTechUI;
import gregtech.api.gui.GuiFunctions;
import gregtech.api.gui.GuiTextures;
import gregtech.api.guiOld.ModularUI;
import gregtech.api.guiOld.widgets.LabelWidget;
import gregtech.api.guiOld.widgets.WidgetGroup;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.covers.newFilter.item.ItemFilter;
import net.minecraft.entity.player.EntityPlayer;
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
    protected final ItemFilter itemFilter;
    protected ItemFilterMode filterMode = ItemFilterMode.FILTER_INSERT;
    protected ItemHandlerFiltered itemHandler;

    public CoverItemFilter(ICoverable coverHolder, EnumFacing attachedSide, String titleLocale, SimpleOverlayRenderer texture, ItemFilter itemFilter) {
        super(coverHolder, attachedSide);
        this.titleLocale = titleLocale;
        this.texture = texture;
        this.itemFilter = itemFilter;
        //this.itemFilter.setItemFilter(itemFilter);
        this.itemFilter.setMaxStackSize(1);
    }

    protected void setFilterMode(ItemFilterMode filterMode) {
        this.filterMode = filterMode;
        coverHolder.markDirty();
    }

    public ItemFilterMode getFilterMode() {
        return filterMode;
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
            //openUI((EntityPlayerMP) playerIn);
            GregTechUI.getCoverUi(attachedSide).open(playerIn, coverHolder.getWorld(), coverHolder.getPos());
        }
        return EnumActionResult.SUCCESS;
    }

    public boolean testItemStack(ItemStack stack) {
        return itemFilter.matches(stack);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        WidgetGroup filterGroup = new WidgetGroup();
        filterGroup.addWidget(new LabelWidget(10, 5, titleLocale));
        filterGroup.addWidget(new gregtech.api.guiOld.widgets.CycleButtonWidget(10, 20, 110, 20,
                GTUtility.mapToString(ItemFilterMode.values(), it -> it.localeName),
                () -> filterMode.ordinal(), (newMode) -> setFilterMode(ItemFilterMode.values()[newMode])));
        //this.itemFilter.initUI(45, filterGroup::addWidget);

        return ModularUI.builder(gregtech.api.guiOld.GuiTextures.BACKGROUND, 176, 105 + 82)
                .widget(filterGroup)
                .bindPlayerInventory(player.inventory, gregtech.api.guiOld.GuiTextures.SLOT, 7, 105)
                .build(this, player);
    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        return ModularWindow.builder(176, 166)
                .setBackground(GuiTextures.BACKGROUND)
                .bindPlayerInventory(buildContext.getPlayer(), new Pos2d(7, 83))
                .widget(new TextWidget(new Text(titleLocale).localise())
                        .setPos(10, 5))
                .widget(new CycleButtonWidget()
                        .setForEnum(ItemFilterMode.class, this::getFilterMode, this::setFilterMode)
                        .setTextureGetter(GuiFunctions.enumStringTextureGetter(ItemFilterMode.class))
                        .setBackground(GuiTextures.BASE_BUTTON)
                        .setPos(10, 14)
                        .setSize(110, 20))
                .widget(itemFilter.createBlacklistButton(buildContext)
                        .setPos(133, 40))
                .widget(itemFilter.createFilterUI(buildContext)
                        .setPos(7, 40))
                .build();
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        this.texture.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("FilterMode", filterMode.ordinal());
        NBTTagCompound filterComponent = new NBTTagCompound();
        this.itemFilter.writeToNBT(filterComponent);
        tagCompound.setTag("Filter", filterComponent);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.filterMode = ItemFilterMode.values()[tagCompound.getInteger("FilterMode")];
        this.itemFilter.readFromNBT(tagCompound.getCompoundTag("Filter"));
        // legacy
        if (tagCompound.hasKey("IsBlacklist")) {
            this.itemFilter.setInverted(tagCompound.getBoolean("IsBlacklist"));
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
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
            if (getFilterMode() == ItemFilterMode.FILTER_EXTRACT || !itemFilter.matches(stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (getFilterMode() != ItemFilterMode.FILTER_INSERT) {
                ItemStack result = super.extractItem(slot, amount, true);
                if (result.isEmpty() || !itemFilter.matches(result)) {
                    return ItemStack.EMPTY;
                }
                return simulate ? result : super.extractItem(slot, amount, false);
            }
            return super.extractItem(slot, amount, simulate);
        }
    }
}
