// Sample MIPS64 program to display.
const SampleProgram = `; EduMIPS64 Web test program. Will run for 5020 cycles.
                .data
format_str:     .asciiz   "%dth of %s:\\n%s version %i.%i is being tested!"
s1:             .asciiz   "February"
s2:             .asciiz   "EduMIPS64 Web"
fs_addr:        .space    4
                .word     9
s1_addr:        .space    4
s2_addr:        .space    4
                .word     0
                .word     9

.code

	; just to run for longer, create a for loop that iterates 1000 times.
	; watch how r10 changes value in the Registers section.
	daddi 	r10, r0, 1000
loop:
	
	daddi 	r10, r10, -1

	; Loop back to the start until r10 reaches zero. We use 'bne … r0'
	; (canonical MIPS64) instead of the WinMIPS64-only 'bnez' alias so
	; that the default sample loads with zero warnings.
	bne	r10, r0, loop

	; Call SYSCALL 5 (printf())
	daddi     r5, r0, format_str
	sw        r5, fs_addr(r0)
	daddi     r2, r0, s1
	daddi     r3, r0, s2
	sd        r2, s1_addr(r0)
	sd        r3, s2_addr(r0)
	daddi     r14, r0, fs_addr
	syscall   5

	; Exit the program.
	syscall   0
`;

export default SampleProgram;
