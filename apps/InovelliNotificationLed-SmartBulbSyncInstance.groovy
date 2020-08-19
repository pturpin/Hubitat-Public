/**
 *
 *  This is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You can obtain a copy of the GNU General Public License @ <https://www.gnu.org/licenses/>.
 */

definition(
    name:"Inovelli Smart Bulb LED Notification Sync",
    namespace: "pturpin",
    author: "Paul Turpin",
    description: "App to keep the LED light strip brightness in sync with bulb level",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: ""
)

preferences {
    page(name: "selectDevices", title: "Select Devices", install: true, uninstall: true) {
        section ("Smart Bulb")                   { input "smartBulb",         "capability.switchLevel",   required: true }
        section ("Notifiction Child")            { input "notificationChild", "capability.switchLevel",   required: true }
    }
} 

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(smartBulb, "level", levelChanged)
    subscribe(smartBulb, "switch.on", bulbOn)
    subscribe(smartBulb, "switch.off", bulbOff)
}

def levelChanged(evt) {
    logEvent(evt, "levelChanged")
    def level = evt.value
    if (level < 10) level = 10
    notificationChild.setLevel evt.value 
}

def bulbOn(evt) {
    logEvent(evt, "bulbOn")
    def level = smartBulb.currentValue('level')
    if (level < 10) level = 10
    notificationChild.setLevel level
}

def bulbOff(evt) {
    logEvent(evt, "bulbOff")
    notificationChild.setLevel 10
}

///////////////// Common Utility Methods ///////////////////////////////////////////
private logEvent(evt, type) {
    def properties = ["name","value","unit","type","archivable","descriptionText","displayed","source","isStateChange","description","translatable","locationId","hubId","installedAppId"]
    def methods = ["getDisplayName","getData","getJsonData","isPhysical","isDigital","getDate","getUnixTime","getDeviceId","getDevice","getLocation","getDoubleValue","getFloatValue","getDateValue","getIntegerValue","getLongValue","getNumberValue","getNumericValue"]
    def sb = new StringBuilder();
    for(def property: properties) {
        try {
            def val = evt."$property"
            if (val != null ) {
                sb.append("$property: $val,")
            }
        } catch (e) {
            log.debug "Error trying to get property $property. $e"
        }
    }
    log.debug "Event Properties ($type): $sb"
}

private switchIsOn(sw) {
    def val = sw.currentValue('switch')
    log.debug "Switch state of ${sw.label}(${sw.name}) is $val"
    return val == 'on';
}

private contactIsOpen(c) {
    def val = c.currentValue('contact')
    log.debug "Contact state of ${c.label}(${c.name}) is $val"
    return val == 'open';
}



