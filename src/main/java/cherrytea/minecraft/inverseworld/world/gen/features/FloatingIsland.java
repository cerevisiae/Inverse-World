package cherrytea.minecraft.inverseworld.world.gen.features;

import java.util.Random;

import org.bukkit.util.noise.SimplexOctaveGenerator;

import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.terraingen.ChunkProviderEvent;

public class FloatingIsland
{
    World theWorld;
    IChunkProvider chunkProvider;
    Random worldRNG;
    int sizeXZ;
    int sizeY;
    int minHeight;
    double scale;
    boolean offset;
    
    //Noise Generators
    SimplexOctaveGenerator islandGen;
    SimplexOctaveGenerator heightGen;
    SimplexOctaveGenerator deformYGen;
    SimplexOctaveGenerator caveGen;
    
    /**
     * Constructor to generate floating islands
     * @param world The world object
     * @param cProvider The chunk provider
     * @param random The random number generator
     * @param XZ The horizontal size of the area
     * @param Y The vertical size of the area
     * @param islandScale The scale to use when generating
     * @param centerOffset If the island should be center offset
     */
    public FloatingIsland(World world, IChunkProvider cProvider, Random random, int XZ, int Y, double islandScale, boolean centerOffset)
    {
        theWorld = world;
        chunkProvider = cProvider;
        worldRNG = random;
        
        this.sizeXZ = XZ / 4;
        this.sizeY = Y / 8;
        
        offset = centerOffset;
        
        this.scale = islandScale;
        
        this.islandGen = new SimplexOctaveGenerator(this.worldRNG, 5);
        this.caveGen = new SimplexOctaveGenerator(this.worldRNG, 5);
        this.deformYGen = new SimplexOctaveGenerator(this.worldRNG, 2);
        this.heightGen = new SimplexOctaveGenerator(this.worldRNG, 1);
    }
    
