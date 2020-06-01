import React from 'react';
import ReactDOM from 'react-dom';

import Simulator from './components/Simulator';

import { ApplicationInsights } from '@microsoft/applicationinsights-web';

// Initialize AppInsights.
const appInsights = new ApplicationInsights({
  config: {
    instrumentationKey: '4fdd6b3c-15fb-4fd2-910e-0bd297b8d293',
  },
});
appInsights.loadAppInsights();
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

simulator.reset();
var initializer = (evt) => {
  console.log('Running the initializer callback');

  // Run this callback only once, to initialize the Simulator
  // React component which will then handle all subsequent messages.
  simulator.removeEventListener('message', initializer);
  var initState = simulator.parseResult(evt.data);

  ReactDOM.render(
    <Simulator sim={simulator} initialState={initState} />,
    document.getElementById('simulator'),
  );
};
simulator.addEventListener('message', initializer);
