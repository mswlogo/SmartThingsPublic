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
 *  Master Slave Switch
 *
 *  Author: mswlogo
 *  Date: 2018-Aug-04
 *
 * Change Log:
 */
definition(
		name: "Master Slave PE653 Switch",
		namespace: "mswlogo",
		author: "mswlogo",
		description: "Synchronize the state of 2 Switchs (One Way).",
		category: "Convenience",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact@2x.png",
		pausable: true
)

preferences {
	section("Choose PE653 Switch..."){
		input "multiChannelSwitch", "capability.switch", title: "PE653 Switch", required: true, multiple: false
        input "phone", "phone", title: "SMS Phone number (Debug)?", required: false
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
    sendSms(phone, "Started")
    }


def subscribeToEvents() {
	subscribe(multiChannelSwitch, "switch1.on", eventHandlerOn1)
	subscribe(multiChannelSwitch, "switch1.off", eventHandlerOff1)
	subscribe(multiChannelSwitch, "switch2.on", eventHandlerOn2)
	subscribe(multiChannelSwitch, "switch2.off", eventHandlerOff2)

    atomicState.active1 = 1
    atomicState.active2 = 1
}

def eventHandlerOn1(evt) {
	log.debug "Notify got evt ${evt}"
    if (phone) {
    	sendSms(phone, "eventHandlerOn1 ${evt}")
        }
    multiChannelSwitch.setVSPSpeed1()
    atomicState.active2 = 0
    multiChannelSwitch.off2()
}

def eventHandlerOff1(evt) {
	log.debug "Notify got evt ${evt}"
    if (phone) {
    	sendSms(phone, "eventHandlerOff1")
        }

    if (atomicState.active1 == 1) {
    	multiChannelSwitch.setVSPSpeed0()
    }
    atomicState.active1 = 1
}

def eventHandlerOn2(evt) {
	log.debug "Notify got evt ${evt}"
    if (phone) {
    	sendSms(phone, "eventHandlerOn2")
        }

    multiChannelSwitch.setVSPSpeed2()
    atomicState.active1 = 0
    multiChannelSwitch.off1()
}

def eventHandlerOff2(evt) {
	log.debug "Notify got evt ${evt}"
    if (phone) {
    	sendSms(phone, "eventHandlerOff2")
        }

    if (atomicState.active2 == 1) {
    	multiChannelSwitch.setVSPSpeed0()
    }
    atomicState.active2 = 1
}

