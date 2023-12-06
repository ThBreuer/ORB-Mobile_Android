//*******************************************************************
package orb.device.AndroidSensor;

//*******************************************************************
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

//*******************************************************************
/**
 * ...
 * @author Thomas Breuer
 */
public class AndroidSensor_Sensor implements SensorEventListener
{
    private SensorManager sensorManager;
    private SensorEvent sensorEvent = null;
    private int type;
    private String name;
    private Sensor sensor = null;

    //--------------------------------------------------------------------------------------------
    AndroidSensor_Sensor( SensorManager sensorManager, int type, String name )
    {
        this.sensorManager = sensorManager;
        this.type = type;
        this.name = name;

        sensor = sensorManager.getDefaultSensor( this.type );
        if( sensor != null)
        {
            sensorEvent = null;
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL );
        }
    }

    //--------------------------------------------------------------------------------------------
    void setName( String name )
    {
        this.name = name;
    }

    //--------------------------------------------------------------------------------------------
    void unregister( )
    {
        if( sensor != null)
        {
            sensorManager.unregisterListener(this);
        }
    }

    //--------------------------------------------------------------------------------------------
    int getType()
    {
        return( type );
    }

    //--------------------------------------------------------------------------------------------
    public String getName()
    {
        return( name );
    }

    //--------------------------------------------------------------------------------------------
    public SensorEvent getSensorEvent()
    {
        return( sensorEvent );
    }

    //--------------------------------------------------------------------------------------------
    @Override
    public void onSensorChanged( SensorEvent event )
    {
        sensorEvent = event;
    }

    //--------------------------------------------------------------------------------------------
    @Override
    public void onAccuracyChanged( Sensor sensor, int accuracy )
    {
    }
}
