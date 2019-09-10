/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.sourceforge.pmd;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Timeout;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersionHandler;
import net.sourceforge.pmd.lang.Parser;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.ast.internal.SmallStreamImpl;
import net.sourceforge.pmd.lang.ast.internal.StreamImpl;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTBlockStatement;
import net.sourceforge.pmd.lang.java.ast.ASTIfStatement;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTStatement;


@BenchmarkMode(Mode.Throughput)
@Fork(value = 1)
@Measurement(iterations = 3)
@Warmup(iterations = 2)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Timeout(time = 15)
public class StreamBench {


    @SuppressWarnings("unchecked")
    private static final Class<? extends Node>[] types = new Class[]{
        ASTIfStatement.class,
        ASTStatement.class,
        ASTBlock.class,
        ASTBlockStatement.class,
        ASTStatement.class
    };

    @Benchmark
    public void iterImpl(Blackhole bh, ParserState state) {
        state.bench(bh, (i, n) -> makeStream(state.pipelineLength, n, (nodes, aClass) -> nodes.flatMap(it -> StreamImpl.children(it, aClass))));
    }

    @Benchmark
    public void streamImpl(Blackhole bh, ParserState state) {
        state.bench(bh, (i, n) -> makeStream(state.pipelineLength, n, (nodes, aClass) -> nodes.flatMap(it -> SmallStreamImpl.children(it, aClass))));
    }


    private Iterable<?> makeStream(int len, Node n, BiFunction<NodeStream<? extends Node>, Class<? extends Node>, NodeStream<? extends Node>> children) {
        NodeStream<? extends Node> s = n.asStream();

        for (int k = 0; k < len; k++) {
            s = children.apply(s, types[k % types.length]);
        }

        return s;
    }

//
//    @Benchmark
//    public void isEmpty(Blackhole bh, ParserState state) {
//        state.bench(bh, (i, n) -> i.childrenIsEmpty(n, ASTBreakStatement.class));
//    }
    //
    //    @Benchmark
    //    public void toList(Blackhole bh, ParserState state) {
    //        state.bench(bh, (i, n) -> i.childrenList(n, Node.class));
    //    }



    /*

        @Override
        public @Nullable R first() {
            return TraversalUtils.getFirstChildOfType(target, node);
        }

        @Override
        public int count() {
            return TraversalUtils.countChildrenOfType(target, node);
        }

        @Override
        public boolean nonEmpty() {
            return TraversalUtils.getFirstChildOfType(target, node) != null;
        }

        @Override
        public List<R> toList() {
            return TraversalUtils.findChildrenOfType(target, node);
        }
     */


    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder().include(StreamBench.class.getName()).build();
        new Runner(options).run();
    }


    @State(Scope.Benchmark)
    public static class ParserState {

        Parser newParser;
        //        @Param({"OPTIMAL", "OPT_RAW"})
        StreamImplementation impl = StreamImplementation.OPTIMAL;
        @Param({"/PLSQLParser.java"})
        String sourceFname;
        @Param({"1", "2", "4", "8", "16"})
        int pipelineLength;
        private Reader source;
        private Node acu;
        private Node annot;


        @Setup
        public void setup() throws IOException {

            LanguageVersionHandler lvh = LanguageRegistry.getLanguage("Java")
                                                         .getDefaultVersion()
                                                         .getLanguageVersionHandler();

            newParser = lvh.getParser(lvh.getDefaultParserOptions());

            InputStreamReader streamReader = new InputStreamReader(StreamBench.class.getResourceAsStream(sourceFname));
            source = new StringReader(IOUtils.toString(streamReader));
            streamReader.close();
            acu = newParser.parse(sourceFname, source);
            annot = acu.descendants().first(ASTAnnotation.class).ancestors(ASTMethodDeclaration.class).first();
        }


        public void bench(Blackhole bh, BiFunction<StreamImplementation, Node, Iterable<?>> consumer) {
            acu.asStream().forEach(it -> consumer.apply(impl, acu).forEach(bh::consume));
        }


        @TearDown
        public void tearDown() throws IOException {
            source.close();
        }

    }
}
