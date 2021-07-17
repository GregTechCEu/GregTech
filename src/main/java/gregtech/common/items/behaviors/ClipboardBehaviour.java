package gregtech.common.items.behaviors;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.gui.widgets.TextFieldWidget;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.common.items.MetaItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ClipboardBehaviour implements IItemBehaviour, ItemUIFactory {
    public static final int MAX_PAGES = 25;

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer entityPlayer) {
        initNBT(holder.getSampleItem());
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 170, 238);

        builder.widget(new TextFieldWidget(20, 10, 130, 13, true,
                () -> getTitle(holder), (x) -> setTitle(holder, x), 23, 12)
                .setValidator((x) -> true));

        for (int i = 0; i < 8; i++) {
            int finalI = i;
            builder.widget(new ImageCycleButtonWidget(5, 37 + 20 * i, 15, 15, GuiTextures.CLIPBOARD_CHECKBOX, 2,
                    () -> getButtonState(holder, finalI), (x) -> toggleButton(holder, finalI)));
            builder.widget(new TextFieldWidget(21, 40 + 20 * i, 140, 10, true,
                    () -> getString(holder, finalI), (x) -> setString(holder, finalI, x), 23, 8)
                    .setValidator((x) -> true));
        }

        builder.widget(new ClickButtonWidget(30, 200, 16, 16, "", (x) -> incrPageNum(holder, -1))
                .setButtonTexture(GuiTextures.BUTTON_LEFT));
        builder.widget(new ClickButtonWidget(124, 200, 16, 16, "", (x) -> incrPageNum(holder, 1))
                .setButtonTexture(GuiTextures.BUTTON_RIGHT));
        builder.widget(new SimpleTextWidget(85, 208, "", 0x000000,
                () -> (getPageNum(holder) + 1) + " / " + MAX_PAGES));

        return builder.build(holder, entityPlayer);
    }

    private static NBTTagCompound getPageCompound(ItemStack stack) {
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            throw new IllegalArgumentException("Given item stack is not a clipboard!");
        assert stack.getTagCompound() != null;
        short pageNum = stack.getTagCompound().getShort("PageIndex");
        return stack.getTagCompound().getCompoundTag("Page" + pageNum);
    }

    private static void setPageCompound(ItemStack stack, NBTTagCompound pageCompound) {
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            throw new IllegalArgumentException("Given item stack is not a clipboard!");
        assert stack.getTagCompound() != null;
        short pageNum = stack.getTagCompound().getShort("PageIndex");
        stack.getTagCompound().setTag("Page" + pageNum, pageCompound);
    }

    private static void initNBT(ItemStack stack) {
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            throw new IllegalArgumentException("Given item stack is not a clipboard!");
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            tagCompound.setShort("PageIndex", (short) 0);
            tagCompound.setShort("TotalPages", (short) 0);

            NBTTagCompound pageCompound = new NBTTagCompound();
            pageCompound.setShort("ButStat", (short) 0);
            pageCompound.setString("PageTitle", "");
            for (int i = 0; i < 8; i++) {
                pageCompound.setString("Task" + i, "");
            }

            for (int i = 0; i < MAX_PAGES; i++) {
                tagCompound.setTag("Page" + i, pageCompound.copy());
            }

            stack.setTagCompound(tagCompound);
        }
    }

    private static void toggleButton(PlayerInventoryHolder holder, int pos) {
        ItemStack stack = holder.getSampleItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            throw new IllegalArgumentException("Given item stack is not a clipboard!");
        NBTTagCompound tagCompound = getPageCompound(stack);
        short buttonState;
        buttonState = tagCompound.getByte("ButStat");
        buttonState ^= 1 << pos;
        tagCompound.setShort("ButStat", buttonState);
        setPageCompound(stack, tagCompound);
    }

    private static boolean getButtonState(PlayerInventoryHolder holder, int pos) {
        ItemStack stack = holder.getSampleItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            throw new IllegalArgumentException("Given item stack is not a clipboard!");
        NBTTagCompound tagCompound = getPageCompound(stack);
        short buttonState = 0;
        buttonState = tagCompound.getShort("ButStat");
        return ((buttonState >> pos) & 1) != 0;
    }

    private static void setString(PlayerInventoryHolder holder, int pos, String newString) {
        ItemStack stack = holder.getSampleItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            throw new IllegalArgumentException("Given item stack is not a clipboard!");
        NBTTagCompound tagCompound = getPageCompound(stack);
        tagCompound.setString("Task" + pos, newString);
        setPageCompound(stack, tagCompound);
    }

    private static String getString(PlayerInventoryHolder holder, int pos) {
        ItemStack stack = holder.getSampleItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            throw new IllegalArgumentException("Given item stack is not a clipboard!");
        NBTTagCompound tagCompound = getPageCompound(stack);
        return tagCompound.getString("Task" + pos);
    }

    private static void setTitle(PlayerInventoryHolder holder, String newString) {
        ItemStack stack = holder.getSampleItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            throw new IllegalArgumentException("Given item stack is not a clipboard!");
        NBTTagCompound tagCompound = getPageCompound(stack);
        assert tagCompound != null;
        tagCompound.setString("Title", newString);
        setPageCompound(stack, tagCompound);
    }

    private static String getTitle(PlayerInventoryHolder holder) {
        ItemStack stack = holder.getSampleItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            throw new IllegalArgumentException("Given item stack is not a clipboard!");
        NBTTagCompound tagCompound = getPageCompound(stack);
        return tagCompound.getString("Title");
    }

    private static int getPageNum(PlayerInventoryHolder holder) {
        ItemStack stack = holder.getSampleItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            throw new IllegalArgumentException("Given item stack is not a clipboard!");
        NBTTagCompound tagCompound = stack.getTagCompound();
        return tagCompound.getInteger("PageIndex");
    }

    private static void incrPageNum(PlayerInventoryHolder holder, int increment) {
        ItemStack stack = holder.getSampleItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            throw new IllegalArgumentException("Given item stack is not a clipboard!");
        NBTTagCompound tagCompound = stack.getTagCompound();
        assert tagCompound != null;

        int currentIndex = tagCompound.getInteger("PageIndex");
        // Clamps currentIndex between 0 and MAX_PAGES.
        tagCompound.setInteger("PageIndex", Math.max(Math.min(currentIndex + increment, MAX_PAGES - 1), 0));
        stack.setTagCompound(tagCompound);
    }


    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!world.isRemote) {
            PlayerInventoryHolder holder = new PlayerInventoryHolder(player, hand);
            holder.openUI();
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, heldItem);
    }
}
