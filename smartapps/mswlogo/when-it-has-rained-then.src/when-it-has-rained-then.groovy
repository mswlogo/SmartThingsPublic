/**
 *  Copyright 2017 mswlogo
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
 *  When it has Rained Then
 *
 *  Author: mswlogo
 */
definition(
    name: "When it has Rained Then",
    namespace: "mswlogo",
    author: "George Mills",
    description: "Will the lawn and plants need to be watered today based on yesterday's rain?",
	category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/text.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/text@2x.png"
)

preferences {
	section("Zip code (or pws:STATIONID)...")
    {
		input "zipcode", "text", title: "Zipcode?"
	}
	section("Amount of Rain to close Valve...")
    {
		input "threshold", "decimal", title: "Inches of rain?"
	}
	section("Check Precipitation at...")
    {
		input "checktime", "time", title: "When?"
	}
	section("Set Valve based on Precipitation at...")
    {
		input "settime", "time", title: "When?"
	}
	section("Open Valve (failure state) at...")
    {
		input "opentime", "time", title: "When?", required: false
	}
	section("Valves to adjust...")
    {
		input "valves", "capability.valve", required: false, multiple: false
	}
	section("Switches to adjust...")
    {
		input "switches", "capability.switch", required: false, multiple: false
	}
	section("Text me on status to...")
    {
        input("recipients", "contact", title: "Send notifications to")
        {
            input "phone", "phone", title: "Phone number?"
        }
	}
}

def reschedule()
{
	schedule(settime, "scheduleSet")
	schedule(checktime, "scheduleCheck")
    if (opentime)
    {
		schedule(opentime, "scheduleOpen")
    }
	state.YesterdayRainInches = 0.0
}

def installed()
{
	log.debug "Installed: $settings"
    reschedule()
}

def updated()
{
	log.debug "Updated: $settings"
	unschedule()
    reschedule()
}

def sendMessage(message)
{
	def stamp = new Date().format('hh:mm:ss ', location.timeZone)
  	if (location.contactBookEnabled)
   	{
    	sendNotificationToContacts(stamp + message, recipients)
   	}
   	else
  	{
   		sendSms(phone, stamp + message)
  	}
    log.debug "sms: $stamp + $message"
}

def checkPrecip()
{
	try
    {    	
		def rainInchesToday = getCurrentPrecip()
        def rainInchesTotal = state.YesterdayRainInches + rainInchesToday
        def rainInchesTotalText = "(Yesterday: $state.YesterdayRainInches + Today: $rainInchesToday)"

		if (rainInchesTotal > threshold)
        {
            setClose("Valve: Closed, Rain: $rainInchesTotalText > $threshold")
        }
        else
        {
            setOpen("Valve: Opened, Rain: $rainInchesTotalText < $threshold")
        }
	} 
	catch (e)
	{
        setOpen("Set: Opened, Exception: $e")
	}
}

def setOpen(message)
{
    sendMessage(message)
	if (valves)
    {
		valves.open()
    }
    if (switches)
    {
	    switches.on()
    }
}

def setClose(message)
{
    sendMessage(message)
	if (valves)
    {
    	valves.close()
    }
    if (switches)
    {
	    switches.off()
    }
}

def scheduleOpen()
{
    setOpen("Open: Scheduled Safe State")	
}

def scheduleSet()
{
	try
    {    	
        checkPrecip()
	} 
	catch (e)
	{
        setOpen("Set: Opened, Exception: $e")
	}
}

def scheduleCheck()
{
	def rainInches = getCurrentPrecip()
    sendMessage("Check: Current Rain Set to $rainInches")                
   	state.YesterdayRainInches = rainInches
}

def getCurrentPrecip()
{
	try
    {
        def response = getWeatherFeature("conditions", zipcode)
        if (response)
        {
            def rainInches = response?.current_observation?.precip_today_in
            if (rainInches)
            {
                if (rainInches == "T")
                {
                    rainInches = "0.01"
                }

                if (rainInches.isFloat())
                {
					return rainInches.toFloat()
                }
                else
                {
                    sendMessage("Check: Cannot Parse Precip")
                }
            }
            else
            {
                sendMessage("Check: No Precip Found")
            }
        }
        else
        {
        	sendMessage("Check: No Conditions Found")
        }
	} 
	catch (e)
	{
        sendMessage("Check: Exception $e")
	}
}
