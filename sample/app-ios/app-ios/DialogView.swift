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

struct DialogView: View {
    
    private let pm: DialogPm
    
    @ObservedObject
    private var showDialog: ObservableBoolean
    
    @ObservedObject
    private var showResult: ObservableBoolean
    
    @ObservedObject
    private var result: ObservableString
    
    @ObservedObject
    private var alertMessage: ObservableString
    
    init(pm: DialogPm) {
        self.pm = pm
        showDialog = ObservableBoolean(pm.alert.isShown)
        showResult = ObservableBoolean(pm.showResult)
        result = ObservableString(pm.alertResult)
        alertMessage = ObservableString(pm.alert.message)
    }
    
    var body: some View {
        VStack {
            Button("Show simple dialog", action: {
                pm.showDialog.invoke()
            })
            .padding()
            .alert(isPresented: $showDialog.value) {
                Alert(
                    title: Text("Dialog"),
                    message: Text(alertMessage.value),
                    primaryButton: .default(Text("Ok")) {
                        pm.alert.okClick()
                    },
                    
                    secondaryButton: .cancel() {
                        pm.alert.cancelClick()
                    }
                )
            }
            
            Button("Show dialog for result", action: {
                pm.showDialogForResult.invoke()
            })
            .padding()
            .alert(isPresented: $showResult.value) {
                Alert(
                    title: Text("Dialog result:"),
                    message: Text(result.value),
                    dismissButton: .default(Text("Ok")) {
                        pm.hideResult.invoke()
                    }
                )
            }
        }
    }
}

struct DialogView_Previews: PreviewProvider {
    static var previews: some View {
        DialogView(pm: Stubs.init().dialogPm)
    }
}
