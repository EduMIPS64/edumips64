const path = require('path');
const webpack = require('webpack');

const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin');
const CopyPlugin = require('copy-webpack-plugin');
const GitRevisionPlugin = require('git-revision-webpack-plugin');

const outputPath = path.resolve(__dirname, 'build/gwt/war/edumips64');
const staticPath = path.resolve(__dirname, 'src/webapp/static');

const copyPlugin = new CopyPlugin({
  patterns: [{ from: staticPath, to: outputPath }],
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

module.exports = {
  entry: './src/webapp/index.js',
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
    contentBase: outputPath,
    publicPath: outputPath,
    open: true,
  },
  plugins: [monacoPlugin, copyPlugin, grPlugin, versionsPlugin],
};
