package com.coremedia.ecommerce.studio.model {
public class ProductPropertyNames {

  /**
   * @eventType shortDescription
   * @see Product#getShortDescription()
   */
  public static const SHORT_DESC:String = 'shortDescription';

  /**
   * @eventType offerPrice
   * @see Product#getOfferPrice()
   */
  public static const OFFER_PRICE:String = 'offerPrice';

  /**
   * @eventType listPrice
   * @see Product#getListPrice()
   */
  public static const LIST_PRICE:String = 'listPrice';

  /**
   * @eventType currency
   * @see Product#getCurrency()
   */
  public static const CURRENCY:String = 'currency';

  /**
   * @eventType variants
   * @see Product#getVariants()
   */
  public static const VARIANTS:String = 'variants';

  /**
   * @eventType definingAttributes
   * @see ProductVariant#getDefiningAttributes
   */
  public static const DEFINING_ATTRIBUTES:String = 'definingAttributes';

  /**
   * @eventType definingAttributes
   * @see ProductVariant#getDescribingAttributes
   */
  public static const DESCRIBING_ATTRIBUTES:String = 'describingAttributes';


  /**
   * @private
   * This class only defines constants.
   */
  public function ProductPropertyNames() {
  }

}
}
