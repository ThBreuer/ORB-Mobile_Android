package orb.main;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.Set;

public class BT_DeviceListActivity extends Activity
{
    private boolean isUSB = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);

        //Bundle bundle = getIntent().getExtras();
        //boolean flag = bundle.getBoolean("USB");
        this.isUSB = getIntent().getBooleanExtra("USB", false );
        createDeviceList();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    //---------------------------------------------------------------
    class myOnItemClickListener implements AdapterView.OnItemClickListener
    {
        //-----------------------------------------------------------
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id)
        {
            //setStatus("onItemClick:"+pos);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();

            if( isUSB == true && pos == 0 )
            {
                intent.putExtra(EXTRA_DEVICE_ADDRESS, "USB");
            }
            else {
                BT_Device = (BluetoothDevice)BT_PairedDevices.toArray()[pos-(isUSB?1:0)];
                intent.putExtra(EXTRA_DEVICE_ADDRESS, BT_Device.getAddress());
            }
            //close();
            //open( BT_Device);



            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();

        }
    }
    //---------------------------------------------------------------
    public void createDeviceList(  )  {

        if (   ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN   ) != PackageManager.PERMISSION_GRANTED)
        {
            String[] permissions = new String[]{android.Manifest.permission.BLUETOOTH_CONNECT,android.Manifest.permission.BLUETOOTH_SCAN};
            ActivityCompat.requestPermissions(this, permissions,0);
            // TODO: overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }

        // todo: handle permissions not granted!

        BT_Adapter       = BluetoothAdapter.getDefaultAdapter();
        BT_PairedDevices = BT_Adapter.getBondedDevices();

        ArrayList<String> list = new ArrayList<String>();

        if( isUSB )
        {
            list.add(new String("USB"));
        }

        for(BluetoothDevice BT_Device : BT_PairedDevices)
        {
            list.add(new String( BT_Device.getName()));
        }

        final ArrayAdapter<String> adapter
                = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                list);

        ListView lv = (ListView)findViewById(R.id.paired_devices);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener( new myOnItemClickListener() );

    }

    BluetoothAdapter     BT_Adapter;
    Set<BluetoothDevice> BT_PairedDevices;
    BluetoothDevice      BT_Device;


    public static String EXTRA_DEVICE_ADDRESS = "device_address";

}
