(ns ring.swagger.openapi3
  (:require [clojure.string :as str]
            [schema.core :as s]
            [schema-tools.core :as stc]
            [plumbing.core :as p]
            [ring.swagger.common :as common]
            [ring.swagger.json-schema :as rsjs]
            [ring.swagger.core :as rsc]
            [ring.swagger.openapi3-schema :as openapi3-schema]
            [ring.swagger.swagger2 :as swagger2]))

(def OpenAPI3 openapi3-schema/OpenAPI3)

;; NOTE: models are definitions in Swagger. OpenAPI3 has this as components > schema >
(defn extract-models [openapi]
  (let [route-meta (->> openapi
                        :paths
                        vals
                        (map vals)
                        flatten)
        body-models (->> route-meta
                         (map (comp :body :parameters)))
        response-models (->> route-meta
                             (map :responses)
                             (mapcat vals)
                             (keep :schema))]
    (concat body-models response-models)))


(s/defn openapi3-json
  "Produces swagger-json output from ring-swagger spec.
   Optional second argument is a options map, supporting
   the following options with defaults:

   :ignore-missing-mappings?        - (false) boolean whether to silently ignore
                                      missing schema to JSON Schema mappings. if
                                      set to false, IllegalArgumentException is
                                      thrown if a Schema can't be presented as
                                      JSON Schema.

   :default-response-description-fn - ((constantly \"\")) - a fn to generate default
                                      response descriptions from http status code.
                                      Takes a status code (Int) and returns a String.

   :handle-duplicate-schemas-fn     - (ring.swagger.core/ignore-duplicate-schemas),
                                      a function to handle possible duplicate schema
                                      definitions. Takes schema-name and set of found
                                      attached schema values as parameters. Returns
                                      sequence of schema-name and selected schema value.

   :collection-format               - Sets the collectionFormat for query and formData
                                      parameters.
                                      Possible values: multi, ssv, csv, tsv, pipes."
  ([openapi :- (s/maybe OpenAPI3)] (openapi3-json openapi nil))
  ([swagger :- (s/maybe OpenAPI3), options :- (s/maybe swagger2/Options)]
   (let [options (merge swagger2/option-defaults options)]
     (binding [rsjs/*ignore-missing-mappings* (true? (:ignore-missing-mappings? options))]
       (let [[paths definitions] (-> swagger
                                     swagger2/ensure-body-and-response-schema-names
                                     (extract-paths-and-definitions options))]
         (common/deep-merge
           swagger-defaults
           (-> swagger
               (assoc :paths paths)
               (assoc :definitions definitions))))))))