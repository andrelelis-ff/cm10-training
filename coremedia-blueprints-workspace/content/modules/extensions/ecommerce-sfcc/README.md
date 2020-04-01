# Salesforce Commerce Cloud Integration

> CoreMedia Live Context 3 for Salesforce Commerce Cloud feature

CoreMedia LC3 for Salesforce Commerce Cloud integrates with _Salesforce Commerce Cloud_. Therefore, the Library was extended in order to offer direct access to the e-Commerce
content.

You can use the placeholder content types `Product Teaser`, `e-Marketing Spot`, `Augmented Category`, `Augmented Product` and `Augmented Page` to add e-Commerce content to the sites managed in CoreMedia LC3 for Salesforce Commerce Cloud. These content items hold a link to the corresponding e-Commerce content. Changes in the e-Commerce system are immediately visible in the CMS.

In addition, you can use _Salesforce Commerce Cloud_ segments in personalized content as described in _Chapter 7, Working with Personalized Content_ [263].

The following sections describe how you access e-Commerce content in Studio and
how you work with e-Commerce content types in order to include content from
your _Salesforce Commerce Cloud_ system into your site (Content-led scenario) or the
opposite direction (Commerce-led scenario or fragment approach).

## Accessing Commerce Content in Studio
This section describes how you access content from the commerce system in CoreMedia Studio.

### Catalog View
When you have selected a site with a connected e-Commerce system, you will see the e-Commerce content in the tree of the Library. You can use the standard Library functionality (see Section 2.4.6,“Library”[34]) to browse through the e-Commerce content, search for items or add products to a `Product Teaser` content type, for instance. Only the filters are not applicable.

In this view you can browse through the catalog as you can browse through the CMS content. You can even open a product as a read-only content in Studio with a double-click or create an `Augmented Category` (see Section “Adding eCommerce Category” [236].

### Searching for Product Variants
> TBD

You can search for product variants (SKUs) of a given product. Simply select the product and click _Search Product Variants_ in the context menu. The results are shown in the Library.

### Searching for Product Pictures
> TBD

When you have mapped pictures from the CoreMedia system to products and product variants in the _Business Manager_, then you can search for the pictures assigned to a given product. Simply select the product and click _Search Product Pictures_ in the context menu.

In the result, you will find the `Picture` items that links to the product.

### Opening the Business Manager GUI
> TBD

CoreMedia LC3 for Salesforce Commerce Cloud seamlessly integrates content from _Salesforce Commerce Cloud_ into CoreMedia Studio so that you can add e-Commerce content to your pages. If you want to edit items from the commerce system, you can directly open the _Business Manager_ from Studio
1. From the Favorite Bar select _Apps_ and from the menu select _SFCC Business Manager_ or
2. Right-click on a product in Studio and select _Open in SFCC Business Manager_ from the context menu.

A window with the _Business Manager_ opens up where you have to log in with your account.

## Adding Commerce Content to CMS Pages
> TBD

## Adding CMS Content to Your Shop
> TBD

If your CoreMedia LC3 site is connected with the commerce system, then you can add content from the CoreMedia system to specific locations of shop pages. There are the following different methods:
- Adding content via the CoreMedia Content Widget
  Allows you, to add content items to WebSphere pages.
- Adding content to a product via the CoreMedia Asset Widget
  Allows you, to add pictures, videos and downloads to product pages.
- Replacing images for products
Allows you, to replace the default product image the commerce system with images from CoreMedia.
- Adding editorial pages
Allows you, to add linked editorial pages to the shop. These pages do not appear in the navigation but can be reached via teasers.


### Adding Default Content for Categories and Product Detail Pages
> TBD

For all shop pages (category overview, product details and others) to which you have added the Content Widget and to which you have not assigned specific content (described in Section “Adding Category Specific Content” [246]), there must be default content that is shown instead. This content is taken from the category root or from the site root page.

All preparatory work (creation and configuring of default pages) will probably be done by a technical editor and does not need to be repeatedly executed in everyday life. Nevertheless, these standard pages can also be refilled on a daily basis. The default content can either be added to the site root of type _Augmented Page_ or to the category root page of type _Augmented Category_. In both cases, proceed as follows:
1. Open the catalog root or site root content item of your site.
2. Select an adequate layout in the _Content_ or _Product Content_ tab (for `Categories` or `Product Detail Pages`, respectively) and add the desired content to the placement that matches the one defined in the _CoreMedia Content Widget_.

The `Augmented Category` document contains two page layouts: the one in the _Content_ tab is applied to the Category Overview Page and the other in the _Product Content_ tab is used for all Product Detail Pages. Both layouts are taken from the root category. The layouts that are set there form the default layouts for a site. Hence, they should be the most commonly used layouts. If you want something different, you can choose another layout from the list.

### Adding Category Specific Content
You can add specific content to certain categories by augmenting the commerce `Category` with an `Augmented Category` item.

1. In the library, select the category that you want to augment. Already augmented categories have a different icon as shown in the screenshot (2).
2. Select _Augment Category_ from the context menu or click in the toolbar. The `Augmented Category` content item is created in the preferred site folder below `Navigation/augmentation`. The content item opens up and all placements are set to inherit from the parent category. If you had the category open in Studio, then the form will be replaced by the `Augmented Category`. Other users that have the category open will get a message, that the category has been augmented.
3. Add your content to the placement in the _Content_ tab, whose name corresponds to the setting of the _CoreMedia Content Widget_.

Now, the category overview page shows the defined content.

### Adding Product Specific Content
> TBD
