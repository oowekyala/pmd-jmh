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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;


@Fork(1)
@Measurement(iterations = 4, timeUnit = TimeUnit.SECONDS, time = 1)
@Warmup(iterations = 3, timeUnit = TimeUnit.SECONDS, time = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class MyBenchmark {
    /*
    Results:

openjdk version "1.8.0_272"
OpenJDK Runtime Environment (build 1.8.0_272-b10)
OpenJDK 64-Bit Server VM (build 25.272-b10, mixed mode)


    Benchmark               Mode  Cnt   Score    Error  Units
    MyBenchmark.withChar    avgt    4  52,382 ± 23,718  us/op
    MyBenchmark.withString  avgt    4  51,795 ± 23,384  us/op

openjdk version "13.0.4" 2020-07-14
OpenJDK Runtime Environment Zulu13.33+25-CA (build 13.0.4+8-MTS)
OpenJDK 64-Bit Server VM Zulu13.33+25-CA (build 13.0.4+8-MTS, mixed mode, sharing)

    Benchmark               Mode  Cnt   Score    Error  Units
    MyBenchmark.withChar    avgt    4  58,963 ±  7,462  us/op
    MyBenchmark.withString  avgt    4  55,696 ± 16,882  us/op


openjdk version "15.0.1" 2020-10-20
OpenJDK Runtime Environment AdoptOpenJDK (build 15.0.1+9)
Eclipse OpenJ9 VM AdoptOpenJDK (build openj9-0.23.0, JRE 15 Linux amd64-64-Bit Compressed References 20201022_81 (JIT enabled, AOT enabled)

    Benchmark               Mode  Cnt   Score   Error  Units
    MyBenchmark.withChar    avgt    4  11,971 ± 0,743  us/op
    MyBenchmark.withString  avgt    4  25,687 ± 1,840  us/op

     */

    public static String[] data;

    @Setup
    public static void setup() {
        List<String> list = Arrays.asList(
                "ohdiue",
                "hdiue",
                "",
                "hddx",
                "o",
                "h",
                "ohh"
        );
        list = Collections.nCopies(1000, list)
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Collections.shuffle(list);
        data = list.toArray(new String[0]);
    }


    @Benchmark
    public void withString(Blackhole blackhole) {
        for (String s : data)
            blackhole.consume(s.startsWith("o"));
    }


    @Benchmark
    public void withChar(Blackhole blackhole) {
        for (String s : data)
            blackhole.consume(!s.isEmpty() && s.charAt(0) == 'o');
    }

}
