package gregtech.api.metatileentity.multiblock;

import java.util.List;

public interface IMultiblockAbilityPart<T> extends IMultiblockPart {

    MultiblockAbility<T> getAbility();

    void registerAbilities(List<T> abilityList);
}
