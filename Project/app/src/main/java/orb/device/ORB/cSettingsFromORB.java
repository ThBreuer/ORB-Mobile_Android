//*******************************************************************
package orb.device.ORB;

//*******************************************************************
import java.nio.ByteBuffer;

//*******************************************************************
/**
 * ...
 * @author Thomas Breuer
 */
public class cSettingsFromORB
{
    //---------------------------------------------------------------
    final byte id = 5;

    //---------------------------------------------------------------
    private int[] version  = new int[2];
    private int[] board    = new int[2];
    private String name    = "---";
    private double Vcc_ok  = 0;
    private double Vcc_low = 0;

    private boolean isNewFlag = false;

    //---------------------------------------------------------------
    cSettingsFromORB()
    {
    }

    //---------------------------------------------------------------
    public boolean isNew()
    {
        boolean ret = isNewFlag;
        isNewFlag = false;
        return( ret);
    }

    //---------------------------------------------------------------
    boolean get( ByteBuffer data )
    {
        if( !ORB_Remote.checkDataFrame( data, id, 31 ) )
        {
            return( false );
        }

        int idx = 4;

        synchronized( this )
        {
            version[0] =   (short)( ((short)data.get(idx++) & 0xFF)
                                   |((short)data.get(idx++) & 0xFF) << 8);
            version[1] =   (short)( ((short)data.get(idx++) & 0xFF)
                                   |((short)data.get(idx++) & 0xFF) << 8);
            board[0] =     (short)( ((short)data.get(idx++) & 0xFF)
                                   |((short)data.get(idx++) & 0xFF) << 8);
            board[1] =     (short)( ((short)data.get(idx++) & 0xFF)
                                   |((short)data.get(idx++) & 0xFF) << 8);

            name = "";
            for( int i = 0; i < 20; i++ )
            {
                char c =  (char)data.get(idx++);
                if( c != 0 )
                    name = name.concat( String.valueOf( c ) );
            }
            idx++; // skip terminating zero

            Vcc_ok  = (double)data.get(idx++)/10;
            Vcc_low = (double)data.get(idx++)/10;

            isNewFlag = true;
        }
        return true;
    }

    //---------------------------------------------------------------
    public String getName()
    {
        return( name );
    }

    //---------------------------------------------------------------
    public int[] getVersion()
    {
        return( version );
    }

    //---------------------------------------------------------------
    public String getSW_Version()
    {
        String str = String.format( "%02d.%02d", version[0], version[1] );
        return( str );
    }

    //---------------------------------------------------------------
    public int[] getBoard()
    {
        return( board );
    }

    //---------------------------------------------------------------
    public String getHW_Version()
    {
        String str = String.format( "%02d.%02d", board[0], board[1] );
        return( str );
    }

    //---------------------------------------------------------------
    public double getVccOk()
    {
        return( Vcc_ok );
    }

    //---------------------------------------------------------------
    public double getVccLow()
    {
        return( Vcc_low );
    }

} // end of class
