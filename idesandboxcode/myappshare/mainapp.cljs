(ns myappshare.mainapp
  (:require [webapp.framework.client.coreclient   :as c]
            [goog.net.XhrIo          :as xhr]
            [om.core]
            [webapp.framework.client.system-globals]
            [om.dom]
            [cljs.js :as cljs]
            [cljs.tools.reader :refer [read-string]]
            [webapp.framework.client.fns :refer [cljs-in-cljs main]]

            )
  (:use-macros [webapp.framework.client.coreclient  :only [defn-ui-component def-coils-app
                                                           container  map-many  inline  text log sql
                                                           div img pre component h2 input section header button label form
                                                           write-ui read-ui container input component <-- data-view-result-set
                                                           h1 h2 h3 h4 h5 h6 span  data-view-v2 select dselect realtime drealtime
                                                           input-field ]])
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))





(defn-ui-component     sandbox-app   [app] {}

    (div {:className "main_div"}

         (om.core/build main app {})

         "Loading app environment... please wait..."

         ))





(def-coils-app     main-view   sandbox-app)

(def autoin (atom 0))

(defn get-file [url cb]
  (.send goog.net.XhrIo url
    (fn [e]
      (cb (.. e -target getResponseText)))))





(defn load-fn [lib cb]
  (do
    (swap! autoin inc)
    (let [filename (str "/appshare/outide/" (:path lib) ".cljs?autoin=" @autoin )]
      ;(log (str "load-fn:" filename))
      (get-file   filename
                  (fn [src]
                    (do
                      ;(log (str "loaded-src:" src))

                      (cb {:lang :clj :source src})))))))







(defn  ^:export  execute_app_code  [code  calling-app-id  args]

  (do
    ;(js/alert (pr-str "::::::::::::" (js->clj args :keywordize-keys true)))
    (reset!  webapp.framework.client.system-globals/app-input-parameters   (js->clj args {:keywordize-keys true}))
    (cljs/eval-str (cljs/empty-state) code 'foo.bar
                   {
                     :eval           cljs/js-eval
                     :load           load-fn
                     :source-map     true
                     :def-emits-var  true
                     :static-fns     true
                     :ns             webapp.framework.client.fns
                     }

                   (fn [result]
                     (do
                           ;(js/alert     (pr-str result))
                       (cond
                         (:error result)
                         (do
                           ;(log     "******************* ERROR *******************************")
                           (js/alert     (pr-str result)))

                         :else
                         (do
                           (js/eval (:value result))))

                       ;(js/alert (str ":"  (js->clj args)))
                       ;(js/alert (pr-str (get (js->clj args) :b)))

                       result)))))



