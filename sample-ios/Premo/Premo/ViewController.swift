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

import UIKit
import Common

class ViewController: UIViewController {
    
    let pm = CounterPm()

    @IBOutlet weak var label: UILabel!
    @IBOutlet weak var plusButton: UIButton!
    @IBOutlet weak var minusButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        pm.count.bindTo(consumer: { v in
            self.label.text = v!.stringValue
        })
        
        pm.plus.accept(v: KotlinUnit.init())
        
        pm.plusButtonEnabled.bindTo(consumer: { enabled in
            self.plusButton.isEnabled = enabled!.boolValue
        })
        
        pm.minusButtonEnabled.bindTo(consumer: { enabled in
            self.minusButton.isEnabled = enabled!.boolValue
        })
    }
    
    @IBAction func plus(_ sender: Any) {
        pm.plus.accept(v: KotlinUnit.init())
    }
    
    @IBAction func minus(_ sender: Any) {
        pm.minus.accept(v: KotlinUnit.init())
    }
    
}

