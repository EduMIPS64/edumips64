const path = require("path");
const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin');

module.exports = {
    entry: "./src/webapp/index.js",
    output: {
        path: path.resolve(__dirname, "build/gwt/war/edumips64"),
        filename: "ui.js"
    },
    module: {
        rules: [
            {test: /\.js$/, exclude: /node_modules/, loader: "babel-loader"},
            {test: /\.css$/, use: ['style-loader', 'css-loader']},
            {test: /\.ttf$/, use: ['file-loader']}
        ]
    },
    plugins: [
        new MonacoWebpackPlugin({
            languages: ['mips']
        })
    ]
}