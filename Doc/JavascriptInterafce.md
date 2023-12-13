### Open Robotic Board

### ORB-Mobile for Android

# <u>Javascript Interface</u>

Author: Thomas Breuer, Hochschule Bonn-Rhein-Sieg, 12.12.2003

# Interface Methods

## JS to App

The App provides an Interface, which can used by the Javascript to send messages to the App. The message must be a JSON-formatted string.
Usage in Javascript:

```
OpenRoberta.jsToAppInterface( msg );
```

## App to JS

The Javascript must provide an interface, which is used by the App to send messages to the Javascript. The message must be a JSON-formatted string:

```
function wvController()
{
    this.appToJsInterface = function( msg )
    {
    // evaluate msg;
    return true;
    }
}

var webviewController = new wvController();
```

# JSON-formatted Messages

## <u>Internal</u>

### Identify

JS to App:

```
{"target":"internal","type":"identify"}
```

App to JS:

```
{"target":"internal","type":"identify","name":"OpenRoberta",  
"app_version":"1.0","device_version":26,"model":"SM-A320FL"}
```

| **Parameter**    | **Purpose** |
| ---------------- | ----------- |
| *app_version*    |             |
| *device_version* |             |
| *model*          |             |

### setRobot

JS to App:

```
{"target":"internal","type":"setRobot","robot":"orb"}
```

App to JS: No response

### startMonitor

JS to App:

```
{"target":"internal","type":"startMonitor"}
```

App to JS: No response

The App starts the Monitor activity to provide a text display and a keyboard for user I/O.

### stopMonitor

JS to App:

```
{"target":"internal","type":"stopMonitor"}
```

App to JS: No response

The App returns to the Main activity.

## <u>ORB Connection</u>

### startScan

JS to App:

```
{"target":"orb","type":"startScan"}
```

App to JS:

Multiple answers, one per detected device.

```
{"target":"orb","type":"scan","state":"appeared",  
"brickid":"00:06:66:69:38:69","brickname":"ORB-2 3869"}
```

| **Parameter** | **Purpose**                                             |
| ------------- | ------------------------------------------------------- |
| *brickid*     | MAC address of Bluetooth module or "USB", if available  |
| *brickname*   | Name of Bluetooth module or "ORB via USB", if available |

### connect

JS to App:

```
{"target":"orb","type":"connect","robot":"00:06:66:69:38:69"}
```

| **Parameter** | **Purpose**                                            |
| ------------- | ------------------------------------------------------ |
| *robot*       | MAC address of Bluetooth module or "USB", if available |

App to JS:

```
{"target":"orb","type":"connect","state":"connected",  
"brickid":"00:06:66:69:38:69","brickname":"ORB-2 3869"}
```

| **Parameter** | **Purpose**                                             |
| ------------- | ------------------------------------------------------- |
| *brickid*     | MAC address of Bluetooth module or "USB", if available  |
| *brickname*   | Name of Bluetooth module or "ORB via USB", if available |

## <u>ORB Data</u>

### configToORB

JS to App:

```
{"target":"orb","type":"configToORB",  
"data":{"Sensor":[{"type":0,"mode":0",option":0},  
                  {"type":0,"mode":0,"option":0},  
                  {"type":0,"mode":0,"option":0},  
                  {"type":0,"mode":0,"option":0}],  
         "Motor":[{"tics":0,"acc":0,"Kp":0,"Ki":0},  
                  {"tics":0,"acc":0,"Kp":0,"Ki":0},  
                  {"tics":0,"acc":0,"Kp":0,"Ki":0},  
                  {"tics":0,"acc":0,"Kp":0,"Ki":0}]}}
```

