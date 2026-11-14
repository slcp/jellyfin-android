# Tabletop playback review

This implementation is AI-assisted and is intended for review in Stuart's fork.
It does not incorporate the rejected upstream patch.

## Behaviour and decisions

A HALF_OPENED horizontal FoldingFeature places the entire native player and its
sibling overlays above the hinge. The lower screen stays black. Web and external
players are outside this change. Unsupported postures, invalid geometry and PiP
restore the original layout parameters. Root layout changes recalculate window
coordinates, avoiding queued callbacks with stale hinge geometry.

The player itself is the bounded container. Media3 zoom can enlarge its internal
surface, but PlayerView clips it within the upper pane. Existing controller
visibility, timeout, playback lock, menus and gesture dimensions stay owned by
the existing helpers. The fullscreen button changes system bars while tabletop
is active instead of forcing landscape. Automatic landscape on startup waits for
the first posture result so it cannot preempt fold detection. Entering tabletop releases an automatic
orientation request only when playback controls are not locked.

The deliberate tradeoff is that controls occupy the upper pane. Moving them below
the hinge requires a separate design using a supported controller integration;
this PR avoids reparenting internal Media3 views or duplicating controller state.
There is no custom immersive flex mode or changes to playback information menus.

The current fork already requires API 23, matching WindowManager 1.5.1. No
manifest minimum-SDK override is used. The posture subscription uses the view
lifecycle; disposal removes the root listener and restores original parameters.
A failed posture flow restores normal layout until the next lifecycle restart.

## Validation

Unit tests cover window-coordinate conversion, zero-width creases, nonzero hinge
bands, PiP and unsupported posture fallback, unmeasured views, hinges outside the
view, invalid hinge ordering and arithmetic overflow. The existing fork CI test task will use the JUnit Platform for the added tests.

Physical playback and UI transitions have not yet been validated. Before release:

| Scenario | Expected result |
| --- | --- |
| Razr inner screen at 90 degrees | Whole video and controls above crease |
| Flat, closed/cover screen, vertical/book posture | Original layout |
| Enter/leave PiP while half folded | Full PiP viewport; split restored on return |
| Resize window so hinge is outside player | Original layout, no stale split |
| Portrait, 4:3 and wide video; pinch zoom | Nothing draws below upper pane |
| Playback lock before/after folding | No navigation or controls revealed |
| Open playback information then fold/unfold | Existing timeout suppression retained |
| Repeated folding, background/foreground, navigate away | No stale updates or crash |
| Normal API 23 phone and device without hinge reporting | Existing playback behaviour |

An emulator can check geometry and lifecycle, but cannot prove Motorola hinge
reporting or physical-device playback. The local VM has no KVM device exposed.

## Fullscreen-to-tabletop follow-up

Stuart confirmed basic tabletop playback on a Razr50Ultra in a quick physical
test. Fullscreen requested before folding left the app in landscape even though
the phone was physically portrait, with system auto-rotate disabled.

Additional commits retain HALF_OPENED hinge information even while the app is in
landscape. The existing SmartOrientationListener now exposes an optional raw
sensor callback before its auto-rotate guard. The player combines a confident
portrait reading, the expected clamshell hinge axis in the current window, and
an app landscape request before requesting portrait. A naturally book-style fold,
physical landscape, unknown orientation, PiP and playback lock do not trigger this
correction. Listener enable/disable invalidates cached sensor readings, so unlock
waits for fresh evidence. The existing portrait-angle tolerance is reused.

This uses the existing sensor and lifecycle; no new sensor polling, dependencies
or player-view reparenting. Unknown readings deliberately retain the current
orientation, which may defer correction if the phone sensor is nearly flat.

Follow-up device checks: fullscreen while flat then portrait tabletop with
auto-rotate on/off; physically landscape half-fold; playback lock/unlock after
rotating the phone; PiP transitions. The rebuilt follow-up still needs these
physical checks. Regression tests cover the rotation decision guards and hinge
axis mapping independently of Android UI classes.
