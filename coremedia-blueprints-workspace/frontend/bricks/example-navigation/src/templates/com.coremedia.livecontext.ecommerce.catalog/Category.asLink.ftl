<#-- @ftlvariable name="self" type="com.coremedia.livecontext.ecommerce.catalog.Category" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#assign cssClass=cm.localParameters().cssClass!"" />

<@utils.optionalLink attr={"class" : cssClass} href="${cm.getLink(self)}">${(self.name)!""}</@utils.optionalLink>
