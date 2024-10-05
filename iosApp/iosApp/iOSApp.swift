import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        IosCycloneApp.shared.initialize()
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
