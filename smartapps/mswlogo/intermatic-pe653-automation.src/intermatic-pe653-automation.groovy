/**
 *  Copyright 2018 mswlogo
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Master Slave Switch and Scheduler
 *
 *  To Make SmartApp Available:
 *
 *  1) Copy this code to ClipBoard
 *  2) Login into SMartThings IDE
 *  3) Click My SmartApps
 *  4) Click New SmartApp
 *  5) Click From Code
 *  6) Paste ClipBoard (this code)
 *  7) Click Save
 *  8) Click Publish -> For Me
 *
 *  To Install in SmartThings and Configure:
 *
 *  1) On your Phone Start SmartThings App
 *  2) Click Automation at the Bottom
 *  3) Click SMartApps at the Top
 *  4) Click Add a SmartApp
 *  5) Scroll to the bottom and Click My Apps
 *  6) Click Intermatic PE653 Automatic
 *  7) It will Jump right into Configure
 *  8) Click PE653 Switch
 *  9) Find your PE653 Device Handler (don't select one of the PE653 Switches)
 *  10) Set SMS Phone number (optional and won't be used unless enabled per channel below)
 *  11) Choose Channel and Speeds you'd like to Monitor through SMS Messages (all optional)
 *  12) Choose Which PE653 Circuit Button to Sync to VSP Speeds (it basically turns your PE653 into another Remote Control for Variable Speed Pumps)
 *  13) Choose Circuits and Speeds to Schedule Turn On or Off times with optional SMS Message when that event fires (be careful with pumping ACID or Chlorine in Pool or anything super critical).
 *
 *  Author: mswlogo
 *  Date: 2018-Aug-04
 *
 * Change Log:
 * 2018-Aug-07 - Added Scheduling for any Circuit or Speed. Made Radio Button Behavior option because it's still buggy. Allow all 4 Circuits to be Synced (All optional).
 * 2018-Aug-07 - Added SMS Monitoring On Any Circuit or Speed (triggered by any event)
 * 2018-Aug-08 - Added Install Instructions, no functional change
 */
definition(
		name: "Intermatic PE653 Automation",
		namespace: "mswlogo",
		author: "mswlogo",
		description: "Schedule Speeds, Circuits and Synchronize Circuits Switchs with Speeds (One Way).",
		category: "Convenience",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact@2x.png",
		pausable: true
)

