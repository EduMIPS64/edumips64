package(default_visibility = ["//visibility:public"])

filegroup(
    name = "javahelp",
    srcs = [
        "EduMIPS64.hs",
        "EduMIPS64Index.xml",
        "EduMIPS64TOC.xml",
        "Map.jhm",
    ] + glob(["JavaHelpSearch/*"]),
)

filegroup(
    name = "makefile",
    srcs = ["src/Makefile"],
)

filegroup(
    name = "images",
    srcs = [
        "img/Back.png",
        "img/Forward.png",
        "img/Home.png",
        "img/Print.png",
    ],
)

filegroup(
    name = "src",
    srcs = glob(["src/*.rst"]) + ["src/conf.py"],
)

html_files = [
    "html/index.html",
    "html/user-interface.html",
    "html/instructions.html",
    "html/fpu.html",
    "html/search.html",
    "html/source-files-format.html",
    "html/genindex.html",
    "html/examples.html",
    "html/_static/file.png",
    "html/_static/minus.png",
    "html/_static/plus.png",
]

genrule(
    name = "html",
    srcs = [
        ":src",
        ":makefile",
        "//docs/user:common_conf",
    ],
    outs = html_files,
    cmd = "\n".join([
        # Get the absolute path of the output directory as a destination.
        "DST=$$(readlink -f $(@D))",
        "cd $$(dirname $(location :makefile))",
        # Remove -Q to debug Sphinx.
        "make html BUILDDIR=$$DST SPHINXOPTS='-N -a -E -Q'",
    ]),
)

genrule(
    name = "pdf",
    srcs = [
        ":src",
        ":makefile",
        "//docs/user:common_conf",
    ],
    outs = ["EduMIPS64.pdf"],
    cmd = "\n".join([
        # Get the absolute path of the output directory as a destination.
        "DST=$$(readlink -f $(@D))",
        "echo $$DST",
        "cd $$(dirname $(location :makefile))",
        # Remove -Q to debug Sphinx.
        "make latexpdf BUILDDIR=$$DST SPHINXOPTS='-N -a -E -Q'",
        "cp $$DST/latex/EduMIPS64.pdf $$DST/EduMIPS64.pdf"
    ]),
)

filegroup(
    name = "html_files",
    srcs = html_files,
)
