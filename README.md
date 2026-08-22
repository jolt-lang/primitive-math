# primitive-math for jolt

The published [primitive-math](https://github.com/clj-commons/primitive-math)
(both the deprecated `primitive-math` ns and `clj-commons.primitive-math`),
with its bundled Java `Primitives` class replaced by jolt class-model statics
(`primitive-math.jolt-shim`). Long arithmetic wraps to 64 bits and the casts
narrow like the JVM's. On jolt there are no primitive types, so the point is
COMPATIBILITY — code written against primitive-math (clj-uuid's bitmop) runs
unchanged — not speed.

```clojure
{:deps {primitive-math/primitive-math
        {:git/url "https://github.com/jolt-lang/primitive-math" :git/sha "..."}}}
```
