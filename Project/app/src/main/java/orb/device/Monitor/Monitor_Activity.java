package orb.device.Monitor;

import static java.lang.String.format;

import android.app.Activity;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import orb.device.ORB.ORB_Manager;
import orb.main.MainActivity;
import orb.main.R;

public class Monitor_Activity extends Activity implements Runnable
{
    private static final String TAG = orb.device.Monitor.Monitor_Activity.class.getSimpleName();

    public Monitor_Activity()
    {
        mainThread = new Thread( this );
    }

    //---------------------------------------------------------------------------------------------
    public static class DataHolder {
        private static String data;
        private static byte key;
        private static JSONObject layout;
        private static boolean isNewLayout = false;

        private static double Vcc = 0;
        private static String status ="...";
        private static boolean isConnected = false;

        private static boolean stopReq = false;

        // todo make private

        public static boolean enableReport = false;

        //-----------------------------------------------------------------------------------------
        public static void setLayout( JSONObject layout )
        {
            DataHolder.layout = layout;
            isNewLayout = true;
        }


        //---------------------------------------------------------------------------------------------
        public static void setTextStatus(final ORB_Manager orb)
        {
            if( orb.isConnected() )
            {
                Vcc = orb.getVcc();
                status = orb.getSettings().getName();
                isConnected = true;
            }
            else
            {
                Vcc = 0;
                status = "";
                isConnected = false;
            }
        }


        //-----------------------------------------------------------------------------------------
        public static JSONObject getLayout(  )
        {
            return( DataHolder.layout );
        }

        //-----------------------------------------------------------------------------------------
        public static JSONObject getNewLayout(  )
        {
            if( isNewLayout )
            {
                isNewLayout = false;
                //layout  = DataHolder.layout;
                return( DataHolder.layout );
            }
            return( null );
        }

        //-----------------------------------------------------------------------------------------
        public static byte getKey() {return key;}

        //-----------------------------------------------------------------------------------------
        public static String getData() {return data;}

        //-----------------------------------------------------------------------------------------
        public static void setKey(byte key) {DataHolder.key = key;}

        //-----------------------------------------------------------------------------------------
        public static void setData(String data) {DataHolder.data = data; DataHolder.enableReport=true; }

        //-----------------------------------------------------------------------------------------
        public static void stopMonitor() {DataHolder.stopReq = true;}
    }

    //---------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.monitor_activity);

        DataHolder.stopReq = false;

        JSONObject msg = DataHolder.getLayout( );
        if( msg != null)
        {
            setLayout( msg );
        }

