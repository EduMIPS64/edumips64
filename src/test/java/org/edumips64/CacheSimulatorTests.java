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
    private CacheSimulator cachesim;

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Before
    public void setUp() {
        cachesim = new CacheSimulator();
    }
    @Test(timeout=2000)
    public void runCacheSimulatorTests() {
        Map<CacheSimulator.CacheConfig, CacheSimulator.Stats> l1iGoldenStats = new HashMap<>();
        Map<CacheSimulator.CacheConfig, CacheSimulator.Stats> l1dGoldenStats = new HashMap<>();

        int[] sizes = {256, 512, 1024, 2048};
        int[] blockSizes = {4, 8, 16};
        int[] associativities = {1, 2, 4};
        int penalty = 50;

        String tracefile = testsLocation + "sample.s.xdin"; // Assicurati che questo file esista

        for (int size : sizes) {
            for (int blockSize : blockSizes) {
                for (int assoc : associativities) {
                    CacheSimulator.CacheConfig config = new CacheSimulator.CacheConfig(size, blockSize, assoc, penalty);

                    cachesim.getL1InstructionCache().setConfig(config);
                    CacheSimulator.Stats statsI = CacheSimulator.SimulateTrace(cachesim.getL1InstructionCache(), tracefile);
                    l1iGoldenStats.put(config, statsI);

                    cachesim.getL1DataCache().setConfig(config);
                    CacheSimulator.Stats statsD = CacheSimulator.SimulateTrace(cachesim.getL1DataCache(), tracefile);
                    l1dGoldenStats.put(config, statsD);
                }
            }
        }

        runTraceOnCachesConfigs(l1iGoldenStats, l1dGoldenStats, tracefile);
    }


    // Compare current cache simulation stats with golden stats
    public void runTraceOnCachesConfigs(Map<CacheSimulator.CacheConfig, CacheSimulator.Stats> l1iGoldenStats,
                                        Map<CacheSimulator.CacheConfig, CacheSimulator.Stats> l1dGoldenStats,
                                        String tracefile) {

        var l1i_cache = cachesim.getL1InstructionCache();
        for (Map.Entry<CacheSimulator.CacheConfig, CacheSimulator.Stats> entry : l1iGoldenStats.entrySet()) {
            CacheSimulator.CacheConfig config = entry.getKey();
            CacheSimulator.Stats expected = entry.getValue();

            l1i_cache.setConfig(config);
            var actual = CacheSimulator.SimulateTrace(l1i_cache, tracefile);

            collector.checkThat("L1I cache mismatch for config " + config, actual, equalTo(expected));
        }

        var l1d_cache = cachesim.getL1DataCache();
        for (Map.Entry<CacheSimulator.CacheConfig, CacheSimulator.Stats> entry : l1dGoldenStats.entrySet()) {
            CacheSimulator.CacheConfig config = entry.getKey();
            CacheSimulator.Stats expected = entry.getValue();

            l1d_cache.setConfig(config);
            var actual = CacheSimulator.SimulateTrace(l1d_cache, tracefile);
            collector.checkThat("L1D cache mismatch for config " + config, actual, equalTo(expected));
        }
    }
    // Save stats to a CSV file
    public void saveStatsToCSV(Map<CacheSimulator.CacheConfig, CacheSimulator.Stats> statsMap, String filename) throws IOException {
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
    public Map<CacheSimulator.CacheConfig, CacheSimulator.Stats> loadStatsFromCSV(String filename) throws IOException {
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
}
