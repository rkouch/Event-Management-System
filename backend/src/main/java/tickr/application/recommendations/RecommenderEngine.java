package tickr.application.recommendations;

import tickr.application.entities.*;
import tickr.persistence.ModelSession;
import tickr.util.Utils;

import java.util.*;
import java.util.stream.Collectors;

public class RecommenderEngine {
    private static final double TERM_WEIGHT = 1;
    private static final double TAG_WEIGHT = 0.4;
    private static final double CATEGORY_WEIGHT = 0.6;
    private static final double HOST_WEIGHT = 0.7;
    private static final double DISTANCE_WEIGHT = 1.5;

    private static final Vector WEIGHT_VECTOR = new Vector(List.of(TERM_WEIGHT, TAG_WEIGHT, CATEGORY_WEIGHT, HOST_WEIGHT, DISTANCE_WEIGHT))
            .normalised();

    public static void forceRecalculate (ModelSession session) {
        session.clear(TfIdf.class);
        session.clear(DocumentTerm.class);

        calculateTfIdfs(session);
    }

    private static void calculateTfIdfs (ModelSession session) {
        var docTerms = new HashMap<String, DocumentTerm>();
        for (var i : session.getAll(Event.class)) {
            var wordCounts = i .getWordCounts();
            //var totalWords = wordCounts.values().stream().mapToInt(Long::intValue).sum();

            var tfidfs = new ArrayList<TfIdf>();
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
                tfidfs.add(tfIdf);
            }

            i.setTfIdfs(tfidfs);
        }
    }

    public static double calculateSimilarity (ModelSession session, Event e1, Event e2) {
        if (e1.equals(e2)) {
            return 1;
        }
        var similarityVector = buildSimilarityVector(session, e1, e2);

        return similarityVector.dotProduct(WEIGHT_VECTOR);
    }


    private static Vector buildSimilarityVector (ModelSession session, Event e1, Event e2) {
        int numEvents = session.getAll(Event.class).size(); // TODO
        var e1TermVec = e1.getTfIdfVector(numEvents);
        var e2TermVec = e2.getTfIdfVector(numEvents);
        var termTfIdf = e1TermVec.dot(e2TermVec);

        var tagIdf = getTagIdf(session, numEvents);
        var e1TagVec = e1.getTagVector().cartesianProduct(tagIdf).normalised();
        var e2TagVec = e2.getTagVector().cartesianProduct(tagIdf).normalised();
        var tagTfIdf = e1TagVec.dot(e2TagVec);

        var categoryIdf = getCategoryIdf(session, numEvents);
        var e1CategoryVec = e1.getCategoryVector().cartesianProduct(categoryIdf).normalised();
        var e2CategoryVec = e2.getCategoryVector().cartesianProduct(categoryIdf).normalised();
        var categoryTfIdf = e1TagVec.dot(e2TagVec);

        var hostTfIdf = getHostTfIdf(session, e1, e2, numEvents);
        var invDistance = 1.0 / (e1.getDistance(e2) + 1);

        return new Vector(List.of(termTfIdf, tagTfIdf, categoryTfIdf, hostTfIdf, invDistance));
    }

    private static SparseVector<String> getTagIdf (ModelSession session, int numEvents) {
        var tagMap = session.getAllStream(Tag.class)
                .map(Tag::getTags)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        var entries = new ArrayList<>(tagMap.entrySet());

        return new SparseVector<>(entries.stream().map(Map.Entry::getKey).collect(Collectors.toList()),
                entries.stream().map(Map.Entry::getValue).map(x -> Utils.getIdf(x.intValue(), numEvents)).collect(Collectors.toList()))
                .normalised();
    }

    private static SparseVector<String> getCategoryIdf (ModelSession session, int numEvents) {
        var categoryMap = session.getAllStream(Category.class)
                .map(Category::getCategory)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        var entries = new ArrayList<>(categoryMap.entrySet());

        return new SparseVector<>(entries.stream().map(Map.Entry::getKey).collect(Collectors.toList()),
                entries.stream().map(Map.Entry::getValue).map(x -> Utils.getIdf(x.intValue(), numEvents)).collect(Collectors.toList()))
                .normalised();
    }

    private static double getHostTfIdf (ModelSession session, Event e1, Event e2, int numEvents) {
        if (!e1.getHost().getId().equals(e2.getHost().getId())) {
            return 0;
        }

        int numEventsHosted = e1.getHost().getHostingEvents().size();

        return Utils.getIdf(numEventsHosted, numEvents);
    }
}
