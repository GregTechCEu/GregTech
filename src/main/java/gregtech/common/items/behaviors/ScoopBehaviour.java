package gregtech.common.items.behaviors;

import forestry.api.lepidopterology.EnumFlutterType;
import forestry.api.lepidopterology.IAlleleButterflySpecies;
import forestry.api.lepidopterology.IEntityButterfly;
import gregtech.api.GTValues;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.util.GTUtility;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional.Method;

import java.util.List;

public class ScoopBehaviour implements IItemBehaviour {

    private final int cost;

    public ScoopBehaviour(int cost) {
        this.cost = cost;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack itemStack, EntityPlayer player, Entity entity) {
        return GTValues.isModLoaded(GTValues.MODID_FR) && processButterflyCatch(itemStack, player, entity);
    }

    @Method(modid = GTValues.MODID_FR)
    private boolean processButterflyCatch(ItemStack itemStack, EntityPlayer player, Entity entity) {
        if (entity instanceof IEntityButterfly) {
            if (player.world.isRemote) {
                return true;
            }
            if (player.capabilities.isCreativeMode || GTUtility.doDamageItem(itemStack, this.cost, false)) {
                IEntityButterfly butterfly = (IEntityButterfly) entity;
                IAlleleButterflySpecies species = butterfly.getButterfly().getGenome().getPrimary();
                species.getRoot().getBreedingTracker(entity.world, player.getGameProfile()).registerCatch(butterfly.getButterfly());
                player.world.spawnEntity(new EntityItem(player.world, entity.posX, entity.posY, entity.posZ,
                        species.getRoot().getMemberStack(butterfly.getButterfly().copy(), EnumFlutterType.BUTTERFLY)));
                entity.setDead();
            }
            return true;
        }
        return false;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("behaviour.scoop"));
    }
}
