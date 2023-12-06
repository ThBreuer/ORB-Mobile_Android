//*******************************************************************
package orb.robot;

//*******************************************************************
import android.content.Intent;

import org.json.JSONObject;

//*******************************************************************
/**
 * ...
 *
 * @author Beate Jost
 *
 * Changed by Thomas Breuer:
 * - This comment
 * - Package name
 */
public interface RobotCommunicatorInterface {

    void jsToRobot(JSONObject msg);

    void handleActivityResult(int requestCode, int resultCode, Intent intent);

    void reportStateChanged(String type, String state, String brickid, String... strg);

    void close();
}