preferences {
	section("Choose PE653 Switch...")
    {
		input "multiChannelSwitch", "capability.switch", title: "PE653 Switch", required: true, multiple: false
        input "phone", "phone", title: "SMS Phone number?", required: false        
	}

    section("Synchornize Circuit Switches with Speeds")
    {
		input "syncCircuit1", "bool", title: "Sync Circuit 1 to Speed 1?", required: true
		input "syncCircuit2", "bool", title: "Sync Circuit 2 to Speed 2?", required: true
		input "syncCircuit3", "bool", title: "Sync Circuit 3 to Speed 3?", required: true
		input "syncCircuit4", "bool", title: "Sync Circuit 4 to Speed 4?", required: true

		input "syncRadio", "bool", title: "Enable Radio Buttons?", required: true 
    }
    
    section("SMS message on Change")
    {
		input "smsCircuit1", "bool", title: "SMS Circuit 1 change?", required: true
		input "smsCircuit2", "bool", title: "SMS Circuit 2 change?", required: true
		input "smsCircuit3", "bool", title: "SMS Circuit 3 change?", required: true
		input "smsCircuit4", "bool", title: "SMS Circuit 4 change?", required: true
		input "smsCircuit5", "bool", title: "SMS Circuit 5 change?", required: true
		input "smsSpeed1", "bool", title: "SMS Speed 1 change?", required: true
		input "smsSpeed2", "bool", title: "SMS Speed 2 change?", required: true
		input "smsSpeed3", "bool", title: "SMS Speed 3 change?", required: true
		input "smsSpeed4", "bool", title: "SMS Speed 4 change?", required: true
    }
    
    section("Event Schedule 1")
    {
        input "setChannel1", "enum", title: "Choose Circuit", required: false,
            options:[1:"Use Circuit-1",
                     2:"Use Circuit-2",
                     3:"Use Circuit-3",
                     4:"Use Circuit-4",
                     5:"Use Circuit-5"]

        input "setSpeed1", "enum", title: "Choose Pump Speed", required: false,
            options:[1:"Use Speed-1",
                     2:"Use Speed-2",
                     3:"Use Speed-3",
                     4:"Use Speed-4"]

		input "setOnTime1", "time", title: "When to turn On?", required: false
		input "setOffTime1", "time", title: "When to turn Off?", required: false
        input "setPhone1", "bool", title: "Send SMS?", required: true 
	}
    
    section("Event Schedule 2")
    {
        input "setChannel2", "enum", title: "Choose Circuit", required: false,
            options:[1:"Use Circuit-1",
                     2:"Use Circuit-2",
                     3:"Use Circuit-3",
                     4:"Use Circuit-4",
                     5:"Use Circuit-5"]

        input "setSpeed2", "enum", title: "Choose Pump Speed", required: false,
            options:[1:"Use Speed-1",
                     2:"Use Speed-2",
                     3:"Use Speed-3",
                     4:"Use Speed-4"]

		input "setOnTime2", "time", title: "When to turn On?", required: false
		input "setOffTime2", "time", title: "When to turn Off?", required: false
        input "setPhone2", "bool", title: "Send SMS?", required: true 
	}

    section("Event Schedule 3")
    {
        input "setChannel3", "enum", title: "Choose Circuit", required: false,
            options:[1:"Use Circuit-1",
                     2:"Use Circuit-2",
                     3:"Use Circuit-3",
                     4:"Use Circuit-4",
                     5:"Use Circuit-5"]

        input "setSpeed3", "enum", title: "Choose Pump Speed", required: false,
            options:[1:"Use Speed-1",
                     2:"Use Speed-2",
                     3:"Use Speed-3",
                     4:"Use Speed-4"]

		input "setOnTime3", "time", title: "When to turn On?", required: false
		input "setOffTime3", "time", title: "When to turn Off?", required: false
        input "setPhone3", "bool", title: "Send SMS?", required: true 
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    unschedule()
	subscribeToEvents()
}

def uninstalled()
{
	log.debug "Uninstalled: $settings"
    
    unschedule()
	unsubscribe()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

    unschedule()
    reschedule()

	unsubscribe()
	subscribeToEvents()
}

def initialize() {
    // initialize counter
    sendSms(phone, "Started")
    }

def reschedule()
{
    if (setOnTime1)
    {
		schedule(setOnTime1, "scheduleOn1")
    }
    
    if (setOffTime1)
    {
		schedule(setOffTime1, "scheduleOff1")
    }

    if (setOnTime2)
    {
		schedule(setOnTime2, "scheduleOn2")
    }
    
    if (setOffTime1)
    {
		schedule(setOffTime2, "scheduleOff2")
    }

    if (setOnTime3)
    {
		schedule(setOnTime3, "scheduleOn3")
    }
    
    if (setOffTime3)
    {
		schedule(setOffTime3, "scheduleOff3")
    }
}    

def subscribeToOnEvents()
{
    if (syncCircuit1 || smsCircuit1)
    {
		subscribe(multiChannelSwitch, "switch1.on", eventHandlerOn1)
    }

	if (syncCircuit2 || smsCircuit2)
    {
		subscribe(multiChannelSwitch, "switch2.on", eventHandlerOn2)
    }

	if (syncCircuit3 || smsCircuit3)
    {
		subscribe(multiChannelSwitch, "switch3.on", eventHandlerOn3)
    }

	if (syncCircuit4 || smsCircuit4)
    {
		subscribe(multiChannelSwitch, "switch4.on", eventHandlerOn4)
    }

	if (smsCircuit5)
    {
		subscribe(multiChannelSwitch, "switch5.on", eventHandlerOn5)
    }

	if (smsSpeed1)
    {
		subscribe(multiChannelSwitch, "swVSP1.on", eventHandlerSpeedOn1)
    }
    
	if (smsSpeed2)
    {
		subscribe(multiChannelSwitch, "swVSP2.on", eventHandlerSpeedOn2)
    }

	if (smsSpeed3)
    {
		subscribe(multiChannelSwitch, "swVSP3.on", eventHandlerSpeedOn3)
    }

	if (smsSpeed4)
    {
		subscribe(multiChannelSwitch, "swVSP4.on", eventHandlerSpeedOn4)
    }
}

def subscribeToSyncOffEvents()
{
	if (syncCircuit1 || smsCircuit1)
    {
		subscribe(multiChannelSwitch, "switch1.off", eventHandlerOff1)
    }

	if (syncCircuit2 || smsCircuit2)
    {
		subscribe(multiChannelSwitch, "switch2.off", eventHandlerOff2)
    }

	if (syncCircuit3 || smsCircuit3)
    {
		subscribe(multiChannelSwitch, "switch3.off", eventHandlerOff3)
    }

	if (syncCircuit4 || smsCircuit4)
    {
		subscribe(multiChannelSwitch, "switch4.off", eventHandlerOff4)
    }

	if (smsCircuit5)
    {
		subscribe(multiChannelSwitch, "switch5.off", eventHandlerOff5)
    }
}

def subscribeToOffEvents()
{
	if (smsSpeed1)
    {
		subscribe(multiChannelSwitch, "swVSP1.off", eventHandlerSpeedOff1)
    }
    
	if (smsSpeed2)
    {
		subscribe(multiChannelSwitch, "swVSP2.off", eventHandlerSpeedOff2)
    }

	if (smsSpeed3)
    {
		subscribe(multiChannelSwitch, "swVSP3.off", eventHandlerSpeedOff3)
    }

	if (smsSpeed4)
    {
		subscribe(multiChannelSwitch, "swVSP4.off", eventHandlerSpeedOff4)
    }
}

def subscribeToEvents()
{
	subscribeToOnEvents()
	subscribeToSyncOffEvents()
	subscribeToOffEvents()
}

def subscribeToNoSyncEvents()
{
	subscribeToOnEvents()
	subscribeToNullEvents()
	subscribeToOffEvents()
}

def subscribeToNullEvents()
{
	if (syncCircuit1 || smsCircuit1)
    {
		subscribe(multiChannelSwitch, "switch1.off", eventHandlerNullOff1)
    }

	if (syncCircuit2 || smsCircuit2)
    {
		subscribe(multiChannelSwitch, "switch2.off", eventHandlerNullOff2)
    }

	if (syncCircuit3 || smsCircuit3)
    {
		subscribe(multiChannelSwitch, "switch3.off", eventHandlerNullOff3)
    }

	if (syncCircuit4 || smsCircuit4)
    {
		subscribe(multiChannelSwitch, "switch4.off", eventHandlerNullOff4)
    }

	if (smsCircuit5)
    {
		subscribe(multiChannelSwitch, "switch5.off", eventHandlerNullOff5)
    }
}

def sendMessage(boolean smsOn, message)
{
    if (smsOn)
    {
        def stamp = new Date().format('hh:mm:ss ', location.timeZone)
        if (location.contactBookEnabled)
        {
            sendNotificationToContacts(stamp + "$app.label " + message, recipients)
        }
        else
        {
            if (phone) {
                sendSms(phone, stamp + "$app.label " + message)
            }
        }
    }
    log.debug "sms: $stamp $app.label $message"
}

def setCircuitState(int setChannel, boolean setPhone, boolean stateOn)
{
    if (setChannel)
    {
 		switch (setChannel) {
        case 1:
            if (stateOn) {
        		multiChannelSwitch.on1()
            } else {
        		multiChannelSwitch.off1()            
            }
			break;
        case 2:
            if (stateOn) {
        		multiChannelSwitch.on2()
            } else {
        		multiChannelSwitch.off2()            
            }
			break;
        case 3:
            if (stateOn) {
        		multiChannelSwitch.on3()
            } else {
        		multiChannelSwitch.off3()            
            }
			break;
        case 4:
            if (stateOn) {
        		multiChannelSwitch.on4()
            } else {
        		multiChannelSwitch.off4()            
            }
			break;
        case 4:
            if (stateOn) {
	        	multiChannelSwitch.on5()
            } else {
        		multiChannelSwitch.off5()            
            }
			break;
		}
        
    	sendMessage(setPhone, "Circuit $setChannel $stateOn")
    }
}

def setSpeedState(int setSpeed, boolean setPhone, boolean stateOn)
{
    if (setSpeed)
    {
    	if (stateOn) {        
            switch (setSpeed) {
            case 1:
                multiChannelSwitch.setVSPSpeed1()
                break;
            case 2:
                multiChannelSwitch.setVSPSpeed2()
                break;
            case 3:
                multiChannelSwitch.setVSPSpeed3()
                break;
            case 4:
                multiChannelSwitch.setVSPSpeed4()
                break;
            }
        } else {
        	multiChannelSwitch.setVSPSpeed0()
        }
                
    	sendMessage(setPhone, "Speed $setSpeed $stateOn")
    }
}

def scheduleOn1()
{
    setCircuitState(setChannel1.toInteger(), setPhone1, true)
    setSpeedState(setSpeed1.toInteger(), setPhone1, true)    
}

def scheduleOff1()
{
    setCircuitState(setChannel1.toInteger(), setPhone1, false)
    setSpeedState(setSpeed1.toInteger(), setPhone1, false)
}

def scheduleOn2()
{
    setCircuitState(setChannel2.toInteger(), setPhone2, true)
    setSpeedState(setSpeed2.toInteger(), setPhone2, true)    
}

def scheduleOff2()
{
    setCircuitState(setChannel2.toInteger(), setPhone2, false)
    setSpeedState(setSpeed2.toInteger(), setPhone2, false)
}

def scheduleOn3()
{
    setCircuitState(setChannel3.toInteger(), setPhone3, true)
    setSpeedState(setSpeed3.toInteger(), setPhone3, true)    
}

def scheduleOff3()
{
    setCircuitState(setChannel3.toInteger(), setPhone3, false)
    setSpeedState(setSpeed3.toInteger(), setPhone3, false)
}

def resetCircuitSwitches(int excludeSwitch)
{
    def boolean needToTurnOffSwitch = false
        
    if (!syncRadio)
    {
    	return
    }

	if (syncCircuit1 && (excludeSwitch != 1) && (multiChannelSwitch.currentValue("switch1") == "on"))
    {
    	needToTurnOffSwitch = true
    }
	if (syncCircuit2 && (excludeSwitch != 2) && (multiChannelSwitch.currentValue("switch2") == "on"))
    {
    	needToTurnOffSwitch = true
    }
	if (syncCircuit3 && (excludeSwitch != 3) && (multiChannelSwitch.currentValue("switch3") == "on"))
    {
    	needToTurnOffSwitch = true
    }
	if (syncCircuit4 && (excludeSwitch != 4) && (multiChannelSwitch.currentValue("switch4") == "on"))
    {
    	needToTurnOffSwitch = true
    }

    if (needToTurnOffSwitch)
    {
        // Resubscribe to Handler that will NOT set a Speed
        unsubscribe()
		subscribeToNoSyncEvents()

        // Turn Off all other Active Circuits swithes Except the Excluded one (the one that should remain On)

        if (syncCircuit1 && (excludeSwitch != 1) && (multiChannelSwitch.currentValue("switch1") == "on"))
        {
            multiChannelSwitch.off1()
            return
        }
        if (syncCircuit2 && (excludeSwitch != 2) && (multiChannelSwitch.currentValue("switch2") == "on"))
        {
            multiChannelSwitch.off2()
            return
        }
        if (syncCircuit3 && (excludeSwitch != 3) && (multiChannelSwitch.currentValue("switch3") == "on"))
        {
            multiChannelSwitch.off3()
            return
        }
        if (syncCircuit4 && (excludeSwitch != 4) && (multiChannelSwitch.currentValue("switch4") == "on"))
        {
            multiChannelSwitch.off4()
            return
        }
	}
}

def eventHandlerOn1(evt) 
{
    if (syncCircuit1)
    {
    	multiChannelSwitch.setVSPSpeed1()
    	resetCircuitSwitches(1)
	    sendMessage(smsCircuit1, "Circuit 1 On - Speed 1")
    }
    else
    {
	    sendMessage(smsCircuit1, "Circuit 1 On - No Sync")
    }
}

def eventHandlerOn2(evt)
{
    if (syncCircuit2)
    {
    	multiChannelSwitch.setVSPSpeed2()
    	resetCircuitSwitches(2)
	    sendMessage(smsCircuit1, "Circuit 2 On - Speed 2")
    }
    else
    {
	    sendMessage(smsCircuit1, "Circuit 2 On - No Sync")
    }
}

def eventHandlerOn3(evt)
{
    if (syncCircuit3)
    {
	    multiChannelSwitch.setVSPSpeed3()
    	resetCircuitSwitches(3)
	    sendMessage(smsCircuit1, "Circuit 3 On - Speed 3")
    }
    else
    {
	    sendMessage(smsCircuit1, "Circuit 3 On - No Sync")
    }
}

def eventHandlerOn4(evt)
{
    if (syncCircuit4)
    {
    	multiChannelSwitch.setVSPSpeed4()
    	resetCircuitSwitches(4)
	    sendMessage(smsCircuit1, "Circuit 4 On - Speed 4")
    }
    else
    {
	    sendMessage(smsCircuit1, "Circuit 4 On - No Sync")
    }
}

def eventHandlerOn5(evt)
{
    sendMessage(smsCircuit5, "Circuit 5 On")
}

// All Speed Off Handlers are the same (SmartThings not Happy if I mapped Multipl Switches to the Same Handler

def eventHandlerOff1(evt) 
{
    if (syncCircuit1)
    {
    	multiChannelSwitch.setVSPSpeed0()
    	sendMessage(smsCircuit1, "Circuit 1 Off - Speed 0")
    }
    else
    {
    	sendMessage(smsCircuit1, "Circuit 1 Off - No Sync")
    }
}

def eventHandlerOff2(evt) 
{
    if (syncCircuit1)
    {
    	multiChannelSwitch.setVSPSpeed0()
    	sendMessage(smsCircuit2, "Circuit 2 Off - Speed 0")
    }
    else
    {
    	sendMessage(smsCircuit1, "Circuit 2 Off - No Sync")
    }
}

def eventHandlerOff3(evt) 
{
    if (syncCircuit3)
    {
    	multiChannelSwitch.setVSPSpeed0()
    	sendMessage(smsCircuit3, "Circuit 3 Off - Speed 0")
    }
    else
    {
    	sendMessage(smsCircuit1, "Circuit 3 Off - No Sync")
    }
}

def eventHandlerOff4(evt) 
{
    if (syncCircuit4)
    {
    	multiChannelSwitch.setVSPSpeed0()
    	sendMessage(smsCircuit4, "Circuit 4 Off - Speed 0")
    }
    else
    {
    	sendMessage(smsCircuit1, "Circuit 4 Off - No Sync")
    }
}

def eventHandlerOff5(evt) 
{
    sendMessage(smsCircuit5, "Circuit 5 Off - Speed NA")
}

// Dummy Handlers that don't take action and put Event Handlers back to Normal

def eventHandlerNullOff1(evt) 
{
	unsubscribe()
	subscribeToEvents()

	sendMessage(smsCircuit1, "Circuit 1 Off - Radio Sync")
}

def eventHandlerNullOff2(evt) 
{
	unsubscribe()
	subscribeToEvents()

	sendMessage(smsCircuit2, "Circuit 2 Off - Radio Sync")
}

def eventHandlerNullOff3(evt) 
{
	unsubscribe()
	subscribeToEvents()

    sendMessage(smsCircuit3, "Circuit 3 Off - Radio Sync")
}

def eventHandlerNullOff4(evt) 
{
	unsubscribe()
	subscribeToEvents()
    
    sendMessage(smsCircuit4, "Circuit 4 Off - Radio Sync")
}

def eventHandlerNullOff5(evt) 
{
    sendMessage(smsCircuit5, "Circuit 5 Off - Radio NA")
}

def eventHandlerSpeedOn1(evt) 
{
    sendMessage(smsSpeed1, "Speed 1 On")
}

def eventHandlerSpeedOn2(evt) 
{
    sendMessage(smsSpeed2, "Speed 2 On")
}

def eventHandlerSpeedOn3(evt) 
{
    sendMessage(smsSpeed3, "Speed 3 On")
}

def eventHandlerSpeedOn4(evt) 
{
    sendMessage(smsSpeed4, "Speed 4 On")
}

def eventHandlerSpeedOff1(evt) 
{
    sendMessage(smsSpeed1, "Speed 1 Off")
}

def eventHandlerSpeedOff2(evt) 
{
    sendMessage(smsSpeed2, "Speed 2 Off")
}

def eventHandlerSpeedOff3(evt) 
{
    sendMessage(smsSpeed3, "Speed 3 Off")
}

def eventHandlerSpeedOff4(evt) 
{
    sendMessage(smsSpeed4, "Speed 4 Off")
}

// End of File