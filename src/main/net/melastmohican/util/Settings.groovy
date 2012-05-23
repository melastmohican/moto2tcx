package net.melastmohican.util

import griffon.util.Metadata
import groovy.util.ConfigObject
import groovy.util.ConfigSlurper


/**
 * @author Mariusz Jurgielewicz
 */
class Settings {
	String settingsFile =  escapeWindowsPath(System.properties['user.home'] + "/." + Metadata.current['app.name'] + "/settings.groovy")
	ConfigObject config

	Settings(String script) {
		config = new ConfigSlurper().parse(script)
		try {
			def saved = new ConfigSlurper().parse(new File(settingsFile).toURI().toURL())
			config = config.merge(saved)
		} catch (all) {
			//assert all in MalformedURLException
		}
	}

	/**
	 * Replaces Windows path separator (\\) with /
	 * @param x
	 * @return
	 */
	public static def escapeWindowsPath(String x) {
		return x.replace("\\", "/") 
	}

	public void save() {
		def file = new File(settingsFile)
		try {
			file << "// Settings"
		} catch (all) {
			def dir = new File(escapeWindowsPath(System.properties['user.home'] + "/." + Metadata.current['app.name']))
			dir.mkdir()
		}
		file.withWriter { writer -> config.writeTo(writer) }
	}
}
