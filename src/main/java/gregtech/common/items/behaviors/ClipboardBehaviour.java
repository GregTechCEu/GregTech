package gregtech.common.items.behaviors;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.TextFieldWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
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
        initNBT(holder.getCurrentItem());
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 170, 238);
        for (int i = 0; i < 8; i++) {
            int finalI = i;
            builder.widget(new ImageCycleButtonWidget(5, 27 + 15 * i, 15, 15, GuiTextures.CLIPBOARD_CHECKBOX, 2,
                    () -> getButtonState(holder, finalI), (x) -> toggleButton(holder, finalI)));
            builder.widget(new TextFieldWidget(21, 30 + 15 * i, 140, 10, true,
                    () -> getString(holder, finalI), (x) -> setString(holder, finalI, x), 23, 8)
            .setValidator((x) -> true));
        }

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
            for(int i = 0; i < 8; i++) {
                pageCompound.setString("Task" + i, "");
            }

            for(int i = 0; i < MAX_PAGES; i++) {
                tagCompound.setTag("Page" + i, pageCompound.copy());
            }

            stack.setTagCompound(tagCompound);
        }
    }

    private static void toggleButton(PlayerInventoryHolder holder, int pos) {
        ItemStack stack = holder.getCurrentItem();
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
        ItemStack stack = holder.getCurrentItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            throw new IllegalArgumentException("Given item stack is not a clipboard!");
        NBTTagCompound tagCompound = getPageCompound(stack);
        short buttonState = 0;
        buttonState = tagCompound.getShort("ButStat");
        return ((buttonState >> pos) & 1) != 0;
    }

    private static void setString(PlayerInventoryHolder holder, int pos, String newString) {
        ItemStack stack = holder.getCurrentItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            throw new IllegalArgumentException("Given item stack is not a clipboard!");
        NBTTagCompound tagCompound = getPageCompound(stack);
        tagCompound.setString("Task" + pos, newString);
        setPageCompound(stack, tagCompound);
    }

    private static String getString(PlayerInventoryHolder holder, int pos) {
        ItemStack stack = holder.getCurrentItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            throw new IllegalArgumentException("Given item stack is not a clipboard!");
        NBTTagCompound tagCompound = getPageCompound(stack);
        return tagCompound.getString("Task" + pos);
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
