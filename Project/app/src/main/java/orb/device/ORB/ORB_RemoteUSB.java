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
import android.os.Build;
import android.util.Log;

import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeoutException;

import orb.main.R;


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
    private static final String ACTION_USB_PERMISSION = "USB_PERMISSION"; //"com.android.example.USB_PERMISSION";
    private static final String TAG = "ORB_USB";

    //---------------------------------------------------------------
    public ORB_RemoteUSB( ORB_Manager orb_manager, Activity activity )
    {
        super(orb_manager);
        this.activity    = activity;
        this.mUsbManager = (UsbManager)activity.getSystemService(Context.USB_SERVICE);
        this.bufferIN    = ByteBuffer.allocate( 128 );
        this.bufferOUT   = ByteBuffer.allocate( 128 );

        PendingIntent permissionIntent = PendingIntent.getBroadcast(activity,
                0,
                new Intent(UsbManager.ACTION_USB_DEVICE_ATTACHED),
                PendingIntent.FLAG_MUTABLE);
        IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);

        activity.registerReceiver(usbReceiver, filter);

        permissionIntent = PendingIntent.getBroadcast(activity,
                0,
                new Intent(UsbManager.ACTION_USB_DEVICE_DETACHED),
                PendingIntent.FLAG_MUTABLE);
        filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);

        activity.registerReceiver(usbReceiver, filter);

        open();

        //mUsbManager.requestPermission(device, permissionIntent);
    }

    //---------------------------------------------------------------
    public final BroadcastReceiver usbReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(    ACTION_USB_PERMISSION.equals(action)
                && intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) )
            {
                synchronized (this) {
                    open();
                }
            }
            else if( UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action) )
            {
                // todo Toast raus???
                // it doesn't make sense because permission is requested too
                /*activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(activity, R.string.toast_usb_connected,Toast.LENGTH_SHORT).show();
                    }
                });

                 */
                open(); //setDevice(device);
            }
            else if( UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action) )
            {
                // Toast raus???
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(activity, R.string.toast_usb_deconnected,Toast.LENGTH_SHORT).show();
                    }
                });
                close();
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
            if( mUsbManager.hasPermission( device ) )
            {
                setDevice( device );
            }
            else
            {
                        // final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

                        PendingIntent permissionIntent = PendingIntent.getBroadcast(activity,
                                0,
                                new Intent(ACTION_USB_PERMISSION),
                                PendingIntent.FLAG_MUTABLE);
                        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                        activity.registerReceiver(usbReceiver, filter);

                        mUsbManager.requestPermission(device, permissionIntent);
                    }
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
      //  synchronized (this) {
            if (usbConnected && mConnection != null) {
                mConnection.close();
            }
            usbConnected = false;

            // todo: close usb device
       // }
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

        requestOUT.queue( bufferOUT, size );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if( mConnection.requestWait(500) == requestOUT ) // wait for status event
                {
                    //Log.i(TAG, "CONFIG");
                }
            }
            else {
                if( mConnection.requestWait() == requestOUT ) // wait for status event
                {
                    //Log.i(TAG, "CONFIG");
                }
            }
        } catch (TimeoutException e) {
        }
    }

    //---------------------------------------------------------------
    private boolean updateIn()
    {
        if( usbConnected ) {
            requestIN.queue(bufferIN, 64+1);  // queue a request on the interrupt endpoint

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (mConnection.requestWait(500) == requestIN) // wait for status event
                    {
                        orb_manager.process(bufferIN);
                        return (true);
                    }
                }
                else {
                    if (mConnection.requestWait() == requestIN) // wait for status event
                    {
                        orb_manager.process(bufferIN);
                        return (true);
                    }
                }
            } catch (TimeoutException e) {
            }
        }
        return( false );
    }

    //---------------------------------------------------------------
    protected boolean update()
    {
        boolean ret = false;
        //synchronized (this)
        //{
            if( usbConnected && updateIn() )
            {
                ret = true;
            }
            updateOut();
       //}
        return( ret );
    }
} // end of class
