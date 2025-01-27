package cech12.extendedmushrooms;

import cech12.extendedmushrooms.compat.ModFeatureEnabledCondition;
import cech12.extendedmushrooms.config.Config;
import cech12.extendedmushrooms.entity.ai.goal.EatMushroomGoal;
import cech12.extendedmushrooms.entity.passive.MushroomSheepEntity;
import cech12.extendedmushrooms.init.ModBlocks;
import cech12.extendedmushrooms.init.ModEntities;
import cech12.extendedmushrooms.init.ModFeatures;
import cech12.extendedmushrooms.init.ModTags;
import cech12.extendedmushrooms.init.ModTileEntities;
import cech12.extendedmushrooms.init.ModVanillaCompat;
import cech12.extendedmushrooms.item.crafting.MushroomArrowRecipe;
import cech12.extendedmushrooms.item.crafting.MushroomBrewingRecipe;
import cech12.extendedmushrooms.loot_modifiers.MushroomCapLootModifier;
import cech12.extendedmushrooms.loot_modifiers.MushroomStemLootModifier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HugeMushroomBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.potion.Potions;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nonnull;

@Mod(ExtendedMushrooms.MOD_ID)
@Mod.EventBusSubscriber
public class ExtendedMushrooms {

    public static final String MOD_ID = "extendedmushrooms";

    // Use for data generation and development
    public static final boolean DEVELOPMENT_MODE = Boolean.parseBoolean(System.getProperty("extendedmushrooms.developmentMode", "false"));

    public ExtendedMushrooms() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON, "extendedmushrooms-common.toml");

        final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        eventBus.addListener(this::setup);
        eventBus.addListener(this::clientSetup);
        eventBus.addGenericListener(GlobalLootModifierSerializer.class, this::onRegisterModifierSerializers);

        // Register an event with the mod specific event bus for mod own recipes.
        eventBus.addGenericListener(IRecipeSerializer.class, this::registerRecipeSerializers);

        ModBlocks.registerBlocks(eventBus);
    }

    private void setup(final FMLCommonSetupEvent event) {
        ModVanillaCompat.setup();

        //add potion recipes
        //BrewingRecipeRegistry.addRecipe(new MushroomBrewingRecipe(ModTags.ForgeItems.MUSHROOMS_GLOWSHROOM, Potions.NIGHT_VISION)); //overpowered
        BrewingRecipeRegistry.addRecipe(new MushroomBrewingRecipe(ModTags.ForgeItems.MUSHROOMS_POISONOUS, Potions.POISON));
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ModBlocks.setupRenderLayers();
        ModEntities.setupRenderers();
        ModTileEntities.setupRenderers(event);
    }

    private void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        //serializer for conditions
        CraftingHelper.register(ModFeatureEnabledCondition.Serializer.INSTANCE);

        // Register the recipe serializer.
        event.getRegistry().register(MushroomArrowRecipe.SERIALIZER);
    }

    /**
     * Add some loot modifiers to be compatible with other mods and change some loot behaviour of vanilla Minecraft.
     */
    public void onRegisterModifierSerializers(@Nonnull final RegistryEvent.Register<GlobalLootModifierSerializer<?>> event) {
        event.getRegistry().register(
                new MushroomCapLootModifier.Serializer().setRegistryName(MOD_ID, "mushroom_cap_harvest")
        );
        event.getRegistry().register(
                new MushroomStemLootModifier.Serializer().setRegistryName(MOD_ID, "mushroom_stem_harvest")
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBiomeLoadingEvent(final BiomeLoadingEvent event) {
        ModEntities.addEntitiesToBiomes(event);
        ModFeatures.addFeaturesToBiomes(event);
    }

    /**
     * Add stripping behaviour to mushroom stems
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockState blockState = event.getWorld().getBlockState(event.getPos());
        ItemStack itemStack = event.getPlayer().getHeldItem(event.getHand());
        //check for mushroom stem and axe
        if (itemStack.getToolTypes().contains(ToolType.AXE)) {
            //get stripped block from stripping map
            Block strippedBlock = ModBlocks.BLOCK_STRIPPING_MAP.get(blockState.getBlock());
            if (strippedBlock != null) {
                //play sound
                event.getWorld().playSound(event.getPlayer(), event.getPos(), SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (!event.getWorld().isRemote) {
                    //copy block state orientation
                    BlockState strippedBlockState = strippedBlock.getDefaultState();
                    if (blockState.hasProperty(HugeMushroomBlock.UP)) strippedBlockState = strippedBlockState.with(HugeMushroomBlock.UP, blockState.get(HugeMushroomBlock.UP));
                    if (blockState.hasProperty(HugeMushroomBlock.DOWN)) strippedBlockState = strippedBlockState.with(HugeMushroomBlock.DOWN, blockState.get(HugeMushroomBlock.DOWN));
                    if (blockState.hasProperty(HugeMushroomBlock.NORTH)) strippedBlockState = strippedBlockState.with(HugeMushroomBlock.NORTH, blockState.get(HugeMushroomBlock.NORTH));
                    if (blockState.hasProperty(HugeMushroomBlock.EAST)) strippedBlockState = strippedBlockState.with(HugeMushroomBlock.EAST, blockState.get(HugeMushroomBlock.EAST));
                    if (blockState.hasProperty(HugeMushroomBlock.SOUTH)) strippedBlockState = strippedBlockState.with(HugeMushroomBlock.SOUTH, blockState.get(HugeMushroomBlock.SOUTH));
                    if (blockState.hasProperty(HugeMushroomBlock.WEST)) strippedBlockState = strippedBlockState.with(HugeMushroomBlock.WEST, blockState.get(HugeMushroomBlock.WEST));
                    //replace block
                    event.getWorld().setBlockState(event.getPos(), strippedBlockState, 11);
                    //do the item damage
                    if (event.getPlayer() != null) {
                        itemStack.damageItem(1, event.getPlayer(), (p_220040_1_) -> {
                            p_220040_1_.sendBreakAnimation(event.getHand());
                        });
                    }
                }
                event.setCanceled(true);
                event.setCancellationResult(ActionResultType.SUCCESS);
            }
        }
    }

    /**
     * Remove dye behaviour from mushroom sheeps.
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack itemStack = event.getPlayer().getHeldItem(event.getHand());
        Entity entity = event.getTarget();
        //check for dye item and mushroom sheep entity
        if (entity instanceof MushroomSheepEntity && itemStack.getItem() instanceof DyeItem) {
            event.setCanceled(true);
            event.setCancellationResult(ActionResultType.FAIL);
        }
    }

    /**
     * Add eat mushroom goal to sheep entities when configured.
     */
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (Config.SHEEP_EAT_MUSHROOM_FROM_GROUND_ENABLED.get()) {
            if (event.getEntity() instanceof SheepEntity) { //also mushroom sheep
                SheepEntity sheep = ((SheepEntity) event.getEntity());
                sheep.goalSelector.addGoal(5, new EatMushroomGoal(sheep));
            }
        }
    }

}
