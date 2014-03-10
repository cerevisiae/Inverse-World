package cherrytea.minecraft.inverseworld.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraftforge.common.MinecraftForge;

public class InverseBlocks {
    
    public static final Block luminousGrass = new BlockLuminousGrass(185);
    public static final Block cloud = new BlockCloud(4000);
    
    public static void init ( )
    {
        registerBlocks();
    }
    
    /**
     * Registers the blocks added by the mod with the game
     */
    private static void registerBlocks()
    {
        GameRegistry.registerBlock(luminousGrass, "LuminousGrass");
        LanguageRegistry.addName(luminousGrass, "Luminous Grass");
        MinecraftForge.setBlockHarvestLevel(luminousGrass, "shovel", 0);
        
        GameRegistry.registerBlock(cloud, "Cloud");
        LanguageRegistry.addName(cloud, "Cloud");
    }
}
