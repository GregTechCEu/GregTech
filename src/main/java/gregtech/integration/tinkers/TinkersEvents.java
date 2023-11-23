package gregtech.integration.tinkers;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Materials;
import gregtech.integration.tinkers.effect.GTTinkerEffects;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.tconstruct.library.events.TinkerRegisterEvent;
import slimeknights.tconstruct.library.smeltery.CastingRecipe;
import slimeknights.tconstruct.library.smeltery.ICastingRecipe;
import slimeknights.tconstruct.shared.TinkerFluids;

public class TinkersEvents {

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntityLiving().getActivePotionEffect(GTTinkerEffects.POTION_UNHEALING) != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void tinkerAlloyRemoval(TinkerRegisterEvent.AlloyRegisterEvent event) {
        FluidStack fluid = event.getRecipe().getResult();
        if (fluid.getFluid() == Materials.Brass.getFluid() && fluid.amount == 3) {
            // Remove tinker brass because it uses a different ratio from GT
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void tinkerMeltingRemoval(TinkerRegisterEvent.MeltingRegisterEvent event) {
        FluidStack fluid = event.getRecipe().getResult();
        if (fluid.getFluid() == TinkerFluids.glass) {
            // Remove tinker glass melting because it does different amounts from GT.
            // We add replacement glass recipes, so we need to carefully
            // check amount here instead of just blindly cancelling all.
            if (fluid.amount % GTValues.L != 0) {
                // If it is not an increment of 144L, then it cannot be from GT.
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void tinkerBasinCastingRemoval(TinkerRegisterEvent.BasinCastingRegisterEvent event) {
        if (event.getRecipe().matches(ItemStack.EMPTY, TinkerFluids.glass)) {
            // Remove tinker glass casting, same reason as above
            if (event.getRecipe().getFluidAmount() == 1000) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void tinkerTableCasingRemoval(TinkerRegisterEvent.TableCastingRegisterEvent event) {
        if (event.getRecipe().matches(ItemStack.EMPTY, TinkerFluids.glass)) {
            // Remove tinker glass casting, same reason as above
            if (event.getRecipe().getFluidAmount() == 375) {
                event.setCanceled(true);
            }
        }
    }

    // lowest so that other event listeners can cancel before we run
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tinkerTableCastingMirroring(TinkerRegisterEvent.TableCastingRegisterEvent event) {
        if (event.isCanceled()) return;
        if (!(event.getRecipe() instanceof CastingRecipe recipe)) return;

        // Probably is a clay cast, can just early exit
        if (recipe.consumesCast()) return;
    }
}
