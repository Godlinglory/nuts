/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.stream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorUtils;
import net.thevpc.nuts.util.NDescribables;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * @param <T> Type
 * @author thevpc
 */
public abstract class AbstractNStream<T> implements NStream<T> {

    protected NSession session;
    protected String nutsBase;

    public AbstractNStream(NSession session, String nutsBase) {
        this.session = session;
        this.nutsBase = nutsBase;
    }


    private static <T> BinaryOperator<T> throwingMerger() {
        return (u, v) -> {
            throw new IllegalStateException(String.format("duplicate key %s", u));
        };
    }

    @Override
    public List<T> toList() {
        List<T> list = new ArrayList<>();
        for (T a : this) {
            list.add(a);
        }
        return list;
    }

    @Override
    public Set<T> toSet() {
        return stream().collect(Collectors.toSet());
    }

    @Override
    public Set<T> toSortedSet() {
        return stream().collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public Set<T> toOrderedSet() {
        return stream().collect(Collectors.toCollection(LinkedHashSet::new));
    }


    @Override
    public NOptional<T> findLast() {
        T t = null;
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            t = it.next();
        }
        return NOptional.ofEmpty(s ->
                nutsBase == null ?
                        NMsg.ofPlain("missing last") :
                        NMsg.ofC("missing last %s", nutsBase)
        );
    }

