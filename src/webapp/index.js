import React from 'react';
import { createRoot } from 'react-dom/client';

import CssBaseline from '@mui/material/CssBaseline';

import { ApplicationInsights } from '@microsoft/applicationinsights-web';

import Simulator from './components/Simulator';

import './css/main.css'


// Set version from the webpack variables. Uses globals defined by webpack.
/* global BRANCH, COMMITHASH */
const version = `${BRANCH}-${COMMITHASH.substring(0, 7)}`;

// Initialize AppInsights.
const appInsights = new ApplicationInsights({
  config: {
    connectionString: 'InstrumentationKey=ae180a87-f990-410c-a51c-8077c240e265;IngestionEndpoint=https://westeurope-5.in.applicationinsights.azure.com/;LiveEndpoint=https://westeurope.livediagnostics.monitor.azure.com/',
  },
});
appInsights.loadAppInsights();
appInsights.context.application.ver = version;
appInsights.context.application.build = version;
const telemetryInitializer = (envelope) => {
  envelope.tags['ai.cloud.role'] = process.env.NODE_ENV,
  envelope.tags['ai.cloud.roleInstance'] = version;
};
appInsights.addTelemetryInitializer(telemetryInitializer);
appInsights.trackPageView();
console.log('Initialized AppInsights');

// Web Worker that runs the EduMIPS64 core, built from the Java codebase.
// Contains some syntactical sugar methods to make working with the
// Web Worker API a bit easier, and some telemetry.
let simulator = new Worker('worker.js');
simulator.reset = () => {
  simulator.postMessage({ method: 'reset' });
  appInsights.trackEvent({name: "reset"});
};
simulator.step = (n) => {
  simulator.postMessage({ method: 'step', steps: n });
  appInsights.trackEvent({name: 'step', properties: {steps: n}});
};
simulator.load = (code) => {
  simulator.postMessage({ method: 'load', code });
  appInsights.trackEvent({name: "load"});
};
simulator.checkSyntax = (code) => {
  simulator.postMessage({ method: 'checksyntax', code });
  appInsights.trackEvent({name: "checkSyntax"});
};
simulator.stdin = (input) => {
  simulator.postMessage({ method: 'stdin', input });
  appInsights.trackEvent({name: "stdin"});
}
simulator.parseResult = (result) => {
  result.registers = JSON.parse(result.registers);
  result.memory = JSON.parse(result.memory);
  result.statistics = JSON.parse(result.statistics);
  return result;
};
simulator.version = version;

simulator.reset();
const initializer = (evt) => {
  console.log('Running the initializer callback');

  // Run this callback only once, to initialize the Simulator
  // React component which will then handle all subsequent messages.
  simulator.removeEventListener('message', initializer);
  const initState = simulator.parseResult(evt.data);
  const container = document.getElementById('simulator');
  const root = createRoot(container);
  
  root.render(
        <>
        <CssBaseline />
        <Simulator sim={simulator} initialState={initState} appInsights={appInsights} />
        </>
  );
};
simulator.addEventListener('message', initializer);
