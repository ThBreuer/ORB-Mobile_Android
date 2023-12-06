//*******************************************************************
package orb.device.ORB;

//*******************************************************************
import java.nio.ByteBuffer;

//*******************************************************************
/**
 * ...
 * @author Thomas Breuer
 */
class cConfigToORB
{
    //---------------------------------------------------------------
    private class Sensor
    {
        byte type    = 0;
        byte mode    = 0;
        short option = 0;
    }

    //---------------------------------------------------------------
    private class Motor
    {
        byte   acceleration    = 0;
        short  ticsPerRotation = 0;
        byte   Regler_Kp       = 0;
        byte   Regler_Ki       = 0;
        byte   Regler_Kd       = 0;
    }

    //---------------------------------------------------------------
    final byte id               = 0;
    final int  numOfSensorPorts = 4;
    final int  numOfMotorPorts  = 4;

    //---------------------------------------------------------------
    private Sensor[] sensor   = new Sensor[numOfSensorPorts];
    private Motor[]  motor    = new Motor[numOfMotorPorts];

    private boolean isNewFlag = true;

    //---------------------------------------------------------------
    cConfigToORB()
    {
        for( int i = 0; i < numOfSensorPorts; i++ )
        {
            sensor[i] = new Sensor();
        }
        for( int i = 0; i < numOfMotorPorts; i++ )
        {
            motor[i] = new Motor();
        }
        isNewFlag = true;
    }

    //---------------------------------------------------------------
    // todo: erst senden, wenn letztes status-bit in propToORB == 0,
    //       reset erst, wenn status-bit in propToORB == 1
    boolean isNew()
    {
        boolean ret = isNewFlag;
        isNewFlag = false;
        return( ret);
    }

    //---------------------------------------------------------------
    int fill( ByteBuffer buffer )
    {
        int idx = 4;

        synchronized( this )
        {
            for( int i = 0; i < numOfSensorPorts; i++ )
            {
                buffer.put( idx++, sensor[i].type   );
                buffer.put( idx++, sensor[i].mode   );
                buffer.put( idx++, (byte)(  sensor[i].option    &0xFF) );
                buffer.put( idx++, (byte)( (sensor[i].option>>8)&0xFF) );
            }
            for( int i = 0; i < numOfMotorPorts; i++ )
            {
                buffer.put( idx++, (byte)( motor[i].ticsPerRotation    &0xFF) );
                buffer.put( idx++, (byte)((motor[i].ticsPerRotation>>8)&0xFF) );
                buffer.put( idx++, motor[i].acceleration );
                buffer.put( idx++, motor[i].Regler_Kp    );
                buffer.put( idx++, motor[i].Regler_Ki    );
                buffer.put( idx++, motor[i].Regler_Kd    );
                buffer.put( idx++, (byte)0               ); // reserved
                buffer.put( idx++, (byte)0               ); // reserved
            }
            for( int i = idx; i < 64; i++ ) // 12 Bytes reserved
            {
                buffer.put( idx++, (byte)0 ); // reserved
            }
        } // synchronized

        ORB_Remote.addDataFrame( buffer, id, idx );
        return( idx );
    }

    //---------------------------------------------------------------
    void configMotor( int idx, int tics, int acc, int Kp, int Ki )
    {
        synchronized( this )
        {
            if( 0 <= idx && idx < numOfMotorPorts )
            {
                motor[idx].ticsPerRotation = (short)tics;
                motor[idx].acceleration    = (byte)acc;
                motor[idx].Regler_Kp       = (byte)Kp;
                motor[idx].Regler_Ki       = (byte)Ki;

                isNewFlag = true;
            }
        }
    }

    //---------------------------------------------------------------
    void configSensor( int idx, int type, int mode, int option )
    {
        synchronized( this )
        {
            if( 0 <= idx && idx < numOfSensorPorts )
            {
                sensor[idx].type   = (byte)type;
                sensor[idx].mode   = (byte)mode;
                sensor[idx].option = (short)option;

                isNewFlag = true;
            }
        }
    }
} // end of class
