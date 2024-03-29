# -*- coding: utf-8 -*-
#
# EduMIPS64 documentation build configuration file, created by
# sphinx-quickstart on Tue Apr 26 23:10:10 2011.
#
# This file is execfile()d with the current directory set to its containing dir.
#
# Note that not all possible configuration values are present in this
# autogenerated file.
#
# All configuration values have a default; values that are commented out
# serve to show the default.
#
# Note: all the common configuration of the EduMIPS64 documentation is stored
# in docs/common_conf.py. This file just imports all the identifiers from that
# module and adds language-specific configuration items.

import sys, os
sys.path.append(os.path.abspath("../.."))
from common_conf import *

pdf_stylesheets = ['sphinx', 'kerning', 'a4', 'zh_CN.json', 'chinese']
pdf_font_path = [os.path.abspath(".\\docs\\zh\\src"),'C:\\Windows\\Fonts']
pdf_language = "zh_CN"
source_encoding = "utf-8"
language = "zh_CN"
copyright = u'2011, Andrea Spadaccini (and the EduMIPS64 development team)'
man_pages = [
    ('index', 'edumips64', u'EduMIPS64 Documentation',
     [u'Andrea Spadaccini (and the EduMIPS64 development team)'], 1)
]
latex_documents = [
  ('index', 'EduMIPS64.tex', u'EduMIPS64 Documentation',
   u'Andrea Spadaccini (and the EduMIPS64 development team)', 'manual'),
]