    @Override
    public NOptional<T> findSingleton() {
        Iterator<T> it = iterator();
        if (it.hasNext()) {
            T t = it.next();
            if (it.hasNext()) {
                return NOptional.ofError(
                        s->NMsg.ofC("too many results for %s", nutsBase),
                        new NTooManyElementsException(session, NMsg.ofC("too many results for %s", nutsBase))
                );
            }
            return NOptional.of(t);
        } else {
            return NOptional.ofEmpty(s ->
                    nutsBase == null ?
                            NMsg.ofPlain("missing") :
                            NMsg.ofC("missing %s", nutsBase)
            );
        }
    }

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
    }

    @Override
    public long count() {
        long count = 0;
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }

    @Override
    public <R> NStream<R> map(NFunction<? super T, ? extends R> mapper) {
        return new AbstractNStream<R>(session, nutsBase) {
            @Override
            public NIterator<R> iterator() {
                NIterator<T> it = AbstractNStream.this.iterator();
                return (NIterator) IteratorBuilder.of(it, session).map(mapper).build();
            }
        };
    }

    @Override
    public <R> NStream<R> map(Function<? super T, ? extends R> mapper, String name) {
        return map(NFunction.of(mapper, name));
    }

    @Override
    public <R> NStream<R> map(Function<? super T, ? extends R> mapper, NElement name) {
        return map(NFunction.of(mapper, name));
    }

    @Override
    public <R> NStream<R> map(Function<? super T, ? extends R> mapper, Function<NSession, NElement> name) {
        return map(NFunction.of(mapper, name));
    }

    @Override
    public <R> NStream<R> mapUnsafe(NUnsafeFunction<? super T, ? extends R> mapper, NFunction<Exception, ? extends R> onError) {
        return map(new NFunction<T, R>() {
            @Override
            public R apply(T t) {
                try {
                    return mapper.apply(t);
                } catch (Exception e) {
                    return onError == null ? null : onError.apply(e);
                }
            }

            @Override
            public NElement describe(NSession session) {
                return mapper.describe(session);
            }
        });
    }

    @Override
    public NStream<T> sorted() {
        return new AbstractNStream<T>(session, nutsBase) {
            @Override
            public NIterator<T> iterator() {
                NIterator<T> it = AbstractNStream.this.iterator();
                return IteratorUtils.sort(it, null, false);
            }
        };
    }

    @Override
    public NStream<T> sorted(NComparator<T> comp) {
        return new AbstractNStream<T>(session, nutsBase) {
            @Override
            public NIterator<T> iterator() {
                NIterator<T> it = AbstractNStream.this.iterator();
                return IteratorUtils.sort(it, comp, false);
            }
        };
    }

    @Override
    public NStream<T> distinct() {
        return new AbstractNStream<T>(session, nutsBase) {
            @Override
            public NIterator<T> iterator() {
                NIterator<T> it = AbstractNStream.this.iterator();
                return IteratorUtils.distinct(it);
            }
        };
    }

    @Override
    public <R> NStream<T> distinctBy(NFunction<T, R> condition) {
        return new AbstractNStream<T>(session, nutsBase) {
            @Override
            public NIterator<T> iterator() {
                NIterator<T> it = AbstractNStream.this.iterator();
                return IteratorUtils.distinct(it, condition);
            }
        };
    }

    @Override
    public NStream<T> nonNull() {
        return filter(Objects::nonNull, "nonNull");
    }

    @Override
    public NStream<T> nonBlank() {
        return filter(x -> {
            if (x == null) {
                return false;
            }
            if (x instanceof CharSequence) {
                return NBlankable.isBlank((CharSequence) x);
            }
            if (x instanceof char[]) {
                return NBlankable.isBlank((char[]) x);
            }
            if (x instanceof NBlankable) {
                return !((NBlankable) x).isBlank();
            }
            return true;
        }, "nonBlank");
    }

    @Override
    public NStream<T> filter(NPredicate<? super T> predicate) {
        NDescribables.cast(predicate);
        return new AbstractNStream<T>(session, nutsBase) {
            @Override
            public NIterator<T> iterator() {
                NIterator<T> it = AbstractNStream.this.iterator();
                return IteratorBuilder.of(it, session).filter(predicate).build();//,"mapped("+it+")"
            }
        };
    }

    @Override
    public NStream<T> filter(Predicate<? super T> predicate, String name) {
        return filter(predicate, e -> NElements.of(e).ofString(name));
    }

    @Override
    public NStream<T> filter(Predicate<? super T> predicate, NElement name) {
        return filter(predicate, e -> name);
    }

    @Override
    public NStream<T> filter(Predicate<? super T> predicate, Function<NSession, NElement> info) {
        NPredicate<? super T> p = predicate == null ? null : NPredicate.of(predicate, info);
        return new AbstractNStream<T>(session, nutsBase) {
            @Override
            public NIterator<T> iterator() {
                NIterator<T> it = AbstractNStream.this.iterator();
                return IteratorBuilder.of(it, session).filter(p).build();//,"mapped("+it+")"
            }
        };
    }

    @Override
    public NStream<T> filterNonNull() {
        return filter(Objects::nonNull, "nonNull");
    }

    @Override
    public NStream<T> filterNonBlank() {
        return filter(x -> !NBlankable.isBlank(x), "nonBlank");
    }

    @Override
    public NStream<T> coalesce(NIterator<? extends T> other) {
        return new AbstractNStream<T>(session, nutsBase) {
            @Override
            public NIterator<T> iterator() {
                NIterator<T> it = AbstractNStream.this.iterator();
                List<NIterator<? extends T>> iterators = Arrays.asList(it, other);
                return IteratorUtils.coalesce(iterators);//,"mapped("+it+")"
            }
        };
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return stream().toArray(generator);
    }

    @Override
    public <K, U> Map<K, U> toMap(Function<? super T, ? extends K> keyMapper,
                                  Function<? super T, ? extends U> valueMapper) {
        return stream().collect(Collectors.toMap(keyMapper, valueMapper));
    }

    @Override
    public <K, U> Map<K, U> toOrderedMap(Function<? super T, ? extends K> keyMapper,
                                         Function<? super T, ? extends U> valueMapper) {
        return stream().collect(Collectors.toMap(keyMapper, valueMapper, throwingMerger(), LinkedHashMap::new));
    }

    @Override
    public <K, U> Map<K, U> toSortedMap(Function<? super T, ? extends K> keyMapper,
                                        Function<? super T, ? extends U> valueMapper) {
        return stream().collect(Collectors.toMap(keyMapper, valueMapper, throwingMerger(), TreeMap::new));
    }

    @Override
    public <R> NStream<R> flatMapIter(NFunction<? super T, ? extends Iterator<? extends R>> mapper) {
        return new AbstractNStream<R>(session, nutsBase) {
            @Override
            public NIterator<R> iterator() {
                return IteratorBuilder.of(AbstractNStream.this.iterator(), session).flatMap(mapper).build();
            }
        };
    }

    @Override
    public <R> NStream<R> flatMapList(NFunction<? super T, ? extends List<? extends R>> mapper) {
        return new AbstractNStream<R>(session, nutsBase) {
            @Override
            public NIterator<R> iterator() {
                IteratorBuilder<T> r = IteratorBuilder.of(AbstractNStream.this.iterator(), session);
                return (NIterator<R>) r.flatMap(
                        NFunction.of(t -> mapper.apply(t).iterator(), mapper::describe)
                ).build();
            }
        };
    }

    @Override
    public <R> NStream<R> flatMapArray(NFunction<? super T, ? extends R[]> mapper) {
        return new AbstractNStream<R>(session, nutsBase) {
            @Override
            public NIterator<R> iterator() {
                return IteratorBuilder.of(AbstractNStream.this.iterator(), session)
                        .flatMap(
                                NFunction.of(t -> Arrays.asList(mapper.apply(t)).iterator(), mapper::describe)
                        ).build();
            }
        };
    }

    @Override
    public <R> NStream<R> flatMap(NFunction<? super T, ? extends Stream<? extends R>> mapper) {
        return new AbstractNStream<R>(session, nutsBase) {
            @Override
            public NIterator<R> iterator() {
                return (NIterator<R>) IteratorBuilder.of(AbstractNStream.this.iterator(), session).flatMap(
                        NFunction.of(t -> mapper.apply(t).iterator(), mapper::describe)
                ).build();
            }
        };
    }

    @Override
    public <R> NStream<R> flatMapStream(NFunction<? super T, ? extends NStream<? extends R>> mapper) {
        return new AbstractNStream<R>(session, nutsBase) {
            @Override
            public NIterator<R> iterator() {
                return (NIterator<R>) IteratorBuilder.of(AbstractNStream.this.iterator(), session)
                        .flatMap(
                                NFunction.of(t -> mapper.apply(t).iterator(), mapper::describe)
                        ).build();
            }
        };
    }

    @Override
    public <K> Map<K, List<T>> groupBy(NFunction<? super T, ? extends K> classifier) {
        Stream<T> it = AbstractNStream.this.stream();
        return it.collect(Collectors.groupingBy(classifier));
    }

    @Override
    public <K> NStream<Map.Entry<K, List<T>>> groupedBy(NFunction<? super T, ? extends K> classifier) {
        Stream<T> it = AbstractNStream.this.stream();
        Set<Map.Entry<K, List<T>>> entries = (Set) it.collect(Collectors.groupingBy(classifier)).entrySet();
        return new NIteratorStream<Map.Entry<K, List<T>>>(
                session, nutsBase, NIterator.of(entries.iterator(),
                e -> NElements.of(e).ofObject()
                        .set("type", "GroupBy")
                        .set("groupBy", classifier.describe(e))
                        .set("base", iterator().describe(e))
                        .build()
        )
        );
    }

    @Override
    public NOptional<T> findAny() {
        return NOptional.ofOptional(stream().findAny(), s -> NMsg.ofC("missing : %s", nutsBase));
    }

    @Override
    public NOptional<T> findFirst() {
        Iterator<T> it = iterator();
        if (it.hasNext()) {
            return NOptional.of(it.next());
        }
        return NOptional.ofEmpty(s ->
                nutsBase == null ?
                        NMsg.ofPlain("missing first") :
                        NMsg.ofC("missing first %s", nutsBase)
        );
    }

    @Override
    public DoubleStream flatMapToDouble(NFunction<? super T, ? extends DoubleStream> mapper) {
        return stream().flatMapToDouble(mapper);
    }

    @Override
    public IntStream flatMapToInt(NFunction<? super T, ? extends IntStream> mapper) {
        return stream().flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(NFunction<? super T, ? extends LongStream> mapper) {
        return stream().flatMapToLong(mapper);
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return stream().allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return stream().noneMatch(predicate);
    }

    @Override
    public NStream<T> limit(long maxSize) {
        return NStream.of(stream().limit(maxSize), session);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return stream().collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return stream().collect(collector);
    }

    @Override
    public NOptional<T> min(Comparator<? super T> comparator) {
        return NOptional.ofOptional(stream().min(comparator), s -> NMsg.ofC("missing : %s", nutsBase));
    }

    @Override
    public NOptional<T> max(Comparator<? super T> comparator) {
        return NOptional.ofOptional(stream().max(comparator), s -> NMsg.ofC("missing : %s", nutsBase));
    }

    @Override
    public NElement describe(NSession session) {
        return iterator().describe(session);
    }
}
