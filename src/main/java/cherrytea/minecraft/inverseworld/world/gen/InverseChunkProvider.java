package cherrytea.minecraft.inverseworld.world.gen;

import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.CAVE;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.MINESHAFT;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.RAVINE;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.SCATTERED_FEATURE;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.STRONGHOLD;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.VILLAGE;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.DUNGEON;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.ICE;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.LAKE;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.LAVA;

import java.util.List;
import java.util.Random;

import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexNoiseGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import cherrytea.minecraft.inverseworld.InverseWorld;
import cherrytea.minecraft.inverseworld.blocks.InverseBlocks;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.feature.MapGenScatteredFeature;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.terraingen.ChunkProviderEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

public class InverseChunkProvider implements IChunkProvider {

    /** Reference to the World object. */
    private World worldObj;
    
    /** RNG variable **/
    private Random worldRNG;
    
    /** A NoiseGeneratorOctaves used in generating terrain */
    private NoiseGeneratorOctaves stoneGen;

    public SimplexOctaveGenerator largeIslandGen;
    public SimplexOctaveGenerator smallIslandGen;
    public SimplexOctaveGenerator rockGen;
    
    public SimplexOctaveGenerator caveGen;
    
    public SimplexOctaveGenerator deformYGen;
    
    public SimplexOctaveGenerator heightGen;

    private double[] largeIslandField;
    private double[] smallIslandField;
    private double[] floatingRockField;

    /** Holds the data from stoneGen used in generating terrain */
    public double[] stoneData;

    /** The biomes that are used to generate the chunk */
    private BiomeGenBase[] biomesForGeneration;

    //World features
    
    /** Holds ravine generator */
    private MapGenBase ravineGenerator = new MapGenRavine();
    
    /** Holds cave generator */
    private MapGenBase caveGenerator = new MapGenCaves();
    
    /** For making some areas barren of grass */
    private double[] stoneNoise = new double[256];


    {
        caveGenerator = TerrainGen.getModdedMapGen(caveGenerator, CAVE);
        ravineGenerator = TerrainGen.getModdedMapGen(ravineGenerator, RAVINE);
    }
    
    /***
     * Constructor for the InverseChunkProvider
     * @param theWorld Reference to the world object
     * @param seed The seed used for world generation
     */
    public InverseChunkProvider(World theWorld, long seed) {
        this.worldObj = theWorld;
        this.worldRNG = new Random(seed);
        
        //Create the noise generation objects
        this.smallIslandGen = new SimplexOctaveGenerator(this.worldRNG, 5);
        this.caveGen = new SimplexOctaveGenerator(this.worldRNG, 1);

        this.rockGen = new SimplexOctaveGenerator(this.worldRNG, 1);

        this.largeIslandGen = new SimplexOctaveGenerator(this.worldRNG, 5);
        this.deformYGen     = new SimplexOctaveGenerator(this.worldRNG, 2);
        
        this.heightGen     = new SimplexOctaveGenerator(this.worldRNG, 5);


        this.stoneGen = new NoiseGeneratorOctaves(this.worldRNG, 4);
    }
    
