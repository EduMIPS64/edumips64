; Forwarding example taken from:
; Hennessy, Patterson, "Computer Architecture: A Quantitative Approach"
; Appendix A, page 18

.code
DADD R1,R2,R3
LD   R4,0(R1)
SD   R4,8(R1)
