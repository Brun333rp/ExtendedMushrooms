package cech12.extendedmushrooms;

import cech12.extendedmushrooms.api.block.ExtendedMushroomsBlocks;
import cech12.extendedmushrooms.api.recipe.ExtendedMushroomsRecipeTypes;
import cech12.extendedmushrooms.api.recipe.FairyCircleRecipe;
import cech12.extendedmushrooms.block.FairyCircleBlock;
import cech12.extendedmushrooms.config.Config;
import cech12.extendedmushrooms.entity.passive.MushroomSheepEntity;
import cech12.extendedmushrooms.init.ModBlocks;
import cech12.extendedmushrooms.init.ModEntities;
import cech12.extendedmushrooms.init.ModFeatures;
import cech12.extendedmushrooms.init.ModTileEntities;
import cech12.extendedmushrooms.init.ModVanillaCompat;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MushroomBlock;
import net.minecraft.entity.Entity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Map;

@Mod(ExtendedMushrooms.MOD_ID)
@Mod.EventBusSubscriber
public class ExtendedMushrooms {

    public static final String MOD_ID = "extendedmushrooms";

    public ExtendedMushrooms() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON, "extendedmushrooms-common.toml");

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        // Register an event with the mod specific event bus for mod own recipes.
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(IRecipeSerializer.class, this::registerRecipeSerializers);
    }

    private void setup(final FMLCommonSetupEvent event) {
        ModVanillaCompat.setup();
        ModBlocks.addBlocksToBiomes();
        ModEntities.addEntitiesToBiomes();
        ModFeatures.addFeaturesToBiomes();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ModBlocks.setupRenderLayers();
        ModEntities.setupRenderers();
        ModTileEntities.setupRenderers();
    }

    private void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        // let other mods register recipes
        Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(ExtendedMushroomsRecipeTypes.FAIRY_CIRCLE.toString()),
                ExtendedMushroomsRecipeTypes.FAIRY_CIRCLE);

        // Register the recipe serializer.
        event.getRegistry().register(FairyCircleRecipe.SERIALIZER);
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
                    //replace block
                    event.getWorld().setBlockState(event.getPos(), strippedBlock.getDefaultState(), 11);
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
     * Add Fairy Circle generation to all mushroom blocks
     */
    @SubscribeEvent
    public static void onNeighbourChanged(BlockEvent.NeighborNotifyEvent event) {
        IWorld world = event.getWorld();
        BlockPos blockPos = event.getPos();
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.getBlock() != ExtendedMushroomsBlocks.FAIRY_CIRCLE) {
            for (Direction direction : event.getNotifiedSides()) {
                BlockPos neighbourPos = blockPos.offset(direction);
                if (world.getBlockState(neighbourPos).getBlock() instanceof MushroomBlock) {
                    //neighbour is mushroom?
                    FairyCircleBlock.fairyCirclePlaceCheck(world, neighbourPos);
                } else if (world.getBlockState(neighbourPos.up()).getBlock() instanceof MushroomBlock) {
                    //for ground blocks - block above neighbour is mushroom?
                    FairyCircleBlock.fairyCirclePlaceCheck(world, neighbourPos.up());
                }
            }
        }
    }


    /**
     * Copy of https://github.com/Minecraft-Forge-Tutorials/Custom-Json-Recipes/blob/master/src/main/java/net/darkhax/customrecipeexample/CustomRecipesMod.java
     *
     * This method lets you get all of the recipe data for a given recipe type. The existing
     * methods for this require an IInventory, and this allows you to skip that overhead. This
     * method uses reflection to get the recipes map, but an access transformer would also
     * work.
     *
     * @param recipeType The type of recipe to grab.
     * @param manager The recipe manager. This is generally taken from a World.
     * @return A map containing all recipes for the passed recipe type. This map is immutable
     *         and can not be modified.
     */
    public static Map<ResourceLocation, IRecipe<?>> getRecipes(IRecipeType<?> recipeType, RecipeManager manager) {
        final Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> recipesMap = ObfuscationReflectionHelper.getPrivateValue(RecipeManager.class, manager, "field_199522_d");
        return recipesMap.get(recipeType);
    }

}
