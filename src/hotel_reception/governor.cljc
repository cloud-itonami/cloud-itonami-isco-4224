(ns hotel-reception.governor
  "HotelReceptionGovernor — the independent safety/traceability layer
  for the ISCO-08 4224 independent hotel-reception actor. Wired as
  its own `:govern` node in `hotel-reception.actor`'s StateGraph,
  downstream of `:advise` — the Advisor has no notion of reservation
  provenance or disclosure/security-override risk, so this MUST be a
  separate system able to reject a proposal (itonami actor pattern,
  per ADR-2607011000 / CLAUDE.md Actors section).

  `check` is a pure function of (request, context, proposal, store) ->
  verdict; it never mutates the store. The StateGraph's `:decide` node
  routes on the verdict:
    :hard? true                → :hold  (irreversible, no write)
    :escalate? true            → :request-approval (interrupt-before)
    otherwise                  → :commit

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. reservation provenance  — the request's reservation must be
       registered.
    2. no-actuation              — proposal :effect must be
       :propose.
  ESCALATION invariants (:escalate? true, ALWAYS human sign-off, per the
  README robotics-premise: disclosing a guest's stay information and
  overriding a reservation-security check always require human
  sign-off):
    3. :op :disclose-stay-information.
    4. :op :override-reservation-security-check.
    5. low confidence (< `confidence-floor`)."
  (:require [hotel-reception.store :as store]))

(def confidence-floor 0.6)
(def ^:private escalating-ops #{:disclose-stay-information :override-reservation-security-check})

(defn- hard-violations [{:keys [proposal]} reservation-record]
  (cond-> []
    (nil? reservation-record)
    (conj {:rule :no-reservation :detail "未登録 reservation"})

    (not= :propose (:effect proposal))
    (conj {:rule :no-actuation :detail "effect は :propose のみ許可（直接書込禁止）"})))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a
  `store` implementing `hotel-reception.store/Store`. Returns
  `{:ok? bool :violations [...] :confidence n :hard? bool :escalate? bool}`."
  [request context proposal store]
  (let [reservation-record (store/reservation store (:reservation-id request))
        hard (hard-violations {:proposal proposal} reservation-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        risky-op? (contains? escalating-ops (:op proposal))]
    {:ok? (and (not hard?) (not low?) (not risky-op?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? risky-op?))}))
