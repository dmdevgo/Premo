# 1.0.0-alpha.14 / 05.02.2024
- Fixed `DialogGroupNavigator` and `DialogGroupNavigationHost`.
- Removed sender tag from `PmMessage`.`PmMessage` is the interface again.

# 1.0.0-alpha.13 / 04.02.2024
- Published new module for PM testing: `premo-test`. Use `runPmTest` to test a presentation and navigation logic.
- Added new methods to send PM messages: `sendToTarget` and `sendToChildren`. Added sender tag to `PmMessage` (now it is abstract class).
- Added navigation hosts interfaces: `StackNavigationMost`, `SetNavigationHost`, `DialogGroupNavigationHost`, `DialogNavigationHost`, `MasterDetailNavigationHost`.
- Added new extensions for `PmLifecycle`: `doOnCreate`, `doOnForeground`, `doOnBackground`, `doOnDestroy`.
- Added errors handler for delegates `onSaveOrRestoreStateError`.
- Added extension `JvmPmDelegate<*>.attachWindowLifecycle` for desktop.
- Added `DialogNavigator`.
- Added `SingleStatePresentationModel`.
- Used `Dispatchers.Main.immediate` for PM scopes now.
- Added `PmStateHandler.setSaved`. 
- Fixed compatibility with java 1.8.

# 1.0.0-alpha.12 / 15.01.2024
- Removed `PmDescription` and `PmParams`. Instead, use `PmArgs` to pass serializable arguments to the `PresentationModel`.
- Added `NoPmStateSaver` and `NoPmStateSaverFactory`.
- Added `oldState` parameter to `PmLifecycle.Observer`
- Kotlin is updated to 1.9.21.

# 1.0.0-alpha.11 / 29.08.2023
- Published new module: `premo-navigation-compose`.
- Removed `showForResult` from `DialogNavigator` because this way of waiting the result is not persistent.
- Removed `sendResult` from `DialogNavigator`. The right way to send a result is using PM messages.
- Kotlin is updated to 1.9.0.

# 1.0.0-alpha.10 / 30.07.2023
- Published new module: `premo-saver-json`.
- Implemented `DialogGroupNavigation` for showing a group of dialogs.
- Implemented new PM delegates: `IosPmDelegate`, `JvmPmDelegate`, `JsPmDelegate`, `AndroidPmDelegate`.
- Removed `PmActivityDelegate` and `Premo`. Use `AndroidPmDelegate` directly in the Activity.
- Removed `PmLifecycle.Event`.
- Lots of improvements in navigators and state savers.

# 1.0.0-alpha.09 / 18.05.2023
- `PmActivity` is interface now. You need to call `Premo.init()` in the Android app's `onCreate`.
- Added list of removed PMs to `BackStackChange`.
- Added a method to replace values for `SetNavigator`.
- Fixed: Detach a dialog's PM after sending result.
- Kotlin is updated to 1.8.20.
- Added React Sample.
- Added Compose Sample for iOS and Web.

# 1.0.0-alpha.08 / 16.12.2022
- Added `iosSimulatorArm64` target.
- Implemented `DialogNavigator`.
- Fixed `currentTopFlow` property of `StackNavigator`.
- Added default back handling for `SetNavigator`.
- A bit of refactoring: improved extensions for navigators.

# 1.0.0-alpha.07 / 11.12.2022
- Kotlin is updated to 1.7.20.
- Fixed the desktop sample.
- Removed `SystemBackMessage`. Use `BackMessage` instead.
- A bit of refactoring: renaming and using SAM interface.

# 1.0.0-alpha.06 / 04.06.2022
Added commands for `StackNavigator`:
- popToRoot
- popUntil
- replaceTop
- replaceAll

# 1.0.0-alpha.05 / 09.05.2022
- Abstraction level support for implementing state persistence in different formats (Json, ProtoBuf, Parcelable) #6.
- Added `PmStateSaverFactory` and `BundleStateSaver`. 

# 1.0.0-alpha.04 / 02.01.2022
- Added JS target.
- Implemented the sample for Web with Compose Multiplatform.

# 1.0.0-alpha.03 / 30.12.2021
- Added JVM target.
- Implemented the sample for desktop with Compose Multiplatform.

# 1.0.0-alpha.02 / 22.12.2021
**Premo**
- Kotlin is updated to 1.6.0.
- Migrated to the new memory model.

**Premo-navigation**
- `MasterDetailNavigator` — implements adaptive navigation for Master-Detail views.

# 1.0.0-alpha.01 / 24.10.2021
First public release:

**Premo**
- `PresentationModel` — the base class for placing presentation logic.
- `PmLifecycle` — represents the lifecycle of the Presentation Model.
- `PmMessageHandler` — provide communication of Presentation Models. Allows you to send a message from the child Presentation Model towards the root Presentation Model.
- `PmStateHandler` - handles the saved state of the Presentation Model.

**Premo-navigation**
- `StackNavigator` — allows you to organize a back stack of the Presentation Models.
- `SetNavigator` — used to implement bottom navigation.
