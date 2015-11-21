(ns bw.core-test
  (:use midje.sweet)
  (:use [bw.core]))

(def logfile-path "bw.log")

(facts "about 'line-info'"
  (fact "it extracts line info"
    (extract-line-info "1400567187,Tue May 20 06:26:27 +0000 2014,1")
    => { :date (java.util.Date. 1400567187000) :status 1 }))
