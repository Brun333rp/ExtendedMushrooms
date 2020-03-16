package cech12.extendedmushrooms.api.recipe;

public enum FairyCircleRecipeMode {

    NORMAL(0, "normal"),
    FAIRY(1, "fairy"),
    WITCH(2, "witch");

    private int id;
    private String name;

    FairyCircleRecipeMode(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public static FairyCircleRecipeMode byName(String modeName) {
        for (FairyCircleRecipeMode mode : FairyCircleRecipeMode.values()) {
            if (mode.name.equals(modeName)) {
                return mode;
            }
        }
        return null;
    }

}
