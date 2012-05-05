package moto2tcx

application(title: 'moto2tcx',
  preferredSize: [640, 480],
  pack: true,
  //location: [50,50],
  locationByPlatform:true,
  iconImage: imageIcon('/griffon-icon-48x48.png').image,
  iconImages: [imageIcon('/griffon-icon-48x48.png').image,
               imageIcon('/griffon-icon-32x32.png').image,
               imageIcon('/griffon-icon-16x16.png').image]) {
    // add content here
    formPanel('ConvertForm.xml')
	noparent {
		bean(model, sport: bind{ cboSport.selectedItem })
		bean(txtFile, text: bind{ model.fileName })
		bean(btnFile, actionPerformed: controller.chooseFile)
		bean(btnConvert, actionPerformed: controller.convert)
	}
}

