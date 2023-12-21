package gregtech.common.items.tool;

import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.items.toolitem.behavior.IToolBehavior;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.IShearable;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class EntityShearBehavior implements IToolBehavior {

    public static final EntityShearBehavior INSTANCE = new EntityShearBehavior();

    private EntityShearBehavior() {}

    @Override
    public boolean onEntityInteract(@NotNull ItemStack stack, @NotNull EntityPlayer player,
                                    @NotNull EntityLivingBase entity, @NotNull EnumHand hand) {
        if (entity.world.isRemote) return false;

        if (entity instanceof IShearable shearable) {
            BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
            if (shearable.isShearable(stack, entity.world, pos)) {
                //noinspection ConstantConditions
                List<ItemStack> drops = shearable.onSheared(stack, entity.world, pos,
                        EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack));

                boolean relocateMinedBlocks = ToolHelper.getBehaviorsTag(stack)
                        .getBoolean(ToolHelper.RELOCATE_MINED_BLOCKS_KEY);
                Iterator<ItemStack> itr = drops.iterator();
                Random r = entity.world.rand;
                while (itr.hasNext()) {
                    ItemStack drop = itr.next();
                    if (drop == ItemStack.EMPTY) continue;
                    if (relocateMinedBlocks && player.addItemStackToInventory(drop)) {
                        itr.remove();
                    } else {
                        EntityItem itemEntity = entity.entityDropItem(drop, 1.0F);
                        // cannot be null if stack is not empty
                        //noinspection ConstantConditions
                        itemEntity.motionY += r.nextFloat() * 0.05F;
                        itemEntity.motionX += (r.nextFloat() - r.nextFloat()) * 0.1F;
                        itemEntity.motionZ += (r.nextFloat() - r.nextFloat()) * 0.1F;
                    }
                }
            }
            return true;
        }
        return false;
    }
}
