import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        KoinInitializerKt.startKoinApp()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}