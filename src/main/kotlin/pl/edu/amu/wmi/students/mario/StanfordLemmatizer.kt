package pl.edu.amu.wmi.students.mario

import edu.stanford.nlp.ling.CoreAnnotations.*
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import java.util.*

class StanfordLemmatizer {

    protected var pipeline: StanfordCoreNLP

    init {
        // Create StanfordCoreNLP object properties, with POS tagging
        // (required for lemmatization), and lemmatization
        val props: Properties
        props = Properties()
        props.put("annotators", "tokenize, ssplit, pos, lemma")

        /*
         * This is a pipeline that takes in a string and returns various analyzed linguistic forms.
         * The String is tokenized via a tokenizer (such as PTBTokenizerAnnotator),
         * and then other sequence model style annotation can be used to add things like lemmas,
         * POS tags, and named entities. These are returned as a list of CoreLabels.
         * Other analysis components build and store parse trees, dependency graphs, etc.
         *
         * This class is designed to apply multiple Annotators to an Annotation.
         * The idea is that you first build up the pipeline by adding Annotators,
         * and then you take the objects you wish to annotate and pass them in and
         * get in return a fully annotated object.
         *
         *  StanfordCoreNLP loads a lot of models, so you probably
         *  only want to do this once per execution
         */
        this.pipeline = StanfordCoreNLP(props)
    }

    fun lemmatize(documentText: String): List<String> {
        val lemmas = LinkedList<String>()
        // Create an empty Annotation just with the given text
        val document = Annotation(documentText)
        // run all Annotators on this text
        this.pipeline.annotate(document)
        // Iterate over all of the sentences found
        val sentences = document.get(SentencesAnnotation::class.java)
        for (sentence in sentences) {
            // Iterate over all tokens in a sentence
            for (token in sentence.get(TokensAnnotation::class.java)) {
                // Retrieve and add the lemma for each word into the
                // list of lemmas
                lemmas.add(token.get(LemmaAnnotation::class.java))
            }
        }
        return lemmas
    }

}