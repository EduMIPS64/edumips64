; .double statement tests
.data
	thepositivebiggest: .double 1.797693134862315708145274237317E308
	thenegativebiggest: .double -1.797693134862315708145274237317E308
    thenegativesmallest: .double -4.9406564584124654417656879286822E-324
	thepositivesmallest: .double 4.9406564584124654417656879286822E-324
    
    theoverflow1:   .double 1.797693134862315708145274237318E308
    theoverflow2:   .double -1.797693134862315708145274237318E308
    theunderflow1: .double 4.9406564584124654417656879286821E-324
    theunderflow2: .double -4.9406564584124654417656879286821E-324
    
    theover32bitexponent: .double 2.0E546456546456456 ;any number of this type
