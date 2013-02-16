; break.s - tests for the BREAK exception
; (c) 2012 Andrea Spadaccini, licensed under the GNU GPL v2 or later
; Just executes a BREAK instruction. This must work because most unit tests
; execute BREAK on failure.
                .code
    break
