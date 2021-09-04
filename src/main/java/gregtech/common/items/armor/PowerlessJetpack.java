package gregtech.common.items.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import gregtech.api.GTValues;
import gregtech.api.items.armor.ArmorMetaItem.ArmorMetaValueItem;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.items.armor.IArmorLogic;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipes.FuelRecipe;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.api.util.input.EnumKey;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

public class PowerlessJetpack implements IArmorLogic {

    public static final List<FuelRecipe> FUELS = RecipeMaps.COMBUSTION_GENERATOR_FUELS.getRecipeList();
    public static final List<Fluid> FUELS_FORBIDDEN = Arrays.asList(Materials.Oil.getFluid(), Materials.SulfuricLightFuel.getFluid());

    public final int tankCapacity = 16000;
    public final int burnTier = GTValues.MV;

    @SideOnly(Side.CLIENT)
    private ArmorUtils.ModularHUD HUD;

    public PowerlessJetpack() {
        if (ArmorUtils.SIDE.isClient()) HUD = new ArmorUtils.ModularHUD();
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
        IFluidHandlerItem internalTank = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        FuelRecipe currentRecipe = null;
        if (internalTank != null && internalTank.drain(1, false) != null && !player.isInWater() && !player.isInLava()) {
            for (FuelRecipe current : FUELS) {
                if (current.getRecipeFluid().isFluidEqual(internalTank.drain(1, false))) {
                    currentRecipe = current;
                    break;
                }
            }

            if (currentRecipe == null) {
                return;
            }

            NBTTagCompound data = GTUtility.getOrCreateNbtCompound(stack);
            int burntime = 0;
            byte toggleTimer = 0;
            boolean hover = false;
            boolean suc = false;
            FluidStack fuel = currentRecipe.getRecipeFluid();
            if (data.hasKey("burnTimer")) burntime = data.getShort("burnTimer");
            if (data.hasKey("toggleTimer")) toggleTimer = data.getByte("toggleTimer");
            if (data.hasKey("hover")) hover = data.getBoolean("hover");

            if (!world.isRemote) {
                if (ArmorUtils.isKeyDown(player, EnumKey.MODE_SWITCH) && ArmorUtils.isKeyDown(player, EnumKey.JUMP) && toggleTimer == 0) {
                    hover = !hover;
                    toggleTimer = 10;
                    data.setBoolean("hover", hover);
                    if (hover) {
                        player.sendMessage(new TextComponentTranslation("metaarmor.jetpack.hover.enable"));
                    } else {
                        player.sendMessage(new TextComponentTranslation("metaarmor.jetpack.hover.disable"));
                    }
                }
            }

            if (player.onGround && !ArmorUtils.isKeyDown(player, EnumKey.JUMP) && hover && !world.isRemote) {
                hover = !hover;
                data.setBoolean("hover", hover);
                player.sendMessage(new TextComponentTranslation("metaarmor.jetpack.hover.disable"));
            }

            if (internalTank.drain(fuel, false).amount == fuel.amount || burntime >= GTValues.V[burnTier]) {
                if (!hover) {
                    if (ArmorUtils.isKeyDown(player, EnumKey.JUMP)) {
                        if (player.motionY < 0.6D) player.motionY += 0.2D;
                        if (ArmorUtils.isKeyDown(player, EnumKey.FORWARD)) {
                            player.moveRelative(0.0F, 0.0F, 1.0F, 0.1F);
                        }
                        ArmorUtils.spawnParticle(world, player, EnumParticleTypes.SMOKE_LARGE, -0.6D);
                        ArmorUtils.spawnParticle(world, player, EnumParticleTypes.CLOUD, -0.6D);
                        ArmorUtils.playJetpackSound(player);
                        suc = true;
                    }
                } else {
                    if (!player.onGround) {
                        ArmorUtils.spawnParticle(world, player, EnumParticleTypes.CLOUD, -0.3D);
                        ArmorUtils.playJetpackSound(player);
                    }
                    if (ArmorUtils.isKeyDown(player, EnumKey.FORWARD) && player.motionX < 0.5D && player.motionZ < 0.5D) {
                        player.moveRelative(0.0F, 0.0F, 1.0F, 0.025F);
                    }

                    if (ArmorUtils.isKeyDown(player, EnumKey.JUMP)) {
                        if (player.motionY < 0.5D) {
                            player.motionY += 0.125D;
                            ArmorUtils.spawnParticle(world, player, EnumParticleTypes.SMOKE_LARGE, -0.6D);
                        }
                    } else if (ArmorUtils.isKeyDown(player, EnumKey.SHIFT)) {
                        if (player.motionY < -0.5D) player.motionY += 0.1D;
                    } else if (!ArmorUtils.isKeyDown(player, EnumKey.JUMP) && !ArmorUtils.isKeyDown(player, EnumKey.SHIFT) && !player.onGround) {
                        if (player.motionY < 0 && player.motionY >= -0.03D) player.motionY = -0.025D;
                        if (player.motionY < -0.025D) {
                            if (player.motionY + 0.2D > -0.025D) {
                                player.motionY = -0.025D;
                            } else {
                                player.motionY += 0.2D;
                            }
                        }
                    }
                    player.fallDistance = 0.0F;
                    suc = true;
                }

                if (suc) {
                    if (!player.onGround) {
                        if (burntime < GTValues.V[burnTier]) {
                            player.fallDistance = 0.0F;
                            burntime = (int) (currentRecipe.getDuration() * currentRecipe.getMinVoltage());
                            internalTank.drain(fuel.amount, true);
                        } else {
                            burntime -= GTValues.V[burnTier];
                        }
                    }
                }
            }

            if (world.getWorldTime() % 40 == 0 && !player.onGround) {
                ArmorUtils.resetPlayerFloatingTime(player);
            }

            if (toggleTimer > 0) toggleTimer--;

            data.setShort("burnTimer", (short) burntime);
            data.setByte("toggleTimer", toggleTimer);
            player.inventoryContainer.detectAndSendChanges();
        }
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot(ItemStack itemStack) {
        return EntityEquipmentSlot.CHEST;
    }

    @Override
    public void damageArmor(EntityLivingBase entity, ItemStack itemStack, DamageSource source, int damage, EntityEquipmentSlot equipmentSlot) {
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        return ImmutableMultimap.of();
    }

    @Override
    public void addToolComponents(@SuppressWarnings("rawtypes") ArmorMetaValueItem mvi) {
        mvi.addComponents(new Behaviour(tankCapacity));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "gregtech:textures/armor/liquid_fuel_jetpack.png";
    }

    @SideOnly(Side.CLIENT)
    public void drawHUD(ItemStack item) {
        IFluidHandlerItem tank = item.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (tank != null) {
            IFluidTankProperties[] prop = tank.getTankProperties();
            if (prop[0] != null) {
                if (prop[0].getContents() != null) {
                    if (prop[0].getContents().amount == 0) return;
                    String formated = String.format("%.1f", (prop[0].getContents().amount * 100.0F / prop[0].getCapacity()));
                    this.HUD.newString(I18n.format("metaarmor.hud.fuel_lvl", formated + "%"));
                    NBTTagCompound data = item.getTagCompound();
                    if (data != null) {
                        if (data.hasKey("hover")) {
                            String status = (data.getBoolean("hover") ? I18n.format("metaarmor.hud.status.enabled") : I18n.format("metaarmor.hud.status.disabled"));
                            String result = I18n.format("metaarmor.hud.hover_mode", status);
                            this.HUD.newString(result);
                        }
                    }
                }
            }
        }
        this.HUD.draw();
        this.HUD.reset();
    }

    @SideOnly(Side.CLIENT)
    public boolean isNeedDrawHUD() {
        return true;
    }


    public static class Behaviour implements IItemDurabilityManager, IItemCapabilityProvider, IItemBehaviour {

        public final int maxCapacity;

        public Behaviour(int internalCapacity) {
            this.maxCapacity = internalCapacity;
        }

        @Override
        public boolean showsDurabilityBar(ItemStack itemStack) {
            return true;
        }

        @Override
        public double getDurabilityForDisplay(ItemStack itemStack) {
            IFluidHandlerItem fluidHandlerItem = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            IFluidTankProperties fluidTankProperties = fluidHandlerItem.getTankProperties()[0];
            FluidStack fluidStack = fluidTankProperties.getContents();
            return fluidStack == null ? 1.0 : (1.0 - fluidStack.amount / (fluidTankProperties.getCapacity() * 1.0));
        }

        @Override
        public int getRGBDurabilityForDisplay(ItemStack itemStack) {
            return MathHelper.hsvToRGB(0.33f, 1.0f, 1.0f);
        }

        @Override
        public ICapabilityProvider createProvider(ItemStack itemStack) {
            return new FluidHandlerItemStack(itemStack, maxCapacity) {
                @Override
                public boolean canFillFluidType(FluidStack fluidStack) {
                    for (FuelRecipe recipe : FUELS)
                        if (fluidStack.isFluidEqual(recipe.getRecipeFluid()) && !FUELS_FORBIDDEN.contains(fluidStack.getFluid()))
                            return true;
                    return false;
                }

                @Override
                public IFluidTankProperties[] getTankProperties() {
                    return new FluidTankProperties[]{new FluidTankProperties(getFluid(), capacity, true, false)};
                }
            };
        }

        @Override
        public void addInformation(ItemStack itemStack, List<String> lines) {
        }
    }
}
