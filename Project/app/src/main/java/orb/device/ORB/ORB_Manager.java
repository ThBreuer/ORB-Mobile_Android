//*******************************************************************
package orb.device.ORB;

//*******************************************************************
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import java.nio.ByteBuffer;

//*******************************************************************
/**
 * ...
 * @author Thomas Breuer
 */
public class ORB_Manager
{
    //---------------------------------------------------------------
    private cConfigToORB     configToORB;
    private cPropToORB       propToORB;
    private cPropFromORB     propFromORB;
    private cSettingsToORB   settingsToORB;
    private cSettingsFromORB settingsFromORB;
    private ORB_Report       orb_report;
    private ORB_RemoteUSB    orb_USB;
    private ORB_RemoteBT     orb_BT;
    private int              updateTimeout = 0;

    //---------------------------------------------------------------
    public ORB_Manager( ORB_Report orb_report, Activity activity )
    {
        this.orb_report = orb_report;

        configToORB     = new cConfigToORB();
        propToORB       = new cPropToORB();
        propFromORB     = new cPropFromORB();
        settingsFromORB = new cSettingsFromORB();
        settingsToORB   = new cSettingsToORB();

        orb_USB = new ORB_RemoteUSB( this, activity );
        orb_BT  = new ORB_RemoteBT( this );
    }

    //---------------------------------------------------------------
    public void close()
    {
        orb_USB.close();
        orb_BT.close();
    }

    //---------------------------------------------------------------------------------------------
    public boolean isUSBavailable()
    {
        return( orb_USB.isAvailable() );
    }

    //---------------------------------------------------------------------------------------------
    public void scan()
    {
        if( orb_USB.isAvailable() )
        {
            orb_report.reportScan( "USB", "ORB via USB" );
        }
        for( BluetoothDevice device : orb_BT.getPairedDevices() )
        {
            orb_report.reportScan( device.getAddress(), device.getName() );
        }
    }

    //---------------------------------------------------------------
    public void connect( String addr )
    {
        orb_USB.close();
        orb_BT.close();

        if( addr.length() > 0 )
        {
            if( addr.equals( "USB" ) )
            {
                orb_USB.open();
                orb_report.reportConnect( "USB", "ORB-USB" );
            }
            else
            {
                BluetoothDevice BT_Device = orb_BT.open( addr );
                orb_report.reportConnect( BT_Device.getAddress(), BT_Device.getName() );
            }
        }
    }

    //---------------------------------------------------------------
    public void update()
    {
        ORB_Remote orbRemote = orb_USB.isConnected() ? orb_USB : orb_BT;

        if( orbRemote.update() )
        {
            orb_report.reportORB();
            updateTimeout = 0;
        }
        else
        {
            if( orb_USB.isConnected() || orb_BT.isConnected() )
            {
                if( updateTimeout++ > 200 )
                {
                    orb_report.reportDisconnect();
                    orb_USB.close();
                    orb_BT.close();
                }
            }
            else
            {
                updateTimeout = 0;
            }
        }
    }

    //---------------------------------------------------------------
    public boolean isConnected()
    {
        return(  orb_USB.isConnected() || orb_BT.isConnected() );
    }

    //---------------------------------------------------------------
    // An der Schnittstelle wurden Daten empfangen, diese jetzt
    // verarbeiten
    void process( ByteBuffer data )
    {
        if( propFromORB.get( data ) )
        {
            return;
        }
        if( settingsFromORB.get( data ) )
        {
            return;
        }
    }

    //---------------------------------------------------------------
    // Die Schnittstelle ist bereit, neue Daten zu versenden,
    // diese ggf. jetzt eintragen
    int fill(  ByteBuffer data )
    {
        int size = 0;

        if( configToORB.isNew() )
        {
            size = configToORB.fill(data);
        }
        else if( settingsToORB.isNew() )
        {
            size = settingsToORB.fill(data);
        }
        else if( propToORB.isNew() )
        {
            size = propToORB.fill(data);
        }
        return( size );
    }

    //---------------------------------------------------------------
    // Motor
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    public void  configMotor( int   id,
                              int   ticsPerRotation,
                              int   acc,
                              int   Kp,
                              int   Ki )
    {
        configToORB.configMotor( id, ticsPerRotation, acc, Kp, Ki );
    }

    //---------------------------------------------------------------
    public void setMotor( int id,
                          int mode,
                          int speed,
                          int pos )
    {
        propToORB.setMotor( id, mode, speed, pos );
    }

    //---------------------------------------------------------------
    public cPropFromORB.Motor getMotor( byte id )
    {
        return( propFromORB.getMotor( id ) );
    }

    //---------------------------------------------------------------
    // ModellServo
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    public void setModelServo( int id,
                               int  speed,
                               int  angle )
    {
        propToORB.setModelServo( id, speed, angle );
    }

    //---------------------------------------------------------------
    // Sensor
    //---------------------------------------------------------------
    public void configSensor( int id,
                              int type,
                              int mode,
                              int option )
    {
        configToORB.configSensor( id, type, mode, option );
    }

    //---------------------------------------------------------------
    public cPropFromORB.Sensor getSensor(int id )
    {
        return( propFromORB.getSensor( id ) );
    }

    //---------------------------------------------------------------
    public boolean getSensorDigital( byte id )
    {
        return( propFromORB.getSensorDigital( id ) );
    }

    //---------------------------------------------------------------
    // Settings
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    public void sendRequest()
    {
        settingsToORB.sendRequest();
    }

    //---------------------------------------------------------------
    public void sendData( String name, double VCC_ok, double VCC_low )
    {
        settingsToORB.sendData( name, VCC_ok, VCC_low );
    }

    //---------------------------------------------------------------
    public cSettingsFromORB getSettings()
    {
        return( settingsFromORB );
    }

    //---------------------------------------------------------------
    // Miscellaneous
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    public double getVcc()
    {
        return( propFromORB.getVcc() );
    }

    //---------------------------------------------------------------
    public byte getStatus()
    {
        return( propFromORB.getStatus() );
    }

    //-----------------------------------------------------------------
}
