/**
 *  Copyright 2015 SmartThings
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
 *  Master Slave Switch
 *
 *  Date: 2018-Aug-04
 *
 * Change Log:
 */
definition(
		name: "Master Slave Switch",
		namespace: "mswlogo",
		author: "mswlogo",
		description: "Synchronize the state of 2 Switchs (One Way).",
		category: "Convenience",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact@2x.png",
		pausable: true
)

preferences {
	section("Choose Switches..."){
		input "masterSwitch1", "capability.switch", title: "Master Switch 1", required: true, multiple: false
		input "slaveSwitch1", "capability.switch", title: "Slave Switch 1", required: true, multiple: false
		input "masterSwitch2", "capability.switch", title: "Master Switch 2", required: false, multiple: false
		input "slaveSwitch2", "capability.switch", title: "Slave Switch 2", required: false, multiple: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
}

def initialize() {
    // initialize counter
    atomicState.active = true
    }


def subscribeToEvents() {
	subscribe(masterSwitch1, "switch.on", eventHandlerOn1)
	subscribe(masterSwitch1, "switch.off", eventHandlerOff1)
	subscribe(masterSwitch2, "switch.on", eventHandlerOn2)
	subscribe(masterSwitch2, "switch.off", eventHandlerOff2)
    atomicState.active1 = 1
    atomicState.active2 = 1

}

def eventHandlerOn1(evt) {
	log.debug "Notify got evt ${evt}"
    slaveSwitch1.on()
    if (masterSwitch2) {
    	atomicState.active2 = 0
    	masterSwitch2.off()
    }
}

def eventHandlerOff1(evt) {
	log.debug "Notify got evt ${evt}"
    if (atomicState.active1 == 1) {
    	slaveSwitch1.off()
    }
    atomicState.active1 = 1
}

def eventHandlerOn2(evt) {
	log.debug "Notify got evt ${evt}"
    slaveSwitch2.on()
    atomicState.active1 = 0
    masterSwitch1.off()
}

def eventHandlerOff2(evt) {
	log.debug "Notify got evt ${evt}"
    if (atomicState.active2 == 1) {
    	slaveSwitch2.off()
    }
    atomicState.active2 = 1
}



