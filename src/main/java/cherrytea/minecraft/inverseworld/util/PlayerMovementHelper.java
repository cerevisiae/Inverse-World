package cherrytea.minecraft.inverseworld.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3Pool;

public class PlayerMovementHelper {
    
    public static Vec3 glide ( Entity entity, int steps)
    {
        // Motion Vector
        Vec3 M = Vec3.createVectorHelper(entity.motionX, entity.motionY, entity.motionZ);
        double M_mag = M.lengthVector();
        if( M_mag > 1.5)
        {
            M.xCoord *= 1.5 / M_mag;
            M.yCoord *= 1.5 / M_mag;
            M.zCoord *= 1.5 / M_mag;
        }
        
        
        // Begin Camera Vector
        Vec3 C = null;
        double cameraX = 0;
        double cameraY = 0;
        double cameraZ = 0;
        
        // Different calculations for not vertical
        if (Math.abs(entity.rotationPitch) != 90)
        {
            cameraY = Math.sin( Math.toRadians( entity.rotationPitch ) );
            
            double tempYaw = getTrueAngle(entity.rotationYaw);
        
            // If the entity is looking due East/West
            if ( Math.abs( entity.rotationYaw ) == 90 )
            {
                cameraX = Math.cos( Math.toRadians( entity.rotationPitch ) );
            }
            else
            {
                double temp2Yaw = tempYaw;
                
                //If the entity is looking Northerly
                if ( tempYaw > 90 )
                    temp2Yaw = 180 - tempYaw;
                else if ( tempYaw < -90 )
                    temp2Yaw = -180 - tempYaw;
                
                cameraX = -Math.sin( Math.toRadians( temp2Yaw ) ) * Math.cos( Math.toRadians( entity.rotationPitch ) );
                cameraZ = -Math.cos( Math.toRadians( temp2Yaw ) ) * Math.cos( Math.toRadians( entity.rotationPitch ) );
                if(Math.abs(tempYaw) < 90)
                    cameraZ *= -1;
            }
        }
        else
        {
            cameraY = entity.rotationPitch / 90;
        }
        
        cameraY *= -1;
        
        // Normalize the Camera vector
        C = Vec3.createVectorHelper( cameraX, cameraY, cameraZ );
        
        // Give the Camera vector the same magnitude as the motion
        C.xCoord *= M.lengthVector();
        C.yCoord *= M.lengthVector();
        C.zCoord *= M.lengthVector();
        
        // A vector pointing from the end of the Motion vector to the end of the Camera vector
        Vec3 S = Vec3.createVectorHelper(C.xCoord - M.xCoord, C.yCoord - M.yCoord, C.zCoord - M.zCoord);
        
        // The magnitude moved in a single step
        double S_n = 1.0D / steps;
        
        //Apply the changed magnitude
        //S = S.normalize();
        S.xCoord *= S_n;
        S.yCoord *= S_n * 2;
        S.zCoord *= S_n;
        
        M.xCoord *= 1.1;
        M.yCoord *= 1.05;
        M.zCoord *= 1.1;
        
        // Change the final motion
        M.xCoord += S.xCoord;
        M.yCoord += S.yCoord + .05;
        M.zCoord += S.zCoord;
        
        return M;
    }
    
    public static double getTrueAngle( double angle )
    {
        double trueAngle = angle % 180;
        if ( angle % 360 > 180 )
            trueAngle -= 180;
        else if ( angle % 360 < -180 )
            trueAngle += 180;
        
        return trueAngle;
    }

}