        if( mainThread.getState() == Thread.State.NEW )
        {
            runMainThread = true;
            mainThread.start();
            mainThread.setPriority(2);
        }
    }

    //-----------------------------------------------------------------
    @Override
    public void onDestroy()
    {
        runMainThread = false;
        DataHolder.setKey( (byte)0 );
        super.onDestroy();
    }

    Thread mainThread;
    boolean runMainThread = false;


    //---------------------------------------------------------------------------------------------
    public void setTextStatus()
    {
        TextView viewStatus = findViewById( R.id.msgName);
        TextView viewVoltage = findViewById( R.id.msgVoltage);

        if( DataHolder.isConnected )
        {
            viewStatus.setText( DataHolder.status );
            viewVoltage.setText( format( Locale.getDefault(), "%s: %.1f V", getString(R.string.text_state_batterie), DataHolder.Vcc ) );
        }
        else
        {
            viewStatus.setText( R.string.text_state_orb_not_connected );
            viewVoltage.setText( format("%s: --- V", getString(R.string.text_state_batterie) ) );
        }
    }

    //-----------------------------------------------------------------
    private void setKeyText(int id, String text)
    {
        Button b = findViewById(id);
        if( text.length() > 0 )
        {
            b.setVisibility( View.VISIBLE );
            b.setText( Html.fromHtml( text ).toString() );
        }
        else
        {
            b.setVisibility( View.INVISIBLE );
        }
    }

    //-----------------------------------------------------------------
    private void setLayout( JSONObject layout )
    {
        try
        {
            JSONObject array = layout.getJSONObject( "Tasten");
            setKeyText(R.id.button_A1, array.getString("A1"));
            setKeyText(R.id.button_A2, array.getString("A2"));
            setKeyText(R.id.button_A3, array.getString("A3"));
            setKeyText(R.id.button_A4, array.getString("A4"));
            setKeyText(R.id.button_A5, array.getString("A5"));
            setKeyText(R.id.button_A6, array.getString("A6"));
            setKeyText(R.id.button_A7, array.getString("A7"));
            setKeyText(R.id.button_A8, array.getString("A8"));
            setKeyText(R.id.button_B1, array.getString("B1"));
            setKeyText(R.id.button_B2, array.getString("B2"));
            setKeyText(R.id.button_B3, array.getString("B3"));
            setKeyText(R.id.button_B4, array.getString("B4"));
            setKeyText(R.id.button_B5, array.getString("B5"));
            setKeyText(R.id.button_B6, array.getString("B6"));
            setKeyText(R.id.button_B7, array.getString("B7"));
            setKeyText(R.id.button_B8, array.getString("B8"));
            setKeyText(R.id.button_B9, array.getString("B9"));
            setKeyText(R.id.button_B10, array.getString("B10"));
            setKeyText(R.id.button_B11, array.getString("B11"));
            setKeyText(R.id.button_B12, array.getString("B12"));
            setKeyText(R.id.button_C1, array.getString("C1"));
        }
        catch (final JSONException e)
        {
            Log.e( TAG, "Json parsing error: " + e.getMessage() + " processing: " + layout );
        }
    }

    //-----------------------------------------------------------------
    @Override
    public void run()
    {
        Button btn[] = new Button[21];
        btn[0] = (Button) findViewById(R.id.button_A1);
        btn[1] = (Button) findViewById(R.id.button_A2);
        btn[2] = (Button) findViewById(R.id.button_A3);
        btn[3] = (Button) findViewById(R.id.button_A4);
        btn[4] = (Button) findViewById(R.id.button_A5);
        btn[5] = (Button) findViewById(R.id.button_A6);
        btn[6] = (Button) findViewById(R.id.button_A7);
        btn[7] = (Button) findViewById(R.id.button_A8);
        btn[8] = (Button) findViewById(R.id.button_B1);
        btn[9] = (Button) findViewById(R.id.button_B2);
        btn[10] = (Button) findViewById(R.id.button_B3);
        btn[11] = (Button) findViewById(R.id.button_B4);
        btn[12] = (Button) findViewById(R.id.button_B5);
        btn[13] = (Button) findViewById(R.id.button_B6);
        btn[14] = (Button) findViewById(R.id.button_B7);
        btn[15] = (Button) findViewById(R.id.button_B8);
        btn[16] = (Button) findViewById(R.id.button_B9);
        btn[17] = (Button) findViewById(R.id.button_B10);
        btn[18] = (Button) findViewById(R.id.button_B11);
        btn[19] = (Button) findViewById(R.id.button_B12);
        btn[20] = (Button) findViewById(R.id.button_C1);

        while ( runMainThread && !DataHolder.stopReq)
        {
            byte key = 0;
            for(int i=0;i<21;i++) {
                if (btn[i] != null && btn[i].isPressed()) {
                    key = (byte) (i + 1);
                    break;
                }
            }
            DataHolder.setKey( key );

            runOnUiThread(new Runnable() {
                public void run() {
                    TextView tv = findViewById( R.id.textView );
                    tv.setText(DataHolder.getData());
                    JSONObject msg = DataHolder.getNewLayout( );
                    if( msg != null)
                    {
                       setLayout( msg );
                    }
                    setTextStatus();
                }
            });

            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                Log.d( TAG, "Err");
            }
        }
        DataHolder.stopReq = false;
        Monitor_Activity.this.finish();
    }
}
