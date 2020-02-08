# Common values for the Sphinx configuration for all languages.

# Get the version from Gradle properties.
properties = {}
with open('../../../../gradle.properties') as f:
    for line in f:
        if "=" not in line:
            continue
        key, value = line.split("=")
        properties[key] = value

release = properties["version"]
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
