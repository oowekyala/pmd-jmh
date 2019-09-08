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
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersionHandler;
import net.sourceforge.pmd.lang.Parser;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.JavaParserVisitorAdapter;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Provider;
import com.github.javaparser.Providers;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;


@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class MyBenchmark {


    @Benchmark
    public void testNewParser(ParserState state, Blackhole blackhole) {
        state.bench(false, blackhole);
    }


    @Benchmark
    public void testJavaParser(ParserState state, Blackhole blackhole) {
        state.benchJP(blackhole);
    }

//
//    @Benchmark
//    public void testNewVisit(ParserState state, Blackhole blackhole) {
//        state.bench(false, node -> new JavaParserVisitorAdapter() {
//            @Override
//            public Object visit(JavaNode node, Object data) {
//                blackhole.consume(data);
//                return super.visit(node, data);
//            }
//        }.visit((ASTCompilationUnit) node, new Object()));
//    }
//
//
//    @Benchmark
//    public void testOldVisit(ParserState state, Blackhole blackhole) {
//        state.bench(true, node -> new net.sourceforge.pmd.lang.oldjava.ast.JavaParserVisitorAdapter() {
//            @Override
//            public Object visit(net.sourceforge.pmd.lang.oldjava.ast.JavaNode node, Object data) {
//                blackhole.consume(data);
//                return super.visit(node, data);
//            }
//        }.visit((net.sourceforge.pmd.lang.oldjava.ast.ASTCompilationUnit) node, new Object()));
//    }


    @State(Scope.Benchmark)
    public static class ParserState {

        Parser newParser;
        Parser oldParser;
        private Reader source;

        @Param( {
		"/JavaParser.java", 
		// "/PLSQLParser.java"
	})

        private String sourceFname;

        @Setup
        public void setup() throws IOException {

            LanguageVersionHandler lvh = LanguageRegistry.getLanguage("Java")
                                                         .getDefaultVersion()
                                                         .getLanguageVersionHandler();

            newParser = lvh.getParser(lvh.getDefaultParserOptions());

            LanguageVersionHandler oldLvh = LanguageRegistry.findLanguageByTerseName("oldjava")
                                                            .getDefaultVersion()
                                                            .getLanguageVersionHandler();

            oldParser = oldLvh.getParser(oldLvh.getDefaultParserOptions());


            InputStreamReader streamReader = new InputStreamReader(MyBenchmark.class.getResourceAsStream(sourceFname));
            source = new StringReader(IOUtils.toString(streamReader));
            streamReader.close();
        }

        public void bench(boolean old, Blackhole blackhole) {
            blackhole.consume((old ? oldParser : newParser).parse(sourceFname, source));
        }

        public void benchJP(Blackhole blackhole) {
            blackhole.consume(StaticJavaParser.parse(source));
        }


        @TearDown
        public void tearDown() throws IOException {
            source.close();
        }

    }

}
