(ns bw.core
  (require [clojure.java.io :as io] 
           [clj-time.format :as timeformat]
           [clj-time.coerce :as timecoerce]
           [clojure.data.json :as json]))

(def weekday-formatter (timeformat/formatter "EEEE"))
(def hour-minute-formatter (timeformat/formatter "Hmm"))
(def month-formatter (timeformat/formatter "M"))
(def date-formatter (timeformat/formatter "YYYYMMdd"))

(defn read-bw-log [path]
  (with-open [rdr (io/reader path)]
    (->> (line-seq rdr)
         (map extract-line-info)
         (doall))))

(defn extract-line-info [^String line]
  (let [[timestamp datestring status] (clojure.string/split line #",")]
    { :date (java.util.Date. (* 1000 (read-string timestamp)))
      :status (read-string status) }))

(defn format-date [line-info formatter]
  (timeformat/unparse formatter (timecoerce/from-date (:date line-info))))

(defn weekday-to-int [weekday]
  (case weekday 
    "Monday" 1 "Tuesday" 2 "Wednesday" 3 
    "Thursday" 4 "Friday" 5 "Saturday" 6 "Sunday" 7))

(defn in-opening-hours? [line-info]
  (let [hm (read-string (format-date line-info hour-minute-formatter))]
    (and (> hm 659) (< hm 2301))))

(defn in-month? [line-info month]
  (= month (read-string (format-date line-info month-formatter))))

(defn avg-status [entries]
  (->> (float (/ (apply + (map :status entries)) (count entries)))
       (format "%.1f")
       (read-string)))

(defn summarize-hour-minute-status [[hour-minute entries]]
  {:hour-minute hour-minute
   :avg-status (avg-status entries)
   :count-entries (count entries)})

(defn summarize-weekday-status [[weekday entries]]
  {:weekday weekday
   :entries (->> (filter in-opening-hours? entries)
                 (group-by #(format-date % hour-minute-formatter))
                 (map summarize-hour-minute-status)
                 (sort-by #(read-string (:hour-minute %))))
   :avg-status (avg-status entries)
   :count-entries (count entries)})

(defn analyze-history [line-infos]
  (->> (group-by #(format-date % weekday-formatter) line-infos)
       (map summarize-weekday-status)
       (sort-by #(weekday-to-int (:weekday %)))))

(defn write-output-file [file output]
  (spit file (json/write-str output)))

(defn analyze-and-create-output-file [line-infos file]
  (->> (analyze-history line-infos)
       (write-output-file file)))

(defn remove-invalid-days [line-infos]
  (->> (group-by #(format-date % date-formatter) line-infos)
       (reduce 
        (fn [acc [day entries]]          
          (if (every? #(= 1 (:status %)) entries)
            acc
            (concat acc entries)))
        [])))

(defn run-analysis [raw-file dest-path]
  (let [raw-line-infos (read-bw-log raw-file)
        line-infos (remove-invalid-days raw-line-infos)]
    (analyze-and-create-output-file 
     line-infos 
     (str dest-path "complete-history.json"))
    (for [month (range 1 13)]
      (analyze-and-create-output-file
       (filter #(in-month? % month) line-infos)
       (str dest-path "month-history-" month ".json")))))
