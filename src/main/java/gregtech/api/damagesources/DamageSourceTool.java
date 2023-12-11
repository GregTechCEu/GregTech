package gregtech.api.damagesources;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;

import org.jetbrains.annotations.NotNull;

public class DamageSourceTool extends EntityDamageSource {

    private final String deathMessage;

    public DamageSourceTool(String type, EntityLivingBase player, String deathMessage) {
        super(type, player);
        this.deathMessage = deathMessage;
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public ITextComponent getDeathMessage(@NotNull EntityLivingBase target) {
        if (deathMessage == null || damageSourceEntity == null || !I18n.canTranslate(deathMessage))
            return super.getDeathMessage(target);
        return new TextComponentTranslation(deathMessage, target.getDisplayName(), damageSourceEntity.getDisplayName());
    }
}
