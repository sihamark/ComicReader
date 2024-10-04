import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        CycloneApp.initialize()
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
