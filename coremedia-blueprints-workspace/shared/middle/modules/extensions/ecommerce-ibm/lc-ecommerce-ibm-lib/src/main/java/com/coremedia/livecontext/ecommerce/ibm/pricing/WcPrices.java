package com.coremedia.livecontext.ecommerce.ibm.pricing;

import com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;

public class WcPrices {

  protected Map<String, WcPrice> prices = emptyMap();
  protected Map<String, Object> dataMap = emptyMap();

  public void setDataMap(@NonNull Map<String, Object> data) {
    dataMap = data;

    List priceArr = (List) DataMapHelper.getValueForPath(data, "CatalogEntryView.Price");
    if (priceArr != null && !priceArr.isEmpty()) {
      prices = new HashMap<>();
      for (Object priceEntry : priceArr) {
        Map priceMap = (Map) priceEntry;
        WcPrice wcPrice = new WcPrice();
        wcPrice.setPriceDescription((String) priceMap.get("priceDescription"));
        wcPrice.setPriceUsage((String) priceMap.get("priceUsage"));
        wcPrice.setPriceValue((String) priceMap.get("priceValue"));
        prices.put((String) priceMap.get("priceUsage"), wcPrice);
      }
    } else {
      priceArr = (List) DataMapHelper.getValueForPath(data, "catalogEntryView.price");
      if (priceArr == null) {
        priceArr = (List) DataMapHelper.getValueForPath(data, "price");
      }
      if (priceArr != null && !priceArr.isEmpty()) {
        prices = new HashMap<>();
        for (Object priceEntry : priceArr) {
          Map priceMap = (Map) priceEntry;
          //{currency=EUR, description=L, usage=Display, value=}
          WcPrice wcPrice = new WcPrice();
          wcPrice.setPriceDescription((String) priceMap.get("description"));
          wcPrice.setPriceUsage((String) priceMap.get("usage"));
          wcPrice.setPriceValue((String) priceMap.get("value"));
          wcPrice.setCurrency((String) priceMap.get("currency"));
          prices.put((String) priceMap.get("usage"), wcPrice);
        }
      }
    }
  }

  public Map<String, WcPrice> getPrices() {
    return prices;
  }
}
