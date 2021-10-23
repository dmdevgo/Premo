# Premo

Premo is a Kotlin Multiplatform library that helps you implement the Presentation Layer and share it on iOS and Android. 

Focus on writing logic instead of solving common and boring UI related issues:
- **Lifecycle**
- **Persistence**
- **Navigation**

At the same time, the library provides freedom of choice:
- **Architecture** — MVVM, PM, MVI or other.
- **UI** — Jetpack Compose for Android and SwiftUI for iOS.

## Main Idea

The library is based on the ideas of the [Presentation Model](https://martinfowler.com/eaaDev/PresentationModel.html) pattern described by Martin Fowler.
The Presentation Model stores the state for presentation and coordinates with the domain model layer.

In practice, you will have not one Presentation Model, but a composition of child Presentation Models.
At the base, there will be a root Presentation Model, from which a tree of children will grow.
Children can be pushed into the Stack Navigator, thus organizing the navigation stack.

Such a tree composition is well suited for a hierarchical view as well as for a composition of functions from declarative UI frameworks.

<img src="/docs/images/premo_diagram.png">

## Sample

[Sample](https://github.com/dmdevgo/Premo/tree/master/sample) demonstrates:
- Sharing presentation logic between Android and iOS.
- Using StateFlow to implement MVVM (PM).
- Simple Counter.
- Stack and Bottom navigation with multistack.
- Saving and restoring the state after killing the process.
- UI on Jetpack Compose and SwiftUI.

## License

```
The MIT License (MIT)

Copyright (c) 2020-2021 Dmitriy Gorbunov (dmitriy.goto@gmail.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
