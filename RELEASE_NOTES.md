# EduMIPS64 version 1.3.0

*16th of October, 2023*

EduMIPS64 is a GPL MIPS64 Instruction Set Architecture (ISA) simulator and graphical debugger.

## Notes for this release

This is version 1.3.0 of EduMIPS64. Its codename is **Lourdes**, as the release is being
published from the french city of Lourdes, home to the Sanctuaire Notre-Dame de Lourdes.

Many complex conflicts are currently plaguing our world. We wish for reason and human
kindness to prevail over reasoning that can only lead to destruction and death, and for
those conflicts to end peacefully as soon as possible. 

This release contains some small improvements, a whole new translation for the simulator
and its documentation and a breaking change.

Let's start from the last one.

### Fixing DMULU

`DMULU` was historically implemented using a syntax that was made incorrect by Release 6
of the MIPS64 ISA in 2014. This version of EduMIPS64 changes `DMULU` to use the new,
correct syntax, and therefore it will break all code using the old `DMULU` syntax.

Porting old code to the new code is pretty simple, as the old version store the results
of the multiplication in the `LO` register, requiring an `MFLO` instruction to fetch it,
while the new version allows users to directly specify the target register.

While the old code may have looked like the following:

```
    [...]
    DMULU r1, r2
    MFLO r3
    [...]
```

The new code should instead be:

```
    [...]
    DMULU r3, r1, r1
```

This is exactly how our internal tests changed, see [code](https://github.com/EduMIPS64/edumips64/commit/897f559ebda971760aae0bcad949b3cf38847b02#diff-24af5fcdf7f63916c891371766a8dc7875e89634fb6dfd5dad34d5b1969846e7).


### Simplified Chinese Translation

Thanks to the effort of @smallg0at, EduMIPS64 now is fully translated to Simplified Chinese,
including the in-app documentation and the HTML/PDF docs.

This change had us find and fix several smaller bugs related to rendering non-ASCII (and non-Italian)
characters, as well as trying to get Sphinx to properly emit Simplified Chinese docs. We haven't
fully succeeded, so the PDF has to be rendered through readthedocs.io, but it is usable and
we have all the needed artifacts.

Huge thanks to @smallg0at for this contribution!

## Special mention: new Web UI

@smallg0at also implemented a brand new, IDE-like layout for the Web UI, which is already deployed
to https://web.edumips.org. This is a major step forward in having a fully-functional version
of EduMIPS64 on the web. Thanks agains, @smallg0at!

Also thanks to @pviotti for doing the foundational work of migrating to more recent major versions
of React and Material UI, which made this work possible.

## Other changes

We also added the `DMUHU` instruction (pretty similar to `DMULU` in terms of implementation), fixed
a few documentation issues (thanks @galloj and @winstonpurnomo) and also changed the look and feel to
be more modern (goodbye, Metal!).

## The usual conclusion

If you find a bug, please open an issue on GitHub.
EduMIPS64 is hosted on GitHub: www.github.com/EduMIPS64/edumips64.

Our web site is https://www.edumips.org.
The web version of EduMIPS64 is available at https://web.edumips.org.