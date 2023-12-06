//*******************************************************************
package orb.device.ORB;

//*******************************************************************
import java.nio.ByteBuffer;

//*******************************************************************
/**
 * ...
 * @author Thomas Breuer
 */
class cPropToORB
{
    //---------------------------------------------------------------
    private class Motor
    {
        int  mode  = 0;
        int  speed = 0;
        int  pos   = 0;
    }

    //---------------------------------------------------------------
    private class ModellServo
    {
        int  mode = 0;
        int  pos  = 0;
    }

    //---------------------------------------------------------------
    final byte id               = 1;
    final int  numOfMotorPorts  = 4;
    final int  numOfServoPorts  = 2;

    //---------------------------------------------------------------
    private Motor[]       motor = new Motor[numOfMotorPorts];
    private ModellServo[] servo = new ModellServo[numOfServoPorts];
    private boolean isNewFlag = true;

    //---------------------------------------------------------------
    cPropToORB()
    {
        for( int i = 0; i < numOfMotorPorts; i++ )
        {
            motor[i] = new Motor();
        }
        for( int i = 0; i < numOfServoPorts; i++ )
        {
            servo[i] = new ModellServo();
        }
        isNewFlag = true;
    }

    //---------------------------------------------------------------
    boolean isNew()
    {
        boolean ret = isNewFlag;
        isNewFlag = false;
        return( true );  // propToORB auch dann senden, wenn sich nichts geändert hat
    }

    //---------------------------------------------------------------
    int fill( ByteBuffer buffer )
    {
        int idx = 4;
        synchronized( this )
        {
            for( int i = 0; i < numOfMotorPorts; i++ )
            {
                buffer.put( idx++, (byte)((motor[i].mode      )       ) );

                buffer.put( idx++, (byte)((motor[i].speed     ) & 0xFF) ); // LSB
                buffer.put( idx++, (byte)((motor[i].speed >> 8) & 0xFF) ); // MSB

                buffer.put( idx++, (byte)((motor[i].pos      ) & 0xFF) ); // LSB
                buffer.put( idx++, (byte)((motor[i].pos >>  8) & 0xFF) ); //
                buffer.put( idx++, (byte)((motor[i].pos >> 16) & 0xFF) ); //
                buffer.put( idx++, (byte)((motor[i].pos >> 24) & 0xFF) ); // MSB
            }

            for( int i = 0; i < numOfServoPorts; i++ )
            {
                buffer.put( idx++, (byte)((servo[i].mode)) );
                buffer.put( idx++, (byte)((servo[i].pos )) );
            }
        } // synchronized

        ORB_Remote.addDataFrame( buffer, id, idx );
        return( idx );
    }

    //---------------------------------------------------------------
    void setMotor( int idx, int mode, int speed, int pos )
    {
        synchronized( this )
        {
            if( 0 <= idx && idx < numOfMotorPorts )
            {
                motor[idx].mode  = mode;
                motor[idx].speed = speed;
                motor[idx].pos   = pos;

                isNewFlag =true;
            }
        } // synchronized
    }

    //---------------------------------------------------------------
    void setModelServo( int idx, int mode, int pos )
    {
        synchronized( this )
        {
            if( 0 <= idx && idx < numOfServoPorts )
            {
                servo[idx].mode = mode;
                servo[idx].pos  = pos;

                isNewFlag = true;
            }
        } // synchronized
    }
} // end of class