| **Parameter**   | **Purpose**                                                                                                                              |
| --------------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| *Sensor.type*   | 0: ANALOG<br>Unspecified analog sensor (e.g. NXT-Light) or digital sensor                                                                |
|                 | 1: UART<br> EV3-UART Sensor (e.g. color, ultrasonic, IR)                                                                                 |
|                 | 2: I2C<br> Sensor with I2C interface (e.g. NXT ultrasonic)                                                                               |
|                 | 3: TOF<br/>Time of flight based sensor (e.g. ultrasonic)                                                                                 |
|                 | 4: TOUCH<br> NXT or EV3 touch sensor                                                                                                     |
| *Sensor.mode*   | The purpose of this parameter depends on *Sensor.type*                                                                                   |
|                 | UART<br>EV3 sensor mode, see LEGO-Mindstrom documentation                                                                                |
|                 | I2C<br> 0: NXT ultrasonic                                                                                                                |
|                 | Other: not used                                                                                                                          |
| *Sensor.option* | The purpose of this parameter depends on *Sensor.type*<br>                                                                               |
|                 | ANALOG<br>I/O configuration of pin 1,2,5 and 6. <br>In case of NXT light sensor, use 1792 to switch off or 3840 to switch on the LED<br> |
|                 | Other: not used                                                                                                                          |
| *Motor.tics*    | Number of encoder tics per revolution                                                                                                    |
| *Motor.acc*     | Acceleration of position control                                                                                                         |
| *Motor.Kp*      | Proportional part of PI speed control                                                                                                    |
| *Motor.Ki*      | Integral part of PI speed control                                                                                                        |

The app starts sending propFromORB periodically as long as the Javascript is running.

### propToORB

JS to App:

```
{"target":"orb","type":"propToORB",  
"data":{"Motor":[{"mode":0,"speed":0,"pos":0},  
                 {"mode":0,"speed":0,"pos":0},
                 {"mode":0,"speed":0,"pos":0},  
                 {"mode":0,"speed":0,"pos":0}],  
        "Servo":[{"speed":0,"pos":0},  
                 {"speed":0,"pos":0}]}}
```

| **Parameter** | **Purpose**                                                                                                           |
| ------------- | --------------------------------------------------------------------------------------------------------------------- |
| *Motor.mode*  | 0: POWER<br> The voltage indicated by *speed* is set                                                                  |
|               | 1: BRAKE<br> Braking operation due to motor short circuit                                                             |
|               | 2: SPEED <br>The rotation specified by speed is regulated                                                             |
|               | 3: MOVETO<br>The motor position specified by *pos* will be reached. The rotation specified in *speed* is not exceeded |
| *Motor.speed* | The purpose of this parameter depends on *Motor.mode*                                                                 |
|               | POWER<br>Voltage in range of -1000 bis 1000 (unit: 1/1000 of supply voltage)                                          |
|               | MOVETO<br>Rotation speed (unit 1/1000 rotations per second)                                                           |
|               | Other: not used                                                                                                       |
| *Motor.pos*   | The purpose of this parameter depends on *Motor.mode*                                                                 |
|               | MOVETO<br>Absolut position (unit: 1/1000 rotation)                                                                    |
|               | Other: not used                                                                                                       |
| *Servo.speed* | Servo positioning speed (unit: 1/10 of control range per second)<br> or 0 to switch servo off                         |
| *Servo.pos*   | Servo position (unit: 1/100 of control range)                                                                         |

The app starts sending *propFromORB* periodically as long as the Javascript is running.

### propFromORB

App to JS:

```
{"target":"orb","type":"propFromORB",  
"data":{"Motor":[{"pwr":0,"speed":0,"pos":0},
                 {"pwr":0,"speed":0,"pos":0},
                 {"pwr":0,"speed":0,"pos":0},
                 {"pwr":0,"speed":0,"pos":0}],
        "Sensor":[{"valid":false,"type":0,"option":0,"value":[0,0]},  
                  {"valid":false,"type":0,"option":0,"value":[0,0]},  
                  {"valid":false,"type":0,"option":0,"value":[0,0]},  
                  {"valid":false,"type":0,"option":0,"value":[0,0]}],  
        "Digital":[false,false],
        "Vcc":0,
        "Status":0}}
```

