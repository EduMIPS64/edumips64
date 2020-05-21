import React from "react";
import { MdWarning, MdError } from "react-icons/md";

const ErrorCount = ({count}) => {
    if (count == 0) {
        return <React.Fragment />;
    }
    return (
        <span id="errorCount"><MdError />{count} </span>
    )
}
const WarningCount = ({count}) => {
    if (count == 0) {
        return <React.Fragment />;
    }
    return (
        <span id="warningCount"><MdWarning />{count} </span>
    )
}

const ErrorDisplay = ({parsingErrors}) => {
    const warningCount = parsingErrors ? parsingErrors.filter(e => e.isWarning).length : 0;
    const errorCount = parsingErrors ? parsingErrors.filter(e => !e.isWarning).length : 0;

    return (
        <>
            <WarningCount count={warningCount} /><ErrorCount count={errorCount} />
        </>
    )
}

export default ErrorDisplay;