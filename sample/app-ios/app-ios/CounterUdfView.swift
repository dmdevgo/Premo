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


struct CounterUdfView: View {
    
    private let pm: CounterUdfPm
    
    @ObservedObject
    private var state: ObservableState<CounterUdfPm.CounterState>
    
    init(pm: CounterUdfPm) {
        self.pm = pm
        state = ObservableState(pm.state)
    }
    
    var body: some View {
        HStack {
            Button("Minus", action: {
                pm.minusClick()
            })
            .disabled(state.value?.minusEnabled == false)
            .padding()
            
            Text("\(state.value?.count ?? 0)")
                .padding()
            
            Button("Plus", action: {
                pm.plusClick()
            })
            .disabled(state.value?.plusEnabled == false)
            .padding()
        }
    }
}

struct CounterUdfView_Previews: PreviewProvider {
    static var previews: some View {
        CounterUdfView(pm: CounterUdfPm(maxCount: 10))
    }
}
