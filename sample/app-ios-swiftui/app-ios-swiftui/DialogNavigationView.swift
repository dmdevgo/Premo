/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2026 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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

struct DialogNavigationView: View {
    
    private let pm: DialogNavigationPm
    
    @ObservedObject
    private var dialog: ObservableState<PresentationModel>
    
    private var alertDialog: AlertDialogPm<AnyObject>? {
        get {
            return dialog.value as? AlertDialogPm<AnyObject>
        }
    }
    
    init(pm: DialogNavigationPm) {
        self.pm = pm
        self.dialog = ObservableState<PresentationModel>(pm.dialogGroupNavigation.dialogFlow)
    }
    
    var body: some View {
        
        NavigationView {
            VStack {
                
                Spacer()
                
                Button("Show simple dialog", action: {
                    pm.onSimpleDialogClick()
                })
                .padding()
                
                Button("Show dialog for result", action: {
                    pm.onSimpleDialogForResultClick()
                })
                .padding()
                
                Spacer()
            }
            .navigationTitle("Dialog Navigation")
            .navigationBarTitleDisplayMode(.inline)
            .navigationBarBackButtonHidden(true)
            .navigationBarItems(leading: Button(action : {
                pm.back()
            }){
                Image(systemName: "arrow.left")
            })
            .alert(
                isPresented: Binding(
                    get: {
                        return alertDialog != nil
                    },
                    set: { isPresented in }
                )
            ) {
                return Alert(
                    title: Text(alertDialog?.args.title ?? ""),
                    message: Text(alertDialog?.args.message ?? ""),
                    primaryButton: .default(Text(alertDialog?.args.cancelButtonText ?? ""), action: {
                        alertDialog?.onCancelClick()
                        
                    }),
                    secondaryButton: .default(Text(alertDialog?.args.okButtonText ?? ""), action: {
                        alertDialog?.onOkClick()
                    })
                )
            }
        }
    }
}

struct DialogNavigationView_Previews: PreviewProvider {
    static var previews: some View {
        DialogNavigationView(pm: Stubs.init().dialogNavigationPm)
    }
}
