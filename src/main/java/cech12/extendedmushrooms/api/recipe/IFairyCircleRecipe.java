package cech12.extendedmushrooms.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IFairyCircleRecipe {

    @Nonnull
    ResourceLocation getId();

    /**
     * Returns a list of ingredients.
     * Recipes returning an empty list or a list with more than 16 ingredients will never be used.
     * @return List of ingredients.
     */
    @Nonnull
    NonNullList<Ingredient> getIngredients();

    /**
     * Returns the time (in ticks) required for this recipe.
     * Value should not be negative.
     * @return the time (in ticks) required for this recipe
     */
    int getRecipeTime();

    /**
     * Returns the required FairyCircleMode.
     * @return required FairyCircleMode
     */
    @Nonnull
    FairyCircleRecipeMode getRequiredMode();

    /**
     * Returns the resulting ItemStack.
     * When null or ItemStack.EMPTY, all ingredients will be destroyed. (can be used for mode changes)
     * @return resulting ItemStack
     */
    @Nullable
    ItemStack getResultItemStack();

    /**
     * Returns the resulting FairyCircleMode. Default implementation returns the required mode,
     * that means the mode of fairy circle does not change.
     * @return resulting mode of fairy circle
     */
    @Nonnull
    default FairyCircleRecipeMode getResultMode() {
        return this.getRequiredMode();
    }

}
