Neologism Recommender
=====================

This repository contains the recommender used for neologism2. It is used for both class and property recommendations.

Recommending classes
---------------------

* Classes are recommended based on a search string and a serialization of the graph in which the class occurs (the context)
	* Currently, that context is only used for some recommenders
* The request is dealt with by multiple internal recommenders that can:
	* Search in local vocabularies 
	    * (DCAT and Dublin Core are pre-configured)
	* Proxies the query to bioportal http://data.bioontology.org/documentation#nav_recommender
	* Proxies the query to the LOV recommender https://lov.linkeddata.es/dataset/lov/api/v2 
	    * Currently term/search is used. Likely term/autocomplete is also a good option
	* Query a given SPARQL endpoint to see whether there are classes used there that contain the search string
        * We use a triple store located on an internal server for this. This can be configured.

It is also possible to add more proxies, more local vocabularies, and more endpoints.




Now, these different backend recommenders have different speeds. The local vocabulary recommender precomputes all answers iand is hence near instant. Others take longer. To get quick results for the application using this system a couple of measures are taken:

Results are cached, also the context is cached so that things do not have to get parsed again.
REquests will timeout if downstream recommenders are too slow.

Results are provided in chunks. this works as follows:
* First request:
    * `/start/`
        * parameter: model (see below)
        * Example request: http://localhost:8080/recommender/start/?model=%40prefix%20ex%3A%20%3Chttp%3A%2F%2Fexample.com%2F%3E%20.%0Aex%3ALion%20a%20ex%3AAnimal%20.%0Aex%3AHouseCat%20a%20ex%3AAnimal%20.%0A%3Cneo%3A%2F%2Fquery%2Fjag%3E%20a%20ex%3AAnimal%20.%0A
        * This returns a json obect with 
            * and ID: `ID`
            * a first part of the recommendations `recommendation`
            * how many more parts are to be expected `expected`
            * a variable `more` indicating wheter more is yet to come.
        * the recommendation itself contains recommendation objects. Each of these has:
            * labels and comments(including language tags)
            * `URI` the identifier of the recommended class
            * `ontology` an indication of the vocabulary from which this is sourced
        * the recommendation also contains a creator. This can be useful later when requesting properties for the the class
        * For example:

        {
        "ID":"7E09404FF6EB45603F6171ED509A26B3",
        "recommendation":{
            "list":[
                {
                    "labels":[
                    {
                        "language":"en",
                        "label":"Dataset"
                    }
                    ],
                    "comments":[
                    {
                        "language":"en",
                        "label":"Data encoded in a defined structure."
                    }
                    ],
                    "URI":"http://purl.org/dc/dcmitype/Dataset",
                    "ontology":"DCAT"
                },
                {
                    "labels":[
                    {
                        "language":"en",
                        "label":"Dataset"
                    }
                    ],
                    "comments":[
                    {
                        "language":"en",
                        "label":"A collection of data, published or curated by a single source, and available for access or download in one or more formats"
                    }
                    ],
                    "URI":"http://www.w3.org/ns/dcat#Dataset",
                    "ontology":"DCAT"
                }
            ],
            "creator":"de.rwth.dbis.neologism.recommender.localVoc.LocalVocabLoaderDCATa49e3f1372ec6d12368e17cdd3c53698"
        },
        "expected":4,
        "more":true
        }

* Next, a subsequent requests can be performed to get the remaining results
    * `/more/` 
        * parameter: ID - the ID as provided in the answer to `/start/`.
        * Example request: http://localhost:8080/recommender/?ID=7E09404FF6EB45603F6171ED509A26B3
        * this returns a JSON object containing a recommendation as above if there are still any left and an indicator `more` indicating whether there is more to come.

###Input to the recommender###

The *model* parameter

The recommender expects two pieces of information. The searchstring and a serialization of the graph in which the class occurs (the context)
    * the model is a serialized graph in turtle format. 
    * The place in the graph were the node occurs has the special form `<neo://query/searchstring>` where searchstring is replaced by the recommenderInput input

For example, given the following graph, where the recommenderInput is askign a recommendation for the string "jag"

<img src="assets/searchcontext1.png"></img>

The graph send to the recommender can be serialized as:

    @prefix ex: <http://example.com/> .
    ex:Lion a ex:Animal .
    ex:HouseCat a ex:Animal .
    <neo://query/jag> a ex:Animal .

Note that we use turtle serialization, so we can write `a` instead of `rdf:type`.
The query string is embedded in the graph. As in a normal RDF graph serialization, it could occur multiple times if more relations are defined.
If used as a GET request, the graph needs to be URL encoded: %40prefix%20ex%3A%20%3Chttp%3A%2F%2Fexample.com%2F%3E%20.%0Aex%3ALion%20a%20ex%3AAnimal%20.%0Aex%3AHouseCat%20a%20ex%3AAnimal%20.%0A%3Cneo%3A%2F%2Fquery%2Fjag%3E%20a%20ex%3AAnimal%20.%0A

In case the class is not yet connected to anything in the graph, it is also possible to send the model and the searchstriong separately.
For this case, the model argument is provided as a POST body. For the normal request, this is not implemented, yet.


Internals
---------

* Currently, comments and labels with non-english language tags are filtered out right before the answer is send to the client.
