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
const grPlugin = new GitRevisionPlugin();
const versionsPlugin = new webpack.DefinePlugin({
  VERSION: JSON.stringify(grPlugin.version()),
  COMMITHASH: JSON.stringify(grPlugin.commithash()),
  BRANCH: JSON.stringify(grPlugin.branch()),
});
const monacoPlugin = new MonacoWebpackPlugin({
  languages: ['mips'],
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
    output: {
      path: outputPath,
      filename: 'ui.js',
    },
    module: {
      rules: [
        { test: /\.js$/, exclude: /node_modules/, loader: 'babel-loader' },
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
