//*******************************************************************
package orb.robot;

//*******************************************************************
import android.content.Intent;
import android.util.Log;
import org.json.JSONObject;

//*******************************************************************
/**
 * ...
 *
 * @author Beate Jost
 *
 * Changed by Thomas Breuer:
 * - This comment
 * - Location of RobotCommunicatorInterface.java moved to upper directory
 */
public class RobotCommunicator implements RobotCommunicatorInterface {
    public String ROBOT = "OpenRoberta";

    public String TAG = RobotCommunicator.class.getSimpleName();

    public RobotCommunicator() {
        Log.d(TAG, "initialized");
    }

    @Override
    public void jsToRobot(JSONObject msg) {
        Log.d(TAG, "jsToRobot: " + msg.toString());
    }

    @Override
    public void handleActivityResult(int requestCode, int resultCode, Intent intent) {
        //
    }

    public void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

    }

    @Override
    public void reportStateChanged(String type, String state, String brickid, String... strg) {

    }


    @Override
    public void close() {
        Log.d(TAG, "close");
    }
}
