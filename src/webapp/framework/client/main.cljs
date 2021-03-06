(ns webapp.framework.client.main
  (:require
    [goog.net.cookies :as cookie]
    [om.core          :as om :include-macros true]
    [om.dom           :as dom :include-macros true]
    [cljs.core.async  :refer [put! chan <! pub timeout]]
    [clojure.data     :as data]
    [clojure.string   :as string]
    )

  (:use
    [webapp.framework.client.coreclient      :only  [remote-fn]]
    [webapp.framework.client.system-globals  :only  [app-state
                                                     reset-app-state
                                                     start-component
                                                     init-fn
                                                     cookie-name
                                                     touch
                                                     client-session-atom
                                                     client-sessions
                                                     ]]
    [webapp.framework.client.components.system-container :only  [main-view]]
    )
  (:use-macros
    [webapp.framework.client.coreclient :only  [remote  log  get-cookie-name]]
    )
  (:require-macros
    [cljs.core.async.macros :refer [go]]))




(defn figwheel-update []
  (do
    (touch [:ui])
    (log "*** Updated UI from figwheel")))





(def history-order (atom 0))
(def start-time (.getTime (js/Date.)))









(defn add-browser-details [field value]
  (reset! app-state (assoc-in @app-state [:system field ] value)))




(defn add-view-details [field value]
  (reset! app-state (assoc-in @app-state [:view field ] value)))




(defn detect-browser []
  (add-browser-details :cookie-enabled (.-cookieEnabled js/navigator))
  (add-browser-details :language (.-language js/navigator))
  (add-browser-details :platform (.-platform js/navigator))
  (add-browser-details :who-am-i (js/sayswho))

  (add-view-details :width js/viewportw)
  (add-view-details :height js/viewporth))












(defn load-vm [cookie-name    view-window     dom-element-name]
  (go
    (let [ dom-element                    (. js/document (getElementById  dom-element-name))
           cookie-session-id              (cookie/get  cookie-name)
           create-session-response        (remote !create-session {
                                                                    :init-state                 (with-out-str (prn @app-state))

                                                                    :browser                    (str (-> @app-state :system :platform) ","
                                                                                                     (-> @app-state :system :who-am-i))

                                                                    :session-id-from-browser     cookie-session-id
                                                                    })
           client-session-id              (:session-id create-session-response)
           client-user                    (:user create-session-response)
           ]
      (log (str "cookie-session-id: " cookie-session-id))
      (log (str "create-session-response: " create-session-response))

      ;(js/alert (str "cookie name:" (get-cookie-name)))





      ;(js/alert (str "Retrieved session ID : " cookie-session-id))
      ;(js/alert (str "Retrieved session ID : " (:session-id create-session-response)))


      (if client-session-id
        (do
          (reset-app-state    cookie-session-id)
          (@init-fn)
          (detect-browser)

          (reset! client-session-atom  {:session-id   client-session-id
                                        :user         client-user})

          (swap! client-sessions assoc client-session-id (atom {}))
          (cookie/set  cookie-name  client-session-id)
          (om/root   view-window

                     app-state

                     {:target dom-element}))))))








(defn main []
  (do
    ;(js/alert (str "Loaded VM for cookie: "  (get-cookie-name)))
    (load-vm  (get-cookie-name)
              main-view
              "main")))












(defn ^:export defaultmain [app owner]
  (dom/div nil
           (dom/h2 nil "Clojure on coils")
           ))

(defn ^:export defaultinit []
  nil
  )


;--------------------------------------------------------
(defn ^:export  load_main [setup-config]
  ;-----------------------1---------------------------------
  (reset!  start-component  (get setup-config :start-component))
  (reset!  init-fn          (get setup-config :setup-fn))
  (main))
