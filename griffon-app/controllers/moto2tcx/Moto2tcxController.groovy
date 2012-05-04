package moto2tcx

import groovy.swing.SwingBuilder
import javax.swing.filechooser.FileFilter
import javax.swing.JFileChooser

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
		println model.sport
		println model.fileName
	}
	
	def convert = { evt = null ->
	}
}
