const path = require("path");

module.exports = {
    entry: "./src/webapp/index.js",
    output: {
        path: path.resolve(__dirname, "build/gwt/war/edumips64"),
        filename: "ui.js"
    },
    module: {
        rules: [
            {test: /\.js$/, exclude: /node_modules/, loader: "babel-loader"}
        ]
    }
}