/**
 * Telemetry abstraction: production builds use the real Azure Application
 * Insights client; every other deployment kind (dev, pr, archive-build) gets
 * a typed no-op stub so all call sites work unchanged without emitting data.
 */

import { ApplicationInsights } from '@microsoft/applicationinsights-web';
import { getBuildInfo, type BuildKind } from './buildInfo';

// ---------------------------------------------------------------------------
// Minimal interface covering the members used across the app
// ---------------------------------------------------------------------------

export interface ITelemetryClient {
  loadAppInsights(): void;
  trackEvent(event: {
    name: string;
    properties?: Record<string, unknown>;
  }): void;
  trackPageView(): void;
  trackException(exception: { exception: Error }): void;
  addTelemetryInitializer(initializer: (envelope: unknown) => void): void;
  context: {
    application: {
      ver: string;
      build: string;
    };
  };
}

// ---------------------------------------------------------------------------
// No-op stub (used in dev / pr / archive-build)
// ---------------------------------------------------------------------------

class NoOpTelemetry implements ITelemetryClient {
  context = {
    application: {
      ver: '',
      build: '',
    },
  };

  loadAppInsights(): void {}
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  trackEvent(_event: {
    name: string;
    properties?: Record<string, unknown>;
  }): void {}
  trackPageView(): void {}
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  trackException(_exception: { exception: Error }): void {}
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  addTelemetryInitializer(_initializer: (envelope: unknown) => void): void {}
}

// ---------------------------------------------------------------------------
// Factory
// ---------------------------------------------------------------------------

const PRODUCTION_CONNECTION_STRING =
  'InstrumentationKey=ae180a87-f990-410c-a51c-8077c240e265;IngestionEndpoint=https://westeurope-5.in.applicationinsights.azure.com/;LiveEndpoint=https://westeurope.livediagnostics.monitor.azure.com/';

/**
 * Create and initialise the telemetry client for the current deployment.
 *
 * Only 'production' builds send data; every other kind returns a no-op stub.
 * The caller should set `context.application.ver/build` and call
 * `addTelemetryInitializer` / `trackPageView` after this returns.
 */
export function createTelemetry(version: string): ITelemetryClient {
  const buildInfo = getBuildInfo();

  if (buildInfo.kind !== ('production' as BuildKind)) {
    const stub = new NoOpTelemetry();
    stub.context.application.ver = version;
    stub.context.application.build = version;
    return stub;
  }

  const client = new ApplicationInsights({
    config: { connectionString: PRODUCTION_CONNECTION_STRING },
  });

  client.loadAppInsights();
  client.context.application.ver = version;
  client.context.application.build = version;

  return client as unknown as ITelemetryClient;
}
