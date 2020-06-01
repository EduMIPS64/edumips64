import React from 'react';
import ErrorDisplay from './ErrorDisplay';

// Number of steps to run with the multi-step button.
const STEP_STRIDE = 500;

const Controls = (props) => {
  return (
    <div id="controls">
      <input
        id="load-button"
        type="button"
        value="Load/Reset"
        onClick={() => {
          props.onLoadClick();
        }}
        disabled={!props.loadEnabled}
      />
      <input
        id="step-button"
        type="button"
        value="Single Step"
        onClick={() => {
          props.onStepClick(1);
        }}
        disabled={!props.stepEnabled}
      />
      <input
        id="multi-step-button"
        type="button"
        value="Multi Step"
        onClick={() => {
          props.onStepClick(STEP_STRIDE);
        }}
        disabled={!props.stepEnabled}
      />
      <input
        id="run-button"
        type="button"
        value="Run All"
        onClick={() => {
          props.onRunClick();
        }}
        disabled={!props.runEnabled}
      />
      <input
        id="stop-button"
        type="button"
        value="Stop"
        onClick={() => {
          props.onStopClick();
        }}
        disabled={!props.stopEnabled}
      />
      <ErrorDisplay parsingErrors={props.parsingErrors} />
    </div>
  );
};

export default Controls;