| **Parameter**   | **Purpose**                                                                                                  |
| --------------- | ------------------------------------------------------------------------------------------------------------ |
| *Motor.pwr*     | Output voltage (unit: 1/1000 of supply voltage)                                                              |
| *Motor.speed*   | Measured rotation speed (unit: 1/1000 rotation per sec)                                                      |
| *Motor.pos*     | Measured absolut position (unit: 1/1000 rotation)                                                            |
| *Sensor.valid*  | true, if readings are valid (not supported for all sensor types)                                             |
| *Sensor.type*   | Used sensor type in actual readings                                                                          |
| *Sensor.option* | Used sensor mode (EV3 only)                                                                                  |
| *Sensor.value*  | Sensor reading                                                                                               |
| *Digital*       | Readings of digital input pins D1 and D2                                                                     |
| *Vcc*           | Power supply voltage (unit: V)                                                                               |
| *Status*        | Bit 0: Local application running<br>Bit 1: Acknowledge bit, set once after receiving a *configToORB* message |

## <u>ORB Settings</u>

### settingsToORB

JS to App:

```
{"target":"orb","type":"settingsToORB",  
"data:"{"update":false,"clearMemory":false,  
"Name":"myORB","VCC_ok":7.5,"VCC_low":7.1}};
```

| **Parameter** | **Purpose** |
| ------------- | ----------- |
| *update*      |             |
| *clearMemory* |             |
| *Name*        |             |
| *VCC_ok*      |             |
| *VCC_low*     |             |

If *update* is true, the given setting is stored in ORB flash. In any case, the App answers with a *settingsFromORB* message.

### settingsFromORB

App to JS:

```
{"target":"orb","type":"settingsFromORB",  
"data:"{"Version":[0,0],"Board":[0,0],"Name":"test",  
"VCC_ok":7.5,"VCC_low":7.1}};
```

| **Parameter** | **Purpose** |
| ------------- | ----------- |
| *Version*     |             |
| *Board*       |             |
| *Name*        |             |
| *VCC_ok*      |             |
| *VCC_low*     |             |

## <u>Android-Sensorik</u>

### CommandToAS

JS to App:

```
{"target":"orb","type":"commandToAS","data":{"cmd":"resetSensor"}}
```

### configToAS

JS to App:

```
{"target":"orb","type":"configToAS",  
"data":{"name":"Umgebungslicht","type":5}}
```

| **Parameter** | **Purpose** |
| ------------- | ----------- |
| *name*        |             |
| *type*        |             |

### sensorFromAS

App to JS:

```
{"target":"orb","type":"sensorFromAS",  
"data":{"Umgebungslicht":[123],"Schwerkraft":[1,1,9.81]}}
```

| **Parameter** | **Purpose** |
| ------------- | ----------- |
| name          |             |

## <u>Monitor</u>

### layoutToMon

JS to App:

```
{"target":"orb","type":"layoutToMon",  
"data":{"button":{"A1":"","A2":"","A3":"","A4":""
                  "A5":"","A6":"","A7":"","A8":"",  
                  "B1":"","B2":"","B3":"","B4":"",  
                  "B5":"","B6":"","B7":"","B8":"",  
                  "B9":"","B10":"","B11":"","B12":"",
                  "C1":""}}}
```

| **Parameter** | **Purpose** |
| ------------- | ----------- |
| *button.xx*   |             |

The App sets the button labeling to the specified text. HTML coded unicode is accepted.

### textToMon

JS to App:

```
{"target":"orb","type":"textToMon",  
"data":{"text":["","","",""]}}
```

| **Parameter** | **Purpose**                                   |
| ------------- | --------------------------------------------- |
| *text*        | Array of strings, one string per display line |

The App displays the text line by line.

### **keyFromMon**

App to JS:

```
{"target":"orb","type":"keyFromMon",  
"data":{"key":"A1"}}
```

| **Parameter** | **Purpose**                                                                       |
| ------------- | --------------------------------------------------------------------------------- |
| *key*         | Keycode of the button id's ("A1",..) or an empty string, if no button is pressed. |
