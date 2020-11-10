package cech12.extendedmushrooms.init;

import cech12.extendedmushrooms.ExtendedMushrooms;
import cech12.extendedmushrooms.api.entity.ExtendedMushroomsEntityTypes;
import cech12.extendedmushrooms.client.renderer.entity.MushroomBoatRenderer;
import cech12.extendedmushrooms.client.renderer.entity.FairyRenderer;
import cech12.extendedmushrooms.client.renderer.entity.MushroomSheepRenderer;
import cech12.extendedmushrooms.config.Config;
import cech12.extendedmushrooms.entity.item.MushroomBoatEntity;
import cech12.extendedmushrooms.entity.passive.FairyEntity;
import cech12.extendedmushrooms.entity.passive.MushroomSheepEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import static cech12.extendedmushrooms.api.entity.ExtendedMushroomsEntityTypes.*;

@Mod.EventBusSubscriber(modid= ExtendedMushrooms.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        registerEntity("fairy", FAIRY);
        registerEntity("mushroom_boat", MUSHROOM_BOAT);
        registerEntity("mushroom_sheep", MUSHROOM_SHEEP);
    }

    private static <T extends Entity> EntityType<T> registerEntity(String key, EntityType<T> entityType) {
        entityType.setRegistryName(new ResourceLocation(ExtendedMushrooms.MOD_ID, key));
        ForgeRegistries.ENTITIES.register(entityType);
        return entityType;
    }

    public static void registerAttributes() {
        GlobalEntityTypeAttributes.put((EntityType<MushroomSheepEntity>) MUSHROOM_SHEEP, MushroomSheepEntity.registerAttributes().create());
    }

    /**
     * Setup renderers for entities. Is called at mod initialisation.
     */
    @OnlyIn(Dist.CLIENT)
    public static void setupRenderers() {
        RenderingRegistry.registerEntityRenderingHandler((EntityType<FairyEntity>) FAIRY, FairyRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler((EntityType<MushroomBoatEntity>) MUSHROOM_BOAT, MushroomBoatRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler((EntityType<MushroomSheepEntity>) MUSHROOM_SHEEP, MushroomSheepRenderer::new);
    }

    /**
     * Add registered entities to biomes. Is called at mod initialisation.
     */
    public static void addEntitiesToBiomes(BiomeLoadingEvent event) {
        //add Mushroom Sheep to Mushroom Biomes
        if (event.getCategory().equals(Biome.Category.MUSHROOM)) {
            if (Config.MUSHROOM_SHEEP_ENABLED.get()) {
                event.getSpawns().withSpawner(EntityClassification.CREATURE,
                        new MobSpawnInfo.Spawners(ExtendedMushroomsEntityTypes.MUSHROOM_SHEEP,
                                Config.MUSHROOM_SHEEP_SPAWN_WEIGHT.get(),
                                Config.MUSHROOM_SHEEP_SPAWN_MIN_GROUP_COUNT.get(),
                                Config.MUSHROOM_SHEEP_SPAWN_MAX_GROUP_COUNT.get()));
            }
        }
    }
}
