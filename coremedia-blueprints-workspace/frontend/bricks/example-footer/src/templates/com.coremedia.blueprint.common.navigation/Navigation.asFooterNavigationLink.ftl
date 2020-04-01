<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.navigation.Navigation" -->

<#assign link=cm.getLink(self!cm.UNDEFINED) />
<a href="${link}" class="cm-footer-navigation-column__link">${self.title!""}</a>

