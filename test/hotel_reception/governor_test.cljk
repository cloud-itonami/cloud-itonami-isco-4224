(ns hotel-reception.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [hotel-reception.store :as store]
            [hotel-reception.governor :as governor]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-reservation! st {:reservation-id "reservation-1" :name "M. Tanaka"})
    st))

(deftest ok-on-clean-check-in
  (let [st (fresh-store)
        proposal {:op :check-in :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:reservation-id "reservation-1"} {} proposal st)]
    (is (:ok? v))
    (is (not (:hard? v)))
    (is (not (:escalate? v)))))

(deftest hard-on-unregistered-reservation
  (let [st (fresh-store)
        proposal {:op :check-in :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:reservation-id "no-such-reservation"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-reservation (:rule %)) (:violations v)))))

(deftest hard-on-no-actuation-violation
  (let [st (fresh-store)
        proposal {:op :check-in :effect :direct-write :confidence 0.9 :stake :low}
        v (governor/check {:reservation-id "reservation-1"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-actuation (:rule %)) (:violations v)))))

(deftest escalates-on-stay-information-disclosure
  (let [st (fresh-store)
        proposal {:op :disclose-stay-information :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:reservation-id "reservation-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-reservation-security-check-override
  (let [st (fresh-store)
        proposal {:op :override-reservation-security-check :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:reservation-id "reservation-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-low-confidence
  (let [st (fresh-store)
        proposal {:op :check-in :effect :propose :confidence 0.2 :stake :low}
        v (governor/check {:reservation-id "reservation-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest store-records-and-ledger-append-only
  (let [st (fresh-store)]
    (store/commit-record! st {:reservation-id "reservation-1" :op :assist})
    (store/append-ledger! st {:disposition :commit})
    (is (= 1 (count (store/records-of st "reservation-1"))))
    (is (= 1 (count (store/ledger st))))))
