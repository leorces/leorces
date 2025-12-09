# Events overview

Events in BPMN describe occurrences that influence the flow of a process. A workflow may respond to incoming events (
catch) or produce outgoing events (throw).
For instance, a catching message event resumes the flow when a specified message arrives, and the BPMN XML defines the
exact message characteristics that allow the token to proceed.
Events can be incorporated into the model in multiple positions. They serve not only as wait states that pause execution
until a condition is met, but also as mechanisms that can interrupt or terminate an ongoing path.
Supported event types at the moment include:

- [None events](none-events.md)
- [Message events](message-events.md)
- [Error events](error-events.md)
- [Escalation events](escalation-events.md)
- [Terminate events](terminate-events.md)