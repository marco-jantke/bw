(ns bw.core
  (require [clojure.java.io :as io] 
           [clj-time.format :as timeformat]
           [clj-time.coerce :as timecoerce]
           [clojure.data.json :as json]))

(def weekday-formatter (timeformat/formatter "EEEE"))
(def hour-minute-formatter (timeformat/formatter "HH:mm"))

(defn extract-line-info [^String line]
  (let [[timestamp datestring status] (clojure.string/split line #",")]
    { :date (java.util.Date. (* 1000 (read-string timestamp)))
      :status (read-string status) }))

(defn format-date [line-info formatter]
  (timeformat/unparse formatter (timecoerce/from-date (:date line-info))))

(defn avg-status [entries]
  (float (/ (apply + (map :status entries)) (count entries))))

(defn summarize-hour-minute-status [[hour-minute entries]]
  {:hour-minute hour-minute
   :avg-status (avg-status entries)
   :count-entries (count entries)})

(defn summarize-weekday-status [[weekday entries]]
  {:weekdy weekday
   :entries (->> (group-by #(format-date % hour-minute-formatter) entries)
                 (map summarize-hour-minute-status))
   :avg-status (avg-status entries)
   :count-entries (count entries)})

(defn read-bw-log [path]
  (with-open [rdr (io/reader path)]
    (->> (line-seq rdr)
         (map extract-line-info)
         (group-by #(format-date % weekday-formatter))
         (map summarize-weekday-status)
         (doall))))

(defn build-bw-json [log-path dest-path]
  (->> (read-bw-log log-path)
       (json/write-str)
       (spit dest-path)
))
