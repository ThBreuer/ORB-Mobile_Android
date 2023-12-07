/*
 This class is a port of 'Util'
 Author of original: Beate Jost, 2018-07-15

 Changes made by Thomas Breuer:
 * - class 'ORLabActivity' renamed to 'MainActivity'
 * - URL has to be loaded, even address is unchanged
 * - Images and text deleted
 * - No READ/WRITE permissions needed
 * - Unused methods deleted
 * - Other minor changes
 */

package orb.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

//*************************************************************************************************
public class Util {

    //---------------------------------------------------------------
    // TB: WiFi connected not checked, app should run without WiFi
    //---------------------------------------------------------------

    //---------------------------------------------------------------
    // TB: Bluetooth handled in other class
    //---------------------------------------------------------------

    //---------------------------------------------------------------
    // TB: Location service not needed
    //---------------------------------------------------------------

    //---------------------------------------------------------------------------------------------
    public static AlertDialog createSettingsDialog(final Activity orLabActivity, final WebView orView) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(orLabActivity);
        final String defaultValue = sharedPreferences.getString("prefUrl", "https://lab.open-roberta.org");

        LayoutInflater prefLI = LayoutInflater.from(orLabActivity);
        View prefView = prefLI.inflate(R.layout.dialog_url, null);
        final EditText userInput = prefView.findViewById(R.id.dialog_url);
        userInput.setText(defaultValue);
        AlertDialog.Builder prefDialogBuilder = new AlertDialog.Builder(orLabActivity);
        prefDialogBuilder.setView(prefView);
        prefDialogBuilder.setMessage(R.string.text_change);
        prefDialogBuilder.setTitle(R.string.pref_title_url);
        prefDialogBuilder.setCancelable(false);
        prefDialogBuilder.setPositiveButton(R.string.btn_OK,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //-------------------------------------------
                        // TB: URL has to be loaded, even address is unchanged
                        //-------------------------------------------
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(orLabActivity);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("prefUrl", userInput.getText().toString());
                        editor.apply();
                        orView.loadUrl(userInput.getText().toString());
                        dialog.dismiss();
                    }
                });
        prefDialogBuilder.setNegativeButton(R.string.btn_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        return prefDialogBuilder.create();
    }

    //---------------------------------------------------------------------------------------------
    public static AlertDialog createAboutDialog(Activity orLabActivity, final WebView orView) {
        LayoutInflater aboutLI = LayoutInflater.from(orLabActivity);
        View aboutView = aboutLI.inflate(R.layout.dialog_about, null);
        AlertDialog.Builder aboutDialogBuilder = new AlertDialog.Builder(orLabActivity);
        aboutDialogBuilder.setTitle(R.string.pref_title_about);
        aboutDialogBuilder.setView(aboutView);
        TextView textAppAbout = aboutView.findViewById(R.id.textAppAbout);
        textAppAbout.setText(R.string.text_about_content);
        ImageView imageAppAbout = aboutView.findViewById(R.id.imageAppAbout);
        imageAppAbout.setImageResource(R.drawable.logo_image);

        //-----------------------------------------------------------
        // TB: Images and text deleted
        //-----------------------------------------------------------

        String versionName = BuildConfig.VERSION_NAME;
        TextView textVersion = aboutView.findViewById(R.id.textVersion);
        textVersion.setText(orLabActivity.getString(R.string.text_about_version, versionName));
        TextView textPublish = aboutView.findViewById(R.id.textPublish);
        textPublish.setMovementMethod(LinkMovementMethod.getInstance());
        textPublish.setText(R.string.text_about_publish);
        aboutDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.btn_OK,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        return aboutDialogBuilder.create();
    }

    //---------------------------------------------------------------
    // TB: No READ/WRITE permissions needed
    //---------------------------------------------------------------

    //---------------------------------------------------------------
    // TB: Unused methods deleted
    //---------------------------------------------------------------

    //---------------------------------------------------------------
    // TB: No READ/WRITE permissions needed
    //---------------------------------------------------------------
}
