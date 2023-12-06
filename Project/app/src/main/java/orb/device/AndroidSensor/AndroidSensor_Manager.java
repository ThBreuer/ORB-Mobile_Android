//*******************************************************************
package orb.device.AndroidSensor;

//*******************************************************************
import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import java.util.ArrayList;
import orb.robot.orb.ORB_Communicator;

//*******************************************************************
/**
 * ...
 * @author Thomas Breuer
 */
public class AndroidSensor_Manager
{
    //--------------------------------------------------------------------------------------------
    private SensorManager                   sensorManager;
    private ArrayList<AndroidSensor_Sensor> list;
    private ORB_Communicator                orb_report;

    //--------------------------------------------------------------------------------------------
    public AndroidSensor_Manager( ORB_Communicator orb_report, Activity activity )
    {
        this.orb_report    = orb_report;
        this.sensorManager = (SensorManager)activity.getSystemService( Context.SENSOR_SERVICE );
        this.list          = new ArrayList<AndroidSensor_Sensor>();
    }

    //--------------------------------------------------------------------------------------------
    public void configSensor( int type, String name )
    {
        AndroidSensor_Sensor sensor = null;

        // Sensor mit gewuenschtem type in Liste?
        for( AndroidSensor_Sensor sTmp : list )
        {
            if( sTmp.getType() == type )
            {
                sensor = sTmp;
                break;
            }
        }
        if( sensor == null )         // wenn nicht, erzeuge Sensor
        {
            sensor = new AndroidSensor_Sensor( sensorManager, type, name );
            if( sensor != null )
            {
                list.add(sensor);
            }
        }
        else                    // ... sonst setze Namen
        {
            sensor.setName( name );
        }
    }

    //--------------------------------------------------------------------------------------------
    public void reset()
    {
        list.clear();
    }

    //--------------------------------------------------------------------------------------------
    public void close()
    {
        for( AndroidSensor_Sensor sTmp : list )
        {
            sTmp.unregister();
        }
    }

    //--------------------------------------------------------------------------------------------
    public void update()
    {
        orb_report.reportAndroidSensor();
    }

    //--------------------------------------------------------------------------------------------
    public ArrayList<AndroidSensor_Sensor> getList()
    {
        return( list );
    }
}
