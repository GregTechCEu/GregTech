package gregtech.common;

import gregtech.api.GTValues;
import gregtech.common.blocks.BlockConcrete;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.armor.ArmorLogicSuite;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.net.KeysPacket;
import gregtech.api.net.NetworkHandler;
import gregtech.api.util.input.Key;
import gregtech.api.util.input.Keybinds;
import gregtech.common.items.Armor.PowerlessJetpack;
import gregtech.common.items.MetaItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = GTValues.MODID)
public class EventHandlers {

    @SubscribeEvent
    public static void onEndermanTeleportEvent(EnderTeleportEvent event) {
        if (event.getEntity() instanceof EntityEnderman && event.getEntityLiving()
                .getActivePotionEffect(MobEffects.WEAKNESS) != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerInteraction(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && stack.getItem() == Items.FLINT_AND_STEEL) {
            if (!event.getWorld().isRemote
                    && !event.getEntityPlayer().capabilities.isCreativeMode
                    && event.getWorld().rand.nextInt(100) >= ConfigHolder.flintChanceToCreateFire) {
                stack.damageItem(1, event.getEntityPlayer());
                if (stack.getItemDamage() >= stack.getMaxDamage()) {
                    stack.shrink(1);
                }
                event.setCanceled(true);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRender(final TickEvent.RenderTickEvent event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.inGameHasFocus && mc.world != null && !mc.gameSettings.showDebugInfo && Minecraft.isGuiEnabled()) {
            final ItemStack item = mc.player.inventory.armorItemInSlot(EntityEquipmentSlot.CHEST.getIndex());
            if (item.getItem() instanceof ArmorMetaItem) {
                ArmorMetaItem<?>.ArmorMetaValueItem armorMetaValue = ((ArmorMetaItem<?>) item.getItem()).getItem(item);
                if (armorMetaValue.getArmorLogic() instanceof ArmorLogicSuite) {
                    ArmorLogicSuite armorLogic = (ArmorLogicSuite) armorMetaValue.getArmorLogic();
                    if (armorLogic.isNeedDrawHUD()) {
                        armorLogic.drawHUD(item);
                    }
                } else if (armorMetaValue.getArmorLogic() instanceof PowerlessJetpack) {
                    PowerlessJetpack armorLogic = (PowerlessJetpack) armorMetaValue.getArmorLogic();
                    if (armorLogic.isNeedDrawHUD()) {
                        armorLogic.drawHUD(item);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (ArmorUtils.SIDE.isClient()) {
            boolean needNewPacket = false;
            for (Key key : Keybinds.REGISTERY) {
                boolean keyState = key.getBind().isKeyDown();
                if (key.state != keyState) {
                    key.state = keyState;
                    needNewPacket = true;
                }
            }
            if (needNewPacket) NetworkHandler.INSTANCE.sendToServer(new KeysPacket(Keybinds.REGISTERY));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEntityLivingFallEvent(LivingFallEvent event) {
        if (!event.getEntity().getEntityWorld().isRemote && event.getEntity() instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) event.getEntity();
            ItemStack armor = entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);
            ItemStack jet = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            final ItemStack NANO = MetaItems.NANO_MUSCLE_SUITE_BOOTS.getStackForm();
            final ItemStack QUARK = MetaItems.QUARK_TECH_SUITE_BOOTS.getStackForm();
            final ItemStack JET = MetaItems.IMPELLER_JETPACK.getStackForm();
            final ItemStack ADJET = MetaItems.ADVANCED_IMPELLER_JETPACK.getStackForm();
            final ItemStack FLUIDJET = MetaItems.SEMIFLUID_JETPACK.getStackForm();


            if (armor != null || jet != null) {
                int fallDamage = 0;
                if (armor.isItemEqual(NANO)) {
                    fallDamage = MathHelper.floor(event.getDistance() - 3.0);
                    if (fallDamage >= 8) return;
                } else if (armor.isItemEqual(QUARK)) {
                    fallDamage = Math.max((int) event.getDistance() - 10, 0);
                }
                if (jet.isItemEqual(JET) || (jet.isItemEqual(ADJET) || (jet.isItemEqual(FLUIDJET)))) {

                } else {
                    return;
                }
                if (jet.isItemEqual(FLUIDJET)) {
                } else {

                    if (fallDamage != 0) {
                        ArmorMetaItem<?>.ArmorMetaValueItem armorMetaValue = ((ArmorMetaItem<?>) armor.getItem()).getItem(armor);
                        ArmorLogicSuite armorLogic = (ArmorLogicSuite) armorMetaValue.getArmorLogic();
                        IElectricItem item = armor.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                        if (item == null) return;
                        int energyCost = armorLogic.getEnergyPerUse() * fallDamage;
                        if (item.getCharge() >= energyCost) {
                            item.discharge(energyCost, item.getTier(), true, false, false);
                            event.setCanceled(true);
                        }
                    }
                    else{
                        ArmorMetaItem<?>.ArmorMetaValueItem armorMetaValue = ((ArmorMetaItem<?>) jet.getItem()).getItem(jet);
                        ArmorLogicSuite armorLogic = (ArmorLogicSuite) armorMetaValue.getArmorLogic();
                        IElectricItem item = jet.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                        if (item == null) return;
                        int energyCost = armorLogic.getEnergyPerUse();
                        if (item.getCharge() >= energyCost) {
                            item.discharge(energyCost, item.getTier(), true, false, false);
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }


}
