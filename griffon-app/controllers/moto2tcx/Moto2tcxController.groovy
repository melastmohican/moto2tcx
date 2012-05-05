package moto2tcx

import groovy.swing.SwingBuilder
import java.text.SimpleDateFormat
import javax.swing.filechooser.FileFilter
import javax.swing.JFileChooser
import com.xlson.groovycsv.CsvParser
import groovy.xml.MarkupBuilder

class Moto2tcxController {
    // these will be injected by Griffon
    def model
    def view

    // void mvcGroupInit(Map args) {
    //    // this method is called after model and view are injected
    // }

    // void mvcGroupDestroy() {
    //    // this method is called when the group is destroyed
    // }

    /*
        Remember that actions will be called outside of the UI thread
        by default. You can change this setting of course.
        Please read chapter 9 of the Griffon Guide to know more.
       
    def action = { evt = null ->
    }
    */
	
	def chooseFile = { evt = null ->
		def openChooseFileDialog  = new SwingBuilder().fileChooser(dialogTitle:"Choose an csv file", 
                                   id:"openExcelDialog", fileSelectionMode : JFileChooser.FILES_ONLY, 
                                   //the file filter must show also directories, in order to be able to look into them
                                   fileFilter: [getDescription: {-> "*.csv"}, accept:{file-> file ==~ /.*?\.csv/ || file.isDirectory() }] as FileFilter) {}
		//later, in the controller
		def fc = openChooseFileDialog
		if(fc.showOpenDialog() != JFileChooser.APPROVE_OPTION) return //user cancelled
		model.fileName = fc.selectedFile
	}
	
/**	
	<Trackpoint>
	<Time>2012-05-01T16:28:03Z</Time>
	<Position>
	 <LatitudeDegrees>37.39442</LatitudeDegrees>
	 <LongitudeDegrees>-122.100174</LongitudeDegrees>
	</Position>
	<AltitudeMeters>-11.0</AltitudeMeters>
	<DistanceMeters>1.9766617</DistanceMeters>
	<HeartRateBpm xsi:type="HeartRateInBeatsPerMinute_t">
	 <Value>73</Value>
	</HeartRateBpm>
	<Cadence>0</Cadence>
	<SensorState>Absent</SensorState>
   </Trackpoint>
**/	
	def convert = { evt = null ->
		SimpleDateFormat ISO8601UTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
		ISO8601UTC.setTimeZone(TimeZone.getTimeZone("UTC"))
		
		Reader reader = new FileReader(model.fileName);		
		def data = new CsvParser().parse(reader)
		
		def writer = new StringWriter()
		def builder = new MarkupBuilder(writer)
		builder.mkp.xmlDeclaration(version:"1.0", encoding:"UTF-8")	
		def xml = builder.TrainingCenterDatabase(xmlns:'http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2',
				'xmlns:xsi':'http://www.w3.org/2001/XMLSchema-instance',
				'xsi:schemaLocation':'http://www.garmin.com/xmlschemas/ActivityExtension/v2 http://www.garmin.com/xmlschemas/ActivityExtensionv2.xsd http://www.garmin.com/xmlschemas/FatCalories/v1 http://www.garmin.com/xmlschemas/fatcalorieextensionv1.xsd http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd') {
			Folders()
			Activities() {
				Activity(sport:model.sport) {
					def start = new Date(new Long(data[0].timestamp_epoch))
					Id(ISO8601UTC.format(start))
					Lap(StartTime:ISO8601UTC.format(start)) {
						TotalTimeSeconds()
						DistanceMeters()
						MaximumSpeed()
						Calories()
						AverageHeartRateBpm('xsi:type':'HeartRateInBeatsPerMinute_t') {
							Value()
						}
						MaximumHeartRateBpm('xsi:type':'HeartRateInBeatsPerMinute_t') {
							Value()
						}
						Intensity('Active')
						TriggerMethod('Distance')
						Track() { 
							data.each { line -> 
								Trackpoint() {
									
									def date = new Date(new Long(line.timestamp_epoch))		
									Time(ISO8601UTC.format(date)) 
									Position() {
										LatitudeDegrees(line.LATITUDE) 
										LongitudeDegrees(line.LONGITUDE)
									}
									AltitudeMeters(line.ELEVATION) 
									DistanceMeters(line.DISTANCE) 
									HeartRateBpm( 'xsi:type':"HeartRateInBeatsPerMinute_t") {
										Value(line.HEARTRATE)
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
		println writer.toString()
		new File("moto.tcx").write(writer.toString())
	}
}
