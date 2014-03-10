package cherrytea.minecraft.inverseworld.util;

import net.minecraft.util.Vec3;

public class Vec3Helper {

	public static Vec3 makePitchYawVector( double pitch, double yaw )
	{
		return makePitchYawVector ( pitch, yaw, 1 );
	}

	/***
	 * Creates a Vec3 from the pitch, yaw, and magnitude provided
	 * @param pitch A double between -90 and 90 inclusive respresenting the angle the player is looking up or down
	 * @param yaw A double between -infinite and infinite representing the angle the player is looking rotationally on the XZ axis
	 * @param magnitude The length of the vector
	 * @return The Vec3 created
	 */
	public static Vec3 makePitchYawVector(double pitch, double yaw, double magnitude)
	{
		
		return null;
	}
	
	/***
	 * Finds the angle the player is facing between -180 and 180
	 * @param angle The angle the player is facing from -infinite to infinite
	 * @return The angle the player is facing from -180 to 180
	 */
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
