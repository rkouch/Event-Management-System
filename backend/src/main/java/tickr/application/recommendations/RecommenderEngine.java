package tickr.application.recommendations;

import tickr.application.entities.DocumentTerm;
import tickr.application.entities.Event;
import tickr.application.entities.TfIdf;
import tickr.persistence.ModelSession;

import java.util.HashMap;
import java.util.stream.Collectors;

public class RecommenderEngine {
    public static void forceRecalculate (ModelSession session) {
        session.clear(DocumentTerm.class);
        session.clear(TfIdf.class);

        calculateTfIdfs(session);
    }

    private static void calculateTfIdfs (ModelSession session) {
        var docTerms = new HashMap<String, DocumentTerm>();
        for (var i : session.getAll(Event.class)) {
            var wordCounts = i .getWordCounts();
            //var totalWords = wordCounts.values().stream().mapToInt(Long::intValue).sum();

            for (var j : wordCounts.entrySet()) {
                DocumentTerm term;
                if (docTerms.containsKey(j.getKey())) {
                    term = docTerms.get(j.getKey());
                } else {
                    term = new DocumentTerm(j.getKey(), 0);
                    session.save(term);
                    docTerms.put(j.getKey(), term);
                }

                term.incrementCount();

                var tfIdf = new TfIdf(term, i, j.getValue().intValue());
                session.save(tfIdf);
            }
        }
    }
}
