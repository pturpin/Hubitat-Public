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
    description: "App to keep the Inovelli switch LED light strip brightness in sync with bulb level",
    documentationLink: "https://community.inovelli.com/t/led-bar-smart-bulbs-etc/4635/9",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    importUrl: "https://raw.githubusercontent.com/pturpin/Hubitat-Public/master/apps/InovelliNotificationLed-SmartBulbSyncInstance.groovy",
)

preferences {
    page name: "mainPage", title: "", install: true, uninstall: true
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
    log.info "There are ${childApps.size()} child apps"
    childApps.each { child ->
    	log.info "Child app: ${child.label}"
    }
}

def installCheck() {         
	state.appInstalled = app.getInstallationState()
	
	if (state.appInstalled != 'COMPLETE') {
		section{paragraph "Please hit 'Done' to finish install '${app.label}' parent app "}
  	}
  	else {
    	log.info "Parent Installed OK"
  	}
}

def mainPage() {
    dynamicPage(name: "mainPage") {
    	installCheck()
		if (state.appInstalled == 'COMPLETE') {
			section("<h2 style='color:#1A77C9;font-weight: bold'>${app.label}</h2>") {
				paragraph "Make the brightness level of the LED strip on an Inovelli wall switch follow the brightness level of a bulb or bulb group."
			}
  			section("<b>Inovelli LEDs to Sync:</b>") {
				app(name: "anyOpenApp", appName: "Inovelli Smart Bulb LED Notification Sync Child", namespace: "pturpin", title: "<b>Add a new switch LED binding</b>", multiple: true)
			}
		}
	}
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
