import React from "react";

const Pipeline = ({pipeline}) => {
    return (
        <div id="pipeline">
            <table>
                <tbody>
                    <tr><td>IF</td><td>{pipeline.IF?.Code}</td></tr>
                    <tr><td>ID</td><td>{pipeline.ID?.Code}</td></tr>
                    <tr><td>EX</td><td>{pipeline.EX?.Code}</td></tr>
                    <tr><td>MEM</td><td>{pipeline.MEM?.Code}</td></tr>
                    <tr><td>WB</td><td>{pipeline.WB?.Code}</td></tr>
                </tbody>
            </table>
        </div>
    )
}

export default Pipeline;