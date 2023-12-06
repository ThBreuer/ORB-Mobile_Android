package orb.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import java.util.Locale;


//*************************************************************************************************
public class ConfigORB_Activity extends Activity
{
    //---------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_orb);

        Bundle bundle = getIntent().getExtras();

        ((TextView)findViewById( R.id.textHW_Version )).setText(bundle.getString(EXTRA_HW_VERSION));
        ((TextView)findViewById( R.id.textSW_Version )).setText(bundle.getString(EXTRA_SW_VERSION));
        ((EditText)findViewById( R.id.editName       )).setText(bundle.getString(EXTRA_NAME));
        ((EditText)findViewById( R.id.editVccOk      )).setText( String.format( Locale.ENGLISH, "%.1f", bundle.getDouble( EXTRA_VCC_OK)) );
        ((EditText)findViewById( R.id.editVccLow     )).setText( String.format( Locale.ENGLISH, "%.1f",bundle.getDouble( EXTRA_VCC_LOW)));

        //  createDeviceList();
    }

    //---------------------------------------------------------------------------------------------
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    //*********************************************************************************************
    class myOnItemClickListener implements AdapterView.OnItemClickListener
    {
        //-----------------------------------------------------------------------------------------
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id)
        {
            // Create the result Intent ...
            Intent intent = new Intent();
//            intent.putExtra(EXTRA_DEVICE_ADDRESS, BT_Device.getAddress());

            // Set result and finish this Activity
            setResult(Activity.RESULT_CANCELED, intent);
            finish();
        }
    }

    //---------------------------------------------------------------------------------------------
    public static String EXTRA_HW_VERSION  = "HW_VERSION";
    public static String EXTRA_SW_VERSION  = "SW_VERSION";
    public static String EXTRA_NAME    = "NAME";
    public static String EXTRA_VCC_OK  = "VCC_OK";
    public static String EXTRA_VCC_LOW = "VCC_LOW";

    //---------------------------------------------------------------------------------------------
    public void onClick_SendToORB(View view)
    {
        //if (isRunning) {
        ///runMainThread=false;

        //orb_BT.setRunMode((byte)0);
        //orb_USB.setRunMode((byte)0);
        //while( !doFinish );


        //finish();

        //}
        //if( !isRunning )
        //	BT_StartDeviceList();
        // Set result and finish this Activity
        // Create the result Intent ...

        String name   =                     ((EditText)findViewById( R.id.editName  )).getText().toString();
        double VccOk  = Double.parseDouble( ((EditText)findViewById( R.id.editVccOk )).getText().toString() );
        double VccLow = Double.parseDouble( ((EditText)findViewById( R.id.editVccLow)).getText().toString() );

        Intent intent = new Intent();

        intent.putExtra(EXTRA_NAME,    name  );
        intent.putExtra(EXTRA_VCC_OK,  VccOk );
        intent.putExtra(EXTRA_VCC_LOW, VccLow);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    //---------------------------------------------------------------------------------------------
    public void onClick_cancel(View view)
    {
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }
}
