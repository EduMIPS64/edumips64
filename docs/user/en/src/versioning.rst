Understanding versions
======================
Every build of EduMIPS64 displays a version identifier so you always know
exactly what you are running. This page explains what the identifier means and
where to find it.

The version string
------------------
At an official release the version is a plain number, for example
``1.4.1``.

A build made *between* two releases looks like ``1.4.0-74-geec1768``. Reading
it from left to right:

* ``1.4.0`` — the most recent official release that this build follows.
* ``74`` — the number of changes made after that release.
* ``geec1768`` — a short unique identifier for this exact build.

You do not need to memorise the identifier. Its main purpose is to let you
report the *exact* build you are using when filing a bug report, so that
maintainers can reproduce the issue without guessing.

Where to find the version
--------------------------
* **Desktop (Swing) application** — shown in the window title and in the
  *Help → About* dialog.
* **Command line** — run the JAR with the ``--version`` flag.
* **Web application** — shown in the **About** tab of the Help dialog, and
  next to the **"Web Version"** label in the top toolbar.

Which web build am I running?
------------------------------
The **production** web app at https://web.edumips.org carries the stable
version. It is only updated when the maintainers explicitly *promote* a new
build, so it is the safest choice for everyday use.

Every commit to the development code is also built and published to its own
permanent address under ``https://web.edumips.org/c/<commit>/``. A small
coloured badge next to the **"Web Version"** label in the toolbar tells you
which kind of build you are on:

* **No badge** — you are on the normal production build. This is the stable
  version at https://web.edumips.org.
* **``ARCHIVED``** — you are viewing a specific per-commit build served from
  ``/c/<commit>/``. This may be a previously promoted version or a candidate
  awaiting promotion; either way it is not the live production site. Hovering
  over the badge shows a tooltip confirming this.
* **``PR #N``** (yellow badge) — a temporary preview built for a specific
  proposed change (pull request) on GitHub. Clicking the badge opens the
  corresponding pull request page.
* **``dev``** (blue badge) — someone is running a local development build
  directly on their machine.

In short: **no badge → stable production; ``ARCHIVED`` → a specific per-commit
build; ``PR #N`` / ``dev`` → temporary or preview builds.**

Browsing and switching versions
-------------------------------
All retained web builds — both the promoted releases and the pending
candidates — are listed in one place, addressed by their commit identifier.
Open the list from the **Help** dialog:

1. Click **Help** → **About** (in the web app toolbar).
2. The About tab shows two lists:

   * **Promoted versions** — the manually promoted releases, shown
     prominently. The live one is marked **current**.
   * **Candidate builds** — newer builds that have not yet been promoted. All
     pending candidates are shown (there are usually only a few).
3. Click any entry to open that exact build in a new tab — your current work is
   preserved in the original tab.

What is kept, and for how long
------------------------------
* **Promoted versions** are kept permanently as immutable snapshots, so you can
  always go back to any release.
* **Candidate builds** are kept until a promotion happens. When a build is
  promoted, the candidates that came *before* it (and were never promoted) are
  removed; candidates newer than the promoted build are preserved and stay
  available until they are themselves promoted or superseded.

This keeps the list focused: every promoted release plus the handful of builds
that are still waiting to be promoted.

