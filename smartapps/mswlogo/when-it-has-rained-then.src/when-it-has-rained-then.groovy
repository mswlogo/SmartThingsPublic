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
    name: "When it has Rained Then", // "When It's Going to Rain", //
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
	section("Amount of Current Rain to close Valve...")
    {
		input "thresholdCurrent", "decimal", title: "Inches of rain?", required: false
	}
	section("Amount of Forecasted Rain to close Valve...")
    {
		input "thresholdForecast", "decimal", title: "Inches of rain?", required: false
	}
	section("Number of days to keep closed (optional)...")
    {
		input "daysClosed", "number", title: "Number of days?", required: false
	}
	section("Check Current Precipitation at (normally 11:50PM)...")
    {
		input "checktime", "time", title: "When?", required: false
	}
	section("Set Valve (1st Watering Required) based on Precipitation at (e.g. 7:50AM just before timers open)...")
    {
		input "settime", "time", title: "When?", required: true
	}
	section("Set Valve (2nd Watering Optional) based on Precipitation at (e.g. 3:50PM just before timers open)...")
    {
		input "set2ndtime", "time", title: "When?", required: false
	}
	section("Open Valve (optional) at...")
    {
		input "opentime", "time", title: "When?", required: false
	}
	section("Close Valve (optional) at...")
    {
		input "closetime", "time", title: "When?", required: false
	}
	section("Valves to adjust...")
    {
		input "valves", "capability.valve", required: true, multiple: true
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
    if (checktime)
    {
		schedule(checktime, "scheduleCheck")
    }
    if (opentime)
    {
		schedule(opentime, "scheduleOpen")
    }
    if (closetime)
    {
		schedule(closetime, "scheduleClose")
    }
    if (set2ndtime)
    {
    	schedule(set2ndtime, "schedule2ndSet")	
    }
    
	if (thresholdCurrent && !checktime)
    {
    	sendMessage("Current threshold set with no time to check it")
    }
    if (!thresholdCurrent && checktime)
    {
    	sendMessage("Check time set with no threshold set")
    }
    if (!thresholdCurrent && !thresholdForecast)
    {
    	sendMessage("Neither threshold is set")
    }
    
	state.YesterdayRainInches = 0.0

	scheduleCheck()

	if (primeCurrent)
    {
		state.YesterdayRainInches = primeCurrent
    }
    
    state.daysClosed = 0

    if (daysClosed)
    {
    	state.daysClosed = daysClosed
    }
    
    scheduleSetTest()
    
    if (daysClosed)
    {
    	state.daysClosed = daysClosed
    }
}

def installed()
{
	log.debug "Installed: $settings"
    unschedule()
    // Even though update calls this it has to be called here too, why?
    reschedule()
}

def uninstalled()
{
	log.debug "Uninstalled: $settings"
    unschedule()
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

def checkPrecip(active)
{
	try
    {    	
		def rainInchesCurrent = getCurrentPrecip()
		def rainInchesForecast = getForecastPrecip()
        def rainInchesText = "Yesterday: $state.YesterdayRainInches, Today: $rainInchesCurrent, Forecast: $rainInchesForecast"
        
        rainInchesCurrent = state.YesterdayRainInches + rainInchesCurrent
        
        if (state.daysClosed > 0)
        {
	        state.daysClosed = state.daysClosed - 1
            setClose("Valve: Closed, $state.daysClosed days closed left", active)
            return
        }

		if (thresholdCurrent && thresholdForecast)
        {
            def stats = "Rain[$rainInchesText] Threshold: [Current: $thresholdCurrent Forecast: $thresholdForecast]"
            if ((rainInchesCurrent && (rainInchesCurrent > thresholdCurrent)) || (rainInchesForecast && (rainInchesForecast > thresholdForecast)))
            {
                setClose("Valve: Closed, $stats", active)
            }
            else
            {
                setOpen("Valve: Opened, $stats", active)
            }
        }
        else if (thresholdForecast)
        {
            def stats = "Rain[$rainInchesText] Threshold: [Forecast: $thresholdForecast]"
            if (rainInchesForecast > thresholdForecast)
            {
                setClose("Valve: Closed, $stats", active)
            }
            else
            {
                setOpen("Valve: Opened, $stats", active)
            }
        }
        else if (thresholdCurrent)
        {
            def stats = "Rain[$rainInchesText] Threshold: [Current: $thresholdCurrent]"
            if (rainInchesCurrent > thresholdCurrent)
            {
                setClose("Valve: Closed, $stats", active)
            }
            else
            {
                setOpen("Valve: Opened, $stats", active)
            }
        }
        else
        {
        	sendMessage("No threshold Set")
        }
	} 
	catch (e)
	{
        setOpen("Set: Opened, Exception: $e")
	}
}

def setOpen(message, active)
{
    sendMessage("$message Active: $active")
    if (active)
    {
        for (valve in valves)
        {
            valve.open()
        }
    }
}

def setClose(message, active)
{
    sendMessage("$message Active: $active")
    if (active)
    {
        for (valve in valves)
        {
            valve.close()
        }
    }
}

def scheduleOpen()
{
    setOpen("Open: Scheduled Standby State")	
}

def scheduleClose()
{
    setClose("Close: Scheduled Standby State")	
}

def scheduleSet()
{
	try
    {    	
        checkPrecip(true)
	} 
	catch (e)
	{
        setOpen("Set: Opened, Exception: $e", true)
	}
}

def schedule2ndSet()
{
    scheduleSet()
}

def scheduleSetTest()
{
	try
    {    	
        checkPrecip(false)
	} 
	catch (e)
	{
        setOpen("Set: Opened, Exception: $e", false)
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
            if (rainInches != null)
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
    
    return null
}

def getForecastPrecip()
{
	try
    {
        def response = getWeatherFeature("forecast", zipcode)
        if (response)
        {
            def forecast = response?.forecast?.simpleforecast?.forecastday?.first()
            if (forecast)
            {
            	//log.debug "forecastday: $forecast"
                def rainInches = forecast?.qpf_allday?.in
            	//log.debug "raininches: $rainInches"
                //Bug in language "0.00" == false 
                if (rainInches != null)
                {
            	//log.debug "raininches: $rainInches"
                    if (rainInches == "T")
                    {
                        rainInches = "0.01"
                    }

                    if ("$rainInches".isFloat())
                    {
                        return rainInches.toFloat()
                    }
                    else
                    {
                        sendMessage("Check: Cannot Parse qpf")
                    }
                }
                else
                {
                    sendMessage("Check: No qpf_allday Found")
                }
            }
            else
            {
            	sendMessage("Check: No Simpleforecast Found")
            }
        }
        else
        {
        	sendMessage("Check: No forecast Found")
        }
	} 
	catch (e)
	{
        sendMessage("Check: Exception $e")        
	}
    
    return null
}

