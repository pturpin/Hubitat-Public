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
    parent:      "pturpin:Inovelli Smart Bulb LED Notification Sync", 
    name:        "Inovelli Smart Bulb LED Notification Sync Child",
    namespace:   "pturpin",
    author:      "Paul Turpin",
    description: "Child app to keep the one LED light strip brightness in sync with bulb level",
    documentationLink: "https://community.inovelli.com/t/led-bar-smart-bulbs-etc/4635/9",
    category:    "Convenience",
    iconUrl:     "",
    iconX2Url:   "",
    iconX3Url:   "",
    importUrl:   "https://raw.githubusercontent.com/pturpin/Hubitat-Public/master/apps/InovelliNotificationLed-SmartBulbSyncInstanceChild.groovy",
)

preferences {
	page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "<h2 style='color:#1A77C9;font-weight: bold'>Configuration</h2>", install: true, uninstall: true) {
        if (!app.label) {
			app.updateLabel(app.name)
		}
        section ("<h3 style='color:#1A77C9;font-weight: bold'>Introduction</h3>") {
            paragraph "You can marry one smart bulb or bulb group to one LED on an Inovelli switch. The bulbs are the source of the level changes and the switch " +
                      "LED is the output - it is a one-way sync only. Bulbs and bulb groups from the Philips Hue bridge are supported."
            paragraph "You must be using the Inovelli-supplied drivers for the switch, you must have created the 'LED Color' child device by activating the " +
                      "'Create \"LED Color\" Child Device' option in the driver preferences. The LED Color device is your Notification Child device, below."
        }
        section("<span style='color:#1A77C9'>" + (app?.label ?: app?.name).toString() +"</span>") {
			input(name:	"nameOverride", type: "string", title: "Custom name for this ${app.name}?", multiple: false, required: false, submitOnChange: true)
            
			if (settings.nameOverride) {
				app.updateLabel(settings.nameOverride)
			}
		}
        section ("Smart Bulb (or group)")        { input "smartBulb",         "capability.switchLevel",   required: true }
        section ("LED Color Notifiction Child")  { input "notificationChild", "capability.switchLevel",   required: true }
    }
} 

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    subscribe(smartBulb, "level", levelChanged)
    subscribe(smartBulb, "switch.on", bulbOn)
    subscribe(smartBulb, "switch.off", bulbOff)
    app.updateLabel(createAppLabel())
}

def levelChanged(evt) {
    logEvent(evt, "levelChanged")
    def level = evt.value as Integer
    if (level == null || level < 10) level = 10
    notificationChild.setLevel level 
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

def createAppLabel() {
	if (settings.nameOverride && settings.nameOverride.size() > 0) {
		return settings.nameOverride	
	} 
	return "Sync " + settings.notificationChild.displayName + " to " + settings.smartBulb.displayName;
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
