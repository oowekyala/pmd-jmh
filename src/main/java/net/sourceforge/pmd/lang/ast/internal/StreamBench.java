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

package net.sourceforge.pmd.lang.ast.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import net.sourceforge.pmd.internal.util.IOUtil;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageProcessor;
import net.sourceforge.pmd.lang.LanguageProcessorRegistry;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.ast.AstVisitorBase;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.Parser;
import net.sourceforge.pmd.lang.ast.Parser.ParserTask;
import net.sourceforge.pmd.lang.ast.SemanticErrorReporter;
import net.sourceforge.pmd.lang.ast.impl.AbstractNode;
import net.sourceforge.pmd.lang.document.TextDocument;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.JavaVisitorBase;
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


@BenchmarkMode(Mode.AverageTime)
@Fork(value = 1)
@Measurement(iterations = 4)
@Warmup(iterations = 2)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Timeout(time = 15)
public class StreamBench {


    @Benchmark
    public void optimizedLoop(Blackhole bh, ParserState state) {
        state.bench(bh, n ->
                n.acceptVisitor(new JavaVisitorBase<Void, Void>() {
                    @Override
                    protected Void visitChildren(Node node, Void data) {
                        for (int i = 0, numChildren = node.getNumChildren(); i < numChildren; i++) {
                            node.getChild(i).acceptVisitor(this, data);
                        }
                        return null;
                    }
                }, null)
        );
    }


    @Benchmark
    public void nonOptChildren(Blackhole bh, ParserState state) {
        state.bench(bh, n ->
                n.acceptVisitor(new JavaVisitorBase<Void, Void>() {
                    @Override
                    protected Void visitChildren(Node node, Void data) {
                        for (Node n : node.children()) {
                            n.acceptVisitor(this, data);
                        }
                        return null;
                    }
                }, null)
        );
    }

    @Benchmark
    public void optChildren(Blackhole bh, ParserState state) {
        state.bench(bh, n ->
                n.acceptVisitor(new JavaVisitorBase<Void, Void>() {

                    @Override
                    protected Void visitChildren(Node node, Void data) {
                        for (Node n : node.childrenOpt()) {
                            n.acceptVisitor(this, data);
                        }
                        return null;
                    }
                }, null)
        );
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
        @Param({"/PLSQLParser.java"})
        String sourceFname;
        private Node acu;

        private LanguageProcessor java;

        @Setup
        public void setup() throws IOException {

            Language lang = LanguageRegistry.PMD.getLanguageById("java");
            this.java = lang.createProcessor(lang.newPropertyBundle());

            newParser = java.services().getParser();

            TextDocument td;
            try (InputStreamReader streamReader = new InputStreamReader(StreamBench.class.getResourceAsStream(sourceFname))) {
                td = TextDocument.readOnlyString(IOUtil.readToString(streamReader), java.getLanguageVersion());
            }
            acu = newParser.parse(new ParserTask(td, SemanticErrorReporter.noop(),
                    LanguageProcessorRegistry.singleton(java)
            ));
        }


        public void bench(Blackhole bh, Function<Node, Void> consumer) {
            bh.consume(consumer.apply(acu));
        }


        @TearDown
        public void tearDown() throws Exception {
            java.close();
        }

    }
}
