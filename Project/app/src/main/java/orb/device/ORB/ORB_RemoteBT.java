//*******************************************************************
package orb.device.ORB;

//*******************************************************************
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;

//*******************************************************************
/**
 * ...
 * @author Thomas Breuer
 */
class ORB_RemoteBT extends ORB_Remote
{
    //---------------------------------------------------------------
    private ByteBuffer      bufferIN;
    private ByteBuffer      bufferOUT;
    private BluetoothSocket BT_Socket;
    private OutputStream    BT_OutStream;
    private InputStream     BT_InStream;
    private boolean isConnected = false;
    private boolean ready       = false;
    private boolean flag        = false;
    private int pos             = 0;
    private static final UUID MY_UUID = UUID.fromString( "00001101-0000-1000-8000-00805F9B34FB" );
    private static final String TAG = "ORB_BT";

    //---------------------------------------------------------------
    ORB_RemoteBT( ORB_Manager orb_manager )
    {
        super( orb_manager );
        bufferIN = ByteBuffer.allocate( 256 );
        bufferOUT = ByteBuffer.allocate( 256 );
    }

    //---------------------------------------------------------------
    BluetoothDevice open( String addr )
    {
        close();
        BluetoothDevice BT_Device
                = BluetoothAdapter.getDefaultAdapter().getRemoteDevice( addr );
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        try
        {
            synchronized( this )
            {
                BT_Socket = BT_Device.createRfcommSocketToServiceRecord( MY_UUID );
                BT_Socket.connect();

                BT_OutStream = BT_Socket.getOutputStream();
                BT_InStream = BT_Socket.getInputStream();
                isConnected = true;
            }
        }
        catch( IOException e )
        {
            isConnected = false;
        }
        return (BT_Device);
    }

    //---------------------------------------------------------------
    void close()
    {
        isConnected = false;
        try
        {
            if(  BT_Socket != null )
            {
                if( BT_InStream != null )
                {
                    BT_InStream.close();
                }
                if( BT_OutStream != null )
                {
                    BT_OutStream.close();
                }
                BT_Socket.close();
                BT_Socket = null;
            }
        }
        catch(IOException e)
        {
            Log.e( TAG, "BT close error" + e.toString() );
        }
    }

    //---------------------------------------------------------------
    Set<BluetoothDevice> getPairedDevices()
    {
        BluetoothAdapter BT_Adapter;
        Set<BluetoothDevice> BT_PairedDevices;

        BT_Adapter = BluetoothAdapter.getDefaultAdapter();
        BT_PairedDevices = BT_Adapter.getBondedDevices();
        return (BT_PairedDevices);
    }

    //---------------------------------------------------------------
    boolean isConnected()
    {
        return (isConnected);
    }

    //---------------------------------------------------------------
    private void updateOut()
    {
        if( !isConnected )
        {
            return;
        }
        short len  = 0;
        short idx  = 0;
        int   size = orb_manager.fill( bufferOUT );

        byte data[] = new byte[1024];
        data[len++] = (byte) 0xA1; // start

        while (idx <  size && len < 1024-1)
        {
            byte b = bufferOUT.get(idx++);
            switch( b )
            {
                case (byte)0xA0: data[len++] = (byte)0xA0;  data[len++] = (byte)0x00;  break;
                case (byte)0xA1: data[len++] = (byte)0xA0;  data[len++] = (byte)0x01;  break;
                case (byte)0xA2: data[len++] = (byte)0xA0;  data[len++] = (byte)0x02;  break;
                default:         data[len++] = b;                                      break;
            }
        }
        data[len++] = (byte)0xA2; // stop

        try
        {
            BT_OutStream.write( data, 0, len );
            BT_OutStream.flush();
        }
        catch( IOException e )
        {
        }
    }

    //---------------------------------------------------------------
    private boolean updateIn()
    {
        if( !isConnected )
        {
            return (false);
        }

        int r;
        try
        {
            while( !ready && BT_InStream.available() > 0 && (r = BT_InStream.read()) >= 0 )
            {
                switch( r )
                {
                    case 0xA1: // start
                        pos = 0;
                        flag = false;
                        break;

                    case 0xA2: // STOP

                        ready = true;
                        flag = false;
                        break;

                    case 0xA0: //
                        flag = true;
                        break;

                    default:
                        if( flag )
                        {
                            r += 0xA0;
                        }
                        bufferIN.put( pos++, (byte)r );
                        flag = false;
                        break;
                }
            }
        } catch( IOException e )
        {
            Log.e( TAG, "error read " );
        }
        if( pos >= 255 )
        {
            ready = false;
            pos   = 0;
            flag  = false;
        }
        if( ready )
        {
            orb_manager.process( bufferIN );
            ready = false;
            pos   = 0;
            flag  = false;
            return (true);
        }
        return (false);
    }

    //---------------------------------------------------------------
    protected boolean update()
    {
        boolean ret = false;
        synchronized( this )
        {
            if( updateIn() )
            {
                ret = true;
            }
            updateOut();
        }
        return (ret);
    }

} // end of class
