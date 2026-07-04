// Monaco must be configured before any @monaco-editor/react component can
// mount.  This import wires the editor web worker and sets loader.config
// so Monaco is loaded from the local bundle (not CDN).
import './monacoSetup';

import React from 'react';
import { createRoot } from 'react-dom/client';

import AppErrorBoundary from './components/AppErrorBoundary';
import AppLoader from './components/AppLoader';

import { createTelemetry } from './telemetry';
import type { SimulatorWorker } from './simulator/protocol';

import './css/main.css';

// VERSION is injected by Vite's define at build time (git-describe
// output, e.g. "1.4.0-74-ge1b45a15").  Declared in vendor.d.ts.
const version = VERSION;

// Initialize telemetry. Only the 'production' deployment kind sends data to
// Azure Application Insights; all other kinds (dev, pr, archive-build) use a
// typed no-op stub so call sites work unchanged without emitting telemetry.
const appInsights = createTelemetry(version);

// The telemetry initializer callback receives the AI envelope object. The
// interface declares it as `unknown` to stay decoupled from the AppInsights
// SDK type; we narrow to the subset we actually write to.
// TODO(ts): Use the full ITelemetryItem type from @microsoft/applicationinsights-web
// once the full typings are wired up.
const telemetryInitializer = (envelope: unknown) => {
  const env = envelope as { tags: Record<string, string> };
  env.tags['ai.cloud.role'] = 'web-ui';
  env.tags['ai.cloud.roleInstance'] = version;
};
appInsights.addTelemetryInitializer(telemetryInitializer);
appInsights.trackPageView();

// Web Worker that runs the EduMIPS64 core, built from the Java codebase.
// We augment the raw Worker with the typed convenience methods expected by
// the rest of the app (AppLoader → Simulator → hooks).
//
// Object.assign gives us a `Worker & {...methods...}` intersection type.
// Since SimulatorWorker extends Worker and declares exactly these methods,
// the cast to SimulatorWorker is safe.
// TODO(ts): Replace the Object.assign pattern with a proper TypeScript class
// implementing SimulatorWorker when the worker module is migrated.
const rawWorker = new Worker('worker.js');
const worker = Object.assign(rawWorker, {
  reset(): void {
    rawWorker.postMessage({ method: 'reset' });
    appInsights.trackEvent({ name: 'reset' });
  },
  step(n: number): void {
    rawWorker.postMessage({ method: 'step', steps: n });
    appInsights.trackEvent({ name: 'step', properties: { steps: n } });
  },
  load(code: string): void {
    rawWorker.postMessage({ method: 'load', code });
    appInsights.trackEvent({ name: 'load' });
  },
  setCacheConfig(config: object): void {
    rawWorker.postMessage({ method: 'setCacheConfig', config });
  },
  setForwarding(enabled: boolean): void {
    rawWorker.postMessage({ method: 'setForwarding', enabled });
  },
  setDelaySlot(enabled: boolean): void {
    rawWorker.postMessage({ method: 'setDelaySlot', enabled });
  },
  checkSyntax(code: string): void {
    rawWorker.postMessage({ method: 'checksyntax', code });
    appInsights.trackEvent({ name: 'checkSyntax' });
  },
  provideInput(input: string): void {
    rawWorker.postMessage({ method: 'provideInput', input });
  },
  parseResult(result: Record<string, unknown>): Record<string, unknown> {
    result.registers = JSON.parse(result.registers as string);
    result.memory = JSON.parse(result.memory as string);
    result.statistics = JSON.parse(result.statistics as string);
    return result;
  },
  version,
  // The `as unknown as SimulatorWorker` double cast is needed because
  // `parseResult` in the object literal returns `Record<string, unknown>`
  // (which is correct at runtime after JSON.parse), while the interface
  // declares `SimulatorResult` as the return type. The cast is safe because
  // AppLoader calls worker.parseResult() and immediately passes the result
  // to applyResultState which expects SimulatorResult, and the runtime
  // shape matches.
  // TODO(ts): add a runtime Zod/io-ts validator here and remove the cast.
}) as unknown as SimulatorWorker;

// Mount React immediately so the user sees a loading indicator rather than
// a blank page.  AppLoader attaches its 'message'/'error' listeners and
// calls worker.reset() in the same componentDidMount block, so the init
// message can never be missed.
//
// StrictMode is a development-only canary: it double-invokes render
// functions, constructors and effect setup/cleanup to surface unsafe side
// effects (like the render-body worker.onmessage assignment fixed in
// #1910). It renders nothing and has zero effect in production builds.
const container = document.getElementById('simulator');
if (!container) {
  throw new Error('Could not find #simulator element');
}
const root = createRoot(container);

root.render(
  <React.StrictMode>
    <AppErrorBoundary appInsights={appInsights}>
      <AppLoader worker={worker} appInsights={appInsights} />
    </AppErrorBoundary>
  </React.StrictMode>,
);
