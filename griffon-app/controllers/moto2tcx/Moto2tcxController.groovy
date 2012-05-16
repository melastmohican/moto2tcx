package moto2tcx

import groovy.swing.SwingBuilder
import java.text.SimpleDateFormat
import javax.swing.filechooser.FileFilter
import javax.swing.JFileChooser
import com.xlson.groovycsv.CsvParser
import groovy.xml.MarkupBuilder
import groovy.util.ConfigSlurper

class Moto2tcxController {
	// these will be injected by Griffon
	def model
	def view
	
	def settingsFile =  System.properties['user.home'] + "/.moto2ctx/settings.groovy"
	
	def settings = new ConfigSlurper().parse('''
	last.directory = '.'
	''')

	def chooseFile = { evt = null ->
		
		def openFileDialog  = new JFileChooser(dialogTitle:"Choose an CSV file",
				fileSelectionMode : JFileChooser.FILES_ONLY,
				fileFilter: [getDescription: {-> "*.csv"}, accept:{file-> file.getName() ==~ /.*?\.csv/ || file.isDirectory() }] as FileFilter)
		
		try {
			def saved = new ConfigSlurper().parse(new File(settingsFile).toURL())
			settings = settings.merge(saved) 
		} catch (all) {
		}
		
		openFileDialog.currentDirectory = new File(settings.last.directory)
		
		if(openFileDialog.showOpenDialog() != JFileChooser.APPROVE_OPTION) return //user cancelled
		model.fileName = openFileDialog.selectedFile				
		settings.last.directory = openFileDialog.currentDirectory.absolutePath
		def settingsFile = new File(settingsFile)
		try {
			settingsFile << "// moto2tcx settings"
		} catch (all) {
			def settingsDir = new File(System.properties['user.home'] + "/.moto2ctx")
			settingsDir.mkdir()
		}
		settingsFile.withWriter { writer -> settings.writeTo(writer) }
	}

	def convert = { evt = null ->
		SimpleDateFormat ISO8601UTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
		ISO8601UTC.setTimeZone(TimeZone.getTimeZone("UTC"))

		Reader reader = new FileReader(model.fileName);
		def lines = new CsvParser().parse(reader).toList()

		int size = lines.size()
		def firstLine = lines[0]
		def lastLine = lines[size-1]
		def sumHR = lines.sum() { line -> return new Double(line.HEARTRATE).toInteger() }
		def avgHR = new Double(sumHR / lines.size()).toInteger()

		def maxHRLine = lines.max() { line -> return new Double(line.HEARTRATE).toInteger() }
		def maxHR = new Double(maxHRLine.HEARTRATE).toInteger()

		def maxSpeedLine = lines.max() { line -> return new Double(line.SPEED) }
		def maxSpeed = new Double(maxSpeedLine.SPEED)

		long start = new Long(firstLine.timestamp_epoch)
		long end = new Long(lastLine.timestamp_epoch)
		long total = (end-start)/1000
		def distance = lastLine.DISTANCE
		int calories = new Double(lastLine.CALORIEBURN).toInteger()

		def writer = new StringWriter()
		def builder = new MarkupBuilder(writer)
		builder.mkp.xmlDeclaration(version:"1.0", encoding:"UTF-8", standalone:"no")
		def xml = builder.TrainingCenterDatabase(xmlns:"http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2",
				"xmlns:xsi":"http://www.w3.org/2001/XMLSchema-instance",
				"xsi:schemaLocation":"http://www.garmin.com/xmlschemas/ActivityExtension/v2 http://www.garmin.com/xmlschemas/ActivityExtensionv2.xsd http://www.garmin.com/xmlschemas/FatCalories/v1 http://www.garmin.com/xmlschemas/fatcalorieextensionv1.xsd http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd") {
					Folders()
					Activities() {
						Activity(Sport:model.sport) {
							Id(ISO8601UTC.format(new Date(start)))
							Lap(StartTime:ISO8601UTC.format(start)) {
								TotalTimeSeconds(total)
								DistanceMeters(distance)
								MaximumSpeed(maxSpeed)
								Calories(calories)
								AverageHeartRateBpm('xsi:type':'HeartRateInBeatsPerMinute_t') { Value(avgHR) }
								MaximumHeartRateBpm('xsi:type':'HeartRateInBeatsPerMinute_t') { Value(maxHR) }
								Intensity('Active')
								TriggerMethod('Distance')
								Track() {
									lines.each { line ->
										Trackpoint() {
											Time(ISO8601UTC.format(new Date(new Long(line.timestamp_epoch))))
											Position() {
												LatitudeDegrees(line.LATITUDE)
												LongitudeDegrees(line.LONGITUDE)
											}
											AltitudeMeters(line.ELEVATION)
											DistanceMeters(line.DISTANCE)
											HeartRateBpm( 'xsi:type':"HeartRateInBeatsPerMinute_t") {
												Value(new Double(line.HEARTRATE).toInteger())
											}
											Cadence(line.CADENCE)
											SensorState('Absent')
										}
									}

								}
							}
							Creator('xsi:type':'Device_t') {
								Name('melastmohican')
								UnitId('007')
								ProductID('007')
								Version() {
									VersionMajor(1)
									VersionMinor(0)
									BuildMajor(1)
									BuildMinor(0)
								}
							}
						}
					}
					Workouts()
					Courses()
					Author('xsi:type':'Application_t') {
						Name('Garmin Training Center')
						Build() {
							Version() {
								VersionMajor(1)
								VersionMinor(0)
								BuildMajor(1)
								BuildMinor(0)
							}
							Type('Release')
							Time('Jan  1 2012, 22:00:00')
							Builder('melastmohican')
						}
						LangID('en')
						PartNumber('006-A0183-00')
					}

				}
		//println writer.toString()
	    def inputFile = new File(model.fileName).getName()
		def outputFile = inputFile.lastIndexOf('.').with {it != -1 ? inputFile[0..<it] : inputFile} + ".tcx"
		def saveFileDialog  = new JFileChooser(dialogTitle:"Select destination file",
				fileSelectionMode : JFileChooser.FILES_ONLY, dialogType: JFileChooser.SAVE_DIALOG,
				fileFilter: [getDescription: {-> "*.tcx"}, accept:{file-> file.getName() ==~ /.*?\.tcx/ || file.isDirectory() }] as FileFilter)
		saveFileDialog.currentDirectory = new File(settings.last.directory)
		saveFileDialog.selectedFile = new File(outputFile)
		if(saveFileDialog.showSaveDialog() == JFileChooser.APPROVE_OPTION) {
			saveFileDialog.selectedFile.write(writer.toString())
		}
	}
}
