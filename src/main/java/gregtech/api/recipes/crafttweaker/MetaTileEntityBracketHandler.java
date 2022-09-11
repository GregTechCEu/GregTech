package gregtech.api.recipes.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.BracketHandler;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.zenscript.IBracketHandler;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.expression.ExpressionCallStatic;
import stanhebben.zenscript.expression.ExpressionString;
import stanhebben.zenscript.parser.Token;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.type.natives.IJavaMethod;

import java.util.List;

@BracketHandler
@ZenRegister
@SuppressWarnings("unused")
public class MetaTileEntityBracketHandler implements IBracketHandler {

    // TODO merge into MetaItemBracketHandler and YEET

    private final IJavaMethod method;

    public MetaTileEntityBracketHandler() {
        this.method = CraftTweakerAPI.getJavaMethod(MetaTileEntityBracketHandler.class, "getMetaTileEntityItem", String.class);
    }

    public static ItemStack getMetaTileEntityItem(String name) {
        String[] resultName = splitObjectName(name);
        MetaTileEntity metaTileEntity = GregTechAPI.MTE_REGISTRY.getObject(new ResourceLocation(resultName[0], resultName[1]));
        return metaTileEntity == null ? null : metaTileEntity.getStackForm();
    }

    public static String[] splitObjectName(String toSplit) {
        String[] resultSplit = new String[]{GTValues.MODID, toSplit};
        int i = toSplit.indexOf(':');
        if (i >= 0) {
            resultSplit[1] = toSplit.substring(i + 1);
            if (i > 1) {
                resultSplit[0] = toSplit.substring(0, i);
            }
        }
        return resultSplit;
    }

    @Override
    public IZenSymbol resolve(IEnvironmentGlobal environment, List<Token> tokens) {
        if ((tokens.size() < 3)) return null;
        if (!tokens.get(0).getValue().equalsIgnoreCase("meta_tile_entity")) return null;
        if (!tokens.get(1).getValue().equals(":")) return null;
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 2; i < tokens.size(); i++) {
            nameBuilder.append(tokens.get(i).getValue());
        }
        return position -> new ExpressionCallStatic(position, environment, method,
                new ExpressionString(position, nameBuilder.toString()));
    }

}
