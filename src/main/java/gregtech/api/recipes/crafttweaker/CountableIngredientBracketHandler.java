package gregtech.api.recipes.crafttweaker;


import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.BracketHandler;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.zenscript.IBracketHandler;
import gregtech.api.GregTechAPI;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.expression.ExpressionCallStatic;
import stanhebben.zenscript.expression.ExpressionString;
import stanhebben.zenscript.parser.Token;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.type.natives.IJavaMethod;

import java.util.List;

@BracketHandler
@ZenRegister
public class CountableIngredientBracketHandler implements IBracketHandler {
    private final IJavaMethod method;

    public CountableIngredientBracketHandler() {
        this.method = CraftTweakerAPI.getJavaMethod(CountableIngredientBracketHandler.class, "get", String.class, String.class);
    }

    public static CountableIngredient get(String member1, String member2) {
        if (member1 == null || member2 == null || member1.isEmpty()) return null;
        if (member2.isEmpty()) {
            return CountableIngredient.from(member1);
        }
        OrePrefix prefix = OrePrefix.getPrefix(member1);
        Material material = GregTechAPI.MaterialRegistry.get(member2);
        return (prefix == null || material == null) ? null : CountableIngredient.from(prefix, material);
    }

    @Override
    public IZenSymbol resolve(IEnvironmentGlobal environment, List<Token> tokens) {
        if ((tokens.size() < 3)) return null;
        if (!tokens.get(0).getValue().equalsIgnoreCase("c_ingredient")) return null;
        if (!tokens.get(1).getValue().equals(":")) return null;
        if (tokens.size() == 3) {
            return position -> new ExpressionCallStatic(position, environment, method,
                    new ExpressionString(position, tokens.get(2).getValue()),
                    new ExpressionString(position, ""));
        } else if (tokens.size() == 5 && tokens.get(3).getValue().equals(":")){
            return position -> new ExpressionCallStatic(position, environment, method,
                    new ExpressionString(position, tokens.get(2).getValue()),
                    new ExpressionString(position, tokens.get(4).getValue()));
        }
        return null;
    }
}
