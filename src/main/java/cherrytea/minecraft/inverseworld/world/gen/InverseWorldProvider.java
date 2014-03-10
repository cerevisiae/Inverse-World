package cherrytea.minecraft.inverseworld.world.gen;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * @author cherrytea
 * @version 1.0
 * The world provider for Inverse world. Creates the dimension and directs to the Chunk Manager and Chunk Provider
 */
public class InverseWorldProvider extends WorldProvider
{
    /**
     * creates a new world chunk manager for WorldProvider
     */
    /*public void registerWorldChunkManager ()
    {
        this.worldChunkMgr = new InverseChunkManager(BiomeGenBase.hell, 1.0F, 0.0F);
        this.setDimension( 0 ); //The dimension number (ex. -1: Nether, 0: Overworld, 1: The End)
    }*/

    /**
     * Returns the name of the dimension
     * @return The name of the dimension.
     */
    public String getDimensionName()
    {
        return "Inverse World";
    }

    /**
     * Returns a new chunk provider which generates chunks for this world
     * @return The new chunk provider
     */
    public IChunkProvider createChunkGenerator()
    {
        return new InverseChunkProvider(this.worldObj, this.worldObj.getSeed());
    }

    /**
     * True if the player can respawn in this dimension (true = overworld, false = nether).
     * @return If the player can respawn here
     */
    public boolean canRespawnHere()
    {
        return false;
    }

    public boolean isDaytime()
    {
        return true;
    }

    /**
     * Calculates the angle of sun and moon in the sky relative to a specified time (usually worldTime)
     */
    public float calculateCelestialAngle(long par1, float par3)
    {

        return 1F;
    }

    /**
     * Returns a double value representing the Y value relative to the top of the map at which void fog is at its
     * maximum. The default factor of 0.03125 relative to 256, for example, means the void fog will be at its maximum at
     * (256*0.03125), or 8.
     */
    @SideOnly(Side.CLIENT)
    public double getVoidFogYFactor()
    {
        return -1;
    }

    /**
     * the y level at which clouds are rendered.
     */
    @SideOnly(Side.CLIENT)
    public float getCloudHeight()
    {
        return 30.0F;
    }

    /**
     * Returns if the specified chunk should show fog.
     * @param chunkX The X location of the chunk
     * @param chunkZ The Z location of the chunk
     * @return Always returns false so as to disable the fog
     */
    public boolean doesXZShowFog(int chunkX, int chunkZ)
    {
        return false;
    }

    /**
     * returns true if this dimension is supposed to display void particles and pull in the far plane based on the
     * user's Y offset.
     */
    @SideOnly(Side.CLIENT)
    public boolean getWorldHasVoidParticles()
    {
        return false;
    }
    
    /***
     * 
     */
    public double getHorizon()
    {
        return -100;
    }
    
    @SideOnly(Side.CLIENT)
    public float getStarBrightness(float par1)
    {
        return 0;
    }
    
    @SideOnly(Side.CLIENT)
    public Vec3 getSkyColor(Entity cameraEntity, float partialTicks)
    {
        return worldObj.getSkyColorBody(cameraEntity, partialTicks);
    }

}
