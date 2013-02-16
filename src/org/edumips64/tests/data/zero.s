; zero.s - simple test for r0's property of always containing zero
; (c) 2012 Andrea Spadaccini, licensed under the GNU GPL v2 or later

            .code
            daddi r0, r0, 10
            bnez r0, failure
            halt

failure:    break     
