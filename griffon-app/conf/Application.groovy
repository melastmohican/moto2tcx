application {
    title = 'Moto2tcx'
    startupGroups = ['moto2tcx']

    // Should Griffon exit when no Griffon created frames are showing?
    autoShutdown = true

    // If you want some non-standard application class, apply it here
    //frameClass = 'javax.swing.JFrame'
}
mvcGroups {
    // MVC Group for "moto2tcx"
    'moto2tcx' {
        model      = 'moto2tcx.Moto2tcxModel'
        view       = 'moto2tcx.Moto2tcxView'
        controller = 'moto2tcx.Moto2tcxController'
    }

}
