package cech12.extendedmushrooms.api.recipe;

import cech12.extendedmushrooms.ExtendedMushrooms;
import net.minecraft.item.crafting.IRecipeType;

public class FairyCircleRecipeType implements IRecipeType<FairyCircleRecipe> {

    @Override
    public String toString () {
        return ExtendedMushrooms.MOD_ID + ":fairy_circle_recipe";
    }

}
