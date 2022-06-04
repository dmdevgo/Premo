# 1.0.0-alpha.06 / 04.06.2022
Added commands for `StackNavigator`:
- popToRoot
- popUntil
- replaceTop
- replaceAll

# 1.0.0-alpha.05 / 09.05.2022
- Abstraction level support for implementing state persistence in different formats (Json, ProtoBuf, Parcelable) #6.
- Added PmStateSaverFactory and BundleStateSaver. 

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
