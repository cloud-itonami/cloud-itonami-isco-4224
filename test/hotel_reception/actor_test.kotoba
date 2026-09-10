(ns hotel-reception.actor-test
  (:require [clojure.test :refer [deftest is testing]]
            [hotel-reception.actor :as actor]
            [hotel-reception.store :as store]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-reservation! st {:reservation-id "reservation-1" :name "M. Tanaka"})
    st))

(deftest commits-a-clean-low-risk-request
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:reservation-id "reservation-1" :op :check-in :stake :low}
        result (actor/run-request! graph request {} "thread-1")]
    (is (= :done (:status result)))
    (is (some? (get-in result [:state :record])))
    (is (= 1 (count (store/records-of st "reservation-1"))))))

(deftest holds-on-unregistered-reservation-without-committing
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:reservation-id "no-such-reservation" :op :check-in :stake :low}
        result (actor/run-request! graph request {} "thread-2")]
    (is (= :done (:status result)))
    (is (nil? (get-in result [:state :record])))
    (is (empty? (store/records-of st "no-such-reservation")))
    (is (= :hold (:disposition (:state result))))))

(deftest interrupts-then-commits-on-human-approval
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        ;; stay-information disclosure always escalates (governor invariant)
        request {:reservation-id "reservation-1" :op :disclose-stay-information :stake :high}
        interrupted (actor/run-request! graph request {} "thread-3")]
    (is (= :interrupted (:status interrupted)))
    (is (empty? (store/records-of st "reservation-1")))
    (let [resumed (actor/approve! graph "thread-3")]
      (is (= :done (:status resumed)))
      (is (some? (get-in resumed [:state :record])))
      (is (= 1 (count (store/records-of st "reservation-1")))))))
