import React from "react";

const Memory = (props) => {
    return (
        <div id="memory">
            <textarea readOnly value={props.memory} />
        </div>
    );
}

export default Memory;