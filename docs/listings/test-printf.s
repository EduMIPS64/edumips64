; test-printf.s
; Example usage of printf()
				.data
format_str:		.asciiz		"Today is the %dth of  %s.\n%s version %i.%i is being tested!"
s1:             .asciiz		"June"
s2:             .asciiz		"EduMIPS64"
fs_addr:        .space      4
				.word	    23	
s1_addr:        .space      4
s2_addr:        .space      4
				.word		0
				.word		5
test:
				.code
                daddi       r5, r0, format_str
                sw          r5, fs_addr(r0)
                daddi       r2, r0, s1
                daddi       r3, r0, s2
                sd          r2, s1_addr(r0)
                sd          r3, s2_addr(r0)
				daddi		r14, r0, fs_addr
				trap		5

				trap		0
