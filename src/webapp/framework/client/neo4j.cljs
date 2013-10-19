(ns webapp.framework.client.neo4j
    (:refer-clojure :exclude [val empty remove find next parents])
    (:require
        [cljs.reader :as reader]
        [crate.core :as crate]
        [cljs.core.async :as async :refer         [chan close!]]
        [clojure.string]
    )
    (:use
        [webapp.framework.client.coreclient :only  [body-html new-dom-id debug popup hide-popovers
                                                    show-popover set-text value-of find-el sql-fn neo4j-fn
                                                    swap-section el clear remote  add-to on-mouseover-fn on-click-fn]]
        [jayq.core                          :only  [$ css  append fade-out fade-in empty attr bind]]
        [webapp.framework.client.help       :only  [help]]
        [webapp.framework.client.eventbus   :only  [do-action esb undefine-action]]
        [domina                             :only  [ by-id value destroy! ]]
  )
  (:require-macros
    [cljs.core.async.macros :refer                 [go alt!]])
  (:use-macros
        [webapp.framework.client.eventbus :only    [redefine-action define-action]]
        [webapp.framework.client.coreclient :only  [ns-coils makeit defn-html on-click on-mouseover sql defn-html
                                                    defn-html2 neo4j log]]
        [webapp.framework.client.interpreter :only [! !! !!!]]
     )
)
(ns-coils 'webapp.framework.client.neo4j)


(defn neo-data [x] (first x))
(defn neo-keys [x] (-> x (neo-data) (keys)))
(defn neo-result [x k] (-> x (neo-data) (get k)))
(defn neo-result-keys [x k] (-> x (neo-data) (get k) (keys)))
(defn neo-properties [x k] (-> x (neo-data) (get k) (get :data)))
(defn neo-incoming [x k] (-> x (neo-data) (get k) :incoming_relationships))
(defn neo-outgoing [x k] (-> x (neo-data) (get k) :outgoing_relationships))


(comment go
   (.log js/console (str (neo-outgoing (<! (neo4j "START x = node(0) RETURN x" {} )) "x")))
)

(comment go
   (.log js/console (str (neo-incoming (<! (neo4j "START x = node(0) RETURN x" {} )) "x")))
)

(comment go
   (.log js/console (str (neo-result (<! (neo4j "START x = node(0) RETURN x" {} )) "x")))
)


(defn neo-id [x]
  (comment  go
      (get (first (<! (neo4j "START x = node(1) RETURN ID(x)" {} ) "x")) "ID(x)")
  )
)

(comment go
    (.log js/console (str  (first (<! (neo-id nil)))))

 )

(comment go
   (.log js/console

         (let [
               rr (<! (neo4j "CREATE (n {name : {value} , title : 'Developer'}) return n" {:value "Zubair"}  ) )
               ]
         (str
          (neo-properties
               rr
               "n")
          " : ID : "
          (<! (neo-id rr))
               ))))


(defn count-all-neo4j-records []
  (go
     (get (first (<!
             (neo4j "START x = node(*) RETURN count(x)" {} )
     )) "count(x)")
  )
)



  (go
     (log (<! (count-all-neo4j-records)))
  )