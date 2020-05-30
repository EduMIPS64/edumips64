const path = require("path");
const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin');
const CopyPlugin = require('copy-webpack-plugin');

const outputPath = path.resolve(__dirname, "build/gwt/war/edumips64");
const staticPath = path.resolve(__dirname, "src/webapp/static");

module.exports = {
    entry: "./src/webapp/index.js",
    output: {
        path: outputPath,
        filename: "ui.js"
    },
    module: {
        rules: [
            {test: /\.js$/, exclude: /node_modules/, loader: "babel-loader"},
            {test: /\.css$/, use: ['style-loader', 'css-loader']},
            {test: /\.ttf$/, use: ['file-loader']}
        ]
    },
    devServer: {
        contentBase: outputPath,
    },
    plugins: [
        new MonacoWebpackPlugin({
            languages: ['mips']
        }),
        new CopyPlugin({
            patterns: [
              { from: staticPath, to: outputPath }
            ],
        }),
    ]
}