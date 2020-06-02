const path = require('path');
const webpack = require('webpack');
const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin');
const CopyPlugin = require('copy-webpack-plugin');

const GitRevisionPlugin = require('git-revision-webpack-plugin');

const outputPath = path.resolve(__dirname, 'build/gwt/war/edumips64');
const staticPath = path.resolve(__dirname, 'src/webapp/static');

const grPlugin = new GitRevisionPlugin();

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
    ],
  },
  devServer: {
    contentBase: outputPath,
  },
  plugins: [
    new MonacoWebpackPlugin({
      languages: ['mips'],
    }),
    new CopyPlugin({
      patterns: [{ from: staticPath, to: outputPath }],
    }),
    grPlugin,
    new webpack.DefinePlugin({
      VERSION: JSON.stringify(grPlugin.version()),
      COMMITHASH: JSON.stringify(grPlugin.commithash()),
      BRANCH: JSON.stringify(grPlugin.branch()),
    }),
  ],
};
