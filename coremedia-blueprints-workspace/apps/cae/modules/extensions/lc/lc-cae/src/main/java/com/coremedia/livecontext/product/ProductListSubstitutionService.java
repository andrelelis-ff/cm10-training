package com.coremedia.livecontext.product;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.context.LiveContextNavigation;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.navigation.LiveContextNavigationFactory;
import com.coremedia.objectserver.view.substitution.Substitution;
import com.google.common.base.Function;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.transform;
import static org.springframework.util.Assert.notNull;

/**
 *
 */
public class ProductListSubstitutionService {

  private static final Logger LOG = LoggerFactory.getLogger(ProductListSubstitutionService.class);

  private static final String PLACEHOLDER_ID = "productlist-placeholder";
  public static final int DEFAULT_STEPS = 20;

  private LiveContextNavigationFactory liveContextNavigationFactory;

  @Substitution(PLACEHOLDER_ID)
  @SuppressWarnings("unused")
  @Nullable
  public ProductList getProductList(@Nullable Page page, @Nullable HttpServletRequest request) {
    if (page == null) {
      return null;
    }

    Navigation navigation = page.getNavigation();

    if (!(navigation instanceof LiveContextNavigation)) {
      LOG.warn("The category substitution does only work for instances of type {} but here I have a {}",
              LiveContextNavigation.class, navigation.getClass());
      return null;
    }

    return getProductList((LiveContextNavigation) navigation, 0, DEFAULT_STEPS);
  }

  /**
   * Used by the REST handler to enable lazy loading/infinite scroll.
   *
   * @param navigation The navigation/category the products to retrieve for.
   * @param startIndex Paging startIndex
   * @param steps      Paging steps
   * @return The paged product category.
   */
  @Nullable
  public ProductList getProductList(@Nullable LiveContextNavigation navigation, int startIndex, int steps) {
    if (navigation == null) {
      return null;
    }

    try {
      StoreContext storeContext = CurrentStoreContext.get();
      CommerceConnection connection = storeContext.getConnection();

      Category category = navigation.getCategory();
      List<Product> list = connection.getCatalogService().findProductsByCategory(category);
      ProductList productList = new ProductList(navigation, startIndex, steps, list.size(),
              liveContextNavigationFactory);

      //apply the subset of products according to the passed parameters.
      List<Product> subList = sublist(list, startIndex, steps);
      List<ProductInSite> wrapped = transform(subList, new ProductListWrapper(navigation.getSite()));
      productList.setLoadedProducts(wrapped);
      return productList;
    } catch (CommerceException e) {
      throw new RuntimeException(e);
    }
  }

  private List<Product> sublist(List<Product> list, int startIndex, int steps) {
    if (list.size() > (startIndex + steps)) {
      return list.subList(startIndex, startIndex + steps);
    } else if (startIndex >= list.size()) {
      return Collections.emptyList();
    } else if (!list.isEmpty()) {
      return list.subList(startIndex, list.size());
    } else {
      return Collections.emptyList();
    }
  }

  private class ProductListWrapper implements Function<Product, ProductInSite> {
    private final Site site;

    public ProductListWrapper(Site site) {
      notNull(site);
      this.site = site;
    }

    @Nullable
    @Override
    public ProductInSite apply(@Nullable Product product) {
      return product == null ? null : liveContextNavigationFactory.createProductInSite(product, site);
    }
  }

  @Required
  public void setLiveContextNavigationFactory(LiveContextNavigationFactory liveContextNavigationFactory) {
    this.liveContextNavigationFactory = liveContextNavigationFactory;
  }
}
