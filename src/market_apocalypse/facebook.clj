(ns market-apocalypse.facebook
  (:gen-class)
  (:require [clojure.string :as s]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [environ.core :refer [env]]))

(def PAGE_ACCESS_TOKEN (env :page-access-token))
(def VERIFY_TOKEN (env :verify-token))
(def MAX_LENGTH 640)

(defn validate-webhook [request]
  (let [params (:params request)]
    (println "Incoming Webhook Request:")
    (println request)
    (if (and (= (params "hub.mode") "subscribe")
             (= (params "hub.verify_token") VERIFY_TOKEN))
        {:status 200 :body (params "hub.challenge")}
        {:status 403})))

(defn handle-message [request on-message on-postback on-attachments]
  ; TODO: IMPLEMENT APP_SECRET VALIDATION
  (println "Incoming Request:")
  (println request)
  (let [data (get-in request [:params])]
    (when (= (:object data) "page")
      (doseq [page-entry (:entry data)]
        (doseq [messaging-event (:messaging page-entry)]
          ; Check for message (onMessage) or postback (onPostback) here
          (cond (contains? messaging-event :postback) (on-postback messaging-event)
                (contains? messaging-event :message) (cond (contains? (:message messaging-event) :attachments) (on-attachments messaging-event)
                                                           :else (on-message messaging-event))
                :else (println (str "Webhook received unknown messaging-event: " messaging-event))))))))

(defn send-api [message-data]
  (println "Sending message-data:")
  (println message-data)
  (try
      (let [response (http/post "https://graph.facebook.com/v2.6/me/messages"
                      {:query-params {"access_token" PAGE_ACCESS_TOKEN}
                       :headers {"Content-Type" "application/json"}
                       :body (json/write-str message-data)
                       :insecure? true})]
        (if (= (:status @response) 200)
            (println "Successfully sent message to FB")
            (do
              (println "Error sending message to FB:")
              (println @response))))
      (catch Exception e (str "caught exception: " (.getMessage e)))))

(defn send-message [recipient-id message]
  (send-api {:recipient {:id recipient-id}
             :message message}))

(defn image-message [image-url]
  {:attachment {:type "image"
                :payload {:url image-url}}})

(defn text-message [message-text]
  {:text message-text})

(defn send-long-message [recipient-id message]
  (doall (map (fn [part] send-api {:recipient {:id recipient-id}
                                   :message (text-message (apply str part))}))
         (partition MAX_LENGTH MAX_LENGTH nil message)))
