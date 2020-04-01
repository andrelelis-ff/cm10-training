<#--
  Renders a given mapping of attributes and their values to be used in a html-tag.
  If the value of an attribute is cm.UNDEFINED it will be omitted.
  The rendered output will always have a leading space and all attributes are rendered in a single line.

  @param attr contains a mapping of attribute names to their corresponding values.

  Example:
  <#assign attr={"style": "display: none;", "id": "exampleId"}/>
  <div class="example"<@utils.optionalAttributes attr/>></div>
-->
<#macro optionalAttributes attr>
  ${""?left_pad(1)}<@compress single_line=true>
  <#list attr?keys as name>
    <#local value=(attr[name]!"")?string />
    <#if !cm.isUndefined(value)>
      ${name}="${value}"
    </#if>
  </#list>
  </@compress>
</#macro>

<#--
  Renders a given mapping of attributes and their values to be used in a html-tag.
  The rendered output will always have a leading space and all attributes are rendered in a single line.

  @param attr contains a mapping of attribute names to their corresponding values.
  @param ignore contains a list of attribute names to ignore (e.g. when passing attr from a different source and some
                attributes should not be written)

  Example:
  <#if self.alt?has_content>
    <#assign alt=self.alt />
  </#if>
  <#assign attributes += {"alt": alt, "src": imageLink}/>
  <img src="#" class="cm-image" <@renderAttr attributes/>>
-->
<#macro renderAttr attr={} ignore=[]>
  ${""?left_pad(1)}<@compress single_line=true>
  <#if attr?keys?seq_contains("classes") && !ignore?seq_contains("classes")>
    <#local classes=attr["classes"] />
    <#if attr?keys?seq_contains("class")>
      <#local classes=classes + attr["class"]?replace("  ", " ")?split(" ") />
    </#if>
    <#local attr=attr + {"class": classes?join(" ")} />
  </#if>
  <#local ignore=ignore + ["classes"] />
  <#list attr?keys as key>
    <#if !ignore?seq_contains(key)>
      <#local value=attr[key]/>
      <#if (key=="metadata" || key=="data-cm-metadata")>
        <@preview.metadata data=value />
      <#elseif key?contains("data-")>
        <@cm.dataAttribute name=key data=value/>
      <#elseif !cm.isUndefined(value)>
        ${key}="${value?string!""}"
      </#if>
    </#if>
  </#list>
  </@compress>
</#macro>

<#--
  Wraps a given content with a tag name and attributes if a condition results to "true". If the condition is "false"
  the tag gets omitted and only the nested content gets rendered.

  @param condition a boolean value which defines if the content is wrapped with a tag or not.
  @param tagName the name of the tag as a string, e.g. "a", "div", ...
  @param attr contains a mapping of attribute names to their corresponding values which are attached to the tag.

  Example:
  <@optionalTag condition=calculateIfNeeded() tagName="div" attr={ "class": "wrapper" }>
    <img src="hello.jpg"/>
  </@optionalTag>
-->
<#macro optionalTag condition tagName="div" attr={}>
  <#if condition><${tagName} <@renderAttr attr />></#if>
    <#nested>
  <#if condition></${tagName}></#if>
</#macro>

<#--
  Wraps the given nested content in a link tag with a given href and other additional attributes. The link can be opened
  in a new tab (optionally). If the given href is empty only the nested content will be rendered.

  @param href contains a string representing the href of the link tag
  @param openInNewTab a boolean value which defines if the link should be opened in a new tab
  @param attr contains a mapping of attribute names to their corresponding values which are attached to the link.

  Example:
  <@utils.optionalLink href=calculateLink() openInNewTab=true attr={ "class": "link" }>
    <img src="hello.jpg"/>
  </@utils.optionalLink>
-->
<#macro optionalLink href openInNewTab=false attr={}>
  <#local target=openInNewTab?then("_blank", cm.UNDEFINED) />
  <#local rel=openInNewTab?then("noopener noreferrer", cm.UNDEFINED) />
  <@optionalTag condition=href?has_content tagName="a" attr={
    "href": href,
    "target": target,
    "rel": rel
  } + attr>
    <#nested>
  </@optionalTag>
</#macro>

<#--
  Renders a time-tag and converts a date to a localized String.

  @param date the date to render
  @param cssClass a CSS class to attach to the time-tag
  @param metadata preview metadata

  Example:
  <@renderDate date=self.externallyDisplayedDate.time
               cssClass="cm-detail__time"
               metadata=["properties.externallyDisplayedDate"] />
-->
<#macro renderDate date cssClass="" metadata=[]>
  <#if date?has_content>
    <time class="${cssClass}" datetime="${date?datetime?string.iso}"<@preview.metadata data=metadata />>${date?date?string.medium}</time>
  </#if>
</#macro>


<#--
  Renders given text with line breaks as <br>

  @param text The text as String.

  Example:
  <#if teaserText?has_content>
    <p>
      <@renderWithLineBreaks teaserText/>
    </p>
  </#if>
-->
<#macro renderWithLineBreaks text>
  <#noautoesc>
    ${text?trim?replace("\n\n", "<br>")?replace("\n", "<br>")}
  </#noautoesc>
</#macro>

<#--
  Extends a sequence entry of a given map by the provided items.

  @param map the map that contains a sequence located under a given key
  @param key the key the sequence is located in the given map
  @param extendBy the items to add to the sequence located in the given map
  @return a new map containing all old values and the extended sequence
-->
<#function extendSequenceInMap map={} key="" extendBy=[]>
  <#local newSequence=extendBy />
  <#if map?keys?seq_contains(key) && map[key]?is_sequence>
    <#local newSequence=map[key] + extendBy />
  </#if>
  <#return map + {key: newSequence} />
</#function>
