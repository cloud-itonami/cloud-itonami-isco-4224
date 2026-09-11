(ns hotel-reception.store
  "SSoT for the ISCO-08 4224 independent hotel-reception sole-
  proprietor actor. Store is a protocol injected into the
  `hotel-reception.actor` StateGraph — `MemStore` is the default,
  deterministic, zero-dep backend; a Datomic/kotoba-server-backed
  implementation can be swapped in without touching the actor or
  governor (itonami actor pattern, per ADR-2607011000 / CLAUDE.md
  Actors section).

  Domain:

    reservation — a registered guest reservation (:reservation-id,
                  :name)
    record      — a committed operating record under a reservation
                  (check-in note, guest-service assist, stay-
                  information disclosure, reservation-security-check
                  override) — written ONLY via commit-record!, never
                  mutated in place
    ledger      — an append-only audit trail of every proposal/
                  verdict/disposition, regardless of outcome (commit
                  or hold)")

(defprotocol Store
  (reservation [s reservation-id])
  (records-of [s reservation-id])
  (ledger [s])
  (register-reservation! [s reservation])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (reservation [_ reservation-id] (get-in @a [:reservations reservation-id]))
  (records-of [_ reservation-id] (filter #(= reservation-id (:reservation-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-reservation! [s reservation]
    (swap! a assoc-in [:reservations (:reservation-id reservation)] reservation) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:reservations {} :records [] :ledger []} seed)))))
