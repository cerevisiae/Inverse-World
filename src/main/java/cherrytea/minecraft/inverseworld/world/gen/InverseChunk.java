package cherrytea.minecraft.inverseworld.world.gen;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class InverseChunk extends Chunk
{

    /**
     * Used to store block IDs, block MSBs, Sky-light maps, Block-light maps, and metadata. Each entry corresponds to a
     * logical segment of 16x16x16 blocks, stacked vertically.
     */
    private ExtendedBlockStorage[] storageArrays;
    
    public InverseChunk(World par1World, byte[] par2ArrayOfByte, int chunkX, int chunkZ)
    {
        super(par1World, chunkX, chunkZ);
        int k = par2ArrayOfByte.length / 512;

        for (int iterX = 0; iterX < 16; ++iterX)
        {
            for (int iterZ = 0; iterZ < 16; ++iterZ)
            {
                for (int iterY = 0; iterY < k; ++iterY)
                {
                    /* FORGE: The following change, a cast from unsigned byte to int,
                     * fixes a vanilla bug when generating new chunks that contain a block ID > 127 */
                    int b0 = par2ArrayOfByte[iterX << 12 | iterZ << 8 | iterY] & 0xFF;

                    if (b0 != 0)
                    {
                        int k1 = iterY >> 4;

                        if (this.storageArrays[k1] == null)
                        {
                            this.storageArrays[k1] = new ExtendedBlockStorage(k1 << 4, !par1World.provider.hasNoSky);
                        }

                        this.storageArrays[k1].setExtBlockID(iterX, iterY & 15, iterZ, b0);
                    }
                }
            }
        }
    }
}
