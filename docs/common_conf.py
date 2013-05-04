# Common values for the Sphinx configuration for all languages.

# Import version from build.xml (hacky)
from xml.dom.minidom import parse
dom = parse('../../../build.xml')
init_node = [x for x in dom.getElementsByTagName('target') if x.getAttribute('name') == u'init'][0]
version_node = [x for x in init_node.childNodes if x.nodeName == u'property' and x.getAttribute('name') == u'version'][0]
version = version_node.getAttribute('value')
release = version

# Other variables
project = u'EduMIPS64'
extensions = []
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
