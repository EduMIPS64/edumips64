package org.edumips64.utils;

/** An enum representing a key in the configuration database. Used to provide
 * compile-type checking of keys.
 *  
 * The actual string values should not be changed, because the keys will
 * already have been used by previous versions of the simulator, therefore
 * such a change will result in loss of the saved value.
 */
public enum ConfigKey {
    LANGUAGE("language"),
    FILES("files"),
    LAST_DIR("lastdir"),
    DINERO("dineroIV"),
    SERIAL_NUMBER("serialNumber"),
    IF_COLOR("IFColor"),
    ID_COLOR("IDColor"),
    EX_COLOR("EXColor"),
    MEM_COLOR("MEMColor"),
    FP_ADDER_COLOR("FPAdderColor"),
    FP_MULTIPLIER_COLOR("FPMultiplierColor"),
    FP_DIVIDER_COLOR("FPDividerColor"),
    WB_COLOR("WBColor"),
    RAW_COLOR("RAWColor"),
    SAME_IF_COLOR("SAMEIFColor"),
    FORWARDING("forwarding"),
    WARNINGS("warnings"),
    VERBOSE("verbose"),
    SYNC_EXCEPTIONS_MASKED("syncexc-masked"),
    SYNC_EXCEPTIONS_TERMINATE("syncexc-terminate"),
    N_STEPS("n_step"),
    SLEEP_INTERVAL("sleep_interval"),
    FP_INVALID_OPERATION("INVALID_OPERATION"),
    FP_OVERFLOW("OVERFLOW"),
    FP_UNDERFLOW("UNDERFLOW"),
    FP_DIVIDE_BY_ZERO("DIVIDE_BY_ZERO"),
    FP_NEAREST("NEAREST"),
    FP_TOWARDS_ZERO("TOWARDZERO"),
    FP_TOWARDS_PLUS_INFINITY("TOWARDS_PLUS_INFINITY"),
    FP_TOWARDS_MINUS_INFINITY("TOWARDS_MINUS_INFINITY"),
    FP_LONG_DOUBLE_VIEW("LONGDOUBLEVIEW");
    
    private final String text;
    
    private ConfigKey(String text) {
        this.text = text;
    }
    
    public String toString() {
        return text;
    }
}