; Source code that contains parsing errors and warnings, just to be able to
; perform very quick tests about parsing errors/warnings. To see the third
; message (a warning), warnings must be enabled.

.code

daddi r1, r0, blah      ; (E) Invalid immediate value
dadd r1, r0, r50        ; (E) Invalid register
halt                    ; (W) WinMIPS64 instruction, not MIPS64
