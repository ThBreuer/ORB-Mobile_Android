/*
 This class is a port of 'ORLabActivity'
 Author of original: Beate Jost, 2018-07-15

 Changes made by Thomas Breuer:
 * - class renamed
 * - Comments revised
 * - Additional menu item: Connect to ORB
 * - Additional menu item: Configure the ORB
 * - Additional menu item: Load service page from assets
 * - WiFi connected not checked, app should run without WiFi
 * - ORB is the only robot system that is supported
 * - Home button should not finish the app
 * - Method 'getOrView()' deleted
 * - Execute local links in WebView too
 * - No READ/WRITE permissions needed
 * - Other minor changes
 */

package orb.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import static java.lang.String.format;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Locale;

import orb.robot.RobotCommunicator;
import orb.robot.orb.ORB_Communicator;
import orb.device.ORB.ORB_Manager;

/**
 * <h1>ORB-Mobile</h1>
 * Main activity for the ORB-Mobile app.
 * <p>
 * The activity holds a connection to a server via a web view. With this connection
 * it is possible to execute javascript controlling an ORB based robot systems.
 *
 * @author Thomas Breuer
 * @since 2023-12-06
 */

//*************************************************************************************************
public class MainActivity extends Activity
{
    private static final int REQUEST_FILENAME         = 1;
    private static final int REQUEST_BLUETOOTH_DEVICE = 3;
    private static final int REQUEST_ORB_CONFIG       = 4;

    private RobotCommunicator    robotCommunicator;
    private ORB_Manager          orbManager;
    private WebView              orView;
    private ValueCallback<Uri[]> orFilePathCallback;

    private static final String TAG = MainActivity.class.getSimpleName();
    private ProgressBar progressBar;

    @SuppressLint({"AddJavascriptInterface", "JavascriptInterface"})

    //---------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // layout is your layout.xml

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String orUrl = sharedPreferences.getString("prefUrl", "");

        //-----------------------------------------------------------
        // TB: WiFi connected not checked, app should run without WiFi
        //-----------------------------------------------------------

        this.orView = findViewById(R.id.orView);
        this.progressBar = findViewById(R.id.progressBar);
        //-----------------------------------------------------------
        // TB: ORB is the only robot system that is supported
        ORB_Communicator com = new ORB_Communicator( this, this.orView );

        this.robotCommunicator = com;
        this.orbManager = com.orbManager;
        //-----------------------------------------------------------

