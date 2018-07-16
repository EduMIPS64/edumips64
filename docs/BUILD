package(default_visibility = ["//visibility:public"])

filegroup(
    name = "common_conf",
    srcs = ["user/common_conf.py"],
)

html_it_files = [
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
    # TODO: figure out if these are necessary.
    #"img/Back.png",
    #"img/Forward.png",
    #"img/Home.png",
    #"img/Print.png",
]

genrule(
    name = "html_it",
    srcs = [
        "//docs/user/it:src",
        "//docs/user/it:makefile",
        ":common_conf",
    ],
    outs = html_it_files,
    cmd = "\n".join([
        # Get the absolute path of the output directory as a destination.
        "DST=$$(readlink -f $(@D))",
        "cd $$(dirname $(location //docs/user/it:makefile))",
        # Remove -Q to debug Sphinx.
        "make html BUILDDIR=$$DST SPHINXOPTS='-N -a -E -Q'",
    ]),
)

filegroup(
    name = "html_it_files",
    srcs = html_it_files,
)
