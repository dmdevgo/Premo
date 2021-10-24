# 1.0.0-alpha.01 / 24.10.2021
First public release:
**Premo**
- `PresentationModel` — the base class for placing presentation logic.
- `PmLifecycle` — represents the lifecycle of the Presentation Model.
- `PmMessageHandle` — provide communication of Presentation Models. Allows you to send a message from the child Presentation Model towards the root Presentation Model.
- `PmStateHandler` - handles the saved state of the Presentation Model.

**Premo-navigation**
- `StackNavigator` — allows you to organize a back stack of the Presentation Models.
- `SetNavigator` — used to implement bottom navigation.