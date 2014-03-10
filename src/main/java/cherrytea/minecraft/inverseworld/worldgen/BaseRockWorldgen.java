package cherrytea.minecraft.inverseworld.worldgen;

import java.util.Random;

import cherrytea.minecraft.inverseworld.world.gen.InverseChunkProvider;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import cpw.mods.fml.common.IWorldGenerator;

public class BaseRockWorldgen implements IWorldGenerator {

    RockGen rock1;
    RockGen rock2;
    RockGen rock3;
    
    /**
     * Default constructor. Sets some default variables.
     */
    public BaseRockWorldgen()
    {
        rock1 = new RockGen(8, 16);
        rock2 = new RockGen(12, 24);
        rock3 = new RockGen(16, 32);
    }
    
    /**
     * Generates a floating rock at the designated point
     * @param random The random number generator
     * @param chunkX Chunk's X location
     * @param chunkZ Chunk's Z location
     * @param world The world to generate in
     * @param chunkGenerator The chunkGenerator
     * @param chunkProvider The chunkProvider
     */
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
            IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if(random.nextInt(10) == 0) //chunkProvider instanceof InverseChunkProvider && 
        {
            int realX = chunkX * 16 + random.nextInt(16);
            int realY = random.nextInt(108) + 20;
            int realZ = chunkZ * 16 + random.nextInt(16);
            int size = random.nextInt(8);

            long start = System.currentTimeMillis();

            if (size < 5)
            {
                rock1.generate(world, random, realX, realY, realZ);
                System.out.print("Small: ");
            }
            else if (size < 7)
            {
                rock2.generate(world, random, realX, realY, realZ);
                System.out.print("Medium: ");
            }
            else
            {
                rock3.generate(world, random, realX, realY, realZ);
                System.out.print("Large: ");
            }

            long end = System.currentTimeMillis();
            System.out.println(end - start + " milliseconds");
        }
        
    }

}
