//*******************************************************************
package orb.device.ORB;

//*******************************************************************
import java.nio.ByteBuffer;

//*******************************************************************
/**
 * ...
 * @author Thomas Breuer
 */
class cSettingsToORB
{
    //---------------------------------------------------------------
    final byte id = 6;

    //---------------------------------------------------------------
    private byte    command   = 0; // 0: read settings, 1: set settings, 2: clear memory
    private String  name      = "";
    private double  VCC_ok    = 7.6;
    private double  VCC_low   = 7.2;
    private boolean isNewFlag = true;

    //---------------------------------------------------------------
    cSettingsToORB()
    {
        isNewFlag = true;
    }

    //---------------------------------------------------------------
    // TODO: erst senden, wenn letztes status-bit in propToORB == 0
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
            buffer.put( idx++, command );
            for( int i = 0; i < 20; i++ )
            {
                if( i < name.length() )
                    buffer.put( idx++, (byte)name.charAt( i ) );
                else
                    buffer.put( idx++, (byte)0 );
            }
            buffer.put( idx++, (byte)0              );
            buffer.put( idx++, (byte)(10.0*VCC_ok ) );
            buffer.put( idx++, (byte)(10.0*VCC_low) );

        } // synchronized
        ORB_Remote.addDataFrame( buffer, id, idx );
        return( idx );
    }

    //---------------------------------------------------------------
    void sendRequest()
    {
        synchronized( this )
        {
            command   = 0;
            isNewFlag = true;
        } // synchronized
    }

    //---------------------------------------------------------------
    void sendData( String name, double VCC_ok, double VCC_low )
    {
        synchronized( this )
        {
            this.command   = 1;
            this.name      = name;
            this.VCC_ok    = VCC_ok;
            this.VCC_low   = VCC_low;
            this.isNewFlag = true;
        } // synchronized
    }

} // end of class
