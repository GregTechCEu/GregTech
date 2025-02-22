package gregtech.common;

import gregtech.api.GTValues;
import gregtech.api.block.IWalkingSpeedBonus;
import gregtech.api.configurator.registry.ConfiguratorDataRegistry;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pipenet.longdist.LongDistanceNetwork;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.BlockUtility;
import gregtech.api.util.CapesRegistry;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Mods;
import gregtech.api.util.virtualregistry.VirtualEnderRegistry;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinSaveData;
import gregtech.common.entities.EntityGTExplosive;
import gregtech.common.items.MetaItems;
import gregtech.common.items.armor.IStepAssist;
import gregtech.common.items.armor.PowerlessJetpack;
import gregtech.common.items.behaviors.ToggleEnergyConsumerBehavior;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityCentralMonitor;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import appeng.entity.EntitySingularity;

@Mod.EventBusSubscriber(modid = GTValues.MODID)
public class EventHandlers {

    private static final String HAS_TERMINAL = GTValues.MODID + ".terminal";
    private static ItemStack lastFeetEquip = ItemStack.EMPTY;

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
        // fix sneaking with shields not allowing tool interactions with GT machines
        TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
        if (tileEntity instanceof IGregTechTileEntity) {
            event.setUseBlock(Event.Result.ALLOW);
        } else if (tileEntity instanceof IPipeTile<?, ?>) {
            event.setUseBlock(Event.Result.ALLOW);
        }

        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && stack.getItem() == Items.FLINT_AND_STEEL) {
            if (!event.getWorld().isRemote && !event.getEntityPlayer().capabilities.isCreativeMode &&
                    GTValues.RNG.nextInt(100) >= ConfigHolder.misc.flintChanceToCreateFire) {
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
            if (holder instanceof IGregTechTileEntity &&
                    ((IGregTechTileEntity) holder).getMetaTileEntity() instanceof MetaTileEntityCentralMonitor) {
                ((MetaTileEntityCentralMonitor) ((IGregTechTileEntity) holder).getMetaTileEntity())
                        .invalidateStructure();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHarvestCheck(net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck event) {
        if (event.canHarvest()) {
            ItemStack item = event.getEntityPlayer().getHeldItemMainhand();
            String tool = event.getTargetBlock().getBlock().getHarvestTool(event.getTargetBlock());
            if (!ToolHelper.canMineWithPick(tool)) {
                return;
            }
            if (ConfigHolder.machines.requireGTToolsForBlocks) {
                event.setCanHarvest(false);
                return;
            }
            tool = ToolClasses.PICKAXE;
            int harvestLevel = event.getTargetBlock().getBlock().getHarvestLevel(event.getTargetBlock());
            if (!item.isEmpty() && harvestLevel >
                    item.getItem().getHarvestLevel(item, tool, event.getEntityPlayer(), event.getTargetBlock())) {
                event.setCanHarvest(false);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onDestroySpeed(net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed event) {
        ItemStack item = event.getEntityPlayer().getHeldItemMainhand();
        String tool = event.getState().getBlock().getHarvestTool(event.getState());
        if (tool != null && !item.isEmpty() && ToolHelper.canMineWithPick(tool) &&
                item.getItem().getToolClasses(item).contains(ToolClasses.PICKAXE)) {
            event.setNewSpeed(event.getNewSpeed() * 0.75f);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onEntityLivingFallEvent(LivingFallEvent event) {
        if (event.getEntity() instanceof EntityPlayerMP player) {
            ItemStack armor = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
            ItemStack jet = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            if (player.fallDistance < 3.2f)
                return;

            if (!armor.isEmpty() && armor.getItem() instanceof ArmorMetaItem<?>) {
                ArmorMetaItem<?>.ArmorMetaValueItem valueItem = ((ArmorMetaItem<?>) armor.getItem()).getItem(armor);
                if (valueItem != null) {
                    valueItem.getArmorLogic().damageArmor(player, armor, DamageSource.FALL,
                            (int) (player.fallDistance - 1.2f), EntityEquipmentSlot.FEET);
                    player.fallDistance = 0;
                    event.setCanceled(true);
                }
            } else if (!jet.isEmpty() && jet.getItem() instanceof ArmorMetaItem<?> &&
                    GTUtility.getOrCreateNbtCompound(jet).hasKey("flyMode")) {
                        ArmorMetaItem<?>.ArmorMetaValueItem valueItem = ((ArmorMetaItem<?>) jet.getItem()).getItem(jet);
                        if (valueItem != null) {
                            valueItem.getArmorLogic().damageArmor(player, jet, DamageSource.FALL,
                                    (int) (player.fallDistance - 1.2f), EntityEquipmentSlot.FEET);
                            player.fallDistance = 0;
                            event.setCanceled(true);
                        }
                    }
        }
    }

    @SubscribeEvent
    public static void onLivingEquipmentChangeEvent(LivingEquipmentChangeEvent event) {
        EntityEquipmentSlot slot = event.getSlot();
        if (event.getFrom().isEmpty() || slot == EntityEquipmentSlot.MAINHAND || slot == EntityEquipmentSlot.OFFHAND)
            return;

        ItemStack stack = event.getFrom();
        if (!(stack.getItem() instanceof ArmorMetaItem) || stack.getItem().equals(event.getTo().getItem()))
            return;

        ArmorMetaItem<?>.ArmorMetaValueItem valueItem = ((ArmorMetaItem<?>) stack.getItem()).getItem(stack);
        if (valueItem == null) return;
        if (valueItem.isItemEqual(MetaItems.NIGHTVISION_GOGGLES.getStackForm()) ||
                valueItem.isItemEqual(MetaItems.NANO_HELMET.getStackForm()) ||
                valueItem.isItemEqual(MetaItems.QUANTUM_HELMET.getStackForm())) {
            event.getEntityLiving().removePotionEffect(MobEffects.NIGHT_VISION);
        }
        if (valueItem.isItemEqual(MetaItems.QUANTUM_CHESTPLATE.getStackForm()) ||
                valueItem.isItemEqual(MetaItems.QUANTUM_CHESTPLATE_ADVANCED.getStackForm())) {
            event.getEntity().isImmuneToFire = false;
        }

        // Workaround to recipe caching issue with fluid jetpack
        // TODO, rewrite logic and remove in armor rewrite
        if (valueItem.isItemEqual(MetaItems.SEMIFLUID_JETPACK.getStackForm())) {
            ((PowerlessJetpack) valueItem.getArmorLogic()).resetRecipe();
        }
    }

    @SuppressWarnings({ "ConstantValue", "deprecation" })
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase == TickEvent.Phase.START && !player.world.isRemote) {
            IAttributeInstance movementSpeed = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
            if (movementSpeed == null) return;
            AttributeModifier modifier = movementSpeed.getModifier(BlockUtility.WALKING_SPEED_UUID);

            double speedBonus;
            if (!player.onGround || player.isInWater() || player.isSneaking()) {
                speedBonus = 0;
            } else {
                IBlockState state = player.world.getBlockState(new BlockPos(
                        player.posX, player.getEntityBoundingBox().minY - 1, player.posZ));
                speedBonus = BlockUtility.WALKING_SPEED_BONUS.getDouble(state);
                // { remove this bit while removing IWalkingSpeedBonus
                if (speedBonus == 0 &&
                        state.getBlock() instanceof IWalkingSpeedBonus walkingSpeedBonus &&
                        walkingSpeedBonus.getWalkingSpeedBonus() != 1 &&
                        walkingSpeedBonus.bonusSpeedCondition(player) &&
                        walkingSpeedBonus.checkApplicableBlocks(state)) {
                    speedBonus = walkingSpeedBonus.getWalkingSpeedBonus() - 1;
                }
                // }
            }
            if (modifier != null) {
                if (speedBonus == modifier.getAmount()) return;
                else movementSpeed.removeModifier(BlockUtility.WALKING_SPEED_UUID);
            } else {
                if (speedBonus == 0) return;
            }
            if (speedBonus != 0) {
                movementSpeed.applyModifier(new AttributeModifier(BlockUtility.WALKING_SPEED_UUID,
                        "Walking Speed Bonus", speedBonus, 2));
            }
        }
    }

    @SuppressWarnings({ "lossy-conversions", "ConstantValue" })
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onFOVUpdate(FOVUpdateEvent event) { // this event SUCKS
        EntityPlayer player = event.getEntity();
        IAttributeInstance movementSpeed = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (movementSpeed == null || movementSpeed.getModifier(BlockUtility.WALKING_SPEED_UUID) == null) return;

        float originalFov = player.capabilities.isFlying ? 1.1f : 1.0f;
        originalFov *= (movementSpeed.getAttributeValue() / player.capabilities.getWalkSpeed() + 1) / 2;

        if (player.capabilities.getWalkSpeed() == 0 || Float.isNaN(originalFov) || Float.isInfinite(originalFov)) {
            return;
        }

        float newFov = player.capabilities.isFlying ? 1.1f : 1.0f;
        newFov *= (computeValueWithoutWalkingSpeed(movementSpeed) / player.capabilities.getWalkSpeed() + 1) / 2;

        event.setNewfov(newFov / originalFov * event.getNewfov());
    }

    /**
     * Computes walking speed without boost from {@link BlockUtility#WALKING_SPEED_BONUS}. Skipping parent check stuff
     * because movement speed attribute does not have any parent modifier.
     */
    private static double computeValueWithoutWalkingSpeed(IAttributeInstance attrib) {
        double base = attrib.getBaseValue();

        for (AttributeModifier m : attrib.getModifiersByOperation(0)) {
            base += m.getAmount();
        }

        double applied = base;

        for (AttributeModifier m : attrib.getModifiersByOperation(1)) {
            applied += base * m.getAmount();
        }

        for (AttributeModifier m : attrib.getModifiersByOperation(2)) {
            if (m.getID() == BlockUtility.WALKING_SPEED_UUID) continue;
            applied *= 1 + m.getAmount();
        }

        return attrib.getAttribute().clampValue(applied);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onPlayerTickClient(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.player.isSpectator() &&
                !(event.player instanceof EntityOtherPlayerMP) && !(event.player instanceof FakePlayer)) {
            ItemStack feetEquip = event.player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
            if (!lastFeetEquip.getItem().equals(feetEquip.getItem())) {
                if (lastFeetEquip.getItem() instanceof ArmorMetaItem<?>) {
                    ArmorMetaItem<?>.ArmorMetaValueItem valueItem = ((ArmorMetaItem<?>) lastFeetEquip.getItem())
                            .getItem(lastFeetEquip);
                    if (valueItem != null && valueItem.getArmorLogic() instanceof IStepAssist) {
                        event.player.stepHeight = 0.6f;
                    }
                }

                lastFeetEquip = feetEquip.copy();
            }
        }
    }

    @SubscribeEvent
    public static void onWorldLoadEvent(WorldEvent.Load event) {
        VirtualEnderRegistry.initializeStorage(event.getWorld());
        CapesRegistry.checkAdvancements(event.getWorld());
        ConfiguratorDataRegistry.initializeStorage(event.getWorld());
    }

    @SubscribeEvent
    public static void onPlayerAdvancement(AdvancementEvent event) {
        CapesRegistry.unlockCapeOnAdvancement(event.getEntityPlayer(), event.getAdvancement());
    }

    @SubscribeEvent
    public static void onWorldUnloadEvent(WorldEvent.Unload event) {
        BedrockFluidVeinSaveData.setDirty();
        if (!event.getWorld().isRemote) {
            LongDistanceNetwork.WorldData.get(event.getWorld()).markDirty();
        }
    }

    @SubscribeEvent
    public static void onWorldSaveEvent(WorldEvent.Save event) {
        BedrockFluidVeinSaveData.setDirty();
        if (!event.getWorld().isRemote) {
            LongDistanceNetwork.WorldData.get(event.getWorld()).markDirty();
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (ConfigHolder.misc.spawnTerminal) {
            NBTTagCompound playerData = event.player.getEntityData();
            NBTTagCompound data = playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG) ?
                    playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG) : new NBTTagCompound();

            if (!data.getBoolean(HAS_TERMINAL)) {
                ItemStack terminal = MetaItems.TERMINAL.getStackForm();
                if (event.player.isCreative()) {
                    terminal.getOrCreateSubCompound("terminal").setBoolean("_creative", true);
                }
                ItemHandlerHelper.giveItemToPlayer(event.player, terminal);
                data.setBoolean(HAS_TERMINAL, true);
                playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
            }
        }
        CapesRegistry.detectNewCapes(event.player);
        CapesRegistry.loadWornCapeOnLogin(event.player);
    }

    @SubscribeEvent
    public static void onFurnaceFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        if (ItemStack.areItemStacksEqual(event.getItemStack(),
                FluidUtil.getFilledBucket(Materials.Creosote.getFluid(1000)))) {
            event.setBurnTime(6400);
        }
    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (event.getExplosion().exploder instanceof EntityGTExplosive explosive) {
            if (explosive.dropsAllBlocks()) {
                event.getAffectedEntities().removeIf(entity -> entity instanceof EntityItem && !checkAEEntity(entity));
            }
        }
    }

    private static boolean checkAEEntity(Entity entity) {
        return Mods.AppliedEnergistics2.isModLoaded() && entity instanceof EntitySingularity;
    }
}
