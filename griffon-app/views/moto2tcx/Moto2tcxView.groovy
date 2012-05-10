package moto2tcx

import groovy.swing.SwingBuilder
import javax.swing.*

application(title: 'moto2tcx',
  preferredSize: [320, 160],
  pack: true,
  //location: [50,50],
  locationByPlatform:true,
  iconImage: imageIcon('/griffon-icon-48x48.png').image,
  iconImages: [imageIcon('/griffon-icon-48x48.png').image,
               imageIcon('/griffon-icon-32x32.png').image,
               imageIcon('/griffon-icon-16x16.png').image]) {
    // add content here   
   borderLayout()
   
   panel(constraints:CENTER) {
	   migLayout()
	   label("Sport", constraints: 'cell 0 0')
	   comboBox(items:["Biking", "Walking", "Running", "Skiing"],selectedIndex:0, selectedItem: bind { model.sport }, constraints: 'cell 1 0');
	   label("CSV File",constraints: 'cell 0 1')
	   button("Choose file", actionPerformed:controller.chooseFile, constraints: 'cell 1 1')
   }
   
   hbox(constraints:SOUTH) {
	   button("Convert", actionPerformed:controller.convert)
	   hstrut(5)
	   label(text:bind {model.fileName})
   }
}

