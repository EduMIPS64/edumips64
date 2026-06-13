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
version. It is only updated when the maintainers explicitly promote a new
release, so it is the safest choice for everyday use.

There are also other web builds, identified by a small coloured badge shown
next to the **"Web Version"** label in the toolbar:

* **No badge** — you are on the normal production build. This is the stable
  version at https://web.edumips.org.
* **``CANDIDATE``** (purple badge) — a build automatically deployed from every
  commit to the latest development code. Each candidate build is labeled with
  its date and sequence number (e.g., `2026-06-13 #2`). You can browse all
  available candidates and share their URLs from the **About** tab. Candidates
  may be unstable and are retained for 14 days. Hovering over the badge shows
  a tooltip confirming this.
* **``PR #N``** (yellow badge) — a temporary preview built for a specific
  proposed change (pull request) on GitHub. Clicking the badge opens the
  corresponding pull request page.
* **``dev``** (blue badge) — someone is running a local development build
  directly on their machine.

In short: **no badge → stable production; ``CANDIDATE`` → newest features but
possibly unstable; ``PR #N`` / ``dev`` → temporary or preview builds.**

Viewing previous versions
--------------------------
Every released web version is kept as an immutable snapshot and remains
available for you to use. You can see a list of all retained versions and open
an older one directly from the **Help** dialog:

1. Click **Help** → **About** (in the web app toolbar).
2. Look for the **Previous Versions** section in the About tab.
3. You will see a list of released versions with their dates.
4. The current version is marked with a label so you know which one is live.
5. Click any older version to open it in a new tab — your current work is
   preserved in the original tab.

This is useful if you need to compare versions, verify a bug in an older release,
or work with a version that suits your needs better.
