/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2023 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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
    private var dialogs: ObservableState<NSArray>
    
    init(pm: DialogNavigationPm) {
        self.pm = pm
        self.dialogs = ObservableState<NSArray>(pm.dialogGroupNavigation.dialogsFlow)
    }
    
    var body: some View {
        
        let alertIsShowingBinding = Binding<Bool>(get: { self.dialogs.value?.count ?? 0 > 0 }, set: { _ in })
        
        NavigationView {
            VStack {
                
                Spacer()
                
                Button("Show simple dialog", action: {
                    pm.showSimpleDialogClick()
                })
                    .padding()
                
                Button("Show dialog for result", action: {
                    pm.showSimpleDialogForResultClick()
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
            .alert(isPresented: alertIsShowingBinding) {
                let dialogPm = dialogs.value?.lastObject as? SimpleDialogPm
                return Alert(
                    title: Text(dialogPm?.title ?? ""),
                    message: Text(dialogPm?.message ?? ""),
                    primaryButton: .default(Text(dialogPm?.cancelButtonText ?? ""), action: {
                        dialogPm?.onCancelClick()

                    }),
                    secondaryButton: .default(Text(dialogPm?.okButtonText ?? ""), action: {
                        dialogPm?.onOkClick()
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
