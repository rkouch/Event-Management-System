package tickr.mock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface IMockGenerator {
    <T> void addGeneratedValue (T entity);
    IMockGenerator copy ();

    static <T, V> IMockGenerator makeGenerator (Class<T> tClass, BiConsumer<T, V> generatedSetter, Function<Set<V>, V> generatorFunc, V start) {
        return new MockGenerator<>(tClass, generatedSetter, generatorFunc, start);
    }

    static <T, V extends Comparable<? super V>> Function<Set<V>, V> sequentialGenerator (Function<V, V> incrementFunc) {
        return s -> incrementFunc.apply(Collections.max(s));
    }

    static Function<Set<UUID>, UUID> uuidGenerator () {
        return s -> UUID.randomUUID();
    }

    class MockGenerator<T, V> implements IMockGenerator {
        private Class<T> tClass;
        private BiConsumer<T, V> generatedSetter;
        private Function<Set<V>, V> generatorFunc;

        private Set<V> previousValues;

        private V start;

        private MockGenerator (Class<T> tClass,  BiConsumer<T, V> generatedSetter, Function<Set<V>, V> generatorFunc, V start) {
            this.tClass = tClass;
            this.generatedSetter = generatedSetter;
            this.generatorFunc = generatorFunc;
            this.start = start;

            previousValues = new HashSet<>();
        }

        private MockGenerator (MockGenerator<T, V> generator) {
            this.tClass = generator.tClass;
            this.generatedSetter = generator.generatedSetter;
            this.generatorFunc = generator.generatorFunc;
            this.start = generator.start;

            this.previousValues = new HashSet<>(generator.previousValues);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> void addGeneratedValue (U entity) {
            if (!tClass.isAssignableFrom(entity.getClass())) {
                throw new RuntimeException("Invalid entity type!");
            }
            var entity2 = (T)entity;
            V newVal;
            if (previousValues.size() == 0) {
                newVal = start;
            } else {
                newVal = generatorFunc.apply(previousValues);
            }
            generatedSetter.accept(entity2, newVal);
            previousValues.add(newVal);
        }

        @Override
        public IMockGenerator copy () {
            return new MockGenerator<>(this);
        }
    }
}
