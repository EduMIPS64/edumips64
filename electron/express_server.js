const express = require('express');
const path = require('path');

const app = express();
const port = 8081;

app.use(express.static(path.join(__dirname, '../out', 'web')));

//app.get('*', (req, res) => {
//res.sendFile(path.join(__dirname, '../out', 'web', 'index.html'));
//});

app.listen(port, () => {
  console.log(`Server express started at http://localhost:${port}`);
});
