package cech12.extendedmushrooms.api.recipe;

import cech12.extendedmushrooms.ExtendedMushrooms;
import cech12.extendedmushrooms.tileentity.FairyCircleTileEntity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FairyCircleRecipe implements IFairyCircleRecipe, IRecipe<IInventory> {

    public static final Serializer SERIALIZER = new Serializer();

    protected ResourceLocation resourceLocation;
    protected NonNullList<Ingredient> ingredients;
    protected FairyCircleRecipeMode requiredMode;
    protected int recipeTime;
    protected ItemStack resultStack;
    protected FairyCircleRecipeMode resultMode;

    public FairyCircleRecipe(ResourceLocation resourceLocation, FairyCircleRecipeMode requiredMode, NonNullList<Ingredient> ingredients, int recipeTime, FairyCircleRecipeMode resultMode, ItemStack resultStack) {
        this.resourceLocation = resourceLocation;
        this.requiredMode = requiredMode;
        this.ingredients = ingredients;
        this.recipeTime = recipeTime;
        this.resultMode = resultMode;
        this.resultStack = resultStack;
    }

    @Nonnull
    @Override
    public ResourceLocation getId () {
        return this.resourceLocation;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Nonnull
    @Override
    public IRecipeType<?> getType() {
        return ExtendedMushroomsRecipeTypes.FAIRY_CIRCLE;
    }

    @Nonnull
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public int getRecipeTime() {
        return this.recipeTime;
    }

    @Nonnull
    @Override
    public FairyCircleRecipeMode getRequiredMode() {
        return this.requiredMode;
    }

    @Nullable
    @Override
    public ItemStack getResultItemStack() {
        return resultStack;
    }

    @Nonnull
    @Override
    public FairyCircleRecipeMode getResultMode() {
        return this.resultMode;
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() {
        return this.resultStack;
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.RED_MUSHROOM);
    }

    public boolean isValid(FairyCircleRecipeMode mode, IInventory inv) {
        // inventory must have a stack limit of 1
        if (inv.getInventoryStackLimit() != 1) return false;
        // actual mode must fit to this recipe
        if (mode != this.getRequiredMode()) return false;

        //ingredients must be the same
        List<Ingredient> ingredientsMissing = new ArrayList<>(this.ingredients);
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack input = inv.getStackInSlot(i);
            if (!input.isEmpty()) {
                int index = -1;
                for (int j = 0; j < ingredientsMissing.size(); j++) {
                    Ingredient ingredient = ingredientsMissing.get(j);
                    if (ingredient.test(input)) {
                        index = j;
                        break;
                    }
                }
                if (index == -1) {
                    return false;
                }
                ingredientsMissing.remove(index);
            }
        }
        return ingredientsMissing.isEmpty();
    }

    /**
     * @deprecated has no use
     */
    @Deprecated
    @Override
    public boolean canFit(int width, int height) {
        // This method is ignored.
        return false;
    }

    /**
     * @deprecated use isValid method
     */
    @Deprecated
    @Override
    public boolean matches(@Nonnull IInventory inv, @Nonnull World worldIn) {
        // This method is ignored. - isValid is used.
        return false;
    }

    /**
     * @deprecated use isValid method
     */
    @Nonnull
    @Deprecated
    @Override
    public ItemStack getCraftingResult (@Nonnull IInventory inv) {
        // This method is ignored. - getResultItemStack is used.
        return this.resultStack.copy();
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<FairyCircleRecipe> {

        Serializer() {
            this.setRegistryName(new ResourceLocation(ExtendedMushrooms.MOD_ID, "fairy_circle_recipe"));
        }

        @Nonnull
        public FairyCircleRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
            //deserialize required mode
            FairyCircleRecipeMode requiredMode = readMode(json, FairyCircleRecipeMode.NORMAL);
            //deserialize ingredients
            NonNullList<Ingredient> ingredients = readIngredients(JSONUtils.getJsonArray(json, "ingredients"));
            if (ingredients.isEmpty()) {
                throw new JsonParseException("No ingredients for fairy circle recipe");
            } else if (ingredients.size() > FairyCircleTileEntity.INVENTORY_SIZE) {
                throw new JsonParseException("Too many ingredients for fairy circle recipe the max is " + FairyCircleTileEntity.INVENTORY_SIZE);
            }
            //deserialize recipe time
            int time = JSONUtils.getInt(json, "recipetime");
            //deserialize result
            JsonObject resultJson = JSONUtils.getJsonObject(json, "result");
            //deserialize result mode
            FairyCircleRecipeMode resultMode = readMode(resultJson, requiredMode);
            //deserialize result item stack
            ItemStack resultStack = ItemStack.EMPTY;
            if (JSONUtils.hasField(resultJson, "item")) {
                resultStack = ShapedRecipe.deserializeItem(resultJson);
            }
            return new FairyCircleRecipe(recipeId, requiredMode, ingredients, time, resultMode, resultStack);
        }

        public FairyCircleRecipe read(@Nonnull ResourceLocation recipeId, PacketBuffer buffer) {
            FairyCircleRecipeMode requiredMode = FairyCircleRecipeMode.byName(buffer.readString(32767));
            int size = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
            for (int j = 0; j < ingredients.size(); ++j) {
                ingredients.set(j, Ingredient.read(buffer));
            }
            int time = buffer.readVarInt();
            FairyCircleRecipeMode resultMode = FairyCircleRecipeMode.byName(buffer.readString(32767));
            ItemStack resultStack = buffer.readItemStack();
            return new FairyCircleRecipe(recipeId, requiredMode, ingredients, time, resultMode, resultStack);
        }

        public void write(PacketBuffer buffer, FairyCircleRecipe recipe) {
            buffer.writeString(recipe.requiredMode.getName());
            buffer.writeVarInt(recipe.ingredients.size());
            for (Ingredient ingredient : recipe.ingredients) {
                ingredient.write(buffer);
            }
            buffer.writeVarInt(recipe.recipeTime);
            buffer.writeString(recipe.resultMode.getName());
            buffer.writeItemStack(recipe.resultStack);
        }

        private static FairyCircleRecipeMode readMode(@Nonnull JsonObject jsonObject, @Nonnull FairyCircleRecipeMode defaultMode) {
            FairyCircleRecipeMode mode = defaultMode;
            try {
                String modeString = JSONUtils.getString(jsonObject, "mode");
                mode = FairyCircleRecipeMode.byName(modeString);
                if (mode == null) {
                    throw new JsonParseException("Mode " + modeString + " does not exist");
                }
            } catch (Exception ignored) {
            }
            return mode;
        }

        private static NonNullList<Ingredient> readIngredients(JsonArray jsonArray) {
            NonNullList<Ingredient> nonnulllist = NonNullList.create();

            for (int i = 0; i < jsonArray.size(); ++i) {
                Ingredient ingredient = Ingredient.deserialize(jsonArray.get(i));
                if (!ingredient.hasNoMatchingItems()) {
                    nonnulllist.add(ingredient);
                }
            }

            return nonnulllist;
        }
    }
}
