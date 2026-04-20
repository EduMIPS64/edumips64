# Common values for the Sphinx configuration for all languages.

import os

# Get the version from build.gradle.kts
import re

regexp = re.compile(r'version\s*=\s*(.*)')
with open('../../../../gradle.properties') as f:
    for line in f:
        if regexp.match(line):
            version = regexp.match(line).group(1)
            break

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
