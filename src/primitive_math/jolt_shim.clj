(ns primitive-math.jolt-shim
  "The Primitives class primitive-math bundles as Java, as jolt class-model
  statics. Long arithmetic wraps to 64 bits and the casts narrow like the JVM's
  — bit-twiddling code (clj-uuid's bitmop) relies on both. Registered under
  every spelling the two published artifacts import: primitive_math.Primitives
  (danlentz-era and the deprecated ns of clj-commons 1.x) and
  clj_commons.primitive_math.Primitives.")

;; wrap/mask by MOD arithmetic, never by bit-and with a 64-bit mask: the mask
;; is a BigInt literal, and bit operations reject BigInt operands (on the JVM
;; and here alike).
(def ^:private two64 18446744073709551616)
(def ^:private two63 9223372036854775808)
(defn- w64 [n] (let [m (mod n two64)] (if (>= m two63) (- m two64) m)))
(defn- u64 [n] (mod n two64))
(defn- narrow [n bits]
  (let [size (bit-shift-left 1 bits)
        m (mod n size)]
    (if (>= m (quot size 2)) (- m size) m)))
(defn- both-int? [a b] (and (integer? a) (integer? b)))
(defn- arith [f] (fn [a b] (let [r (f a b)] (if (both-int? a b) (w64 r) r))))
(defn- rev-bytes [n bytes]
  (loop [i 0 acc 0 v (mod n (bit-shift-left 1 (* 8 bytes)))]
    (if (= i bytes)
      (narrow acc (* 8 bytes))
      (recur (inc i) (+ (* acc 256) (mod v 256)) (quot v 256)))))

(def ^:private table
  {"add"      (arith +)
   "subtract" (arith -)
   "multiply" (arith *)
   "divide"   (fn [a b] (if (both-int? a b) (w64 (quot a b)) (/ a b)))
   "rem"      (fn [a b] (rem a b))
   "inc"      (fn [n] (if (integer? n) (w64 (inc n)) (inc n)))
   "dec"      (fn [n] (if (integer? n) (w64 (dec n)) (dec n)))
   "negate"   (fn [n] (if (integer? n) (w64 (- n)) (- n)))
   "max"      (fn [a b] (if (> a b) a b))
   "min"      (fn [a b] (if (< a b) a b))
   "isZero"   (fn [n] (zero? n))
   "isPos"    (fn [n] (pos? n))
   "isNeg"    (fn [n] (neg? n))
   "lt"       (fn [a b] (< a b))
   "lte"      (fn [a b] (<= a b))
   "gt"       (fn [a b] (> a b))
   "gte"      (fn [a b] (>= a b))
   "eq"       (fn [a b] (== a b))
   "neq"      (fn [a b] (not (== a b)))
   "isTrue"   (fn [x] (true? x))
   "isFalse"  (fn [x] (false? x))
   "and"      (fn [a b] (and a b))
   "or"       (fn [a b] (or a b))
   "not"      (fn [a] (not a))
   "xor"      (fn [a b] (not= (boolean a) (boolean b)))
   "bitAnd"   (fn [a b] (w64 (bit-and a b)))
   "bitOr"    (fn [a b] (w64 (bit-or a b)))
   "bitXor"   (fn [a b] (w64 (bit-xor a b)))
   "bitNot"   (fn [a] (w64 (bit-not a)))
   ;; JVM long shifts use the count mod 64, and >>> shifts the UNSIGNED value
   "shiftLeft"          (fn [a n] (w64 (bit-shift-left a (bit-and n 63))))
   "shiftRight"         (fn [a n] (w64 (bit-shift-right a (bit-and n 63))))
   "unsignedShiftRight" (fn [a n] (w64 (quot (u64 a) (bit-shift-left 1 (bit-and n 63)))))
   "toByte"    (fn [n] (narrow n 8))
   "toShort"   (fn [n] (narrow n 16))
   "toInteger" (fn [n] (narrow n 32))
   "toFloat"   (fn [n] (double n))
   "reverseShort"   (fn [n] (rev-bytes n 2))
   "reverseInteger" (fn [n] (rev-bytes n 4))
   "reverseLong"    (fn [n] (rev-bytes n 8))})

(doseq [cls ["Primitives" "primitive_math.Primitives" "clj_commons.primitive_math.Primitives"]]
  (clojure.core/__register-class-statics! cls table))