    /***
     * Generates the shape of the terrain in the Inverse World.
     * @param chunkX The chunk to be generated's X location
     * @param chunkZ The chunk to be generated's Z location
     * @param lowerIDs
     * @return Nothing
     */
    public void generateTerrain (int chunkX, int chunkZ, byte[] lowerIDs)
    {
        
        //Reorganizes how chunks are accessed.
        //Must be a divisor of 16
        //Larger numbers create more strain, smaller numbers look ugly?
        byte noiseInitXZ = 4;
        int sizeX = noiseInitXZ + 1;
        int sizeZ = noiseInitXZ + 1;
        
        //noiseInitY * maxOffsetY is where max height the ground will generate to.
        int noiseInitY = 16;
        int maxOffsetY = 8;
        int sizeY = noiseInitY + 1;
        
        //The sea level. Can be used to generate all blocks under this to a liquid if desired
        //byte seaLevel = 63;
        
        //Gets the biomes that will be generated
        this.biomesForGeneration = this.worldObj.getWorldChunkManager().getBiomesForGeneration(this.biomesForGeneration, chunkX * 4 - 2, chunkZ * 4 - 2, sizeX + 5, sizeZ + 5);
        
        //Initializes the noise values
        this.largeIslandField = this.largeIslandNoiseField(this.largeIslandField, chunkX * noiseInitXZ, 0, chunkZ * noiseInitXZ, sizeX, sizeY, sizeZ);
        this.smallIslandField = this.smallIslandNoiseField(this.smallIslandField, chunkX * noiseInitXZ, 0, chunkZ * noiseInitXZ, sizeX, sizeY, sizeZ);
        this.floatingRockField = this.floatingRockNoiseField(this.floatingRockField, chunkX * noiseInitXZ, 0, chunkZ * noiseInitXZ, sizeX, sizeY, sizeZ);

        for (int iterX = 0; iterX < noiseInitXZ; ++iterX)
        {
            for (int iterZ = 0; iterZ < noiseInitXZ; ++iterZ)
            {
                for (int iterY = 0; iterY < noiseInitY; ++iterY)
                {
                    double noiseOffset1 = 0.125D;
                    //Large Islands
                    double large1 = this.largeIslandField[((iterX + 0) * sizeZ + iterZ + 0) * sizeY + iterY + 0];
                    double large2 = this.largeIslandField[((iterX + 0) * sizeZ + iterZ + 1) * sizeY + iterY + 0];
                    double large3 = this.largeIslandField[((iterX + 1) * sizeZ + iterZ + 0) * sizeY + iterY + 0];
                    double large4 = this.largeIslandField[((iterX + 1) * sizeZ + iterZ + 1) * sizeY + iterY + 0];
                    double large5 = (this.largeIslandField[((iterX + 0) * sizeZ + iterZ + 0) * sizeY + iterY + 1] - large1) * noiseOffset1;
                    double large6 = (this.largeIslandField[((iterX + 0) * sizeZ + iterZ + 1) * sizeY + iterY + 1] - large2) * noiseOffset1;
                    double large7 = (this.largeIslandField[((iterX + 1) * sizeZ + iterZ + 0) * sizeY + iterY + 1] - large3) * noiseOffset1;
                    double large8 = (this.largeIslandField[((iterX + 1) * sizeZ + iterZ + 1) * sizeY + iterY + 1] - large4) * noiseOffset1;
                    
                    //Small Islands
                    double small1 = this.smallIslandField[((iterX + 0) * sizeZ + iterZ + 0) * sizeY + iterY + 0];
                    double small2 = this.smallIslandField[((iterX + 0) * sizeZ + iterZ + 1) * sizeY + iterY + 0];
                    double small3 = this.smallIslandField[((iterX + 1) * sizeZ + iterZ + 0) * sizeY + iterY + 0];
                    double small4 = this.smallIslandField[((iterX + 1) * sizeZ + iterZ + 1) * sizeY + iterY + 0];
                    double small5 = (this.smallIslandField[((iterX + 0) * sizeZ + iterZ + 0) * sizeY + iterY + 1] - small1) * noiseOffset1;
                    double small6 = (this.smallIslandField[((iterX + 0) * sizeZ + iterZ + 1) * sizeY + iterY + 1] - small2) * noiseOffset1;
                    double small7 = (this.smallIslandField[((iterX + 1) * sizeZ + iterZ + 0) * sizeY + iterY + 1] - small3) * noiseOffset1;
                    double small8 = (this.smallIslandField[((iterX + 1) * sizeZ + iterZ + 1) * sizeY + iterY + 1] - small4) * noiseOffset1;

                    //Floating Rocks
                    double rock1 = this.floatingRockField[((iterX + 0) * sizeZ + iterZ + 0) * sizeY + iterY + 0];
                    double rock2 = this.floatingRockField[((iterX + 0) * sizeZ + iterZ + 1) * sizeY + iterY + 0];
                    double rock3 = this.floatingRockField[((iterX + 1) * sizeZ + iterZ + 0) * sizeY + iterY + 0];
                    double rock4 = this.floatingRockField[((iterX + 1) * sizeZ + iterZ + 1) * sizeY + iterY + 0];
                    double rock5 = (this.floatingRockField[((iterX + 0) * sizeZ + iterZ + 0) * sizeY + iterY + 1] - rock1) * noiseOffset1;
                    double rock6 = (this.floatingRockField[((iterX + 0) * sizeZ + iterZ + 1) * sizeY + iterY + 1] - rock2) * noiseOffset1;
                    double rock7 = (this.floatingRockField[((iterX + 1) * sizeZ + iterZ + 0) * sizeY + iterY + 1] - rock3) * noiseOffset1;
                    double rock8 = (this.floatingRockField[((iterX + 1) * sizeZ + iterZ + 1) * sizeY + iterY + 1] - rock4) * noiseOffset1;

                    for (int offsetY = 0; offsetY < maxOffsetY; ++offsetY)
                    {
                        double noiseOffset2 = 0.25D;
                        double large9 = large1;
                        double large10 = large2;
                        double large11 = (large3 - large1) * noiseOffset2;
                        double large12 = (large4 - large2) * noiseOffset2;

                        double small9 = small1;
                        double small10 = small2;
                        double small11 = (small3 - small1) * noiseOffset2;
                        double small12 = (small4 - small2) * noiseOffset2;

                        double rock9 = rock1;
                        double rock10 = rock2;
                        double rock11 = (rock3 - rock1) * noiseOffset2;
                        double rock12 = (rock4 - rock2) * noiseOffset2;

                        for (int offsetX = 0; offsetX < 16 / noiseInitXZ; ++offsetX)
                        {
                            //Position in the array for the current layer being generated
                            int layerPos = offsetX + iterX * (16 / noiseInitXZ) << 11 | 0 + iterZ * (16 / noiseInitXZ) << 7 | iterY * maxOffsetY + offsetY;
                            short amountPerLayer = 128;
                            
                            double noiseOffset3 = 0.25D;
                            double largeIslandValue = large9;
                            double largeIslandOffset = (large10 - large9) * noiseOffset3;

                            double smallIslandValue = small9;
                            double smallIslandOffset = (small10 - small9) * noiseOffset3;

                            double rockValue = rock9;
                            double rockOffset = (rock10 - rock9) * noiseOffset3;

                            for (int offsetZ = 0; offsetZ < 16 / noiseInitXZ; ++offsetZ)
                            {
                                int blockID = 0;
 
                                if (largeIslandValue > 0.08D)
                                {
                                    blockID = Block.stone.blockID;
                                }
 
                                if (smallIslandValue > 0.3D)
                                {
                                    blockID = Block.stone.blockID;
                                }
 
                                if (rockValue > 0.3D)
                                {
                                    blockID = Block.stone.blockID;
                                }

                                lowerIDs[layerPos] = (byte) blockID;
                                layerPos += amountPerLayer;
                                largeIslandValue += largeIslandOffset;
                                smallIslandValue += smallIslandOffset;
                                rockValue += rockOffset;
                            }

                            large9 += large11;
                            large10 += large12;
                            
                            small9 += small11;
                            small10 += small12;
                            
                            rock9 += rock11;
                            rock10 += rock12;
                        }

                        large1 += large5;
                        large2 += large6;
                        large3 += large7;
                        large4 += large8;

                        small1 += small5;
                        small2 += small6;
                        small3 += small7;
                        small4 += small8;

                        rock1 += rock5;
                        rock2 += rock6;
                        rock3 += rock7;
                        rock4 += rock8;
                    }
                }
            }
        }
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
    private double[] largeIslandNoiseField (double[] noiseArray, int xPos, int yPos, int zPos, int xSize, int ySize, int zSize)
    {
        //Alert forge of an attempt to initialize a noise field
        ChunkProviderEvent.InitNoiseField event = new ChunkProviderEvent.InitNoiseField(this, noiseArray, xPos, yPos, zPos, xSize, ySize, zSize);
        MinecraftForge.EVENT_BUS.post(event);
        
        //Make sure we're allowed to.
        if (event.getResult() == Result.DENY)
            return event.noisefield; //Do not initialize it.
        if (noiseArray == null)
        {
            noiseArray = new double[xSize * ySize * zSize];
        }
        
        this.stoneData = this.stoneGen.generateNoiseOctaves(this.stoneData, xPos, zPos, xSize, zSize, 1.121D, 1.121D, 0.5D);
        
        this.largeIslandGen.setScale(1/64.0);
        this.largeIslandGen.setYScale(1/128.0);

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
                    
                    double largeIsland = this.largeIslandGen.noise(realX, offsetY, realZ, 1.5, 0.5, true);
                    double deformDensityY = this.deformYGen.noise(realX, realZ, 0.5, 0.5);
                    double height = this.heightGen.noise(realX, realZ, 0.5, 0.5, true);

                    //Make the noise between 0.5 and 0.8
                    double randomDeformRidges = ( (height + 1) / (20 / 3) ) + 0.5;
                    
                    double centerFalloff = 0.1 / (Math.pow((x - 0.5) * 1.5, 2) + Math.pow((y - 1.0) * 0.8, 2) + Math.pow((z - 0.5) * 1.5, 2));
                    largeIsland *= centerFalloff;
                    
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
    }

    /**
     * Generates the noise array for the small island terrain
     * @param noiseArray An empty noise array
     * @param xPos A chunk's X location
     * @param yPos Will be 0 because chunks do not have y position
     * @param zPos A chunk's Z location
     * @param xSize
     * @param ySize
     * @param zSize
     */
    private double[] smallIslandNoiseField (double[] noiseArray, int xPos, int yPos, int zPos, int xSize, int ySize, int zSize)
    {
        //Alert forge of an attempt to initialize a noise field
        ChunkProviderEvent.InitNoiseField event = new ChunkProviderEvent.InitNoiseField(this, noiseArray, xPos, yPos, zPos, xSize, ySize, zSize);
        MinecraftForge.EVENT_BUS.post(event);
        
        //Make sure we're allowed to.
        if (event.getResult() == Result.DENY)
            return event.noisefield; //Do not initialize it.
        if (noiseArray == null)
        {
            noiseArray = new double[xSize * ySize * zSize];
        }
        
        this.stoneData = this.stoneGen.generateNoiseOctaves(this.stoneData, xPos, zPos, xSize, zSize, 1.121D, 1.121D, 0.5D);
        
        this.smallIslandGen.setScale(1/16.0);
        this.smallIslandGen.setYScale(1/32.0);
        
        this.deformYGen.setScale(1/8.0);
        
        //Used to access the noiseArray in order
        int noiseArrayInt = 0;

        for (int offsetX = 0; offsetX < xSize; ++offsetX)
        {
            for (int offsetZ = 0; offsetZ < zSize; ++offsetZ)
            {
                
                double realX = offsetX + xPos;
                double realZ = offsetZ + zPos;
                double deformDensityY = this.deformYGen.noise(realX, realZ, 0.5, 0.5);

                for (int offsetY = 0; offsetY < ySize; ++offsetY)
                {
                    double finalDensity = 0.0;

                    ///////////////////////////
                    //Small Island Generation//
                    ///////////////////////////
                    double sectionSizeXZ = 128;
                    double sectionSizeY = 128;
                    sectionSizeY /= 8;
                    sectionSizeXZ /= 4;
                    float x = (float) (Math.abs(realX ) % sectionSizeXZ);
                    float y = (float) (offsetY / sectionSizeY);
                    float z = (float) (Math.abs(realZ ) % sectionSizeXZ);
                    x /= sectionSizeXZ;
                    z /= sectionSizeXZ;

                    double smallIsland = this.smallIslandGen.noise(realX, offsetY, realZ, 0.8, 0.5);
                    
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
                    
                    smallIsland = smallIsland * centerFalloff * plateauFalloff;

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
                    smallIsland *= deformY;
                    
                    finalDensity = smallIsland;
                    
                    noiseArray[noiseArrayInt] = finalDensity;
                    ++noiseArrayInt;
                }
            }
        }

        return noiseArray;
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
    private double[] floatingRockNoiseField (double[] noiseArray, int xPos, int yPos, int zPos, int xSize, int ySize, int zSize)
    {
        //Alert forge of an attempt to initialize a noise field
        ChunkProviderEvent.InitNoiseField event = new ChunkProviderEvent.InitNoiseField(this, noiseArray, xPos, yPos, zPos, xSize, ySize, zSize);
        MinecraftForge.EVENT_BUS.post(event);
        
        //Make sure we're allowed to.
        if (event.getResult() == Result.DENY)
            return event.noisefield; //Do not initialize it.
        if (noiseArray == null)
        {
            noiseArray = new double[xSize * ySize * zSize];
        }
        
        this.stoneData = this.stoneGen.generateNoiseOctaves(this.stoneData, xPos, zPos, xSize, zSize, 1.121D, 1.121D, 0.5D);
        
        this.rockGen.setScale(1/16.0);
        this.rockGen.setYScale(1/32.0);
        
        this.deformYGen.setScale(1/8.0);
        
        //Used to access the noiseArray in order
        int noiseArrayInt = 0;

        //Move the islands up and down
        double islandTop = 0;
        double islandBottom = 0;
        islandTop = this.worldRNG.nextInt(12) + 4;
        islandBottom = islandTop - 4;
        
        //Check if we should build a floating rock at this chunk
        boolean buildIsland = true;
        if(this.worldRNG.nextInt(10) < 8)
        {
            buildIsland = false;
        }
        
        for (int offsetX = 0; offsetX < xSize; ++offsetX)
        {
            for (int offsetZ = 0; offsetZ < zSize; ++offsetZ)
            {
                
                double realX = offsetX + xPos;
                double realZ = offsetZ + zPos;
                double deformDensityY = this.deformYGen.noise(realX, realZ, 0.5, 0.5);
                
                //Make the noise fluctuate between minBiomeHeight and maxBiomeHeight
                
                for (int offsetY = 0; offsetY < ySize; ++offsetY)
                {
                    double finalDensity = 0.0;
                    
                    if (buildIsland && offsetY > islandBottom && offsetY < islandTop)
                    {
                           ///////////////////////////
                        //Rock Generation        //
                        ///////////////////////////
                        double sectionSizeXZ = 16;
                        double sectionSizeY = 32;
                        sectionSizeY /= 8;
                        sectionSizeXZ /= 4;
                        float x = (float) (Math.abs(realX ) % sectionSizeXZ);
                        float y = (float) ((offsetY - islandBottom) / sectionSizeY);
                        float z = (float) (Math.abs(realZ ) % sectionSizeXZ);
                        x /= sectionSizeXZ;
                        z /= sectionSizeXZ;

                        double floatingRock = this.rockGen.noise(realX, offsetY, realZ, 0.8, 0.5);
                        
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
    
    /***
     * Replaces the stone that was placed in with blocks that match the biome
     * @param chunkX The chunk to be generated's X location
     * @param chunkZ The chunk to be generated's Z location
     * @param lowerIDs
     * @return Nothing
     */
    public void replaceBlocksForBiome (int chunkX, int chunkZ, byte[] lowerIDs, BiomeGenBase[] arrayOfBiomes)
    {
        ChunkProviderEvent.ReplaceBiomeBlocks event = new ChunkProviderEvent.ReplaceBiomeBlocks(this, chunkX, chunkZ, lowerIDs, arrayOfBiomes);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Result.DENY) return;

        byte seaLevel = 0;
        double d0 = 0.03125D;
        this.stoneNoise = this.stoneGen.generateNoiseOctaves(this.stoneNoise, chunkX * 16, chunkZ * 16, 0, 16, 16, 1, d0 * 2.0D, d0 * 2.0D, d0 * 2.0D);

        for (int posX = 0; posX < 16; ++posX)
        {
            for (int posZ = 0; posZ < 16; ++posZ)
            {
                BiomeGenBase biomegenbase = arrayOfBiomes[posZ + posX * 16];
                float biomeTemp = biomegenbase.getFloatTemperature();
                int sNoise = (int)(this.stoneNoise[posX + posZ * 16] / 3.0D + 3.0D + this.worldRNG.nextDouble() * 0.25D);
                int j1 = -1;
                //System.out.println(biomegenbase.topBlock + " " + Block.grass.blockID);
                byte b1 = biomegenbase.topBlock;
                
                //Use luminous grass instead of grass
                if(biomegenbase.topBlock == Block.grass.blockID)
                {
                    b1 = (byte) InverseBlocks.luminousGrass.blockID;
                }
                byte b2 = biomegenbase.fillerBlock;

                for (int posY = 127; posY >= 0; --posY)
                {
                    int blockPos = (posZ * 16 + posX) * 128 + posY;
                    
                    byte currentBlock = lowerIDs[blockPos];

                    if (currentBlock == 0)
                    {
                        j1 = -1;
                    }
                    else if (currentBlock == Block.stone.blockID)
                    {
                        if (j1 == -1)
                        {
                            if (sNoise <= 0)
                            {
                                b1 = 0;
                                b2 = (byte)Block.stone.blockID;
                            }
                            else if (posY >= seaLevel - 4 && posY <= seaLevel + 1)
                            {
                                b1 = biomegenbase.topBlock;
                                b2 = biomegenbase.fillerBlock;
                            }

                            j1 = sNoise;

                            if (posY >= seaLevel - 1)
                            {
                                lowerIDs[blockPos] = b1;
                            }
                            else
                            {
                                lowerIDs[blockPos] = b2;
                            }
                        }
                        else if (j1 > 0)
                        {
                            --j1;
                            lowerIDs[blockPos] = b2;

                            if (j1 == 0 && b2 == Block.sand.blockID)
                            {
                                j1 = this.worldRNG.nextInt(4);
                                b2 = (byte)Block.sandStone.blockID;
                            }
                        }
                    }
                }
            }
        }
    }
    
    /***
     * Checks to see if a chunk exists at x, y
     * @return Always returns true because a chunk always has to exist I suppose?
     * @param chunkX The X location of the chunk
     * @param chunkZ The Z location of the chunk
     */
    public boolean chunkExists(int chunkX, int chunkZ) {
        return true;
    }
    
    /**
     * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
     * specified chunk from the map seed and chunk seed
     */
    public Chunk provideChunk(int chunkX, int chunkZ)
    {
        this.worldRNG.setSeed((long)chunkX * 341873128712L + (long)chunkZ * 132897987541L);
        byte[] abyte = new byte[32768];
        this.generateTerrain(chunkX, chunkZ, abyte);
        
        //Get the biome to use
        this.biomesForGeneration = this.worldObj.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration, chunkX * 16, chunkZ * 16, 16, 16);
        this.replaceBlocksForBiome(chunkX, chunkZ, abyte, this.biomesForGeneration); //Applies biome features
        
        //Generate extra terrain features
        this.caveGenerator.generate(this, this.worldObj, chunkX, chunkZ, abyte);
        this.ravineGenerator.generate(this, this.worldObj, chunkX, chunkZ, abyte);

        Chunk chunk = new Chunk(this.worldObj, abyte, chunkX, chunkZ);
        byte[] abyte1 = chunk.getBiomeArray();

        for (int i = 0; i < abyte1.length; ++i)
        {
            abyte1[i] = (byte)this.biomesForGeneration[i].biomeID;
        }

        chunk.generateSkylightMap();
        return chunk;
    }
    /***
     * Loads or generates the chunk at the chunk location specified
     * @param chunkX The X location of the chunk
     * @param chunkZ The Z location of the chunk
     * @return The chunk at the specified location
     */
    public Chunk loadChunk(int chunkX, int chunkZ) {
        return this.provideChunk(chunkX, chunkZ);
    }

    /**
     * Populates chunk with ores etc etc
     * @param chunkProvider The chunk provider to use
     * @param chunkX The X location of the chunk
     * @param chunkZ The Z location of the chunk
     * @return Nothing
     */
    public void populate(IChunkProvider chunkProvider, int chunkX, int chunkZ) {
        
    
        BlockSand.fallInstantly = false; //Stop everything from falling immediately
        
        int k = chunkX * 16;
        int l = chunkZ * 16;
        BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(k + 16, l + 16);
        this.worldRNG.setSeed(this.worldObj.getSeed());
        long i1 = this.worldRNG.nextLong() / 2L * 2L + 1L;
        long j1 = this.worldRNG.nextLong() / 2L * 2L + 1L;
        this.worldRNG.setSeed((long)chunkX * i1 + (long)chunkZ * j1 ^ this.worldObj.getSeed());

        //Inform forge of world generation
        boolean flag = false; //For sending event updates to forge
        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(chunkProvider, this.worldObj, this.worldRNG, chunkX, chunkZ, flag)); //Tell forge we're beginning to populate a new chunk

        int posX;
        int posY;
        int posZ;

        if (TerrainGen.populate(chunkProvider, worldObj, this.worldRNG, chunkX, chunkZ, flag, LAKE) && 
                !flag && this.worldRNG.nextInt(4) == 0)
        {
            posX = k + this.worldRNG.nextInt(16) + 8;
            posY = this.worldRNG.nextInt(128);
            posZ = l + this.worldRNG.nextInt(16) + 8;
            (new WorldGenLakes(Block.waterStill.blockID)).generate(this.worldObj, this.worldRNG, posX, posY, posZ);
        }

        if (TerrainGen.populate(chunkProvider, worldObj, this.worldRNG, chunkX, chunkZ, flag, LAVA) &&
                !flag && this.worldRNG.nextInt(8) == 0)
        {
            posX = k + this.worldRNG.nextInt(16) + 8;
            posY = this.worldRNG.nextInt(this.worldRNG.nextInt(120) + 8);
            posZ = l + this.worldRNG.nextInt(16) + 8;

            if (posY < 63 || this.worldRNG.nextInt(10) == 0)
            {
                (new WorldGenLakes(Block.lavaStill.blockID)).generate(this.worldObj, this.worldRNG, posX, posY, posZ);
            }
        }

        boolean doGen = TerrainGen.populate(chunkProvider, worldObj, this.worldRNG, chunkX, chunkZ, flag, DUNGEON);
        for (posX = 0; doGen && posX < 8; ++posX)
        {
            posY = k + this.worldRNG.nextInt(16) + 8;
            posZ = this.worldRNG.nextInt(128);
            int j2 = l + this.worldRNG.nextInt(16) + 8;

            if ((new WorldGenDungeons()).generate(this.worldObj, this.worldRNG, posY, posZ, j2))
            {
                ;
            }
        }

        biomegenbase.decorate(this.worldObj, this.worldRNG, k, l);
        SpawnerAnimals.performWorldGenSpawning(this.worldObj, biomegenbase, k + 8, l + 8, 16, 16, this.worldRNG);
        k += 8;
        l += 8;

        doGen = TerrainGen.populate(chunkProvider, worldObj, this.worldRNG, chunkX, chunkZ, flag, ICE);
        for (posX = 0; doGen && posX < 16; ++posX)
        {
            for (posY = 0; posY < 16; ++posY)
            {
                posZ = this.worldObj.getPrecipitationHeight(k + posX, l + posY);

                if (this.worldObj.isBlockFreezable(posX + k, posZ - 1, posY + l))
                {
                    this.worldObj.setBlock(posX + k, posZ - 1, posY + l, Block.ice.blockID, 0, 2);
                }

                if (this.worldObj.canSnowAt(posX + k, posZ, posY + l))
                {
                    this.worldObj.setBlock(posX + k, posZ, posY + l, Block.snow.blockID, 0, 2);
                }
            }
        }

        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(chunkProvider, worldObj, this.worldRNG, chunkX, chunkZ, flag));

        BlockSand.fallInstantly = false;
        
    }

    /**
     * Two modes of operation: if passed true, save all Chunks in one go.  If passed false, save up to two chunks.
     * @return Returns true if all chunks have been saved.
     */
    public boolean saveChunks(boolean flag, IProgressUpdate iprogressupdate) {
        return true;
    }

    /**
     * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
     * @return Always returns false because I don't think any of this is implemented anywhere.
     */
    public boolean unloadQueuedChunks() {
        return false;
    }

    /**
     * Returns if the IChunkProvider supports saving.
     */
    public boolean canSave() {
        return true;
    }

    /**
     * Converts the instance data to a readable string.
     */
    public String makeString() {
        return "InverseWorldRandomLevelSource";
    }

    /**
     * Returns a list of creatures of the specified type that can spawn at the given location.
     * @param enumcreaturetype A list of creature types to check if they can possibly spawn there
     * @param xPos The X position to check
     * @param yPos The Y position to check
     * @param zPos The Z position to check
     */
    public List getPossibleCreatures(EnumCreatureType enumcreaturetype, int xPos, int yPos, int zPos) {
        BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(xPos, zPos);
        return biomegenbase == null ? null : biomegenbase.getSpawnableList(enumcreaturetype);
    }

    /**
     * Returns the location of the closest structure of the specified type. If not found returns null.
     * @param world A reference to the world object
     * @param s Some string I guess. I don't know yet.
     * @param xPos The X position to check from
     * @param yPos The Y position to check from
     * @param zPos The Z position to check from
     */
    public ChunkPosition findClosestStructure(World world, String s, int xPos, int yPos, int zPos) {
        return null;
    }

    /**
     * Returns the amount of loaded chunks.
     * @return Doesn't do anything really. Not even in Minecraft I guess.
     */
    public int getLoadedChunkCount() {
        return 0;
    }
    
    @Override
    public void recreateStructures(int i, int j) {
        // TODO Auto-generated method stub
        
    }

    /**
     * No clue.
     */
    public void func_104112_b() {}

    @Override
    public void saveExtraData() {
        // TODO Auto-generated method stub
        
    }

}