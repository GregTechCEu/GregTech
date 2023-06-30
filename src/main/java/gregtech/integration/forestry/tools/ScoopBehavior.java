package gregtech.integration.forestry.tools;

import forestry.api.lepidopterology.EnumFlutterType;
import forestry.api.lepidopterology.IAlleleButterflySpecies;
import forestry.api.lepidopterology.IEntityButterfly;
import gregtech.api.GTValues;
import gregtech.api.items.toolitem.behavior.IToolBehavior;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ScoopBehavior implements IToolBehavior {

    public static final ScoopBehavior INSTANCE = new ScoopBehavior();

    private ScoopBehavior() {/**/}

    @Override
    public void hitEntity(@Nonnull ItemStack stack, @Nonnull EntityLivingBase target, @Nonnull EntityLivingBase attacker) {
        if (!Loader.isModLoaded(GTValues.MODID_FR)) return;
        if (!(target instanceof IEntityButterfly butterfly)) return;
        if (!(attacker instanceof EntityPlayer player)) return;
        if (attacker.world == null || attacker.world.isRemote) return;

        IAlleleButterflySpecies species = butterfly.getButterfly().getGenome().getPrimary();
        species.getRoot().getBreedingTracker(target.world, player.getGameProfile()).registerCatch(butterfly.getButterfly());
        player.world.spawnEntity(new EntityItem(player.world, target.posX, target.posY, target.posZ,
                species.getRoot().getMemberStack(butterfly.getButterfly().copy(), EnumFlutterType.BUTTERFLY)));
        target.setDead();
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(I18n.format("item.gt.tool.behavior.scoop"));
    }
}
