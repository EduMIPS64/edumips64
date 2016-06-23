# Web UI User Stories (draft)

This is just a brainstorming of some user stories for the web UI, to be further 
refined, categorized and prioritized.

The user here is just the student or enthusiast interested in playing with a
MIPS64 simulator. The context is always a desktop web interface, unless
specified.

# Compatibility

1. open a MIPS64 assembly program from disk, or input it in a text area
1. parsing a MIPS64 assembly program, having clear indications of errors
   1. option to make warnings errors (?)
1. executing a MIPS64 assembly program
  1. all at once
    1. showing progress as time goes by
      1. setting a customizable processor frequency (to show updates slowly)
  1. step-by-step
     1. with a customizable stride
  1. reset execution state while paused
1. downloading a Dinero Tracefile at the end of the execution
1. setting the UI language
1. enabling / disabling forwarding
1. storing preferences
1. SYSCALL support
   1. console I/O
   1. file I/O (??)
1. BREAK support
1. Cycles window (temporal instruction diagram)
1. Pipeline window (state of which instructions are in which stage)
1. Setting options related to exceptions
1. Setting options related to the FPU

# New features

1. online warnings/errors in the code window
1. open from URL (?)
