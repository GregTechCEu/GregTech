package gregtech.integration.forestry.tools;

import gregtech.api.GTValues;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.items.toolitem.behavior.IToolBehavior;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.Loader;

import forestry.api.lepidopterology.EnumFlutterType;
import forestry.api.lepidopterology.IAlleleButterflySpecies;
import forestry.api.lepidopterology.IEntityButterfly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScoopBehavior implements IToolBehavior {

    public static final ScoopBehavior INSTANCE = new ScoopBehavior();

    private ScoopBehavior() {/**/}

    @Override
    public void hitEntity(@NotNull ItemStack stack, @NotNull EntityLivingBase target,
                          @NotNull EntityLivingBase attacker) {
        if (!Loader.isModLoaded(GTValues.MODID_FR)) return;
        if (!(target instanceof IEntityButterfly butterfly)) return;
        if (!(attacker instanceof EntityPlayer player)) return;
        if (attacker.world == null || attacker.world.isRemote) return;

        IAlleleButterflySpecies species = butterfly.getButterfly().getGenome().getPrimary();
        species.getRoot().getBreedingTracker(target.world, player.getGameProfile())
                .registerCatch(butterfly.getButterfly());

        ItemStack memberStack = species.getRoot().getMemberStack(butterfly.getButterfly().copy(),
                EnumFlutterType.BUTTERFLY);
        EntityItem entityItem = new EntityItem(player.world, target.posX, target.posY, target.posZ, memberStack);
        target.setDead();
        NBTTagCompound behaviorTag = ToolHelper.getBehaviorsTag(stack);
        if (behaviorTag.getBoolean(ToolHelper.RELOCATE_MINED_BLOCKS_KEY)) {
            if (ForgeEventFactory.onItemPickup(entityItem, player) == -1) return;
            if (player.addItemStackToInventory(memberStack)) return;
        }
        player.world.spawnEntity(entityItem);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        tooltip.add(I18n.format("item.gt.tool.behavior.scoop"));
    }
}
