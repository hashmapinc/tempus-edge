# -- Project information -----------------------------------------------------
project = 'Tempus Edge'
copyright = '2018, Hashmap, Inc'
author = 'Hashmap, Inc'
version = '0.1'
release = '0.1.0'

# -- General configuration ---------------------------------------------------

extensions = [
    'sphinx.ext.intersphinx',
]
templates_path = ['_templates']
source_suffix = '.rst'
master_doc = 'index'
language = None
exclude_patterns = []
pygments_style = 'sphinx'


# -- Options for HTML output -------------------------------------------------
html_favicon = './_images/favicon.png'
html_theme = 'sphinx_rtd_theme'
html_static_path = ['_static']
htmlhelp_basename = 'TempusEdgedoc'

html_context = {
    'css_files': [
        '_static/theme_overrides.css',  # override wide tables in RTD theme
    ],
}

def setup(app):
    app.add_stylesheet('theme_overrides.css')

# -- Options for LaTeX output ------------------------------------------------
latex_elements = {}

latex_documents = [
    (master_doc, 'TempusEdge.tex', 'Tempus Edge Documentation',
     'Randy Pitcher', 'manual'),
]


# -- Options for manual page output ------------------------------------------
man_pages = [
    (master_doc, 'tempusedge', 'Tempus Edge Documentation',
     [author], 1)
]

texinfo_documents = [
    (master_doc, 'TempusEdge', 'Tempus Edge Documentation',
     author, 'TempusEdge', 'One line description of project.',
     'Miscellaneous'),
]

# -- Options for epub output ------------------------------------------
epub_title = project
epub_author = author
epub_publisher = author
epub_copyright = copyright
epub_exclude_files = ['search.html']
intersphinx_mapping = {'https://docs.python.org/': None}
