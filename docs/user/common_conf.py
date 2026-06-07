# Common values for the Sphinx configuration for all languages.

import os
import re
import subprocess

# ---------------------------------------------------------------------------
# Build identity — layered fallback so any build environment works:
#   1. EDUMIPS64_BUILD_VERSION env var (explicit override)
#   2. git describe --tags --match v* --always --dirty (repo root from __file__)
#   3. READTHEDOCS_GIT_COMMIT_HASH (shortened, for RTD builds without git)
#   4. gradle.properties version= (resolved from __file__, not cwd)
#   5. "unknown"
# ---------------------------------------------------------------------------

_version = None

# 1. Env override.
if not _version:
    _version = os.environ.get('EDUMIPS64_BUILD_VERSION') or None

# 2. git describe, using repo root resolved from __file__ (not cwd).
if not _version:
    try:
        _conf_dir = os.path.dirname(os.path.abspath(__file__))       # docs/user/
        _repo_root = os.path.normpath(os.path.join(_conf_dir, '..', '..'))
        _out = subprocess.check_output(
            ['git', '-C', _repo_root, 'describe', '--tags',
             '--match', 'v*', '--always', '--dirty'],
            stderr=subprocess.DEVNULL,
        ).decode().strip()
        if _out:
            _version = _out.lstrip('v')
    except (OSError, subprocess.SubprocessError):
        # git may be absent or this may not be a git checkout (e.g. a source
        # tarball); intentionally fall through to the next fallback.
        pass

# 3. Read the Docs git commit hash (shortened to 8 chars).
if not _version:
    _rtd_sha = os.environ.get('READTHEDOCS_GIT_COMMIT_HASH') or ''
    if _rtd_sha:
        _version = _rtd_sha[:8]

# 4. gradle.properties, resolved relative to __file__.
if not _version:
    try:
        _conf_dir = os.path.dirname(os.path.abspath(__file__))
        _gradle_props = os.path.normpath(
            os.path.join(_conf_dir, '..', '..', 'gradle.properties')
        )
        _re = re.compile(r'^version\s*=\s*(.+)')
        with open(_gradle_props) as _f:
            for _line in _f:
                _m = _re.match(_line)
                if _m:
                    _version = _m.group(1).strip().strip('"').strip("'")
                    break
    except (OSError, UnicodeDecodeError):
        # gradle.properties may be missing or unreadable in some build
        # environments; intentionally fall through to the "unknown" fallback.
        pass

# 5. Last resort.
if not _version:
    _version = 'unknown'

version = _version

release = version
project = u'EduMIPS64'

# rst2pdf config
extensions = ['sphinx.ext.autodoc','rst2pdf.pdfbuilder']
pdf_documents = [
 ('index', project, project, u'Andrea Spadaccini and the EduMIPS64 development team'),
]

# Other variables
# Shared templates live in docs/user/_templates (one level above each
# language's conf.py); use an absolute path so that all language builds
# pick them up regardless of the working directory.
templates_path = [os.path.join(os.path.dirname(os.path.abspath(__file__)), '_templates')]
source_suffix = '.rst'
master_doc = 'index'
exclude_patterns = ['_build']
pygments_style = 'sphinx'
html_theme = 'epub'
html_static_path = ['_static']
htmlhelp_basename = 'EduMIPS64doc'
html_context = {
    'html5_doctype': False,
    'use_meta_charset': False,
}
latex_preamble = u'''
\DeclareUnicodeCharacter{22C3}{$\cup$}
\DeclareUnicodeCharacter{2208}{$\in$}
\DeclareUnicodeCharacter{221E}{$\infty$}
'''
