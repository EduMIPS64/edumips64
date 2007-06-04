; Example for SYSCALL 5

                .data
params_sys5:    .space  8

                .text
print_string:   sw      $a0, params_sys5(r0)    
                daddi   r14, r0, params_sys5
                syscall 5
                jr      r31
