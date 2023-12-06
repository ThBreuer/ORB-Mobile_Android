//*******************************************************************
package orb.device.ORB;

//*******************************************************************
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.util.Log;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

// *******************************************************************
/**
 * ...
 * @author Thomas Breuer
 */
class ORB_RemoteUSB extends ORB_Remote
{
    //---------------------------------------------------------------
    private ByteBuffer          bufferIN;
    private ByteBuffer          bufferOUT;
    private Activity            activity;
    private UsbManager          mUsbManager;
    private UsbDeviceConnection mConnection;
    private UsbRequest          requestIN;
    private UsbRequest          requestOUT;
    private boolean             usbConnected = false;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String TAG = "ORB_USB";

    //---------------------------------------------------------------
    public ORB_RemoteUSB( ORB_Manager orb_manager, Activity activity )
    {
        super(orb_manager);
        this.activity    = activity;
        this.mUsbManager = (UsbManager)activity.getSystemService(Context.USB_SERVICE);
        this.bufferIN    = ByteBuffer.allocate( 128 );
        this.bufferOUT   = ByteBuffer.allocate( 128 );
    }

    //---------------------------------------------------------------
    public final BroadcastReceiver usbReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            //call method to set up device communication
                            setDevice(device);
                        }
                    }
                    else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

    //---------------------------------------------------------------
    public void open( )
    {
        HashMap<String, UsbDevice> map = mUsbManager.getDeviceList();

        Iterator<UsbDevice> it = map.values().iterator();

        while( it.hasNext() )
        {
            UsbDevice device = it.next();

            final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

            PendingIntent permissionIntent = PendingIntent.getBroadcast( activity,
                                                                         0,
                                                                         new Intent(ACTION_USB_PERMISSION),
                                                                         0 );
            IntentFilter filter = new IntentFilter( ACTION_USB_PERMISSION );
            activity.registerReceiver( usbReceiver, filter );

            mUsbManager.requestPermission( device, permissionIntent );
        }
    }

    //---------------------------------------------------------------
    public boolean isAvailable()
    {
        HashMap<String, UsbDevice> map = mUsbManager.getDeviceList();

        Iterator<UsbDevice> it = map.values().iterator();

        return( it.hasNext() );
    }

    //---------------------------------------------------------------
    public void close()
    {
        if( usbConnected && mConnection != null )
        {
            mConnection.close();
        }
        usbConnected = false;
        // todo: close usb device
    }

    //---------------------------------------------------------------
    public boolean isConnected()
    {
        return( usbConnected );
    }

    //-----------------------------------------------------------------
    private void setDevice( UsbDevice device )
    {
        if( device.getInterfaceCount() != 1 )
        {
            Log.e(TAG, "could not find interface");
            return;
        }

        UsbInterface intf = device.getInterface(0);

        // device should have one endpoint
        if( intf.getEndpointCount() < 1 )
        {
            Log.e(TAG, "could not find endpoint");
            return;
        }

        // endpoint should be of type interrupt
        // todo index is NOT EP address, check mAdress == 129 or epIN.getDirection() == UsbConstants.USB_DIR_IN
        UsbEndpoint epIN = intf.getEndpoint( 1 );
        if( epIN.getType() != UsbConstants.USB_ENDPOINT_XFER_INT )
        {
            Log.e(TAG, "endpoint is not interrupt type");
            return;
        }

        // todo index is NOT EP address, check mAdress == 1 or epOUT.getDirection() == UsbConstants.USB_DIR_OUT
        UsbEndpoint epOUT = intf.getEndpoint( 0 );
        if( epOUT.getType() != UsbConstants.USB_ENDPOINT_XFER_INT )
        {
            Log.e(TAG, "endpoint is not interrupt type");
            return;
        }

        if( device != null )
        {
            mConnection = mUsbManager.openDevice(device);
            if( mConnection != null && mConnection.claimInterface( intf, true ) )
            {
                Log.e(TAG, "open SUCCESS");

                requestIN = new UsbRequest();
                requestIN.initialize( mConnection, epIN );

                requestOUT = new UsbRequest();
                requestOUT.initialize( mConnection, epOUT );
                usbConnected = true;
            }
            else
            {
                Log.d(TAG, "open FAIL");
                mConnection = null;
            }
        }
        else
        {
            usbConnected = false;
        }
    }

    //---------------------------------------------------------------
    private void updateOut()
    {
        int  size = orb_manager.fill(bufferOUT);

        requestOUT.queue( bufferOUT, 64 );

        if( mConnection.requestWait() == requestOUT ) // wait for status event
        {
            //Log.i(TAG, "CONFIG");
        }
    }

    //---------------------------------------------------------------
    private boolean updateIn()
    {
        if( usbConnected ) {
            requestIN.queue(bufferIN, 64+1);  // queue a request on the interrupt endpoint

            if (mConnection.requestWait() == requestIN) // wait for status event
            {
                orb_manager.process(bufferIN);
                return (true);
            }
        }
        return( false );
    }

    //---------------------------------------------------------------
    protected boolean update()
    {
        boolean ret = false;
        synchronized (this)
        {
            if( usbConnected && updateIn() )
            {
                ret = true;
            }
            updateOut();
        }
        return( ret );
    }
} // end of class
