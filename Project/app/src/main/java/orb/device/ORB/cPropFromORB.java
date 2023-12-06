//*******************************************************************
package orb.device.ORB;

//*******************************************************************
import java.nio.ByteBuffer;

//*******************************************************************
/**
 * ...
 * @author Thomas Breuer
 */
public class cPropFromORB
{
    //---------------------------------------------------------------
    public class Motor
    {
        public short pwr   = 0;
        public short speed = 0;
        public int   pos   = 0;
    }

    //---------------------------------------------------------------
    public class Sensor
    {
        public boolean isValid    = false;
        public int[]   value      = {0,0};
        public byte    type       = 0;
        public byte    option     = 0;
    }

    //---------------------------------------------------------------
    private class Sensor_temp
    {
        int[]     valueTemp = {0,0};
    }

    //---------------------------------------------------------------
    final byte id = 2;
    public static final int numOfMotorPorts = 4;
    public static final int numOfSensorPorts = 4;

    //---------------------------------------------------------------
    private Motor[]  motor  = new Motor[numOfMotorPorts];
    private Sensor[] sensor = new Sensor[numOfSensorPorts];
    private Sensor_temp[] sensor_temp = new Sensor_temp[numOfSensorPorts];

    private int     Vcc      = 0;
    private byte    Status   = 0;
    private boolean D1       = true;
    private boolean D2       = true;

    //---------------------------------------------------------------
    cPropFromORB()
    {
        for( int i = 0; i < numOfMotorPorts; i++ )
        {
            motor[i] = new Motor();
        }

        for( int i = 0; i < numOfSensorPorts; i++ )
        {
            sensor[i] = new Sensor();
            sensor_temp[i] = new Sensor_temp();
        }
    }

    //---------------------------------------------------------------
    boolean get( ByteBuffer data )
    {
        if( !ORB_Remote.checkDataFrame( data, id, 60 ) )
        {
            return( false );
        }

        int idx = 4;

        synchronized( this )
        {
            for( int i = 0; i < numOfMotorPorts; i++ )
            {
                motor[i].pwr   = (short)( ((byte)data.get(idx++) & (byte)0xFF) );

                motor[i].speed = (short)( ((short)data.get(idx++) & 0xFF)
                                         |((short)data.get(idx++) & 0xFF) << 8);

                motor[i].pos   = (int)  (  ((int)data.get(idx++) & 0xFF)
                                         | ((int)data.get(idx++) & 0xFF) <<  8
                                         | ((int)data.get(idx++) & 0xFF) << 16
                                         | ((int)data.get(idx++) & 0xFF) << 24);
            }

            for( int i = 0; i < numOfSensorPorts; i++ )
            {
                int value    = (int)  (  ((int) data.get(idx++) & 0xFF)
                                       | ((int) data.get(idx++) & 0xFF) <<  8
                                       | ((int) data.get(idx++) & 0xFF) << 16
                                       | ((int) data.get(idx++) & 0xFF) << 24);

				byte type            = data.get(idx++);
                byte descriptor      = data.get(idx++);
                byte option          = data.get(idx++);


                int lenExp = (descriptor >> 5) & 0xFF;
                int id =  (descriptor) & 0x1F;

                if( lenExp <= 2) // 1,2 oder 4 Byte
                {
                    sensor[i].value[0] = value;
                    sensor[i].value[1] = 0;
                    sensor[i].isValid = ((type & 0x80)==0x80)? true : false;
                    sensor[i].type    = (byte)(( type & 0x7F));
                    sensor[i].option  = option;
                }
                else if( lenExp == 3 && id == 0 ) // 1. von 2 Packeten
                {
                    sensor_temp[i].valueTemp[0] = value;
                }
                else if( lenExp == 3 && id == 1 ) // 2. von 2 Packeten
                {
                    sensor[i].value[0] = sensor_temp[i].valueTemp[0];
                    sensor[i].value[1] = value;
                    sensor[i].isValid = ((type & 0x80)==0x80)? true : false;
                    sensor[i].type    = (byte)(( type & 0x7F));
                    sensor[i].option  = option;
                }
                else {
                    sensor[i].isValid = false;
                    // ignore!
                }
            }

            Byte digital = data.array()[idx++];
            D1 = (digital & (byte)0x01) != 0;
            D2 = (digital & (byte)0x02) != 0;

            Vcc      = data.get(idx++) & 0xFF;
            Status   = data.get(idx++);
        }
        return true;
    }

    //---------------------------------------------------------------
    Motor getMotor( int idx )
    {
        if( 0 <= idx && idx < numOfMotorPorts )
        {
            return( motor[idx] );
        }
        return( motor[0] );
    }


    //---------------------------------------------------------------
    Sensor getSensor( int idx )
    {
        if( 0 <= idx && idx < numOfSensorPorts )
        {
            return( sensor[idx] );
        }
        return(  sensor[0]  );
    }

    //---------------------------------------------------------------
    boolean getSensorDigital( int idx )
    {
        if( idx == 0 )  return( D1 );
        else            return( D2 );
    }

    //---------------------------------------------------------------
    double getVcc()
    {
        return ((double)Vcc/10);
    }

    //---------------------------------------------------------------
    byte getStatus()
    {
        return( Status );
    }

} // end of class
