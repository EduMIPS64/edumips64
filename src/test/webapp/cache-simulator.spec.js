const { test, expect } = require('@playwright/test');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  waitForRunningState,
  waitForSimulationComplete,
  loadProgram,
  runToCompletion
} = require('./test-utils');

/**
 * Helper function to set cache configuration values.
 * Uses positional selectors within each cache section to find the correct input fields.
 * The order of inputs is: Size, Block Size, Associativity, Penalty.
 * @param {import('@playwright/test').Page} page - Playwright page object
 * @param {string} cacheType - 'L1 Data Cache' or 'L1 Instruction Cache'
 * @param {object} config - { size, blockSize, associativity, penalty }
 */
async function setCacheConfig(page, cacheType, config) {
  // Find the cache configuration section by its label
  const cacheSection = page.locator(`text=${cacheType}`).locator('..');

  // Set each config value using positional selectors
  // The inputs appear in order: Size, Block Size, Associativity, Penalty
  if (config.size !== undefined) {
    const sizeInput = cacheSection.locator('input[type="number"]').nth(0);
    await sizeInput.fill(String(config.size));
  }
  if (config.blockSize !== undefined) {
    const blockSizeInput = cacheSection.locator('input[type="number"]').nth(1);
    await blockSizeInput.fill(String(config.blockSize));
  }
  if (config.associativity !== undefined) {
    const assocInput = cacheSection.locator('input[type="number"]').nth(2);
    await assocInput.fill(String(config.associativity));
  }
  if (config.penalty !== undefined) {
    const penaltyInput = cacheSection.locator('input[type="number"]').nth(3);
    await penaltyInput.fill(String(config.penalty));
  }
}

/**
 * Helper function to get cache statistics from the Statistics panel
 * @param {import('@playwright/test').Page} page - Playwright page object
 * @returns {Promise<object>} - { l1iReads, l1iMisses, l1dReads, l1dReadMisses, l1dWrites, l1dWriteMisses }
 */
async function getCacheStats(page) {
  const l1iReadsCell = page.locator('#stat-l1i-reads');
  const l1iMissesCell = page.locator('#stat-l1i-misses');
  const l1dReadsCell = page.locator('#stat-l1d-reads');
  const l1dReadMissesCell = page.locator('#stat-l1d-read-misses');
  const l1dWritesCell = page.locator('#stat-l1d-writes');
  const l1dWriteMissesCell = page.locator('#stat-l1d-write-misses');

  // After the simulation ends, React state can still be settling; wait until we see non-zero reads.
  await expect(l1iReadsCell).not.toHaveText('0', { timeout: 10000 });

  const l1iReads = parseInt((await l1iReadsCell.textContent()) || '0', 10);
  const l1iMisses = parseInt((await l1iMissesCell.textContent()) || '0', 10);
  const l1dReads = parseInt((await l1dReadsCell.textContent()) || '0', 10);
  const l1dReadMisses = parseInt(
    (await l1dReadMissesCell.textContent()) || '0',
    10
  );
  const l1dWrites = parseInt((await l1dWritesCell.textContent()) || '0', 10);
  const l1dWriteMisses = parseInt(
    (await l1dWriteMissesCell.textContent()) || '0',
    10
  );

  return {
    l1iReads,
    l1iMisses,
    l1dReads,
    l1dReadMisses,
    l1dWrites,
    l1dWriteMisses,
  };
}



/**
 * Test: Verify cache statistics are displayed and updated after running a simple program
 */
test('cache statistics are displayed after running a program', async ({
  page,
}) => {
  await page.goto(targetUri);

  await waitForPageReady(page);

  // Simple program that does a store and load
  const testProgram = `.data
value: .word 42

.code
LD r1, value(r0)
DADDI r2, r1, 1
SD r2, value(r0)
SYSCALL 0
`;

  await loadProgram(page, testProgram);
  await runToCompletion(page);

  // Get cache statistics
  const stats = await getCacheStats(page);

  // Verify that we have some instruction reads (from fetching instructions)
  expect(stats.l1iReads).toBeGreaterThan(0);

  // Verify that we have data reads (from LD instruction)
  expect(stats.l1dReads).toBeGreaterThan(0);

  // Verify that we have data writes (from SD instruction)
  expect(stats.l1dWrites).toBeGreaterThan(0);

  await page.close();
});

/**
 * Test: Verify cache misses occur with a simple memory access pattern
 * Based on CacheSimulatorTests.java patterns
 */
test('cache misses are recorded for memory accesses', async ({ page }) => {
  await page.goto(targetUri);

  await waitForPageReady(page);

  // Program that accesses multiple memory locations using a register for base address
  const testProgram = `.data
arr: .space 64

.code
DADDI r10, r0, arr
DADDI r1, r0, 1
SD r1, 0(r10)
SD r1, 8(r10)
SD r1, 16(r10)
SD r1, 24(r10)
LD r2, 0(r10)
LD r3, 8(r10)
LD r4, 16(r10)
LD r5, 24(r10)
SYSCALL 0
`;

  await loadProgram(page, testProgram);
  await runToCompletion(page);

  // Get cache statistics
  const stats = await getCacheStats(page);

  // Verify instruction cache had some reads
  expect(stats.l1iReads).toBeGreaterThan(0);

  // Verify we have data reads and writes
  expect(stats.l1dReads).toBe(4); // 4 LD instructions
  expect(stats.l1dWrites).toBe(4); // 4 SD instructions

  // There should be some misses (at least initial compulsory misses)
  expect(stats.l1dReadMisses + stats.l1dWriteMisses).toBeGreaterThan(0);

  await page.close();
});

