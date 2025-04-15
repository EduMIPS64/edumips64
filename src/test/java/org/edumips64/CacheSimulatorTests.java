package org.edumips64;

import org.edumips64.core.CacheSimulator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;

public class CacheSimulatorTests {
    // Run all cache configurations on a trace and collect golden stats
    private static String testsLocation = "src/test/resources/";
    private CacheSimulator cachesim = new CacheSimulator();

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Before
    public void setUp() {
    }

    public static void main(String[] args) {
        var cs = new CacheSimulatorTests();
        String tracefile ="sample.s.xdin";
        cs.runTraceOnCacheConfigs(tracefile);
        cs.checkTraceOnCacheConfigStats(tracefile);
    }

    @Test
    public void runCacheSimulatorTests() {
        String tracefile ="sample.s.xdin";
        checkTraceOnCacheConfigStats(tracefile);
        tracefile ="sample2.s.xdin";
        checkTraceOnCacheConfigStats(tracefile);
    }

    // Used to generate reference stats for a tracefile
    public void runTraceOnCacheConfigs(String tracefile) {
        Map<CacheSimulator.CacheConfig, CacheSimulator.Stats> L1I_stats = new HashMap<>();
        Map<CacheSimulator.CacheConfig, CacheSimulator.Stats> L1D_stats = new HashMap<>();

        int[] sizes = {256, 512, 1024, 2048};
        int[] blockSizes = {4, 8, 16};
        int[] associativities = {1, 2, 4};
        int penalty = 50;

        for (int size : sizes) {
            for (int blockSize : blockSizes) {
                for (int assoc : associativities) {
                    var config = new CacheSimulator.CacheConfig(size, blockSize, assoc, penalty);

                    cachesim.getL1InstructionCache().setConfig(config);
                    var statsI = CacheSimulator.runTraceOnCache(cachesim.getL1InstructionCache(), testsLocation+tracefile);
                    L1I_stats.put(config, statsI);

                    cachesim.getL1DataCache().setConfig(config);
                    CacheSimulator.Stats statsD = CacheSimulator.runTraceOnCache(cachesim.getL1DataCache(), testsLocation+tracefile);
                    L1D_stats.put(config, statsD);
                }
            }
        }

        try {
            CacheSimulator.saveStatsToCSV(L1I_stats, testsLocation+tracefile+"_golden_stats_L1I.csv");
            CacheSimulator.saveStatsToCSV(L1D_stats, testsLocation+tracefile+"_golden_stats_L1D.csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // Compare current cache simulation stats with golden stats
    public void checkTraceOnCacheConfigStats(String tracefile) {

        Map<CacheSimulator.CacheConfig, CacheSimulator.Stats> l1iGoldenStats = null;
        Map<CacheSimulator.CacheConfig, CacheSimulator.Stats> l1dGoldenStats = null;
        try {
            l1iGoldenStats = CacheSimulator.loadStatsFromCSV(testsLocation+tracefile+"_golden_stats_L1I.csv");
            l1dGoldenStats = CacheSimulator.loadStatsFromCSV(testsLocation+tracefile+"_golden_stats_L1D.csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var l1i_cache = cachesim.getL1InstructionCache();
        for (Map.Entry<CacheSimulator.CacheConfig, CacheSimulator.Stats> entry : l1iGoldenStats.entrySet()) {
            CacheSimulator.CacheConfig config = entry.getKey();
            CacheSimulator.Stats expected = entry.getValue();

            l1i_cache.setConfig(config);
            var actual = CacheSimulator.runTraceOnCache(l1i_cache, testsLocation+tracefile);

            collector.checkThat("L1I cache mismatch for config " + config, actual, equalTo(expected));

            /*
            if (!actual.equals(expected)) {
                System.out.println("****L1I cache check for " + config + " actual: "+ actual+ " VS expected: "+ expected);
            }
             */
        }

        var l1d_cache = cachesim.getL1DataCache();
        for (Map.Entry<CacheSimulator.CacheConfig, CacheSimulator.Stats> entry : l1dGoldenStats.entrySet()) {
            CacheSimulator.CacheConfig config = entry.getKey();
            CacheSimulator.Stats expected = entry.getValue();

            l1d_cache.setConfig(config);
            var actual = CacheSimulator.runTraceOnCache(l1d_cache, testsLocation+tracefile);
            collector.checkThat("L1D cache mismatch for config " + config, actual, equalTo(expected));
        }
    }

}
