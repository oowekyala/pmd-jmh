package net.sourceforge.pmd;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;


@Fork(1)
@Measurement(iterations = 4, timeUnit = TimeUnit.SECONDS, time = 1)
@Warmup(iterations = 3, timeUnit = TimeUnit.SECONDS, time = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class MyBenchmark {
    /*
    Results:

openjdk version "1.8.0_272"
OpenJDK Runtime Environment (build 1.8.0_272-b10)
OpenJDK 64-Bit Server VM (build 25.272-b10, mixed mode)

    Benchmark                  Mode  Cnt   Score    Error  Units
    MyBenchmark.withConcat     avgt    4  58,171 ± 29,917  ms/op
    MyBenchmark.withSbAppend   avgt    4   0,152 ±  0,001  ms/op
    MyBenchmark.withSbPrepend  avgt    4   6,604 ±  0,261  ms/op

openjdk version "13.0.4" 2020-07-14
OpenJDK Runtime Environment Zulu13.33+25-CA (build 13.0.4+8-MTS)
OpenJDK 64-Bit Server VM Zulu13.33+25-CA (build 13.0.4+8-MTS, mixed mode, sharing)

    Benchmark                  Mode  Cnt   Score   Error  Units
    MyBenchmark.withConcat     avgt    4  20,481 ± 0,443  ms/op
    MyBenchmark.withSbAppend   avgt    4   0,097 ± 0,001  ms/op
    MyBenchmark.withSbPrepend  avgt    4   3,159 ± 0,048  ms/op

     */

    @Benchmark
    public void withSbPrepend(Blackhole blackhole) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.insert(0, i);
        }
        blackhole.consume(sb);
    }


    @Benchmark
    public void withSbAppend(Blackhole blackhole) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append(i);
        }
        blackhole.consume(sb);
    }

    @Benchmark
    public void withConcat(Blackhole blackhole) {
        String sb = "";
        for (int i = 0; i < 10000; i++) {
            sb = i + sb;
        }
        blackhole.consume(sb);
    }
}
