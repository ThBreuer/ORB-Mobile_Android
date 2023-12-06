package orb.device.ORB;

//*******************************************************************
/**
 * ...
 * @author Thomas Breuer
 */
public interface ORB_Report
{
    void reportDisconnect();

    void reportConnect( String brickId, String brickName );

    void reportScan( String brickId, String brickName ) ;

    void reportORB();
}
