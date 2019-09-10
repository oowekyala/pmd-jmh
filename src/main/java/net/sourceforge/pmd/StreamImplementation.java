package net.sourceforge.pmd;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.ast.internal.SmallStreamImpl;
import net.sourceforge.pmd.lang.ast.internal.StreamImpl;


/**
 * @author Clément Fournier
 */
public enum StreamImplementation {
/*

Throughput:
OPTIMAL = 3 * ITERATOR, ITERATOR = 3 * STREAM

StreamBench.childFirst  STREAM      /PLSQLParser.java  thrpt    3  4295,710 ± 251,929  ops/s
StreamBench.toList      STREAM      /PLSQLParser.java  thrpt    3  3683,646 ± 398,946  ops/s
StreamBench.childFirst  ITERATOR  /PLSQLParser.java  thrpt    3  6470,152 ±  221,586  ops/s
StreamBench.toList      ITERATOR  /PLSQLParser.java  thrpt    3  6352,169 ±  371,971  ops/s
StreamBench.childFirst  OPTIMAL   /PLSQLParser.java  thrpt    3  9657,104 ± 1539,569  ops/s
StreamBench.toList      OPTIMAL   /PLSQLParser.java  thrpt    3  9324,981 ±  899,448  ops/s

 */

    /** Just iterators for singleton streams. */
    ITERATOR {
        @Override
        public <R extends Node> @Nullable Node firstChild(Node n, Class<R> target) {
            return SmallStreamImpl.children(n, target).first();
        }


        @Override
        public <R extends Node> int countChildren(Node n, Class<R> target) {
            return SmallStreamImpl.children(n, target).count();
        }


        @Override
        public <R extends Node> boolean childrenIsEmpty(Node n, Class<R> target) {
            return SmallStreamImpl.children(n, target).isEmpty();
        }


        @Override
        public <R extends Node> List<R> childrenList(Node n, Class<R> target) {
            return SmallStreamImpl.children(n, target).toList();
        }
    },

    /** Iterators + some hand-written optimised implementations. */
    OPTIMAL {
        @Override
        public <R extends Node> @Nullable Node firstChild(Node n, Class<R> target) {
            return StreamImpl.children(n, target).first();
        }


        @Override
        public <R extends Node> int countChildren(Node n, Class<R> target) {
            return StreamImpl.children(n, target).count();
        }


        @Override
        public <R extends Node> boolean childrenIsEmpty(Node n, Class<R> target) {
            return StreamImpl.children(n, target).isEmpty();
        }


        @Override
        public <R extends Node> List<R> childrenList(Node n, Class<R> target) {
            return StreamImpl.children(n, target).toList();
        }
    },

    /** Just the default methods of the interface. */
    STREAM {
        private <T extends Node> NodeStream<T> deopt(NodeStream<T> ns) {
            return ns::toStream;
        }


        @Override
        public <R extends Node> @Nullable Node firstChild(Node n, Class<R> target) {
            return deopt(StreamImpl.children(n, target)).first();
        }


        @Override
        public <R extends Node> int countChildren(Node n, Class<R> target) {
            return deopt(StreamImpl.children(n, target)).count();
        }


        @Override
        public <R extends Node> boolean childrenIsEmpty(Node n, Class<R> target) {
            return deopt(StreamImpl.children(n, target)).isEmpty();
        }


        @Override
        public <R extends Node> List<R> childrenList(Node n, Class<R> target) {
            return deopt(StreamImpl.children(n, target)).toList();
        }
    },
    ITER_RAW {
        @Override
        public <R extends Node> @Nullable Node firstChild(Node n, Class<R> target) {
            return SmallStreamImpl.children(n).first();
        }


        @Override
        public <R extends Node> int countChildren(Node n, Class<R> target) {
            return SmallStreamImpl.children(n).count();
        }


        @Override
        public <R extends Node> boolean childrenIsEmpty(Node n, Class<R> target) {
            return SmallStreamImpl.children(n).isEmpty();
        }


        @Override
        public <R extends Node> List<Node> childrenList(Node n, Class<R> target) {
            return SmallStreamImpl.children(n).toList();
        }

    },

    OPT_RAW {
        @Override
        public <R extends Node> @Nullable Node firstChild(Node n, Class<R> target) {
            return StreamImpl.children(n, target).first();
        }


        @Override
        public <R extends Node> int countChildren(Node n, Class<R> target) {
            return StreamImpl.children(n, target).count();
        }


        @Override
        public <R extends Node> boolean childrenIsEmpty(Node n, Class<R> target) {
            return StreamImpl.children(n, target).isEmpty();
        }


        @Override
        public <R extends Node> List<R> childrenList(Node n, Class<R> target) {
            return StreamImpl.children(n, target).toList();
        }


    };


    abstract <R extends Node> @Nullable Node firstChild(Node n, Class<R> target);


    abstract <R extends Node> int countChildren(Node n, Class<R> target);


    abstract <R extends Node> boolean childrenIsEmpty(Node n, Class<R> target);


    abstract <R extends Node> List<? extends Node> childrenList(Node n, Class<R> target);
}
