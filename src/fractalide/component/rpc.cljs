(ns fractalide.component.rpc
  (:require-macros
    [javelin.core :refer [defc defc= cell=]]
    [config :as compile-time])
  (:require
    [javelin.core :refer [cell]]
    [castra.core :refer [mkremote]]))

(defc port-compilation-result nil)
(defc solution-compilation-result nil)
(defc error nil)
(cell= (when error
         (print "Remote error:")
         (vec (for [k (.keys js/Object error)]
                (print k ":" (aget error k))))))
(defc loading [])
(defc config nil)

(defn remote [fn-name result & [error* loading*]]
  (mkremote (symbol (str "component.api/" fn-name))
            result
            (or error* error)
            (or loading* loading)
            {:url (compile-time/config :srv-fractalide-url)}))

(def compile-port
  (remote 'compile-port port-compilation-result))

(def compile-solution
  (remote 'compile-solution solution-compilation-result))



