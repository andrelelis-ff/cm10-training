<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->

<#--
    Template Description:

    This partial template overwrites the default Page._favicon.ftl from the page-brick to add more favicon variants in
    this theme based on the tool https://realfavicongenerator.net

    @since 1907
-->

<link rel="apple-touch-icon" sizes="180x180" href="${bp.getLinkToThemeResource('img/apple-touch-icon.png')}">
<link rel="icon" type="image/png" sizes="32x32" href="${bp.getLinkToThemeResource('img/favicon-32x32.png')}">
<link rel="icon" type="image/png" sizes="16x16" href="${bp.getLinkToThemeResource('img/favicon-16x16.png')}">
<link rel="mask-icon" href="${bp.getLinkToThemeResource('img/favicon-safari-pinned-tab.svg')}" color="#672779">
<link rel="shortcut icon" href="${bp.getLinkToThemeResource('img/favicon.ico')}" />
<meta name="msapplication-TileColor" content="#672779">
<meta name="msapplication-TileImage" content="${bp.getLinkToThemeResource('img/favicon-mstile-144x144.png')}">
<meta name="theme-color" content="#672779">
