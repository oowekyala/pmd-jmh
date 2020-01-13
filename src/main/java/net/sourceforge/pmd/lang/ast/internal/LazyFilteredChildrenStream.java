package net.sourceforge.pmd.lang.ast.internal;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.internal.util.AssertionUtil;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.NodeStream;


/**
 * This is the implementation before
 */
class LazyFilteredChildrenStream<T extends Node> extends AxisStream<T> {

    final int low; // inclusive
    final int len;

    LazyFilteredChildrenStream(@NonNull Node root, Filtermap<Node, T> filtermap, int low, int len) {
        super(root, filtermap);
        this.low = low;
        this.len = len;
    }

    LazyFilteredChildrenStream(Node root, Filtermap<Node, T> filtermap) {
        this(root, filtermap, 0, root.jjtGetNumChildren());
    }


    @Override
    protected <S extends Node> NodeStream<S> copyWithFilter(Filtermap<Node, S> filterMap) {
        return new FilteredChildrenStream<>(node, filterMap, low, len);
    }

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliterator(iterator(), count(), Spliterator.SIZED | Spliterator.ORDERED);
    }

    @Override
    protected Iterator<Node> baseIterator() {
        return TraversalUtils.childrenIterator(node, low, low + len);
    }

    @Override
    public @Nullable T first() {
        return TraversalUtils.getFirstChildMatching(node, filter, low, len);
    }

    @Override
    public @Nullable T last() {
        return TraversalUtils.getLastChildMatching(node, filter, low, len);
    }


    @Override
    public <R extends Node> @Nullable R first(Class<R> rClass) {
        return TraversalUtils.getFirstChildMatching(node, filter.thenCast(rClass), low, len);
    }

    @Override
    public <R extends Node> @Nullable R last(Class<R> rClass) {
        return TraversalUtils.getLastChildMatching(node, filter.thenCast(rClass), low, len);
    }

    @Override
    public int count() {
        return TraversalUtils.countChildrenMatching(node, filter, low, len);
    }

    @Override
    public boolean nonEmpty() {
        return first() != null;
    }

    @Override
    public List<T> toList() {
        return TraversalUtils.findChildrenMatching(node, filter, low, len);
    }


    @Override
    public NodeStream<T> take(int maxSize) {
        AssertionUtil.requireNonNegative("maxSize", maxSize);
        return StreamImpl.sliceChildren(node, filter, low, min(maxSize, len));
    }

    @Override
    public NodeStream<T> drop(int n) {
        AssertionUtil.requireNonNegative("n", n);
        int newLow = min(low + n, node.jjtGetNumChildren());
        int newLen = max(len - n, 0);

        return n == 0 ? this : StreamImpl.sliceChildren(node, filter, newLow, newLen);
    }

    @Override
    public String toString() {
        return "Slice[" + node + ", " + low + ".." + (low + len) + "] -> " + toList();
    }
}

