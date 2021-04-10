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

struct CounterView: View {
    
    private let pm: CounterPm
    
    @ObservedObject
    private var counter: ObservableInt
    
    @ObservedObject
    private var minusButtonEnabled: ObservableBoolean
    
    @ObservedObject
    private var plusButtonEnabled: ObservableBoolean
    
    init(pm: CounterPm) {
        self.pm = pm
        counter = ObservableInt(pm.count)
        minusButtonEnabled = ObservableBoolean(pm.minusButtonEnabled)
        plusButtonEnabled = ObservableBoolean(pm.plusButtonEnabled)
    }
    
    var body: some View {
        HStack {
            
            Button("Minus", action: {
                pm.minus.invoke()
            })
            .disabled(minusButtonEnabled.value == false)
            .padding()
            
            Text("\(counter.value)")
                .padding()
            
            Button("Plus", action: {
                pm.plus.invoke()
            })
            .disabled(plusButtonEnabled.value == false)
            .padding()
        }
    }
}

struct CounterView_Previews: PreviewProvider {
    static var previews: some View {
        CounterView(pm: Stubs.init().counterPm)
    }
}
