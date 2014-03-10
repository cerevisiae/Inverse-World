package cherrytea.minecraft.inverseworld;

import java.util.Random;

import cherrytea.minecraft.inverseworld.blocks.BlockLuminousGrass;
import cherrytea.minecraft.inverseworld.blocks.InverseBlocks;
import cherrytea.minecraft.inverseworld.items.InverseItems;
import cherrytea.minecraft.inverseworld.network.PacketHandler;
import cherrytea.minecraft.inverseworld.world.gen.InverseWorldProvider;
import cherrytea.minecraft.inverseworld.worldgen.BaseRockWorldgen;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "InverseWorld", name = "Inverse World", version = "0.0.0")
@NetworkMod(clientSideRequired=true, serverSideRequired=false, 
        channels={"GenericRandom"}, packetHandler = PacketHandler.class)
public class InverseWorld
{
    // The instance of your mod that Forge uses.
    @Instance("InverseWorld")
    public static InverseWorld instance;

    // Says where the client and server 'proxy' code is loaded.
    @SidedProxy(clientSide = "cherrytea.minecraft.inverseworld.client.ClientProxy", serverSide = "cherrytea.minecraft.inverseworld.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        DimensionManager.unregisterProviderType(0);
        DimensionManager.registerProviderType(0, InverseWorldProvider.class, true);
    }

    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        proxy.registerRenderers();
        InverseBlocks.init();
        InverseItems.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        // Stub Method
    }
    
}