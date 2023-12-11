package gregtech.api.items.armor;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.common.ConfigHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArmorUtils {

    public static final Side SIDE = FMLCommonHandler.instance().getSide();
    public static final SoundEvent JET_ENGINE = new SoundEvent(new ResourceLocation("gregtech:jet_engine"));

    /**
     * Check is possible to charge item
     */
    public static boolean isPossibleToCharge(ItemStack chargeable) {
        IElectricItem container = chargeable.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (container != null) {
            return container.getCharge() < container.getMaxCharge() &&
                    (container.getCharge() + container.getTransferLimit()) <= container.getMaxCharge();
        }
        return false;
    }

    /**
     * Searches all three player inventories for items that can be charged
     *
     * @param tier of charger
     * @return Map of the inventory and a list of the index of a chargable item
     */
    public static List<Pair<NonNullList<ItemStack>, List<Integer>>> getChargeableItem(EntityPlayer player, int tier) {
        List<Pair<NonNullList<ItemStack>, List<Integer>>> inventorySlotMap = new ArrayList<>();

        List<Integer> openMainSlots = new ArrayList<>();
        for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
            ItemStack current = player.inventory.mainInventory.get(i);
            IElectricItem item = current.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            if (item == null) continue;

            if (isPossibleToCharge(current) && item.getTier() <= tier) {
                openMainSlots.add(i);
            }
        }

        if (!openMainSlots.isEmpty()) {
            inventorySlotMap.add(Pair.of(player.inventory.mainInventory, openMainSlots));
        }

        List<Integer> openArmorSlots = new ArrayList<>();
        for (int i = 0; i < player.inventory.armorInventory.size(); i++) {
            ItemStack current = player.inventory.armorInventory.get(i);
            IElectricItem item = current.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            if (item == null) {
                continue;
            }

            if (isPossibleToCharge(current) && item.getTier() <= tier) {
                openArmorSlots.add(i);
            }
        }

        if (!openArmorSlots.isEmpty()) {
            inventorySlotMap.add(Pair.of(player.inventory.armorInventory, openArmorSlots));
        }

        ItemStack offHand = player.inventory.offHandInventory.get(0);
        IElectricItem offHandItem = offHand.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (offHandItem == null) {
            return inventorySlotMap;
        }

        if (isPossibleToCharge(offHand) && offHandItem.getTier() <= tier) {
            inventorySlotMap.add(Pair.of(player.inventory.offHandInventory, Collections.singletonList(0)));
        }

        return inventorySlotMap;
    }

    /**
     * Spawn particle behind player with speedY speed
     */
    public static void spawnParticle(World world, EntityPlayer player, EnumParticleTypes type, double speedY) {
        if (type != null && SIDE.isClient()) {
            Vec3d forward = player.getForward();
            world.spawnParticle(type, player.posX - forward.x, player.posY + 0.5D, player.posZ - forward.z, 0.0D,
                    speedY, 0.0D);
        }
    }

    public static void playJetpackSound(@NotNull EntityPlayer player) {
        if (player.world.isRemote) {
            float cons = (float) player.motionY + player.moveForward;
            cons = MathHelper.clamp(cons, 0.6F, 1.0F);

            if (player.motionY > 0.05F) {
                cons += 0.1F;
            }

            if (player.motionY < -0.05F) {
                cons -= 0.4F;
            }

            player.playSound(JET_ENGINE, 0.3F, cons);
        }
    }

    /**
     * Resets private field, amount of ticks player in the sky
     */
    @SuppressWarnings("deprecation")
    public static void resetPlayerFloatingTime(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            ObfuscationReflectionHelper.setPrivateValue(NetHandlerPlayServer.class,
                    ((EntityPlayerMP) player).connection, 0, "field_147365_f", "floatingTickCount");
        }
    }

    /**
     * This method feeds player with food, if food heal amount more than
     * empty food gaps, then reminder adds to saturation
     *
     * @return result of eating food
     */
    public static ActionResult<ItemStack> canEat(EntityPlayer player, ItemStack food) {
        if (!(food.getItem() instanceof ItemFood)) {
            return new ActionResult<>(EnumActionResult.FAIL, food);
        }

        ItemFood foodItem = (ItemFood) food.getItem();
        if (player.getFoodStats().needFood()) {
            if (!player.isCreative()) {
                food.setCount(food.getCount() - 1);
            }

            // Find the saturation of the food
            float saturation = foodItem.getSaturationModifier(food);

            // The amount of empty food haunches of the player
            int hunger = 20 - player.getFoodStats().getFoodLevel();

            // Increase the saturation of the food if the food replenishes more than the amount of missing haunches
            saturation += (hunger - foodItem.getHealAmount(food)) < 0 ? foodItem.getHealAmount(food) - hunger : 1.0F;

            // Use this method to add stats for compat with TFC, who overrides addStats(int amount, float saturation)
            // for their food and does nothing
            player.getFoodStats().addStats(
                    new ItemFood(foodItem.getHealAmount(food), saturation, foodItem.isWolfsFavoriteMeat()), food);

            return new ActionResult<>(EnumActionResult.SUCCESS, food);
        } else {
            return new ActionResult<>(EnumActionResult.FAIL, food);
        }
    }

    /**
     * Format itemstacks list from [1xitem@1, 1xitem@1, 1xitem@2] to
     * [2xitem@1, 1xitem@2]
     *
     * @return Formated list
     */
    public static List<ItemStack> format(List<ItemStack> input) {
        Object2IntMap<ItemStack> items = new Object2IntOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        List<ItemStack> output = new ArrayList<>();
        for (ItemStack itemStack : input) {
            if (items.containsKey(itemStack)) {
                int amount = items.get(itemStack);
                items.replace(itemStack, ++amount);
            } else {
                items.put(itemStack, 1);
            }
        }
        for (Object2IntMap.Entry<ItemStack> entry : items.object2IntEntrySet()) {
            ItemStack stack = entry.getKey().copy();
            stack.setCount(entry.getIntValue());
            output.add(stack);
        }
        return output;
    }

    @NotNull
    public static String format(long value) {
        return new DecimalFormat("###,###.##").format(value);
    }

    public static String format(double value) {
        return new DecimalFormat("###,###.##").format(value);
    }

    /**
     * Modular HUD class for armor
     * now available only string rendering, if will be needed,
     * may be will add some additional functions
     */
    @SideOnly(Side.CLIENT)
    public static class ModularHUD {

        private byte stringAmount = 0;
        private final List<String> stringList;
        private static final Minecraft mc = Minecraft.getMinecraft();

        public ModularHUD() {
            this.stringList = new ArrayList<>();
        }

        public void newString(String string) {
            this.stringAmount++;
            this.stringList.add(string);
        }

        public void draw() {
            for (int i = 0; i < stringAmount; i++) {
                Pair<Integer, Integer> coords = this.getStringCoord(i);
                mc.ingameGUI.drawString(mc.fontRenderer, stringList.get(i), coords.getLeft(), coords.getRight(),
                        0xFFFFFF);
            }
        }

        @NotNull
        private Pair<Integer, Integer> getStringCoord(int index) {
            int posX;
            int posY;
            int fontHeight = mc.fontRenderer.FONT_HEIGHT;
            int windowHeight = new ScaledResolution(mc).getScaledHeight();
            int windowWidth = new ScaledResolution(mc).getScaledWidth();
            int stringWidth = mc.fontRenderer.getStringWidth(stringList.get(index));
            if (ConfigHolder.client.armorHud.hudLocation == 1) {
                posX = 1 + ConfigHolder.client.armorHud.hudOffsetX;
                posY = 1 + ConfigHolder.client.armorHud.hudOffsetY + (fontHeight * index);
            } else if (ConfigHolder.client.armorHud.hudLocation == 2) {
                posX = windowWidth - (1 + ConfigHolder.client.armorHud.hudOffsetX) - stringWidth;
                posY = 1 + ConfigHolder.client.armorHud.hudOffsetY + (fontHeight * index);
            } else if (ConfigHolder.client.armorHud.hudLocation == 3) {
                posX = 1 + ConfigHolder.client.armorHud.hudOffsetX;
                posY = windowHeight - fontHeight * (stringAmount - index) - 1 - ConfigHolder.client.armorHud.hudOffsetY;
            } else if (ConfigHolder.client.armorHud.hudLocation == 4) {
                posX = windowWidth - (1 + ConfigHolder.client.armorHud.hudOffsetX) - stringWidth;
                posY = windowHeight - fontHeight * (stringAmount - index) - 1 - ConfigHolder.client.armorHud.hudOffsetY;
            } else {
                throw new IllegalArgumentException("Armor Hud config hudLocation is improperly configured.");
            }
            return Pair.of(posX, posY);
        }

        public void reset() {
            this.stringAmount = 0;
            this.stringList.clear();
        }
    }
}
