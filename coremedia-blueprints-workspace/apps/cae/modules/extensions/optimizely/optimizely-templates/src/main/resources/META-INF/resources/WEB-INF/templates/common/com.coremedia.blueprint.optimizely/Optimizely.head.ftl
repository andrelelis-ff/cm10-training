<#-- @ftlvariable name="self" type="com.coremedia.blueprint.optimizely.Optimizely" -->

<#if self.enabled>
    <#-- Optimizely snippet must be the first JavaScript file loaded -->
    <script type="text/javascript" language="JavaScript" src="//cdn.optimizely.com/js/${self.optimizelyId}.js"></script>
</#if>