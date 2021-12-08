package gregtech.common.items.behaviors;

import codechicken.lib.raytracer.RayTracer;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntityClipboard;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import static gregtech.common.blocks.MetaBlocks.MACHINE;
import static gregtech.common.metatileentities.MetaTileEntities.CLIPBOARD_TILE;

public class ClipboardBehavior implements IItemBehaviour, ItemUIFactory {
    public static final int MAX_PAGES = 25;

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 170, 238);
        initNBT(holder.getCurrentItem());

        builder.widget(new ImageTextFieldWidget(20, 10, 130, 12, GuiTextures.CLIPBOARD_TEXT_BOX,
                () -> getTitle(holder), (x) -> setTitle(holder, x), 23, 0xFFFFFF)
                .setValidator((x) -> true)
                .doesClientCallback(true));

        for (int i = 0; i < 8; i++) {
            int finalI = i;
            builder.widget(new ImageCycleButtonWidget(6, 37 + 20 * i, 15, 15, GuiTextures.CLIPBOARD_BUTTON, 4,
                    () -> getButtonState(holder, finalI), (x) -> setButton(holder, finalI, x)));
            builder.widget(new ImageTextFieldWidget(24, 40 + 20 * i, 140, 12, GuiTextures.CLIPBOARD_TEXT_BOX,
                    () -> getString(holder, finalI), (x) -> setString(holder, finalI, x), 23, 0xFFFFFF)
                    .setValidator((x) -> true)
                    .doesClientCallback(true));
        }

        builder.widget(new ClickButtonWidget(30, 200, 16, 16, "", (x) -> incrPageNum(holder, -1))
                .setButtonTexture(GuiTextures.BUTTON_LEFT).setShouldClientCallback(true));
        builder.widget(new ClickButtonWidget(124, 200, 16, 16, "", (x) -> incrPageNum(holder, 1))
                .setButtonTexture(GuiTextures.BUTTON_RIGHT).setShouldClientCallback(true));
        builder.widget(new SimpleTextWidget(85, 208, "", 0xFFFFFF,
                () -> (getPageNum(holder) + 1) + " / " + MAX_PAGES, true));

        return builder.build(holder, entityPlayer);
    }

    public ModularUI createMTEUI(PlayerInventoryHolder holder, EntityPlayer entityPlayer) { // So that people don't click on any text fields
        initNBT(holder.getCurrentItem());
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 170, 238);

        builder.image(18, 8, 130, 14, GuiTextures.CLIPBOARD_TEXT_BOX);
        builder.widget(new SimpleTextWidget(20, 10, "", 0xFFFFFF, () -> getTitle(holder), false).setCenter(false));


        for (int i = 0; i < 8; i++) {
            int finalI = i;
            builder.widget(new ImageCycleButtonWidget(6, 37 + 20 * i, 15, 15, GuiTextures.CLIPBOARD_BUTTON, 4,
                    () -> getButtonState(holder, finalI), (x) -> setButton(holder, finalI, x)));
            builder.image(22, 38 + 20 * i, 140, 12, GuiTextures.CLIPBOARD_TEXT_BOX);
            builder.widget(new SimpleTextWidget(24, 40 + 20 * i, "", 0xFFFFFF, () -> getString(holder, finalI), false).setCenter(false));
        }

        builder.widget(new ClickButtonWidget(30, 200, 16, 16, "", (x) -> incrPageNum(holder, -1))
                .setButtonTexture(GuiTextures.BUTTON_LEFT).setShouldClientCallback(true));
        builder.widget(new ClickButtonWidget(124, 200, 16, 16, "", (x) -> incrPageNum(holder, 1))
                .setButtonTexture(GuiTextures.BUTTON_RIGHT).setShouldClientCallback(true));
        builder.widget(new SimpleTextWidget(85, 208, "", 0xFFFFFF,
                () -> (getPageNum(holder) + 1) + " / " + MAX_PAGES, true));

        return builder.build(holder, entityPlayer);
    }


    private static NBTTagCompound getPageCompound(ItemStack stack) {
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return null;
        assert stack.getTagCompound() != null;
        short pageNum = stack.getTagCompound().getShort("PageIndex");
        return stack.getTagCompound().getCompoundTag("Page" + pageNum);
    }

    private static void setPageCompound(ItemStack stack, NBTTagCompound pageCompound) {
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return;
        assert stack.getTagCompound() != null;
        short pageNum = stack.getTagCompound().getShort("PageIndex");
        stack.getTagCompound().setTag("Page" + pageNum, pageCompound);
    }

    private static void initNBT(ItemStack stack) {
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return;
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            tagCompound.setShort("PageIndex", (short) 0);
            tagCompound.setShort("TotalPages", (short) 0);

            NBTTagCompound pageCompound = new NBTTagCompound();
            pageCompound.setShort("ButStat", (short) 0);
            pageCompound.setString("Title", "");
            for (int i = 0; i < 8; i++) {
                pageCompound.setString("Task" + i, "");
            }

            for (int i = 0; i < MAX_PAGES; i++) {
                tagCompound.setTag("Page" + i, pageCompound.copy());
            }

            stack.setTagCompound(tagCompound);
        }
    }

    private static void setButton(PlayerInventoryHolder holder, int pos, int newState) {
        ItemStack stack = holder.getCurrentItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
           return;
        NBTTagCompound tagCompound = getPageCompound(stack);
        short buttonState;
        buttonState = tagCompound.getShort("ButStat");

        short clearedState = (short) (buttonState & ~(3 << (pos * 2))); // Clear out the desired slot
        buttonState = (short) (clearedState | (newState << (pos * 2))); // And add the new state back in

        tagCompound.setShort("ButStat", buttonState);
        setPageCompound(stack, tagCompound);
    }

    private static int getButtonState(PlayerInventoryHolder holder, int pos) {
        ItemStack stack = holder.getCurrentItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return 0;
        NBTTagCompound tagCompound = getPageCompound(stack);
        short buttonState;
        buttonState = tagCompound.getShort("ButStat");
        return ((buttonState >> pos * 2) & 3);
    }

    private static void setString(PlayerInventoryHolder holder, int pos, String newString) {
        ItemStack stack = holder.getCurrentItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return;
        NBTTagCompound tagCompound = getPageCompound(stack);
        tagCompound.setString("Task" + pos, newString);
        setPageCompound(stack, tagCompound);
    }

    private static String getString(PlayerInventoryHolder holder, int pos) {
        ItemStack stack = holder.getCurrentItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return "";
        NBTTagCompound tagCompound = getPageCompound(stack);
        return tagCompound.getString("Task" + pos);
    }

    private static void setTitle(PlayerInventoryHolder holder, String newString) {
        ItemStack stack = holder.getCurrentItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return;
        NBTTagCompound tagCompound = getPageCompound(stack);
        assert tagCompound != null;
        tagCompound.setString("Title", newString);
        setPageCompound(stack, tagCompound);
    }

    private static String getTitle(PlayerInventoryHolder holder) {
        ItemStack stack = holder.getCurrentItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return "";
        NBTTagCompound tagCompound = getPageCompound(stack);
        return tagCompound.getString("Title");
    }

    private static int getPageNum(PlayerInventoryHolder holder) {
        ItemStack stack = holder.getCurrentItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return 1;
        NBTTagCompound tagCompound = stack.getTagCompound();
        return tagCompound.getInteger("PageIndex");
    }

    private static void incrPageNum(PlayerInventoryHolder holder, int increment) {
        ItemStack stack = holder.getCurrentItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return;
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
        if (!world.isRemote && RayTracer.retrace(player).typeOfHit != RayTraceResult.Type.BLOCK) { // So that the player doesn't place a clipboard before suddenly getting the GUI
            PlayerInventoryHolder holder = new PlayerInventoryHolder(player, hand);
            holder.openUI();
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, heldItem);
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!world.isRemote) {
            ItemStack heldItem = player.getHeldItem(hand).copy();
            heldItem.setCount(1); // don't place multiple items at a time
            EnumFacing playerFacing = player.getHorizontalFacing();
            // Make sure it's the right block
            Block testBlock = world.getBlockState(pos).getBlock();
            IBlockState testState = testBlock.getDefaultState();
            if (!testBlock.isAir(world.getBlockState(pos), world, pos) && testState.isSideSolid(world, pos, playerFacing)) {
                // Step away from the block so you don't replace it, and then give it our fun blockstate
                BlockPos shiftedPos = pos.offset(playerFacing.getOpposite());
                Block shiftedBlock = world.getBlockState(shiftedPos).getBlock();
                if (shiftedBlock.isAir(world.getBlockState(shiftedPos), world, shiftedPos)) {
                    IBlockState state = MACHINE.getDefaultState();
                    world.setBlockState(shiftedPos, state);
                    // Get new TE
                    shiftedBlock.createTileEntity(world, state);
                    // And manipulate it to our liking
                    MetaTileEntityHolder holder = (MetaTileEntityHolder) world.getTileEntity(shiftedPos);
                    if (holder != null) {
                        MetaTileEntityClipboard clipboard = (MetaTileEntityClipboard) holder.setMetaTileEntity(CLIPBOARD_TILE, heldItem);
                        if (clipboard != null) {
                            clipboard.setFrontFacing(playerFacing);
                            clipboard.setClipboard(heldItem);
                            ItemStack returnedStack = player.getHeldItem(hand);
                            if (!player.isCreative()) {
                                returnedStack.setCount(player.getHeldItem(hand).getCount() - 1);
                            }
                            return ActionResult.newResult(EnumActionResult.SUCCESS, returnedStack);
                        }
                    }
                }
            }
        }
        return ActionResult.newResult(EnumActionResult.FAIL, player.getHeldItem(hand));
    }
}
