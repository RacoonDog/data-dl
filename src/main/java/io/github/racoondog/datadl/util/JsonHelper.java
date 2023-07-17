package io.github.racoondog.datadl.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;

public class JsonHelper {
    public static JsonObject addObject(JsonObject parent, String property) {
        JsonObject child = new JsonObject();
        parent.add(property, child);
        return child;
    }

    public static JsonObject addObject(JsonArray parent) {
        JsonObject child = new JsonObject();
        parent.add(child);
        return child;
    }

    public static JsonArray addArray(JsonObject parent, String property) {
        JsonArray child = new JsonArray();
        parent.add(property, child);
        return child;
    }

    public static JsonArray addArray(JsonArray parent) {
        JsonArray child = new JsonArray();
        parent.add(child);
        return child;
    }

    public static JsonObject serializeAdvancement(Advancement advancement) {
        JsonObject object = new JsonObject();
        if (advancement.getDisplay() != null) object.add("display", advancement.getDisplay().toJson());
        if (advancement.getParent() != null) object.addProperty("parent", advancement.getParent().getId().toString());

        JsonObject criteria = JsonHelper.addObject(object, "criteria");
        for (var criterion : advancement.getCriteria().entrySet()) criteria.add(criterion.getKey(), criterion.getValue().toJson());

        if (advancement.getRequirementCount() > 0) {
            JsonArray requirements = JsonHelper.addArray(object, "requirements");
            for (var requirement : advancement.getRequirements()) {
                JsonArray sublist = JsonHelper.addArray(requirements);
                for (var criterion : requirement) sublist.add(criterion);
            }
        }

        if (advancement.getRewards() != AdvancementRewards.NONE) object.add("rewards", advancement.getRewards().toJson());

        return object;
    }
}
