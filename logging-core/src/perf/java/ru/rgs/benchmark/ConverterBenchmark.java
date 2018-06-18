/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

package ru.rgs.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import ru.rgs.logging.Converter;
import ru.rgs.utils.Resources;

@BenchmarkMode(Mode.Throughput)
@State(Scope.Thread)
public class ConverterBenchmark {

    private static final int WARMUP_ITER = 20;
    private static final int MEASURE_ITER = 20;

    private String json;
    private String xml;

    @SuppressWarnings("unused")
    @Setup
    public void prepare() {
        json = Resources.readFromClassPath(ConverterBenchmark.class, "json_complex2.json", null);
        xml = Resources.readFromClassPath(ConverterBenchmark.class, "xml1.xml", null);
        assert json != null;
        assert xml != null;
    }

    @SuppressWarnings("unused")
    @Benchmark
    public void jacksonImp_jsonAsDataStructure() {
        Converter.jsonAsDataStructure(json);
    }

    @SuppressWarnings("unused")
    @Benchmark
    public void jacksonImp_xmlAsDataStructure() {
        Converter.xmlAsDataStructure(xml);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ConverterBenchmark.class.getSimpleName())
                .warmupIterations(WARMUP_ITER)
                .measurementIterations(MEASURE_ITER)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}