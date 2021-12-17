package gregtech.common;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.enchants.EnchantmentHardHammer;
import gregtech.api.items.armor.ArmorLogicSuite;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.net.NetworkHandler;
import gregtech.api.net.packets.CPacketKeysPressed;
import gregtech.api.util.VirtualTankRegistry;
import gregtech.api.util.input.Key;
import gregtech.api.util.input.KeyBinds;
import gregtech.common.items.MetaItems;
import gregtech.common.items.armor.IJetpack;
import gregtech.common.items.armor.PowerlessJetpack;
import gregtech.common.items.behaviors.ToggleEnergyConsumerBehavior;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityCentralMonitor;
import gregtech.common.tools.ToolUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
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
    public static void onEntitySpawn(LivingSpawnEvent.SpecialSpawn event) {
        EntityLivingBase entity = event.getEntityLiving();
        EnumDifficulty difficulty = entity.world.getDifficulty();
        if (difficulty == EnumDifficulty.HARD && entity.getRNG().nextFloat() <= 0.03f) {
            if (entity instanceof EntityZombie && ConfigHolder.tools.nanoSaber.zombieSpawnWithSabers) {
                ItemStack itemStack = MetaItems.NANO_SABER.getInfiniteChargedStack();
                ToggleEnergyConsumerBehavior.setItemActive(itemStack, true);
                entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, itemStack);
                ((EntityZombie) entity).setDropChance(EntityEquipmentSlot.MAINHAND, 0.0f);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInteractionRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().getTileEntity(event.getPos()) instanceof MetaTileEntityHolder) {
            event.setUseBlock(Event.Result.ALLOW);
        }
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && stack.getItem() == Items.FLINT_AND_STEEL) {
            if (!event.getWorld().isRemote
                    && !event.getEntityPlayer().capabilities.isCreativeMode
                    && GTValues.RNG.nextInt(100) >= ConfigHolder.misc.flintChanceToCreateFire) {
                stack.damageItem(1, event.getEntityPlayer());
                if (stack.getItemDamage() >= stack.getMaxDamage()) {
                    stack.shrink(1);
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInteractionLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntityPlayer().isCreative()) {
            TileEntity holder = event.getWorld().getTileEntity(event.getPos());
            if (holder instanceof MetaTileEntityHolder && ((MetaTileEntityHolder) holder).getMetaTileEntity() instanceof MetaTileEntityCentralMonitor) {
                ((MetaTileEntityCentralMonitor) ((MetaTileEntityHolder) holder).getMetaTileEntity()).invalidateStructure();
            }
        }
    }

    @SubscribeEvent
    public static void hammer(BlockEvent.HarvestDropsEvent event) {
        if (!event.getWorld().isRemote && event.getHarvester() != null && !event.isSilkTouching()) {
            int level = EnchantmentHelper.getEnchantmentLevel(EnchantmentHardHammer.INSTANCE, event.getHarvester().getHeldItemMainhand());
            if (level > 0) {
                ToolUtility.applyHammerDrops(event.getWorld().rand, event.getState(), event.getDrops(), EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, event.getHarvester().getHeldItemMainhand()), event.getHarvester());

            }
        }

    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onRender(final TickEvent.RenderTickEvent event) {
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
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (ArmorUtils.SIDE.isClient()) {
            boolean needNewPacket = false;
            for (Key key : KeyBinds.REGISTRY) {
                boolean keyState = key.getBind().isKeyDown();
                if (key.state != keyState) {
                    key.state = keyState;
                    needNewPacket = true;
                }
            }
            if (needNewPacket) {
                NetworkHandler.channel.sendToServer(new CPacketKeysPressed(KeyBinds.REGISTRY).toFMLPacket());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onEntityLivingFallEvent(LivingFallEvent event) {
        if (!event.getEntity().getEntityWorld().isRemote && event.getEntity() instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) event.getEntity();
            ItemStack armor = entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);
            ItemStack jet = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            if (!jet.isEmpty() && jet.getItem() instanceof IJetpack) {
                event.setDamageMultiplier(1 / Math.max(0.00001f, ((IJetpack) jet.getItem()).getFallDamageReduction()));
            }

            if (armor.isEmpty())
                return;

            ArmorMetaItem<?>.ArmorMetaValueItem armorMetaValue = ((ArmorMetaItem<?>) armor.getItem()).getItem(armor);
            if (armorMetaValue.getArmorLogic() instanceof ArmorLogicSuite) {
                if (entity.fallDistance >= 3.2f) {
                    ArmorLogicSuite armorLogic = (ArmorLogicSuite) armorMetaValue.getArmorLogic();
                    IElectricItem item = armor.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                    if (item == null) return;
                    int energyCost = (int) (armorLogic.getEnergyPerUse() * Math.sqrt(entity.fallDistance));
                    if (item.getCharge() >= energyCost) {
                        item.discharge(energyCost, item.getTier(), true, false, false);
                        entity.fallDistance = 0;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onWorldLoadEvent(WorldEvent.Load event) {
        VirtualTankRegistry.initializeStorage(event.getWorld());
    }
}
