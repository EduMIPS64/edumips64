import React from 'react';

const parseData = (dataString) => {
  const rows = dataString.split('\n').map(line => {
    let address = "", value = "", label = "", code = "", comment = "";

    const addressMatch = line.match(/ADDRESS\s+([^\s,]+)/);
    if (addressMatch) address = addressMatch[1];

    const valueMatch = line.match(/VALUE\s+([^\s,]+)/);
    if (valueMatch) value = valueMatch[1];

    const labelMatch = line.match(/LABEL\s+([^\s,]+)/);
    if (labelMatch) label = labelMatch[1];

    const codeMatch = line.match(/CODE\s+([^,]*)/);
    if (codeMatch) code = codeMatch[1];

    const commentMatch = line.match(/COMMENT\s+(.+)/);
    if (commentMatch) comment = commentMatch[1];

    return { address, value, label, code, comment };
  });
  return rows;
};

const Memory = (props) => {
  const memoryString = props.memory;
  const dataPart = memoryString.split("Code:")[0].replace("Data:", "").trim();
  const codePart = memoryString.includes("Code:") ? memoryString.split("Code:")[1].trim() : "";
  const dataRows = parseData(dataPart);

  return (
    <div>
      <div id="data-view">
        <h3>Data:</h3>
        <table>
          <thead>
            <tr>
              <th>ADDRESS</th>
              <th>VALUE</th>
              <th>LABEL</th>
              <th>CODE</th>
              <th>COMMENT</th>
            </tr>
          </thead>
          <tbody>
            {dataRows.map((row, index) => (
              <tr key={index}>
                <td>{row.address}</td>
                <td>{row.value}</td>
                <td>{row.label}</td>
                <td>{row.code}</td>
                <td>{row.comment}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div id="code-view">
        <h3>Code:</h3>
        <textarea readOnly value={codePart} />
      </div>
    </div>
  );
};

export default Memory;
