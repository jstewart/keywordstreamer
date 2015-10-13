(ns ^:figwheel-always keywordstreamer.views
  (:require [reagent.core  :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))

(defn instructions []
  [:div
   [:h4 "Instructions"]
   [:p
    (str  "Keyword streamer is a free keyword tool that searches several data sources for long "
             "tail keyword suggestions. Just type your keyword into "
             "the search box and watch as we find hundreds of long tail keywords")]
   [:p (str "Let keyword streamer run for as long as you like. The longer you "
            "let it run, the more keywords we find for you.")]

   [:div.bs-callout.bs-callout-info
    [:h4 "Channels"]
    [:p
     (str "If you want video or shopping results you can select those "
          "channels underneath the search bar for even more results")]]

   [:div.bs-callout.bs-callout-info
    [:h4 "Drill Down"]
    [:p
     (str "Let's say that you found a long tail keyword that you're interested in "
          "expanding. You don't have to start over, like you will with many other "
          "tools. Just click on the keyword to drill down into that keyword")]]


   [:div.bs-callout.bs-callout-info
    [:h4 "Download Keywords"]
    [:p
     (str "When you're happy with your list of keywords, select some with the "
          "checkboxes, then click the \"Download Selected\" button to download and use "
          "in your favorite competition analysis tool.")]]])

(defn search-input [{:keys [title on-save on-stop]}]
  (let [query (subscribe [:query])]
    (fn [props]
      [:a {:name "searchbox"}]
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
        [:span.glyphicon.glyphicon-floppy-save
         {:aria-hidden "true" :style {:padding-right "5px"}}]
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
              :on-change #(dispatch
                           [:search-type-changed
                            (let [t (.-target %)]
                              {(keyword (.-id t))
                               (.-checked t)})])
              :checked (val search-type)}]
     label-text]))

(defn search-type-selector []
  (let [searches (subscribe [:searches])]
    (fn []
      [:div.btn-group {:data-toggle "buttons"}
       (for [search @searches]
         ^{:key (key search)} [search-type-button search])
       ])))

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

(defn keyword-row
  [id]
  (fn [id]
    (let [row (subscribe [:row id])
          {:keys [selected query name search-type]} @row]
      [:tr
       [:td
        [:input {:type "checkbox"
                 :title "Select Keyword"
                 :checked selected
                 :on-change #(dispatch
                              [:toggle-selection id])}]]
       [:td query]
       [:td
        [:a {:on-click #(dispatch [:focus-keyword name])
             :href "#searchbox"}
         name]]
       [:td (subs (str search-type) 1)]])))

(defn keywords-table [result-ids]
  (fn [result-ids]
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
       [:th "Keyword (Click to Drill Down)"]

       [:th "Source"]]
      [:tbody
       (for [id @result-ids] ^{:key id} [keyword-row id])]]]))

(defn no-results []
  (let [totals (subscribe [:totals])]
    (fn []
      (if (and (zero? (:visible @totals)) (> (:all @totals) 0))
        [:p.lead "All results filtered"]
        [instructions]))))

(defn keyword-results [result-ids]
  (fn [result-ids]
    (if (seq @result-ids)
       [keywords-table result-ids]
       [no-results])))

(defn keywordstreamer-app []
  (let [result-ids (subscribe [:visible-result-ids])]
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
         [search-type-selector]
         ]]
       [:div.row
        [:div.col-md-10.search-results
         [keyword-results result-ids]]]])))
