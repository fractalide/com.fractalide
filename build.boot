(set-env!
  :project 'fractalide
  :version "0.1.0-SNAPSHOT"
  :dependencies
  '[[hoplon/castra "3.0.0-alpha1"]
    [hoplon/boot-hoplon "0.1.9"]
    [hoplon/hoplon "6.0.0-alpha10"]
    [pandeiro/boot-http "0.6.3"]
    [org.clojure/clojurescript "1.7.48"]
    [adzerk/cljs-console "0.1.1"]
    [adzerk/boot-cljs "1.7.48-5"]
    [com.cognitect/transit-cljs "0.8.232"]
    [fractalide/boot-nix "0.1.0-SNAPSHOT"]
    [cljsjs/boot-cljsjs "0.5.0" :scope "test"]
    [exicon/semantic-ui "2.1.4-SNAPSHOT"]]
  :source-paths #{"src"}
  :resource-paths #{"assets"})

(require
  '[hoplon.boot-hoplon :refer [hoplon prerender html2cljs]]
  '[pandeiro.boot-http :refer [serve]]
  '[adzerk.boot-cljs :refer [cljs]]
  '[cljsjs.boot-cljsjs :refer [from-cljsjs]]
  '[fractalide.boot-nix :refer [nixos]]
  '[clojure.java.io :as io]
  '[clojure.pprint :refer :all]
  '[boot.util :refer [info]])

(task-options!
  cljs {:compiler-options
        {:pseudo-names false}})

(deftask copy-index-html
  [d dirs DIRS #{str} "Directories for the main index.html to be accessible under"]
  (with-pre-wrap
    fileset
    (let [_ (info "Copying index.html...\n")
          index-html* (->> fileset ls
                           (by-re [#"^index.html$"
                                   #"^index.html.js$"
                                   #"^index.html.out"]))
          copy-into (fn [subdir fs file]
                      (let [file-in-subdir (str subdir (tmp-path file))]
                        (cp fs (tmp-file file)
                            (assoc file :path file-in-subdir))))
          copy-index-html-into (fn [fs subdir]
                                 (info "• %s\n" subdir)
                                 (reduce (partial copy-into subdir)
                                         fs index-html*))]
      (commit! (reduce copy-index-html-into fileset dirs)))))

(deftask copy-index-htmls []
  (copy-index-html
    :dirs #{
            "about-us/"
            "account/"
            "C4/"
            "change-password/"
            "contact-us/"
            "docs/"
            "fargo/"
            "founder/"
            "fractalide/"
            "freedom/"
            "auth/"
            "fvm/"
            "news/"
            "component/"
            "non-programmer-cut-op-costs/"
            "non-programmer-quick-build/"
            "privacy-policy/"
            "programmer-cut-op-costs/"
            "programmer-quick-build/"
            "rustfbp/"
            "terms-of-use/"
            }))

(deftask dev
  "Build homepage for development."
  []
  (comp
    (serve :port 3000)
    (from-cljsjs :profile :development)
    (sift :to-resource #{#"themes"})
    (sift :to-resource #{#"semantic-ui.inc.css"})
    (sift :move {#"^semantic-ui.inc.css$" "semantic-ui.css"})
    (watch)
    (nixos)
    ;   (speak)
    (hoplon :pretty-print true)
    ; (reload)   ; Causes the `with-init` functions called infinitely
    (cljs :optimizations :none
          :source-map true)
    (copy-index-htmls)))

(deftask prod
  "Build homepage for production."
  []
  (comp
    (from-cljsjs :profile :production)
    (sift :to-resource #{#"themes"})
    (sift :to-resource #{#"semantic-ui.min.inc.css"})
    (sift :move {#"^semantic-ui.min.inc.css$" "semantic-ui.css"})
    (hoplon)
    (cljs :optimizations :advanced :source-map true)
    (copy-index-htmls)
    (prerender)
    #_(sift :invert true :include #{#"index.html.out/"})))

(deftask stg [] (prod))
