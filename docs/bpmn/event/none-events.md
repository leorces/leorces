# None events

“None” events represent unspecified occurrences, often referred to as “blank” events. They do not carry any specific
trigger or payload and are used purely to control the flow of the process.

## Start events

A process can have at most one none start event, in addition to any other types of start events.
A none start event triggers the start of a process instance or a subprocess as soon as the process or subprocess is
activated, without requiring any specific condition or external input.

## End events

A process or subprocess may include multiple none end events. When a none end event is reached, the current execution
path terminates. If no other active execution paths remain in the process instance or subprocess, the instance is
considered complete.
Similarly, if an activity has no outgoing sequence flows, it functions as though it were linked to a none end event—once
the activity finishes, the current execution path ends.