package io.github.racoondog.recipedl.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(ShapelessRecipe.class)
public abstract class ShapelessRecipeMixin implements CraftingRecipe {
    @Shadow @Final CraftingRecipeCategory category;
    @Shadow @Final ItemStack output;

    @Override
    public JsonObject serializeToJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", Registries.RECIPE_SERIALIZER.getId(getSerializer()).toString());
        if (!getGroup().isEmpty()) object.addProperty("group", getGroup());
        if (category != CraftingRecipeCategory.MISC) object.addProperty("category", category.asString());
        DefaultedList<Ingredient> ingredients = getIngredients();
        JsonArray ingredientsArray = new JsonArray();
        for (var ingredient : ingredients) ingredientsArray.add(ingredient.toJson());

        JsonObject result = new JsonObject();
        result.addProperty("item", Registries.ITEM.getId(output.getItem()).toString());
        if (output.getCount() != 1) result.addProperty("count", output.getCount());
        object.add("result", result);

        return object;
    }
}
