//*******************************************************************
package orb.robot.orb;

//*******************************************************************
import android.hardware.SensorEvent;
import android.os.Build;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import orb.device.AndroidSensor.AndroidSensor_Manager;
import orb.device.AndroidSensor.AndroidSensor_Report;
import orb.device.AndroidSensor.AndroidSensor_Sensor;
import orb.device.Monitor.Monitor_Activity;
import orb.device.Monitor.Monitor_Manager;
import orb.device.Monitor.Monitor_Report;
import orb.device.ORB.ORB_Manager;
import orb.device.ORB.ORB_Report;
import orb.device.ORB.cPropFromORB;
import orb.main.MainActivity;
import orb.robot.RobotCommunicator;

//*******************************************************************
/**
 * ...
 *
 * @author Thomas Breuer
 *
 * This class is based on "WeDoCommunicator.java" by Beate Jost
 */
public class ORB_Communicator extends    RobotCommunicator
                              implements Runnable,
                                         ORB_Report,
                                         AndroidSensor_Report,
                                         Monitor_Report

{
    //---------------------------------------------------------------
    public final ORB_Manager      orbManager;

    //---------------------------------------------------------------
    private MainActivity          mainActivity;
    private WebView               webView;
    private AndroidSensor_Manager androidSensorManager;
    private Monitor_Manager monitorManager;

    private java.lang.Thread      mainThread;
    private boolean runMainThread = false;
    private boolean runFlag       = false;

    private static final String TAG = ORB_Communicator.class.getSimpleName();

    //---------------------------------------------------------------
    public ORB_Communicator( MainActivity mainActivity, WebView webView )
    {
        this.ROBOT        = "orb";
        this.webView      = webView;
        this.mainActivity = mainActivity;

        orbManager           = new ORB_Manager          ( this, mainActivity );
        androidSensorManager = new AndroidSensor_Manager( this, mainActivity );
        monitorManager       = new Monitor_Manager      ( this );

        mainThread = new Thread( this );
        if( mainThread.getState() == Thread.State.NEW )
        {
            runMainThread = true;
            mainThread.start();
            mainThread.setPriority( 4 ); //MAX_PRIORITY );
        }
        Log.d( TAG, "OrbCommunicator instantiated" );
    }

    //---------------------------------------------------------------
    @Override
    public void close()
    {
        runMainThread = false;
        orbManager.close();
        androidSensorManager.close();
        monitorManager.close();

    }

    //---------------------------------------------------------------
    @Override
    public void run()
    {
        while( runMainThread )
        {
            orbManager.update();
            androidSensorManager.update();
            monitorManager.update();
            mainActivity.setTextStatus( orbManager );

            monitorManager.setTextStatus( orbManager );

            try
            {
                Thread.sleep( 10 ); // todo check timing
            }
            catch (InterruptedException e )
            {
            }
        }
    }

    //---------------------------------------------------------------
    @Override
    public void jsToRobot( JSONObject msg )
    {
        try
        {
            String target = msg.getString( "target" );
            String type   = msg.getString( "type" );

            if( !target.equals( ROBOT ) )
            {
                Log.e( TAG, "jsToRobot: " + "wrong target" );
                return; // wrong robot
            }

            switch( type )
            {
                case "startScan":     handle_startScan    ( msg ); runFlag = false; break;
                case "stopScan":      handle_stopScan     ( msg ); runFlag = false; break;
                case "connect":       handle_connect      ( msg ); runFlag =  true; break;
                case "disconnect":    handle_disconnect   ( msg ); runFlag = false; break;
                case "configToORB":   handle_configToORB  ( msg ); runFlag =  true; break;
                case "propToORB":     handle_propToORB    ( msg );                  break;
                case "settingsToORB": handle_settingsToORB( msg ); runFlag =  true; break;
                case "commandToAS":   handle_commandToAS  ( msg ); runFlag =  true; break;
                case "configToAS":    handle_configToAS   ( msg ); runFlag =  true; break;
                case "layoutToMon":   handle_layoutToMon  ( msg );                  break;
                case "textToMon":     handle_textToMon    ( msg );                  break;

                default:
                    Log.e( TAG, "Not supported msg: " + msg );
                    break;
            }
        }
        catch (final JSONException e)
        {
            // ignore invalid messages
            Log.e( TAG, "Json parsing error: " + e.getMessage() + " processing: " + msg );
        }
        catch (final NullPointerException e)
        {
            Log.e( TAG, "Command parsing error: " + e.getMessage() + " processing: " + msg );
        }
        catch (final Exception e)
        {
            Log.e( TAG, "Exception: " + e.getMessage() + " processing: " + msg );
        }
    }

    //---------------------------------------------------------------
    // Handle input from JavaScript (jsToRobot)
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    private void handle_startScan( JSONObject msg )
    {
        orbManager.scan();
    }

    //---------------------------------------------------------------
    private void handle_stopScan( JSONObject msg )
    {
        Log.e( TAG, "stop scan is not implemented for orb robot" );
    }

    //---------------------------------------------------------------
    private void handle_connect( JSONObject msg )
    {
        try
        {
            String robot = msg.getString( "robot" );
            orbManager.connect( robot );
        }
        catch( final JSONException e )
        {
            // ignore invalid messages
            Log.e( TAG, "Json parsing error: " + e.getMessage() + " processing: " + msg );
        }
    }

    //---------------------------------------------------------------
    private void handle_disconnect( JSONObject msg )
    {
        Log.d( TAG, "disconnect is not implemented for orb robot" );
    }

    //---------------------------------------------------------------
    private void handle_propToORB( JSONObject msg )
    {
        try
        {
            if( msg.has("data" ) )
            {
                JSONObject data = msg.getJSONObject( "data" );
                JSONArray motor = data.getJSONArray( "Motor" );
                synchronized( orbManager )
                {
                    for( int i = 0; i < motor.length(); i++ )
                    {
                        JSONObject m = motor.getJSONObject( i );
                        orbManager.setMotor( i, m.getInt( "mode"  ),
                                                m.getInt( "speed" ),
                                                m.getInt( "pos"   ) );
                    }
                }
                JSONArray servo = data.getJSONArray( "Servo" );
                synchronized( orbManager )
                {
                    for( int i = 0; i < servo.length(); i++ )
                    {
                        JSONObject s = servo.getJSONObject( i );
                        orbManager.setModelServo( i, s.getInt( "mode" ),
                                                     s.getInt( "pos"  ) );
                    }
                }
            }
            else
            {
                Log.e( TAG, " processing: " + msg );
            }
        }
        catch( final JSONException e )
        {
            // ignore invalid messages
            Log.e( TAG, "Json parsing error: " + e.getMessage() + " processing: " + msg );
        }
    }

    //---------------------------------------------------------------
    private void handle_configToORB( JSONObject msg )
    {
        try
        {
            if( msg.has("data" ) )
            {
                JSONObject data = msg.getJSONObject( "data" );
                JSONArray motor = data.getJSONArray( "Motor" );
                synchronized( orbManager )
                {
                    for( byte i = 0; i < motor.length(); i++ )
                    {
                        JSONObject m = motor.getJSONObject( i );
                        orbManager.configMotor( i, m.getInt( "tics" ),
                                                   m.getInt( "acc"  ),
                                                   m.getInt( "Kp"   ),
                                                   m.getInt( "Ki"   ) );
                    }
                }
                JSONArray sensor = data.getJSONArray( "Sensor" );
                synchronized( orbManager )
                {
                    for( byte i = 0; i < sensor.length(); i++ )
                    {
                        JSONObject s = sensor.getJSONObject( i );
                        orbManager.configSensor( i, s.getInt( "type"   ),
                                                    s.getInt( "mode"   ),
                                                    s.getInt( "option" ) );
                    }
                }
            }
            else
            {
                Log.e( TAG, " processing: " + msg );
            }
        }
        catch( final JSONException e )
        {
            // ignore invalid messages
            Log.e( TAG, "Json parsing error: " + e.getMessage() + " processing: " + msg );
        }
    }

    //---------------------------------------------------------------
    private void handle_settingsToORB( JSONObject msg )
    {
        try
        {
            if( msg.has("data" ) )
            {
                JSONObject data = msg.getJSONObject( "data" );

                synchronized( orbManager )
                {
                    if( data.getBoolean( "update" ) )
                    {
                        orbManager.sendData( data.getString( "Name"    ),
                                             data.getDouble( "VCC_ok"  ),
                                             data.getDouble( "VCC_low" ) );
                    }
                    else
                    {
                        orbManager.sendRequest();
                    }
                }
            }
            else
            {
                Log.e( TAG, " processing: " + msg );
            }
        }
        catch( final JSONException e )
        {
            // ignore invalid messages
            Log.e( TAG, "Json parsing error: " + e.getMessage() + " processing: " + msg );
        }
    }

   //---------------------------------------------------------------
    private void handle_commandToAS( JSONObject msg )
    {
        try
        {
            if( msg.has("data") )
            {
                JSONObject data = msg.getJSONObject( "data" );
                String name = data.getString( "cmd" );
                androidSensorManager.reset(); // todo use cmd
            }
            else
            {
                Log.e( TAG, " processing: " + msg );
            }
        }
        catch (final JSONException e)
        {
            Log.e( TAG, "Json parsing error: " + e.getMessage() + " processing: " + msg );
        }
    }

    //---------------------------------------------------------------
    private void handle_configToAS( JSONObject msg )
    {
        try
        {
            if( msg.has("data" ) )
            {
                JSONObject data = msg.getJSONObject( "data" );
                String name = data.getString( "name" );
                int type = data.getInt( "type" );
                androidSensorManager.configSensor( type, name );
            }
            else
            {
                Log.e( TAG, " processing: " + msg );
            }
        }
        catch( final JSONException e )
        {
            // ignore invalid messages
            Log.e( TAG, "Json parsing error: " + e.getMessage() + " processing: " + msg );
        }
    }

    //---------------------------------------------------------------
    private void handle_layoutToMon(JSONObject msg )
    {
        try
        {
            if(msg.has("data"))
            {
                synchronized (orbManager)
                {
                    JSONObject data = msg.getJSONObject( "data" );
                    monitorManager.setLayout( data );
                }
            }
            else {
                Log.e( TAG, " processing: " + msg );
            }
        }
        catch (final JSONException e)
        {
            // ignore invalid messages
            Log.e( TAG, "Json parsing error: " + e.getMessage() + " processing: " + msg );
        }
    }

    //---------------------------------------------------------------
    private void handle_textToMon(JSONObject msg )
    {
        try
        {
            if(msg.has("data"))
            {
                synchronized (orbManager)
                {
                    JSONObject data = msg.getJSONObject( "data" );
                    JSONArray  text = data.getJSONArray( "text" );

                    String str = "";
                    for( byte z = 0; z < text.length(); z++ )
                    {
                        str += text.getString( z ) +"\n";
                    }
                    monitorManager.setText( str );
                }
            }
            else {
                Log.e( TAG, " processing: " + msg );
            }
        }
        catch (final JSONException e)
        {
            // ignore invalid messages
            Log.e( TAG, "Json parsing error: " + e.getMessage() + " processing: " + msg );
        }
    }

    //---------------------------------------------------------------
    // Report to JavaScript
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    @Override
    public void reportDisconnect()
    {
        reportStateChanged( "connect", "disconnected", "orb" );
    }

    //---------------------------------------------------------------
    @Override
    public void reportConnect( String brickId, String brickName )
    {
        reportStateChanged( "connect","connected", brickId, "brickname", brickName );
    }

    //---------------------------------------------------------------
    @Override
    public void reportScan( String brickId, String brickName )
    {
        reportStateChanged( "scan", "appeared", brickId, "brickname", brickName );
    }

    //---------------------------------------------------------------
    @Override
    public void reportORB( )
    {
        JSONObject msg = new JSONObject();
        try
        {
            if( orbManager.getSettings().isNew() )
            {
                msg.put("target", "orb");
                msg.put("type", "settingsFromORB");
                JSONObject data = new JSONObject();
                JSONArray version = new JSONArray();
                version.put(orbManager.getSettings().getVersion()[0]);
                version.put(orbManager.getSettings().getVersion()[1]);
                data.put("Version",version);
                JSONArray board = new JSONArray();
                board.put(orbManager.getSettings().getBoard()[0]);
                board.put(orbManager.getSettings().getBoard()[1]);
                data.put("Board",board);
                data.put("Name",orbManager.getSettings().getName());
                data.put("VCC_ok",orbManager.getSettings().getVccOk());
                data.put("VCC_low",orbManager.getSettings().getVccLow());
                msg.put( "data", data );
                sendToJS( msg.toString() );
            }
            else
            {
                if( runFlag == true )
                {
                    msg.put( "target", "orb" );
                    msg.put( "type", "propFromORB" );
                    JSONObject data = new JSONObject();

                    JSONArray motor = new JSONArray();
                    for( byte i = 0; i < cPropFromORB.numOfMotorPorts; i++ )
                    {
                        JSONObject m = new JSONObject();
                        cPropFromORB.Motor value = orbManager.getMotor( i );
                        m.put( "pwr", value.pwr );
                        m.put( "speed", value.speed );
                        m.put( "pos", value.pos );
                        motor.put( m );
                    }
                    data.put( "Motor", motor );

                    JSONArray sensor = new JSONArray();
                    for( byte i = 0; i < cPropFromORB.numOfSensorPorts; i++ )
                    {
                        JSONObject s = new JSONObject();
                        cPropFromORB.Sensor value = orbManager.getSensor( i );
                        s.put( "valid", value.isValid );
                        s.put( "type", value.type );
                        s.put( "option", value.option );
                        JSONArray v = new JSONArray();
                        v.put( value.value[0] );
                        v.put( value.value[1] );
                        s.put( "value", v );
                        sensor.put( s );
                    }

                    data.put( "Sensor", sensor );

                    data.put( "Vcc", orbManager.getVcc() );

                    JSONArray digital = new JSONArray();
                    digital.put( orbManager.getSensorDigital( (byte) 0 ) );
                    digital.put( orbManager.getSensorDigital( (byte) 1 ) );

                    data.put( "Digital", digital );

                    data.put( "Status", orbManager.getStatus() );  // TODO: get status

                    msg.put( "data", data );
                    sendToJS( msg.toString() );
                }
            }
        }
        catch( JSONException e )
        {
            Log.e(TAG, "Json parsing error: " + e.getMessage());
        }
    }

    //---------------------------------------------------------------
    // target:orb,type:sensorFromAS,data:{abc:[0,1],xyz:[7]}
    //
    @Override
    public void reportAndroidSensor()
    {
        if( runFlag == true )
        {
            JSONObject msg = new JSONObject();
            try
            {
                msg.put( "target", "orb" );
                msg.put( "type", "sensorFromAS" );

                JSONObject data = new JSONObject();
                for( AndroidSensor_Sensor s : androidSensorManager.getList() )
                {
                    JSONArray val = new JSONArray();
                    SensorEvent event = s.getSensorEvent();
                    for( byte i = 0; event != null && i < event.values.length; i++ )
                    {
                        val.put( event.values[i] );
                    }
                    data.put( s.getName(), val );
                }
                msg.put( "data", data );
            }
            catch( JSONException e )
            {
                Log.e( TAG, "Json parsing error: " + e.getMessage() );
            }
            sendToJS( msg.toString() );
        }
    }

 
    //---------------------------------------------------------------
    @Override
    public void reportMonitor()
    {
        if( Monitor_Activity.DataHolder.enableReport == true ) {
            String key = monitorManager.getKey();
            JSONObject msg = new JSONObject();
            try {
                msg.put("target", "orb");
                msg.put("type", "keyFromMon");
                JSONObject data = new JSONObject();
                data.put("key", key);
                msg.put("data", data);
            } catch (JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
            }
            sendToJS(msg.toString());
        }
    }

    //---------------------------------------------------------------
    //
    //---------------------------------------------------------------
    /**
     * Report to the webview new state information.
     *
     * @param strg, min. type, state and brickid in this order are reqired (3 arguments).
     *              All next arguments have to appear in pairs, key <-> value
     */
    @Override
    public void reportStateChanged( String type, String state, String brickid, String... strg )
    {
        try
        {
            if( type != null && state != null && brickid != null )
            {
                JSONObject newMsg = new JSONObject();
                newMsg.put( "target", ROBOT );
                newMsg.put( "type", type );
                newMsg.put( "state", state );
                newMsg.put( "brickid", brickid );
                if( strg != null )
                {
                    for( int i = 0; i < strg.length; i += 2 )
                    {
                        newMsg.put( strg[i], strg[i + 1] );
                    }
                }
                sendToJS( newMsg.toString() );
            }
            else
            {
                throw new IllegalArgumentException(
                        "Min. 3 parameters required + additional parameters in pairs!" );
            }
        }
        catch( JSONException | IllegalArgumentException | IndexOutOfBoundsException e )
        {
            Log.e( TAG, e.getMessage() + "caused by: " + type + state + brickid + strg );
        }
    }

    //---------------------------------------------------------------
    private void sendToJS( String str )
    {
        this.webView.post( new Runnable()
                           {
                               @Override
                               public void run()
                               {
                                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                                   {
                                       webView.evaluateJavascript("webviewController.appToJsInterface('" + str + "');", new ValueCallback<String>()
                                       {
                                           @Override
                                           public void onReceiveValue(String value)
                                           {
                                               if( value == null || value.compareTo("true") != 0 )
                                               {
                                                   runFlag = false;
                                                   Monitor_Activity.DataHolder.enableReport = false;
                                               }
                                           }
                                       });
                                   }
                               }
                           });

        // todo check if we need this further more:
        /*
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)

                        webView.loadUrl( "javascript:webviewController.appToJsInterface('" + str + "')" );

                    }
                } );
        */
    }
}  // end of class
