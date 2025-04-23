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

    // Save stats to a CSV file
    public static void saveStatsToCSV(Map<CacheSimulator.CacheConfig, CacheSimulator.Stats> statsMap, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("size,blockSize,associativity,penalty,readAccesses,readMisses,writeAccesses,writeMisses");
            for (Map.Entry<CacheSimulator.CacheConfig, CacheSimulator.Stats> entry : statsMap.entrySet()) {
                CacheSimulator.CacheConfig config = entry.getKey();
                CacheSimulator.Stats stats = entry.getValue();
                writer.printf("%d,%d,%d,%d,%d,%d,%d,%d%n",
                        config.size,
                        config.blockSize,
                        config.associativity,
                        config.penalty,
                        stats.getReadAccesses(),
                        stats.getReadMisses(),
                        stats.getWriteAccesses(),
                        stats.getWriteMisses()
                );
            }
        }
    }

    // Load stats from a CSV file
    public static Map<CacheSimulator.CacheConfig, CacheSimulator.Stats> loadStatsFromCSV(String filename) throws IOException {
        Map<CacheSimulator.CacheConfig, CacheSimulator.Stats> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int size = Integer.parseInt(parts[0]);
                int blockSize = Integer.parseInt(parts[1]);
                int associativity = Integer.parseInt(parts[2]);
                int penalty = Integer.parseInt(parts[3]);
                int readAccesses = Integer.parseInt(parts[4]);
                int readMisses = Integer.parseInt(parts[5]);
                int writeAccesses = Integer.parseInt(parts[6]);
                int writeMisses = Integer.parseInt(parts[7]);

                CacheSimulator.CacheConfig config = new CacheSimulator.CacheConfig(size, blockSize, associativity, penalty);
                CacheSimulator.Stats stats = CacheSimulator.Stats.of(readAccesses, readMisses,  writeAccesses, writeMisses);

                map.put(config, stats);
            }
        }
        return map;
    }

    public static CacheSimulator.Stats runTraceOnCache(CacheSimulator.CacheMemory cache, String traceFile) {

        cache.resetStats();
        // Process the trace file line by line
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(traceFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        };
        try  {
            String line;
            while ((line = br.readLine()) != null) {
                CacheSimulator.processDineroTraceEntry(cache, line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return CacheSimulator.Stats.of(
                cache.stats.getReadAccesses(),
                cache.stats.getReadMisses(),
                cache.stats.getWriteAccesses(),
                cache.stats.getWriteMisses()
        );
    }

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
                    var statsI = runTraceOnCache(cachesim.getL1InstructionCache(), testsLocation+tracefile);
                    L1I_stats.put(config, statsI);

                    cachesim.getL1DataCache().setConfig(config);
                    CacheSimulator.Stats statsD = runTraceOnCache(cachesim.getL1DataCache(), testsLocation+tracefile);
                    L1D_stats.put(config, statsD);
                }
            }
        }

        try {
            saveStatsToCSV(L1I_stats, testsLocation+tracefile+"_golden_stats_L1I.csv");
            saveStatsToCSV(L1D_stats, testsLocation+tracefile+"_golden_stats_L1D.csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // Compare current cache simulation stats with golden stats
    public void checkTraceOnCacheConfigStats(String tracefile) {

        Map<CacheSimulator.CacheConfig, CacheSimulator.Stats> l1iGoldenStats = null;
        Map<CacheSimulator.CacheConfig, CacheSimulator.Stats> l1dGoldenStats = null;
        try {
            l1iGoldenStats = loadStatsFromCSV(testsLocation+tracefile+"_golden_stats_L1I.csv");
            l1dGoldenStats = loadStatsFromCSV(testsLocation+tracefile+"_golden_stats_L1D.csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var l1i_cache = cachesim.getL1InstructionCache();
        for (Map.Entry<CacheSimulator.CacheConfig, CacheSimulator.Stats> entry : l1iGoldenStats.entrySet()) {
            CacheSimulator.CacheConfig config = entry.getKey();
            CacheSimulator.Stats expected = entry.getValue();

            l1i_cache.setConfig(config);
            var actual = runTraceOnCache(l1i_cache, testsLocation+tracefile);

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
            var actual = runTraceOnCache(l1d_cache, testsLocation+tracefile);
            collector.checkThat("L1D cache mismatch for config " + config, actual, equalTo(expected));
        }
    }

}
