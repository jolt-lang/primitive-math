(ns primitive-math-test
  "The shim vs JVM semantics: long arithmetic wraps, shifts mask their count,
  >>> shifts the unsigned value, casts narrow. Expected values measured on
  OpenJDK 20."
  (:require [primitive-math :as p]))

(def failures (atom 0))
(defn check [label expected actual]
  (if (= expected actual)
    (println "  ok  " label)
    (do (swap! failures inc)
        (println "  FAIL" label "— expected" (pr-str expected) "got" (pr-str actual)))))

(defn -main [& _]
  (check "add wraps at 64 bits" -9223372036854775808 (p/+ 9223372036854775807 1))
  (check "subtract wraps" 9223372036854775807 (p/- -9223372036854775808 1))
  (check "multiply wraps" -2 (p/* 9223372036854775807 2))
  (check "shift left wraps" -9223372036854775808 (p/<< 1 63))
  (check "shift count masks to 63" 2 (p/<< 1 65))
  (check "signed shift right" -1 (p/>> -1 1))
  (check "unsigned shift right" 9223372036854775807 (p/>>> -1 1))
  (check "bit-not" -43 (p/bit-not 42))
  (check "bit ops" [8 14 6] [(p/bit-and 12 10) (p/bit-or 12 10) (p/bit-xor 12 10)])
  (check "byte narrows" -1 (p/byte 255))
  (check "short narrows" -1 (p/short 65535))
  (check "int narrows" -1 (p/int 4294967295))
  (check "inc wraps" -9223372036854775808 (p/inc 9223372036854775807))
  (check "comparisons" [true false true] [(p/< 1 2) (p/> 1 2) (p/== 3 3)])
  (if (pos? @failures)
    (throw (ex-info "failures" {:n @failures}))
    (println "all primitive-math checks passed")))
