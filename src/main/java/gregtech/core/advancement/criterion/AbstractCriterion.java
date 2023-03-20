package gregtech.core.advancement.criterion;

import gregtech.api.advancement.IAdvancementCriterion;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public abstract class AbstractCriterion implements IAdvancementCriterion {

    private ResourceLocation id = new ResourceLocation("MISSING");

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public void setId(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "AbstractCriterion{id=" + this.id + "}";
    }
}
