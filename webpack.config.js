const path = require('path');
const webpack = require('webpack');

const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin');
const CopyPlugin = require('copy-webpack-plugin');
const { GitRevisionPlugin } = require('git-revision-webpack-plugin')

const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

const outputPath = path.resolve(__dirname, 'out/web');
const staticPath = path.resolve(__dirname, 'src/webapp/static');
const docsPath = path.resolve(__dirname, 'out/docs');

const copyPlugin = new CopyPlugin({
  patterns: [
    { from: staticPath, to: outputPath },
    { 
      from: docsPath, 
      to: path.join(outputPath, 'docs'),
      globOptions: {
        ignore: ['**/doctrees/**', '**/.buildinfo', '**/objects.inv', '**/_sources/**']
      },
      noErrorOnMissing: true
    }
  ],
});
const grPlugin = new GitRevisionPlugin({
  versionCommand: 'describe --tags --match v* --always --dirty',
});
const versionsPlugin = new webpack.DefinePlugin({
  VERSION: JSON.stringify(grPlugin.version().replace(/^v/, '')),
});
const monacoPlugin = new MonacoWebpackPlugin({
  // The app registers its own custom 'mips' Monarch grammar and tokens-provider
  // factory in Code.js, deliberately replacing monaco's stock mips contribution
  // (see the comments there about the tokenization race). Loading no built-in
  // languages keeps the bundle minimal and avoids that race entirely. The core
  // editor worker is still emitted regardless of this list.
  languages: [],
  features: ['comment', 'foldng', 'hover'],
});

module.exports = (env, argv) => {
  const isProduction = argv && argv.mode === 'production';
  return {
    mode: isProduction ? 'production' : 'development',
    entry: './src/webapp/index.js',
    // Inline source maps embed the full source into the bundle and bloat
    // the production build by an order of magnitude (~30 MiB vs ~2 MiB).
    // Use an external source map in production and keep fast inline maps
    // only for development.
    devtool: isProduction ? 'source-map' : 'inline-source-map',
    resolve: {
      alias: {
        // Force react-monaco-editor and monaco-vim to resolve the bare
        // 'monaco-editor' import to the core editor API instead of the full
        // package entry (editor.main), which eagerly bundles ~100 unused
        // language grammars and the JSON/CSS/HTML/TS language services. The
        // app only uses the core editor plus its own custom 'mips' grammar.
        'monaco-editor$': 'monaco-editor/esm/vs/editor/editor.api',
      },
    },
    output: {
      path: outputPath,
      filename: 'ui.js',
    },
    module: {
      rules: [
        {
          test: /\.js$/,
          exclude: /node_modules/,
          loader: 'babel-loader',
          // Babel resolves its env from BABEL_ENV/NODE_ENV, *not* from
          // webpack's --mode. Since Babel 8, @babel/preset-react defaults to
          // the automatic JSX runtime and emits the development-only
          // `jsxDEV()` calls unless the Babel env is 'production'. A
          // `webpack --mode production` build therefore compiled dev-mode
          // JSX while bundling production React (whose jsx-dev-runtime
          // exports nothing), crashing the app at startup with
          // "jsxDEV is not a function" and a blank page. Tie Babel's env to
          // the webpack mode in production; keep the default resolution in
          // development so `BABEL_ENV=coverage` still activates the istanbul
          // instrumentation env from .babelrc.
          options: isProduction ? { envName: 'production' } : {},
        },
        // Webpack 5 enforces fully-specified ESM resolution for .mjs files,
        // which breaks extensionless imports like MUI's
        // 'react-transition-group/TransitionGroupContext' (added in @mui v9.1).
        // Relax the requirement for node_modules .mjs files.
        { test: /\.mjs$/, include: /node_modules/, resolve: { fullySpecified: false } },
        { test: /\.css$/, use: ['style-loader', 'css-loader'] },
        { test: /\.ttf$/, use: ['file-loader'] },
        { test: /\.png$/, use: ['file-loader'] },
      ],
    },
    devServer: {
      static: outputPath,
      open: true,
    },
    plugins: [
      monacoPlugin,
      copyPlugin,
      grPlugin,
      versionsPlugin,
      //new BundleAnalyzerPlugin(),
    ],
  };
};
