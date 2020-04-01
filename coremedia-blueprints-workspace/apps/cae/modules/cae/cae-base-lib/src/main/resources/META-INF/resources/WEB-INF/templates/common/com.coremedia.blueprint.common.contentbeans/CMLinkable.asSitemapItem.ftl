<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMLinkable" -->

<#--
    Template Description:

    This template renders a linkable item as link.
-->

<li class="cm-sitemap__item">
  <#-- link to this item in navigation -->
  <a href="${cm.getLink(self)}">${self.title!""}</a>
</li>
