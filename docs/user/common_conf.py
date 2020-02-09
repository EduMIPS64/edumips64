# Common values for the Sphinx configuration for all languages.

# Get the version from build.gradle.kts
import re

regexp = re.compile(r'rootProject\.version\s*=\s*"(\d+\.\d+\.\d+)"')
with open('../../../../build.gradle.kts') as f:
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
templates_path = ['_templates']
source_suffix = '.rst'
master_doc = 'index'
exclude_patterns = ['_build']
pygments_style = 'sphinx'
html_theme = 'epub'
html_static_path = ['_static']
htmlhelp_basename = 'EduMIPS64doc'
latex_preamble = u'''
\DeclareUnicodeCharacter{22C3}{$\cup$}
\DeclareUnicodeCharacter{2208}{$\in$}
\DeclareUnicodeCharacter{221E}{$\infty$}
'''
