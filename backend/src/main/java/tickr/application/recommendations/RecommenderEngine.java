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

    public static double calculateUserScore (ModelSession session, Event event, EventVector userProfile) {
        int numEvents = session.getAll(Event.class).size(); // TODO
        var tagIdf = getTagIdf(session, numEvents);
        var categoryIdf = getCategoryIdf(session, numEvents);

        var userVector = userProfile.applyIdfs(tagIdf, categoryIdf);
        var eventVector = event.getEventVector(numEvents).applyIdfs(tagIdf, categoryIdf);

        return userProfile.combine(eventVector, 0.0).dotProduct(WEIGHT_VECTOR);
    }


    private static Vector buildSimilarityVector (ModelSession session, Event e1, Event e2) {
        int numEvents = session.getAll(Event.class).size(); // TODO
        var e1Vec = e1.getEventVector(numEvents);
        var e2Vec = e2.getEventVector(numEvents);

        var tagIdf = getTagIdf(session, numEvents);
        var categoryIdf = getCategoryIdf(session, numEvents);

        e1Vec = e1Vec.applyIdfs(tagIdf, categoryIdf);
        e2Vec = e2Vec.applyIdfs(tagIdf, categoryIdf);

        var invDistance = 1.0 / (e1.getDistance(e2) + 1);

        return e1Vec.combine(e2Vec, invDistance);
    }

    public static EventVector buildUserProfile (ModelSession session, User user) {
        int numEvents = session.getAll(Event.class).size(); // TODO
        var profile = EventVector.identity();
        for (var i : user.getInteractions()) {
            profile = profile.add(i.getVector(numEvents));
        }

        return profile.normalise();
    }

    public static void recordInteraction (ModelSession session, User user, Event event, InteractionType type) {
        assert type != InteractionType.REVIEW;
        var interaction = new UserInteraction(user, event, type, null);
        session.save(interaction);
    }

    public static void recordRating (ModelSession session, User user, Event event, double rating) {
        var interaction = new UserInteraction(user, event, InteractionType.REVIEW, rating);
        session.save(interaction);
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
}
