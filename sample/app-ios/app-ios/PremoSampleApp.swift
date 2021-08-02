/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2021 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import SwiftUI
import Common

@main
struct PremoSampleApp: App {
    
    @Environment(\.scenePhase) var scenePhase
    
    private let delegate: PmDelegate<MainPm>
    
    init() {
        delegate = PmDelegate(
            pmParams: PmParams(
                tag: "MainPm",
                parent: nil,
                state: nil,
                factory: MainPmFactory(),
                description: MainPm.Description(),
                stateSaver: JsonStateSaver()
            )
        )
        delegate.onCreate()
    }
    
    var body: some Scene {
        WindowGroup {
            MainView(pm: delegate.presentationModel as MainPm)
        }
        .onChange(of: scenePhase) { newScenePhase in
              switch newScenePhase {
              case .active:
                print("App is active")
                delegate.onForeground()
              case .inactive:
                print("App is inactive")
              case .background:
                print("App is in background")
                delegate.onBackground()
              @unknown default:
                print("Unexpected Scene Phase")
              }
        }
    }
}
