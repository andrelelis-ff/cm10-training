<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMSpinner" -->
<#-- @ftlvariable name="classBox" type="java.lang.String" -->
<#-- @ftlvariable name="classMedia" type="java.lang.String" -->
<#-- @ftlvariable name="metadata" type="java.util.List" -->
<#-- @ftlvariable name="metadataMedia" type="java.util.List" -->

<#assign classBox=cm.localParameters().classBox!""/>
<#assign classMedia=cm.localParameters().classMedia!""/>
<#assign metadata=cm.localParameters().metadata![]/>
<#assign metadataMedia=cm.localParameters().metadataMedia![]/>

<#assign sequence=self.sequence />
<#switch sequence?size>
  <#case 0>
  <#case 1>
    <@cm.include self=(sequence?first)!cm.UNDEFINED view="media" params={
      "classBox": classBox,
      "classMedia": classMedia,
      "metadata": metadata,
      "metadataMedia": metadataMedia
    } />
    <#break>
  <#default>
    <#-- spinner (with at least 2 images) -->
    <div class="${classBox}"<@preview.metadata data=metadata + [self.content]/>>
      <div class="${classMedia} cm-spinner"<@preview.metadata data=metadataMedia />>
        <div class="cm-spinner__wrapper">
          <ol class="cm-spinner__images"<@preview.metadata "properties.sequence"/>>
            <#list sequence as image>
              <li class="cm-spinner__image">
                <@cm.include self=image view="media" params={
                  "classBox": "cm-spinner__picture-box",
                  "classMedia": "cm-spinner__picture"
                }/>
              </li>
            </#list>
          </ol>
          <div class="cm-spinner__icon cm-spinner-button"></div>
        </div>
      </div>
    </div>
</#switch>
