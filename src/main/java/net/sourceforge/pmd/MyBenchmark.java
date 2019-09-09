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
import java.util.function.Consumer;

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

import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersionHandler;
import net.sourceforge.pmd.lang.Parser;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTBreakStatement;


@BenchmarkMode(Mode.Throughput)
@Fork(value = 3)
@Measurement(time = 5, iterations = 3)
@Warmup(time = 5, iterations = 3)
@OutputTimeUnit(TimeUnit.SECONDS)
@Timeout(time = 15)
public class MyBenchmark {


//    @Benchmark
//    public static void testOpt(ParserState state, Blackhole blackhole) {
//        state.bench(node -> descendantsByOptConcat(node).forEach(blackhole::consume));
//    }
//
//
//    @Benchmark
//    public static void testIter(ParserState state, Blackhole blackhole) {
//        state.bench(node -> descendantsByIter(node).forEach(blackhole::consume));
//    }

    //
    //    @Benchmark
    //    public static void testConcat(ParserState state, Blackhole blackhole) {
    //        state.bench(node -> descendantsByConcat(node).forEach(blackhole::consume));
    //    }

    //        @Benchmark
    //        public static void testFlatmap(ParserState state, Blackhole blackhole) {
    //            state.bench(node -> descendantsByFlatmap(node).forEach(blackhole::consume));
    //        }
    //
    //
    //    @Benchmark
    //    public static void testStream(ParserState state, Blackhole blackhole) {
    //        state.bench(node -> node.descendants(ASTBreakStatement.class).first().ifPresent(blackhole::consume));
    //    }


    @Benchmark
    public static void testDescendantsThenFirst(ParserState state, Blackhole blackhole) {
        state.bench(1000, node -> blackhole.consume(node.descendants(ASTAnnotation.class).first()));
    }


    @Benchmark
    public static void testGetFirstDescendantOfType(ParserState state, Blackhole blackhole) {
        state.bench(1000, node -> blackhole.consume(node.getFirstDescendantOfType(ASTAnnotation.class)));
    }


    @Benchmark
    public static void testChildrenThenFirst(ParserState state, Blackhole blackhole) {
        state.bench(1, node -> node.descendants().forEach(it -> blackhole.consume(node.children().first(ASTBreakStatement.class))));
    }


    @Benchmark
    public static void testChildrenClassThenFirst(ParserState state, Blackhole blackhole) {
        state.bench(1, node -> node.descendants().forEach(it -> blackhole.consume(node.children(ASTBreakStatement.class).first())));
    }


    @Benchmark
    public static void testGetFirstChildOfType(ParserState state, Blackhole blackhole) {
        state.bench(1, node -> node.descendants().forEach(it -> blackhole.consume(node.getFirstChildOfType(ASTBreakStatement.class))));
    }


    @State(Scope.Benchmark)
    public static class ParserState {

        Parser newParser;
        private Reader source;

        @Param({"/PLSQLParser.java"})
        String sourceFname;
        private Node acu;

        @Setup
        public void setup() throws IOException {

            LanguageVersionHandler lvh = LanguageRegistry.getLanguage("Java")
                                                         .getDefaultVersion()
                                                         .getLanguageVersionHandler();

            newParser = lvh.getParser(lvh.getDefaultParserOptions());

            InputStreamReader streamReader = new InputStreamReader(MyBenchmark.class.getResourceAsStream(sourceFname));
            source = new StringReader(IOUtils.toString(streamReader));
            streamReader.close();
        }


        public void bench(int iter, Consumer<Node> consumer) {
            acu = newParser.parse(sourceFname, source);
            for (int i = 0; i < iter; i++) {
                consumer.accept(acu);
            }
        }


        @TearDown
        public void tearDown() throws IOException {
            source.close();
        }

    }

}
