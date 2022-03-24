package gregtech.common.items.behaviors;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.util.GTTransferUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemMagnetBehavior implements IItemBehaviour {

    private final int range;
    private final float speed;

    public ItemMagnetBehavior(int range, float speed) {
        this.range = range;
        this.speed = speed;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, @Nonnull EntityPlayer player, EnumHand hand) {
        if (!player.world.isRemote && player.isSneaking()) {
            player.sendStatusMessage(new TextComponentTranslation(toggleActive(player.getHeldItem(hand)) ? "behavior.item_magnet.enabled" : "behavior.item_magnet.disabled"), true);
        }
        return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    private boolean isActive(ItemStack stack) {
        if (stack == ItemStack.EMPTY) {
            return false;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            return false;
        }
        if (tag.hasKey("IsActive")) {
            return tag.getBoolean("IsActive");
        }
        return false;
    }

    private boolean toggleActive(ItemStack stack) {
        boolean isActive = isActive(stack);
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        //noinspection ConstantConditions
        stack.getTagCompound().setBoolean("IsActive", !isActive);
        return !isActive;
    }

    @Override
    public void onUpdate(ItemStack itemStack, Entity entity) {
        if (!isActive(itemStack) || !(entity instanceof EntityPlayer) || entity.getEntityWorld().isRemote)
            return;

        EntityPlayer entityPlayer = (EntityPlayer) entity;

        if (entityPlayer.isSpectator()) {
            return;
        }

        List<EntityItem> itemsInRange = entityPlayer.getEntityWorld().getEntitiesWithinAABB(EntityItem.class, getAreaBoundingBox(entityPlayer));
        for (EntityItem entityItem : itemsInRange) {
            if (entityItem.isDead) continue;

            double distanceX = (entityPlayer.getPosition().getX() + 0.5) - entityItem.posX;
            double distanceY = (entityPlayer.getPosition().getY() + 0.5) - entityItem.posY;
            double distanceZ = (entityPlayer.getPosition().getZ() + 0.5) - entityItem.posZ;
            double distance = MathHelper.sqrt(distanceX * distanceX + distanceZ * distanceZ);
            if (distance >= 0.7) {
                if (!entityItem.cannotPickup()) {
                    if (!drainEnergy(true, itemStack, (long) distance))
                        return;

                    drainEnergy(false, itemStack, (long) distance);

                    double directionX = distanceX / distance;
                    double directionY = distanceY / distance;
                    double directionZ = distanceZ / distance;
                    entityItem.motionX = directionX * speed * 8;
                    entityItem.motionY = directionY * speed * 8;
                    entityItem.motionZ = directionZ * speed * 8;
                    entityItem.velocityChanged = true;
                    entityItem.setNoPickupDelay();
                }
            } else if (!entityItem.cannotPickup()) {
                ItemStack stack = entityItem.getItem();

                ItemStack remainder = GTTransferUtils.insertItem(new ItemStackHandler(entityPlayer.inventory.mainInventory), stack, false);
                if (remainder.isEmpty()) {
                    entityItem.setDead();
                } else if (stack.getCount() > remainder.getCount()) {
                    entityItem.setItem(remainder);
                }
            }
        }
    }

    @SubscribeEvent
    public void onItemToss(@Nonnull ItemTossEvent event) {
        if (event.getPlayer() == null)
            return;

        ItemStack stack = event.getEntityItem().getItem();
        if (isMagnet(stack)) {
            return;
        }

        for (ItemStack itemStack : event.getPlayer().inventory.mainInventory) {
            if (isMagnet(itemStack) && isActive(itemStack)) {
                event.getEntityItem().setPickupDelay(60);
                return;
            }
        }
        if (isMagnet(event.getPlayer().inventory.offHandInventory.get(0)) && isActive(event.getPlayer().inventory.offHandInventory.get(0))) {
            event.getEntityItem().setPickupDelay(60);
        }
    }

    private boolean isMagnet(@Nonnull ItemStack stack) {
        return stack.getItem() instanceof MetaItem && ((MetaItem<?>) stack.getItem()).getBehaviours(stack).contains(this);
    }

    @Nonnull
    private AxisAlignedBB getAreaBoundingBox(@Nonnull EntityPlayer player) {
        return new AxisAlignedBB(player.getPosition()).grow(range, range, range);
    }

    private boolean drainEnergy(boolean simulate, @Nonnull ItemStack stack, long amount) {
        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem == null)
            return false;

        return electricItem.discharge(amount, Integer.MAX_VALUE, true, false, simulate) >= amount;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        IItemBehaviour.super.addInformation(itemStack, lines);
        lines.add(I18n.format(isActive(itemStack) ? "behavior.item_magnet.enabled" : "behavior.item_magnet.disabled"));
    }
}
