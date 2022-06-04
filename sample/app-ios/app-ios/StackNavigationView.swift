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

struct StackNavigationView: View {
    
    private let pm: StackNavigationPm
    
    @ObservedObject
    private var backstack: ObservableString
    
    @ObservedObject
    private var currentPm: ObservableState<PresentationModel>
    
    init(pm: StackNavigationPm) {
        self.pm = pm
        backstack = ObservableString(pm.backstackAsStringState)
        currentPm = ObservableState(pm.navigation.currentTopState)
    }
    
    var body: some View {
        
        NavigationView {
            VStack {
                
                Spacer()
                
                switch currentPm.value {
                case let pm as SimpleScreenPm: SimpleView(pm: pm)
                default: EmptyView()
                }
                
                Spacer()
                
                Text("Stack: \(backstack.value)")
                    .padding()
                
                Spacer()
                
                HStack {
                    Button("Push", action: {
                        pm.pushClick()
                    }).padding()
                    
                    Button("Pop", action: {
                        pm.popClick()
                    }).padding()
                    
                    Button("Pop to root", action: {
                        pm.popToRootClick()
                    }).padding()
                }
                
                HStack {
                    Button("Replace top", action: {
                        pm.replaceTopClick()
                    }).padding()
                    
                    Button("Replace all", action: {
                        pm.replaceAllClick()
                    }).padding()
                }
                
                Button("Set back stack", action: {
                    pm.setBackstackClick()
                }).padding()
                
                Spacer()
            }
            .navigationTitle("Stack Navigation")
            .navigationBarTitleDisplayMode(.inline)
            .navigationBarBackButtonHidden(true)
            .navigationBarItems(leading: Button(action : {
                pm.back()
            }){
                Image(systemName: "arrow.left")
            })
        }
    }
}

struct StackNavigationView_Previews: PreviewProvider {
    static var previews: some View {
        StackNavigationView(pm: Stubs.init().stackNavigationPm)
    }
}
