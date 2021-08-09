package cofh.api.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public interface IToolHammer {

    boolean isUsable(ItemStack item, EntityLivingBase user, BlockPos pos);

    boolean isUsable(ItemStack item, EntityLivingBase user, Entity entity);

    void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos);

    void toolUsed(ItemStack item, EntityLivingBase user, Entity entity);
}
