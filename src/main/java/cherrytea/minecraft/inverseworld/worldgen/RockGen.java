package cherrytea.minecraft.inverseworld.worldgen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class RockGen extends WorldGenerator {

	int rockSizeX;
	int rockSizeY;
	private double[] rockField;
    
	/**
	 * Sets the rock size and height to generate within.
	 * @param sizeX Horizontal size of the floating rock
	 * @param sizeY Viertical size of the floating rock
	 */
	public RockGen(int sizeX, int sizeY)
	{
		rockSizeX = sizeX;
		rockSizeY = sizeY;
	}
	
	@Override
	public boolean generate(World world, Random random, int x, int y, int z) {
		
		for (int offsetX = 0; offsetX < rockSizeX; ++offsetX)
        {
        	int realX = offsetX + x;
        	
            for (int offsetZ = 0; offsetZ < rockSizeX; ++offsetZ)
            {
            	int realZ = offsetZ + z;

            	int heightCounter = random.nextInt(3) + 2;
            	boolean top = true;
            	
                for (int offsetY = rockSizeY - 1; offsetY >= 0 ; --offsetY)
                {
                	int realY = offsetY + y;
                	
                	//Get the distance from the center of the rock
                	float centerX = (float) offsetX / (float) rockSizeX;
                	float centerY = (float) offsetY / (float) rockSizeY;
                	float centerZ = (float) offsetZ / (float) rockSizeX;

                	//Generates noise for the rocks
                	double rockGen = 1.0;
                	
                	//The center falloff
                	double centerFalloff = 0.1 / (Math.pow((centerX - 0.5) * 1.5, 2) + Math.pow((centerY - 1.0) * 0.8, 2) + Math.pow((centerZ - 0.5) * 1.5, 2));

                	//Round the top and bottom if they get too close to the edges
                	//Prevents the tops from being completely flat
                	double plateauFalloff = 0.0;
                	if( 0.1 < centerY && centerY < 0.2)
                	{
                    	plateauFalloff = 1 + ( centerY - 0.2 ) * 10.0;
                	}
                	else if( 0.2 <= centerY && centerY <= 0.8)
                	{
                        plateauFalloff = 1.0;
                    }
                    else if(0.8 < centerY && centerY < 0.9)
                    {
                    	plateauFalloff = 1.0 - ( centerY - 0.8 ) * 10.0;
                    }

                	rockGen *= centerFalloff * plateauFalloff;
                	
                	//Set blocks
                	if (rockGen > 0.3)
                	{
                		int blockID = Block.stone.blockID;
                		
                		//Place grass on top
                		if(heightCounter > 0)
                		{
                			if (top)
                			{
                    			top = false;
                    			blockID = Block.grass.blockID;
                			}
                			else
                			{
                    			blockID = Block.dirt.blockID;
                			}
                			
                			--heightCounter;
                		}
                		
                		this.setBlock(world, realX, realY, realZ, blockID);
                	}
                	
                }
            }
        }

		return false;
	}

}
