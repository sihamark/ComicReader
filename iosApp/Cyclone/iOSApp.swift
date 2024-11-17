import SwiftUI
import CycloneApp

@main
struct iOSApp: App {
    init() {
        IosCycloneApp.shared.initialize()
    }
    var body: some Scene {
        WindowGroup {
            CycloneAppWrapper().ignoresSafeArea()
        }
    }
}
