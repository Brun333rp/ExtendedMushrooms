package cech12.extendedmushrooms.block.mushrooms;

import cech12.extendedmushrooms.api.block.ExtendedMushroomsBlocks;
import cech12.extendedmushrooms.init.ModFeatures;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.BigMushroomFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;

import javax.annotation.Nullable;
import java.util.Random;

public class Glowshroom extends MegaMushroom {

    public static BigMushroomFeatureConfig getConfig() {
        return new BigMushroomFeatureConfig(new SimpleBlockStateProvider(getDefaultCapState(ExtendedMushroomsBlocks.GLOWSHROOM_CAP)),
                new SimpleBlockStateProvider(getDefaultStemState(ExtendedMushroomsBlocks.GLOWSHROOM_STEM)), 2);
    }

    @Nullable
    @Override
    protected ConfiguredFeature<?, ?> getBigMushroomFeature(Random var1, boolean var2) {
        return ModFeatures.BIG_GLOWSHROOM.withConfiguration(getConfig());
    }

    @Nullable
    @Override
    protected ConfiguredFeature<?, ?> getMegaMushroomFeature(Random var1) {
        return ModFeatures.MEGA_GLOWSHROOM.withConfiguration(getConfig());
    }


}
