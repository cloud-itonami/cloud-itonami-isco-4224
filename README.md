# cloud-itonami-isco-4224

Open Occupation Blueprint for **ISCO-08 4224**: Hotel Receptionists.

This repository designs a forkable OSS business for an independent hotel receptionist: a check-in-assist robot performs luggage handling and key-card dispensing under a governor-gated actor, so the practice keeps its own guest-service and disclosure records instead of renting a closed hotel-management SaaS.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a check-in-assist robot performs luggage handling and key-card dispensing tasks under an actor that proposes
actions and an independent **Hotel Reception Governor** that gates them. The governor never
dispatches hardware itself; `:high`/`:safety-critical` actions (such as
disclosing a guest's stay information, or overriding a reservation-security check) require human sign-off.

A live sample of the operator console (robotics safety console, shared template) is rendered in [docs/samples/operator-console.html](docs/samples/operator-console.html) — pure-data HTML output of `kotoba.robotics.ui`.

## Core Contract

```text
reservation record + guest-service scope + disclosure policy
        |
        v
Reception Advisor -> Hotel Reception Governor -> check-in/assist, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, suppress
an operating record, or disclose sensitive data without governor approval and
audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `4224`). Required capabilities:

- :robotics
- :forms
- :identity
- :audit-ledger
- :bpmn

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
