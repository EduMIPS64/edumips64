import React from 'react';
import { createRoot } from 'react-dom/client';

import AppErrorBoundary from './components/AppErrorBoundary';
import AppLoader from './components/AppLoader';

import { createTelemetry } from './telemetry';

import './css/main.css';

// Set version from the webpack variables. Uses globals defined by webpack.
// VERSION is git-describe (e.g. "1.4.0-74-ge1b45a15"), matching desktop identity.
/* global VERSION */
const version = VERSION;

// Initialize telemetry. Only the 'production' deployment kind sends data to
// Azure Application Insights; all other kinds (dev, pr, archive-build) use a
// typed no-op stub so call sites work unchanged without emitting telemetry.
const appInsights = createTelemetry(version);

const telemetryInitializer = (envelope) => {
  ((envelope.tags['ai.cloud.role'] = process.env.NODE_ENV),
    (envelope.tags['ai.cloud.roleInstance'] = version));
};
appInsights.addTelemetryInitializer(telemetryInitializer);
appInsights.trackPageView();

// Web Worker that runs the EduMIPS64 core, built from the Java codebase.
// Contains some syntactical sugar methods to make working with the
// Web Worker API a bit easier, and some telemetry.
let worker = new Worker('worker.js');
worker.reset = () => {
  worker.postMessage({ method: 'reset' });
  appInsights.trackEvent({ name: 'reset' });
};
worker.step = (n) => {
  worker.postMessage({ method: 'step', steps: n });
  appInsights.trackEvent({ name: 'step', properties: { steps: n } });
};
worker.load = (code) => {
  worker.postMessage({ method: 'load', code });
  appInsights.trackEvent({ name: 'load' });
};

worker.setCacheConfig = (config) => {
  worker.postMessage({ method: 'setCacheConfig', config });
};

worker.setForwarding = (enabled) => {
  worker.postMessage({ method: 'setForwarding', enabled });
};

worker.setDelaySlot = (enabled) => {
  worker.postMessage({ method: 'setDelaySlot', enabled });
};

worker.checkSyntax = (code) => {
  worker.postMessage({ method: 'checksyntax', code });

  appInsights.trackEvent({ name: 'checkSyntax' });
};
worker.provideInput = (input) => {
  worker.postMessage({ method: 'provideInput', input });
};
worker.parseResult = (result) => {
  result.registers = JSON.parse(result.registers);
  result.memory = JSON.parse(result.memory);
  result.statistics = JSON.parse(result.statistics);
  return result;
};
worker.version = version;

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
const root = createRoot(container);

root.render(
  <React.StrictMode>
    <AppErrorBoundary appInsights={appInsights}>
      <AppLoader worker={worker} appInsights={appInsights} />
    </AppErrorBoundary>
  </React.StrictMode>,
);
