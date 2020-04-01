SCSS code for the blueprint-studio-theme

This folder contains SASS files of various kinds, organized in sub-folders:

    /etc        Custom SASS files that do not rely on the ExtJS micro loader.
                The SASS files will be including via import statements in imports.scss.
    
    /mixins     Component specific SASS Mixins that will be loaded via the ExtJS micro loader.
    
    /src        Component specific CSS generating SCSS code that will be loaded via the ExtJS micro
                loader.
    
    /var        Component specific SCSS variables used in mixins and CSS generating SCSS code that
                will be loaded via the ExtJS micro loader.
