// Regression test for issue #1723: syntax highlighting disappears while a
// program is executing.
//
// Root cause: Code.js used to call monaco.languages.setMonarchTokensProvider
// in the component's render body. Each Run All step triggers many parent
// re-renders (one per pipeline update), so the tokenizer provider was
// reinstalled several times per second. Each reinstall invalidates already-
// tokenized lines and starts an async re-tokenization pass, during which
// Monaco renders the affected lines with the default token class — i.e.
// plain black text with no syntax colours.
//
// Detection strategy: a `.code` line that contains an instruction mnemonic
// is normally split by Monaco into several spans, at least one of which is
// the keyword span coloured blue. While the regression is active, the
// tokenizer is constantly being torn down, so every span on those lines
// renders as `mtk1` with the default colour `rgb(0, 0, 0)`. We sample the
// editor DOM repeatedly during a Run All and fail the test if every span
// on every mnemonic-bearing line is black for the entire sample window.
const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady } = require('./test-utils');

async function sampleMnemonicLines(page) {
  return page.evaluate(() => {
    const lines = [...document.querySelectorAll('.monaco-editor .view-line')];
    return lines
      .map((line) => {
        const text = line.innerText.replace(/\u00a0/g, ' ');
        const spans = [...line.querySelectorAll('span[class^="mtk"]')].map(
          (s) => ({
            color: getComputedStyle(s).color,
            cls: s.className,
          }),
        );
        return { text, spans };
      })
      .filter((l) =>
        /\b(daddi|bne|nop|syscall|sd|sw|ld|lw|add|sub|halt)\b/.test(l.text),
      );
  });
}

function lineLooksUnhighlighted(line) {
  if (line.spans.length === 0) return false;
  return line.spans.every((s) =>
    /^rgb\(\s*0\s*,\s*0\s*,\s*0\s*\)$/.test(s.color),
  );
}

test.describe('Syntax highlighting during execution (#1723)', () => {
  test('instruction lines keep their colour while Run All is in progress', async ({
    page,
  }) => {
    await page.goto(targetUri);
    await waitForPageReady(page);

    // Use the default sample program that ships with the page — it is long
    // enough that the re-tokenization storm caused by the bug can be
    // observed reliably.
    await page.waitForSelector('#load-button:not([disabled])', {
      timeout: 10000,
    });
    await page.click('#load-button');
    await page.waitForSelector('#step-button:not([disabled])', {
      timeout: 10000,
    });

    // Sanity check: the editor exposes mnemonic-bearing lines after Load.
    const idle = await sampleMnemonicLines(page);
    expect(idle.length).toBeGreaterThan(0);

    // Kick off Run All. During the run we keep polling the DOM and assert
    // that at least one mnemonic-bearing line continues to expose a non-
    // black span. With the bug present, every poll sees only mtk1/black.
    await page.click('#run-button');

    let badPolls = 0;
    let totalPolls = 0;
    let lastBad = null;
    const deadline = Date.now() + 4000;
    while (Date.now() < deadline) {
      const samples = await sampleMnemonicLines(page);
      if (samples.length > 0) {
        totalPolls += 1;
        if (samples.every(lineLooksUnhighlighted)) {
          badPolls += 1;
          lastBad = samples;
        }
      }
      const finished = await page
        .locator('#clear-code-button')
        .isEnabled()
        .catch(() => false);
      if (finished) break;
      await page.waitForTimeout(80);
    }

    expect(totalPolls).toBeGreaterThan(0);
    // Allow at most one polling sample to catch a re-tokenization gap; if
    // *every* sample during the run shows fully-black mnemonic lines the
    // tokenizer is getting stomped continuously.
    expect(
      badPolls,
      `${badPolls}/${totalPolls} polls during Run All saw all mnemonic lines unhighlighted (#1723). Last sample: ${JSON.stringify(lastBad).slice(0, 600)}`,
    ).toBeLessThanOrEqual(1);
  });
});
