package orb.device.Monitor;

import static java.lang.String.format;

import android.widget.TextView;

import org.json.JSONObject;

import java.util.Locale;

import orb.device.ORB.ORB_Manager;
import orb.main.R;
import orb.robot.orb.ORB_Communicator;

public class Monitor_Manager
{
    ORB_Communicator orb_report;

    //--------------------------------------------------------------------------------------------
    public Monitor_Manager(ORB_Communicator orb_report)
    {
        this.orb_report = orb_report;
    }

    //--------------------------------------------------------------------------------------------
    public void close()
    {
    }

    //--------------------------------------------------------------------------------------------
    public void update()
    {
        orb_report.reportMonitor();
    }

    //---------------------------------------------------------------------------------------------
    public void setTextStatus(final ORB_Manager orb)
    {
        Monitor_Activity.DataHolder.setTextStatus( orb_report.orbManager );
    }

    //--------------------------------------------------------------------------------------------
    public void setText( String str )
    {
        Monitor_Activity.DataHolder.setData( str );
    }

    //--------------------------------------------------------------------------------------------
    public void setLayout(JSONObject layout)
    {
        Monitor_Activity.DataHolder.setLayout( layout );
    }

    //--------------------------------------------------------------------------------------------
    public String getKey()
    {
        return( Monitor_Activity.DataHolder.getKey() );
    }
}