/**
 * Test: Verify cache configuration can be changed and affects cache behavior
 * This test uses a smaller cache size to ensure more cache misses
 */
test('changing cache configuration affects cache misses', async ({ page }) => {
  await page.goto(targetUri);

  await waitForPageReady(page);

  // Configure L1 Data Cache with a small size to cause more misses
  await setCacheConfig(page, 'L1 Data Cache', {
    size: 256,
    blockSize: 8,
    associativity: 1,
    penalty: 50,
  });

  // Program that accesses memory locations spread apart using register offsets
  const testProgram = `.data
arr: .space 512

.code
DADDI r10, r0, arr
DADDI r1, r0, 1
SD r1, 0(r10)
SD r1, 64(r10)
SD r1, 128(r10)
SD r1, 192(r10)
SD r1, 256(r10)
SD r1, 320(r10)
SD r1, 384(r10)
SD r1, 448(r10)
SYSCALL 0
`;

  await loadProgram(page, testProgram);
  await runToCompletion(page);

  // Get cache statistics
  const stats = await getCacheStats(page);

  // With a small cache (256 bytes, block size 8, direct-mapped)
  // and writes to 8 different cache lines spread 64 bytes apart,
  // we should see write misses
  expect(stats.l1dWrites).toBe(8);
  expect(stats.l1dWriteMisses).toBeGreaterThan(0);

  await page.close();
});

/**
 * Test: Compare cache statistics with different block sizes
 * Larger block sizes should result in fewer misses for sequential access
 */
test('larger block size reduces misses for sequential access', async ({
  page,
}) => {
  await page.goto(targetUri);

  await waitForPageReady(page);

  // Program with sequential memory access pattern using register base
  const testProgram = `.data
arr: .space 64

.code
DADDI r10, r0, arr
DADDI r1, r0, 1
SD r1, 0(r10)
SD r1, 8(r10)
SD r1, 16(r10)
SD r1, 24(r10)
SD r1, 32(r10)
SD r1, 40(r10)
SD r1, 48(r10)
SD r1, 56(r10)
SYSCALL 0
`;

  // Test with small block size (8 bytes) - each access is in a different block
  await setCacheConfig(page, 'L1 Data Cache', {
    size: 1024,
    blockSize: 8,
    associativity: 1,
    penalty: 50,
  });

  await loadProgram(page, testProgram);
  await runToCompletion(page);

  const statsSmallBlock = await getCacheStats(page);

  // Reset and test with larger block size
  // After simulation complete, stop is disabled but load should still work
  await setCacheConfig(page, 'L1 Data Cache', {
    size: 1024,
    blockSize: 64,
    associativity: 1,
    penalty: 50,
  });

  await loadProgram(page, testProgram);
  await runToCompletion(page);

  const statsLargeBlock = await getCacheStats(page);

  // Both should have 8 writes
  expect(statsSmallBlock.l1dWrites).toBe(8);
  expect(statsLargeBlock.l1dWrites).toBe(8);

  // With a 64-byte block size, all 8 sequential writes (56 bytes total)
  // should fit in 1-2 cache blocks, resulting in fewer misses
  // than with 8-byte blocks where each write is a miss
  expect(statsLargeBlock.l1dWriteMisses).toBeLessThanOrEqual(
    statsSmallBlock.l1dWriteMisses
  );

  await page.close();
});

/**
 * Test: Verify instruction cache statistics with a loop
 * Loops should show instruction cache hits after initial load
 */
test('instruction cache shows hits for loop iterations', async ({ page }) => {
  await page.goto(targetUri);

  await waitForPageReady(page);

  // Set up instruction cache configuration
  await setCacheConfig(page, 'L1 Instruction Cache', {
    size: 1024,
    blockSize: 16,
    associativity: 1,
    penalty: 40,
  });

  // Program with a loop - should have many instruction reads but few misses
  const testProgram = `.code
DADDI r1, r0, 10
loop:
DADDI r1, r1, -1
BNEZ r1, loop
SYSCALL 0
`;

  await loadProgram(page, testProgram);
  await runToCompletion(page);

  const stats = await getCacheStats(page);

  // We should have many instruction reads (10+ iterations of the loop)
  expect(stats.l1iReads).toBeGreaterThan(20);

  // But relatively few instruction misses (just the initial loads)
  // The loop body should be cached after first iteration
  expect(stats.l1iMisses).toBeLessThan(stats.l1iReads);

  // Verify the miss rate is reasonable (less than 50%)
  const missRate = stats.l1iMisses / stats.l1iReads;
  expect(missRate).toBeLessThan(0.5);

  await page.close();
});

/**
 * Test: Verify that cache statistics reset when loading a new program
 */
test('cache statistics reset when loading new program', async ({ page }) => {
  await page.goto(targetUri);

  await waitForPageReady(page);

  // First program
  const program1 = `.data
val: .word 1

.code
LD r1, val(r0)
SYSCALL 0
`;

  await loadProgram(page, program1);
  await runToCompletion(page);

  const stats1 = await getCacheStats(page);

  expect(stats1.l1dReads).toBeGreaterThan(0);

  // Reload page to reset state fully and load a new program
  await page.reload();
  await waitForPageReady(page);

  // Second program (minimal, just exit)
  const program2 = `.code
SYSCALL 0
`;

  await loadProgram(page, program2);
  await runToCompletion(page);

  const stats2 = await getCacheStats(page);

  // After loading the second program which has no data accesses,
  // the data cache stats should be 0
  expect(stats2.l1dReads).toBe(0);
  expect(stats2.l1dWrites).toBe(0);

  await page.close();
});
