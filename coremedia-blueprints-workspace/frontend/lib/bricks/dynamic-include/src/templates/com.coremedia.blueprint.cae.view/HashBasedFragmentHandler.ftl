<#-- @ftlvariable name="self" type="com.coremedia.blueprint.cae.view.HashBasedFragmentHandler" -->

<#assign baseData={
  "baseUrl": cm.getLink(self.delegate, self.view),
  "validParameters": self.validParameters,
  "modifiedParametersHeaderPrefix": self.modifiedParametersHeaderPrefix
} />

<div <@cm.dataAttribute name="data-hash-based-fragment-handler" data=baseData />>
</div>