    /**
     * Generates the noise array for the random floating rock generation
     * @param noiseArray An empty noise array
     * @param xPos A chunk's X location
     * @param yPos Will be 0 because chunks do not have y position
     * @param zPos A chunk's Z location
     * @param xSize
     * @param ySize
     * @param zSize
     */
    public double[] generate (double[] noiseArray, int xPos, int yPos, int zPos, int xSize, int ySize, int zSize)
    {
        //Alert forge of an attempt to initialize a noise field
        ChunkProviderEvent.InitNoiseField event = new ChunkProviderEvent.InitNoiseField(this.chunkProvider, noiseArray, xPos, yPos, zPos, xSize, ySize, zSize);
        MinecraftForge.EVENT_BUS.post(event);
        
        this.islandGen.setScale(this.scale);
        this.islandGen.setYScale(this.scale / 2);
        
        this.deformYGen.setScale(1/8.0);
        
        this.heightGen.setScale(1/32.0);
        
        //Make sure we're allowed to.
        if (event.getResult() == Result.DENY)
            return event.noisefield; //Do not initialize it.
        if (noiseArray == null)
        {
            noiseArray = new double[xSize * ySize * zSize];
        }
        
        //Used to access the noiseArray in order
        int noiseArrayInt = 0;
        
        for (int offsetX = 0; offsetX < xSize; ++offsetX)
        {
            for (int offsetZ = 0; offsetZ < zSize; ++offsetZ)
            {
                
                double realX = offsetX + xPos;
                double realZ = offsetZ + zPos;
                double deformDensityY = this.deformYGen.noise(realX, realZ, 0.5, 0.5);

                float x;
                float z;
                
                //If the island should be offset
                if(this.offset)
                {
                    x = (float) (Math.abs(realX + this.sizeXZ / 2) % this.sizeXZ) / this.sizeXZ;
                    z = (float) (Math.abs(realZ + this.sizeXZ / 2) % this.sizeXZ) / this.sizeXZ;
                }
                else
                {
                    x = (float) (Math.abs(realX) % this.sizeXZ) / this.sizeXZ;
                    z = (float) (Math.abs(realZ) % this.sizeXZ) / this.sizeXZ;
                }
                
                if (x == 0 && z == 0)
                {
                    double heightVal = this.heightGen.noise(realX, realZ, 0.5, 0.5, true);
                    System.out.println(realX + " " + realZ);
                    this.minHeight = (int) (((heightVal + 1) / 2) * (16 - this.sizeY));
                }
                
                //Make the noise fluctuate between minBiomeHeight and maxBiomeHeight
                
                for (int offsetY = 0; offsetY < ySize; ++offsetY)
                {
                    double finalDensity = 0.0;

                    float y = (((float) offsetY - this.minHeight ) / (float) this.sizeY);
                    
                    
                    if (offsetY > this.minHeight && offsetY < this.minHeight + this.sizeY)
                    {

                        double floatingRock = this.islandGen.noise(realX, offsetY, realZ, 0.8, 0.5);
                        
                        double centerFalloff = 0.1 / (Math.pow((x - 0.5) * 1.5, 2) + Math.pow((y - 1.0) * 0.8, 2) + Math.pow((z - 0.5) * 1.5, 2));

                        //Round the top and bottom if they get too close to the edges
                        //Prevents the tops from being completely flat
                        double plateauFalloff = 0.0;
                        if( 0.1 < y && y < 0.2)
                        {
                            plateauFalloff = 1 + ( y - 0.2 ) * 10.0;
                        }
                        else if( 0.2 <= y && y <= 0.8)
                        {
                            plateauFalloff = 1.0;
                        }
                        else if(0.8 < y && y < 0.9)
                        {
                            plateauFalloff = 1.0 - ( y - 0.8 ) * 10.0;
                        }
                        
                        floatingRock = floatingRock * centerFalloff * plateauFalloff;

                        //Deform the islands on the Y axis
                        double deformY = 1.0;
                        if ( y < 0.6 && deformDensityY > 0.8)
                        {
                            deformY = deformDensityY * y;
                        }
                        else
                        {
                            deformY = 1.0 * (1 - deformDensityY) + deformDensityY * deformDensityY * y;
                        }
                        floatingRock *= deformY;

                        finalDensity = floatingRock;
                    }

                    noiseArray[noiseArrayInt] = finalDensity;
                    ++noiseArrayInt;
                }
            }
        }

        return noiseArray;
    }
    /**
     * Large Island generation
     * @param noiseArray An empty noise array
     * @param xPos A chunk's X location
     * @param yPos Will be 0 because chunks do not have y position
     * @param zPos A chunk's Z location
     * @param xSize
     * @param ySize
     * @param zSize
     */
    /*    private double[] generate (double[] noiseArray, int xPos, int yPos, int zPos, int xSize, int ySize, int zSize)
    {
        //Alert forge of an attempt to initialize a noise field
        ChunkProviderEvent.InitNoiseField event = new ChunkProviderEvent.InitNoiseField(this.chunkProvider, noiseArray, xPos, yPos, zPos, xSize, ySize, zSize);
        MinecraftForge.EVENT_BUS.post(event);
        
        //Make sure we're allowed to.
        if (event.getResult() == Result.DENY)
            return event.noisefield; //Do not initialize it.
        if (noiseArray == null)
        {
            noiseArray = new double[xSize * ySize * zSize];
        }
        
        this.islandGen.setScale(1/64.0);
        this.islandGen.setYScale(1/128.0);

        this.caveGen.setScale(1/16.0);
        this.caveGen.setYScale(1/8.0);

        this.deformYGen.setScale(1/8.0);
        
        this.heightGen.setScale(1/16.0);
        
        //Used to access the noiseArray in order
        int noiseArrayInt = 0;

        for (int offsetX = 0; offsetX < xSize; ++offsetX)
        {
            for (int offsetZ = 0; offsetZ < zSize; ++offsetZ)
            {
                
                double realX = offsetX + xPos;
                double realZ = offsetZ + zPos;
                
                for (int offsetY = 0; offsetY < ySize; ++offsetY)
                {
                    double finalDensity = 0.0;

                    ///////////////////////////
                    //Large Island Generation//
                    ///////////////////////////
                    double sectionSizeXZ = 256;
                    double sectionSizeY = 96;
                    sectionSizeY /= 8;
                    sectionSizeXZ /= 4;
                    float x = (float) (Math.abs(realX + sectionSizeXZ / 2) % sectionSizeXZ);
                    float y = (float) (offsetY / sectionSizeY);
                    float z = (float) (Math.abs(realZ + sectionSizeXZ / 2) % sectionSizeXZ);
                    x /= sectionSizeXZ;
                    z /= sectionSizeXZ;
                    
                    double largeIsland = this.islandGen.noise(realX, offsetY, realZ, 1.5, 0.5, true);
                    double deformDensityY = this.deformYGen.noise(realX, realZ, 0.5, 0.5);
                    double height = this.heightGen.noise(realX, realZ, 0.5, 0.5, true);

                    //Make the noise between 0.5 and 0.8
                    double randomDeformRidges = ( (height + 1) / (20 / 3) ) + 0.5;
                    
                    double centerFalloff = 0.1 / (Math.pow((x - 0.5) * 1.5, 2) + Math.pow((y - 1.0) * 0.8, 2) + Math.pow((z - 0.5) * 1.5, 2));
                    largeIsland *= centerFalloff;
                    
                    //double heightMod = (height * height);
                    
                    //Round the top and bottom if they get too close to the edges
                    //Prevents the tops from being completely flat
                    double plateauFalloff = 0.0;
                    if( 0.1 < y && y < 0.2)
                    {
                        plateauFalloff = 1 + ( y - 0.2 ) * 10.0;
                    }
                    else if(0.2 <= y && y < randomDeformRidges)
                    {
                        plateauFalloff = 1.0;
                    }
                    else if(randomDeformRidges <= y && y <= 0.8)
                    {
                        ++height;
                        height /= (20 / 3);
                        //plateauFalloff = y + height;
                        //plateauFalloff = 1.0 - ( y - height ) * 10.0;
                        plateauFalloff = 1.0 - (height * height * ( y - randomDeformRidges ));
                    }
                    else if(0.8 < y && y < 0.9)
                    {
                        plateauFalloff = 1.0 - ( y - 0.8 ) * 10.0;
                    }
                    
                    largeIsland *= plateauFalloff;
                    
                    //Deform the islands on the Y axis

                    double deformY = 1.0 * (1 - deformDensityY) + deformDensityY * deformDensityY * y;
                    largeIsland *= deformY;
                    
                    //Generate caves to cut into the large islands
                    double caveDensity = this.caveGen.noise(realX, offsetY, realZ, 0.5, 0.5);
                    caveDensity = Math.pow(caveDensity, 3);
                    
                    //Apply caves to the islands
                    if (caveDensity > 0.5)
                    {
                        largeIsland *= (1 - caveDensity) + caveDensity * 0.0;
                    }
                    
                    //Apply hills tot het op of the islands
                    height = (height + 1) / 5 + 0.7;
                    if(y > height)
                    {
                        largeIsland *= (1 - y);
                    }
                    
                    if(offsetY > sectionSizeY)
                    {
                        largeIsland = 0.0;
                    }
                    finalDensity = largeIsland;
                    
                    noiseArray[noiseArrayInt] = finalDensity;
                    ++noiseArrayInt;
                }
            }
        }

        return noiseArray;
    }*/
}
