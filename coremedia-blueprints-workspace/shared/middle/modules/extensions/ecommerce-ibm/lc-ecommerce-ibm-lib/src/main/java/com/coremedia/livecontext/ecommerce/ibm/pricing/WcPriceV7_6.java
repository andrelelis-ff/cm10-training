package com.coremedia.livecontext.ecommerce.ibm.pricing;

import com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper;

import java.util.List;
import java.util.Map;

public class WcPriceV7_6 extends WcPrice {

  @Override
  public void setDataMap(Map<String, Object> data) {
    if (data != null) {
      dataMap = data;

      List priceArr = (List) DataMapHelper.getValueForPath(data, "CatalogEntryView.Price");
      if (priceArr != null && !priceArr.isEmpty()) {
        for (Object priceEntry : priceArr) {
          Map priceMap = (Map) priceEntry;
          if (priceMap.get("priceUsage").equals("Offer")) {
            setPriceDescription((String) priceMap.get("priceDescription"));
            setPriceUsage((String) priceMap.get("priceUsage"));
            setPriceValue((String) priceMap.get("priceValue"));
          }
        }
      }
    }
  }
}
