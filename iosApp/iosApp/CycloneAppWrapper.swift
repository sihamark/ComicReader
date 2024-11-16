import UIKit
import SwiftUI
import CycloneApp

struct CycloneAppWrapper: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        RootView.shared.viewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}



