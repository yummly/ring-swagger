(ns ring.swagger.openapi3-schema
  "Schemas that Ring-Swagger expects from it's clients"
  (:require [schema.core :as s]
            [ring.swagger.swagger2-schema :as swagger2-schema]))




(s/defschema OpenAPI3
  {(swagger2-schema/opt :info) swagger2-schema/Info})