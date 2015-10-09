(ns keywordstreamer.views
  (:require [reagent.core  :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))

(defn search-input [{:keys [title on-save on-stop]}]
  (let [query (subscribe [:query])]
    (fn [props]
      [:input (merge props
                     {:type "text"
                      :value @query
                      :on-change #(dispatch-sync
                                   [:query-changed (-> % .-target .-value)])
                      :class "form-control"})])))

(defn search-button []
  (let [streaming (subscribe [:streaming?])
        ready     (subscribe [:ready?])
        verbiage  (subscribe [:stream-button-verbiage])]
    (fn []
      [:button#search-keywords {:type "submit"
                                :disabled (not @ready)
                                :on-click #(dispatch [:submit])
                                :class "btn btn-primary"}
       [(:span @verbiage)
        {:aria-hidden "true" :style {:padding-right "5px"}}]
       (str (:label @verbiage) " Streaming")])))

(defn download-button []
  (let [selected (subscribe [:selected-results])
        csv-data (subscribe [:csv-data])]
    (fn []
      ;(when (seq @selected))
      [:form {:on-submit #(set! (.-value (.getElementById js/document "csrf-token")) js/csrf)
              :action "/download"
              :method "POST"}
       [:input {:type "hidden"
                :name "data"
                :value @csv-data}]
       [:input {:type "hidden" :name "__anti-forgery-token" :id "csrf-token"}]
       [:button#download-button {:class "btn btn-primary btn-sm pull-right"
                                 :disabled (not (seq @selected))
                                 :type "submit"}
        "Download Selected"]])))

(defn search-type-button [search-type]
  (let [id         (-> search-type key name)
        label-text (clojure.string/capitalize id)
        active?    (val search-type)
        classes    (str "btn btn-sm btn-default"
                        (if active? " active"))]
    [:label {:class classes}
     [:input {:type "checkbox"
              :id   id
              :on-change #(dispatch-sync
                           [:search-type-changed
                            (let [t (.-target %)]
                              {(keyword (.-id t))
                               (.-checked t)})])
              :checked (val search-type)}]
     label-text]))

(defn search-type-selector [searches]
  (fn [props]
    [:div.btn-group {:data-toggle "buttons"}
     (for [search @searches]
       ^{:key (key search)} [search-type-button search])]))

(defn table-header []
  (let [totals (subscribe [:totals])]
    (fn []
      [:div.row
       [:div.col-md-6 {:class "bottom-align-text"}
        [:strong "Keywords: (Visible/Selected/All) "
         (str (:visible @totals) "/"
              (:selected @totals) "/"
              (:all @totals))]]
       [:div.col-md-6
        [:button {:class    "btn btn-danger btn-sm pull-right"
                  :on-click #(dispatch-sync [:clear-results])}
         [:span.glyphicon.glyphicon-remove
          {:aria-hidden "true" :style {:padding-right "5px"}}]
         "Clear"]
        [download-button]]])))

(defn keywords-table [results]
  (fn [results]
    [:div#results-table
     [table-header]
     [:table.table.table-striped.table-hover
      [:thead
       [:th
        [:input {:type "checkbox"
                 :title "Select/Deselect All"
                           :on-click #(dispatch [:select-deselect-all
                                                 (-> % .-target .-checked)])}]]
       [:th "Search Term"]
       [:th "Keyword"]
       [:th "Source"]
       [:th "Focus"]]
      [:tbody
       (for [{:keys [id name search-type selected query]} @results]
         ^{:key id} [:tr
                     [:td
                      [:input {:type "checkbox"
                               :title "Select Keyword"
                               :checked selected
                               :on-change #(dispatch
                                            [:toggle-selection id])}]]
                     [:td query]
                     [:td name]
                     [:td (subs (str search-type) 1)]
                     [:td {:on-click #(dispatch [:focus-keyword name])}
                      [:span.glyphicon.glyphicon-play.focus-keyword
                       {:aria-hidden "true"}]]])]]]))

(defn no-results []
  (let [totals (subscribe [:totals])]
    (fn []
      (when (and
             (>     (:all @totals) 0)
             (zero? (:visible @totals)))
        [:p.lead "All results filtered"]))))

(defn keyword-results [results]
  (fn []
    (if (seq @results)
      [keywords-table results]
      [no-results])))

(defn keywordstreamer-app []
  (let [results  (subscribe [:visible-results])
        searches (subscribe [:searches])]
    (fn []
      [:div
       [:div.page-header [:h1 "Keyword Streamer"]]
       [:div.row
        [:div.col-md-10
         [search-input {:title "query"
                        :id "query"
                        :placeholder "Type your keywords here"}]]
        [:div.col-md-2
         [search-button]]]
       [:div.row
        [:div.col-md-10.search-verticals
         [search-type-selector searches]]]
       [:div.row
        [:div.col-md-10.search-results
         [keyword-results results]]]])))
