(ns market-apocalypse.bot
  (:gen-class)
  (:require [clojure.core]
            [clojure.string :as s]
            [environ.core :refer [env]]
            [market-apocalypse.facebook :as fb]
            [clojure.java.io :as io]))

;(defn items {"cigarettes": "http://images.indianexpress.com/2015/05/cigarette-butt-main.jpg"
;             "bread": "https://media.mnn.com/assets/images/2014/05/primary_Sliced%20Bread.jpg.653x0_q80_crop-smart.jpg"]))

(def poems (map (fn [fname] (slurp (clojure.java.io/resource fname))) ["stillirise.txt" "thingsworkout.txt"]))

(defn random-poem []
  (-> poems shuffle first))

(defn on-message [payload]
  (println "on-message payload:")
  (println payload)
  (let [sender-id (get-in payload [:sender :id])
        recipient-id (get-in payload [:recipient :id])
        time-of-message (get-in payload [:timestamp])
        message-text (get-in payload [:message :text])]
    (cond
      (s/includes? (s/lower-case message-text) "nothing to offer") (fb/send-long-message (random-poem))
      (s/includes? (s/lower-case message-text) "help") (fb/send-message sender-id (fb/text-message "Hi there, happy to help :)"))
      (s/includes? (s/lower-case message-text) "image") (fb/send-message sender-id (fb/image-message "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/M101_hires_STScI-PRC2006-10a.jpg/1280px-M101_hires_STScI-PRC2006-10a.jpg"))
      ; If no rules apply echo the user's message-text input
      :else (fb/send-message sender-id (fb/text-message message-text)))))

(defn read-commodity [message]
  (""))

(defn on-postback [payload]
  (println "on-postback payload:")
  (println payload)
  (let [sender-id (get-in payload [:sender :id])
        recipient-id (get-in payload [:recipient :id])
        time-of-message (get-in payload [:timestamp])
        postback (get-in payload [:postback :payload])
        referral (get-in payload [:postback :referral :ref])]
    (cond
      (= postback "GET_STARTED") (fb/send-message sender-id (fb/text-message "Welcome =)"))
      :else (fb/send-message sender-id (fb/text-message "Sorry, I don't know how to handle that postback")))))

(defn on-attachments [payload]
  (println "on-attachment payload:")
  (println payload)
  (let [sender-id (get-in payload [:sender :id])
        recipient-id (get-in payload [:recipient :id])
        time-of-message (get-in payload [:timestamp])
        attachments (get-in payload [:message :attachments])]
    (fb/send-message sender-id (fb/text-message "Thanks for your attachments :)"))))
