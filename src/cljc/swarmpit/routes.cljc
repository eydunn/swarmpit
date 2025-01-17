(ns swarmpit.routes
  (:require [bidi.bidi :as b]
            [cemerick.url :refer [map->query]]
            [clojure.string :as str]))

(def backend
  ["" {"/"           {:get :index}
       "/events"     {:get  :events
                      :post :event-push}
       "/version"    {:get :version}
       "/login"      {:post :login
                      :get  :index}
       "/slt"        {:get :slt}
       "/password"   {:post :password}
       "/api-token"  {:get    :index
                      :post   :api-token-generate
                      :delete :api-token-remove}
       "/initialize" {:post :initialize}
       "/api"        {"/secrets/"    {:get    {[:id] {""          :secret
                                                      "/services" :secret-services}}
                                      :delete {[:id] :secret-delete}
                                      :post   {[:id] :secret-update}}
                      "/volumes"     {:get :volumes, :post :volume-create}
                      "/nodes/"      {:get  {[:id] {""       :node
                                                    "/tasks" :node-tasks}}
                                      :post {[:id] :node-update}}
                      "/plugin/"     {:get {"network" :plugin-network
                                            "log"     :plugin-log
                                            "volume"  :plugin-volume}}
                      "/tasks/"      {:get {[:id] :task}}
                      "/volumes/"    {:get    {[:name] {""          :volume
                                                        "/services" :volume-services}}
                                      :delete {[:name] :volume-delete}}
                      "/stacks"      {:get  :stacks
                                      :post :stack-create}
                      "/admin/"      {"users"  {:get  :users
                                                :post :user-create}
                                      "users/" {:get    {[:id] :user}
                                                :delete {[:id] :user-delete}
                                                :post   {[:id] :user-update}}}
                      "/secrets"     {:get  :secrets
                                      :post :secret-create}
                      "/configs"     {:get  :configs
                                      :post :config-create}
                      "/registry/"   {"public"        {:get {"/repositories" :public-repositories}}
                                      [:registryType] {""  {:get  :registries
                                                            :post :registry-create}
                                                       "/" {:get    {[:id "/repositories"] :registry-repositories
                                                                     [:id]                 :registry}
                                                            :delete {[:id] :registry-delete}
                                                            :post   {[:id] :registry-update}}}}
                      "/networks"    {:get  :networks
                                      :post :network-create}
                      "/networks/"   {:get    {[:id] {""          :network
                                                      "/services" :network-services}}
                                      :delete {[:id] :network-delete}}
                      "/nodes"       {:get :nodes}
                      "/repository/" {:get {"tags"  :repository-tags
                                            "ports" :repository-ports}}
                      "/placement"   {:get :placement}
                      "/configs/"    {:get    {[:id] {""          :config
                                                      "/services" :config-services}}
                                      :delete {[:id] :config-delete}}
                      "/tasks"       {:get :tasks}
                      "/stacks/"     {:get    {[:name] {"/file"     :stack-file
                                                        "/compose"  :stack-compose
                                                        "/services" :stack-services
                                                        "/networks" :stack-networks
                                                        "/volumes"  :stack-volumes
                                                        "/configs"  :stack-configs
                                                        "/secrets"  :stack-secrets}}
                                      :delete {[:name] :stack-delete}
                                      :post   {[:name] {""          :stack-update
                                                        "/redeploy" :stack-redeploy
                                                        "/rollback" :stack-rollback}}}
                      "/me"          {:get :me},
                      "/services/"   {:get    {[:id] {""          :service
                                                      "/logs"     :service-logs
                                                      "/networks" :service-networks
                                                      "/tasks"    :service-tasks
                                                      "/compose"  :service-compose}}
                                      :delete {[:id] :service-delete}
                                      :post   {[:id] {""          :service-update
                                                      "/redeploy" :service-redeploy
                                                      "/rollback" :service-rollback}}}
                      "/labels/"     {:get {"service" :labels-service}}
                      "/services"    {:get  :services
                                      :post :service-create}}}])

(def frontend ["" {"/"                 :index
                   "/login"            :login
                   "/error"            :error
                   "/unauthorized"     :unauthorized
                   "/account-settings" :account-settings
                   "/services"         {""               :service-list
                                        "/create/wizard" {"/image"  :service-create-image
                                                          "/config" :service-create-config}
                                        ["/" :id]        {""      :service-info
                                                          "/edit" :service-edit
                                                          "/log"  {""            :service-log
                                                                   ["/" :taskId] :service-task-log}}}
                   "/stacks"           {""                      :stack-list
                                        "/create"               :stack-create
                                        ["/" :name]             :stack-info
                                        ["/" :name "/previous"] :stack-previous
                                        ["/" :name "/last"]     :stack-last
                                        ["/" :name "/compose"]  :stack-compose}
                   "/networks"         {""        :network-list
                                        "/create" :network-create
                                        ["/" :id] :network-info}
                   "/volumes"          {""          :volume-list
                                        "/create"   :volume-create
                                        ["/" :name] :volume-info}
                   "/secrets"          {""        :secret-list
                                        "/create" :secret-create
                                        ["/" :id] :secret-info}
                   "/configs"          {""        :config-list
                                        "/create" :config-create
                                        ["/" :id] :config-info}
                   "/nodes"            {""                :node-list
                                        ["/" :id]         :node-info
                                        ["/" :id "/edit"] :node-edit}
                   "/tasks"            {""        :task-list
                                        ["/" :id] :task-info}
                   "/registries"       {""        :registry-list
                                        "/create" :registry-create}
                   "/registries/"      {[:registryType] {["/" :id]         :registry-info
                                                         ["/" :id "/edit"] :registry-edit}}
                   "/users"            {""                :user-list
                                        "/create"         :user-create
                                        ["/" :id]         :user-info
                                        ["/" :id "/edit"] :user-edit}}])

(defn- path
  [routes prefix handler params query]
  (let [path (b/unmatch-pair routes {:handler handler
                                     :params  params})]
    (if (some? query)
      (str prefix path "?" (map->query query))
      (str prefix path))))

(defn path-for-frontend
  ([handler] (path-for-frontend handler {} nil))
  ([handler params] (path-for-frontend handler params nil))
  ([handler params query] (path frontend "#" handler params query)))

(defn path-for-backend
  ([handler] (path-for-backend handler {} nil))
  ([handler params] (path-for-backend handler params nil))
  ([handler params query] (str/replace (path backend "" handler params query) #"^/" "")))