        this.orView.getSettings().setJavaScriptEnabled(true);
        this.orView.getSettings().setDomStorageEnabled(true);
        this.orView.getSettings().setLoadWithOverviewMode(true);
        this.orView.getSettings().setUseWideViewPort(true);
        this.orView.requestFocus(View.FOCUS_DOWN);
        this.orView.addJavascriptInterface(this, "OpenRoberta");
        this.orView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition,
                                        String mimeType,
                                        long contentLength) {
                //---------------------------------------------------
                // TB: No READ/WRITE permissions needed
                //---------------------------------------------------

                DownloadManager downloadManager = (DownloadManager) MainActivity.this.getSystemService(DOWNLOAD_SERVICE);
                File file;
                try {
                    String content = URLDecoder.decode(url, "UTF-8");
                    int start = content.indexOf('/') + 1;
                    int end = content.indexOf(';');
                    String fileType = content.substring(start, end);
                    String fileName = "NepoProg." + fileType;
                    file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
                    PrintWriter printWriter;
                    printWriter = new PrintWriter(file);
                    printWriter.println(content.substring(content.indexOf(",") + 1));
                    printWriter.close();
                    Log.d(TAG, file.toString());
                    downloadManager.addCompletedDownload(file.getName(), file.getName(), true, "text/xml", file.getAbsolutePath(), file.length(), true);
                } catch (java.io.IOException e) {
                    Log.e(TAG, "download failed: " + e.getMessage());
                }
            }
        });

        this.orView.loadUrl(orUrl);
        this.orView.setWebChromeClient(new orWebViewClient());
        this.orView.setWebViewClient(new myWebViewClient());
        WebView.setWebContentsDebuggingEnabled(true);
    }

    //---------------------------------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        //------------------------------------------------------------
        // TB: Close communication added
        robotCommunicator.close();
        //------------------------------------------------------------
        super.onDestroy();
    }

    //---------------------------------------------------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();

       /// orbManager.connect("USB");

    }

    //---------------------------------------------------------------------------------------------
    @Override
    protected void onPause() {
        super.onPause();
    }

    //---------------------------------------------------------------
    // TB: Home button should not finish the app
    //---------------------------------------------------------------------------------------------
    @Override
    public void onBackPressed()
    {
    }
    //---------------------------------------------------------------

    //---------------------------------------------------------------------------------------------
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case REQUEST_FILENAME:
                Uri[] result;
                if (orFilePathCallback == null) {
                    super.onActivityResult(requestCode, resultCode, intent);
                    return;
                }
                if (resultCode == Activity.RESULT_OK) {
                    if (intent != null) {
                        String file = intent.getDataString();
                        if (file != null) {
                            result = new Uri[]{Uri.parse(file)};
                            orFilePathCallback.onReceiveValue(result);
                        }
                    }
                }
                orFilePathCallback = null;
                break;
 
            //---------------------------------------------------------------
            // TB: Additional menu item: Connect to ORB
            case REQUEST_BLUETOOTH_DEVICE:
                if (resultCode == Activity.RESULT_OK && orbManager != null )
                {
                    String addr = intent.getExtras().getString(BT_DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    orbManager.connect( addr );
                }
                break;
            //---------------------------------------------------------------

            //---------------------------------------------------------------
            // TB: Additional menu item: Configure the ORB
            case REQUEST_ORB_CONFIG:
                if (resultCode == Activity.RESULT_OK && orbManager != null)
                {
                    String name = intent.getExtras().getString(ConfigORB_Activity.EXTRA_NAME);
                    double VccOk = intent.getExtras().getDouble(ConfigORB_Activity.EXTRA_VCC_OK);
                    double VccLow = intent.getExtras().getDouble(ConfigORB_Activity.EXTRA_VCC_LOW);
                    orbManager.sendData( name, VccOk, VccLow );
                }
                break;
            //---------------------------------------------------------------

            default:
                robotCommunicator.handleActivityResult(requestCode, resultCode, intent);
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    //---------------------------------------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            //-------------------------------------------------------
            // TB: No READ/WRITE permissions needed
            //-------------------------------------------------------

            default:
                robotCommunicator.handleRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    //---------------------------------------------------------------------------------------------
    @JavascriptInterface
    public void jsToAppInterface(String msg) {
        try {
            JSONObject newMsg = new JSONObject(msg);
            String target = newMsg.getString("target");
            String type = newMsg.getString("type");
            if (target == null || type == null) {
                throw new IllegalArgumentException("Min. 2 parameters required !");
            }
            if (target.equals("internal")) {
                if (type.equals("identify")) {
                    this.identify(newMsg);
                } else if (type.equals("setRobot")) {
                    //-----------------------------------------------
                    // TB: ORB is the only robot system that is
                    //     supported. Nothing to do here
                    //-----------------------------------------------
                }
            } else if (target.equals(this.robotCommunicator.ROBOT))
                this.robotCommunicator.jsToRobot(newMsg);
        } catch (final JSONException e) {
            // ignore invalid messages
            Log.e(TAG, "Json parsing error: " + e.getMessage() + " processing: " + msg);
        }
    }

    //---------------------------------------------------------------------------------------------
    public void identify(JSONObject msg) {
        try {
            msg.put("name", "OpenRoberta");
            msg.put("app_version", BuildConfig.VERSION_NAME);
            msg.put("device_version", Build.VERSION.SDK_INT);
            msg.put("model", Build.MODEL);
        } catch (JSONException e) {
            Log.e(TAG, "Json parsing error: " + e.getMessage());
            // nothing to do, msg/answer is save but empty
        }
        final JSONObject answer = msg;
        this.orView.post(new Runnable() {
            @Override
            public void run() {
                orView.loadUrl("javascript:webviewController.appToJsInterface('" + answer.toString() + "')");
            }
        });
    }

    //---------------------------------------------------------------
    // TB: ORB is the only one supported robot system.
    //     Method 'setRobot()' obsolet
    //---------------------------------------------------------------

    //---------------------------------------------------------------------------------------------
    public void openSettings(View v) {
        PopupMenu popup = new PopupMenu(getBaseContext(), v);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

        //-----------------------------------------------------------------------------------------
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.settings:
                        AlertDialog prefDialog = Util.createSettingsDialog(MainActivity.this, orView);
                        prefDialog.show();
                        break;
                    case R.id.about:
                        AlertDialog aboutDialog = Util.createAboutDialog(MainActivity.this, orView);
                        aboutDialog.show();
                        break;
                    case R.id.exit:
                        MainActivity.this.finish();
                        System.exit(0);

                    //-----------------------------------------------
                    // TB: Additional menu item: Connect to ORB
                    case R.id.action_connect:
                        BT_StartDeviceList(orbManager.isUSBavailable());
                        break;
                    //-----------------------------------------------

                    //-----------------------------------------------
                    // TB: Additional menu item: Load service page from assets
                    case R.id.service:
                        orView.loadUrl("file:///android_asset/HTML/Service/index.html");
                        break;
                    //-----------------------------------------------

                    //-----------------------------------------------
                    // TB: Additional menu item: Configure the ORB
                    case R.id.configORB:
                        configORB();
                        break;
                    //-----------------------------------------------
                }
                return true;
            }
        });
        popup.show();
    }

    //---------------------------------------------------------------
    // TB: Method 'getOrView()' deleted
    //---------------------------------------------------------------

    //*********************************************************************************************
    private class orWebViewClient extends WebChromeClient
    {
        //-----------------------------------------------------------------------------------------
        // TODO check if we need this
        public boolean publicbooleanonJsAlert(WebView view, String url, String message,
                                              final JsResult result) {
            return true;
        }

        //-----------------------------------------------------------------------------------------
        // TODO check if we need this
        public boolean publicbooleanonJsConfirm(WebView view, String url, String message, final JsResult result) {
            return true;
        }

        /**
         * This method is called in blockly "window.prompt(Blockly.Msg.CHANGE_VALUE_TITLE, this.text_)" to allow Android/IOS users to make inputs.
         */
        //-----------------------------------------------------------------------------------------
        public boolean publicbooleanonJsPrompt(WebView view, String url, String message,
                                               String defaultValue, final JsPromptResult result) {
            return true;
        }

        /**
         * Overwrite this method to enable the import functions from the Open Roberta Lab.
         */
        //-----------------------------------------------------------------------------------------
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                         FileChooserParams fileChooserParams) {
            if (orFilePathCallback != null) {
                orFilePathCallback.onReceiveValue(null);
            }
            orFilePathCallback = filePathCallback;

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            // TODO check if we want to allow source code uploading as well. Then we have to determine the file type.
            contentSelectionIntent.setTypeAndNormalize("text/xml");

            startActivityForResult(contentSelectionIntent, REQUEST_FILENAME);
            return true;
        }

        //-----------------------------------------------------------------------------------------
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {
            return true;
        }

        //-----------------------------------------------------------------------------------------
        @Override
        public void onProgressChanged(WebView view, int progress) {
            MainActivity.this.progressBar.setProgress(progress);
            progressBar.setVisibility( ( progress >= 100 ) ? View.GONE : View.VISIBLE );
        }
    } // end of class orWebViewClient

    //*********************************************************************************************
    public class myWebViewClient extends WebViewClient {
        /**
         * Overwrite this method to open the system browser for external links, e.g. data protection declaration
         */
        //------------------------------------------------------------------------------------------
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            //-------------------------------------------------------
            // TB: Execute local links in WebView too
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N )
            {
                if( !request.isRedirect() )
                {
                    return false;
                }
            }
            //-------------------------------------------------------

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString()));
            startActivity(browserIntent);
            return true;
        }
    }


    //---------------------------------------------------------------
    // TB: Additional menu item: Connect to ORB
    //---------------------------------------------------------------------------------------------
    void BT_StartDeviceList( boolean isUSB )
    {
        Intent serverIntent = new Intent(this, BT_DeviceListActivity.class);
        serverIntent.putExtra( "USB", isUSB );
        startActivityForResult(serverIntent, REQUEST_BLUETOOTH_DEVICE);
    }
    //---------------------------------------------------------------

    //---------------------------------------------------------------
    // TB: Additional menu item: Configure the ORB
    //---------------------------------------------------------------------------------------------
    void configORB()
    {
        if( orbManager != null )
        {
            Intent intent = new Intent( this, ConfigORB_Activity.class );
            intent.putExtra( ConfigORB_Activity.EXTRA_HW_VERSION,
                    orbManager.getSettings().getHW_Version() );
            intent.putExtra( ConfigORB_Activity.EXTRA_SW_VERSION,
                    orbManager.getSettings().getSW_Version() );
            intent.putExtra( ConfigORB_Activity.EXTRA_NAME,
                    orbManager.getSettings().getName() );
            intent.putExtra( ConfigORB_Activity.EXTRA_VCC_OK,
                    orbManager.getSettings().getVccOk() );
            intent.putExtra( ConfigORB_Activity.EXTRA_VCC_LOW,
                    orbManager.getSettings().getVccLow() );
            startActivityForResult( intent, REQUEST_ORB_CONFIG );
        }
    }
    //---------------------------------------------------------------

    //---------------------------------------------------------------
    // TB: Show ORB state (connection, battery)
    //---------------------------------------------------------------------------------------------
    public void setTextStatus(final ORB_Manager orb)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                TextView viewStatus = findViewById( R.id.status);
                TextView viewVoltage = findViewById( R.id.voltage);

                if( orb.isConnected() )
                {
                    viewStatus.setText( orb.getSettings().getName() );
                    viewVoltage.setText( format( Locale.getDefault(), "%s: %.1f V", getString(R.string.text_state_batterie), orb.getVcc() ) );
                }
                else
                {
                    viewStatus.setText( R.string.text_state_orb_not_connected);
                    viewVoltage.setText( format("%s: --- V", getString(R.string.text_state_batterie) ) );
                }
            }
        });
    }
    //---------------------------------------------------------------
}
