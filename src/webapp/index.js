import React from 'react';
import ReactDOM from 'react-dom';

import CssBaseline from '@mui/material/CssBaseline';

import { ApplicationInsights } from '@microsoft/applicationinsights-web';

import Simulator from './components/Simulator';
import Header from './components/Header';

import './css/main.css'


// Set version from the webpack variables. Uses globals defined by webpack.
/* global BRANCH, COMMITHASH */
const version = `${BRANCH}-${COMMITHASH.substring(0, 7)}`;

// Initialize AppInsights.
const appInsights = new ApplicationInsights({
  config: {
    connectionString: 'InstrumentationKey=4fdd6b3c-15fb-4fd2-910e-0bd297b8d293;IngestionEndpoint=https://westus2-0.in.applicationinsights.azure.com/;LiveEndpoint=https://westus2.livediagnostics.monitor.azure.com/',
  },
});
appInsights.loadAppInsights();
appInsights.context.application.ver = version;
appInsights.context.application.build = version;
const telemetryInitializer = (envelope) => {
  envelope.tags['ai.cloud.role'] = process.env.NODE_ENV;
};
appInsights.addTelemetryInitializer(telemetryInitializer);
appInsights.trackPageView();
console.log('Initialized AppInsights');

// Web Worker that runs the EduMIPS64 core, built from the Java codebase.
// Contains some syntactical sugar methods to make working with the
// Web Worker API a bit easier.
let simulator = new Worker('worker.js');
simulator.reset = () => {
  simulator.postMessage({ method: 'reset' });
};
simulator.step = (n) => {
  simulator.postMessage({ method: 'step', steps: n });
};
simulator.load = (code) => {
  simulator.postMessage({ method: 'load', code });
};
simulator.checkSyntax = (code) => {
  simulator.postMessage({ method: 'checksyntax', code });
};
simulator.parseResult = (result) => {
  result.registers = JSON.parse(result.registers);
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

  ReactDOM.render(
        <>
        <CssBaseline />
        <Simulator sim={simulator} initialState={initState} />
        </>,
    document.getElementById('simulator'),
  );
};
simulator.addEventListener('message', initializer);
