#Imagga Connector for Feedback Hub

This extension implements the Feedback Hub's API to connect CoreMedia to the external systems `Imagga`, in order to provide keywords for selected content.

## Basic Setup

To activate this extension, you need to follow the documentation steps, described in the `Studio Development Manual`, under the section `Feedback Hub`. 

Assuming you have read the Feedback Hubs documentation and are familiar with the terminology of CoreMedia Feedback Hub, the next steps will shortly explain how to activate the `Imagga Adapter`:
 
- activate the `studio-server` extension with the extension tool
- provide a `settings document` to configure your `Imagga Adapter` as described in the documentation
- within your `Imagga` settings document you need to provide the value `imagga` for the key `factoryId` (The value must match the value, returned by the `ImaggaFeedbackHubAdapterFactory#getId` method)
- within your `Imagga` settings document you need to provide the following String values underneath the struct `settings`: 
    - `sourceBlobProperty`: the name of the blob property that stores a picture, that should be evaluated by Imagga
    - `basicAuthKey` : the authentication key, provided by Imagga after creating an account that allows using the API
    
    
#CoreMedia Labs

The software component within this subfolder of the Blueprint workspace is part of CoreMedia’s modularization and open source initiative. Note that in future releases it will be detached from this workspace and released independently.

As of CoreMedia Content Cloud v10 this component is released under the CoreMedia Open Source License (see LICENSE.txt). The open source license applies only to the software found in the subfolder of this file. Other software license agreements pertaining to CoreMedia Blueprints are not affected by this unless so stated in the README file of the specific subfolder.


