const express = require('express');
const path = require('path');

const app = express();
const port = 8081;

app.use(express.static(path.join(__dirname, '../build', 'gwt', 'war', 'edumips64')));

//app.get('*', (req, res) => {
//res.sendFile(path.join(__dirname, 'build', 'gwt', 'war', 'edumips64', 'index.html'));
//});

app.listen(port, () => {
  console.log(`Server express started at http://localhost:${port}`);
});
