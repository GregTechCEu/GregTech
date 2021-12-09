package gregtech.common.items.armor;

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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class PowerlessJetpack implements IArmorLogic {

    public static final List<FuelRecipe> FUELS = RecipeMaps.COMBUSTION_GENERATOR_FUELS.getRecipeList();
    public static final List<Fluid> FUELS_FORBIDDEN = Arrays.asList(Materials.Oil.getFluid(), Materials.SulfuricLightFuel.getFluid());

    public final int tankCapacity = 16000;

    private FuelRecipe previousRecipe = null;

    @SideOnly(Side.CLIENT)
    private ArmorUtils.ModularHUD HUD;

    public PowerlessJetpack() {
        if (ArmorUtils.SIDE.isClient())
            //noinspection NewExpressionSideOnly
            HUD = new ArmorUtils.ModularHUD();
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, @Nonnull ItemStack stack) {
        IFluidHandlerItem internalTank = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);

        NBTTagCompound data = GTUtility.getOrCreateNbtCompound(stack);
        int burnTime = 0;
        byte toggleTimer = 0;
        boolean hover = false;

        FuelRecipe currentRecipe = null;
        FluidStack fuel = currentRecipe.getRecipeFluid();
        if (data.hasKey("burnTimer")) burnTime = data.getShort("burnTimer");
        if (data.hasKey("toggleTimer")) toggleTimer = data.getByte("toggleTimer");
        if (data.hasKey("hover")) hover = data.getBoolean("hover");

        if (toggleTimer == 0 && ArmorUtils.isKeyDown(player, EnumKey.HOVER_KEY)) {
            hover = !hover;
            toggleTimer = 10;
            data.setBoolean("hover", hover);
            if (!world.isRemote) {
                if (hover)
                    player.sendMessage(new TextComponentTranslation("metaarmor.jetpack.hover.enable"));
                else
                    player.sendMessage(new TextComponentTranslation("metaarmor.jetpack.hover.disable"));
            }
        }

        if (internalTank != null && !player.isInWater() && !player.isInLava()) {
            FluidStack fluidStack = internalTank.drain(1, false);
            if (previousRecipe != null && fluidStack != null && fluidStack.isFluidEqual(previousRecipe.getRecipeFluid()) && fluidStack.amount > 0) {
                currentRecipe = previousRecipe;
            } else if (fluidStack != null) {
                for (FuelRecipe recipe : FUELS) {
                    if (recipe.getRecipeFluid().isFluidEqual(fluidStack)) {
                        currentRecipe = recipe;
                        previousRecipe = currentRecipe;
                        break;
                    }
                }
            } else {
                return;
            }

            if (currentRecipe == null)
                return;

            if (burnTime > 0 || internalTank.drain(fuel, false).amount >= fuel.amount) {
                if (!hover) {
                    if (ArmorUtils.isKeyDown(player, EnumKey.JUMP)) { //todo prevent holding space + e from flying upwards
                        if (player.motionY < 0.6D) player.motionY += 0.2D;
                        if (ArmorUtils.isKeyDown(player, EnumKey.FORWARD)) {
                            player.moveRelative(0.0F, 0.0F, 1.0F, 0.1F);
                        }
                        ArmorUtils.spawnParticle(world, player, EnumParticleTypes.SMOKE_LARGE, -0.6D);
                        ArmorUtils.spawnParticle(world, player, EnumParticleTypes.CLOUD, -0.6D);
                        ArmorUtils.playJetpackSound(player);
                    }
                } else {
                    if (!player.onGround) {
                        ArmorUtils.spawnParticle(world, player, EnumParticleTypes.CLOUD, -0.3D);
                        ArmorUtils.playJetpackSound(player);
                    }

                    if (ArmorUtils.isKeyDown(player, EnumKey.FORWARD) && player.motionX < 0.5D && player.motionZ < 0.5D) {
                        player.moveRelative(0.0F, 0.0F, 1.0F, 0.025F);
                    }

                    if (ArmorUtils.isKeyDown(player, EnumKey.JUMP)) { //todo prevent holding space + e from flying upwards
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
                }

                if (!player.onGround) {
                    if (burnTime <= 0) {
                        player.fallDistance = 0.0F;
                        burnTime = (int) (currentRecipe.getDuration() * currentRecipe.getMinVoltage());
                        internalTank.drain(fuel.amount, true);
                    } else {
                        burnTime -= 1;
                    }
                }
            }
            if (world.getWorldTime() % 40 == 0 && !player.onGround) {
                ArmorUtils.resetPlayerFloatingTime(player);
            }

            if (toggleTimer > 0)
                toggleTimer--;

            data.setShort("burnTimer", (short) burnTime);
            data.setByte("toggleTimer", toggleTimer);
            player.inventoryContainer.detectAndSendChanges();

        }
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot(ItemStack itemStack) {
        return EntityEquipmentSlot.CHEST;
    }

    @Override
    public void addToolComponents(@Nonnull ArmorMetaValueItem mvi) {
        mvi.addComponents(new Behaviour(tankCapacity));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "gregtech:textures/armor/liquid_fuel_jetpack.png";
    }

    @SideOnly(Side.CLIENT)
    public void drawHUD(@Nonnull ItemStack item) {
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
        public double getDurabilityForDisplay(@Nonnull ItemStack itemStack) {
            IFluidHandlerItem fluidHandlerItem = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandlerItem == null)
                return 1.0;
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
                    for (FuelRecipe recipe : FUELS) {
                        if (fluidStack.isFluidEqual(recipe.getRecipeFluid()) && !FUELS_FORBIDDEN.contains(fluidStack.getFluid()))
                            return true;
                    }
                    return false;
                }

                @Override
                public IFluidTankProperties[] getTankProperties() {
                    return new FluidTankProperties[]{new FluidTankProperties(getFluid(), capacity, true, false)};
                }
            };
        }
    }
}
