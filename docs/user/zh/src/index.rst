.. EduMIPS64 documentation master file, created by
   sphinx-quickstart on Tue Apr 26 23:10:10 2011.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

欢迎访问 EduMIPS64 文档！
=======================================

If the characters in the document are badly garbled, please set the environment variable JAVA_TOOL_OPTIONS to -Dfile.encoding=UTF8, or load the jar file with -Dfile.encoding=utf-8.

For more information on this bug, please see here: 

https://github.com/EduMIPS64/edumips64/issues/785

--------------------------------------

EduMIPS64 是 MIPS64 指令集架构 (ISA) 模拟器。它设计用于执行使用模拟器实现的 MIPS64 ISA 子集的小程序，允许用户查看指令在流水线中的行为、CPU 如何处理停滞、寄存器和内存的状态等。它既是一个模拟器，也是一个可视化调试器。

该项目的网站是 http://www.edumips.org，代码托管在 http://github.com/EduMIPS64/edumips64。如果发现任何错误，或有任何改进模拟器的建议，请在 github 上发布问题，或发送电子邮件至 bugs@edumips.org。

EduMIPS64 是由卡塔尼亚大学（意大利）的一群学生开发的，起初是 WinMIPS64 的克隆版，尽管现在这两个模拟器之间有很多不同之处。

本手册将向您介绍 EduMIPS64，并详细介绍如何使用它。

本手册第一章介绍了模拟器接受的源文件格式，除了命令行参数外，还描述了数据类型和指令。

第二章概述了 EduMIPS64 可接受的 MIPS64 指令集子集，以及所有需要的参数和使用说明。

第三章介绍浮点运算单元及其指令集。

第四章是 EduMIPS64 用户界面的说明，解释了每个框架和菜单的目的，以及配置对话框、Dinero 前端对话框、手册对话框和命令行选项的说明。

第五章包含一些有用的示例。

本手册介绍了 EduMIPS64 版本 |version|。

**译者注：中文文档使用了 DeepL 机翻，不能保证完全准确。如发现错误，请以英文文档为准，并在 GitHub 上提交修改 Pull Request。**


.. toctree::
   :maxdepth: 2

   source-files-format
   instructions
   fpu
   user-interface
   examples

.. Indices and tables
   ==================

   * :ref:`genindex`
   * :ref:`modindex`
   * :ref:`search`

